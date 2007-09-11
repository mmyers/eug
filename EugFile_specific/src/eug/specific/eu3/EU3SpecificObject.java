/*
 * EU3SpecificObject.java
 *
 * Created on March 22, 2007, 2:22 PM
 */

package eug.specific.eu3;

import eug.shared.GenericObject;
import eug.shared.SpecificObject;
import java.util.Comparator;

/**
 *
 * @author Michael Myers
 * @since EUGFile 1.03.00
 */
public class EU3SpecificObject extends SpecificObject {
    
    public EU3SaveGame scenario;
    
    /** Creates a new instance of EU3SpecificObject */
    public EU3SpecificObject(GenericObject o, EU3SaveGame s) {
        go = o;
        scenario = s;
    }
    
}
