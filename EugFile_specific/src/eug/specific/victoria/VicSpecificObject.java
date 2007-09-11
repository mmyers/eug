/*
 * "Mother class" of classes used to provide object type-specific methods
 */

package eug.specific.victoria;

import eug.shared.GenericObject;
import eug.shared.SpecificObject;

public abstract class VicSpecificObject extends SpecificObject {
    public VicScenario scenario;
    
    public VicSpecificObject(GenericObject o, VicScenario t) {
        go = o;
        scenario = t;
    }
    
}
