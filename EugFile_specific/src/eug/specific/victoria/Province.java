package eug.specific.victoria;

import eug.shared.GenericList;
import eug.shared.GenericObject;

public class Province extends VicSpecificObject {
    
    public Province(GenericObject o, VicScenario t) {
        super(o, t);
    }
    
    public int nbPop() {
        int nb = 0;
        
        for (int i = 0; i < go.nbChild(); i++)
            if (go.getChild(i).name.equals("pop"))
                nb++;
        
        return nb;
    }
    
    public int getId() {
        return go.getInt("id");
    }
    
    public Population pop(int pos) {
        
        if (pos >= nbPop())
            return null;
        
        int nb = 0;
        
        for (int i = 0; i < go.nbChild(); i++)
            if (go.getChild(i).name.equals("pop")) {
            if (pos == nb)
                return new Population(go.getChild(i), scenario);
            else
                nb++;
            }
        
        return null;
    }
    
    public void removePop(int pos){
        if (pos >= nbPop())
            return;
        
        for (int i = 0; i < go.nbChild(); i++)
            if (go.getChild(i).name.equals("pop")) {
            if (pos == go.getChild(i).getInt("id"))
                go.getChild(i).children.remove(i);
            }
    }
    
    public void addPop(Population pop){
        go.children.add(pop.go);
    }
    
    public VicCountry getOwner() {
        int id = getId();
        String sid = go.getString("id");
        
        if (scenario.provLandType[id] == 'o')//ocean province
            return null;
        else {
            
            for (int i = 0; i < scenario.numCountries(); i++) {
                GenericList provs = scenario.getCountry(i).go.getList("ownedprovinces");
                for (String owned : provs) {
                    if (owned.equals(sid))
                        return scenario.getCountry(i);
                }
            }
        }
        return null; //not found
    }
    
    public VicCountry getController() {
        int id = getId();
        String sid=go.getString("id");
        
        if (scenario.provLandType[id] == 'o')//ocean province
            return null;
        else {
            
            for (int i = 0; i < scenario.numCountries(); i++) {
                GenericList provs = scenario.getCountry(i).go.getList("controlledprovinces");
                for (String controlled : provs) {
                    if (controlled.equals(sid))
                        return scenario.getCountry(i);
                }
            }
        }
        return null; //not found
    }
    
    public void changeOwner(VicCountry country) {
        int id = go.getInt("id");
        String sid = go.getString("id");
        boolean b = false;
        VicCountry prevOwner = null;
        
        for (int i = 0; i < scenario.numCountries() && !b; i++) {
            GenericList provs=scenario.getCountry(i).go.getList("ownedprovinces");
            for (String owned : provs) {
               if (owned.equals(sid)) {
                    b = true;
                    prevOwner = scenario.getCountry(i);
                    break;
                }
            }
        }
        prevOwner.go.getList("ownedprovinces").delete(sid);
        country.go.getList("ownedprovinces").add(sid, false);
        
        /*
         * Now the tricky part: handling states.
         *
         * This part will only be understandable if you are familiar
         * with the way Victoria handles states and their splits between countries.
         */
        
        int prevStatePos = prevOwner.getStatePos(scenario.provState[id]);
        System.out.println("prev: "+prevStatePos);
        int newStatePos = country.getStatePos(scenario.provState[id]);
        System.out.println("new: "+newStatePos);
        int rgopos;
        
        if (prevStatePos == -1 && newStatePos != -1) {
            //new owner has corresponding state, old one doesn't.
            //we add province to new one, that's all
            country.go.getChild(newStatePos).addString("province", sid);
        } else if (prevStatePos != -1 && newStatePos == -1) {
            //old owner has corresponding state, new one doesn't
            
            System.out.println(scenario.provState[id]+" in "+prevOwner.getName()+
                    ": "+prevOwner.go.getChild(prevStatePos).nbVar("province")+" provs.");
            //first question: does state only have one province?
            if (prevOwner.go.getChild(prevStatePos).nbVar("province") == 1) {
                System.out.println("moving");
                //yes: we move the state in bulk to new owner
                //this include factories
                country.go.addChild(prevOwner.go.getChild(prevStatePos));
                prevOwner.go.removeChild(prevOwner.go.getChild(prevStatePos));
            } else {
                //no: does the province moved have an RGO to move?
                rgopos=prevOwner.getProvRGO(prevStatePos, id);
                
                if (rgopos != -1) {//yes
                    GenericObject newState = country.go.createChild("state");
                    newState.addString("name", scenario.provState[id], true);
                    newState.addString("province",sid);
                    b = false;
                    for (int i = 0; i < prevOwner.go.getChild(prevStatePos).nbVar() && !b; i++) {
                        if (prevOwner.go.getChild(prevStatePos).getVariable(i).varname.equals("province") &&
                                prevOwner.go.getChild(prevStatePos).getVariable(i).getValue().equals(sid)) {
                            prevOwner.go.getChild(prevStatePos).removeVariable(i);
                            b = true;
                        }
                    }
                    //moving rgo:
                    newState.addChild(prevOwner.go.getChild(prevStatePos).getChild(rgopos));
                    prevOwner.go.getChild(prevStatePos).removeChild(prevOwner.go.getChild(prevStatePos).getChild(rgopos));
                }//otherwise, no need to create state
            }
        } else if (prevStatePos != -1 && newStatePos != -1) {
            //both countries have states
            System.out.println(scenario.provState[id]+" in "+prevOwner.getName()+": "+prevOwner.go.getChild(prevStatePos).nbVar("province")+" provs.");
            
//          first question: does the state only have one province?
            if (prevOwner.go.getChild(prevStatePos).nbVar("province") == 1) {
                //yes: we merge it with the new country's state
                System.out.println("merging");
                country.go.getChild(newStatePos).addString("province",sid);
                for (int i = 0; i < prevOwner.go.getChild(prevStatePos).nbChild(); i++) {
                    country.go.getChild(newStatePos).addChild(prevOwner.go.getChild(prevStatePos).getChild(i));
                }
                prevOwner.go.removeChild(prevOwner.go.getChild(prevStatePos));
            } else {
                //no
                //taking care of the "province" tags:
                country.go.getChild(newStatePos).addString("province", sid);
                b = false;
                for (int i = 0; i < prevOwner.go.getChild(prevStatePos).nbVar() && !b; i++) {
                    if (prevOwner.go.getChild(prevStatePos).getVariable(i).varname.equals("province") &&
                            prevOwner.go.getChild(prevStatePos).getVariable(i).getValue().equals(sid)) {
                        System.out.println("removing province tag");
                        prevOwner.go.getChild(prevStatePos).removeVariable(i);
                        b=true;
                    }
                }
                //moving the RGO, if it exists
                rgopos=prevOwner.getProvRGO(prevStatePos, id);
                if (rgopos != -1) {
                    //moving rgo:
                    country.go.getChild(newStatePos).addChild(prevOwner.go.getChild(prevStatePos).getChild(rgopos));
                    prevOwner.go.getChild(prevStatePos).removeChild(prevOwner.go.getChild(prevStatePos).getChild(rgopos));
                }
            }
        }
    }
    
    public void changeController(VicCountry country) {
        String sid = go.getString("id");
        boolean b = false;
        VicCountry prevController = null;
        
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
            prevController.go.getList("controlledprovinces").delete(sid);
            country.go.getList("controlledprovinces").add(sid, false);
        }
    }
    
    public double getTotalPop() {
        double totalPop = 0.0;
        
        for (GenericObject pop : go.getChildren("pop"))
            totalPop += pop.getDouble("size");
        
        return totalPop*100000.0;
    }
}
