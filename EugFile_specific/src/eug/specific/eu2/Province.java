/*
 * Province.java
 *
 * Created on June 23, 2006, 7:48 PM
 */

package eug.specific.eu2;

import eug.shared.GenericList;
import eug.shared.GenericObject;

/**
 *
 * @author Michael Myers
 */
public class Province extends EU2SpecificObject {
    
    /** Creates a new instance of Province */
    public Province(GenericObject go, EU2Scenario s) {
        super(go, s);
    }
    
    public int getId() {
        return go.getInt("id");
    }
    
    public String getName() {
        return scenario.provName[getId()];
    }
    
    
    public String getReligion() {
        String rel = go.getString("religion");
        if (!rel.equals(""))
            return rel;
        
        rel = scenario.provReligion[getId()];
        
//        if (rel.equals(""))
//            System.err.println("Error: Province "+getId()+" ("+getName()+") has no religion!");
        
        return rel;
    }
    
    
    public String getCulture() {
        String cul = go.getString("culture");
        if (!cul.equals(""))
            return cul;
        
        cul = scenario.provCulture[getId()];
        
//        if (cul.equals(""))
//            System.err.println("Error: Province "+getId()+" ("+getName()+") has no culture!");
        
        return cul;
    }
    
    public int getIncome() {
        // As far as I can tell, tax and income are always the same.
        int income = go.getInt("income");
        if (income >= 0)
            return income;
        
        income = scenario.provIncome[getId()];
        
        if (income < 0)
            System.err.println("Error: Province "+getId()+" ("+getName()+" has negative income!");
        
        return income;
    }
    
    public int getMine() {
        int mine = go.getInt("mine");
        if (mine >= 0)
            return mine;
        
        return scenario.provMine[getId()];
    }
    
    public String getGoods() {
        int gdsIdx = go.getInt("goods");
        if (gdsIdx >= 0)
            return scenario.goods[gdsIdx];
        
        return scenario.provGoods[getId()];
    }
    
    
    public EU2Country getOwner() {
        int id = getId();
        String sid = go.getString("id");
        
        if (!scenario.provCanHaveOwner(id))
            return null;
        
        for (GenericObject c : scenario.countries)
            if (c.getChild("ownedprovinces").contains(sid))
                return scenario.getCountry(c.getString("tag"));
        
        return null; //not found
    }
    
    public EU2Country getController() {
        int id = getId();
        String sid = go.getString("id");
        
        if (!scenario.provCanHaveOwner(id))
            return null;
        
        for (GenericObject c : scenario.countries)
            if (c.getChild("controlledprovinces").contains(sid))
                return scenario.getCountry(c.getString("tag"));
        
        return null; //not found
    }
    
    public void changeOwner(EU2Country country) {
        int id = go.getInt("id");
        String sid = go.getString("id");
        boolean b = false;
        EU2Country prevOwner = null;
        
        for (int i = 0; i < scenario.numCountries() && !b; i++) {
            GenericList provs = scenario.getCountry(i).go.getList("ownedprovinces");
            for (String owned : provs) {
                if (owned.equals(sid)) {
                    b = true;
                    prevOwner = scenario.getCountry(i);
                    break;
                }
            }
//            for (int j = 0; j < provs.size() && !b; j++) {
//                if (provs.getVariable(j).equals(sid)) {
//                    provs.removeVariable(j);
//                    b = true;
//                    prevOwner = scenario.getCountry(i);
//                }
//            }
        }
        if (b) {
            prevOwner.transferCity(id, country);
        }
//        country.go.getList("ownedprovinces").add(sid, false);
    }
    
    public void changeController(EU2Country country) {
        String sid = go.getString("id");
        boolean b = false;
        EU2Country prevController = null;
        
        for (int i = 0; i < scenario.numCountries() && !b; i++) {
            GenericList provs = scenario.getCountry(i).go.getList("controlledprovinces");
            for (String controlled : provs) {
                if (controlled.equals(sid)) {
                    b = true;
                    prevController = scenario.getCountry(i);
                    break;
                }
            }
//            for (int j = 0; j < provs.size() && !b; j++) {
//                if (provs.getVariable(j).equals(sid)) {
//                    provs.removeVariable(j);
//                    b = true;
//                }
//            }
        }
        if (b) {
            prevController.removeControlledProv(Integer.parseInt(sid));
            country.addControlledProv(Integer.parseInt(sid));
        }
//        country.go.getList("controlledprovinces").add(sid, false);
    }
    
    
    public boolean cultureChanged() {
        return !scenario.provCulture[getId()].equals(go.getString("culture"));
    }
    
    public boolean religionChanged() {
        return !scenario.provReligion[getId()].equals(go.getString("religion"));
    }
}
