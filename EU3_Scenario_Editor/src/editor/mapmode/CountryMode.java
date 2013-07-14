package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A <code>MapMode</code> that paints all land provinces the color of the
 * country that owns them.
 * @author Michael Myers
 * @since 0.4pre1
 */
public class CountryMode extends ProvincePaintingMode {
    
    public CountryMode() {
    }
    
    public CountryMode(MapPanel panel) {
        super(panel);
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final String owner = mapPanel.getModel().getHistString(provId, "owner");
        mapPanel.paintProvince(g, provId, (owner == null ? Utilities.COLOR_NO_HIST : getCtryColor(owner)));
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Don't paint sea zones.
        return;
    }
    
    protected Color getCtryColor(String country) {
        return Utilities.getCtryColor(country);
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        final String ret = Text.getText(mapPanel.getModel().getHistString(id, "owner"));
        if (ret.length() == 0)
            return "";
        return "Owned by: " + ret;
    }
    
}