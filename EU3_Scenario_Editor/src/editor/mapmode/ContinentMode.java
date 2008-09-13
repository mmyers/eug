/*
 * ContinentMode.java
 *
 * Created on August 1, 2007, 9:22 AM
 */

package editor.mapmode;

import editor.Main;
import editor.MapPanel;
import editor.ProvinceData.Province;
import java.util.List;

/**
 *
 * @author Michael Myers
 * @since 0.5pre3
 */
public class ContinentMode extends GroupMode {
    
    /** Creates a new instance of ContinentMode */
    public ContinentMode(String continent) {
        super(Main.map.getContinent(continent));
    }
    
    public ContinentMode(List<String> continent) {
        super(continent);
    }
    
    public ContinentMode(MapPanel panel, String continent) {
        super(panel, Main.map.getContinent(continent));
    }
    
    public ContinentMode(MapPanel panel, List<String> continent) {
        super(panel, continent);
    }
    
    public String getTooltipExtraText(final Province current) {
        if (!Main.map.isLand(current.getId()))
            return "";
        
        return "Continent: " + Main.map.getContinentOfProv(current.getId());
    }
    
}
