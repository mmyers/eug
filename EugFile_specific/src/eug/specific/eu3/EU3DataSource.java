/*
 * EU3DataSource.java
 *
 * Created on July 2, 2007, 1:30 PM
 */

package eug.specific.eu3;

import eug.shared.GenericObject;

/**
 * Interface defining common methods used on EU3 data.
 * <p>
 * <strong>Note:</strong> All getters return <code>null</code> on any error,
 * unless otherwise specified.
 * @author Michael Myers
 * @since EUGFile 1.06.00pre1
 */
public interface EU3DataSource {
    
    /**
     * Returns the country data for the country with the given tag.
     */
    public GenericObject getCountry(String tag);
    
    /**
     * Returns the country history for the country with the given tag.
     */
    public GenericObject getCountryHistory(String tag);
    
    /**
     * Returns the province data for the province with the given ID.
     */
    public GenericObject getProvince(int id);
    
    /**
     * Returns the province history for the province with the given ID.
     */
    public GenericObject getProvinceHistory(int id);
    
    
    /**
     * Returns the country data for the country with the given tag.
     */
    public String getCountryAsStr(String tag);
    
    /**
     * Returns the country history for the country with the given tag.
     */
    public String getCountryHistoryAsStr(String tag);
    
    /**
     * Returns the province data for the province with the given ID.
     */
    public String getProvinceAsStr(int id);
    
    /**
     * Returns the province history for the province with the given ID.
     */
    public String getProvinceHistoryAsStr(int id);
    
    
    /**
     * Forces a reload of the data for the country with the given tag, if the
     * implementation uses a cache.
     */
    public void reloadCountry(String tag);
    
    /**
     * Forces a reload of the history for the country with the given tag, if the
     * implementation uses a cache.
     */
    public void reloadCountryHistory(String tag);
    
    /**
     * Forces a reload of the data for the province with the given ID, if the
     * implementation uses a cache.
     */
    public void reloadProvince(int id);
    
    /**
     * Forces a reload of the history for the province with the given ID, if the
     * implementation uses a cache.
     */
    public void reloadProvinceHistory(int id);
    
    
    /**
     * Saves the data for the given country.
     */
    public void saveCountry(String tag, String cname, final String data);
    
    /**
     * Saves the data for the given province.
     */
    public void saveProvince(int id, String pname, final String data);
    
    /**
     * Saves any outstanding changes. Usually called at the end of a session.
     */
    public void saveChanges();
    
    /**
     * Returns true if there are changes that have not yet been permanently
     * saved.
     */
    public boolean hasUnsavedChanges();
    
    
    /**
     * Preloads all province data for provinces with IDs up to
     * <code>last</code>, usually into a cache.
     * Some implementations may go further, if preloading is inexpensive.
     */
    public void preloadProvinces(int last);
    /**
     * Preloads all country data (usually into a cache).
     */
    public void preloadCountries();
}
