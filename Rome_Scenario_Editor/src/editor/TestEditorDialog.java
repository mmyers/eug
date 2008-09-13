/*
 * TestEditorDialog.java
 *
 * Created on June 29, 2007, 8:35 PM
 */

package editor;

import eug.specific.eu3.EU3DataSource;
import java.util.HashMap;

/**
 *
 * @author Michael Myers
 */
public class TestEditorDialog extends EditorDialog {
    
    private int id;
    
    /** Creates a new instance of TestEditorDialog */
    public TestEditorDialog(int id) {
        super(null, "Test");
        this.id = id;
        register(this);
    }
    
    public TestEditorDialog(String countryTag, String countryName) {
        super(null, countryName);
        this.id = countryTag.hashCode();
//        this.tag = countryTag;
//        this.name = countryName;
        readFile(countryTag, countryName);
        register(this); // Register last in case exceptions occur
    }
    
    private void readFile(String tag, String cname) {
        final String data = dataSource.getCountryAsStr(tag);
        
        if (data == null) {
            setOriginalContents("# No previous file for " + cname + "\n");
        } else {
            setOriginalContents(data);
        }
    }

    @Override
    protected void close() {
        unRegister(this);
        super.close();
    }
    
    private static final java.util.Map<Integer, TestEditorDialog> showing =
            new HashMap<Integer, TestEditorDialog>();
    
    private static void register(TestEditorDialog d) {
        showing.put(d.id, d);
    }
    private static void unRegister(TestEditorDialog d) {
        showing.remove(d.id);
    }
    
    private static EU3DataSource dataSource = null;
    
    public static void setDataSource(EU3DataSource newSource) {
        dataSource = newSource;
    }
    
    public static void showDialog(String tag, String name) {
        TestEditorDialog d = showing.get(tag.hashCode());
        if (d == null) {
            // No previous one, so create new.
            try {
                TestEditorDialog ted = new TestEditorDialog(tag, name);
                ted.setVisible(true);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        } else {
            d.setVisible(true);
        }
    }
    

    public static void main(String[] args) {
        setDataSource(new eug.specific.eu3.EU3Scenario(Main.filenameResolver));
        showDialog("ROM", "Rome");
//        showDialog(100, "test = {\n\tstring = \"value\"\n}");
//        showDialog(100, "gobbledygook");
    }
}
