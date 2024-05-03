package de.intranda.goobi.plugins;


public class FileCorresp {

    public FileCorresp() {
    }

    public String strMMName;
    
    public String strMMPath;
    
    public String strTEIName;
    
    public String strTEIPath;
    
    public String toString() {
        
        String str = strMMName + System.lineSeparator() + strMMPath  + System.lineSeparator() +strTEIName + System.lineSeparator() +strTEIPath;
        
        return str;
    }
}
