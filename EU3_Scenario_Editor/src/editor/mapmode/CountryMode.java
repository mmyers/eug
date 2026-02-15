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
    
    @Override
    protected void paintProvince(final Graphics2D g, int provId) {
        final String owner = mapPanel.getModel().getOwner(provId);
        if (getMap().isWasteland(provId))
            mapPanel.paintProvince(g, provId, java.awt.Color.BLACK);
        else if (owner == null || owner.isEmpty())
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        else
            mapPanel.paintProvince(g, provId, getCtryColor(owner));
    }
    
    @Override
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Don't paint sea zones.
    }
    
    protected Color getCtryColor(String country) {
        return Utilities.getCtryColor(country);
    }

    protected Object getCountryBorderGroup(String country) {
        if (country == null || country.isEmpty() || Utilities.isNotACountry(country)) {
            return "NO_COUNTRY";
        }
        return country.toUpperCase();
    }

    @Override
    public Object getBorderGroup(final int provId) {
        if (getMap().isWasteland(provId)) {
            return "WASTELAND";
        }
        if (!getMap().isLand(provId)) {
            return "SEA_ZONE";
        }
        return getCountryBorderGroup(mapPanel.getModel().getOwner(provId));
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        final String ret = Text.getText(mapPanel.getModel().getOwner(id));
        if (ret.length() == 0)
            return "";
        return "Owned by: " + ret;
    }
    
}