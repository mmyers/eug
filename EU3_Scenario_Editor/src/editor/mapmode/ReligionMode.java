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
    
    private final java.util.Map<String, String> ctryReligions = new HashMap<>();
    
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

        ctryReligions.clear();

        final List<String> tags = model.getTags();
        for (String tag : tags) {
            ctryReligions.put(tag.toUpperCase(), model.getHistString(tag, "religion"));
        }
    }
    
    protected void paintProvince(final Graphics2D g, int provId) {
        if (getMap().isWasteland(provId)) {
            mapPanel.paintProvince(g, provId, java.awt.Color.BLACK);
            return;
        }
        
        final String religion = mapPanel.getModel().getHistString(provId, "religion");
        
        if (religion == null) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_HIST);
        } else if (religion.length() == 0 || religion.equalsIgnoreCase("none")) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_RELIGION);
        } else {
            final String owner = mapPanel.getModel().getOwner(provId).toUpperCase();
            final String ownerRel = ctryReligions.get(owner);

            if (ownerRel == null || religion.equalsIgnoreCase(ownerRel)) {
                mapPanel.paintProvince(g, provId, Utilities.getReligionColor(religion));
            } else {
                final Paint p = Utilities.createPaint(Utilities.getReligionColor(religion), Utilities.getReligionColor(ownerRel));
                mapPanel.paintProvince(g, provId, p);
            }
        }
    }
    
    @Override
    protected void paintSeaZone(final Graphics2D g, int id) {
        // do nothing
    }

    @Override
    public Object getBorderGroup(final int provId) {
        if (getMap().isWasteland(provId))
            return "WASTELAND";
        if (!getMap().isLand(provId))
            return "SEA_ZONE";

        final String religion = mapPanel.getModel().getHistString(provId, "religion");
        if (religion == null)
            return "MISSING";
        if (religion.length() == 0 || religion.equalsIgnoreCase("none"))
            return "NONE";

        final String owner = mapPanel.getModel().getOwner(provId);
        final String ownerRel = (owner == null) ? null : ctryReligions.get(owner.toUpperCase());
        if (ownerRel == null || religion.equalsIgnoreCase(ownerRel))
            return "REL:" + religion.toLowerCase();
        return "REL:" + religion.toLowerCase() + "|OWNER_REL:" + ownerRel.toLowerCase();
    }
    
    
    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        final String rel = Text.getText(mapPanel.getModel().getHistString(id, "religion"));
        if (rel == null || rel.length() == 0)
            return "";
        
        String owner = mapPanel.getModel().getOwner(id);
        String ctryRel = null;
        
        if (owner != null) {
            if (Utilities.isNotACountry(owner)) {
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
