/*
 * Map.java
 *
 * Created on January 26, 2007, 6:31 PM
 */

package editor;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Michael Myers
 */
public final class Map {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Map.class.getName());
    
    static final String MAP_DIR_NAME = "/map";
    
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

    private final FilenameResolver resolver;
    private final GameVersion version;
    
    /**
     * Creates a new instance of Map.
     */
    public Map(FilenameResolver resolver, GameVersion version) {
        this.resolver = resolver;
        this.version = version;
//        try {
            loadData();
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        }
    }

    
    private void loadData() {
        //log.log(Level.INFO, "Map file is {0}", resolver.resolveFilename(MAP_DIR_NAME + File.separator + "default.map"));
        String mapFilename = resolver.resolveFilename(MAP_DIR_NAME + File.separator + "default.map");
        mapData = EUGFileIO.load(mapFilename, ParserSettings.getQuietSettings());
        if (mapData == null) {
            log.log(Level.SEVERE, "Failed to load map file from {0}", mapFilename); 
            throw new RuntimeException("Failed to load map file " + mapFilename);
        }
        
        String contFilename = mapData.getString("continent").replace('\\', '/');
        if (!contFilename.contains("/"))
            contFilename = MAP_DIR_NAME + '/' + contFilename;
        
        continents = EUGFileIO.load(
                resolver.resolveFilename(contFilename),
                ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                );
        
        if (version.hasClimateTxt()) {
            String climateFilename = mapData.getString("climate").replace('\\', '/');
            if (!climateFilename.contains("/"))
                climateFilename = MAP_DIR_NAME + '/' + climateFilename;

            climates = EUGFileIO.load(
                    resolver.resolveFilename(climateFilename),
                    ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                    );
        }
        
        String nativesFilename = "common/natives.txt";
        
        natives = EUGFileIO.load(
                resolver.resolveFilename(nativesFilename),
                ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                );
        
        if (version.hasRegions()) {
            String regFilename = mapData.getString("region").replace('\\', '/');
            if (!regFilename.contains("/"))
                regFilename = MAP_DIR_NAME + '/' + regFilename;

            regions = EUGFileIO.load(
                    resolver.resolveFilename(regFilename),
                    ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                    );
        }
        
        if (version.hasLandList()) {
            // Initialize boolean array
            isLand = new boolean[mapData.getInt("max_provinces")];
            for (int i = 1; i < isLand.length; i++) {
                isLand[i] = true;   // unfortunately, the default is false
            }
            
            GenericList seaProvs = mapData.getList("sea_starts");
            if (seaProvs == null) {
                log.log(Level.WARNING, "No sea_starts found in default.map; weird things might start happening now");
            } else {
                for (String provId : seaProvs) {
                    int id = Integer.parseInt(provId);
                    isLand[id] = false;
                }
            }

            if (version.hasLakes()) {
                GenericList lakes = mapData.getList("lakes");
                if (lakes != null) {
                    for (String provId : lakes) {
                        int id = Integer.parseInt(provId);
                        isLand[id] = false;
                    }
                }
            }
        } else if (mapData.getString("sea_starts").isEmpty()) {
            log.log(Level.WARNING, "Error: No numeric value for sea_starts found in map file. Game should probably been defined has_land_list = yes");
        }
    }
    
    public java.util.Map<String, List<String>> getContinents() {
        if (contList == null) {
            contList = new HashMap<>(continents.size());
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
            climateList = new HashMap<>(climates.size()+1);
            final List<String> usedIds = new ArrayList<>(1000);
            for (GenericList climate : climates.lists) {
                final List<String> ids = climate.getList();
                usedIds.addAll(ids);
                climateList.put(climate.getName(), ids);
            }
            Collections.sort(usedIds);
            
            final List<String> unusedIds = new ArrayList<>();
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
            regionList = new HashMap<>(regions.size());
            if (!regions.lists.isEmpty()) {
                for (GenericList cont : regions.lists) {
                    regionList.put(cont.getName(), cont.getList());
                }
            } else {
                for (GenericObject reg : regions.children) {
                    if (reg.containsList("provinces"))
                        regionList.put(reg.name, reg.getList("provinces").getList());
                }
            }
        }
        return regionList;
    }
    
    public List<String> getRegion(String name) {
        return getRegions().get(name);
    }
    
    public List<String> getRegionsOfProv(String provId) {
        List<String> ret = new ArrayList<>();
        getRegions().entrySet().stream()
                .filter((entry) -> (entry.getValue().contains(provId)))
                .forEach((entry) -> {
            ret.add(entry.getKey());
        });
        
        if (ret.isEmpty())
            ret.add("(none)");
        
        return ret;
    }
    
    public List<String> getRegionsOfProv(int provId) {
        return getRegionsOfProv(Integer.toString(provId));
    }

    public void setNatives(GenericObject natives) {
        this.natives = natives;
        this.nativeList = null;
    }
    
    public java.util.Map<String, List<String>> getNatives() {
        if (nativeList == null) {
            nativeList = new HashMap<>(natives.size()+1);
            final List<String> usedIds = new ArrayList<>(1000);
            for (GenericObject nativeGroup : natives.children) {
                final List<String> ids = nativeGroup.getList("provinces").getList();
                usedIds.addAll(ids);
                nativeList.put(nativeGroup.name, ids);
            }
            Collections.sort(usedIds);
            
            final List<String> unusedIds = new ArrayList<>();
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
        if (version.hasLandList())
            return new INLandProvIterator();
        return new LandProvIterator(mapData.getInt("sea_starts"));
    }
    
    public boolean isLand(int provId) {
        if (provId <= 0)
            return false;
        
        if (version.hasLandList())
            return isLand[provId];
        return provId < mapData.getInt("sea_starts");
    }

    public int getFirstSeaProv() {
        if (version.hasLandList()) {
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
        
        @Override
        public Iterator<Integer> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            while (++index < numProvs) {
                if (isLand[index])
                    return true;
            }
            
            return false;
        }

        @Override
        public Integer next() {
            return index++;
        }

        @Override
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

        @Override
        public Iterator<Integer> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return index < seaStarts;
        }

        @Override
        public Integer next() {
            return index++;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
}
