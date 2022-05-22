/*
 * Text.java
 *
 * Created on May 31, 2007, 12:47 PM
 */

package posed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

/**
 *
 * @author Michael Myers
 */
public final class Text {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Text.class.getName());
    
    // All keys are normalized to lower case
    private static final java.util.Map<String, String> text = new HashMap<>();
    
    /** Creates a new instance of Text */
    private Text() { }

    public static void initText(File mainDir) {
        try {
            if (mainDir.getAbsolutePath().contains(File.separator + "mod" + File.separator)) {
                String modPath = mainDir.getParentFile().getParentFile().getAbsolutePath() + File.separator + "localisation";
                initTextFromFolder(new File(modPath));
            }
            String path = mainDir.getAbsolutePath() + File.separator + "localisation";
            initTextFromFolder(new File(path));
        } catch (FileNotFoundException ex) {
            log.log(Level.SEVERE, "Could not load localization", ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Could not load localization", ex);
        }
    }
    
    private static void initTextFromFolder(File locFolder) throws FileNotFoundException, IOException {
        File[] files = locFolder.listFiles();
        if (files == null) {
            log.log(Level.WARNING, "Could not find localization files in {0}", locFolder.getAbsolutePath());
            return;
        }
        
        Arrays.sort(files);
        for (File f : files) {
            if (!f.getName().endsWith(".csv"))
                continue;   // Could use a FileFilter or FilenameFilter
            
            if (f.length() <= 0) {
                continue;
            }
            
            int bufferSize = Math.min(1024000, (int)f.length());
            try (BufferedReader reader = new BufferedReader(new FileReader(f), bufferSize)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() == 0 || line.charAt(0) == '#')
                        continue;

                    int firstSemi = line.indexOf(';');
                    int secondSemi = line.indexOf(';', firstSemi + 1);
                    if (firstSemi < 0 || secondSemi < 0) {
                        log.log(Level.WARNING, "Malformed line in file {0}:\n{1}", new Object[]{f.getPath(), line});
                    }
                    if (secondSemi < 0)
                        secondSemi = line.length() - 1;
                    String key = line.substring(0, firstSemi).toLowerCase();
                    if (!text.containsKey(key))
                        text.put(key, line.substring(firstSemi + 1, secondSemi));
                }
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
