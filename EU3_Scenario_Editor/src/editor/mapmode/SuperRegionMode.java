
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.util.List;

/**
 * @since 0.9
 */
public class SuperRegionMode extends GroupMode {
    
    public SuperRegionMode(MapPanel panel, String superRegion) {
        super(panel);
        provIds = getMap().getSuperRegion(superRegion);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        StringBuilder ret = new StringBuilder("Super regions: ");
        List<String> regions = getMap().getSuperRegionsOfProv(current.getId());
        ret.append(String.join(", ", regions));
        
        return ret.toString();
    }
    
}
