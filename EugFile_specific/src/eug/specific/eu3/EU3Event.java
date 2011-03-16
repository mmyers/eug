/*
 * EU3Event.java
 *
 * Created on September 12, 2007, 2:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eug.specific.eu3;

import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzDataSource;


/**
 *
 * @author jeff
 */
public class EU3Event  extends EU3SpecificObject {
    
    /** Creates a new instance of EU3Event */
    public EU3Event(GenericObject g, ClausewitzDataSource ds) {
        super(g, ds);
    }
    
    public int getID() {
        return 0;
    }
    
    public String getIDAsString() {
        return "";
    }
    
    
    
}
