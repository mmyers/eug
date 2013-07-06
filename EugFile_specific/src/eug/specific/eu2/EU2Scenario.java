/*
 * EU2Scenario.java
 *
 * Created on June 23, 2006, 6:57 PM
 */

package eug.specific.eu2;

import eug.parser.EUGFileIO;

import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.Scenario;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Michael Myers
 */
public class EU2Scenario extends Scenario {
    
//    private String scenarioFilePath;
    static String eu2FolderPath = "c:/program files/strategy first/europa universalis 2/";
    static String modPath = "AGCEEP/";
//    private String scenarioName;
    
    public String[] provName;
    public String[] provReligion;
    public String[] provCulture;
    public int[] provIncome;
    private int[] provTerrain;
    public int[] provMine;
    public String[] provGoods;
    
    public String[] goods;
    
    /** Caches requested countries for better performance. */
    private Map<String, EU2Country> countryCache;
    
//    private int lastID;
    
    private static final int NUM_PROVINCES = 2020;
    
    public EU2Scenario(GenericObject r, String filePath) {
        this(r, filePath, true);
    }
    
    /** Creates a new instance of EU2Scenario */
    public EU2Scenario(GenericObject r, String filePath, boolean includeEvents) {
        
//        scenarioFilePath = filePath;
        
        //mac game setup:
        if (new File(eu2FolderPath+"Europa Universalis 2 Data Files/config/text.csv").exists()) {
            eu2FolderPath+="Europa Universalis 2 Data Files";
            
            //PC game setup:
        } else if (new File(eu2FolderPath+"config/text.csv").exists()) {
            
        } else {
            JOptionPane.showMessageDialog(null, "Could not locate the EU2 game files.");
            System.exit(1);
        }
        
        root=r;
        scenarioName = r.getChild("header").getString("name");
        
//        lastID = 0;
        
        initDisplayNames();
        initProvs();
        
        // Note: initGoods() MUST come after initDisplayNames() !!
        initGoods();
        
        if (includeEvents)
            loadEvents();
        
        /*
         * The following methods are used to find commonly-used objects
         * for quick access later
         */
        setupProvinces();
        setupCountries();
        
//        root.getChild("header").getList("selectable").sort(); // just for fun
        
//        System.out.println(countries.size()+" countries.");
    }
    
    
    private void initDisplayNames() {
        /* Changed in EUGFile 1.02.01 to eliminate dependency on ostermillerutils. */
        
        /*
         * TODO: make multi-platform
         */
        final File f = new File(eu2FolderPath+modPath+"config/text.csv");
        
        String cL;
        String line;
        String[] arr;
        displayNames = new HashMap<String, String>();
        /*
         * Reading the displayNames from the EU2 file containing them
         */
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(f));
//            final CSVParser parser =
//                    new CSVParser(new BufferedReader(new FileReader(f)), ';');
            
//            parser.setCommentStart("#");
            
            while ((line = reader.readLine()) != null) {
//                name = parser.nextValue();
//
//                if (name == null)
//                    break;
                if (line.charAt(0) == '#')
                    continue;
                
//                try {
                    arr = line.split(";", -1);
                    displayNames.put(arr[0].toLowerCase(), arr[1]); //displayNames.put(name.toLowerCase(), parser.nextValue());
//                } catch (Exception ex) {
//                    System.out.println("DEBUG: "+line);
//                    if (arr != null)
//                        System.out.println("DEBUG: "+java.util.Arrays.deepToString(arr));
//                }
                //displayNames.put(name.toLowerCase(), parser.nextValue());
//                parser.getLine();
            }
            
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initProvs() {
        /* Changed in EUGFile 1.02.01 to eliminate dependency on ostermillerutils. */
        
        try {
            
            final BufferedReader br =
                    new BufferedReader(new FileReader(eu2FolderPath+modPath+"db/province.csv"));
            String currLine;
            
//            final CSVParser p = new CSVParser(br, ';');
//            p.setCommentStart("#");
//            final LabeledCSVParser reader = new LabeledCSVParser(p);
            
            provTerrain = new int[NUM_PROVINCES];
            provName = new String[NUM_PROVINCES];
            provReligion = new String[NUM_PROVINCES];
            provCulture = new String[NUM_PROVINCES];
            provIncome = new int[NUM_PROVINCES];
            provMine = new int[NUM_PROVINCES];
            provGoods = new String[NUM_PROVINCES];
            
            for (int i = 0; i < NUM_PROVINCES; i++) {
                provName[i] = "";
                provReligion[i] = "";
                provCulture[i] = "";
                provIncome[i] = -1;
                provTerrain[i] = -1;
                provMine[i] = 0;
                provGoods[i] = "";
            }
            
            int id = -1;
            
            br.readLine(); // eat first line
            
            while ((currLine = br.readLine()) != null) {
                if (currLine.charAt(0) == '#' || currLine.charAt(0) == ';')
                    continue;
                
                String[] args = currLine.split(";");
                
                try {
                    String sid = args[0]; //reader.getValueByLabel("Id");
                    
                    if (sid.length() != 0) {
                        id = Integer.parseInt(sid);
                        
                        if (id > 0) {
                            provName[id] = args[1]; //reader.getValueByLabel("Name");
                            provReligion[id] = args[3]; //reader.getValueByLabel("Religion");
                            provCulture[id] = args[4]; //reader.getValueByLabel("Culture");
                            provIncome[id] = Integer.parseInt(args[12]); //Integer.parseInt(reader.getValueByLabel("Income"));
                            provTerrain[id] = Integer.parseInt(args[13]); //Integer.parseInt(reader.getValueByLabel("Terrain"));
                            // NOTE: Entries 14, 15, and 16 (ToT-SPA;ToT-POR;HRE;) were added in 1.09, so
                            // these next two may be problematic for those without access to the patch.
                            provMine[id] = Integer.parseInt(args[18]); //Integer.parseInt(reader.getValueByLabel("MineValue"));
                            provGoods[id] = getDisplayName(args[19]); //getDisplayName(reader.getValueByLabel("Goods"));
                        }
                    }
                } catch (Exception e) {
                    System.err.print("Error with "+id+": ");
                    e.printStackTrace();
                }
            }
            
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initGoods() {
        try {
            final BufferedReader br = new BufferedReader(new FileReader(eu2FolderPath+modPath+"db/goods.csv"));
            br.readLine();      // eat first line
            
            String currentLine;
            goods = new String[25];
            int i = 0;
            
            while ((currentLine = br.readLine()) != null)
                goods[i++] = getDisplayName(currentLine.split(";")[0]);
            
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadEvents() {
        System.out.println("Loading event files...\n");
        
        for (eug.shared.ObjectVariable var : root.values) {
            if (var.varname.equals("event"))
                EventDatabase.loadEvents(var.getValue(), this);
        }
        
        System.out.println("\n --- Done ---");
        System.out.println("Loaded "+EventDatabase.database.size()+" events.\n");
    }
    
    public EU2Country getCountry(String tag) {
        tag = tag.toUpperCase().substring(0,3); // in case
        
        if (countryCache.containsKey(tag))
            return countryCache.get(tag);
        
        for (GenericObject ctry : countries) {
            if (ctry.getString("tag").equals(tag)) {
                final EU2Country country = new EU2Country(ctry, this);
                countryCache.put(tag, country);
                return country;
            }
        }
        
        return null;
    }
    
    public void removeCountry(EU2Country ctry) {
        root.removeChild(ctry.go);
        countries.remove(ctry.go);
        countryCache.remove(ctry.getTag());
        
        // These may not exist
        root.getChild("header").getList("selectable").delete(ctry.getTag());
        root.getChild("header").removeChild(ctry.getTag());
        
        final GenericObject globaldata = root.getChild("globaldata");
        
        List<GenericObject> toRemove = new java.util.ArrayList<GenericObject>();
        GenericList participant;
        for (GenericObject alliance : globaldata.getChildren("alliance")) {
            participant = alliance.getList("participant");
            if (participant.contains(ctry.getTag())) {
                if (participant.size() == 2)
                    toRemove.add(alliance);
                else
                    participant.delete(ctry.getTag());
            }
        }
        
        GenericObject score;
        for (GenericObject war : globaldata.getChildren("war")) {
            participant = war.getChild("attackers").getList("participant");
            if (participant.contains(ctry.getTag())) {
                if (participant.size() == 1)
                    toRemove.add(war);
                else
                    participant.delete(ctry.getTag());
            }
            
            participant = war.getChild("defenders").getList("participant");
            if (participant.contains(ctry.getTag())) {
                if (participant.size() == 1)
                    toRemove.add(war);
                else
                    participant.delete(ctry.getTag());
            }
            
            score = war.getChild("attackerscore");
            score.removeChild(ctry.getTag());
            for (GenericObject s : score.children)
                s.removeVariable(ctry.getTag());
            
            score = war.getChild("defenderscore");
            score.removeChild(ctry.getTag());
            for (GenericObject s : score.children)
                s.removeVariable(ctry.getTag());
        }
        
        for (GenericObject o : toRemove)
            globaldata.removeChild(o);
    }
    
    public EU2Country getCountry(int pos) {
        final String tag = countries.get(pos).getString("tag");
        
        if (countryCache.containsKey(tag))
            return countryCache.get(tag);
        
        final EU2Country ctry = new EU2Country(countries.get(pos), this);
        countryCache.put(tag, ctry);
        
        return ctry;
    }
    
    public Province getProvince(int id) {
        if (provinces.containsKey(id))
            return new Province(provinces.get(id), this);
        
        String sid = String.valueOf(id);
        for (GenericObject prov : provinces.values()) {
            if (prov.getString("id").equals(sid))
                return new Province(prov, this);
        }
        System.err.println("Province " + sid + " cannot be found");
        return null;
    }
    
    public boolean provCanHaveOwner(int id) {
        return provTerrain[id] < 5;
    }
    
    private void setupCountries() {
        countries = new ArrayList<GenericObject>();
        for (GenericObject obj : root.children) {
            if (obj.name.equals("country")) {
                countries.add(obj);
            }
        }
        Collections.sort(countries, new Comparator<GenericObject>() {
            public int compare(GenericObject o1, GenericObject o2) {
                return getDisplayName((o1.getString("tag"))).compareTo(getDisplayName(o2.getString("tag")));
            }
        });
        
        countryCache = new HashMap<String,EU2Country>(countries.size());
    }
    
    private void setupProvinces() {
        provinces = new HashMap<Integer, GenericObject>();
        for (GenericObject obj : root.getChildren("province")) {
            int id = obj.getInt("id");
            if (id < 0)
                System.err.println("A province has no id field");
            provinces.put(id, obj);
        }
    }
    
    public double getTotalPopulation() {
        double pop = 0.0;
        
        for (int i = 0; i < countries.size(); i++)
            pop += getCountry(i).getTotalPopulation();
        
        return pop;
    }
    
    public double getTotalLandForces() {
        double mil = 0.0;
        
        for (int i = 0; i < countries.size(); i++)
            mil += getCountry(i).numLandTroops();
        
        return mil;
    }
    
    public double getTotalSeaForces() {
        double mil = 0.0;
        
        for (int i = 0; i < countries.size(); i++)
            mil += getCountry(i).numShips();
        
        return mil;
    }
    
    public static EU2Scenario openEU2Scenario() {
        
        JFileChooser fc = new JFileChooser(eu2FolderPath);
//        ExtensionFileFiler filter = new ExtensionFileFiler();
//        filter.addExtension("eug");
//        filter.setDescription("Europa Universalis 2 scenarios");
//        fc.setFileFilter(filter);
        int res = fc.showOpenDialog(null);
        
        if (res != JFileChooser.APPROVE_OPTION){
            return null;
        }
        
        GenericObject root = EUGFileIO.load(fc.getSelectedFile());
        
        return new EU2Scenario(root, fc.getSelectedFile().getPath());
    }
    
    public static EU2Scenario openEU2Scenario(String filename) {
        GenericObject root = EUGFileIO.load(filename);
        return new EU2Scenario(root, filename);
    }
    
    public static EU2Scenario openEU2Scenario(String filename, boolean includeEvents) {
        GenericObject root = EUGFileIO.load(filename);
        return new EU2Scenario(root, filename, includeEvents);
    }
    
    public String getScenarioName() {
        return scenarioName;
    }
    
    public GenericList getSelectable() {
        return root.getChild("header").getList("selectable");
    }
    
    void changeCountryTag(String oldTag, String newTag) {
        // Normalize tags, just in case
        oldTag = oldTag.toUpperCase();
        newTag = newTag.toUpperCase();
        
        // Make sure the tag change makes sense
        if (oldTag.equals(newTag))
            return;
        
        countryCache.put(newTag, countryCache.get(oldTag));
        countryCache.remove(oldTag);
        
        // Change selectable
        GenericList selectable = root.getChild("header").getList("selectable");
        selectable.delete(oldTag);
        selectable.add(newTag, false);
        
        // If the old country was one of the ones shown in scenario setup,
        // change it.
        GenericObject shield = root.getChild("header").getChild(oldTag);
        if (shield != null)
            shield.name = newTag;
        
        // Change the tag in alliances (this also includes RM's and vassalizations)
        for (GenericObject alliance : root.getChild("globaldata").getChildren("alliance")) {
            alliance.getList("participant").replace(oldTag, newTag);
        }
        
        // This is the most involved part: Change the tag in all wars and all warscores.
        GenericObject score;
        for (GenericObject war : root.getChild("globaldata").getChildren("war")) {
            war.getChild("attackers").getList("participant").replace(oldTag, newTag);
            war.getChild("defenders").getList("participant").replace(oldTag, newTag);
            
            score = war.getChild("attackerscore");
            if (score.containsChild(oldTag))
                score.getChild(oldTag).name = newTag;
            
            for (GenericObject s : score.children)
                if (s.containsChild(oldTag))
                    s.getChild(oldTag).name = newTag;
            
            score = war.getChild("defenderscore");
            if (score.containsChild(oldTag))
                score.getChild(oldTag).name = newTag;
            
            for (GenericObject s : score.children)
                if (s.containsChild(oldTag))
                    s.getChild(oldTag).name = newTag;
        }
    }
    
    public static void main(String[] args) {
        EU2Scenario scenario = openEU2Scenario();
        System.out.println("Forces:");
        System.out.format("%12s %12s %14s\n", "Land", "Naval", "Population");
        System.out.format("%8.3f %8.3f %10.3f\n", scenario.getTotalLandForces(), scenario.getTotalSeaForces(), scenario.getTotalPopulation());
    }
}
