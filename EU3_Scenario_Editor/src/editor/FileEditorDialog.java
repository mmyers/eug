/*
 * FileEditorDialog.java
 *
 * Created on January 27, 2007, 6:16 PM
 */

package editor;

import eug.shared.FilenameResolver;
import eug.specific.ck2.CK2DataSource;
import eug.specific.clausewitz.ClausewitzDataSource;
import eug.specific.victoria2.Vic2Scenario;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 * Dialog to edit province or country history files with syntax highlighting.
 * <p>
 * All interaction with this class is handled through the static methods
 * {@link #showDialog(Frame, ProvinceData.Province, FilenameResolver, ProvinceData)} and
 * {@link #showDialog(Frame, String, String, FilenameResolver, ProvinceData)}.
 * @author Michael Myers
 */
public class FileEditorDialog extends EditorDialog {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(FileEditorDialog.class.getName());
    
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
    private final String name;
    
    /**
     * True if editing a province; false if editing a country.
     */
    private final boolean isProvince;
    
    
    /**
     * Creates new form FileEditorDialog.
     *
     * @param parent    the <code>Frame</code> that this dialog is a child of.
     * @param p         the <code>Province</code> that this dialog will display
     *                  the history file for.
     */
    private FileEditorDialog(java.awt.Frame parent, ProvinceData.Province p, FilenameResolver resolver, ProvinceData data) {
        super(parent, p.getName(), resolver, data);
        
        this.isProvince = true;
        this.id = p.getId();
        this.name = p.getName();
        readFile(p.getId(), p.getName());
        if (dataSource instanceof Vic2Scenario) {
            String file = resolver.getVic2ProvinceHistoryFile(p.getId());
            if (file != null)
                setTitle(getTitle() + " (" + file + ")");
        } else {
            String[] files = resolver.getProvinceHistoryFiles(p.getId());
            if (files.length > 0 && files[0] != null)
                setTitle(getTitle() + " (" + files[0] + ")");
        }
        register(this); // Register last in case exceptions occur
    }
    
    private FileEditorDialog(java.awt.Frame parent, String countryTag, String countryName, FilenameResolver resolver, ProvinceData data) {
        super(parent, countryName, resolver, data);
        
        this.isProvince = false;
        this.id = countryTag.hashCode();
        this.tag = countryTag;
        this.name = countryName;
        readFile(countryTag, countryName);
        String[] files = resolver.getCountryHistoryFiles(countryTag); // TODO: Doesn't work with Vic 2
        if (files.length > 0 && files[0] != null)
            setTitle(getTitle() + " (" + files[0] + ")");
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
        String data = null;
        if (dataSource instanceof CK2DataSource)
            data = ((CK2DataSource)dataSource).getTitleAsStr(tag);
        else
            data = dataSource.getCountryAsStr(tag);
        
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
        getParent().repaint();
    }
    
    private void save() {
        dataSource.setBackups(saveBackups);
        if (isProvince) {
            dataSource.saveProvince(id, name, getText());
            dataSource.reloadProvince(id);
        } else {
            if (dataSource instanceof CK2DataSource) {
                ((CK2DataSource) dataSource).saveTitle(tag, getText());
                ((CK2DataSource) dataSource).reloadTitle(tag);
            } else {
                dataSource.saveCountry(tag, name, getText());
                dataSource.reloadCountry(tag);
            }
        }
    }
    
    // For some reason, overriding hashCode() consistently generates three
    // stack traces when closing the dialog. I have yet to figure out why, but
    // at least I finally figured out what was causing it....
    
//    /** Compares id numbers for equality. */
//    @Override
//    public boolean equals(Object obj) {
//        return (obj instanceof FileEditorDialog) &&
//                ((FileEditorDialog) obj).id == id;
//    }
//    
//    @Override
//    public int hashCode() {
//        return id;
//    }

    
    // -------------------------------------------------------------------
    // Static Members
    // -------------------------------------------------------------------
    
    private static final java.util.Map<Integer, FileEditorDialog> showing = new HashMap<>();
    
    private static void register(FileEditorDialog d) {
        showing.put(d.id, d);
    }
    private static void unRegister(FileEditorDialog d) {
        showing.remove(d.id);
    }
    
    private static ClausewitzDataSource dataSource = null;
    
    public static void setDataSource(ClausewitzDataSource newSource) {
        dataSource = newSource;
    }
    
    /**
     * Shows a province history dialog for the given province.
     * @param parent the <code>Frame</code> that the dialog will be a child of.
     * @param p      the <code>Province</code> that the dialog will show the
     *               history file for.
     */
    public static void showDialog(java.awt.Frame parent, ProvinceData.Province p, FilenameResolver resolver, ProvinceData data) {
        final FileEditorDialog d = showing.get(p.getId());
        if (d == null) {
            // No previous one, so create new.
            try {
                new FileEditorDialog(parent, p, resolver, data).setVisible(true);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error with province " + p + ": " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                log.log(Level.WARNING, "Error with province " + p, ex);
            }
        } else {
            d.setVisible(true);
        }
    }
    
    /**
     * Shows a country history dialog for the given country.
     * @param parent      the <code>Frame</code> that the dialog will be a
     *                    child of.
     * @param countryTag  the country's tag.
     * @param countryName the country's name. Used in the dialog's title, and
     *                    if no previous country history file existed.
     */
    public static void showDialog(java.awt.Frame parent, String countryTag, String countryName, FilenameResolver resolver, ProvinceData data) {
        final FileEditorDialog d = showing.get(countryTag.hashCode());
        if (d == null) {
            // No previous one, so create new.
            try {
                new FileEditorDialog(parent, countryTag, countryName, resolver, data).setVisible(true);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error with country " + countryTag + ": " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                log.log(Level.WARNING, "Error with country " + countryTag, ex);
            }
        } else {
            d.setVisible(true);
        }
    }
    
    /**
     * Closes all showing dialogs naturally (the user gets a chance to save).
     */
    public static void disposeAll() {
        // Clone the list to avoid ConcurrentModificationException
        @SuppressWarnings("unchecked")
        final java.util.Map<Integer, FileEditorDialog> showingClone =
                (java.util.Map<Integer, FileEditorDialog>) ((HashMap<Integer, FileEditorDialog>)showing).clone();
        
        for (FileEditorDialog d : showingClone.values()) {
            d.close();
            if (d.isShowing())
                d.dispose();
        }
    }
    
}
