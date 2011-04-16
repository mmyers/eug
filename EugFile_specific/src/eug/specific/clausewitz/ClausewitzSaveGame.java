

package eug.specific.clausewitz;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import eug.shared.Scenario;
import eug.shared.Style;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Myers
 * @since EUGFile 1.03.00
 */
public class ClausewitzSaveGame extends Scenario implements ClausewitzDataSource {
    
    protected final String savePath;
    
    protected Map<String, GenericObject> countryMap;
//    private List<GenericObject> provinces;
    
    protected int lastProvId;
    
    protected GenericObject diplomacy;
    
    protected boolean hasUnsavedChanges = false;
    
    /**
     * Creates a new instance of ClausewitzSaveGame
     */
    public ClausewitzSaveGame(GenericObject root, String savePath, String mainPath, String modName) {
        this.root = root;
        this.scenarioName = "";
        this.savePath = savePath;
    }
    
    public static ClausewitzSaveGame loadSaveGame(String filename, String mainPath, String modName) {
        return new ClausewitzSaveGame(EUGFileIO.load(filename), filename, mainPath, modName);
    }
    
    
    private void initProvs() {
        provinces = new ArrayList<GenericObject>(1500);
        for (int i = 1; /* loop until broken */; i++) {
            final GenericObject prov = root.getChild(Integer.toString(i));
            if (prov == null) {
                lastProvId = i-1;
                break;
            }
            provinces.add(prov);
        }
    }
    
    private static final java.util.regex.Pattern tagPattern =
            java.util.regex.Pattern.compile("[A-Z][A-Z0-9]{2}");
    private void initCountries() {
        countryMap = new HashMap<String, GenericObject>(100);
        for (GenericObject obj : root.children) {
            if (tagPattern.matcher(obj.name).matches()) {
                countryMap.put(obj.name, obj);
            }
        }
    }
    
    // Lazy accessor
    private Map<String, GenericObject> getCountryMap() {
        if (countryMap == null)
            initCountries();
        
        return countryMap;
    }
    
    // Lazy accessor
    private List<GenericObject> getProvinces() {
        if (provinces == null)
            initProvs();
        
        return provinces;
    }
    
    
    public List<String> getCountryTags() {
        return new ArrayList<String>(getCountryMap().keySet());
    }
    
    public List<GenericObject> getCountries() {
        return new ArrayList<GenericObject>(getCountryMap().values());
    }
    
    
    public GenericObject getCountry(String tag) {
        return getCountryMap().get(tag.substring(0,3).toUpperCase());
    }
    
    public GenericObject getCountryHistory(String tag) {
        return getCountryMap().get(tag.substring(0,3).toUpperCase()).getChild("history");
    }
    
    public GenericObject getProvince(int id) {
        if (id > lastProvId)
            return null;
        return getProvinces().get(id-1);
    }
    
    public GenericObject getProvinceHistory(int id) {
        return getProvinces().get(id-1).getChild("history");
    }
    
    public List<GenericObject> getWars() {
        final List<GenericObject> ret = root.getChildren("active_war");
        ret.addAll(root.getChildren("previous_war"));
        return ret;
    }
    
    public List<GenericObject> getWars(String date) {
        final List<GenericObject> ret = new ArrayList<GenericObject>();
        
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
    
    
    public String getCountryAsStr(String tag) {
        return getCountryMap().get(tag.substring(0,3).toUpperCase()).toString(Style.EU3_SAVE_GAME);
    }
    
    public String getCountryHistoryAsStr(String tag) {
        return getCountryMap().get(tag.substring(0,3).toUpperCase()).getChild("history").toString(Style.EU3_SAVE_GAME);
    }
    
    public String getProvinceAsStr(int id) {
        return getProvinces().get(id-1).toString(Style.EU3_SAVE_GAME);
    }
    
    public String getProvinceHistoryAsStr(int id) {
        return getProvinces().get(id-1).getChild("history").toString(Style.EU3_SAVE_GAME);
    }
    
    public void saveCountry(String tag, String cname, final String data) {
        final GenericObject country = getCountryMap().get(tag);
        country.clear();
        final GenericObject newCountry = EUGFileIO.loadFromString(data);
        country.addAllChildren(newCountry.getChild(tag));
        hasUnsavedChanges = true;
    }
    
    public void saveProvince(int id, String pname, final String data) {
        final GenericObject province = getProvinces().get(id-1);
        province.clear();
        final GenericObject newProvince = EUGFileIO.loadFromString(data);
        province.addAllChildren(newProvince.getChild(Integer.toString(id)));
        hasUnsavedChanges = true;
    }

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
    
    public void saveChanges() {
        if (hasUnsavedChanges) {
            EUGFileIO.save(root, savePath, EUGFileIO.NO_COMMENT, true, Style.EU3_SAVE_GAME);
            hasUnsavedChanges = false;
        }
    }
    
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    
    public void reloadCountry(String tag) {
        // This method should never be needed, since any modifications would be
        // working directly on the GenericObject.
    }
    
    public void reloadCountryHistory(String tag) {
        // This method should never be needed, since any modifications would be
        // working directly on the GenericObject.
    }

    public void reloadCountries() {
        initCountries();
    }
    
    public void reloadProvince(int id) {
        // This method should never be needed, since any modifications would be
        // working directly on the GenericObject.
    }
    
    public void reloadProvinceHistory(int id) {
        // This method should never be needed, since any modifications would be
        // working directly on the GenericObject.
    }

    public void reloadProvinces() {
        initProvs();
    }
    
    public void preloadProvinces(int last) {
        // ignore parameter, since it's so quick anyway
        preloadProvinces();
    }
    
    public void preloadProvinces() {
        if (provinces == null)
            initProvs();
    }
    
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
