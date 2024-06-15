package editor.mapmode;

import editor.MapPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 * A <code>MapMode</code> that paints a blue background and then delegates
 * any painting of provinces to the subclass. Painting provinces is made
 * easier by {@link #paintProvince}.
 * <p>
 * Subclasses must override {@link #paintProvince(Graphics2D, int)} and
 * {@link #paintSeaZone(Graphics2D, int)}. They may also override
 * {@link #paintBackground(Graphics2D)} or
 * {@link #paintTerraIncognita(Graphics2D)} if they wish; the default is to
 * paint the background blue and terra incognita black.
 * @author Michael Myers
 * @since 0.4pre1
 */
public abstract class ProvincePaintingMode extends MapMode {
    
    private static final Paint background = new Color(15, 100, 255);
    
    protected ProvincePaintingMode() {
    }
    
    protected ProvincePaintingMode(MapPanel panel) {
        super(panel);
    }
    
    @Override
    public void paint(final Graphics2D g) {
        paintBackground(g);
        
        paintingStarted(g);
        
        // First, paint PTI
        paintTerraIncognita(g);
        
        // Now, send the rest to the subclass.
        int maxProvinces = getMap().getMaxProvinces();
        for (int i = 1; i < maxProvinces; i++) {
            if (mapPanel.shouldPaintProvince(i)) {
                if (getMap().isLand(i))
                    paintProvince(g, i);
                else
                    paintSeaZone(g, i);
            }
        }
        
        paintingEnded(g);
    }
    
    /**
     * Called after the background is painted, but before any provinces have
     * been painted. Subclasses may override this to do some processing (e.g.
     * caching) before having to paint.
     * @param g the <code>Graphics2D</code> object to paint with.
     */
    protected void paintingStarted(final Graphics2D g) {
        // do nothing, but allow subclasses to override
    }
    
    protected void paintTerraIncognita(final Graphics2D g) {
        mapPanel.paintProvince(g, 0, Color.BLACK);
    }
    
    protected void paintBackground(final Graphics2D g) {
        g.setPaint(background);
        g.fillRect(0, 0, mapPanel.getWidth(), mapPanel.getHeight());
    }
    
    protected abstract void paintProvince(final Graphics2D g, int provId);
    
    protected abstract void paintSeaZone(final Graphics2D g, int id);
    
    /**
     * Called after all provinces have been painted. Subclasses may override
     * this, but will typically only do so if also overriding
     * {@link #paintingStarted}.
     * @param g the <code>Graphics2D</code> object to paint with.
     */
    protected void paintingEnded(final Graphics2D g) {
        // do nothing, but allow subclasses to override
    }

    @Override
    public boolean paintsBorders() {
        return true;
    }

}