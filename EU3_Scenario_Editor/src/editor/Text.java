/*
 * Text.java
 *
 * Created on May 31, 2007, 12:47 PM
 */

package editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
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
        File[] files = Main.filenameResolver.listFiles("localisation");
        Arrays.sort(files);
        for (File f : files) {
            if (!f.getName().endsWith(".csv"))
                continue;   // Could use a FileFilter or FilenameFilter
            
            if (f.length() <= 0) {
                continue;
            }
            
            reader = new java.io.BufferedReader(new java.io.FileReader(f), Math.min(1024000, (int)f.length()));
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.length() == 0 || line.charAt(0) == '#')
                        continue;

                    int firstSemi = line.indexOf(';');
                    int secondSemi = line.indexOf(';', firstSemi + 1);
                    if (firstSemi < 0 || secondSemi < 0) {
                        System.err.println("Malformed line in file " + f.getPath() + ":");
                        System.err.println(line);
                    }
                    String key = line.substring(0, firstSemi).toLowerCase();
                    if (!text.containsKey(key))
                        text.put(key, line.substring(firstSemi + 1, secondSemi));
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
