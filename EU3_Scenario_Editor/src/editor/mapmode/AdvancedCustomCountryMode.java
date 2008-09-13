/*
 * AdvancedCustomCountryMode.java
 *
 * Created on July 30, 2007, 9:30 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Color;
import java.util.regex.Pattern;

/**
 *
 * @author Michael Myers
 * @since 0.5pre3
 */
public class AdvancedCustomCountryMode extends CustomCountryMode {
    
    private final Pattern pattern;
    
    /** Creates a new instance of AdvancedCustomCountryMode */
    public AdvancedCustomCountryMode(String name, String pattern) {
        super(name, pattern);
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
    
    public AdvancedCustomCountryMode(MapPanel panel, String name, String pattern) {
        super(panel, name, pattern);
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
    
    protected Color getCtryColor(String country) {
        country = country.toUpperCase();
        
        final String prop = mapPanel.getModel().getHistString(country, name);
        
        if (prop == null || !pattern.matcher(prop).matches()) {
            return notFoundColor;
        } else {
            return foundColor;
        }
    }
    
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
    
    public String toString() {
        return "Countries with " + name + " matching " + pattern;
    }
}
