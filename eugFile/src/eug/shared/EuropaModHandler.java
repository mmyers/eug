
package eug.shared;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Europa engine mod handler. This class has very basic mod handling behavior.
 * If a file is in the mod directory, it overrides a file in the base game directory.
 * @author Michael
 */
public class EuropaModHandler implements ModHandler {
    
    private final Path baseDir;
    private final Path modDir;
    
    /**
     * Creates a new mod handler with the specified base (game) directory and
     * mod directory. If @{code modDir} is null, only vanilla files will be used.
     * @param baseDir
     * @param modDir 
     */
    public EuropaModHandler(String baseDir, String modDir) {
        this.baseDir = Paths.get(baseDir);
        //if (!this.baseDir.toFile().exists())
        //    throw new IllegalArgumentException("Base dir " + baseDir + " does not exist");
        
        if (modDir == null || modDir.isEmpty())
            this.modDir = null;
        else if (new File(modDir).exists())
            this.modDir = Paths.get(modDir);
        else //if (new File(baseDir + File.separator + modDir).exists())
            this.modDir = Paths.get(baseDir, modDir);
        //else
        //    throw new IllegalArgumentException("Mod dir must either be a subfolder of the game folder or else a fully qualified pathname");
    }
    
    @Override
    public String resolveDirectory(String path) {
        // Europa engine resolves files and directories the same
        return resolveFilename(path) + File.separatorChar;
    }

    @Override
    public String resolveFilename(String filename) {
        if (modDir != null) {
            Path modPath = modDir.resolve(filename);
            if (modPath.toFile().exists())
                return modPath.toAbsolutePath().toString();
        }
        
        return baseDir.resolve(filename).toAbsolutePath().toString();
    }

    @Override
    public File[] listFiles(String path) {
        return new File(resolveDirectory(path)).listFiles();
    }
}
