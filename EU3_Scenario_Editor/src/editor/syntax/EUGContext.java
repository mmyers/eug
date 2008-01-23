/*
 * EUGContext.java
 *
 * Created on June 26, 2007, 11:59 AM
 */

package eug.syntax;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.Utilities;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 *
 * @author Michael Myers
 */
public class EUGContext extends StyleContext implements ViewFactory {
    
    private static final long serialVersionUID = 1L;
    
    private StyleSet styles;
    
    /**
     * Creates a new instance of EUGContext
     */
    public EUGContext() {
        styles = new DefaultEUGStyleSet();
        ((DefaultEUGStyleSet)styles).addDefaults();
    }
    
    public View create(Element elem) {
        return new EUGView(elem);
    }

    public StyleSet getStyles() {
        return styles;
    }

    public void setStyles(StyleSet styles) {
        this.styles = styles;
    }
    
    private class EUGView extends PlainView {
        
        private EUGDocument.Scanner scanner;
        private boolean scannerValid;
        
        EUGView(Element elem) {
            super(elem);
            scanner = ((EUGDocument) getDocument()).createScanner();
            scannerValid = false;
        }
        
        @Override
        public void paint(Graphics g, Shape a) {
            super.paint(g, a);
            scannerValid = false;
        }
        
        @Override
        protected int drawSelectedText(final Graphics g, int x, int y,
                int p0, int p1)
                throws BadLocationException {
            // Same as drawUnselectedText, but without colors.
            final Font boldFont = g.getFont().deriveFont(Font.BOLD);
            final Font plainFont = g.getFont().deriveFont(Font.PLAIN);
            final Document doc = getDocument();
            
            boolean bold = false;
            boolean lastBold = false;
            
            int mark = p0;
            
            g.setColor(Color.BLACK);
            
            while (p0 < p1) {
                updateScanner(p0);
                int p = Math.min(scanner.getEndOffset(), p1);
                p = (p <= p0) ? p1 : p;
                
                MutableAttributeSet style = styles.getStyle(scanner.lastToken(), scanner.lastStr());
                bold = StyleConstants.isBold(style);
                
                if (bold != lastBold) {
                    // style change, flush what we have
                    g.setFont(lastBold ? boldFont : plainFont);
                    Segment text = getLineBuffer();
                    doc.getText(mark, p0 - mark, text);
                    x = Utilities.drawTabbedText(text, x, y, g, this, mark);
                    mark = p0;
                }
                lastBold = bold;
                p0 = p;
            }
            // flush remaining
            g.setFont(lastBold ? boldFont : plainFont);
            Segment text = getLineBuffer();
            doc.getText(mark, p1 - mark, text);
            return Utilities.drawTabbedText(text, x, y, g, this, mark);
        }
        
        @Override
        protected int drawUnselectedText(final Graphics g, int x, int y,
                int p0, int p1)
                throws BadLocationException {
            // Copied, with a few changes
            final Font boldFont = g.getFont().deriveFont(Font.BOLD);
            final Font plainFont = g.getFont().deriveFont(Font.PLAIN);
            final Document doc = getDocument();
            
            Color last = null;
            Color fg;
            
            boolean bold = false;
            boolean lastBold = false;
            
            int mark = p0;
            
            while (p0 < p1) {
                updateScanner(p0);
                int p = Math.min(scanner.getEndOffset(), p1);
                p = (p <= p0) ? p1 : p;
                
                MutableAttributeSet style = styles.getStyle(scanner.lastToken(), scanner.lastStr());
                fg = StyleConstants.getForeground(style);
                bold = StyleConstants.isBold(style);
                
                if ((fg != last && last != null) || (bold != lastBold)) {
                    // color change, flush what we have
                    g.setColor(last);
                    g.setFont(lastBold ? boldFont : plainFont);
                    Segment text = getLineBuffer();
                    doc.getText(mark, p0 - mark, text);
                    x = Utilities.drawTabbedText(text, x, y, g, this, mark);
                    mark = p0;
                }
                last = fg;
                lastBold = bold;
                p0 = p;
            }
            // flush remaining
            g.setColor(last);
            g.setFont(lastBold ? boldFont : plainFont);
            Segment text = getLineBuffer();
            doc.getText(mark, p1 - mark, text);
            return Utilities.drawTabbedText(text, x, y, g, this, mark);
        }
        
        private void updateScanner(int pos) {
            // Copied, with a few changes
            if (!scannerValid) {
                EUGDocument doc = (EUGDocument) getDocument();
                scanner.setRange(doc.getScannerStart(pos), doc.getLength());
                scannerValid = true;
            }
            
            while (scanner.getEndOffset() <= pos) {
                scanner.scan();
            }
        }
    }
}
