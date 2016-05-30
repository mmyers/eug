package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import eug.shared.GenericObject;
import eug.specific.ck2.CK2DataSource;
import eug.specific.clausewitz.ClausewitzHistory;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A <code>MapMode</code> that paints all land provinces the color of the
 * landed title that owns them or that is their liege. Only valid for CK2.
 * @author Michael Myers
 * @since 0.8.2
 */
public class TitleMode extends ProvincePaintingMode {

    protected CK2DataSource dataSource;
    
    protected TitleType type;

    public enum TitleType {
        //BARONY("Barony"),
        COUNTY("County"),
        DUCHY("Duchy"),
        KINGDOM("Kingdom"),
        EMPIRE("Empire");
        
        private final String name;
        private TitleType(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    };

    public TitleMode(MapPanel panel, TitleType level) {
        super(panel);
        if (panel.getDataSource() instanceof CK2DataSource)
            this.dataSource = (CK2DataSource) panel.getDataSource();
        else
            throw new IllegalArgumentException("Title mode is only valid for Crusader Kings 2");
        this.type = level;
    }

    @Override
    protected void paintProvince(final Graphics2D g, int provId) {
        // In history: a province has a single "title" string defined. Follow that title up the chain as far as necessary.
        // In saved games: a province has multiple barony-level titles defined. Each of them *should* be subject to the same count???
        String owner = mapPanel.getModel().getHistString(provId, "title");
        if (owner == null || owner.isEmpty()) {
            GenericObject history = dataSource.getProvinceHistory(provId);
            if (history != null) {
                for (GenericObject obj : history.children) {
                    if (obj.name.startsWith("b_")) {
                        owner = getLiege(obj.name);
                        break;
                    }
                }
            }
        }
        owner = getLiege(owner);
        mapPanel.paintProvince(g, provId, (owner == null ? Utilities.COLOR_NO_CTRY_DEF : getTitleColor(owner)));
    }

    @Override
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Don't paint sea zones.
    }

    protected Color getTitleColor(String title) {
        return Utilities.getTitleColor(title);
    }

    protected String getTitleHistString(String title, String name) {
        return ClausewitzHistory.getHistString(dataSource.getTitleHistory(title), name, mapPanel.getModel().getDate());
    }

    public String getLiege(String title) {
        return getLiege(title, type);
    }

    protected String getLiege(String title, TitleType level) {
        if (title == null) {
            return null;
        }

        String liege = "";
        switch (level) {
            //case BARONY:
            //    return title;
            case COUNTY:
                if (title.startsWith("b_"))
                    liege = getTitleHistString(title, "liege");

                if (liege != null && !liege.isEmpty())
                    return liege;
                return title;
            case DUCHY:
                if (title.startsWith("b_"))
                    liege = getLiege(getTitleHistString(title, "liege"), level);
                else if (title.startsWith("c_"))
                    liege = getTitleHistString(title, "liege");

                if (liege != null && !liege.isEmpty())
                    return liege;
                return title;
            case KINGDOM:
                if (title.startsWith("b_") || title.startsWith("c_"))
                    liege = getLiege(getTitleHistString(title, "liege"), level);
                else if (title.startsWith("d_"))
                    liege = getTitleHistString(title, "liege");

                if (liege != null && !liege.isEmpty())
                    return liege;
                return title;
            case EMPIRE:
                if (title.startsWith("b_") || title.startsWith("c_") || title.startsWith("d_"))
                    liege = getLiege(getTitleHistString(title, "liege"), level);
                else if (title.startsWith("k_"))
                    liege = getTitleHistString(title, "liege");

                if (liege != null && !liege.isEmpty())
                    return liege;
                return title;
            default:
                return title;
        }
    }

    @Override
    public String getTooltipExtraText(final Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";

        String lowestTitle = mapPanel.getModel().getHistString(id, "title");
        
        if (lowestTitle == null || lowestTitle.isEmpty()) {
            GenericObject history = dataSource.getProvinceHistory(id);
            if (history != null) {
                for (GenericObject obj : history.children) {
                    if (obj.name.startsWith("b_")) {
                        lowestTitle = obj.name;
                        break;
                    }
                }
            }
        }
        
        if (lowestTitle == null)
            return "";

        StringBuilder ret = new StringBuilder();
        String title = lowestTitle;
        while (title != null && !title.isEmpty()) {
            ret.append(getTitleName(title, current)).append("<br>");
            title = getTitleHistString(title, "liege");
        }

        return ret.toString();

//        String title = getLiege(lowestTitle, type);
//        String titleName = getTitleName(title, current);
//        if (!titleName.isEmpty())
//            return titleName;
//
//        return "Unknown owner";
    }

    public static String getTitleName(String title, Province current) {
        if (title.startsWith("e_"))
            return Text.getText("empire_of") + " " + Text.getText(title);
        else if (title.startsWith("k_"))
            return Text.getText("kingdom_of") + " " + Text.getText(title);
        else if (title.startsWith("d_"))
            return Text.getText("duchy_of") + " " + Text.getText(title);
        else if(title.startsWith("c_"))
            return Text.getText("county_of") + " " + current.getName(); // counties share the name of the province
        else if (title.startsWith("b_"))
            return Text.getText("barony_of") + " " + Text.getText(title);
        else
            return "";
    }
}
