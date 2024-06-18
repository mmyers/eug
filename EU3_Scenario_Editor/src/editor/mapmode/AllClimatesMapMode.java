
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Michael
 */
public class AllClimatesMapMode extends ProvincePaintingMode {
    
    // These maps are static because they're all initialized from the same 
    // data file, so we might as well do them all at once
    private static final java.util.Map<Integer, String> provinceClimates = new HashMap<>();
    private static final java.util.Map<Integer, String> provinceWinters = new HashMap<>();
    private static final java.util.Map<Integer, String> provinceMonsoons = new HashMap<>();
    
    private static final java.util.Map<String, Color> climateColors = new HashMap<>();
    
    private static boolean areClimatesInitialized = false;
    
    private final java.util.Map<Integer, Paint> provColors = new HashMap<>();
    
    private final ClimateType climateType;
    
    public enum ClimateType {
        CLIMATE("Province climates"),
        WINTER("Province winters"),
        MONSOON("Province monsoons"),
        ALL("All climate types");
        
        private final String readableName;
        
        private ClimateType(String readableName) {
            this.readableName = readableName;
        }
        
        public String getReadableName() {
            return readableName;
        }
    }

    public AllClimatesMapMode(MapPanel panel, ClimateType type) {
        super(panel);
        this.climateType = type;
        
        if (!areClimatesInitialized) {
            prepareGeography(mapPanel.getMap().getClimates());
        }
        
        initProvinceColors();
    }
    
    private static void prepareGeography(java.util.Map<String, List<Integer>> climates) {
        List<Color> colors = Utilities.getGeographyColors();
        
        int colorIdx = Math.min(15, colors.size()-1);
        for (java.util.Map.Entry<String, List<Integer>> climate : climates.entrySet()) {
            String climateTag = climate.getKey();
            if (climateTag.equals("(none)"))
                continue;
            
            java.util.Map<Integer, String> climateMap = provinceClimates;
            if (climateTag.endsWith("_winter") || climateTag.equalsIgnoreCase("normal")) { // "normal" almost always refers to winter
                climateMap = provinceWinters;
            } else if (climateTag.endsWith("_monsoon")) {
                climateMap = provinceMonsoons;
            }
            
            List<Integer> areaProvIds = climate.getValue();
            for (Integer provId : areaProvIds) {
                climateMap.put(provId, climateTag);
            }
            climateColors.put(climateTag, colors.get(colorIdx++));
            if (colorIdx >= colors.size())
                colorIdx = 0;
        }
        
        areClimatesInitialized = true;
    }
    
    private void initProvinceColors() {
        for (int i = 0; i < mapPanel.getMap().getMaxProvinces(); i++) {
            switch (climateType) {
                case CLIMATE:
                    String climate = provinceClimates.get(i);
                    Color c = climateColors.get(climate);
                    if (c != null)
                        provColors.put(i, c);
                    else
                        provColors.put(i, Utilities.COLOR_LAND_DEFAULT);
                    break;
                case WINTER:
                    String winter = provinceWinters.get(i);
                    Color w = climateColors.get(winter);
                    if (w != null)
                        provColors.put(i, w);
                    else
                        provColors.put(i, Utilities.COLOR_LAND_DEFAULT);
                    break;
                case MONSOON:
                    String monsoon = provinceMonsoons.get(i);
                    Color m = climateColors.get(monsoon);
                    if (m != null)
                        provColors.put(i, m);
                    else
                        provColors.put(i, Utilities.COLOR_LAND_DEFAULT);
                    break;
                case ALL:
                default:
                    provColors.put(i, getAllClimatesPaint(i));
                    break;
            }
        }
    }
    
    private Paint getAllClimatesPaint(int provId) {
        String climate = provinceClimates.get(provId);
        String winter = provinceWinters.get(provId);
        String monsoon = provinceMonsoons.get(provId);
        
        Color c = climateColors.get(climate);
        Color w = climateColors.get(winter);
        Color m = climateColors.get(monsoon);
        
        if (climate != null) {
            if (winter != null) {
                if (monsoon != null) {
                    return Utilities.createTricolorPaint(c, m, w);
                }
                return Utilities.createEqualPaint(c, w);
            } else if (monsoon != null) {
                return Utilities.createEqualPaint(c, m);
            } else {
                return c;
            }
        } else if (winter != null) {
            if (monsoon != null) {
                return Utilities.createEqualPaint(m, w);
            } else {
                return w;
            }
        } else if (monsoon != null) {
            return m;
        } else {
            return Utilities.COLOR_LAND_DEFAULT;
        }
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        mapPanel.paintProvince(g, provId, provColors.get(provId));
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        // EU4 does not have climates for sea zones, but we'll check just in case
        Paint c = provColors.get(id);
        if (c != Utilities.COLOR_LAND_DEFAULT)
            mapPanel.paintProvince(g, id, c);
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        StringBuilder ret = new StringBuilder();
        
        String climate = provinceClimates.get(current.getId());
        if (climate != null) {
            ret.append("Climate: ").append(Text.getText(climate)).append("<br>");
        }
        
        String winter = provinceWinters.get(current.getId());
        if (winter != null) {
            ret.append("Winter: ").append(Text.getText(winter)).append("<br>");
        }
        
        String monsoon = provinceMonsoons.get(current.getId());
        if(monsoon != null) {
            ret.append("Monsoon: ").append(Text.getText(monsoon)).append("<br>");
        }
        
        return ret.toString();
    }
}
