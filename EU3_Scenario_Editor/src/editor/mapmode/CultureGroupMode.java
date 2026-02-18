
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Michael
 */
public class CultureGroupMode extends ProvincePaintingMode {
    
    private final String cultureGroup;

    public CultureGroupMode(MapPanel panel, String cultureGroup) {
        super(panel);
        this.cultureGroup = cultureGroup;
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        if (mapPanel.getMap().isWasteland(provId)) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_CULTURE);
            return;
        }
            
        final String pCulture = mapPanel.getModel().getHistString(provId, "culture");
        if (pCulture != null) {
            String pGroup = Utilities.getCultureGroup(pCulture);
            if (pGroup != null && pGroup.equalsIgnoreCase(this.cultureGroup)) {
                // randomly assign a color based on the culture name
                Color c = Utilities.getGeographyColors().get(Math.abs(pCulture.hashCode()) % Utilities.getGeographyColors().size());
                mapPanel.paintProvince(g, provId, c);
                return;
            }
        }
        mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        // do nothing
    }

    @Override
    public Object getBorderGroup(final int provId) {
        if (mapPanel.getMap().isWasteland(provId))
            return "WASTELAND";
        if (!getMap().isLand(provId))
            return "SEA_ZONE";

        final String pCulture = mapPanel.getModel().getHistString(provId, "culture");
        if (pCulture != null) {
            String pGroup = Utilities.getCultureGroup(pCulture);
            if (pGroup != null && pGroup.equalsIgnoreCase(this.cultureGroup))
                return pCulture.toLowerCase();
        }
        return "OTHER";
    }
    
    @Override
    public String getTooltipExtraText(final ProvinceData.Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        final String cultureTag = mapPanel.getModel().getHistString(current.getId(), "culture");
        final String cultureText = Text.getText(cultureTag);
        if (cultureText == null || cultureText.length() == 0)
            return "";
        final String cultureGroupTag = Utilities.getCultureGroup(cultureTag);
        String ret = "Culture: " + cultureTag + " (" + cultureText + ")<br>";
        ret += "Group: " + cultureGroupTag + " (" + Text.getText(cultureGroupTag) + ")<br>";
        return ret;
    }
}
