
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.util.List;

/**
 * @since 0.9
 */
public class SingleAreaMode extends GroupMode {
    
    public SingleAreaMode(MapPanel panel, String area) {
        super(panel);
        provIds = getMap().getArea(area);
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
