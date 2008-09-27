/*
 * Map.java
 *
 * Created on January 26, 2007, 6:31 PM
 */

package editor;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Michael Myers
 */
public final class Map {
    
    private static final String MAP_DIR_NAME = "/map";
    
    private GenericObject mapData;
    
    private GenericObject continents;
    private java.util.Map<String, List<String>> contList = null;
    
    private GenericObject climates;
    private java.util.Map<String, List<String>> climateList = null;
    
    private GenericObject natives;
    private java.util.Map<String, List<String>> nativeList = null;
    
    private GenericObject regions;
    private java.util.Map<String, List<String>> regionList = null;
    
    private boolean[] isLand = null;   // for In Nomine mainly
    
    private boolean isInNomine;
    
    /**
     * Creates a new instance of Map.
     */
    public Map() {
        try {
            loadData();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadData() throws FileNotFoundException {
        System.out.println("Map file is " + Main.filenameResolver.resolveFilename(MAP_DIR_NAME + "/default.map"));
        mapData = EUGFileIO.load(Main.filenameResolver.resolveFilename(MAP_DIR_NAME + "/default.map"));
//        System.out.println("default.map:");
//        System.out.println(mapData);
        if (mapData == null) {
            throw new RuntimeException("Failed to load map file");
        }
        isInNomine = mapData.getString("sea_starts").length() == 0;
        
        System.out.println(isInNomine ? "In Nomine" : "Not In Nomine");
        
        String contFilename = mapData.getString("continent").replace('\\', '/');
        if (!contFilename.contains("/"))
            contFilename = MAP_DIR_NAME + '/' + contFilename;
        
        continents = EUGFileIO.load(
                Main.filenameResolver.resolveFilename(contFilename),
                ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                );
        
        String climateFilename = mapData.getString("climate").replace('\\', '/');
        if (!climateFilename.contains("/"))
            climateFilename = MAP_DIR_NAME + '/' + climateFilename;
        
        climates = EUGFileIO.load(
                Main.filenameResolver.resolveFilename(climateFilename),
                ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                );
        
        String nativesFilename = "common/natives.txt";
        
        natives = EUGFileIO.load(
                Main.filenameResolver.resolveFilename(nativesFilename),
                ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                );
        
        if (isInNomine) {
            String regFilename = mapData.getString("region").replace('\\', '/');
            if (!regFilename.contains("/"))
                regFilename = MAP_DIR_NAME + '/' + regFilename;

            regions = EUGFileIO.load(
                    Main.filenameResolver.resolveFilename(regFilename),
                    ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                    );
        }
        
        if (isInNomine) {
            // Initialize boolean array
            isLand = new boolean[mapData.getInt("max_provinces")];
            for (int i = 1; i < isLand.length; i++) {
                isLand[i] = true;   // unfortunately, the default is false
            }
            
            GenericList seaProvs = mapData.getList("sea_starts");
            if (seaProvs == null) {
                System.err.println("No sea_starts found in default.map; weird things might start happening now");
            } else {
                for (String provId : seaProvs) {
                    int id = Integer.parseInt(provId);
                    isLand[id] = false;
                }
            }
        }
    }
    
    public java.util.Map<String, List<String>> getContinents() {
        if (contList == null) {
            contList = new HashMap<String, List<String>>(continents.size());
            for (GenericList cont : continents.lists) {
                contList.put(cont.getName(), cont.getList());
            }
        }
        return contList;
    }
    
    public List<String> getContinent(String name) {
        return getContinents().get(name);
    }
    
    public String getContinentOfProv(String provId) {
        for (java.util.Map.Entry<String, List<String>> entry : getContinents().entrySet()) {
            if (entry.getValue().contains(provId)) {
                return entry.getKey();
            }
        }
        return "(none)";
    }
    
    public String getContinentOfProv(int provId) {
        return getContinentOfProv(Integer.toString(provId));
    }
    
    public java.util.Map<String, List<String>> getClimates() {
        if (climateList == null) {
            climateList = new HashMap<String, List<String>>(climates.size()+1);
            final List<String> usedIds = new ArrayList<String>(1000);
            for (GenericList climate : climates.lists) {
                final List<String> ids = climate.getList();
                usedIds.addAll(ids);
                climateList.put(climate.getName(), ids);
            }
            Collections.sort(usedIds);
            
            final List<String> unusedIds = new ArrayList<String>();
            for (int i : getLandProvs()) {
                final String sid = Integer.toString(i);
                final int idx = Collections.binarySearch(usedIds, sid);
                if (idx < 0) {
                    unusedIds.add(sid);
                }
            }
            climateList.put("normal", unusedIds);
        }
        return climateList;
    }
    
    public List<String> getClimate(String name) {
        return getClimates().get(name);
    }
    
    public String getClimateOfProv(String provId) {
        for (java.util.Map.Entry<String, List<String>> entry : getClimates().entrySet()) {
            if (entry.getValue().contains(provId)) {
                return entry.getKey();
            }
        }
        return "(none)";
    }
    
    public String getClimateOfProv(int provId) {
        return getClimateOfProv(Integer.toString(provId));
    }
    
    public boolean hasRegions() {
        return regions != null;
    }
    
    public java.util.Map<String, List<String>> getRegions() {
        if (regionList == null) {
            regionList = new HashMap<String, List<String>>(regions.size());
            for (GenericList cont : regions.lists) {
                regionList.put(cont.getName(), cont.getList());
            }
        }
        return regionList;
    }
    
    public List<String> getRegion(String name) {
        return getRegions().get(name);
    }
    
    public List<String> getRegionsOfProv(String provId) {
        List<String> ret = new ArrayList<String>();
        for (java.util.Map.Entry<String, List<String>> entry : getRegions().entrySet()) {
            if (entry.getValue().contains(provId)) {
                ret.add(entry.getKey());
            }
        }
        
        if (ret.size() == 0)
            ret.add("(none)");
        
        return ret;
    }
    
    public List<String> getRegionsOfProv(int provId) {
        return getRegionsOfProv(Integer.toString(provId));
    }
    
    public java.util.Map<String, List<String>> getNatives() {
        if (nativeList == null) {
            nativeList = new HashMap<String, List<String>>(natives.size()+1);
            final List<String> usedIds = new ArrayList<String>(1000);
            for (GenericObject nativeGroup : natives.children) {
                final List<String> ids = nativeGroup.getList("provinces").getList();
                usedIds.addAll(ids);
                nativeList.put(nativeGroup.name, ids);
            }
            Collections.sort(usedIds);
            
            final List<String> unusedIds = new ArrayList<String>();
            for (int i : getLandProvs()) {
                final String sid = Integer.toString(i);
                final int idx = Collections.binarySearch(usedIds, sid);
                if (idx < 0) {
                    unusedIds.add(sid);
                }
            }
            nativeList.put("normal", unusedIds);
        }
        return nativeList;
    }
    
    public List<String> getNatives(String name) {
        return getNatives().get(name);
    }
    
    public String getNativeTypeOfProv(String provId) {
        for (java.util.Map.Entry<String, List<String>> entry : getNatives().entrySet()) {
            if (entry.getValue().contains(provId)) {
                return entry.getKey();
            }
        }
        return "(none)";
    }
    
    public String getNativeTypeOfProv(int provId) {
        return getNativeTypeOfProv(Integer.toString(provId));
    }
    
    public String getString(String key) {
        return mapData.getString(key);
    }
    
    public Iterable<Integer> getLandProvs() {
        if (isInNomine)
            return new INLandProvIterator();
        return new LandProvIterator(mapData.getInt("sea_starts"));
    }
    
    public boolean isLand(int provId) {
        if (provId <= 0)
            return false;
        
        if (isInNomine)
            return isLand[provId];
        return provId < mapData.getInt("sea_starts");
    }

    public boolean isInNomine() {
        return isInNomine;
    }

    public int getFirstSeaProv() {
        if (isInNomine) {
            for (int i = 1; i < isLand.length; i++) {
                if (!isLand[i])
                    return i;
            }
            return isLand.length;
        } else {
            return Integer.parseInt(mapData.getString("sea_starts"));
        }
    }
    
    private final class INLandProvIterator
            implements Iterable<Integer>, Iterator<Integer> {

        private final int numProvs;
        private int index;
        
        public INLandProvIterator() {
            this.numProvs = mapData.getInt("max_provinces");
            index = 1;
        }
        
        public Iterator<Integer> iterator() {
            return this;
        }

        public boolean hasNext() {
            while (++index < numProvs) {
                if (isLand[index])
                    return true;
            }
            
            return false;
        }

        public Integer next() {
            return index++;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static final class LandProvIterator
            implements Iterable<Integer>, Iterator<Integer> {
        
        private final int seaStarts;
        private int index;

        public LandProvIterator(int seaStarts) {
            // requires a parameter so we can make this class static
            this.seaStarts = seaStarts;
            index = 1;
        }

        public Iterator<Integer> iterator() {
            return this;
        }

        public boolean hasNext() {
            return index < seaStarts;
        }

        public Integer next() {
            return index++;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
}
