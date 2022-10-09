
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
        if (mapPanel.getMap().isWasteland(provId)) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_CULTURE);
            return;
        }
            
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
        
        final String cultureTag = mapPanel.getModel().getHistString(current.getId(), "culture");
        final String cultureText = Text.getText(cultureTag);
        if (cultureText == null || cultureText.length() == 0)
            return "";
        final String cultureGroupTag = Utilities.getCultureGroup(cultureTag);
        java.awt.Color c = Utilities.getCultureColor(cultureTag);
        String ret = "Culture: " + cultureTag + " (" + cultureText + ")<br>";
        ret += "Group: " + cultureGroupTag + " (" + Text.getText(cultureGroupTag) + ")<br>";
        ret += "Color: " + c.getRed() + " " + c.getGreen() + " " + c.getBlue();
        return ret;
    }
}
