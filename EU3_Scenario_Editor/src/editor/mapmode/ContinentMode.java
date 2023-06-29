/*
 * ContinentMode.java
 *
 * Created on August 1, 2007, 9:22 AM
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
public class ContinentMode extends GroupMode {
    
    /** Creates a new instance of ContinentMode */
    public ContinentMode(String continent) {
        provIds = getMap().getContinent(continent);
    }
    
    public ContinentMode(List<Integer> continent) {
        super(continent);
    }
    
    public ContinentMode(MapPanel panel, String continent) {
        super(panel);
        provIds = getMap().getContinent(continent);
    }
    
    public ContinentMode(MapPanel panel, List<Integer> continent) {
        super(panel, continent);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        String continent = getMap().getContinentOfProv(current.getId());
        if (continent.startsWith("("))
            return "";
        
        return "Continent: " + continent;
    }
    
}
