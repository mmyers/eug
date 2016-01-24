/*
 * MapPanelDataModel.java
 *
 * Created on June 11, 2007, 1:49 PM
 */

package posed;

import java.util.List;

/**
 *
 * @author Michael Myers
 */
public class MapPanelDataModel implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // TODO: Perhaps an abstract superclass should be extracted?
    
    private final ProvinceData provinceData;
    
    private final MapData mapData;
    
    
    public MapPanelDataModel(final MapData data, final int numProvs, final String defFileName, boolean useLocalization, String provinceLocFmt) {
        provinceData = new ProvinceData(numProvs, defFileName, useLocalization, provinceLocFmt);
        mapData = data;
    }
    
    
    public ProvinceData getProvinceData() {
        return provinceData;
    }
    
    public MapData getMapData() {
        return mapData;
    }
    
    public List<Integer[]> getLinesInProv(int provId) {
        return mapData.getLinesInProv(provinceData.getProvByID(provId).getColor());
    }
    
}
