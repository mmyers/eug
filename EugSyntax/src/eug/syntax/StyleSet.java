/*
 * StyleSet.java
 *
 * Created on June 26, 2007, 5:18 PM
 */

package eug.syntax;

import eug.parser.TokenType;
import javax.swing.text.MutableAttributeSet;

/**
 *
 * @author Michael Myers
 */
public interface StyleSet extends java.io.Serializable {
    
    /**
     * Returns the style that should be used to color the given token.
     */
    public MutableAttributeSet getStyle(TokenType token, String str);
    
}
