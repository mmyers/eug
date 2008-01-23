/*
 * DefaultEUGStyleSet.java
 *
 * Created on June 26, 2007, 5:21 PM
 */

package eug.syntax;

import eug.parser.TokenType;
import java.awt.Color;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author Michael Myers
 */
public class DefaultEUGStyleSet implements StyleSet {
    
    private static final long serialVersionUID = 1L;
    
    private static final Style DEFAULT_STYLE =
            StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    
    private final Map<Pattern, MutableAttributeSet> keywords =
            new HashMap<Pattern, MutableAttributeSet>();
    
    private final Map<TokenType, MutableAttributeSet> tokenStyles =
            new EnumMap<TokenType, MutableAttributeSet>(TokenType.class);
    
    private final List<TokenType> overrides = Arrays.asList(new TokenType[] {
        TokenType.ULSTRING,
    });
    
    
    /**
     * Creates a new instance of DefaultEUGStyleSet
     */
    public DefaultEUGStyleSet() {
//        addDefaults();
    }
    
    /**
     * Adds the default keywords. Included are:
     * <ul>
     * <li>EU3-style dates, such as 1492.1.1</li>
     * <li>The constant strings "yes" and "no" (without quotes)</li>
     * <li>Country tags (three characters beginning with a capital letter and
     * including only numbers and capital letters)</li>
     * <li>Numbers</li>
     * <li>Comments</li>
     * <li>Quote-delimited strings</li>
     * </ul>
     * Each of these has its own style; for example, "yes" and "no" are in a
     * dark blue bold font.
     */
    public void addDefaults() {
        final StyleContext defaultStyleContext = StyleContext.getDefaultStyleContext();
        final Style basic = defaultStyleContext.getStyle(StyleContext.DEFAULT_STYLE);
        MutableAttributeSet as;
        
        // Keywords
        
        // Dates (1492.1.1)
        as = new SimpleAttributeSet(basic);
        StyleConstants.setForeground(as, Color.MAGENTA);
        StyleConstants.setBold(as, false);
        keywords.put(Pattern.compile("\\d{1,4}\\.\\d{1,2}\\.\\d{1,2}"), as);
        
        // Yes/no
        as = new SimpleAttributeSet(basic);
        StyleConstants.setForeground(as, Color.BLUE.darker());
        StyleConstants.setBold(as, true);
        keywords.put(Pattern.compile("(yes|no)", Pattern.CASE_INSENSITIVE), as);
        
        // Country tags
        // Lowercase not allowed
        as = new SimpleAttributeSet(basic);
        StyleConstants.setForeground(as, Color.ORANGE.darker());
        StyleConstants.setBold(as, false);
        keywords.put(Pattern.compile("[A-Z][A-Z0-9]{2}"), as);
        
        // Numbers
        as = new SimpleAttributeSet(basic);
        StyleConstants.setForeground(as, Color.GREEN.darker());
        StyleConstants.setBold(as, false);
        keywords.put(Pattern.compile("[+-]?([0-9]+(\\.[0-9]*)?)"), as);
        
        
        // Tokens
        
        as = new SimpleAttributeSet(basic);
        StyleConstants.setForeground(as, Color.GRAY);
        tokenStyles.put(TokenType.COMMENT, as);
        
        as = new SimpleAttributeSet(basic);
        StyleConstants.setForeground(as, Color.MAGENTA);
        tokenStyles.put(TokenType.DLSTRING, as);
        
        as = new SimpleAttributeSet(basic);
        StyleConstants.setForeground(as, Color.BLACK);
        StyleConstants.setBold(as, /*true*/ false);
        tokenStyles.put(TokenType.IDENT, as);
    }
    
    public MutableAttributeSet getStyle(TokenType token, String str) {
        if (overrides.contains(token)) {
            return getKeywordStyle(str);
        } else {
            return getTokenStyle(token);
        }
    }
    
    private MutableAttributeSet getKeywordStyle(String str) {
        for (Map.Entry<Pattern, MutableAttributeSet> entry : keywords.entrySet()) {
            if (entry.getKey().matcher(str).matches())
                return entry.getValue();
        }
        return DEFAULT_STYLE;
    }
    
    private MutableAttributeSet getTokenStyle(TokenType token) {
        MutableAttributeSet mas = tokenStyles.get(token);
        if (mas == null)
            return DEFAULT_STYLE;
        return mas;
    }
    
    /**
     * Adds a custom keyword style to the style set.
     * @param pattern the pattern (in java.util.regex.Pattern syntax) that
     * describes the keyword or set of keywords.
     * @param attrs the <code>AttributeSet</code> that will be used when the
     * given pattern is found in the document's text.
     * @see #addKeywordStyle(Pattern, MutableAttributeSet)
     * @see #removeKeywordStyle(String)
     * @see #removeKeywordStyle(Pattern)
     */
    public void addKeywordStyle(String pattern, MutableAttributeSet attrs) {
        addKeywordStyle(Pattern.compile(pattern), attrs);
    }
    
    /**
     * Add a custom keyword style to the style set.
     * @param pattern the pattern that describes the keyword or set of keywords.
     * @param attrs the <code>AttributeSet</code> that will be used when the
     * given pattern is found in the document's text.
     * @see #addKeywordStyle(String, MutableAttributeSet)
     * @see #removeKeywordStyle(Pattern)
     * @see #removeKeywordStyle(String)
     */
    public void addKeywordStyle(Pattern pattern, MutableAttributeSet attrs) {
        keywords.put(pattern, attrs);
    }
    
    /**
     * Removes the given pattern from the list of keyword patterns.
     * @param pattern the pattern (in java.util.regex.Pattern syntax) that
     * describes the keyword or set of keywords.
     * @see #removeKeywordStyle(Pattern)
     * @see #addKeywordStyle(String, MutableAttributeSet)
     * @see #addKeywordStyle(Pattern, MutableAttributeSet)
     */
    public void removeKeywordStyle(String pattern) {
        removeKeywordStyle(Pattern.compile(pattern));
    }
    
    /**
     * Removes the given pattern from the list of keyword patterns.
     * @param pattern the pattern that describes the keyword or set of keywords.
     * @see #removeKeywordStyle(String)
     * @see #addKeywordStyle(Pattern, MutableAttributeSet)
     * @see #addKeywordStyle(String, MutableAttributeSet)
     */
    public void removeKeywordStyle(Pattern pattern) {
        keywords.remove(pattern);
    }
    
    /**
     * Adds a custom token style to the style set. If there is already a style
     * listed for the given token type, the given attributes will be added to it
     * instead of replacing it.
     * @param type the TokenType to apply the attributes to.
     * @param attrs the <code>AttributeSet</code> that will be used when the
     * given pattern is found in the document's text.
     * @see #addTokenStyle(TokenType, MutableAttributeSet, boolean)
     * @see #addKeywordStyle(String, MutableAttributeSet)
     * @see #addKeywordStyle(Pattern, MutableAttributeSet)
     */
    public void addTokenStyle(TokenType type, MutableAttributeSet attrs) {
        addTokenStyle(type, attrs, false);
    }
    
    /**
     * Adds a custom token style to the style set. If there is already a style
     * listed for the given token type, the parameter <code>override</code>
     * determines whether it will be replaced or added to.
     * @param type the TokenType to apply the attributes to.
     * @param attrs the <code>AttributeSet</code> that will be used when the
     * given pattern is found in the document's text.
     * @param override whether or not to override any previous attributes for
     * the given token type. Ignored if there is no previous entry.
     * @see #addTokenStyle(TokenType, MutableAttributeSet)
     * @see #addKeywordStyle(String, MutableAttributeSet)
     * @see #addKeywordStyle(Pattern, MutableAttributeSet)
     */
    public void addTokenStyle(TokenType type, MutableAttributeSet attrs, boolean override) {
        if (override) {
            // Don't check for a previous listing.
            tokenStyles.put(type, attrs);
            return;
        }
        MutableAttributeSet previous = tokenStyles.get(type);
        if (previous == null) {
            tokenStyles.put(type, attrs);
            return;
        }
        previous.addAttributes(attrs);
    }
    
}
