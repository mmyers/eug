/*
 * EU2SpecificObject.java
 *
 * Created on June 23, 2006, 6:57 PM
 */

package eug.specific.eu2;

import eug.shared.GenericObject;
import eug.shared.SpecificObject;

/**
 *
 * @author Michael Myers
 */
public class EU2SpecificObject extends SpecificObject {
    
    public EU2Scenario scenario;
    
    /** Creates a new instance of EU2SpecificObject */
    public EU2SpecificObject(GenericObject o, EU2Scenario s) {
        go = o;
        scenario = s;
    }
}
