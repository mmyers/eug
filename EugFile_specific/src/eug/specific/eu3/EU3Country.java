/*
 * EU3Country.java
 *
 * Created on March 22, 2007, 6:14 PM
 */

package eug.specific.eu3;

import eug.shared.GenericObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Myers
 * @since EUGFile 1.03.00
 */
public class EU3Country extends EU3SpecificObject {
    
    private GenericObject history = null;
    
    private List<GenericObject> armies = null;
    private List<GenericObject> navies = null;
    
    /** Creates a new instance of EU3Country */
    public EU3Country(GenericObject go, EU3SaveGame s) {
        super(go, s);
    }
    
    // Lazy accessors, to reduce object creation time
    
    private GenericObject getHistory() {
        if (history == null) {
            history = go.getChild("history");
        }
        return history;
    }
    
    private List<GenericObject> getArmies() {
        if (armies == null) {
            armies = go.getChildren("army");
        }
        return armies;
    }
    
    private List<GenericObject> getNavies() {
        if (navies == null) {
            navies = go.getChildren("navy");
        }
        return navies;
    }
    
    // End of lazy accessors
    
    public String getTag() {
        return go.name;
    }
    
    public void changeTag(String newTag) {
        scenario.changeCountryTag(getTag(), newTag);
        go.name = newTag.toUpperCase();
    }
    
    public int getCapital() {
        return go.getInt("capital");
    }
    
    public String getGovernmentType() {
        return go.getString("government");
    }
    
    public int getSliderValue(String sliderName) {
        return go.getInt(sliderName);
    }
    
    public String getPrimaryCulture() {
        return go.getString("primary_culture");
    }
    
    public List<String> getSecondaryCultures() {
        return go.getStrings("secondary_culture");
    }
    
    public String getReligion() {
        return go.getString("religion");
    }
    
    public String getTechnologyGroup() {
        return go.getString("technology_group");
    }
    
    public int getTechnologyLevel(String techName) {
        return Integer.parseInt(go.getChild("technology").getList(techName).get(0));
    }
    
    public double getPrestige() {
        return go.getDouble("prestige");
    }
    
    public double getTreasury() {
        return go.getDouble("treasury");
    }
    
    public double getInflation() {
        return go.getDouble("inflation");
    }
    
    public double getArmyTradition() {
        return go.getDouble("army_tradition");
    }
    
    public double getNavyTradition() {
        return go.getDouble("navy_tradition");
    }
    
    public double getBadBoy() {
        return go.getDouble("badboy");
    }
    
    public boolean hasNationalIdea(String name) {
        return go.getString(name).equals("yes");
    }
    
    public double getManpower() {
        return go.getDouble("manpower");
    }
    
    /**
     * @param type One of
     * <ul>
     * <li>infantry
     * <li>cavalry
     * <li>artillery
     * <li>big_ship
     * <li>light_ship
     * <li>galley
     * <li>transport</ul>
     */
    public String getModelName(String type) {
        return go.getString(type);
    }
    
    public int getRelation(String tag) {
        return go.getChild(tag).getInt("value");
    }
    

    public int getPolicyValue(String policy) {
        return go.getInt(policy);
    }
    
    public boolean ownsProvince(int id) {
        return scenario.getEU3Province(id).getOwner().equals(getTag());
    }
    
    public boolean controllsProvince(int id) {
        return scenario.getEU3Province(id).getOwner().equals(getTag());
    }
    
    public boolean isCore(int id) {
        return scenario.getEU3Province(id).isCoreOf().contains(getTag());
    }
    

    public int numOwned() {
        int ret = 0;
        final String tag = getTag();
        for (int i = 1; i <= scenario.getLastProvID(); i++) {
            EU3Province prov = scenario.getEU3Province(i);
            if (prov.getOwner().equals(tag))
                ret++;
        }
        return ret;
    }
    
    public List<EU3Province> getOwned() {
        final List<EU3Province> owned = new ArrayList<EU3Province>();
        final String tag = getTag();
        for (int i = 1; i <= scenario.getLastProvID(); i++) {
            EU3Province prov = scenario.getEU3Province(i);
            if (prov.getOwner().equals(tag))
                owned.add(prov);
        }
        return owned;
    }
    
    public List<EU3Province> getControlled() {
        final List<EU3Province> controlled = new ArrayList<EU3Province>();
        final String tag = getTag();
        for (int i = 1; i <= scenario.getLastProvID(); i++) {
            EU3Province prov = scenario.getEU3Province(i);
            if (prov.getController().equals(tag))
                controlled.add(prov);
        }
        return controlled;
    }
    
    public List<EU3Province> getCore() {
        final List<EU3Province> core = new ArrayList<EU3Province>();
        final String tag = getTag();
        for (int i = 1; i <= scenario.getLastProvID(); i++) {
            EU3Province prov = scenario.getEU3Province(i);
            if (prov.isCoreOf().contains(tag))
                core.add(prov);
        }
        return core;
    }
    
    
    //
    // *** SETTERS ***
    //
    
    private void setTech(String name, int level) {
        go.getChild("technology").getList(name).set(0, Integer.toString(level));
    }
    
    
    //
    // *** MERGING ***
    //
    
    
    private static final String[] techs = { "land_tech", "naval_tech", "trade_tech", "infra_tech", "government_tech"/*, "stability" */};
    
    public void merge(EU3Country country) {
        if (true)
            throw new UnsupportedOperationException("Not yet implemented");
        
        for (String tech : techs) {
            setTech(tech, (getTechnologyLevel(tech) + country.getTechnologyLevel(tech)) / 2);
        }
        
        country.transferAllUnitsTo(this);
        country.transferAllCitiesTo(this);
    }
    
    private void transferAllUnitsTo(EU3Country country) {
        for (GenericObject army : getArmies()) {
            country.go.addChild(army);
            go.removeChild(army);
        }
        armies = null;
        
        for (GenericObject navy : getNavies()) {
            country.go.addChild(navy);
            go.removeChild(navy);
        }
        navies = null;
    }
    
    private void transferAllCitiesTo(EU3Country country) {
        final String tag = getTag();
        final String otherTag = country.getTag();
        
        for (GenericObject prov : scenario.provinces) {
            if (prov.getString("owner").equals(otherTag)) {
                prov.setString("owner", tag);
                if (prov.getString("controller").equals(otherTag))
                    prov.setString("controller", tag);
            }
            if (prov.getStrings("core").contains(tag))
                prov.addString("core", tag);
        }
    }
}
