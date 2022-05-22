/*
 * PositionsEditorUI.java
 *
 * Created on May 14, 2007, 11:02 AM
 */

package posed;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.shared.Style;
import eug.shared.WritableObject;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.AbstractListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Michael Myers
 */
public class PositionsEditorUI extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(PositionsEditorUI.class.getName());
    
    private static final String APP_NAME    = "Positions Editor";
    private static final String APP_VERSION = "Beta";
    
    private MapPanel mapPanel;
    
    private File mapDir;
    private File positionsFile;
    GenericObject positions;
    private final GameVersion gameVersion;

    private final java.util.Map<Point, String> validationErrors;
    
    private boolean hasUnsavedChanges;
    
    public PositionsEditorUI(String mapFileName, GameVersion gameVersion, boolean useLocalization) {
        this.gameVersion = gameVersion;
        initComponents();
        
        load(mapFileName, gameVersion, useLocalization);
        
        setLocationRelativeTo(null);
        jSplitPane1.setDividerLocation(0.8);

        validationErrors = new java.util.HashMap<>();
    }
    
    private void load(String mapFileName, GameVersion gameVersion, boolean useLocalization) {
        File mapFile = new File(mapFileName);
        mapDir = mapFile.getParentFile();

        File mainDir = mapDir.getParentFile();
        if (useLocalization)
            Text.initText(mainDir);

        Map map = new Map(mapFile.getAbsolutePath(), gameVersion, useLocalization);

        positionsFile = new File(mapFile.getParent() + "/" + map.getString("positions"));
        log.log(Level.INFO, "Loading positions from {0}", positionsFile.getAbsolutePath());

        positions = EUGFileIO.load(positionsFile, ParserSettings.getQuietSettings());
        mapPanel = new MapPanel(map, mapDir, positions, gameVersion);
        mapScrollPane.setViewportView(mapPanel);

        provinceList.setModel(new ProvListModel());
        provinceList.setPrototypeCellValue("99999 - Province Name"); // so it doesn't have to calculate the maximum width

        MapPanelMouseListener listener = new MapPanelMouseListener();
        mapPanel.addMouseListener(listener);
        mapPanel.addMouseMotionListener(listener);

        pack();
        setLocationRelativeTo(null);
        setTitle(gameVersion.getDisplay() + " " + APP_NAME + " [" + positionsFile.getPath() + "]");
        validateFile();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        positionTextArea = new javax.swing.JEditorPane();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        mapScrollPane = new javax.swing.JScrollPane();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        provinceList = new javax.swing.JList<>();
        goToProvButton = new javax.swing.JButton();
        javax.swing.JMenuBar menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        checkMapMenuItem = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator3 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu viewMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        javax.swing.JSeparator jSeparator2 = new javax.swing.JSeparator();
        showPositionsMenuItem = new javax.swing.JCheckBoxMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(gameVersion.getDisplay() + " " + APP_NAME);
        addWindowListener(formListener);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel2.setLayout(new java.awt.BorderLayout());

        positionTextArea.setEditable(false);
        positionTextArea.setEditorKit(new eug.syntax.EUGEditorKit());
        jScrollPane2.setViewportView(positionTextArea);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel2);

        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(mapScrollPane, java.awt.BorderLayout.CENTER);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Provinces"));
        jPanel1.setLayout(new java.awt.BorderLayout());

        provinceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        provinceList.addMouseListener(formListener);
        provinceList.addListSelectionListener(formListener);
        jScrollPane1.setViewportView(provinceList);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        goToProvButton.setText("Center on province");
        goToProvButton.addActionListener(formListener);
        jPanel1.add(goToProvButton, java.awt.BorderLayout.SOUTH);

        jPanel3.add(jPanel1, java.awt.BorderLayout.WEST);

        jSplitPane1.setLeftComponent(jPanel3);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");

        checkMapMenuItem.setText("Check map pixels");
        checkMapMenuItem.addActionListener(formListener);
        fileMenu.add(checkMapMenuItem);
        fileMenu.add(jSeparator3);

        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(formListener);
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setText("Save as...");
        saveAsMenuItem.addActionListener(formListener);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(jSeparator1);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(formListener);
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        viewMenu.setText("View");

        zoomInMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_EQUALS, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        zoomInMenuItem.setText("Zoom in");
        zoomInMenuItem.addActionListener(formListener);
        viewMenu.add(zoomInMenuItem);

        zoomOutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_MINUS, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        zoomOutMenuItem.setText("Zoom out");
        zoomOutMenuItem.addActionListener(formListener);
        viewMenu.add(zoomOutMenuItem);
        viewMenu.add(jSeparator2);

        showPositionsMenuItem.setText("Show positions on map");
        showPositionsMenuItem.setToolTipText("Not currently functional");
        showPositionsMenuItem.setEnabled(false);
        showPositionsMenuItem.addActionListener(formListener);
        viewMenu.add(showPositionsMenuItem);

        menuBar.add(viewMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(formListener);
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.MouseListener, java.awt.event.WindowListener, javax.swing.event.ListSelectionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == goToProvButton) {
                PositionsEditorUI.this.goToProvButtonActionPerformed(evt);
            }
            else if (evt.getSource() == checkMapMenuItem) {
                PositionsEditorUI.this.checkMapMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == saveMenuItem) {
                PositionsEditorUI.this.saveMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == saveAsMenuItem) {
                PositionsEditorUI.this.saveAsMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == exitMenuItem) {
                PositionsEditorUI.this.exitMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == zoomInMenuItem) {
                PositionsEditorUI.this.zoomInMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == zoomOutMenuItem) {
                PositionsEditorUI.this.zoomOutMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == showPositionsMenuItem) {
                PositionsEditorUI.this.showPositionsMenuItemActionPerformed(evt);
            }
            else if (evt.getSource() == aboutMenuItem) {
                PositionsEditorUI.this.aboutMenuItemActionPerformed(evt);
            }
        }

        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getSource() == provinceList) {
                PositionsEditorUI.this.provinceListMouseClicked(evt);
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

        public void windowActivated(java.awt.event.WindowEvent evt) {
        }

        public void windowClosed(java.awt.event.WindowEvent evt) {
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == PositionsEditorUI.this) {
                PositionsEditorUI.this.formWindowClosing(evt);
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

        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
            if (evt.getSource() == provinceList) {
                PositionsEditorUI.this.provinceListValueChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void provinceListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_provinceListMouseClicked
        if (evt.getClickCount() == 2) {
            int prov = provinceList.getSelectedIndex();
            GenericObject position = positions.getChild(Integer.toString(prov));
            EditorDialog dialog = new EditorDialog(this,
                    mapPanel.createImage(prov),
                    position,
                    mapPanel.getMap().isLand(prov) ? gameVersion.getLand() : gameVersion.getSea());
            dialog.setVisible(true);
            if (dialog.isSaveChanges()) {
                log.log(Level.INFO, "Province {0}:", prov);
                log.log(Level.INFO, "Old entry:\n{0}", position);
                log.log(Level.INFO, "New entry:\n{0}", dialog.getPositions());
                if (position != null) {
                    position.clear();
                    if (dialog.getPositions() != null)
                        position.addAllChildren(dialog.getPositions());
                } else if (dialog.getPositions() != null ) {
                    positions.addChild(dialog.getPositions());
                }
                
                hasUnsavedChanges = true;
            }
        }
    }//GEN-LAST:event_provinceListMouseClicked

    private void zoomInMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInMenuItemActionPerformed
        mapPanel.zoomIn();
        mapPanel.repaint();
        mapScrollPane.setViewport(mapScrollPane.getViewport());
    }//GEN-LAST:event_zoomInMenuItemActionPerformed

    private void zoomOutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutMenuItemActionPerformed
        mapPanel.zoomOut();
        mapPanel.repaint();
        mapScrollPane.setViewport(mapScrollPane.getViewport());
    }//GEN-LAST:event_zoomOutMenuItemActionPerformed

    private void showPositionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPositionsMenuItemActionPerformed
        mapPanel.setPaintPositions(showPositionsMenuItem.isSelected());
        mapPanel.repaint();
    }//GEN-LAST:event_showPositionsMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        if (!hasUnsavedChanges) {
            JOptionPane.showMessageDialog(this, "There are no changes to save.");
            return;
        }
        
        if (!EUGFileIO.save(positions, positionsFile.getAbsolutePath(), EUGFileIO.NO_COMMENT, true, POSITIONS_STYLE))
            JOptionPane.showMessageDialog(this, "Failed to save", "Error", JOptionPane.ERROR_MESSAGE);
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        if (!hasUnsavedChanges) {
            JOptionPane.showMessageDialog(this, "There are no changes to save.");
            return;
        }
        
        JFileChooser chooser = new JFileChooser(mapDir);
        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            positionsFile = chooser.getSelectedFile();
            if (!EUGFileIO.save(positions, positionsFile.getAbsolutePath(), EUGFileIO.NO_COMMENT, true, POSITIONS_STYLE))
                JOptionPane.showMessageDialog(this, "Failed to save", "Error", JOptionPane.ERROR_MESSAGE);
            setTitle(gameVersion.getDisplay() + " " + APP_NAME + " [" + positionsFile.getPath() + "]");
        }
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        doClose();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        doClose();
    }//GEN-LAST:event_formWindowClosing

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        Object[] options = { "Visit EU3 forum thread", "Close" };
        
        int ret = JOptionPane.showOptionDialog(this, APP_NAME + " " + APP_VERSION + "\nBy MichaelM",
                "About", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[options.length-1]);
        
        if (ret == 1 || ret < 0)
            return;
        
        String thread = "https://forum.paradoxplaza.com/forum/index.php?threads/clausewitz-positions-editor.535767/";
        
        try {
            Desktop.getDesktop().browse(new java.net.URI(thread));
        } catch (URISyntaxException | IOException ex) {
            log.log(Level.SEVERE, "Could not browse to forum thread " + thread, ex);

            JOptionPane.showMessageDialog(this,
                    "Could not open browser. Please visit " + thread + " in your browser instead.",
                    "Update failed", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void goToProvButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToProvButtonActionPerformed
        int prov = provinceList.getSelectedIndex();
        mapPanel.goToProv(prov);
        mapPanel.flashProvince(prov, 2);
    }//GEN-LAST:event_goToProvButtonActionPerformed

    private void provinceListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_provinceListValueChanged
        int prov = provinceList.getSelectedIndex();
        GenericObject position = positions.getChild(Integer.toString(prov)); // -1 because there is no entry for 0 (PTI)
        if (position != null)
            positionTextArea.setText(position.toString(POSITIONS_STYLE));
        else
            positionTextArea.setText("# No previous data");
        positionTextArea.setCaretPosition(0);
    }//GEN-LAST:event_provinceListValueChanged

    private static String getColorString(Color c) {
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }
    
    private void checkMapMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMapMenuItemActionPerformed
        int ret = JOptionPane.showConfirmDialog(
                this,
                "This operation will check every pixel in the map for possible"
                        + " errors (missing provinces and misplaced pixels)."
                        + " This may take several minutes.\n\nDo you want to continue?",
                "Confirm operation", JOptionPane.YES_NO_OPTION);
        
        if (ret == JOptionPane.NO_OPTION)
            return;
        
        List<String> output = new ArrayList<>();
        
        final int width = mapPanel.getMapImage().getWidth();
        int[] rgbData = new int[width];
        
        for (int y = 0; y < mapPanel.getMapImage().getHeight(); y++) {
            mapPanel.getMapImage().getRGB(0, y, width, 1, rgbData, 0, y);
            
            for (int x = 0; x < width; x++) {
                int rgb = rgbData[x];
                if (mapPanel.getMap().getProvinceData().getProv(rgb) == null) {
                    Color c = new Color(rgb);
                    String msg = "Pixel at "  + x + "," + y + ": no province for rgb " + getColorString(c);

                    List<String> neighbors = new ArrayList<>();
                    
                    for (int tempy = y-1; tempy <= y+1; tempy++) {
                        if (tempy < 0 || tempy >= mapPanel.getMapImage().getHeight())
                            continue;
                        
                        for (int tempx = x-1; tempx <= x+1; tempx++) {
                            if (tempx < 0)
                                tempx = mapPanel.getMapImage().getWidth() - 1;
                            else if (tempx == mapPanel.getMapImage().getWidth())
                                break;
                            
                            int temprgb = mapPanel.getMapImage().getRGB(tempx, tempy);
                            ProvinceData.Province p = mapPanel.getMap().getProvinceData().getProv(temprgb);
                            if (p != null && !neighbors.contains(p.getName() + "(" + getColorString(new Color(temprgb)) + ")"))
                                neighbors.add(p.getName() + " (" + getColorString(new Color(temprgb)) + ")");
                        }
                    }
                    msg += "; neighbors are " + neighbors.toString();
                    output.add(msg);
                    validationErrors.put(new Point(x, y), "Invalid province");
                } else {
                    int neighborPixelCount = 0;
                    boolean hasNonCornerNeighbor = false;
                    List<Integer> neighbors = new ArrayList<>();
                    
                    for (int tempy = y-1; tempy <= y+1; tempy++) {
                        if (tempy < 0 || tempy >= mapPanel.getMapImage().getHeight())
                            continue;
                        
                        for (int tempx = x-1; tempx <= x+1; tempx++) {
                            if (tempx < 0 || tempx >= mapPanel.getMapImage().getWidth())
                                continue;
                            else if (tempx == x && tempy == y)
                                continue;
                            
                            int temprgb = mapPanel.getMapImage().getRGB(tempx, tempy);
                            if (temprgb == rgb) {
                                neighborPixelCount++;
                                if (tempy == y || tempx == x)
                                    hasNonCornerNeighbor = true;
                            }
                            if (!neighbors.contains(temprgb))
                                neighbors.add(temprgb);
                        }
                    }
                    if (neighborPixelCount == 0) {
                        if (neighbors.size() == 1 && mapPanel.getMap().isRGBLand(rgb) == mapPanel.getMap().isRGBLand(neighbors.get(0))) {
                            //String terra = mapPanel.getMap().isRGBLand(rgb) ? "land" : "sea";
                            StringBuilder builder = new StringBuilder("Pixel at ").append(x).append(",").append(y);
                            builder.append(" (").append(mapPanel.getMap().getProvinceData().getProv(rgb).getName()).append(") has no neighboring pixels of the same color; neighbors are ");
                            for (int temprgb : neighbors) {
                                ProvinceData.Province p = mapPanel.getMap().getProvinceData().getProv(temprgb);
                                if (p != null)
                                    builder.append(p.getName()).append(" (").append(getColorString(new Color(temprgb))).append(") ");
                            }
                            validationErrors.put(new Point(x, y), "Warning: No neighboring pixels of the same color");
                            output.add(builder.toString());
                        }
                    } else if (neighborPixelCount == 1 && hasNonCornerNeighbor == false) {
                        // if the pixel is surrounded on 7 sides by another province
                        // and both provinces are sea or both are land, that's suspicious
                        if (neighbors.size() == 2
                                && mapPanel.getMap().isRGBLand(neighbors.get(0)) == mapPanel.getMap().isRGBLand(neighbors.get(1))) {
                            String terra = mapPanel.getMap().isRGBLand(neighbors.get(0)) ? "land" : "sea";
                            validationErrors.put(new Point(x, y), "Warning: Pixel mostly surrounded by another province and both are " + terra);
                            output.add("Pixel at " + x + "," + y + " ("
                                    + mapPanel.getMap().getProvinceData().getProv(rgb).getName()
                                    + ") mostly surrounded by " +
                                    mapPanel.getMap().getProvinceData().getProv(neighbors.get(neighbors.get(0) == rgb ? 1 : 0)).getName()
                                    + " and both are " + terra);
                        }
                    }
                }
            }
        }

        if (output.size() > 0) {
            try {
                Files.write(Paths.get("mapcheck.txt"), output);
                Desktop.getDesktop().open(new File("mapcheck.txt"));
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Could not write map check log file", ex);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No map issues detected.");
        }

        log.log(Level.INFO, "{0} errors", output.size());
    }//GEN-LAST:event_checkMapMenuItemActionPerformed

    private static final GenericObject xy = EUGFileIO.loadFromString("x = double y = double");
    private void validateFile() {
        log.log(Level.INFO, "Validating positions.txt against the position_types definition...");
        for (GenericObject obj : positions.children) {
            if (mapPanel.getMap().isLand(Integer.parseInt(obj.name))) {
                validateObject(obj.name, obj, gameVersion.getLand());
            } else {
                validateObject(obj.name, obj, gameVersion.getSea());
            }
        }
        log.log(Level.INFO, "Done validating positions.txt");
    }

    private void validateObject(String name, GenericObject positions, GenericObject validation) {
        for (WritableObject wo : positions.getAllWritable()) {
            if (wo instanceof GenericObject) {
                GenericObject obj = (GenericObject) wo;
                String objName = obj.name.equals("") ? "empty" : obj.name;
                GenericObject subValidation = validation.getChild(objName);

                if (subValidation != null) {
                    validateObject(name, obj, subValidation);
                } else if (validation.getString(objName).equals("xy")) {
                    // special case: xy is really an object
                    validateObject(name, obj, xy);
                } else {
                    log.log(Level.WARNING, "Unknown object name in {0} ({1}): {2}", new Object[]{name, validation.name, obj.name});
                }
            } else if (wo instanceof ObjectVariable) {
                String varname = ((ObjectVariable) wo).varname;
                String subv = validation.getString(varname);

                if (subv.equals("")) {
                    log.log(Level.WARNING, "Unknown variable name in {0}: {1}", new Object[]{name, varname});
                }
            } else if (wo instanceof GenericList) {
                String listName = ((GenericList) wo).getName();
                GenericList valList = validation.getList(listName);
                
                if (valList != null)
                    validateList(name, (GenericList) wo, valList);
                else
                    log.log(Level.WARNING, "Did not expect list: {0}", ((GenericList)wo).getName());
            }
        }
    }
    
    /**
     * Validates a list of values against a template list. The items in the
     * template list must include a slash "/" which separates the entry name
     * from the entry data type. For example, the item "port/rotation" in the
     * template list will cause the validation to expect a rotation (radian
     * value in decimal) in the list being checked. The special data type "/xy"
     * causes the validation to expect <i>two</i> values, one each for an X and
     * a Y coordinate.
     * 
     * @param objectName the name of the object in which the list is found, for debugging purposes
     * @param list
     * @param validationList 
     */
    private void validateList(String objectName, GenericList list, GenericList validationList) {
        // Lists must be in a definite order
        int i = 0;
        for (int j = 0; j < validationList.size(); j++) {
            String val = validationList.get(j);
            i++;
            if (i > list.size())
                log.log(Level.WARNING, "List in province {0} did not have enough elements to match the validation: {1}", new Object[]{ objectName, list.getName() });
            
            if (val.contains("/")) {
                String[] valSplit = val.split("/");
                String valType = valSplit[1];
                if (valType.equals("xy"))
                    i++; // expect two list elements for an X/Y coordinate pair
            }
        }
    }

    private void doClose() {
        if (hasUnsavedChanges) {
            int choice = JOptionPane.showConfirmDialog(this, "You have unsaved changes. Do you want to save them before exiting?");
            if (choice == JOptionPane.YES_OPTION) {
                saveMenuItemActionPerformed(null);
            } else if (choice == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        dispose();
    }
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new PositionsEditorUI().setVisible(true);
//            }
//        });
//    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JMenuItem aboutMenuItem;
    javax.swing.JMenuItem checkMapMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    javax.swing.JButton goToProvButton;
    javax.swing.JSplitPane jSplitPane1;
    javax.swing.JScrollPane mapScrollPane;
    javax.swing.JEditorPane positionTextArea;
    javax.swing.JList<String> provinceList;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    javax.swing.JCheckBoxMenuItem showPositionsMenuItem;
    javax.swing.JMenuItem zoomInMenuItem;
    javax.swing.JMenuItem zoomOutMenuItem;
    // End of variables declaration//GEN-END:variables

    private static final Style POSITIONS_STYLE = new Style() {

        @Override
        public String getTab(int depth) {
            return Style.DEFAULT.getTab(depth);
        }

        @Override
        public String getEqualsSign(int depth) {
            return Style.DEFAULT.getEqualsSign(depth);
        }

        @Override
        public String getCommentStart() {
            return Style.DEFAULT.getCommentStart();
        }

        @Override
        public void printTab(BufferedWriter bw, int depth) throws IOException {
            Style.DEFAULT.printTab(bw, depth);
        }

        @Override
        public void printEqualsSign(BufferedWriter bw, int depth) throws IOException {
            Style.DEFAULT.printEqualsSign(bw, depth);
        }

        @Override
        public void printCommentStart(BufferedWriter bw, int depth) throws IOException {
            Style.DEFAULT.printCommentStart(bw, depth);
        }

        @Override
        public void printHeaderCommentStart(BufferedWriter bw, int depth) throws IOException {
            // Do nothing
        }

        @Override
        public void printHeaderCommentEnd(BufferedWriter bw, int depth) throws IOException {
            // Do nothing
        }

        @Override
        public boolean isInline(GenericObject obj) {
            return obj.isEmpty();
        }

        @Override
        public boolean newLineAfterObject() {
            return Style.DEFAULT.newLineAfterObject();
        }

        @Override
        public void printOpeningBrace(BufferedWriter bw, int depth) throws IOException {
            bw.write("{");
        }

        @Override
        public boolean isInline(GenericList list) {
            return true;
        }
    };

    private final class ProvListModel extends AbstractListModel<String> {
        private final int max;

        public ProvListModel() {
            max = Integer.parseInt(mapPanel.getMap().getString("max_provinces"));
        }

        @Override
        public int getSize() {
            return max;
        }

        @Override
        public String getElementAt(int index) {
            ProvinceData.Province prov = mapPanel.getProvinceData().getProvByID(index);
            if (prov != null) {
                return Integer.toString(index) + " - " + prov.getName();
            } else {
                log.log(Level.WARNING, "No province for id {0}", index);
                return "Unknown province #" + index;
            }
        }
    }

    private final class MapPanelMouseListener extends MouseAdapter {

        public MapPanelMouseListener() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            doClick(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            doClick(e);
        }

        private void doClick(MouseEvent e) {
            ProvinceData.Province p = mapPanel.getProvinceAt(e.getPoint());
            if (p == null || p.getId() == 0) {
                return;
            }
            if (e.getClickCount() == 2) {
                provinceListMouseClicked(e);
            } else {
                provinceList.setSelectedIndex(p.getId());
                provinceList.ensureIndexIsVisible(p.getId());
                mapPanel.flashProvince(p.getId());
            }
        }
    }

    private static final class MapFileFilter extends FileFilter {

        public MapFileFilter() {
        }

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".map");
        }

        @Override
        public String getDescription() {
            return "Map files";
        }
    }
}
