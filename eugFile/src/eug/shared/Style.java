/*
 * Style.java
 *
 * Created on July 2, 2007, 11:07 PM
 */

package eug.shared;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Collection of preferences determining how a <code>GenericObject</code> tree
 * will appear when printed out.
 * <p>
 * There are also five implementations: {@link #DEFAULT}, {@link #AGCEEP},
 * {@link #NO_WHITESPACE}, {@link #EU3_SAVE_GAME}, and {@link #EU4_SAVE_GAME}.
 * @author Michael Myers
 * @see WritableObject#toFileString(BufferedWriter, int, Style)
 * @see GenericObject#toFileString(BufferedWriter, Style)
 * @see GenericObject#toFileString(BufferedWriter, String, Style)
 */
public interface Style {
    
    public String getTab(int depth);
    public String getEqualsSign(int depth);
    public String getCommentStart();
    public void printTab(final BufferedWriter bw, int depth) throws IOException;
    public void printEqualsSign(final BufferedWriter bw, int depth) throws IOException;
    public void printOpeningBrace(final BufferedWriter bw, int depth) throws IOException;
    public void printCommentStart(final BufferedWriter bw, int depth) throws IOException;
    public void printHeaderCommentStart(final BufferedWriter bw, int depth) throws IOException;
    public void printHeaderCommentEnd(final BufferedWriter bw, int depth) throws IOException;
    public boolean isInline(final GenericObject obj);
    public boolean isInline(final GenericList list);
    public boolean newLineAfterObject();
    
    
    // Implementations
    
    /**
     * <code>Style</code> that prints the same way as pre-EU3 save games. Tabs
     * consist of four spaces, and objects with three or fewer
     * children are printed on a single line (with a few exceptions).
     * Header comments are printed as in most EU2 event files: with a line of
     * hashes before and after. There is a space between the hash mark and the
     * comment text.
     */
    //<editor-fold defaultstate="collapsed" desc=" DEFAULT ">
    public static final Style DEFAULT = new Style() {
        
        @Override
        public String getTab(int depth) {
            final StringBuilder sb = new StringBuilder(depth*4);
            for (int i = 0; i < depth; i++)
                sb.append("    ");
            return sb.toString();
        }
        
        @Override
        public String getEqualsSign(int depth) {
            return " = ";
        }
        
        @Override
        public String getCommentStart() {
            return "# ";
        }
        
        @Override
        public void printTab(final BufferedWriter bw, int depth) throws IOException {
            for (int i = 0; i < depth; i++)
                bw.write("    ");
        }
        
        @Override
        public void printEqualsSign(final BufferedWriter bw, int depth) throws IOException {
            bw.write(" = ");
        }
        
        @Override
        public void printOpeningBrace(final BufferedWriter bw, int depth) throws IOException {
            bw.write("{ ");
        }
        
        @Override
        public void printHeaderCommentStart(final BufferedWriter bw, int depth) throws IOException {
            printTab(bw, depth);
            
            for (int i = depth*GenericObject.tabLength; i < 80; i++)
                bw.write('#');
            
            bw.newLine();
        }
        
        @Override
        public void printCommentStart(final BufferedWriter bw, int depth) throws IOException {
            bw.write("# ");
        }
        
        @Override
        public void printHeaderCommentEnd(final BufferedWriter bw, int depth) throws IOException {
            bw.newLine();
            
            printTab(bw, depth);
            
            for (int i = depth*GenericObject.tabLength; i < 80; i++)
                bw.write('#');
        }
        
        @Override
        public boolean isInline(final GenericObject obj) {
            if (obj == null)
                return false;
            
            final String name = obj.name;
            final int size = obj.size();
            
            if (name.equals("root"))
                return false;
            
            boolean sameLine = (size == 0 ||
                    (
                    (size < 4) &&
                    !(
                    name.equalsIgnoreCase("trigger") || name.startsWith("action_") ||
                    (size != 1 && name.equalsIgnoreCase("NOT") || (name.equalsIgnoreCase("AND") || name.equalsIgnoreCase("OR")))
                    )
                    ));
            if (sameLine) {
                for (ObjectVariable v : obj.values)
                    if (v.getInlineComment().length() != 0)
                        return false;
                for (GenericObject o : obj.children)
                    if (o.getInlineComment().length() != 0)
                        return false;
                for (GenericList l : obj.lists)
                    if (l.getInlineComment().length() != 0)
                        return false;
            }
            return sameLine;
        }
        
        @Override
        public boolean isInline(final GenericList list) {
            return true;
        }
        
        @Override
        public boolean newLineAfterObject() {
            return true;
        }
    };
    //</editor-fold>
    
    /**
     * <code>Style</code> that formats the same way as AGCEEP event files. Tabs
     * consist of a single tab character, and objects are printed on a single
     * line exactly when {@link #DEFAULT} would. Header comments have nothing
     * before or after. Comment text follows the hash mark without a space.
     */
    //<editor-fold defaultstate="collapsed" desc=" AGCEEP ">
    public static final Style AGCEEP = new Style() {
        
        @Override
        public String getTab(int depth) {
            final StringBuilder sb = new StringBuilder(depth);
            for (int i = 0; i < depth; i++)
                sb.append("\t");
            return sb.toString();
        }
        
        @Override
        public String getEqualsSign(int depth) {
            return " = ";
        }
        
        @Override
        public String getCommentStart() {
            return "#";
        }
        
        @Override
        public void printTab(final BufferedWriter bw, int depth) throws IOException {
            for (int i = 0; i < depth; i++)
                bw.write("\t");
        }
        
        @Override
        public void printEqualsSign(final BufferedWriter bw, int depth) throws IOException {
            bw.write(" = ");
        }
        
        @Override
        public void printOpeningBrace(final BufferedWriter bw, int depth) throws IOException {
            bw.write("{ ");
        }
        
        @Override
        public void printHeaderCommentStart(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public void printCommentStart(final BufferedWriter bw, int depth) throws IOException {
            bw.write('#');
        }
        
        @Override
        public void printHeaderCommentEnd(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public boolean isInline(final GenericObject obj) {
            // Same as default, I think.
            if (obj == null)
                return false;
            
            final String name = obj.name;
            final int size = obj.size();
            
            if (name.equals("root"))
                return false;
            
            boolean sameLine = (size == 0);
            if (sameLine) {
                    sameLine = (size < 4) && !(
                    name.equalsIgnoreCase("trigger") || name.startsWith("action_") ||
                    (size != 1 && (name.equalsIgnoreCase("NOT") || (name.equalsIgnoreCase("AND") || name.equalsIgnoreCase("OR"))))
                    );
            }
            if (sameLine) {
                for (ObjectVariable v : obj.values)
                    if (v.getInlineComment().length() != 0)
                        return false;
                for (GenericObject o : obj.children)
                    if (o.getInlineComment().length() != 0)
                        return false;
                for (GenericList l : obj.lists)
                    if (l.getInlineComment().length() != 0)
                        return false;
            }
            return sameLine;
        }
        
        @Override
        public boolean isInline(final GenericList list) {
            return true;
        }
        
        @Override
        public boolean newLineAfterObject() {
            return true;
        }
    };
    //</editor-fold>
    
    /**
     * <code>Style</code> that compresses as much as possible. There are no line
     * breaks unless there is a comment, and there are no spaces except between
     * variables. Comments are treated as in {@link #AGCEEP}.
     */
    //<editor-fold defaultstate="collapsed" desc=" NO_WHITESPACE ">
    public static final Style NO_WHITESPACE = new Style() {
        
        @Override
        public String getTab(int depth) {
            return "";
        }
        
        @Override
        public String getEqualsSign(int depth) {
            return "=";
        }
        
        @Override
        public String getCommentStart() {
            return "#";
        }
        
        @Override
        public void printTab(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public void printEqualsSign(final BufferedWriter bw, int depth) throws IOException {
            bw.write('=');
        }
        
        @Override
        public void printOpeningBrace(final BufferedWriter bw, int depth) throws IOException {
            bw.write('{');
        }
        
        @Override
        public void printHeaderCommentStart(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public void printCommentStart(final BufferedWriter bw, int depth) throws IOException {
            bw.write('#');
        }
        
        @Override
        public void printHeaderCommentEnd(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public boolean isInline(final GenericObject obj) {
            if (obj == null)
                return false;
            
            final String name = obj.name;
            final int size = obj.size();
            
            if (name.equals("root"))
                return false;
            
            boolean sameLine = (size < 4);
            if (sameLine) {
                for (ObjectVariable v : obj.values)
                    if (v.getInlineComment().length() != 0)
                        return false;
                for (GenericObject o : obj.children)
                    if (o.getInlineComment().length() != 0)
                        return false;
                for (GenericList l : obj.lists)
                    if (l.getInlineComment().length() != 0)
                        return false;
            }
            return sameLine;
        }
        
        @Override
        public boolean isInline(final GenericList list) {
            return true;
        }
        
        @Override
        public boolean newLineAfterObject() {
            return false;
        }
    };
    //</editor-fold>
    
    /**
     * <code>Style</code> that formats the same way as EU3 save game files. Tabs
     * consist of a single tab character, and objects are never inline. There is
     * no space on either side of equals signs. Comments are treated as in
     * {@link #AGCEEP}, for lack of a better reference.
     */
    //<editor-fold defaultstate="collapsed" desc=" EU3_SAVE_GAME ">
    public static final Style EU3_SAVE_GAME = new Style() {
        
        @Override
        public String getTab(int depth) {
            final StringBuilder sb = new StringBuilder(depth*4);
            for (int i = 0; i < depth; i++)
                sb.append("\t");
            return sb.toString();
        }
        
        @Override
        public String getEqualsSign(int depth) {
            return "=";
        }
        
        @Override
        public String getCommentStart() {
            return "#";
        }
        
        @Override
        public void printTab(final BufferedWriter bw, int depth) throws IOException {
            for (int i = 0; i < depth; i++)
                bw.write("\t");
        }
        
        @Override
        public void printEqualsSign(final BufferedWriter bw, int depth) throws IOException {
            bw.write('=');
        }
        
        @Override
        public void printOpeningBrace(final BufferedWriter bw, int depth) throws IOException {
            bw.write("{ ");
        }
        
        @Override
        public void printHeaderCommentStart(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public void printCommentStart(final BufferedWriter bw, int depth) throws IOException {
            bw.write('#');
        }
        
        @Override
        public void printHeaderCommentEnd(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public boolean isInline(final GenericObject obj) {
            return false;
        }
        
        @Override
        public boolean isInline(final GenericList list) {
            return true;
        }
        
        @Override
        public boolean newLineAfterObject() {
            return false;
        }
    };
    //</editor-fold>
    
    /**
     * <code>Style</code> that formats the same way as EU4 save game files. Tabs
     * consist of a single tab character, and objects are never inline. There is
     * no space on either side of equals signs, but opening curly brackets
     * appear on the next line. Comments are treated as in {@link #AGCEEP}, for
     * lack of a better reference.
     */
    //<editor-fold defaultstate="collapsed" desc=" EU4_SAVE_GAME ">
    public static final Style EU4_SAVE_GAME = new Style() {
        
        @Override
        public String getTab(int depth) {
            final StringBuilder sb = new StringBuilder(depth*4);
            for (int i = 0; i < depth; i++)
                sb.append("\t");
            return sb.toString();
        }
        
        @Override
        public String getEqualsSign(int depth) {
            return "=";
        }
        
        @Override
        public String getCommentStart() {
            return "#";
        }
        
        @Override
        public void printTab(final BufferedWriter bw, int depth) throws IOException {
            for (int i = 0; i < depth; i++)
                bw.write("\t");
        }
        
        @Override
        public void printEqualsSign(final BufferedWriter bw, int depth) throws IOException {
            bw.write('=');
        }
        
        @Override
        public void printOpeningBrace(final BufferedWriter bw, int depth) throws IOException {
            bw.write('\n');
            printTab(bw, depth);
            bw.write('{');
        }
        
        @Override
        public void printHeaderCommentStart(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public void printCommentStart(final BufferedWriter bw, int depth) throws IOException {
            bw.write('#');
        }
        
        @Override
        public void printHeaderCommentEnd(final BufferedWriter bw, int depth) throws IOException {
            // do nothing
        }
        
        @Override
        public boolean isInline(final GenericObject obj) {
            return false;
        }
        
        @Override
        public boolean isInline(final GenericList list) {
            return false;
        }
        
        @Override
        public boolean newLineAfterObject() {
            return false;
        }
    };
    //</editor-fold>
}
