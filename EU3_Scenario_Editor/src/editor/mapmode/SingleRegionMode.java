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
    
    private boolean isMetaRegion = false;
    
    /** Creates a new instance of RegionsMode */
    public SingleRegionMode(String region) {
        provIds = getMap().getRegion(region);
        if ((provIds == null || provIds.isEmpty()) && getMap().getMetaRegions() != null) {
            provIds = getMap().getMetaRegions().get(region);
            if (provIds != null && !provIds.isEmpty())
                isMetaRegion = true;
        }
    }
    
    public SingleRegionMode(List<Integer> continent) {
        super(continent);
    }
    
    public SingleRegionMode(MapPanel panel, String region) {
        super(panel);
        provIds = getMap().getRegion(region);
        if ((provIds == null || provIds.isEmpty()) && getMap().getMetaRegions() != null) {
            provIds = getMap().getMetaRegions().get(region);
            if (provIds != null && !provIds.isEmpty())
                isMetaRegion = true;
        }
    }
    
    public SingleRegionMode(MapPanel panel, List<Integer> region) {
        super(panel, region);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        StringBuilder ret = new StringBuilder("Regions: ");
        List<String> regions = isMetaRegion ? getMap().getMetaRegionsOfProv(current.getId()) : getMap().getRegionsOfProv(current.getId());
        ret.append(String.join(", ", regions));
        
        return ret.toString();
    }
    
    public boolean isMetaRegion() {
        return isMetaRegion;
    }
}
