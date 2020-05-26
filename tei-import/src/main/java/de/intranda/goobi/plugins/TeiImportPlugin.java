package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.goobi.beans.Step;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Document;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
//import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.extern.log4j.Log4j;

@PluginImplementation
@Log4j
public class TeiImportPlugin implements IStepPlugin, IPlugin {

    public String strTestMmFile = "";
    public String strTestSource = "";
    public XMLConfiguration config;

    private static final String PLUGIN_NAME = "intranda_step_tei_import";

    private Process process;
    private Step step;
    private String returnPath;
    public Prefs prefs;

    //    private SubnodeConfiguration config;
    private ConnectMMtoTEI connector;

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    public String getDescription() {
        return PLUGIN_NAME;
    }

    @Override
    public void initialize(Step step1, String returnPath) {

        this.returnPath = returnPath;
        this.step = step1;

        String projectName = step.getProzess().getProjekt().getTitel();

        XMLConfiguration xmlConfig = ConfigPlugins.getPluginConfig(PLUGIN_NAME);
        xmlConfig.setExpressionEngine(new XPathExpressionEngine());
        xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());

        SubnodeConfiguration myconfig = null;

        // order of configuration is:
        // 1.) project name and step name matches
        // 2.) step name matches and project is *
        // 3.) project name matches and step name is *
        // 4.) project name and step name are *
        try {
            myconfig = xmlConfig.configurationAt("//config[./project = '" + projectName + "'][./step = '" + step.getTitel() + "']");
        } catch (IllegalArgumentException e) {
            try {
                myconfig = xmlConfig.configurationAt("//config[./project = '*'][./step = '" + step.getTitel() + "']");
            } catch (IllegalArgumentException e1) {
                try {
                    myconfig = xmlConfig.configurationAt("//config[./project = '" + projectName + "'][./step = '*']");
                } catch (IllegalArgumentException e2) {
                    myconfig = xmlConfig.configurationAt("//config[./project = '*'][./step = '*']");
                }
            }
        }

        this.config = new XMLConfiguration(myconfig);

        this.returnPath = returnPath;
        this.process = step.getProzess();
        prefs = process.getRegelsatz().getPreferences();
        //        this.urn = prefs.getMetadataTypeByName("_urn");
        //        String strPEMFile = config.getString("PEMFile", PEM_FILE);
    }

    @Override
    public boolean execute() {

        try {
            //read the metadata file
            DigitalDocument dd = getDD();
            DocStruct logical = dd.getLogicalDocStruct();
            //            DocStruct physical = digitalDocument.getPhysicalDocStruct();

            //get the MPIWG ID:
            String strId = getMPIWGId(logical);

            //none? then quit
            if (strId == null) {
                return false;
            }

            //Otherwise initialize the machinery:
            this.connector = new ConnectMMtoTEI(config);

            //find any corresponding ECHO file
            File fileEcho = connector.findEcho(strId);

            //convert it to TEI
            File fileTEI = connector.convertToTEI(fileEcho);

            //change page links:
            Document teiConverted = connector.convertPageLinks(fileTEI, getCatalogId(logical));

            //move the TEI file to subfolder of goobi folder: 
            //to metadata/Process_no/images/Process_id_source/
            String strFolder = getSourceFolder(logical);

            //make sure the folder exists:
            File folder = new File(strFolder);
            folder.mkdir();

            if (teiConverted == null) {
                moveTeiFile(fileTEI, strFolder);
            } else {
                String strFilenameNew = FilenameUtils.concat(strFolder, "tei.xml");
                saveTeiFile(teiConverted, strFilenameNew);
            }

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    private DigitalDocument getDD()
            throws ReadException, PreferencesException, WriteException, IOException, InterruptedException, SwapException, DAOException {

        //for testing:
        if (this.process == null) {
            MetsMods mm = new MetsMods(prefs);
            mm.read(strTestMmFile);

            return mm.getDigitalDocument();
        }

        Fileformat fileformat = process.readMetadataFile();

        return fileformat.getDigitalDocument();
    }

    private void moveTeiFile(File fileTEI, String strFolder) throws IOException {

        String strFilenameNew = FilenameUtils.concat(strFolder, fileTEI.getName());
        File fileDestination = new File(strFilenameNew);
        FileUtils.copyFile(fileTEI, fileDestination);
    }

    private void saveTeiFile(Document teiConverted, String strFilenameNew) throws IOException {

        XMLOutputter xmlOutput = new XMLOutputter();

        // display nice nice
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(teiConverted, new FileWriter(strFilenameNew));
    }

    /**
     * Get the MPIWGID from the current matadatra file. if any
     * 
     * @param logical
     * @return
     */
    private String getMPIWGId(DocStruct logical) {

        List<Metadata> lstMetadata = logical.getAllMetadata();

        if (lstMetadata != null) {
            for (Metadata metadata : lstMetadata) {
                if (metadata.getType().getName().equals("MPIWGID")) {
                    return cleanID(metadata.getValue());
                }
            }
        }

        //otherwise:
        return null;
    }

    /**
     * Given a string id like "ECHO:5PPYB69C.xml" or "MPIWG:XVCX1Y20" just return the ID
     * 
     * @param strOrigId
     * @return
     */
    private String cleanID(String strOrigId) {

        String strId = strOrigId.replace("ECHO:", "");
        strId = strId.replace("MPIWG:", "");
        strId = strId.replace(".xml", "");

        return strId;
    }

    private String getSourceFolder(DocStruct logical) throws IOException, InterruptedException, SwapException, DAOException {

        //for testing:
        if (this.process == null) {
            return strTestSource;
        }

        String strImagesDir = this.process.getImagesDirectory();
        String strIdNumber = getCatalogId(logical);
        //        int iProcessId = this.process.getId();

        String strSource = FilenameUtils.concat(strImagesDir, strIdNumber + "_source/");
        return strSource;
    }

    /**
     * Get the MPIWGID from the current matadatra file. if any
     * 
     * @param logical
     * @return
     */
    private String getCatalogId(DocStruct logical) {

        List<Metadata> lstMetadata = logical.getAllMetadata();

        if (lstMetadata != null) {
            for (Metadata metadata : lstMetadata) {
                if (metadata.getType().getName().equals("CatalogIDDigital")) {
                    return cleanID(metadata.getValue());
                }
            }
        }

        //otherwise:
        return null;
    }

    @Override
    public String cancel() {
        return returnPath;
    }

    @Override
    public String finish() {
        return returnPath;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public Step getStep() {
        return null;
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.NONE;
    }

    @Override
    public String getPagePath() {
        return null;
    }

}
