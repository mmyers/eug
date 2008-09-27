/*
 * CapitalsMode.java
 *
 * Created on Feb 23, 2008, 11:15:31 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>MapMode</code> that highlights all provinces that are capitals.
 * @author Michael
 */
public final class CapitalsMode extends ProvincePaintingMode {

    protected Color foundColor = Color.GREEN;
    protected Color notFoundColor = new Color(100, 100, 100); // darker than GRAY, lighter than DARK_GRAY
    
    private final Map<Integer, String> capitals =
            new HashMap<Integer, String>(100);
    
    public CapitalsMode() {
        super();
    }
    
    public CapitalsMode(MapPanel panel) {
        super(panel);
    }
    
    @Override
    protected void paintingStarted(final Graphics2D g) {
        capitals.clear();
        final List<String> tags = mapPanel.getModel().getTags();
        
        for (String tag : tags) {
            String cap = mapPanel.getModel().getHistString(tag, "capital");
            if (cap.length() != 0)
                capitals.put(Integer.parseInt(cap), Text.getText(tag));
        }
    }
    
    @Override
    protected void paintProvince(final Graphics2D g, int provId) {
        if (capitals.containsKey(provId))
            mapPanel.paintProvince(g, provId, foundColor);
        else
            mapPanel.paintProvince(g, provId, notFoundColor);
    }

    @Override
    protected void paintSeaZone(final Graphics2D g, int id) {
        // do nothing
    }

    @Override
    public String getTooltipExtraText(final Province current) {
        if (capitals.containsKey(current.getId()))
            return "Capital of " + capitals.get(current.getId());
        return "";
    }

}
