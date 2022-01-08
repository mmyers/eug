
package eug.shared;

import eug.parser.EUGFileIO;
import java.io.File;
import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author Michael
 */
public class FilenameResolverModHandlerTest extends TestCase {
    
    public FilenameResolverModHandlerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNoModReturnsSameDirectory() {
        String baseDir = "C:" + File.separator + "testGame";
        String dirPath = "common";
        
        FilenameResolver resolver = new FilenameResolver(baseDir, "");
        String result1 = resolver.resolveDirectory(dirPath);
        
        assertEquals(baseDir + File.separator + dirPath + File.separator, result1);
        
        ModHandler handler1 = new EuropaModHandler(baseDir, "");
        String result2 = handler1.resolveDirectory(dirPath);
        
        assertEquals(result1, result2);
        
        ModHandler handler2 = new ClausewitzModHandler(baseDir, "");
        String result3 = handler2.resolveDirectory(dirPath);
        
        assertEquals(result1, result3);
        
        ModHandler handler3 = new Clausewitz2ModHandler(baseDir, "");
        String result4 = handler3.resolveDirectory(dirPath);
        
        assertEquals(result1, result4);
    }
    
    public void testEuropaModReturnsVanillaFileIfNotInMod() {
        String baseDir = "C:" + File.separator + "testGame";
        String modDir = "testMod";
        String filePath = "common" + File.separator + "countries.txt";
        
        FilenameResolver resolver = new FilenameResolver(baseDir, modDir);
        String result1 = resolver.resolveFilename(filePath);
        
        assertEquals(baseDir + File.separator + filePath, result1);
        
        ModHandler handler1 = new EuropaModHandler(baseDir, modDir);
        String result2 = handler1.resolveFilename(filePath);
        
        assertEquals(result1, result2);
    }
    
    public void testBothResolveSameFilesEU4() {
        String baseDirName = "C:\\Games\\Steam\\SteamApps\\common\\Europa Universalis IV";
        String modDirName = baseDirName + "\\mod\\MEIOUandTaxes";
        String modFileName = modDirName + ".mod";
        
        // only run this test locally, not on Github
        if (!(new File(baseDirName).exists()))
            return;
        
        FilenameResolver resolver = new FilenameResolver(baseDirName);
        resolver.setClausewitz2Mod(true);
        resolver.setModDirectory(modDirName);
        resolver.setModFileName(modFileName);
        ClausewitzModHandler handler = new Clausewitz2ModHandler(baseDirName, modFileName);
        
        //File baseDir = new File(baseDirName);
        
        //testFiles(baseDir, resolver, handler, "");
        testProvinceHistory(resolver, handler, 2795);
        testCountryHistory(resolver, handler);
    }
    
    public void testBothResolveSameFilesVic2() {
        String baseDirName = "C:\\Games\\Steam\\SteamApps\\common\\Victoria 2";
        String modDirName = baseDirName + "\\mod\\HFM";
        String modFileName = modDirName + ".mod";
        
        // only run this test locally, not on Github
        if (!(new File(baseDirName).exists()))
            return;
        
        FilenameResolver resolver = new FilenameResolver(baseDirName);
        resolver.setClausewitz2Mod(false);
        resolver.setModDirectory(modDirName);
        resolver.setModFileName(modFileName);
        ClausewitzModHandler handler = new ClausewitzModHandler(baseDirName, modFileName);
        
        //File baseDir = new File(baseDirName);
        
        //testFiles(baseDir, resolver, handler, "");
        testProvinceHistory(resolver, handler, 3275);
        testClausewitz1CountryHistory(resolver, handler);
    }
    
    private static void testFiles(File dir, FilenameResolver resolver, ModHandler handler, String parent) {
        for (File f : dir.listFiles()) {
            String name = f.getName();
            if (!"".equals(parent)) {
                name = parent + File.separator + name;
            }
            if (f.isDirectory() && !"mod".equals(f.getName()) && !"history".equals(f.getName())) {
                testFiles(f, resolver, handler, (!"".equals(parent) ? parent + File.separator : "") + f.getName());
                
                String fnName = resolver.resolveDirectory(name);
                String mhName = handler.resolveDirectory(name);
                System.out.println(name + " -D> " + fnName);
                assertEquals(fnName, mhName);
                
                File[] fnFiles = resolver.listFiles(name);
                File[] mhFiles = handler.listFiles(name);
                assertEquals(fnFiles.length, mhFiles.length);
            } else {
                String fnName = resolver.resolveFilename(name);
                String mhName = handler.resolveFilename(name);
                //System.out.println(name + " -> " + fnName);
                try {
                assertEquals(fnName, mhName);
                } catch (Error e) {
                    System.out.println("Discrepancy: " + fnName + " -> " + mhName);
                }
            }
        }
    }
    
    private static void testProvinceHistory(FilenameResolver resolver, ClausewitzModHandler handler, int numProvs) {
        for (int i = 1; i <= numProvs; i++) {
            String fnName = resolver.getVic2ProvinceHistoryFile(i);
            String mhName = handler.getVic2ProvinceHistoryFile(i);
            assertEquals(fnName, mhName);
        }
    }
    
    private static void testCountryHistory(FilenameResolver resolver, ClausewitzModHandler handler) {
        File[] countriesTxtFilenames = resolver.listFiles("common/country_tags");
        File[] countriesTxtFilenames2 = handler.listFiles("common/country_tags");
        assertEquals(countriesTxtFilenames2.length, countriesTxtFilenames.length);
        
        GenericObject countries = EUGFileIO.loadAll(countriesTxtFilenames);
        testCountryHistory(countries, resolver, handler);
    }
    
    private static void testClausewitz1CountryHistory(FilenameResolver resolver, ClausewitzModHandler handler) {
        String countriesTxtFilename = resolver.resolveFilename("common\\countries.txt");
        String countriesTxtFilename2 = handler.resolveFilename("common\\countries.txt");
        assertEquals(countriesTxtFilename2, countriesTxtFilename);
        
        GenericObject countries = EUGFileIO.load(countriesTxtFilename);
        testCountryHistory(countries, resolver, handler);
    }

    private static void testCountryHistory(GenericObject countries, FilenameResolver resolver, ClausewitzModHandler handler) {
        for (ObjectVariable countryDef : countries.values) {
            String fnName = resolver.getCountryHistoryFile(countryDef.varname);
            String mhName = handler.getCountryHistoryFile(countryDef.varname);
            System.out.println(countryDef.varname + " -> " + mhName);
            assertEquals(fnName, mhName);
        }
    }
}
