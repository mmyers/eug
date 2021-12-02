
package eug.shared;

import eug.parser.EUGFileIO;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Clausewitz 1 engine mod handler. This mod handler requires a .mod file.
 * Folders are either marked "replace", marked "extend", or nothing. If nothing,
 * only vanilla files in the folder are used. Subfolders cannot be marked as
 * either replace or extend, only primary folders (e.g. "common" but not
 * "common/countries").
 * @author Michael
 */
public class ClausewitzModHandler implements ModHandler {
    
    /** Fully qualified path of the base game directory. */
    private Path baseDir;
    /** Fully qualified path of the mod directory. */
    private Path modDir;
    
    /** Set of folder names that are marked as "extend" in the .mod file. */
    private Set<String> extended;
    /** Set of folder names that are marked as "replace" in the .mod file. */
    private Set<String> replaced;
    
    /** Only for use by subclasses */
    ClausewitzModHandler() { }
    
    /**
     * Creates a new instance of ClausewitzModHandler using the specified base
     * game directory and .mod file.
     * @param baseDir
     * @param modFileName 
     */
    public ClausewitzModHandler(String baseDir, String modFileName) {
        this.baseDir = Paths.get(baseDir);
        if (!this.baseDir.toFile().exists())
            throw new IllegalArgumentException("Base dir " + baseDir + " does not exist");
        
        if (modFileName == null) {
            modDir = null;
        } else {
            GenericObject mod = EUGFileIO.load(modFileName);

            if (mod == null) {
                modDir = null;
            } else {
                modDir = this.baseDir.resolve(mod.getString("path"));
                
                extended = new HashSet<>();
                replaced = new HashSet<>();
                
                // Lowercase the strings so that resolution is case-insensitive
                for (String str : mod.getStrings("extend")) {
                    extended.add(str.toLowerCase());
                }
                for (String str : mod.getStrings("replace")) {
                    replaced.add(str.toLowerCase());
                }
            }
        }
    }

    @Override
    public String resolveDirectory(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String resolveFilename(String filename) {
        Path p = Paths.get(filename);
        Path dir = p.getParent(); // all Clausewitz game files should have a parent dir - nothing is in the base dir
        
        if (isFileReplaced(dir, p)) {
            // Case 1: Directory is replaced.
            // Return the file in the moddir, even if it doesn't exist.
            return modDir.resolve(filename).toString();
        } else if (isExtended(dir)) {
            // Case 2: Directory is extended.
            // Check if the file exists in the moddir.
            Path modFilePath = modDir.resolve(filename);
            if (modFilePath.toFile().exists()) {
                // It does, so return it.
                return modFilePath.toString();
            } else {
                // It doesn't, so return the file in the main dir.
                return baseDir.resolve(filename).toString();
            }
        } else {
            // Case 3: Directory is not modded.
            // Return the file in the main dir.
            return baseDir.resolve(filename).toString();
        }
    }

    @Override
    public String[] listFiles(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    // Utility methods (package-private so that Clausewitz2ModHandler can override them)
    
    /**
     * Returns true if the highest-level directory in the path under baseDir is extended.
     */
    boolean isExtended(Path directory) {
        if (modDir == null)
            return false;
        
        // turn a path like "C:/Games/Europa Universalis III/history/provinces"
        // into "history/provinces" and then into "history"
        String topDir = directory.relativize(baseDir).getName(0).toString();
        
        return extended.contains(topDir.toLowerCase());
    }
    
    boolean isFileReplaced(Path directory, Path filename) {
        return isFolderReplaced(directory);
    }
    
    /**
     * Returns true if the highest-level directory in the path under baseDir is replaced.
     */
    boolean isFolderReplaced(Path directory) {
        // For Clausewitz 1, replacing and extending work the exact same way
        if (modDir == null)
            return false;
        
        // turn a path like "C:/Games/Europa Universalis III/history/provinces"
        // into "history/provinces" and then into "history"
        String topDir = directory.relativize(baseDir).getName(0).toString();
        
        return replaced.contains(topDir.toLowerCase());
    }
}
