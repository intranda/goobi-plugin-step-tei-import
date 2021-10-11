package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.exceptions.UGHException;

/**
 * Hello world!
 *
 */
public class ImageCorresp {

    protected static final Logger logger = Logger.getLogger(ImageCorresp.class);

    //    private Prefs prefs;
    private XMLConfiguration config;

    public ImageCorresp(XMLConfiguration config) throws ConfigurationException, UGHException {

        this.config = config;
        //        this.prefs = new Prefs();
        //        prefs.loadPrefs(config.getString("rulesetPath"));
    }

    /**
     * For each file link in the TEI file, replace it with a link to the same image in the viewer.
     * 
     * @param fileTEI
     * @param dd
     * @return
     */
    public Document convertTeiLinks(File fileTEI, String strViewerId) {

        String strUrlBase = config.getString("viewerPages") + strViewerId + "/";
        int iPage = 1;

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

        //Try version with "facsimile":
        Element eltFacs = getElement(rootNode, "facsimile");

        if (eltFacs != null) {
            List<Element> lstFacs = eltFacs.getChildren();

            for (Element elt : lstFacs) {

                for (Element eltChild : elt.getChildren()) {

                    if (eltChild.getName().contentEquals("graphic")) {
                        for (Attribute attC : eltChild.getAttributes()) {
                            if (attC.getName().contentEquals("url")) {

                                //change the url:
                                String strNewURL = strUrlBase + iPage + "/";
                                attC.setValue(strNewURL);
                                //increment pages
                                iPage++;
                                break;
                            }
                        }
                    }
                }
            }
        } else {

            //no facsimile: try version with <body   <div   <pb facs="id" 

        }

        //return the changed doc:
        return document;
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

    public List<FileCorresp> fromFilePair(DigitalDocument dd, File fileTEI) {
        List<FileCorresp> lstFCsMM = imagesFromMM(dd);
        List<FileCorresp> lstFCsTEI = imagesFromTEI(fileTEI);

        if (lstFCsMM == null || lstFCsMM.isEmpty() || lstFCsTEI == null || lstFCsMM.isEmpty()) {
            return null;
        }

        List<FileCorresp> lstFCs = new ArrayList<FileCorresp>();

        for (int i = 0; i < lstFCsMM.size(); i++) {

            FileCorresp fcMM = lstFCsMM.get(i);
            FileCorresp fcTEI = lstFCsTEI.get(i);

            FileCorresp fc = new FileCorresp();
            fc.strMMName = fcMM.strMMName;
            fc.strMMPath = fcMM.strMMPath;
            fc.strTEIName = fcTEI.strTEIName;
            fc.strTEIPath = fcTEI.strTEIPath;

            lstFCs.add(fc);
        }

        return lstFCs;
    }

    /**
     * Get the file names and paths, in order, that appear in the MM file
     * 
     * @param strFile
     * @return
     * @throws UGHException
     */
    public List<FileCorresp> imagesFromMM(DigitalDocument dd) {

        DocStruct physical = dd.getPhysicalDocStruct();

        List<FileCorresp> lstImages = new ArrayList<FileCorresp>();
        List<DocStruct> lstPages = physical.getAllChildren();

        for (DocStruct page : lstPages) {

            if (page.getDocstructType() != "div") {
                continue;
            }

            FileCorresp fc = new FileCorresp();
            fc.strMMName = page.getImageName();
            fc.strMMPath = page.getAllContentFiles().get(0).getLocation();
            lstImages.add(fc);
        }

        return lstImages;
    }

    /**
     * Get the file names and paths, in order, that appear in the TEI file
     * 
     * @param strFile
     * @return
     * @throws UGHException
     * @throws IOException
     * @throws JDOMException
     */
    public List<FileCorresp> imagesFromTEI(File fileTEI) {

        List<FileCorresp> lstImages = new ArrayList<FileCorresp>();

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

        Element eltFacs = null;
        List<Element> elts = rootNode.getChildren("facsimile");

        if (!elts.isEmpty()) {
            eltFacs = elts.get(0);
        } else {
            for (Element element : rootNode.getChildren()) {
                if (element.getName().contentEquals("facsimile")) {
                    eltFacs = element;
                    break;
                }
            }
        }

        List<Element> lstFacs = eltFacs.getChildren();

        for (Element elt : lstFacs) {

            String strId = null;
            List<Attribute> lstAtts = elt.getAttributes();

            for (Attribute att : lstAtts) {
                if (att.getName().contentEquals("id")) {
                    strId = att.getValue();
                    break;
                }
            }

            //            String strId = elt.getAttributeValue("id");

            String strURL = null;

            for (Element eltChild : elt.getChildren()) {

                if (eltChild.getName().contentEquals("graphic")) {
                    for (Attribute attC : eltChild.getAttributes()) {
                        if (attC.getName().contentEquals("url")) {
                            strURL = attC.getValue();
                            break;
                        }
                    }
                }
            }
            //            String strURL = elt.getChild("graphic").getAttributeValue("url");

            FileCorresp fc = new FileCorresp();
            fc.strTEIName = strId;
            fc.strTEIPath = strURL;
            lstImages.add(fc);
        }

        return lstImages;
    }

    public Document movePBs(Document document) {

        Element rootNode = document.getRootElement();

        Element eltTxt = getElement(rootNode, "text");

        if (eltTxt != null) {

            Element eltBody = getElement(eltTxt, "body");
            if (eltBody != null) {

                List<Element> lstDivs = eltBody.getChildren();
                for (Element eltDiv : lstDivs) {

                    if (eltDiv.getName().contentEquals("div")) {

                        Boolean boContinue = true;
                        while (boContinue) {

                            boContinue = false;

                            List<Element> lstParas = new ArrayList<Element>(eltDiv.getChildren());
                            for (Element eltPara : lstParas) {

                                // note elts
                                if (eltPara.getName().contentEquals("note")) {
                                    boContinue = movePBup(eltDiv, eltPara);
                                    if (boContinue)
                                        break;
                                }

                                // p elts:
                                if (eltPara.getName().contentEquals("p")) {

                                    List<Element> lstChildren = new ArrayList<Element>(eltPara.getChildren());
                                    for (Element eltS : lstChildren) {

                                        if (eltS.getName().contentEquals("s")) {

                                            boContinue = movePBup(eltPara, eltS);
                                            if (boContinue)
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //return the changed doc:
        return document;
    }

    /**
     * Given an elt which may contain a pb elt, move the first pb elt up to the same level. Return true. If there is none, return false.
     * 
     * @param eltPara
     * @param eltS
     */
    private Boolean movePBup(Element eltPara, Element eltS) {

        int indexS = eltPara.indexOf(eltS);

        //here we may find a pb, which must be moved out:
        List<Element> lstSub = new ArrayList<Element>(eltS.getChildren());
        for (Element eltPB : lstSub) {

            if (eltPB.getName().contentEquals("pb")) {

                //split the text at the child:
                int index = eltS.indexOf(eltPB);

                if (index < eltS.getText().length()) {
                    Element elt1 = eltS.clone();
                    Element elt2 = eltS.clone();

                    for (int i = index; i < eltS.getContentSize(); i++) {
                        elt1.removeContent(index);
                    }
                    for (int i = 0; i <= index; i++) {
                        elt2.removeContent(0);
                    }

                    eltPara.removeContent(indexS);
                    eltPara.addContent(indexS, elt1);
                    eltPara.addContent(indexS + 1, elt2);
                }

                //move it up one level
                eltPB.detach();
                eltPara.addContent(indexS + 1, eltPB);

                return true;
            }
        }

        //none?
        return false;
    }

    //    /**
    //     * Get the list of corresponding files for the MM and TEI files.
    //     * 
    //     * @param strMMFile
    //     * @param strTEIFile
    //     * @return
    //     * @throws UGHException
    //     * @throws JDOMException
    //     * @throws IOException
    //     */
    //    public List<FileCorresp> fromFilePair(String strMMFile, String strTEIFile) throws UGHException, JDOMException, IOException {
    //
    //        List<FileCorresp> lstFCsMM = imagesFromMM(strMMFile);
    //        List<FileCorresp> lstFCsTEI = imagesFromTEI(strTEIFile);
    //
    //        List<FileCorresp> lstFCs = new ArrayList<FileCorresp>();
    //
    //        for (int i = 0; i < lstFCsMM.size(); i++) {
    //
    //            FileCorresp fcMM = lstFCsMM.get(i);
    //            FileCorresp fcTEI = lstFCsTEI.get(i);
    //
    //            FileCorresp fc = new FileCorresp();
    //            fc.strMMName = fcMM.strMMName;
    //            fc.strMMPath = fcMM.strMMPath;
    //            fc.strTEIName = fcTEI.strTEIName;
    //            fc.strTEIPath = fcTEI.strTEIPath;
    //
    //            lstFCs.add(fc);
    //        }
    //
    //        return lstFCs;
    //    }
    //
    //    /**
    //     * Get the file names and paths, in order, that appear in the MM file
    //     * 
    //     * @param strFile
    //     * @return
    //     * @throws UGHException
    //     */
    //    public List<FileCorresp> imagesFromMM(String strFile) throws UGHException {
    //
    //        MetsMods mm = new MetsMods(prefs);
    //        mm.read(strFile);
    //
    //        DigitalDocument dd = mm.getDigitalDocument();
    //
    //        return imagesFromMM(dd);
    //    }
    //
    //    /**
    //     * Get the file names and paths, in order, that appear in the TEI file
    //     * 
    //     * @param strFile
    //     * @return
    //     * @throws UGHException
    //     * @throws IOException
    //     * @throws JDOMException
    //     */
    //    public List<FileCorresp> imagesFromTEI(String strFile) throws JDOMException, IOException {
    //
    //        File xmlFile = new File(strFile);
    //        return imagesFromTEI(xmlFile);
    //    }
}
