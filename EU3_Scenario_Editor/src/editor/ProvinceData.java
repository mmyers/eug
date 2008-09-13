/*
 * ProvinceData.java
 *
 * Created on January 26, 2007, 6:30 PM
 */

package editor;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 *
 * @author Michael Myers
 */
public final class ProvinceData {
    
    private java.util.Map<Integer, Province> rgbMap;
    private Province[] allProvs;
    private static final int ALPHA = 0xff << 24;
    private static final Pattern SEMICOLON = Pattern.compile(";");
    
    /**
     * Creates a new instance of ProvinceData.
     */
    public ProvinceData(Map map) {
        final int numProvs = Integer.parseInt(map.getString("max_provinces"));
        
        rgbMap = new HashMap<Integer, Province>(numProvs);
        allProvs = new Province[numProvs];
        
        String defFileName = map.getString("definitions").replace('\\', '/');
        if (!defFileName.contains("/"))
            defFileName = "map/" + defFileName;
        
        try {
            parseDefs(Main.filenameResolver.resolveFilename(defFileName), numProvs);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void parseDefs(String fileName, int numProvs) throws FileNotFoundException, IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(fileName));
        
        try {
            reader.readLine();  // eat first line
            
            for (int i = 1; i < numProvs; i++) {
                String line;
                if ((line = reader.readLine()) == null)
                    break;
                
                String[] arr = SEMICOLON.split(line);
            
                if (arr == null) {
                    System.err.println("Badly formatted province definition: " + line);
                    continue;
                }
                
                final int id = Integer.parseInt(arr[0]);
                final int r = Integer.parseInt(arr[1]);
                final int g = Integer.parseInt(arr[2]);
                final int b = Integer.parseInt(arr[3]);
                
                int color = ALPHA;
                color += (r & 0xFF) << 16;
                color += (g & 0xFF) << 8;
                color += (b & 0xFF);
                
                final Province p = new Province(id, arr[4], color);
                
                rgbMap.put(color, p);
                allProvs[id] = p;
            }
        } finally {
            reader.close();
        }
        
        final Province ti = new Province(0, "Terra Incognita", ALPHA);
        rgbMap.put(ALPHA, ti);
        allProvs[0] = ti;
    }
    
    public Province getProv(final Color color) {
        return getProv(color.getRGB());
    }
    
//    /**
//     * @param rgb a string with three integer components separated by commas.
//     */
//    public Province getProv(final String rgb) {
//        final String[] components = rgb.split(",");
//        final int red = Integer.parseInt(components[0]);
//        final int green = Integer.parseInt(components[1]);
//        final int blue = Integer.parseInt(components[2]);
//        return getProv(new Color(red, green, blue));
//    }
    
    public Province getProv(final int rgb) {
        return rgbMap.get(rgb);
    }
    
    public Province getProvByID(int id) {
        return allProvs[id];
    }
    
    public Province getProvByName(String name) {
        for (Province prov : rgbMap.values()) {
            if (name.equalsIgnoreCase(prov.name))
                return prov;
        }
        return null;
    }
    
    public int getRGB(int provId) {
        for (java.util.Map.Entry<Integer, Province> e : rgbMap.entrySet()) {
            if (e.getValue().getId() == provId) {
                return e.getKey();
            }
        }
        return 0;
    }
    
    /** @return a shallow copy of the province array. */
    public Province[] getAllProvs() {
        final Province[] ret = new Province[allProvs.length];
        System.arraycopy(allProvs, 0, ret, 0, allProvs.length);
        return ret;
    }
    
    /**
     * Class that encapsulates the data associated with an entry in the province
     * definition file.
     */
    public static final class Province {
        
        private int id;
        private String name;
        private int color;
        
        Province(int id, String name, int color) {
            this.id = id;
            this.name = name;
            this.color = color;
        }
        
        public int getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public int getColor() {
            return color;
        }
        
        public String toString() {
            return id + " = " + name;
        }
    }
}
