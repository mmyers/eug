
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.util.List;

/**
 * Highlights a single province group as defined in provincegroup.txt
 * @since 0.9
 */
public class ProvinceGroupMode extends GroupMode {
    
    public ProvinceGroupMode(MapPanel panel, String group) {
        super(panel);
        provIds = getMap().getProvinceGroup(group);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        StringBuilder ret = new StringBuilder("Province groups: ");
        List<String> groups = getMap().getGroupsOfProv(current.getId());
        ret.append(String.join(", ", groups));
        
        return ret.toString();
    }
    
}
