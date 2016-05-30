package eug.specific.ck2;

import eug.parser.EUGFileIO;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.shared.Style;
import eug.specific.clausewitz.ClausewitzSaveGame;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael
 */
public class CK2SaveGame extends ClausewitzSaveGame implements CK2DataSource {
    
    protected Map<String, GenericObject> titleMap;
    protected Map<String, GenericObject> characterMap;
    protected Map<String, String> characterCapitals;
    protected Map<String, String> capitalsOfCharacters;

    public CK2SaveGame(GenericObject root, String savePath, String mainPath, String modName) {
        super(root, savePath, mainPath, modName);
    }
    private CK2SaveGame(GenericObject root, String savePath, FilenameResolver resolver) {
        super(root, savePath, null, null);
    }
    
    public static CK2SaveGame loadSaveGame(String filename, String mainPath, String modName) {
        return new CK2SaveGame(EUGFileIO.load(filename), filename, mainPath, modName);
    }
    
    public static CK2SaveGame loadSaveGame(String filename, FilenameResolver resolver) {
        return new CK2SaveGame(EUGFileIO.load(filename), filename, resolver);
    }
    
    private void initTitles() {
        titleMap = new HashMap<>();
        root.children.stream().filter(
                (obj) -> (obj.name.startsWith("b_")
                        || obj.name.startsWith("c_")
                        || obj.name.startsWith("d_")
                        || obj.name.startsWith("k_")
                        || obj.name.startsWith("e_"))
                ).forEach((obj) -> titleMap.put(obj.name, obj));
    }
    
    private void initCharacters() {
        characterMap = new HashMap<>();
        characterCapitals = new HashMap<>();
        capitalsOfCharacters = new HashMap<>();
        GenericObject characters = root.getChild("characters");
        characters.children.stream().forEach((obj) -> {
                characterMap.put(obj.name, obj);
                String capital = obj.getChild("demesne").getString("capital");
                characterCapitals.put(obj.name, capital);
                capitalsOfCharacters.put(capital, obj.name);
            });
    }
    
    protected Map<String, GenericObject> getTitleMap() {
        if (titleMap == null)
            initTitles();
        return titleMap;
    }
    
    protected Map<String, GenericObject> getCharacterMap() {
        if (characterMap == null)
            initCharacters();
        return characterMap;
    }
    
    /**
     * @return a mapping of demesne capital titles (e.g. "b_reykjavik") to character IDs
     */
    protected Map<String, String> getCapitals() {
        if (capitalsOfCharacters == null)
            initCharacters();
        return capitalsOfCharacters;
    }

    
    @Override
    public GenericObject getTitle(String title) {
        GenericObject t = getTitleMap().get(title);
        if (t == null) {
            System.err.println("Cannot find title '" + title + "'");
        }
        return t;
    }

    @Override
    public GenericObject getTitleHistory(String title) {
        GenericObject t = getTitleMap().get(title);
        if (t == null) {
            System.err.println("Cannot find title '" + title + "'");
        }
        return t;
    }

    @Override
    public String getTitleAsStr(String title) {
        GenericObject t = getTitleMap().get(title);
        if (t == null) {
            System.err.println("Cannot find title '" + title + "'");
            return null;
        }
        return t.toString(Style.EU4_SAVE_GAME);
    }

    @Override
    public String getTitleHistoryAsStr(String title) {
        GenericObject t = getTitleMap().get(title);
        if (t == null) {
            System.err.println("Cannot find title '" + title + "'");
            return null;
        }
        return t.toString(Style.EU4_SAVE_GAME);
    }

    @Override
    public void reloadTitle(String title) {
        //
    }

    @Override
    public void reloadTitleHistory(String title) {
        
    }

    @Override
    public void reloadTitles() {
        initTitles();
    }

    @Override
    public void saveTitle(String title, String data) {
        final GenericObject oldTitle = getTitleMap().get(title);
        final GenericObject newTitle = EUGFileIO.loadFromString(data);
        if (oldTitle == null) {
            getCountryMap().put(title, newTitle);
        } else {
            oldTitle.clear();
            oldTitle.addAllChildren(newTitle.getChild(title));
        }
        hasUnsavedChanges = true;
    }

    @Override
    public GenericObject getProvinceHistory(int id) {
        return getProvince(id);
    }
}
