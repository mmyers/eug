package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Handles both colonial regions and trade companies, since the mechanics and
 * format appear to be identical.
 * @author Michael
 * @since 0.8.6
 */
public class ColonialRegionsMode extends ProvincePaintingMode {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(ColonialRegionsMode.class.getName());
    
    private final Map<Integer, ColonialRegion> regions;

    public ColonialRegionsMode(MapPanel panel, FilenameResolver resolver) {
        super(panel);
        this.regions = new HashMap<>();
        
        GenericObject obj = EUGFileIO.loadAll(resolver.listFiles("common/colonial_regions"), ParserSettings.getQuietSettings());
        if (obj != null)
            loadRegions(obj);
        else
            log.warning("Could not load colonial regions files from common/colonial_regions");
        
        obj = EUGFileIO.loadAll(resolver.listFiles("common/trade_companies"), ParserSettings.getQuietSettings());
        if (obj != null)
            loadRegions(obj);
        else
            log.warning("Could not load trade company definitions files from common/trade_companies");
    }
    
    private void loadRegions(GenericObject source) {
        for (GenericObject region : source.children) {
            String name = Text.getText(region.name);
            Color color;
            GenericList colorList = region.getList("color");
            if (colorList != null && colorList.size() == 3) {
                color = new Color(
                        Integer.parseInt(colorList.get(0)),
                        Integer.parseInt(colorList.get(1)),
                        Integer.parseInt(colorList.get(2)));
            } else {
                log.log(Level.WARNING, "No valid color found in {0}", region.name);
                color = Color.GRAY;
            }
            
            ColonialRegion cr = new ColonialRegion(name, color);
            
            GenericList provinces = region.getList("provinces");
            if (provinces == null) {
                log.log(Level.WARNING, "No valid list of provinces found in {0}", region.name);
                continue;
            }
            
            for (String prov : provinces) {
                int id = Integer.parseInt(prov);
                if (regions.get(id) != null) {
                    log.log(Level.INFO, "Colonial regions/trade companies: Province {0} is in both {1} and {2}",
                            new Object[]{id, regions.get(id).getName(), name});
                }
                regions.put(id, cr);
            }
        }
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        ColonialRegion cr = regions.get(provId);
        if (cr != null) {
            mapPanel.paintProvince(g, provId, cr.getColor());
        } else {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        }
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        ColonialRegion cr = regions.get(id);
        if (cr != null) {
            mapPanel.paintProvince(g, id, cr.getColor());
        }
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        ColonialRegion cr = regions.get(current.getId());
        if (cr != null) {
            return cr.getName();
        }
        return "";
    }
    
    private static class ColonialRegion {
        private final String name;
        private final Color color;
        
        public ColonialRegion(String name, Color color) {
            this.name = name;
            this.color = color;
        }
        
        public String getName() {
            return name;
        }
        public Color getColor() {
            return color;
        }
    }
}
