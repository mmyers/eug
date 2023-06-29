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
        provIds = getMap().getNatives(nativeGroup);
    }
    
    public NativeGroupMode(List<Integer> nativeGroup) {
        super(nativeGroup);
    }
    
    public NativeGroupMode(MapPanel panel, String nativeGroup) {
        super(panel);
        provIds = getMap().getNatives(nativeGroup);
    }
    
    public NativeGroupMode(MapPanel panel, List<Integer> nativeGroup) {
        super(panel, nativeGroup);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        return "Natives type: " + getMap().getNativeTypeOfProv(current.getId());
    }
}
