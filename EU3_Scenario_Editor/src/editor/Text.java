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
//import java.util.regex.Pattern;

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
    
    //private static final Pattern semicolon = Pattern.compile(";");
    
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
        //String[] splitLine;
        for (File f : Main.filenameResolver.listFiles("localisation")) {
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
//                    splitLine = semicolon.split(line); //line.split(";");
//                    if (splitLine.length < 2) {
//                        if (!line.contains(";")) {
//                            // If it contains ";", then it's probably just a line like ;;;;;;;;;;;;
//                            // If not, we need to know what it is.
//                            System.err.println("Malformed line in file " + f.getPath() + ":");
//                            System.err.println(line);
//                        }
//                        continue;
//                    }
//                    text.put(splitLine[0].toLowerCase(), splitLine[1]); // English

                    int firstSemi = line.indexOf(';');
                    int secondSemi = line.indexOf(';', firstSemi + 1);
                    if (firstSemi < 0 || secondSemi < 0) {
                        System.err.println("Malformed line in file " + f.getPath() + ":");
                        System.err.println(line);
                    }
                    text.put(line.substring(0, firstSemi).toLowerCase(), line.substring(firstSemi + 1, secondSemi));
                }
            } finally {
                reader.close();
            }
        }
    }
    
    public static String getText(final String key) {
        if (key == null)
            return null; // is this a good idea? Won't this just pass the NPE further down the line?
        final String ret = text.get(key.toLowerCase());
        return (ret == null ? key : ret);
    }
    
    /** Creates a new instance of Text */
    private Text() { }
    
}
