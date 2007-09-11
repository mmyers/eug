/*
 * Monarch.java
 *
 * Created on July 14, 2006, 7:52 PM
 */

package eug.specific.eu2;

import eug.shared.GenericObject;

/**
 *
 * @author Michael Myers
 */
public class Monarch extends EU2SpecificObject {
    
    /** Creates a new instance of Monarch */
    public Monarch(GenericObject go, EU2Scenario s) {
        super(go, s);
    }
    public int getId() {
        return go.getChild("id").getInt("id");
    }
    
    public String getName() {
        return go.getString("name");
    }
    
    public int getDip() {
        return go.getInt("DIP");
    }
    
    public int getAdm() {
        return go.getInt("ADM");
    }
    
    public int getMil() {
        return go.getInt("MIL");
    }
    
    public int getTotalSkill() {
        return getDip()+getAdm()+getMil();
    }
    
    public boolean isDormant() {
        return go.getString("dormant").equals("yes");
    }
    
    public int getStartYear() {
        return go.getChild("startdate").getInt("year");
    }
    
    public int getDeathYear() {
        GenericObject deathDate = go.getChild("deathdate");
        if (deathDate == null) {
            deathDate = go.getChild("enddate");
            if (deathDate == null)
                return 2000;
        }
        return deathDate.getInt("year");
    }
    
}
