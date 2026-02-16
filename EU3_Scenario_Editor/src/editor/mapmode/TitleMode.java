package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import eug.shared.GenericObject;
import eug.specific.ck2.CK2DataSource;
import eug.specific.ck3.CK3DataSource;
import eug.specific.clausewitz.ClausewitzDataSource;
import eug.specific.clausewitz.ClausewitzHistory;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * A <code>MapMode</code> that paints all land provinces the color of the
 * landed title that owns them or that is their liege. Only valid for CK2 and CK3.
 * @author Michael Myers
 * @since 0.8.2
 */
public class TitleMode extends ProvincePaintingMode {

    protected CK2DataSource dataSource;
    protected CK3DataSource ck3DataSource;
    
    protected TitleType type;

    public enum TitleType {
        //BARONY("Barony"),
        COUNTY("County"),
        DUCHY("Duchy"),
        KINGDOM("Kingdom"),
        EMPIRE("Empire"),
        HEGEMONY("Hegemony");
        
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
        else if (panel.getDataSource() instanceof CK3DataSource)
            this.ck3DataSource = (CK3DataSource) panel.getDataSource();
        else
            throw new IllegalArgumentException("Title mode is only valid for Crusader Kings 2 or 3");
        this.type = level;
    }
    
    private GenericObject getProvinceHistory(int provId) {
        if (dataSource != null)
            return dataSource.getProvinceHistory(provId);
        return ck3DataSource.getProvinceHistory(provId);
    }
    
    private GenericObject getTitleHistory(String title) {
        if (dataSource != null)
            return dataSource.getTitleHistory(title);
        return ck3DataSource.getTitleHistory(title);
    }

    @Override
    protected void paintProvince(final Graphics2D g, int provId) {
        // In history: a province has a single "title" string defined. Follow that title up the chain as far as necessary.
        // In saved games: a province has multiple barony-level titles defined. Each of them *should* be subject to the same count???
        String owner = getLowestHistTitleHolder(provId);
        if (owner == null || owner.isEmpty()) {
            GenericObject history = getProvinceHistory(provId);
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
    
    protected String getLowestHistTitleHolder(int provId) {
        String title = mapPanel.getModel().getHistString(provId, "title");
        if (title != null && !title.isEmpty())
            return title;
        
        return mapPanel.getMap().getDeJureCounty(provId);
    }

    protected Color getTitleColor(String title) {
        return Utilities.getTitleColor(title);
    }

    protected String getTitleHistString(String title, String name) {
        return ClausewitzHistory.getHistString(getTitleHistory(title), name, mapPanel.getModel().getDate());
    }
    
    protected List<String> getTitleHistStrings(String title, String name) {
        return ClausewitzHistory.getHistStrings(getTitleHistory(title), name, mapPanel.getModel().getDate());
    }
    
    protected String getLiegeHistString(String title, String name) {
        // First check this level of title and see if we can find this key.
        // If not, iterate up the chain of titles until we find one that does have the key. Lowest title wins.
        String originalTitle = title;
        String ret = getTitleHistString(title, name);
        while (ret == null || ret.isEmpty()) {
            title = getTitleHistString(title, "liege");
            if (title == null || title.isEmpty() || title.equals("0"))
                break;
            ret = getTitleHistString(title, name);
        }
        // If we haven't found the key, check previous lieges (much slower to iterate, so we won't do this unless necessary)
        if (ret == null || ret.isEmpty()) {
            return getHistoricalLiegeHistString(originalTitle, name);
        }
        return ret;
    }
    
    private String getHistoricalLiegeHistString(String title, String name) {
        // Iteratively check all lieges of this title
        // First we find all previous lieges. Then for each of them, we check all previous lieges.
        // At each stage we save the last non-empty value of the key.
        String ret = null; // skip the initial check because we already know we don't have the key in the immediate title block
        while (ret == null || ret.isEmpty()) {
            List<String> lieges = getTitleHistStrings(title, "liege");
            if (lieges == null || lieges.isEmpty())
                break;
            for (String previousLiege : lieges) {
                if (previousLiege.equals("0"))
                    continue;
                
                String possibleValue = getTitleHistString(previousLiege, name);
                if (possibleValue == null || possibleValue.isEmpty())
                    continue;
                
                ret = possibleValue;
            }
            title = lieges.get(lieges.size()-1);
        }
        return ret;
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

                if (liege != null && !liege.isEmpty() && !liege.equals("0"))
                    return liege;
                return title;
            case DUCHY:
                if (title.startsWith("b_"))
                    liege = getLiege(getTitleHistString(title, "liege"), level);
                else if (title.startsWith("c_"))
                    liege = getTitleHistString(title, "liege");

                if (liege != null && !liege.isEmpty() && !liege.equals("0"))
                    return liege;
                return title;
            case KINGDOM:
                if (title.startsWith("b_") || title.startsWith("c_"))
                    liege = getLiege(getTitleHistString(title, "liege"), level);
                else if (title.startsWith("d_"))
                    liege = getTitleHistString(title, "liege");

                if (liege != null && !liege.isEmpty() && !liege.equals("0"))
                    return liege;
                return title;
            case EMPIRE:
                if (title.startsWith("b_") || title.startsWith("c_") || title.startsWith("d_")) {
                    String tmpLiege = getTitleHistString(title, "liege");
                    if (tmpLiege != null && tmpLiege.equalsIgnoreCase(title))
                        return title;
                    liege = getLiege(tmpLiege, level);
                } else if (title.startsWith("k_"))
                    liege = getTitleHistString(title, "liege");

                if (liege != null && !liege.isEmpty() && !liege.equals("0"))
                    return liege;
                return title;
            case HEGEMONY:
                if (title.startsWith("b_") || title.startsWith("c_") || title.startsWith("d_") || title.startsWith("k_")) {
                    String tmpLiege = getTitleHistString(title, "liege");
                    if (tmpLiege != null && tmpLiege.equalsIgnoreCase(title))
                        return title;
                    liege = getLiege(tmpLiege, level);
                } else if (title.startsWith("e_"))
                    liege = getTitleHistString(title, "liege");

                if (liege != null && !liege.isEmpty() && !liege.equals("0"))
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
        if (getMap().isWasteland(id))
            return "";

        String lowestTitle = getLowestHistTitleHolder(id);
        
        if (lowestTitle == null || lowestTitle.isEmpty()) {
            GenericObject history = getProvinceHistory(id);
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
        while (true) {
            ret.append(getTitleName(title, current, dataSource)).append("<br>");
            String liege = getTitleHistString(title, "liege");
            if (liege == null || liege.isEmpty() || liege.equals("0") || liege.equalsIgnoreCase(title))
                break;
            title = liege;
        }

        return ret.toString();
    }

    public static String getTitleName(String title, Province current, ClausewitzDataSource dataSource) {
        if (dataSource instanceof CK2DataSource) {
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
        } else {
            if (title.startsWith("h_"))
                return Text.getText("hegemony") + " of " + Text.getText(title);
            else if (title.startsWith("e_"))
                return Text.getText("empire") + " of " + Text.getText(title);
            else if (title.startsWith("k_"))
                return Text.getText("kingdom") + " of " + Text.getText(title);
            else if (title.startsWith("d_"))
                return Text.getText("duchy") + " of " + Text.getText(title);
            else if(title.startsWith("c_"))
                return Text.getText("county") + " of " + Text.getText(title); // counties share the name of the province
            else if (title.startsWith("b_"))
                return Text.getText("barony") + " of " + Text.getText(title);
            else
                return "";
        }
    }
}
