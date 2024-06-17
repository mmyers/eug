

package eug.specific.clausewitz;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.shared.Style;
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
    protected final Map<Integer, GenericObject> provHistoryCache;
    
    protected Style saveStyle = Style.EU3_SAVE_GAME;
    
    protected static final ParserSettings settings = ParserSettings.getQuietSettings();
    
    protected boolean saveBackups = true;
    
    /**
     * Creates a ClausewitzScenario which will use the given
     * {@link eug.shared.FilenameResolver} to locate history files.
     * @param resolver a {@code FilenameResolver} with mod information
     */
    public ClausewitzScenario(FilenameResolver resolver) {
        this.resolver = resolver;
        ctryHistoryCache = new HashMap<>();
        provHistoryCache = new HashMap<>();
    }
    
    /**
     * Creates a ClausewitzScenario which will use the given main game directory
     * and mod directory to locate history files. This is essentially the same
     * as using the {@link ClausewitzScenario(FilenameResolver)} constructor
     * with a {@link FilenameResolver} that is set to Clausewitz 1 mode.
     * @param mainDir the main game directory.
     * @param modDir the mod directory (or null if no mod is used).
     * @see FilenameResolver#isClausewitz2Mod() 
     */
    public ClausewitzScenario(String mainDir, String modDir) {
        resolver = new FilenameResolver(mainDir, modDir);
        ctryHistoryCache = new HashMap<>();
        provHistoryCache = new HashMap<>();
    }
    
    @Override
    public void setStyle(Style style) {
        this.saveStyle = style;
    }
    
    public void setResolver(FilenameResolver resolver) {
        this.resolver = resolver;
    }
    
    @Override
    public GenericObject getCountry(String tag) {
        return getCtryHistory(tag);
    }
    
    @Override
    public GenericObject getCountryHistory(String tag) {
        return getCtryHistory(tag);
    }
    
    @Override
    public GenericObject getProvince(int id) {
        return getProvHistory(id);
    }
    
    @Override
    public GenericObject getProvinceHistory(int id) {
        return getProvHistory(id);
    }
    
    @Override
    public List<GenericObject> getWars() {
        final List<GenericObject> ret = new ArrayList<>();
        
        for (File file : resolver.listFiles("history/wars")) {
            final GenericObject obj = EUGFileIO.load(file, settings);
            if (obj != null && !obj.isEmpty())
                ret.add(obj);
        }
        
        return ret;
    }
    
    @Override
    public List<GenericObject> getWars(final String date) {
        final List<GenericObject> ret = new ArrayList<>();
        
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

    @Override
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
    
    
    @Override
    public String getCountryAsStr(String tag) {
        return loadFile(resolveCountryHistoryFile(tag));
    }
    
    @Override
    public String getCountryHistoryAsStr(String tag) {
        return loadFile(resolveCountryHistoryFile(tag));
    }
    
    @Override
    public String getProvinceAsStr(int id) {
        return loadFile(resolveProvinceHistoryFile(id));
    }
    
    @Override
    public String getProvinceHistoryAsStr(int id) {
        return loadFile(resolveProvinceHistoryFile(id));
    }
    
    
    @Override
    public void saveCountry(String tag, String cname, final String data) {
        String filename = resolveCountryHistoryFile(tag);
        if (filename == null) {
            filename = resolver.resolveFilenameForWrite("history/countries/" + tag + " - " + cname + ".txt");
        } else {
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveFilenameForWrite("history/countries" + filename);
        }
        
        final File file = new File(filename);
        if (file.exists() && saveBackups) {
            final String backupFilename = getBackupFilename(filename);
            if (!file.renameTo(new File(backupFilename)))
                System.err.println("Backup of " + file.getName() + " failed");
        }
        
        saveFile(filename, data);
    }
    
    @Override
    public void saveProvince(int id, String pname, final String data) {
        String filename = resolveProvinceHistoryFile(id);
        if (filename == null) {
            filename = resolver.resolveFilenameForWrite("history/provinces/" + id + " - " + pname + ".txt");
        } else {
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveFilenameForWrite("history/provinces" + filename);
        }
        
        final File file = new File(filename);
        if (file.exists() && saveBackups) {
            final String backupFilename = getBackupFilename(filename);
            if (!file.renameTo(new File(backupFilename)))
                System.err.println("Backup of " + file.getName() + " failed");
        }
        
        saveFile(filename, data);
    }

    @Override
    public void saveWar(String name, String data) {
        String filename =
                resolver.resolveFilenameForWrite("history/wars/" + name + ".txt");
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
            // shouldn't need to do anything here now that we use resolveFilenameForWrite rather than resolveDirectory
            //filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            //filename = resolver.resolveFilenameForWrite("history/wars/" + filename);
        } else if (saveBackups) {
            if (!file.renameTo(new File(getBackupFilename(filename))))
                System.err.println("Backup of " + file.getName() + " failed");
        }
        saveFile(filename, data);
    }
    
    protected static String getBackupFilename(String filename) {
        final File f = new File(filename);
        return f.getParent() + File.separatorChar + "~" + f.getName();
    }
    
    @Override
    public void setBackups(boolean useBackups) {
        this.saveBackups = useBackups;
    }
    
    @Override
    public void saveChanges() {
        // do nothing
    }
    
    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }
    
    
    @Override
    public void reloadCountry(String tag) {
        ctryHistoryCache.remove(tag);
        resolver.reset();
    }
    
    @Override
    public void reloadCountryHistory(String tag) {
        ctryHistoryCache.remove(tag);
        resolver.reset();
    }

    @Override
    public void reloadCountries() {
        ctryHistoryCache.clear();
        resolver.reset();
        //preloadCountries();
    }
    
    @Override
    public void reloadProvince(int id) {
        provHistoryCache.remove(id);
        resolver.reset();
    }
    
    @Override
    public void reloadProvinceHistory(int id) {
        provHistoryCache.remove(id);
        resolver.reset();
    }

    @Override
    public void reloadProvinces() {
        provHistoryCache.clear();
        resolver.reset();
        //preloadProvinces(2000); // no way to know what a sensible number is here, and it won't matter much.
    }
    
    @Override
    public void preloadProvinces(int last) {
        preloadProvHistory(1, last);
    }
    
    @Override
    public void preloadCountries() {
        preloadCountryHistory();
    }
    
    private void preloadProvHistory(int start, int end) {
        for (int id = start; id < end; id++) {
            GenericObject hist = provHistoryCache.get(id);
            if (hist == null) {
                final String[] histFiles = resolveProvinceHistoryFiles(id);
                
                if (histFiles == null || histFiles.length == 0 || histFiles[0] == null) {
                    continue;
                }
                
                hist = EUGFileIO.loadAll(histFiles, settings);
                
                if (hist != null) {
                    provHistoryCache.put(id, hist);
                }
            }
        }
    }
    
    private void preloadCountryHistory() {
        GenericObject countries = EUGFileIO.load(resolver.resolveFilename("common/countries.txt"), settings);
        if (countries == null)
            countries = EUGFileIO.loadAll(resolver.listFiles("common/country_tags"), settings);
        
        for (ObjectVariable def : countries.values) {
            String tag = def.varname;
            
            GenericObject hist = ctryHistoryCache.get(tag);
            if (hist == null) {
                final String histFile = resolveCountryHistoryFile(tag);
                
                if (histFile == null) {
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

    protected String[] resolveProvinceHistoryFiles(int id) {
        return resolver.getProvinceHistoryFiles(id);
    }
    
    private static final GenericObject NO_HISTORY = new GenericObject();
    
    /** Gets the province history for the given id, using the cache. */
    private GenericObject getProvHistory(final int id) {
        GenericObject hist = provHistoryCache.get(id);
        if (hist == null) {
            final String[] histFiles = resolveProvinceHistoryFiles(id);
            
            if (histFiles == null || histFiles.length == 0 || histFiles[0] == null) {
                //System.err.println("Cannot find province history file for ID " + id);
                provHistoryCache.put(id, NO_HISTORY);
                return null;
            }
            
            hist = EUGFileIO.loadAll(histFiles, settings);
            
            if (hist == null) {
                System.err.println("Failed to load province history file for ID " + id);
            } else {
                provHistoryCache.put(id, hist);
            }
        } else if (hist == NO_HISTORY) {
            return null;
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
        try (FileReader reader = new FileReader(file)) {
            if (reader.read(data) != data.length)
                System.err.println("???");
            return String.valueOf(data);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    protected final void saveFile(final String filename, final String data) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(data);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isSavedGame() {
        return false;
    }
}
