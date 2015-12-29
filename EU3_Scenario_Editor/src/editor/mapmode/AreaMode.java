
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.util.List;

/**
 * @since 0.9
 */
public class AreaMode extends GroupMode {
    
    public AreaMode(MapPanel panel, String area) {
        super(panel);
        provIds = makeIntList(getMap().getArea(area));
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        StringBuilder ret = new StringBuilder("Areas: ");
        List<String> areas = getMap().getAreasOfProv(current.getId());
        ret.append(String.join(", ", areas));
        
        return ret.toString();
    }
    
}
