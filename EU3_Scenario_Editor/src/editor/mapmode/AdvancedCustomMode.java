/*
 * AdvancedCustomMode.java
 *
 * Created on June 12, 2007, 12:47 PM
 */

package editor.mapmode;

import editor.MapPanel;
import java.awt.Graphics2D;
import java.util.regex.Pattern;

/**
 * Similar to {@link CustomMode}, but with regex support.
 * @author Michael Myers
 */
public class AdvancedCustomMode extends CustomMode {
    
    private final Pattern pattern;
    
    /** Creates a new instance of AdvancedCustomMode */
    public AdvancedCustomMode(String name, String pattern) {
        super(name, pattern);
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
    
    public AdvancedCustomMode(MapPanel panel, String name, String pattern) {
        super(panel, name, pattern);
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final String prop = mapPanel.getModel().getHistString(provId, name);
        
        if (prop == null || !pattern.matcher(prop).matches()) {
            mapPanel.paintProvince(g, provId, notFoundColor);
        } else {
            mapPanel.paintProvince(g, provId, foundColor);
        }
    }
    
    public String toString() {
        return "Provinces with " + name + " matching " + pattern;
    }
}
