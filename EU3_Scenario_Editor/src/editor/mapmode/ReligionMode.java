/*
 * ReligionMode.java
 *
 * Created on August 18, 2007, 6:21 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.MapPanelDataModel;
import editor.ProvinceData.Province;
import editor.Text;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Michael Myers
 * @since 0.5pre3
 */
public final class ReligionMode extends ProvincePaintingMode {
    
    private String lastDate = null;
    private final java.util.Map<String, String> ctryReligions =
            new HashMap<String, String>();
    
    /**
     * Creates a new instance of ReligionMode.
     */
    public ReligionMode() {
    }
    
    public ReligionMode(MapPanel panel) {
        super(panel);
    }

    @Override
    protected void paintingStarted(final Graphics2D g) {
        final MapPanelDataModel model = mapPanel.getModel();
        if (lastDate != null && lastDate.equals(model.getDate()))
            return;

        ctryReligions.clear();

        final List<String> tags = model.getTags();
        for (String tag : tags) {
            ctryReligions.put(tag.toUpperCase(), model.getHistString(tag, "religion"));
        }

        lastDate = model.getDate();
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        final String religion = mapPanel.getModel().getHistString(provId, "religion");
        
        if (religion == null) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_HIST);
        } else if (religion.length() == 0 || religion.equalsIgnoreCase("none")) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_RELIGION);
        } else {
            final String owner = mapPanel.getModel().getHistString(provId, "owner").toUpperCase();
            //final String ownerRel = (Utilities.isNotACountry(owner) ? null : mapPanel.getModel().getHistString(owner, "religion"));
            final String ownerRel = ctryReligions.get(owner);

            if (ownerRel == null || religion.equalsIgnoreCase(ownerRel)) {
                mapPanel.paintProvince(g, provId, Utilities.getReligionColor(religion));
            } else {
                final Paint p = Utilities.createPaint(Utilities.getReligionColor(religion), Utilities.getReligionColor(ownerRel));
//                if (p != null)
                    mapPanel.paintProvince(g, provId, p);
//                else
//                    System.err.println("Unknown problem in ReligionMode.java");
            }
        }
    }
    
    protected void paintSeaZone(final Graphics2D g, int id) {
        // do nothing
    }
    
    
    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        final String rel = Text.getText(mapPanel.getModel().getHistString(id, "religion"));
        if (rel == null || rel.length() == 0)
            return "";
        
        String owner = mapPanel.getModel().getHistString(id, "owner");
        String ctryRel = null;
        
        if (owner != null) {
            if (Utilities.isNotACountry(owner) /*noCountryPattern.matcher(owner).matches()*/) {
                // don't add anything
                owner = "-";
            } else {
                owner = owner.toUpperCase();
                ctryRel = Text.getText(mapPanel.getModel().getHistString(owner, "religion"));
            }
        } else {
            owner = "-";
        }
        
        final StringBuilder text = new StringBuilder(rel).append("<br />");
        if (!owner.equals("-") && ctryRel != null)
            text.append(Text.getText(owner)).append(": ").append(ctryRel);
        
        return text.toString();
    }
    
}
