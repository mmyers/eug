/*
 * NativeGroupMode.java
 *
 * Created on December 19, 2007, 5:38 PM
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
public class NativeGroupMode extends GroupMode {
    
    public NativeGroupMode(String nativeGroup) {
        provIds = makeIntList(getMap().getNatives(nativeGroup));
    }
    
    public NativeGroupMode(List<String> nativeGroup) {
        super(nativeGroup);
    }
    
    public NativeGroupMode(MapPanel panel, String nativeGroup) {
        super(panel);
        provIds = makeIntList(getMap().getNatives(nativeGroup));
    }
    
    public NativeGroupMode(MapPanel panel, List<String> nativeGroup) {
        super(panel, nativeGroup);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        return "Natives type: " + getMap().getNativeTypeOfProv(current.getId());
    }
}
