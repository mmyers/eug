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
        System.out.println("default.map:");
        System.out.println(mapData);
        
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
        for (java.util.Map.Entry<String, List<String>> entry : contList.entrySet()) {
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
            
            final int sea_starts = Integer.parseInt(getString("sea_starts"));
            final List<String> unusedIds = new ArrayList<String>(sea_starts - usedIds.size());
            for (int i = 1; i < sea_starts; i++) {
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
        for (java.util.Map.Entry<String, List<String>> entry : climateList.entrySet()) {
            if (entry.getValue().contains(provId)) {
                return entry.getKey();
            }
        }
        return "(none)";
    }
    
    public String getClimateOfProv(int provId) {
        return getClimateOfProv(Integer.toString(provId));
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
            
            final int sea_starts = Integer.parseInt(getString("sea_starts"));
            final List<String> unusedIds = new ArrayList<String>(sea_starts - usedIds.size());
            for (int i = 1; i < sea_starts; i++) {
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
        for (java.util.Map.Entry<String, List<String>> entry : nativeList.entrySet()) {
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
}
