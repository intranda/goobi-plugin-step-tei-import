package de.intranda.goobi.plugins;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * Hello world!
 *
 */
public class App {
    //Testing
    public static void main(String[] args) throws ConfigurationException {
        String strConfigFile = "";
        String strMMFile = "";
        String strSourceFolder = "";

        TeiImportPlugin plugin = new TeiImportPlugin();
        plugin.strTestMmFile = strMMFile;
        plugin.strTestSource = strSourceFolder;
        plugin.config = new XMLConfiguration(strConfigFile);

        Boolean boOk = plugin.execute();
    }
}
