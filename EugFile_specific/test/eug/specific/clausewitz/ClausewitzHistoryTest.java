
package eug.specific.clausewitz;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Michael
 */
public class ClausewitzHistoryTest extends TestCase {
    
    private static final String ORIGINAL_OBJECT =
            "add_core = SWE\n" +
            "owner = SWE\n" +
            "controller = SWE\n" +
            "culture = swedish\n" +
            "base_tax = 8\n" +
            "\n" +
            "1450.1.1 = { base_tax = 10 }\n" +
            "1550.1.1 = { base_manpower = 5 }";
    
    private static final String ADDITIONAL_OBJECT =
            "#no hre\n" +
            "hre = no\n" +
            "1420.1.1 = { controller = REB } # rebellion\n" +
            "1550.1.1 = { religion = protestant }\n" +
            "1450.1.1 = { base_tax = 12 }";
    
    public ClausewitzHistoryTest(String testName) {
        super(testName);
    }

    /**
     * Test of mergeHistObjects method, of class ClausewitzHistory.
     */
    @Test
    public void testMergeHistObjects() {
        System.out.println("mergeHistObjects");
        GenericObject existing = EUGFileIO.loadFromString(ORIGINAL_OBJECT);
        GenericObject additions = EUGFileIO.loadFromString(ADDITIONAL_OBJECT);
        
        ClausewitzHistory.mergeHistObjects(existing, additions);
        
        System.out.println(existing.toString());
    }
    
}
