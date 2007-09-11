/*
 * DefaultEUGStyleSet.java
 *
 * Created on June 26, 2007, 5:21 PM
 */

package editor.syntax;

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
    
    public void addKeywordStyle(String pattern, MutableAttributeSet attrs) {
        keywords.put(Pattern.compile(pattern), attrs);
    }
    
    public void addKeywordStyle(Pattern pattern, MutableAttributeSet attrs) {
        keywords.put(pattern, attrs);
    }
    
    public void removeKeywordStyle(Pattern pattern) {
        keywords.remove(pattern);
    }
    
    public void addTokenStyle(TokenType type, MutableAttributeSet attrs) {
        addTokenStyle(type, attrs, false);
    }
    
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
