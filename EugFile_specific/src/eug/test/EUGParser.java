/*
 * EUGParser.java
 *
 * Created on March 11, 2006, 6:47 PM
 */

package eug.test;

import eug.shared.GenericObject;
import eug.shared.Utilities;
import eug.specific.eu2.EU2Country;
import eug.specific.eu2.EU2Scenario;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Michael Myers
 */
public class EUGParser {
    public static final String sfPath = "C:/Program Files/Strategy First/";
    public static final String pePath = "C:/Program Files/Paradox Entertainment/";
    
    public static final String eu2Path = sfPath + "Europa Universalis 2/";
    public static final String vicPath = sfPath + "Victoria/";
    
    public static final String modPath = eu2Path + "AGCEEP/";
    
    public static final String savePath = modPath + "Scenarios/Save Games/";
    public static final String dbPath = eu2Path + "DB/";
    public static final String dbModPath = modPath + "DB/";
    public static final String configPath = eu2Path + "Config/";
    public static final String configModPath = modPath + "Config/";
    
    private PrintStream outStream = System.out;
    
    private Map<String, List<Object>> milTable;
//    private Map<String, Country> ctryTable;
//    private List<Country> countries;
    private List<GenericObject> alliances;
    
    private EU2Scenario saveGame;
    
    public static final Comparator<EU2Country> SORT_CTRY_BY_TAG = new Comparator<EU2Country>() {
        public int compare(EU2Country c1, EU2Country c2) {
            return (c1.getTag().compareTo(c2.getTag()));
        }
    };
    
    
    /**
     * Creates a new instance of EUGParser.
     */
    public EUGParser(String filename) {
        try {
            parseEU2(filename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param args the command line arguments. If there is an argument, it
     * should be the name of a savegame file. Otherwise, the newest save file
     * will be used.
     */
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        EUGParser parser = null;
        InputStream inStream = null;    // allows use of script files
        PrintStream out = null;         // allows saving results to file
        
        for (String arg : args) {
            if (arg.matches("(/|--)script=.*")) {
                try {
                    inStream = new FileInputStream(arg.substring(arg.indexOf((int)'=') + 1));
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            } else if (arg.matches("(/|--)save=.*")) {
                try {
                    out = new PrintStream(arg.substring(arg.indexOf((int)'=') + 1));
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            } else if (parser == null) {    //assume it's a filename
                parser = new EUGParser(arg);
            } else {
                System.err.println(arg);
            }
        }
        
        if (parser == null)
            parser = new EUGParser(Utilities.findNewestFile(new File(savePath)));
        if (inStream == null)
            inStream = System.in;
        if (out != null)
            parser.setOutStream(out);
        
        // BEGIN {Parse commands}
        final Scanner s = new Scanner(inStream);
        String com;     // command
        final String prompt = "Command> ";
        
        parser.outStream.print(prompt);
        
        while (!(com = s.next()).equals("end")) {
            if (inStream != System.in)
                parser.outStream.println(com);    // echo the command
            
            parser.outStream.println();
            
            if (com.equals("mil"))
                parser.printMilitary();
            else if (com.equals("all"))
                parser.printAlliances();
            else if (com.equals("own")) {
                com = s.next();
                parser.printOwned(com);
            } else if (com.equals("ctrl")) {
                com = s.next();
                parser.printControlled(com);
            } else if (com.equals("core")) {
                com = s.next();
                parser.printCores(com);
            } else if (com.equals("ctrlNotOwn")) {
                com = s.next();
                parser.printControlledNotOwned(com);
            } else if (com.equals("ownNotCtrl")) {
                com = s.next();
                parser.printOwnedNotControlled(com);
            } else if (com.equals("ownNotCore")) {
                com = s.next();
                parser.printOwnedNotCore(com);
            } else if (com.equals("whoOwns")) {
                com = s.next();
                parser.printOwner(com);
            } else if (com.equals("whoControlls")) {
                com = s.next();
                parser.printController(com);
            } else if (com.equals("list")) {
                parser.listCountries();
            } else if (com.equals("name")) {
                com = s.next();
                try {
                    parser.outStream.println(com + ": " + parser.saveGame.getDisplayName(com));
                } catch (Exception ex) {
                    parser.outStream.println(com + ": No text found.");
                }
            } else if (com.equals("help")) {
                parser.outStream.println("COMMANDS:");
                parser.outStream.println("mil\t\t\tPrint military strength of all countries.");
                parser.outStream.println("all\t\t\tPrint military strength of all alliances.");
                parser.outStream.println("own <tag>\t\tPrint provinces owned by <tag>.");
                parser.outStream.println("ctrl <tag>\t\tPrint provinces controlled by <tag>.");
                parser.outStream.println("core <tag>\t\tPrint national provinces of <tag>.");
                parser.outStream.println("ctrlNotOwn <tag>\tPrint provinces controlled (but not owned) by <tag>.");
                parser.outStream.println("ownNotCtrl <tag>\tPrint provinces owned (but not controlled) by <tag>.");
                parser.outStream.println("ownNotCore <tag>\tPrint non-national provinces owned by <tag>.");
                parser.outStream.println("whoOwns <id>\t\tPrint the country that owns province <id>.");
                parser.outStream.println("whoControlls <id>\tPrint the country that controlls province <id>.");
                parser.outStream.println("list\t\t\tPrint a listing of all countries.");
                parser.outStream.println("help\t\t\tPrint this help.");
                parser.outStream.println("end\t\t\tExit the program.");
            } else {
                parser.outStream.println("Unknown command: \'" + com + "\'");
            }
            parser.outStream.println();
            parser.outStream.print(prompt);
        }
        // END {Parse commands}
        
        long endTime = System.nanoTime();
        System.out.println("\nTotal time: " + (endTime - startTime) + " nanoseconds.");
    }
    
    private void parseEU2(String filename) throws Exception {
        long startTime = System.nanoTime();
        saveGame = EU2Scenario.openEU2Scenario(filename);
        long endTime = System.nanoTime();
        System.out.println("\nTime to load savegame: " + (endTime - startTime) + " nanoseconds.");
    }
    
//    public void printMilitary(String tag) {
//        Country ctry = getCountry(tag);
//        if (ctry == null) {
//            System.err.println("Error: Can't find country " + tag);
//            return;
//        }
//
//        Map<String, Float> military = ctry.getMilitary();
//        outStream.format("%6s %10.3f %10.3f %8.3f %12.3f %10.3f %10.3f %10.3f %12.3f\n",
//                ctry.getTag(), military.get("inf"), military.get("cav"), military.get("art"),
//                military.get("army"),
//                military.get("warships"), military.get("galleys"), military.get("transports"),
//                military.get("navy"));
//
//    }
    
    public void printMilitary() {
        outStream.format("%6s %10s %10s %10s %12s %10s %10s %10s %12s", "Tag",
                "Infantry", "Cavalry", "Artillery", "Total army",
                "Warships", "Galleys", "Transports", "Total navy");
        outStream.println();
        
        for (String tag : saveGame.getSelectable()) {
            EU2Country ctry = saveGame.getCountry(tag);
            outStream.format("%6s %10.3f %10.3f %8.3f %12.3f %10.3f %10.3f %10.3f %12.3f",
                    tag, ctry.numInf(), ctry.numCav(), ctry.numArt(),
                    ctry.numLandTroops(),
                    ctry.numWarships(), ctry.numGalleys(), ctry.numTransports(),
                    ctry.numShips());
            outStream.println();
        }
    }
    
    public void printAlliances() {
        outStream.format("%23s  %10s %10s", "Alliance", "Army size", "Navy size");
        outStream.println();
        
        double army, navy;
        StringBuilder allTag;
        for (GenericObject alliance : saveGame.root.getChild("globaldata").getChildren("alliance")) {
            if (!alliance.getString("type").equals("militaryalliance")) {
                continue;
            }
            
            army = navy = 0;
            allTag = new StringBuilder();
            
            java.util.Iterator<String> itr = alliance.getList("participant").iterator();
            while (itr.hasNext()) {
                String tag = itr.next();
                allTag.append(tag);
                if (itr.hasNext())
                    allTag.append("-");
                army += saveGame.getCountry(tag).numLandTroops();
                navy += saveGame.getCountry(tag).numShips();
            }
            
            outStream.format("%23s  %10.3f %10.3f", allTag, army, navy);
            outStream.println();
        }
    }
    
    public void printOwned(String countryTag) {
        EU2Country country = getCountry(countryTag);
        if (country == null) {
            System.err.println("Error: Can't find country " + countryTag);
            return;
        }
        
        outStream.println(country.getName() + "'s owned provinces:");
        
        printProvs(country.getOwnedProvs());
    }
    
    public void printControlled(String countryTag) {
        EU2Country country = getCountry(countryTag);
        if (country == null) {
            System.err.println("Error: Can't find country " + countryTag);
            return;
        }
        
        outStream.println(country.getName() + "'s controlled provinces:");
        
        printProvs(country.getControlledProvs());
    }
    
    public void printControlledNotOwned(String countryTag) {
        EU2Country country = getCountry(countryTag);
        if (country == null) {
            System.err.println("Error: Can't find country " + countryTag);
            return;
        }
        
        outStream.println(country.getName() + "'s controlled (but not owned) provinces:");
        
        printProvs(country.getControlledNotOwned());
    }
    
    public void printOwnedNotControlled(String countryTag) {
        EU2Country country = getCountry(countryTag);
        if (country == null) {
            System.err.println("Error: Can't find country " + countryTag);
            return;
        }
        
        outStream.println(country.getName() + "'s owned (but not controlled) provinces:");
        
        printProvs(country.getOwnedNotControlled());
    }
    
    public void printCores(String countryTag) {
        EU2Country country = getCountry(countryTag);
        if (country == null) {
            System.err.println("Error: Can't find country " + countryTag);
            return;
        }
        
        outStream.println(country.getName() + "'s national provinces:");
        
        printProvs(country.getNationalProvs());
    }
    
    public void printOwnedNotCore(String countryTag) {
        EU2Country country = getCountry(countryTag);
        if (country == null) {
            System.err.println("Error: Can't find country " + countryTag);
            return;
        }
        
        outStream.println(country.getName() + "'s owned non-national provinces:");
        
        printProvs(country.getOwnedNotCore());
    }
    
    public void printOwner(String sid) {
        int id = Integer.parseInt(sid);
        
        for (int i = 0; i < saveGame.numCountries(); i++) {
            EU2Country ctry = saveGame.getCountry(i);
            if (ctry.ownsProvince(id)) {
                outStream.println(saveGame.provName[id] + " is owned by " + ctry.getName() + ".");
                return;
            }
        }
        outStream.println(saveGame.provName[id] + " is not owned.");
    }
    
    public void printController(String sid) {
        int id = Integer.parseInt(sid);
        
        for (int i = 0; i < saveGame.numCountries(); i++) {
            EU2Country ctry = saveGame.getCountry(i);
            if (ctry.controllsProvince(id)) {
                outStream.println(saveGame.provName[id] + " is controlled by " + ctry.getName() + ".");
                return;
            }
        }
        outStream.println(saveGame.provName[id] + " is not controlled.");
    }
    
    private void printProvs(final List<Integer> provIds) {
        for (Integer p : provIds)
            outStream.println(saveGame.provName[p]);
        
        outStream.println("Total: " + provIds.size() + " provinces.");
    }
    
    public void listCountries() {
        for (String tag : saveGame.getSelectable())
            outStream.println(tag + "\t" + saveGame.getDisplayName(tag));
        outStream.println("Total: " + saveGame.getSelectable().size() + " countries.");
    }
    
//    public List<EU2Country> getCountries() {
//        return saveGame.getCountries();
//    }
//
//    public Country getCountry(String tag) {
//        return ctryTable.get(tag);
//    }
    
    private EU2Country getCountry(final String tag) {
        // normalize the tag, just in case
        return saveGame.getCountry(tag.toUpperCase().substring(0,3));
    }
    
    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }
}
