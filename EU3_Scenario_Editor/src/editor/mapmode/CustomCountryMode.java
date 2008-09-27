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
 * either the constructor or in {@link #setName}.
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
        super();
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
            country = "NAT";
//        if (noCountryPattern.matcher(country).matches())
//            country = "NAT";
        
        if (value.equals(mapPanel.getModel().getHistString(country, name).toLowerCase()))
            return foundColor;
        else
            return notFoundColor;
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!editor.Main.map.isLand(id))
            return "";
        
        final String tag = mapPanel.getModel().getHistString(id, "owner");
        if (tag.length() == 0)
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
