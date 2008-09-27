/*
 * GoodsMode.java
 *
 * Created on June 12, 2007, 3:27 PM
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
public class GoodsMode extends ProvincePaintingMode {
    
    
    /** Creates a new instance of GoodsMode */
    public GoodsMode() {
        super();
    }
    
    public GoodsMode(MapPanel panel) {
        super(panel);
    }
    
    protected void paintProvince(Graphics2D g, int provId) {
        final String goods = mapPanel.getModel().getHistString(provId, "trade_goods");
        if (goods == null) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_HIST);
        } else if (goods.length() == 0 || goods.equalsIgnoreCase("none")) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_GOODS);
        } else {
            mapPanel.paintProvince(g, provId, Utilities.getGoodsColor(goods));
        }
    }
    
    protected void paintSeaZone(Graphics2D g, int id) {
        // Do nothing
        return;
    }
    
    
    public String getTooltipExtraText(Province current) {
        if (!editor.Main.map.isLand(current.getId()))
            return "";
        
        final String ret = Text.getText(mapPanel.getModel().getHistString(current.getId(), "trade_goods"));
        if (ret.length() == 0)
            return "";
        return "Goods: " + ret;
    }
    
}
