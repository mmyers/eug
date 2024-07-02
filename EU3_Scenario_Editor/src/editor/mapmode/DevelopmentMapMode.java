
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;

/**
 *
 * @author Michael
 */
public class DevelopmentMapMode extends DiscreteScalingMapMode {
    
    public DevelopmentMapMode(MapPanel panel, int min, int max, int step) {
        super(panel, "", min, max, step);
        setName("development");
    }

    
    @Override
    protected double getProvinceValue(int provId) {
        String manpower = mapPanel.getModel().getHistString(provId, "base_manpower");
        String baseTax = mapPanel.getModel().getHistString(provId, "base_tax");
        String production = mapPanel.getModel().getHistString(provId, "base_production");
        
        return toInt(manpower) + toInt(baseTax) + toInt(production);
    }
    
    private int toInt(String strInt) {
        if (strInt == null || strInt.isEmpty())
            return 0;
        try {
            return Integer.parseInt(strInt);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        if (getMap().isWasteland(id))
            return "";
        
        String manpower = mapPanel.getModel().getHistString(id, "base_manpower");
        String baseTax = mapPanel.getModel().getHistString(id, "base_tax");
        String production = mapPanel.getModel().getHistString(id, "base_production");
        
        return "Base tax: " + baseTax
                + "<br>Production: " + production
                + "<br>Manpower: " + manpower
                + "<br><b>Total development: " + (int)getProvinceValue(id) + "</b>";
    }
    
    
}
