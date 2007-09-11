/*
 * Country.java
 *
 * Created on July 13, 2006, 2:20 PM
 */

package eug.specific.ck;

import eug.shared.GenericObject;

/**
 *
 * @author Michael Myers
 */
public class Country extends CKSpecificObject {
    
    /** Creates a new instance of Country */
    public Country(GenericObject go, CKScenario s) {
        super(go, s);
    }
    
    public String getTag() {
        return go.getString("tag");
    }
    
    
}
