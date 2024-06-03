/*
 * MapPixelData.java
 *
 * Moved out of MapPanel.java in 0.4pre1
 */

package editor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/** @since 0.3pre1 */
public final class MapPixelData {
    
    /**
     * Mapping of rgb value to a list of horizontal lines in the province.
     * Each line is kept in an array of size 3.
     * array[0] is the y value, array[1] is the starting x value, and
     * array[2] is the ending x value.
     */
    private final java.util.Map<Integer, List<Integer[]>> provLines;
    
    /**
     * List of two-element arrays holding coordinates of border pixels.
     * @since 0.5pre3
     */
    private final List<Integer[]> borders;
    
    private static final int BLACK = 0xFF000000; // java.awt.Color.BLACK.getRGB();
    
    public MapPixelData(final BufferedImage img, final int numProvs) {
        provLines = new HashMap<>(numProvs);
        java.util.Map<Integer[], Object> tmpBorders = new HashMap<>(numProvs*32);
        
        int rgb;
        
        final int width = img.getWidth();
        final int height = img.getHeight();
        final int[] rgbLine = new int[width]; // fetch RGB data one line at a time
        
        for (int y = 0; y < height; y++) {
            img.getRGB(0, y, width, 1, rgbLine, 0, 1);
            for (int x = 0; x < width; x++) {
                rgb = rgbLine[x];
                
                if (provLines.get(rgb) == null)
                    provLines.put(rgb, new ArrayList<>(100));
                
                Integer[] points = new Integer[3];
                
                // Store the first point
                points[0] = y;
                points[1] = x;
                
                // Go until it's not the same color or it hits the edge of the image
                while (x < width && rgb == rgbLine[x]) {
                    x++;
                }
                
                points[2] = x;
                
                provLines.get(rgb).add(points);
                if (rgb != BLACK && x < width && rgbLine[x] != BLACK) { // it's PTI, so don't bother with a border
                    tmpBorders.put(new Integer[] {x,y}, null);
                }
                
                x--;
            }
        }
        
        final int[] rgbCol = new int[height];
        
        for (int x = 0; x < width; x++) {
            img.getRGB(x, 0, 1, height, rgbCol, 0, 1);
            for (int y = 0; y < height; y++) {
                rgb = rgbCol[y];
                
                do {
                    y++;
                } while (y < height && rgb == rgbCol[y]);
                if (rgb != BLACK && y < height && rgbCol[y] != BLACK) { // it's PTI, so don't bother with a border
                    tmpBorders.put(new Integer[] {x,y}, null);
                }
            }
        }
        
        borders = new ArrayList<>(tmpBorders.keySet());
    }
    
    public List<Integer[]> getLinesInProv(int rgb) {
        return provLines.get(rgb);
    }
    
    public List<Integer[]> getBorderPixels() {
        return borders;
    }
}
