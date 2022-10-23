
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import eug.shared.GenericObject;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Michael
 */
public class CountryHistoryExistsMode extends ProvincePaintingMode {
    
    private static final Color COLOR_NO_HIST = Color.RED.darker();
    private static final Color COLOR_HAS_HIST = Color.GREEN.darker();

    public CountryHistoryExistsMode() {
    }

    public CountryHistoryExistsMode(MapPanel panel) {
        super(panel);
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        final String ownerTag = mapPanel.getModel().getOwner(provId);
        if (Utilities.isNotACountry(ownerTag)) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
            return;
        }
        
        final GenericObject history = mapPanel.getDataSource().getCountryHistory(ownerTag);
        
        if (history != null)
            mapPanel.paintProvince(g, provId, COLOR_HAS_HIST);
        else
            mapPanel.paintProvince(g, provId, COLOR_NO_HIST);
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        // Nothing here
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        final String ownerTag = mapPanel.getModel().getOwner(current.getId());
        if (Utilities.isNotACountry(ownerTag))
            return "Not part of a country";
        
        final GenericObject history = mapPanel.getDataSource().getCountryHistory(ownerTag);
        
        if (history == null)
            return ownerTag + " does not have a country history file";
        else
            return ownerTag + " has a country history file";
    }
    
}
