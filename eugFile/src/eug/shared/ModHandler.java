package eug.shared;

import java.io.File;

/**
 *
 * @author Michael
 */
public interface ModHandler {
    
    public String resolveDirectory(String path);
    public String resolveFilename(String filename);
    public File[] listFiles(String path);
}
