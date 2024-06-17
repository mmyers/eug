
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import eug.shared.GenericObject;
import eug.specific.ck2.CK2DataSource;
import eug.specific.ck3.CK3DataSource;
import eug.specific.clausewitz.ClausewitzHistory;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;


/**
 * Similar to {@link TitleMode} except purely based on landed_titles definition.
 * @since 0.9.13
 */
public class DeJureTitleMode extends ProvincePaintingMode {
    
    protected CK2DataSource dataSource;
    protected CK3DataSource ck3DataSource;
    
    protected final TitleType type;
    private final java.util.Map<Integer, String> provinceTitles;

    public enum TitleType {
        //BARONY("Barony", "b_"),
        COUNTY("County", "c_"),
        DUCHY("Duchy", "d_"),
        KINGDOM("Kingdom", "k_"),
        EMPIRE("Empire", "e_");
        
        private final String name;
        private final String prefix;
        
        private TitleType(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }
        public String getName() {
            return name;
        }
        public String getPrefix() {
            return prefix;
        }
    };

    public DeJureTitleMode(MapPanel panel, TitleType level) {
        super(panel);
        if (panel.getDataSource() instanceof CK2DataSource)
            this.dataSource = (CK2DataSource) panel.getDataSource();
        else if (panel.getDataSource() instanceof CK3DataSource)
            this.ck3DataSource = (CK3DataSource) panel.getDataSource();
        else
            throw new IllegalArgumentException("Title mode is only valid for Crusader Kings 2 or 3");
        
        this.type = level;
        this.provinceTitles = new HashMap<>();
        
        initData();
    }

    private void initData() {
        java.util.Map<String, List<Integer>> allTitles = mapPanel.getMap().getAllDeJureHoldings();
        if (allTitles == null)
            return;
        
        for (java.util.Map.Entry<String, List<Integer>> title : allTitles.entrySet()) {
            String titleTag = title.getKey();
            
            if (!titleTag.startsWith(type.getPrefix()))
                continue;
            
            List<Integer> holdingProvIds = title.getValue();
            for (Integer provId : holdingProvIds) {
                provinceTitles.put(provId, titleTag);
            }
        }
    }
    

    @Override
    protected void paintProvince(final Graphics2D g, int provId) {
        String title = provinceTitles.get(provId);
        mapPanel.paintProvince(g, provId, Utilities.getTitleColor(title));
    }

    @Override
    protected void paintSeaZone(final Graphics2D g, int id) {
        // Don't paint sea zones.
    }

    protected Color getTitleColor(String title) {
        return Utilities.getTitleColor(title);
    }

    protected String getTitleHistString(String title, String name) {
        return ClausewitzHistory.getHistString(getTitleHistory(title), name, mapPanel.getModel().getDate());
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

    public String getLiege(String title) {
        return mapPanel.getMap().getDeJureLiege(title);
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
            default:
                return title;
        }
    }

    @Override
    public String getTooltipExtraText(final ProvinceData.Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        if (getMap().isWasteland(id))
            return "";
        
        String title = provinceTitles.get(id);
        if (title == null)
            return "No de jure title at this level";

        StringBuilder ret = new StringBuilder();
        while (true) {
            ret.append(TitleMode.getTitleName(title, current, dataSource)).append("<br>");
            String liege = mapPanel.getMap().getDeJureLiege(title);
            if (liege == null || liege.isEmpty() || liege.equals("0") || liege.equalsIgnoreCase(title))
                break;
            title = liege;
        }

        return ret.toString();

//        String title = getLiege(lowestTitle, type);
//        String titleName = getTitleName(title, current);
//        if (!titleName.isEmpty())
//            return titleName;
//
//        return "Unknown owner";
    }
}
