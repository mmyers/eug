/*
 * EU3SaveGame.java
 *
 * Created on March 22, 2007, 2:24 PM
 */

package eug.specific.eu3;

import eug.parser.EUGFileIO;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.shared.Style;
import eug.specific.clausewitz.ClausewitzSaveGame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Myers
 * @since EUGFile 1.03.00
 */
public class EU3SaveGame extends ClausewitzSaveGame {
    
    //private final FilenameResolver resolver;
    
    /**
     * Creates a new instance of EU3SaveGame
     */
    public EU3SaveGame(GenericObject root, String savePath, String mainPath, String modName) {
        super(root, savePath, mainPath, modName);
        
        //resolver = new FilenameResolver(mainPath, modName);
        
//        initDisplayNames();
//        initProvs();
//        initCountries();
    }
    
    public EU3SaveGame(GenericObject root, String savePath, FilenameResolver resolver) {
        super(root, savePath, null, null);
        //this.resolver = resolver;
    }
    
    public static EU3SaveGame loadSaveGame(String filename, String mainPath, String modName) {
        return new EU3SaveGame(EUGFileIO.load(filename), filename, mainPath, modName);
    }
    
    public static EU3SaveGame loadSaveGame(String filename, FilenameResolver resolver) {
        return new EU3SaveGame(EUGFileIO.load(filename), filename, resolver);
    }
    
//    /** Load English display names from all files in the localisation dir. */
    // not currently used (Sept 08)
//    private void initDisplayNames() {
//        displayNames = new HashMap<String, String>();
//        
//        for (File f : resolver.listFiles("localisation")) {
//            BufferedReader r = null;
//            try {
//                r = new BufferedReader(new FileReader(f), (int)f.length());
//                String line;
//                while ((line = r.readLine()) != null) {
//                    String[] array = line.split(";");
//                    displayNames.put(array[0], array[1]);
//                }
//            } catch (FileNotFoundException ex) {
//                ex.printStackTrace();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            } finally {
//                if (r != null) {
//                    try {
//                        r.close();
//                    } catch (IOException ex) {} // must never throw exception from 'finally' block
//                }
//            }
//        }
//    }
    
    private void initProvs() {
        provinces = new HashMap<Integer, GenericObject>(2048);
        for (int i = 1; /* loop until broken */; i++) {
            final GenericObject prov = root.getChild(Integer.toString(i));
            if (prov == null) {
                lastProvId = i-1;
                break;
            }
            provinces.put(i, prov);
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
    private Map<Integer, GenericObject> getProvinces() {
        if (provinces == null)
            initProvs();
        
        return provinces;
    }
    
    
    public EU3Country getEU3Country(final String tag) {
        return new EU3Country(getCountryMap().get(tag.substring(0,3).toUpperCase()), this);
    }
    
    public EU3Province getEU3Province(int id) {
        return new EU3Province(getProvinces().get(id + 1), this);
    }
    
    
    @Override
    public void saveChanges() {
        if (hasUnsavedChanges) {
            EUGFileIO.save(root, savePath, EUGFileIO.NO_COMMENT, true, Style.EU3_SAVE_GAME);
            hasUnsavedChanges = false;
        }
    }
    
    public List<GenericObject> getAlliances() {
        return getDiplomacy().getChildren("alliance");
    }
    
    public List<GenericObject> getTradeAgreements() {
        return getDiplomacy().getChildren("trade_agreement");
    }
    
    public List<GenericObject> getVassalizations() {
        return getDiplomacy().getChildren("vassal");
    }
    
    public List<GenericObject> getCasusBellis() {
        return getDiplomacy().getChildren("casus_belli");
    }
    
    public List<GenericObject> getRoyalMarriages() {
        return getDiplomacy().getChildren("royal_marriage");
    }
    
    public List<GenericObject> getUnions() {
        return getDiplomacy().getChildren("union");
    }

    void changeCountryTag(String oldTag, String newTag) {
        oldTag = oldTag.toUpperCase();
        newTag = newTag.toUpperCase();
        
        if (oldTag.equals(newTag))
            return;
        
        if (getCountryMap() != null) {
            countryMap.put(newTag, getCountryMap().get(oldTag));
            countryMap.remove(oldTag);
        }
        
        // Emperor
        if (root.getString("emperor").equals(oldTag))
            root.setString("emperor", newTag);
        
        // Papacy and cardinals
        final GenericObject papacy = root.getChild("papacy");
        if (papacy.getString("controller").equals(oldTag))
            papacy.setString("controller", newTag);
        
        for (GenericObject cardinal : papacy.getChildren("cardinal"))
            if (cardinal.getString("controller").equals(oldTag))
                cardinal.setString("controller", newTag);
        
        // Trade
        final GenericObject trade = root.getChild("trade");
        for (GenericObject cot : trade.getChildren("cot")) {
            for (GenericObject ctry : cot.children) {
                if (ctry.name.equals(oldTag)) {
                    ctry.name = newTag;
                    break;
                }
            }
        }
        
        // Provinces
        for (GenericObject prov : provinces.values()) {
            if (prov.getString("owner").equals(oldTag))
                prov.setString("owner", newTag);
            if (prov.getString("controller").equals(oldTag))
                prov.setString("controller", newTag);
            for (ObjectVariable var : prov.values) {
                if (var.varname.equals("core")) {
                    if (var.getValue().equals(oldTag)) {
                        var.setValue(newTag);
                        break;
                    }
                }
            }
            // ignore discoveries...
        }
        
        // Relations
        preloadCountries();
        for (GenericObject country : getCountryMap().values()) {
            for (GenericObject child : country.children) {
                if (child.name.equals(oldTag)) {
                    child.name = newTag;
                    break;
                }
            }
        }
        
        // Diplomacy
        for (GenericObject alliance : getDiplomacy().children) {
            if (alliance.getString("first").equals(oldTag) && !alliance.getString("second").equals(newTag))
                alliance.setString("first", newTag);
            else if (alliance.getString("second").equals(oldTag) && !alliance.getString("first").equals(newTag))
                alliance.setString("second", newTag);
        }
        
        // Wars
        for (GenericObject war : root.getChildren("active_war")) {
            if (war.getString("attacker").equals(oldTag) && !war.getString("defender").equals(newTag))
                war.setString("attacker", newTag);
            else if (war.getString("defender").equals(oldTag) && !war.getString("attacker").equals(newTag))
                war.setString("defender", newTag);
        }
        
        // Whew... I think that's it.
        // Now to make sure there aren't old entries in the country database.
        initCountries();
    }
    
    public static void main(String[] args) {
//        javax.swing.JFileChooser jfc = new javax.swing.JFileChooser();
//        int choice = jfc.showOpenDialog(null);
//
//        if (choice == jfc.APPROVE_OPTION) {
        /*EU3SaveGame scenario =*/ new EU3SaveGame(
                EUGFileIO.load("C:\\Documents and Settings\\Michael\\Desktop\\Game utils\\EU3 save games\\Russia.eu3"),
                "C:\\Documents and Settings\\Michael\\Desktop\\Game utils\\EU3 save games\\Russia.eu3",
                "C:\\Program Files\\Paradox Entertainment\\Eu3 - DEMO\\",
                "");
//            Scanner scanner = new Scanner(System.in);
//            while (true) {
//                System.out.print("Command> ");
//                String command = scanner.next();
//                if (command.equals("end"))
//                    break;
//                else if (command.equals("country")) {
//                    command = scanner.next();
//                    System.out.println(scenario.getCountry(command).go.toString());
//                } else if (command.equals("prov")) {
//                    command = scanner.next();
//                    System.out.println(scenario.getProvince(Integer.parseInt(command)).go.toString());
//                } else {
//                    System.err.println("Unknown command: " + command);
//                }
//            }
//        }
    }

}
