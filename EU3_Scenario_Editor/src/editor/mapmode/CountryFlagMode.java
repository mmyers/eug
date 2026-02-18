
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Highlights provinces whose owner has the specified flag set. That is, they must have
 * <code>set_country_flag = &lt;flag&gt;</code>
 * without a corresponding
 * <code>clr_country_flag = &lt;flag&gt;</code>
 * in their history.
 * @author Michael
 */
public class CountryFlagMode extends CountryMode {
    
    private final String flagName;
    
    private static final String SET_COUNTRY_FLAG = "set_country_flag";
    private static final String CLR_COUNTRY_FLAG = "clr_country_flag";
    
    public CountryFlagMode(MapPanel panel, String flagName) {
        super(panel);
        this.flagName = flagName;
    }

    @Override
    protected Color getCtryColor(String country) {
        boolean flag = mapPanel.getModel().isRhsSet(country, SET_COUNTRY_FLAG, CLR_COUNTRY_FLAG, flagName);
        
        return flag ? Color.GREEN : Color.DARK_GRAY;
    }

    @Override
    protected Object getCountryBorderGroup(String country) {
        if (country == null || country.isEmpty() || Utilities.isNotACountry(country))
            return "NO_COUNTRY";
        return mapPanel.getModel().isRhsSet(country, SET_COUNTRY_FLAG, CLR_COUNTRY_FLAG, flagName);
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        String owner = mapPanel.getModel().getOwner(current.getId());
        if (Utilities.isNotACountry(owner))
            return "";
        
        StringBuilder sb = new StringBuilder("<html>Flags: <br>");
        List<String> flags = mapPanel.getModel().getRhsSet(owner, SET_COUNTRY_FLAG, CLR_COUNTRY_FLAG);
        sb.append(String.join("<br>", flags));
        return sb.toString();
    }
}
