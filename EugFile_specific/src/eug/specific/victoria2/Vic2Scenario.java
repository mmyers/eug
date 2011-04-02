
package eug.specific.victoria2;

import eug.shared.FilenameResolver;
import eug.specific.clausewitz.ClausewitzScenario;
import java.io.File;

/**
 *
 * @author Michael
 */
public class Vic2Scenario extends ClausewitzScenario {

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
    public void saveProvince(int id, String pname, String data) {
        String filename = resolveProvinceHistoryFile(id);
        if (filename == null) {
            // how do we decide which subfolder to put the new file in?
            // just put it in the main provinces directory for now (still works)
            filename = resolver.resolveDirectory("history") + "provinces/" +
                    id + " - " + pname + ".txt";
        } else {
            String subfolder = new File(filename).getParentFile().getName();
            if (!subfolder.equalsIgnoreCase("provinces"))
                subfolder = "provinces/" + subfolder;
            filename = filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')));
            filename = resolver.resolveDirectory("history") + subfolder + filename;
        }

        final File file = new File(filename);
        if (file.exists()) {
            final String backupFilename = getBackupFilename(filename);
            if (!file.renameTo(new File(backupFilename)))
                System.err.println("Backup of " + file.getName() + " failed");
        }

        saveFile(filename, data);
    }

}
