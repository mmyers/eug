package eug.parser;

import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.HeaderComment;
import eug.shared.Style;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael
 */
public class EUGFileIOTest {
    
    private static final ParserSettings TEST_SETTINGS = ParserSettings.getDefaults()
                                                            .setWarningsAreErrors(true)
                                                            .setTryToRecover(false)
            .setPrintWarnings(true)
            .setPrintTimingInfo(true);
    
    private static final String PARSE_STRING =
            "test = {\n"
                + "\tvalue = 1\n"
                + "\tlist = { 1 2 3 }\n"
                + "\tchild = { childvalue = 1 }\n"
                + "}";
    
    // Testing with this string uncovered an apparent bug, that a header comment
    // with \n\n after it would simply be absorbed into the header comment for
    // the next object. Only \r\n\r\n works to separate the two.
    private static final String PARSE_STRING_WITH_HEADER =
            "# Header comment\r\n\r\n"
            + "# Object comment\n"
            + PARSE_STRING;
    
    private static GenericObject buildObject() {
        GenericObject composite = new GenericObject();
        GenericObject t = composite.createChild("test");
        t.setInt("value", 1);
        GenericList l = t.createList("list");
        l.add(1); l.add(2); l.add(3);
        GenericObject cc = t.createChild("child");
        cc.setInt("childvalue", 1);
        
        return composite;
    }
    
    @Test
    public void testLoadFromString() {
        GenericObject test = EUGFileIO.loadFromString(PARSE_STRING, TEST_SETTINGS);
        assertNotNull(test);
        assertEquals("root", test.name);
        assertNotNull(test.children);
        assertNotNull(test.getChild("test"));
        assertNotNull(test.values);
        assertNotNull(test.lists);
    }
    
    @Test
    public void testLoadFromStringSameAsBuildingInCode() {
        GenericObject test = EUGFileIO.loadFromString(PARSE_STRING, TEST_SETTINGS);
        GenericObject test2 = buildObject();
        assertEquals(test, test2);
    }
    
    @Test
    public void testDifferentObjectsToStringSameIfSameStyle() {
        GenericObject test = EUGFileIO.loadFromString(PARSE_STRING, TEST_SETTINGS);
        GenericObject test2 = buildObject();
        
        assertEquals(test.toString(), test2.toString());
        assertNotEquals(test.toString(Style.AGCEEP), test2.toString(Style.EU4_SAVE_GAME));
    }
    
    @Test
    public void testHeaderCommentSavedDuringParse() {
        GenericObject test = EUGFileIO.loadFromString(PARSE_STRING, TEST_SETTINGS);
        GenericObject testHeader = EUGFileIO.loadFromString(PARSE_STRING_WITH_HEADER, TEST_SETTINGS);
        
        // plain one has one child which has no header comment
        assertEquals("", test.getChild("test").getHeadComment());
        // but commented one has a child which does
        assertNotEquals("", testHeader.getChild("test").getHeadComment());
        
        // plain one has no file header comment
        assertTrue(!test.getAllWritable().stream().anyMatch(obj -> obj instanceof HeaderComment));
        // but commented one does
        assertTrue(testHeader.getAllWritable().stream().anyMatch(obj -> obj instanceof HeaderComment));
    }
    
    private byte[] createZip() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                ZipEntry entry1 = new ZipEntry("data1");
                zos.putNextEntry(entry1);
                byte[] data1 = PARSE_STRING.getBytes();
                zos.write(data1, 0, data1.length);
                zos.closeEntry();
                
                ZipEntry entry2 = new ZipEntry("data2");
                zos.putNextEntry(entry2);
                byte[] data2 = PARSE_STRING_WITH_HEADER.getBytes();
                zos.write(data2, 0, data2.length);
                zos.closeEntry();
            }
            
            baos.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    @Test
    public void testLoadFromZip() {
        File file = new File("test/testLoadFromZip.eu4");
        if (!file.exists()) {
            byte[] zipFile = createZip();
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(zipFile);
                stream.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        assertTrue(file.exists());
        GenericObject object = EUGFileIO.load(file, TEST_SETTINGS);
        
        assertNotNull(object);
        assertTrue(object.children.size() == 2);
        assertNotNull(object.getChild("test"));
        assertNotNull(object.getLastChild("test"));
        assertTrue(object.getChild("test") != object.getLastChild("test"));
    }
}
