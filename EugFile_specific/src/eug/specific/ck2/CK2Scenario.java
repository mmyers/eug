package eug.specific.ck2;

import eug.parser.EUGFileIO;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzScenario;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of <code>CK2DataSource</code> which gets
 * data from the history files.
 * @author Michael Myers
 * @since EUGFile 1.06.00pre1
 */
public class CK2Scenario extends ClausewitzScenario implements CK2DataSource {
    
    private final Map<String, GenericObject> titleHistoryCache;

    public CK2Scenario(String mainDir, String modDir) {
        super(mainDir, modDir);
        titleHistoryCache = new HashMap<>();
    }

    public CK2Scenario(FilenameResolver resolver) {
        super(resolver);
        titleHistoryCache = new HashMap<>();
    }

    /** Gets the country history for the given tag, using the cache. */
    private GenericObject _getTitleHistory(final String title) {
        GenericObject hist = titleHistoryCache.get(title);
        if (hist == null) {
            final String histFile = resolveTitleHistoryFile(title);

            if (histFile == null) {
                System.err.println("Cannot find title history file for " + title);
                return null;
            }

            hist = EUGFileIO.load(histFile, settings);

            if (hist == null) {
                System.err.println("Failed to load title history file for " + title);
            } else {
                titleHistoryCache.put(title, hist);
            }
        }
        return hist;
    }

    private String resolveTitleHistoryFile(String title) {
        return resolver.resolveFilename("history/titles/" + title + ".txt");
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
        return loadFile(resolveTitleHistoryFile(title));
    }

    @Override
    public String getTitleHistoryAsStr(String title) {
        return loadFile(resolveTitleHistoryFile(title));
    }


    @Override
    public void reloadTitle(String title) {
        titleHistoryCache.remove(title);
        resolver.reset();
    }

    @Override
    public void reloadTitleHistory(String title) {
        titleHistoryCache.remove(title);
        resolver.reset();
    }

    @Override
    public void reloadTitles() {
        titleHistoryCache.clear();
        resolver.reset();
        //preloadTitles(); // not implemented
    }


    @Override
    public void saveTitle(String title, String data) {
        String filename = resolveTitleHistoryFile(title);
        if (filename == null) {
            filename = resolver.resolveDirectory("history") + "titles/" +
                    title + ".txt";
        } else {
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveDirectory("history") + "titles/" + filename;
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
