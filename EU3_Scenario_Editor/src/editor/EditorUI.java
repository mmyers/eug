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
import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.shared.Style;
import eug.specific.eu3.EU3SaveGame;
import eug.specific.eu3.EU3Scenario;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author  Michael Myers
 * @version 1.0
 */
public final class EditorUI extends javax.swing.JFrame {
    
    protected transient ProvinceData.Province currentProvince = null;
    
//    /** The province that is selected in the combo box at the left. */
//    private transient ProvinceData.Province selectedProvince;
    
    private static final String VERSION = "0.6.1";
    
    private JPopupMenu bookmarkMenu = new JPopupMenu("Bookmarks");
    
    /**
     * Creates new form EditorUI.
     */
    public EditorUI() {
//        screen = this.getGraphicsConfiguration().getBounds();
        initComponents();
        
        class PaintBordersAction extends AbstractAction {
            PaintBordersAction() {
                super("Toggle borders");
                putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('B', InputEvent.CTRL_DOWN_MASK));
            }
            public void actionPerformed(ActionEvent e) {
                mapPanel.setPaintBorders(!mapPanel.isPaintBorders());
                mapPanel.repaint();
            }
        }
        
        viewMenu.add(new PaintBordersAction());
        
        mapPanel.initialize();
        pack();
        
        if (Main.map.isInNomine()) {
            viewRegionMenu.setEnabled(true);
        }
        
        // Make sure the window isn't too big
        final java.awt.Rectangle bounds = getGraphicsConfiguration().getBounds();
        setSize(Math.min(getWidth(), bounds.width), Math.min(getHeight(), bounds.height));
        
        mapPanel.centerMap();
        setDataSource();
        addFilters();
        viewProvincesMenuItem.doClick();
    }
    
    private void setDataSource() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to load a saved game?");
        if (choice == JOptionPane.NO_OPTION) {
            System.out.println("Loading province history...");
            EU3Scenario scen = new EU3Scenario(Main.filenameResolver);
            mapPanel.setDataSource(scen);
            System.out.println("Done.");
            FileEditorDialog.setDataSource(scen);
            
            GenericObject defines =
                    EUGFileIO.load(Main.filenameResolver.resolveFilename("common/defines.txt"), defaultSettings);
            
            if (defines.containsChild("start_date")) {
                GenericObject startDate = defines.getChild("start_date");
                yearSpinner.setModel(new SpinnerNumberModel(startDate.getInt("year"), 0, 50000, 1));
                monthSpinner.setModel(new SpinnerNumberModel(startDate.getInt("month"), 0, 12, 1));
                daySpinner.setModel(new SpinnerNumberModel(startDate.getInt("day"), 0, 31, 1));
            } else {
                // must not be In Nomine
                yearSpinner.setModel(new SpinnerNumberModel(1453, 0, 50000, 1));
                monthSpinner.setModel(new SpinnerNumberModel(5, 0, 12, 1));
                daySpinner.setModel(new SpinnerNumberModel(29, 0, 31, 1));
            }
            
            readBookmarks();
        } else if (choice == JOptionPane.CANCEL_OPTION) {
            dispose();
            System.exit(0);
        } else {
            JFileChooser chooser = new JFileChooser(Main.filenameResolver.getModDirName() + "/save games");
            chooser.setFileFilter(new SaveGameFileFilter());
            int secondChoice = chooser.showOpenDialog(this);
            if (secondChoice == JFileChooser.APPROVE_OPTION) {
                System.out.println("Loading saved game...");
                EU3SaveGame save = EU3SaveGame.loadSaveGame(
                        chooser.getSelectedFile().getAbsolutePath(),
                        Main.filenameResolver);
                System.out.println("Done.");
                
                System.out.println("Loading province history...");
                mapPanel.setDataSource(save);
                System.out.println("Done.");
                
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
            } else {
                // do it again
                setDataSource();
            }
        }
    }
    
    private void readBookmarks() {
        final GenericObject bookmarks =
                EUGFileIO.load(
                Main.filenameResolver.resolveFilename("common/bookmarks.txt"),
                ParserSettings.getNoCommentSettings().setPrintTimingInfo(false)
                );
        
        for (GenericObject bookmark : bookmarks.children) {
            bookmarkMenu.add(
                    new BookmarkAction(
                    Text.getText(bookmark.getString("name")),
                    Text.getText(bookmark.getString("desc")),
                    bookmark.getString("date")
                    ));
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

        viewModeButtonGroup = new javax.swing.ButtonGroup();
        javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        viewModeLabel = new javax.swing.JLabel();
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
        viewProvincesMenuItem = new javax.swing.JMenuItem();
        viewCountriesMenuItem = new javax.swing.JMenuItem();
        viewSingleCountryMenu = new javax.swing.JMenu();
        viewContinentMenu = new javax.swing.JMenu();
        viewRegionMenu = new javax.swing.JMenu();
        viewClimateMenu = new javax.swing.JMenu();
        viewProvReligionsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewCtryReligionsMenuItem = new javax.swing.JMenuItem();
        viewSingleReligionMenu = new javax.swing.JMenu();
        viewBuildingMenu = new javax.swing.JMenu();
        viewCultureMenu = new javax.swing.JMenu();
        viewGoodsMenuItem = new javax.swing.JMenuItem();
        viewSingleGoodMenu = new javax.swing.JMenu();
        viewSingleGovernmentMenu = new javax.swing.JMenu();
        viewTechGroupMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem viewCOTMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewHREMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewBaseTaxMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewPopulationMenuItem = new javax.swing.JMenuItem();
        viewManpowerMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewRevoltRiskMenuItem = new javax.swing.JMenuItem();
        final DiscreteStepFilterAction act = new DiscreteStepFilterAction("Revolt risk", "revolt_risk", 0, 10, 1);
        //act.setMinColor(java.awt.Color.GREEN);
        //act.setMaxColor(java.awt.Color.RED);
        viewRevoltRiskMenuItem.setAction(act);
        javax.swing.JMenuItem viewCapitalsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu viewNativesMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem viewNativeSizeMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewNativeFerocityMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem viewNativeHostilenessMenuItem = new javax.swing.JMenuItem();
        viewNativeTypesMenu = new javax.swing.JMenu();
        warsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator6 = new javax.swing.JSeparator();
        customMapModeMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("EU3 Scenario Editor");
        addWindowListener(formListener);

        toolBar.setRollover(true);

        jLabel1.setText("Current view: ");
        toolBar.add(jLabel1);
        toolBar.add(viewModeLabel);

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolBar.add(jSeparator4);

        jLabel2.setText("Date: ");
        toolBar.add(jLabel2);

        yearSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(yearSpinner, "#####"));
        yearSpinner.setMaximumSize(new java.awt.Dimension(100, 30));
        yearSpinner.setPreferredSize(new java.awt.Dimension(60, 20));
        toolBar.add(yearSpinner);

        monthSpinner.setMaximumSize(new java.awt.Dimension(100, 30));
        monthSpinner.setPreferredSize(new java.awt.Dimension(40, 20));
        toolBar.add(monthSpinner);

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

        viewProvincesMenuItem.setAction(provinceFilterAction);
        viewMenu.add(viewProvincesMenuItem);

        viewCountriesMenuItem.setAction(countryFilterAction);
        viewMenu.add(viewCountriesMenuItem);

        viewSingleCountryMenu.setText("Single country");
        viewMenu.add(viewSingleCountryMenu);

        viewContinentMenu.setText("Continents...");
        viewMenu.add(viewContinentMenu);

        viewRegionMenu.setText("Regions...");
        viewRegionMenu.setEnabled(false);
        viewMenu.add(viewRegionMenu);

        viewClimateMenu.setText("Climates...");
        viewMenu.add(viewClimateMenu);

        viewProvReligionsMenuItem.setAction(provReligionFilterAction);
        viewMenu.add(viewProvReligionsMenuItem);

        viewCtryReligionsMenuItem.setAction(ctryReligionFilterAction);
        viewMenu.add(viewCtryReligionsMenuItem);

        viewSingleReligionMenu.setText("Single religion");
        viewMenu.add(viewSingleReligionMenu);

        viewBuildingMenu.setText("Building");
        viewMenu.add(viewBuildingMenu);

        viewCultureMenu.setText("Culture");
        viewMenu.add(viewCultureMenu);

        viewGoodsMenuItem.setAction(goodsFilterAction);
        viewMenu.add(viewGoodsMenuItem);

        viewSingleGoodMenu.setText("Single trade good");
        viewMenu.add(viewSingleGoodMenu);

        viewSingleGovernmentMenu.setText("Governments...");
        viewMenu.add(viewSingleGovernmentMenu);

        viewTechGroupMenu.setText("Tech groups...");
        viewMenu.add(viewTechGroupMenu);

        viewCOTMenuItem.setAction(new CustomFilterAction("Centers of trade", "cot", "yes"));
        viewMenu.add(viewCOTMenuItem);

        viewHREMenuItem.setAction(new CustomFilterAction("Holy Roman Empire", "hre", "yes"));
        viewMenu.add(viewHREMenuItem);

        viewBaseTaxMenuItem.setAction(new DiscreteStepFilterAction("Base tax value", "base_tax", 0, 18, 1));
        viewMenu.add(viewBaseTaxMenuItem);

        viewPopulationMenuItem.setAction(new DiscreteStepFilterAction("Population", "citysize", 0, 500000, 10000));
        viewMenu.add(viewPopulationMenuItem);

        viewManpowerMenuItem.setAction(new DiscreteStepFilterAction("Manpower", "manpower", 0, 10, 1));
        viewMenu.add(viewManpowerMenuItem);
        viewMenu.add(viewRevoltRiskMenuItem);

        viewCapitalsMenuItem.setAction(new CapitalFilterAction());
        viewMenu.add(viewCapitalsMenuItem);

        viewNativesMenu.setText("Natives...");

        viewNativeSizeMenuItem.setAction(new DiscreteStepFilterAction("Native size", "native_size", 0, 100, 5));
        viewNativesMenu.add(viewNativeSizeMenuItem);

        viewNativeFerocityMenuItem.setAction(new DiscreteStepFilterAction("Native ferocity", "native_ferocity", 0, 10, 1));
        viewNativesMenu.add(viewNativeFerocityMenuItem);

        viewNativeHostilenessMenuItem.setAction(new DiscreteStepFilterAction("Native hostileness", "native_hostileness", 0, 10, 1));
        viewNativesMenu.add(viewNativeHostilenessMenuItem);

        viewNativeTypesMenu.setText("Native types...");
        viewNativesMenu.add(viewNativeTypesMenu);

        viewMenu.add(viewNativesMenu);

        warsMenuItem.setText("Wars...");
        warsMenuItem.addActionListener(formListener);
        viewMenu.add(warsMenuItem);
        viewMenu.add(jSeparator6);

        customMapModeMenuItem.setText("Custom map mode...");
        customMapModeMenuItem.addActionListener(formListener);
        viewMenu.add(customMapModeMenuItem);

        menuBar.add(viewMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(formListener);
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.MouseListener, java.awt.event.MouseMotionListener, java.awt.event.WindowListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == bookmarksButton) {
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
            else if (evt.getSource() == warsMenuItem) {
                EditorUI.this.warsMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == customMapModeMenuItem) {
                EditorUI.this.customMapModeMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == aboutMenuItem) {
                EditorUI.this.aboutMenuItemActionPerformed(evt);
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
        /*
        String prop = JOptionPane.showInputDialog(this, "Enter the property that the province must have:");
        if (prop == null || prop.length() == 0)
            return;
         
        StringBuilder pattern = new StringBuilder("(");
        String value;
        do {
            value = JOptionPane.showInputDialog(this, "Enter a value that property " +
                    prop + " can have.\nPress \"Cancel\" or leave the field blank to stop.");
            if (value == null || value.length() == 0)
                break;
            else {
                if (pattern.length() != 1)
                    pattern.append("|");
                pattern.append(value);
            }
        } while (true);
         
        if (pattern.length() == 1)
            return;
         
        pattern.append(")");
         
        mapPanel.setMode(new AdvancedCustomMode(mapPanel, prop, pattern.toString()));
        viewModeLabel.setText(prop + " matches " + pattern);
        mapPanel.repaint();
         */
    private void customMapModeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customMapModeMenuItemActionPerformed
//        String prop = JOptionPane.showInputDialog(this, "Enter the property that the province must have (e.g., \"native_size\"):");
//        if (prop == null || prop.length() == 0)
//            return;
//
//        String value = JOptionPane.showInputDialog(this, "Enter the value that property " + prop + " must have:");
//        if (value == null || prop.length() == 0)
//            return;
//
//        mapPanel.setMode(new CustomMode(mapPanel, prop, value));
//        viewModeLabel.setText(prop + " = " + value);
//        mapPanel.repaint();
        
        final MapMode mode = CustomModeDialog.showDialog(this);
        if (mode != null) {
            mode.setMapPanel(mapPanel);
            mapPanel.setMode(mode);
            viewModeLabel.setText(mode.toString());
            mapPanel.repaint();
        }
    }//GEN-LAST:event_customMapModeMenuItemActionPerformed
    
    private void mapPanelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelMouseReleased
        if (evt.isPopupTrigger()) {
            popupTriggered(evt);
            return;
        }
    }//GEN-LAST:event_mapPanelMouseReleased
    
    private void mapPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelMousePressed
        if (evt.isPopupTrigger()) {
            popupTriggered(evt);
            return;
        }
    }//GEN-LAST:event_mapPanelMousePressed
    
    private void showCountryHistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCountryHistButtonActionPerformed
        FileEditorDialog.showDialog(this, lastCountry, Text.getText(lastCountry));
//        final String tag = lastCountry;
//        final String name = Text.getText(tag);
//        EditorDialog ed = new EditorDialog(this, name,
//                mapPanel.getModel().getDataSource().getCountryAsStr(tag)
//                );
//        ed.setVisible(true);
//        if (ed.textHasChanged()) {
//            JOptionPane.showMessageDialog(this, "You changed the text!");
//            mapPanel.getModel().getDataSource().saveCountry(tag, name, ed.getText());
//        }
//        mapPanel.getModel().getDataSource().reloadCountry(tag);
//        mapPanel.repaint();
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
        javax.swing.JOptionPane.showMessageDialog(this, "EU3 Scenario Editor\nVersion " + VERSION + "\nBy MichaelM");
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    private void showProvHistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showProvHistButtonActionPerformed
        FileEditorDialog.showDialog(EditorUI.this, currentProvince);
//        final Province prov = currentProvince;
        
//        EditorDialog ed = new EditorDialog(EditorUI.this, prov.getName(),
//                mapPanel.getModel().getDataSource().getProvinceAsStr(prov.getId())
//                );
//        ed.setVisible(true);
//        if (ed.textHasChanged()) {
//            JOptionPane.showMessageDialog(this, "You changed the text!");
//            mapPanel.getModel().getDataSource().saveProvince(prov.getId(), prov.getName(), ed.getText());
//        } else {
//            JOptionPane.showMessageDialog(this, "You didn't change the text.");
//        }
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
                new Runnable() {
            public void run() {
                doMouseClick(lastProv);
                
                // If it was a double click, go ahead and show an editor.
                if (clickCount > 1) {
                    Class c = mapPanel.getMode().getClass();
                    if (c.equals(ProvinceMode.class)) {
                        showProvHistButton.doClick(0);
                    } else if (c.equals(PoliticalMode.class) ||
                            c.equals(CountryMode.class) ||
                            c.equals(SingleCountryMode.class))  {
                        showCountryHistButton.doClick(0);
                    } else {
                        // do what?
                        showProvHistButton.doClick(0);
                    }
                }
            }
        });
        
//        lastClick = evt.getPoint();
    }//GEN-LAST:event_mapPanelMouseClicked

    private void warsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warsMenuItemActionPerformed
        final List<GenericObject> wars = mapPanel.getDataSource().getWars();
        JDialog dialog = new JDialog(this, "Wars", false);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(0, 4));
        JScrollPane scrollPane = new JScrollPane(panel);
        for (final GenericObject war : wars) {
            boolean active = war.name.equals("active_war");
            JButton button = new JButton(war.getString("name") + (active ? " (active)" : ""));
            
            if (active) {
                StringBuilder tooltip = new StringBuilder("<html>");
                tooltip.append("Attackers: <ul>");
                for (String attacker : war.getStrings("attacker"))
                    tooltip.append("<li>").append(Text.getText(attacker)).append("</li>");
                
                tooltip.append("</ul>Defenders: <ul>");
                for (String defender : war.getStrings("defender"))
                    tooltip.append("<li>").append(Text.getText(defender)).append("</li>");
                tooltip.append("</ul>");
                tooltip.append("</html>");
                button.setToolTipText(tooltip.toString());
//                System.out.println(tooltip);
            }
            
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    EditorDialog ed = new EditorDialog(EditorUI.this, war.getString("name"), war.toString(Style.EU3_SAVE_GAME));
                    ed.setVisible(true);
                }
            });
            panel.add(button);
        }
        dialog.add(scrollPane);
        dialog.pack();
        dialog.setSize(Math.min(dialog.getWidth(), 800), Math.min(dialog.getHeight(), 600));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_warsMenuItemActionPerformed

    private void reloadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadMenuItemActionPerformed
        int numProvs = Integer.parseInt(Main.map.getString("max_provinces"));
        for (int i = 1; i < numProvs; i++) {
            mapPanel.getDataSource().reloadProvince(i);
            mapPanel.getDataSource().reloadProvinceHistory(i);
        }
        GenericObject countries =
                EUGFileIO.load(
                Main.filenameResolver.resolveFilename("common/countries.txt"),
                defaultSettings);
        for (ObjectVariable def : countries.values) {
            mapPanel.getDataSource().reloadCountry(def.varname);
            mapPanel.getDataSource().reloadCountryHistory(def.varname);
        }
    }//GEN-LAST:event_reloadMenuItemActionPerformed
    
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
                ctryNameLabel.setText(Text.getText(lastCountry));
                showCountryHistButton.setEnabled(true);
            }
            
            // Do some neat stuff if we're viewing a land province
            if (Main.map.isLand(lastProv.getId())) {
                if (mode instanceof ContinentMode) {
                    String cont = Main.map.getContinentOfProv(lastProv.getId());
                    mapPanel.setMode(new ContinentMode(mapPanel, cont));
                    viewModeLabel.setText("Provinces in " + cont);
                    mapPanel.repaint();
                } else if (mode instanceof RegionsMode) {
                    String reg = Main.map.getRegionsOfProv(lastProv.getId()).get(0);
                    if (!reg.equals("(none)")) {
                        mapPanel.setMode(new RegionsMode(mapPanel, reg));
                        viewModeLabel.setText("Provinces in " + reg);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof ClimateMode) {
                    String climate = Main.map.getClimateOfProv(lastProv.getId());
                    mapPanel.setMode(new ClimateMode(mapPanel, climate));
                    viewModeLabel.setText("Provinces with climate " + climate);
                    mapPanel.repaint();
                } else if (mode instanceof NativeGroupMode) {
                    String nativeType = Main.map.getNativeTypeOfProv(lastProv.getId());
                    mapPanel.setMode(new NativeGroupMode(mapPanel, nativeType));
                    viewModeLabel.setText("Provinces with " + nativeType + " natives");
                    mapPanel.repaint();
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
                }
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
        System.out.println("Exiting...");
    }
    
//    private void updateProvincePanel(int provId) {
//        ((ProvinceEditorPanel)innerProvincePanel).setProvId(provId);
//    }
    
    
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
        addReligionFilters();
        addBuildingFilters();
        addCountryFilters();
        addCultureFilters();
        addGoodsFilters();
        addGovernmentFilters();
        addTechGroupFilters();
        addContinentFilters();
        if (Main.map.isInNomine())
            addRegionFilters();
        addClimateFilters();
        addNativesFilters();
        
        // Anything else?
    }
    
    private void addReligionFilters() {
        final GenericObject religions =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/religion.txt"),
                defaultSettings);
        
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
            
            viewSingleReligionMenu.add(groupMenu);
        }
        
        allReligions.deleteCharAt(allReligions.length()-1); // get rid of the last '|'
        allReligions.append(')');
        
        viewSingleReligionMenu.add(new MultiFilterAction("All religions (province)", "religion", allReligions.toString()));
        viewSingleReligionMenu.add(new MultiCountryFilterAction("All religions (country)", "religion", allReligions.toString()));
    }
    
    private void addBuildingFilters() {
        final GenericObject buildings =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/buildings.txt"),
                defaultSettings);
        
        Collections.sort(buildings.children, new ObjectComparator());
        
        JMenu capitalBuildingsMenu = new JMenu("Capital buildings");
        viewBuildingMenu.add(capitalBuildingsMenu);
        JMenu manufactoriesMenu = new JMenu("Manufactories");
        viewBuildingMenu.add(manufactoriesMenu);
        java.util.List<GenericObject> forts = new java.util.ArrayList<GenericObject>();
        
        for (GenericObject building : buildings.children) {
            if (building.hasString("fort_level")) {
                forts.add(building);
                continue;
            }
            
            CustomFilterAction act = new CustomFilterAction(Text.getText(building.name), building.name, "yes");
            if (building.containsList("manufactory"))
                manufactoriesMenu.add(act);
            else if (building.getString("capital").equals("yes"))
                capitalBuildingsMenu.add(act);
            else
                viewBuildingMenu.add(act);
        }
        
        viewBuildingMenu.add(new FortLevelFilterAction(forts));
    }
    
    private void addCountryFilters() {
        final GenericObject countries =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/countries.txt"),
                defaultSettings);
        
        Collections.sort(countries.values, new VariableComparator());
        
        List[] menus = new List[26];
        for (int i = 0; i < menus.length; i++) {
            menus[i] = new ArrayList<JMenu>();
        }
        List<JMenu> otherMenu = new ArrayList<JMenu>();
        
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
                menus[idx].add(menu);
            else
                otherMenu.add(menu);
        }
        
        for (int i = 0; i < menus.length; i++) {
            JMenu menu = new JMenu(" " + (char)('A' + i) + " ");
            if (menus[i].size() == 0) {
                JMenuItem dummy = new JMenuItem("(none)");
                dummy.setEnabled(false);
                menu.add(dummy);
            } else {
                for (Object m : menus[i]) {
                    menu.add((JMenu)m);
                }
            }
            viewSingleCountryMenu.add(menu);
        }
        
        if (!otherMenu.isEmpty()) {
            JMenu menu = new JMenu("Other");
            for (JMenu m : otherMenu) {
                menu.add(m);
            }
            viewSingleCountryMenu.add(menu);
        }
    }
    
    private void addCultureFilters() {
        final GenericObject cultures =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/cultures.txt"),
                defaultSettings);
        
        if (!cultures.lists.isEmpty()) {
            Collections.sort(cultures.lists, new ListComparator());

            final Comparator<String> listSorter = new StringComparator();

            for (GenericList group : cultures.lists) {
                group.sort(listSorter);

                JMenu groupMenu = new JMenu(Text.getText(group.getName()));

                StringBuilder pattern = new StringBuilder(group.size()*10).append('(');

                for (String culture : group) {
                    groupMenu.add(new CustomFilterAction(Text.getText(culture), "culture", culture));
                    pattern.append(culture).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');

                groupMenu.add(new MultiFilterAction("All " + Text.getText(group.getName()), "culture", pattern.toString()));

                viewCultureMenu.add(groupMenu);
            }
        } else {
            // In Nomine
            final ObjectComparator comp = new ObjectComparator();
            Collections.sort(cultures.children, comp);

            for (GenericObject group : cultures.children) {
                Collections.sort(group.children, comp);

                JMenu groupMenu = new JMenu(Text.getText(group.name));

                StringBuilder pattern = new StringBuilder(group.size()*10).append('(');

                for (GenericObject culture : group.children) {
                    groupMenu.add(new CustomFilterAction(Text.getText(culture.name), "culture", culture.name));
                    pattern.append(culture.name).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');

                groupMenu.add(new MultiFilterAction("All " + Text.getText(group.name), "culture", pattern.toString()));

                viewCultureMenu.add(groupMenu);
            }
        }
    }
    
    private void addGoodsFilters() {
        final GenericObject goods =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/tradegoods.txt"),
                defaultSettings);
        
        Collections.sort(goods.children, new ObjectComparator());
        
        int counter = 0;
        for (GenericObject good : goods.children) {
            viewSingleGoodMenu.add(new CustomFilterAction(Text.getText(good.name), "trade_goods", good.name));
            if (counter++ > 25) {
                viewSingleGoodMenu = (JMenu) viewSingleGoodMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }
    
    private void addGovernmentFilters() {
        final GenericObject governments =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/governments.txt"),
                defaultSettings);
        
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
            
            viewSingleGovernmentMenu.add(republicMenu);
            viewSingleGovernmentMenu.add(religiousMenu);
            viewSingleGovernmentMenu.add(normalMenu);
            
        } else {
        
            for (GenericObject government : governments.children) {
                allGovernments.append(government.name).append('|');
                viewSingleGovernmentMenu.add(new CustomCountryFilterAction(Text.getText(government.name), "government", government.name));
            }
        }
        
        allGovernments.deleteCharAt(allGovernments.length()-1); // get rid of the last '|'
        allGovernments.append(')');
        
        viewSingleGovernmentMenu.add(new MultiCountryFilterAction("All governments", "government", allGovernments.toString()));
    }
    
    private void addTechGroupFilters() {
        GenericObject groups =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/technology.txt"),
                defaultSettings);
        
        groups = groups.getChild("groups");
        
        if (!groups.values.isEmpty()) {
            for (ObjectVariable group : groups.values) {
                viewTechGroupMenu.add(new CustomCountryFilterAction(Text.getText(group.varname), "technology_group", group.varname));
            }
        } else {
            // must be In Nomine
            for (GenericObject group : groups.children) {
                viewTechGroupMenu.add(new CustomCountryFilterAction(Text.getText(group.name), "technology_group", group.name));
            }
        }
    }
    
    private void addContinentFilters() {
        for (String cont : Main.map.getContinents().keySet()) {
            viewContinentMenu.add(new ContinentFilterAction(Text.getText(cont), cont));
        }
    }
    
    private void addClimateFilters() {
        for (String climate : Main.map.getClimates().keySet()) {
            viewClimateMenu.add(new ClimateFilterAction(Text.getText(climate), climate));
        }
    }
    
    private void addRegionFilters() {
        java.util.Map<String, List<String>> regions = Main.map.getRegions();
        List<String> regionNames = new ArrayList<String>(regions.keySet());
        Collections.sort(regionNames);
        
        int counter = 0;
        for (String region : regionNames) {
            viewRegionMenu.add(new RegionFilterAction(Text.getText(region), region));
            if (counter++ > 25) {
                viewRegionMenu = (JMenu) viewRegionMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }
    
    private void addNativesFilters() {
        for (String nativeType : Main.map.getNatives().keySet()) {
            viewNativeTypesMenu.add(new NativesFilterAction((Text.getText(nativeType)), nativeType));
        }
    }
    
// <editor-fold defaultstate="collapsed" desc=" Generated Variables ">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton bookmarksButton;
    private javax.swing.JLabel ctryNameLabel;
    private javax.swing.JMenuItem customMapModeMenuItem;
    private javax.swing.JSpinner daySpinner;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JButton goToProvButton;
    private javax.swing.JMenuItem goToProvMenuItem;
    private final editor.MapPanel mapPanel = new editor.MapPanel();
    private javax.swing.JScrollPane mapScrollPane;
    private javax.swing.JSpinner monthSpinner;
    private javax.swing.JLabel provNameLabel;
    javax.swing.JMenuItem reloadMenuItem;
    private javax.swing.JButton setDateButton;
    private javax.swing.JMenuItem setDateMenuItem;
    private javax.swing.JButton showCountryHistButton;
    private javax.swing.JButton showProvHistButton;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu viewBuildingMenu;
    private javax.swing.JMenu viewClimateMenu;
    private javax.swing.JMenu viewContinentMenu;
    private javax.swing.JMenuItem viewCountriesMenuItem;
    private javax.swing.JMenu viewCultureMenu;
    private javax.swing.JMenuItem viewGoodsMenuItem;
    private javax.swing.JMenuItem viewManpowerMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.ButtonGroup viewModeButtonGroup;
    private javax.swing.JLabel viewModeLabel;
    private javax.swing.JMenu viewNativeTypesMenu;
    private javax.swing.JMenuItem viewProvReligionsMenuItem;
    private javax.swing.JMenuItem viewProvincesMenuItem;
    javax.swing.JMenu viewRegionMenu;
    private javax.swing.JMenu viewSingleCountryMenu;
    private javax.swing.JMenu viewSingleGoodMenu;
    private javax.swing.JMenu viewSingleGovernmentMenu;
    private javax.swing.JMenu viewSingleReligionMenu;
    javax.swing.JMenu viewTechGroupMenu;
    javax.swing.JMenuItem warsMenuItem;
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
    
    private final Action provinceFilterAction = new ProvinceFilterAction();
    private final Action countryFilterAction = new PoliticalFilterAction(); //new CountryFilterAction();
    private final Action provReligionFilterAction = new ProvReligionFilterAction();
    private final Action ctryReligionFilterAction = new ReligionFilterAction(); //CtryReligionFilterAction();
    private final Action goodsFilterAction = new GoodsFilterAction();
    
    // Inner classes
    
    // Utilities
    
//    private static class ProvinceChanger {
//        public ProvinceChanger() {
//        }
//        public void changeValue(String name, String date, String value) {
//
//        }
//    }
    
    private static class SaveGameFileFilter extends javax.swing.filechooser.FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".eu3");
        }
        
        public String getDescription() {
            return "Europa Universalis 3 saved game files";
        }
    }
    
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
            final int maxProv = Integer.parseInt(Main.map.getString("max_provinces")) - 1;
            String response =
                    JOptionPane.showInputDialog(EditorUI.this,
                    "Enter a province ID number (between 1 and " + maxProv +
                    ") or a province name:",
                    currentProvince == null ? null : currentProvince.getId());
            
            if (response == null || response.length() == 0)
                return;
            
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
            System.out.println("Setting date to " + date);
            mapPanel.getModel().setDate(date);
//            ((ProvinceEditorPanel)innerProvincePanel).setDate(date);
//            String country = (String) countryComboBox.getSelectedItem();
//            countryComboBox.setModel(new DefaultComboBoxModel(new java.util.Vector<String>(mapPanel.getModel().getTags())));
//            countryComboBox.setSelectedItem(country);
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
            FileEditorDialog.showDialog(EditorUI.this, tag, name);
        }
    }
    
    private abstract class FilterAction extends AbstractAction {
        protected final MapMode mode;
        protected FilterAction(String name, MapMode mode) {
            super(name);
            this.mode = mode;
        }
        
        public void actionPerformed(ActionEvent e) {
//            java.awt.EventQueue.invokeLater(new Runnable() {
//                public void run() {
            mapPanel.setMode(mode);
            viewModeLabel.setText((String) getValue(SHORT_DESCRIPTION));
            mapPanel.repaint();
            doMouseClick(currentProvince);
//                }
//            });
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
            super("Trade goods", new GoodsMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Trade goods");
        }
    }
    
    private class CoreFilterAction extends FilterAction {
        public CoreFilterAction(String tag) {
            super("Core", new CoreMapMode(mapPanel, tag));
            putValue(SHORT_DESCRIPTION, "Cores of " + tag);
        }
    }
    
    private class DiscreteStepFilterAction extends FilterAction {
        public DiscreteStepFilterAction(String name, String prop, int min, int max, int step) {
            super(name, new DiscreteScalingMapMode(mapPanel, prop, min, max, step));
            putValue(SHORT_DESCRIPTION, name);
        }
        
//        private void setMinColor(Color c) {
//            ((DiscreteScalingMapMode)mode).setMinColor(c);
//        }
//
//        private void setMaxColor(Color c) {
//            ((DiscreteScalingMapMode)mode).setMaxColor(c);
//        }
    }
    
    private class FortLevelFilterAction extends FilterAction {
        public FortLevelFilterAction(final java.util.List<GenericObject> forts) {
            super("Fort level", new FortMapMode(mapPanel, forts));
            putValue(SHORT_DESCRIPTION, "Fort level");
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
