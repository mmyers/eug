package eug.specific.ck3;

import eug.specific.ck2.*;
import eug.shared.GenericObject;
import eug.specific.clausewitz.ClausewitzDataSource;

/**
 *
 * @author Michael
 */
public interface CK3DataSource extends ClausewitzDataSource {

    /**
     * Returns the title data for the landed title with the given name.
     * <p>
     * In some implementations, this may be equivalent to
     * {@link #getTitleHistory}.
     * @param title the title to fetch data for.
     * @return the title data.
     */
    public GenericObject getTitle(String title);

    /**
     * Returns the title history for the landed title with the given name.
     * <p>
     * In some implementations, this may be equivalent to
     * {@link #getTitle}.
     * @param title the title to fetch data for.
     * @return the title history.
     */
    public GenericObject getTitleHistory(String title);

    /**
     * Returns the title data for the landed title with the given name.
     * <p>
     * This may or may not be equivalent to calling
     * <code>getTitle(tag).toString()</code>.
     * @param title the title to fetch data for.
     * @return the title data in the form of a string.
     */
    public String getTitleAsStr(String title);

    /**
     * Returns the title history for the landed title with the given name.
     * <p>
     * This may or may not be equivalent to calling
     * <code>getTitleHistory(title).toString()</code>.
     * @param title the title to fetch data for.
     * @return the title data in the form of a string.
     */
    public String getTitleHistoryAsStr(String title);


    /**
     * Forces a reload of the data for the title with the given name, if the
     * implementation uses a cache.
     * @param title the title to reload data for.
     */
    public void reloadTitle(String title);

    /**
     * Forces a reload of the history for the the given title, if the
     * implementation uses a cache.
     * @param title the title to reload data for.
     */
    public void reloadTitleHistory(String title);

    /**
     * Forces a reload of all title data if the implementation uses a cache.
     */
    public void reloadTitles();


    /**
     * Saves the data for the given title. Depending on the implementation, a
     * call to {@link #saveChanges()} may still be necessary.
     * @param title the name (tag) of the title.
     * @param data the title data to save.
     */
    public void saveTitle(String title, final String data);
}
