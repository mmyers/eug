/*
 * PopSplitMapMode.java
 *
 * Created on Apr 5, 2008, 12:09:11 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.regex.Pattern;

/**
 *
 * @author Michael
 */
public class PopSplitMapMode extends ProvincePaintingMode {

    private static final Pattern DOT = Pattern.compile("\\.");
    private static final double MAX_PERCENT = 60.0;
    private static final double MIN_PERCENT = 0.0;
    private static final double MAX_COLOR = 255.0;
    private static final double ratio = MAX_COLOR / MAX_PERCENT;
    
    public PopSplitMapMode() {
    }
    
    public PopSplitMapMode(MapPanel panel) {
        super(panel);
    }
    
    @Override
    protected void paintProvince(final Graphics2D g2d, final int provId) {
        final String[] split =
                DOT.split(mapPanel.getModel().getHistString(provId, "split"));
        
        int r,g,b;
        int citizens = Integer.parseInt(split[0]);
        int freemen = Integer.parseInt(split[1]);
        int slaves = Integer.parseInt(split[2]);
        r = (int) Math.min((citizens - MIN_PERCENT) * ratio, MAX_COLOR);
        g = (int) Math.min((freemen - MIN_PERCENT) * ratio, MAX_COLOR);
        b = (int) Math.min((slaves - MIN_PERCENT) * ratio, MAX_COLOR);
        
        try {
        mapPanel.paintProvince(g2d, provId, new Color(r,g,b));
        } catch (IllegalArgumentException ex) {
            System.out.println("r = " + r + ", g = " + g + ", b = " + b);
        }
    }

    @Override
    protected final void paintSeaZone(Graphics2D g, int id) {
        // do nothing
    }

    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        final StringBuilder ret = new StringBuilder();
        final String[] split =
                DOT.split(mapPanel.getModel().getHistString(id, "split"));
        ret.append("Citizens: ").append(split[0]).append("%<br />");
        ret.append("Freedmen: ").append(split[1]).append("%<br />");
        ret.append("Slaves:   ").append(split[2]).append("%");
        return ret.toString();
    }

}
