/*
 * SingleCountryMode.java
 *
 * Created on June 11, 2007, 2:56 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Michael Myers
 */
public class SingleCountryMode extends ProvincePaintingMode {
    
    private Color sameCountryColor = Color.GREEN;
    private Color differentCountryColor = Color.GRAY;

    private String tag;
    
    /** Creates a new instance of SingleCountryMode */
    public SingleCountryMode(String tag) {
        super();
        this.tag = tag;
    }
    
    public SingleCountryMode(MapPanel panel, String tag) {
        super(panel);
        this.tag = tag;
    }

    protected void paintProvince(Graphics2D g, int provId) {
        if (mapPanel.getModel().getHistString(provId, "owner").equalsIgnoreCase(tag))
            mapPanel.paintProvince(g, provId, sameCountryColor);
        else
            mapPanel.paintProvince(g, provId, differentCountryColor);
    }

    protected void paintSeaZone(Graphics2D g, int id) {
        // Do nothing
        return;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public String getTooltipExtraText(Province current) {
        if (!editor.Main.map.isLand(current.getId()))
            return "";
        
        final String owner = mapPanel.getModel().getHistString(current.getId(), "owner");
        if (owner == null || owner.length() == 0)
            return "";
        return "Owned by: " + Text.getText(owner);
    }
}
