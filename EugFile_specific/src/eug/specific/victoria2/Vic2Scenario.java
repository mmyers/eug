
package eug.specific.victoria2;

import eug.shared.FilenameResolver;
import eug.specific.clausewitz.ClausewitzScenario;

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

}
