/*
 * EU2Country.java
 *
 * Created on June 23, 2006, 2:23 PM
 */

package eug.specific.eu2;

import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.shared.Utilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Michael Myers
 */
public class EU2Country extends EU2SpecificObject {
    
    public List<GenericObject> cities;
    public List<GenericObject> milUnits;
    
    /**
     * Creates a new instance of EU2Country
     */
    public EU2Country(GenericObject go, EU2Scenario s) {
        super(go, s);
        cities = go.getChildren("city");
        cities.addAll(go.getChildren("tradingpost"));
        initMilUnits();
        
        sortProvLists(); // just for fun
    }
    
    private void initMilUnits() {
        milUnits = go.getChildren("landunit");
        for (GenericObject navalunit : go.getChildren("navalunit")) {
            milUnits.add(navalunit);
            for (GenericObject landunit : navalunit.getChildren("landunit"))
                milUnits.add(landunit);
        }
    }
    
    private final void sortProvLists() {
        go.getList("ownedprovinces").sort();
        go.getList("controlledprovinces").sort();
        go.getList("knownprovinces").sort();
        go.getList("nationalprovinces").sort();
    }
    
    public String getTag() {
        return go.getString("tag");
    }
    
    public void changeTag(String newTag) {
        scenario.changeCountryTag(getTag(), newTag);
        go.setString("tag", newTag);
    }
    
    public String getName() {
        return scenario.getDisplayName(getTag());
    }
    
    public int getCapital() {
        for (GenericObject city : cities) {
            if (!city.hasString("capital"))
                return city.getInt("location");
        }
        return -1;  // will never happen except for REB, PIR, MER, and NAT
    }
    
    public String getReligion() {
        return go.getChild("religion").getString("type");
    }
    
    public List<String> getCulture() {
        final GenericObject cultures = go.getChild("culture");
        final List<String> cul = new ArrayList<String>(cultures.nbVar());
        
        for (ObjectVariable culture : cultures.values)
            cul.add(culture.getValue());
        
        return cul;
    }
    
    
    public double getTreasury() {
        return go.getDouble("treasury");
    }
    
    public double getInflation() {
        return go.getDouble("inflation");
    }
    
    
    public double getVP() {
        return go.getDouble("vp");
    }
    
    
    public double getBadBoy() {
        return go.getDouble("badboy");
    }
    
    
    public int nbOwned() {
        return go.getList("ownedprovinces").size();
    }
    
    public Province owned(int pos) {
        final GenericList owned = go.getList("ownedprovinces");
        
        if (pos >= owned.size()) {
            System.err.println("Invalid pos id: "+pos);
            return null;
        }
        
        return scenario.getProvince(Integer.parseInt(owned.get(pos)));
    }
    
    public boolean ownsProvince(int id) {
        return go.getList("ownedprovinces").contains(Integer.toString(id));
    }
    
    
    public int nbControlled() {
        return go.getList("controlledprovinces").size();
    }
    
    public boolean controllsProvince(int id) {
        try {
            return go.getList("controlledprovinces").contains(Integer.toString(id));
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("Error in country " + getTag());
            throw ex;
        }
    }
    
    
    public boolean isSameCulture(Province prov) {
        final List<String> cultures = getCulture();
        
        final String provCul = prov.getCulture();
        
        for (String culture : cultures) {
            if (culture.equals(provCul))
                return true;
        }
        
        return false;
    }
    
    public boolean isSameReligion(Province prov) {
        return getReligion().equals(prov.getReligion());
    }
    
    public boolean isCore(Province prov) {
        return isCore(prov.getId());
    }
    
    public boolean isCore(int id) {
        return go.getList("nationalprovinces").contains(Integer.toString(id));
    }
    
    
    public double getPopulation(int id) {
        double pop = getCity(id).getDouble("population");
        if (pop < 0.0)
            return 0.0;
        return pop;
    }
    
    
    public boolean hasBailiff(int id) {
        return getCity(id).getString("bailiff").equals("yes");
    }
    
    public boolean hasJudge(int id) {
        return getCity(id).getString("courthouse").equals("yes");
    }
    
    public boolean hasGovernor(int id) {
        return getCity(id).getString("cityrights").equals("yes");
    }
    
    public boolean hasFactory(int id) {
        return getCity(id).hasString("manufactory");
    }
    
    public boolean hasShipyard(int id) {
        return getCity(id).getString("shipyard").equals("yes");
    }
    
    public boolean hasBarrack(int id) {
        return getCity(id).getString("barrack").equals("yes");
    }
    
    public int getFortressLevel(int id) {
        return getFortress0(getCity(id));
    }
    
    private static int getFortress0(GenericObject city) {
        try {
            // just typecast, since there should only be whole-number levels
            return (int) city.getChild("fortress").getDouble("level");
        } catch (NullPointerException ex) {
            // There wasn't a fortress, so the level is 0
            return 0;
        }
    }
    
    public List<Integer> getRichProvs() {
        final List<Integer> ret = new ArrayList<Integer>();
//        final List<String> cultures = getCulture();
//        final String religion = getReligion();
        final int MIN_TAX = 10;
        final int MIN_GOLD = 10;
        final String gold = scenario.getDisplayName("gold");
        
        for (GenericObject city : cities) {
            Province prov = scenario.getProvince(city.getInt("location"));
            
            if ( ( prov.getIncome() >= MIN_TAX ||
                    (prov.getGoods().equals(gold) && prov.getMine() >= MIN_GOLD) )
//                  && religion.equals(prov.getReligion())
//                  && cultures.contains(prov.getCulture())
                    )
                ret.add(city.getInt("location"));
        }
        
        sortByTax(ret);
        return ret;
    }
    
    public List<Integer> getCitiesLowFortress(int max) {
        final List<Integer> ret = new ArrayList<Integer>();
        
        for (GenericObject city : cities)
            if (getFortress0(city) <= max)
                ret.add(city.getInt("location"));
        
        return ret;
    }
    
    public List<Integer> getVulnerableRichProvs() {
        List<Integer> ret = getRichProvs();
        ret.retainAll(getCitiesLowFortress(2));
        return ret;
    }
    
    public List<Integer> getFavorableCitiesNoBailiff() {
        final List<Integer> ret = new ArrayList<Integer>();
        final List<String> cultures = getCulture();
        final String religion = getReligion();
        final int MIN_TAX = 5;
        
        for (GenericObject city : cities) {
            Province prov = scenario.getProvince(city.getInt("location"));
            
            if (cultures.contains(prov.getCulture()) &&
                    religion.equals(prov.getReligion()) &&
                    !city.getString("bailiff").equals("yes") &&
                    prov.getIncome() >= MIN_TAX)
                ret.add(city.getInt("location"));
        }
        
        return ret;
    }
    
    public List<Integer> getBailiffNoJudge() {
        final List<Integer> ret = new ArrayList<Integer>();
        
        for (GenericObject city : cities) {
            if (city.getString("bailiff").equals("yes") &&
                    !city.getString("courthouse").equals("yes"))
                ret.add(city.getInt("location"));
        }
        
        return ret;
    }
    
    public List<Integer> getBailiffNoJudge(int minTaxVal) {
        final List<Integer> ret = new ArrayList<Integer>();
        
        for (GenericObject city : cities) {
            int loc = city.getInt("location");
            
            if (city.getString("bailiff").equals("yes") &&
                    !city.getString("courthouse").equals("yes") &&
                    scenario.getProvince(loc).getIncome() >= minTaxVal)
                ret.add(loc);
        }
        
        return ret;
    }
    
    public List<Integer> getOwnedProvs() {
        final List<Integer> ret = new ArrayList<Integer>();
        for (String id : go.getList("ownedprovinces"))
            ret.add(Integer.parseInt(id));
        return ret;
    }
    
    public List<Integer> getControlledProvs() {
        final List<Integer> ret = new ArrayList<Integer>();
        for (String id : go.getList("controlledprovinces"))
            ret.add(Integer.parseInt(id));
        return ret;
    }
    
    public List<Integer> getNationalProvs() {
        final List<Integer> ret = new ArrayList<Integer>();
        for (String id : go.getList("nationalprovinces"))
            ret.add(Integer.parseInt(id));
        return ret;
    }
    
    public List<Integer> getOwnedNotControlled() {
        final List<Integer> ret = getOwnedProvs();
        ret.removeAll(getControlledProvs());
        return ret;
    }
    
    public List<Integer> getOwnedNotCore() {
        final List<Integer> ret = getOwnedProvs();
        ret.removeAll(getNationalProvs());
        return ret;
    }
    
    public List<Integer> getControlledNotOwned() {
        final List<Integer> ret = getControlledProvs();
        ret.removeAll(getOwnedProvs());
        return ret;
    }
    
    
    public String getFactory(int id) {
        return getCity(id).getString("manufactory");
    }
    
    public java.util.Map<Integer, String> getFactories() {
        final java.util.Map<Integer, String> ret =
                new java.util.HashMap<Integer, String>();
        
        String tmp;
        
        for (GenericObject city : cities) {
            tmp = city.getString("manufactory");
            
            if (tmp.length() != 0)
                ret.put(city.getInt("location"), tmp);
        }
        
        return ret;
    }
    
    
    public List<Integer> getShipyards() {
        final List<Integer> ret = new ArrayList<Integer>();
        for (GenericObject city : cities) {
            if (city.getString("shipyard").equals("yes"))
                ret.add(city.getInt("location"));
        }
        return ret;
    }
    
    
    public List<Integer> getBarracks() {
        final List<Integer> ret = new ArrayList<Integer>();
        for (GenericObject city : cities) {
            if (city.getString("barrack").equals("yes"))
                ret.add(city.getInt("location"));
        }
        return ret;
    }
    
    
    private final GenericObject getCity(int id) {
        for (GenericObject obj : cities)
            if (Integer.parseInt(obj.getString("location")) == id)
                return obj;
        
        return null;
    }
    
    
    public double getRelation(String tag) {
        tag = tag.substring(0,3).toUpperCase();
        
        GenericObject diplomacy = go.getChild("diplomacy");
        
        for (GenericObject relation : diplomacy.children) {
            if (relation.getString("tag").equals(tag))
                return relation.getDouble("value");
        }
        
        return 0.0;     // What is the default relation??
    }
    
    // *** Military ***
    
    // Army
    
    public double numInf() {
        double inf = 0.0;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("landunit"))
                inf += unit.getDouble("inf");
            else {  // naval
                for (GenericObject unit2 : unit.getChildren("landunit")) {
                    inf += unit2.getDouble("inf");
                }
            }
        }
        
        return inf;
    }
    
    public double numCav() {
        double cav = 0.0;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("landunit"))
                cav += unit.getDouble("cav");
            else {  // naval
                for (GenericObject unit2 : unit.getChildren("landunit"))
                    cav += unit2.getDouble("cav");
            }
        }
        
        return cav;
    }
    
    public double numArt() {
        double art = 0.0;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("landunit"))
                art += unit.getDouble("art");
            else {  // naval
                for (GenericObject unit2 : unit.getChildren("landunit"))
                    art += unit2.getDouble("art");
            }
        }
        
        return art;
    }
    
    
    public double numLandTroops() {
        double num = 0.0;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("landunit")) {
                num += unit.getDouble("inf");
                num += unit.getDouble("cav");
                num += 100*unit.getDouble("art");
            } else {  // naval
                for (GenericObject unit2 : unit.getChildren("landunit")) {
                    num += unit2.getDouble("inf");
                    num += unit2.getDouble("cav");
                    num += 100*unit2.getDouble("art");
                }
            }
        }
        
        return num;
    }
    
    // Navy
    
    public double numWarships() {
        double ws = 0.0;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("navalunit"))
                ws += unit.getDouble("warships");
        }
        
        return ws;
    }
    
    public double numGalleys() {
        double gl = 0.0;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("navalunit"))
                gl += unit.getDouble("galleys");
        }
        
        return gl;
    }
    
    public double numTransports() {
        double tr = 0.0;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("navalunit"))
                tr += unit.getDouble("transports");
        }
        
        return tr;
    }
    
    
    public double numShips() {
        double num = 0.0;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("navalunit")) {
                num += unit.getDouble("warships");
                num += unit.getDouble("galleys");
                num += unit.getDouble("transports");
            }
        }
        
        return num;
    }
    
    // Miscellaneous
    
    public double getTotalPopulation() {
        // Trading posts are not included. Should they be?
        double pop = 0.0;
        for (GenericObject city : cities)
            pop += city.getDouble("population");
        return pop;
    }
    
    // Technology
    
    public int getLandTech() {
        return getTech("land");
    }
    
    public int getNavalTech() {
        return getTech("naval");
    }
    
    public int getTradeTech() {
        return getTech("trade");
    }
    
    public int getInfraTech() {
        return getTech("infra");
    }
    
    public int getStability() {
        return getTech("stability");
    }
    
    public int getTech(String type) {
        return go.getChild("technology").getChild(type).getInt("level");
    }
    
    public String getTechGroup() {
        return go.getChild("technology").getString("group");
    }
    
    
    public int getPolicyValue(String type) {
        return go.getChild("policy").getInt(type);
    }
    
    
//    public static GenericObject createCountry(String tag, EU2Scenario s) {
//        final GenericObject ctry = new GenericObject(s.go, "country");
//
//        ctry.a
//
//        return new EU2Country(ctry, s);
//    }
    
    
    public void printInfo() {
        System.out.println();
        System.out.println(getName() + " has " + numInf() + " infantry, " +
                numCav() + " cavalry, and " + numArt() + " artillery.");
        
        System.out.println("It supports " + numLandTroops() +
                " land troops and " + numShips() + " ships with " +
                Utilities.formatPop(getTotalPopulation()) + " population.");
        
        System.out.println();
        
        System.out.println("Same-religion, same-culture cities with base tax >= 5 and no bailiff:");
        printList(getFavorableCitiesNoBailiff());
        
        System.out.println();
        
        if (getInfraTech() >= 4) {
            System.out.println("Provinces with base tax >= 10 with bailiff but no judge:");
            printList(getBailiffNoJudge(10));
        }
        
        System.out.println();
        
        System.out.println("Vulnerable rich cities:");
        printDetailedList(getVulnerableRichProvs());
        
        System.out.println();
        
        System.out.println("Owned but not controlled:");
        printList(getOwnedNotControlled());
        
        System.out.println();
        
        System.out.println("Controlled but not owned");
        printList(getControlledNotOwned());
        
        System.out.println();
    }
    
    private final java.util.Comparator<Integer> SORT_BY_TAX = new java.util.Comparator<Integer>() {
        public int compare(Integer i1, Integer i2) {
            return Integer.valueOf(scenario.getProvince(i1).getIncome())
            .compareTo(Integer.valueOf(scenario.getProvince(i2).getIncome()));
        }
    };
    
    public List<Integer> sortByTax(List<Integer> ids) {
        java.util.Collections.sort(ids, SORT_BY_TAX);
        return ids;
    }
    
    private void printList(List<Integer> ids) {
        System.out.format("%4s %12s %10s %10s%n", "ID", "Name", "Income", "Goods");
        sortByTax(ids);
        for (int id : ids) {
            Province prov = scenario.getProvince(id);
            System.out.format("%4d %12s %2d %17s%n",
                    prov.getId(), prov.getName(), prov.getIncome(), prov.getGoods());
        }
    }
    
    private void printDetailedList(List<Integer> ids) {
        System.out.format("%4s %12s %10s %10s %12s %10s%n", "ID", "Name", "Income", "Goods", "Population", "Fortress level");
        sortByTax(ids);
        for (int id : ids) {
            Province prov = scenario.getProvince(id);
            GenericObject city = getCity(id);
            System.out.format("%4d %12s %2d %17s %8.3f %2d%n",
                    prov.getId(), prov.getName(), prov.getIncome(),
                    prov.getGoods(), city.getDouble("population"),
                    getFortress0(city));
        }
    }
    
    
    
    
    //
    // *** MUTATORS ***
    //
    
    
    public void setTech(String type, int level) {
        go.getChild("technology").getChild(type).setInt("level", level);
    }
    
    public void setLandTech(int level) {
        setTech("land", level);
    }
    
    public void setNavalTech(int level) {
        setTech("naval", level);
    }
    
    public void setInfraTech(int level) {
        setTech("infra", level);
    }
    
    public void setTradeTech(int level) {
        setTech("trade", level);
    }
    
    public void setStability(int level) {
        setTech("stability", level);
    }
    
    
    public void addUnit(GenericObject unit) {
        go.addChild(unit);
        milUnits.add(unit);
    }
    
    public void removeUnit(GenericObject unit) {
        go.removeChild(unit);
        milUnits.remove(unit);
    }
    
    
    public GenericObject removeCity(int id) {
        String sid = Integer.toString(id);
        if (!go.getList("ownedprovinces").delete(sid))
            throw new RuntimeException("tried to remove province "+sid+" from "+getName()+", which didn't own it!");
        
        go.getList("controlledprovinces").delete(sid);
        
        for (int i = 0; i < cities.size(); i++) {
            GenericObject city = cities.get(i);
            if (city.getString("location").equals(sid)) {
                cities.remove(i);
                go.removeChild(city);
                return city;
            }
        }
        
        throw new RuntimeException("couldn't find a city in province "+sid);
    }
    
    public void addCity(GenericObject city) {
        go.getList("ownedprovinces").add(city.getInt("location"));
        if (!go.getList("controlledprovinces").contains(city.getString("location")))
            go.getList("controlledprovinces").add(city.getInt("location"));
        cities.add(city);
        go.addChild(city);
    }
    
    public void addControlledProv(int id) {
        go.getList("controlledprovinces").add(id);
    }
    
    public void removeControlledProv(int id) {
        go.getList("controlledprovinces").delete(Integer.toString(id));
    }
    
    public void addNationalProv(int id) {
        go.getList("nationalprovinces").add(id);
    }
    
    public void removeNationalProv(int id) {
        go.getList("nationalprovinces").delete(Integer.toString(id));
    }
    
    
    //
    // *** MERGING ***
    //
    
    
    private static final String[] techs = { "land", "naval", "trade", "infra"/*, "stability" */};
    
    public void merge(EU2Country other) {
        for (String tech : techs)
            setTech(tech, (getTech(tech) + other.getTech(tech)) / 2);
        
        other.transferAllUnitsTo(this);
        other.transferAllCitiesTo(this);
        
        go.getList("nationalprovinces").addAll(other.go.getList("nationalprovinces"));
        
        scenario.removeCountry(other);
    }
    
    public void mergeUnits() {
        java.util.Map<Integer, List<GenericObject>> landLoc =
                new java.util.HashMap<Integer, List<GenericObject>>(milUnits.size());
        
        java.util.Map<Integer, List<GenericObject>> navalLoc =
                new java.util.HashMap<Integer, List<GenericObject>>(milUnits.size());
        
        int location;
        
        for (GenericObject unit : milUnits) {
            if (unit.name.equals("landunit")) {
                location = unit.getInt("location");
                
                if (landLoc.get(location) == null)
                    landLoc.put(location, new ArrayList<GenericObject>());
                
                landLoc.get(location).add(unit);
            } else {
                location = unit.getInt("location");
                
                if (navalLoc.get(location) == null)
                    navalLoc.put(location, new ArrayList<GenericObject>());
                
                navalLoc.get(location).add(unit);
            }
        }
        
        GenericObject base;
        double inf, cav, art;
        boolean first;
        
        for (List<GenericObject> list : landLoc.values()) {
            if (list.size() <= 1)
                continue;
            
            base = list.get(0);
            inf = 0.0; cav = 0.0; art = 0.0;
            first = true;
            
            for (GenericObject tmp : list) {
                inf += tmp.getDouble("inf");
                cav += tmp.getDouble("cav");
                art += tmp.getDouble("art");
                
                if (first)
                    first = false;
                else
                    go.removeChild(tmp);
            }
            
            base.setDouble("inf", inf);
            base.setDouble("cav", cav);
            base.setDouble("art", art);
        }
        
        
        double warships, galleys, transports;
        GenericObject tmpTrans;
        
        for (List<GenericObject> list : navalLoc.values()) {
            if (list.size() <= 1)
                continue;
            
            tmpTrans = null;
            
            base = list.get(0);
            warships = 0.0; galleys = 0.0; transports = 0.0;
            first = true;
            
            for (GenericObject tmp : list) {
                warships += tmp.getDouble("warships");
                galleys += tmp.getDouble("galleys");
                transports += tmp.getDouble("transports");
                
                if (tmpTrans == null)
                    tmpTrans = tmp.getChild("landunit");
                
                if (first)
                    first = false;
                else
                    go.removeChild(tmp);
            }
            
            base.setDouble("warships", warships);
            base.setDouble("galleys", galleys);
            base.setDouble("transports", transports);
            
            if (tmpTrans != null)
                base.addChild(tmpTrans);
        }
        
        initMilUnits();
    }
    
    public void transferAllUnitsTo(EU2Country other) {
        for (GenericObject unit : milUnits) {
            other.addUnit(unit);
            go.removeChild(unit);
        }
        this.initMilUnits();
        other.initMilUnits();
    }
    
    public void transferCity(int id, EU2Country other) {
        GenericObject city = getCity(id);
        other.addCity(city);
        go.removeChild(city);
	cities.remove(city);
        
        go.getList("ownedprovinces").delete(Integer.toString(id));
        go.getList("controlledprovinces").delete(Integer.toString(id));
    }
    
    public void transferAllCitiesTo(EU2Country other) {
        for (GenericObject city : cities) {
            other.addCity(city);
            go.removeChild(city);
            
            String loc = city.getString("location");
            if (!go.getList("ownedprovinces").delete(loc))
                System.err.println("tried to remove province "+loc+" from "+getName()+", which didn't own it!");
            
            go.getList("controlledprovinces").delete(loc);
        }
    }
}
