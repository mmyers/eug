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
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 *
 * @author Michael Myers
 */
public final class Map {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Map.class.getName());
    
    private static final String MAP_DIR_NAME = "/map";
    
    private GenericObject mapData;
    
    private int maxProvinces;
    
    private GenericObject continents;
    private java.util.Map<String, List<Integer>> contList = null;
    
    private GenericObject climates;
    private java.util.Map<String, List<Integer>> climateList = null;
    
    private GenericObject natives;
    private java.util.Map<String, List<Integer>> nativeList = null;
    
    private GenericObject areas;
    private java.util.Map<String, List<Integer>> areaList = null;
    
    private GenericObject regions;
    private java.util.Map<String, List<Integer>> regionList = null;
    
    private GenericObject superRegions;
    private java.util.Map<String, List<Integer>> superRegionList = null;
    
    private GenericObject provinceGroups;
    private java.util.Map<String, List<Integer>> provinceGroupList = null;
    
    private GenericObject terrains;
    private java.util.Map<String, Color> terrainColorsList = null;
    private java.util.Map<String, Terrain> terrainOverridesList = null;
    
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

    private String readFile(String path) {
        try {
            String ret = new String(Files.readAllBytes(Paths.get(path)));
            if (ret.charAt(0) == 0xFFEF)
                return ret.substring(1);
            return ret;
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
            return "";
        }
    }
    
    private String stripColors(String text) {
        // Areas could be:
        // bourgogne_area = { 191 192 193 1877 }
        // or:
        // bourgogne_area = { color = { 144 0 32 } 191 192 193 1877 }

        // This method strips such color blocks completely out of a file. It's
        // easier than rewriting the parsing code to work with objects inside lists.
        
        StringBuilder sb = new StringBuilder(text.length());
        int index = 0;
        
        while (true) {
            int nextIndex = text.indexOf("color =", index);
            if (nextIndex == -1)
                break;
            
            sb.append(text.substring(index, nextIndex));
            index = text.indexOf("}", nextIndex) + 1;
        }
        sb.append(text.substring(index));
        
        return sb.toString();
    }
    
    private void loadData() {
        String mapFilename = resolver.resolveFilename(MAP_DIR_NAME + File.separator + "default.map");
        
        final ParserSettings fastSettings = ParserSettings.getNoCommentSettings().setPrintTimingInfo(false);
        
        log.log(Level.INFO, "Reading map data from {0}", mapFilename);
        mapData = EUGFileIO.load(mapFilename, fastSettings);
        if (mapData == null) {
            log.log(Level.SEVERE, "Failed to load map file from {0}", mapFilename); 
            throw new RuntimeException("Failed to load map file " + mapFilename);
        }
        
        maxProvinces = mapData.getInt("max_provinces");
        
        String contFilename = mapData.getString("continent").replace('\\', '/');
        if (!contFilename.contains("/"))
            contFilename = MAP_DIR_NAME + '/' + contFilename;
        
        continents = EUGFileIO.load(resolver.resolveFilename(contFilename), fastSettings);
        
        if (version.hasClimateTxt()) {
            String climateFilename = mapData.getString("climate").replace('\\', '/');
            
            // Vic2 has climates but doesn't have the path in default.map
            if (climateFilename.equals(""))
                climateFilename = MAP_DIR_NAME + File.separator + "climate.txt";
            else if (!climateFilename.contains("/"))
                climateFilename = MAP_DIR_NAME + '/' + climateFilename;
            
            climates = EUGFileIO.load(resolver.resolveFilename(climateFilename), fastSettings);
        }
        
        String nativesFilename = "common/natives.txt";
        
        natives = EUGFileIO.load(resolver.resolveFilename(nativesFilename), fastSettings);
        
        String terrainFilename = mapData.getString("terrain_definition").replace('\\', '/');
        if (!terrainFilename.contains("/"))
            terrainFilename = MAP_DIR_NAME + '/' + terrainFilename;
        
        terrains = EUGFileIO.load(resolver.resolveFilename(terrainFilename), fastSettings);
        
        if (version.hasRegions()) {
            String regFilename = mapData.getString("region").replace('\\', '/');
            if (!regFilename.contains("/"))
                regFilename = MAP_DIR_NAME + '/' + regFilename;

            String regionText = stripColors(readFile(resolver.resolveFilename(regFilename)));
            regions = EUGFileIO.loadFromString(regionText, fastSettings);
            
            if (mapData.hasString("area")) {
                String areaFilename = mapData.getString("area").replace('\\', '/');
                if (!areaFilename.contains("/"))
                    areaFilename = MAP_DIR_NAME + '/' + areaFilename;
                
                String areaText = stripColors(readFile(resolver.resolveFilename(areaFilename)));
                areas = EUGFileIO.loadFromString(areaText, fastSettings);
            }
            if (mapData.hasString("superregion")) {
                String srFilename = mapData.getString("superregion").replace('\\', '/');
                if (!srFilename.contains("/"))
                    srFilename = MAP_DIR_NAME + '/' + srFilename;
                
                String srText = stripColors(readFile(resolver.resolveFilename(srFilename)));
                superRegions = EUGFileIO.loadFromString(srText, fastSettings);
            }
            if (mapData.hasString("provincegroup")) {
                String pgFilename = mapData.getString("provincegroup").replace('\\', '/');
                if (!pgFilename.contains("/"))
                    pgFilename = MAP_DIR_NAME + '/' + pgFilename;
                
                provinceGroups = EUGFileIO.load(resolver.resolveFilename(pgFilename), fastSettings);
            }
        }
        
        if (version.hasLandList()) {
            // Initialize boolean array
            isLand = new boolean[maxProvinces];
            for (int i = 1; i < isLand.length; i++) {
                isLand[i] = true;   // unfortunately, the default is false
            }
            
            GenericList seaProvs = mapData.getList("sea_starts");
            if (seaProvs == null) {
                List<GenericList> seaZonesLists = mapData.lists.stream()
                        .filter(w -> w.getName().equalsIgnoreCase("sea_zones"))
                        .collect(Collectors.toList());
                if (seaZonesLists.isEmpty()) {
                    log.log(Level.WARNING, "No sea_starts or sea_zones found in default.map; weird things might start happening now");
                } else {
                    for (GenericList seaZoneRange : seaZonesLists) {
                        if (seaZoneRange.size() == 2) {
                            int start = Integer.parseInt(seaZoneRange.get(0));
                            int end = Integer.parseInt(seaZoneRange.get(1));
                            for (int i = start; i <= end; i++) {
                                isLand[i] = false;
                            }
                        }
                    }
                }
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
            if (version.hasRivers()) {
                GenericList rivers = mapData.getList("major_rivers");
                if (rivers != null) {
                    for (String provId : rivers) {
                        int id = Integer.parseInt(provId);
                        isLand[id] = false;
                    }
                }
            }
        } else if (mapData.getString("sea_starts").isEmpty()) {
            log.log(Level.WARNING, "Error: No numeric value for sea_starts found in map file. Game should probably been defined has_land_list = yes");
        }
    }
    
    /**
     * Converts a list of Strings to a list of Integers. If an element of the
     * list cannot be converted to integer, this method will print a warning
     * and skip it.
     * @param stringList the list to convert
     * @param name the name of the list, for debug error messages
     * @return the list of integers, not including any invalid strings
     */
    private static List<Integer> convertToIntList(List<String> stringList, String name) {
        // Performance is not critical in this method, since it is only called a few times on startup and never again
        List<Integer> ret = new ArrayList<>(stringList.size());
        for (int i = 0; i < stringList.size(); i++) {
            String next = stringList.get(i);
            try {
                int test = Integer.parseInt(next);
                ret.add(test);
            } catch (NumberFormatException ex) {
                log.log(Level.WARNING, "Expected {0} list to have only province ID numbers, but found {1}", new Object[] { name, next });
            }
        }
        return ret;
    }
    
    public java.util.Map<String, List<Integer>> getContinents() {
        if (contList == null) {
            contList = new HashMap<>(continents.size());
            if (!continents.lists.isEmpty()) {
                for (GenericList cont : continents.lists) {
                    contList.put(cont.getName(), convertToIntList(cont.getList(), "continent"));
                }
            } else {
                // should have continent = { provinces = { 1 2 3 } }
                for (GenericObject cont : continents.children) {
                    if (cont.containsList("provinces"))
                        contList.put(cont.name, convertToIntList(cont.getList("provinces").getList(), "continent"));
                }
            }
        }
        return contList;
    }
    
    public List<Integer> getContinent(String name) {
        return getContinents().get(name);
    }
    
    public String getContinentOfProv(String provId) {
        return getContinentOfProv(Integer.parseInt(provId));
    }
    
    public String getContinentOfProv(int provId) {
        for (java.util.Map.Entry<String, List<Integer>> entry : getContinents().entrySet()) {
            if (entry.getValue().contains(provId)) {
                return entry.getKey();
            }
        }
        return "(none)";
    }
    
    public java.util.Map<String, List<Integer>> getClimates() {
        if (climateList == null) {
            if (climates == null) {
                climateList = new HashMap<>();
                return climateList;
            }
            
            climateList = new HashMap<>(climates.size()+1);
            final List<Integer> usedIds = new ArrayList<>(1000);
            for (GenericList climate : climates.lists) {
                final List<Integer> ids = convertToIntList(climate.getList(), "climate");
                usedIds.addAll(ids);
                climateList.put(climate.getName(), ids);
            }
            Collections.sort(usedIds);
            
            final List<Integer> unusedIds = new ArrayList<>();
            for (int i : getLandProvs()) {
                final int idx = Collections.binarySearch(usedIds, i);
                if (idx < 0) {
                    unusedIds.add(i);
                }
            }
            climateList.put("normal", unusedIds);
        }
        return climateList;
    }
    
    public List<Integer> getClimate(String name) {
        return getClimates().get(name);
    }
    
    public String getClimateOfProv(String provId) {
        return getClimateOfProv(Integer.parseInt(provId));
    }
    
    public String getClimateOfProv(int provId) {
        for (java.util.Map.Entry<String, List<Integer>> entry : getClimates().entrySet()) {
            if (entry.getValue().contains(provId)) {
                return entry.getKey();
            }
        }
        return "(none)";
    }
    
    public boolean isWasteland(int provId) {
        List<Integer> wasteland = getClimates().get("impassable");
        return wasteland != null && wasteland.contains(provId);
    }
    
    public java.util.Map<String, List<Integer>> getAreas() {
        if (areaList == null) {
            areaList = new HashMap<>(areas.size());
            
            areas.lists.stream()
                    .forEach((area) -> areaList.put(area.getName(), convertToIntList(area.getList(), "area")));

            // empty lists are parsed as objects, so convert them
            areas.children.stream()
                    .filter(obj -> obj.isEmpty())
                    .forEach(obj -> areaList.put(obj.name, new ArrayList<>()));
        }
        return areaList;
    }
    
    public java.util.Map<String, List<Integer>> getRegions() {
        if (regionList == null) {
            regionList = new HashMap<>(regions.size());
            
            // regions could be:
            // france = { 0 1 2 }
            // or:
            // france = { provinces = { 0 1 2 } }
            // or:
            // france = { areas = { brittany_area normandy_area provence_area } }
            // either way, it's easier to deal with later if we just flatten it all out now
            if (!regions.lists.isEmpty()) {
                for (GenericList reg : regions.lists) {
                    if (areas != null) {
                        // areas.txt exists, so assume regions are lists of areas (EU4 1.14)
                        List<Integer> aggregate = reg.getList().stream()
                                .flatMap(area -> getArea(area).stream()) // list all provinces in each area of the region
                                .collect(Collectors.toList());
                        regionList.put(reg.getName(), aggregate);
                    } else {
                        // france = { 0 1 2 }
                        regionList.put(reg.getName(), convertToIntList(reg.getList(), "region"));
                    }
                }
            } else {
                // france = { provinces = { 0 1 2 } }
                // or france = { areas = { brittany_area normandy_area provence_area } }
                for (GenericObject reg : regions.children) {
                    if (reg.containsList("areas")) {
                        List<Integer> aggregate = reg.getList("areas").getList().stream()
                                .flatMap(area -> getArea(area).stream()) // list all provinces in each area of the region
                                .collect(Collectors.toList());
                        regionList.put(reg.name, aggregate);
                    } else if (reg.containsList("provinces")) {
                        regionList.put(reg.name, convertToIntList(reg.getList("provinces").getList(), "region"));
                    }
                }
            }
        }
        return regionList;
    }
    
    public java.util.Map<String, List<Integer>> getSuperRegions() {
        if (superRegionList == null) {
            superRegionList = new HashMap<>(superRegions.size());
            if (!superRegions.lists.isEmpty()) {
                for (GenericList sr : superRegions.lists) {
                    // at this point, if superregions.txt exists, just assume areas.txt also exists
                    List<Integer> aggregate = sr.getList().stream()
                            .filter(region -> !region.equals("restrict_charter")) // EU4 puts this special string in some superregions, but it's not a region
                            .flatMap(region -> getRegion(region).stream()) // list all provinces in each area of each region of the superregion
                            .collect(Collectors.toList());
                    superRegionList.put(sr.getName(), aggregate);
                }
            }
        }
        return superRegionList;
    }
    
    public java.util.Map<String, List<Integer>> getProvinceGroups() {
        // exactly like areas.txt
        if (provinceGroupList == null) {
            provinceGroupList = new HashMap<>(provinceGroups.size());
            if (!provinceGroups.lists.isEmpty()) {
                for (GenericList group : provinceGroups.lists) {
                    provinceGroupList.put(group.getName(), convertToIntList(group.getList(), "province group"));
                }
            }
        }
        return provinceGroupList;
    }
    
    public List<Integer> getArea(String name) {
        List<Integer> ret = getAreas().get(name);
        if (ret == null) {
            log.log(Level.WARNING, "Area {0} is not defined in the map files", name);
            ret = new ArrayList<>();
            areaList.put(name, ret);
        }
        return ret;
    }
    
    public List<Integer> getRegion(String name) {
        List<Integer> ret = getRegions().get(name);
        if (ret == null) {
            log.log(Level.WARNING, "Region {0} is not defined in the map files", name);
            ret = new ArrayList<>();
            regionList.put(name, ret);
        }
        return ret;
    }
    
    public List<Integer> getSuperRegion(String name) {
        List<Integer> ret = getSuperRegions().get(name);
        if (ret == null) {
            log.log(Level.WARNING, "Super region {0} is not defined in the map files", name);
            ret = new ArrayList<>();
            superRegionList.put(name, ret);
        }
        return ret;
    }
    
    public List<Integer> getProvinceGroup(String name) {
        List<Integer> ret = getProvinceGroups().get(name);
        if (ret == null) {
            log.log(Level.WARNING, "Province group {0} is not defined in the map files", name);
            ret = new ArrayList<>();
            provinceGroupList.put(name, ret);
        }
        return ret;
    }
    
    public List<String> getAreasOfProv(String provId) {
        return getAreasOfProv(Integer.parseInt(provId));
    }
    
    public List<String> getAreasOfProv(int provId) {
        List<String> ret = getAreas().entrySet().stream()
                .filter(entry -> entry.getValue().contains(provId))
                .map(Entry::getKey)
                .collect(Collectors.toList());
        
        if (ret.isEmpty())
            ret.add("(none)");
        
        return ret;
    }
    
    public List<String> getRegionsOfProv(String provId) {
        return getRegionsOfProv(Integer.parseInt(provId));
    }
    
    public List<String> getRegionsOfProv(int provId) {
        List<String> ret = getRegions().entrySet().stream()
                .filter((entry) -> (entry.getValue().contains(provId)))
                .map(Entry::getKey)
                .collect(Collectors.toList());
        
        if (ret.isEmpty())
            ret.add("(none)");
        
        return ret;
    }
    
    public List<String> getSuperRegionsOfProv(String provId) {
        return getSuperRegionsOfProv(Integer.parseInt(provId));
    }
    
    public List<String> getSuperRegionsOfProv(int provId) {
        List<String> ret = getSuperRegions().entrySet().stream()
                .filter(entry -> entry.getValue().contains(provId))
                .map(Entry::getKey)
                .collect(Collectors.toList());
        
        if (ret.isEmpty())
            ret.add("(none)");
        
        return ret;
    }
    
    /** Returns all province groups (from provincegroup.txt) that this province is in. */
    public List<String> getGroupsOfProv(String provId) {
        return getGroupsOfProv(Integer.parseInt(provId));
    }
    
    /** Returns all province groups (from provincegroup.txt) that this province is in. */
    public List<String> getGroupsOfProv(int provId) {
        List<String> ret = getProvinceGroups().entrySet().stream()
                .filter(entry -> entry.getValue().contains(provId))
                .map(Entry::getKey)
                .collect(Collectors.toList());
        
        if (ret.isEmpty())
            ret.add("(none)");
        
        return ret;
    }

    public void setNatives(GenericObject natives) {
        this.natives = natives;
        this.nativeList = null;
    }
    
    public java.util.Map<String, List<Integer>> getNatives() {
        if (nativeList == null) {
            nativeList = new HashMap<>(natives.size()+1);
            final List<Integer> usedIds = new ArrayList<>(1000);
            for (GenericObject nativeGroup : natives.children) {
                final List<Integer> ids = convertToIntList(nativeGroup.getList("provinces").getList(), "natives");
                usedIds.addAll(ids);
                nativeList.put(nativeGroup.name, ids);
            }
            Collections.sort(usedIds);
            
            final List<Integer> unusedIds = new ArrayList<>();
            for (int i : getLandProvs()) {
                final int idx = Collections.binarySearch(usedIds, i);
                if (idx < 0) {
                    unusedIds.add(i);
                }
            }
            nativeList.put("normal", unusedIds);
        }
        return nativeList;
    }
    
    public List<Integer> getNatives(String name) {
        return getNatives().get(name);
    }
    
    public String getNativeTypeOfProv(String provId) {
        return getNativeTypeOfProv(Integer.parseInt(provId));
    }
    
    public String getNativeTypeOfProv(int provId) {
        for (java.util.Map.Entry<String, List<Integer>> entry : getNatives().entrySet()) {
            if (entry.getValue().contains(provId)) {
                return entry.getKey();
            }
        }
        return "(none)";
    }
    
    public java.util.Map<String, Color> getTerrainColors() {
        if (terrainColorsList == null) {
            terrainColorsList = new HashMap<>();
            
            GenericObject categories = terrains.getChild("categories");
            if (categories != null) {
                for (GenericObject terrain : categories.children) {
                    terrainColorsList.put(terrain.name, parseColor(terrain.getList("color")));
                }
            }
        }
        return terrainColorsList;
    }
    
    public java.util.Map<String, Terrain> getTerrainOverrides() {
        if (terrainOverridesList == null) {
            terrainOverridesList = new HashMap<>();
            
            GenericObject categories = terrains.getChild("categories");
            if (categories != null) {
                for (GenericObject terrain : categories.children) {
                    if (terrain.containsList("terrain_override")) {
                        terrainOverridesList.put(terrain.name,
                                new Terrain(terrain.name, parseColor(terrain.getList("color")), terrain.getList("terrain_override").getList()));
                    }
                }
            }
        }
        return terrainOverridesList;
    }
    
    // same as editor.mapmode.Utilities
    private static Color parseColor(GenericList color) {
        if (color.size() != 3) {
            log.log(Level.WARNING, "Unable to parse color: {0}", color.toString());
            return null;
        }
        
        // No rail correction is done in this method. Float colors are assumed to be between 0.0 and 1.0
        // and integer colors are assumed to be between 0 and 255.
        // If a value is out of bounds, an IllegalArgumentException will be thrown.
        
        float r = Float.parseFloat(color.get(0));
        float g = Float.parseFloat(color.get(1));
        float b = Float.parseFloat(color.get(2));
        
        if (color.get(0).contains(".") || color.get(1).contains(".") || color.get(2).contains("."))
            return new Color(r, g, b);
        
        if (r > 1 || g > 1 || b > 1) // assume [0, 255] scale if any value is outside [0, 1]
            return new Color((int) r, (int) g, (int) b);
        return new Color(r, g, b);
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
    
    public int getMaxProvinces() {
        return maxProvinces;
    }
    
    private final class INLandProvIterator
            implements Iterable<Integer>, Iterator<Integer> {

        private final int numProvs;
        private int index;
        
        public INLandProvIterator() {
            this.numProvs = maxProvinces;
            index = 1;
        }
        
        @Override
        public Iterator<Integer> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            while (index < numProvs) {
                if (isLand[index])
                    return true;
                index++;
            }
            
            return false;
        }

        @Override
        public Integer next() {
            if (index >= numProvs || !isLand[index])
                throw new java.util.NoSuchElementException();
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
            if (index >= seaStarts)
                throw new java.util.NoSuchElementException();
            return index++;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public static class Terrain {
        private final String name;
        private final Color color;
        private final List<String> overrides;
        private Terrain(String name, Color color, List<String> overrides) {
            this.name = name;
            this.color = color;
            this.overrides = overrides;
        }
        
        public String getName() {
            return name;
        }
        
        public Color getColor() {
            return color;
        }
        
        public List<String> getOverrides() {
            return overrides;
        }
    }
}
