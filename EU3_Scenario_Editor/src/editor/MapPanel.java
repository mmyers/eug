/*
 * MapPanel.java
 *
 * Created on January 25, 2007, 6:39 PM
 */

package editor;

import editor.mapmode.MapMode;
import editor.mapmode.ProvinceMode;
import eug.specific.eu3.EU3DataSource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.Timer;

/**
 *
 * @author  Michael Myers
 */
public class MapPanel extends javax.swing.JPanel implements Scrollable {
    
    
    private transient BufferedImage mapImage;
    private transient BufferedImage scaledMapImage;
    
    private double scaleFactor = DEFAULT_SCALE_FACTOR;
    public static final double DEFAULT_SCALE_FACTOR = 1.0; //0.8; //0.7;
    
    private static final double MIN_SCALE = 0.05;
    private static final double MAX_SCALE = 5.0;
    private static final double DEFAULT_ZOOM_AMOUNT = 0.2; //0.1;
    
    /** @since 0.4pre1 */
    private MapPanelDataModel model;
    
    /** @since 0.4pre1 */
    private transient MapMode mode;
    
    /**
     * Mapping which allows this class to override the color the
     * {@link #mode MapMode} paints a province.
     * @since 0.4pre4
     */
    private transient java.util.Map<Integer, Color> overrides;

    /** @since 0.5pre3 */
    private boolean paintBorders;
    
    /**
     * Creates new form MapPanel. {@link #initialize()} must be called before the
     * panel is shown; this way, GUI editors can use this component without
     * having to load the data.
     */
    public MapPanel() {
//        initialize();
    }
    
    /**
     * Loads the necessary data for showing the map. This method must be called
     * before the panel is shown on screen; otherwise, it's just a blank JPanel.
     */
    public void initialize() {
        createMapImage();
        
        System.out.println("Reading map...");
        final MapData data = new MapData(scaledMapImage, Integer.parseInt(Main.map.getString("max_provinces")));
        System.out.println("Done.");
        model = new MapPanelDataModel(data);
        model.setDate("1453.1.1");
        
        initComponents();
        
        overrides = new java.util.HashMap<Integer, Color>();
        
        mode = new ProvinceMode(this);
        
        paintBorders = true;
    }
    
    private void createMapImage() {
        try {
            String provFileName = Main.map.getString("provinces").replace('\\', '/');
            if (!provFileName.contains("/"))
                provFileName = "map/" + provFileName;
            
            mapImage = ImageIO.read(new File(Main.filenameResolver.resolveFilename(provFileName)));
            rescaleMap();
            
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error reading map: " + ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

    }// </editor-fold>//GEN-END:initComponents
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Variables ">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
    
    @Override
    protected void paintComponent(final Graphics g) {
        if (scaledMapImage == null) {
            super.paintComponent(g);
        } else {
            mode.paint(g);

            for (java.util.Map.Entry<Integer, Color> override : overrides.entrySet()) {
                paintProvince((Graphics2D) g, override.getKey(), override.getValue());
            }

            if (paintBorders && mode.paintsBorders()) {
                paintBorders((Graphics2D) g);
            }
        }
    }
    
    /**
     * Paints the province map image.
     */
    public final void paintProvinces(final Graphics2D g2D) {
        g2D.drawImage(scaledMapImage, 0, 0, null);
    }
    
    /**
     * Paint a province the given color.
     */
    public final void paintProvince(final Graphics2D g, int provId, final Color c) {
        paintLines(g, model.getLinesInProv(provId), c);
    }
    
    /**
     * Paint a province the given paint.
     */
    public final void paintProvince(final Graphics2D g, int provId, final Paint p) {
        paintLines(g, model.getLinesInProv(provId), p);
    }
    
    private final void paintLines(final Graphics2D g, final List<Integer[]> lines, final Color c) {
        paintLines(g, lines, (Paint) c);
    }
    
    /**
     * This is the actual method used to paint a province.
     */
    private final void paintLines(final Graphics2D g, final List<Integer[]> lines, final Paint paint) {
        // Quick sanity check
        if (lines == null)
            return;
        
        if (g.getPaint() != paint) {
            g.setPaint(paint);
        }
        
        final double scale = scaleFactor/DEFAULT_SCALE_FACTOR;
        final int maxY = getHeight();
        
        int x1,  x2,  y1,  y2;
        double y;
        
        for(Integer[] line : lines) {
            x1 = (int) Math.round(((double) line[1]) * scale);
            x2 = (int) Math.round(((double) line[2]) * scale);
            
            // If there is an inaccuracy, y is usually the culprit.
            // This has been hacked to keep from overrunning the province bounds.
            y = ((double) (line[0]+1)) * scale;
            y1 = Math.max((int) Math.round(y - scale), 0);
            y = ((double) (line[0])) * scale;
            y2 = Math.min((int) Math.round(y + scale), maxY);
            
            g.fillRect(x1, y1, x2-x1, y2-y1);
        }
    }
    
    private final void paintBorders(final Graphics2D g) {
        g.setColor(Color.BLACK);
        
        final double scale = scaleFactor/DEFAULT_SCALE_FACTOR;
        final double halfScale = scale/2;
        final int maxX = getWidth();
        final int maxY = getHeight();
        
        int x1, x2, y1, y2;
        double x, y;
        
        for (Integer[] pt : model.getMapData().getBorderPixels()) {
            x = ((double) pt[0]) * scale;
            x1 = Math.max((int) Math.round(x - halfScale), 0);
            x2 = Math.min((int) Math.round(x + halfScale), maxX);
            y = ((double) pt[1]) * scale;
            y1 = Math.max((int) Math.round(y - halfScale), 0);
            y2 = Math.min((int) Math.round(y + halfScale), maxY);
            g.fillRect(x1, y1, x2-x1, y2-y1);
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        if (scaledMapImage == null) {
//            System.err.println("scaledMap == null!");
            return super.getPreferredSize();
        }
        return new Dimension(scaledMapImage.getWidth(), scaledMapImage.getHeight());
    }
    
    // Locale.US is necessary because, in some locales, the comma ',' is used as
    // the decimal separator, which Double.parseDouble doesn't recognize.
    private static final java.text.NumberFormat rounder = java.text.NumberFormat.getNumberInstance(Locale.US);
    static {
        if (rounder instanceof java.text.DecimalFormat) {
            ((java.text.DecimalFormat)rounder).setDecimalSeparatorAlwaysShown(true);
            ((java.text.DecimalFormat)rounder).setMaximumFractionDigits(2);
        }
    }
    
    /**
     * @deprecated As of 0.3, use {@link zoomIn()}, {@link zoomIn(double)},
     * {@link zoomOut}, or {@link zoomOut(double)} instead.
     */
    @Deprecated
    public void setScaleFactor(double factor) {
        if (factor < MIN_SCALE)
            return;
//        System.out.println("New scale factor: " + factor);
        scaleFactor = factor;
        rescaleMap();
//        repaint();
    }
    
    public double getScaleFactor() {
        return scaleFactor;
    }
    
    /** @since 0.3pre1 */
    public void zoomIn() {
        zoomIn(DEFAULT_ZOOM_AMOUNT);
    }
    
    /** @since 0.3pre1 */
    public void zoomIn(double amount) {
        if (scaleFactor <= MAX_SCALE - amount) {
//            final Point oldPosition = ((JViewport) getParent()).getViewPosition();
//            final int oldx = (int) (oldPosition.getX() / scaleFactor);
//            final int oldy = (int) (oldPosition.getY() / scaleFactor);
            
            scaleFactor += amount;
            scaleFactor = Double.parseDouble(rounder.format(scaleFactor));
            
            rescaleMap();
//            ((JViewport) getParent()).setViewPosition(new Point((int) (oldx * scaleFactor), (int) (oldy * scaleFactor)));
        }
    }
    
    /** @since 0.3pre1 */
    public void zoomOut() {
        zoomOut(DEFAULT_ZOOM_AMOUNT);
    }
    
    /** @since 0.3pre1 */
    public void zoomOut(double amount) {
        if (scaleFactor >= amount + MIN_SCALE) {
//            final Point oldPosition = ((JViewport) getParent()).getViewPosition();
//            final int oldx = (int) (oldPosition.getX() / scaleFactor);
//            final int oldy = (int) (oldPosition.getY() / scaleFactor);
            
            scaleFactor -= amount;
            scaleFactor = Double.parseDouble(rounder.format(scaleFactor));
            
            rescaleMap();
//            ((JViewport) getParent()).setViewPosition(new Point((int) (oldx * scaleFactor), (int) (oldy * scaleFactor)));
        }
    }
    
    private static final RenderingHints scalingHints = new RenderingHints(null);
    static {
        scalingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        scalingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        scalingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }
    
    private void rescaleMap() {
        final BufferedImageOp flipAndScale =
                new AffineTransformOp(
                new AffineTransform(scaleFactor, 0.0, 0.0, -scaleFactor, 0.0, 0.0),
                scalingHints
                );
        
        if (scaledMapImage != null) {
            scaledMapImage.flush();
            scaledMapImage = null;
        }
        
        scaledMapImage = new BufferedImage(
                (int) Math.ceil(mapImage.getWidth() * scaleFactor),
                (int) Math.ceil(mapImage.getHeight() * scaleFactor),
                mapImage.getType()
                );
        
        scaledMapImage.createGraphics().drawImage(mapImage, flipAndScale, 0, scaledMapImage.getHeight());
    }
    
    public ProvinceData.Province getProvinceAt(final Point pt) {
        if (pt == null)
            return null;
        
        final ProvinceData.Province p = model.getProvinceData().getProv(scaledMapImage.getRGB(pt.x, pt.y));
        
        if (p == null) {
            java.awt.Color c = new java.awt.Color(scaledMapImage.getRGB(pt.x, pt.y));
            System.err.println("No province registered for " + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
        }
        
        return p;
    }
    
    
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 5;
    }
    
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 50;
    }
    
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
    
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    
    /** @since 0.3pre1 */
    public void goToProv(int provId) {
        java.awt.Rectangle r =
                calculateBounds(model.getMapData().getLinesInProv(model.getProvinceData().getProvByID(provId).getColor()));
        // We want the point x + width/2, y + height/2 to be in the
        // center of the viewport.
        int x = r.x + r.width/2;
        int y = r.y + r.height/2;
        // Since the bounds were calculated for the default scale factor, we
        // need to adjust them.
        x = (int) (((double) x) * (scaleFactor / DEFAULT_SCALE_FACTOR));
        y = (int) (((double) y) * (scaleFactor / DEFAULT_SCALE_FACTOR));
        
        centerMap(x, y);
    }
    
    /** @since 0.4pre1 */
    public void centerMap() {
        centerMap(scaledMapImage.getWidth()/2, scaledMapImage.getHeight()/2);
    }
    
    private void centerMap(int x, int y) {
        java.awt.Dimension size = getParent().getSize();
        x = Math.max(0, x - (size.width/2));
        y = Math.max(0, y - (size.height/2));
        ((JViewport)getParent()).setViewPosition(new Point(x, y));
    }
    
    private static java.awt.Rectangle calculateBounds(final List<Integer[]> lines) {
        int xLeft = Integer.MAX_VALUE;
        int xRight = Integer.MIN_VALUE;
        int yTop = Integer.MAX_VALUE;
        int yBot = Integer.MIN_VALUE;
        
        for (Integer[] line : lines) {
            if (line[0] < yTop)
                yTop = line[0];
            
            if (line[0] > yBot)
                yBot = line[0];
            
            if (line[1] < xLeft)
                xLeft = line[1];
            
            if (line[2] > xRight)
                xRight = line[2];
        }
        
        return new java.awt.Rectangle(xLeft, yTop, xRight-xLeft, yBot-yTop);
    }
    
    /** @since 0.3pre1 */
    public Color getColor(Point point) {
        if (point == null)
            return null;
        
        return new Color(scaledMapImage.getRGB(point.x, point.y));
    }
    
    
    /** @since 0.3pre1 */
    public MapMode getMode() {
        return mode;
    }
    
    /** @since 0.3pre1 */
    public void setMode(MapMode mode) {
        this.mode = mode;
    }
    
    /** @since 0.4pre1 */
    public MapPanelDataModel getModel() {
        return model;
    }
    
    public void setModel(MapPanelDataModel model) {
        this.model = model;
    }
    
    
    /** @since 0.3pre1 */
    public void setToolTipTextForProv(final ProvinceData.Province p) {
        final StringBuilder tooltip = new StringBuilder("<html><b>");
        tooltip.append(p.getName()).append("</b><br>ID: ").append(p.getId());
        
        final String extra = mode.getTooltipExtraText(p);
        if (extra.length() != 0)
            tooltip.append("<br>").append(extra);
        
        setToolTipText(tooltip.append("</html>").toString());
    }
    
    /** @since 0.4pre4 */
    public void colorProvince(final int provId, final Color color) {
        overrides.put(provId, color);
    }
    
    /** @since 0.4pre4 */
    public void uncolorProvince(final int provId) {
        overrides.remove(provId);
    }
    /** @since 0.4pre4 */
    public void flashProvince(final int provId) {
        flashProvince(provId, 3);
    }
    
    /** @since 0.4pre4 */
    public void flashProvince(final int provId, final int numFlashes) {
        final ActionListener listener = new ActionListener() {
            private boolean color = true;
            public void actionPerformed(final ActionEvent e) {
                if (color) {
                    colorProvince(provId, Color.WHITE);
                } else {
                    uncolorProvince(provId);
                }
                color = !color;
                repaint();
            }
        };
        
        for (int i = 1; i <= numFlashes*2; i++) {
            Timer t = new Timer(333*i, listener);
            t.setRepeats(false);
            t.start();
        }
    }
    
    /** @since 0.6pre1 */
    public EU3DataSource getDataSource() {
        return model.getDataSource();
    }
    
    /** @since 0.5pre1 */
    public void setDataSource(EU3DataSource dataSource) {
        model.setDataSource(dataSource);
        model.preloadProvs();
    }

    /** @since 0.5pre3 */
    public boolean isPaintBorders() {
        return paintBorders;
    }

    /** @since 0.5pre3 */
    public void setPaintBorders(boolean paintBorders) {
        this.paintBorders = paintBorders;
    }
    
    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        mode = new ProvinceMode(this);
        overrides = new java.util.HashMap<Integer, Color>();
        createMapImage();
    }
    
//    private static class BoundBox {
//        private List<Integer[]> lines;
//        private Integer[] bounds;
//
//        public BoundBox(final List<Integer[]> lines) {
//            this.lines = lines;
//            bounds = new Integer[] {Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE};
//            updateBounds();
//        }
//
//        private void updateBounds() {
//            int x1 = bounds[0];
//            int x2 = bounds[1];
//            int y1 = bounds[2];
//            int y2 = bounds[3];
//            for (Integer[] line : lines) {
//                // X
//                x1 = Math.min(line[1], x1);
//                x2 = Math.max(line[2], x2);
//                // Y
//                int y = line[0];
//                y1 = Math.min(y, y1);
//                y2 = Math.max(y, y2);
//            }
//            bounds[0] = x1;
//            bounds[1] = x2;
//            bounds[2] = y1;
//            bounds[3] = y2;
//        }
//    }
    
}
