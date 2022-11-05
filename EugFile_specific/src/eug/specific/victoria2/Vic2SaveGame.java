

package eug.specific.victoria2;

import eug.parser.EUGFileIO;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import eug.shared.Style;
import eug.specific.clausewitz.ClausewitzSaveGame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Michael
 */
public class Vic2SaveGame extends ClausewitzSaveGame implements Vic2DataSource {

    public Vic2SaveGame(GenericObject root, String savePath, String mainPath, String modName) {
        super(root, savePath, mainPath, modName);
    }

    public Vic2SaveGame(GenericObject root, String savePath, FilenameResolver resolver) {
        super(root, savePath, null, null);
    }

    public static Vic2SaveGame loadSaveGame(String filename, String mainPath, String modName) {
        return new Vic2SaveGame(EUGFileIO.load(filename), filename, mainPath, modName);
    }

    public static Vic2SaveGame loadSaveGame(String filename, FilenameResolver resolver) {
        return new Vic2SaveGame(EUGFileIO.load(filename), filename, resolver);
    }

    @Override
    public GenericObject getProvinceHistory(int id) {
        return getProvince(id);
    }

    @Override
    public String getProvinceHistoryAsStr(int id) {
        return getProvince(id).toString(Style.EU3_SAVE_GAME);
    }

    @Override
    public GenericObject getCountryHistory(String tag) {
        return getCountry(tag);
    }

    @Override
    public String getCountryHistoryAsStr(String tag) {
        return getCountry(tag).toString(Style.EU3_SAVE_GAME);
    }

    @Override
    public List<GenericObject> getPops(int provId) {
        GenericObject province = getProvince(provId);
        // Pops are directly inside the province data
        if (province != null)
            return Collections.singletonList(province);
        else
            return Collections.emptyList();
    }
}
