/*
 * GoodsMode.java
 *
 * Created on June 12, 2007, 3:27 PM
 */

package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import editor.Text;
import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.logging.Level;

/**
 *
 * @author Michael Myers
 */
public class GoodsMode extends ProvincePaintingMode {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(GoodsMode.class.getName());

    private boolean isVictoriaStyle;
    private FilenameResolver resolver;
    
    /** Creates a new instance of GoodsMode */
    public GoodsMode() {
    }
    
    public GoodsMode(MapPanel panel, boolean isVictoriaStyle, FilenameResolver resolver) {
        super(panel);
        this.isVictoriaStyle = isVictoriaStyle;
        this.resolver = resolver;
    }
    
    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        final String goods = mapPanel.getModel().getHistString(provId, "trade_goods");
        if (goods == null) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_HIST);
        } else if (goods.length() == 0 || goods.equalsIgnoreCase("none")) {
            mapPanel.paintProvince(g, provId, Utilities.COLOR_NO_GOODS);
        } else {
            mapPanel.paintProvince(g, provId, getGoodsColor(goods));
        }
    }
    
    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        // Do nothing
    }
    
    
    @Override
    public String getTooltipExtraText(Province current) {
        if (!getMap().isLand(current.getId()))
            return "";
        
        final String ret = Text.getText(mapPanel.getModel().getHistString(current.getId(), "trade_goods"));
        if (ret == null || ret.length() == 0)
            return "";
        return "Goods: " + ret;
    }



    private GenericObject allGoods;

    private final java.util.Map<String, Color> goodsColorCache = new HashMap<>();

    Color getGoodsColor(String good) {
        good = good.toLowerCase();

        Color ret = goodsColorCache.get(good);

        if (ret == null) {
            if (isVictoriaStyle) {
                // Victoria 2: goods are divided by type and colors are 0-255
                if (allGoods == null) {
                    allGoods = EUGFileIO.load(
                        resolver.resolveFilename("common/goods.txt"),
                        ParserSettings.getQuietSettings()
                        );
                }
                for (GenericObject type : allGoods.children) {
                    for (GenericObject def : type.children) {
                        if (def.name.equalsIgnoreCase(good)) {
                            // found it
                            GenericList color = def.getList("color");
                            if (color == null) {
                                log.log(Level.WARNING, "color for {0} is null", good);
                                return Utilities.COLOR_NO_GOOD_DEF;
                            }
                            ret = new Color(
                                    Integer.parseInt(color.get(0)),
                                    Integer.parseInt(color.get(1)),
                                    Integer.parseInt(color.get(2))
                                    );
                            goodsColorCache.put(good, ret);
                            return ret;
                        }
                    }
                }
            } else {
                // EU3 and Rome: goods are top-level objects and colors are 0-1.0
                if (allGoods == null) {
                    allGoods = EUGFileIO.load(
                        resolver.resolveFilename("common/tradegoods.txt"),
                        ParserSettings.getQuietSettings()
                        );
                    
                    if (allGoods == null)
                        allGoods = EUGFileIO.loadAll(resolver.listFiles("common/tradegoods"), ParserSettings.getQuietSettings());
                }
                for (GenericObject def : allGoods.children) {
                    if (def.name.equalsIgnoreCase(good)) {
                        // found it
                        GenericList color = def.getList("color");
                        if (color == null) {
                            log.log(Level.WARNING, "color for {0} is null", good);
                            return Utilities.COLOR_NO_GOOD_DEF;
                        }
                        ret = new Color(
                                Float.parseFloat(color.get(0)),
                                Float.parseFloat(color.get(1)),
                                Float.parseFloat(color.get(2))
                                );
                        goodsColorCache.put(good, ret);
                        return ret;
                    }
                }
            }
            return Utilities.COLOR_NO_GOOD_DEF;
        }
        return ret;
    }
}
