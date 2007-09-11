/*
 * Utilities.java
 *
 * Created on August 22, 2007, 6:04 PM
 */

package editor.mapmode;

import editor.Main;
import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * A place for static methods commonly used by mapmodes.
 * @author Michael Myers
 * @since 0.5pre3
 */
final class Utilities {
    
    /** Creates a new instance of Utilities */
    private Utilities() { }
    
    
//    static final String[] noCountryStrings = {
//        "", "---", "XXX", "none"
//    };
    
    static boolean isNotACountry(final String tag) {
        if (tag.length() == 0 || tag.equals("---") || tag.equalsIgnoreCase("XXX") || tag.equalsIgnoreCase("none"))
            return true;
        return false;
        
//        for (String match : noCountryStrings) {
//            if (tag.equalsIgnoreCase(match))
//                return true;
//        }
//        return false;
    }
    
    
    // Country, religion, and goods colors
    
    /** Color used if the province history cannot be found. */
    static final Color COLOR_NO_HIST = Color.RED;
    /** Color used if the country definition cannot be found. */
    static final Color COLOR_NO_CTRY_DEF = Color.BLACK;
    /** Color used if the religion definition cannot be found. */
    static final Color COLOR_NO_RELIGION_DEF = Color.BLACK;
    /** Color used if a land province does not have a defined religion. */
    static final Color COLOR_NO_RELIGION = Color.RED;
    /** Color used if the goods definition cannot be found. */
    static final Color COLOR_NO_GOOD_DEF = Color.BLACK;
    /** Color used if a land province does not have a defined trade good. */
    static final Color COLOR_NO_GOODS = Color.RED;
    
    private static final ParserSettings settings =
            ParserSettings.getNoCommentSettings().setPrintTimingInfo(false);
    
    private static final GenericObject countries = EUGFileIO.load(
            Main.filenameResolver.resolveFilename("common/countries.txt"),
            settings
            );
    
    private static final java.util.Map<String, Color> ctryColorCache =
            new HashMap<String, Color>();
    
    private static final GenericObject religions = EUGFileIO.load(
            Main.filenameResolver.resolveFilename("common/religion.txt"),
            settings
            );
    
    private static final java.util.Map<String, Color> relColorCache =
            new HashMap<String, Color>();
    
    private static final GenericObject goods = EUGFileIO.load(
            Main.filenameResolver.resolveFilename("common/tradegoods.txt"),
            settings
            );
    
    private static final java.util.Map<String, Color> goodsColorCache =
            new HashMap<String, Color>();
    
    static Color getCtryColor(String country) {
        // Special case: XXX means no one
        if (isNotACountry(country))
            country = "NAT";
        else
            country = toUpperCase(country);
        
        
        Color ret = ctryColorCache.get(country);
        if (ret == null) {
            
            String filename = countries.getString(country);
            if (!filename.startsWith("common"))
                filename = "common/" + filename;
            filename = Main.filenameResolver.resolveFilename(filename);
            
            final GenericObject countryDef = EUGFileIO.load(filename, settings);
            
            if (countryDef == null) {
                System.err.println("No country definition file found for " + country + ".");
                System.err.println("Expected to find it in " + filename);
                return COLOR_NO_CTRY_DEF;
            }
            
            final GenericList color = countryDef.getList("color");
            
            if (color == null) {
                System.err.println("color for " + country + " is null");
                return COLOR_NO_CTRY_DEF;
            }
            
            final int red = Math.min(Integer.parseInt(color.get(0)), 255);
            final int green = Math.min(Integer.parseInt(color.get(1)), 255);
            final int blue = Math.min(Integer.parseInt(color.get(2)), 255);
            
            ret = new Color(red, green, blue);
            
            ctryColorCache.put(country, ret);
        }
        return ret;
    }
    
    static Color getReligionColor(String religion) {
        religion = religion.toLowerCase();
        
        Color ret = relColorCache.get(religion);
        
        if (ret == null) {
            for (GenericObject group : religions.children) {
                for (GenericObject rel : group.children) {
                    if (rel.name.equals(religion)) {
                        // found it
                        GenericList color = rel.getList("color");
                        if (color == null) {
                            System.err.println("color for " + religion + " is null");
                            return COLOR_NO_RELIGION_DEF;
                        }
                        ret = new Color(
                                Float.parseFloat(color.get(0)),
                                Float.parseFloat(color.get(1)),
                                Float.parseFloat(color.get(2))
                                );
                        relColorCache.put(religion, ret);
                        return ret;
                    }
                }
            }
            
            return COLOR_NO_RELIGION_DEF;
        }
        
        return ret;
    }
    
    static Color getGoodsColor(String good) {
        good = good.toLowerCase();
        
        Color ret = goodsColorCache.get(good);
        
        if (ret == null) {
            for (GenericObject def : goods.children) {
                if (def.name.equals(good)) {
                    // found it
                    GenericList color = def.getList("color");
                    if (color == null) {
                        System.err.println("color for " + good + " is null");
                        return COLOR_NO_GOOD_DEF;
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
            return COLOR_NO_GOOD_DEF;
        }
        return ret;
    }
    
    
    // Our own little toUpperCase method, because every tag is converted to
    // upper case before processing. This method, unlike String.toUpperCase(),
    // assumes that only ASCII characters are used, so a faster algorithm can be
    // used.
    
    private static final int diff = 'A' - 'a';
    
    private static String toUpperCase(final String st) {
        final char[] cArr = st.toCharArray();
        boolean change = false;
        for (int i = 0; i < cArr.length; i++) {
            int c = (int) cArr[i];
            if (c >= 'a' && c <= 'z') {
                cArr[i] = (char) (c+diff);
                change = true;
            }
        }
        // only create a new string when necessary
        return change ? String.valueOf(cArr) : st;
    }
    
    
    // Texture handling (striped paints)
    
    private static final java.util.Map<ColorPair, Paint> imgCache =
            new HashMap<ColorPair, Paint>();
    
    private static final Rectangle imageRect = new Rectangle(0,0,8,1);
    
    static Paint createPaint(final Color c1, final Color c2) {
        final ColorPair cp = new ColorPair(c1, c2);
        Paint ret = imgCache.get(cp);
        if (ret == null) {
            final BufferedImage img = new BufferedImage(8, 1, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = img.createGraphics();
            g.setColor(c1);
            g.drawLine(0,0,3,0);
            g.setColor(c2);
            g.drawLine(4,0,7,0);
            g.dispose();
            
            ret = new TexturePaint(img, imageRect);
            imgCache.put(cp, ret);
        }
        
        return ret;
    }
    
    private static final class ColorPair {
        private final Color c1;
        private final Color c2;
        public ColorPair(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
        }
        public boolean equals(Object other) {
            if (!(other instanceof ColorPair))
                return false;
            final ColorPair cp = (ColorPair) other;
            return c1.equals(cp.c1) && c2.equals(cp.c2);
        }
        public int hashCode() {
            return c1.hashCode() * c2.hashCode();
        }
    }
}
