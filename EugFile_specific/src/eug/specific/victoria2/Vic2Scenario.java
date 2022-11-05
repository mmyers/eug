
package eug.specific.victoria2;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzScenario;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class Vic2Scenario extends ClausewitzScenario implements Vic2DataSource {
    
    private List<GenericObject> allPops;

    public Vic2Scenario(String mainDir, String modDir) {
        super(mainDir, modDir);
    }

    public Vic2Scenario(FilenameResolver resolver) {
        super(resolver);
    }

    @Override
    protected String resolveProvinceHistoryFile(int id) {
        return resolver.getVic2ProvinceHistoryFile(id);
    }

    @Override
    protected String[] resolveProvinceHistoryFiles(int id) {
        return new String[] { resolveProvinceHistoryFile(id) };
    }

    @Override
    public void saveProvince(int id, String pname, String data) {
        String filename = resolveProvinceHistoryFile(id);
        if (filename == null) {
            // how do we decide which subfolder to put the new file in?
            // just put it in the main provinces directory for now (still works)
            filename = resolver.resolveFilenameForWrite("history/provinces/" + id + " - " + pname + ".txt");
        } else {
            String subfolder = new File(filename).getParentFile().getName();
            if (!subfolder.equalsIgnoreCase("provinces"))
                subfolder = "provinces" + File.separator + subfolder;
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveFilenameForWrite("history/" + subfolder + filename);
        }

        final File file = new File(filename);
        if (file.exists()) {
            final String backupFilename = getBackupFilename(filename);
            if (!file.renameTo(new File(backupFilename)))
                System.err.println("Backup of " + file.getName() + " failed");
        }

        saveFile(filename, data);
    }
    
    private void loadPops() {
        allPops = new ArrayList<>();
        File[] popsFilesOrFolders = resolver.listFiles("history/pops");
        for (File f : popsFilesOrFolders) {
            getPops(f, allPops);
        }
    }
    
    @Override
    public List<GenericObject> getPops(int provId) {
        if (allPops == null)
            loadPops();
        
        String provIdStr = Integer.toString(provId);
        List<GenericObject> ret = new ArrayList<>();
        for (GenericObject obj : allPops) {
            ret.addAll(obj.getChildren(provIdStr));
        }
        
        return ret;
    }
    
    private void getPops(File dir, List<GenericObject> ret) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory())
                getPops(f, ret);
            else
                ret.add(EUGFileIO.load(f, ParserSettings.getQuietSettings()));
        }
    }

}
