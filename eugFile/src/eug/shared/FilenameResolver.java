/*
 * FilenameResolver.java
 *
 * Created on February 9, 2007, 4:44 PM
 */

package eug.shared;

import eug.parser.EUGFileIO;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Resolves filenames, given a main directory name and a mod name.
 * <p>
 * The default behavior is EU3-compatible: mod files are in a directory named
 * <code>getMainDirName() + getModPrefix() + getModName()</code>, and rules for
 * extending/replacing directories are in
 * <code>getMainDirName() + getModPrefix() + getModName() + ".mod"</code>.
 * <p>
 * To change to EU2-compatible behavior, use
 * {@link #setModFile(boolean) setModFile(false)} and
 * {@link #setModPrefix(String) setModPrefix("")}.
 * @author Michael Myers
 * @since EUGFile 1.02.01
 */
public final class FilenameResolver {
    
    /** The base directory. */
    private String mainDirName;
    
    private String modName;
    
    private String modPrefix = "mod" + File.separator;
    
    private String modDirName; // in the game directory
    
    private Set<String> extended;
    private Set<String> replaced;
    
    private boolean usingMod;
    
    /** true <=> main mod directory has a .mod file corresponding to each mod. */
    private boolean modFile = true;
    
    /**
     * Clausewitz 2 (V2:AHD, EU4, and later) mods: all folders are extended
     * and files or folders can be marked "replace_path"
     * See: http://forum.paradoxplaza.com/forum/showthread.php?587405-HOW-TO-make-a-modfile-with-the-new-mod-folder
     */
    private boolean clausewitz2Mod;
    
    /**
     * Creates a new instance of FilenameResolver, loading configuration from
     * the given file.
     * @param configFile a file containing at least the following two entries:
     * <ul>
     * <li><code>maindir = "&lt;full path&gt;"</code></li>
     * <li><code>moddir = "&lt;name of mod&gt;"</code></li>
     * </ul>
     */
    public FilenameResolver(File configFile) {
        initDirNames(configFile);
    }
    
    /**
     * Creates a new instance of FilenameResolver with the given main directory
     * and no mod.
     * @param mainDirName the fully qualified name of the main directory.
     */
    public FilenameResolver(String mainDirName) {
        this(mainDirName, "", false);
    }
    
    /**
     * Creates a new instance of FilenameResolver with the given main directory
     * and mod name.
     * @param mainDirName the fully qualified name of the main directory.
     * @param modName the unqualified name of the mod.
     */
    public FilenameResolver(String mainDirName, String modName) {
        this(mainDirName, modName, false);
    }
    
    public FilenameResolver(String mainDirName, String modName, boolean clausewitz2Mod) {
        this.clausewitz2Mod = clausewitz2Mod;
        setMainDirectory(mainDirName);
        setModName(modName != null ? modName : "");
    }
    
    private void initDirNames(File cfgFile) {
        final GenericObject cfg = EUGFileIO.load(cfgFile);
        
        if (cfg == null) {
            throw new RuntimeException("Failed to load config from " + cfgFile.getName());
        } else {
            setMainDirectory(cfg.getString("maindir"));
            setModName(cfg.getString("moddir"));
        }
    }
    
    /**
     * Sets the main directory name to the given string.
     * @param dirName the main directory.
     * @throws NullPointerException if <code>dirName</code> is <code>null</code>.
     */
    public void setMainDirectory(String dirName) {
        mainDirName = dirName;
        
        if (!(mainDirName.endsWith("/") || mainDirName.endsWith("\\"))) {
            mainDirName += File.separator;
        }
    }
    
    /**
     * Sets the mod name to the given string.
     * @param modName the name of the mod.
     * @throws NullPointerException if <code>modName</code> is <code>null</code>.
     */
    public void setModName(String modName) {
        this.modName = modName;
        
        if (modName.length() != 0) {
            usingMod = true;
            setModDirectory(modName);
        } else {
            usingMod = false;
            modDirName = mainDirName;
        }
    }

    /**
     * Sets the fully-qualified mod directory. If the .mod file does not have
     * the same name as the directory (i.e. if the .mod file uses a "path"
     * directive), the .mod file will not be loaded. Instead, the application
     * must call {@link #setModFileName(java.lang.String) setModFileName()}.
     * @param dirName
     */
    public void setModDirectory(String dirName) {
        modDirName = dirName;
        if (dirName == null || dirName.isEmpty()) {
            usingMod = false;
            return;
        }

        usingMod = true;

        if (modFile) {
            File file = new File(modDirName + ".mod");

            if (file.exists())
                setModFileName(modDirName + ".mod");
        }
        
        if (!(modDirName.endsWith("/") || modDirName.endsWith("\\"))) {
            modDirName += File.separator;
        }
    }

    /**
     * Sets the fully-qualified name of the .mod file.
     * @param filename
     */
    public void setModFileName(String filename) {
        final GenericObject mod = EUGFileIO.load(filename);

        extended = new HashSet<>();
        replaced = new HashSet<>();
        if (mod == null) {
            modFile = false;
        } else if (clausewitz2Mod) {
            for (String str : mod.getStrings("replace_path"))
                replaced.add(str.toLowerCase());
        } else {
            // Lowercase the strings so that resolution is case-insensitive
            for (String str : mod.getStrings("extend"))
                extended.add(str.toLowerCase());
            for (String str : mod.getStrings("replace"))
                replaced.add(str.toLowerCase());
        }
    }
    
    /**
     * Resolves the name of the given directory.
     * <p>
     * If no mod is being used, this simply returns the parameter. Otherwise,
     * the mod's .mod file will be checked to see if the directory is replaced.
     * If so, the mod path will be returned.
     *
     * @param dirName the name of the directory to resolve.
     * @return the path that the game will look in for the directory. This path
     * will end with the default file separator character.
     * @see resolveFilename(String)
     */
    public String resolveDirectory(String dirName) {
        if (dirName.charAt(0) == '/' || dirName.charAt(0) == '\\')
            dirName = dirName.substring(1);
        
        if (!usingMod)
            return mainDirName + dirName;
        
        final String[] splitPath = splitParent(dirName);
        
        if (modFile) {
            if (isFolderReplaced(dirName)) {
                return modDirName + dirName;
            } else if (isExtended(splitPath[0])) {
                // XXX I don't think extending directories is done correctly
                if (new File(modDirName + dirName).exists()) {
                    return modDirName + dirName;
                } else {
                    return mainDirName + dirName;
                }
            } else {
                return mainDirName + dirName;
            }
        } else {
            if (new File(modDirName + dirName).exists())
                return modDirName + dirName;
            else
                return mainDirName + dirName;
        }
    }
    
    /**
     * Lists all files in the given directory. If a mod is being used and the
     * directory is set to extend, files in both the original and the mod
     * directory are returned.
     * @param dirName the name of the directory to list files in.
     */
    public File[] listFiles(String dirName) {
        if (dirName.charAt(0) == '/' || dirName.charAt(0) == '\\')
            dirName = dirName.substring(1);
        
        if (!usingMod)
            return new File(mainDirName + dirName).listFiles();
        
        final String[] splitPath = splitParent(dirName);
        
        if (modFile) {
            if (isFolderReplaced(dirName)) {
                return new File(modDirName + dirName).listFiles();
            } else if (isExtended(splitPath[0])) {
                final java.util.Map<String, File> ret = new java.util.HashMap<String, File>();

                final File[] files = new File(mainDirName + dirName).listFiles();
                if (files != null) {
                    for (File file : files)
                        ret.put(file.getName().toLowerCase(), file);
                }
                
                final File moddedDir = new File(modDirName + dirName);
                if (moddedDir.exists()) {
                    // only add files which don't use the same name
                    for (File f : moddedDir.listFiles())
                        ret.put(f.getName().toLowerCase(), f); // overwrites
                }
                
                return ret.values().toArray(new File[ret.size()]);
            } else {
                return new File(mainDirName + dirName).listFiles();
            }
        } else {
            if (new File(modDirName + dirName).exists())
                return new File(modDirName + dirName).listFiles();
            else
                return new File(mainDirName + dirName).listFiles();
        }
    }
    
    /**
     * Resolves the name of the given file.
     * <p>
     * If no mod is being used, this simply returns the parameter. Otherwise,
     * the mod's .mod file will be checked to see if the file's directory
     * (the part of the filename before the first '/' or '\' character) is
     * being replaced. If so, the mod path will be returned.
     *
     * @param filename the name of the file to resolve the path of.
     * @return the path that the game will look in for the file.
     * @see resolveDirectory(String)
     */
    public String resolveFilename(String filename) {
        if (filename.charAt(0) == '/' || filename.charAt(0) == '\\')
            filename = filename.substring(1);
        
        if (!usingMod) {
            return mainDirName + filename;
        }
        
        final String[] splitPath = splitParent(filename);
        
        if (modFile) {
            // EU3-style mod
            if (isFileReplaced(splitPath[0], splitPath[1])) {
                // Case 1: Directory is replaced.
                // Return the file in the moddir, even if it doesn't exist.
                return modDirName + filename;
            } else if (isExtended(splitPath[0])) {
                // Case 2: Directory is extended.
                // Check if the file exists in the moddir.
                if (new File(modDirName + filename).exists()) {
                    // It does, so return it.
                    return modDirName + filename;
                } else {
                    // It doesn't, so return the file in the main dir.
                    return mainDirName + filename;
                }
            } else {
                // Case 3: Directory is not modded.
                // Return the file in the main dir.
                return mainDirName + filename;
            }
        } else {
            // EU2-style mod
            if (new File(modDirName + filename).exists())
                return modDirName + filename;
            else
                return mainDirName + filename;
        }
    }
    
    // Yes, there's probably no reason to have this method, but it works, so I'll leave it. -MM Sept 08
    private static String[] splitParent(String path) {
        int separatorIdx = path.indexOf('/');
        if (separatorIdx < 0) {
            separatorIdx = path.indexOf('\\');
            if (separatorIdx < 0)
                return new String[] {"", path};
        }
        
        return new String[] {
            path.substring(0, separatorIdx),
            path.substring((separatorIdx == path.length()-1) ? separatorIdx : separatorIdx+1)
        };
    }
    
    /**
     * Clausewitz 2 (EU4 and later, I think)
     * @return the names of all active province history files for the province
     * @since EUGFile 1.10.00
     */
    public String[] getProvinceHistoryFiles(final int provId) {
        if (usingMod && clausewitz2Mod) {
            String id = Integer.toString(provId);
            File vanillaFile = getFile(mainDirName + "history/provinces", id, true);
            File modFile = getFile(modDirName + "history/provinces", id, true);

            if (modFile == null)
                return new String[] { vanillaFile == null ? null : vanillaFile.getAbsolutePath() };
            else if (vanillaFile == null
                    || vanillaFile.getName().equals(modFile.getName())
                    || isFileReplaced("history/provinces", vanillaFile.getName()))
                return new String[] { modFile.getAbsolutePath() };
            else
                return new String[] { vanillaFile.getAbsolutePath(), modFile.getAbsolutePath() };
        } else {
            return new String[] { getProvinceHistoryFile(provId) };
        }
    }

    /**
     * Clausewitz 2 (EU4 and later, I think)
     * @return the names of all active province history files for the country
     * @since EUGFile 1.10.00
     */
    public String[] getCountryHistoryFiles(final String tag) {
        if (usingMod && clausewitz2Mod) {
            File vanillaFile = getFile(mainDirName + "history/countries", tag, true);
            File modFile = getFile(modDirName + "history/countries", tag, true);

            if (modFile == null)
                return new String[] { vanillaFile == null ? null : vanillaFile.getAbsolutePath() };
            else if (vanillaFile == null
                    || vanillaFile.getName().equals(modFile.getName())
                    || isFileReplaced("history/countries", vanillaFile.getName()))
                return new String[] { modFile.getAbsolutePath() };
            else
                return new String[] { vanillaFile.getAbsolutePath(), modFile.getAbsolutePath() };
        } else {
            return new String[] { getCountryHistoryFile(tag) };
        }
    }
    
    /**
     * Clausewitz 1 method to find the province history file for the given
     * province.<p>
     * Originally package-private, but became public upon moving to eug.shared.
     * @return the name of the province history file, or <code>null</code> if
     * the file could not be found.
     */
    public String getProvinceHistoryFile(final int provId) {
        // Can't use resolveFilename, because we don't know what the file is
        // called.
        if (usingMod) {
            if (isFolderReplaced("history/provinces")) {
                // Case 1: History is replaced.
                // If there is a file in the mod's province history folder that
                // starts with the provId, return it.
                // Otherwise, return null.
                return getFilePath(modDirName + "history/provinces", Integer.toString(provId), true);
            } else if (isExtended("history")) {
                // Case 2: History is extended.
                // If there is a file in the mod's province history folder that
                // starts with the provId, return it.
                // Otherwise, try to return the vanilla file.
                final String filename = getFilePath(
                        modDirName + "history/provinces",
                        Integer.toString(provId),
                        true);
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
        return getFilePath(mainDirName + "history/provinces", Integer.toString(provId), true);
    }


    /**
     * Victoria 2-specific method to find the province history file for the
     * given province.
     * @return the name of the province history file, or <code>null</code> if
     * the file could not be found.
     * @since EUGFile 1.07.00
     */
    public String getVic2ProvinceHistoryFile(final int provId) {
        // Can't use resolveFilename, because we don't know what the file is
        // called.
        if (usingMod) {
            if (isFolderReplaced("history/provinces")) {
                // Case 1: History is replaced.
                // If there is a file in the mod's province history folder that
                // starts with the provId, return it.
                // Otherwise, return null.
                return getVic2ProvHistoryFile(modDirName + "history/provinces",
                        Integer.toString(provId), true);
            } else if (isExtended("history")) {
                // Case 2: History is extended.
                // If there is a file in the mod's province history folder that
                // starts with the provId, return it.
                // Otherwise, try to return the vanilla file.
                final String filename = getVic2ProvHistoryFile(
                        modDirName + "history/provinces",
                        Integer.toString(provId),
                        true);
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
        return getVic2ProvHistoryFile(mainDirName + "history/provinces", Integer.toString(provId), true);
    }

    private String getVic2ProvHistoryFile(String dirName, String prefix, boolean exactMatch) {
        // first try the directory itself in case some files aren't in subfolders
        String ret = getFilePath(dirName, prefix, exactMatch);
        if (ret != null)
            return ret;

        String[] array = enumerateFiles(dirName);
        for (String subdir : array) {
            ret = getFilePath(dirName + File.separator + subdir, prefix, exactMatch);
            if (ret != null)
                return ret;
        }
        return null;
    }
    
    /**
     * Clausewitz 1 method to find the country history file for the given
     * country.
     * @since EUGFile 1.04.00pre1
     */
    public String getCountryHistoryFile(final String tag) {
        if (usingMod) {
            if (isFolderReplaced("history/countries")) {
                return getFilePath(modDirName + "history/countries", tag, false);
            } else if (isExtended("history")) {
                final String filename = getFilePath(
                        modDirName + "history/countries", tag, false);
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
        return getFilePath(mainDirName + "history/countries", tag, false);
    }
    
    private final java.util.Map<String, String[]> directories =
            new java.util.HashMap<String, String[]>();
    
    private File getFile(String dirname, String start, boolean exactMatch) {
        String[] array = enumerateFiles(dirname);
        
        final int length = start.length();

        int index = -Arrays.binarySearch(array, start, String.CASE_INSENSITIVE_ORDER) - 1;
        if (index >= array.length)
            return null;
        
        String name = array[index];
        /*for (String name : array)*/ {
            if (name.substring(0, length).equalsIgnoreCase(start) && //!name.contains("~") &&
                    (!exactMatch || !Character.isLetterOrDigit(name.charAt(length)))) {
                return new File(dirname + File.separatorChar + name);
            }
        }
        return null;
    }
    
    private String getFilePath(final String dirname, final String start, final boolean exactMatch) {
        File f = getFile(dirname, start, exactMatch);
        if (f != null)
            return f.getAbsolutePath();
        return null;
    }

    private String[] enumerateFiles(final String dirname) throws RuntimeException {
        String[] array = directories.get(dirname.toLowerCase());
        if (array == null) {
            File dir = new File(dirname);
            if (!dir.isDirectory())
                return new String[]{};
            
            array = dir.list();
            if (array == null) {
                throw new RuntimeException("Failed to open directory " + dirname);
            }
            Arrays.sort(array, String.CASE_INSENSITIVE_ORDER);
            directories.put(dirname.toLowerCase(), array);
        }
        return array;
    }
    
    /**
     * Directory information for province and country history files is cached,
     * to avoid calling <code>File.listFiles()</code> every time a file is
     * requested. This method clears the caches to force reloading of the
     * directory information.
     */
    public void reset() {
        directories.clear();
    }
    
    /**
     * Returns the prefix used between the main directory name and the mod name.
     * For EU3, this is "mod/".
     */
    public String getModPrefix() {
        return modPrefix;
    }
    
    /**
     * Sets the prefix used between the main directory name and the mod name.
     * For EU3, this is "mod/".
     * <p>
     * For EU2 compatibility, set this to "" and set modFile to
     * <code>false</code>.
     * @param prefix the prefix that all mod directories start with.
     * @see #setModFile
     */
    public void setModPrefix(String prefix) {
        modPrefix = prefix;
        setModName(modName);
    }
    
    /**
     * Returns the main directory name. This method is not generally useful;
     * {@link #getModDirName()} is more common.
     */
    public String getMainDirName() {
        return mainDirName;
    }
    
    /**
     * Returns the fully qualified name of the mod directory, ending with a
     * file separator. Use this method when creating a new file, for example.
     * @since EUGFile 1.04.00pre1
     */
    public String getModDirName() {
        return modDirName;
    }
    
    /**
     * Returns the name of the mod, or "" if no mod is being used.
     * @return the name of the mod being used.
     * @since EUGFile 1.07.00pre1
     */
    public String getModName() {
        return modName;
    }
    
    /**
     * @return true if an EU3-style .mod file is being used.
     */
    public boolean hasModFile() {
        return modFile;
    }
    
    public void setModFile(boolean modFile) {
        this.modFile = modFile;
        setModName(modName);
    }
    
    public boolean isClausewitz2Mod() {
        return clausewitz2Mod;
    }

    public void setClausewitz2Mod(boolean value) {
        this.clausewitz2Mod = value;
    }
    
    /**
     * @return <code>true</code> if the given directory is extended.
     * @since EUGFile 1.04.00pre1
     */
    public boolean isExtended(String directory) {
        if (usingMod) {
            if (clausewitz2Mod)
                return !replaced.contains(directory.toLowerCase());
            else
                return extended.contains(directory.toLowerCase());
        }
        return false;
    }
    
    /**
     * @return <code>true</code> if the given directory is replaced.
     * @since EUGFile 1.04.00pre1
     */
    public boolean isReplaced(String directory) {
        return usingMod && replaced.contains(directory.toLowerCase());
    }

    /**
     * Returns true if the given file or any parent directory is replaced.
     */
    private boolean isFileReplaced(String directory, String filename) {
        if (isFolderReplaced(directory))
            return true;
        
        return replaced.contains(directory.toLowerCase() + "/" + filename.toLowerCase());
    }
    
    private java.util.regex.Pattern SLASH = java.util.regex.Pattern.compile("[//\\\\]");

    /**
     * Returns true if the given directory or any parent directory is replaced.
     */
    private boolean isFolderReplaced(String directory) {
        if (!usingMod)
            return false;

        String[] paths = SLASH.split(directory.toLowerCase());
        StringBuilder pathBuilder = new StringBuilder();
        for (String path : paths) {
            pathBuilder.append(path);
            if (replaced.contains(pathBuilder.toString())) // this is bound to be a lot of duplication
                return true;
            pathBuilder.append("/");
        }
        return false;
    }
}
