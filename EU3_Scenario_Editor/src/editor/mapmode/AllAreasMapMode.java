
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Michael
 */
public class AllAreasMapMode extends ProvincePaintingMode {
    
    private final java.util.Map<Integer, String> provinceAreas;
    private final java.util.Map<Integer, List<String>> provinceMultiAreas;
    
    private final java.util.Map<String, Color> areaColors;
    
    public enum GeographyType {
        AREAS("Area"),
        REGIONS("Region"),
        META_REGIONS("Region"),
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
        provinceMultiAreas = new HashMap<>();
        areaColors = new HashMap<>();
        
        this.type = type;
        java.util.Map<String, List<Integer>> geography = null;
        switch (type) {
            case AREAS: geography = mapPanel.getMap().getAreas(); break;
            case REGIONS: geography = mapPanel.getMap().getRegions(); break;
            case META_REGIONS: geography = mapPanel.getMap().getMetaRegions(); break;
            case SUPER_REGIONS: geography = mapPanel.getMap().getSuperRegions(); break;
            case CONTINENTS: geography = mapPanel.getMap().getContinents(); break;
        }
        prepareGeography(geography);
    }
    
    private void prepareGeography(java.util.Map<String, List<Integer>> areas) {
        List<Color> colors = Utilities.getGeographyColors();
        // The color index does not begin at 0 simply because EU4's colors are in such an
        // order that Europe and Africa would be nearly indistinguishable in continent mode
        int colorIdx = Math.min(15, colors.size()-1);
        for (java.util.Map.Entry<String, List<Integer>> area : areas.entrySet()) {
            String areaTag = area.getKey();
            List<Integer> areaProvIds = area.getValue();
            for (Integer provId : areaProvIds) {
                provinceAreas.putIfAbsent(provId, areaTag); // change: if a province is in multiple areas, show only the first (usually a more "canonical" area)
                // also create a list of areas per province, for tooltip purposes
                provinceMultiAreas.computeIfAbsent(provId, k -> new ArrayList<String>());
                provinceMultiAreas.get(provId).add(areaTag);
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
    public Object getBorderGroup(final int provId) {
        String area = provinceAreas.get(provId);
        if (area != null)
            return area;
        if (mapPanel.getMap().isWasteland(provId))
            return "WASTELAND";
        if (!mapPanel.getMap().isLand(provId))
            return "SEA_ZONE";
        return "DEFAULT";
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        List<String> areas = provinceMultiAreas.get(current.getId());
        if (areas != null) {
            String allAreasText = areas.stream()
                                       .map(Text::getText)
                                       .collect(Collectors.joining(", "));
            return type.getReadableName() + ": " + allAreasText;
        } else if (mapPanel.getMap().isWasteland(current.getId())) {
            return "Wasteland";
        }
        return "";
    }
}
