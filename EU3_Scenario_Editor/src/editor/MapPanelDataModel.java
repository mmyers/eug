/*
 * MapPanelDataModel.java
 *
 * Created on June 11, 2007, 1:49 PM
 */

package editor;

import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzDataSource;
import eug.specific.clausewitz.ClausewitzHistory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Michael Myers
 */
public class MapPanelDataModel implements java.io.Serializable {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(MapPanelDataModel.class.getName());
    
    private static final long serialVersionUID = 1L;
    
    // TODO: Perhaps an abstract superclass should be extracted?
    
    private final ProvinceData provinceData;
    
    private final MapData mapData;
    
    private final Map map;
    
    private ClausewitzDataSource dataSource;
    
    
    private String date;
    
    public MapPanelDataModel(MapData data, Map map, ProvinceData pData) {
        //this(data, null, pData);
        this.mapData = data;
        this.map = map;
        this.provinceData = pData;
        this.dataSource = null;
    }
    
//    private MapPanelDataModel(MapData data, ClausewitzDataSource source, ProvinceData provinceData) {
//        this.provinceData = provinceData;
//        mapData = data;
//        dataSource = source;
//    }
    
    
    public ProvinceData getProvinceData() {
        return provinceData;
    }
    
    public MapData getMapData() {
        return mapData;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public void setDataSource(ClausewitzDataSource source) {
        this.dataSource = source;
    }
    
    public ClausewitzDataSource getDataSource() {
        return dataSource;
    }
    
    public void preloadProvs() {
        dataSource.preloadProvinces(map.getFirstSeaProv());
    }
    
    public void preloadCountries() {
        dataSource.preloadCountries();
    }
    
    public List<String> isCoreOf(int provId, String date) {
        return ClausewitzHistory.isCoreOf(dataSource.getProvinceHistory(provId), date);
    }
    
    public List<String> isCoreOf(int provId) {
        return isCoreOf(provId, date);
    }
    
    
    public String getHistString(int provId, String name) {
        return ClausewitzHistory.getHistString(dataSource.getProvinceHistory(provId), name, date);
    }
    
    public List<String> getHistStrings(int provId, String name) {
        return ClausewitzHistory.getHistStrings(dataSource.getProvinceHistory(provId), name, date);
    }
    
    public GenericObject getHistObject(int provId, String name) {
        return ClausewitzHistory.getHistObject(dataSource.getProvinceHistory(provId), name, date);
    }
    
    public List<GenericObject> getHistObjects(int provId, String name) {
        return ClausewitzHistory.getHistObjects(dataSource.getProvinceHistory(provId), name, date);
    }
    
    public String getHistString(String tag, String name) {
        return ClausewitzHistory.getHistString(dataSource.getCountryHistory(tag), name, date);
    }
    
    public List<String> getHistStrings(String tag, String name) {
        return ClausewitzHistory.getHistStrings(dataSource.getCountryHistory(tag), name, date);
    }
    
    public GenericObject getHistObject(String tag, String name) {
        return ClausewitzHistory.getHistObject(dataSource.getCountryHistory(tag), name, date);
    }
    
    public List<GenericObject> getHistObjects(String tag, String name) {
        return ClausewitzHistory.getHistObjects(dataSource.getCountryHistory(tag), name, date);
    }
    
    
    public List<String> getCountries() {
        return getCountries(date);
    }
    
    public List<String> getCountries(String date) {
        final java.util.Map<String, Object> ret = new HashMap<>();
        
        for (Integer id : map.getLandProvs()) {
            ProvinceData.Province p = provinceData.getProvByID(id);
            
            String owner = ClausewitzHistory.getHistString(dataSource.getProvince(p.getId()), "owner", date);
            
            if (owner == null || owner.length() == 0 || owner.equals("none") || owner.equals("XXX")) {
                continue;
            } else {
                ret.put(Text.getText(owner), null);
            }
        }
        
        final List<String> list = new ArrayList<>(ret.keySet());
        Collections.sort(list);
        return list;
    }
    
    public List<String> getTags() {
        return getTags(date);
    }
    
    public List<String> getTags(String date) {
        final java.util.Map<String, Object> ret = new HashMap<>();
        
        for (Integer id : map.getLandProvs()) {
            ProvinceData.Province p = provinceData.getProvByID(id);
            
            String owner = ClausewitzHistory.getHistString(dataSource.getProvince(p.getId()), "owner", date);
            
            if (owner == null || owner.length() == 0 || owner.equals("none") || owner.equals("XXX")) {
                continue;
            } else {
                ret.put(owner, null);
            }
        }
        
        final List<String> list = new ArrayList<>(ret.keySet());
        Collections.sort(list);
        return list;
    }
    
    public List<Integer[]> getLinesInProv(int provId) {
        ProvinceData.Province p = provinceData.getProvByID(provId);
        if (p == null) {
            log.log(Level.WARNING, "Unknown province {0}", provId);
            return Collections.emptyList();
        } else
            return mapData.getLinesInProv(p.getColor());
    }
    
}
