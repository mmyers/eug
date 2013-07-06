/*
 * EU3Country.java
 *
 * Created on March 22, 2007, 6:14 PM
 */

package eug.specific.eu3;

import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzDataSource;
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
    
    /**
     * Creates a new instance of EU3Country.
     * <p>
     * Note that some methods require that <code>src</code> be an instance of
     * {@link EU3SaveGame}.
     */
    public EU3Country(GenericObject go, ClausewitzDataSource src) {
        super(go, src);
    }
    
    // Lazy accessors, to reduce object creation time
    
    private GenericObject getHistory() {
        if (history == null) {
            history = go.getChild("history");
        }
        return history;
    }
    
    public List<GenericObject> getArmies() {
        if (armies == null) {
            armies = go.getChildren("army");
        }
        return armies;
    }
    
    public List<GenericObject> getNavies() {
        if (navies == null) {
            navies = go.getChildren("navy");
        }
        return navies;
    }
    
    // End of lazy accessors
    
    public String getTag() {
        return go.name;
    }
    
    /**
     * This method can only be called if the data source is a saved game.
     * It will throw an exception otherwise.
     */
    public void changeTag(String newTag) {
        ((EU3SaveGame)dataSource).changeCountryTag(getTag(), newTag);
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
        GenericObject dip = go.getChild(tag);
        if (dip != null)
            return dip.getInt("value");
        return 0;
    }
    
    
    public int getPolicyValue(String policy) {
        return go.getInt(policy);
    }
    
    public boolean ownsProvince(int id) {
        return dataSource.getProvince(id).getString("owner").equals(getTag());
    }
    
    public boolean controllsProvince(int id) {
        return dataSource.getProvince(id).getString("controller").equals(getTag());
    }
    
    public boolean isCore(int id) {
        GenericObject prov = dataSource.getProvince(id);
        // this is necessary because history files do it one way and saved games do it another
        return prov.getStrings("add_core").contains(getTag()) ||
                prov.getStrings("core").contains(getTag());
    }
    
    
    public int numOwned() {
        int ret = 0;
        final String tag = getTag();
        for (int i = 1; /* loop until broken */; i++) {
            GenericObject prov = dataSource.getProvince(i);
            if (prov == null)
                break;
            if (prov.getString("owner").equals(tag))
                ret++;
        }
        return ret;
    }
    
    public List<GenericObject> getOwned() {
        final List<GenericObject> owned = new ArrayList<GenericObject>();
        final String tag = getTag();
        for (int i = 1; /* loop until broken */; i++) {
            GenericObject prov = dataSource.getProvince(i);
            if (prov == null)
                break;
            if (prov.getString("owner").equals(tag))
                owned.add(prov);
        }
        return owned;
    }
    
    public List<GenericObject> getControlled() {
        final List<GenericObject> controlled = new ArrayList<GenericObject>();
        final String tag = getTag();
        for (int i = 1; /* loop until broken */; i++) {
            GenericObject prov = dataSource.getProvince(i);
            if (prov == null)
                break;
            if (prov.getString("controller").equals(tag))
                controlled.add(prov);
        }
        return controlled;
    }
    
    public List<GenericObject> getCore() {
        final List<GenericObject> core = new ArrayList<GenericObject>();
        final String tag = getTag();
        for (int i = 1; /* loop until broken */; i++) {
            GenericObject prov = dataSource.getProvince(i);
            if (prov == null)
                break;
            if (prov.getStrings("core").contains(tag) || prov.getStrings("is_core").contains(tag))
                core.add(prov);
        }
        return core;
    }
    
    
    public List<Integer> getProvsWithBuilding(String building) {
        final List<Integer> provs = new ArrayList<Integer>();
        final String tag = getTag();
        for (int i = 1; /* loop until broken */; i++) {
            GenericObject prov = dataSource.getProvince(i);
            if (prov == null)
                break;
            if (prov.getString("owner").equals(tag) && prov.getString(building).equals("yes"))
                provs.add(i);
        }
        return provs;
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
    
    
    private static final String[] techs = { "land_tech", "naval_tech", "trade_tech", "production_tech", "government_tech"/*, "stability" */};
    
    /**
     * This method can only be called if the data source is a saved game.
     * It will throw an exception otherwise.
     */
    public void merge(EU3Country country) {
//        if (true)
//            throw new UnsupportedOperationException("Not yet implemented");
        
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
        
        ((EU3SaveGame)dataSource).preloadProvinces();
        for (GenericObject prov : ((EU3SaveGame)dataSource).provinces.values()) {
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
