package editor;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;
import java.util.ArrayList;
import java.util.List;

public class GameVersion {

//    VANILLA             (false, true,  false, true),
//    IN_NOMINE           (true,  true,  true,  true),
//    HEIR_TO_THE_THRONE  (true,  true,  true,  true),
//    DIVINE_WIND         (true,  false, true,  true),
//    MAGNA_MUNDI         (true,  true,  true,  true),
//    ROME                (false, false, false, true),
//    VICTORIA            (true,  true,  true,  false),
//    HOI3                (true,  true,  true,  false),
//    CK2                 (true,  false,  true,  false);

    private static List<GameVersion> gameVersions;
    
    private String name;
    private String display;
    
    private String startDate;
    
    private boolean hasLandList;
    private boolean isMapInverted;
    private boolean hasRegions;
    private boolean hasClimateTxt;

    private String saveType;
    private boolean hasBookmarks;

    private String provinceLocFmt;

    private String viewSet;
    
    private GameVersion() {
    }

    private static void readVersions() {
        gameVersions = new ArrayList<GameVersion>();

        GenericObject allVersions = EUGFileIO.load("games.txt", ParserSettings.getQuietSettings());

        for (GenericObject version : allVersions.children) {
            GameVersion newVersion = new GameVersion();
            if (version.hasString("inherit")) {
                GameVersion old = getByName(version.getString("inherit"));
                if (old != null) {
                    newVersion.startDate = old.startDate;
                    newVersion.isMapInverted = old.isMapInverted;
                    newVersion.hasLandList = old.hasLandList;
                    newVersion.hasRegions = old.hasRegions;
                    newVersion.hasClimateTxt = old.hasClimateTxt;
                    newVersion.saveType = old.saveType;
                    newVersion.hasBookmarks = old.hasBookmarks;
                    newVersion.provinceLocFmt = old.provinceLocFmt;
                    newVersion.viewSet = old.viewSet;
                } else {
                    System.err.println("Invalid 'inherit' directive: '" + version.getString("inherit") + "'");
                }
            }

            newVersion.name = version.name;
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

            if (version.hasString("province_loc"))
                newVersion.provinceLocFmt = version.getString("province_loc");
            else
                newVersion.provinceLocFmt = "PROV%d";

            if (version.hasString("save_type"))
                newVersion.saveType = version.getString("save_type");

            if (version.hasString("has_bookmarks"))
                newVersion.hasBookmarks = version.getBoolean("has_bookmarks");

            if (version.hasString("view_set"))
                newVersion.viewSet = version.getString("view_set");

            gameVersions.add(newVersion);
        }
    }

    public static GameVersion getByName(String name) {
        for (GameVersion version : gameVersions) {
            if (version.getName().equalsIgnoreCase(name))
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

    @Override
    public String toString() {
        return display;
    }

//    public static GameVersion detectGame(FilenameResolver gameResolver) {
//        if (gameResolver.getMainDirName().contains("Victoria")) {
//            return GameVersion.VICTORIA;
//        } else if (gameResolver.getMainDirName().contains("Hearts")) {
//            return GameVersion.HOI3;
//        } else if (gameResolver.getMainDirName().contains("Rome")) {
//            return GameVersion.ROME;
//        } else if (gameResolver.getMainDirName().contains("Magna")) {
//            return GameVersion.MAGNA_MUNDI;
//        } else if (gameResolver.getMainDirName().contains("Crusader")) {
//            return GameVersion.CK2;
//        } else if (gameResolver.getMainDirName().contains("Europa")) {
//            GenericObject mapData = EUGFileIO.load(gameResolver.resolveFilename("map/default.map"), ParserSettings.getQuietSettings());
//            if (mapData.getString("sea_starts").length() != 0)
//                return GameVersion.VANILLA; // vanilla had sea_starts = <province id> instead of sea_starts = { <list> }
//
//            GenericObject defines = EUGFileIO.load(gameResolver.resolveFilename("common/defines.txt"), ParserSettings.getQuietSettings());
//            if (defines.containsChild("startdate"))
//                return GameVersion.IN_NOMINE;
//
//            if (new java.io.File(gameResolver.resolveFilename("common/faction.txt")).exists())
//                return GameVersion.DIVINE_WIND;
//            else
//                return GameVersion.HEIR_TO_THE_THRONE;
//        } else {
//            System.out.println("Could not detect game version");
//            System.exit(-1);
//            return null;
//        }
//    }
}
