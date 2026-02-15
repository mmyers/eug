/*
 * CustomCountryMode.java
 *
 * Created on July 30, 2007, 11:19 AM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Color;

/**
 * A <code>MapMode</code> that paints countries based on a property defined in
 * the constructor.
 * @author Michael Myers
 * @since 0.5pre3
 */
public class CustomCountryMode extends CountryMode {
    
    protected static final Color COLOR_ERROR = Color.RED;
    protected Color foundColor = Color.GREEN;
    protected Color notFoundColor = new Color(100, 100, 100); // darker than GRAY, lighter than DARK_GRAY
    
    protected String name;
    protected String value;
    
    /** Creates a new instance of CustomCountryMode */
    public CustomCountryMode(String name, String value) {
        this.name = name;
        this.value = value.toLowerCase();
    }
    
    public CustomCountryMode(MapPanel panel, String name, String value) {
        super(panel);
        this.name = name;
        this.value = value.toLowerCase();
    }
    
    
    @Override
    protected Color getCtryColor(String country) {
        country = country.toUpperCase();
        
        // Special case: XXX means no one
        if (Utilities.isNotACountry(country))
            return notFoundColor;
        
        if (value.equals(mapPanel.getModel().getHistString(country, name).toLowerCase()))
            return foundColor;
        else
            return notFoundColor;
    }

    @Override
    protected Object getCountryBorderGroup(String country) {
        if (country == null || country.isEmpty() || Utilities.isNotACountry(country))
            return "NO_COUNTRY";

        country = country.toUpperCase();
        String histValue = mapPanel.getModel().getHistString(country, name);
        if (histValue == null)
            return "MISSING";
        return value.equals(histValue.toLowerCase());
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        final String tag = mapPanel.getModel().getOwner(id);
        if (tag == null || tag.length() == 0)
            return "";
        final String owner = Text.getText(tag);
        final StringBuilder ret = new StringBuilder("Owned by: ").append(owner).append("<br />");
        ret.append(Text.getText(name)).append(": ").append(Text.getText(mapPanel.getModel().getHistString(tag, name)));
        return ret.toString();
    }
    
    @Override
    public String toString() {
        return "Countries with " + name + " = " + value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
