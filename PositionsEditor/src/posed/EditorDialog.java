/*
 * EditorDialog.java
 *
 * Created on December 20, 2007, 11:07 PM
 */

package posed;

import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.shared.WritableObject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

/**
 *
 * @author Michael Myers
 */
public class EditorDialog extends javax.swing.JDialog implements ActionListener, MouseListener, MouseMotionListener, WindowListener {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(EditorDialog.class.getName());
    
    private final MapPanel.ProvinceImage image;
    private GenericObject originalPositions;
    private final GenericObject positionTypes;

    private GenericObject positions;

    private final java.util.Map<String, Object> allFields = new LinkedHashMap<>();
    
    private boolean saveChanges = false;
    
    // Locale.US ensures that decimal points won't be switched to commas
    // so we don't get { x = 1.213,500000 y = 694,000000 }
    private static final NumberFormat sixDigitFormat = NumberFormat.getNumberInstance(Locale.US);
    static {
        if (sixDigitFormat instanceof DecimalFormat) {
            sixDigitFormat.setGroupingUsed(false);      // don't print commas in number bigger than 1000
            sixDigitFormat.setMinimumFractionDigits(6); // Paradox uses 6 digits
            sixDigitFormat.setMaximumFractionDigits(6);
        }
    }
    
    
    public EditorDialog(java.awt.Frame parent, MapPanel.ProvinceImage image,
            GenericObject positions, GenericObject positionTypes) {
        super(parent, true);
        this.image = image;
        this.positions = positions;
        this.positionTypes = positionTypes;
        
        initComponents();
        ((ProvincePanel)provincePanel).setImage(image);
        ((ProvincePanel)provincePanel).setPositionData(positions);
        
        if (positions != null) {
            this.originalPositions = positions.clone();
        }
        initFields();
        
        setTitle("Editing " + image.getProvName());
        pack();
        setLocationRelativeTo(parent);
    }

    private String toReadableLabel(String label) {
        if (label.equalsIgnoreCase("empty"))
            return "";
        
        String[] tmp = label.split("_");
        StringBuilder ret = new StringBuilder();
        ret.append(Character.toUpperCase(label.charAt(0))).append(tmp[0].substring(1));
        for (int i = 1; i < tmp.length; i++) {
            ret.append(" ").append(tmp[i]);
        }
        return ret.toString();
    }

    private void initFields() {
        JPanel container = new JPanel();
        JPanel subContainer = new JPanel();
        subContainer.setLayout(new BoxLayout(subContainer, BoxLayout.Y_AXIS));
        container.add(subContainer);

        positionsPane.add(container, "Main");
        initFields(subContainer, positions, positionTypes, allFields);
    }
    
    private void initFields(JPanel parent, GenericObject positions, GenericObject positionTypes, java.util.Map<String, Object> registry) {
        for (WritableObject wo : positionTypes.getAllWritable()) {
            if (wo instanceof ObjectVariable) {
                ObjectVariable var = (ObjectVariable) wo;

                JPanel container = new JPanel(new GridLayout(0, 3, 1, 5));
                container.setBorder(new LineBorder(Color.LIGHT_GRAY));
                parent.add(container);

                JLabel label = new JLabel(toReadableLabel(var.varname));
                container.add(label);

                if (var.getValue().equalsIgnoreCase("xy")) {
                    createXYFields(container, positions, var, registry);
                } else if (var.getValue().equalsIgnoreCase("rotation")) {
                    createRotationField(container, positions, var, false, registry);
                } else if (var.getValue().equalsIgnoreCase("reverserotation")) {
                    createRotationField(container, positions, var, true, registry);
                } else if (var.getValue().equalsIgnoreCase("scale")) {
                    createNonButtonField(container, positions, var, registry);
                } else if (var.getValue().equalsIgnoreCase("nudge")) {
                    createNonButtonField(container, positions, var, registry);
                }
            } else if (wo instanceof GenericObject) {
                GenericObject obj = (GenericObject) wo;
                if (obj.isEmpty())
                    continue;

                JPanel container = new JPanel();
                JPanel subContainer = new JPanel();
                subContainer.setLayout(new BoxLayout(subContainer, BoxLayout.Y_AXIS));
                container.add(subContainer);

                positionsPane.add(container, toReadableLabel(obj.name));

                java.util.Map<String, Object> tmpMap = new java.util.LinkedHashMap<>();
                registry.put(obj.name, tmpMap);
                initFields(subContainer, positions.getChild(obj.name), obj, tmpMap);
            }
        }
    }

    private void createNonButtonField(JPanel parent, GenericObject positions, ObjectVariable var, java.util.Map<String, Object> registry) {
        JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JTextField scaleField = new JTextField(8);
        if (positions != null)
            scaleField.setText(positions.getString(var.varname));
        middlePanel.add(scaleField);
        parent.add(middlePanel);
        parent.add(new JPanel()); // to take the JButton's slot

        registry.put(var.varname, scaleField);
    }

    private void createRotationField(JPanel parent, GenericObject positions, final ObjectVariable var, final boolean reversed, java.util.Map<String, Object> registry) {
        JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JTextField rotationField = new JTextField(8);
        if (positions != null)
            rotationField.setText(positions.getString(var.varname));
        middlePanel.add(rotationField);
        parent.add(middlePanel);
        JPanel rightPanel = new JPanel();
        JButton editButton = new JButton("Edit...");

        editButton.addActionListener((ActionEvent e) -> {
            double oldRotation = 0.0;
            try {
                oldRotation = Double.parseDouble(rotationField.getText());
                if (reversed) {
                    oldRotation = -oldRotation;
                    if (oldRotation < 0.0)
                        oldRotation += 2.0*Math.PI;
                }
            } catch (NumberFormatException ex) {
            }
            
            RotationDialog dialog = new RotationDialog(EditorDialog.this, oldRotation, var.varname);
            dialog.setVisible(true);
            
            double rotation = dialog.getRotation();
            
            if (reversed) {
                rotation = -rotation;
                if (rotation < 0.0)
                    rotation += 2.0*Math.PI;
            }
            
            rotationField.setText(rotation != 0.0 ? sixDigitFormat.format(rotation) : "");
            
            update();
        });

        rightPanel.add(editButton);
        parent.add(rightPanel);

        registry.put(var.varname, rotationField);
    }

    private void createXYFields(JPanel parent, GenericObject positions, ObjectVariable var, java.util.Map<String, Object> registry) {
        JPanel middlePanel = new JPanel(new GridLayout(0, 1));
        JPanel xPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 1));
        xPanel.add(new JLabel("x"));
        final JTextField xField = new JTextField(7); // 11 columns to fit 1000.000000
        xPanel.add(xField);
        JPanel yPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 1));
        yPanel.add(new JLabel("y"));
        final JTextField yField = new JTextField(7);
        yPanel.add(yField);
        if (positions != null) {
            GenericObject position = positions.getChild(var.varname);
            if (position != null) {
                xField.setText(position.getString("x"));
                yField.setText(position.getString("y"));
            }
        }
        middlePanel.add(xPanel);
        middlePanel.add(yPanel);
        parent.add(middlePanel);
        JPanel rightPanel = new JPanel();
        JButton editButton = new JButton("Edit...");
        editButton.addActionListener((ActionEvent e) -> {
            doSetPosition(xField, yField);
        });
        rightPanel.add(editButton);
        parent.add(rightPanel);

        java.util.Map<String, Object> tmpMap = new java.util.LinkedHashMap<>();
        tmpMap.put("x", xField);
        tmpMap.put("y", yField);
        registry.put(var.varname, tmpMap);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        textColorToggleButton = new javax.swing.JToggleButton();
        updateButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        javax.swing.JPanel jPanel24 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel25 = new javax.swing.JPanel();
        positionsPane = new javax.swing.JTabbedPane();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        provincePanel = new posed.ProvincePanel(positionTypes);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        textColorToggleButton.setText("Toggle text color");
        textColorToggleButton.addActionListener(this);
        jPanel2.add(textColorToggleButton);

        updateButton.setText("Update image");
        updateButton.addActionListener(this);
        jPanel2.add(updateButton);

        okButton.setText("OK");
        okButton.addActionListener(this);
        jPanel2.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this);
        jPanel2.add(cancelButton);

        jPanel3.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 1, 1));

        statusLabel.setText(" ");
        jPanel4.add(statusLabel);

        jPanel3.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jPanel1.add(jPanel3, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jPanel24, java.awt.BorderLayout.WEST);
        jPanel1.add(jPanel25, java.awt.BorderLayout.EAST);
        jPanel1.add(positionsPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.WEST);

        provincePanel.addMouseListener(this);
        provincePanel.addMouseMotionListener(this);
        jScrollPane1.setViewportView(provincePanel);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == textColorToggleButton) {
            EditorDialog.this.textColorToggleButtonActionPerformed(evt);
        }
        else if (evt.getSource() == updateButton) {
            EditorDialog.this.updateButtonActionPerformed(evt);
        }
        else if (evt.getSource() == okButton) {
            EditorDialog.this.okButtonActionPerformed(evt);
        }
        else if (evt.getSource() == cancelButton) {
            EditorDialog.this.cancelButtonActionPerformed(evt);
        }
    }

    public void mouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getSource() == provincePanel) {
            EditorDialog.this.provincePanelMouseClicked(evt);
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
    }

    public void mouseMoved(java.awt.event.MouseEvent evt) {
        if (evt.getSource() == provincePanel) {
            EditorDialog.this.provincePanelMouseMoved(evt);
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
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        update();
    }//GEN-LAST:event_updateButtonActionPerformed


    private void update() {
        positions = new GenericObject().createChild(Integer.toString(image.getProvId()));
        doUpdate(positions, allFields);

        positions.setHeadComment(image.getProvName());
        ((ProvincePanel)provincePanel).setPositionData(positions);
        provincePanel.repaint();
    }


    private void doUpdate(GenericObject positions, java.util.Map<String, Object> objMap) {
        for (java.util.Map.Entry<String, Object> entry : objMap.entrySet()) {
            if (entry.getValue() instanceof JTextField) {
                JTextField jtf = (JTextField) entry.getValue();
                if (jtf.getText().length() > 0)
                    positions.addString(entry.getKey(), jtf.getText());
            } else if (entry.getValue() instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> tmpMap = (java.util.Map<String, Object>) entry.getValue();
                GenericObject tmpPos = positions.createChild(entry.getKey());
                doUpdate(tmpPos, tmpMap);
                if (tmpPos.isEmpty())
                    positions.removeChild(tmpPos);
            } else {
                log.log(Level.WARNING, "Internal error: Unknown positions value {0}, {1}",
                        new Object[] { entry.getKey(), entry.getValue() });
            }
        }
    }
    
    private void provincePanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_provincePanelMouseMoved
        double x = ((ProvincePanel)provincePanel).reverseTranslateX(evt.getX());
        double y = ((ProvincePanel)provincePanel).reverseTranslateY(evt.getY());
        statusLabel.setText("x=" + x + ", y=" + y);
    }//GEN-LAST:event_provincePanelMouseMoved

    private void provincePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_provincePanelMouseClicked
//        int rgb = provincePanel.getRGB(evt.getPoint());
        
    }//GEN-LAST:event_provincePanelMouseClicked

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        doClose();
    }//GEN-LAST:event_formWindowClosing

    private void textColorToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textColorToggleButtonActionPerformed
        ((ProvincePanel)provincePanel).setTextColor(textColorToggleButton.isSelected() ? Color.LIGHT_GRAY : Color.BLACK);
        provincePanel.repaint();
    }//GEN-LAST:event_textColorToggleButtonActionPerformed

    private void doSetPosition(final JTextField xField, final JTextField yField) {
        // TODO: Disable buttons and reenable after the click
        // TODO: Make the button a cancel button while this is active
        statusLabel.setText("Click somewhere in the province");
        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double x = ((ProvincePanel)provincePanel).reverseTranslateX(e.getX());
                double y = ((ProvincePanel)provincePanel).reverseTranslateY(e.getY());

                xField.setText(sixDigitFormat.format(x));
                yField.setText(sixDigitFormat.format(y));

                update();
                provincePanel.removeMouseListener(this);
                provincePanel.addMouseListener(EditorDialog.this);
            }
        };
        provincePanel.removeMouseListener(this);
        provincePanel.addMouseListener(listener);
    }
        
    private void doClose() {
        update();
        
        if ((originalPositions == null && positions.isEmpty()) || positions.equals(originalPositions) /*editorPane.getText().equals(originalText)*/) {
            saveChanges = false;
            dispose();
            return;
        }
        
        int save = JOptionPane.showConfirmDialog(this, "Do you want to save changes?\n"
                + "This is not permanent; you will get another chance to save or discard changes when you exit the main program.");
        if (save == JOptionPane.YES_OPTION) {
            saveChanges = true;
        } else if (save == JOptionPane.NO_OPTION) {
            saveChanges = false;
            dispose();
            return;
        } else {
            return;
        }
        
//        if (saveChanges && updateButton.isEnabled()) {
//            int choice =
//                    JOptionPane.showConfirmDialog(
//                    this,
//                    "You have not updated since your last changes. Would you like to use them anyway?"
//                    );
//            if (choice == JOptionPane.YES_OPTION) {
//                updateButtonActionPerformed(null);
////                positions = EUGFileIO.loadFromString(editorPane.getText(), settings);
//                if (positions != null)
//                    positions = positions.getChild(Integer.toString(image.getProvId()));
//            } else if (choice == JOptionPane.NO_OPTION) {
//                // use previously loaded positions
//            } else {
//                return;
//            }
//        }
        if (saveChanges && positions == null) {
            int lastChance = JOptionPane.showConfirmDialog(this, "There are syntax errors, so no position data will be saved. Continue anyway?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (lastChance == JOptionPane.NO_OPTION) {
                return;
            }
        }
        dispose();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton cancelButton;
    javax.swing.JButton okButton;
    javax.swing.JTabbedPane positionsPane;
    javax.swing.JPanel provincePanel;
    javax.swing.JLabel statusLabel;
    javax.swing.JToggleButton textColorToggleButton;
    javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

    public boolean isSaveChanges() {
        return saveChanges;
    }

    public GenericObject getPositions() {
        return positions;
    }
    
    
    private static final class RotationDialog extends JDialog {
        
        private static final int SIZE = 400;
        
        private static final RenderingHints drawingHints = new RenderingHints(null);
        static {
            drawingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            drawingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            drawingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            drawingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            drawingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        }
        
        private final Point first;
        private Point second;
        
        private double rotation;
        private final String provName;
        
        private JTextField radianField;
        private JTextField degreeField;

        public RotationDialog(JDialog owner, double rotation, String provName) {
            super(owner, true);
            
            this.rotation = rotation;
            this.provName = provName;
            first = new Point(SIZE/2, SIZE/2);
            second = new Point(SIZE/2 + (int) (SIZE/2.0*Math.cos(rotation)), SIZE/2 - (int) (SIZE/2.0*Math.sin(rotation)));
            
            initComponents();
            setLocationRelativeTo(owner);
        }
        
        private void initComponents() {
            setLayout(new BorderLayout());
            
            JPanel mainPanel;
            mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    
                    ((Graphics2D)g).addRenderingHints(drawingHints);
                    
                    g.setColor(Color.BLACK);
                    
                    g.drawOval(0, 0, SIZE, SIZE);
                    
                    g.drawOval(first.x-2, first.y-2, 5, 5);
                    g.drawOval(second.x-2, second.y-2, 5, 5);
                    g.drawLine(first.x, first.y, second.x, second.y);
                    
                    
                    Rectangle2D rect = g.getFontMetrics().getStringBounds(provName, g);

                    AffineTransform at = AffineTransform.getTranslateInstance(first.x, first.y);
                    at.rotate(-rotation);
                    AffineTransform oldTx = ((Graphics2D)g).getTransform();
                    ((Graphics2D)g).transform(at);

                    g.drawString(provName, -(int) (rect.getWidth()/2), 0);

                    ((Graphics2D)g).setTransform(oldTx);
                }
                
            };
            mainPanel.setToolTipText("<html>Click to move the point around the circle.<br />"
                    + "Note that ports will actually face the center of the circle in game (i.e., the<br />"
                    + "outside point would be the land and the center point would be the water).</html>");
            add(mainPanel, BorderLayout.CENTER);
            
            mainPanel.setPreferredSize(new Dimension(SIZE, SIZE));
            
            JPanel lowerPanel = new JPanel(new GridLayout());
            
            radianField = new JTextField(sixDigitFormat.format(rotation));
            degreeField = new JTextField(sixDigitFormat.format(Math.toDegrees(rotation)));
            
            radianField.addKeyListener(new KeyAdapter() {

                @Override
                public void keyTyped(KeyEvent e) {
                    if (!"".equals(radianField.getText())) {
                        try {
                            Double.parseDouble(radianField.getText());
                        } catch (NumberFormatException ex) {
                            degreeField.setForeground(Color.RED);
                            return;
                        }
                        degreeField.setForeground(UIManager.getColor("TextField.foreground"));
                        rotation = Double.parseDouble(radianField.getText());
                        degreeField.setText(sixDigitFormat.format(Math.toDegrees(rotation)));
                        second = normalizePoint(new Point(SIZE/2 + (int) (Math.cos(rotation)*SIZE/2.0), SIZE/2 - (int) (Math.sin(rotation)*SIZE/2.0)));
                        repaint();
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    keyTyped(e);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    keyTyped(e);
                }
                
            });
            
            degreeField.addKeyListener(new KeyAdapter() {

                @Override
                public void keyTyped(KeyEvent e) {
                    if (!"".equals(degreeField.getText())) {
                        try {
                            Double.parseDouble(degreeField.getText());
                        } catch (NumberFormatException ex) {
                            degreeField.setForeground(Color.RED);
                            return;
                        }
                        degreeField.setForeground(UIManager.getColor("TextField.foreground"));
                        rotation = Math.toRadians(Double.parseDouble(degreeField.getText()));
                        radianField.setText(sixDigitFormat.format(rotation));
                        second = normalizePoint(new Point(SIZE/2 + (int) (Math.cos(rotation)*SIZE/2.0), SIZE/2 - (int) (Math.sin(rotation)*SIZE/2.0)));
                        repaint();
                    }
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    keyTyped(e);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    keyTyped(e);
                }
                
            });
            
            lowerPanel.add(new JLabel("Radians:"));
            lowerPanel.add(radianField);
            lowerPanel.add(new JLabel("Degrees:"));
            lowerPanel.add(degreeField);
            
            add(lowerPanel, BorderLayout.SOUTH);
            
//            statusLabel = new JLabel(sixDigitFormat.format(rotation) + " (" + sixDigitFormat.format(Math.toDegrees(rotation)) + "°)");
//            add(statusLabel, BorderLayout.SOUTH);
            
            MouseAdapter listener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    second = normalizePoint(e.getPoint());
                    recalculateRotation();
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    second = normalizePoint(e.getPoint());
                    recalculateRotation();
                    repaint();
                }
            };
            
            mainPanel.addMouseListener(listener);
            mainPanel.addMouseMotionListener(listener);
            
            pack();
            setTitle("Rotation");
            setResizable(false);
        }

        private void recalculateRotation() {
            rotation = Math.atan2((double) first.y - second.y, -((double) first.x - second.x));
            
            if (rotation < 0.0)
                rotation += 2.0*Math.PI;
            
            if (rotation == -0.0)
                rotation = 0.0;
            
            radianField.setText(sixDigitFormat.format(rotation));
            degreeField.setText(sixDigitFormat.format(Math.toDegrees(rotation)));
        }
        
        // Extends the line between the first point and pt to as close to SIZE/2
        // in length as possible.
        private Point normalizePoint(Point pt) {
            // Known:
            //      desired hypotenuse = SIZE/2
            //      ratio between sides of the triangle must remain the same
            
            // Pythagorean Theorem
            double hyp = Math.sqrt(Math.pow((double)(pt.x-first.x), 2.0) + Math.pow((double)(pt.y-first.y), 2.0));
            
            double ratio = (SIZE/2.0) / hyp;
            
            return new Point((int) ((pt.x-first.x)*ratio + first.x), (int) ((pt.y-first.y)*ratio + first.y));
        }
        
        public double getRotation() {
            return rotation;
        }
    }
    
}
