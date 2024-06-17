package eug.specific.ck3;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzScenario;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of <code>CK3DataSource</code> which gets
 * data from the history files.
 * Unlike older games, CK3 history files require knowledge of other game files
 * to read properly. Titles from common/landed_titles/ must be read before
 * history/provinces, since in most cases only the primary holding as defined in
 * landed titles has any culture/religion data.
 * <p>
 * This brings an additional wrinkle. This data source must be able to 
 * @author Michael Myers
 */
public class CK3Scenario extends ClausewitzScenario implements CK3DataSource {
    
    private final Map<String, GenericObject> titleHistoryCache;
    
    /**
     * Mapping of province ID to which file its history is found in (e.g. province 8289 is found in history/provinces/k_abyssinia.txt).
     */
    private final Map<Integer, String> provinceHistoryFiles;
    
    private final GenericObject landedTitles;
    
    private final Map<String, String> titleHistoryFiles;
    
    /**
     * Mapping of province ID to the ID of the primary holding of its county.
     */
    private final Map<Integer, Integer> primaryHoldings;

    public CK3Scenario(String mainDir, String modDir) {
        super(mainDir, modDir);
        titleHistoryCache = new HashMap<>();
        provinceHistoryFiles = new HashMap<>();
        primaryHoldings = new HashMap<>();
        titleHistoryFiles = new HashMap<>();
        landedTitles = loadTitles(new FilenameResolver(mainDir, modDir, true));
        preloadProvHistory();
    }

    public CK3Scenario(FilenameResolver resolver) {
        super(resolver);
        titleHistoryCache = new HashMap<>();
        provinceHistoryFiles = new HashMap<>();
        primaryHoldings = new HashMap<>();
        titleHistoryFiles = new HashMap<>();
        landedTitles = loadTitles(resolver);
        preloadProvHistory();
    }
    
    private GenericObject loadTitles(FilenameResolver resolver) {
        GenericObject titles = EUGFileIO.loadAllUTF8(resolver.listFiles("common/landed_titles"), ParserSettings.getQuietSettings());
        
        for (GenericObject topLevelTitle : titles.children) {
            loadHoldings(topLevelTitle);
        }
        
        return titles;
    }
    
    private void loadHoldings(GenericObject title) {
        if (title.name.startsWith("c_")) {
            int primary = -1;
            for (GenericObject barony : title.children) {
                if (!barony.name.startsWith("b_"))
                    continue;
                int prov = barony.getInt("province");
                if (primary == -1)
                    primary = prov;
                
                primaryHoldings.put(prov, primary);
            }
        } else if (title.name.charAt(1) == '_') { // some kind of higher title
            for (GenericObject subTitle : title.children)
                loadHoldings(subTitle);
        }
    }
    
    private void preloadProvHistory() {
        for (File f : resolver.listFiles("history/provinces")) {
            GenericObject provs = EUGFileIO.loadUTF8(f, ParserSettings.getQuietSettings());
            for (GenericObject prov : provs.children) {
                try {
                    int id = Integer.parseInt(prov.name);
                    provHistoryCache.put(id, prov);
                    provinceHistoryFiles.put(id, f.getName());
                } catch (NumberFormatException ex) { }
            }
        }
    }

    @Override
    public void reloadProvinces() {
        super.reloadProvinces(); // clears the cache
        provinceHistoryFiles.clear();
    }

    @Override
    public void reloadProvinceHistory(int id) {
        // we don't have a way to discover what file now contains this ID
        // so reload everything
        reloadProvinces();
    }

    @Override
    public void reloadProvince(int id) {
        reloadProvinces();
    }
    
    @Override
    protected String resolveProvinceHistoryFile(int id) {
        return resolver.resolveFilename("history/provinces/" + provinceHistoryFiles.get(id));
    }
    
    /**
     * Since CK3 province history is not in individual files, this method returns
     * the name of the file in which the given province's history is located.
     * @param id the province ID to locate the history file for
     * @return the name of the history file, e.g. "history/provinces/k_abyssinia.txt"
     */
    public String getProvinceHistoryFileName(int id) {
        return provinceHistoryFiles.get(id);
    }

    @Override
    public GenericObject getProvinceHistory(int id) {
        if (provHistoryCache.isEmpty())
            preloadProvHistory();
        return provHistoryCache.get(primaryHoldings.get(id));
    }

    @Override
    public String getProvinceAsStr(int id) {
        try {
            String fileName = resolveProvinceHistoryFile(id);
            return new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    // TODO: save province file:
    // need to resolve the file and place it in the new location if necessary
    // then update provinceHistoryFiles for all affected provinces
//    @Override
//    public void saveProvince(int id, String pname, String data) {
//        super.saveProvince(id, pname, data); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//    }
    
    private void preloadTitleHistory() {
        for (File f : resolver.listFiles("history/titles")) {
            GenericObject titles = EUGFileIO.loadUTF8(f, ParserSettings.getQuietSettings());
            for (GenericObject title : titles.children) {
                titleHistoryCache.put(title.name, title);
                titleHistoryFiles.put(title.name, f.getName());
            }
        }
    }
    

    /** Gets the country history for the given tag, using the cache. */
    private GenericObject _getTitleHistory(final String title) {
        if (titleHistoryCache.isEmpty())
            preloadTitleHistory();
        
        GenericObject hist = titleHistoryCache.get(title);
        if (hist == null) {
            System.err.println("Cannot find title history for " + title);
            return null;
        }
        return hist;
    }

    private String resolveTitleHistoryFile(String title) {
        return resolver.resolveFilename("history/titles/" + titleHistoryFiles.get(title));
    }

    @Override
    public GenericObject getTitle(String title) {
        return _getTitleHistory(title);
    }

    @Override
    public GenericObject getTitleHistory(String title) {
        return _getTitleHistory(title);
    }

    @Override
    public String getTitleAsStr(String title) {
        try {
            String fileName = resolveTitleHistoryFile(title);
            return new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    @Override
    public String getTitleHistoryAsStr(String title) {
        return loadFile(resolveTitleHistoryFile(title));
    }


    @Override
    public void reloadTitle(String title) {
        reloadTitles();
    }

    @Override
    public void reloadTitleHistory(String title) {
        reloadTitles();
    }

    @Override
    public void reloadTitles() {
        titleHistoryCache.clear();
        titleHistoryFiles.clear();
        resolver.reset();
    }
    
    @Override
    public void preloadTitles() {
        preloadTitleHistory();
    }


    @Override
    public void saveTitle(String title, String data) {
        String filename = resolveTitleHistoryFile(title);
        if (filename == null) {
            filename = resolver.resolveFilenameForWrite("history/titles/" + title + ".txt");
        } else {
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveFilenameForWrite("history/titles/" + filename);
        }

        final File file = new File(filename);
        if (file.exists() && saveBackups) {
            final String backupFilename = getBackupFilename(filename);
            if (!file.renameTo(new File(backupFilename)))
                System.err.println("Backup of " + file.getName() + " failed");
        }

        saveFile(filename, data);
    }

}
