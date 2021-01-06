/*
 * CtryReligionMode.java
 *
 * Created on July 30, 2007, 12:19 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Color;

/**
 *
 * @author Michael Myers
 * @since 0.5pre3
 */
public class CtryReligionMode extends CountryMode {
    
    // Mostly the same as ProvReligionMode, but paints countries instead.
    
    /** Creates a new instance of CtryReligionMode */
    public CtryReligionMode() {
    }
    
    public CtryReligionMode(MapPanel panel) {
        super(panel);
    }
    
    @Override
    protected Color getCtryColor(String country) {
        if (Utilities.isNotACountry(country))
            return Utilities.COLOR_LAND_DEFAULT;
//        if (noCountryPattern.matcher(country).matches())
//            country = "NAT";
        else
            country = country.toUpperCase();
        
        final String religion = mapPanel.getModel().getHistString(country, "religion");
        
        if (religion == null) {
            return Utilities.COLOR_NO_HIST;
        } else if (religion.length() == 0 || religion.equalsIgnoreCase("none")) {
            return Utilities.COLOR_NO_RELIGION;
        } else {
            return Utilities.getReligionColor(religion);
        }
    }
    
    
    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        final String rel = Text.getText(mapPanel.getModel().getHistString(id, "religion"));
        //if (rel.length() == 0)
        //    return "";
        
        String owner = mapPanel.getModel().getOwner(id);
        String ctryRel = null;
        
        if (owner != null) {
            owner = owner.toUpperCase();
            if (Utilities.isNotACountry(owner)) {
                // don't add anything
                owner = "-";
            } else {
                ctryRel = Text.getText(mapPanel.getModel().getHistString(owner, "religion"));
            }
        } else {
            owner = "-";
        }
        
        final StringBuilder text = new StringBuilder("Owner: ");
        if (!owner.equals("-"))
            text.append(Text.getText(owner)).append(" ").append("(").append(owner).append(")<br />");
        if (rel.length() != 0)
            text.append("Religion: ").append(rel);
        
        if (ctryRel != null)
            text.append("<br />Owner's religion: ").append(ctryRel);
        
        return text.toString();
    }
    
}
