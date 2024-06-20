/*
 * Text.java
 *
 * Created on May 31, 2007, 12:47 PM
 */

package editor;

import eug.shared.FilenameResolver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Michael Myers
 * @since 0.4pre1
 */
public final class Text {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Text.class.getName());
    
    // All keys are converted to lower case before putting or getting text, to
    // standardize.
    private static final java.util.Map<String, String> text = new HashMap<>();
    
    /**
     * Must be called before {@link getText} is used.
     * @param resolver
     * @param version
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void initText(FilenameResolver resolver, GameVersion version) throws FileNotFoundException, IOException {
        List<File> files = listLocFiles(resolver);
        
        if (files.isEmpty())
            return;

        long startTime = System.currentTimeMillis();
        int processedFiles;
        if (version.getTextFormat().equals("yaml")) {
            log.log(Level.INFO, "Reading YAML localization files");
            processedFiles = processFilesYaml(files);
        } else {
            log.log(Level.INFO, "Reading CSV localization files");
            processedFiles = processFilesCsv(files);
        }
        log.log(Level.INFO, "Processed {0} localization files in {1} ms.", new Object[] { processedFiles, System.currentTimeMillis() - startTime });
    }
    
    private static List<File> listLocFiles(FilenameResolver resolver) {
        // HOI4
        List<File> files = resolver.listFilesRecursive("localisation/english");
        if (!files.isEmpty()) {
            files.addAll(resolver.listFilesRecursive("localisation/replace/english"));
            return files;
        }

        // EU3/EU4
        files = resolver.listFilesRecursive("localisation");
        if (!files.isEmpty())
            return files;

        // CK3
        files = resolver.listFilesRecursive("localization/english");
        if (!files.isEmpty()) {
            files.addAll(resolver.listFilesRecursive("localization/replace/english"));
            return files;
        }
        return files;
    }
    
    private static int processFilesCsv(List<File> files) throws FileNotFoundException, IOException {
        int count = 0;
        
        for (File f : files) {
            if (!f.getName().endsWith(".csv"))
                continue;   // Could use a FileFilter or FilenameFilter
            
            if (f.length() <= 0) {
                continue;
            }
            
            count++;
            
            int bufferSize = Math.min(1024000, (int)f.length());
            try (BufferedReader reader = new BufferedReader(new FileReader(f), bufferSize)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() == 0 || line.charAt(0) == '#')
                        continue;

                    int firstSemi = line.indexOf(';');
                    if (firstSemi < 0) {
                        log.log(Level.WARNING, "Malformed line in file {0}:", f.getPath());
                        log.log(Level.WARNING, line);
                        continue;
                    }

                    int secondSemi = line.indexOf(';', firstSemi + 1);
                    if (secondSemi < 0)
                        secondSemi = line.length();
                    
                    String key = line.substring(0, firstSemi); //.toLowerCase();
                    //if (!text.containsKey(key))
                        text.put(key, line.substring(firstSemi + 1, secondSemi));
                }
            }
        }
        
        return count;
    }

    private static int processFilesYaml(List<File> files) throws FileNotFoundException, IOException {
        int count = 0;
        
        // very naive implementation
        // EU4 YAML files consist of a single node, defined in the first line
        // so we skip that line and break everything else at a ":"
        for (File f : files) {
            if (!f.getName().endsWith(".yml"))
                continue;   // Could use a FileFilter or FilenameFilter

            if (f.length() <= 0) {
                continue;
            }
            
            if (processYamlFile(f))
                count++;
        }
        
        return count;
    }

    private static boolean processYamlFile(File f) throws IOException {
        int bufferSize = Math.min(102400, (int)f.length());
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8), bufferSize)) {
            // Read the first line and make sure everything is in order
            String line = reader.readLine();
            if (line.charAt(0) == '\uFEFF') // Unicode BOM, which Java doesn't handle in UTF-8 files
                line = line.substring(1);
            if (!line.startsWith("l_english")) {
                return false;
            }
            
            // Read the rest of the file
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#')
                    continue;
                if (line.charAt(0) == '\uFEFF')
                    line = line.substring(1);
                
                if (line.endsWith("\\n")) {
                    StringBuilder lineBuilder = new StringBuilder(line).append("\n").append(line = reader.readLine());
                    while (line.endsWith("\\n")) {
                        lineBuilder.append("\n").append(line = reader.readLine());
                    }
                    line = lineBuilder.toString();
                }
                
                int comment = line.indexOf('#');
                if (comment > 0)
                    line = line.substring(0, comment);
                
                int firstColon = line.indexOf(':');
                if (firstColon < 0) {
                    log.log(Level.WARNING, "Malformed line in file {0}:", f.getPath());
                    log.log(Level.WARNING, line);
                    continue;
                }
                
                String key = line.substring(0, firstColon).trim();
                String value = line.substring(firstColon + 1).trim();
                value = extractQuote(value);
                text.put(key, value);
            }
        }
        return true;
    }
    
    private static String extractQuote(String value) {
        int quoteIdx = -1;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') {
                if (quoteIdx < 0) {
                    // mark the opening quote for later
                    quoteIdx = i;
                } else {
                    // already had an opening quote, so close it and get out
                    return value.substring(quoteIdx+1, i);
                }
            }
        }
        // no quote or only a single quote found - keep original string
        return value;
    }
    
    public static String getText(final String key) {
        if (key == null)
            return null; // is this a good idea? Won't this just pass the NPE further down the line?
        final String ret = text.get(key/*.toLowerCase()*/);
        return (ret == null ? key : ret);
    }
    
    /** Creates a new instance of Text */
    private Text() { }
    
}
