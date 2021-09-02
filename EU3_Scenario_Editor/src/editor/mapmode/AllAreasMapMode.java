
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Michael
 */
public class AllAreasMapMode extends ProvincePaintingMode {
    
    private final java.util.Map<Integer, String> provinceAreas;
    
    private final java.util.Map<String, Color> areaColors;
    
    public enum GeographyType {
        AREAS("Area"),
        REGIONS("Region"),
        SUPER_REGIONS("Super region"),
        CONTINENTS("Continent");
        private final String readableName;
        private GeographyType(String readableName) {
            this.readableName = readableName;
        }
        public String getReadableName() {
            return readableName;
        }
    };
    
    private final GeographyType type;

    public AllAreasMapMode(MapPanel panel, GeographyType type) {
        super(panel);
        provinceAreas = new HashMap<>();
        areaColors = new HashMap<>();
        
        this.type = type;
        java.util.Map<String, List<String>> geography = null;
        switch (type) {
            case AREAS: geography = mapPanel.getMap().getAreas(); break;
            case REGIONS: geography = mapPanel.getMap().getRegions(); break;
            case SUPER_REGIONS: geography = mapPanel.getMap().getSuperRegions(); break;
            case CONTINENTS: geography = mapPanel.getMap().getContinents(); break;
        }
        prepareGeography(geography);
    }
    
    private void prepareGeography(java.util.Map<String, List<String>> areas) {
        List<Color> colors = Utilities.getGeographyColors();
        // The color index does not begin at 0 simply because EU4's colors are in such an
        // order that Europe and Africa would be nearly indistinguishable in continent mode
        int colorIdx = Math.min(15, colors.size()-1);
        for (java.util.Map.Entry<String, List<String>> area : areas.entrySet()) {
            String areaTag = area.getKey();
            List<String> areaProvIds = area.getValue();
            for (String provIdStr : areaProvIds) {
                Integer provId = Integer.valueOf(provIdStr);
                provinceAreas.put(provId, areaTag);
            }
            areaColors.put(areaTag, colors.get(colorIdx++));
            if (colorIdx >= colors.size())
                colorIdx = 0;
        }
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        String area = provinceAreas.get(provId);
        if (area != null) {
            Color c = areaColors.get(area);
            if (c != null) {
                mapPanel.paintProvince(g, provId, c);
            }
        } else if (mapPanel.getMap().isWasteland(provId)) {
            mapPanel.paintProvince(g, provId, Color.BLACK);
        } else {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        }
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        String area = provinceAreas.get(id);
        if (area != null) {
            Color c = areaColors.get(area);
            if (c != null) {
                mapPanel.paintProvince(g, id, c);
            }
        }
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        String area = provinceAreas.get(current.getId());
        if (area != null) {
            return type.getReadableName() + ": " + Text.getText(area);
        } else if (mapPanel.getMap().isWasteland(current.getId())) {
            return "Wasteland";
        }
        return "";
    }
}
