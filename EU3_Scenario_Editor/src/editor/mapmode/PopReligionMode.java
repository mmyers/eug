
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.specific.victoria2.Vic2DataSource;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Michael
 */
public class PopReligionMode extends PopMode {
    
    private String lastDate;
    private final java.util.Map<Integer, List<GenericObject>> provPops;
    private final java.util.Map<Integer, String> provReligions;
    private final java.util.Map<Integer, Integer> provTotalPopulation;
    
    private static final String SIZE_KEY = "size";
    
    public PopReligionMode(MapPanel panel) {
        super(panel);
        
        provPops = new HashMap<>();
        provReligions = new HashMap<>();
        provTotalPopulation = new HashMap<>();
    }

    @Override
    protected void paintingStarted(Graphics2D g) {
        if (mapPanel.getModel().getDate().equals(lastDate))
            return;
        
        lastDate = mapPanel.getModel().getDate();
        
        provPops.clear();
        provReligions.clear();
        provTotalPopulation.clear();
        
        Vic2DataSource ds = (Vic2DataSource)mapPanel.getDataSource();
        for (int i = 0; i < mapPanel.getMap().getMaxProvinces(); i++) {
            List<GenericObject> pops = ds.getPops(i); // each of these objects will be <provid> = { <pops> }, so we'll need to go down another level
            int total = 0;
            java.util.Map<String, Integer> religions = new HashMap<>();
            for (GenericObject provPop : pops) {
                for (GenericObject pop : provPop.children) {
                    int size = pop.getInt(SIZE_KEY);
                    if (size < 0)
                        continue;
                    total += size;
                    provPops.computeIfAbsent(i, k -> new ArrayList<>()).add(pop);
                    
                    String religion = getReligion(pop);
                    religions.merge(religion, size, (val1, val2) -> val1 + val2);
                }
            }
            
            if (!pops.isEmpty() && !religions.isEmpty()) {
                // sum pops by religion and find the largest group
                Optional<Map.Entry<String, Integer>> max = religions.entrySet().stream().max(java.util.Map.Entry.comparingByValue());
                if (!max.isPresent())
                    continue;
                String topReligion = max.get().getKey();
                provReligions.put(i, topReligion);
                Collections.sort(provPops.get(i),
                        (GenericObject o1, GenericObject o2) -> Integer.compare(o2.getInt(SIZE_KEY), o1.getInt(SIZE_KEY))); // descending
                provTotalPopulation.put(i, total);
            }
        }
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        String topReligion = provReligions.get(provId);
        if (topReligion == null) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
        } else {
            mapPanel.paintProvince(g, provId, Utilities.getReligionColor(topReligion));
        }
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        // do nothing
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        int provId = current.getId();
        if (!mapPanel.getMap().isLand(provId))
            return "";
        
        List<GenericObject> pops = provPops.get(provId);
        if (pops == null)
            return "No pops";
        
        String topReligion = provReligions.get(provId);
        int sum = pops.stream().filter(pop -> getReligion(pop).equalsIgnoreCase(topReligion)).mapToInt(pop -> pop.getInt(SIZE_KEY)).sum();
        double percent = 100 * sum / (double) provTotalPopulation.get(provId);
        
        StringBuilder ret = new StringBuilder("Majority religion: ").append(Text.getText(topReligion)).append(" (").append(String.format("%.1f", percent)).append("%)");
        for (GenericObject pop : pops) {
            // "600 Albanian Catholic aristocrats"
            ret.append("<br>")
                    .append(pop.getInt(SIZE_KEY))
                    .append(" ")
                    .append(Text.getText(getCulture(pop)))
                    .append(" ")
                    .append(Text.getText(getReligion(pop)))
                    .append(" ")
                    .append(Text.getText(pop.name));
        }
        return ret.toString();
    }
    
}
