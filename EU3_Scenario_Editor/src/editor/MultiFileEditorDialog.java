
package editor;

import eug.parser.EUGFileIO;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.specific.ck2.CK2DataSource;
import eug.specific.clausewitz.ClausewitzDataSource;
import eug.specific.clausewitz.ClausewitzHistory;
import java.awt.Frame;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

/**
 * Dialog to edit multiple provinces or countries at the same time. To allow
 * editing files that have different contents, this editor does not attempt to
 * show any previous file contents at all. Rather, the user is given a blank
 * slate and can make any edits they choose (inside a date block if desired),
 * which then overwrite the corresponding portions of the selected files.
 * <p>
 * For example, suppose that there are two provinces with the following contents
 * in their history files:
 * <p>
 * <b>1 - Stockholm.txt</b>
 * <pre>
 * add_core = SWE
 * owner = SWE
 * controller = SWE
 * culture = swedish
 * base_tax = 8
 * 
 * 1450.1.1 = { base_tax = 10 }
 * </pre>
 * <p>
 * <b>2 - Paris.txt</b>
 * <pre>
 * add_core = FRA
 * owner = FRA
 * controller = FRA
 * culture = cosmopolitan_french
 * base_tax = 15
 * 
 * 1420.1.1 = { controller = ENG }
 * </pre>
 * 
 * If the user edited these two files with this editor and wrote the following:
 * <pre>
 * hre = no
 * 1420.1.1 = { controller = REB }
 * 1550.1.1 = { religion = protestant }
 * </pre>
 * 
 * Then the files would be transformed into:
 * <p>
 * <b>1 - Stockholm.txt</b>
 * <pre>
 * add_core = SWE
 * owner = SWE
 * controller = SWE
 * culture = swedish
 * base_tax = 8
 * hre = no
 * 
 * 1420.1.1 = { controller = REB }
 * 1450.1.1 = { base_tax = 10 }
 * 1550.1.1 = { religion = protestant }
 * </pre>
 * <p>
 * <b>2 - Paris.txt</b>
 * <pre>
 * add_core = FRA
 * owner = FRA
 * controller = FRA
 * culture = cosmopolitan_french
 * base_tax = 15
 * hre = no
 * 
 * 1420.1.1 = { controller = REB }
 * 1550.1.1 = { religion = protestant }
 * </pre>
 * 
 * The file formatting may or may not be modified in the process, and any 
 * comments in the contents of this editor may or may not be carried over into
 * the target files. This documentation may be updated at a later date to specify.
 * 
 * @author Michael
 */
public class MultiFileEditorDialog extends EditorDialog {
    
    private final List<ProvinceData.Province> provinces;
    private final List<String> countryTags;
    private final List<String> countryNames;

    public MultiFileEditorDialog(Frame parent, List<ProvinceData.Province> provinces, FilenameResolver resolver, ProvinceData data) {
        super(parent, String.join(", ", provinces.stream().map(p -> p.getName()).collect(Collectors.toList())), resolver, data);
        
        // don't need to do much else here, since we don't open files till we finish this dialog
        this.provinces = provinces;
        this.countryTags = null;
        this.countryNames = null;
        
        setOriginalContents("# Anything typed here will be added to all selected province history files");
    }

    public MultiFileEditorDialog(Frame parent, List<String> countryTags, List<String> countryNames, String contents, FilenameResolver resolver, ProvinceData data) {
        super(parent, String.join(", ", countryTags), contents, resolver, data);
        
        // don't need to do much else here, since we don't open files till we finish this dialog
        this.provinces = null;
        this.countryTags = countryTags;
        this.countryNames = countryNames;
        
        setOriginalContents("# Anything typed here will be added to all selected country history files");
    }
    
    
    @Override
    protected void close() {
        if (textHasChanged()) {
            int check = JOptionPane.showConfirmDialog(this,
                    "Save changes to these files?", "Confirmation",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (check == JOptionPane.YES_OPTION)
                save();
            else if (check == JOptionPane.CANCEL_OPTION)
                return;
            // else continue on to disposing
        }
        
        super.close();
        getParent().repaint();
    }

    private void save() {
        dataSource.setBackups(saveBackups);
        if (provinces != null) {
            saveProvinces();
        } else {
            if (dataSource instanceof CK2DataSource) {
                saveTitles();
            } else {
                saveCountries();
            }
        }
    }

    private void saveProvinces() {
        GenericObject created = EUGFileIO.loadFromString(getText());
        
        for (ProvinceData.Province p : provinces) {
            GenericObject oldProvince = dataSource.getProvince(p.getId());
            
            ClausewitzHistory.mergeHistObjects(oldProvince, created);
            
            dataSource.saveProvince(p.getId(), p.getName(), oldProvince.toString());
            dataSource.reloadProvince(p.getId());
        }
    }

    private void saveTitles() {
        GenericObject created = EUGFileIO.loadFromString(getText());
        
        for (String titleStr : countryTags) {
            GenericObject oldTitle = ((CK2DataSource)dataSource).getTitle(titleStr);
            
            ClausewitzHistory.mergeHistObjects(oldTitle, created);
            
            ((CK2DataSource)dataSource).saveTitle(titleStr, oldTitle.toString());
            ((CK2DataSource)dataSource).reloadTitle(titleStr);
        }
    }

    private void saveCountries() {
        GenericObject created = EUGFileIO.loadFromString(getText());
        
        for (String tagStr : countryTags) {
            GenericObject oldCountry = dataSource.getCountry(tagStr);
            
            ClausewitzHistory.mergeHistObjects(oldCountry, created);
            
            dataSource.saveCountry(tagStr, tagStr, oldCountry.toString()); // TODO: need to get the country name
            dataSource.reloadCountry(tagStr);
        }
    }
    
    
    private static ClausewitzDataSource dataSource = null;
    
    public static void setDataSource(ClausewitzDataSource newSource) {
        dataSource = newSource;
    }
}
