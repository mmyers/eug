
package editor;

import editor.mapmode.*;
import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.ObjectVariable;
import eug.specific.ck2.CK2DataSource;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

/**
 *
 * @author Michael
 */
public class MapmodeBuilder {
    
    private static final java.util.logging.Logger log =
            java.util.logging.Logger.getLogger(MapmodeBuilder.class.getName());
    
    private static FilenameResolver resolver;
    private static MapPanel mapPanel;
    private static JLabel viewModeLabel;
    
    private static final ParserSettings defaultSettings =
            ParserSettings.getNoCommentSettings().setPrintTimingInfo(false);
    
    
    private MapmodeBuilder() { }
    
    
    public static void buildMapmodeMenu(JMenu viewMenu, MapPanel mapPanel, JLabel viewModeLabel, GameVersion version, FilenameResolver resolver, boolean isSavedGame) {
        MapmodeBuilder.mapPanel = mapPanel;
        MapmodeBuilder.viewModeLabel = viewModeLabel;
        MapmodeBuilder.resolver = resolver;
        
        log.log(Level.INFO, "Initializing mapmodes...");
        long startTime = System.currentTimeMillis();
        GenericObject allViews = EUGFileIO.load("views.txt", defaultSettings);
        GenericObject allColors = EUGFileIO.load("colors.txt", defaultSettings);

        GenericList views = allViews.getList(version.getViewSet());
        if (views == null) {
            log.log(Level.WARNING, "No view set specified.");
            log.log(Level.WARNING, "Defaulting to EU3");
            views = allViews.getList("eu3");
        }

        boolean exception = false;
        
        for (String viewName : views) {
            String view = viewName.toLowerCase();
            try {
                if (view.equals("provinces")) {
                    viewMenu.add(new ProvinceFilterAction());
                } else if (view.equals("countries")) {
                    viewMenu.add(new PoliticalFilterAction());
                    if (mapPanel.getDataSource().isSavedGame())
                        viewMenu.add(new PlayerCountriesFilterAction());
                } else if (view.equals("override-simple-terrain")) {
                    viewMenu.add(new SimpleTerrainFilterAction());
                } else if (view.equals("history-simple-terrain")) {
                    viewMenu.add(new HistorySimpleTerrainFilterAction());
                } else if (view.equals("titles")) {
                    JMenu menu = new JMenu("Titles");
                    for (TitleMode.TitleType t : TitleMode.TitleType.values())
                        menu.add(new TitleFilterAction(t));
                    viewMenu.add(menu);
                } else if (view.equals("de-jure-titles")) {
                    JMenu menu = new JMenu("De Jure Titles");
                    for (DeJureTitleMode.TitleType t : DeJureTitleMode.TitleType.values())
                        menu.add(new DeJureTitleFilterAction(t));
                    viewMenu.add(menu);
                } else if (view.equals("country-menu")) {
                    JMenu menu = new JMenu("Single country");
                    addCountryFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("continents-menu")) {
                    JMenu menu = new JMenu("Continents");
                    addContinentFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("regions-menu") && version.hasRegions()) {
                    JMenu menu = new JMenu("Regions");
                    addRegionFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("areas-menu") && version.hasRegions()) {
                    JMenu menu = new JMenu("Areas");
                    addAreaFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("province-areas-menu") && version.hasRegions()) {
                    JMenu menu = new JMenu("Geography");
                    try {
                        menu.add(new AllAreasFilterAction(AllAreasMapMode.GeographyType.AREAS));
                    } catch (Exception ex) {}
                    try {
                        menu.add(new AllAreasFilterAction(AllAreasMapMode.GeographyType.REGIONS));
                    } catch (Exception ex) {}
                    try {
                        menu.add(new AllAreasFilterAction(AllAreasMapMode.GeographyType.SUPER_REGIONS));
                    } catch (Exception ex) {}
                    try {
                        menu.add(new AllAreasFilterAction(AllAreasMapMode.GeographyType.CONTINENTS));
                    } catch (Exception ex) {}
                    viewMenu.add(menu);
                } else if (view.equals("superregions-menu") && version.hasRegions()) {
                    JMenu menu = new JMenu("Super regions");
                    addSuperRegionFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("provincegroups-menu") && version.hasRegions()) {
                    JMenu menu = new JMenu("Province groups");
                    addProvinceGroupFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("colonial-regions")) {
                    viewMenu.add(new ColonialRegionFilterAction());
                } else if (view.equals("climates-menu") && version.hasClimateTxt()) {
                    JMenu menu = new JMenu("Climates");
                    addClimateFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("province-religions")) {
                    viewMenu.add(new ProvReligionFilterAction());
                } else if (view.equals("country-religions")) {
                    viewMenu.add(new CtryReligionFilterAction());
                } else if (view.equals("religions")) {
                    viewMenu.add(new ReligionFilterAction());
                } else if (view.equals("pop-religions")) {
                    viewMenu.add(new PopReligionFilterAction());
                } else if (view.equals("pop-cultures")) {
                    viewMenu.add(new PopCultureFilterAction());
                } else if (view.equals("pop-types")) {
                    viewMenu.add(new PopTypeFilterAction());
                } else if (view.equals("religions-menu")) {
                    JMenu menu = new JMenu("Single religion");
                    addReligionFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("buildings-menu")) {
                    JMenu menu = new JMenu("Buildings");
                    addBuildingFilters(menu, allColors);
                    viewMenu.add(menu);
                } else if (view.equals("hoi3-buildings-menu")) {
                    JMenu menu = new JMenu("Buildings");
                    addHOI3BuildingFilters(menu, allColors);
                    viewMenu.add(menu);
                } else if (view.equals("old-cultures-menu")) {
                    JMenu menu = new JMenu("Cultures");
                    addOldCultureFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("new-cultures-menu")) {
                    JMenu menu = new JMenu("Cultures");
                    addNewCultureFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("rome-cultures-menu")) {
                    JMenu menu = new JMenu("Cultures");
                    addRomeCultureFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("province-cultures")) {
                    viewMenu.add(new ProvinceCultureFilterAction());
                } else if (view.equals("trade-goods")) {
                    viewMenu.add(new GoodsFilterAction());
                } else if (view.equals("victoria-trade-goods")) {
                    viewMenu.add(new GoodsFilterAction(true));
                } else if (view.equals("trade-goods-menu")) {
                    JMenu menu = new JMenu("Single trade good");
                    addGoodsFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("hoi3-goods-menu")) {
                    JMenu menu = new JMenu("Province production levels");
                    addHOI3GoodsFilters(menu, allColors);
                    viewMenu.add(menu);
                } else if (view.equals("victoria-goods-menu") && isSavedGame) {
                    JMenu menu = new JMenu("Single trade good");
                    addVictoriaGoodsFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("governments-menu")) {
                    JMenu menu = new JMenu("Governments");
                    addGovernmentFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("tech-groups-menu")) {
                    JMenu menu = new JMenu("Tech groups");
                    addTechGroupFilters(menu);
                    viewMenu.add(menu);
                } else if (view.equals("cots")) {
                    viewMenu.add(new CustomFilterAction("Centers of trade", "cot", "yes"));
                } else if (view.equals("tradenodes")) {
                    viewMenu.add(new TradeNodeFilterAction());
                } else if (view.equals("capitals")) {
                    viewMenu.add(new CapitalFilterAction());
                } else if (view.equals("hre")) {
                    viewMenu.add(new CustomFilterAction("Holy Roman Empire", "hre", "yes"));
                } else if (view.equals("base-tax")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Base tax value", "base_tax", 0, 18, 1);
                    actn.setStepColors(allColors, view, Color.RED.darker(), Color.GREEN.darker(), Color.BLUE);
                    viewMenu.add(actn);
                } else if (view.equals("base-production")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Base production", "base_production", 0, 18, 1);
                    actn.setStepColors(allColors, view, Color.RED.darker(), Color.GREEN.darker(), Color.BLUE);
                    viewMenu.add(actn);
                } else if (view.equals("base-manpower")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Base manpower", "base_manpower", 0, 18, 1);
                    actn.setStepColors(allColors, view, Color.RED.darker(), Color.GREEN.darker(), Color.BLUE);
                    viewMenu.add(actn);
                } else if (view.equals("population")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Population", "citysize", 0, 250000, 10000);
                    actn.setStepColors(allColors, view, Color.LIGHT_GRAY, null, Color.GREEN.darker());
                    viewMenu.add(actn);
                } else if (view.equals("rome-population")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Population", "population", 0, 100, 5);
                    actn.setStepColors(allColors, view, Color.LIGHT_GRAY, null, Color.GREEN.darker());
                    viewMenu.add(actn);
                } else if (view.equals("population-split")) {
                    viewMenu.add(new PopSplitFilterAction());
                } else if (view.equals("manpower")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Manpower", "manpower", 0, 10, 1);
                    actn.setStepColors(allColors, view);
                    viewMenu.add(actn);
                } else if (view.equals("revolt-risk")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Revolt risk", "revolt_risk", 0, 10, 1);
                    actn.setStepColors(allColors, view, Color.YELLOW, Color.ORANGE.darker(), Color.RED.darker());
                    viewMenu.add(actn);
                } else if (view.equals("unrest")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Unrest", "unrest", 0, 10, 1);
                    actn.setStepColors(allColors, view, Color.YELLOW, Color.ORANGE.darker(), Color.RED.darker());
                    viewMenu.add(actn);
                } else if (view.equals("life-rating")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Life rating", "life_rating", 0, 50, 5);
                    actn.setStepColors(allColors, view, Color.RED, null, Color.GREEN.darker());
                    viewMenu.add(actn);
                } else if (view.equals("civilization-value")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Civilization value", "civilization_value", 0, 100, 5);
                    actn.setStepColors(allColors, view);
                    viewMenu.add(actn);
                } else if (view.equals("barbarian-power")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Barbarian power", "barbarian_power", 0, 10, 1);
                    actn.setStepColors(allColors, view, Color.LIGHT_GRAY, null, Color.RED.darker());
                    viewMenu.add(actn);
                } else if (view.equals("leadership")) {
                    DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Leadership", "leadership", 0, 10, 1);
                    actn.setStepColors(allColors, view);
                    viewMenu.add(actn);
                } else if (view.equals("development")) {
                    viewMenu.add(new DevelopmentFilterAction(0, 30, 1));
                } else if (view.equals("ck3-development")) {
                    viewMenu.add(new CK3DevelopmentFilterAction());
                } else if (view.equals("hotspots-owner")) {
                    viewMenu.add(new HotspotFilterAction("owner", 20, 1));
                } else if (view.equals("hotspots-controller")) {
                    viewMenu.add(new HotspotFilterAction("controller", 40, 2));
                } else if (view.equals("natives-menu")) {
                    JMenu menu = new JMenu("Natives");
                    addNativesFilters(menu, allColors);
                    viewMenu.add(menu);
                } else if (view.equals("wars")) {
                    viewMenu.add(new WarsAction());
                } else if (view.equals("modding-menu")) {
                    JMenu menu = new JMenu("Mod-creating views");
                    if (!mapPanel.getDataSource().isSavedGame()) {
                        menu.add(new HistoryExistsFilterAction());
                        if (!(mapPanel.getDataSource() instanceof CK2DataSource))
                            menu.add(new CountryHistoryExistsFilterAction());
                        viewMenu.add(menu);
                    }
                } else if (view.equals("---")) {
                    viewMenu.add(new JSeparator());
                } else {
                    log.log(Level.WARNING, "Unknown menu item: {0}", view);
                }
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Error loading menu item {0}", view);
                log.log(Level.SEVERE, "The error information is: ", ex);
                exception = true; // instead of showing message boxes for each exception, show only one in case there's a cascade
            }
        }
        
        if (exception) {
            JOptionPane.showMessageDialog(null, "There was an error loading one of the view menu items. Please see the log for details.", "Problem", JOptionPane.WARNING_MESSAGE);
        }

        viewMenu.add(new JSeparator());
        viewMenu.add(new CustomMapModeAction());
        viewMenu.add(new CustomScalingMapModeAction());
        viewMenu.add(new PaintInternalBordersAction());
        viewMenu.add(new PaintExternalBordersAction());
        
        log.log(Level.INFO, "Done in {0} ms.", System.currentTimeMillis() - startTime);
    }
    
    
    // loadFileOrFolder and loadFolderUTF8 are duplicated in EditorUI and also more or less in editor.mapmode.Utilities.
    // Probably should put them in a better place.
    private static GenericObject loadFileOrFolder(String folderName, String fileName) {
        java.io.File folder = new java.io.File(resolver.resolveFilename(folderName));
        java.io.File file = new java.io.File(resolver.resolveFilename(fileName));
        if (folder.exists() && folder.isDirectory()) {
            return EUGFileIO.loadAll(resolver.listFiles(folderName), defaultSettings);
        } else if (file.exists()) {
            return EUGFileIO.load(file, defaultSettings);
        } else {
            log.log(Level.WARNING, "Could not load definition files from {0} or {1}", new Object[] { folderName, fileName });
            return null;
        }
    }
    
    private static GenericObject loadFolderUTF8(String folderName) {
        java.io.File folder = new java.io.File(resolver.resolveFilename(folderName));
        if (folder.exists() && folder.isDirectory()) {
            return EUGFileIO.loadAllUTF8(resolver.listFiles(folderName), defaultSettings);
        }
        return null;
    }
    
    private static final int MAX_PER_SUBMENU = 30;
    
    private static void addReligionFilters(JMenu rootMenu) {
        GenericObject religions = loadFileOrFolder("common/religions", "common/religion.txt");
        
        if (religions == null) {
            religions = loadFolderUTF8("common/religion/religions"); // CK3
            if (religions == null)
                return;
        }

        Collections.sort(religions.children, new ObjectComparator());
        
        final StringBuilder allReligions = new StringBuilder("(");
        
        for (GenericObject group : religions.children) {
            // CKII now includes religion visibility triggers in 00_religions.txt.
            // We don't need to know about triggers.
            if (group.name.contains("trigger"))
                continue;
            
            Collections.sort(group.children, new ObjectComparator());
            
            JMenu groupMenu = new JMenu(Text.getText(group.name));
            
            if (!group.children.isEmpty()){
                StringBuilder pattern = new StringBuilder(group.size()*10).append('(');

                for (GenericObject religion : group.children) {
                    if (religion.isEmpty())
                        continue;
                    groupMenu.add(new CustomFilterAction(Text.getText(religion.name), "religion", religion.name));
                    pattern.append(religion.name).append('|');
                    allReligions.append(religion.name).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');

                groupMenu.add(new MultiFilterAction("All " + Text.getText(group.name), "religion", pattern.toString()));
            }
            rootMenu.add(groupMenu);
        }
        
        allReligions.deleteCharAt(allReligions.length()-1); // get rid of the last '|'
        allReligions.append(')');
        
        rootMenu.add(new MultiFilterAction("All religions (province)", "religion", allReligions.toString()));
        rootMenu.add(new MultiCountryFilterAction("All religions (country)", "religion", allReligions.toString()));
    }
    
    private static void addBuildingFilters(JMenu rootMenu, GenericObject colors) {
        final GenericObject buildings = loadFileOrFolder("common/buildings", "common/buildings.txt");
        
        if (buildings == null)
            return;
        
        Collections.sort(buildings.children, new ObjectComparator());
        
        java.util.List<GenericObject> forts = new ArrayList<>();
        
        java.util.List<Action> capitalBuildings = new ArrayList<>();
        java.util.List<Action> manufactories = new ArrayList<>();
        java.util.List<Action> onePerCountry = new ArrayList<>();
        java.util.List<Action> other = new ArrayList<>();

        for (GenericObject building : buildings.children) {
            boolean isFort = building.hasString("fort_level");
            if (!isFort) {
                GenericObject mod = building.getChild("modifier");
                if (mod != null && mod.hasString("fort_level"))
                    isFort = true;
            }
            if (isFort) {
                forts.add(building);
                continue;
            }

            String display = Text.getText("building_" + building.name);
            if (display.equals("building_" + building.name))
                display = Text.getText(building.name);
            CustomFilterAction act = new CustomFilterAction(display, building.name, "yes");
            if (building.containsList("manufactory")) {
                manufactories.add(act);
            } else if (building.getBoolean("capital")) {
                capitalBuildings.add(act);
            } else if (building.getBoolean("one_per_country")) {
                onePerCountry.add(act);
            }  else {
                other.add(act);
            }
        }
        
        if (!manufactories.isEmpty()) {
            JMenu manufactoriesMenu = (JMenu) rootMenu.add(new JMenu("Manufactories"));
            for (Action a : manufactories)
                manufactoriesMenu.add(a);
        }
        if (!capitalBuildings.isEmpty()) {
            JMenu capitalBuildingsMenu = (JMenu) rootMenu.add(new JMenu("Capital buildings"));
            for (Action a : capitalBuildings)
                capitalBuildingsMenu.add(a);
        }
        if (!onePerCountry.isEmpty()) {
            JMenu uniqueMenu = (JMenu) rootMenu.add(new JMenu("Unique"));
            for (Action a : onePerCountry)
                uniqueMenu.add(a);
        }

        if (!forts.isEmpty()) {
            FortLevelFilterAction actn = new FortLevelFilterAction(forts);
            actn.setStepColors(colors, "fort-level", Color.YELLOW, Color.GREEN.darker(), Color.BLUE.darker());
            rootMenu.add(actn);
        }
        
        for (int i = 0; i < other.size(); i++) {
            rootMenu.add(other.get(i));
            if ((i+1) % MAX_PER_SUBMENU == 0)
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
        }
    }

    private static void addHOI3BuildingFilters(JMenu rootMenu, GenericObject colors) {
        // HOI3 buildings are on a scale of 0-10 in each province
        final GenericObject buildings =
                EUGFileIO.load(resolver.resolveFilename("common/buildings.txt"),
                defaultSettings);
        
        if (buildings == null)
            return;

        Collections.sort(buildings.children, new ObjectComparator());

        for (GenericObject building : buildings.children) {
            DiscreteStepFilterAction act = new DiscreteStepFilterAction(Text.getText(building.name), building.name, 0, 10, 1);
            act.setStepColors(colors, "buildings");
            rootMenu.add(act);
        }
    }
    
    @SuppressWarnings("unchecked") // array of lists, can't use generics
    private static void addCountryFilters(JMenu rootMenu) {
        final GenericObject countries = loadFileOrFolder("common/country_tags", "common/countries.txt");

        if (countries == null)
            return;
        
        Collections.sort(countries.values, new VariableComparator());
        
        @SuppressWarnings("rawtypes")
        List[] menus = new List[26];
        for (int i = 0; i < menus.length; i++) {
            menus[i] = new ArrayList<>();
        }
        List<JMenu> otherMenu = new ArrayList<>();
        
        for (ObjectVariable var : countries.values) {
            if (var.varname.equals("dynamic_tags"))
                continue;
            
            String cname = Text.getText(var.varname);
            if (cname == null || cname.isEmpty()) {
                log.log(Level.WARNING, "No localization found for country tag {0}", var.varname);
                cname = var.varname;
            }
            char start = Character.toUpperCase(cname.charAt(0));
            
            JMenu menu = new JMenu(cname);
            menu.add(new CustomFilterAction("Owned", "owner", var.varname));
            menu.add(new CustomFilterAction("Controlled", "controller", var.varname));
            menu.add(new CoreFilterAction(var.varname));
            menu.add(new ShowCountryAction(var.varname, Text.getText(var.varname)));
            
            int idx = start - 'A';
            if (idx >= 0 && idx < menus.length)
                menus[idx].add(menu); // unchecked
            else
                otherMenu.add(menu);
        }
        
        for (int i = 0; i < menus.length; i++) {
            JMenu menu = new JMenu("  " + (char)('A' + i) + "  ");
            rootMenu.add(menu);
            if (menus[i].isEmpty()) {
                JMenuItem dummy = new JMenuItem("(none)");
                dummy.setEnabled(false);
                menu.add(dummy);
            } else {
                int counter = 0;
                for (Object m : menus[i]) {
                    menu.add((JMenu)m);
                    if (counter++ > MAX_PER_SUBMENU) {
                        menu = (JMenu) menu.add(new JMenu("More..."));
                        counter = 0;
                    }
                }
            }
        }
        
        if (!otherMenu.isEmpty()) {
            JMenu menu = new JMenu("Other");
            rootMenu.add(menu);
            int counter = 0;
            for (JMenu m : otherMenu) {
                menu.add(m);
                if (counter++ > MAX_PER_SUBMENU) {
                    menu = (JMenu) menu.add(new JMenu("More..."));
                    counter = 0;
                }
            }
        }
    }
    
    private static void addOldCultureFilters(JMenu rootMenu) { // EU3 vanilla and NA
        final GenericObject cultures =
                EUGFileIO.load(resolver.resolveFilename("common/cultures.txt"),
                defaultSettings);
        
        if (cultures == null)
            return;
        
        Collections.sort(cultures.lists, new ListComparator());

        final Comparator<String> listSorter = new StringComparator();

        int counter = 0;
        for (GenericList group : cultures.lists) {
            group.sort(listSorter);

            JMenu groupMenu = new JMenu(Text.getText(group.getName()));

            if (group.size() > 0) {
                StringBuilder pattern = new StringBuilder(group.size()*10).append('(');

                for (String culture : group) {
                    groupMenu.add(new CustomFilterAction(Text.getText(culture), "culture", culture));
                    pattern.append(culture).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');

                groupMenu.add(new MultiFilterAction("All " + Text.getText(group.getName()), "culture", pattern.toString()));
            }

            rootMenu.add(groupMenu);
            if (counter++ > MAX_PER_SUBMENU) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }

    private static void addNewCultureFilters(JMenu rootMenu) { // In Nomine and newer, and also Victoria 2
        GenericObject cultures = loadFileOrFolder("common/cultures", "common/cultures.txt");
        
        if (cultures == null) {
            cultures = loadFolderUTF8("common/culture/cultures"); // CK3
            if (cultures == null)
                return;
        }

        final ObjectComparator comp = new ObjectComparator();
        Collections.sort(cultures.children, comp);

        int counter = 0;
        for (GenericObject group : cultures.children) {
            Collections.sort(group.children, comp);

            JMenu groupMenu = new JMenu(Text.getText(group.name));

            if (!group.children.isEmpty()) {
                StringBuilder pattern = new StringBuilder(group.size()*10).append('(');

                for (GenericObject culture : group.children) {
                    if (culture.name.equals("alternate_start"))
                        continue;
                    groupMenu.add(new CustomFilterAction(Text.getText(culture.name), "culture", culture.name));
                    pattern.append(culture.name).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');

                //groupMenu.add(new MultiFilterAction("All " + Text.getText(group.name), "culture", pattern.toString()));
                groupMenu.add(new CultureGroupFilterAction(Text.getText(group.name), group.name));
            }

            rootMenu.add(groupMenu);
            if (counter++ > MAX_PER_SUBMENU) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }

    private static void addRomeCultureFilters(JMenu rootMenu) {
        final GenericObject cultures =
                EUGFileIO.load(resolver.resolveFilename("common/cultures.txt"),
                defaultSettings);
        
        if (cultures == null)
            return;

        final ObjectComparator comp = new ObjectComparator();
        Collections.sort(cultures.children, comp);

        for (GenericObject group : cultures.children) {
            GenericList groupCultures = group.getList("culture");
            groupCultures.sort();

            JMenu groupMenu = new JMenu(Text.getText(group.name));

            if (!group.children.isEmpty()) {
                StringBuilder pattern = new StringBuilder().append('(');

                for (String culture : groupCultures) {
                    groupMenu.add(new CustomFilterAction(Text.getText(culture), "culture", culture));
                    pattern.append(culture).append('|');
                }

                pattern.deleteCharAt(pattern.length()-1); // get rid of the last '|'
                pattern.append(')');
                
                groupMenu.add(new MultiFilterAction("All " + Text.getText(group.name), "culture", pattern.toString()));
            }

            rootMenu.add(groupMenu);
        }
    }
    
    private static void addGoodsFilters(JMenu rootMenu) {
        final GenericObject goods = loadFileOrFolder("common/tradegoods", "common/tradegoods.txt");
        
        if (goods == null)
            return;
        
        Collections.sort(goods.children, new ObjectComparator());
        
        int counter = 0;
        for (GenericObject good : goods.children) {
            rootMenu.add(new CustomFilterAction(Text.getText(good.name), "trade_goods", good.name));
            if (counter++ > MAX_PER_SUBMENU) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }


    private static void addVictoriaGoodsFilters(JMenu rootMenu) {
        final GenericObject goods =
                EUGFileIO.load(resolver.resolveFilename("common/goods.txt"),
                defaultSettings);
        
        if (goods == null)
            return;

        Collections.sort(goods.children, new ObjectComparator());

        for (GenericObject group : goods.children) {
            JMenu groupMenu = new JMenu(Text.getText(group.name));
            rootMenu.add(groupMenu);
            Collections.sort(group.children, new ObjectComparator());

            int counter = 0;
            for (GenericObject good : group.children) {
                groupMenu.add(new CustomFilterAction(Text.getText(good.name), "trade_goods", good.name));
                if (counter++ > MAX_PER_SUBMENU) {
                    groupMenu = (JMenu) groupMenu.add(new JMenu("More..."));
                    counter = 0;
                }
            }
        }
    }

    private static void addHOI3GoodsFilters(JMenu rootMenu, GenericObject colors) {
        for (String good : new String[] { "metal", "energy", "rare_materials" }) {
            DiscreteStepFilterAction actn = new DiscreteStepFilterAction(Text.getText(good), good, 0, 30, 3);
            actn.setStepColors(colors, "resources");
            rootMenu.add(actn);
        }
    }
    
    private static void addGovernmentFilters(JMenu rootMenu) {
        final GenericObject governments = loadFileOrFolder("common/governments", "common/governments.txt");
        
        if (governments == null)
            return;
        
        Collections.sort(governments.children, new ObjectComparator());
        final StringBuilder allGovernments = new StringBuilder("(");
        if (governments.size() > MAX_PER_SUBMENU) {
            JMenu republicMenu = new JMenu("Republics");
            JMenu religiousMenu = new JMenu("Religious");
            JMenu normalMenu = new JMenu("Normal");
            
            JMenu tmpRepublic = republicMenu;
            JMenu tmpReligious = religiousMenu;
            JMenu tmpNormal = normalMenu;
            
            for (GenericObject government : governments.children) {
                allGovernments.append(government.name).append('|');
                FilterAction action =
                        new CustomCountryFilterAction(Text.getText(government.name), "government", government.name);
                
                if (government.getString("republican_name").equals("yes")) {
                    if (tmpRepublic.getMenuComponentCount() > MAX_PER_SUBMENU)
                        tmpRepublic = (JMenu)tmpRepublic.add(new JMenu("More..."));
                    tmpRepublic.add(action);
                } else if (government.getString("religion").equals("yes")) {
                    if (tmpReligious.getMenuComponentCount() > MAX_PER_SUBMENU)
                        tmpReligious = (JMenu)tmpReligious.add(new JMenu("More..."));
                    tmpReligious.add(action);
                } else {
                    if (tmpNormal.getMenuComponentCount() > MAX_PER_SUBMENU)
                        tmpNormal = (JMenu)tmpNormal.add(new JMenu("More..."));
                    tmpNormal.add(action);
                }
            }
            
            rootMenu.add(republicMenu);
            rootMenu.add(religiousMenu);
            rootMenu.add(normalMenu);
            
        } else if (!governments.isEmpty()) {
            for (GenericObject government : governments.children) {
                allGovernments.append(government.name).append('|');
                rootMenu.add(new CustomCountryFilterAction(Text.getText(government.name), "government", government.name));
            }
        } else {
            return;
        }
        
        allGovernments.deleteCharAt(allGovernments.length()-1); // get rid of the last '|'
        allGovernments.append(')');
        
        rootMenu.add(new MultiCountryFilterAction("All governments", "government", allGovernments.toString()));
    }
    
    private static void addTechGroupFilters(JMenu rootMenu) {
        GenericObject groups =
                EUGFileIO.load(resolver.resolveFilename("common/technology.txt"),
                defaultSettings);
        
        if (groups != null)
            groups = groups.getChild("groups");
        
        if (groups == null)
            return;
        
        if (!groups.values.isEmpty()) {
            Collections.sort(groups.values);
            for (ObjectVariable group : groups.values) {
                rootMenu.add(new CustomCountryFilterAction(Text.getText(group.varname), "technology_group", group.varname));
            }
        } else {
            // must be In Nomine
            Collections.sort(groups.children, new ObjectComparator());
            for (GenericObject group : groups.children) {
                rootMenu.add(new CustomCountryFilterAction(Text.getText(group.name), "technology_group", group.name));
            }
        }
    }
    
    private static void addContinentFilters(JMenu rootMenu) {
        List<String> contNames = new ArrayList<>(mapPanel.getMap().getContinents().keySet());
        Collections.sort(contNames, new StringComparator());
        for (String cont : contNames) {
            rootMenu.add(new ContinentFilterAction(Text.getText(cont), cont));
        }
    }
    
    private static void addClimateFilters(JMenu rootMenu) {
        rootMenu.add(new AllClimatesFilterAction(AllClimatesMapMode.ClimateType.ALL));
        if (mapPanel.getMap().getClimates().keySet().stream().anyMatch(k -> !k.contains("winter") && !k.equalsIgnoreCase("normal") && !k.equals("(none)")))
            rootMenu.add(new AllClimatesFilterAction(AllClimatesMapMode.ClimateType.CLIMATE));
        if (mapPanel.getMap().getClimates().keySet().stream().anyMatch(k -> k.contains("winter")))
            rootMenu.add(new AllClimatesFilterAction(AllClimatesMapMode.ClimateType.WINTER));
        if (mapPanel.getMap().getClimates().keySet().stream().anyMatch(k -> k.contains("monsoon")))
            rootMenu.add(new AllClimatesFilterAction(AllClimatesMapMode.ClimateType.MONSOON));
        List<String> climateNames = new ArrayList<>(mapPanel.getMap().getClimates().keySet());
        Collections.sort(climateNames, new StringComparator());
        for (String climate : climateNames) {
            rootMenu.add(new ClimateFilterAction(Text.getText(climate), climate));
        }
    }
    
    private static void addAreaFilters(JMenu rootMenu) {
        java.util.Map<String, List<Integer>> areas = mapPanel.getMap().getAreas();
        List<String> areaNames = new ArrayList<>(areas.keySet());
        Collections.sort(areaNames, new StringComparator());
        
        int counter = 0;
        for (String area : areaNames) {
            rootMenu.add(new AreaFilterAction(Text.getText(area), area));
            if (counter++ > MAX_PER_SUBMENU) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }
    
    private static void addRegionFilters(JMenu rootMenu) {
        java.util.Map<String, List<Integer>> regions = mapPanel.getMap().getRegions();
        List<String> regionNames = new ArrayList<>(regions.keySet());
        Collections.sort(regionNames, new StringComparator());
        
        int counter = 0;
        for (String region : regionNames) {
            rootMenu.add(new RegionFilterAction(Text.getText(region), region));
            if (counter++ > MAX_PER_SUBMENU) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }
    private static void addSuperRegionFilters(JMenu rootMenu) {
        java.util.Map<String, List<Integer>> superRegions = mapPanel.getMap().getSuperRegions();
        List<String> superRegionNames = new ArrayList<>(superRegions.keySet());
        Collections.sort(superRegionNames, new StringComparator());
        
        int counter = 0;
        for (String region : superRegionNames) {
            rootMenu.add(new SuperRegionFilterAction(Text.getText(region), region));
            if (counter++ > MAX_PER_SUBMENU) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }
    
    private static void addProvinceGroupFilters(JMenu rootMenu) {
        java.util.Map<String, List<Integer>> groups = mapPanel.getMap().getProvinceGroups();
        List<String> groupNames = new ArrayList<>(groups.keySet());
        Collections.sort(groupNames, new StringComparator());
        
        int counter = 0;
        for (String group : groupNames) {
            rootMenu.add(new ProvinceGroupFilterAction(Text.getText(group), group));
            if (counter++ > MAX_PER_SUBMENU) {
                rootMenu = (JMenu) rootMenu.add(new JMenu("More..."));
                counter = 0;
            }
        }
    }
    
    private static void addNativesFilters(JMenu rootMenu, GenericObject colors) {
        final GenericObject natives = loadFileOrFolder("common/natives", "common/natives.txt");
        
        if (natives == null)
            return;
        
        mapPanel.getMap().setNatives(natives);
        
        JMenu nativeTypesMenu = new JMenu("Native types");
        List<String> nativeNames = new ArrayList<>(mapPanel.getMap().getNatives().keySet());
        Collections.sort(nativeNames, new StringComparator());
        for (String nativeType : nativeNames) {
            nativeTypesMenu.add(new NativesFilterAction(Text.getText(nativeType), nativeType));
        }
        rootMenu.add(nativeTypesMenu);
        DiscreteStepFilterAction actn = new DiscreteStepFilterAction("Native size", "native_size", 0, 100, 5);
        actn.setStepColors(colors, "natives", Color.YELLOW, Color.ORANGE.darker(), Color.RED);
        rootMenu.add(actn);
        actn = new DiscreteStepFilterAction("Native ferocity", "native_ferocity", 0, 10, 1);
        actn.setStepColors(colors, "natives", Color.YELLOW, Color.ORANGE.darker(), Color.RED);
        rootMenu.add(actn);
        actn = new DiscreteStepFilterAction("Native hostility", "native_hostileness", 0, 10, 1);
        actn.setStepColors(colors, "natives", Color.YELLOW, Color.ORANGE.darker(), Color.RED);
        rootMenu.add(actn);
    }
    
    
    //
    // COMPARATORS
    //
    
    
    private static class ObjectComparator implements Comparator<GenericObject>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(final GenericObject o1, final GenericObject o2) {
            return Text.getText(o1.name).compareToIgnoreCase(Text.getText(o2.name));
        }
    }
    
    private static class ListComparator implements Comparator<GenericList>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(final GenericList o1, final GenericList o2) {
            return Text.getText(o1.getName()).compareToIgnoreCase(Text.getText(o2.getName()));
        }
    }
    
    private static class VariableComparator implements Comparator<ObjectVariable>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(final ObjectVariable o1, final ObjectVariable o2) {
            return Text.getText(o1.varname).compareToIgnoreCase(Text.getText(o2.varname));
        }
    }
    
    private static class StringComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public int compare(final String o1, final String o2) {
            return Text.getText(o1).compareToIgnoreCase(Text.getText(o2));
        }
    }
    
    
    //
    // ACTIONS
    //
    
    
    private abstract static class FilterAction extends AbstractAction {
        protected MapMode mode;
        protected FilterAction(String name, MapMode mode) {
            super(name);
            this.mode = mode;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            mapPanel.setMode(mode);
            viewModeLabel.setText((String) getValue(SHORT_DESCRIPTION));
            mapPanel.repaint();
        }
    }
    
    private static class CustomFilterAction extends FilterAction {
        public CustomFilterAction(String name, String key, String value) {
            super(name, new CustomMode(mapPanel, key, value));
            putValue(SHORT_DESCRIPTION, key + " = " + value);
        }
    }
    
    private static class CustomCountryFilterAction extends FilterAction {
        public CustomCountryFilterAction(String name, String key, String value) {
            super(name, new CustomCountryMode(mapPanel, key, value));
            putValue(SHORT_DESCRIPTION, key + " = " + value);
        }
    }
    
    private static class MultiFilterAction extends FilterAction {
        public MultiFilterAction(String name, String key, String pattern) {
            super(name, new AdvancedCustomMode(mapPanel, key, pattern));
            if (pattern.length() > 60)
                putValue(SHORT_DESCRIPTION, name);
            else
                putValue(SHORT_DESCRIPTION, key + " matches " + pattern);
        }
    }
    
    private static class MultiCountryFilterAction extends FilterAction {
        public MultiCountryFilterAction(String name, String key, String pattern) {
            super(name, new AdvancedCustomCountryMode(mapPanel, key, pattern));
            if (pattern.length() > 60)
                putValue(SHORT_DESCRIPTION, name);
            else
                putValue(SHORT_DESCRIPTION, key + " matches " + pattern);
        }
    }
    
    private static class ProvinceFilterAction extends FilterAction {
        public ProvinceFilterAction() {
            super("Provinces", new ProvinceMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Provinces");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('P', InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        }
    }
    
//    private static class CountryFilterAction extends FilterAction {
//        public CountryFilterAction() {
//            super("Countries", new CountryMode(mapPanel));
//            putValue(SHORT_DESCRIPTION, "Countries");
//        }
//    }
    
    private static class PoliticalFilterAction extends FilterAction {
        public PoliticalFilterAction() {
            super("Countries", new PoliticalMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Countries");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        }
    }
    
    private static class PlayerCountriesFilterAction extends FilterAction {
        public PlayerCountriesFilterAction() {
            super("Player countries", new IsPlayerMapMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Countries played by humans");
        }
    }

    private static class TitleFilterAction extends FilterAction {
        public TitleFilterAction(TitleMode.TitleType type) {
            super(type.getName() + " Titles", new TitleMode(mapPanel, type));
            putValue(SHORT_DESCRIPTION, type.getName() + " Titles");
        }
    }
    
    private static class DeJureTitleFilterAction extends FilterAction {
        public DeJureTitleFilterAction(DeJureTitleMode.TitleType type) {
            super("De Jure " + type.getName() + " Titles", new DeJureTitleMode(mapPanel, type));
            putValue(SHORT_DESCRIPTION, "De Jure " + type.getName() + " Titles");
        }
    }
    
    private static class SimpleTerrainFilterAction extends FilterAction {
        public SimpleTerrainFilterAction() {
            super ("Simple terrain", new SimpleTerrainMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Simple terrain");
        }
    }
    
    private static class HistorySimpleTerrainFilterAction extends FilterAction {
        public HistorySimpleTerrainFilterAction() {
            super ("Simple terrain", new HistorySimpleTerrainMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Simple terrain");
        }
    }
    
    private static class ProvReligionFilterAction extends FilterAction {
        public ProvReligionFilterAction() {
            super("Province religions", new ProvReligionMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Province religions");
        }
    }
    
    private static class CtryReligionFilterAction extends FilterAction {
        public CtryReligionFilterAction() {
            super("Country religions", new CtryReligionMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Country religions");
        }
    }
    
    private static class ReligionFilterAction extends FilterAction {
        public ReligionFilterAction() {
            super("Religions", new ReligionMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Religions");
        }
    }
    
    private static class ProvinceCultureFilterAction extends FilterAction {
        public ProvinceCultureFilterAction() {
            super("Province cultures", new ProvCultureMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Province cultures");
        }
    }
    
    private static class GoodsFilterAction extends FilterAction {
        public GoodsFilterAction() {
            this(false);
        }
        public GoodsFilterAction(boolean isVictoria) {
            super("Trade goods", new GoodsMode(mapPanel, isVictoria, resolver));
            putValue(SHORT_DESCRIPTION, "Trade goods");
        }
    }
    
    private static class CoreFilterAction extends FilterAction {
        public CoreFilterAction(String tag) {
            super("Core", new CoreMapMode(mapPanel, tag));
            putValue(SHORT_DESCRIPTION, "Cores of " + tag);
        }
    }

    private static class TradeNodeFilterAction extends FilterAction {
        public TradeNodeFilterAction() {
            super("Trade nodes", new TradeMode(mapPanel, resolver));
            putValue(SHORT_DESCRIPTION, "Trade nodes");
        }
    }
    
    private static class DiscreteStepFilterAction extends FilterAction {
        public DiscreteStepFilterAction(String name, String prop, int min, int max, int step) {
            super(name, new DiscreteScalingMapMode(mapPanel, prop, min, max, step));
            putValue(SHORT_DESCRIPTION, name);
        }

        protected DiscreteStepFilterAction(String name) {
            super(name, null);
            putValue(SHORT_DESCRIPTION, name);
        }

        private void setMinColor(Color c) {
            ((DiscreteScalingMapMode)mode).setMinColor(c);
        }

        private void setMidColor(Color c) {
            ((DiscreteScalingMapMode)mode).setMidColor(c);
        }

        private void setMaxColor(Color c) {
            ((DiscreteScalingMapMode)mode).setMaxColor(c);
        }

        public void setStepColors(GenericObject allColors, String name) {
            setStepColors(allColors, name, null, null, null);
        }

        public void setStepColors(GenericObject allColors, String name,
                Color defaultMin, Color defaultMid, Color defaultMax) {
            if (defaultMin != null)
                setMinColor(defaultMin);
            if (defaultMid != null)
                setMidColor(defaultMid);
            if (defaultMax != null)
                setMaxColor(defaultMax);

            GenericObject colors = allColors.getChild(name);
            if (colors != null) {
                GenericList min = colors.getList("low");
                if (min != null && min.size() >= 3) {
                    Color c = new Color(Integer.parseInt(min.get(0)), Integer.parseInt(min.get(1)), Integer.parseInt(min.get(2)));
                    setMinColor(c);
                }
                GenericList mid = colors.getList("middle");
                if (mid != null && mid.size() >= 3) {
                    Color c = new Color(Integer.parseInt(mid.get(0)), Integer.parseInt(mid.get(1)), Integer.parseInt(mid.get(2)));
                    setMidColor(c);
                }
                GenericList high = colors.getList("high");
                if (high != null && high.size() >= 3) {
                    Color c = new Color(Integer.parseInt(high.get(0)), Integer.parseInt(high.get(1)), Integer.parseInt(high.get(2)));
                    setMaxColor(c);
                }
            }
            ((DiscreteScalingMapMode)mode).setName(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // reload colors since they might have been changed by another
            // mapmode that uses the same settings
            GenericObject allColors = EUGFileIO.load("colors.txt", defaultSettings);
            GenericObject colors = allColors.getChild(((DiscreteScalingMapMode)mode).getName());
            if (colors != null)
                setStepColors(allColors, ((DiscreteScalingMapMode)mode).getName());

            super.actionPerformed(e);
        }
    }
    
    private static class FortLevelFilterAction extends DiscreteStepFilterAction {
        public FortLevelFilterAction(final java.util.List<GenericObject> forts) {
            super("Fort level");
            this.mode = new FortMapMode(mapPanel, forts);
        }
    }
    
//    private static class GroupFilterAction extends FilterAction {
//        public GroupFilterAction(String name, final java.util.List<String> group) {
//            super(name, new GroupMode(mapPanel, group));
//            putValue(SHORT_DESCRIPTION, "Provinces in " + name);
//        }
//    }
    
    private static class ContinentFilterAction extends FilterAction {
        public ContinentFilterAction(String name, String contName) {
            super(name, new ContinentMode(mapPanel, contName));
            putValue(SHORT_DESCRIPTION, "Provinces in " + contName);
        }
    }
    
    private static class ClimateFilterAction extends FilterAction {
        public ClimateFilterAction(String name, String climateName) {
            super(name, new ClimateMode(mapPanel, climateName));
            putValue(SHORT_DESCRIPTION, "Provinces with climate " + climateName);
        }
    }
    
    private static class AllClimatesFilterAction extends FilterAction {
        public AllClimatesFilterAction(AllClimatesMapMode.ClimateType type) {
            super(type.getReadableName(), new AllClimatesMapMode(mapPanel, type));
            putValue(SHORT_DESCRIPTION, type.getReadableName());
        }
    }
    
    private static class AreaFilterAction extends FilterAction {
        public AreaFilterAction(String name, String areaName) {
            super(name, new SingleAreaMode(mapPanel, areaName));
            putValue(SHORT_DESCRIPTION, "Provinces in " + areaName);
        }
    }
    
    private static class AllAreasFilterAction extends FilterAction {
        public AllAreasFilterAction(AllAreasMapMode.GeographyType type) {
            super("All " + type.getReadableName() + "s", new AllAreasMapMode(mapPanel, type));
            putValue(SHORT_DESCRIPTION, "All map " + type.getReadableName().toLowerCase() + "s");
        }
    }
    
    private static class RegionFilterAction extends FilterAction {
        public RegionFilterAction(String name, String regName) {
            super(name, new SingleRegionMode(mapPanel, regName));
            putValue(SHORT_DESCRIPTION, "Provinces in " + regName);
        }
    }
    
    private static class SuperRegionFilterAction extends FilterAction {
        public SuperRegionFilterAction(String name, String regName) {
            super(name, new SuperRegionMode(mapPanel, regName));
            putValue(SHORT_DESCRIPTION, "Provinces in " + regName);
        }
    }
    
    private static class ProvinceGroupFilterAction extends FilterAction {
        public ProvinceGroupFilterAction(String name, String groupName) {
            super(name, new ProvinceGroupMode(mapPanel, groupName));
            putValue(SHORT_DESCRIPTION, "Provinces in " + groupName);
        }
    }
    
    private static class ColonialRegionFilterAction extends FilterAction {
        public ColonialRegionFilterAction() {
            super("Colonial regions", new ColonialRegionsMode(mapPanel, resolver));
            putValue(SHORT_DESCRIPTION, "Colonial Regions & Trade Companies");
        }
    }
    
    private static class CapitalFilterAction extends FilterAction {
        public CapitalFilterAction() {
            super("Capitals", new CapitalsMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Capitals");
        }
    }
    
    private static class NativesFilterAction extends FilterAction {
        public NativesFilterAction(String name, String nativeType) {
            super(name, new NativeGroupMode(mapPanel, nativeType));
            putValue(SHORT_DESCRIPTION, "Provinces with " + nativeType + " natives");
        }
    }

    private static class PopSplitFilterAction extends FilterAction {
        public PopSplitFilterAction() {
            super("Population split", new PopSplitMapMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Population split");
        }
    }
    
    private static class PopReligionFilterAction extends FilterAction {
        public PopReligionFilterAction() {
            super("Pop religions", new PopReligionMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Majority religion in each province");
        }
    }
    
    private static class PopCultureFilterAction extends FilterAction {
        public PopCultureFilterAction() {
            super("Pop cultures", new PopCultureMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Majority culture in each province");
        }
    }
    
    private static class PopTypeFilterAction extends FilterAction {
        public PopTypeFilterAction() {
            super("Pop types", new PopTypeMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Majority pop type in each province");
        }
    }
    
    private static class HotspotFilterAction extends FilterAction {
        public HotspotFilterAction(String property, int max, int step) {
            super("Hot spots (" + property + ")", new HotspotMode(mapPanel, property, max, step));
            putValue(SHORT_DESCRIPTION, "Provinces which have changed hands the most");
        }
    }
    
    private static class HistoryExistsFilterAction extends FilterAction {
        public HistoryExistsFilterAction() {
            super("History files", new HistoryExistsMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Provinces with existing history files");
        }
    }
    
    private static class CountryHistoryExistsFilterAction extends FilterAction {
        public CountryHistoryExistsFilterAction() {
            super("Country history files", new CountryHistoryExistsMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Countries with existing history files");
        }
    }
    
    private static class CultureGroupFilterAction extends FilterAction {
        public CultureGroupFilterAction(String name, String cultureGroup) {
            super(name, new CultureGroupMode(mapPanel, cultureGroup));
            putValue(SHORT_DESCRIPTION, name + " culture group");
        }
    }
    
    private static class DevelopmentFilterAction extends FilterAction {
        public DevelopmentFilterAction(int min, int max, int step) {
            super("Total development level", new DevelopmentMapMode(mapPanel, min, max, step));
            putValue(SHORT_DESCRIPTION, "Total development level");
        }
    }
    
    private static class CK3DevelopmentFilterAction extends FilterAction {
        public CK3DevelopmentFilterAction() {
            super("Development level", new CK3DevelopmentMapMode(mapPanel));
            putValue(SHORT_DESCRIPTION, "Development level");
        }
    }
    
    
    private static class WarsAction extends AbstractAction {
        public WarsAction() {
            super("Wars...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            WarsDialog dlg = new WarsDialog(null, mapPanel.getDataSource(), resolver, mapPanel.getModel().getProvinceData());
            dlg.setVisible(true);
        }
    }
    

    private static class CustomMapModeAction extends AbstractAction {
        public CustomMapModeAction() {
            super("Custom map mode");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final MapMode mode = CustomModeDialog.showDialog(null);
            if (mode != null) {
                mode.setMapPanel(mapPanel);
                mapPanel.setMode(mode);
                viewModeLabel.setText(mode.toString());
                mapPanel.repaint();
            }
        }
    }
    
    private static class CustomScalingMapModeAction extends AbstractAction {
        public CustomScalingMapModeAction() {
            super("Custom scaling map mode");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final MapMode mode = CustomScalingModeDialog.showDialog(null);
            if (mode != null) {
                mode.setMapPanel(mapPanel);
                mapPanel.setMode(mode);
                viewModeLabel.setText(mode.toString());
                mapPanel.repaint();
            }
        }
    }

    private static class PaintInternalBordersAction extends AbstractAction {
        PaintInternalBordersAction() {
            super("Toggle internal borders");
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('B', InputEvent.CTRL_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            mapPanel.setPaintInternalBorders(!mapPanel.isPaintInternalBorders());
            mapPanel.repaint();
        }
    }

    private static class PaintExternalBordersAction extends AbstractAction {
        PaintExternalBordersAction() {
            super("Toggle external borders");
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('B', InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            mapPanel.setPaintExternalBorders(!mapPanel.isPaintExternalBorders());
            mapPanel.repaint();
        }
    }
    
    
    private static class ShowCountryAction extends AbstractAction {
        private final String tag;
        private final String name;
        public ShowCountryAction(String tag, String name) {
            super("Show history");
            this.tag = tag;
            this.name = name;
            putValue(SHORT_DESCRIPTION, "Edit " + name + "'s country history file");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            FileEditorDialog.showDialog(null, tag, name, resolver, mapPanel.getModel().getProvinceData());
        }
    }
}
