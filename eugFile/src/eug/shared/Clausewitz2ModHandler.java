
package eug.shared;

import eug.parser.EUGFileIO;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Clausewitz 2 engine mod handler. This mod handler requires a .mod file.
 * Folders are either marked "replace_path" or not. If not, the folder is
 * assumed to be extended (mod files may override vanilla files only if present).
 * Subfolders such as "history/provinces" can be marked as "replace_path"
 * without marking the top-level folder as replace_path.
 * @author Michael
 */
public class Clausewitz2ModHandler extends ClausewitzModHandler implements ModHandler {
    /** Fully qualified path of the base game directory. */
    private final Path baseDir;
    /** Fully qualified path of the mod directory. */
    private final Path modDir;
    
    /** Set of folder names that are marked as "replace_path" in the .mod file. */
    private Set<String> replaced;
    
    public Clausewitz2ModHandler(String baseDir, String modFileName) {
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
                
                replaced = new HashSet<>();
                
                // Lowercase the strings so that resolution is case-insensitive
                for (String str : mod.getStrings("replace_path")) {
                    // may need to create Paths and convert back to string to avoid file separator issues
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
    
    @Override
    boolean isExtended(Path dir) {
        if (modDir == null)
            return false;
        
        return !isFolderReplaced(dir);
    }
    
    @Override
    boolean isFileReplaced(Path directory, Path filename) {
        if (isFolderReplaced(directory))
            return true;
        
        String relativePathName = directory.resolve(filename).relativize(baseDir).toString();
        return replaced.contains(relativePathName.toLowerCase());
    }
    
    @Override
    boolean isFolderReplaced(Path directory) {
        if (modDir == null)
            return false;
        
        // turn a path like "C:/Games/Europa Universalis III/history/provinces"
        // into "history/provinces" and then into "history"
        // then iterate through subfolders and see if any is replaced
        directory = directory.relativize(baseDir);
        if (replaced.contains(directory.getName(0).toString().toLowerCase()))
            return true;
        
        for (int i = 1; i < directory.getNameCount(); i++) {
            Path p = directory.subpath(0, i);
            if (replaced.contains(p.toString().toLowerCase()))
                return true;
        }
        
        return false;
    }
}
