package eug.specific.victoria; // moved from victoria to eug.specific.victoria


import eug.parser.EUGFileIO;

import eug.shared.GenericObject;
import eug.shared.Scenario;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

//import victoria.fieldtype.ConstantListField;
//import victoria.fieldtypexml.Field;
//import victoria.xml.EditorTemplateXml;
//import victoria.xml.EditorTemplates;
//import victoria.xml.MappingTest;

/**
 * Main class. Its primary function is to centralised all the data needed on a scenario
 * so that having a reference to it allows other classes to interact with the full scenario.
 * In particular, provides:
 * - the root of the object tree
 * - quick access to various kinds of objects
 * - access to info loaded from the game's config files,
 * like constant lists
 */
public class VicScenario extends Scenario {
    
    String victoriaFolderPath;//,scenarioFilePath;
    List<GenericObject> populations;    //,openedFrames;
    Map<Integer, GenericObject> popMap;
    public Map<String, String> editorTemplates, constantPrefixes;
    public Map<String, List<String>> constantLists;
    public Map<String, Integer> countryColors;
    public int lastID;
    public String mapNames[];
    public int mapCoords[][];
    public int mapScale[];
    public char provLandType[];
    public char provVegType[];
    public int provCityCords[][];
    public String provState[];
//    public Vector mapFrames;
    
    public int lastPopID;
    
//    public ScenarioFrame frame;
    
    public VicScenario(GenericObject r,String n, String filePath) {
        
//        scenarioFilePath = filePath;
        victoriaFolderPath = "c:/program files/strategy first/victoria";
//        victoriaFolderPath = (new File(System.getProperty("user.dir"))).getParent();
        
        //mac game setup:
        if (new File(victoriaFolderPath+"/Victoria Data Files/config/world_names.csv").exists()) {
            victoriaFolderPath+="/Victoria Data Files";
            
            //PC game setup:
        } else if (new File(victoriaFolderPath+"/config/world_names.csv").exists()) {
            
        } else {
            JOptionPane.showMessageDialog(null, "Could not locate the Victoria game files. Please check that VictoriaEditor is installed in the Victoria folder.");
            System.exit(1);
        }
        
        root = r;
        scenarioName = n;
//        openedFrames = new Vector();
        lastID = 0;
        
        initConstants();
        initDisplayNames();
//        initEditors();
        initCountryColors();
        initMaps();
        initProvs();
        
//        mapFrames=new Vector();
        
        /*
         * The following methods are used to find commonly-used objects
         * for quick access later
         */
        setupProvinces();
        System.out.println(provinces.size()+" provinces.");
        setupPops();
        System.out.println(populations.size()+" populations.");
        setupCountries();
        System.out.println(countries.size()+" countries.");
        
    }
    
    private void initDisplayNames() {
        /*
         * TODO: make multi-platform
         */
        File f, files[] = new File[4];
        
        
        files[0] = new File(victoriaFolderPath+"/config/text.csv");
        files[1] = new File(victoriaFolderPath+"/config/world_names.csv");
        files[2] = new File(victoriaFolderPath+"/config/unit_names.csv");
        files[3] = new File(victoriaFolderPath+"/config/province_names.csv");
        
        BufferedReader br;
        String cL;
        displayNames = new HashMap<String, String>();
        /*
         * Reading the displayNames from the various Victoria files
         * containing them
         */
        for (int i = 0; i < files.length; i++) {
            f = files[i];
            try {
                br = new BufferedReader(new FileReader(f));
                
                while ((cL = br.readLine()) != null) {
                    if (cL.charAt(0) != '#') {
                        displayNames.put(cL.split(";")[0].toLowerCase(),cL.split(";")[1]);
                    }
                }
                
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }
    
    private void initCountryColors() {
        /*
         * TODO: make multi-plateform
         */
        File colorf, countryf;
        
        
        colorf = new File("config/colors.txt");
        
        Map<String, Integer> colors = new HashMap<String, Integer>();
        
        BufferedReader br;
        String cL;
        /*
         * Reading the color name - hexa value matches
         */
        try {
            br = new BufferedReader(new FileReader(colorf));
            
            cL = br.readLine();
            
            while (cL != null) {
                if (cL.charAt(0) != '#') {
                    colors.put(cL.split(",")[0].toLowerCase(),Integer.decode("0x"+cL.split(",")[1]));
                }
                
                cL = br.readLine();
            }
            
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        /*
         * Now reading the country file and filling the countryColors HashMap
         */
        
        countryColors = new HashMap<String, Integer>();
        countryf = new File(victoriaFolderPath+"/db/country.csv");
        
        try {
            br = new BufferedReader(new FileReader(countryf));
            
            while ((cL = br.readLine()) != null) {
                if (cL.charAt(0) != '#') {
                    countryColors.put(cL.split(";")[0].toLowerCase(),
                                      colors.get(cL.split(";")[1].toLowerCase()));
                }
            }
            
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private void initConstants() {
        constantLists = new HashMap<String, List<String>>();
        constantPrefixes = new HashMap<String, String>();
        
        try {
            BufferedReader br = new BufferedReader(new FileReader("config/constants.txt"));
            
            String currentLine, s, values[];
            
            List<String> list;
            
            currentLine = br.readLine();
            
            while (currentLine != null) {
                
                if (currentLine.charAt(0) != '#') {
                    s = currentLine.split(":")[0];//contsant list name
                    constantPrefixes.put(s,currentLine.split(":")[1]);//prefix
                    values = (currentLine.split(":")[2]).split(",");//list of possible values
                    list = new ArrayList<String>();
                    
                    for (int i = 0; i < values.length; i++)
                        list.add(values[i]);
                    
                    constantLists.put(s,list);
                }
                
                currentLine = br.readLine();
            }
            
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
//        /**
//         * TODO: Replace with XML loader
//         *
//         * This initialise the various EditorTemplates
//         * used the generate the editor classes
//         */
//    private void initEditors() {
//
//        MappingTest mt=new MappingTest();
//        EditorTemplates editors=mt.map();
//        EditorTemplate editor;
//        editorTemplates = new HashMap();
//        //EditorTemplate editor = ;
//        //Ajout
//        int i,j;
//        System.out.println("editors.getEditors().size(): "+editors.getEditors().size());
//
//        for (i=0;i<editors.getEditors().size();i++) {
//
//            editor=new EditorTemplate(this);
//            editor.setCode(editors.getEditor(i).getCode());
//            System.out.println("\nLoading editor: "+editor.getCode());
//            editor.setTLabel(editors.getEditor(i).getTLabel());
//            editor.setTValue(editors.getEditor(i).getTValue());
//            editor.setTDisplay(editors.getEditor(i).getTDisplay());
//            //editor.setFields(editors.getEditor(i).getFields().getFields());
//            int k = (int)( (EditorTemplateXml) editors.getEditor(i)).getFields().getFields().size();
//            System.out.println(k);
//            for (j=0; j<k; j++){
//
//                Field field=(Field)(editors.getEditor(i).getFields().getFields().get(j));
//
//                if(field.getStaticfield()!=null){
//                    if (field.getStaticfield().getFieldName() == null)
//                        editor.addStaticField(field.getStaticfield().label);
//                    else if (field.getStaticfield().getDisplayType() == null)
//                        editor.addStaticField(field.getStaticfield().label,field.getStaticfield().getFieldName());
//                    else
//                        editor.addStaticField(field.getStaticfield().label,field.getStaticfield().getFieldName(),field.getStaticfield().getDisplayType());
//                    //System.out.println("Static field: "+field.getStaticfield().label+" "+field.getStaticfield().getFieldName());
//                } else if(field.getStringfield()!=null){
//                    if (field.getStringfield().getDataType() == null)
//                        editor.addStringField(field.getStringfield().label,field.getStringfield().getFieldName());
//                    else
//                        editor.addStringField(field.getStringfield().label,field.getStringfield().getFieldName(),field.getStringfield().getDataType());
//                } else if (field.getEditorlauncherfield()!=null){
//                    editor.addEditorLauncher(field.getEditorlauncherfield().label,field.getEditorlauncherfield().getObject(),field.getEditorlauncherfield().getEditor());
//                } else if(field.getObjectlistfield()!=null){
//                    int n;
//                    String temp[] =new String[field.getObjectlistfield().getColDisplay().getConstant().size()];
//                    String temp1[]=new String[field.getObjectlistfield().getColType().getConstant().size()];
//                    for(n=0;n<field.getObjectlistfield().getColDisplay().getConstant().size();n++){
//                        temp[n]=(String)field.getObjectlistfield().getColDisplay(n);
//                        System.out.println(n);
//                        //System.out.println(temp.get(n));
//                    }
//                    for(n=0;n<field.getObjectlistfield().getColType().getConstant().size();n++){
//                        temp1[n]=(String)field.getObjectlistfield().getColType(n);
//                        System.out.println(n);
//                        //System.out.println(temp.get(n));
//                    }
//
//                    editor.addObjectListField(field.getObjectlistfield().label,field.getObjectlistfield().getObjectType(),temp,temp1,field.getObjectlistfield().getChildEditor(),field.getObjectlistfield().isCanCreate());
//                } else if(field.getProvcountryfield()!=null){
//                    editor.addProvCountryField();
//                } else if(field.getProvinceselectorfield()!=null){
//                    editor.addProvinceSelectorField(field.getProvinceselectorfield().label,field.getProvinceselectorfield().getField(),field.getProvinceselectorfield().getProvtype(),field.getProvinceselectorfield().getSelectTitle(),field.getProvinceselectorfield().getTitleField(),field.getProvinceselectorfield().getTitleFieldType());
//                } else if(field.getConstantlistfield()!=null){
//                    int n;
//                    Vector temp = new Vector(); //field.getConstantlistfield().getConstants().getConstant().size()];
//                    System.out.println(field.getConstantlistfield().getConstants().getConstant().size());
//                    //System.out.println(field.getConstantlistfield().getConstant(1));
//
//                    for(n=0;n<field.getConstantlistfield().getConstants().getConstant().size();n++){
//                        temp.addElement((String)field.getConstantlistfield().getConstant(n));
//                        System.out.println(n);
//                        //System.out.println(temp.get(n));
//                    }
//                    ConstantListField cons=new ConstantListField(this,field.getConstantlistfield().getLabel(),field.getConstantlistfield().getFieldName(),temp);
//                    editor.addField(cons);
//                    //editor.addConstantField(field.getConstantlistfield().getLabel(),field.getConstantlistfield().getFieldName(),temp);
//                }
//                //editor.addField((FieldType)editors.getEditor(i).getFields().getFields().get(j));
//                System.out.println("scenario");
//            }
//            System.out.println(editor.getTLabel());
//            editorTemplates.put(editor.getCode(),editor);
//            System.out.println("scenario");
//        }
//
//    }
//
//    public EditorFrame createEditor(String edType, String objType, GenericObject go) {
//        return ((EditorTemplate)editorTemplates.get(edType)).createEditor(go, objType);
//    }
    
    private void setupPops() {
        
        lastPopID = 0;
        
        populations = new ArrayList<GenericObject>(5000);
        popMap = new HashMap<Integer, GenericObject>(5000);
        
        //populations are stored in provinces
        for (GenericObject prov : provinces.values()) {
            for (GenericObject pop : prov.children) {
                if (pop.name.equals("pop")) {
                    populations.add(pop);
                    popMap.put(pop.getInt("id"), pop);
                    
                    if (pop.getInt("id") > lastPopID)
                        lastPopID = pop.getInt("id");
                    
                }
            }
        }
    }
    
    public List<String> getConstantList(String constant) {
        return constantLists.get(constant);
    }
    
    public String getConstantPrefix(String constant) {
        return constantPrefixes.get(constant);
    }
    
    public int getCountryColor(String tag) {
        return countryColors.get(tag.toLowerCase());
    }
    
    private void initMaps() {
        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader("config/maps.txt"));
            String currentLine = br.readLine();
            
            
            String tempName[] = new String[100];
            int tempPos[][] = new int[100][4];
            int tempScale[] = new int[100];
            
            int nb = 0;
            
            while (currentLine != null) {
                
                String[] sargs = currentLine.split(",");
                
                if (sargs.length == 6) {
                    tempName[nb] = sargs[0];
                    tempPos[nb][0] = Integer.parseInt(sargs[1]);
                    tempPos[nb][1] = Integer.parseInt(sargs[2]);
                    tempPos[nb][2] = Integer.parseInt(sargs[3]);
                    tempPos[nb][3] = Integer.parseInt(sargs[4]);
                    tempScale[nb] = Integer.parseInt(sargs[5]);
                    nb++;
                }
                currentLine = br.readLine();
            }
            
            mapNames = new String[nb];
            mapCoords = new int[nb][4];
            mapScale = new int[nb];
            
            
            for (int i = 0; i < nb; i++) {
                mapNames[i] = tempName[i];
                mapCoords[i][0] = tempPos[i][0];
                mapCoords[i][1] = tempPos[i][1];
                mapCoords[i][2] = tempPos[i][2];
                mapCoords[i][3] = tempPos[i][3];
                mapScale[i] = tempScale[i];
            }
            
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    private void initProvs() {
        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader("config/province.csv"));
            String currentLine = br.readLine();
            
            provCityCords = new int[3000][2];
            provLandType = new char[3000];
            provVegType = new char[3000];
            provState = new String[3000];
            
            for (int i = 0; i < 3000; i++) {
                provCityCords[i][0] = -1;
                provCityCords[i][1] = -1;
                provLandType[i] = ' ';
                provVegType[i] = ' ';
                provState[i] = null;
            }
            
            int id = -1;
            
            while (currentLine != null) {
                String[] sargs = currentLine.split(";");
                
                try {
                    if (currentLine.charAt(0) != 'g') {
                        id = Integer.parseInt(sargs[0]);
                        
                        if (id > 0) {
                            provState[id] = sargs[2];
                            provCityCords[id][0] = Integer.parseInt(sargs[13])/2;
                            provCityCords[id][1] = Integer.parseInt(sargs[14])/2;
                            
                            if (sargs[6].equals("Ocean"))
                                provLandType[id] = 'o';
                            else if (sargs[6].equals("Plains"))
                                provLandType[id] = 'p';
                            else if (sargs[6].equals("Broken"))
                                provLandType[id] = 'b';
                            else if (sargs[6].equals("Hills"))
                                provLandType[id] = 'h';
                            else if (sargs[6].equals("Mountain"))
                                provLandType[id] = 'm';
                            else if (sargs[6].equals("Marsh"))
                                provLandType[id] = 'a';
                            else
                                provLandType[id] = ' ';
                            
                            if (sargs[7].equals("Clear"))
                                provVegType[id] = 'c';
                            else if (sargs[7].equals("Steppe"))
                                provVegType[id] = 's';
                            else if (sargs[7].equals("Woodland"))
                                provVegType[id] = 'w';
                            else if (sargs[7].equals("Forest"))
                                provVegType[id] = 'f';
                            else if (sargs[7].equals("Frozen"))
                                provVegType[id] = 'z';
                            else if (sargs[7].equals("Desert"))
                                provVegType[id] = 'd';
                            else
                                provVegType[id] = ' ';
                        }
                    }
                } catch (Exception e) {
                    System.err.print("Error with "+id+": ");
                    e.printStackTrace();
                }
                currentLine = br.readLine();
            }
            
            br.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public String getLandTypeName(char c) {
        switch(c) {
            case 'o'://ocean
                return "ocean";
            case 'p'://plains
                return "plains";
            case 'b'://broken
                return "broken";
            case 'h'://hills
                return "hills";
            case 'm'://mountain
                return "mountains";
            case 'a'://marsh
                return "marshes";
        }
        return "";
    }
    
    public String getLandVegName(char c) {
        switch(c) {
            case 'c'://clear
                return "clear";
            case 's'://Steppe
                return "steppe";
            case 'w'://Woodland
                return "woodland";
            case 'f'://Forest
                return "forest";
            case 'z'://Frozen
                return "frozen";
            case 'd'://Desert
                return "desert";
        }
        return "";
    }
    
//<editor-fold defaultstate="collapsed" desc=" old code ">
//    public void refreshMapOwned() {
//        int i;
//        for (i=0;i<mapFrames.size();i++)
//            if (((MapFrame)mapFrames.get(i)).viewMode == 'o') {
//            ((MapFrame)mapFrames.get(i)).colorCountriesOwned();
//            ((MapFrame)mapFrames.get(i)).applyProvColors();
//            }
//    }
//
//    public void refreshMapControlled() {
//        int i;
//        for (i=0;i<mapFrames.size();i++)
//            if (((MapFrame)mapFrames.get(i)).viewMode == 'c') {
//            ((MapFrame)mapFrames.get(i)).colorCountriesControlled();
//            ((MapFrame)mapFrames.get(i)).applyProvColors();
//            }
//    }
//
//    public void refreshForeignObjects() {
//        int i;
//        for (i=0;i<openedFrames.size();i++)
//            ((EditorFrame)openedFrames.get(i)).refreshForeignObjects();
//    }
//
//    public JMenu generateWindowsMenu() {
//
//        JMenu windowsmenu = new JMenu("Windows");
//
//        JMenuItem tmpMenuItem = new JMenuItem("Scenario : "+scenarioName);
//        ActionListener alScenario = new ActionListener(){
//            public void actionPerformed(ActionEvent a){
//                ((ScenarioFrame)root.frame).toFront();
//            }
//        };
//        tmpMenuItem.addActionListener(alScenario);
//        windowsmenu.add(tmpMenuItem);
//
//        for(int i=0; i<openedFrames.size(); i++){
//            String name =((EditorFrame)openedFrames.get(i)).getTitle().toString();
//            final int pos = i;
//            tmpMenuItem = new JMenuItem(name);
//
//            ActionListener tmpAl = new ActionListener(){
//                public void actionPerformed(ActionEvent a){
//                    ((EditorFrame)openedFrames.get(pos)).toFront();
//                }
//            };
//
//            tmpMenuItem.addActionListener(tmpAl);
//            windowsmenu.add(tmpMenuItem);
//        }
//        for(int i=0; i<mapFrames.size(); i++){
//            String name =((MapFrame)mapFrames.get(i)).getTitle();
//            final int pos = i;
//            tmpMenuItem = new JMenuItem(name);
//
//            ActionListener tmpAl = new ActionListener(){
//                public void actionPerformed(ActionEvent a){
//                    ((MapFrame)mapFrames.get(pos)).toFront();
//                }
//            };
//            tmpMenuItem.addActionListener(tmpAl);
//
//            windowsmenu.add(tmpMenuItem);
//        }
//
//        return windowsmenu;
//    }
//
//    public void refreshWindowsMenus() {
//
//        JMenuBar menubar;
//        int i;
//
//        for (i=0;i<openedFrames.size();i++) {
//            menubar=((EditorFrame)openedFrames.get(i)).getJMenuBar();
//            menubar.remove(1);
//            menubar.add(generateWindowsMenu());
//            ((EditorFrame)openedFrames.get(i)).setJMenuBar(menubar);
//        }
//        for (i=0;i<mapFrames.size();i++) {
//            ((MapFrame)mapFrames.get(i)).getJMenuBar().remove(0);
//            ((MapFrame)mapFrames.get(i)).getJMenuBar().add(generateWindowsMenu());
//            ((MapFrame)mapFrames.get(i)).getJMenuBar().repaint();
//        }
//        //	frame.getJMenuBar().remove(1);
//        //	frame.getJMenuBar().add(generateWindowsMenu());
//        frame.repaint();
//
//    }
//</editor-fold>
    
    public static boolean openVicScenario() {
        
        JFileChooser fc = new JFileChooser();
//        ExtensionFileFiler filter = new ExtensionFileFiler();
//        filter.addExtension("eug");
//        filter.setDescription("Victoria scenarios");
//        fc.setFileFilter(filter);
        int res = fc.showOpenDialog(null);
        
        if(res != JFileChooser.APPROVE_OPTION){
            return false;
        }
        
        GenericObject root = EUGFileIO.load(fc.getSelectedFile());
        
//        VicScenario scenario =
                new VicScenario(root,fc.getSelectedFile().getName(), fc.getSelectedFile().getPath());
        
//            ScenarioFrame frame = new ScenarioFrame(scenario);
//            frame.show();
//            scenario.frame=frame;
        
        return true;
    }
    
    public static VicScenario openVicScenario(String filename) {
        GenericObject root = EUGFileIO.load(filename);
        return new VicScenario(root,filename, filename);
    }
    
    public VicCountry getCountry(String tag) {
        for (GenericObject ctry : countries)
            if (ctry.getString("tag").equals(tag))
                return new VicCountry(ctry, this);
        return null;
    }
    
    public VicCountry getCountry(int pos) {
        return new VicCountry(countries.get(pos), this);
    }
    
    public void addCountry(VicCountry c) {
        countries.add(c.go);
    }
    
    public void addSelectableCountry(VicCountry c) {
        addCountry(c);
        root.getChild("header").getList("selectable").add(c.getTag(), false);
    }
    
    public Province getProvince(int id) {
        if (provinces.containsKey(id))
            return new Province(provinces.get(id), this);

        String sid = String.valueOf(id);
        for (GenericObject prov : provinces.values()) {
            if (prov.getString("id").equals(sid))
                return new Province(prov, this);
        }
        System.err.println("Province " + sid + " cannot be found");
        return null;
    }
    
    private void setupCountries() {
        countries = new ArrayList<GenericObject>();
        for (GenericObject obj : root.children) {
            if (obj.name.equals("country")) {
                countries.add(obj);
            }
        }
        Collections.sort(countries, new Comparator<GenericObject>() {
            public int compare(GenericObject o1, GenericObject o2) {
                return getDisplayName((o1.getString("tag"))).compareTo(getDisplayName(o2.getString("tag")));
            }
        });
    }
    
    private void setupProvinces() {
        provinces = new HashMap<Integer, GenericObject>();
        for (GenericObject obj : root.getChildren("province")) {
            int id = obj.getInt("id");
            if (id < 0)
                System.err.println("A province has no id field");
            provinces.put(id, obj);
        }
    }
    
    public Population getPOP(int id) {
        return new Population(popMap.get(id), this);
    }
    
    
    public double getTotalPopulation() {
        double total = 0.0;
        for (GenericObject pop : populations) {
            total += pop.getDouble("size");
        }
        return total*100000.0;
    }
    
    
//    public static VicScenario createSkeletonScenario(String name) {
//        final GenericObject scen = new GenericObject();
//        
//        scen.addString("name", name, true);
//        
//        final GenericObject header = scen.createChild("header");
//        header.createChild("capital");  //blank
//        header.createChild("province"); //blank
//        header.createChild("total");    //blank
//        header.createChild("prestige"); //blank
//        header.createChild("military"); //blank
//        header.createChild("industry"); //blank
//        
//        final GenericObject startDate = header.createChild("startdate");
//        startDate.addString("year", Integer.toString(1836));
//        startDate.addString("month", "january");
//        startDate.addString("day", Integer.toString(0));
//        
//        final GenericObject endDate = header.createChild("enddate");
//        endDate.addString("year", Integer.toString(1920));
//        endDate.addString("month", "january");
//        endDate.addString("day", Integer.toString(0));
//        
//        header.createChild("selectable"); //blank
//        
//        final GenericObject globaldata = scen.createChild("globaldata");
//        
//        globaldata.addChild(startDate);
//        globaldata.addChild(endDate);
//        globaldata.createChild("worldmarket");  //blank
//        
//        return new VicScenario(scen, name, "c:/program files/strategy first/victoria/scenarios/"+name+".eug");
//    }
//    
//    public VicScenario createSkeletonScenario(String name, String skelFileName) {
//        final GenericObject scen = new GenericObject();
//        scen.addAllChildren(EUGFileIO.load(skelFileName));
//        
//        return new VicScenario(scen, name, "c:/program files/strategy first/victoria/scenarios/"+name+".eug");
//    }
}
