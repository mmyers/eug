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

/**
 *
 * @author Michael Myers
 */
public final class Map {
    
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
            ex.printStackTrace();
        }
    }

    private void loadData(String filename, boolean useLocalization) throws FileNotFoundException {
        System.out.println("Map file is " + filename);
        mapData = EUGFileIO.load(filename);
        //System.out.println("default.map:");
        //System.out.println(mapData);
        provinceData = new ProvinceData(Integer.parseInt(getString("max_provinces")),
                new File(filename).getParent() + "/definition.csv", useLocalization);

        if (gameVersion.hasSeaList()) {
            // Initialize boolean array
            isLand = new boolean[mapData.getInt("max_provinces")];
            for (int i = 1; i < isLand.length; i++) {
                isLand[i] = true;
            }

            GenericList seaProvs = mapData.getList("sea_starts");
            if (seaProvs == null) {
                System.err.println("No sea_starts found; weird things might start happening now");
            } else {
                for (String provId : seaProvs) {
                    int id = Integer.parseInt(provId);
                    isLand[id] = false;
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
}
