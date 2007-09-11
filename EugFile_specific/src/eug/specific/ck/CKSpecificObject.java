/*
 * CKSpecificObject.java
 *
 * Created on July 13, 2006, 1:59 PM
 */

package eug.specific.ck;

import eug.shared.GenericObject;
import eug.shared.SpecificObject;

/**
 *
 * @author Michael Myers
 */
public class CKSpecificObject extends SpecificObject {
    
    public CKScenario scenario;
    
    /** Creates a new instance of CKSpecificObject */
    public CKSpecificObject(GenericObject o, CKScenario s) {
        go = o;
        scenario = s;
    }
}
