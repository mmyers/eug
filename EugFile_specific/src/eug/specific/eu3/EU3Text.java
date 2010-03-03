/*
 * EU3Text.java
 *
 * Created on September 12, 2007, 2:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eug.specific.eu3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

import eug.shared.FilenameResolver;
    
/**
 * 
 * @author jeff
 */
public class EU3Text {
    FilenameResolver filenameresolver = null;
    
    /** Creates a new instance of EU3Text */
    public EU3Text(FilenameResolver fnr) {
        filenameresolver = fnr;
        try {
            initText();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    private static final java.util.Map<String, String> text =
            new HashMap<String, String>();
    
    private static final Pattern semicolon = Pattern.compile(";");
    
    
    private void initText() throws FileNotFoundException, IOException {
        java.io.BufferedReader reader;
        String line;
        String[] splitLine;
        for (File f : filenameresolver.listFiles("localisation")) {
            if (!f.getName().endsWith(".csv"))
                continue;   // Could use a FileFilter or FilenameFilter
            
            if (f.length() <= 0) {
                continue;
            }
            
            reader = new java.io.BufferedReader(new java.io.FileReader(f), Math.min(1024000, (int)f.length()));
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.charAt(0) == '#')
                        continue;
                    splitLine = semicolon.split(line); //line.split(";");
                    if (splitLine.length < 2) {
                        if (!line.contains(";")) {
                            // If it contains ";", then it's probably just a line like ;;;;;;;;;;;;
                            // If not, we need to know what it is.
                            System.err.println("Malformed line in file " + f.getPath() + ":");
                            System.err.println(line);
                        }
                        continue;
                    }
                    text.put(splitLine[0].toLowerCase(), splitLine[1]);   // English
                }
            } finally {
                reader.close();
            }
        }
    }
    
    public static String getText(final String key) {
        final String ret = text.get(key.toLowerCase());
        return (ret == null ? key : ret);
    }
    
    
}
