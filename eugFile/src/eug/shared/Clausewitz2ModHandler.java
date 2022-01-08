
package eug.shared;

import eug.parser.EUGFileIO;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clausewitz 2 engine mod handler. This mod handler requires a .mod file.
 * Folders are either marked "replace_path" or not. If not, the folder is
 * assumed to be extended (mod files may override vanilla files only if present).
 * Subfolders such as "history/provinces" can be marked as "replace_path"
 * without marking the top-level folder as replace_path.
 * If the mod has dependencies, they are recursively checked before attempting
 * to use a vanilla file or folder.
 * @author Michael
 */
public class Clausewitz2ModHandler extends ClausewitzModHandler implements ModHandler {
    
    protected List<Path> dependencyModPaths;
    
    private Mod baseMod;
    
    public Clausewitz2ModHandler(String baseDir, String modFileName) {
        this.baseDir = Paths.get(baseDir);
        
        if (modFileName == null || modFileName.isEmpty()) {
            modDir = null;
        } else {
            loadModTree(modFileName, true);
        }
    }
    
    private void loadModTree(String modFileName, boolean topLevel) {
        GenericObject mod = EUGFileIO.load(modFileName);

        if (mod == null) {
            if (topLevel)
                modDir = null;
        } else {
            modDir = this.baseDir.resolve(mod.getString("path"));

            replaced = new HashSet<>();

            // Lowercase the strings so that resolution is case-insensitive
            for (String str : mod.getStrings("replace_path")) {
                // may need to create Paths and convert back to string to avoid file separator issues
                replaced.add(str.toLowerCase());
            }

            // if mod has dependencies, then we have to load every mod file
            // since dependencies are indicated by mod name, not by mod file name
            if (mod.containsList("dependencies")) {
                dependencyModPaths = new ArrayList<>();
                Map<String, Mod> mods = loadMods(this.baseDir.resolve("mod"));
                
                baseMod = mods.get(mod.getString("name"));
                
                //loadModHierarchy(mod.getList("dependencies"));
                
                for (String depModName : mod.getList("dependencies")) {
                    Mod depMod = mods.get(depModName);
                    baseMod.addDependency(depMod);
                    
                    if (depMod.dependenciesRaw != null) {
                        
                    }
                }
            } else {
                baseMod = new Mod() {{
                    name = mod.getString("name");
                    modPath = baseDir.resolveSibling(mod.getString("path"));
                    addReplaced(mod.getStrings("replace_path"));
                }};
            }
        }
    }
    
    /**
     * Returns a mapping of mod name to mod file path
     */
    private Map<String, Mod> loadMods(Path modDirPath) {
        Map<String, Mod> ret = new HashMap<>();
        
        File[] modFiles = modDirPath.toFile().listFiles((File dir, String filename) -> filename.endsWith(".mod"));
        
        for (File f : modFiles) {
            GenericObject mod = EUGFileIO.load(f);
            ret.put(mod.getString("name"), new Mod() {{
                name = mod.getString("name");
                modFileName = f.getAbsolutePath();
                modPath = modDirPath.resolveSibling(mod.getString("path"));
                dependenciesRaw = mod.getList("dependencies");
                addReplaced(mod.getStrings("replace_path"));
            }});
        }
        
        return ret;
    }
    
    @Override
    protected boolean isExtended(Path dir) {
        if (modDir == null)
            return false;
        
        return !isFolderReplaced(dir);
    }
    
    @Override
    protected boolean isFileReplaced(Path directory, Path filename) {
        if (isFolderReplaced(directory))
            return true;
        if (modDir == null)
            return false;
        
        String relativePathName = filename.isAbsolute() ? baseDir.relativize(directory.resolve(filename)).toString() : filename.toString();
        return baseMod.doesAnyReplace(relativePathName.toLowerCase());
        //return replaced.contains(relativePathName.toLowerCase());
    }
    
    @Override
    protected boolean isFolderReplaced(Path directory) {
        if (modDir == null)
            return false;
        if (directory == null)
            return false;
        
        // turn a path like "C:/Games/Europa Universalis III/history/provinces"
        // into "history/provinces" and then into "history"
        // then iterate through subfolders and see if any is replaced
        if (directory.isAbsolute())
            directory = baseDir.relativize(directory);
        if (baseMod.doesAnyReplace(directory.getName(0).toString().toLowerCase()))
//        if (replaced.contains(directory.getName(0).toString().toLowerCase()))
            return true;
        
        for (int i = 1; i <= directory.getNameCount(); i++) {
            Path p = directory.subpath(0, i);
            if (baseMod.doesAnyReplace(p.toString().toLowerCase()))
//            if (replaced.contains(p.toString().toLowerCase()))
                return true;
        }
        
        return false;
    }
    
    // Mods exist in a tree structure
    private static class Mod {
        String name;
        String modFileName;
        Path modPath;
        GenericList dependenciesRaw;
        List<Mod> dependencies = new ArrayList<>();
        
        Set<String> replaced = new HashSet<>();
        
        public void addDependency(Mod mod) {
            dependencies.add(mod);
        }
        
        public void addReplaced(List<String> replaced) {
            for (String s : replaced) {
                s = s.replace("//", File.separator).replace("/", File.separator);
                this.replaced.add(s);
            }
        }
        
        /**
         * Returns true if this mod or any of its dependencies has the given
         * path marked as replace_path.
         * @param path a relative path, such as "common/countries"
         * @return whether the path is replaced by a mod
         */
        public boolean doesAnyReplace(String path) {
            return getReplacingMod(path) != null;
        }
        
        public Mod getReplacingMod(String path) {
            if (replaced.contains(path))
                return this;
            
            for (Mod dependency : dependencies) {
                if (dependency.doesAnyReplace(path))
                    return dependency;
            }
            
            return null;
        }
        
        public Path resolveFilename(String filename) {
            // first try to resolve on this mod, then recursively on any dependencies
            
            // if file exists, always return it
            Path p = modPath.resolve(filename);
            if (p.toFile().exists()) {
                return p;
            }
            
            // if path is replaced, return it even if not present
            if (replaced.contains(filename))
                return p;
            
            // file doesn't exist and isn't in replace_path: check parent mods
            for (Mod m : dependencies) {
                Path mp = m.resolveFilename(filename);
                if (mp != null)
                    return mp;
            }
            
            // welp
            return null;
        }
        
        public Path resolveDirectory(String path) {
            // first try to resolve on this mod, then recursively on any dependencies
            
            Path p = Paths.get(path);
            Path parentDir = p.getParent();
            Path pathInMod = modPath.resolve(p);
            
            if (replaced.contains(path) || replaced.contains(parentDir.toString())) { // TODO replacements of parent dirs (e.g. replacing common should replace all subfolders)
                return pathInMod;
            } else if (pathInMod.toFile().exists()) {
                return pathInMod;
            } else {
                // this mod doesn't contain or replace the directory
                // so check parent mods
                for (Mod m : dependencies) {
                    Path mp = m.resolveDirectory(path);
                    if (mp != null)
                        return mp;
                }
            }
            
            // welp
            return null;
        }
    }
}
