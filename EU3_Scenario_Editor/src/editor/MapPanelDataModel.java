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

/**
 *
 * @author Michael Myers
 */
public class MapPanelDataModel implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // TODO: Perhaps an abstract superclass should be extracted?
    
    private final ProvinceData provinceData;
    
    private final MapData mapData;
    
    private ClausewitzDataSource dataSource;
    
    
    private String date;
    
    /** Creates a new instance of MapPanelDataModel */
    public MapPanelDataModel(final MapData data) {
        this(data, null);
    }
    
    public MapPanelDataModel(final MapData data, ClausewitzDataSource source) {
        provinceData = Main.provinceData;
        mapData = data;
        dataSource = source;
    }
    
    
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
        dataSource.preloadProvinces(Main.map.getFirstSeaProv());
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
    
    public String getHistString(String tag, String name) {
        return ClausewitzHistory.getHistString(dataSource.getCountryHistory(tag), name, date);
    }
    
    public List<String> getHistStrings(String tag, String name) {
        return ClausewitzHistory.getHistStrings(dataSource.getCountryHistory(tag), name, date);
    }
    
    public GenericObject getHistObject(String tag, String name) {
        return ClausewitzHistory.getHistObject(dataSource.getCountryHistory(tag), name, date);
    }
    
    
    public List<String> getCountries() {
        return getCountries(date);
    }
    
    public List<String> getCountries(String date) {
        final java.util.Map<String, Object> ret = new HashMap<String, Object>();
        
        for (Integer id : Main.map.getLandProvs()) {
            ProvinceData.Province p = provinceData.getProvByID(id);
            
            String owner = ClausewitzHistory.getHistString(dataSource.getProvince(p.getId()), "owner", date);
            
            if (owner == null || owner.length() == 0 || owner.equals("none") || owner.equals("XXX")) {
                continue;
            } else {
                ret.put(Text.getText(owner), null);
            }
        }
        
        final List<String> list = new ArrayList<String>(ret.keySet());
        Collections.sort(list);
        return list;
    }
    
    public List<String> getTags() {
        return getTags(date);
    }
    
    public List<String> getTags(String date) {
        final java.util.Map<String, Object> ret = new HashMap<String, Object>();
        
        for (Integer id : Main.map.getLandProvs()) {
            ProvinceData.Province p = provinceData.getProvByID(id);
            
            String owner = ClausewitzHistory.getHistString(dataSource.getProvince(p.getId()), "owner", date);
            
            if (owner == null || owner.length() == 0 || owner.equals("none") || owner.equals("XXX")) {
                continue;
            } else {
                ret.put(owner, null);
            }
        }
        
        final List<String> list = new ArrayList<String>(ret.keySet());
        Collections.sort(list);
        return list;
    }
    
    public List<Integer[]> getLinesInProv(int provId) {
        ProvinceData.Province p = provinceData.getProvByID(provId);
        if (p == null) {
            System.err.println("Unknown province " + provId);
            return Collections.EMPTY_LIST;
        } else
            return mapData.getLinesInProv(p.getColor());
    }
    
}
