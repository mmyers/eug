/*
 * TestEditorDialog.java
 *
 * Created on June 29, 2007, 8:35 PM
 */

package editor;

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
        showing.put(id, this);
    }
    
    public TestEditorDialog(int id, String contents) {
        super(null, "Test", contents);
        this.id = id;
        showing.put(id, this);
    }

    protected void close() {
        showing.remove(id);
        super.close();
    }
    
    private static final java.util.Map<Integer, TestEditorDialog> showing =
            new HashMap<Integer, TestEditorDialog>();
    
    public static void showDialog(int id, String contents) {
        TestEditorDialog d = showing.get(id);
        if (d == null) {
            // No previous one, so create new.
            try {
                new TestEditorDialog(id, contents).setVisible(true);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        } else {
            d.setVisible(true);
        }
    }

    public static void main(String[] args) {
        showDialog(100, "test = {\n\tstring = \"value\"\n}");
    }
}
