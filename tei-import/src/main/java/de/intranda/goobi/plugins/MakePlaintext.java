package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class MakePlaintext {

    protected static final Logger logger = Logger.getLogger(MakePlaintext.class);

    private static String strPageCut = "QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ";
    
    public static void main(String[] args) throws Exception {

        //      String in = "-c /home/joel/git/MPI-TEI/test/tei_import_config.xml --moveTEI";
        //      String[] in = {"-c", "/home/joel/git/MPI-TEI/test/tei_import_config.xml", "--makeTEI"};

        String strInput = "/home/joel/git/MPI-TEI/test_xsl/Angeli_TEI.xml";
        String strOutput = "/home/joel/git/MPI-TEI/test_xsl/Angeli.txt";

        MakePlaintext makeText = new MakePlaintext();
        List<String> lstPages = makeText.splitIntoPages(new File(strInput));

        try (PrintWriter out = new PrintWriter(strOutput)) {

            for (int i = 0; i < lstPages.size(); i++) {
                
                out.println("##########################################################");
                out.println("Page " + i);
                
                out.println(lstPages.get(i));
            }
        }
    }

    public MakePlaintext() {
    }

    /**
     * For each file link in the TEI file, replace it with a link to the same image in the viewer.
     * 
     * @param fileTEI
     * @param dd
     * @return
     */
    public List<String> splitIntoPages(File fileTEI) {

        SAXBuilder builder = new SAXBuilder();
        Document document;
        try {
            document = (Document) builder.build(fileTEI);
        } catch (JDOMException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        Element rootNode = document.getRootElement();

        //Collect page numbers 
        Element eltFacs = getElement(rootNode, "facsimile");
        ArrayList<String> lstIds = new ArrayList<String>();

        if (eltFacs != null) {
            List<Element> lstFacs = eltFacs.getChildren();

            for (Element elt : lstFacs) {

                for (Element eltChild : elt.getChildren()) {

                    if (eltChild.getName().contentEquals("surface")) {
                        for (Attribute attC : eltChild.getAttributes()) {
                            if (attC.getName().contentEquals("id")) {

                                lstIds.add(attC.getValue());
                                break;
                            }
                        }
                    }
                }
            }
        }

        //now go through document, removing all xml except for <pb facs="0008"/>
        String strTotal = new String();

        Element eltText = getElement(rootNode, "text");
        List<Element> lstChildren = eltText.getChildren();

        //go through the children recursively:
        for (Element element : lstChildren) {

            strTotal = strTotal + getTextWithImages(element) + System.lineSeparator();
        }

        //now go through document, splitting off at each <pb facs="0008"/>
        ArrayList<String> lstPages = splitTextAtImages(strTotal);

        return lstPages;
    }

    /**
     * Go through the element, removing all metadata apart from <pb facs="0008"/>
     * 
     * @param element
     * @return
     */
    private String getTextWithImages(Element elt) {

        if (elt.getName().contentEquals("pb")) {
//            return elt.toString();
            return strPageCut;
        }

        String strText = "";
        List<Element> lstChildren = elt.getChildren();

        //go through the children recursively:
        for (Element child : lstChildren) {

            strText = strText + getTextWithImages(child);
        }

        strText = strText + elt.getText();

        return strText;
    }

    /**
     * Split into list at each <pb facs="0008"/>
     * 
     * @param strTotal
     * @return
     */
    private ArrayList<String> splitTextAtImages(String strTotal) {

        ArrayList<String> lstPages = new ArrayList<String>();

        String[] parts = strTotal.split(strPageCut);

        for (int i = 0; i < parts.length; i++) {
            lstPages.add(parts[i]);
        }

        return lstPages;
    }

    private Element getElement(Element rootNode, String strFacs) {
        Element eltFacs = null;
        List<Element> elts = rootNode.getChildren(strFacs);

        if (!elts.isEmpty()) {
            eltFacs = elts.get(0);
        } else {
            for (Element element : rootNode.getChildren()) {
                if (element.getName().contentEquals(strFacs)) {
                    eltFacs = element;
                    break;
                }
            }
        }
        return eltFacs;
    }

}
