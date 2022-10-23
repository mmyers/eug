
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Michael
 */
public class HistoryExistsMode extends ProvincePaintingMode {
    
    private static final Color COLOR_NO_HIST = Color.RED.darker();

    public HistoryExistsMode() {
    }

    public HistoryExistsMode(MapPanel panel) {
        super(panel);
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        if (mapPanel.getDataSource().getProvinceHistory(provId) != null)
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        else
            mapPanel.paintProvince(g, provId, COLOR_NO_HIST);
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        if (mapPanel.getDataSource().getProvinceHistory(id) == null)
            mapPanel.paintProvince(g, id, COLOR_NO_HIST);
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        if (mapPanel.getDataSource().getProvinceHistory(current.getId()) == null)
            return "Does not have a history file";
        else
            return "Has a history file";
    }
    
}
