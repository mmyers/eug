/*
 * CustomMode.java
 *
 * Created on June 11, 2007, 12:15 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A <code>MapMode</code> that paints provinces based on a property defined in
 * either the constructor or in {@link #setName}.
 * @author Michael Myers
 */
public class CustomMode extends ProvincePaintingMode {
    
    protected static final Color COLOR_ERROR = Color.RED;
    protected Color foundColor = Color.GREEN;
    protected Color notFoundColor = new Color(100, 100, 100); // darker than GRAY, lighter than DARK_GRAY
    
    protected String name;
    protected String value;
    
    /** Creates a new instance of CustomMode */
    public CustomMode(String name, String value) {
        super();
        this.name = name;
        this.value = value.toLowerCase();
    }
    
    public CustomMode(MapPanel panel, String name, String value) {
        super(panel);
        this.name = name;
        this.value = value.toLowerCase();
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final String prop = mapPanel.getModel().getHistString(provId, name);
        
        if (prop == null || !value.equals(prop.toLowerCase())) {
            mapPanel.paintProvince(g, provId, notFoundColor);
        } else {
            mapPanel.paintProvince(g, provId, foundColor);
        }
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Default is to do nothing
        return;
    }
    
    public String getTooltipExtraText(final Province current) {
        // Default is to only show extra text for land provinces
        if (!editor.Main.map.isLand(current.getId()))
            return "";
        
        final String prop = mapPanel.getModel().getHistString(current.getId(), name);
        if (prop == null || prop.length() == 0)
            return "";
        return name + ": " + prop;
    }
    
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value.toLowerCase();
    }
    
    public Color getFoundColor() {
        return foundColor;
    }
    
    public void setFoundColor(Color foundColor) {
        this.foundColor = foundColor;
    }
    
    public Color getNotFoundColor() {
        return notFoundColor;
    }
    
    public void setNotFoundColor(Color notFoundColor) {
        this.notFoundColor = notFoundColor;
    }
    
    public String toString() {
        return "Provinces with " + name + " = " + value;
    }
    
}
