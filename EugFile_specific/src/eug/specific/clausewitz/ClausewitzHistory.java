

package eug.specific.clausewitz;

import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.HeaderComment;
import eug.shared.InlineComment;
import eug.shared.ObjectVariable;
import eug.shared.WritableObject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class which deals with Clausewitz-engine histories, such as are found in the
 * history subfolder or in saved games.
 * @since EUGFile 1.04.00pre1
 * @author Michael Myers
 */
public final class ClausewitzHistory {
    
    private ClausewitzHistory() { }
    
    
    /**
     * Comparator for date strings like "1492.1.1".
     */
    public static final DateComparator DATE_COMPARATOR = new DateComparator();
    
//    private static final Pattern DATE_PATTERN =
//            Pattern.compile("[0-9]{1,4}\\.[0-9]{1,2}\\.[0-9]{1,2}");

    private static boolean isDate(final String str) {
        // As it turns out, a regex is simple but rather slow for this.
        // Instead, this method uses a handcrafted validator.
//        return DATE_PATTERN.matcher(str).matches();
        final int firstDot = str.indexOf('.');
        if (firstDot < 1)
            return false;

        final int secondDot = str.indexOf('.', firstDot + 1);
        if (secondDot < 1 || secondDot > str.length()-2)
            return false;
        final int diff = secondDot - firstDot;
        if (diff == 1 || diff > 3)
            return false;

        if (!areAllDigits(str, 0, firstDot))
            return false;
        if (!areAllDigits(str, firstDot + 1, secondDot))
            return false;
        if (!areAllDigits(str, secondDot + 1, str.length()))
            return false;

        return true;
    }

    private static boolean areAllDigits(final String str, int start, int end) {
        for (int i = start; i < end; i++) {
            final char c = str.charAt(i);
            if (c < '0' || c > '9')
                return false;
        }
        return true;
    }
    
    
    public static GenericObject getHistObject(final GenericObject history, String name) {
        if (history == null)
            return null;
        
        GenericObject value = history.getLastChild(name);
        String lastDate = "0.0.0";
        for (GenericObject date : history.children) {
            if (!isDate(date.name)) {
                continue;
            }
            
            if (DATE_COMPARATOR.compare(date.name, lastDate) >= 0) {
                GenericObject newVal = date.getLastChild(name);
                if (newVal != null) {
                    value = newVal;
                    lastDate = date.name;
                }
            }
        }
        return value;
    }
    
    public static GenericObject getHistObject(final GenericObject history, String name, String date) {
        if (history == null)
            return null;
        
        GenericObject value = history.getLastChild(name);
        String lastDate = "0.0.0";
        for (GenericObject dateObj : history.children) {
            if (!isDate(dateObj.name)) {
                continue;
            }
            
            if (DATE_COMPARATOR.compare(dateObj.name, lastDate) >= 0 &&
                    DATE_COMPARATOR.compare(dateObj.name, date) <= 0) {
                // The new date is after the old date and before or equal to the target date
                GenericObject newVal = dateObj.getLastChild(name);
                if (newVal != null) {
                    value = newVal;
                    lastDate = dateObj.name;
                }
            }
        }
        return value;
    }
    
    public static List<GenericObject> getHistObjects(final GenericObject history, String name, String date) {
        if (history == null)
            return null;
        
        final List<GenericObject> objects = history.getChildren(name);
        
        for (GenericObject dateObj : history.children) {
            if (!isDate(dateObj.name)) {
                continue;
            }
            
            if (DATE_COMPARATOR.compare(dateObj.name, date) <= 0) {
                // The new date is before or equal to the target date
                GenericObject obj = dateObj.getLastChild(name);
                if (obj != null) {
                    objects.add(obj);
                }
            }
        }
        
        return objects;
    }
    
    
    public static String getHistString(final GenericObject history, String name) {
        if (history == null)
            return null;
        
        String value = history.getLastString(name);
        String lastDate = "0.0.0";
        for (GenericObject date : history.children) {
            if (!isDate(date.name)) {
                if (!"advisor".equals(date.name) && !"controller".equals(date.name))
                    System.err.println(date.name + " is not a valid date");
                continue;
            }
            
            if (DATE_COMPARATOR.compare(date.name, lastDate) >= 0) {
                String newVal = date.getLastString(name);
                if (newVal.length() != 0) {
                    value = newVal;
                    lastDate = date.name;
                }
            }
        }
        return value;
    }
    
    public static String getHistString(final GenericObject history, String name, String date) {
        if (history == null)
            return null;
        
        String value = history.getLastString(name);
        String lastDate = "0.0.0";
        for (GenericObject dateObj : history.children) {
            if (!isDate(dateObj.name)) {
                continue;
            }
            
            if (DATE_COMPARATOR.compare(dateObj.name, lastDate) >= 0 &&
                    DATE_COMPARATOR.compare(dateObj.name, date) <= 0) {
                // The new date is after the old date and before or equal to the target date
                String newVal = dateObj.getLastString(name);
                if (newVal.length() != 0) {
                    value = newVal;
                    lastDate = dateObj.name;
                }
            }
        }
        return value;
    }
    
    
    public static String getHistString(final GenericObject history, String name1, String name2, String date) {
        if (history == null)
            return null;
        
        // Check both keys and get the last instance of either
        String value = history.getLastString(name1, name2);
        String lastDate = "0.0.0";
        for (GenericObject dateObj : history.children) {
            if (!isDate(dateObj.name)) {
                continue;
            }
            
            if (DATE_COMPARATOR.compare(dateObj.name, lastDate) >= 0 &&
                    DATE_COMPARATOR.compare(dateObj.name, date) <= 0) {
                // The new date is after the old date and before or equal to the target date
                String newVal = dateObj.getLastString(name1, name2);
                if (newVal.length() != 0) {
                    value = newVal;
                    lastDate = dateObj.name;
                }
            }
        }
        return value;
    }
    
    public static List<String> getHistStrings(final GenericObject history, String name, String date) {
        if (history == null)
            return null;
        
        final List<String> values = history.getStrings(name);
        
        for (GenericObject dateObj : history.children) {
            if (!isDate(dateObj.name)) {
                continue;
            }
            
            if (DATE_COMPARATOR.compare(dateObj.name, date) <= 0) {
                // The new date is before or equal to the target date
                String value = dateObj.getLastString(name);
                if (value.length() != 0) {
                    values.add(value);
                }
            }
        }
        
        return values;
    }

    /**
     * Special utility method because otherwise determining cores is really nasty.
     */
    public static List<String> isCoreOf(final GenericObject provHistory, String date) {
        return getHistStrings(provHistory, date, "add_core", "remove_core");
    }
    
    /**
     * Searches through a history object and finds all strings that have been
     * added by the <code>adder</code> string and not removed by the
     * <code>remover</code> string. Examples of adder/remover pairs include
     * "add_core"/"remove_core" in province histories and 
     * "add_attacker"/"rem_attacker" in war histories.
     * @param history the history object to search through.
     * @param date the last date that is checked. Anything added or removed
     * after this date is not considered.
     * @param adder the string that signals that something has been added.
     * @param remover the string that signals that something has been removed.
     * @return the list of strings that have been added but not removed. This
     * could be, for example, the list of country tags that have cores on a
     * particular province.
     */
    public static List<String> getHistStrings(
            final GenericObject history,
            final String date,
            final String adder,
            final String remover)
    {
        
        if (history == null)
            return null;
        
        final List<String> values = history.getStrings(adder);
        
        for (GenericObject dateObj : history.children) {
            if (!isDate(dateObj.name)) {
                continue;
            }
            
            if (DATE_COMPARATOR.compare(dateObj.name, date) <= 0) {
                values.removeAll(dateObj.getStrings(remover));
                values.addAll(dateObj.getStrings(adder));
            }
        }
        
        return values;
    }
    
    /**
     * Returns true if the specified right-hand side has been set by the specified
     * left-hand side without subsequently being cleared by the specified left-hand side.
     * That is, if <code>lhsSet = rhs</code> has appeared without a subsequent
     * <code>lhsClear = rhs</code>.
     * <p>
     * Mainly used for set_province_flag/clr_province_flag and set_country_flag/clr_country_flag.
     * @param history
     * @param lhsSet the left-hand side which sets the flag
     * @param lhsClear the left-hand side which clears the flag
     * @param rhs the flag being set (case sensitive)
     * @param date
     * @return 
     */
    public static boolean isRhsSet(
            final GenericObject history,
            final String lhsSet,
            final String lhsClear,
            final String rhs,
            final String date)
    {
        if (history == null)
            return false;
        
        boolean isSet = false;
        
        for (GenericObject dateObj : history.children) {
            if (!isDate(dateObj.name)) {
                continue;
            }
            
            if (DATE_COMPARATOR.compare(dateObj.name, date) <= 0) {
                for (ObjectVariable variable : dateObj.values) {
                    if (variable.varname.equalsIgnoreCase(lhsSet) && variable.getValue().equals(rhs)) { // rhs is case sensitive
                        isSet = true;
                    } else if (variable.varname.equalsIgnoreCase(lhsClear) && variable.getValue().equals(rhs)) {
                        isSet = false;
                    }
                }
            }
        }
        
        return isSet;
    }
    
    /**
     * Merges the two history objects, overwriting any parts of existing object
     * that the additions match. The final product is then sorted in the usual
     * history order (first bare variables, then date objects in order).
     * <p>
     * For example, take the object:
     * <pre>
     * add_core = SWE
     * owner = SWE
     * controller = SWE
     * culture = swedish
     * base_tax = 8
     * 
     * 1450.1.1 = { base_tax = 10 }
     * </pre>
     * And the following additions:
     * <pre>
     * hre = no
     * 1420.1.1 = { controller = REB }
     * 1550.1.1 = { religion = protestant }
     * </pre>
     * 
     * After invoking this method, the original object would be updated to:
     * <pre>
     * add_core = SWE
     * owner = SWE
     * controller = SWE
     * culture = swedish
     * base_tax = 8
     * hre = no
     * 
     * 1420.1.1 = { controller = REB }
     * 1450.1.1 = { base_tax = 10 }
     * 1550.1.1 = { religion = protestant }
     * </pre>
     * @param existing the object to add new parts into
     * @param additions the new parts to add into the existing object
     */
    public static void mergeHistObjects(GenericObject existing, GenericObject additions) {
        for (WritableObject wo : additions.getAllWritable()) {
            if (wo instanceof ObjectVariable) {
                // instead of using setString, we do the loop ourselves so we have access to the original ObjectVariable to add any comments
                ObjectVariable newVar = (ObjectVariable) wo;
                boolean found = false;
                for (ObjectVariable oldVar : existing.values) {
                    if (oldVar.varname.equalsIgnoreCase(newVar.varname)) {
                        found = true;
                        // copy everything over
                        // could merge the comments instead of copying, but that would likely result in odd outcomes
                        oldVar.setHeadComment(newVar.getHeadComment());
                        oldVar.setValue(newVar.getValue());
                        oldVar.setInlineComment(newVar.getInlineComment());
                    }
                }
                if (!found) {
                    existing.addVariable(newVar);
                }
            } else if (wo instanceof GenericList) {
                GenericList newList = (GenericList) wo;
                GenericList oldList = existing.getList(newList.getName());
                if (oldList != null) {
                    // merging lists seems wrong, so let's just overwrite it
                    oldList.clear();
                    oldList.addAll(newList);
                    oldList.setHeaderComment(newList.getHeaderComment());
                    oldList.setInlineComment(newList.getInlineComment());
                } else {
                    existing.addList(newList);
                }
            } else if (wo instanceof GenericObject) {
                GenericObject newObj = (GenericObject) wo;
                GenericObject oldObj = existing.getChild(newObj.name);
                if (oldObj != null) {
                    mergeHistObjects(oldObj, newObj);
                } else {
                    existing.addChild(newObj);
                }
            } else if (wo instanceof HeaderComment) {
                existing.addGeneralComment(((HeaderComment) wo).getComment(), true);
            } else if (wo instanceof InlineComment) {
                existing.addGeneralComment(((InlineComment) wo).getComment(), false);
            }
        }
        
        existing.setHeadComment(additions.getHeadComment());
        existing.setInlineComment(additions.getInlineComment());
        
        existing.getAllWritable().sort(new HistoryObjectComparator());
    }

    public static final class DateComparator implements Comparator<String> {

        public DateComparator() {
            // nothing to do here
        }
        
        private static final Map<String, String[]> splitMap = new HashMap<>(100);
        private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

        private static String[] split(final String s) {
            return splitMap.computeIfAbsent(s, DOT_PATTERN::split);
        }
        private static final Map<String, Integer> intMap = new HashMap<>(100);

        private static Integer getInt(final String s) {
            return intMap.computeIfAbsent(s, Integer::valueOf);
        }

        @Override
        public final int compare(final String s1, final String s2) {
            final String[] s1Split = split(s1);
            final String[] s2Split = split(s2);

            int ret = getInt(s1Split[0]).compareTo(getInt(s2Split[0]));
            if (ret != 0) {
                return ret;
            }
            ret = getInt(s1Split[1]).compareTo(getInt(s2Split[1]));
            if (ret != 0) {
                return ret;
            }
            return getInt(s1Split[2]).compareTo(getInt(s2Split[2]));
        }

        /**
         * Returns <code>true</code> if <code>date1</code> is before
         * <code>date2</code>.
         */
        public boolean isBefore(final String date1, final String date2) {
            return compare(date1, date2) < 0;
        }
    }

    private static class HistoryObjectComparator implements Comparator<WritableObject> {

        private final DateComparator dateComparator;
        
        public HistoryObjectComparator() {
            dateComparator = new DateComparator();
        }

        @Override
        public int compare(WritableObject o1, WritableObject o2) {
            int score1 = objectScore(o1);
            int score2 = objectScore(o2);
            if (score1 == score2) {
                if (o1 instanceof GenericObject) {
                    GenericObject g1 = (GenericObject) o1;
                    GenericObject g2 = (GenericObject) o2;
                    
                    return dateComparator.compare(g1.name, g2.name);
                }
                return 0;
            }
            return score1 - score2;
        }
        
        private int objectScore(WritableObject obj) {
            if (obj instanceof ObjectVariable)
                return 1;
            if (obj instanceof GenericList)
                return 2;
            if (obj instanceof GenericObject)
                return 3;
            return 4;
        }
    }
    
}
