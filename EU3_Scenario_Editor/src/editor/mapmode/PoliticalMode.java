/*
 * PoliticalMode.java
 *
 * Created on August 18, 2007, 9:08 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 *
 * @author Michael Myers
 * @since 0.5pre3
 */
public final class PoliticalMode extends ProvincePaintingMode {
    
    
    /** Creates a new instance of PoliticalMode */
    public PoliticalMode() {
        super();
    }
    
    public PoliticalMode(MapPanel panel) {
        super(panel);
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final String owner = mapPanel.getModel().getHistString(provId, "owner");
        final String controller = (owner == null ? null : mapPanel.getModel().getHistString(provId, "controller"));
        
        if (owner == null) {
            mapPanel.paintProvince(g, provId, Utilities.getCtryColor("NAT"));
        } else if (controller == null) {
            final Paint p = Utilities.createPaint(Utilities.getCtryColor(owner), Utilities.COLOR_NO_HIST);
//            if (p != null)
                mapPanel.paintProvince(g, provId, p);
//            else
//                System.err.println("Unknown problem in PoliticalMode.java");
        } else if (!owner.equalsIgnoreCase(controller)) {
            final Paint p = Utilities.createPaint(Utilities.getCtryColor(owner), Utilities.getCtryColor(controller));
//            if (p != null)
                mapPanel.paintProvince(g, provId, p);
//            else
//                System.err.println("Unknown problem in PoliticalMode.java");
        } else {
            mapPanel.paintProvince(g, provId, Utilities.getCtryColor(owner));
        }
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // do nothing
    }
    
    
    
    public String getTooltipExtraText(final Province curr) {
        final int id = curr.getId();
        if (!editor.Main.map.isLand(id))
            return "";
        
        String owner = mapPanel.getModel().getHistString(id, "owner");
        String controller = mapPanel.getModel().getHistString(id, "controller");
        
        if (owner == null && controller == null)
            return "";
        
        owner = Text.getText(owner);
        controller = Text.getText(controller);
        
        final StringBuilder ret = new StringBuilder();
        if (owner != null && !Utilities.isNotACountry(owner))
            ret.append("Owner: ").append(owner).append("<br />");
        if (controller != null && !controller.equalsIgnoreCase(owner))
            ret.append("Controller: ").append(Utilities.isNotACountry(controller) ? "none" : controller);
        
        return ret.toString();
    }
    
}
