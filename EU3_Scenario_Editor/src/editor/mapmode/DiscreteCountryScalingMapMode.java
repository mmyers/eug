
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * <code>MapMode</code> that highlights provinces lighter or darker depending
 * on the value of a property. The colors are determined in discrete steps
 * between a given minimum and a given maximum, with a given step size.
 * @author Michael Myers
 */
public class DiscreteCountryScalingMapMode extends CountryMode {

    private String name;
    protected String prop;
    private int min;
    private int max;
    private int step;
    
    protected int numColors;
    
    protected Color[] colors;
    
    private Color minColor = Color.RED.darker();
    private Color midColor = Color.YELLOW;
    private Color maxColor = Color.GREEN.darker();
    
    /**
     * Creates a new instance of DiscreteScalingMapMode.
     */
    public DiscreteCountryScalingMapMode(String prop, int min, int max, int step) {
        this.prop = prop;
        this.min = min;
        this.max = max;
        this.step = step;
        initializeColors();
    }
    
    public DiscreteCountryScalingMapMode(MapPanel panel, String prop, int min, int max, int step) {
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
        
        // sanity in case there are too few colors and we have rounding errors
        colors[0] = minColor;
        if (numColors > 0)
            colors[numColors-1] = maxColor;
    }
    
    private static int mix(int min, int max, int ratio, int maxRatio) {
        int ret = (max * ratio);
        ret += (min * (maxRatio - ratio));
        return (ret / maxRatio);
    }
    
    @Override
    protected Color getCtryColor(String countryTag) {
        String value = mapPanel.getModel().getHistString(countryTag, prop);

        if (value == null || value.length() == 0) {
            value = "0";
        }
        
        int index = (int) ((Double.parseDouble(value) + min) / step);
        index = Math.max(0, Math.min(numColors-1, index));
        
        return colors[index];
    }

    @Override
    protected Object getCountryBorderGroup(String countryTag) {
        if (countryTag == null || countryTag.isEmpty() || Utilities.isNotACountry(countryTag))
            return "NO_COUNTRY";

        String value = mapPanel.getModel().getHistString(countryTag, prop);
        if (value == null || value.length() == 0)
            value = "0";

        int index = (int) ((Double.parseDouble(value) + min) / step);
        index = Math.max(0, Math.min(numColors-1, index));
        return index;
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
        
        String ownerTag = mapPanel.getModel().getHistString(id, "owner");
        if (Utilities.isNotACountry(ownerTag))
            return "";
        
        String value = mapPanel.getModel().getHistString(ownerTag, prop);
        if (value == null || value.length() == 0)
            value = "0";
        return "<b>" + Text.getText(ownerTag) + "</b><br>" + prop + ": " + value;
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
