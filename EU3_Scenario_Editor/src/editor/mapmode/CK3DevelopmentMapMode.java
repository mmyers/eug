
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import java.awt.Color;

/**
 *
 * @author Michael
 */
public class CK3DevelopmentMapMode extends TitleMode {

    private final Color[] colors = Utilities.createSteppedColors(0, 101, 1, Color.WHITE, Color.GREEN.darker(), Color.GREEN.darker().darker().darker());
    
    public CK3DevelopmentMapMode(MapPanel panel) {
        super(panel, TitleMode.TitleType.COUNTY);
    }

    @Override
    protected Color getTitleColor(String title) {
        String devStr = getLiegeHistString(title, "change_development_level");
        if (devStr == null || devStr.isEmpty())
            return getColorFromDevLevel(0);
        
        try {
            int devLevel = Integer.parseInt(devStr);
            return getColorFromDevLevel(devLevel);
        } catch (NumberFormatException ex) {
        }
        return Utilities.COLOR_NO_CTRY_DEF;
    }

    private Color getColorFromDevLevel(int devLevel) {
        if (devLevel >= 0 && devLevel < colors.length)
            return colors[devLevel];
        return Utilities.COLOR_LAND_DEFAULT;
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        if (!mapPanel.getMap().isLand(current.getId()) || mapPanel.getMap().isWasteland(current.getId()))
            return "";
        
        return "Development level: " + getLiegeHistString(getLowestHistTitleHolder(current.getId()), "change_development_level");
    }
    
}
