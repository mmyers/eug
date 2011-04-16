

package eug.specific.clausewitz;

import eug.shared.GenericObject;
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
//                if (!date.name.equals("advisor"))
//                    System.err.println(date.name + " is not a valid date");
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
//                if (!dateObj.name.equals("advisor"))
//                    System.err.println(dateObj.name + " is not a valid date");
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
    
    
    public static String getHistString(final GenericObject history, String name) {
        if (history == null)
            return null;
        
        String value = history.getLastString(name);
        String lastDate = "0.0.0";
        for (GenericObject date : history.children) {
            if (!isDate(date.name)) {
                if (!date.name.equals("advisor"))
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
                //if (!dateObj.name.equals("advisor"))
                //    System.err.println(dateObj.name + " is not a valid date");
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
    
    public static List<String> getHistStrings(final GenericObject history, String name, String date) {
        if (history == null)
            return null;
        
        final List<String> values = history.getStrings(name);
        
        for (GenericObject dateObj : history.children) {
            if (!isDate(dateObj.name)) {
//                if (!dateObj.name.equals("advisor"))
//                    System.err.println(dateObj.name + " is not a valid date");
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
                if (!"advisor".equals(dateObj.name))
                    System.err.println(dateObj.name + " is not a valid date");
                continue;
            }
            
            if (DATE_COMPARATOR.compare(dateObj.name, date) <= 0) {
                values.removeAll(dateObj.getStrings(remover));
                values.addAll(dateObj.getStrings(adder));
            }
        }
        
        return values;
    }

    public static final class DateComparator implements Comparator<String> {

        public DateComparator() {
        }
        
        private static final Map<String, String[]> splitMap = new HashMap<String, String[]>(100);
        private static final Pattern dot = Pattern.compile("\\.");

        private static String[] split(final String s) {
            String[] split = splitMap.get(s);
            if (split == null) {
                split = dot.split(s);
                splitMap.put(s, split);
            }
            return split;
        }
        private static final Map<String, Integer> intMap = new HashMap<String, Integer>(100);

        private static Integer getInt(final String s) {
            Integer i = intMap.get(s);
            if (i == null) {
                i = Integer.valueOf(s);
                intMap.put(s, i);
            }
            return i;
        }

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
    
}
