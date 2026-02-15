/*
 * CoreMapMode.java
 *
 * Created on June 13, 2007, 4:15 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * <code>MapMode</code> that highlights core provinces of a single country.
 * @author Michael Myers
 */
public class CoreMapMode extends ProvincePaintingMode {
    
    protected String tag;
    protected Color coreColor = Color.GREEN;
    protected Color notCoreColor = new Color(100, 100, 100); // darker than GRAY, lighter than DARK_GRAY
    
    /** Creates a new instance of CoreMapMode */
    public CoreMapMode(String tag) {
        this.tag = tag;
    }
    
    public CoreMapMode(MapPanel panel, String tag) {
        super(panel);
        this.tag = tag;
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final List<String> coreOf = mapPanel.getModel().isCoreOf(provId);
        if (coreOf != null && coreOf.contains(tag)) {
            mapPanel.paintProvince(g, provId, coreColor);
        } else {
            mapPanel.paintProvince(g, provId, notCoreColor);
        }
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Sea zones can't be cores.
        return;
    }

    @Override
    public Object getBorderGroup(final int provId) {
        if (!getMap().isLand(provId))
            return "SEA_ZONE";
        final List<String> coreOf = mapPanel.getModel().isCoreOf(provId);
        if (coreOf != null && coreOf.contains(tag))
            return "CORE";
        return "NOT_CORE";
    }
    
    @Override
    public String getTooltipExtraText(Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        final List<String> coreOf = mapPanel.getModel().isCoreOf(current.getId());
        if (coreOf.isEmpty())
            return "";
        return "Core of: " + coreOf;
    }
    
}
