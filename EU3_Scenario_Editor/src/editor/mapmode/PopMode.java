
package editor.mapmode;

import editor.MapPanel;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;

/**
 *
 * @author Michael
 */
public abstract class PopMode extends ProvincePaintingMode {

    protected PopMode() {
    }

    protected PopMode(MapPanel panel) {
        super(panel);
    }
    
    // For some reason, pops in saved games don't do religion = "religion" or culture = "culture".
    // Instead, they do culture = religion. But it's always in the third position, so we can reliably access it.
    protected String getCulture(GenericObject pop) {
        if (mapPanel.getDataSource().isSavedGame()) {
            ObjectVariable variable = pop.getVariable(2);
            if (variable == null)
                return "";
            return variable.varname;
        } else {
            return pop.getString("culture");
        }
    }
    protected String getReligion(GenericObject pop) {
        if (mapPanel.getDataSource().isSavedGame()) {
            ObjectVariable variable = pop.getVariable(2);
            if (variable == null)
                return "";
            return variable.getValue();
        } else {
            return pop.getString("religion");
        }
    }
}
