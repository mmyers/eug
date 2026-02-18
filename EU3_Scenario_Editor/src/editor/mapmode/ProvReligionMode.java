/*
 * ProvReligionMode.java
 *
 * Created on June 12, 2007, 2:00 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Graphics2D;

/**
 *
 * @author Michael Myers
 */
public class ProvReligionMode extends ProvincePaintingMode {
    
    /**
     * Creates a new instance of ProvReligionMode.
     */
    public ProvReligionMode() {
    }
    
    public ProvReligionMode(MapPanel panel) {
        super(panel);
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final String religion = mapPanel.getModel().getHistString(provId, "religion");
        if (mapPanel.getMap().isWasteland(provId))
            mapPanel.paintProvince(g, provId, java.awt.Color.BLACK);
        else if (religion == null) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_HIST);
        } else if (religion.length() == 0 || religion.equalsIgnoreCase("none")) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_RELIGION);
        } else {
            mapPanel.paintProvince(g, provId, Utilities.getReligionColor(religion));
        }
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Do nothing
        return;
    }

    @Override
    public Object getBorderGroup(final int provId) {
        if (mapPanel.getMap().isWasteland(provId))
            return "WASTELAND";
        if (!getMap().isLand(provId))
            return "SEA_ZONE";

        final String religion = mapPanel.getModel().getHistString(provId, "religion");
        if (religion == null)
            return "MISSING";
        if (religion.length() == 0 || religion.equalsIgnoreCase("none"))
            return "NONE";
        return religion.toLowerCase();
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        final String ret = Text.getText(mapPanel.getModel().getHistString(current.getId(), "religion"));
        if (ret == null || ret.length() == 0)
            return "";
        return "Religion: " + ret;
    }
}
