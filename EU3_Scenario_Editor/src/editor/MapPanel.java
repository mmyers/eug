/*
 * MapPanel.java
 *
 * Created on January 25, 2007, 6:39 PM
 */

package editor;

import editor.mapmode.MapMode;
import editor.mapmode.ProvinceMode;
import eug.shared.FilenameResolver;
import eug.specific.clausewitz.ClausewitzDataSource;
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
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.Timer;

/**
 *
 * @author  Michael Myers
 */
public class MapPanel extends javax.swing.JPanel implements Scrollable {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(MapPanel.class.getName());
    
    private transient BufferedImage mapImage;
    private transient BufferedImage scaledMapImage;
    private transient BufferedImage backBufferImage;
    
    private double scaleFactor = DEFAULT_SCALE_FACTOR;
    public static final double DEFAULT_SCALE_FACTOR = 1.0; //0.8; //0.7;
    
    private static final double MIN_SCALE = 0.05;
    private static final double MAX_SCALE = 5.0;
    private static final double DEFAULT_ZOOM_AMOUNT = 0.2; //0.1;
    
    /** @since 0.4pre1 */
    private MapPanelDataModel model;
    
    /** @since 0.4pre1 */
    private transient MapMode mode;
    
    private boolean isDirty = true;
    
    /**
     * Mapping which allows this class to override the color the
     * {@link #mode MapMode} paints a province.
     * @since 0.4pre4
     */
    private transient java.util.Map<Integer, Color> overrides;

    /** @since 0.5pre3 */
    private boolean paintBorders;

    /** @since 0.7.5 */
    private boolean isMapInverted;

    /** @since 0.7.5 */
    private Map map;
    
    /**
     * Creates new form MapPanel. {@link #initialize(Map, FilenameResolver, ProvinceData, boolean)} must be called before the
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
    public void initialize(Map map, FilenameResolver resolver, ProvinceData provData, boolean isInverted) {
        this.map = map;
        isMapInverted = isInverted;
        createMapImage(map, resolver);
        
        log.log(Level.INFO, "Reading map from image...");
        long startTime = System.currentTimeMillis();
        final MapPixelData data = new MapPixelData(scaledMapImage, map.getMaxProvinces());
        log.log(Level.INFO, "Done in {0} ms.", System.currentTimeMillis() - startTime);
        model = new MapPanelDataModel(data, map, provData);
        model.setDate("1453.1.1");
        
        initComponents();
        
        overrides = new java.util.HashMap<>();
        
        mode = new ProvinceMode(this);
        
        paintBorders = true;
    }
    
    private void createMapImage(Map map, FilenameResolver resolver) {
        try {
            String provFileName = map.getString("provinces").replace('\\', '/');
            if (!provFileName.contains("/"))
                provFileName = map.getMapPath() + "/" + provFileName;
            
            mapImage = ImageIO.read(new File(resolver.resolveFilename(provFileName)));
            rescaleMap();
            
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error reading map: " + ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            log.log(Level.SEVERE, "Error reading map", ex);
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
            if (isDirty || backBufferImage == null)
                paintBuffer();
            
            g.drawImage(backBufferImage, 0, 0, null);
            
            for (java.util.Map.Entry<Integer, Color> override : overrides.entrySet()) {
                paintProvince((Graphics2D) g, override.getKey(), override.getValue());
            }
        }
    }
    
    private void paintBuffer() {
        if (backBufferImage != null)
            backBufferImage.flush();
        
        backBufferImage = new BufferedImage(scaledMapImage.getWidth(), scaledMapImage.getHeight(), scaledMapImage.getType());
        
        Graphics g = backBufferImage.getGraphics();
        
        try {
            mode.paint(g);

            if (paintBorders && mode.paintsBorders()) {
                paintBorders((Graphics2D) g);
            }
        } catch (RuntimeException ex) {
            // can't pop up a message box from inside paintComponent, but the problem will probably be obvious anyway
            log.log(Level.SEVERE, "Error while coloring the map! Current map mode is {0}", mode.getClass().getName());
            log.log(Level.SEVERE, "The exception was: ", ex);
        }
        
        isDirty = false;
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
    
    private void paintLines(final Graphics2D g, final List<Integer[]> lines, final Color c) {
        paintLines(g, lines, (Paint) c);
    }
    
    /**
     * This is the actual method used to paint a province.
     */
    private void paintLines(final Graphics2D g, final List<Integer[]> lines, final Paint paint) {
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
    
    private void paintBorders(final Graphics2D g) {
        g.setColor(Color.BLACK);
        
        final double scale = scaleFactor/DEFAULT_SCALE_FACTOR;
        
        double x, y;
        
        int pixelSize = 1;
        if (scale > 1)
            pixelSize = 2;
        if (scale > 2)
            pixelSize = 3;
        if (scale > 3)
            pixelSize = 4;
        
        for (Integer[] pt : model.getMapData().getBorderPixels()) {
            x = ((double) pt[0]) * scale;
            y = ((double) pt[1]) * scale;
            
            g.fillRect((int)x, (int)y, pixelSize, pixelSize);
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        if (scaledMapImage == null) {
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
            scaleFactor += amount;
            scaleFactor = Double.parseDouble(rounder.format(scaleFactor));
            
            rescaleMap();
        }
    }
    
    /** @since 0.3pre1 */
    public void zoomOut() {
        zoomOut(DEFAULT_ZOOM_AMOUNT);
    }
    
    /** @since 0.3pre1 */
    public void zoomOut(double amount) {
        if (scaleFactor >= amount + MIN_SCALE) {
            scaleFactor -= amount;
            scaleFactor = Double.parseDouble(rounder.format(scaleFactor));
            
            rescaleMap();
        }
    }
    
    private static final RenderingHints scalingHints = new RenderingHints(null);
    static {
        scalingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        scalingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        scalingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }

    private boolean isMapRightSideUp() {
        return !isMapInverted;
    }
    
    private void rescaleMap() {
        if (scaledMapImage != null) {
            scaledMapImage.flush();
            scaledMapImage = null;
        }
        
        scaledMapImage = new BufferedImage(
                (int) Math.ceil(mapImage.getWidth() * scaleFactor),
                (int) Math.ceil(mapImage.getHeight() * scaleFactor),
                mapImage.getType()
                );

        final BufferedImageOp transform;
        if (isMapRightSideUp()) {
            transform =
                new AffineTransformOp(
                new AffineTransform(scaleFactor, 0.0, 0.0, scaleFactor, 0.0, -scaledMapImage.getHeight()),
                scalingHints
                );
        } else {
            transform =
                new AffineTransformOp(
                new AffineTransform(scaleFactor, 0.0, 0.0, -scaleFactor, 0.0, 0.0),
                scalingHints
                );
        }
        
        scaledMapImage.createGraphics().drawImage(mapImage, transform, 0, scaledMapImage.getHeight());
        
        isDirty = true;
    }
    
    public ProvinceData.Province getProvinceAt(final Point pt) {
        if (pt == null)
            return null;
        
        final ProvinceData.Province p = model.getProvinceData().getProv(scaledMapImage.getRGB(pt.x, pt.y));
        
        if (p == null) {
            java.awt.Color c = new java.awt.Color(scaledMapImage.getRGB(pt.x, pt.y));
            log.log(Level.WARNING, "No province registered for {0},{1},{2}", new Object[]{c.getRed(), c.getGreen(), c.getBlue()});
        }
        
        return p;
    }
    
    
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 5;
    }
    
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 50;
    }
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
    
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    private java.util.Map<Integer, Rectangle> boundsCache = new java.util.HashMap<>();
    private Rectangle getProvinceBoundingRect(int provId) {
        Rectangle r = boundsCache.get(provId);
            
        if (r == null) {
            ProvinceData.Province p = model.getProvinceData().getProvByID(provId);
            if (p == null)
                return null;
            r = calculateBounds(model.getMapData().getLinesInProv(p.getColor()));
            boundsCache.put(provId, r);
        }
        
        return r;
    }
    private Point getCenterPoint(int provId) {
        Rectangle r = getProvinceBoundingRect(provId);
        if (r == null) // province does not appear on the map
            return null;

        // We want the point x + width/2, y + height/2 to be in the
        // center of the viewport.
        int x = r.x + r.width/2;
        int y = r.y + r.height/2;
        // Since the bounds were calculated for the default scale factor, we
        // need to adjust them.
        x = (int) (((double) x) * (scaleFactor / DEFAULT_SCALE_FACTOR));
        y = (int) (((double) y) * (scaleFactor / DEFAULT_SCALE_FACTOR));

        return new Point(x, y);
    }
    
    /** @since 0.3pre1 */
    public void goToProv(int provId) {
        Point p = getCenterPoint(provId);
        if (p == null)
            return;
        centerMap(p.x, p.y);
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
        if (lines == null || lines.isEmpty())
            return null;

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
        isDirty = true;
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
    
    @Override
    public Point getToolTipLocation(java.awt.event.MouseEvent evt) {
        Point p = evt.getPoint();
        p.translate(0, 20); // move 20 px down to avoid the cursor itself
        return p;
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
        flashProvinces(java.util.Arrays.asList(provId), numFlashes, Color.WHITE);
    }
    
    public void flashProvinces(final List<Integer> provIds, final int numFlashes, final Color provColor) {
        final ActionListener listener = new ActionListener() {
            private boolean color = false;
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (color) {
                    for (Integer provId : provIds)
                        colorProvince(provId, provColor);
                } else {
                    for (Integer provId : provIds)
                        uncolorProvince(provId);
                }
                color = !color;
                repaint();
            }
        };
        
        for (Integer provId : provIds)
            colorProvince(provId, provColor);
        repaint();
        
        for (int i = 1; i <= numFlashes*2 - 1; i++) {
            Timer t = new Timer(250*i, listener);
            t.setRepeats(false);
            t.start();
        }
    }
    
    /** @since 0.6pre1 */
    public ClausewitzDataSource getDataSource() {
        return model.getDataSource();
    }
    
    /** @since 0.5pre1 */
    public void setDataSource(ClausewitzDataSource dataSource) {
        model.setDataSource(dataSource);
    }

    /** @since 0.5pre3 */
    public boolean isPaintBorders() {
        return paintBorders;
    }

    /** @since 0.5pre3 */
    public void setPaintBorders(boolean paintBorders) {
        this.paintBorders = paintBorders;
        isDirty = true;
    }
    
    public void refresh() {
        isDirty = true;
        repaint();
    }

    /** @since 0.7.5 */
    public Map getMap() {
        return map;
    }

    /** @since 0.8.2 */
    public void drawArrow(int from, int to, Graphics2D g) {
        Point pFrom = getCenterPoint(from);
        Point pTo = getCenterPoint(to);
        if (pFrom == null || pTo == null)
            return;
        
        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        double x1 = pFrom.x, x2 = pTo.x;
        double y1 = pFrom.y, y2 = pTo.y;
        
        if (Math.abs(x1 - x2) < scaledMapImage.getWidth()/2) {
            // easy case, line doesn't cross the edge of the map
            g.drawLine(pFrom.x, pFrom.y, pTo.x, pTo.y);
        } else if (x1 < x2) {
            // line goes off left side and should wrap to the right side
            g.drawLine(pFrom.x, pFrom.y, pTo.x - scaledMapImage.getWidth(), pTo.y);
            g.drawLine(pFrom.x + scaledMapImage.getWidth(), pFrom.y, pTo.x, pTo.y);
            
            x1 = pFrom.x + scaledMapImage.getWidth();
        } else {
            // line goes off right side and should wrap to the left side
            g.drawLine(pFrom.x, pFrom.y, pTo.x + scaledMapImage.getWidth(), pTo.y);
            g.drawLine(pFrom.x - scaledMapImage.getWidth(), pFrom.y, pTo.x, pTo.y);
            
            x1 = pFrom.x - scaledMapImage.getWidth();
        }
        
        // arrow head adapted from https://stackoverflow.com/a/4112875
        // slight modification because arrows would be drawn backwards when a line crosses the edge of the map
        final int ARROW_SIZE = (int) Math.max(4, 4 * scaleFactor);
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform old = g.getTransform();
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.fillPolygon(new int[] {len, len-ARROW_SIZE, len-ARROW_SIZE, len},
                      new int[] {0, -ARROW_SIZE, ARROW_SIZE, 0}, 4);
        g.setTransform(old);
    }

    // blah
//    private void readObject(java.io.ObjectInputStream in)
//    throws IOException, ClassNotFoundException {
//        in.defaultReadObject();
//        mode = new ProvinceMode(this);
//        overrides = new java.util.HashMap<Integer, Color>();
//        createMapImage();
//    }
    
}
