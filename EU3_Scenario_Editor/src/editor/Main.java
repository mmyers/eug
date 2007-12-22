/*
 * Main.java
 *
 * Created on January 25, 2007, 4:40 PM
 */

package editor;

import eug.shared.FilenameResolver;
//import javax.swing.UIManager;

/**
 *
 * @author Michael Myers
 */
public class Main {
    
    public static final FilenameResolver filenameResolver =
            new FilenameResolver(new java.io.File("config.txt"));
    
    public static final Map map = new Map();  // must be initialized after filenameResolver
    
    public static final ProvinceData provinceData = new ProvinceData(map);
    
    /** Creates a new instance of Main */
    private Main() { }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Main EU3 directory is " + filenameResolver.getMainDirName());
        
        if (filenameResolver.getModDirName().length() == 0)
            System.out.println("No mod");
        else
            System.out.println("Mod directory is " + filenameResolver.getModDirName());
        
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        new EditorUI().setVisible(true);
    }
    
}
