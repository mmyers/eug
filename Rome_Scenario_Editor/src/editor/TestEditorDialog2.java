/*
 * FileEditorDialog.java
 *
 * Created on January 27, 2007, 6:16 PM
 */

package editor;

import eug.specific.eu3.EU3DataSource;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 * Dialog to edit province or country history files with syntax highlighting.
 * <p>
 * All interaction with this class is handled through the static methods
 * {@link #showDialog(Frame, ProvinceData.Province)} and
 * {@link #showDialog(Frame, String, String)}.
 * @author Michael Myers
 */
public class TestEditorDialog2 extends EditorDialog {
    
    
    /**
     * The province id number or the hashcode of the country tag. Used to
     * prevent two dialogs from editing the same file, which could cause
     * problems.
     */
    private int id;
    
    /**
     * The country tag, if a country is being edited; otherwise <code>null</code>.
     */
    private String tag = null;
    
    /**
     * The name of the province or country.
     */
    private String name;
    
    
    /**
     * Creates new form FileEditorDialog.
     *
     * @param parent    the <code>Frame</code> that this dialog is a child of.
     * @param p         the <code>Province</code> that this dialog will display
     *                  the history file for.
     */
    private TestEditorDialog2(final java.awt.Frame parent, final ProvinceData.Province p) {
        super(parent, p.getName());
        
        this.id = p.getId();
        this.name = p.getName();
        readFile(p.getId(), p.getName());
        register(this); // Register last in case exceptions occur
    }
    
    private TestEditorDialog2(final java.awt.Frame parent, String countryTag, String countryName) {
        super(parent, countryName);
        
        this.id = countryTag.hashCode();
        this.tag = countryTag;
        this.name = countryName;
        readFile(countryTag, countryName);
        register(this); // Register last in case exceptions occur
    }
    
    /**
     * Reads in the province history file.
     * @param pid   the province's ID number.
     * @param pname the province's name.
     */
    private void readFile(int pid, String pname) {
        final String data = dataSource.getProvinceAsStr(pid);
        
        if (data == null) {
            setOriginalContents("# No previous file for " + pname + "\n");
        } else {
            setOriginalContents(data);
        }
    }
    
    /**
     * Reads in the country history file.
     * @param tag   the country's tag.
     * @param cname the country's name.
     */
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
        if (textHasChanged()) {
            int check = JOptionPane.showConfirmDialog(this,
                    "File has changed. Save changes?", "Confirmation",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (check == JOptionPane.YES_OPTION)
                save();
            else if (check == JOptionPane.CANCEL_OPTION)
                return;
            // else continue on to disposing
        }
        
        unRegister(this);
        super.close();
    }
    
    private static final int MIN_TAG_HASH = "AAA".hashCode();
    
    private void save() {
        if (id < MIN_TAG_HASH) {
            // Province
            dataSource.saveProvince(id, name, getText());
            dataSource.reloadProvince(id);
        } else {
            dataSource.saveCountry(tag, name, getText());
            dataSource.reloadCountry(tag);
        }
    }
    
    
//    /** Compares id numbers for equality. */
//    @Override
//    public boolean equals(Object obj) {
//        System.err.println("equals() called");
//        Thread.dumpStack();
//        return (obj instanceof TestEditorDialog2) &&
//                ((TestEditorDialog2) obj).id == id;
//    }

//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 73 * hash + this.id;
//        return hash;
//    }
    
    
    
    // -------------------------------------------------------------------
    // Static Members
    // -------------------------------------------------------------------
    
    private static final java.util.Map<Integer, TestEditorDialog2> showing =
            new HashMap<Integer, TestEditorDialog2>();
    
    private static void register(TestEditorDialog2 d) {
        showing.put(d.id, d);
    }
    private static void unRegister(TestEditorDialog2 d) {
        showing.remove(d.id);
    }
    
    private static EU3DataSource dataSource = null;
    
    public static void setDataSource(EU3DataSource newSource) {
        dataSource = newSource;
    }
    
    
    /**
     * Shows a country history dialog for the given country.
     * @param parent      the <code>Frame</code> that the dialog will be a
     *                    child of.
     * @param countryTag  the country's tag.
     * @param countryName the country's name. Used in the dialog's title, and
     *                    if no previous country history file existed.
     */
    public static void showDialog(java.awt.Frame parent, String countryTag, String countryName) {
        final TestEditorDialog2 d = showing.get(countryTag.hashCode());
        if (d == null) {
            // No previous one, so create new.
            try {
                new TestEditorDialog2(parent, countryTag, countryName).setVisible(true);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error with country " + countryTag + ": " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            d.setVisible(true);
        }
    }
    
    /** For testing only. */
    public static void main(String[] args) {
//        showDialog(null, "FRA", "France");
//        showDialog(null, "RUS", "Russia");
//        showDialog(null, "FRA", "France");
        setDataSource(new eug.specific.eu3.EU3Scenario(Main.filenameResolver));
        showDialog(null, "ROM", "Rome");
        showDialog(null, "ROM", "Rome");
    }
    
}
