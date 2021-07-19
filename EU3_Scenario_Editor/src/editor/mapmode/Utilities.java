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
import java.util.logging.Level;

/**
 * A place for static methods commonly used by mapmodes.
 * Must first be initialized by calling {@link init}.
 * @author Michael Myers
 * @since 0.5pre3
 */
public final class Utilities {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Utilities.class.getName());
    
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
    /** Color used if the culture definition cannot be found. */
    static final Color COLOR_NO_CULTURE_DEF = Color.BLACK;
    /** Color used if a land province does not have a defined culture. */
    static final Color COLOR_NO_CULTURE = Color.BLACK;
    
    /* Settings used when loading all data files in this class. */
    private static final ParserSettings settings =
            ParserSettings.getNoCommentSettings().setPrintTimingInfo(false);
    
    
    private static GenericObject countries;
    
    private static final java.util.Map<String, Color> ctryColorCache = new HashMap<>();
    
    private static GenericObject religions;
    
    private static final java.util.Map<String, Color> relColorCache = new HashMap<>();

    private static GenericObject titles;

    private static final java.util.Map<String, Color> titleColorCache = new HashMap<>();
    
    private static GenericObject cultures;
    
    private static final java.util.Map<String, Color> cultureColorCache = new HashMap<>();
    
    /** Mapping of culture to its culture group */
    private static final java.util.Map<String, String> cultureGroups = new HashMap<>();

    private static FilenameResolver resolver;

    /** @since 0.7.5 */
    public static void init(FilenameResolver resolver) {
        Utilities.resolver = resolver;
    }
    
    private static void initCountries() {
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
        if (titles == null)
            titles = EUGFileIO.loadAll(resolver.listFiles("common/landed_titles"), settings);

        readTitleColors(titles);
    }
    
    private static void initCultures() { 
        cultures = EUGFileIO.load(resolver.resolveFilename("common/cultures.txt"), settings);
        if (cultures == null)
            cultures = EUGFileIO.loadAll(resolver.listFiles("common/cultures"), settings);
        
        // go ahead and set up the colors here since we need to keep track of
        // which color each culture group is
        
        Color[] groupColors = {
            Color.BLUE.darker().darker(),
            Color.GRAY,
            Color.RED.darker().darker(),
            Color.CYAN.darker().darker(),
            Color.PINK.darker(),
            Color.GREEN.darker().darker(),
            Color.YELLOW.darker().darker(),
            Color.MAGENTA.darker().darker()
        };
        int colorIdx = 0;
        
        for (GenericObject group : cultures.children) {
            try {
                Color baseColor = groupColors[colorIdx++];
                colorIdx = colorIdx % groupColors.length;

                int colorModIdx = 0;

                for (GenericObject cul : group.children) {
                    Color modColor = baseColor;
                    if (colorModIdx % 2 == 0) {
                        for (int i = 0; i < colorModIdx / 2; i++) {
                            modColor = modColor.darker();
                        }
                    } else {
                        for (int i = 0; i < colorModIdx / 2 + 1; i++) {
                            modColor = modColor.brighter();
                        }
                    }
                    colorModIdx++;
                    cultureColorCache.put(cul.name, modColor);

                    cultureGroups.put(cul.name, group.name);
                }
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Error parsing a culture color in culture group {0}", group.name);
                log.log(Level.SEVERE, "The actual error is below.", ex);
            }
        }
    }
    
    private static Color parseColor(GenericList color) {
        if (color.size() != 3) {
            log.log(Level.WARNING, "Unable to parse color: {0}", color.toString());
            return COLOR_NO_HIST;
        }
        
        // No rail correction is done in this method. Float colors are assumed to be between 0.0 and 1.0
        // and integer colors are assumed to be between 0 and 255.
        // If a value is out of bounds, an IllegalArgumentException will be thrown.
        
        float r = Float.parseFloat(color.get(0));
        float g = Float.parseFloat(color.get(1));
        float b = Float.parseFloat(color.get(2));
        
        if (color.get(0).contains(".") || color.get(1).contains(".") || color.get(2).contains("."))
            return new Color(r, g, b);
        
        if (r > 1 || g > 1 || b > 1) // assume [0, 255] scale if any value is outside [0, 1]
            return new Color((int) r, (int) g, (int) b);
        return new Color(r, g, b);
    }

    private static void readTitleColors(GenericObject titles) {
        titles.children.stream()
                .filter((title) -> !(title.isEmpty())) // skip bishoprics and such
                .map((title) -> {

            GenericList color = title.getList("color");
            if (color == null) {
                //log.log(Level.WARNING, "color for {0} is null", title.name);
                titleColorCache.put(title.name, COLOR_NO_CTRY_DEF);
            } else {
                try {
                    Color c = parseColor(color);
                    titleColorCache.put(title.name, c);
                } catch (RuntimeException ex) {
                    log.log(Level.SEVERE, "Error parsing a title color. Title is {0}", title.name);
                    log.log(Level.SEVERE, "The actual error is below.", ex);
                    titleColorCache.put(title.name, COLOR_NO_CTRY_DEF);
                }
            }
            return title;
        }).forEach((title) -> {
            readTitleColors(title);
        });
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
                log.log(Level.WARNING, "No country definition file found for {0}. Expected to find it in {1}.", new Object[] { country, countries.getString(country) });
                ctryColorCache.put(country, COLOR_NO_CTRY_DEF);
                return COLOR_NO_CTRY_DEF;
            }
            
            final GenericList color = countryDef.getList("color");
            
            if (color == null) {
                log.log(Level.WARNING, "color for {0} is null", country);
                ctryColorCache.put(country, COLOR_NO_CTRY_DEF);
                return COLOR_NO_CTRY_DEF;
            }
            
            try {
                ret = parseColor(color);

                ctryColorCache.put(country, ret);
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Error parsing a country\'s color. Country tag is {0}", country);
                log.log(Level.SEVERE, "The actual error is below.", ex);
            }
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
                try {
                    if (group.name.equalsIgnoreCase(religion)) {
                            // found it, which means this isn't a group at all
                            GenericList color = group.getList("color");
                            if (color == null) {
                                log.log(Level.WARNING, "color for {0} is null", religion);
                                relColorCache.put(religion, COLOR_NO_RELIGION_DEF);
                                return COLOR_NO_RELIGION_DEF;
                            }
                            ret = parseColor(color);
                            relColorCache.put(religion, ret);
                            return ret;
                    }
                    for (GenericObject rel : group.children) {
                        if (rel.name.equalsIgnoreCase(religion)) {
                            // found it
                            GenericList color = rel.getList("color");
                            if (color == null) {
                                log.log(Level.WARNING, "color for {0} is null", religion);
                                relColorCache.put(religion, COLOR_NO_RELIGION_DEF);
                                return COLOR_NO_RELIGION_DEF;
                            }
                            ret = parseColor(color);
                            relColorCache.put(religion, ret);
                            return ret;
                        }
                    }
                } catch (RuntimeException ex) {
                    log.log(Level.SEVERE, "Error parsing a religion color. Religion (or religion group) is {0}", religion);
                    log.log(Level.SEVERE, "The actual error is below.", ex);
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
    
    static Color getCultureColor(String culture) {
        if (cultures == null)
            initCultures();
        
        culture = culture.toLowerCase();
        
        Color ret = cultureColorCache.get(culture);
        
        if (ret == null) {
            return COLOR_NO_CULTURE_DEF;
        }
        
        return ret;
    }
    
    static String getCultureGroup(String culture) {
        return cultureGroups.get(culture);
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
    
    private static final java.util.Map<ColorPair, Paint> imgCache = new HashMap<>();
    
    private static final Rectangle imageRect = new Rectangle(0,0,8,8);
    
    /*
     * The TexturePaints use 8x8 rectangles like the following (where x is the
     * background color and o is the foreground color):
     * 
     * ooxxxxxo
     * oxxxxxoo
     * xxxxxooo
     * xxxxooox
     * xxxoooxx
     * xxoooxxx
     * xoooxxxx
     * oooxxxxx
     * 
     * Tiling this produces diagonally striped paint.
     */
    static Paint createPaint(final Color background, final Color foreground) {
        final ColorPair cp = new ColorPair(background, foreground);
        Paint ret = imgCache.get(cp);
        if (ret == null) {
            final BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = img.createGraphics();
            g.setBackground(background);
            g.clearRect(0, 0, 8, 8);
            
            g.setColor(foreground);
            //        (x,y) (x,y)  // done left-to-right, top-to-bottom
            g.drawLine(0,0,  1,0); // upper left corner
            g.drawLine(7,0,  7,0); // and upper right

            g.drawLine(0,1,  0,1); // second line
            g.drawLine(6,1,  7,1);
            
            g.drawLine(5,2,  7,2);
            g.drawLine(4,3,  6,3);
            g.drawLine(3,4,  5,4);
            g.drawLine(2,5,  4,5);
            g.drawLine(1,6,  3,6);
            
            g.drawLine(0,7,  2,7);
            //g.drawLine(7,7,  7,7);
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
