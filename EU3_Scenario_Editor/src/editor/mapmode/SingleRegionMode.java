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
 * @since 0.6pre1
 */
public class SingleRegionMode extends GroupMode {
    
    /** Creates a new instance of RegionsMode */
    public SingleRegionMode(String region) {
        provIds = makeIntList(getMap().getRegion(region));
    }
    
    public SingleRegionMode(List<String> continent) {
        super(continent);
    }
    
    public SingleRegionMode(MapPanel panel, String region) {
        super(panel);
        provIds = makeIntList(getMap().getRegion(region));
    }
    
    public SingleRegionMode(MapPanel panel, List<String> region) {
        super(panel, region);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        StringBuilder ret = new StringBuilder("Regions: ");
        List<String> regions = getMap().getRegionsOfProv(current.getId());
        ret.append(String.join(", ", regions));
        
        return ret.toString();
    }
    
}
