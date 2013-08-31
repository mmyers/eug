/*
 * Utilities.java
 *
 * Created on August 22, 2007, 6:04 PM
 */

package editor.mapmode;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
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
 * Must first be initialized by calling {@link init}.
 * @author Michael Myers
 * @since 0.5pre3
 */
public final class Utilities {
    
    /** Creates a new instance of Utilities */
    private Utilities() { }
    
    
//    static final String[] noCountryStrings = {
//        "", "---", "XXX", "none"
//    };
    
    static boolean isNotACountry(final String tag) {
        // Tested and found that .equals is much faster than .matches, even if
        // it's done several times and .matches is done once.
        if (tag.length() == 0 || tag.equals("---") ||
                tag.equalsIgnoreCase("XXX") || tag.equalsIgnoreCase("none"))
            return true;
        return false;
        
//        for (String match : noCountryStrings) {
//            if (tag.equalsIgnoreCase(match))
//                return true;
//        }
//        return false;
    }
    
    
    // Country, religion, and goods colors

    static final Color COLOR_LAND_DEFAULT = new Color(100, 100, 100); // darker than GRAY, lighter than DARK_GRAY
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
    
    /** Settings used when loading all data files in this class. */
    private static final ParserSettings settings =
            ParserSettings.getNoCommentSettings().setPrintTimingInfo(false);
    
    
    private static GenericObject countries;
    
    private static final java.util.Map<String, Color> ctryColorCache =
            new HashMap<String, Color>();
    
    private static GenericObject religions;
    
    private static final java.util.Map<String, Color> relColorCache =
            new HashMap<String, Color>();

    private static GenericObject titles;

    private static final java.util.Map<String, Color> titleColorCache =
            new HashMap<String, Color>();

    private static FilenameResolver resolver;

    /** @since 0.7.5 */
    public static void init(FilenameResolver resolver) {
        Utilities.resolver = resolver;
    }
    
    public static void initCountries() {
        countries = EUGFileIO.load(resolver.resolveFilename("common/countries.txt"), settings);
        if (countries == null)
            countries = EUGFileIO.loadAll(resolver.listFiles("common/country_tags"), settings);
    }

    private static void initReligions() {
        religions = EUGFileIO.load(resolver.resolveFilename("common/religion.txt"), settings);
        if (religions == null)
            religions = EUGFileIO.loadAll(resolver.listFiles("common/religions"), settings);
    }
    
    private static void initTitles() {
        titles = EUGFileIO.load(resolver.resolveFilename("common/landed_titles.txt"), settings);

        readTitleColors(titles);
    }

    private static void readTitleColors(GenericObject titles) {
        for (GenericObject title : titles.children) {
            if (title.isEmpty())
                continue; // skip bishoprics and such

            GenericList color = title.getList("color");

            if (color == null) {
                //System.err.println("color for " + title.name + " is null");
                titleColorCache.put(title.name, COLOR_NO_CTRY_DEF);
            } else {
                final int red = Math.min((int)Double.parseDouble(color.get(0)), 255);
                final int green = Math.min((int)Double.parseDouble(color.get(1)), 255);
                final int blue = Math.min((int)Double.parseDouble(color.get(2)), 255);

                titleColorCache.put(title.name, new Color(red, green, blue));
            }

            readTitleColors(title);
        }
    }
    
    static Color getCtryColor(String country) {
        // Special case: XXX means no one
        if (isNotACountry(country))
            return COLOR_LAND_DEFAULT;
        else
            country = toUpperCase(country);
        
        if (countries == null)
            initCountries();
        
        Color ret = ctryColorCache.get(country);
        if (ret == null) {
            String filename = countries.getString(country);
            if (!filename.startsWith("common"))
                filename = "common/" + filename;
            filename = resolver.resolveFilename(filename);

            final GenericObject countryDef = EUGFileIO.load(filename, settings);
            
            if (countryDef == null) {
                System.err.println("No country definition file found for " + country + ".");
                System.err.println("Expected to find it in " + countries.getString(country));
                return COLOR_NO_CTRY_DEF;
            }
            
            final GenericList color = countryDef.getList("color");
            
            if (color == null) {
                System.err.println("color for " + country + " is null");
                return COLOR_NO_CTRY_DEF;
            }
            
            final int red = Math.min((int)Double.parseDouble(color.get(0)), 255);
            final int green = Math.min((int)Double.parseDouble(color.get(1)), 255);
            final int blue = Math.min((int)Double.parseDouble(color.get(2)), 255);
            
            ret = new Color(red, green, blue);
            
            ctryColorCache.put(country, ret);
        }
        return ret;
    }
    
    static Color getReligionColor(String religion) {
        if (religions == null)
            initReligions();
        
        religion = religion.toLowerCase();
        
        Color ret = relColorCache.get(religion);
        
        if (ret == null) {
            for (GenericObject group : religions.children) {
                if (group.name.equalsIgnoreCase(religion)) {
                        // found it, which means this isn't a group at all
                        GenericList color = group.getList("color");
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
                for (GenericObject rel : group.children) {
                    if (rel.name.equalsIgnoreCase(religion)) {
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
    
    static Color getTitleColor(String title) {
        if (titles == null)
            initTitles();

        return titleColorCache.get(title);
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
    
    private static final Rectangle imageRect = new Rectangle(0,0,8,8);
    
    /*
     * The TexturePaints use 8x8 rectangles like the following (where x is the
     * background color and o is the foreground color):
     * 
     * ooxxxxoo
     * oxxxxooo
     * xxxxoooo
     * xxxoooox
     * xxooooxx
     * xooooxxx
     * ooooxxxx
     * oooxxxxo
     * 
     * Tiling this produces diagonally striped paint.
     */
    static Paint createPaint(final Color c1, final Color c2) {
        final ColorPair cp = new ColorPair(c1, c2);
        Paint ret = imgCache.get(cp);
        if (ret == null) {
            final BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = img.createGraphics();
            g.setBackground(c2);
            g.clearRect(0, 0, 8, 8);
            
            g.setColor(c1);
            g.drawLine(0,0,1,0);
            g.drawLine(6,0,7,0);
            
            g.drawLine(0,1,0,1);
            g.drawLine(5,1,7,1);
            
            g.drawLine(4, 2, 7, 2);
            g.drawLine(3, 3, 6, 3);
            g.drawLine(2, 4, 5, 4);
            g.drawLine(1, 5, 4, 5);
            g.drawLine(0, 6, 3, 6);
            
            g.drawLine(0, 7, 2, 7);
            g.drawLine(7, 7, 7, 7);
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
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ColorPair))
                return false;
            final ColorPair cp = (ColorPair) other;
            return c1.equals(cp.c1) && c2.equals(cp.c2);
        }
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.c1 != null ? this.c1.hashCode() : 0);
            hash = 29 * hash + (this.c2 != null ? this.c2.hashCode() : 0);
            return hash;
        }
    }
}
