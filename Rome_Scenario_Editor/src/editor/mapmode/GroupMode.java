/*
 * GroupMode.java
 *
 * Created on July 31, 2007, 8:32 PM
 */

package editor.mapmode;

import editor.MapPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
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
    
    /**
     * Creates a new instance of GroupMode
     */
    public GroupMode(List<String> provIds) {
        super();
        this.provIds = makeIntList(provIds);
    }
    
    public GroupMode(MapPanel panel, List<String> provIds) {
        super(panel);
        this.provIds = makeIntList(provIds);
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        if (provIds.contains(provId)) {
            mapPanel.paintProvince(g, provId, foundColor);
        } else {
            mapPanel.paintProvince(g, provId, notFoundColor);
        }
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        if (provIds.contains(id)) {
            mapPanel.paintProvince(g, id, foundColor);
        }
    }
    
    private List<Integer> makeIntList(List<String> provIds) {
        final List<Integer> ret = new ArrayList<Integer>(provIds.size());
        for (String id : provIds) {
            ret.add(Integer.parseInt(id));
        }
        return ret;
    }
    
}
