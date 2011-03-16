/*
 * EU3Scenario.java
 *
 * Created on July 2, 2007, 1:59 PM
 */

package eug.specific.eu3;

import eug.shared.FilenameResolver;
import eug.specific.clausewitz.ClausewitzScenario;

/**
 * Implementation of <code>EU3DataSource</code> which gets country and province
 * data from the history files.
 * @author Michael Myers
 * @since EUGFile 1.06.00pre1
 */
public class EU3Scenario extends ClausewitzScenario {
    
    /** Creates a new instance of EU3Scenario */
    public EU3Scenario(FilenameResolver resolver) {
        super(resolver);
    }
    
    public EU3Scenario(String mainDir, String modDir) {
        super(mainDir, modDir);
    }
    
}
