
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData;
import editor.Text;
import eug.shared.GenericList;
import eug.specific.clausewitz.ClausewitzSaveGame;
import java.awt.Color;

/**
 *
 * @author Michael
 */
public class IsPlayerMapMode extends CountryMode {
    
    private ClausewitzSaveGame saveGame;
    protected Color foundColor = Color.GREEN;
    protected Color notFoundColor = new Color(100, 100, 100); // darker than GRAY, lighter than DARK_GRAY

    public IsPlayerMapMode(MapPanel panel) {
        super(panel);
        if (mapPanel.getModel().getDataSource().isSavedGame()) {
            saveGame = (ClausewitzSaveGame) mapPanel.getModel().getDataSource();
        }
    }

    @Override
    protected Color getCtryColor(String country) {
        if (saveGame == null)
            return super.getCtryColor(country);
        
        if (Utilities.isNotACountry(country))
            return Utilities.COLOR_LAND_DEFAULT;
        else
            country = country.toUpperCase();
        
        boolean player = saveGame.getCountry(country).getBoolean("was_player");
        
        if (player) {
            return super.getCtryColor(country);
        } else {
            return notFoundColor;
        }
    }

    @Override
    protected Object getCountryBorderGroup(String country) {
        if (country == null || country.isEmpty() || Utilities.isNotACountry(country))
            return "NO_COUNTRY";

        country = country.toUpperCase();
        if (saveGame == null)
            return country;

        boolean player = saveGame.getCountry(country).getBoolean("was_player");
        if (player)
            return country;
        return "NOT_PLAYER";
    }
    

    @Override
    public String getTooltipExtraText(ProvinceData.Province current) {
        if (saveGame == null)
            return "This map mode can only be used with saved games.";
                    
        final int id = current.getId();
        if (!getMap().isLand(id))
            return "";
        
        String owner = mapPanel.getModel().getOwner(id);
        
        if (owner != null) {
            owner = owner.toUpperCase();
            if (!Utilities.isNotACountry(owner)) {
                boolean player = saveGame.getCountry(owner).getBoolean("was_player");
                if (player) {
                    GenericList players = saveGame.root.getList("players_countries");
                    for (int i = 0; i < players.size() - 1; i+=2) {
                        String playerName = players.get(i);
                        String playerTag = players.get(i+1);
                        if (playerTag.equals(owner)) {
                            return Text.getText(owner) + " played by " + playerName;
                        }
                    }
                    return Text.getText(owner) + " (unknown player)";
                }
            }
        }
        
        return "Not a player country";
    }
}
