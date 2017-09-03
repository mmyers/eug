
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import eug.shared.GenericObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Mode for saves mostly which shows which provinces have changed hands the most.
 * @author Michael
 */
public class HotspotMode extends DiscreteScalingMapMode {
    
    public HotspotMode(MapPanel panel, String property, int max, int step) {
        super(panel, property, 0, max, step);
        setMinColor(Color.GREEN.darker());
        setMaxColor(Color.RED.darker());
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        List<String> strings = mapPanel.getModel().getHistStrings(provId, prop); // not very efficient - could precalculate some of this maybe
        int count;
        
        if (strings != null && !strings.isEmpty()) {
            count = strings.size();
        } else {
            List<GenericObject> objects = mapPanel.getModel().getHistObjects(provId, prop);
            if (objects != null) {
                count = objects.size();
            } else {
                mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
                return;
            }
        }
        
        int index = (int) ((count + 0.0) / getStep());
        index = Math.max(0, Math.min(numColors-1, index));
        
        mapPanel.paintProvince(g, provId, colors[index]);
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        List<String> strings = mapPanel.getModel().getHistStrings(id, prop);
        int count = -1;
        
        if (strings != null && !strings.isEmpty()) {
            count = strings.size();
        } else {
            List<GenericObject> objects = mapPanel.getModel().getHistObjects(id, prop);
            if (objects != null) {
                count = objects.size();
            }
        }
        return "\"" + prop + "\" changes: " + count;
    }
    
    @Override
    public String getName() {
        return "hotspot";
    }
}
