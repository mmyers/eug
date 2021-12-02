package eug.shared;

/**
 *
 * @author Michael
 */
public interface ModHandler {
    
    public String resolveDirectory(String path);
    public String resolveFilename(String filename);
    public String[] listFiles(String path);
}
