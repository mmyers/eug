/*
 * EditorDialog.java
 *
 * Created on June 25, 2007, 3:49 PM
 */

package editor;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.event.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author Michael Myers
 * @since  0.5pre1
 */
public class EditorDialog extends JDialog {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(EditorDialog.class.getName());
    
    /** The contents of the original file. */
    private String originalContents;
    protected final UndoManager undo = new UndoManager();
    
    private static final GenericObject config =
            EUGFileIO.load("editor_cfg.txt",
            eug.parser.ParserSettings.getDefaults().setPrintTimingInfo(false));
    
    private static Font font = initFont();
    protected static boolean saveBackups = initBackups();

    private final FilenameResolver resolver;
    private final ProvinceData provinceData;
    
    private static Font initFont() {
        String fontName = config.getString("editor.font.name");
        int fontSize = config.getInt("editor.font.size");
        return new Font(fontName, Font.PLAIN, fontSize);
    }
    
    private static void setStaticFont(Font newFont) {
        font = newFont;
        config.setString("editor.font.name", newFont.getFamily(), true);
        config.setInt("editor.font.size", newFont.getSize());
    }
    
    private static boolean initBackups() {
        // getBoolean defaults to false if not found, which is what we want
        return config.getBoolean("editor.savebackups");
    }
    
    private static void setStaticBackups(boolean backups) {
        saveBackups = backups;
        config.setBoolean("editor.savebackups", backups);
    }
    
    /** Creates new form EditorDialog */
    public EditorDialog(java.awt.Frame parent, String name, FilenameResolver resolver, ProvinceData data) {
        super(parent, false);
        setTitle(name);
        
        originalContents = "";
        this.resolver = resolver;
        this.provinceData = data;
        setup();
        setLocationRelativeTo(parent);
    }
    
    /** Creates new form EditorDialog */
    public EditorDialog(java.awt.Frame parent, String name, String contents, FilenameResolver resolver, ProvinceData data) {
        super(parent, false);
        setTitle(name);
        
        originalContents = contents;
        this.resolver = resolver;
        this.provinceData = data;
        setup();
        setLocationRelativeTo(parent);
    }
    
    /** Initializes the dialog. */
    private void setup() {
        initComponents();
        
        //{{{ Set up the text area
        textPane.setCaretPosition(0);
        
        initDocument();
        
        textPane.addCaretListener((CaretEvent e) -> {
            // TODO: Highlight matching brackets
            if (e.getMark() != e.getDot()) {
                cutMenuItem.setEnabled(true);
                copyMenuItem.setEnabled(true);
            } else {
                cutMenuItem.setEnabled(false);
                copyMenuItem.setEnabled(false);
            }
        });
        
        //}}}
        
        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener((FlavorEvent e) -> {
            final Clipboard clipboard =
                    Toolkit.getDefaultToolkit().getSystemClipboard();
            
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                pasteMenuItem.setEnabled(true);
            } else {
                pasteMenuItem.setEnabled(false);
            }
        });
        
        textAreaKeyTyped(); // set the syntaxCheckLabel
    }
    
    private void initDocument() {
        textPane.getDocument().addUndoableEditListener((UndoableEditEvent e) -> {
            undo.addEdit(e.getEdit());
            updateUndoStates();
        });
        
        // Here I want to make it so that if a change is made, a timer (one
        // second or so) is started. If the timer ends before another change is
        // made, then validation is triggered.
        // I couldn't figure out how to do it, though.
        textPane.getDocument().addDocumentListener(new DocumentListener() {
//            class TimerListener implements ActionListener {
//                boolean changed = false;
//                public void actionPerformed(ActionEvent e) {
//                    if (changed) {
//                        log.log(Level.FINEST, "Changed text");
//                        textAreaKeyTyped();
//                        changed = false;
//                    } else {
//                        log.log(Level.FINEST, "Not changed");
//                    }
//                }
//            }
//            private final TimerListener timerListener = new TimerListener();
//            private final Timer checkTimer = new Timer(500, timerListener);
            public void changedUpdate(DocumentEvent e) {
                textAreaKeyTyped();
//                timerListener.changed = true;
//                if (!checkTimer.isRunning())
//                    checkTimer.start();
//                checkTimer.restart();
            }
            public void insertUpdate(DocumentEvent e) {
                textAreaKeyTyped();
//                timerListener.changed = true;
//                if (!checkTimer.isRunning())
//                    checkTimer.start();
//                checkTimer.restart();
            }
            public void removeUpdate(DocumentEvent e) {
                textAreaKeyTyped();
//                timerListener.changed = true;
//                if (!checkTimer.isRunning())
//                    checkTimer.start();
//                checkTimer.restart();
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel lowerPanel = new javax.swing.JPanel();
        syntaxCheckLabel = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        textPane = new javax.swing.JEditorPane();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        setBackupFilesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu editMenu = new javax.swing.JMenu();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
        findMenuItem = new javax.swing.JMenuItem();
        replaceMenuItem = new javax.swing.JMenuItem();
        goToMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu toolsMenu = new javax.swing.JMenu();
        showCountryListMenuItem = new javax.swing.JMenuItem();
        showCultureListMenuItem = new javax.swing.JMenuItem();
        showProvinceListMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu formatMenu = new javax.swing.JMenu();
        setFontMenuItem = new javax.swing.JMenuItem();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(formListener);

        lowerPanel.add(syntaxCheckLabel);

        closeButton.setText("Close");
        closeButton.addActionListener(formListener);
        lowerPanel.add(closeButton);

        getContentPane().add(lowerPanel, java.awt.BorderLayout.SOUTH);

        textPane.setContentType("text/eug"); // NOI18N
        textPane.setEditorKit(new eug.syntax.EUGEditorKit());
        textPane.setFont(font);
        textPane.setText(originalContents);
        textPane.setPreferredSize(new java.awt.Dimension(600, 500));
        jScrollPane1.setViewportView(textPane);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        setBackupFilesCheckBoxMenuItem.setSelected(saveBackups);
        setBackupFilesCheckBoxMenuItem.setText("Save backup files");
        setBackupFilesCheckBoxMenuItem.addActionListener(formListener);
        fileMenu.add(setBackupFilesCheckBoxMenuItem);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        closeMenuItem.setText("Close");
        closeMenuItem.addActionListener(formListener);
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText("Edit");

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        undoMenuItem.setMnemonic('U');
        undoMenuItem.setText("Undo");
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(formListener);
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        redoMenuItem.setMnemonic('R');
        redoMenuItem.setText("Redo");
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(formListener);
        editMenu.add(redoMenuItem);
        editMenu.add(jSeparator1);

        cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        cutMenuItem.setEnabled(false);
        cutMenuItem.addActionListener(formListener);
        editMenu.add(cutMenuItem);

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        copyMenuItem.setMnemonic('C');
        copyMenuItem.setText("Copy");
        copyMenuItem.setEnabled(false);
        copyMenuItem.addActionListener(formListener);
        editMenu.add(copyMenuItem);

        pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.setText("Paste");
        pasteMenuItem.setEnabled(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor));
        pasteMenuItem.addActionListener(formListener);
        editMenu.add(pasteMenuItem);
        editMenu.add(jSeparator2);

        findMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        findMenuItem.setMnemonic('F');
        findMenuItem.setText("Find...");
        findMenuItem.addActionListener(formListener);
        editMenu.add(findMenuItem);

        replaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        replaceMenuItem.setMnemonic('R');
        replaceMenuItem.setText("Replace...");
        replaceMenuItem.setEnabled(false);
        editMenu.add(replaceMenuItem);

        goToMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        goToMenuItem.setMnemonic('G');
        goToMenuItem.setText("Go to...");
        goToMenuItem.addActionListener(formListener);
        editMenu.add(goToMenuItem);

        menuBar.add(editMenu);

        toolsMenu.setText("Tools");

        showCountryListMenuItem.setText("Show countries...");
        showCountryListMenuItem.addActionListener(formListener);
        toolsMenu.add(showCountryListMenuItem);

        showCultureListMenuItem.setText("Show cultures...");
        showCultureListMenuItem.addActionListener(formListener);
        toolsMenu.add(showCultureListMenuItem);

        showProvinceListMenuItem.setText("Show provinces...");
        showProvinceListMenuItem.addActionListener(formListener);
        toolsMenu.add(showProvinceListMenuItem);

        menuBar.add(toolsMenu);

        formatMenu.setText("Format");

        setFontMenuItem.setText("Set font...");
        setFontMenuItem.addActionListener(formListener);
        formatMenu.add(setFontMenuItem);

        menuBar.add(formatMenu);

        setJMenuBar(menuBar);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.WindowListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == closeButton) {
                EditorDialog.this.closeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == setBackupFilesCheckBoxMenuItem) {
                EditorDialog.this.setBackupFilesCheckBoxMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == closeMenuItem) {
                EditorDialog.this.closeMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == undoMenuItem) {
                EditorDialog.this.undoMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == redoMenuItem) {
                EditorDialog.this.redoMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == cutMenuItem) {
                EditorDialog.this.cutMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == copyMenuItem) {
                EditorDialog.this.copyMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == pasteMenuItem) {
                EditorDialog.this.pasteMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == findMenuItem) {
                EditorDialog.this.findMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == goToMenuItem) {
                EditorDialog.this.goToMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == showCountryListMenuItem) {
                EditorDialog.this.showCountryListMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == showCultureListMenuItem) {
                EditorDialog.this.showCultureListMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == showProvinceListMenuItem) {
                EditorDialog.this.showProvinceListMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == setFontMenuItem) {
                EditorDialog.this.setFontMenuItemActionPerformed(evt);
            }
        }

        public void windowActivated(java.awt.event.WindowEvent evt) {
        }

        public void windowClosed(java.awt.event.WindowEvent evt) {
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == EditorDialog.this) {
                EditorDialog.this.formWindowClosing(evt);
            }
        }

        public void windowDeactivated(java.awt.event.WindowEvent evt) {
        }

        public void windowDeiconified(java.awt.event.WindowEvent evt) {
        }

        public void windowIconified(java.awt.event.WindowEvent evt) {
        }

        public void windowOpened(java.awt.event.WindowEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents
    
    private void findMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findMenuItemActionPerformed
        new FindDialog().setVisible(true);
    }//GEN-LAST:event_findMenuItemActionPerformed
    
    private void showProvinceListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showProvinceListMenuItemActionPerformed
        ProvinceListDialog.showDialog((java.awt.Frame) getOwner(), provinceData);
    }//GEN-LAST:event_showProvinceListMenuItemActionPerformed
    
    private void showCultureListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCultureListMenuItemActionPerformed
        CultureListDialog.showDialog((java.awt.Frame) getOwner(), resolver);
    }//GEN-LAST:event_showCultureListMenuItemActionPerformed
    
    private void showCountryListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCountryListMenuItemActionPerformed
        CountryListDialog.showDialog((java.awt.Frame) getOwner(), resolver);
    }//GEN-LAST:event_showCountryListMenuItemActionPerformed
    
    /*
    private void reformatMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        final GenericObject obj = EUGFileIO.loadFromString(textPane.getText(), false);
        if (obj == null)
            JOptionPane.showMessageDialog(this, "Can't reformat when there are syntax errors.", "Error", JOptionPane.ERROR_MESSAGE);
        else
            textPane.setText(obj.toString());
    }
     */
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        close();
    }//GEN-LAST:event_closeButtonActionPerformed
    
    private void setFontMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setFontMenuItemActionPerformed
        Font f = textPane.getFont();
        FontSelectorDialog fsd = new FontSelectorDialog(this, f.getFamily(), f.getStyle(), f.getSize());
        fsd.setVisible(true);
        if (fsd.closedOK) {
            setStaticFont(fsd.getSelectedFont());
            textPane.setFont(fsd.getSelectedFont());
        }
    }//GEN-LAST:event_setFontMenuItemActionPerformed
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        close();
    }//GEN-LAST:event_formWindowClosing
    
    private void goToMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToMenuItemActionPerformed
        int lineCount = getLineCount();
        int caretLine = getCaretLine();
        String response =
                JOptionPane.showInputDialog(this, "Go to line (0 - " + (lineCount-1) + "): ", caretLine);
        
        if (response == null || response.length() == 0)
            return;
        
        textPane.setCaretPosition(getLineStartOffset(Integer.parseInt(response)));
    }//GEN-LAST:event_goToMenuItemActionPerformed
    
    private void pasteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteMenuItemActionPerformed
        paste();
    }//GEN-LAST:event_pasteMenuItemActionPerformed
    
    private void copyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyMenuItemActionPerformed
        copy();
    }//GEN-LAST:event_copyMenuItemActionPerformed
    
    private void cutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutMenuItemActionPerformed
        cut();
    }//GEN-LAST:event_cutMenuItemActionPerformed
    
    private void redoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
        redo();
    }//GEN-LAST:event_redoMenuItemActionPerformed
    
    private void undoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        undo();
    }//GEN-LAST:event_undoMenuItemActionPerformed
    
    private void closeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuItemActionPerformed
        close();
    }//GEN-LAST:event_closeMenuItemActionPerformed

    private void setBackupFilesCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setBackupFilesCheckBoxMenuItemActionPerformed
        boolean selection = setBackupFilesCheckBoxMenuItem.isSelected();
        setStaticBackups(selection);
    }//GEN-LAST:event_setBackupFilesCheckBoxMenuItemActionPerformed
    
    
    private void updateUndoStates() {
        undoMenuItem.setEnabled(undo.canUndo());
        if (undo.canUndo())
            undoMenuItem.setText(undo.getUndoPresentationName());
        else
            undoMenuItem.setText("Can't undo");
        
        redoMenuItem.setEnabled(undo.canRedo());
        if (undo.canRedo())
            redoMenuItem.setText(undo.getRedoPresentationName());
        else
            redoMenuItem.setText("Can't redo");
    }
    
    
    private static final Color COLOR_SYNTAX_OK = Color.GREEN.darker();
    private static final Color COLOR_SYNTAX_NOTOK = Color.RED.darker();
    private static final Color COLOR_HIGHLIGHT_ERROR = new Color(255, 128, 128, 128);   // half-transparent light red
    
    /**
     * Checks the text area's text for validity.
     * @see #validateText()
     */
    private void textAreaKeyTyped() {
        if (validateText()) {
            syntaxCheckLabel.setText("Syntax OK");
            syntaxCheckLabel.setForeground(COLOR_SYNTAX_OK);
        } else {
            syntaxCheckLabel.setText("Syntax invalid");
            syntaxCheckLabel.setForeground(COLOR_SYNTAX_NOTOK);
        }
        syntaxCheckLabel.setToolTipText(getUnmatchedBracketText());
        highlightInvalidLines();
    }
    
    private static final ParserSettings checkSettings =
            ParserSettings.getStrictSettings().setPrintTimingInfo(false).setPrintWarnings(false);
    
    /** @return whether the data was parsed successfully or not. */
    private boolean validateText() {
        return EUGFileIO.loadFromString(textPane.getText(), checkSettings) != null;
    }
    
    /**
     * Returns a mapping of line numbers to either 0 (left bracket) or 1 (right
     * bracket).
     */
    private java.util.Map<Integer, Integer> findUnmatchedBrackets() {
        final Segment text = new Segment();
        final java.util.Stack<Integer> lefts = new java.util.Stack<>();
        final java.util.Map<Integer, Integer> unmatched = new java.util.HashMap<>();
        final int lineCount = getLineCount();
        
        for (int line = 0; line < lineCount; line++) {
            getLineText(line, text);
            
            for (int i = text.getBeginIndex(); i < text.getEndIndex(); i++) {
                char c = text.array[i];
                if (c == '{') {
                    lefts.push(line);
                } else if (c == '}') {
                    if (lefts.isEmpty()) {
                        unmatched.put(line, 1);
                    } else {
                        lefts.pop();
                    }
                } else if (c == '#') {
                    break; // break line loop
                }
            }
        }
        
        while (!lefts.isEmpty()) {
            unmatched.put(lefts.pop(), 0);
        }
        
        return unmatched;
    }
    
    private void clearHighlights() {
        textPane.getHighlighter().removeAllHighlights();
    }
    
    private void highlightInvalidLines() {
        clearHighlights();
        for (Integer line : findUnmatchedBrackets().keySet()) {
            highlightErrorLine(line);
        }
    }
    
    private void highlightErrorLine(int line) {
        try {
            textPane.getHighlighter().addHighlight(
                    getLineStartOffset(line),
                    getLineEndOffset(line),
                    new DefaultHighlighter.DefaultHighlightPainter(COLOR_HIGHLIGHT_ERROR)
                    );
            // TODO: Make error stripe
        } catch (BadLocationException ex) {
            log.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
    
    private String getUnmatchedBracketText() {
        final java.util.Map<Integer, Integer> brackets =
                new java.util.TreeMap<>(findUnmatchedBrackets());
        
        final StringBuilder text = new StringBuilder("<html>Unmatched brackets:<br>");
        
        for (java.util.Map.Entry<Integer, Integer> entry : brackets.entrySet()) {
            if (entry.getValue() == 0) {
                text.append("Left bracket on line ");
            } else {
                text.append("Right bracket on line ");
            }
            text.append(entry.getKey()).append("<br>");
        }
        text.append("</html>");
        return text.toString();
    }
    
    /**
     * Called when the dialog is closed by any of the three methods: the close
     * button, the close menu item, or the window decorations.
     * <p>Subclasses should override this method if they wish to, e.g., save
     * changes before closing.
     */
    protected void close() {
        dispose();
        EUGFileIO.save(config, "editor_cfg.txt", EUGFileIO.NO_COMMENT);
    }
    
    /**
     * Returns true iff the text in the text area has changed since being
     * loaded.
     */
    public final boolean textHasChanged() {
        return !originalContents.equals(textPane.getText());
    }
    
    protected final String getOriginalContents() {
        return originalContents;
    }
    
    /** Use with care! */
    protected final void setOriginalContents(final String contents) {
        // All this replacing is a hack to get originalContents to match up with
        // the text pane's text. Apparently, any \n characters inserted into
        // the document automatically turn into \r\n (even if they were already
        // part of one!).
        originalContents = contents.replaceAll("(\r\n|\r|\n)", "\r\n");
        try {
            Document newDoc = textPane.getEditorKit().createDefaultDocument();
            newDoc.insertString(0, contents.replaceAll("\r\n", "\n"), null);
            textPane.setDocument(newDoc);
        } catch (BadLocationException ex) {
            log.log(Level.WARNING, ex.getMessage(), ex);
        }
        initDocument();
        textPane.setCaretPosition(0);
        textAreaKeyTyped();
        
        undo.discardAllEdits();
        updateUndoStates();
    }
    
    public String getText() {
        return textPane.getText();
    }
    
    // A few private utility methods
    
    private int getLineCount() {
        return textPane.getDocument().getDefaultRootElement().getElementCount();
    }
    
    private static final Pattern newLine = Pattern.compile("\\n");
    
    private int getCaretLine() {
        try {
            final String text = textPane.getText(0, textPane.getCaretPosition());
            return newLine.split(text, -1).length;
        } catch (BadLocationException ex) {
            log.log(Level.WARNING, ex.getMessage(), ex);
            return -1;
        }
    }
    
    private int getLineStartOffset(int line) {
        // From JEditTextArea, with modifications
        Element lineElement = textPane.getDocument().getDefaultRootElement().getElement(line);
        if (lineElement == null)
            return -1;
        else
            return lineElement.getStartOffset();
    }
    
    private int getLineEndOffset(int line) {
        // From JEditTextArea, with modifications
        Element lineElement = textPane.getDocument().getDefaultRootElement().getElement(line);
        if (lineElement == null)
            return -1;
        else
            return lineElement.getEndOffset();
    }
    
    private void getLineText(int lineIndex, Segment segment) {
        int start = getLineStartOffset(lineIndex);
        try {
            textPane.getDocument().getText(start, getLineEndOffset(lineIndex) - start - 1,segment);
        } catch (BadLocationException ex) {
            log.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
    
    private void cut() {
        textPane.cut();
    }
    
    private void copy() {
        textPane.copy();
    }
    
    private void paste() {
        textPane.paste();
    }
    
    private void undo() {
        try {
            undo.undo();
        } catch (CannotUndoException ex) {
            log.log(Level.WARNING, "Unable to undo", ex);
        }
        updateUndoStates();
        textAreaKeyTyped();
    }
    
    private void redo() {
        try {
            undo.redo();
        } catch (CannotRedoException ex) {
            log.log(Level.WARNING, "Unable to redo: ", ex);
        }
        updateUndoStates();
        textAreaKeyTyped();
    }
    
    public static void main(String[] args) {
        new EditorDialog(null, "Test", "country = {\n\ttag = ENG\n\tai = \"england.ai\"\n\tmajor = yes\n\tbadboy = 0\n}", null, null)
        .setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem findMenuItem;
    private javax.swing.JMenuItem goToMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem replaceMenuItem;
    javax.swing.JCheckBoxMenuItem setBackupFilesCheckBoxMenuItem;
    private javax.swing.JMenuItem setFontMenuItem;
    private javax.swing.JMenuItem showCountryListMenuItem;
    private javax.swing.JMenuItem showCultureListMenuItem;
    private javax.swing.JMenuItem showProvinceListMenuItem;
    private javax.swing.JLabel syntaxCheckLabel;
    protected javax.swing.JEditorPane textPane;
    private javax.swing.JMenuItem undoMenuItem;
    // End of variables declaration//GEN-END:variables
    
    // -------------------------------------------------------------------
    // Inner Classes
    // -------------------------------------------------------------------
    
    
    //<editor-fold defaultstate="collapsed" desc=" FontSelectorDialog ">
    private static final class FontSelectorDialog extends JDialog {
        private JList<String> nameList;
//        private JList<String> typeList;
        private JList<Integer> sizeList;
        private final JLabel testLabel;
        
        boolean closedOK = false;
        
        private String name;
        private final int type;
        private int size;
        
        private transient final ListSelectionListener listListener = (ListSelectionEvent e) -> {
            if (e.getSource() == nameList)
                name = nameList.getSelectedValue();
//                else if (e.getSource() == typeList)
//                    type = getType(typeList.getSelectedValue());
            else if (e.getSource() == sizeList)
                size = sizeList.getSelectedValue();
            else
                log.log(Level.FINER, "Not sure what to do with {0}", e);
            update();
        };
        
        FontSelectorDialog(JDialog parent, String family, int type, int size) {
            super(parent, "Font selector", true);
            
            setLayout(new BorderLayout());
            
            JPanel centerPanel = new JPanel(new BorderLayout(0, 5));
            
            JPanel upperPanel = new JPanel(/*new GridLayout(0, 3)*/);
            
            JPanel namePanel = new JPanel();
            namePanel.setBorder(BorderFactory.createTitledBorder("Name"));
            nameList = new JList<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            nameList.addListSelectionListener(listListener);
            namePanel.add(new JScrollPane(nameList));
            upperPanel.add(namePanel);
            
//            JPanel typePanel = new JPanel();
//            typePanel.setBorder(BorderFactory.createTitledBorder("Type"));
//            typeList = new JList(new String[] { "Plain", "Bold", "Italic", "Bold Italic" } );
//            typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//            typeList.addListSelectionListener(listListener);
//            typePanel.add(new JScrollPane(typeList));
//            upperPanel.add(typePanel);
            
            JPanel sizePanel = new JPanel();
            sizePanel.setBorder(BorderFactory.createTitledBorder("Size"));
            sizeList = new JList<>(new Integer[] { 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 22, 24, 28, 32, 36, 40 } );
            sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            sizeList.addListSelectionListener(listListener);
            sizePanel.add(new JScrollPane(sizeList));
            upperPanel.add(sizePanel);
            
            centerPanel.add(upperPanel, BorderLayout.CENTER);
            
            JPanel lowerPanel = new JPanel();
            lowerPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
            testLabel = new JLabel("Sample text");
            lowerPanel.add(testLabel);
            
            centerPanel.add(lowerPanel, BorderLayout.SOUTH);
            
            add(centerPanel, BorderLayout.CENTER);
            
            JPanel bottomPanel = new JPanel();
            
            final JButton okButton = new JButton("OK");
            final JButton cancelButton = new JButton("Cancel");
            
            ActionListener buttonListener = (ActionEvent e) -> {
                closedOK = (e.getSource() == okButton);
                dispose();
            };
            
            okButton.addActionListener(buttonListener);
            cancelButton.addActionListener(buttonListener);
            
            bottomPanel.add(okButton);
            bottomPanel.add(cancelButton);
            
            add(bottomPanel, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(parent);
            
            nameList.setSelectedValue(family, true);
//            typeList.setSelectedValue(getTypeName(type), true);
            sizeList.setSelectedValue(size, true);
            
            name = family;
            this.type = type;
            this.size = size;
            
            update();
        }
        
//        private int getType(String type) {
//            if (type == null)
//                return Font.PLAIN;
//
//            if (type.equalsIgnoreCase("plain"))
//                return Font.PLAIN;
//            else if (type.equalsIgnoreCase("bold"))
//                return Font.BOLD;
//            else if (type.equalsIgnoreCase("italic"))
//                return Font.ITALIC;
//            else if (type.equalsIgnoreCase("bold italic"))
//                return Font.BOLD | Font.ITALIC;
//            else
//                return Font.PLAIN;
//        }
//
//        private String getTypeName(int type) {
//            if ((type & Font.BOLD) != 0) {
//                // bold, now check italic
//                if ((type & Font.ITALIC) != 0)
//                    return "Bold Italic";
//                else
//                    return "Bold";
//            } else if ((type & Font.ITALIC) != 0) {
//                return "Italic";
//            } else {
//                return "Plain";
//            }
//        }
        
        private void update() {
            testLabel.setFont(getSelectedFont());
        }
        
        public Font getSelectedFont() {
            return new Font(name, type, size);
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc=" FindDialog ">
    private final class FindDialog extends JDialog {
        private final JTextField findField;
        private final JTextField replaceField;
        private final JRadioButton forwardButton;
        private final JRadioButton backwardButton;
        private final JCheckBox matchCaseCheckBox;
        FindDialog() {
            super(EditorDialog.this, "Find");
            
            setLayout(new BorderLayout());
            
            JPanel centerPanel = new JPanel(new GridLayout(0, 1));
            
            JPanel findPanel = new JPanel();
            findPanel.add(new JLabel("Find: "));
            findField = new JTextField(textPane.getSelectedText());
            findField.setPreferredSize(new java.awt.Dimension(100, 25));
            findField.addActionListener(new FindListener());
            findPanel.add(findField);
            centerPanel.add(findPanel);
            
            JPanel replacePanel = new JPanel();
            replacePanel.add(new JLabel("Replace: "));
            replaceField = new JTextField();
            replaceField.setPreferredSize(new java.awt.Dimension(100, 25));
            replaceField.setEnabled(false);
            replaceField.setToolTipText("Replace is not yet supported.");
            replacePanel.add(replaceField);
            centerPanel.add(replacePanel);
            
            add(centerPanel, BorderLayout.CENTER);
            
            JPanel lowerPanel = new JPanel();
            
            JPanel directionPanel = new JPanel();
            directionPanel.setBorder(BorderFactory.createTitledBorder("Direction"));
            
            ButtonGroup directionButtonGroup = new ButtonGroup();
            
            forwardButton = new JRadioButton("Forward", true);
            directionButtonGroup.add(forwardButton);
            directionPanel.add(forwardButton);
            
            backwardButton = new JRadioButton("Backward", false);
            backwardButton.setEnabled(false);
            backwardButton.setToolTipText("Backward searching is not yet supported.");
            directionButtonGroup.add(backwardButton);
            directionPanel.add(backwardButton);
            
            lowerPanel.add(directionPanel);
            
            matchCaseCheckBox = new JCheckBox("Match case");
            matchCaseCheckBox.setSelected(false);
            lowerPanel.add(matchCaseCheckBox);
            
            add(lowerPanel, BorderLayout.SOUTH);
            
            JPanel rightPanel = new JPanel();
            
            JButton findButton = new JButton(new FindListener());
            rightPanel.add(findButton);
            
            JButton replaceButton = new JButton("Replace");
            replaceButton.setEnabled(false);
            replaceButton.setToolTipText("Replace is not yet supported.");
            rightPanel.add(replaceButton);
            
            add(rightPanel, BorderLayout.EAST);
            
            pack();
            setLocationRelativeTo(EditorDialog.this);
        }
        
        private class FindListener extends AbstractAction {
            FindListener() {
                super("Find");
                putValue(ACCELERATOR_KEY,
                        KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
                putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean matchCase = matchCaseCheckBox.isSelected();
                final int lineCount = getLineCount();
                Segment textSegment = new Segment();
                String findText = findField.getText();
                if (!matchCase)
                    findText = findText.toLowerCase();
                
                if (forwardButton.isSelected()) {
                    for (int lineNum = 0; lineNum < lineCount; lineNum++) {
                        int lineStart = getLineStartOffset(lineNum);
                        if (lineNum < lineCount-1 && getLineStartOffset(lineNum+1) < textPane.getCaretPosition())
                            continue;

                        getLineText(lineNum, textSegment);
                        String lineText = textSegment.toString();
                        if (!matchCase)
                            lineText = lineText.toLowerCase();
                        if (lineText.contains(findText)) {
                            // try to figure out where it is
                            for (int i = 0; i < textSegment.count; i++) {
                                if (lineStart + i < textPane.getCaretPosition())
                                    continue;

                                String remainder = new String(textSegment.array, textSegment.offset+i, textSegment.count-i);
                                if (!matchCase)
                                    remainder = remainder.toLowerCase();
                                int idx = remainder.indexOf(findText);
                                if (idx >= 0) {
                                    idx += lineStart + i;
                                    textPane.setCaretPosition(idx);
                                    textPane.select(idx, idx + findText.length());
                                    return;
                                }
                            }
//                            log.log(Level.WARNING, "Something is wrong in FindDialog");
                        }
                    }
                } else {
                    // Backwards
//                    JOptionPane.showMessageDialog(EditorDialog.FindDialog.this,
//                            "Backwards search not yet supported.");
                    
                    lineLoop:
                        for (int line = lineCount-1; line >= 0; line--) {
                            // loop until the line start is before the caret
                            int lineStart = getLineStartOffset(line);
                            if (lineStart >= textPane.getCaretPosition())
                                continue;
                            
                            getLineText(line, textSegment);
                            if (textSegment.toString().contains(findText)) {
                                // try to figure out where it is
                                log.log(Level.FINEST, "Found on line {0}", line);
                                for (int i = textSegment.count-1; i >= 0; i--) {
                                    // loop until i is at or before the caret
                                    if (lineStart + i > textPane.getCaretPosition())
                                        continue;
                                    
                                    log.log(Level.FINEST, textSegment.toString());
                                    String remainder = new String(textSegment.array, textSegment.offset, textSegment.count-i); //text.subSequence(0, i).toString();
                                    log.log(Level.FINEST, "remainder = {0}", remainder);
                                    int idx = remainder.indexOf(findText);
                                    if (idx >= 0) {
                                        idx += lineStart + i;
                                        textPane.setCaretPosition(idx);
                                        textPane.select(idx, idx + findText.length());
//                                    break lineLoop;
                                        return;
                                    }
                                }
//                            log.log(Level.WARNING, "Something is wrong in FindDialog");
                            }
                        }
                }
                JOptionPane.showMessageDialog(FindDialog.this, "String '" + findText + "' not found.");
            }
        }
    }
    //</editor-fold>
    
    
    //<editor-fold defaultstate="collapsed" desc=" CountryListDialog ">
    private static final class CountryListDialog extends JDialog {
        CountryListDialog(java.awt.Frame parent, FilenameResolver resolver) {
            super(parent, "Countries", false);
            
            GenericObject countries =
                    EUGFileIO.load(resolver.resolveFilename("common/countries.txt"),
                    ParserSettings.getNoCommentSettings());
            if (countries == null)
                countries = EUGFileIO.loadAll(resolver.listFiles("common/country_tags"),
                        ParserSettings.getNoCommentSettings());
            
            String[][] tagNameTable = new String[countries.size()][2];
            int i = 0;
            for (eug.shared.ObjectVariable var : countries.values) {
                tagNameTable[i][0] = var.varname;
                tagNameTable[i][1] = Text.getText(var.varname);
                i++;
            }
            
            JTable countryTable = new JTable(tagNameTable, new String[] { "Tag", "Name" } );
            countryTable.setDefaultEditor(Object.class, null);
            
            countryTable.setAutoCreateRowSorter(true);
            
            setLayout(new BorderLayout());
            add(new JScrollPane(countryTable), BorderLayout.CENTER);
            
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
        }
        private static CountryListDialog dialog;
        public static void showDialog(java.awt.Frame parent, FilenameResolver resolver) {
            if (dialog == null) {
                dialog = new CountryListDialog(parent, resolver);
            }
            dialog.setVisible(true);
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc=" ProvinceListDialog ">
    private static final class ProvinceListDialog extends JDialog {
        ProvinceListDialog(java.awt.Frame parent, ProvinceData provinceData) {
            super(parent, "Provinces", false);
            
            ProvinceData.Province[] provs = provinceData.getAllProvs();
            
            String[][] provNameTable = new String[provs.length][3];
            int i = 0;
            boolean pti = true;
            for (ProvinceData.Province prov : provs) {
                provNameTable[i][0] = Integer.toString(prov.getId());
                provNameTable[i][1] = prov.getName();
                if (pti) {
                    provNameTable[i][2] = Text.getText("terra_incognita");
                    pti = false;
                } else {
                    provNameTable[i][2] = Text.getText("prov" + prov.getId());
                }
                i++;
            }
            
            JTable provTable = new JTable(provNameTable, new String[] { "ID", "Display name" } );
            provTable.setDefaultEditor(Object.class, null);
            
            provTable.setAutoCreateRowSorter(true);
            
            setLayout(new BorderLayout());
            add(new JScrollPane(provTable), BorderLayout.CENTER);
            
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
        }
        
        private static ProvinceListDialog dialog;
        public static void showDialog(java.awt.Frame parent, ProvinceData provinceData) {
            if (dialog == null) {
                dialog = new ProvinceListDialog(parent, provinceData);
            }
            dialog.setVisible(true);
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc=" CultureListDialog ">
    private static final class CultureListDialog extends JDialog {
        CultureListDialog(java.awt.Frame parent, FilenameResolver resolver) {
            super(parent, "Cultures", false);
            
            GenericObject cultureGroups =
                    EUGFileIO.load(resolver.resolveFilename("common/cultures.txt"),
                    ParserSettings.getNoCommentSettings());
            if (cultureGroups == null)
                cultureGroups = EUGFileIO.loadAll(resolver.listFiles("common/cultures"),
                        ParserSettings.getNoCommentSettings());
            
            // No easy way to do this with an array or a list, so use vector instead.
            Vector<Vector<String>> cultureGroupNameTable =
                    new Vector<Vector<String>>();
            if (!cultureGroups.lists.isEmpty()) {
                for (GenericList group : cultureGroups.lists) {
                    String groupName = group.getName();

                    for (String culture : group) {
                        Vector<String> vector = new Vector<String>(2);
                        vector.add(groupName);
                        vector.add(culture);
                        cultureGroupNameTable.add(vector);
                    }
                }
            } else if (!cultureGroups.children.isEmpty()) {
                for (GenericObject group : cultureGroups.children) {
                    String groupName = group.name;
                    
                    for (GenericObject culture : group.children) {
                        String cultureName = culture.name;
                        Vector<String> vector = new Vector<String>(2);
                        vector.add(groupName);
                        vector.add(cultureName);
                        cultureGroupNameTable.add(vector);
                    }
                }
            }
            
            Vector<String> labels = new Vector<String>(4);
            labels.add("Group");
            labels.add("Culture");
            
            JTable cultureTable = new JTable(cultureGroupNameTable, labels);
            cultureTable.setDefaultEditor(Object.class, null);
            
            cultureTable.setAutoCreateRowSorter(true);
            
            setLayout(new BorderLayout());
            add(new JScrollPane(cultureTable), BorderLayout.CENTER);
            
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
        }
        private static CultureListDialog dialog;
        public static void showDialog(java.awt.Frame parent, FilenameResolver resolver) {
            if (dialog == null) {
                dialog = new CultureListDialog(parent, resolver);
            }
            dialog.setVisible(true);
        }
    }
    //</editor-fold>
    
}
