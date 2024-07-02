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
import eug.shared.ObjectVariable;
import eug.shared.WritableObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        if (tag == null || tag.length() == 0 || tag.equals("---") ||
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
    
    private static final Color[] DEFAULT_CULTURE_COLORS = {
        // These are the 12 basic RGB colors, plus a couple extras, shuffled around a bit so that adjacent culture groups don't get too similar colors (much).
        // Most of them have also been slightly modified by boosting values of 0 up to 10 to give a slightly brighter base color before we darken them.
        new Color( 10,  10, 255), // blue
        new Color( 10, 255, 255), // cyan
        new Color(192, 192, 192), // light gray
        new Color( 10, 255, 127),
        new Color( 10, 255,  10), // green
        new Color(255, 255,  10), // yellow
        new Color(255, 127,  10),
        new Color(127,  10, 255),
        new Color( 10, 127, 255),
        new Color(255,  10,  10), // red
        new Color(255,  10, 127),
        new Color(255,  10, 255), // magenta
        new Color(127, 255,  10),
        new Color(210, 180, 140), // tan
        new Color(210,  92,  92), // dusky red
        new Color(140, 180, 210), // steel blue
    };
    
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
    
    private static final List<Color> geographyColors = new ArrayList<>();
    
    private static java.util.Map<String, Color> namedColors;

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
        if (religions == null || religions.isEmpty())
            religions = EUGFileIO.loadAllUTF8(resolver.listFiles("common/religion/religions"), settings);
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
        if (cultures == null || cultures.isEmpty())
            initCK3Cultures();
        
        // go ahead and set up the colors here since we need to keep track of
        // which color each culture group is
        
        Color[] groupColors = DEFAULT_CULTURE_COLORS;
        int colorIdx = 0;
        
        for (GenericObject group : cultures.children) {
            try {
                Color baseColor = groupColors[colorIdx++];
                colorIdx = colorIdx % groupColors.length;

                int colorModIdx = 0;

                int numColors = group.children.size(); // shouldn't include male_names, female_names, or dynasty_names since those are lists and not children
                
                double redFactor   = baseColor.getRed()   / (numColors+1.0);
                double greenFactor = baseColor.getGreen() / (numColors+1.0);
                double blueFactor  = baseColor.getBlue()  / (numColors+1.0);
                
                Color[] colors = new Color[numColors];
                colors[0] = baseColor;
                for (int i = 1; i < numColors; i++) {
                    colors[i] = new Color(
                                    (int)(Math.max(colors[0].getRed()   - i*redFactor,   0)),
                                    (int)(Math.max(colors[0].getGreen() - i*greenFactor, 0)),
                                    (int)(Math.max(colors[0].getBlue()  - i*blueFactor,  0)));
                }
                int i = 0;
                for (GenericObject cul : group.children) {
                    if (cul.name.equals("country") || cul.name.equals("province")) // special stuff Paradox decided should go into cultures.txt
                        continue;
                    
                    int index;
                    if (colorModIdx % 2 == 0) // alternate light and dark shades since cultures are usually defined in geographically sequential order
                        index = i;
                    else
                        index = (int)(numColors/2.0 + 0.5) + i++; // use ceiling to avoid off-by-one when we have an odd number. Then post-increment i to avoid a different off-by-one.
                    
                    Color modColor = colors[index];
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
    
    private static void initCK3Cultures() {
        initNamedColors();
        cultures = EUGFileIO.loadAllUTF8(resolver.listFiles("common/culture/cultures"), settings);
        for (GenericObject culture : cultures.children) {
            Color color = parseColorMaybeHsv(culture, "color");
            if (color == null)
                color = namedColors.get(culture.name);
            cultureColorCache.put(culture.name, color);
        }
    }
    
    private static void initNamedColors() {
        if (namedColors == null) {
            namedColors = new HashMap<>();
            GenericObject namedColorsObj = EUGFileIO.loadUTF8(new java.io.File(resolver.resolveFilename("common/named_colors/culture_colors.txt")), ParserSettings.getQuietSettings());
            GenericObject colorsRoot = namedColorsObj.getChild("colors");
            for (int i = 0; i < colorsRoot.getAllWritable().size(); i++) {
                WritableObject obj = colorsRoot.getAllWritable().get(i);
                if (obj instanceof GenericList) {
                    GenericList list = (GenericList) obj;
                    namedColors.put(list.getName(), parseColor(list));
                } else if (obj instanceof ObjectVariable) {
                    namedColors.put(((ObjectVariable) obj).varname, parseColorHsv((GenericList)colorsRoot.getAllWritable().get(++i)));
                }
            }
        }
    }
    
    private static void initGeographyColors() {
        GenericObject colors = EUGFileIO.load(resolver.resolveFilename("common/region_colors.txt"), settings);
        if (colors == null)
            colors = EUGFileIO.loadAll(resolver.listFiles("common/region_colors"), settings);
        if (colors == null || colors.isEmpty())
            colors = EUGFileIO.load(resolver.resolveFilename("common/cot_colors.txt"), settings);
        if (colors == null) {
            geographyColors.addAll(Arrays.asList(DEFAULT_CULTURE_COLORS));
            return;
        }
        
        for (GenericList color : colors.lists) {
            if (color.getName().equalsIgnoreCase("color")) {
                Color c = parseColor(color);
                if (c != null)
                    geographyColors.add(c);
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
        
        if (r > 1 || g > 1 || b > 1) { // assume [0, 255] scale if any value is outside [0, 1]
            if (r > 255 || g > 255 || b > 255)
                log.log(Level.WARNING, "Color value is outside of the expected range: {0} = '{' {1} {2} {3} '}'", new Object[] { color.getName(), r, g, b });
            return new Color((int) Math.min(r, 255), (int) Math.min(g, 255), (int) Math.min(b, 255));
        }
        return new Color(r, g, b);
    }
    
    private static Color parseColorHsv(GenericList color) {
        if (color.size() < 3) {
            log.log(Level.WARNING, "Unable to parse HSV color: {0}", color.toString());
            return null;
        }
        
        float h = Float.parseFloat(color.get(0));
        float s = Float.parseFloat(color.get(1));
        float v = Float.parseFloat(color.get(2));
        
        return new Color(Color.HSBtoRGB(h, s, v));
    }
    
    private static Color parseColorMaybeHsv(GenericObject parent, String key) {
        for (int i = 0; i < parent.getAllWritable().size(); i++) {
            WritableObject obj = parent.getAllWritable().get(i);
            if (obj instanceof GenericList) {
                GenericList maybeColor = (GenericList) obj;
                if (maybeColor.getName().equalsIgnoreCase(key))
                    return parseColor(maybeColor);
            } else if (obj instanceof ObjectVariable) {
                ObjectVariable maybeColorVar = (ObjectVariable) obj;
                if (maybeColorVar.varname.equalsIgnoreCase(key) && maybeColorVar.getValue().equalsIgnoreCase("hsv")) {
                    WritableObject shouldBeColorObj = parent.getAllWritable().get(i+1);
                    if (shouldBeColorObj instanceof GenericList) {
                        return parseColorHsv((GenericList) shouldBeColorObj);
                    }
                } else if (maybeColorVar.varname.equalsIgnoreCase(key) && maybeColorVar.getValue().equalsIgnoreCase("rgb")) {
                    WritableObject shouldBeColorObj = parent.getAllWritable().get(i+1);
                    if (shouldBeColorObj instanceof GenericList) {
                        return parseColor((GenericList) shouldBeColorObj);
                    }
                }
            }
        }
        return null;
    }

    private static void readTitleColors(GenericObject titles) {
        titles.children.stream()
                .filter((GenericObject title) -> !title.isEmpty() && title.name.charAt(1) == '_') // skip bishoprics and triggers and such
                .map((GenericObject title) -> {

            try {
                Color c = parseColorMaybeHsv(title, "color");
                if (c == null) {
                    titleColorCache.put(title.name, COLOR_NO_CTRY_DEF);
                } else {
                    titleColorCache.put(title.name, c);
                }
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Error parsing a title color. Title is {0}", title.name);
                log.log(Level.SEVERE, "The actual error is below.", ex);
                titleColorCache.put(title.name, COLOR_NO_CTRY_DEF);
            }
            return title;
        }).forEach(title -> readTitleColors(title));
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
                        return cacheReligion(group, religion);
                    }
                    for (GenericObject rel : group.children) {
                        if (rel.name.equalsIgnoreCase(religion)) {
                            return cacheReligion(rel, religion);
                        }
                    }
                    // didn't find it any other way
                    // let's try CK3 style, which uses christianity_religion = { faiths = { catholic = { } } }
                    if (group.containsChild("faiths")) {
                        GenericObject faiths = group.getChild("faiths");
                        
                        for (GenericObject rel : faiths.children) {
                            if (rel.name.equalsIgnoreCase(religion)) {
                                return cacheReligion(rel, religion);
                            }
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

    private static Color cacheReligion(GenericObject group, String religion) {
        Color ret = parseColorMaybeHsv(group, "color");
        if (ret == null && group.hasString("color")) {
            initNamedColors();
            ret = namedColors.get(group.getString("color"));
        }
        if (ret == null) {
            log.log(Level.WARNING, "color for {0} is null", religion);
            ret = COLOR_NO_RELIGION_DEF;
        }
        relColorCache.put(religion, ret);
        return ret;
    }
    
    static Color getTitleColor(String title) {
        if (titles == null)
            initTitles();

        return titleColorCache.getOrDefault(title, COLOR_NO_CTRY_DEF);
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
    
    public static String getCultureGroup(String culture) {
        if (cultures == null)
            initCultures();
        return cultureGroups.get(culture);
    }
    
    static List<Color> getGeographyColors() {
        if (geographyColors.isEmpty()) {
            initGeographyColors();
        }
        return geographyColors;
    }
    
    // Our own little toUpperCase method, because every tag is converted to
    // upper case before processing. This method, unlike String.toUpperCase(),
    // assumes that only ASCII characters are used, so a faster algorithm can be
    // used.
    
    private static final int ASCII_CASE_DIFF = 'A' - 'a';
    
    private static String toUpperCase(final String st) {
        final char[] cArr = st.toCharArray();
        boolean change = false;
        for (int i = 0; i < cArr.length; i++) {
            int c = cArr[i];
            if (c >= 'a' && c <= 'z') {
                cArr[i] = (char) (c+ASCII_CASE_DIFF);
                change = true;
            }
        }
        // only create a new string when necessary
        return change ? String.valueOf(cArr) : st;
    }
    
    /**
     * Creates an array of colors ranging from minColor through midColor to maxColor.
     */
    public static Color[] createSteppedColors(int min, int max, int step, Color minColor, Color midColor, Color maxColor) {
        int numColors = (max-min)/step;
        Color[] colors = new Color[numColors];
        final int minRed = minColor.getRed(),   midRed = midColor.getRed(),   maxRed = maxColor.getRed();
        final int minGrn = minColor.getGreen(), midGrn = midColor.getGreen(), maxGrn = maxColor.getGreen();
        final int minBlu = minColor.getBlue(),  midBlu = midColor.getBlue(),  maxBlu = maxColor.getBlue();
        final int middle = Math.max(1, numColors/2);
        for (int i = 0; i < middle; i++) {
            int red = mix(minRed, midRed, i, middle);
            int grn = mix(minGrn, midGrn, i, middle);
            int blu = mix(minBlu, midBlu, i, middle);
            colors[i] = new Color(red, grn, blu);
        }
        for (int i = middle; i < numColors; i++) {
            int red = mix(midRed, maxRed, i - middle, middle);
            int grn = mix(midGrn, maxGrn, i - middle, middle);
            int blu = mix(midBlu, maxBlu, i - middle, middle);
            colors[i] = new Color(red, grn, blu);
        }
        
        // sanity in case there are too few colors and we have rounding errors
        colors[0] = minColor;
        if (numColors > 0)
            colors[numColors-1] = maxColor;
        
        return colors;
    }
    
    private static int mix(int min, int max, int ratio, int maxRatio) {
        int ret = (max * ratio);
        ret += (min * (maxRatio - ratio));
        return (ret / maxRatio);
    }
    
    
    // Texture handling (striped paints)
    
    private static final java.util.Map<ColorPair, Paint> imgCache = new HashMap<>();
    private static final java.util.Map<ColorPair, Paint> imgCacheEqual = new HashMap<>();
    private static final java.util.Map<ColorTriple, Paint> imgCacheTriple = new HashMap<>();
    
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
    
    /*
     * The TexturePaints use 8x8 rectangles like the following (where x is the
     * background color and o is the foreground color):
     * 
     *   01234567
     * 0 oooxxxxo
     * 1 ooxxxxoo
     * 2 oxxxxooo
     * 3 xxxxoooo
     * 4 xxxoooox
     * 5 xxooooxx
     * 6 xooooxxx
     * 7 ooooxxxx
     * 
     * Tiling this produces diagonally striped paint.
     */
    static Paint createEqualPaint(final Color background, final Color foreground) {
        final ColorPair cp = new ColorPair(background, foreground);
        Paint ret = imgCacheEqual.get(cp);
        if (ret == null) {
            final BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = img.createGraphics();
            g.setBackground(background);
            g.clearRect(0, 0, 8, 8);
            
            g.setColor(foreground);
            //        (x,y) (x,y)  // done left-to-right, top-to-bottom
            g.drawLine(0,0,  2,0); // upper left corner
            g.drawLine(7,0,  7,0); // and upper right

            g.drawLine(0,1,  1,1); // second line
            g.drawLine(6,1,  7,1);
            
            g.drawLine(0,2,  0,2); // third line
            g.drawLine(5,2,  7,2);
            
            g.drawLine(4,3,  7,3);
            g.drawLine(3,4,  6,4);
            g.drawLine(2,5,  5,5);
            g.drawLine(1,6,  4,6);
            g.drawLine(0,7,  3,7);
            g.dispose();
            
            ret = new TexturePaint(img, imageRect);
            imgCacheEqual.put(cp, ret);
        }
        
        return ret;
    }
    
    /*
     * The TexturePaints use 9x9 rectangles like the following (where x is the
     * background color, o is the first foreground color, and z is the second):
     * 
     *   012345678
     * 0 oooxxxzzz
     * 1 ooxxxzzzo
     * 2 oxxxzzzoo
     * 3 xxxzzzooo
     * 4 xxzzzooox
     * 5 xzzzoooxx
     * 6 zzzoooxxx
     * 7 zzoooxxxz
     * 8 zoooxxxzz
     * 
     * Tiling this produces diagonally striped paint.
     */
    static Paint createTricolorPaint(final Color background, final Color foreground1, final Color foreground2) {
        final ColorTriple ct = new ColorTriple(background, foreground1, foreground2);
        Paint ret = imgCacheTriple.get(ct);
        if (ret == null) {
            final BufferedImage img = new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = img.createGraphics();
            g.setBackground(background);
            g.clearRect(0, 0, 9, 9);
            
            g.setColor(foreground1);
            //        (x,y) (x,y)  // done left-to-right, top-to-bottom
            g.drawLine(0,0,  2,0); // upper left corner

            g.drawLine(0,1,  1,1); // second line
            g.drawLine(8,1,  8,1);
            
            g.drawLine(0,2,  0,2); // third line
            g.drawLine(7,2,  8,2);
            
            g.drawLine(6,3,  8,3);
            g.drawLine(5,4,  7,4);
            g.drawLine(4,5,  6,5);
            g.drawLine(3,6,  5,6);
            g.drawLine(2,7,  4,7);
            g.drawLine(1,8,  3,8);
            
            g.setColor(foreground2);
            //        (x,y) (x,y)  // done left-to-right, top-to-bottom
            g.drawLine(6,0,  8,0);
            g.drawLine(5,1,  7,1);
            g.drawLine(4,2,  6,2);
            g.drawLine(3,3,  5,3);
            g.drawLine(2,4,  4,4);
            g.drawLine(1,5,  3,5);
            g.drawLine(0,6,  2,6);
            
            g.drawLine(0,7,  1,7);
            g.drawLine(8,7,  8,7); // right side
            
            g.drawLine(0,8,  0,8);
            g.drawLine(7,8,  8,8); // right side
            
            g.dispose();
            
            ret = new TexturePaint(img, imageRect);
            imgCacheTriple.put(ct, ret);
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
    
    private static final class ColorTriple {
        private final Color c1;
        private final Color c2;
        private final Color c3;
        public ColorTriple(Color c1, Color c2, Color c3) {
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
        }
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ColorTriple))
                return false;
            final ColorTriple ct = (ColorTriple) other;
            return c1.equals(ct.c1) && c2.equals(ct.c2) && c3.equals(ct.c3);
        }
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.c1 != null ? this.c1.hashCode() : 0);
            hash = 29 * hash + (this.c2 != null ? this.c2.hashCode() : 0);
            hash = 29 * hash + (this.c3 != null ? this.c3.hashCode() : 0);
            return hash;
        }
    }
}
