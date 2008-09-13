/*
 * FortMapMode.java
 *
 * Created on June 15, 2007, 6:02 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.MapPanelDataModel;
import editor.ProvinceData.Province;
import eug.shared.GenericObject;
import java.awt.Graphics2D;
import java.util.List;

/**
 * <code>MapMode</code> that highlights provinces lighter or darker depending on
 * fort level.
 * @author Michael Myers
 */
public final class FortMapMode extends DiscreteScalingMapMode {
    
    private GenericObject[] forts;
    private int[] fortLevels;
    
    /** Creates a new instance of FortMapMode */
    public FortMapMode(final List<GenericObject> forts) {
        super("", 0, 1, 1);
        initialize(forts);
        setMax(getMaxFortLevel(forts) + 1);
    }
    
    public FortMapMode(final MapPanel panel, final List<GenericObject> forts) {
        super(panel, "", 0, 1, 1);
        initialize(forts);
        setMax(getMaxFortLevel(forts) + 1);
    }

    private void initialize(final List<GenericObject> forts) {
        this.forts = forts.toArray(new GenericObject[forts.size()]);
        fortLevels = new int[this.forts.length];
        for (int i = 0; i < this.forts.length; i++) {
            fortLevels[i] = this.forts[i].getInt("fort_level");
        }
    }

    protected void paintProvince(final Graphics2D g, int provId) {
        mapPanel.paintProvince(g, provId, colors[getFortLevel(provId)]);
    }

    private int getFortLevel(final int provId) {
        int level = 0;
        final MapPanelDataModel model = mapPanel.getModel();
        for (int i = 0; i < forts.length; i++) {
            if ("yes".equalsIgnoreCase(model.getHistString(provId, forts[i].name))) {
                level += fortLevels[i];
            }
        }
        return level;
    }

    private static int getMaxFortLevel(final List<GenericObject> forts) {
        int level = 0;
        for (GenericObject fort : forts) {
            level += fort.getInt("fort_level");
        }
        return level;
    }

    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!editor.Main.map.isLand(id))
            return "";
        
        return "Fort level: " + getFortLevel(id);
    }

}
