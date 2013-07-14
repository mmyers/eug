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

/**
 *
 * @author Michael Myers
 */
public class GoodsMode extends ProvincePaintingMode {

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
    
    protected void paintSeaZone(Graphics2D g, int id) {
        // Do nothing
        return;
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

    private final java.util.Map<String, Color> goodsColorCache =
            new HashMap<String, Color>();

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
                                System.err.println("color for " + good + " is null");
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
                }
                for (GenericObject def : allGoods.children) {
                    if (def.name.equalsIgnoreCase(good)) {
                        // found it
                        GenericList color = def.getList("color");
                        if (color == null) {
                            System.err.println("color for " + good + " is null");
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
