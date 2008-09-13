/*
 * DiscreteScalingMapMode.java
 *
 * Created on June 14, 2007, 2:01 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * <code>MapMode</code> that highlights provinces lighter or darker depending
 * on the value of a property. The colors are determined in discrete steps
 * between a given minimum and a given maximum, with a given step size.
 * @author Michael Myers
 */
public class DiscreteScalingMapMode extends ProvincePaintingMode {
    
    private String prop;
    private int min;
    private int max;
    private int step;
    
    private int numColors;
    
    protected Color[] colors;
    
    // TODO: Perhaps make it so an arbitrary number of colors could be used,
    // fading from one to another?
    private Color minColor = Color.GREEN.darker();
    private Color midColor = Color.YELLOW;
    private Color maxColor = Color.WHITE;
    
    /**
     * Creates a new instance of DiscreteScalingMapMode.
     */
    public DiscreteScalingMapMode(String prop, int min, int max, int step) {
        super();
        this.prop = prop;
        this.min = min;
        this.max = max;
        this.step = step;
        initializeColors();
    }
    
    public DiscreteScalingMapMode(MapPanel panel, String prop, int min, int max, int step) {
        super(panel);
        this.prop = prop;
        this.min = min;
        this.max = max;
        this.step = step;
        initializeColors();
    }
    
    private void initializeColors() {
        numColors = (max-min)/step;
        colors = new Color[numColors];
        final int minRed = minColor.getRed(),       midRed = midColor.getRed(),     maxRed = maxColor.getRed();
        final int minGreen = minColor.getGreen(),   midGreen = midColor.getGreen(), maxGreen = maxColor.getGreen();
        final int minBlue = minColor.getBlue(),     midBlue = midColor.getBlue(),   maxBlue = maxColor.getBlue();
        final int middle = Math.max(1, numColors/2);
        for (int i = 0; i < middle; i++) {
            int red = mix(minRed, midRed, i, middle);
            int gr = mix(minGreen, midGreen, i, middle);
            int bl = mix(minBlue, midBlue, i, middle);
            colors[i] = new Color(red, gr, bl);
        }
        for (int i = middle; i < numColors; i++) {
            int red = mix(midRed, maxRed, i - middle, middle);
            int gr = mix(midGreen, maxGreen, i - middle, middle);
            int bl = mix(midBlue, maxBlue, i - middle, middle);
            colors[i] = new Color(red, gr, bl);
        }
    }
    
    private static int mix(int min, int max, int ratio, int maxRatio) {
        int ret = (max * ratio);
        ret += (min * (maxRatio - ratio));
        return (ret / maxRatio);
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        String value = mapPanel.getModel().getHistString(provId, prop);
        if (value == null || value.length() == 0)
            value = "0";
        
        int index = (int) ((Double.parseDouble(value) + min) / step);
        index = Math.max(0, Math.min(numColors-1, index));
        
        mapPanel.paintProvince(g, provId, colors[index]);
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Do nothing
        return;
    }
    
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!editor.Main.map.isLand(id))
            return "";
        
        String value = mapPanel.getModel().getHistString(id, prop);
        if (value == null || value.length() == 0)
            value = "0";
        return prop + ": " + value;
    }
    
    public Color getMinColor() {
        return minColor;
    }
    
    public void setMinColor(Color minColor) {
        this.minColor = minColor;
        initializeColors();
    }
    
    public Color getMidColor() {
        return midColor;
    }
    
    public void setMidColor(Color midColor) {
        this.midColor = midColor;
        initializeColors();
    }
    
    public Color getMaxColor() {
        return maxColor;
    }
    
    public void setMaxColor(Color maxColor) {
        this.maxColor = maxColor;
        initializeColors();
    }
    
    public int getMin() {
        return min;
    }
    
    public void setMin(int min) {
        this.min = min;
        initializeColors();
    }
    
    public int getMax() {
        return max;
    }
    
    public void setMax(int max) {
        this.max = max;
        initializeColors();
    }
    
    public int getStep() {
        return step;
    }
    
    public void setStep(int step) {
        this.step = step;
        initializeColors();
    }
    
}
