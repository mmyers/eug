/*
 * ProvReligionMode.java
 *
 * Created on June 12, 2007, 2:00 PM
 */

package editor.mapmode;

import editor.Main;
import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;

/**
 *
 * @author Michael Myers
 */
public class ProvReligionMode extends ProvincePaintingMode {
    
    /**
     * Creates a new instance of ProvReligionMode.
     */
    public ProvReligionMode() {
        super();
    }
    
    public ProvReligionMode(MapPanel panel) {
        super(panel);
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final String religion = mapPanel.getModel().getHistString(provId, "religion");
        if (religion == null) {
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
    public String getTooltipExtraText(final Province current) {
        if (!editor.Main.map.isLand(current.getId()))
            return "";
        
        final String ret = Text.getText(mapPanel.getModel().getHistString(current.getId(), "religion"));
        if (ret.length() == 0)
            return "";
        return "Religion: " + ret;
    }
}
