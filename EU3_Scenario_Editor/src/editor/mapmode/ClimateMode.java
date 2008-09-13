/*
 * ClimateMode.java
 *
 * Created on August 1, 2007, 2:19 PM
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
public class ClimateMode extends GroupMode {
    
    /** Creates a new instance of ClimateMode */
    public ClimateMode(String climate) {
        super(Main.map.getClimate(climate));
    }
    
    public ClimateMode(List<String> climate) {
        super(climate);
    }
    
    public ClimateMode(MapPanel panel, String climate) {
        super(panel, Main.map.getClimate(climate));
    }
    
    public ClimateMode(MapPanel panel, List<String> climate) {
        super(panel, climate);
    }
    
    public String getTooltipExtraText(final Province current) {
        if (!editor.Main.map.isLand(current.getId()))
            return "";
        
        return "Climate: " + Main.map.getClimateOfProv(current.getId());
    }
}
