/*
 * EUGDocument.java
 *
 * Created on June 26, 2007, 12:01 PM
 */

package eug.syntax;

import eug.parser.TokenScanner;
import eug.parser.TokenType;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;

/**
 *
 * @author Michael Myers
 */
public class EUGDocument extends PlainDocument {
    
    private static final long serialVersionUID = 1L;
    
    /** Creates a new instance of EUGDocument */
    public EUGDocument() {
        super();
    }
    
    public Scanner createScanner() {
        return new Scanner();
    }
    
    public int getScannerStart(int p) {
        Element elem = getDefaultRootElement();
        int lineNum = elem.getElementIndex(p);
        Element line = elem.getElement(lineNum);
//	AttributeSet a = line.getAttributes();
//	while (a.isDefined(CommentAttribute) && lineNum > 0) {
//	    lineNum -= 1;
//	    line = elem.getElement(lineNum);
//	    a = line.getAttributes();
//	}
        return line.getStartOffset();
    }
    
    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        super.insertUpdate(chng, attr);
        
//        // update comment marks
//        Element root = getDefaultRootElement();
//        DocumentEvent.ElementChange ec = chng.getChange(root);
//        if (ec != null) {
//            Element[] added = ec.getChildrenAdded();
//            boolean inComment = false;
//            for (int i = 0; i < added.length; i++) {
//                Element elem = added[i];
//                int p0 = elem.getStartOffset();
//                int p1 = elem.getEndOffset();
//                String s;
//                try {
//                    s = getText(p0, p1 - p0);
//                } catch (BadLocationException bl) {
//                    s = null;
//                }
//                if (inComment) {
//                    MutableAttributeSet a = (MutableAttributeSet) elem.getAttributes();
//                    a.addAttribute(CommentAttribute, CommentAttribute);
//                    int index = s.indexOf("*/");
//                    if (index >= 0) {
//                        // found an end of comment, turn off marks
//                        inComment = false;
//                    }
//                } else {
//                    // scan for multiline comment
//                    int index = s.indexOf("/*");
//                    if (index >= 0) {
//                        // found a start of comment, see if it spans lines
//                        index = s.indexOf("*/", index);
//                        if (index < 0) {
//                            // it spans lines
//                            inComment = true;
//                        }
//                    }
//                }
//            }
//        }
    }
    
    @Override
    protected void removeUpdate(DefaultDocumentEvent chng) {
        super.removeUpdate(chng);
        
        // update comment marks
    }
    
    
    public final class Scanner {
        private TokenScanner scanner;
        private int p0;
        
        Scanner() {
            scanner = new TokenScanner(new DocumentInputStream(0, getLength()));
        }
        
        public void setRange(int start, int end) {
            scanner.reset(new DocumentInputStream(start, end));
            p0 = start;
        }
        
        public int getStartOffset() {
            return p0 + scanner.getTokenStart();
        }
        
        public int getEndOffset() {
            return p0 + scanner.getTokenEnd();
        }
        
        public void scan() {
            scanner.nextToken();
        }
        
        public TokenType lastToken() {
            return scanner.lastToken();
        }
        
        public String lastStr() {
            return scanner.lastStr();
        }
    }
    
    private final class DocumentInputStream extends InputStream {
        private Segment text;
//        private int start;
        private int end;
        private int position;   // in document
        private int index;      // in segment
        
        private static final int MAX_SEGMENT_SIZE = 1024;
        
        DocumentInputStream(int p0, int p1) {
            text = new Segment();
//            start = p0;
            end = Math.min(getLength(), p1);    // to be safe
            position = p0;
            loadSegment();
        }
        
        public int read() throws IOException {
            if (index >= text.offset + text.count) {
                // finished this segment
                if (position >= end) {
                    // finished the stream
                    return -1;
                } else {
                    // stream isn't finished; get some more
                    loadSegment();
                }
            }
            
            return text.array[index++];
        }
        
        private void loadSegment() {
            try {
                int n = Math.min(MAX_SEGMENT_SIZE, end - position);
                getText(position, n, text);
                position += n;
                index = text.offset;
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        
    }
}
