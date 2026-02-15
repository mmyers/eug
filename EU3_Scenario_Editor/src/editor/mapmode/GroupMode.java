/*
 * GroupMode.java
 *
 * Created on July 31, 2007, 8:32 PM
 */

package editor.mapmode;

import editor.MapPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 *
 * @author Michael Myers
 * @since 0.5pre3
 */
public class GroupMode extends ProvincePaintingMode {
    
    protected List<Integer> provIds;
    
    protected Color foundColor = Color.GREEN;
    protected Color notFoundColor = new Color(100, 100, 100); // darker than GRAY, lighter than DARK_GRAY
    
    public GroupMode(List<Integer> provIds) {
        this.provIds = provIds;
    }
    
    public GroupMode(MapPanel panel, List<Integer> provIds) {
        super(panel);
        this.provIds = provIds;
    }

    protected GroupMode() {
    }

    protected GroupMode(MapPanel panel) {
        super(panel);
    }
    
    @Override
    protected void paintProvince(final Graphics2D g, int provId) {
        if (provIds.contains(provId)) {
            mapPanel.paintProvince(g, provId, foundColor);
        } else {
            mapPanel.paintProvince(g, provId, notFoundColor);
        }
    }
    
    @Override
    protected void paintSeaZone(final Graphics2D g, int id) {
        if (provIds.contains(id)) {
            mapPanel.paintProvince(g, id, foundColor);
        }
    }

    @Override
    public Object getBorderGroup(final int provId) {
        return provIds.contains(provId);
    }
}
