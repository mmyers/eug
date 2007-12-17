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
import eug.specific.eu3.EU3SaveGame;
import eug.specific.eu3.EU3Scenario;
import java.awt.Point;
import java.awt.event.ActionEvent;
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
public class EditorUI extends javax.swing.JFrame {
    
    private transient ProvinceData.Province currentProvince = null;
    
//    /** The province that is selected in the combo box at the left. */
//    private transient ProvinceData.Province selectedProvince;
    
    private static final String VERSION = "0.5.1";
    
    private JPopupMenu bookmarkMenu = new JPopupMenu("Bookmarks");
    
    /**
     * Creates new form EditorUI.
     */
    public EditorUI() {
//        screen = this.getGraphicsConfiguration().getBounds();
        initComponents();
        
        // Make sure the window isn't too big
        final java.awt.Rectangle bounds = getGraphicsConfiguration().getBounds();
        setSize(Math.min(getWidth(), bounds.width), Math.min(getHeight(), bounds.height));
        
        class PaintBordersAction extends AbstractAction {
            PaintBordersAction() {
                super("Toggle borders");
                putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('B', InputEvent.CTRL_DOWN_MASK));
            }
            public void actionPerformed(ActionEvent e) {
                mapPanel.setPaintBorders(!mapPanel.isPaintBorders());
                mapPanel.repaint();
            }
        };
        viewMenu.add(new PaintBordersAction());
        
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JMenu fileMenu;
        javax.swing.JMenu helpMenu;
        javax.swing.JLabel jLabel1;
        javax.swing.JLabel jLabel2;
        javax.swing.JSeparator jSeparator1;
        javax.swing.JSeparator jSeparator2;
        javax.swing.JSeparator jSeparator3;
        javax.swing.JSeparator jSeparator4;
        javax.swing.JSeparator jSeparator5;
        javax.swing.JSeparator jSeparator6;
        javax.swing.JPanel lowerPanel;
        javax.swing.JMenuBar menuBar;
        javax.swing.JToolBar toolBar;
        javax.swing.JMenuItem viewBaseTaxMenuItem;
        javax.swing.JMenuItem viewCOTMenuItem;
        javax.swing.JMenuItem viewCtryReligionsMenuItem;
        javax.swing.JMenuItem viewHREMenuItem;
        javax.swing.JMenuItem viewNativeFerocityMenuItem;
        javax.swing.JMenuItem viewNativeHostilenessMenuItem;
        javax.swing.JMenuItem viewNativeSizeMenuItem;
        javax.swing.JMenu viewNativesMenu;
        javax.swing.JMenuItem viewPopulationMenuItem;
        javax.swing.JMenuItem viewRevoltRiskMenuItem;

        viewModeButtonGroup = new javax.swing.ButtonGroup();
        toolBar = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        viewModeLabel = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        yearSpinner = new javax.swing.JSpinner();
        monthSpinner = new javax.swing.JSpinner();
        daySpinner = new javax.swing.JSpinner();
        setDateButton = new javax.swing.JButton();
        bookmarksButton = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        zoomInButton = new javax.swing.JButton();
        zoomOutButton = new javax.swing.JButton();
        zoomLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        goToProvButton = new javax.swing.JButton();
        mapScrollPane = new javax.swing.JScrollPane();
        lowerPanel = new javax.swing.JPanel();
        provNameLabel = new javax.swing.JLabel();
        showProvHistButton = new javax.swing.JButton();
        ctryNameLabel = new javax.swing.JLabel();
        showCountryHistButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        goToProvMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        setDateMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewProvincesMenuItem = new javax.swing.JMenuItem();
        viewCountriesMenuItem = new javax.swing.JMenuItem();
        viewSingleCountryMenu = new javax.swing.JMenu();
        viewContinentMenu = new javax.swing.JMenu();
        viewClimateMenu = new javax.swing.JMenu();
        viewProvReligionsMenuItem = new javax.swing.JMenuItem();
        viewCtryReligionsMenuItem = new javax.swing.JMenuItem();
        viewSingleReligionMenu = new javax.swing.JMenu();
        viewBuildingMenu = new javax.swing.JMenu();
        viewCultureMenu = new javax.swing.JMenu();
        viewGoodsMenuItem = new javax.swing.JMenuItem();
        viewSingleGoodMenu = new javax.swing.JMenu();
        viewSingleGovernmentMenu = new javax.swing.JMenu();
        viewCOTMenuItem = new javax.swing.JMenuItem();
        viewHREMenuItem = new javax.swing.JMenuItem();
        viewBaseTaxMenuItem = new javax.swing.JMenuItem();
        viewPopulationMenuItem = new javax.swing.JMenuItem();
        viewManpowerMenuItem = new javax.swing.JMenuItem();
        viewRevoltRiskMenuItem = new javax.swing.JMenuItem();
        final DiscreteStepFilterAction act = new DiscreteStepFilterAction("Revolt risk", "revolt_risk", 0, 10, 1);
        //act.setMinColor(java.awt.Color.GREEN);
        //act.setMaxColor(java.awt.Color.RED);
        viewRevoltRiskMenuItem.setAction(act);
        viewNativesMenu = new javax.swing.JMenu();
        viewNativeSizeMenuItem = new javax.swing.JMenuItem();
        viewNativeFerocityMenuItem = new javax.swing.JMenuItem();
        viewNativeHostilenessMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        customMapModeMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
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

        yearSpinner.setModel(new SpinnerNumberModel(1453, 0, 50000, 1));
        yearSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(yearSpinner, "#####"));
        yearSpinner.setMaximumSize(new java.awt.Dimension(100, 30));
        yearSpinner.setPreferredSize(new java.awt.Dimension(60, 20));
        toolBar.add(yearSpinner);

        monthSpinner.setModel(new SpinnerNumberModel(1, 0, 12, 1));
        monthSpinner.setMaximumSize(new java.awt.Dimension(100, 30));
        monthSpinner.setPreferredSize(new java.awt.Dimension(40, 20));
        toolBar.add(monthSpinner);

        daySpinner.setModel(new SpinnerNumberModel(1, 0, 31, 1));
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

        viewNativesMenu.setText("Natives...");
        viewNativeSizeMenuItem.setAction(new DiscreteStepFilterAction("Native size", "native_size", 0, 100, 5));
        viewNativesMenu.add(viewNativeSizeMenuItem);

        viewNativeFerocityMenuItem.setAction(new DiscreteStepFilterAction("Native ferocity", "native_ferocity", 0, 10, 1));
        viewNativesMenu.add(viewNativeFerocityMenuItem);

        viewNativeHostilenessMenuItem.setAction(new DiscreteStepFilterAction("Native hostileness", "native_hostileness", 0, 10, 1));
        viewNativesMenu.add(viewNativeHostilenessMenuItem);

        viewMenu.add(viewNativesMenu);

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
            if (evt.getSource() == exitMenuItem) {
                EditorUI.this.exitMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == customMapModeMenuItem) {
                EditorUI.this.customMapModeMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == aboutMenuItem) {
                EditorUI.this.aboutMenuItemActionPerformed(evt);
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
    
    private transient Point lastClick = null;
    
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
                    } else if (c.equals(CountryMode.class) || c.equals(SingleCountryMode.class))  {
                        showCountryHistButton.doClick(0);
                    } else {
                        // do what?
                    }
                }
            }
        });
        
        lastClick = evt.getPoint();
    }//GEN-LAST:event_mapPanelMouseClicked
    
    private void doMouseClick(final ProvinceData.Province p) {
        currentProvince = p;
        
        if (p == null) {
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
//                    else if (name.equals("religion")) {
//                        mapPanel.setMode(new CustomMode(mapPanel, "religion", lastCountry));
//                        viewModeLabel.setText("controller = " + lastCountry);
//                        mapPanel.repaint();
//                    }
                } else if (mode instanceof CoreMapMode) {
                    mapPanel.setMode(new CoreMapMode(mapPanel, lastCountry));
                    viewModeLabel.setText("Cores of " + lastCountry);
                    mapPanel.repaint();
                }
                ctryNameLabel.setText(Text.getText(lastCountry));
                showCountryHistButton.setEnabled(true);
            }
            
            if (mode instanceof ContinentMode) {
                String cont = Main.map.getContinentOfProv(lastProv.getId());
                mapPanel.setMode(new ContinentMode(mapPanel, cont));
                viewModeLabel.setText("Provinces in " + cont);
                mapPanel.repaint();
            } else if (mode instanceof ClimateMode) {
                String climate = Main.map.getClimateOfProv(lastProv.getId());
                mapPanel.setMode(new ClimateMode(mapPanel, climate));
                viewModeLabel.setText("Provinces with climate " + climate);
                mapPanel.repaint();
            } /*else if (mode instanceof CustomMode) {
                String name = ((CustomMode)mode).getName();
                if (name.equals("trade_goods")) {
               
                }
            } */
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
        lastClick = null;
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
        addContinentFilters();
        addClimateFilters();
        
        // Anything else?
    }
    
    private void addReligionFilters() {
        final GenericObject religions =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/religion.txt"),
                defaultSettings);
        
        Collections.sort(religions.children, new ObjectComparator());
        
        for (GenericObject group : religions.children) {
            Collections.sort(group.children, new ObjectComparator());
            
            JMenu groupMenu = new JMenu(Text.getText(group.name));
            
            StringBuilder pattern = new StringBuilder(group.size()*10).append('(');
            
            for (GenericObject religion : group.children) {
                groupMenu.add(new CustomFilterAction(Text.getText(religion.name), "religion", religion.name));
                pattern.append(religion.name).append('|');
            }
            
            pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
            pattern.append(')');
            
            groupMenu.add(new MultiFilterAction("All " + Text.getText(group.name), "religion", pattern.toString()));
            
            viewSingleReligionMenu.add(groupMenu);
        }
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
            
            int idx = start - 'A';
            if (idx >= 0 && idx < menus.length)
                menus[start - 'A'].add(menu);
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
    }
    
    private void addGoodsFilters() {
        final GenericObject goods =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/tradegoods.txt"),
                defaultSettings);
        
        Collections.sort(goods.children, new ObjectComparator());
        
        for (GenericObject good : goods.children) {
            viewSingleGoodMenu.add(new CustomFilterAction(Text.getText(good.name), "trade_goods", good.name));
        }
    }
    
    private void addGovernmentFilters() {
        final GenericObject governments =
                EUGFileIO.load(Main.filenameResolver.resolveFilename("common/governments.txt"),
                defaultSettings);
        
//        Collections.sort(governments.children, new ObjectComparator());
        
        for (GenericObject government : governments.children) {
            viewSingleGovernmentMenu.add(new CustomCountryFilterAction(Text.getText(government.name), "government", government.name));
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
    private javax.swing.JMenuItem viewProvReligionsMenuItem;
    private javax.swing.JMenuItem viewProvincesMenuItem;
    private javax.swing.JMenu viewSingleCountryMenu;
    private javax.swing.JMenu viewSingleGoodMenu;
    private javax.swing.JMenu viewSingleGovernmentMenu;
    private javax.swing.JMenu viewSingleReligionMenu;
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
