/*
 * Text.java
 *
 * Created on May 31, 2007, 12:47 PM
 */

package posed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Michael Myers
 */
public final class Text {
    
    // All keys are normalized to lower case
    private static final java.util.Map<String, String> text =
            new HashMap<String, String>();
    
    /** Creates a new instance of Text */
    private Text() { }
    
//    static {
//        try {
//            initText();
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }

    public static void initText(File mainDir) {
        try {
            if (mainDir.getAbsolutePath().contains(File.separator + "mod" + File.separator))
                initTextFromFolder(new File(mainDir.getParentFile().getParentFile().getAbsolutePath() + File.separator + "localisation"));
            initTextFromFolder(new File(mainDir.getAbsolutePath() + File.separator + "localisation"));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void initTextFromFolder(File locFolder) throws FileNotFoundException, IOException {
        java.io.BufferedReader reader;
        String line;
        //String[] splitLine;
        for (File f : locFolder.listFiles()) {
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
    
}
