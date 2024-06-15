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
import java.util.Objects;
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
    
    private final MapPixelData mapData;
    
    private final Map map;
    
    private ClausewitzDataSource dataSource;
    
    
    private String date;
    
    public MapPanelDataModel(MapPixelData data, Map map, ProvinceData pData) {
        //this(data, null, pData);
        this.mapData = data;
        this.map = map;
        this.provinceData = pData;
        this.dataSource = null;
    }
    
//    private MapPanelDataModel(MapPixelData data, ClausewitzDataSource source, ProvinceData provinceData) {
//        this.provinceData = provinceData;
//        mapData = data;
//        dataSource = source;
//    }
    
    
    public ProvinceData getProvinceData() {
        return provinceData;
    }
    
    public MapPixelData getMapData() {
        return mapData;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
        clearHistoryCache();
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
    
    private final java.util.Map<Integer, String> provOwnerCache = new HashMap<>();
    private final java.util.Map<IntStringTuple, String> provHistStringCache = new HashMap<>();
    private final java.util.Map<IntStringTuple, List<String>> provHistStringsCache = new HashMap<>();
    private final java.util.Map<IntStringTuple, GenericObject> provHistObjectCache = new HashMap<>();
    private final java.util.Map<IntStringTuple, List<GenericObject>> provHistObjectsCache = new HashMap<>();
    
    private final java.util.Map<StringStringTuple, String> countryHistStringCache = new HashMap<>();
    private final java.util.Map<StringStringTuple, List<String>> countryHistStringsCache = new HashMap<>();
    private final java.util.Map<StringStringTuple, GenericObject> countryHistObjectCache = new HashMap<>();
    private final java.util.Map<StringStringTuple, List<GenericObject>> countryHistObjectsCache = new HashMap<>();
    
    public void clearHistoryCache() {
        provOwnerCache.clear();
        provHistStringCache.clear();
        provHistStringsCache.clear();
        provHistObjectCache.clear();
        provHistObjectsCache.clear();
        
        countryHistStringCache.clear();
        countryHistStringsCache.clear();
        countryHistObjectCache.clear();
        countryHistObjectsCache.clear();
    }
    
    
    /** Special method that treats "owner" and "fake_owner" the same in history files. */
    public String getOwner(int provId) {
        return provOwnerCache.computeIfAbsent(provId, id -> ClausewitzHistory.getHistString(dataSource.getProvinceHistory(provId), "owner", "fake_owner", date));
        //return ClausewitzHistory.getHistString(dataSource.getProvinceHistory(provId), "owner", "fake_owner", date);
    }
    
    public String getHistString(int provId, String name) {
        return provHistStringCache.computeIfAbsent(new IntStringTuple(provId, name), t -> ClausewitzHistory.getHistString(dataSource.getProvinceHistory(t.i), t.s, date));
        //return ClausewitzHistory.getHistString(dataSource.getProvinceHistory(provId), name, date);
    }
    
    public List<String> getHistStrings(int provId, String name) {
        return provHistStringsCache.computeIfAbsent(new IntStringTuple(provId, name), t -> ClausewitzHistory.getHistStrings(dataSource.getProvinceHistory(t.i), t.s, date));
        //return ClausewitzHistory.getHistStrings(dataSource.getProvinceHistory(provId), name, date);
    }
    
    public GenericObject getHistObject(int provId, String name) {
        return provHistObjectCache.computeIfAbsent(new IntStringTuple(provId, name), t -> ClausewitzHistory.getHistObject(dataSource.getProvinceHistory(t.i), t.s, date));
        //return ClausewitzHistory.getHistObject(dataSource.getProvinceHistory(provId), name, date);
    }
    
    public List<GenericObject> getHistObjects(int provId, String name) {
        return provHistObjectsCache.computeIfAbsent(new IntStringTuple(provId, name), t -> ClausewitzHistory.getHistObjects(dataSource.getProvinceHistory(t.i), t.s, date));
        //return ClausewitzHistory.getHistObjects(dataSource.getProvinceHistory(provId), name, date);
    }
    
    public String getHistString(String tag, String name) {
        return countryHistStringCache.computeIfAbsent(new StringStringTuple(tag, name), t -> ClausewitzHistory.getHistString(dataSource.getCountryHistory(t.s1), t.s2, date));
        //return ClausewitzHistory.getHistString(dataSource.getCountry(tag), name, date);
    }
    
    public List<String> getHistStrings(String tag, String name) {
        return countryHistStringsCache.computeIfAbsent(new StringStringTuple(tag, name), t -> ClausewitzHistory.getHistStrings(dataSource.getCountryHistory(t.s1), t.s2, date));
        //return ClausewitzHistory.getHistStrings(dataSource.getCountryHistory(tag), name, date);
    }
    
    public GenericObject getHistObject(String tag, String name) {
        return countryHistObjectCache.computeIfAbsent(new StringStringTuple(tag, name), t -> ClausewitzHistory.getHistObject(dataSource.getCountryHistory(t.s1), t.s2, date));
        //return ClausewitzHistory.getHistObject(dataSource.getCountryHistory(tag), name, date);
    }
    
    public List<GenericObject> getHistObjects(String tag, String name) {
        return countryHistObjectsCache.computeIfAbsent(new StringStringTuple(tag, name), t -> ClausewitzHistory.getHistObjects(dataSource.getCountryHistory(t.s1), t.s2, date));
        //return ClausewitzHistory.getHistObjects(dataSource.getCountryHistory(tag), name, date);
    }
    
    public boolean isRhsSet(int provId, String lhsSet, String lhsClear, String rhs) {
        return ClausewitzHistory.isRhsSet(dataSource.getProvinceHistory(provId), lhsSet, lhsClear, rhs, date);
    }
    
    public boolean isRhsSet(String tag, String lhsSet, String lhsClear, String rhs) {
        return ClausewitzHistory.isRhsSet(dataSource.getCountryHistory(tag), lhsSet, lhsClear, rhs, date);
    }
    
    public List<String> getRhsSet(int provId, String lhsSet, String lhsClear) {
        List<String> values = ClausewitzHistory.getHistStrings(dataSource.getProvinceHistory(provId), date, lhsSet, lhsClear);
        return (values == null) ? new ArrayList<>() : values;
    }
    
    public List<String> getRhsSet(String tag, String lhsSet, String lhsClear) {
        List<String> values = ClausewitzHistory.getHistStrings(dataSource.getCountryHistory(tag), date, lhsSet, lhsClear);
        return (values == null) ? new ArrayList<>() : values;
    }
    
    
    public List<String> getCountries() {
        return getCountries(date);
    }
    
    public List<String> getCountries(String date) {
        final java.util.Map<String, Object> ret = new HashMap<>();
        
        for (Integer id : map.getLandProvs()) {
            String owner = getOwner(id);
            
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
            String owner = getOwner(id);
            
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
    
    private List<Integer> warnedAbout = new ArrayList<>();
    public List<Integer[]> getLinesInProv(int provId) {
        ProvinceData.Province p = provinceData.getProvByID(provId);
        if (p == null) {
            if (!warnedAbout.contains(provId)) {
                warnedAbout.add(provId);
                log.log(Level.WARNING, "Unknown province {0}", provId);
            }
            return Collections.emptyList();
        } else
            return mapData.getLinesInProv(p.getColor());
    }
    
    
    private static final class IntStringTuple {
        private final int i;
        private final String s;
        IntStringTuple(int i, String s) {
            this.i = i;
            this.s = s;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof IntStringTuple) {
                return i == ((IntStringTuple)o).i
                        && s.equals(((IntStringTuple)o).s);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + i;
            hash = 73 * hash + Objects.hashCode(s);
            return hash;
        }
    }
    
    private static final class StringStringTuple {
        private final String s1;
        private final String s2;
        StringStringTuple(String s1, String s2) {
            this.s1 = s1;
            this.s2 = s2;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof StringStringTuple) {
                return s1.equals(((StringStringTuple)o).s1)
                        && s2.equals(((StringStringTuple)o).s2);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + Objects.hashCode(s1);
            hash = 73 * hash + Objects.hashCode(s2);
            return hash;
        }
    }
}
