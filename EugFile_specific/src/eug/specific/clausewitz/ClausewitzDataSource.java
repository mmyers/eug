/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eug.specific.clausewitz;

import eug.shared.GenericObject;
import java.util.List;

/**
 * Interface defining common methods used on Clausewitz-engine data.
 * <p>
 * <strong>Note:</strong> All getters return <code>null</code> on any error,
 * unless otherwise specified.
 * @author Michael Myers
 * @since EUGFile 1.07.00
 */
public interface ClausewitzDataSource {

    /**
     * Returns the country data for the country with the given tag.
     * <p/>
     * In some implementations, this may be equivalent to
     * {@link #getCountryHistory}.
     * @param tag the country tag to fetch data for.
     * @return the country data.
     */
    public GenericObject getCountry(String tag);

    /**
     * Returns the country history for the country with the given tag.
     * <p/>
     * In some implementations, this may be equivalent to
     * {@link #getCountry}.
     * @param tag the country tag to fetch data for.
     * @return the country history.
     */
    public GenericObject getCountryHistory(String tag);

    /**
     * Returns the province data for the province with the given ID.
     * <p/>
     * In some implementations, this may be equivalent to
     * {@link #getProvinceHistory}.
     * @param id the ID of the province to fetch data for.
     * @return the province data.
     */
    public GenericObject getProvince(int id);

    /**
     * Returns the province history for the province with the given ID.
     * <p/>
     * In some implementations, this may be equivalent to
     * {@link #getProvince}.
     * @param id the ID of the province to fetch data for.
     * @return the province data.
     */
    public GenericObject getProvinceHistory(int id);

    /**
     * Returns a list of all wars (including those that are not active).
     * @return all wars that have happened, are happening, or will happen (if
     * possible).
     */
    public List<GenericObject> getWars();

    /**
     * Returns a list of all wars that are or were active on the given date.
     * @param date the date (in standard EU3 format) to find wars from.
     * @return all active wars from the given date.
     */
    public List<GenericObject> getWars(String date);


    /**
     * Returns the country data for the country with the given tag.
     * <p/>
     * This may or may not be equivalent to calling
     * <code>getCountry(tag).toString()</code>.
     * @param tag the country tag to fetch data for.
     * @return the country data in the form of a string.
     */
    public String getCountryAsStr(String tag);

    /**
     * Returns the country history for the country with the given tag.
     * <p/>
     * This may or may not be equivalent to calling
     * <code>getCountryHistory(tag).toString()</code>.
     * @param tag the country tag to fetch data for.
     * @return the country data in the form of a string.
     */
    public String getCountryHistoryAsStr(String tag);

    /**
     * Returns the province data for the province with the given ID.
     * <p/>
     * This may or may not be equivalent to calling
     * <code>getProvince(id).toString()</code>.
     * @param id the ID of the province to fetch data for.
     * @return the province data in the form of a string.
     */
    public String getProvinceAsStr(int id);

    /**
     * Returns the province history for the province with the given ID.
     * <p/>
     * This may or may not be equivalent to calling
     * <code>getProvinceHistory(id).toString()</code>.
     * @param id the ID of the province to fetch data for.
     * @return the province data in the form of a string.
     */
    public String getProvinceHistoryAsStr(int id);


    /**
     * Forces a reload of the data for the country with the given tag, if the
     * implementation uses a cache.
     * @param tag the tag of the country to reload data for.
     */
    public void reloadCountry(String tag);

    /**
     * Forces a reload of the history for the country with the given tag, if the
     * implementation uses a cache.
     * @param tag the tag of the country to reload data for.
     */
    public void reloadCountryHistory(String tag);

    /**
     * Forces a reload of all country data if the implementation uses a cache.
     */
    public void reloadCountries();

    /**
     * Forces a reload of the data for the province with the given ID, if the
     * implementation uses a cache.
     * @param id the ID of the province to reload data for.
     */
    public void reloadProvince(int id);

    /**
     * Forces a reload of the history for the province with the given ID, if the
     * implementation uses a cache.
     * @param id the ID of the province to reload data for.
     */
    public void reloadProvinceHistory(int id);

    /**
     * Forces a reload of all province data if the implementation uses a cache.
     */
    public void reloadProvinces();


    /**
     * Removes the given war from the history. Depending on the implementation,
     * a call to {@link #saveChanges()} may still be necessary. Removing a
     * non-existent war is guaranteed to cause no problems.
     * @param name the name of the war to remove.
     */
    public void removeWar(String name);


    /**
     * Saves the data for the given country. Depending on the implementation, a
     * call to {@link #saveChanges()} may still be necessary.
     * @param tag the tag of the country.
     * @param cname the name of the country (mainly used for filename purposes).
     * @param data the country data to save.
     */
    public void saveCountry(String tag, String cname, final String data);

    /**
     * Saves the data for the given province. Depending on the implementation, a
     * call to {@link #saveChanges()} may still be necessary.
     * @param id the ID of the province.
     * @param pname the name of the province (mainly used for filename purposes).
     * @param data the province data to save.
     */
    public void saveProvince(int id, String pname, final String data);

    /**
     * Saves the data for the given war (that is, the one with the given name).
     * Depending on the implementation, a call to {@link #saveChanges()} may
     * still be necessary.
     * @param name the name of the war (if not found, it is assumed to be a new
     * war).
     * @param data the war data to save.
     */
    public void saveWar(String name, final String data);

    /**
     * Saves any outstanding changes. Usually called at the end of a session.
     */
    public void saveChanges();

    /**
     * Returns true if there are changes that have not yet been permanently
     * saved.
     * @return <code>true</code> if there are unsaved changes,
     * <code>false</code> otherwise.
     */
    public boolean hasUnsavedChanges();

    // Optional methods:

    /**
     * Preloads all province data for provinces with IDs up to
     * <code>last</code>, usually into a cache.
     * <p/>
     * Some implementations may go further, if preloading is inexpensive (i.e.,
     * does not involve file I/O).
     * @param last the ID of the last province to preload.
     */
    public void preloadProvinces(int last);

    /**
     * Preloads all country data (usually into a cache).
     */
    public void preloadCountries();
}
