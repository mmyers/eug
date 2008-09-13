/*
 * EU3Scenario.java
 *
 * Created on July 2, 2007, 1:59 PM
 */

package eug.specific.eu3;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of <code>EU3DataSource</code> which gets country and province
 * data from the history files.
 * @author Michael Myers
 * @since EUGFile 1.06.00pre1
 */
public class EU3Scenario implements EU3DataSource {
    
    private FilenameResolver resolver;
    
    private final Map<Object, GenericObject> historyCache;
    
    private static final ParserSettings settings =
            ParserSettings.getDefaults().setPrintTimingInfo(false);
    
    /** Creates a new instance of EU3Scenario */
    public EU3Scenario(FilenameResolver resolver) {
        this.resolver = resolver;
        historyCache = new HashMap<Object, GenericObject>();
    }
    
    public EU3Scenario(String mainDir, String modDir) {
        resolver = new FilenameResolver(mainDir, modDir);
        historyCache = new HashMap<Object, GenericObject>();
    }
    
    public void setResolver(FilenameResolver resolver) {
        this.resolver = resolver;
    }
    
    public GenericObject getCountry(String tag) {
        return getCtryHistory(tag);
    }
    
    public GenericObject getCountryHistory(String tag) {
        return getCtryHistory(tag);
    }
    
    public GenericObject getProvince(int id) {
        return getProvHistory(id);
    }
    
    public GenericObject getProvinceHistory(int id) {
        return getProvHistory(id);
    }
    
    public List<GenericObject> getWars() {
        List<GenericObject> ret = new ArrayList<GenericObject>();
        
        for (File file : resolver.listFiles("history/wars")) {
            GenericObject obj = EUGFileIO.load(file, settings);
            if (obj != null)
                ret.add(obj);
        }
        
        return ret;
    }
    
    public List<GenericObject> getWars(final String date) {
        List<GenericObject> ret = new ArrayList<GenericObject>();
        
        for (File file : resolver.listFiles("history/wars")) {
            GenericObject obj = EUGFileIO.load(file, settings);
            if (obj != null) {
                List<String> participants =
                        EU3History.getHistStrings(obj, date, "add_attacker", "rem_attacker");
                
                if (!participants.isEmpty())
                    ret.add(obj);
            }
        }
        
        return ret;
    }

    public void removeWar(String name) {
        String filename = resolver.resolveFilename("history/wars/" + name + ".txt");
        File file = new File(filename);
        if (file.exists()) {
            if (!file.delete())
                System.err.println("Could not delete " + filename);
        } else {
            // What do we do here? Assume it's a war name, not a filename?
            // If so, what do we do about it?
        }
    }
    
    
    public String getCountryAsStr(String tag) {
        return loadFile(resolver.getCountryHistoryFile(tag));
    }
    
    public String getCountryHistoryAsStr(String tag) {
        return loadFile(resolver.getCountryHistoryFile(tag));
    }
    
    public String getProvinceAsStr(int id) {
        return loadFile(resolver.getProvinceHistoryFile(id));
    }
    
    public String getProvinceHistoryAsStr(int id) {
        return loadFile(resolver.getProvinceHistoryFile(id));
    }
    
    
    public void saveCountry(String tag, String cname, final String data) {
        String filename = resolver.getCountryHistoryFile(tag);
        if (filename == null) {
            filename = resolver.resolveDirectory("history") + "countries/" +
                    tag + " - " + cname + ".txt";
        } else {
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveDirectory("history") + "countries/" + filename;
        }
        
        File file = new File(filename);
        String backupFilename = getBackupFilename(filename);
        if (file.exists()) {
            file.renameTo(new File(backupFilename));
        }
        
        saveFile(filename, data);
    }
    
    public void saveProvince(int id, String pname, final String data) {
        String filename = resolver.getProvinceHistoryFile(id);
        if (filename == null) {
            filename = resolver.resolveDirectory("history") + "provinces/" +
                    id + " - " + pname + ".txt";
        } else {
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveDirectory("history") + "provinces/" + filename;
        }
        
        File file = new File(filename);
        if (file.exists()) {
            String backupFilename = getBackupFilename(filename);
            file.renameTo(new File(backupFilename));
        }
        
        saveFile(filename, data);
    }

    public void saveWar(String name, String data) {
        String filename = resolver.resolveFilename("history/wars/" + name + ".txt");
        File file = new File(filename);
//        if (!file.exists()) {
//            // we were given the war name, not the filename
//            // or is this reasonable? Left commented out for now.
//            for (File warFile : resolver.listFiles("history/wars")) {
//                GenericObject war = EUGFileIO.load(warFile, settings);
//                if (war.getString("name").equals(name)) {
//                    file = warFile;
//                    filename = file.getAbsolutePath();
//                    break;
//                }
//            }
//        }
        
        if (!file.exists()) {
            // new file
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveDirectory("history") + "wars/" + filename;
        } else {
            file.renameTo(new File(getBackupFilename(filename)));
        }
        saveFile(filename, data);
    }
    
    private static final String getBackupFilename(String filename) {
        File f = new File(filename);
        return f.getParent() + File.separatorChar + "~" + f.getName();
    }
    
    public void saveChanges() {
        // do nothing
    }
    
    public boolean hasUnsavedChanges() {
        return false;
    }
    
    
    public void reloadCountry(String tag) {
        historyCache.remove(tag);
        resolver.reset();
    }
    
    public void reloadCountryHistory(String tag) {
        historyCache.remove(tag);
        resolver.reset();
    }
    
    public void reloadProvince(int id) {
        historyCache.remove(id);
        resolver.reset();
    }
    
    public void reloadProvinceHistory(int id) {
        historyCache.remove(id);
        resolver.reset();
    }
    
    public void preloadProvinces(int last) {
        preloadProvHistory(1, last);
    }
    
    public void preloadCountries() {
        preloadCountryHistory();
    }
    
    private void preloadProvHistory(int start, int end) {
        GenericObject hist;
        for (int id = start; id < end; id++) {
            hist = historyCache.get(id);
            if (hist == null) {
                String histFile = resolver.getProvinceHistoryFile(id);
                
                if (histFile == null) {
//                    System.err.println("Cannot find province history file for ID " + id);
                    continue;
                }
                
                hist = EUGFileIO.load(histFile, settings);
                
                if (hist == null) {
//                    System.err.println("Failed to load province history file for ID " + id);
                } else {
                    historyCache.put(id, hist);
                }
            }
        }
    }
    
    private void preloadCountryHistory() {
        GenericObject hist;
        String tag;
        
        final GenericObject countries =
                EUGFileIO.load(resolver.resolveFilename("common/countries.txt"), settings);
        
        for (ObjectVariable def : countries.values) {
            tag = def.varname;
            
            hist = historyCache.get(tag);
            if (hist == null) {
                String histFile = resolver.getCountryHistoryFile(tag);
                
                if (histFile == null) {
//                    System.err.println("Cannot find country history file for " + tag);
                    continue;
                }
                
                hist = EUGFileIO.load(histFile, settings);
                
                if (hist == null) {
                    System.err.println("Failed to load country history file for " + tag);
                } else {
                    historyCache.put(tag, hist);
                }
            }
        }
    }
    
    /** Gets the province history for the given id, using the cache. */
    private final GenericObject getProvHistory(final int id) {
        GenericObject hist = historyCache.get(id);
        if (hist == null) {
            String histFile = resolver.getProvinceHistoryFile(id);
            
            if (histFile == null) {
                System.err.println("Cannot find province history file for ID " + id);
                return null;
            }
            
            hist = EUGFileIO.load(histFile, settings);
            
            if (hist == null) {
                System.err.println("Failed to load province history file for ID " + id);
            } else {
//                java.util.Collections.sort(hist.children, DATE_OBJECT_COMPARATOR);
                historyCache.put(id, hist);
            }
        }
        return hist;
    }
    
    /** Gets the country history for the given tag, using the cache. */
    private final GenericObject getCtryHistory(final String tag) {
        GenericObject hist = historyCache.get(tag);
        if (hist == null) {
            String histFile = resolver.getCountryHistoryFile(tag);
            
            if (histFile == null) {
                System.err.println("Cannot find country history file for " + tag);
                return null;
            }
            
            hist = EUGFileIO.load(histFile, settings);
            
            if (hist == null) {
                System.err.println("Failed to load country history file for " + tag);
            } else {
//                java.util.Collections.sort(hist.children, DATE_OBJECT_COMPARATOR);
                historyCache.put(tag, hist);
            }
        }
        return hist;
    }
    
    private final String loadFile(final String filename) {
        if (filename == null)
            return null;
        
        final File file = new File(filename);
        final char[] data = new char[(int)file.length()];
        try {
            final FileReader reader = new FileReader(file);
            if (reader.read(data) != data.length)
                System.err.println("???");
            reader.close();
            return String.valueOf(data);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private final void saveFile(final String filename, final String data) {
        try {
            final FileWriter writer = new FileWriter(filename);
            writer.write(data);
            writer.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
