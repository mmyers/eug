

package eug.specific.clausewitz;

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
 * Implementation of <code>ClausewitzDataSource</code> which gets country and
 * province data from the history files.
 * @author Michael Myers
 * @since EUGFile 1.07.00
 */
public abstract class ClausewitzScenario implements ClausewitzDataSource {
    
    protected FilenameResolver resolver;
    
    private final Map<String, GenericObject> ctryHistoryCache;
    private final Map<Integer, GenericObject> provHistoryCache;
    
    private static final ParserSettings settings =
            ParserSettings.getDefaults().setPrintTimingInfo(false);
    
    /** Creates a new instance of EU3Scenario */
    public ClausewitzScenario(FilenameResolver resolver) {
        this.resolver = resolver;
        ctryHistoryCache = new HashMap<String, GenericObject>();
        provHistoryCache = new HashMap<Integer, GenericObject>();
    }
    
    public ClausewitzScenario(String mainDir, String modDir) {
        resolver = new FilenameResolver(mainDir, modDir);
        ctryHistoryCache = new HashMap<String, GenericObject>();
        provHistoryCache = new HashMap<Integer, GenericObject>();
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
        final List<GenericObject> ret = new ArrayList<GenericObject>();
        
        for (File file : resolver.listFiles("history/wars")) {
            final GenericObject obj = EUGFileIO.load(file, settings);
            if (obj != null && !obj.isEmpty())
                ret.add(obj);
        }
        
        return ret;
    }
    
    public List<GenericObject> getWars(final String date) {
        final List<GenericObject> ret = new ArrayList<GenericObject>();
        
        for (File file : resolver.listFiles("history/wars")) {
            final GenericObject obj = EUGFileIO.load(file, settings);
            if (obj != null) {
                final List<String> participants = ClausewitzHistory.getHistStrings(
                        obj, date, "add_attacker", "rem_attacker");
                
                if (!participants.isEmpty())
                    ret.add(obj);
            }
        }
        
        return ret;
    }

    public void removeWar(String name) {
        final String filename =
                resolver.resolveFilename("history/wars/" + name + ".txt");
        final File file = new File(filename);
        if (file.exists()) {
            if (!file.delete())
                System.err.println("Could not delete " + filename);
        } else {
            // What do we do here? Assume it's a war name, not a filename?
            // If so, what do we do about it?
        }
    }
    
    
    public String getCountryAsStr(String tag) {
        return loadFile(resolveCountryHistoryFile(tag));
    }
    
    public String getCountryHistoryAsStr(String tag) {
        return loadFile(resolveCountryHistoryFile(tag));
    }
    
    public String getProvinceAsStr(int id) {
        return loadFile(resolveProvinceHistoryFile(id));
    }
    
    public String getProvinceHistoryAsStr(int id) {
        return loadFile(resolveProvinceHistoryFile(id));
    }
    
    
    public void saveCountry(String tag, String cname, final String data) {
        String filename = resolveCountryHistoryFile(tag);
        if (filename == null) {
            filename = resolver.resolveDirectory("history") + "countries/" +
                    tag + " - " + cname + ".txt";
        } else {
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveDirectory("history") + "countries/" + filename;
        }
        
        final File file = new File(filename);
        final String backupFilename = getBackupFilename(filename);
        if (file.exists()) {
            if (!file.renameTo(new File(backupFilename)))
                System.err.println("Backup of " + file.getName() + " failed");
        }
        
        saveFile(filename, data);
    }
    
    public void saveProvince(int id, String pname, final String data) {
        String filename = resolveProvinceHistoryFile(id);
        if (filename == null) {
            filename = resolver.resolveDirectory("history") + "provinces/" +
                    id + " - " + pname + ".txt";
        } else {
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveDirectory("history") + "provinces/" + filename;
        }
        
        final File file = new File(filename);
        if (file.exists()) {
            final String backupFilename = getBackupFilename(filename);
            if (!file.renameTo(new File(backupFilename)))
                System.err.println("Backup of " + file.getName() + " failed");
        }
        
        saveFile(filename, data);
    }

    public void saveWar(String name, String data) {
        String filename =
                resolver.resolveFilename("history/wars/" + name + ".txt");
        final File file = new File(filename);
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
            if (!file.renameTo(new File(getBackupFilename(filename))))
                System.err.println("Backup of " + file.getName() + " failed");
        }
        saveFile(filename, data);
    }
    
    protected static final String getBackupFilename(String filename) {
        final File f = new File(filename);
        return f.getParent() + File.separatorChar + "~" + f.getName();
    }
    
    public void saveChanges() {
        // do nothing
    }
    
    public boolean hasUnsavedChanges() {
        return false;
    }
    
    
    public void reloadCountry(String tag) {
        ctryHistoryCache.remove(tag);
        resolver.reset();
    }
    
    public void reloadCountryHistory(String tag) {
        ctryHistoryCache.remove(tag);
        resolver.reset();
    }

    public void reloadCountries() {
        ctryHistoryCache.clear();
        resolver.reset();
        preloadCountries();
    }
    
    public void reloadProvince(int id) {
        provHistoryCache.remove(id);
        resolver.reset();
    }
    
    public void reloadProvinceHistory(int id) {
        provHistoryCache.remove(id);
        resolver.reset();
    }

    public void reloadProvinces() {
        provHistoryCache.clear();
        resolver.reset();
        preloadProvinces(2000); // no way to know what a sensible number is here, and it won't matter much.
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
            hist = provHistoryCache.get(id);
            if (hist == null) {
                final String histFile = resolveProvinceHistoryFile(id);
                
                if (histFile == null) {
//                    System.err.println("Cannot find province history file for ID " + id);
                    continue;
                }
                
                hist = EUGFileIO.load(histFile, settings);
                
                if (hist == null) {
//                    System.err.println("Failed to load province history file for ID " + id);
                } else {
                    provHistoryCache.put(id, hist);
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
            
            hist = ctryHistoryCache.get(tag);
            if (hist == null) {
                final String histFile = resolveCountryHistoryFile(tag);
                
                if (histFile == null) {
//                    System.err.println("Cannot find country history file for " + tag);
                    continue;
                }
                
                hist = EUGFileIO.load(histFile, settings);
                
                if (hist == null) {
                    System.err.println("Failed to load country history file for " + tag);
                } else {
                    ctryHistoryCache.put(tag, hist);
                }
            }
        }
    }

    protected String resolveCountryHistoryFile(String tag) {
        return resolver.getCountryHistoryFile(tag);
    }

    protected String resolveProvinceHistoryFile(int id) {
        return resolver.getProvinceHistoryFile(id);
    }
    
    /** Gets the province history for the given id, using the cache. */
    private GenericObject getProvHistory(final int id) {
        GenericObject hist = provHistoryCache.get(id);
        if (hist == null) {
            final String histFile = resolveProvinceHistoryFile(id);
            
            if (histFile == null) {
                //System.err.println("Cannot find province history file for ID " + id);
                return null;
            }
            
            hist = EUGFileIO.load(histFile, settings);
            
            if (hist == null) {
                System.err.println("Failed to load province history file for ID " + id);
            } else {
                provHistoryCache.put(id, hist);
            }
        }
        return hist;
    }
    
    /** Gets the country history for the given tag, using the cache. */
    private GenericObject getCtryHistory(final String tag) {
        GenericObject hist = ctryHistoryCache.get(tag);
        if (hist == null) {
            final String histFile = resolveCountryHistoryFile(tag);
            
            if (histFile == null) {
                System.err.println("Cannot find country history file for " + tag);
                return null;
            }
            
            hist = EUGFileIO.load(histFile, settings);
            
            if (hist == null) {
                System.err.println("Failed to load country history file for " + tag);
            } else {
                ctryHistoryCache.put(tag, hist);
            }
        }
        return hist;
    }
    
    protected final String loadFile(final String filename) {
        if (filename == null)
            return null;
        
        final File file = new File(filename);
        final char[] data = new char[(int)file.length()];
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            if (reader.read(data) != data.length)
                System.err.println("???");
            return String.valueOf(data);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {}
            }
        }
        return null;
    }
    
    protected final void saveFile(final String filename, final String data) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            writer.write(data);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {}
            }
        }
    }
}
