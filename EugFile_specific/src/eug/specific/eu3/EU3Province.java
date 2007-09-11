/*
 * EU3Province.java
 *
 * Created on March 22, 2007, 9:48 PM
 */

package eug.specific.eu3;

import eug.shared.GenericObject;
import java.util.List;

/**
 *
 * @author Michael Myers
 * @since EUGFile 1.03.00
 */
public class EU3Province extends EU3SpecificObject {
    
    private GenericObject history = null;
    
    /** Creates a new instance of EU3Province */
    public EU3Province(GenericObject go, EU3SaveGame s) {
        super(go, s);
    }
    
    /** Lazy creation of history object. */
    private GenericObject getHistory() {
        if (history == null) {
            history = go.getChild("history");
        }
        return history;
    }
    
    public int getId() {
        return Integer.parseInt(go.name);
    }
    
    public String getOwner() {
        return go.getString("owner");
    }
    
    public String getController() {
        return go.getString("controller");
    }
    
    public List<String> isCoreOf() {
        return go.getStrings("core");
    }
    
    public boolean hasBuilding(String name) {
        return go.getString(name).equals("yes");
    }
    
    public double getCitySize() {
        return go.getDouble("citysize");
    }
    
    public String getCulture() {
        return go.getString("culture");
    }
    
    public String getReligion() {
        return go.getString("religion");
    }
    
    public double getBaseTax() {
        return go.getDouble("base_tax");
    }
    
    public double getManpower() {
        return go.getDouble("manpower");
    }
    
    public String getTradeGoods() {
        return go.getString("trade_goods");
    }
}
