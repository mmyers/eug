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
    
    private final GenericObject religions = EUGFileIO.load(
            Main.filenameResolver.resolveFilename("common/religion.txt"),
            ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
            );
    private final java.util.Map<String, Color> relColorCache =
            new HashMap<String, Color>();
    
    /** Color used if the province history cannot be found. */
    private static final Color COLOR_NO_HIST = Color.RED;
    /** Color used if the country definition cannot be found. */
    private static final Color COLOR_NO_CTRY_DEF = Color.BLACK;
    /** Color used if the religion definition cannot be found. */
    private static final Color COLOR_NO_RELIGION_DEF = Color.BLACK;
    /** Color used if a land province does not have a defined religion. */
    private static final Color COLOR_NO_RELIGION = Color.RED;
    
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
            mapPanel.paintProvince(g, provId, COLOR_NO_HIST);
        } else if (religion.length() == 0 || religion.equals("none")) {
            mapPanel.paintProvince(g, provId, COLOR_NO_RELIGION);
        } else {
            mapPanel.paintProvince(g, provId, getReligionColor(religion));
        }
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Do nothing
        return;
    }
    
    private Color getReligionColor(String religion) {
        religion = religion.toLowerCase();
        
        Color ret = relColorCache.get(religion);
        
        if (ret == null) {
            for (GenericObject group : religions.children) {
                for (GenericObject rel : group.children) {
                    if (rel.name.equals(religion)) {
                        // found it
                        GenericList color = rel.getList("color");
                        if (color == null) {
                            System.err.println("color for " + religion + " is null");
                            return COLOR_NO_RELIGION_DEF;
                        }
                        ret = new Color(
                                Float.parseFloat(color.get(0)),
                                Float.parseFloat(color.get(1)),
                                Float.parseFloat(color.get(2))
                                );
                        relColorCache.put(religion, ret);
                        return ret;
                    }
                }
            }
            return COLOR_NO_RELIGION_DEF;
        }
        return ret;
    }
    
    
    public String getTooltipExtraText(final Province current) {
        if (current.getId() == 0 || current.getId() >= SEA_STARTS)
            return "";
        
        final String ret = Text.getText(mapPanel.getModel().getHistString(current.getId(), "religion"));
        if (ret.length() == 0)
            return "";
        return "Religion: " + ret;
    }
}
