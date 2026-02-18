
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael
 */
public class SimpleTerrainMode extends ProvincePaintingMode {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(SimpleTerrainMode.class.getName());
    private final Map<Integer, String> provTerrains;
    private Map<String, editor.Map.Terrain> terrains;
    
    public SimpleTerrainMode(MapPanel panel) {
        super(panel);
        
        terrains = panel.getMap().getTerrainOverrides();
        if (terrains == null)
            terrains = panel.getMap().getTerrainOverridesCK3();
        
        provTerrains = new HashMap<>();
        for (editor.Map.Terrain terr : terrains.values()) {
            for (String strId : terr.getOverrides()) {
                int id = Integer.parseInt(strId);
                if (provTerrains.get(id) != null)
                    log.log(java.util.logging.Level.WARNING, "Province {0} is defined in multiple terrain_override lists", strId);
                provTerrains.put(id, terr.getName());
            }
        }
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        String sTerr = provTerrains.getOrDefault(provId, null);
        if (sTerr != null) {
            editor.Map.Terrain terr = terrains.getOrDefault(sTerr, null);
            if (terr != null)
                mapPanel.paintProvince(g, provId, terr.getColor());
            else
                mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        } else {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        }
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        String terr = provTerrains.getOrDefault(id, null);
        if (terr != null) {
            mapPanel.paintProvince(g, id, terrains.get(terr).getColor());
        }
    }

    @Override
    public Object getBorderGroup(final int provId) {
        String terr = provTerrains.get(provId);
        if (terr != null)
            return terr;
        if (getMap().isWasteland(provId))
            return "WASTELAND";
        if (!getMap().isLand(provId))
            return "SEA_ZONE";
        return "DEFAULT";
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        String terr = provTerrains.get(current.getId());
        if (terr != null) {
            return Text.getText(terr);
        }
        return "";
    }
}
