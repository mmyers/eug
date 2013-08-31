package eug.specific.ck2;

import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzSaveGame;

/**
 *
 * @author Michael
 */
public class CK2SaveGame extends ClausewitzSaveGame implements CK2DataSource {

    public CK2SaveGame(GenericObject root, String savePath, String mainPath, String modName) {
        super(root, savePath, mainPath, modName);
    }

    // TODO

    
    public GenericObject getTitle(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GenericObject getTitleHistory(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTitleAsStr(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getTitleHistoryAsStr(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void reloadTitle(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void reloadTitleHistory(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void reloadTitles() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveTitle(String title, String data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
