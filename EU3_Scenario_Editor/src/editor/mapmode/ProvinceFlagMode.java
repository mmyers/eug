
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Highlights provinces that have the specified flag set. That is, they must have
 * <code>set_province_flag = &lt;flag&gt;</code>
 * without a corresponding
 * <code>clr_province_flag = &lt;flag&gt;</code>
 * in their history.
 * @author Michael
 */
public class ProvinceFlagMode extends ProvincePaintingMode {
    
    private final String flagName;
    
    private static final String SET_PROVINCE_FLAG = "set_province_flag";
    private static final String CLR_PROVINCE_FLAG = "clr_province_flag";
    
    public ProvinceFlagMode(MapPanel panel, String flagName) {
        super(panel);
        this.flagName = flagName;
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        // flags are tricky because the left-hand side isn't unique, just the right-hand side
        // so the usual getHistString will only retrieve the last flag that was set
        // and getHistStrings will return all of them, but not the dates they were set
        boolean flag = mapPanel.getModel().isRhsSet(provId, SET_PROVINCE_FLAG, CLR_PROVINCE_FLAG, flagName);
        
        if (flag) {
            mapPanel.paintProvince(g, provId, Color.GREEN);
        } else {
            mapPanel.paintProvince(g, provId, Color.DARK_GRAY);
        }
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        // probably don't care about sea zones - who is setting flags on them?
    }
    
    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        StringBuilder sb = new StringBuilder("<html>Flags: <br>");
        List<String> flags = mapPanel.getModel().getRhsSet(current.getId(), SET_PROVINCE_FLAG, CLR_PROVINCE_FLAG);
        sb.append(String.join("<br>", flags));
        return sb.toString();
    }
}
