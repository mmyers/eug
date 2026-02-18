
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
    protected double getProvinceValue(int provId) {
        List<String> strings = mapPanel.getModel().getHistStrings(provId, prop);
        if (strings != null && !strings.isEmpty())
            return strings.size();
        
        List<GenericObject> objects = mapPanel.getModel().getHistObjects(provId, prop);
        if (objects != null)
            return objects.size();
        
        return 0;
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        return prop + " changes: " + (int)getProvinceValue(id);
    }
    
    @Override
    public String getName() {
        return "hotspot";
    }
}
