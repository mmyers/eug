
package posed;

import eug.parser.EUGFileIO;
import eug.shared.GenericObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class GameVersion {

    private static List<GameVersion> gameVersions;

    private String name;
    private String display;
    private boolean isMapInverted;
    private boolean hasSeaList;
    private GenericObject land;
    private GenericObject sea;

    private GameVersion() {
        land = new GenericObject();
        sea = new GenericObject();
    }

    private GameVersion(String name, String display, boolean isMapInverted, boolean hasSeaList, GenericObject land, GenericObject sea) {
        this.name = name;
        this.display = display;
        this.isMapInverted = isMapInverted;
        this.hasSeaList = hasSeaList;
        this.land = land;
        this.sea = sea;
    }

    private static void readVersions() {
        gameVersions = new ArrayList<GameVersion>();

        GenericObject allVersions = EUGFileIO.load("position_types.txt");

        for (GenericObject version : allVersions.children) {
            GameVersion newVersion = new GameVersion();
            if (version.hasString("inherit")) {
                GameVersion old = getByName(version.getString("inherit"));
                newVersion.isMapInverted = old.isMapInverted;
                newVersion.hasSeaList = old.hasSeaList;
                newVersion.land = old.land.clone();
                newVersion.sea = old.sea.clone();
            }

            newVersion.name = version.name;
            newVersion.display = version.getString("display");

            if (version.hasString("map_inverted"))
                newVersion.isMapInverted = version.getBoolean("map_inverted");
            if (version.hasString("has_sea_list"))
                newVersion.hasSeaList = version.getBoolean("has_sea_list");

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
