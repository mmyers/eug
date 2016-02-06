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
import java.util.logging.Level;

/**
 *
 * @author Michael Myers
 * @since 0.5pre3
 */
public class GroupMode extends ProvincePaintingMode {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(GroupMode.class.getName());
    
    protected List<Integer> provIds;
    
    protected Color foundColor = Color.GREEN;
    protected Color notFoundColor = new Color(100, 100, 100); // darker than GRAY, lighter than DARK_GRAY
    
    public GroupMode(List<String> provIds) {
        this.provIds = makeIntList(provIds);
    }
    
    public GroupMode(MapPanel panel, List<String> provIds) {
        super(panel);
        this.provIds = makeIntList(provIds);
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
    
    protected final List<Integer> makeIntList(List<String> provIds) {
        final List<Integer> ret = new ArrayList<>(provIds.size());
        for (String id : provIds) {
            if (id.equals("peace") || id.equals("ai_prio"))
                continue;
            
            try {
                ret.add(Integer.parseInt(id));
            } catch (NumberFormatException ex) {
                log.log(Level.WARNING, "Expected province ID but found {0}; list was {1}", new Object[]{id, provIds});
            }
        }
        return ret;
    }
    
}
