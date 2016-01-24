/*
 * Map.java
 *
 * Created on January 26, 2007, 6:31 PM
 */

package posed;

import eug.parser.EUGFileIO;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;

/**
 *
 * @author Michael Myers
 */
public final class Map {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Map.class.getName());
    
//    private static final String MAP_DIR_NAME = "/map";
    private GenericObject mapData;
    private ProvinceData provinceData;
    
    private GameVersion gameVersion;

    private boolean[] isLand = null;
    
    public Map(String mapFileName, GameVersion gameVersion, boolean useLocalization) {
        this.gameVersion = gameVersion;
        try {
            loadData(mapFileName, useLocalization);
        } catch (FileNotFoundException ex) {
            log.log(Level.SEVERE, "Could not load .map file", ex);
        }
    }

    private void loadData(String filename, boolean useLocalization) throws FileNotFoundException {
        log.log(Level.INFO, "Map file is {0}", filename);
        mapData = EUGFileIO.load(filename);
        
        log.config(String.valueOf(mapData));
        
        provinceData = new ProvinceData(Integer.parseInt(getString("max_provinces")),
                new File(filename).getParent() + "/definition.csv",
                useLocalization, gameVersion.getProvinceLocFormat());

        if (gameVersion.hasSeaList()) {
            // Initialize boolean array
            isLand = new boolean[mapData.getInt("max_provinces")];
            for (int i = 1; i < isLand.length; i++) {
                isLand[i] = true;
            }

            GenericList seaProvs = mapData.getList("sea_starts");
            if (seaProvs == null) {
                log.warning("No sea_starts found; weird things might start happening now");
            } else {
                for (String provId : seaProvs) {
                    int id = Integer.parseInt(provId);
                    isLand[id] = false;
                }
            }

            if (gameVersion.hasLakes()) {
                GenericList lakes = mapData.getList("lakes");
                if (lakes != null) {
                    for (String provId : lakes) {
                        int id = Integer.parseInt(provId);
                        isLand[id] = false;
                    }
                }
            }
        }
    }
    
    public String getString(String key) {
        return mapData.getString(key);
    }
    
    public ProvinceData getProvinceData() {
        return provinceData;
    }

    public boolean isLand(int provId) {
        if (provId <= 0)
            return false;

        if (gameVersion.hasSeaList())
            return isLand[provId];
        return provId < mapData.getInt("sea_starts");
    }
    
    public boolean isRGBLand(int rgb) {
        ProvinceData.Province p = provinceData.getProv(rgb);
        if (p == null)
            return false;
        return isLand(p.getId());
    }
}
