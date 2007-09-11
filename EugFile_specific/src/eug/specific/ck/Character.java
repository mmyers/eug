/*
 * Character.java
 *
 * Created on July 13, 2006, 2:01 PM
 */

package eug.specific.ck;

import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Myers
 */
public class Character extends CKSpecificObject {
    
    /** Creates a new instance of Character */
    public Character(GenericObject go, CKScenario s) {
        super(go, s);
    }
    
    public String getName() {
        return go.getString("name");
    }
    
    public String getTag() {
        return go.getString("tag");
    }
    
    public String getReligion() {
        return go.getString("religion");
    }
    
    public String getCulture() {
        return go.getString("culture");
    }
    
    public double getAttribute(String attr) {
        return go.getChild("attributes").getDouble(attr);
    }
    
    
    public double getMartial() {
        return getAttribute("martial");
    }
    
    public double getDiplomacy() {
        return getAttribute("diplomacy");
    }
    
    public double getIntrigue() {
        return getAttribute("intrigue");
    }
    
    public double getStewardship() {
        return getAttribute("stewardship");
    }
    
    public double getHealth() {
        return getAttribute("health");
    }
    
    public double getFertility() {
        return getAttribute("fertility");
    }
    
    public List<String> getTraits() {
        final GenericObject traits = go.getChild("traits");
        final List<String> ret = new ArrayList<String>(traits.size());
        
        for (ObjectVariable var : traits.values)
            ret.add(var.varname);
        
        return ret;
    }
    
    public boolean hasTrait(String trait) {
        return go.getChild("traits").contains(trait);
    }
    
    /**
     * Note: This method does not check whether a contradictory trait
     * is already present.
     */
    public void addTrait(String type) {
        go.getChild("traits").setString(type, "yes", false);
    }
    
    public boolean isDead() {
        return go.contains("deathdate");
    }
}
