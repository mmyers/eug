package editor;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.GenericObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class GameVersion {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(GameVersion.class.getName());

    private static List<GameVersion> gameVersions;
    
    private String name;
    private String display;
    
    private String startDate;
    
    private boolean hasLandList;
    private boolean hasLakes;
    private boolean isMapInverted;
    private boolean hasRegions;
    private boolean hasClimateTxt;

    private String saveType;
    private boolean hasBookmarks;

    private String provinceLocFmt;
    private String textFmt; // Either "csv" or "yaml", as of EU4

    private String viewSet;

    private boolean newStyleMod;
    private String modPath;
    
    private GameVersion() {
    }

    private static void readVersions() {
        gameVersions = new ArrayList<>();

        GenericObject allVersions = EUGFileIO.load("games.txt", ParserSettings.getQuietSettings());

        for (GenericObject version : allVersions.children) {
            GameVersion newVersion = new GameVersion();
            if (version.hasString("inherit")) {
                GameVersion old = getByName(version.getString("inherit").toLowerCase());
                if (old != null) {
                    newVersion.startDate = old.startDate;
                    newVersion.isMapInverted = old.isMapInverted;
                    newVersion.hasLandList = old.hasLandList;
                    newVersion.hasLakes = old.hasLakes;
                    newVersion.hasRegions = old.hasRegions;
                    newVersion.hasClimateTxt = old.hasClimateTxt;
                    newVersion.saveType = old.saveType;
                    newVersion.hasBookmarks = old.hasBookmarks;
                    newVersion.provinceLocFmt = old.provinceLocFmt;
                    newVersion.textFmt = old.textFmt;
                    newVersion.viewSet = old.viewSet;
                    newVersion.newStyleMod = old.newStyleMod;
                    newVersion.modPath = old.modPath;
                } else {
                    log.log(Level.WARNING, "Invalid ''inherit'' directive: ''{0}''", version.getString("inherit"));
                }
            }

            newVersion.name = version.name.toLowerCase();
            newVersion.display = version.getString("display");

            if (version.hasString("start_date"))
                newVersion.startDate = version.getString("start_date");

            if (version.hasString("map_inverted"))
                newVersion.isMapInverted = version.getBoolean("map_inverted");
            if (version.hasString("has_land_list"))
                newVersion.hasLandList = version.getBoolean("has_land_list");
            if (version.hasString("has_regions"))
                newVersion.hasRegions = version.getBoolean("has_regions");
            if (version.hasString("has_climate_txt"))
                newVersion.hasClimateTxt = version.getBoolean("has_climate_txt");
            if (version.hasString("has_lakes"))
                newVersion.hasLakes = version.getBoolean("has_lakes");

            if (version.hasString("province_loc"))
                newVersion.provinceLocFmt = version.getString("province_loc");
            else
                newVersion.provinceLocFmt = "PROV%d";

            if (version.hasString("text"))
                newVersion.textFmt = version.getString("text").toLowerCase();
            else
                newVersion.textFmt = "csv";

            if (version.hasString("save_type"))
                newVersion.saveType = version.getString("save_type").toLowerCase();

            if (version.hasString("has_bookmarks"))
                newVersion.hasBookmarks = version.getBoolean("has_bookmarks");

            if (version.hasString("view_set"))
                newVersion.viewSet = version.getString("view_set").toLowerCase();

            if (version.hasString("new_style_mod"))
                newVersion.newStyleMod = version.getBoolean("new_style_mod");
            if (version.hasString("mod_path"))
                newVersion.modPath = version.getString("mod_path");

            gameVersions.add(newVersion);
        }
    }

    public static GameVersion getByName(String name) {
        name = name.toLowerCase();
        for (GameVersion version : gameVersions) {
            if (version.getName().equals(name))
                return version;
        }
        return null;
    }

    public static GameVersion getByDisplay(String display) {
        for (GameVersion version : gameVersions) {
            if (version.getDisplay().equalsIgnoreCase(display))
                return version;
        }
        return null;
    }

    public static List<GameVersion> getGameVersions() {
        if (gameVersions == null)
            readVersions();
        return gameVersions;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public String getStartDate() {
        return startDate;
    }

    /** True if default.map has a list of sea provinces instead of a sea_starts number. */
    public boolean hasLandList() {
        return hasLandList;
    }

    public boolean hasLakes() {
        return hasLakes;
    }

    public boolean isMapUpsideDown() {
        return isMapInverted;
    }

    public boolean hasRegions() {
        return hasRegions;
    }

    public boolean hasClimateTxt() {
        return hasClimateTxt;
    }

    public String getSaveType() {
        return saveType;
    }

    public boolean hasBookmarks() {
        return hasBookmarks;
    }

    public String getViewSet() {
        return viewSet;
    }

    public String getTextFormat() {
        return textFmt;
    }

    public boolean isNewStyleMod() {
        return newStyleMod;
    }

    public String getModPath() {
        return modPath;
    }

    @Override
    public String toString() {
        return display;
    }
}
