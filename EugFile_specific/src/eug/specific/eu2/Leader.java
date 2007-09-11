/*
 * Leader.java
 *
 * Created on July 14, 2006, 6:17 PM
 */

package eug.specific.eu2;

import eug.shared.GenericObject;

/**
 *
 * @author Michael Myers
 */
public final class Leader extends EU2SpecificObject {
    
    /** Creates a new instance of Leader */
    public Leader(GenericObject go, EU2Scenario s) {
        super(go, s);
    }
    
    public int getId() {
        return go.getChild("id").getInt("id");
    }
    
    public String getCategory() {
        return go.getString("category");
    }
    
    public String getName() {
        return go.getString("name");
    }
    
    public int getRank() {
        return go.getInt("rank");
    }
    
    public int getMovement() {
        return go.getInt("movement");
    }
    
    public int getFire() {
        return go.getInt("fire");
    }
    
    public int getShock() {
        return go.getInt("shock");
    }
    
    public int getSiege() {
        final int siege = go.getInt("siege");
        return (siege == -1 ? 0 : siege);
    }
    
    public int getTotalSkill() {
        return getMovement()+getFire()+getShock()+getSiege();
    }
    
    public boolean isDormant() {
        return go.getString("dormant").equals("yes");
    }
    
    public int getStartYear() {
        return go.getChild("startdate").getInt("year");
    }
    
    public int getDeathYear() {
        GenericObject deathDate = go.getChild("deathdate");
        if (deathDate == null)
            deathDate = go.getChild("enddate");
        return deathDate.getInt("year");
    }
}
