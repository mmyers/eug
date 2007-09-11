/*
 * TestMain.java
 *
 * Created on June 23, 2006, 8:15 PM
 */

package eug.test;

import eug.shared.Utilities;
import eug.specific.eu2.EU2Scenario;
import eug.specific.eu2.Event;
import eug.specific.eu2.EventDatabase;
import eug.specific.eu2.Province;
import eug.specific.victoria.VicScenario;
import java.io.File;
import javax.swing.JOptionPane;

/**
 *
 * @author Michael Myers
 */
public class TestMain {
    
    private static final File eu2SaveFolder =
            new File("C:/Program Files/Strategy First/Europa Universalis 2/AGCEEP/Scenarios/Save Games/");
    
    private static final File vicScenFolder =
            new File("C:/Program Files/Strategy First/Victoria/scenarios/");
    
    /** Creates a new instance of TestMain */
    private TestMain() { }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        long startTime = System.nanoTime();
        boolean eu2 = (
                JOptionPane.showConfirmDialog(
                null, "EU2? (select \"No\" for Victoria)", "Question", JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION
                );
        
        if (eu2) {
            EU2Scenario scen = EU2Scenario.openEU2Scenario(
                    Utilities.findNewestFile(eu2SaveFolder)
//                    args[0]
//                "C:/Program Files/Strategy First/Europa Universalis 2/AGCEEP/Scenarios/Save Games/Burgundy_September_1795.eug"
//                "C:/Documents and Settings/Michael/Desktop/Test.txt"
                    );
            
//        long midTime = System.nanoTime();
//        System.out.println("so far: " + (midTime-startTime));
            
            System.out.print("Total population of the world (excluding natives): ");
            System.out.println(Utilities.formatPop(scen.getTotalPopulation()));
            
            System.out.println("Total military strength of the world: "+scen.getTotalLandForces()+" land troops and "+scen.getTotalSeaForces()+" ships.");
            
            while (true) {
                String tag = javax.swing.JOptionPane.showInputDialog("Please input tag:");
                
                if (tag == null)
                    break;
                
                try {
                    scen.getCountry(tag).printInfo();
                } catch (NullPointerException ignored) {
                    continue;
                }
            }
            
            if (false) {
                while (true) {
                    String sid = javax.swing.JOptionPane.showInputDialog("Please input province ID:");
                    
                    if (sid == null)
                        break;
                    
                    Province p = scen.getProvince(Integer.parseInt(sid));
                    System.out.println();
                    System.out.println("Province "+p.getId()+" ("+p.getName()+
                            "): Religion is "+p.getReligion()+", culture is "+p.getCulture());
                    
                    System.out.println("Owner is "+p.getOwner().getName()+
                            ", controller is "+p.getController().getName());
                }
            }
            
            while (true) {
                String eid = javax.swing.JOptionPane.showInputDialog("Please enter event ID:");
                
                if (eid == null)
                    break;
                
                Event evt = EventDatabase.byId.get(Integer.parseInt(eid));
                if (evt == null) {
                    JOptionPane.showMessageDialog(null, "No event found with ID "+eid);
                    continue;
                }
                System.out.println(evt.go.toString());
                System.out.println();
                System.out.println("Triggered by: " + EventDatabase.triggeredBy(evt));
                System.out.println("Triggers: " + EventDatabase.triggers(evt));
                System.out.println("Prereqs: " + EventDatabase.prereqs(evt));
                System.out.println("Prereq of: " + EventDatabase.prereqOf(evt));
                System.out.println();
            }
            
//        scen.saveFile("C:/Documents and Settings/Michael/Desktop/Atest.txt");
            
            long endTime = System.nanoTime();
//        System.out.println("time: " + (endTime - midTime));
            System.out.println();
            System.out.println("total: " + (endTime - startTime) + " ns.");
        } else {
            VicScenario scen = VicScenario.openVicScenario(
                    Utilities.findNewestFile(vicScenFolder)
//                    args[4]
                    );
            
            System.out.print("Total population of the world (excluding natives): ");
            System.out.println(Utilities.formatPop(scen.getTotalPopulation()));
            
//            System.out.print("Germany's population: ");
//            System.out.println(EU2Country.popFormatter.format(scen.getCountry("GER").getTotalPop()));
            
            System.out.print("Belgium's population: ");
            System.out.println(Utilities.formatPop(scen.getCountry("BEL").getTotalPop()));
        }
    }
    
}
