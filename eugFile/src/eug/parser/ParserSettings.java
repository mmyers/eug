/*
 * ParserSettings.java
 *
 * Created on July 4, 2007, 11:07 AM
 */

package eug.parser;

/**
 * Encapsulates a number of parameters used during parsing.
 * @author Michael Myers
 * @since EUGFile 1.06.00pre1
 */
public final class ParserSettings implements Cloneable, java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates whether or not lists should be allowed.
     * <p>
     * The default value is <code>true</code>.
     */
    private boolean allowLists;
    
    /**
     * Indicates whether or not single words should be allowed.
     * <p>
     * The default value is <code>false</code>.
     */
    private boolean allowSingleTokens;
    
    /**
     * Indicates whether or not comments should be ignored.
     * <p>
     * The default value is <code>false</code>.
     */
    private boolean ignoreComments;
    
    /**
     * Indicates whether to display info on how long the loading took.
     * <p>
     * The default value is <code>true</code>.
     */
    private boolean printTimingInfo;
    
    /**
     * Indicates whether or not the parser should try to recover after, e.g.,
     * an extra '}'.
     * <p>
     * The default value is <code>true</code>.
     */
    private boolean tryToRecover;
    
    /**
     * Indicates whether or not warnings should be treated as errors.
     * <p>
     * The default value is <code>false</code>.
     */
    private boolean warningsAreErrors;
    
    /** Creates a new instance of ParserSettings */
    private ParserSettings() {
        setAllowLists(true);
        setAllowSingleTokens(false);
        setIgnoreComments(false);
        setPrintTimingInfo(true);
        setTryToRecover(true);
        setWarningsAreErrors(false);
    }
    
    public ParserSettings setAllowLists(boolean allow) {
        allowLists = allow;
        return this;
    }
    
    public ParserSettings setAllowSingleTokens(boolean allow) {
        allowSingleTokens = allow;
        return this;
    }
    
    public ParserSettings setIgnoreComments(boolean ignore) {
        ignoreComments = ignore;
        return this;
    }
    
    public ParserSettings setPrintTimingInfo(boolean info) {
        printTimingInfo = info;
        return this;
    }
    
    public ParserSettings setTryToRecover(boolean recover) {
        tryToRecover = recover;
        return this;
    }
    
    public ParserSettings setWarningsAreErrors(boolean errors) {
        warningsAreErrors = errors;
        return this;
    }

    public boolean isAllowLists() {
        return allowLists;
    }

    public boolean isAllowSingleTokens() {
        return allowSingleTokens;
    }

    public boolean isIgnoreComments() {
        return ignoreComments;
    }

    public boolean isPrintTimingInfo() {
        return printTimingInfo;
    }

    public boolean isTryToRecover() {
        return tryToRecover;
    }

    public boolean isWarningsAreErrors() {
        return warningsAreErrors;
    }
    
    
    private static final ParserSettings defaults = new ParserSettings();
    private static final ParserSettings noCommentSettings = new ParserSettings().setIgnoreComments(true);
    private static final ParserSettings strictSettings = new ParserSettings().setTryToRecover(false).setWarningsAreErrors(true);
    
    
    public static ParserSettings getDefaults() {
        return defaults.clone();
    }
    
    public static ParserSettings getNoCommentSettings() {
        return noCommentSettings.clone();
    }
    
    public static ParserSettings getStrictSettings() {
        return strictSettings.clone();
    }
    
    
    @Override
    public ParserSettings clone() {
        try {
            return (ParserSettings) super.clone(); // we only have booleans, which should clone OK
        } catch (CloneNotSupportedException ex) {
            throw new InternalError(ex.getMessage());
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParserSettings))
            return false;
        
        ParserSettings other = (ParserSettings) o;
        
        return (allowLists == other.allowLists) &&
                (allowSingleTokens == other.allowSingleTokens) &&
                (ignoreComments == other.ignoreComments) &&
                (printTimingInfo == other.printTimingInfo) &&
                (tryToRecover == other.tryToRecover) &&
                (warningsAreErrors == other.warningsAreErrors);
    }
    
    @Override
    public int hashCode() {
        return Boolean.valueOf(allowLists).hashCode() +
                Boolean.valueOf(allowSingleTokens).hashCode() +
                Boolean.valueOf(ignoreComments).hashCode() +
                Boolean.valueOf(printTimingInfo).hashCode() +
                Boolean.valueOf(tryToRecover).hashCode() +
                Boolean.valueOf(warningsAreErrors).hashCode();
    }
}
