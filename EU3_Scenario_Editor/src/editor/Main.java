/*
 * Main.java
 *
 * Created on January 25, 2007, 4:40 PM
 */

package editor;

import eug.parser.EUGFileIO;
import eug.shared.FilenameResolver;
//import javax.swing.UIManager;

/**
 *
 * @author Michael Myers
 */
public class Main {
    
    public static final FilenameResolver filenameResolver =
            new FilenameResolver(new java.io.File("config.txt"));
    
    public static final GameVersion gameVersion = setVersion();
    
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
//            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        new EditorUI().setVisible(true);
    }

    private static GameVersion setVersion() {
        String version = EUGFileIO.load("config.txt").getString("gameversion").toLowerCase();
        // bring on the Java 7 string switch statement!
        if (version.equals("eu3 vanilla"))
            return GameVersion.VANILLA;
        else if (version.equals("in nomine")) {
            return GameVersion.IN_NOMINE;
        } else if (version.equals("httt")) {
            return GameVersion.HEIR_TO_THE_THRONE;
        } else if (version.equals("divine wind")) {
            return GameVersion.DIVINE_WIND;
        } else if (version.equals("victoria 2")) {
            return GameVersion.VICTORIA;
        } else if (version.equals("hoi3")) {
            return GameVersion.HOI3;
        } else if (version.equals("rome")) {
            return GameVersion.ROME;
        }
        System.err.println("Game version '" + version + "' not recognized");
        return GameVersion.VANILLA;
    }
    
}
