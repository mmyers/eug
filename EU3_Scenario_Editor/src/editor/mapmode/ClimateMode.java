/*
 * ClimateMode.java
 *
 * Created on August 1, 2007, 2:19 PM
 */

package editor.mapmode;

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
        provIds = getMap().getClimate(climate);
    }
    
    public ClimateMode(List<Integer> climate) {
        super(climate);
    }
    
    public ClimateMode(MapPanel panel, String climate) {
        super(panel);
        provIds = getMap().getClimate(climate);
    }
    
    public ClimateMode(MapPanel panel, List<Integer> climate) {
        super(panel, climate);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        return "Climate: " + getMap().getClimateOfProv(current.getId());
    }
}
