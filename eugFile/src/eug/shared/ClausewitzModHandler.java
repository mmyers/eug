
package eug.shared;

import eug.parser.EUGFileIO;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    protected Path baseDir;
    /** Fully qualified path of the mod directory. */
    protected Path modDir;
    
    /** Set of folder names that are marked as "extend" in the .mod file. */
    protected Set<String> extended;
    /** Set of folder names that are marked as "replace" in the .mod file. */
    protected Set<String> replaced;
    
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
        
        if (modFileName == null || modFileName.isEmpty()) {
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
        Path p = Paths.get(path);
        Path parentDir = p.getParent();
        
        if (isFolderReplaced(p)) {
            return modDir.resolve(path).toString() + File.separator;
        } else if (isExtended(parentDir)) {
            // XXX I don't think extending directories is done correctly
            // (above comment was copied over from FilenameResolver - behavior should match the old implementation for now at least)
            Path dirPathInMod = modDir.resolve(path);
            if (dirPathInMod.toFile().exists()) {
                return modDir.resolve(path).toString() + File.separator;
            } else {
                return baseDir.resolve(path).toString() + File.separator;
            }
        } else {
            return baseDir.resolve(path).toString() + File.separator;
        }
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
    public File[] listFiles(String path) {
        Path p = Paths.get(path);
        Path dir = p.getParent();
        
        if (isFolderReplaced(p)) {
            return filterFiles(modDir.resolve(path).toFile().listFiles());
        } else if (isExtended(dir)) {
            final Map<String, File> ret = new HashMap<>();

            final File[] files = baseDir.resolve(path).toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    String filename = file.getName().toLowerCase();
//                    if (ignoreFileTypes.stream().anyMatch(ext -> filename.endsWith(ext)))
//                        continue;
                    ret.put(filename, file);
                }
            }

            final File moddedDir = modDir.resolve(path).toFile();
            if (moddedDir.exists()) {
                // only add files which don't use the same name
                for (File f : moddedDir.listFiles()) {
                    String filename = f.getName().toLowerCase();
//                    if (ignoreFileTypes.stream().anyMatch(ext -> filename.endsWith(ext)))
//                        continue;
                    ret.put(filename, f); // overwrites
                }
            }

            return ret.values().toArray(new File[ret.size()]);
        } else {
            return filterFiles(baseDir.resolve(path).toFile().listFiles());
        }
    }
    
    
    // Utility methods (protected so that Clausewitz2ModHandler can override them)
    
    /**
     * Returns true if the highest-level directory in the path under baseDir is extended.
     */
    protected boolean isExtended(Path directory) {
        if (modDir == null)
            return false;
        if (directory == null)
            return false;
        
        // turn a path like "C:/Games/Europa Universalis III/history/provinces"
        // into "history/provinces" and then into "history"
        if (directory.isAbsolute())
            directory = baseDir.relativize(directory);
        String topDir = directory.getName(0).toString();
        
        return extended.contains(topDir.toLowerCase());
    }
    
    protected boolean isFileReplaced(Path directory, Path filename) {
        return isFolderReplaced(directory);
    }
    
    /**
     * Returns true if the highest-level directory in the path under baseDir is replaced.
     */
    protected boolean isFolderReplaced(Path directory) {
        // For Clausewitz 1, replacing and extending work the exact same way
        if (modDir == null)
            return false;
        if (directory == null)
            return false;
        
        // turn a path like "C:/Games/Europa Universalis III/history/provinces"
        // into "history/provinces" and then into "history"
        if (directory.isAbsolute())
            directory = baseDir.relativize(directory);
        String topDir = directory.getName(0).toString();
        
        return replaced.contains(topDir.toLowerCase());
    }
    
    protected File[] filterFiles(File[] files) {
        return files; //Arrays.stream(files).map(f -> f.getName()).toArray(String[]::new);
    }
    
    // Specific methods for history files, which are overridden not by filename
    // but by province ID or country tag
    // e.g. history/provinces/1 - Alaska.txt in a mod can override
    //      history/provinces/1 - Stockholm.txt in vanilla.
    
    public String getProvinceHistoryFile(int provId) {
        // Can't use resolveFilename, because we don't know what the file is called.
        if (modDir != null) {
            if (isFolderReplaced(Paths.get("history/provinces"))) {
                // Case 1: History is replaced.
                // If there is a file in the mod's province history folder that
                // starts with the provId, return it.
                // Otherwise, return null.
                return getFilePath(modDir.resolve("history/provinces"), Integer.toString(provId), true);
            } else if (isExtended(Paths.get("history/provinces"))) {
                // Case 2: History is extended.
                // If there is a file in the mod's province history folder that
                // starts with the provId, return it.
                // Otherwise, try to return the vanilla file.
                String filename = getFilePath(modDir.resolve("history/provinces"), Integer.toString(provId), true);
                
                if (filename != null)
                    return filename;
                else
                    return getVanillaProvHistoryFile(provId);
            } else {
                // Case 3: History is not modded.
                // Try to return the vanilla file.
                return getVanillaProvHistoryFile(provId);
            }
        } else {
            // No mod. Try to return the vanilla file.
            return getVanillaProvHistoryFile(provId);
        }
    }
    
    private String getVanillaProvHistoryFile(final int provId) {
        return getFilePath(baseDir.resolve("history/provinces"), Integer.toString(provId), true);
    }
    
    /**
     * Victoria 2-specific method to find the province history file for the
     * given province.
     * @param provId the ID of the province to locate the history file for
     * @return the name of the province history file, or {@code null} if
     * the file could not be found.
     * @since EUGFile 1.07.00
     */
    public String getVic2ProvinceHistoryFile(final int provId) {
        // Can't use resolveFilename, because we don't know what the file is called.
        if (modDir != null) {
            if (isFolderReplaced(Paths.get("history/provinces"))) {
                // Case 1: History is replaced.
                // If there is a file in the mod's province history folder that
                // starts with the provId, return it.
                // Otherwise, return null.
                return getVic2ProvHistoryFile(modDir.resolve("history/provinces"), Integer.toString(provId), true);
            } else if (isExtended(Paths.get("history/provinces"))) {
                // Case 2: History is extended.
                // If there is a file in the mod's province history folder that
                // starts with the provId, return it.
                // Otherwise, try to return the vanilla file.
                String filename = getVic2ProvHistoryFile(modDir.resolve("history/provinces"), Integer.toString(provId), true);
                
                if (filename != null)
                    return filename;
                else
                    return getVanillaVic2ProvHistoryFile(provId);
            } else {
                // Case 3: History is not modded.
                // Try to return the vanilla file.
                return getVanillaVic2ProvHistoryFile(provId);
            }
        } else {
            // No mod. Try to return the vanilla file.
            return getVanillaVic2ProvHistoryFile(provId);
        }
    }

    private String getVanillaVic2ProvHistoryFile(final int provId) {
        return getVic2ProvHistoryFile(baseDir.resolve("history/provinces"), Integer.toString(provId), true);
    }

    private String getVic2ProvHistoryFile(Path dirName, String prefix, boolean exactMatch) {
        // first try the directory itself in case some files aren't in subfolders
        String ret = getFilePath(dirName, prefix, exactMatch);
        if (ret != null)
            return ret;

        String[] array = enumerateFiles(dirName);
        for (String subdir : array) {
            ret = getFilePath(dirName.resolve(subdir), prefix, exactMatch);
            if (ret != null)
                return ret;
        }
        return null;
    }
    
    public String getCountryHistoryFile(final String tag) {
        if (modDir != null) {
            if (isFolderReplaced(Paths.get("history/countries"))) {
                return getFilePath(modDir.resolve("history/countries"), tag, false);
            } else if (isExtended(Paths.get("history/countries"))) {
                final String filename = getFilePath(modDir.resolve("history/countries"), tag, false);
                if (filename != null)
                    return filename;
                else
                    return getVanillaCtryHistoryFile(tag);
            } else {
                return getVanillaCtryHistoryFile(tag);
            }
        } else {
            return getVanillaCtryHistoryFile(tag);
        }
    }
    
    private String getVanillaCtryHistoryFile(final String tag) {
        return getFilePath(baseDir.resolve("history/countries"), tag, false);
    }
    
    // Utility methods used by all the get<X>HistoryFile() methods
    
    private String getFilePath(final Path dirname, final String start, final boolean exactMatch) {
        File f = getFile(dirname, start, exactMatch);
        if (f != null)
            return f.getAbsolutePath();
        return null;
    }
    
    private File getFile(Path dirname, String start, boolean exactMatch) {
        String[] array = enumerateFiles(dirname);
        
        final int length = start.length();

        int index = -Arrays.binarySearch(array, start, String.CASE_INSENSITIVE_ORDER) - 1;
        if (index >= array.length)
            return null;
        
        String name = array[index];
        /*for (String name : array)*/ {
            if (name.substring(0, length).equalsIgnoreCase(start) && //!name.contains("~") &&
                    (!exactMatch || !Character.isLetterOrDigit(name.charAt(length)))) {
                return dirname.resolve(name).toFile();
            }
        }
        return null;
    }
    
    // Cache of directory contents for faster enumeration when required. Sorted by file name.
    private final Map<Path, String[]> directories = new HashMap<>();
    
    private String[] enumerateFiles(final Path path) throws RuntimeException {
        String[] array = directories.get(path);
        if (array == null) {
            File dir = path.toFile();
            if (!dir.isDirectory())
                return new String[]{};
            
            array = dir.list();
            if (array == null) {
            //File[] files = dir.listFiles();
            //if (files == null) {
                throw new RuntimeException("Failed to open directory " + path);
            }
            //List<String> filenames = new ArrayList<>();
            //Collections.addAll(filenames, dir.list());
            // go one level deeper to handle Vic2 province history files
            //for (File f : files) {
            //    if (f.isDirectory())
            //        Collections.addAll(filenames, f.list());
            //}
            //array = filenames.toArray(new String[]{});
            
            Arrays.sort(array, String.CASE_INSENSITIVE_ORDER);
            directories.put(path, array);
        }
        return array;
    }
}
