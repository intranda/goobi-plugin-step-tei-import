package de.intranda.goobi.plugins;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.sub.goobi.helper.StorageProvider;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;

/**
 * Entry point for testing
 *
 */
public class App {
    
    //Testing
    public static void main(String[] args) throws ConfigurationException, PreferencesException {
        String strConfigFile = "/home/joel/git/MPI-TEI/plugin/plugin_intranda_step_tei_import.xml";
        String strMMFile = "/home/joel/git/MPI-TEI/plugin/metadata/184/meta.xml";
        String strSourceFolder = "/home/joel/git/MPI-TEI/plugin/metadata/184/images/868393576_source";

        String strTEI = "/home/joel/git/MPI-TEI/test1/Vitruvius_1757.xml";
        
        TeiImportPlugin plugin = new TeiImportPlugin();
        plugin.strTestMmFile = strMMFile;
        plugin.strTestSource = strSourceFolder;
        XMLConfiguration xmlConfig = new XMLConfiguration(strConfigFile);
        SubnodeConfiguration subConfig = xmlConfig.configurationAt("config");
        plugin.config = new XMLConfiguration(subConfig);
        plugin.prefs = new Prefs();
        plugin.prefs.loadPrefs(plugin.config.getString("rulesetPath"));
        
        try {
            cleanup(strTEI, strConfigFile);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
        
//        Boolean boOk = plugin.execute();
    }

    private static void cleanup(String fileTEI, String strConfigFile) throws Exception {
        ImageCorresp corresp = new ImageCorresp(new XMLConfiguration(strConfigFile));
        
        SAXBuilder builder = new SAXBuilder();
        Document    document = (Document) builder.build(fileTEI);
        
        Document teiConverted = corresp.movePBs(document);
        saveTeiFile(teiConverted, fileTEI.replace(".xml", "_new.xml"));
        
    }
    
    private static void saveTeiFile(Document teiConverted, String strFilenameNew) throws IOException {

        XMLOutputter xmlOutput = new XMLOutputter();

        // display nice nice
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(teiConverted, new FileWriter(strFilenameNew));
        
    }
}
