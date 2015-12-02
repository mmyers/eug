
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Graphics2D;

/**
 *
 * @author Michael Myers
 */
public class ProvCultureMode extends ProvincePaintingMode {
    
    public ProvCultureMode() {
    }
    
    public ProvCultureMode(MapPanel panel) {
        super(panel);
    }
    
    @Override
    protected void paintProvince(final Graphics2D g, int provId) {
        final String culture = mapPanel.getModel().getHistString(provId, "culture");
        if (culture == null) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_HIST);
        } else if (culture.length() == 0 || culture.equalsIgnoreCase("none")) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_CULTURE);
        } else {
            mapPanel.paintProvince(g, provId, Utilities.getCultureColor(culture));
        }
    }
    
    @Override
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Do nothing
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        final String culture = mapPanel.getModel().getHistString(current.getId(), "culture");
        final String ret = Text.getText(culture);
        if (ret == null || ret.length() == 0)
            return "";
        return "Culture: " + ret + " (" + Text.getText(Utilities.getCultureGroup(culture)) + ")";
    }
}
