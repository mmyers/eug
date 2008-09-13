/*
 * Text.java
 *
 * Created on May 31, 2007, 12:47 PM
 */

package editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 *
 * @author Michael Myers
 * @since 0.4pre1
 */
public final class Text {
    
    // All keys are converted to lower case before putting or getting text, to
    // standardize.
    private static final java.util.Map<String, String> text =
            new HashMap<String, String>();
    
    private static final Pattern semicolon = Pattern.compile(";");
    
    static {
        try {
            initText();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void initText() throws FileNotFoundException, IOException {
        java.io.BufferedReader reader;
        String line;
        String[] splitLine;
        for (File f : Main.filenameResolver.listFiles("localisation")) {
            if (!f.getName().endsWith(".csv"))
                continue;   // Could use a FileFilter or FilenameFilter
            
            if (f.length() <= 0) {
                continue;
            }
            
            reader = new java.io.BufferedReader(new java.io.FileReader(f), Math.min(1024000, (int)f.length()));
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#"))
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
    
    /** Creates a new instance of Text */
    private Text() { }
    
}
