/*
 * Event.java
 *
 * Created on July 14, 2006, 2:04 PM
 */

package eug.specific.eu2;

import eug.shared.GenericObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Myers
 */
public class Event extends EU2SpecificObject {
    /*
    private static Map<String, CommandText> cmdText = new HashMap<String, CommandText>(128);
    
    private static final int STRING = 1;
    private static final int INT = 2;
    private static final int DOUBLE = 3;
    private static final int SIGN = 4;
    private static final int PROV = 5;
    private static final int TAG = 6;
    
    private static CommandText cmd(String text, int... format) {
        return new CommandText(text, format);
    }
    
    static {
        cmdText.put("desertion",        cmd("EE_DESERTIONS", DOUBLE, PROV)); //;Lose %d men in %s.;;;;;;;;;;7734
        cmdText.put("provinceculture",  cmd("EE_P_CUL", PROV, STRING)); //;Culture in %s changed to %s;;;;;;;;;;7737
        cmdText.put("provincetax",      cmd("EE_P_TAX", PROV, SIGN, INT)); //;Basic Taxvalue in %s:  %s%d;;;;;;;;;;7738
        cmdText.put("provincemanpower", cmd("EE_P_MAN", PROV, STRING, INT)); //;Manpower in %s: %s%d;;;;;;;;;;7739
        cmdText.put("mine",             cmd("EE_P_MINE", PROV, SIGN, INT)); //;Mine in %s: %s%d;;;;;;;;;;7740
        cmdText.put("natives",          cmd("EE_NATIVE", PROV, INT)); //;Native Aggressiveness in %s set to level %d;;;;;;;;;;7741
        cmdText.put("fortress",         cmd("EE_FORT", PROV, SIGN, INT)); //;Fortress in %s: %s%d;;;;;;;;;;7742
        cmdText.put("population",       cmd("EE_POP", PROV, SIGN, INT)); //;Population in %s: %s%d;;;;;;;;;;7743
        cmdText.put("revolt",           cmd("EE_N_REVOLT", PROV)); //;Revolt in %s.;;;;;;;;;;7744
        cmdText.put("religiousrevolt",  cmd("EE_R_REVOLT", PROV)); //;Revolt in %s.;;;;;;;;;;7745
        cmdText.put("colonialrevolt",   cmd("EE_C_REVOLT", PROV)); //;Revolt in %s.;;;;;;;;;;7746
        cmdText.put("heretic",          cmd("EE_HERETICS", PROV)); //;Heresy in %s;;;;;;;;;;7747
        cmdText.put("conversion",       cmd("EE_CONVERTED", PROV)); //;%s converts to the state religion.;;;;;;;;;;7748
        cmdText.put("province_revoltrisk", cmd("EE_P_RR", PROV, SIGN, INT)); //;Revoltrisk in %s:  %s%d;;;;;;;;;;7749
        cmdText.put("provincereligion", cmd("EE_P_REL", PROV, STRING)); //;%s converts to %s.;;;;;;;;;;7750
        cmdText.put("adm",              cmd("EE_P_ADM", INT, INT)); //;Monarchs administrative skill changed by %d for the next %d months.;;;;;;;;;;7751
        cmdText.put("dip",              cmd("EE_P_DIP", INT, INT)); //;Monarchs diplomatic skill changed by %d for the next %d months.;;;;;;;;;;7752
        cmdText.put("mil",              cmd("EE_P_MIL", INT, INT)); //;Monarchs military skill changed by %d for the next %d months.;;;;;;;;;;7753
        cmdText.put("religion",         cmd("EE_S_REL", STRING)); //;State religion changed to %s.;;;;;;;;;;7754
        cmdText.put("revoltrisk",       cmd("EE_RRISK", SIGN, INT, INT)); //;Revoltrisk changed by %s%d for the next %d months.;;;;;;;;;;7755
        cmdText.put("inflation",        cmd("EE_INFLATION", SIGN, INT)); //;Inflation: %s%d;;;;;;;;;;7756
        cmdText.put("breakdynastic",    cmd("EE_BROY", TAG)); //;Royal Marriage with %s broken.;;;;;;;;;;7757
        cmdText.put("breakvassal",      cmd()"EE_BVAS"); //;Vassalisation with %s broken.;;;;;;;;;;7758
        cmdText.put("dynastic", "EE_ROY"); //;Gain a Royal Marriage with %s.;;;;;;;;;;7759
        cmdText.put("vassal", "EE_VAS"); //;Gain %s as vassals.;;;;;;;;;;7760
        cmdText.put("alliance", "EE_ALL"); //;Alliance with %s.;;;;;;;;;;7761
        cmdText.put("inherit", "EE_INHERIT"); //;Inherit the realms of %s.;;;;;;;;;;7762
        cmdText.put("cash", "EE_CASH"); //;%s%d$;;;;;;;;;;7763
        cmdText.put("treasury", "EE_CASH"); //;%s%d$;;;;;;;;;;7763
        cmdText.put("manpower", "EE_MANPOWER"); //;Manpower: %s%d;;;;;;;;;;7764
        cmdText.put("land", "EE_LANDTECH"); //;Land Investment: %s%d;;;;;;;;;;7765
        cmdText.put("naval", "EE_NAVALTECH"); //;Naval Investment: %s%d;;;;;;;;;;7766
        cmdText.put("infra", "EE_INFRATECH"); //;Infra Investment: %s%d;;;;;;;;;;7767
        cmdText.put("trade", "EE_TRADETECH"); //;Trade Investment: %s%d;;;;;;;;;;7768
        cmdText.put("stability", "EE_STABILITY"); //;Stability: %s%d;;;;;;;;;;7769
        cmdText.put("diplomats", "EE_DIPLOMAT"); //;Diplomats: %s%d;;;;;;;;;;7770
        cmdText.put("colonists", "EE_COLONIST"); //;Colonists: %s%d;;;;;;;;;;7771
        cmdText.put("merchants", "EE_MERCHANT"); //;Merchants: %s%d;;;;;;;;;;7772
        cmdText.put("missionaries", "EE_MISSIONARY"); //;Missionaries: %s%d;;;;;;;;;;7773
        cmdText.put("conquistador", "EE_CONQUISTADOR"); //;Gain conquistador in %s.;;;;;;;;;;7774
        cmdText.put("explorer", "EE_EXPLORER"); //;Gain explorer in %s.;;;;;;;;;;7775
        cmdText.put("leader", "EE_LEADER"); //;Gain the service of %s.;;;;;;;;;;7776
        cmdText.put("wakeleader", "EE_LEADER"); //;Gain the service of %s.;;;;;;;;;;7776
        cmdText.put("monarch", "EE_MONARCH"); //;Make %s the leader of your country.;;;;;;;;;;7777
        cmdText.put("wakemonarch", "EE_MONARCH"); //;Make %s the leader of your country.;;;;;;;;;;7777
        cmdText.put("sleepleader", "EE_SLEADER"); //;Forever lose the service of %s.;;;;;;;;;;7778
        cmdText.put("sleepmonarch", "EE_SMONARCH"); //;Make %s forgotten!;;;;;;;;;;7779
        cmdText.put("country", "EE_TAG"); //;Found the realms of %s;;;;;;;;;;7780
        cmdText.put("relation", "EE_RELATION"); //;Relation with %s: %s%d;;;;;;;;;;7781
        cmdText.put("gainmanufactory", "EE_GAIN_MANU"); //;Gain manufactory in %s;;;;;;;;;;7782
        cmdText.put("losemanufactory", "EE_LOSE_MANU"); //;Lose Manufactory in %s;;;;;;;;;;7783
        cmdText.put("inf", "EE_INFANTRY"); //;Gain %d infantry in %s.;;;;;;;;;;7784
        cmdText.put("infantry", "EE_INFANTRY"); //;Gain %d infantry in %s.;;;;;;;;;;7784
        cmdText.put("cav", "EE_CAVALRY"); //;Gain %d cavalry in %s;;;;;;;;;;7785
        cmdText.put("cavalry", "EE_CAVALRY"); //;Gain %d cavalry in %s;;;;;;;;;;7785
        cmdText.put("art", "EE_ARTILLERY"); //;Gain %d artillery in %s;;;;;;;;;;7786
        cmdText.put("artillery", "EE_ARTILLERY"); //;Gain %d artillery in %s;;;;;;;;;;7786
        cmdText.put("warships", "EE_WARSHIPS"); //;Gain %d warships in %s;;;;;;;;;;7787
        cmdText.put("galleys", "EE_GALLEYS"); //;Gain %d galleys in %s;;;;;;;;;;7788
        cmdText.put("transports", "EE_TRANSPORTS"); //;Gain %d transports in %s;;;;;;;;;;7789
        cmdText.put("trigger", "EE_TRIGGER"); //;Trigger the '%s' Event.;;;;;;;;;;7790
        cmdText.put("sleepevent", "EE_SLEEP"); //;The '%s' Event will never occur.;;;;;;;;;;7791
        cmdText.put("vp", "EE_VP"); //;%s%d victory points.;;;;;;;;;;7792
        cmdText.put("casusbelli", "EE_CB"); //;Gain a casus belli against %s for %d months.;;;;;;;;;;7793
        cmdText.put("capital", "EE_CAPITAL"); //;Move national capital to %s.;;;;;;;;;;7794
//        EE_BANK;Create a national bank.;;;;;;;;;;7795
//        EE_STOCK;Create a national stock market.;;;;;;;;;;7796
        cmdText.put("addcore", "EE_ADDCORE"); //;%s will be considered one of your national provinces.;;;;;;;;;;7797
        cmdText.put("removecore", "EE_REMCORE"); //;%s will no longer be your national province.;;;;;;;;;;7798
        cmdText.put("cot", "EE_COT"); //;Center of Trade created in %s.;;;;;;;;;;7799
        cmdText.put("secedeprovince", "EE_SECEDE"); //;Secede %s to %s.;;;;;;;;;;7800
        cmdText.put("add_countryculture", "EE_A_CUL"); //;Gain %s as a state culture.\n;;;;;;;;;;7801
        cmdText.put("remove_countryculture", "EE_R_CUL"); //;Lose %s as a state culture.\n;;;;;;;;;;7802
        cmdText.put("loansize", "EE_LOAN"); //;Size of loans will now be %d$\n;;;;;;;;;;7803
        cmdText.put("gainbuilding", "EE_GAIN_BUILDING"); //;Gain a %s in %s;;;;;;;;;;7804
        cmdText.put("losebuilding", "EE_LOSE_BUILDING"); //;Lose %s in %s;;;;;;;;;;7805
        cmdText.put("war", "EE_WAR"); //;War with %s.;;;;;;;;;;7806
        cmdText.put("independence", "EE_INDY"); //;Grant Independence to %s;;;;;;;;;;7807
        cmdText.put("technology", "EE_TECH"); //;Switch Technology group to %s;;;;;;;;;;7808
//        #;;;;;;;;;;;7809
        cmdText.put("flag0", "EE_FLAG_0"); //;Enable Treaty of Tordesillas rules.;;;;;;;;;;7810
        cmdText.put("flag1", "EE_FLAG_1"); //;Enable Protestant religion;;;;;;;;;;7811
        cmdText.put("flag2", "EE_FLAG_2"); //;Enable Reformed religion;;;;;;;;;;7812
        cmdText.put("flag3", "EE_FLAG_3"); //;Enable Counter-reform Catholicism.;;;;;;;;;;7813
        cmdText.put("flag4", "EE_FLAG_4"); //;Disable Treaty of Tordesillas.\r\nReduce the effect of Religion.;;;;;;;;;;7814
//        EE_FLAG_5;Use the Tricolor.;;;;;;;;;;7815
//        EE_FLAG_6;Use Union Jack;;;;;;;;;;7816
        cmdText.put("badboy", "EE_REPUTATION"); //;%s%d Badboy;;;;;;;;;;12861
        cmdText.put("removecot", "EE_RCOT"); //;Lose Center of Trade in %s.;;;;;;;;;;12868
        
        cmdText.put("setflag", ""); // no text
        cmdText.put("clrflag", ""); // no text
        cmdText.put("ai", ""); // no text
        cmdText.put("flagname", ""); // no text, RIGHT???
    }
     */
    
//    private Trigger trigger;
//    
//    private Action action_a;
//    private Action action_b;
//    private Action action_c;
//    private Action action_d;
    
    /** Creates a new instance of Event */
    public Event(GenericObject go, EU2Scenario s) {
        super(go, s);
        
//        trigger = new Trigger(go, s, this);
//        
//        action_a = new Action(go.getChild("action_a"), s, this);
//        
//        GenericObject tmp = go.getChild("action_b");
//        
//        if (tmp != null) {
//            action_b = new Action(tmp, s, this);
//            
//            tmp = go.getChild("action_c");
//            if (tmp != null) {
//                action_c = new Action(tmp, s, this);
//                
//                tmp = go.getChild("action_d");
//                if (tmp != null)
//                    action_d = new Action(tmp, s, this);
//            }
//        }
    }
    
    public int getId() {
        return go.getInt("id");
    }
    
    public String getCountry() {
        return go.getString("country");
    }
    
    public int getProvince() {
        return go.getInt("province");
    }
    
    public boolean isRandom() {
        return go.getString("random").equals("yes");
    }
    
    public String getName() {
        return go.getString("name");
    }
    
    public String getDesc() {
        return go.getString("desc");
    }
    
    public String getDisplayName() {
        return scenario.getDisplayName(go.getString("name"));
    }
    
    public String getDisplayDesc() {
        return scenario.getDisplayName(go.getString("desc"));
    }
    
    public String toString() {
        return go.toString();
    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    
    
//    private static class Action extends EU2SpecificObject {
//        private Event owner;
//        private List<Command> commands = new ArrayList<Command>();
//        
//        Action(GenericObject go, EU2Scenario s, Event owner) {
//            super(go, s);
//            this.owner = owner;
//            initCommands();
//        }
//        
//        private void initCommands() {
//            for (GenericObject cmd : go.getChildren("command"))
//                commands.add(new Command(cmd, scenario, owner));
//        }
//        
//        public String getName() {
//            return scenario.getDisplayName(go.getString("name"));
//        }
//    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    
    
//    private static class Command extends EU2SpecificObject {
//        private Event owner;
//        
//        Command(GenericObject go, EU2Scenario s, Event owner) {
//            super(go, s);
//            this.owner = owner;
//            
//            if (getType().equals("")) // empty command
//                return;
//            
////            if (cmdText.get(getType().toLowerCase()) == null &&
////                    !getType().equalsIgnoreCase("domestic") && cmdText.get(getType()+getWhich()) == null)
////                System.err.println("Error: Unknown command type '" + getType() + "'");
//            
//            if (getType().equals("trigger"))
//                EventDatabase.addTrigger(owner.getId(), go.getInt("which"));
//        }
//        
//        public String getType() {
//            return go.getString("type");
//        }
//        
//        public String getValue() {
//            return go.getString("value");
//        }
//        
//        public String getWhich() {
//            return go.getString("which");
//        }
//        
////        public String getDisplayString() {
////            String displayName = this.scenario.getDisplayName(cmdText.get(getType().toLowerCase()));
////            
//////            int numString = displayName.
//////            if (displayName.contains("%s"))
////        }
//    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    
    
//    private static class Trigger extends EU2SpecificObject {
//        private Event owner;
//        
//        Trigger(GenericObject go, EU2Scenario s, Event owner) {
//            super(go, s);
//            this.owner = owner;
//            
//            for (eug.shared.ObjectVariable var : go.values) {
//                if (var.varname.equals("event"))
//                    EventDatabase.addPrereq(Integer.parseInt(var.getValue()), owner.getId());
//            }
//        }
//        
////        public boolean isMet() {
////            System.err.println("Event.Trigger.isMet() is not implemented");
////            return false;
////        }
//    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    
    
//    private static class CommandFormatter {
//        private CommandFormatter() { }
//        
//        static String format(Command cmd) {
//            return "";
//        }
//    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    
    
//    // Really need a struct here...
//    private static class CommandText {
//        public String text;
//        public int[] format;
//        
//        CommandText(String text, int... format) {
//            this.text = text;
//            this.format = format;
//        }
//    }
}
