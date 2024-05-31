
package eug.specific.clausewitz;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import java.util.Arrays;
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
            "discovered_by = latin\n" +
            "\n" +
            "1450.1.1 = { base_tax = 10 }\n" +
            "1550.1.1 = { base_manpower = 5 }";
    
    private static final String ADDITIONAL_OBJECT =
            "add_core = DAN\n" +
            "owner = DAN\n" +
            "hre = no # comment\n" +
            "discovered_by = eastern\n" +
            "1420.1.1 = { controller = REB } # rebellion\n" +
            "1550.1.1 = { religion = protestant }\n" +
            "1450.1.1 = { base_tax = 12 }";
    
    private static final String OBJECT_TO_DELETE =
            "testlist = { AAA }\n" +
            "testlist2 = { AAA }\n" +
            "discovered_by = latin\n" +
            "1550.1.1 = { base_manpower = 5 }";
    
    public ClausewitzHistoryTest(String testName) {
        super(testName);
    }

    /**
     * Test of mergeHistObjects method, of class ClausewitzHistory.
     */
    @Test
    public void testMergeHistObjects() {
        String expected =
                "add_core = DAN\r\n" +
                "owner = DAN\r\n" +
                "controller = SWE\r\n" +
                "culture = swedish\r\n" +
                "base_tax = 8\r\n" +
                "discovered_by = eastern\r\n" +
                "hre = no # comment\r\n" +
                "1420.1.1 = { controller = REB } # rebellion\r\n" +
                "1450.1.1 = { base_tax = 12 } \r\n" +
                "1550.1.1 = { base_manpower = 5 religion = protestant } ";
        
        System.out.println("mergeHistObjects");
        GenericObject existing = EUGFileIO.loadFromString(ORIGINAL_OBJECT);
        GenericObject additions = EUGFileIO.loadFromString(ADDITIONAL_OBJECT);
        
        new ClausewitzHistoryMergeTool().mergeHistObjects(existing, additions, ClausewitzHistoryMergeTool.MergeMode.OVERWRITE);
        
        assertEquals(expected, existing.toString());
    }
    
    @Test
    public void testMergeAddHistObjects() {
        String expected =
                "add_core = SWE\r\n" +
                "owner = SWE\r\n" +
                "controller = SWE\r\n" +
                "culture = swedish\r\n" +
                "base_tax = 8\r\n" +
                "discovered_by = latin\r\n" +
                "add_core = DAN\r\n" +
                "owner = DAN\r\n" +
                "hre = no # comment\r\n" +
                "discovered_by = eastern\r\n" +
                "1420.1.1 = { controller = REB } # rebellion\r\n" +
                "1450.1.1 = { base_tax = 10 base_tax = 12 } \r\n" +
                "1550.1.1 = { base_manpower = 5 religion = protestant } ";
        
        System.out.println("mergeHistObjects");
        GenericObject existing = EUGFileIO.loadFromString(ORIGINAL_OBJECT);
        GenericObject additions = EUGFileIO.loadFromString(ADDITIONAL_OBJECT);
        
        new ClausewitzHistoryMergeTool().mergeHistObjects(existing, additions, ClausewitzHistoryMergeTool.MergeMode.ADD);
        
        assertEquals(expected, existing.toString());
    }
    
    @Test
    public void testAutoMergeHistObjects() {
        String expected =
                "add_core = SWE\r\n" +
                "owner = DAN\r\n" +
                "controller = SWE\r\n" +
                "culture = swedish\r\n" +
                "base_tax = 8\r\n" +
                "discovered_by = latin\r\n" +
                "add_core = DAN\r\n" +
                "hre = no # comment\r\n" +
                "discovered_by = eastern\r\n" +
                "1420.1.1 = { controller = REB } # rebellion\r\n" +
                "1450.1.1 = { base_tax = 12 } \r\n" +
                "1550.1.1 = { base_manpower = 5 religion = protestant } ";
        
        System.out.println("mergeHistObjects");
        GenericObject existing = EUGFileIO.loadFromString(ORIGINAL_OBJECT);
        GenericObject additions = EUGFileIO.loadFromString(ADDITIONAL_OBJECT);
        
        ClausewitzHistoryMergeTool mergeTool = new ClausewitzHistoryMergeTool();
        mergeTool.initAutoMergeList(Arrays.asList("add_core", "discovered_by"));
        mergeTool.mergeHistObjects(existing, additions, ClausewitzHistoryMergeTool.MergeMode.AUTO);
        
        assertEquals(expected, existing.toString());
    }
    
    @Test
    public void testDeleteHistObjects() {
        String expected =
                "testlist = { BBB }\r\n" +
                "add_core = SWE\r\n" +
                "owner = SWE\r\n" +
                "controller = SWE\r\n" +
                "culture = swedish\r\n" +
                "base_tax = 8\r\n" +
                "1450.1.1 = { base_tax = 10 } ";
        
        System.out.println("mergeHistObjects");
        GenericObject existing = EUGFileIO.loadFromString("testlist = { AAA BBB } testlist2 = { AAA } " + ORIGINAL_OBJECT);
        GenericObject additions = EUGFileIO.loadFromString(OBJECT_TO_DELETE);
        
        ClausewitzHistoryMergeTool mergeTool = new ClausewitzHistoryMergeTool();
        mergeTool.mergeHistObjects(existing, additions, ClausewitzHistoryMergeTool.MergeMode.DELETE);
        
        assertEquals(expected, existing.toString());
    }
}
