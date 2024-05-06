package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import de.sub.goobi.helper.StorageProvider;
import ugh.exceptions.UGHException;

public class ConnectMMtoTEI {

    protected static final Logger logger = Logger.getLogger(ConnectMMtoTEI.class);

    //config strings
    private static String confXslFile = "xslFile";
    private static String confTeiFolder = "teiFolder";
    private static String confEchoFolder = "echoFolder";
    private static String confRulesetPath = "rulesetPath";

    private XMLConfiguration config;
    private ImageCorresp corresp;

    public ConnectMMtoTEI(XMLConfiguration config) throws ConfigurationException, UGHException {

        this.config = config;
        corresp = new ImageCorresp(config);
    }

    public File findEcho(String strID) {

        File folderXML = new File(config.getString(confEchoFolder));
        File[] lstFiles = folderXML.listFiles();
        Arrays.sort(lstFiles);
        SAXBuilder builder = new SAXBuilder();

        for (File xmlFile : lstFiles) {

            String strFilename = xmlFile.getName();
            if (xmlFile.isFile() && FilenameUtils.getExtension(strFilename).equalsIgnoreCase("xml")) {

                try {
                    Document document = (Document) builder.build(xmlFile);
                    Element rootNode = document.getRootElement();

                    Element eltMeta = getChild(rootNode, "metadata");
                    Element eltID = getChild(eltMeta, "identifier");

                    String strIdEcho = eltID.getValue();

                    if (strIdEcho.toUpperCase().contains(strID.toUpperCase())) {
                        return xmlFile;
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    System.out.println(xmlFile.getName() + " - " + e.toString());
                }
            }
        }

        //otherwise
        return null;
    }

    public File convertToTEI(File fileEcho) throws IOException {

        String strTeiFilename = FilenameUtils.getBaseName(fileEcho.getName()) + "_TEI.xml";
        
        Path pathTei = Paths.get(config.getString(confTeiFolder) + strTeiFilename);
        File fileXLS =new File(config.getString(confXslFile));

        XSLTrans.makeTEI(fileXLS, fileEcho, pathTei);

        return pathTei.toFile();
        
        }

    private Element getChild(Element eltParent, String strChildName) {

        Element eltChild = null;
        List<Element> elts = eltParent.getChildren(strChildName);

        if (!elts.isEmpty()) {
            eltChild = elts.get(0);
        } else {
            for (Element element : eltParent.getChildren()) {
                if (element.getName().contentEquals(strChildName)) {
                    eltChild = element;
                    break;
                }
            }
        }

        return eltChild;
    }

    public Document convertPageLinks(File fileTEI, String documentId) throws ConfigurationException, UGHException {

        Document teiConverted = corresp.convertTeiLinks(fileTEI, documentId);
        return teiConverted;
    }

    
    /**
     * Find any pb elts within <s> elts, and move them outside
     * @param document
     * @return
     * @throws UGHException 
     * @throws ConfigurationException 
     */
    public Document cleanup(Document document) throws ConfigurationException, UGHException {

        Document teiConverted = corresp.movePBs(document);
        return teiConverted;

    }

}
