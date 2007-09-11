/*
 * EventDatabase.java
 *
 * Created on July 25, 2006, 5:31 PM
 */

package eug.specific.eu2;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.GenericObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Myers
 */
public class EventDatabase {
    
    public static final List<Event> database = new ArrayList<Event>();
    public static final Map<Integer, Event> byId = new HashMap<Integer, Event>();
    
    public static final Map<Integer, List<Integer>> triggers = new HashMap<Integer, List<Integer>>();
    public static final Map<Integer, List<Integer>> triggeredBy = new HashMap<Integer, List<Integer>>();
    
    public static final Map<Integer, List<Integer>> prereqOf = new HashMap<Integer, List<Integer>>();
    public static final Map<Integer, List<Integer>> prereqs = new HashMap<Integer, List<Integer>>();
    
    /** Creates a new instance of EventDatabase */
    private EventDatabase() { }
    
    private static void addEvent(Event evt) {
        if (byId.get(evt.getId()) != null)
            System.out.println("Error: Multiple events found with id = " + evt.getId());
        else {
            if (evt.getId() < 0)
                System.out.println("Event " + evt.go.getInt("ID"));
            database.add(evt);
            byId.put(evt.getId(), evt);
        }
    }
    
    
    public static void loadEvents(String filename, EU2Scenario scen) {
        ParserSettings settings = ParserSettings.getDefaults().setIgnoreComments(false).setPrintTimingInfo(false);
//        EUGFileIO.ignoreComments = false;
//        EUGFileIO.setTimingInfo(false);
        
        GenericObject root = EUGFileIO.load(filename, settings);
        if (root == null) {
            root = EUGFileIO.load(scen.eu2FolderPath + scen.modPath + filename);
            if (root == null) {
                root = EUGFileIO.load(scen.eu2FolderPath + filename);
                if (root == null) {
                    System.err.println("Error: Cannot find file " + filename + " in any known location!");
                    return;
                }
            }
        }
        
        for (GenericObject evt : root.children) {
            if (!evt.name.equals("event")) {
                if (!evt.name.equalsIgnoreCase("event")) {
                System.err.println("Error: Non-event found in file " + filename);
                continue;
                }
            }
            
            addEvent(new Event(evt, scen));
        }
    }
    
    public static int numEvents() {
        return database.size();
    }
    
    public static void clear() {
        database.clear();
        byId.clear();
        triggers.clear();
        triggeredBy.clear();
        prereqOf.clear();
        prereqs.clear();
    }
    
    /* e1 triggers e2 */
    static void addTrigger(int e1, int e2) {
        if (triggers.get(e1) == null)
            triggers.put(e1, new ArrayList<Integer>());
        if (triggeredBy.get(e2) == null)
            triggeredBy.put(e2, new ArrayList<Integer>());
        
        triggers.get(e1).add(e2);
        triggeredBy.get(e2).add(e1);
    }
    
    /* e1 is a prereq of e2 */
    static void addPrereq(int e1, int e2) {
        if (prereqOf.get(e1) == null)
            prereqOf.put(e1, new ArrayList<Integer>());
        if (prereqs.get(e2) == null)
            prereqs.put(e2, new ArrayList<Integer>());
        
        prereqOf.get(e1).add(e2);
        prereqs.get(e2).add(e1);
    }
    
    public static String triggeredBy(Event evt) {
        List<Integer> list = triggeredBy.get(evt.getId());
        if (list == null)
            return "none";
        
        StringBuilder ret = new StringBuilder();
        for (int id : list) {
            ret.append(id).append(" ");
        }
        
        return ret.toString();
    }
    
    public static String triggers(Event evt) {
        List<Integer> list = triggers.get(evt.getId());
        if (list == null)
            return "none";
        
        StringBuilder ret = new StringBuilder();
        for (int id : list) {
            ret.append(id).append(" ");
        }
        
        return ret.toString();
    }
    
    public static String prereqOf(Event evt) {
        List<Integer> list = prereqOf.get(evt.getId());
        if (list == null)
            return "none";
        
        StringBuilder ret = new StringBuilder();
        for (int id : list) {
            ret.append(id).append(" ");
        }
        
        return ret.toString();
    }
    
    public static String prereqs(Event evt) {
        List<Integer> list = prereqs.get(evt.getId());
        if (list == null)
            return "none";
        
        StringBuilder ret = new StringBuilder();
        for (int id : list) {
            ret.append(id).append(" ");
        }
        
        return ret.toString();
    }
}
