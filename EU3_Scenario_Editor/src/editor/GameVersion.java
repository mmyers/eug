package editor;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericObject;

public enum GameVersion {

    VANILLA             (false, true,  false, true),
    IN_NOMINE           (true,  true,  true,  true),
    HEIR_TO_THE_THRONE  (true,  true,  true,  true),
    DIVINE_WIND         (true,  false, true,  true),
    ROME                (false, false, false, true),
    VICTORIA            (true,  true,  true,  false),
    HOI3                (true,  true,  true,  false);

    private boolean hasLandList;
    private boolean isMapUpsideDown;
    private boolean hasRegions;
    private boolean hasClimateTxt;

    private GameVersion(boolean hasLandList, boolean isMapUpsideDown, boolean hasRegions, boolean hasClimateTxt) {
        this.hasLandList = hasLandList;
        this.isMapUpsideDown = isMapUpsideDown;
        this.hasRegions = hasRegions;
        this.hasClimateTxt = hasClimateTxt;
    }

    public boolean isEU3() {
        switch (this) {
            case VANILLA:
            case IN_NOMINE:
            case HEIR_TO_THE_THRONE:
            case DIVINE_WIND:
                return true;
            default:
                return false;
        }
    }

    /** True if default.map has a list of sea provinces instead of a sea_starts number. */
    public boolean hasLandList() {
        return hasLandList;
    }

    public boolean isMapUpsideDown() {
        return isMapUpsideDown;
    }

    public boolean hasRegions() {
        return hasRegions;
    }

    public boolean hasClimateTxt() {
        return hasClimateTxt;
    }

    public static GameVersion detectGame(FilenameResolver gameResolver) {
        if (gameResolver.getMainDirName().contains("Victoria")) {
            return GameVersion.VICTORIA;
        } else if (gameResolver.getMainDirName().contains("Hearts")) {
            return GameVersion.HOI3;
        } else if (gameResolver.getMainDirName().contains("Rome")) {
            return GameVersion.ROME;
        } else if (gameResolver.getMainDirName().contains("Europa")) {
            GenericObject mapData = EUGFileIO.load(gameResolver.resolveFilename("map/default.map"), ParserSettings.getQuietSettings());
            if (mapData.getString("sea_starts").length() != 0)
                return GameVersion.VANILLA; // vanilla had sea_starts = <province id> instead of sea_starts = { <list> }

            GenericObject defines = EUGFileIO.load(gameResolver.resolveFilename("common/defines.txt"), ParserSettings.getQuietSettings());
            if (defines.containsChild("startdate"))
                return GameVersion.IN_NOMINE;

            if (new java.io.File(gameResolver.resolveFilename("common/faction.txt")).exists())
                return GameVersion.DIVINE_WIND;
            else
                return GameVersion.HEIR_TO_THE_THRONE;
        } else {
            System.out.println("Could not detect game version");
            System.exit(-1);
            return null;
        }
    }
}
