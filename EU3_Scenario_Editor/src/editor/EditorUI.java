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
import eug.specific.ck2.CK2SaveGame;
import eug.specific.ck2.CK2Scenario;
import eug.specific.ck3.CK3DataSource;
import eug.specific.ck3.CK3Scenario;
import eug.specific.clausewitz.ClausewitzHistory;
import eug.specific.clausewitz.ClausewitzHistoryMergeTool;
import eug.specific.clausewitz.ClausewitzSaveGame;
import eug.specific.clausewitz.ClausewitzScenario;
import eug.specific.eu3.EU3SaveGame;
import eug.specific.eu3.EU3Scenario;
import eug.specific.victoria2.Vic2SaveGame;
import eug.specific.victoria2.Vic2Scenario;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author  Michael Myers
 */
public final class EditorUI extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(EditorUI.class.getName());
    
    private transient List<ProvinceData.Province> currentProvinces = new ArrayList<>();
    
//    /** The province that is selected in the combo box at the left. */
//    private transient ProvinceData.Province selectedProvince;
    
    private JPopupMenu bookmarkMenu = new JPopupMenu("Bookmarks");

    private boolean isSavedGame = false;

    private GameVersion version;
    private FilenameResolver resolver;
    private Map map;
    private ProvinceData provinceData;
    
    private ClausewitzHistoryMergeTool provinceHistoryMergeTool; // only initialized if the user wants to do a multi-file edit
    
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
        
        setExtendedState(getExtendedState() | java.awt.Frame.MAXIMIZED_BOTH);
        
        mapPanel.centerMap();
        setDataSource(saveFile);
        MapmodeBuilder.buildMapmodeMenu(viewMenu, mapPanel, viewModeLabel, version, resolver, isSavedGame);
        
        preloadHistory();
        
        if (checkUpdates)
            checkForUpdates();
    }
    
    private transient Thread historyPreloadThread;
    
    private void preloadHistory() {
        Runnable r = () -> {
            log.log(Level.INFO, "Preloading history");

            long startTime = System.currentTimeMillis();

            mapPanel.getDataSource().preloadProvinces(map.getMaxProvinces());

            // Preloading countries and titles sometimes takes quite a long time
            // and it isn't necessary in all cases, so let's not do it here
//            if (mapPanel.getDataSource() instanceof CK3DataSource)
//                ((CK3DataSource)mapPanel.getDataSource()).preloadTitles();
//            else
//                mapPanel.getDataSource().preloadCountries();

            log.log(Level.INFO, "Loaded history in {0} ms.", System.currentTimeMillis() - startTime);
        };
        historyPreloadThread = new Thread(r);
        historyPreloadThread.start();
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        
        if (b) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                historyPreloadThread.join();
            } catch (InterruptedException ex) {
                log.log(Level.WARNING, "History preload failed");
                historyPreloadThread.interrupt();
            }
            setCursor(null);
        }
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
            java.util.regex.Pattern.compile("\\d{3,4}\\.\\d{1,2}\\.\\d{1,2}");

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
        if (definesLua == null)
            return false;
        
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
            long startTime = System.currentTimeMillis();

            ClausewitzScenario scen;
            if (version.getSaveType().equalsIgnoreCase("eu3"))
                scen = new EU3Scenario(resolver);
            else if (version.getSaveType().equalsIgnoreCase("victoria"))
                scen = new Vic2Scenario(resolver);
            else if (version.getSaveType().equalsIgnoreCase("ck2"))
                scen = new CK2Scenario(resolver);
            else if (version.getSaveType().equalsIgnoreCase("ck3"))
                scen = new CK3Scenario(resolver);
            else {
                log.log(Level.WARNING, "Unknown or missing scenario type specified by config (\"{0}\"). Defaulting to EU3/EU4.", version.getSaveType());
                scen = new EU3Scenario(resolver);
            }
            
            mapPanel.setDataSource(scen);
            log.log(Level.INFO, "Done in {0} ms.", System.currentTimeMillis() - startTime);
            FileEditorDialog.setDataSource(scen);
            MultiFileEditorDialog.setDataSource(scen);

            if (!setStartDateNew(version.getStartDate())) {
                GenericObject defines =
                        EUGFileIO.load(resolver.resolveFilename("common/defines.txt"), defaultSettings);
                if (defines == null) {
                    defines = loadLuaFile(resolver.resolveFilename("common/defines.lua"));
                    java.io.File[] defFiles = resolver.listFiles("common/defines");
                    GenericObject modDefines = loadAllLuaFiles(defFiles);
                    if (!setStartDateLua(modDefines))
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

            if (version.hasBookmarks()) {
                String firstDate = readBookmarks();
            
                if (date.equals("0.0.0") && firstDate != null) {
                    setStartDateNew(firstDate);
                    mapPanel.getModel().setDate(firstDate);
                }
            }
        } else {
            log.log(Level.INFO, "Loading saved game...");

            ClausewitzSaveGame save;
            if (version.getSaveType().equalsIgnoreCase("eu3"))
                save = EU3SaveGame.loadSaveGame(saveFile, resolver);
            else if (version.getSaveType().equalsIgnoreCase("victoria"))
                save = Vic2SaveGame.loadSaveGame(saveFile, resolver);
            else if (version.getSaveType().equalsIgnoreCase("ck2"))
                save = CK2SaveGame.loadSaveGame(saveFile, resolver);
            else {
                log.log(Level.WARNING, "Unknown or missing save game type specified by config (\"{0}\"). Defaulting to EU3/EU4.", version.getSaveType());
                save = EU3SaveGame.loadSaveGame(saveFile, resolver);
            }
            
            if (version.getName().equalsIgnoreCase("EU4"))
                save.setStyle(Style.EU4_SAVE_GAME);

            log.log(Level.INFO, "Done.");

            log.log(Level.INFO, "Loading province history...");
            long startTime = System.currentTimeMillis();
            mapPanel.setDataSource(save);
            log.log(Level.INFO, "Done in {0} ms.", System.currentTimeMillis() - startTime);

            FileEditorDialog.setDataSource(save);
            MultiFileEditorDialog.setDataSource(save);

            String[] date = save.getDate().split("\\.");
            if (date.length != 3) {
                log.log(Level.SEVERE, "Could not parse date from {0}", save.getDate());
            }
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
        if (mapPanel.getModel().getDataSource() instanceof CK2DataSource || mapPanel.getModel().getDataSource() instanceof CK3DataSource)
            showCountryHistButton.setText("Show title history");
    }
    
    private GenericObject loadLuaFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while((line = reader.readLine()) != null) {
                int index = line.indexOf("--"); // remove Lua comments
                if (index >= 0)
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
    
    private GenericObject loadAllLuaFiles(java.io.File[] files) {
        if (files == null)
            return null;
        if (files.length == 1)
            return loadLuaFile(files[0].getAbsolutePath());
        
        GenericObject root = new GenericObject();
        for (java.io.File file : files) {
            GenericObject obj = loadLuaFile(file.getAbsolutePath());
            if (obj != null)
                root.addAllChildren(obj);
        }
        return root;
    }
    
    private static final ParserSettings defaultSettings =
            ParserSettings.getNoCommentSettings().setPrintTimingInfo(false);
    
    // loadFileOrFolder and loadFolderUTF8 are duplicated in MapmodeBuilder and also more or less in editor.mapmode.Utilities.
    // Probably should put them in a better place.
    private GenericObject loadFileOrFolder(String folderName, String fileName) {
        java.io.File folder = new java.io.File(resolver.resolveFilename(folderName));
        java.io.File file = new java.io.File(resolver.resolveFilename(fileName));
        if (folder.exists() && folder.isDirectory()) {
            return EUGFileIO.loadAll(resolver.listFiles(folderName), defaultSettings);
        } else if (file.exists()) {
            return EUGFileIO.load(file, defaultSettings);
        } else {
            log.log(Level.WARNING, "Could not load definition files from {0} or {1}", new Object[] { folderName, fileName });
            return null;
        }
    }
    
    private GenericObject loadFolderUTF8(String folderName) {
        java.io.File folder = new java.io.File(resolver.resolveFilename(folderName));
        if (folder.exists() && folder.isDirectory()) {
            return EUGFileIO.loadAllUTF8(resolver.listFiles(folderName), defaultSettings);
        }
        return null;
    }
    
    private String readBookmarks() {
        GenericObject bookmarks = loadFileOrFolder("common/bookmarks", "common/bookmarks.txt");
        if (bookmarks == null || bookmarks.isEmpty())
            bookmarks = loadFolderUTF8("common/bookmarks/bookmarks");
        
        if (bookmarks != null)
        {
            Collections.sort(bookmarks.children, new Comparator<GenericObject>() {
                private ClausewitzHistory.DateComparator dc = new ClausewitzHistory.DateComparator();
                @Override
                public int compare(GenericObject o1, GenericObject o2) {
                    String firstDate = o1.getString("date");
                    if (firstDate.isEmpty())
                        firstDate = o1.getString("start_date");
                    String secondDate = o2.getString("date");
                    if (secondDate.isEmpty())
                        secondDate = o2.getString("start_date");
                    return dc.compare(firstDate, secondDate);
                }
            });
            String firstDate = null;
            for (GenericObject bookmark : bookmarks.children) {
                String date = bookmark.getString("date");
                if (date.isEmpty())
                    date = bookmark.getString("start_date");
                
                if (firstDate == null)
                    firstDate = date;
                
                String name = bookmark.getString("name");
                if (name == null || name.isEmpty())
                    name = bookmark.name;
                bookmarkMenu.add(
                        new BookmarkAction(
                        Text.getText(name) + " (" + date + ")",
                        Text.getText(bookmark.getString("desc")),
                        date
                        ));
            }
            bookmarkMenu.pack();
            return firstDate;
        }
        
        bookmarkMenu.pack();
        return null;
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
        flashProvsButton = new javax.swing.JButton();
        copyProvIdsButton = new javax.swing.JButton();
        mapScrollPane = new javax.swing.JScrollPane();
        javax.swing.JPanel lowerPanel = new javax.swing.JPanel();
        provNameLabel = new javax.swing.JLabel();
        showProvHistButton = new javax.swing.JButton();
        ctryNameLabel = new javax.swing.JLabel();
        showCountryHistButton = new javax.swing.JButton();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        exportImageMenuItem = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator8 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        goToProvMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator3 = new javax.swing.JSeparator();
        setDateMenuItem = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator7 = new javax.swing.JPopupMenu.Separator();
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

        flashProvsButton.setText("Blink selected province(s)");
        flashProvsButton.setToolTipText("Where am I? Click to find out!");
        flashProvsButton.setFocusable(false);
        flashProvsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        flashProvsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        flashProvsButton.addActionListener(formListener);
        toolBar.add(flashProvsButton);

        copyProvIdsButton.setText("Copy selected province ID(s)");
        copyProvIdsButton.setToolTipText("");
        copyProvIdsButton.setFocusable(false);
        copyProvIdsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        copyProvIdsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        copyProvIdsButton.addActionListener(formListener);
        toolBar.add(copyProvIdsButton);

        getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

        mapPanel.addMouseMotionListener(formListener);
        mapPanel.addMouseWheelListener(formListener);
        mapPanel.addMouseListener(formListener);
        mapScrollPane.setViewportView(mapPanel);

        getContentPane().add(mapScrollPane, java.awt.BorderLayout.CENTER);

        provNameLabel.setText("Click to select a province; right-click or hold <ctrl> to select multiple provinces");
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

        exportImageMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        exportImageMenuItem.setText("Export map as image...");
        exportImageMenuItem.addActionListener(formListener);
        fileMenu.add(exportImageMenuItem);
        fileMenu.add(jSeparator8);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_DOWN_MASK));
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

        reloadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
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

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.MouseListener, java.awt.event.MouseMotionListener, java.awt.event.MouseWheelListener, java.awt.event.WindowListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == scaleColorsButton) {
                EditorUI.this.scaleColorsButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bookmarksButton) {
                EditorUI.this.bookmarksButtonActionPerformed(evt);
            }
            else if (evt.getSource() == flashProvsButton) {
                EditorUI.this.flashProvsButtonActionPerformed(evt);
            }
            else if (evt.getSource() == copyProvIdsButton) {
                EditorUI.this.copyProvIdsButtonActionPerformed(evt);
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
            else if (evt.getSource() == exportImageMenuItem) {
                EditorUI.this.exportImageMenuItemActionPerformed(evt);
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
        }

        public void mouseReleased(java.awt.event.MouseEvent evt) {
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

        public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
            if (evt.getSource() == mapPanel) {
                EditorUI.this.mapPanelMouseWheelMoved(evt);
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
            
    private void showCountryHistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCountryHistButtonActionPerformed
        if (!FileEditorDialog.isShowingDialog(lastCountry)) {
            FileEditorDialog dlg = new FileEditorDialog(EditorUI.this, lastCountry, getLastCountryName(), resolver, provinceData);
            dlg.registerSaveHandler(new FileEditorDialog.SaveHandler() {
                @Override
                public void handleSaveEvent(int provId) {
                    mapPanel.getModel().clearHistoryCache();
                    mapPanel.refresh();
                }

                @Override
                public void handleSaveEvent(String tagOrTitle) {
                    mapPanel.getModel().clearHistoryCache();
                    mapPanel.refresh();
                }
            });
            dlg.setVisible(true);
        }
    }//GEN-LAST:event_showCountryHistButtonActionPerformed
    
    /**
     * Cache the last province that the mouse was on, to keep from constantly
     * setting tooltip text.
     */
    private transient ProvinceData.Province lastProvRolledOver = null;
    
    /**
     * Cache the last point, also.
     */
    private transient Point lastPt = null;
    
//    private transient Point lastClick = null;
    
    /** Tag of the owner of the last province that was clicked on */
    private transient String lastCountry = null;

    private String getLastCountryName() {
        if (mapPanel.getDataSource() instanceof CK2DataSource || mapPanel.getDataSource() instanceof CK3DataSource) {
            if (!currentProvinces.isEmpty()) {
                Province lastClickedProv = currentProvinces.get(currentProvinces.size()-1);
                return TitleMode.getTitleName(lastCountry, lastClickedProv, mapPanel.getDataSource());
            }
            return "";
        } else {
            String name = Text.getText(lastCountry);
            if (name.equals(lastCountry)) { // no text?
                name = mapPanel.getDataSource().getCountry(lastCountry).getString("name");
                if (name != null && !"".equals(name))
                    return name + " (" + lastCountry + ")";
            } else {
                return name + " (" + lastCountry + ")";
            }
        }
        return lastCountry; // just the tag if that's all we have
    }
    
    private void mapPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mapPanelMouseMoved
        updateMapTooltip(evt.getPoint());
    }//GEN-LAST:event_mapPanelMouseMoved
    
    private void updateMapTooltip(final Point mousePt) {
        if (mapPanel.getPreferredSize().height < mousePt.y || mapPanel.getPreferredSize().width < mousePt.x) {
            lastProvRolledOver = null;
            mapPanel.setToolTipText(null);
            return;
        }
        
        final ProvinceData.Province prov = mapPanel.getProvinceAt(mousePt);
        
        if (prov == lastProvRolledOver) {
            // no need to update anything
            return;
        }
        
        lastProvRolledOver = prov;
        
        if (lastProvRolledOver == null) {
            mapPanel.setToolTipText(null);
        } else {
            mapPanel.setToolTipTextForProv(lastProvRolledOver);
        }
        lastPt = mousePt;
    }
    
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        doClose();
    }//GEN-LAST:event_formWindowClosing
    
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        new AboutDialog(this, true).setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    private void showProvHistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showProvHistButtonActionPerformed
        if (currentProvinces.size() == 1) {
            if (!FileEditorDialog.isShowingDialog(currentProvinces.get(0).getId())) {
                FileEditorDialog dlg = new FileEditorDialog(EditorUI.this, currentProvinces.get(0), resolver, provinceData);
                dlg.registerSaveHandler(new FileEditorDialog.SaveHandler() {
                    @Override
                    public void handleSaveEvent(int provId) {
                        mapPanel.getModel().clearHistoryCache();
                        mapPanel.refresh();
                    }

                    @Override
                    public void handleSaveEvent(String tagOrTitle) {
                        mapPanel.getModel().clearHistoryCache();
                        mapPanel.refresh();
                    }
                });
                dlg.setVisible(true);
            }
        } else {
            if (mapPanel.getDataSource() instanceof CK3DataSource) {
                JOptionPane.showMessageDialog(this, "Multi-editing is not yet supported in CK3 because of the different history structure. Please edit a single province at a time.");
                return;
            }
            if (provinceHistoryMergeTool == null) {
                provinceHistoryMergeTool = new ClausewitzHistoryMergeTool();
                provinceHistoryMergeTool.initAutoMergeList(mapPanel.getDataSource());
            }
            // multi file dialog is modal -- too confusing if we let there be more than one at a time
            MultiFileEditorDialog dlg = new MultiFileEditorDialog(this, currentProvinces, resolver, provinceData, provinceHistoryMergeTool);
            dlg.setModal(true);
            dlg.setVisible(true);
            mapPanel.getModel().clearHistoryCache();
        }
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
        showProvHistButton.setEnabled(false);
        final int clickCount = evt.getClickCount();
        final boolean addToSelection = (evt.isControlDown() || evt.isMetaDown() || SwingUtilities.isRightMouseButton(evt));
        final Province selectedProv = lastProvRolledOver;
        
        java.awt.EventQueue.invokeLater(
                () -> {
                    doMouseClick(selectedProv, addToSelection);
                    
                    // If it was a double click, go ahead and show an editor.
                    if (clickCount > 1 && !addToSelection) {
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
        mapPanel.getModel().setDate(mapPanel.getModel().getDate());
        mapPanel.refresh();
    }//GEN-LAST:event_reloadMenuItemActionPerformed

    private void scaleColorsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleColorsButtonActionPerformed
        if (mapPanel.getMode() instanceof DiscreteScalingMapMode) {
            ColorChooserDialog dialog = new ColorChooserDialog(this, (DiscreteScalingMapMode) mapPanel.getMode());
            dialog.setVisible(true);
            mapPanel.refresh();
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

    private void flashProvsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flashProvsButtonActionPerformed
        if (currentProvinces.isEmpty())
            return;
        
        mapPanel.goToProv(currentProvinces.get(0).getId());
        flashCurrentProvinces(3, Color.WHITE);
    }//GEN-LAST:event_flashProvsButtonActionPerformed

    private void copyProvIdsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyProvIdsButtonActionPerformed
        if (currentProvinces.isEmpty())
            return;
        StringBuilder sb = new StringBuilder();
        List<ProvinceData.Province> sortedProvs = new ArrayList<>(currentProvinces);
        Collections.sort(sortedProvs);
        for (ProvinceData.Province prov : sortedProvs) {
            sb.append(prov.getId());
            sb.append(" ");
        }
        StringSelection sel = new StringSelection(sb.toString().trim());
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            flashCurrentProvinces(3, Color.WHITE);
            provNameLabel.setText("Copied " + sb.toString().trim() + "!");
        } catch (IllegalStateException ex) {
            log.log(Level.WARNING, "Couldn''t copy province IDs for some reason. Ids were \"{0}\"", sb.toString().trim());
            log.log(Level.WARNING, "Actual exception is: ", ex);
            JTextField textField = new JTextField("Selected province IDs are: " + sb.toString().trim());
            textField.setEditable(false);
            JOptionPane.showMessageDialog(this, textField);
        }
    }//GEN-LAST:event_copyProvIdsButtonActionPerformed

    private void mapPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_mapPanelMouseWheelMoved
        if (evt.isControlDown()) {
            if (evt.getPreciseWheelRotation() > 0)
                zoomOutAction.actionPerformed(null);
            else
                zoomInAction.actionPerformed(null);
        }
    }//GEN-LAST:event_mapPanelMouseWheelMoved

    private void exportImageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportImageMenuItemActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle("Choose an image file to export the map");
        chooser.setApproveButtonText("Export");
        chooser.setSelectedFile(new java.io.File("export.png"));
        chooser.setFileFilter(new FileNameExtensionFilter("JPG or PNG files", "jpg", "jpeg", "png"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fileName = chooser.getSelectedFile().toString();
            String format;
            if (fileName.toLowerCase().endsWith("jpg")
                    || fileName.toLowerCase().endsWith("jpeg")) {
                format = "JPEG";
            } else if (fileName.toLowerCase().endsWith("png")) {
                format = "PNG";
            } else {
                return;
            }
            
            try {
                ImageIO.write(mapPanel.getCurrentSnapshot(), format, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Saved successfully to " + fileName);
            } catch (IOException ex) {
                log.log(Level.WARNING, "Failed to save snapshot image to {0}", fileName);
                JOptionPane.showMessageDialog(this, "Failed to save image!", "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_exportImageMenuItemActionPerformed
    
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
                    if (get()) {
                        log.log(Level.INFO, "New version found: {0}", Version.getLatestVersion());
                        EditorUI.this.helpMenu.setBackground(Color.ORANGE);
                        EditorUI.this.helpMenu.setOpaque(true);
                        EditorUI.this.checkVersionMenuItem.setBackground(Color.ORANGE);
                    } else {
                        log.log(Level.INFO, "No new version found");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    log.log(Level.SEVERE, null, ex);
                }
            }
        };
        worker.execute();
    }
    
    private void doMouseClick(final ProvinceData.Province p, boolean addToSelection) {
        if (!addToSelection)
            currentProvinces.clear();
        if (addToSelection && currentProvinces.contains(p))
            currentProvinces.remove(p);
        else
            currentProvinces.add(p);
        
        if (p != null && p.getId() != 0 && !currentProvinces.isEmpty()) {
            flashCurrentProvinces(1, Color.WHITE);
        } else { // (p == null)
            provNameLabel.setText("Click to select a province; right-click or hold <ctrl> to select multiple provinces");
            showProvHistButton.setEnabled(false);
            ctryNameLabel.setText("No country");
            showCountryHistButton.setEnabled(false);
            return;
        }
        
        if (currentProvinces.size() == 1)
            provNameLabel.setText(p.getName() + " #" + p.getId());
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(currentProvinces.size()).append(" provinces selected: ");
            boolean first = true;
            for (ProvinceData.Province prov : currentProvinces) {
                if (!first)
                    sb.append(", ");
                first = false;
                sb.append(prov.getName()).append(" #").append(prov.getId());
            }
            provNameLabel.setText(sb.toString());
        }
        
        if (p.getId() != 0) { // Fix this
            showProvHistButton.setEnabled(true);
            if (mapPanel.getModel().getDataSource() instanceof CK2DataSource || mapPanel.getModel().getDataSource() instanceof CK3DataSource) {
                lastCountry = mapPanel.getModel().getHistString(p.getId(), "title");
                if (mapPanel.getModel().getDataSource() instanceof CK3DataSource)
                    lastCountry = map.getDeJureCounty(p.getId());
                if (mapPanel.getMode() instanceof TitleMode) {
                    TitleMode mode = ((TitleMode)mapPanel.getMode());
                    lastCountry = mode.getLiege(lastCountry);
                } else if (mapPanel.getMode() instanceof DeJureTitleMode) {
                    DeJureTitleMode mode = ((DeJureTitleMode)mapPanel.getMode());
                    lastCountry = mode.getLiege(lastCountry);
                }
            } else
                lastCountry = mapPanel.getModel().getOwner(p.getId());
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
                    final String owner = mapPanel.getModel().getOwner(p.getId());
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
                    String cont = map.getContinentOfProv(lastProvRolledOver.getId());
                    if (!cont.equals("(none)")) {
                        mapPanel.setMode(new ContinentMode(mapPanel, cont));
                        viewModeLabel.setText("Provinces in " + cont);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof SingleAreaMode) {
                    String area = map.getAreasOfProv(lastProvRolledOver.getId()).get(0);
                    if (!area.equals("(none)")) {
                        mapPanel.setMode(new SingleAreaMode(mapPanel, area));
                        viewModeLabel.setText("Provinces in " + area);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof SingleRegionMode) {
                    String reg = map.getRegionsOfProv(lastProvRolledOver.getId()).get(0);
                    if (!reg.equals("(none)")) {
                        mapPanel.setMode(new SingleRegionMode(mapPanel, reg));
                        viewModeLabel.setText("Provinces in " + reg);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof SuperRegionMode) {
                    String reg = map.getSuperRegionsOfProv(lastProvRolledOver.getId()).get(0);
                    if (!reg.equals("(none)")) {
                        mapPanel.setMode(new SuperRegionMode(mapPanel, reg));
                        viewModeLabel.setText("Provinces in " + reg);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof ProvinceGroupMode) {
                    String group = map.getGroupsOfProv(lastProvRolledOver.getId()).get(0);
                    if (!group.equals("(none)")) {
                        mapPanel.setMode(new ProvinceGroupMode(mapPanel, group));
                        viewModeLabel.setText("Provinces in " + group);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof ClimateMode) {
                    String climate = map.getClimateOfProv(lastProvRolledOver.getId());
                    if (!climate.equals("(none)")) {
                        mapPanel.setMode(new ClimateMode(mapPanel, climate));
                        viewModeLabel.setText("Provinces with climate " + climate);
                        mapPanel.repaint();
                    }
                } else if (mode instanceof NativeGroupMode) {
                    String nativeType = map.getNativeTypeOfProv(lastProvRolledOver.getId());
                    if (!nativeType.equals("(none)")) {
                        mapPanel.setMode(new NativeGroupMode(mapPanel, nativeType));
                        viewModeLabel.setText("Provinces with " + nativeType + " natives");
                        mapPanel.repaint();
                    }
                } else if (mode instanceof CultureGroupMode) {
                        String culture = mapPanel.getModel().getHistString(p.getId(), "culture");
                        if (culture != null && !culture.isEmpty()) {
                            String group = Utilities.getCultureGroup(culture);
                            if (group != null) {
                                mapPanel.setMode(new CultureGroupMode(mapPanel, group));
                                viewModeLabel.setText(Text.getText(group) + " culture group");
                                mapPanel.repaint();
                            }
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

    private void flashCurrentProvinces(int numFlashes, Color color) {
        // grab just the IDs
        List<Integer> provIds = currentProvinces
                .stream()
                .map(p -> p.getId())
                .collect(Collectors.toList());
        mapPanel.flashProvinces(provIds, numFlashes, color);
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
        currentProvinces = new ArrayList<>();
        lastCountry = null;
        lastPt = null;
        lastProvRolledOver = null;
//        lastClick = null;
    }
    
// <editor-fold defaultstate="collapsed" desc=" Generated Variables ">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton bookmarksButton;
    javax.swing.JMenuItem checkVersionMenuItem;
    javax.swing.JButton copyProvIdsButton;
    private javax.swing.JLabel ctryNameLabel;
    private javax.swing.JSpinner daySpinner;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem exportImageMenuItem;
    javax.swing.JButton flashProvsButton;
    private javax.swing.JButton goToProvButton;
    private javax.swing.JMenuItem goToProvMenuItem;
    javax.swing.JMenu helpMenu;
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
    
    // Actions
    
    private class ZoomInAction extends AbstractAction {
        public ZoomInAction() {
            super("Zoom in", new javax.swing.ImageIcon(EditorUI.this.getClass().getResource("/editor/zoomin.gif")));
            enabled = true;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('=', InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            mapPanel.zoomIn();
            resetScrollPane();
            updateZoomLabel();
            
            if (currentProvinces.size() == 1)
                mapPanel.goToProv(currentProvinces.get(0).getId());
        }
    }
    
    private class ZoomOutAction extends AbstractAction {
        public ZoomOutAction() {
            super("Zoom out", new javax.swing.ImageIcon(EditorUI.this.getClass().getResource("/editor/zoomout.gif")));
            enabled = true;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('-', InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            mapPanel.zoomOut();
            resetScrollPane();
            updateZoomLabel();
            
            if (currentProvinces.size() == 1)
                mapPanel.goToProv(currentProvinces.get(0).getId());
        }
    }
    
    private class GoToProvAction extends AbstractAction {
        public GoToProvAction() {
            super("Go to province");
            enabled = true;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_G);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String response =
                    JOptionPane.showInputDialog(EditorUI.this,
                    "Enter a province ID number (between 1 and " + map.getMaxProvinces() +
                    ") or a province name:",
                    currentProvinces.isEmpty() ? null : currentProvinces.get(0).getId());
            
            if (response == null || response.length() == 0)
                return;
            response = response.trim();
            
            Province p;
            
            if (response.matches("[0-9]{1,}")) {
                int prov = Integer.parseInt(response);
                if (prov <= 0 || prov > map.getMaxProvinces()) {
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
            doMouseClick(p, false);
        }
    }
    
    private class SetDateAction extends AbstractAction {
        public SetDateAction() {
            super("Set date");
            enabled = true;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String date = yearSpinner.getValue().toString() + "." +
                    monthSpinner.getValue().toString() + "." +
                    daySpinner.getValue().toString();
            log.log(Level.FINE, "Setting date to {0}", date);
            mapPanel.getModel().setDate(date);
//            ((ProvinceEditorPanel)innerProvincePanel).setDate(date);
            mapPanel.refresh();
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
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] splitDate = date.split("\\.");
            ((SpinnerNumberModel)yearSpinner.getModel()).setValue(Integer.parseInt(splitDate[0]));
            ((SpinnerNumberModel)monthSpinner.getModel()).setValue(Integer.parseInt(splitDate[1]));
            ((SpinnerNumberModel)daySpinner.getModel()).setValue(Integer.parseInt(splitDate[2]));
            mapPanel.getModel().setDate(date);
            mapPanel.refresh();
        }
    }
    
}
