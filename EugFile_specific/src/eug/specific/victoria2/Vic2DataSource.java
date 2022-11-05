
package eug.specific.victoria2;

import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzDataSource;
import java.util.List;

/**
 * Shared interface for Victoria 2 and 3 specific data, primarily pops.
 * @author Michael
 */
public interface Vic2DataSource extends ClausewitzDataSource {
    
    public List<GenericObject> getPops(int provId);
}
