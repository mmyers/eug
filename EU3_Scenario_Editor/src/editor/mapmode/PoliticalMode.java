/*
 * PoliticalMode.java
 *
 * Created on August 18, 2007, 9:08 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import eug.shared.GenericObject;
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
    }
    
    public PoliticalMode(MapPanel panel) {
        super(panel);
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final String owner = mapPanel.getModel().getHistString(provId, "owner");
        String controller = (owner == null || owner.isEmpty()) ? "" : getController(provId);
        
        if (owner == null) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        } else if (controller == null) {
            final Paint p = Utilities.createPaint(Utilities.getCtryColor(owner), Utilities.COLOR_NO_HIST);
            mapPanel.paintProvince(g, provId, p);
        } else if (!owner.equalsIgnoreCase(controller) && !controller.isEmpty() && !Utilities.isNotACountry(controller)) { // colonies don't always have a controller in history
            final Paint p = Utilities.createPaint(Utilities.getCtryColor(owner), Utilities.getCtryColor(controller));
            mapPanel.paintProvince(g, provId, p);
        } else {
            mapPanel.paintProvince(g, provId, Utilities.getCtryColor(owner));
        }
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // do nothing
    }

    private String getController(int provId) {
        String controller = mapPanel.getModel().getHistString(provId, "controller");
        if (controller.isEmpty()) {
            GenericObject controllerObj = mapPanel.getModel().getHistObject(provId, "controller");
            if (controllerObj != null)
                controller = controllerObj.getString("controller");
        }
        return controller;
    }
    
    @Override
    public String getTooltipExtraText(final Province curr) {
        final int id = curr.getId();
        if (!getMap().isLand(id))
            return "";
        
        String owner = mapPanel.getModel().getHistString(id, "owner");
        if (owner == null || owner.isEmpty())
            return "";

        String controller = getController(curr.getId());
        
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
