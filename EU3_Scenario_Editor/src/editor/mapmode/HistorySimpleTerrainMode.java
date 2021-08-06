
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Michael
 */
public class HistorySimpleTerrainMode extends ProvincePaintingMode {

    public HistorySimpleTerrainMode(MapPanel panel) {
        super(panel);
    }
    
    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        String terrain = mapPanel.getModel().getHistString(provId, "terrain");
        if (terrain != null) {
            Color c = mapPanel.getMap().getTerrainColors().getOrDefault(terrain, null);
            if (c != null)
                mapPanel.paintProvince(g, provId, c);
            else
                mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        } else {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        }
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        // do nothing
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        String terr = mapPanel.getModel().getHistString(current.getId(), "terrain");
        if (terr != null) {
            return Text.getText(terr);
        }
        return "";
    }
    
}
