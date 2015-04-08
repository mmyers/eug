/*
 * EditorUI.java
 *
 * Created on January 25, 2007, 5:00 PM
 */

package editor;

import editor.ProvinceData.Province;
import editor.mapmode.*;
import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.shared.Style;
import eug.specific.ck2.CK2DataSource;
import eug.specific.ck2.CK2Scenario;
import eug.specific.clausewitz.ClausewitzHistory;
import eug.specific.clausewitz.ClausewitzSaveGame;
import eug.specific.clausewitz.ClausewitzScenario;
import eug.specific.eu3.EU3SaveGame;
import eug.specific.eu3.EU3Scenario;
import eug.specific.victoria2.Vic2SaveGame;
import eug.specific.victoria2.Vic2Scenario;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.swing.*;

/**
 *
 * @author  Michael Myers
 */
public final class EditorUI extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(EditorUI.class.getName());
    
    private transient ProvinceData.Province currentProvince = null;
    
//    /** The province that is selected in the combo box at the left. */
//    private transient ProvinceData.Province selectedProvince;
    
    private JPopupMenu bookmarkMenu = new JPopupMenu("Bookmarks");

    private boolean isSavedGame = false;

    private GameVersion version;
    private FilenameResolver resolver;
    private Map map;
    private ProvinceData provinceData;
    
    /**
     * Creates new form EditorUI.
     */
    public EditorUI(String saveFile, GameVersion version, FilenameResolver resolver, boolean checkUpdates) {
        this.version = version;
        this.resolver = resolver;
        this.map = new Map(resolver, version);
        this.provinceData = new ProvinceData(map, resolver);
        initComponents();
        
        mapPanel.initialize(map, resolver, provinceData, version.isMapUpsideDown());
        pack();
        
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        mapPanel.centerMap();
        setDataSource(saveFile);
        addFilters();
        
        if (checkUpdates)
            checkForUpdates();
    }

    private boolean setStartDateOld(GenericObject defines) {
        GenericObject startDate = defines.getChild("start_date");
        if (startDate == null)
            return false;

        yearSpinner.setValue(startDate.getInt("year"));
        monthSpinner.setValue(startDate.getInt("month"));
        daySpinner.setValue(startDate.getInt("day"));
        return true;
    }

    private static final java.util.regex.Pattern DATE_PATTERN =
            java.util.regex.Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}");

    private boolean setStartDateNew(String startDate) {
        if (startDate == null || startDate.isEmpty())
            return false;
        java.util.regex.Matcher match = DATE_PATTERN.matcher(startDate);
        if (!match.find())
            return false;
        startDate = match.group();
        
        final String[] date = startDate.split("\\.");
        if (date.length < 3)
            return false;
        
        yearSpinner.setValue(Integer.parseInt(date[0]));
        monthSpinner.setValue(Integer.parseInt(date[1]));
        daySpinner.setValue(Integer.parseInt(date[2]));
        return true;
    }

    private boolean setStartDateLua(GenericObject definesLua) {
        if (definesLua.containsChild("NDefines"))
            definesLua = definesLua.getChild("NDefines");

        if (definesLua.containsChild("NGame"))
            definesLua = definesLua.getChild("NGame");

        if (definesLua.containsChild("defines"))
            definesLua = definesLua.getChild("defines");

        if (definesLua.contains("start_date")) {
            return setStartDateNew(definesLua.getString("start_date"));
        }
        return false;
    }
    
    private void setDataSource(String saveFile) {
        if (saveFile == null) {
            log.log(Level.INFO, "Loading province history...");

            ClausewitzScenario scen = null;
            if (version.getSaveType().equalsIgnoreCase("eu3"))
                scen = new EU3Scenario(resolver);
            else if (version.getSaveType().equalsIgnoreCase("victoria"))
                scen = new Vic2Scenario(resolver);
            else if (version.getSaveType().equalsIgnoreCase("ck2"))
                scen = new CK2Scenario(resolver);
            else {
                log.log(Level.WARNING, "Unknown or missing scenario type specified by config.");
                log.log(Level.WARNING, "Defaulting to EU3");
                scen = new EU3Scenario(resolver);
            }

            mapPanel.setDataSource(scen);
            log.log(Level.INFO, "Done.");
            FileEditorDialog.setDataSource(scen);

            if (!setStartDateNew(version.getStartDate())) {
                GenericObject defines =
                        EUGFileIO.load(resolver.resolveFilename("common/defines.txt"), defaultSettings);
                if (defines == null) {
                    defines = loadLuaFile(resolver.resolveFilename("common/defines.lua"));
                    setStartDateLua(defines);
                } else {
                    if (!setStartDateOld(defines))
                        setStartDateNew(defines.getString("start_date"));
                }
            }
            
            String date = yearSpinner.getValue().toString() + "." +
                    monthSpinner.getValue().toString() + "." +
                    daySpinner.getValue().toString();
            mapPanel.getModel().setDate(date);

            if (version.hasBookmarks())
                readBookmarks();
        } else {
            log.log(Level.INFO, "Loading saved game...");

            ClausewitzSaveGame save = null;
            if (version.getSaveType().equalsIgnoreCase("eu3"))
                save = EU3SaveGame.loadSaveGame(saveFile, resolver);
            else if (version.getSaveType().equalsIgnoreCase("victoria"))
                save = Vic2SaveGame.loadSaveGame(saveFile, resolver);
            else {
                log.log(Level.WARNING, "Unknown or missing save game type specified by config.");
                log.log(Level.WARNING, "Defaulting to EU3");
                save = EU3SaveGame.loadSaveGame(saveFile, resolver);
            }
            
            if (version.getName().equalsIgnoreCase("EU4"))
                save.setStyle(Style.EU4_SAVE_GAME);

            log.log(Level.INFO, "Done.");

            log.log(Level.INFO, "Loading province history...");
            mapPanel.setDataSource(save);
            log.log(Level.INFO, "Done.");

            FileEditorDialog.setDataSource(save);

            String[] date = save.getDate().split("\\.");
            ((SpinnerNumberModel)yearSpinner.getModel()).setMaximum(Integer.parseInt(date[0]));

            mapPanel.getModel().setDate(save.getDate());
            ((SpinnerNumberModel)yearSpinner.getModel()).setValue(Integer.parseInt(date[0]));
            ((SpinnerNumberModel)monthSpinner.getModel()).setValue(Integer.parseInt(date[1]));
            ((SpinnerNumberModel)daySpinner.getModel()).setValue(Integer.parseInt(date[2]));

            bookmarkMenu.add(new BookmarkAction("Game start", "The world before any history", "1.1.1"));
            bookmarkMenu.add(new BookmarkAction("Current year", "The current game year", save.getDate()));
            bookmarkMenu.pack();

            isSavedGame = true;
        }
    }
    
    private GenericObject loadLuaFile(String filename) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();

            while((line = reader.readLine()) != null) {
                int index = line.indexOf("--"); // remove Lua comments
                if (index > 0)
                    line = line.substring(0, index);
                stringBuilder.append(line).append('\n');
            }
            
            return EUGFileIO.loadFromString(stringBuilder.toString(), defaultSettings.setPrintWarnings(false));
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }
    
    private void readBookmarks() {
        final GenericObject bookmarks = loadFileOrFolder("common/bookmarks", "common/bookmarks.txt");
        
        if (bookmarks != null)
        {
            Collections.sort(bookmarks.children, new Comparator<GenericObject>() {
                private ClausewitzHistory.DateComparator dc = new ClausewitzHistory.DateComparator();
                @Override
                public int compare(GenericObject o1, GenericObject o2) {
                    return dc.compare(o1.getString("date"), o2.getString("date"));
                }
            });
            for (GenericObject bookmark : bookmarks.children) {
                bookmarkMenu.add(
                        new BookmarkAction(
                        Text.getText(bookmark.getString("name")) + " (" + bookmark.getString("date") + ")",
                        Text.getText(bookmark.getString("desc")),
                        bookmark.getString("date")
                        ));
            }
        }
        
        bookmarkMenu.pack();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        viewModeLabel = new javax.swing.JLabel();
        javax.swing.JToolBar.Separator jSeparator6 = new javax.swing.JToolBar.Separator();
        scaleColorsButton = new javax.swing.JButton();
        javax.swing.JSeparator jSeparator4 = new javax.swing.JSeparator();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        yearSpinner = new javax.swing.JSpinner();
        monthSpinner = new javax.swing.JSpinner();
        daySpinner = new javax.swing.JSpinner();
        setDateButton = new javax.swing.JButton();
        bookmarksButton = new javax.swing.JButton();
        javax.swing.JSeparator jSeparator5 = new javax.swing.JSeparator();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        zoomLabel = new javax.swing.JLabel();
        javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
        goToProvButton = new javax.swing.JButton();
        mapScrollPane = new javax.swing.JScrollPane();
        javax.swing.JPanel lowerPanel = new javax.swing.JPanel();
        provNameLabel = new javax.swing.JLabel();
        showProvHistButton = new javax.swing.JButton();
        ctryNameLabel = new javax.swing.JLabel();
        showCountryHistButton = new javax.swing.JButton();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        goToProvMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator3 = new javax.swing.JSeparator();
        setDateMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator7 = new javax.swing.JSeparator();
        reloadMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        checkVersionMenuItem = new javax.swing.JMenuItem();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Clausewitz Scenario Editor");
        addWindowListener(formListener);

        toolBar.setRollover(true);

        jLabel1.setText("Current view: ");
        toolBar.add(jLabel1);

        viewModeLabel.setText("Provinces");
        toolBar.add(viewModeLabel);
        toolBar.add(jSeparator6);

        scaleColorsButton.setText("Colors...");
        scaleColorsButton.setFocusable(false);
        scaleColorsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        scaleColorsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        scaleColorsButton.addActionListener(formListener);
        toolBar.add(scaleColorsButton);

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolBar.add(jSeparator4);

        jLabel2.setText("Date: ");
        toolBar.add(jLabel2);

        yearSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 50000, 1));
        yearSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(yearSpinner, "#####"));
        yearSpinner.setMaximumSize(new java.awt.Dimension(100, 30));
        yearSpinner.setPreferredSize(new java.awt.Dimension(60, 20));
        toolBar.add(yearSpinner);

        monthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 12, 1));
        monthSpinner.setMaximumSize(new java.awt.Dimension(100, 30));
        monthSpinner.setPreferredSize(new java.awt.Dimension(40, 20));
        toolBar.add(monthSpinner);

        daySpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 31, 1));
        daySpinner.setMaximumSize(new java.awt.Dimension(100, 30));
        daySpinner.setPreferredSize(new java.awt.Dimension(40, 20));
        toolBar.add(daySpinner);

        setDateButton.setAction(setDateAction);
        toolBar.add(setDateButton);

        bookmarksButton.setText("Bookmarks...");
        bookmarksButton.addActionListener(formListener);
        toolBar.add(bookmarksButton);

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolBar.add(jSeparator5);

        zoomInButton.setAction(zoomInAction);
        toolBar.add(zoomInButton);

        zoomOutButton.setAction(zoomOutAction);
        toolBar.add(zoomOutButton);

        updateZoomLabel();
        toolBar.add(zoomLabel);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator2.setPreferredSize(new java.awt.Dimension(2, 25));
        toolBar.add(jSeparator2);

        goToProvButton.setAction(goToProvAction);
        toolBar.add(goToProvButton);

        getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

        mapPanel.addMouseListener(formListener);
        mapPanel.addMouseMotionListener(formListener);
        mapScrollPane.setViewportView(mapPanel);

        getContentPane().add(mapScrollPane, java.awt.BorderLayout.CENTER);

        lowerPanel.add(provNameLabel);

        showProvHistButton.setText("Show province history");
        showProvHistButton.setEnabled(false);
        showProvHistButton.addActionListener(formListener);
        lowerPanel.add(showProvHistButton);
        lowerPanel.add(ctryNameLabel);

        showCountryHistButton.setText("Show country history");
        showCountryHistButton.setEnabled(false);
        showCountryHistButton.addActionListener(formListener);
        lowerPanel.add(showCountryHistButton);

        getContentPane().add(lowerPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(formListener);
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        toolsMenu.setMnemonic('T');
        toolsMenu.setText("Tools");

        zoomInMenuItem.setAction(zoomInAction);
        toolsMenu.add(zoomInMenuItem);

        zoomOutMenuItem.setAction(zoomOutAction);
        toolsMenu.add(zoomOutMenuItem);
        toolsMenu.add(jSeparator1);

        goToProvMenuItem.setAction(goToProvAction);
        toolsMenu.add(goToProvMenuItem);
        toolsMenu.add(jSeparator3);

        setDateMenuItem.setAction(setDateAction);
        toolsMenu.add(setDateMenuItem);
        toolsMenu.add(jSeparator7);

        reloadMenuItem.setText("Force a reload of all data");
        reloadMenuItem.addActionListener(formListener);
        toolsMenu.add(reloadMenuItem);

        menuBar.add(toolsMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");
        menuBar.add(viewMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(formListener);
        helpMenu.add(aboutMenuItem);

        checkVersionMenuItem.setText("Check for newer version");
        checkVersionMenuItem.addActionListener(formListener);
        helpMenu.add(checkVersionMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.MouseListener, java.awt.event.MouseMotionListener, java.awt.event.WindowListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == scaleColorsButton) {
                EditorUI.this.scaleColorsButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bookmarksButton) {
                EditorUI.this.bookmarksButtonActionPerformed(evt);
            }
            else if (evt.getSource() == showProvHistButton) {
                EditorUI.this.showProvHistButtonActionPerformed(evt);
            }
            else if (evt.getSource() == showCountryHistButton) {
                EditorUI.this.showCountryHistButtonActionPerformed(evt);
            }
            else if (evt.getSource() == exitMenuItem) {
                EditorUI.this.exitMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == reloadMenuItem) {
                EditorUI.this.reloadMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == aboutMenuItem) {
                EditorUI.this.aboutMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == checkVersionMenuItem) {
                EditorUI.this.checkVersionMenuItemActionPerformed(evt);
            }
        }

        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == mapPanel) {
                EditorUI.this.mapPanelMouseClicked(evt);
            }
        }

        public void mouseEntered(java.awt.event.MouseEvent evt) {
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
        }

        public void mousePressed(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == mapPanel) {
                EditorUI.this.mapPanelMousePressed(evt);
            }
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == mapPanel) {
                EditorUI.this.mapPanelMouseReleased(evt);
            }
        }

        public void mouseDragged(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == mapPanel) {
                EditorUI.this.mapPanelMouseClicked(evt);
            }
        }

        public void mouseMoved(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == mapPanel) {
                EditorUI.this.mapPanelMouseMoved(evt);
            }
        }

        public void windowActivated(java.awt.event.WindowEvent evt) {
        }

        public void windowClosed(java.awt.event.WindowEvent evt) {
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == EditorUI.this) {
                EditorUI.this.formWindowClosing(evt);
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
    
    private void bookmarksButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookmarksButtonActionPerformed
        bookmarkMenu.show(bookmarksButton, bookmarksButton.getWidth(), 0);
    }//GEN-LAST:event_bookmarksButtonActionPerformed
    
    private void mapPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelMouseReleased
        if (evt.isPopupTrigger()) {
            popupTriggered(evt);
        }
    }//GEN-LAST:event_mapPanelMouseReleased
    
    private void mapPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelMousePressed
        if (evt.isPopupTrigger()) {
            popupTriggered(evt);
        }
    }//GEN-LAST:event_mapPanelMousePressed
    
    private void showCountryHistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCountryHistButtonActionPerformed
        FileEditorDialog.showDialog(this, lastCountry, getLastCountryName(), resolver, provinceData);
    }//GEN-LAST:event_showCountryHistButtonActionPerformed
    
    /**
     * Cache the last province that the mouse was on, to keep from constantly
     * setting tooltip text.
     */
    private transient ProvinceData.Province lastProv = null;
    
    /**
     * Cache the last point, also.
     */
    private transient Point lastPt = null;
    
//    private transient Point lastClick = null;
    
    private transient String lastCountry = null;

    private String getLastCountryName() {
        if (mapPanel.getDataSource() instanceof CK2DataSource)
            return TitleMode.getTitleName(lastCountry, lastProv);
        else
            return Text.getText(lastCountry);
    }
    
    private void mapPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelMouseMoved
        updateMapTooltip(evt.getPoint());
    }//GEN-LAST:event_mapPanelMouseMoved
    
    private void updateMapTooltip(final Point mousePt) {
        if (mapPanel.getPreferredSize().height < mousePt.y || mapPanel.getPreferredSize().width < mousePt.x) {
            lastProv = null;
            mapPanel.setToolTipText(null);
            return;
        }
        
        final ProvinceData.Province prov = mapPanel.getProvinceAt(mousePt);
        
        if (prov == lastProv) {
            // no need to update anything
            return;
        }
        
        lastProv = prov;
        
        if (lastProv == null) {
            mapPanel.setToolTipText(null);
        } else {
            mapPanel.setToolTipTextForProv(lastProv);
        }
        lastPt = mousePt;
    }
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        doClose();
    }//GEN-LAST:event_formWindowClosing
    
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        Object[] options = { "Visit EU3 forum thread", "Visit EU4 forum thread", "Close" };
        
        int ret = JOptionPane.showOptionDialog(this, "Clausewitz Scenario Editor\nVersion " + Version.getCurrentVersion() + "\nBy MichaelM",
                "About", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[options.length-1]);
        
        if (ret == 2)
            return;
        
        String eu3Thread = "https://forum.paradoxplaza.com/forum/index.php?threads/clausewitz-save-game-and-scenario-editor-viewer.527308/";
        String eu4Thread = "https://forum.paradoxplaza.com/forum/index.php?threads/announcing-a-scenario-editor-and-map-viewer.707474/";
        
        String thread = (ret == 0 ? eu3Thread : eu4Thread);
        
        try {
            Desktop.getDesktop().browse(new java.net.URI(thread));
        } catch (URISyntaxException | IOException ex) {
            log.log(Level.SEVERE, "Could not browse to forum thread " + thread, ex);

            JOptionPane.showMessageDialog(this,
                    "Could not open browser. Please visit " + thread + " in your browser instead.",
                    "Update failed", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    private void showProvHistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showProvHistButtonActionPerformed
        FileEditorDialog.showDialog(EditorUI.this, currentProvince, resolver, provinceData);
//        mapPanel.getModel().getDataSource().reloadProvince(prov.getId());
//        mapPanel.repaint();
    }//GEN-LAST:event_showProvHistButtonActionPerformed
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        doClose();
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    private void resetScrollPane() {
        mapScrollPane.setViewport(mapScrollPane.getViewport());
//        mapPanel.unColorAll();
//        mapPanel.setToolTipText(null);
//        lastProv = null;
//        lastPt = null;
//        lastClick = null;
        updateMapTooltip(lastPt);
    }
    
    private void updateZoomLabel() {
        zoomLabel.setText("Scale factor: " + mapPanel.getScaleFactor());
    }
    
    private void mapPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelMouseClicked
//        provNameLabel.setText("Loading...");
        if (evt.isPopupTrigger()) {
            popupTriggered(evt);
            return;
        }
        
        showProvHistButton.setEnabled(false);
        final int clickCount = evt.getClickCount();
        
        java.awt.EventQueue.invokeLater(
                () -> {
                    doMouseClick(lastProv);
                    
                    // If it was a double click, go ahead and show an editor.
                    if (clickCount > 1) {
//                    Class c = mapPanel.getMode().getClass();
//                    if (c.equals(ProvinceMode.class)) {
                        showProvHistButton.doClick(0);
//                    } else if (c.equals(PoliticalMode.class) ||
//                            c.equals(CountryMode.class) ||
//                            c.equals(SingleCountryMode.class))  {
//                        showCountryHistButton.doClick(0);
//                    } else {
//                        // do what?
//                        showProvHistButton.doClick(0);
//                    }
                    }
        });
        
//        lastClick = evt.getPoint();
    }//GEN-LAST:event_mapPanelMouseClicked

    private void reloadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadMenuItemActionPerformed
        mapPanel.getDataSource().reloadProvinces();
        mapPanel.getDataSource().reloadCountries();
        repaint();
    }//GEN-LAST:event_reloadMenuItemActionPerformed

    private void scaleColorsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleColorsButtonActionPerformed
        if (mapPanel.getMode() instanceof DiscreteScalingMapMode) {
            ColorChooserDialog dialog = new ColorChooserDialog(this, (DiscreteScalingMapMode) mapPanel.getMode());
            dialog.setVisible(true);
        }
    }//GEN-LAST:event_scaleColorsButtonActionPerformed

    private void checkVersionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkVersionMenuItemActionPerformed
        if (Version.isNewVersionAvailable()) {
            Object[] options = { "Download", "Close" };

            int ret = JOptionPane.showOptionDialog(this,
                    "New version " + Version.getLatestVersion() + " available!\nDo you want to download it now?",
                    "New version!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (ret == 0) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(Version.getLatestVersionLink()));
                } catch (IOException | URISyntaxException ex) {
                    log.log(Level.SEVERE, "Could not browse to URI " + Version.getLatestVersionLink(), ex);

                    JOptionPane.showMessageDialog(this,
                            "Could not open browser. Please visit " + Version.getProjectURL() + " in your browser to get the latest version.",
                            "Update failed", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No new version available!");
        }
    }//GEN-LAST:event_checkVersionMenuItemActionPerformed
    
    private void checkForUpdates() {
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                log.log(Level.INFO, "Checking for new versions");
                return Version.isNewVersionAvailable();
            }

            @Override
            protected void done() {
                try {
                    if (get() == true) {
                        log.log(Level.INFO, "New version found: {0}", Version.getLatestVersion());
                        EditorUI.this.helpMenu.setBackground(Color.ORANGE);
                        EditorUI.this.helpMenu.setOpaque(true);
                        EditorUI.this.checkVersionMenuItem.setBackground(Color.ORANGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        };
        worker.execute();
    }
    
    private void doMouseClick(final ProvinceData.Province p) {
        currentProvince = p;
        
        if (p != null && p.getId() != 0) {
            mapPanel.flashProvince(p.getId(), 1);
        } else { // (p == null)
            provNameLabel.setText("No province");
            showProvHistButton.setEnabled(false);
            ctryNameLabel.setText("No country");
            showCountryHistButton.setEnabled(false);
            return;
        }
        
        provNameLabel.setText(p.getName());
        
        if (p.getId() != 0) {
            showProvHistButton.setEnabled(true);
            if (mapPanel.getModel().getDataSource() instanceof CK2DataSource) {
                lastCountry = mapPanel.getModel().getHistString(p.getId(), "title");
                if (mapPanel.getMode() instanceof TitleMode) {
                    TitleMode mode = ((TitleMode)mapPanel.getMode());
                    lastCountry = mode.getLiege(lastCountry);
                }
            } else
                lastCountry = mapPanel.getModel().getHistString(p.getId(), "owner");
        } else {
            showProvHistButton.setEnabled(false);
            lastCountry = "";
        }
        
        MapMode mode = mapPanel.getMode();
        if (!(mode instanceof ProvinceMode)) {
            if (lastCountry == null) {
                ctryNameLabel.setText("Province has no history file.");
                showCountryHistButton.setEnabled(false);
            } else if (lastCountry.length() == 0 || lastCountry.equals("none") || lastCountry.equals("XXX") || lastCountry.equals("---")) {
                ctryNameLabel.setText("No country");
                showCountryHistButton.setEnabled(false);
            } else {
                // Do some neat stuff if we're viewing a single country.
                if (mode instanceof CustomMode) {
                    final String name = ((CustomMode)mode).getName();
                    if (name.equals("owner")) {
                        mapPanel.setMode(new CustomMode(mapPanel, "owner", lastCountry));
                        viewModeLabel.setText("owner = " + lastCountry);
                        mapPanel.repaint();
                    } else if (name.equals("controller")) {
                        mapPanel.setMode(new CustomMode(mapPanel, "controller", lastCountry));
                        viewModeLabel.setText("controller = " + lastCountry);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof CoreMapMode) {
                    mapPanel.setMode(new CoreMapMode(mapPanel, lastCountry));
                    viewModeLabel.setText("Cores of " + lastCountry);
                    mapPanel.repaint();
                } else if (mode instanceof CustomCountryMode) {
                    final String name = ((CustomCountryMode)mode).getName();
                    final String owner = mapPanel.getModel().getHistString(p.getId(), "owner");
                    if (name.equals("government")) {
                        String government = mapPanel.getModel().getHistString(owner, name);
                        mapPanel.setMode(new CustomCountryMode(mapPanel, "government", government));
                        viewModeLabel.setText("government = " + government);
                        mapPanel.repaint();
                    } else if (name.equals("technology_group")) {
                        String tech = mapPanel.getModel().getHistString(owner, name);
                        mapPanel.setMode(new CustomCountryMode(mapPanel, "technology_group", tech));
                        viewModeLabel.setText("technology_group = " + tech);
                        mapPanel.repaint();
                    }
                }
                ctryNameLabel.setText(getLastCountryName());
                showCountryHistButton.setEnabled(true);
            }
            
            // Do some neat stuff if we're viewing a land province
            //if (Main.map.isLand(lastProv.getId())) {
                if (mode instanceof ContinentMode) {
                    String cont = map.getContinentOfProv(lastProv.getId());
                    if (!cont.equals("(none)")) {
                        mapPanel.setMode(new ContinentMode(mapPanel, cont));
                        viewModeLabel.setText("Provinces in " + cont);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof RegionsMode) {
                    String reg = map.getRegionsOfProv(lastProv.getId()).get(0);
                    if (!reg.equals("(none)")) {
                        mapPanel.setMode(new RegionsMode(mapPanel, reg));
                        viewModeLabel.setText("Provinces in " + reg);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof ClimateMode) {
                    String climate = map.getClimateOfProv(lastProv.getId());
                    if (!climate.equals("(none)")) {
                        mapPanel.setMode(new ClimateMode(mapPanel, climate));
                        viewModeLabel.setText("Provinces with climate " + climate);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof NativeGroupMode) {
                    String nativeType = map.getNativeTypeOfProv(lastProv.getId());
                    if (!nativeType.equals("(none)")) {
                        mapPanel.setMode(new NativeGroupMode(mapPanel, nativeType));
                        viewModeLabel.setText("Provinces with " + nativeType + " natives");
                        mapPanel.repaint();
                    }
                } else if (mode instanceof CustomMode && !(mode instanceof AdvancedCustomMode)) {
                    String name = ((CustomMode)mode).getName();
                    if (name.equals("trade_goods")) {
                        String goods = mapPanel.getModel().getHistString(p.getId(), name);
                        mapPanel.setMode(new CustomMode(mapPanel, name, goods));
                        viewModeLabel.setText("trade_goods = " + goods);
                        mapPanel.repaint();
                    } else if (name.equals("religion")) {
                        String religion = mapPanel.getModel().getHistString(p.getId(), name);
                        mapPanel.setMode(new CustomMode(mapPanel, "religion", religion));
                        viewModeLabel.setText("religion = " + religion);
                        mapPanel.repaint();
                    } else if (name.equals("culture")) {
                        String culture = mapPanel.getModel().getHistString(p.getId(), name);
                        mapPanel.setMode(new CustomMode(mapPanel, "culture", culture));
                        viewModeLabel.setText("culture = " + culture);
                        mapPanel.repaint();
                    } else {
                        // maybe some other types, too
                    }
                //}
            }
        } else {
            ctryNameLabel.setText("");
            showCountryHistButton.setEnabled(false);
        }
    }
    
    private void popupTriggered(java.awt.event.MouseEvent evt) {
//        // lastProv stores last province that mouse was on.
//        // if it's not null, try to make a menu.
//        if (lastProv == null)
//            return;
//
//        JPopupMenu popup = new JPopupMenu(lastProv.getName());
//        JMenuItem nameMenuItem = popup.add("<html><b>"+lastProv.getName()+"</b></html>");
//        nameMenuItem.setEnabled(false);
//        popup.addSeparator();
//        JMenuItem setCountryMenuItem = popup.add("Set country...");
//        JMenuItem setReligionMenuItem = popup.add("Set religion...");
////        setCountryMenuItem.addActionListener(new ActionListener() {
////            public void actionPerformed(ActionEvent e) {
////                String ctry = JOptionPane.showInputDialog(this, "Enter the new country:");
////
////            }
////        });
//        popup.setLocation(evt.getXOnScreen(), evt.getYOnScreen());
//        popup.setVisible(true);
    }
    
    
    private void doClose() {
        FileEditorDialog.disposeAll();
        if (mapPanel.getModel().getDataSource().hasUnsavedChanges()) {
            int choice =
                    JOptionPane.showConfirmDialog(this, "You have unsaved changes. Would you like to save them now?");
            if (choice == JOptionPane.CANCEL_OPTION)
                return;
            else if (choice == JOptionPane.YES_OPTION)
                mapPanel.getModel().getDataSource().saveChanges();
        }
        dispose();
        log.log(Level.INFO, "Exiting...");
    }
    
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        currentProvince = null;
        lastCountry = null;
        lastPt = null;
        lastProv = null;
//        lastClick = null;
    }
    
    private static final ParserSettings defaultSettings =
            ParserSettings.getNoCommentSettings().setPrintTimingInfo(false);
    
    private void addFilters() {
        GenericObject allViews = EUGFileIO.load("views.txt", defaultSettings);
        GenericObject allColors = EUGFileIO.load("colors.txt", defaultSettings);

        GenericList views = allViews.getList(version.getViewSet());
        if (views == null) {
            log.log(Level.WARNING, "No view set specified.");
            log.log(Level.WARNING, "Defaulting to EU3");
            views = allViews.getList("eu3");
        }

        for (String viewName : views) {
            String view = viewName.toLowerCase();
            if (view.equals("provinces")) {
                viewMenu.add(new ProvinceFilterAction());
            } else if (view.equals("countries")) {
                viewMenu.add(new PoliticalFilterAction());
            } else if (view.equals("titles")) {
                for (TitleMode.TitleType t : TitleMode.TitleType.values())
                    viewMenu.add(new TitleFilterAction(t));
            } else if (view.equals("country-menu")) {
                JMenu menu = new JMenu("Single country");
                addCountryFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("continents-menu")) {
                JMenu menu = new JMenu("Continents");
                addContinentFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("regions-menu") && version.hasRegions()) {
                JMenu menu = new JMenu("Regions");
                addRegionFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("colonial-regions")) {
                viewMenu.add(new ColonialRegionFilterAction());
            } else if (view.equals("climates-menu") && version.hasClimateTxt()) {
                JMenu menu = new JMenu("Climates");
                addClimateFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("province-religions")) {
                viewMenu.add(new ProvReligionFilterAction());
            } else if (view.equals("country-religions")) {
                viewMenu.add(new CtryReligionFilterAction());
            } else if (view.equals("religions")) {
                viewMenu.add(new ReligionFilterAction());
            } else if (view.equals("religions-menu")) {
                JMenu menu = new JMenu("Single religion");
                addReligionFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("buildings-menu")) {
                JMenu menu = new JMenu("Buildings");
                addBuildingFilters(menu, allColors);
                viewMenu.add(menu);
            } else if (view.equals("hoi3-buildings-menu")) {
                JMenu menu = new JMenu("Buildings");
                addHOI3BuildingFilters(menu, allColors);
                viewMenu.add(menu);
            } else if (view.equals("old-cultures-menu")) {
                JMenu menu = new JMenu("Cultures");
                addOldCultureFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("new-cultures-menu")) {
                JMenu menu = new JMenu("Cultures");
                addNewCultureFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("rome-cultures-menu")) {
                JMenu menu = new JMenu("Cultures");
                addRomeCultureFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("trade-goods")) {
                viewMenu.add(new GoodsFilterAction());
            } else if (view.equals("victoria-trade-goods")) {
                viewMenu.add(new GoodsFilterAction(true));
            } else if (view.equals("trade-goods-menu")) {
                JMenu menu = new JMenu("Single trade good");
                addGoodsFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("hoi3-goods-menu")) {
                JMenu menu = new JMenu("Province production levels");
                addHOI3GoodsFilters(menu, allColors);
                viewMenu.add(menu);
            } else if (view.equals("victoria-goods-menu") && isSavedGame) {
                JMenu menu = new JMenu("Single trade good");
                addVictoriaGoodsFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("governments-menu")) {
                JMenu menu = new JMenu("Governments");
                addGovernmentFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("tech-groups-menu")) {
                JMenu menu = new JMenu("Tech groups");
                addTechGroupFilters(menu);
                viewMenu.add(menu);
            } else if (view.equals("cots")) {
                viewMenu.add(new CustomFilterAction("Centers of trade", "cot", "yes"));
            } else if (view.equals("tradenodes")) {
                viewMenu.add(new TradeNodeFilterAction());
            } else if (view.equals("capitals")) {
                viewMenu.add(new CapitalFilterAction());
            } else if (view.equals("hre")) {
                viewMenu.add(new CustomFilterAction("Holy Roman Empire", "hre", "yes"));
            } else if (view.equals("base-tax")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Base tax value", "base_tax", 0, 18, 1);
                actn.setStepColors(allColors, view, Color.RED.darker(), Color.GREEN.darker(), Color.BLUE);
                viewMenu.add(actn);
            } else if (view.equals("population")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Population", "citysize", 0, 250000, 10000);
                actn.setStepColors(allColors, view, Color.LIGHT_GRAY, null, Color.GREEN.darker());
                viewMenu.add(actn);
            } else if (view.equals("rome-population")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Population", "population", 0, 100, 5);
                actn.setStepColors(allColors, view, Color.LIGHT_GRAY, null, Color.GREEN.darker());
                viewMenu.add(actn);
            } else if (view.equals("population-split")) {
                viewMenu.add(new PopSplitFilterAction());
            } else if (view.equals("manpower")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Manpower", "manpower", 0, 10, 1);
                actn.setStepColors(allColors, view);
                viewMenu.add(actn);
            } else if (view.equals("revolt-risk")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Revolt risk", "revolt_risk", 0, 10, 1);
                actn.setStepColors(allColors, view, Color.YELLOW, Color.ORANGE.darker(), Color.RED.darker());
                viewMenu.add(actn);
            } else if (view.equals("life-rating")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Life rating", "life_rating", 0, 50, 5);
                actn.setStepColors(allColors, view, Color.RED, null, Color.GREEN.darker());
                viewMenu.add(actn);
            } else if (view.equals("civilization-value")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Civilization value", "civilization_value", 0, 100, 5);
                actn.setStepColors(allColors, view);
                viewMenu.add(actn);
            } else if (view.equals("barbarian-power")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Barbarian power", "barbarian_power", 0, 10, 1);
                actn.setStepColors(allColors, view, Color.LIGHT_GRAY, null, Color.RED.darker());
                viewMenu.add(actn);
            } else if (view.equals("leadership")) {
                DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Leadership", "leadership", 0, 10, 1);
                actn.setStepColors(allColors, view);
                viewMenu.add(actn);
            } else if (view.equals("natives-menu")) {
                JMenu menu = new JMenu("Natives");
                addNativesFilters(menu, allColors);
                viewMenu.add(menu);
            } else if (view.equals("wars")) {
                viewMenu.add(new WarsAction());
            } else {
                log.log(Level.WARNING, "Unknown menu item: {0}", view);
            }
        }

        viewMenu.add(new JSeparator());
        viewMenu.add(new CustomMapModeAction());
        viewMenu.add(new PaintBordersAction());
    }
    
    private GenericObject loadFileOrFolder(String folderName, String fileName) {
        java.io.File folder = new java.io.File(resolver.resolveFilename(folderName));
        java.io.File file = new java.io.File(resolver.resolveFilename(fileName));
        if (folder.exists() && folder.isDirectory()) {
            return EUGFileIO.loadAll(resolver.listFiles(folderName), defaultSettings);
        } else if (file.exists()) {
            return EUGFileIO.load(file, defaultSettings);
        } else {
            return null;
        }
    }
    
    private void addReligionFilters(JMenu rootMenu) {
        final GenericObject religions = loadFileOrFolder("common/religions", "common/religion.txt");

        Collections.sort(religions.children, new ObjectComparator());
        
        final StringBuilder allReligions = new StringBuilder("(");
        
        for (GenericObject group : religions.children) {
            Collections.sort(group.children, new ObjectComparator());
            
            JMenu groupMenu = new JMenu(Text.getText(group.name));
            
            StringBuilder pattern = new StringBuilder(group.size()*10).append('(');
            
            for (GenericObject religion : group.children) {
                groupMenu.add(new CustomFilterAction(Text.getText(religion.name), "religion", religion.name));
                pattern.append(religion.name).append('|');
                allReligions.append(religion.name).append('|');
            }
            
            pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
            pattern.append(')');
            
            groupMenu.add(new MultiFilterAction("All " + Text.getText(group.name), "religion", pattern.toString()));
            
            rootMenu.add(groupMenu);
        }
        
        allReligions.deleteCharAt(allReligions.length()-1); // get rid of the last '|'
        allReligions.append(')');
        
        rootMenu.add(new MultiFilterAction("All religions (province)", "religion", allReligions.toString()));
        rootMenu.add(new MultiCountryFilterAction("All religions (country)", "religion", allReligions.toString()));
    }
    
    private void addBuildingFilters(JMenu rootMenu, GenericObject colors) {
        final GenericObject buildings = loadFileOrFolder("common/buildings", "common/buildings.txt");
        
        Collections.sort(buildings.children, new ObjectComparator());
        
        java.util.List<GenericObject> forts = new ArrayList<>();
        
        java.util.List<Action> capitalBuildings = new ArrayList<>();
        java.util.List<Action> manufactories = new ArrayList<>();
        java.util.List<Action> onePerCountry = new ArrayList<>();
        java.util.List<Action> other = new ArrayList<>();

        for (GenericObject building : buildings.children) {
            boolean isFort = building.hasString("fort_level");
            if (!isFort) {
                GenericObject mod = building.getChild("modifier");
                if (mod != null && mod.hasString("fort_level"))
                    isFort = true;
            }
            if (isFort) {
                forts.add(building);
                continue;
            }

            String display = Text.getText("building_" + building.name);
            if (display.equals("building_" + building.name))
                display = Text.getText(building.name);
            CustomFilterAction act = new CustomFilterAction(display, building.name, "yes");
            if (building.containsList("manufactory")) {
                manufactories.add(act);
            } else if (building.getBoolean("capital")) {
                capitalBuildings.add(act);
            } else if (building.getBoolean("one_per_country")) {
                onePerCountry.add(act);
            }  else {
                other.add(act);
            }
        }
        
        if (!manufactories.isEmpty()) {
            JMenu manufactoriesMenu = (JMenu) rootMenu.add(new JMenu("Manufactories"));
            for (Action a : manufactories)
                manufactoriesMenu.add(a);
        }
        if (!capitalBuildings.isEmpty()) {
            JMenu capitalBuildingsMenu = (JMenu) rootMenu.add(new JMenu("Capital buildings"));
            for (Action a : capitalBuildings)
                capitalBuildingsMenu.add(a);
        }
        if (!onePerCountry.isEmpty()) {
            JMenu uniqueMenu = (JMenu) rootMenu.add(new JMenu("Unique"));
            for (Action a : onePerCountry)
                uniqueMenu.add(a);
        }
        for (int i = 0; i < other.size(); i++) {
            rootMenu.add(other.get(i));
            if ((i+1) % 25 == 0)
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
        }

        if (!forts.isEmpty()) {
            FortLevelFilterAction actn = new FortLevelFilterAction(forts);
            actn.setStepColors(colors, "fort-level", Color.YELLOW, Color.GREEN.darker(), Color.BLUE.darker());
            rootMenu.add(actn);
        }
    }

    private void addHOI3BuildingFilters(JMenu rootMenu, GenericObject colors) {
        // HOI3 buildings are on a scale of 0-10 in each province
        final GenericObject buildings =
                EUGFileIO.load(resolver.resolveFilename("common/buildings.txt"),
                defaultSettings);

        Collections.sort(buildings.children, new ObjectComparator());

        for (GenericObject building : buildings.children) {
            DiscreteStepFilterAction act = new DiscreteStepFilterAction(Text.getText(building.name), building.name, 0, 10, 1);
            act.setStepColors(colors, "buildings");
            rootMenu.add(act);
        }
    }
    
    @SuppressWarnings("unchecked") // array of lists, can't use generics
    private void addCountryFilters(JMenu rootMenu) {
        final GenericObject countries = loadFileOrFolder("common/country_tags", "common/countries.txt");

        if (countries == null)
            return;
        
        Collections.sort(countries.values, new VariableComparator());
        
        @SuppressWarnings("rawtypes")
        List[] menus = new List[26];
        for (int i = 0; i < menus.length; i++) {
            menus[i] = new ArrayList<>();
        }
        List<JMenu> otherMenu = new ArrayList<>();
        
        for (ObjectVariable var : countries.values) {
            String cname = Text.getText(var.varname);
            char start = Character.toUpperCase(cname.charAt(0));
            
            JMenu menu = new JMenu(cname);
            menu.add(new CustomFilterAction("Owned", "owner", var.varname));
            menu.add(new CustomFilterAction("Controlled", "controller", var.varname));
            menu.add(new CoreFilterAction(var.varname));
            menu.add(new ShowCountryAction(var.varname, Text.getText(var.varname)));
            
            int idx = start - 'A';
            if (idx >= 0 && idx < menus.length)
                menus[idx].add(menu); // unchecked
            else
                otherMenu.add(menu);
        }
        
        for (int i = 0; i < menus.length; i++) {
            JMenu menu = new JMenu("  " + (char)('A' + i) + "  ");
            if (menus[i].isEmpty()) {
                JMenuItem dummy = new JMenuItem("(none)");
                dummy.setEnabled(false);
                menu.add(dummy);
            } else {
                for (Object m : menus[i]) {
                    menu.add((JMenu)m);
                }
            }
            rootMenu.add(menu);
        }
        
        if (!otherMenu.isEmpty()) {
            JMenu menu = new JMenu("Other");
            for (JMenu m : otherMenu) {
                menu.add(m);
            }
            rootMenu.add(menu);
        }
    }
    
    private void addOldCultureFilters(JMenu rootMenu) { // EU3 vanilla and NA
        final GenericObject cultures =
                EUGFileIO.load(resolver.resolveFilename("common/cultures.txt"),
                defaultSettings);
        
        Collections.sort(cultures.lists, new ListComparator());

        final Comparator<String> listSorter = new StringComparator();

        int counter = 0;
        for (GenericList group : cultures.lists) {
            group.sort(listSorter);

            JMenu groupMenu = new JMenu(Text.getText(group.getName()));

            if (group.size() > 0) {
                StringBuilder pattern = new StringBuilder(group.size()*10).append('(');

                for (String culture : group) {
                    groupMenu.add(new CustomFilterAction(Text.getText(culture), "culture", culture));
                    pattern.append(culture).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');

                groupMenu.add(new MultiFilterAction("All " + Text.getText(group.getName()), "culture", pattern.toString()));
            }

            rootMenu.add(groupMenu);
            if (counter++ > 25) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }

    private void addNewCultureFilters(JMenu rootMenu) { // In Nomine and newer, and also Victoria 2
        final GenericObject cultures = loadFileOrFolder("common/cultures", "common/cultures.txt");

        final ObjectComparator comp = new ObjectComparator();
        Collections.sort(cultures.children, comp);

        int counter = 0;
        for (GenericObject group : cultures.children) {
            Collections.sort(group.children, comp);

            JMenu groupMenu = new JMenu(Text.getText(group.name));

            if (!group.children.isEmpty()) {
                StringBuilder pattern = new StringBuilder(group.size()*10).append('(');

                for (GenericObject culture : group.children) {
                    groupMenu.add(new CustomFilterAction(Text.getText(culture.name), "culture", culture.name));
                    pattern.append(culture.name).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');

                groupMenu.add(new MultiFilterAction("All " + Text.getText(group.name), "culture", pattern.toString()));
            }

            rootMenu.add(groupMenu);
            if (counter++ > 25) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }

    private void addRomeCultureFilters(JMenu rootMenu) {
        final GenericObject cultures =
                EUGFileIO.load(resolver.resolveFilename("common/cultures.txt"),
                defaultSettings);

        final ObjectComparator comp = new ObjectComparator();
        Collections.sort(cultures.children, comp);

        for (GenericObject group : cultures.children) {
            GenericList groupCultures = group.getList("culture");
            groupCultures.sort();

            JMenu groupMenu = new JMenu(Text.getText(group.name));

            if (!group.children.isEmpty()) {
                StringBuilder pattern = new StringBuilder().append('(');

                for (String culture : groupCultures) {
                    groupMenu.add(new CustomFilterAction(Text.getText(culture), "culture", culture));
                    pattern.append(culture).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');
                
                groupMenu.add(new MultiFilterAction("All " + Text.getText(group.name), "culture", pattern.toString()));
            }

            rootMenu.add(groupMenu);
        }
    }
    
    private void addGoodsFilters(JMenu rootMenu) {
        final GenericObject goods = loadFileOrFolder("common/tradegoods", "common/tradegoods.txt");
        
        Collections.sort(goods.children, new ObjectComparator());
        
        int counter = 0;
        for (GenericObject good : goods.children) {
            rootMenu.add(new CustomFilterAction(Text.getText(good.name), "trade_goods", good.name));
            if (counter++ > 25) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }


    private void addVictoriaGoodsFilters(JMenu rootMenu) {
        final GenericObject goods =
                EUGFileIO.load(resolver.resolveFilename("common/goods.txt"),
                defaultSettings);

        Collections.sort(goods.children, new ObjectComparator());

        for (GenericObject group : goods.children) {
            JMenu groupMenu = new JMenu(Text.getText(group.name));
            rootMenu.add(groupMenu);
            Collections.sort(group.children, new ObjectComparator());

            int counter = 0;
            for (GenericObject good : group.children) {
                groupMenu.add(new CustomFilterAction(Text.getText(good.name), "trade_goods", good.name));
                if (counter++ > 25) {
                    groupMenu = (JMenu) groupMenu.add(new JMenu("More..."));
                    counter = 0;
                }
            }
        }
    }

    private void addHOI3GoodsFilters(JMenu rootMenu, GenericObject colors) {
        for (String good : new String[] { "metal", "energy", "rare_materials" }) {
            DiscreteStepFilterAction actn = new DiscreteStepFilterAction(Text.getText(good), good, 0, 30, 3);
            actn.setStepColors(colors, "resources");
            rootMenu.add(actn);
        }
    }
    
    private void addGovernmentFilters(JMenu rootMenu) {
        final GenericObject governments = loadFileOrFolder("common/governments", "common/governments.txt");
        
//        Collections.sort(governments.children, new ObjectComparator());
        final StringBuilder allGovernments = new StringBuilder("(");
        if (governments.size() > 20) {
            JMenu republicMenu = new JMenu("Republics");
            JMenu religiousMenu = new JMenu("Religious");
            JMenu normalMenu = new JMenu("Normal");
            
            JMenu tmpRepublic = republicMenu;
            JMenu tmpReligious = religiousMenu;
            JMenu tmpNormal = normalMenu;
            
            for (GenericObject government : governments.children) {
                allGovernments.append(government.name).append('|');
                FilterAction action =
                        new CustomCountryFilterAction(Text.getText(government.name), "government", government.name);
                
                if (government.getString("republican_name").equals("yes")) {
                    if (tmpRepublic.getMenuComponentCount() > 25)
                        tmpRepublic = (JMenu)tmpRepublic.add(new JMenu("More..."));
                    tmpRepublic.add(action);
                } else if (government.getString("religion").equals("yes")) {
                    if (tmpReligious.getMenuComponentCount() > 25)
                        tmpReligious = (JMenu)tmpReligious.add(new JMenu("More..."));
                    tmpReligious.add(action);
                } else {
                    if (tmpNormal.getMenuComponentCount() > 25)
                        tmpNormal = (JMenu)tmpNormal.add(new JMenu("More..."));
                    tmpNormal.add(action);
                }
            }
            
            rootMenu.add(republicMenu);
            rootMenu.add(religiousMenu);
            rootMenu.add(normalMenu);
            
        } else if (!governments.isEmpty()) {
            for (GenericObject government : governments.children) {
                allGovernments.append(government.name).append('|');
                rootMenu.add(new CustomCountryFilterAction(Text.getText(government.name), "government", government.name));
            }
        } else {
            return;
        }
        
        allGovernments.deleteCharAt(allGovernments.length()-1); // get rid of the last '|'
        allGovernments.append(')');
        
        rootMenu.add(new MultiCountryFilterAction("All governments", "government", allGovernments.toString()));
    }
    
    private void addTechGroupFilters(JMenu rootMenu) {
        GenericObject groups =
                EUGFileIO.load(resolver.resolveFilename("common/technology.txt"),
                defaultSettings);
        
        groups = groups.getChild("groups");
        
        if (!groups.values.isEmpty()) {
            for (ObjectVariable group : groups.values) {
                rootMenu.add(new CustomCountryFilterAction(Text.getText(group.varname), "technology_group", group.varname));
            }
        } else {
            // must be In Nomine
            for (GenericObject group : groups.children) {
                rootMenu.add(new CustomCountryFilterAction(Text.getText(group.name), "technology_group", group.name));
            }
        }
    }
    
    private void addContinentFilters(JMenu rootMenu) {
        for (String cont : map.getContinents().keySet()) {
            rootMenu.add(new ContinentFilterAction(Text.getText(cont), cont));
        }
    }
    
    private void addClimateFilters(JMenu rootMenu) {
        for (String climate : map.getClimates().keySet()) {
            rootMenu.add(new ClimateFilterAction(Text.getText(climate), climate));
        }
    }
    
    private void addRegionFilters(JMenu rootMenu) {
        java.util.Map<String, List<String>> regions = map.getRegions();
        List<String> regionNames = new ArrayList<>(regions.keySet());
        Collections.sort(regionNames, new StringComparator());
        
        int counter = 0;
        for (String region : regionNames) {
            rootMenu.add(new RegionFilterAction(Text.getText(region), region));
            if (counter++ > 25) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }
    
    private void addNativesFilters(JMenu rootMenu, GenericObject colors) {
        final GenericObject natives = loadFileOrFolder("common/natives", "common/natives.txt");
        map.setNatives(natives);
        
        JMenu nativeTypesMenu = new JMenu("Native types");
        for (String nativeType : map.getNatives().keySet()) {
            nativeTypesMenu.add(new NativesFilterAction(Text.getText(nativeType), nativeType));
        }
        rootMenu.add(nativeTypesMenu);
        DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Native size", "native_size", 0, 100, 5);
        actn.setStepColors(colors, "natives", Color.YELLOW, Color.ORANGE.darker(), Color.RED);
        rootMenu.add(actn);
        actn = new DiscreteStepFilterAction("Native ferocity", "native_ferocity", 0, 10, 1);
        actn.setStepColors(colors, "natives", Color.YELLOW, Color.ORANGE.darker(), Color.RED);
        rootMenu.add(actn);
        actn = new DiscreteStepFilterAction("Native hostility", "native_hostileness", 0, 10, 1);
        actn.setStepColors(colors, "natives", Color.YELLOW, Color.ORANGE.darker(), Color.RED);
        rootMenu.add(actn);
    }
    
// <editor-fold defaultstate="collapsed" desc=" Generated Variables ">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton bookmarksButton;
    javax.swing.JMenuItem checkVersionMenuItem;
    private javax.swing.JLabel ctryNameLabel;
    private javax.swing.JSpinner daySpinner;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JButton goToProvButton;
    private javax.swing.JMenuItem goToProvMenuItem;
    private javax.swing.JMenu helpMenu;
    private final editor.MapPanel mapPanel = new editor.MapPanel();
    private javax.swing.JScrollPane mapScrollPane;
    private javax.swing.JSpinner monthSpinner;
    private javax.swing.JLabel provNameLabel;
    javax.swing.JMenuItem reloadMenuItem;
    javax.swing.JButton scaleColorsButton;
    private javax.swing.JButton setDateButton;
    private javax.swing.JMenuItem setDateMenuItem;
    private javax.swing.JButton showCountryHistButton;
    private javax.swing.JButton showProvHistButton;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JLabel viewModeLabel;
    private javax.swing.JSpinner yearSpinner;
    private javax.swing.JButton zoomInButton;
    private javax.swing.JMenuItem zoomInMenuItem;
    private javax.swing.JLabel zoomLabel;
    private javax.swing.JButton zoomOutButton;
    private javax.swing.JMenuItem zoomOutMenuItem;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
    
    private final Action zoomInAction = new ZoomInAction();
    private final Action zoomOutAction = new ZoomOutAction();
    private final Action goToProvAction = new GoToProvAction();
    private final Action setDateAction = new SetDateAction();
    
    // Inner classes
    
    private static class ObjectComparator implements Comparator<GenericObject>, Serializable {
        private static final long serialVersionUID = 1L;
        public int compare(final GenericObject o1, final GenericObject o2) {
            return Text.getText(o1.name).compareTo(Text.getText(o2.name));
        }
    }
    
    private static class ListComparator implements Comparator<GenericList>, Serializable {
        private static final long serialVersionUID = 1L;
        public int compare(final GenericList o1, final GenericList o2) {
            return Text.getText(o1.getName()).compareTo(Text.getText(o2.getName()));
        }
    }
    
    private static class VariableComparator implements Comparator<ObjectVariable>, Serializable {
        private static final long serialVersionUID = 1L;
        public int compare(final ObjectVariable o1, final ObjectVariable o2) {
            return Text.getText(o1.varname).compareTo(Text.getText(o2.varname));
        }
    }
    
    private static class StringComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 1L;
        public int compare(final String o1, final String o2) {
            return Text.getText(o1).compareTo(Text.getText(o2));
        }
    }
    
    // Actions
    
    private class ZoomInAction extends AbstractAction {
        public ZoomInAction() {
            super("Zoom in", new javax.swing.ImageIcon(EditorUI.this.getClass().getResource("/editor/zoomin.gif")));
            enabled = true;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('=', InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        }
        public void actionPerformed(ActionEvent e) {
            mapPanel.zoomIn();
            resetScrollPane();
            updateZoomLabel();
            
            if (currentProvince != null)
                mapPanel.goToProv(currentProvince.getId());
        }
    }
    
    private class ZoomOutAction extends AbstractAction {
        public ZoomOutAction() {
            super("Zoom out", new javax.swing.ImageIcon(EditorUI.this.getClass().getResource("/editor/zoomout.gif")));
            enabled = true;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('-', InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }
        public void actionPerformed(ActionEvent e) {
            mapPanel.zoomOut();
            resetScrollPane();
            updateZoomLabel();
            
            if (currentProvince != null)
                mapPanel.goToProv(currentProvince.getId());
        }
    }
    
    private class GoToProvAction extends AbstractAction {
        public GoToProvAction() {
            super("Go to province");
            enabled = true;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_G);
        }
        public void actionPerformed(ActionEvent e) {
            final int maxProv = Integer.parseInt(map.getString("max_provinces")) - 1;
            String response =
                    JOptionPane.showInputDialog(EditorUI.this,
                    "Enter a province ID number (between 1 and " + maxProv +
                    ") or a province name:",
                    currentProvince == null ? null : currentProvince.getId());
            
            if (response == null || response.length() == 0)
                return;
            response = response.trim();
            
            Province p;
            
            if (response.matches("[0-9]{1,4}")) {
                int prov = Integer.parseInt(response);
                if (prov <= 0 || prov > maxProv) {
                    JOptionPane.showMessageDialog(EditorUI.this, "Invalid province ID: " + response);
                    return;
                }
                
                p = mapPanel.getModel().getProvinceData().getProvByID(prov);
            } else {
                p = mapPanel.getModel().getProvinceData().getProvByName(response);
            }
            
            if (p == null) {
                JOptionPane.showMessageDialog(EditorUI.this, "Cannot find province \"" + response + "\"");
                return;
            }
            
            resetScrollPane();
            mapPanel.goToProv(p.getId());
            mapPanel.flashProvince(p.getId());
            doMouseClick(p);
        }
    }
    
    private class SetDateAction extends AbstractAction {
        public SetDateAction() {
            super("Set date");
            enabled = true;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
        }
        public void actionPerformed(ActionEvent e) {
            String date = yearSpinner.getValue().toString() + "." +
                    monthSpinner.getValue().toString() + "." +
                    daySpinner.getValue().toString();
            log.log(Level.FINE, "Setting date to {0}", date);
            mapPanel.getModel().setDate(date);
//            ((ProvinceEditorPanel)innerProvincePanel).setDate(date);
            mapPanel.repaint();
        }
    }
    
    private class ShowCountryAction extends AbstractAction {
        private final String tag;
        private final String name;
        public ShowCountryAction(String tag, String name) {
            super("Show history");
            this.tag = tag;
            this.name = name;
            putValue(SHORT_DESCRIPTION, "Edit " + name + "'s country history file");
        }
        public void actionPerformed(ActionEvent e) {
            FileEditorDialog.showDialog(EditorUI.this, tag, name, resolver, provinceData);
        }
    }
    
    private abstract class FilterAction extends AbstractAction {
        protected MapMode mode;
        protected FilterAction(String name, MapMode mode) {
            super(name);
            this.mode = mode;
        }
        
        public void actionPerformed(ActionEvent e) {
            mapPanel.setMode(mode);
            viewModeLabel.setText((String) getValue(SHORT_DESCRIPTION));
            mapPanel.repaint();
        }
    }
    
    private class CustomFilterAction extends FilterAction {
        public CustomFilterAction(String name, String key, String value) {
            super(name, new CustomMode(mapPanel, key, value));
            putValue(SHORT_DESCRIPTION, key + " = " + value);
        }
    }
    
    private class CustomCountryFilterAction extends FilterAction {
        public CustomCountryFilterAction(String name, String key, String value) {
            super(name, new CustomCountryMode(mapPanel, key, value));
            putValue(SHORT_DESCRIPTION, key + " = " + value);
        }
    }
    
    private class MultiFilterAction extends FilterAction {
        public MultiFilterAction(String name, String key, String pattern) {
            super(name, new AdvancedCustomMode(mapPanel, key, pattern));
            if (pattern.length() > 60)
                putValue(SHORT_DESCRIPTION, name);
            else
                putValue(SHORT_DESCRIPTION, key + " matches " + pattern);
        }
    }
    
    private class MultiCountryFilterAction extends FilterAction {
        public MultiCountryFilterAction(String name, String key, String pattern) {
            super(name, new AdvancedCustomCountryMode(mapPanel, key, pattern));
            if (pattern.length() > 60)
                putValue(SHORT_DESCRIPTION, name);
            else
                putValue(SHORT_DESCRIPTION, key + " matches " + pattern);
        }
    }
    
    private class ProvinceFilterAction extends FilterAction {
        public ProvinceFilterAction() {
            super("Provinces", new ProvinceMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Provinces");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('P', InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        }
    }
    
    private class CountryFilterAction extends FilterAction {
        public CountryFilterAction() {
            super("Countries", new CountryMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Countries");
        }
    }
    
    private class PoliticalFilterAction extends FilterAction {
        public PoliticalFilterAction() {
            super("Countries", new PoliticalMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Countries");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        }
    }

    private class TitleFilterAction extends FilterAction {
        public TitleFilterAction(TitleMode.TitleType type) {
            super(type.getName() + " Titles", new TitleMode(mapPanel, type));
            putValue(SHORT_DESCRIPTION, type.getName() + " Titles");
        }
    }
    
    private class ProvReligionFilterAction extends FilterAction {
        public ProvReligionFilterAction() {
            super("Province religions", new ProvReligionMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Province religions");
        }
    }
    
    private class CtryReligionFilterAction extends FilterAction {
        public CtryReligionFilterAction() {
            super("Country religions", new CtryReligionMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Country religions");
        }
    }
    
    private class ReligionFilterAction extends FilterAction {
        public ReligionFilterAction() {
            super("Religions", new ReligionMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Religions");
        }
    }
    
    private class GoodsFilterAction extends FilterAction {
        public GoodsFilterAction() {
            this(false);
        }
        public GoodsFilterAction(boolean isVictoria) {
            super("Trade goods", new GoodsMode(mapPanel, isVictoria, resolver));
            putValue(SHORT_DESCRIPTION, "Trade goods");
        }
    }
    
    private class CoreFilterAction extends FilterAction {
        public CoreFilterAction(String tag) {
            super("Core", new CoreMapMode(mapPanel, tag));
            putValue(SHORT_DESCRIPTION, "Cores of " + tag);
        }
    }

    private class TradeNodeFilterAction extends FilterAction {
        public TradeNodeFilterAction() {
            super("Trade nodes", new TradeMode(mapPanel, resolver));
            putValue(SHORT_DESCRIPTION, "Trade nodes");
        }
    }
    
    private class DiscreteStepFilterAction extends FilterAction {
        public DiscreteStepFilterAction(String name, String prop, int min, int max, int step) {
            super(name, new DiscreteScalingMapMode(mapPanel, prop, min, max, step));
            putValue(SHORT_DESCRIPTION, name);
        }

        protected DiscreteStepFilterAction(String name) {
            super(name, null);
            putValue(SHORT_DESCRIPTION, name);
        }

        private void setMinColor(Color c) {
            ((DiscreteScalingMapMode)mode).setMinColor(c);
        }

        private void setMidColor(Color c) {
            ((DiscreteScalingMapMode)mode).setMidColor(c);
        }

        private void setMaxColor(Color c) {
            ((DiscreteScalingMapMode)mode).setMaxColor(c);
        }

        public void setStepColors(GenericObject allColors, String name) {
            setStepColors(allColors, name, null, null, null);
        }

        public void setStepColors(GenericObject allColors, String name,
                Color defaultMin, Color defaultMid, Color defaultMax) {
            if (defaultMin != null)
                setMinColor(defaultMin);
            if (defaultMid != null)
                setMidColor(defaultMid);
            if (defaultMax != null)
                setMaxColor(defaultMax);

            GenericObject colors = allColors.getChild(name);
            if (colors != null) {
                GenericList min = colors.getList("low");
                if (min != null && min.size() >= 3) {
                    Color c = new Color(Integer.parseInt(min.get(0)), Integer.parseInt(min.get(1)), Integer.parseInt(min.get(2)));
                    setMinColor(c);
                }
                GenericList mid = colors.getList("middle");
                if (mid != null && mid.size() >= 3) {
                    Color c = new Color(Integer.parseInt(mid.get(0)), Integer.parseInt(mid.get(1)), Integer.parseInt(mid.get(2)));
                    setMidColor(c);
                }
                GenericList high = colors.getList("high");
                if (high != null && high.size() >= 3) {
                    Color c = new Color(Integer.parseInt(high.get(0)), Integer.parseInt(high.get(1)), Integer.parseInt(high.get(2)));
                    setMaxColor(c);
                }
            }
            ((DiscreteScalingMapMode)mode).setName(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // reload colors since they might have been changed by another
            // mapmode that uses the same settings
            GenericObject allColors = EUGFileIO.load("colors.txt", defaultSettings);
            GenericObject colors = allColors.getChild(((DiscreteScalingMapMode)mode).getName());
            if (colors != null)
                setStepColors(allColors, ((DiscreteScalingMapMode)mode).getName());

            super.actionPerformed(e);
        }
    }
    
    private class FortLevelFilterAction extends DiscreteStepFilterAction {
        public FortLevelFilterAction(final java.util.List<GenericObject> forts) {
            super("Fort level");
            this.mode = new FortMapMode(mapPanel, forts);
        }
    }
    
//    private class GroupFilterAction extends FilterAction {
//        public GroupFilterAction(String name, final java.util.List<String> group) {
//            super(name, new GroupMode(mapPanel, group));
//            putValue(SHORT_DESCRIPTION, "Provinces in " + name);
//        }
//    }
    
    private class ContinentFilterAction extends FilterAction {
        public ContinentFilterAction(String name, String contName) {
            super(name, new ContinentMode(mapPanel, contName));
            putValue(SHORT_DESCRIPTION, "Provinces in " + contName);
        }
    }
    
    private class ClimateFilterAction extends FilterAction {
        public ClimateFilterAction(String name, String climateName) {
            super(name, new ClimateMode(mapPanel, climateName));
            putValue(SHORT_DESCRIPTION, "Provinces with climate " + climateName);
        }
    }
    
    private class RegionFilterAction extends FilterAction {
        public RegionFilterAction(String name, String regName) {
            super(name, new RegionsMode(mapPanel, regName));
            putValue(SHORT_DESCRIPTION, "Provinces in " + regName);
        }
    }
    
    private class ColonialRegionFilterAction extends FilterAction {
        public ColonialRegionFilterAction() {
            super("Colonial regions", new ColonialRegionsMode(mapPanel, resolver));
            putValue(SHORT_DESCRIPTION, "Colonial Regions & Trade Companies");
        }
    }
    
    private class CapitalFilterAction extends FilterAction {
        public CapitalFilterAction() {
            super("Capitals", new CapitalsMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Capitals");
        }
    }
    
    private class NativesFilterAction extends FilterAction {
        public NativesFilterAction(String name, String nativeType) {
            super(name, new NativeGroupMode(mapPanel, nativeType));
            putValue(SHORT_DESCRIPTION, "Provinces with " + nativeType + " natives");
        }
    }

    private class PopSplitFilterAction extends FilterAction {
        public PopSplitFilterAction() {
            super("Population split", new PopSplitMapMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Population split");
        }
    }

    private class WarsAction extends AbstractAction {
        public WarsAction() {
            super("Wars...");
        }

        public void actionPerformed(ActionEvent e) {
            WarsDialog dlg = new WarsDialog(EditorUI.this, mapPanel.getDataSource(), resolver, provinceData);
            dlg.setVisible(true);
        }
    }

    private class CustomMapModeAction extends AbstractAction {
        public CustomMapModeAction() {
            super("Custom map mode");
        }

        public void actionPerformed(ActionEvent e) {
            final MapMode mode = CustomModeDialog.showDialog(EditorUI.this);
            if (mode != null) {
                mode.setMapPanel(mapPanel);
                mapPanel.setMode(mode);
                viewModeLabel.setText(mode.toString());
                mapPanel.repaint();
            }
        }
    }

    private class PaintBordersAction extends AbstractAction {
        PaintBordersAction() {
            super("Toggle borders");
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('B', InputEvent.CTRL_DOWN_MASK));
        }
        public void actionPerformed(ActionEvent e) {
            mapPanel.setPaintBorders(!mapPanel.isPaintBorders());
            mapPanel.repaint();
        }
    }

    
    private final class BookmarkAction extends AbstractAction {
        private final String date;
        public BookmarkAction(String name, String desc, String date) {
            super(name);
            this.date = date;
            
            // split desc into lines
            final StringBuilder sb = new StringBuilder("<html>");
            final int len = desc.length();
            int last = 0;
            // Try to get 80-character lines
            for (int i = 80; i < len; i+=80) {
                // Go to the end of the current word
                while (desc.charAt(i) != ' ' && i<len-1)
                    i++;
                sb.append(desc.substring(last, i));
                sb.append("<br>");
                last = i;
            }
            // If len wasn't divisible by 80, we didn't get all of the desc in the loop.
            sb.append(desc.substring(last)).append("<br>");
            
            sb.append("<br>Date: ").append(date).append("</html>");
            putValue(SHORT_DESCRIPTION, sb.toString());
        }
        public void actionPerformed(ActionEvent e) {
            String[] splitDate = date.split("\\.");
            ((SpinnerNumberModel)yearSpinner.getModel()).setValue(Integer.parseInt(splitDate[0]));
            ((SpinnerNumberModel)monthSpinner.getModel()).setValue(Integer.parseInt(splitDate[1]));
            ((SpinnerNumberModel)daySpinner.getModel()).setValue(Integer.parseInt(splitDate[2]));
            mapPanel.getModel().setDate(date);
            mapPanel.repaint();
        }
    }
    
}
