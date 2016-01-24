
package posed;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Michael
 */
public class GameVersion {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(GameVersion.class.getName());

    private static List<GameVersion> gameVersions;

    private String name;
    private String display;
    private boolean isMapInverted;
    private boolean hasSeaList;
    private boolean hasLakes;
    private String provinceLocFmt;
    private GenericObject land;
    private GenericObject sea;

    private GameVersion() {
        land = new GenericObject();
        sea = new GenericObject();
    }

    private static void readVersions() {
        gameVersions = new ArrayList<>();

        GenericObject allVersions = EUGFileIO.load("position_types.txt");

        for (GenericObject version : allVersions.children) {
            GameVersion newVersion = new GameVersion();
            if (version.hasString("inherit")) {
                GameVersion old = getByName(version.getString("inherit"));
                if (old != null) {
                    newVersion.isMapInverted = old.isMapInverted;
                    newVersion.hasSeaList = old.hasSeaList;
                    newVersion.hasLakes = old.hasLakes;
                    newVersion.provinceLocFmt = old.provinceLocFmt;
                    newVersion.land = old.land.clone();
                    newVersion.sea = old.sea.clone();
                } else {
                    log.log(Level.WARNING, "Invalid ''inherit'' directive: ''{0}''", version.getString("inherit"));
                }
            }

            newVersion.name = version.name;
            newVersion.display = version.getString("display");

            if (version.hasString("map_inverted"))
                newVersion.isMapInverted = version.getBoolean("map_inverted");
            if (version.hasString("has_sea_list"))
                newVersion.hasSeaList = version.getBoolean("has_sea_list");
            if (version.hasString("has_lakes"))
                newVersion.hasLakes = version.getBoolean("has_lakes");

            if (version.hasString("province_loc"))
                newVersion.provinceLocFmt = version.getString("province_loc");
            else
                newVersion.provinceLocFmt = "PROV%d";

            if (version.getChild("land") != null)
                newVersion.land = version.getChild("land");
            if (version.getChild("sea") != null)
                newVersion.sea = version.getChild("sea");

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
    
    public static GameVersion[] getGameVersionArray() {
        if (gameVersions == null)
            readVersions();
        
        GameVersion[] versions = new GameVersion[gameVersions.size()];
        return gameVersions.toArray(versions);
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isMapInverted() {
        return isMapInverted;
    }

    public boolean hasSeaList() {
        return hasSeaList;
    }

    public boolean hasLakes() {
        return hasLakes;
    }

    public String getProvinceLocFormat() {
        return provinceLocFmt;
    }

    public GenericObject getLand() {
        return land;
    }

    public GenericObject getSea() {
        return sea;
    }

    @Override
    public String toString() {
        return display;
    }
}
