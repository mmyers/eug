/*
 * MapMode.java
 *
 * Created on June 7, 2007, 5:45 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author Michael Myers
 * @version 1.0
 * @since 0.4pre1
 */
public abstract class MapMode {
    
    protected MapPanel mapPanel;
    
    /**
     * Creates a new instance of MapMode.
     */
    public MapMode() {
        this(null);
    }
    
    public MapMode(MapPanel panel) {
        setMapPanel(panel);
    }
    
    public MapPanel getMapPanel() {
        return mapPanel;
    }
    
    public void setMapPanel(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }
    
    public abstract void paint(final Graphics2D g);
    
    public void paint(final Graphics g) {
        paint((Graphics2D) g);
    }
    
    public String getTooltipExtraText(final Province current) {
        return "";
    }

    public abstract boolean paintsBorders();
}
