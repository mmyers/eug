/*
 * Province.java
 *
 * Created on July 13, 2006, 2:29 PM
 */

package eug.specific.ck;

import eug.shared.GenericObject;

/**
 *
 * @author Michael Myers
 */
public class Province extends CKSpecificObject {
    
    /** Creates a new instance of Province */
    public Province(GenericObject go, CKScenario s) {
        super(go, s);
    }
    
    public int getId() {
        return go.getInt("id");
    }
    
    public String getReligion() {
        return go.getString("religion");
    }
    
    public String getCulture() {
        return go.getString("culture");
    }
}
