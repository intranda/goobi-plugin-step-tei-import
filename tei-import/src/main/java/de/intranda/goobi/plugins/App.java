package de.intranda.goobi.plugins;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

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

        TeiImportPlugin plugin = new TeiImportPlugin();
        plugin.strTestMmFile = strMMFile;
        plugin.strTestSource = strSourceFolder;
        XMLConfiguration xmlConfig = new XMLConfiguration(strConfigFile);
        SubnodeConfiguration subConfig = xmlConfig.configurationAt("config");
        plugin.config = new XMLConfiguration(subConfig);
        plugin.prefs = new Prefs();
        plugin.prefs.loadPrefs(plugin.config.getString("rulesetPath"));
        
        Boolean boOk = plugin.execute();
    }
}
