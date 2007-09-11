package editor.mapmode;

import editor.MapPanel;
import java.awt.Graphics2D;


/**
 * A <code>MapMode</code> that only paints the province map.
 * @author Michael Myers
 * @since 0.4pre1
 */
public class ProvinceMode extends MapMode {
    public ProvinceMode() {
        super();
    }
    
    public ProvinceMode(MapPanel panel) {
        super(panel);
    }
    
    public void paint(Graphics2D g) {
        paintBackground(g);
    }
    
    public void paintBackground(Graphics2D g) {
        mapPanel.paintProvinces(g);
    }

    public boolean paintsBorders() {
        return false;
    }
    
}