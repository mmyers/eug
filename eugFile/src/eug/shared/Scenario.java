/*
 * Scenario.java
 *
 * Created on June 23, 2006, 6:50 PM
 */

package eug.shared;

import eug.parser.EUGFileIO;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Myers
 */
public abstract class Scenario {
    
    public GenericObject root;
    public List<GenericObject> countries;
    public Map<Integer, GenericObject> provinces;
    public String scenarioName;
    public Map<String, String> displayNames;
    protected Style saveStyle = Style.EU3_SAVE_GAME;
    
    /** Creates a new instance of Scenario */
    public Scenario() {
    }
    
    public void setStyle(Style style) {
        this.saveStyle = style;
    }
    
    public GenericObject getRoot() {
        return root;
    }
    
    public int numCountries() {
        return countries.size();
    }
    
    /**
     * Returns the display name of the given value as found in the game text
     * files, or the string itself if it cannot be found.
     */
    public String getDisplayName(String value) {
        
        String s = displayNames.get(value.toLowerCase());
        
        if (s == null) {
            String stab[] = value.split("_");
            return stab[stab.length-1];
        }
        
        return s;
    }
    
    
    public void saveFile(String path) {
        eug.parser.EUGFileIO.save(root, path, EUGFileIO.NO_COMMENT, true, saveStyle);
    }
    
}
