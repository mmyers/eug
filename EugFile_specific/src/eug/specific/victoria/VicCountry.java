package eug.specific.victoria;

import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;

public class VicCountry extends VicSpecificObject {
    
    public VicCountry(GenericObject o, VicScenario t) {
        super(o, t);
    }
    
    public String getTag() {
        return go.getString("tag");
    }
    
    public String getName() {
        return scenario.getDisplayName(go.getString("tag"));
    }
    
    public int getCapital() {
        return go.getInt("capital");
    }
    
    public int nbOwned() {
        return go.getChild("ownedprovinces").size();
    }
    
    public Province owned(int pos) {
        GenericObject owned = go.getChild("ownedprovinces");
        
        if (pos >= owned.size()) {
            System.err.println("Invalid pos id: "+pos);
            return null;
        }
        
        String id = owned.getVariable(pos).getValue();
        
        return scenario.getProvince(Integer.parseInt(id));
    }
    
    public int getStatePos(String name) {
        for (int i = 0; i < go.nbChild(); i++) {
            if (go.getChild(i).name.equals("state"))
                if (go.getChild(i).getString("name").equals(name))
                    return i;
        }
        return -1;
    }
    
    public int getProvRGO(int statepos,int provid) {
        GenericObject state=go.getChild(statepos);
        for (int i = 0; i < state.nbChild(); i++) {
            if (state.getChild(i).name.equals("rgo") && state.getChild(i).getInt("location") == provid)
                return i;
            
        }
        return -1;
    }
    
    public boolean ownsProvince(int id) {
        return go.getList("ownedprovinces").contains(Integer.toString(id));
    }
    
    public double getTotalPop() {
        double pop = 0.0;
        final GenericList owned = go.getList("ownedprovinces");
        
        for (String sid : owned) {
            int id = Integer.parseInt(sid);
            pop += scenario.getProvince(id).getTotalPop();
        }
        return pop;
    }
}
