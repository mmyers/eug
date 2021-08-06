

package eug.specific.clausewitz;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import eug.shared.Scenario;
import eug.shared.Style;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Michael Myers
 * @since EUGFile 1.03.00
 */
public class ClausewitzSaveGame extends Scenario implements ClausewitzDataSource {
    
    protected final String savePath;
    
    protected Map<String, GenericObject> countryMap;
    
    protected int lastProvId;
    
    protected GenericObject diplomacy;
    
    protected boolean hasUnsavedChanges = false;
    
    protected boolean saveBackups = true;
    
    /**
     * Creates a new instance of ClausewitzSaveGame
     */
    public ClausewitzSaveGame(GenericObject root, String savePath, String mainPath, String modName) {
        if (root == null)
            throw new NullPointerException("Failed to load game from " + savePath);
        
        this.root = root;
        this.scenarioName = "";
        this.savePath = savePath;
        this.saveStyle = Style.EU3_SAVE_GAME;
    }
    
    public static ClausewitzSaveGame loadSaveGame(String filename, String mainPath, String modName) {
        return new ClausewitzSaveGame(EUGFileIO.load(filename), filename, mainPath, modName);
    }

    @Override
    public void setBackups(boolean useBackup) {
        this.saveBackups = useBackup;
    }
    
    protected void initProvs() {
        provinces = new HashMap<>(1500);
        for (int i = 1; /* loop until broken */; i++) {
            final GenericObject prov = root.getChild(Integer.toString(i));
            if (prov == null) {
                lastProvId = i-1;
                break;
            }
            provinces.put(i, prov);
        }
        if (lastProvId == 0) { // EU4
            GenericObject provs = root.getChild("provinces");
            if (provs == null)
                return;

            for (int i = 1; ; i++) {
                GenericObject prov = provs.getChild("-" + i);
                if (prov == null) {
                    lastProvId = i-1;
                    break;
                }
                provinces.put(i, prov);
            }
            
            if (lastProvId == 0) {
                for (int i = 1; ; i++) {
                    GenericObject prov = provs.getChild(Integer.toString(i));
                    if (prov == null) {
                        lastProvId = i-1;
                        break;
                    }
                    provinces.put(i, prov);
                }
            }
        }
    }
    
    protected static final Pattern tagPattern = Pattern.compile("[A-Z][A-Z0-9]{2}");
    protected void initCountries() {
        countryMap = new HashMap<>(100);
        GenericObject countryRoot = root.getChild("countries");
        if (countryRoot == null)
            countryRoot = root;
        for (GenericObject obj : countryRoot.children) {
            if (tagPattern.matcher(obj.name).matches()) {
                countryMap.put(obj.name, obj);
            }
        }
    }
    
    // Lazy accessor
    protected Map<String, GenericObject> getCountryMap() {
        if (countryMap == null)
            initCountries();
        
        return countryMap;
    }
    
    // Lazy accessor
    protected Map<Integer, GenericObject> getProvinces() {
        if (provinces == null)
            initProvs();
        
        return provinces;
    }
    
    
    public List<String> getCountryTags() {
        return new ArrayList<>(getCountryMap().keySet());
    }
    
    public List<GenericObject> getCountries() {
        return new ArrayList<>(getCountryMap().values());
    }
    
    
    @Override
    public GenericObject getCountry(String tag) {
        if (tag.length() < 3)
            return null;
        return getCountryMap().get(tag.substring(0,3).toUpperCase());
    }
    
    @Override
    public GenericObject getCountryHistory(String tag) {
        GenericObject ctry = getCountry(tag);
        if (ctry == null)
            return null;
        return ctry.getChild("history");
    }
    
    @Override
    public GenericObject getProvince(int id) {
        if (id > lastProvId)
            return tryLoadProvince(id);
        return getProvinces().get(id);
    }
    
    @Override
    public GenericObject getProvinceHistory(int id) {
        if (id > lastProvId) {
            GenericObject obj = tryLoadProvince(id);
            if (obj != null)
                return obj.getChild("history");
        }
        
        GenericObject obj = getProvinces().get(id);
        if (obj == null)
            return new GenericObject();
        return obj.getChild("history");
    }

    private GenericObject tryLoadProvince(int id) {
        GenericObject prov = root.getChild(Integer.toString(id));
        if (prov != null) {
            provinces.put(id, prov);
            if (id > lastProvId)
                lastProvId = id;
        } else {
            GenericObject provinceRoot = root.getChild("provinces");
            if (provinceRoot != null) {
                prov = provinceRoot.getChild(Integer.toString(id));
                if (prov != null) {
                    provinces.put(id, prov);
                    if (id > lastProvId)
                        lastProvId = id;
                } else {
                    prov = provinceRoot.getChild("-" + id);
                    if (prov != null) {
                        provinces.put(id, prov);
                        if (id > lastProvId)
                            lastProvId = id;
                    }
                }
            }
        }
        return prov;
    }
    
    @Override
    public List<GenericObject> getWars() {
        final List<GenericObject> ret = root.getChildren("active_war");
        ret.addAll(root.getChildren("previous_war"));
        return ret;
    }
    
    @Override
    public List<GenericObject> getWars(String date) {
        final List<GenericObject> ret = new ArrayList<>();
        
        // Only check wars if the date is in the past
        if (ClausewitzHistory.DATE_COMPARATOR.isBefore(date, getDate())) {
            for (GenericObject war : root.getChildren("active_war")) {
                final List<String> participants =
                        ClausewitzHistory.getHistStrings(war, date, "add_attacker", "rem_attacker");
                
                if (!participants.isEmpty())
                    ret.add(war);
            }
        
            for (GenericObject war : root.getChildren("previous_war")) {
                final List<String> participants =
                        ClausewitzHistory.getHistStrings(war, date, "add_attacker", "rem_attacker");
                
                if (!participants.isEmpty())
                    ret.add(war);
            }
        }
        
        return ret;
    }

    @Override
    public void removeWar(String name) {
        GenericObject toRemove = null;
        for (GenericObject war : getWars()) {
            if (war.getString("name").equals(name)) {
                toRemove = war;
                break;
            }
        }
        if (toRemove != null) {
            toRemove.getParent().removeChild(toRemove);
            hasUnsavedChanges = true;
        }
    }
    
    
    @Override
    public String getCountryAsStr(String tag) {
        GenericObject obj = getCountryMap().get(tag.substring(0,3).toUpperCase());
        if (obj == null)
            return null;
        return obj.toString(saveStyle);
    }
    
    @Override
    public String getCountryHistoryAsStr(String tag) {
        GenericObject obj = getCountryMap().get(tag.substring(0,3).toUpperCase());
        if (obj == null)
            return null;
        obj = obj.getChild("history");
        if (obj == null)
            return null;
        return obj.toString(saveStyle);
    }
    
    @Override
    public String getProvinceAsStr(int id) {
        GenericObject obj = getProvinces().get(id);
        if (obj == null)
            return null;
        return obj.toString(saveStyle);
    }
    
    @Override
    public String getProvinceHistoryAsStr(int id) {
        GenericObject obj = getProvinces().get(id);
        if (obj == null)
            return null;
        obj = obj.getChild("history");
        if (obj == null)
            return null;
        return obj.toString(saveStyle);
    }
    
    @Override
    public void saveCountry(String tag, String cname, final String data) {
        final GenericObject country = getCountryMap().get(tag);
        final GenericObject newCountry = EUGFileIO.loadFromString(data);
        if (country == null) {
            getCountryMap().put(tag, newCountry);
        } else {
            country.clear();
            country.addAllChildren(newCountry.getChild(tag));
        }
        hasUnsavedChanges = true;
    }
    
    @Override
    public void saveProvince(int id, String pname, final String data) {
        final GenericObject province = getProvinces().get(id);
        final GenericObject newProvince = EUGFileIO.loadFromString(data);
        if (province == null) {
            getProvinces().put(id, newProvince);
        } else {
            province.clear();
            province.addAllChildren(newProvince.getChild(0));
        }
        hasUnsavedChanges = true;
    }

    @Override
    public void saveWar(String name, String data) {
        final GenericObject newWar = EUGFileIO.loadFromString(data);
        for (GenericObject war : getWars()) {
            if (war.getString("name").equals(name)) {
                war.clear();
                war.addAllChildren(newWar.getChild(0));
                hasUnsavedChanges = true;
                return;
            }
        }
    }
    
    @Override
    public void saveChanges() {
        if (hasUnsavedChanges) {
            if (saveBackups) {
                final File file = new File(savePath);
                if (!file.renameTo(new File(getBackupFilename(savePath))))
                    System.err.println("Backup of " + file.getName() + " failed");
            }
                
            EUGFileIO.save(root, savePath, EUGFileIO.NO_COMMENT, true, saveStyle);
            hasUnsavedChanges = false;
        }
    }
    
    protected static String getBackupFilename(String filename) {
        final File f = new File(filename);
        return f.getParent() + File.separatorChar + "~" + f.getName();
    }
    
    @Override
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    
    @Override
    public void reloadCountry(String tag) {
        // This method should never be needed, since any modifications would be
        // working directly on the GenericObject.
    }
    
    @Override
    public void reloadCountryHistory(String tag) {
        // This method should never be needed, since any modifications would be
        // working directly on the GenericObject.
    }

    @Override
    public void reloadCountries() {
        initCountries();
    }
    
    @Override
    public void reloadProvince(int id) {
        // This method should never be needed, since any modifications would be
        // working directly on the GenericObject.
    }
    
    @Override
    public void reloadProvinceHistory(int id) {
        // This method should never be needed, since any modifications would be
        // working directly on the GenericObject.
    }

    @Override
    public void reloadProvinces() {
        initProvs();
    }
    
    @Override
    public void preloadProvinces(int last) {
        // ignore parameter, since it's so quick anyway
        preloadProvinces();
    }
    
    public void preloadProvinces() {
        if (provinces == null)
            initProvs();
    }
    
    @Override
    public void preloadCountries() {
        if (countries == null)
            initCountries();
    }
    
    
    @Override
    public int numCountries() {
        return getCountryMap().size();
    }
    
    
    public String getDate() {
        return root.getString("date");
    }
    
    public int getLastProvID() {
        return lastProvId;
    }
    
    // Lazy accessor
    public GenericObject getDiplomacy() {
        if (diplomacy == null)
            diplomacy = root.getChild("diplomacy");
        return diplomacy;
    }

}
