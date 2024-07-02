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

    private String name;
    protected String prop;
    private int min;
    private int max;
    private int step;
    
    protected Color[] colors;
    
    // TODO: Perhaps make it so an arbitrary number of colors could be used,
    // fading from one to another?
    private Color minColor = Color.RED.darker();
    private Color midColor = Color.YELLOW;
    private Color maxColor = Color.GREEN.darker();
    
    /**
     * Creates a new instance of DiscreteScalingMapMode.
     */
    public DiscreteScalingMapMode(String prop, int min, int max, int step) {
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
        colors = Utilities.createSteppedColors(min, max, step, minColor, midColor, maxColor);
    }
    
    @Override
    protected void paintProvince(final Graphics2D g, int provId) {
        String value = mapPanel.getModel().getHistString(provId, prop);

        if (value == null || value.length() == 0 || value.equals("0")) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
            return;
        }
        
        int index = (int) ((Double.parseDouble(value) + min) / step);
        index = Math.max(0, Math.min(colors.length-1, index));
        
        mapPanel.paintProvince(g, provId, colors[index]);
    }
    
    @Override
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Do nothing
    }
    
    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name + " from " + min + " to " + max;
    }
    
}
