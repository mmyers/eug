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
 * @since 0.6pre1
 */
public class RegionsMode extends GroupMode {
    
    /** Creates a new instance of RegionsMode */
    public RegionsMode(String region) {
        super(Main.map.getRegion(region));
    }
    
    public RegionsMode(List<String> continent) {
        super(continent);
    }
    
    public RegionsMode(MapPanel panel, String region) {
        super(panel, Main.map.getRegion(region));
    }
    
    public RegionsMode(MapPanel panel, List<String> region) {
        super(panel, region);
    }
    
    public String getTooltipExtraText(final Province current) {
        if (!Main.map.isLand(current.getId()))
            return "";
        
        StringBuilder ret = new StringBuilder("Regions: ");
        List<String> regions = Main.map.getRegionsOfProv(current.getId());
        java.util.Iterator<String> itr = regions.iterator();
        while (itr.hasNext()) {
            ret.append(itr.next());
            if (itr.hasNext())
                ret.append(", ");
        }
        
        return ret.toString();
    }
    
}
