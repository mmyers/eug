package eug.specific.clausewitz;

import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.HeaderComment;
import eug.shared.InlineComment;
import eug.shared.ObjectVariable;
import eug.shared.WritableObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Michael
 */
public class ClausewitzHistoryMergeTool {

    
    public enum MergeMode {
        
        /**
         * Mode to merge the two objects, only adding and never overwriting any
         * parts of the existing object that the additions match.
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
         * add_core = DAN
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
         * add_core = DAN
         * 
         * 1420.1.1 = { controller = REB }
         * 1450.1.1 = { base_tax = 10 }
         * 1550.1.1 = { religion = protestant }
         * </pre>
         */
        ADD,
        
        /**
         * Mode to merge the two history objects, overwriting any parts of existing object
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
         */
        OVERWRITE,
        
        /**
         * Mode to merge the two objects, overwriting any parts of the existing object
         * that the additions match, except for ObjectVariables whose name is
         * in the set of elements not to merge.
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
         * add_core = DAN
         * owner = DAN
         * 1420.1.1 = { controller = REB }
         * 1550.1.1 = { religion = protestant }
         * </pre>
         * 
         * Assuming {@link #initAutoMergeList} has been called with an EU4 data
         * source, the value "owner" will be automatically merged (since it only
         * ever appears once in the same block) but "add_core" will not.
         * So after invoking this method, the original object would be updated to:
         * <pre>
         * add_core = SWE
         * owner = DAN
         * controller = SWE
         * culture = swedish
         * base_tax = 8
         * add_core = DAN
         * 
         * 1420.1.1 = { controller = REB }
         * 1450.1.1 = { base_tax = 10 }
         * 1550.1.1 = { religion = protestant }
         * </pre>
         * @see initAutoMergeList
         */
        AUTO,
        
        /**
         * Mode to merge the two objects, deleting any parts of the existing object that
         * the modifications match. A date object will not be deleted unless all
         * its contents have been deleted.
         */
        DELETE
    }
    
    /**
     * Stores all strings that we detect multiples of in province histories,
     * so that when using automatic merge mode, we don't overwrite variables
     * whose names are in the set.
     * Stored canonically lower-case so we don't have to check equalsIgnoreCase()
     * on each element, which defeats the purpose of using a set rather than a list.
     */
    private final Set<String> elementsToNotMerge;
    
    public ClausewitzHistoryMergeTool() {
        elementsToNotMerge = new HashSet<>();
    }
    
    /**
     * Reads province history files from the given data source in an attempt to
     * deduce which items may appear more than once and which can only appear
     * once. This information is used when merging objects with
     * {@code MergeMode.AUTO}.
     * @param data 
     */
    public void initAutoMergeList(ClausewitzDataSource data) {
        for (int i = 1; ; i++) {
            GenericObject hist = data.getProvinceHistory(i);
            if (hist == null)
                break;
            
            checkDuplicateValues(hist);
            for (GenericObject child : hist.children)
                checkDuplicateValues(child);
        }
    }
    
    private void checkDuplicateValues(GenericObject obj) {
        Set<String> vals = new HashSet<>();
        for (ObjectVariable v : obj.values) {
            String name = v.varname.toLowerCase();
            if (vals.contains(name))
                elementsToNotMerge.add(name);
            vals.add(name);
        }
    }
    
    public void initAutoMergeList(Collection<String> notToMerge) {
        elementsToNotMerge.addAll(notToMerge.stream().map(String::toLowerCase).collect(Collectors.toList()));
    }
    
    public void mergeHistObjects(GenericObject existing, GenericObject modifications) {
        mergeHistObjects(existing, modifications, MergeMode.OVERWRITE);
    }
    
    public void mergeHistObjects(GenericObject existing, GenericObject modifications, MergeMode mode) {
        switch(mode) {
            case DELETE:
                deleteFrom(existing, modifications);
                break;
            case ADD:
            case AUTO:
            case OVERWRITE:
            default:
                doMerge(existing, modifications, mode);
                break;
        }
    }
    
    private void doMerge(GenericObject existing, GenericObject additions, MergeMode mode) {
        for (WritableObject wo : additions.getAllWritable()) {
            if (wo instanceof ObjectVariable) {
                ObjectVariable newVar = (ObjectVariable) wo;
                if (!mergeVariable(existing, newVar, mode)) {
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
                    doMerge(oldObj, newObj, mode); // recursion
                } else {
                    existing.addChild(newObj);
                }
            } else if (wo instanceof HeaderComment) {
                existing.addGeneralComment(((HeaderComment) wo).getComment(), true);
            } else if (wo instanceof InlineComment) {
                existing.addGeneralComment(((InlineComment) wo).getComment(), false);
            }
        }
        
        if (!"".equals(additions.getHeadComment()))
            existing.setHeadComment(additions.getHeadComment());
        if (!"".equals(additions.getInlineComment()))
            existing.setInlineComment(additions.getInlineComment());
        
        existing.getAllWritable().sort(new ClausewitzHistory.HistoryObjectComparator());
    }
    

    /**
     * Merges the two objects, deleting any parts of the existing object that
     * the modifications match. A date object will not be deleted unless all
     * its contents have been deleted.
     * @param existing
     * @param modifications 
     */
    private void deleteFrom(GenericObject existing, GenericObject modifications) {
        List<GenericObject> deletedObjects = new ArrayList<>();
        List<GenericList> deletedLists = new ArrayList<>();
        
        for (WritableObject wo : modifications.getAllWritable()) {
            if (wo instanceof ObjectVariable) {
                ObjectVariable varToDelete = (ObjectVariable) wo;
                deleteVariable(existing, varToDelete);
            } else if (wo instanceof GenericList) {
                GenericList newList = (GenericList) wo;
                GenericList oldList = existing.getList(newList.getName());
                if (oldList != null) {
                    if (oldList.deleteAll(newList) && oldList.size() == 0)
                        deletedLists.add(oldList);
                } else {
                    existing.addList(newList);
                }
            } else if (wo instanceof GenericObject) {
                GenericObject newObj = (GenericObject) wo;
                GenericObject oldObj = existing.getChild(newObj.name);
                if (oldObj != null) {
                    deleteFrom(oldObj, newObj); // recursion
                    if (oldObj.isEmpty())
                        deletedObjects.add(oldObj);
                }
            }
        }
        
        for (GenericObject obj : deletedObjects) {
            existing.getAllWritable().remove(obj);
            existing.children.remove(obj);
        }
        
        for (GenericList list : deletedLists) {
            existing.getAllWritable().remove(list);
            existing.lists.remove(list);
        }
        
        // unlike doMerge(), we don't care about any comments on the new object
        // nor do we need to sort the history
    }
    

    /**
     * Merges the variable into the object, returning true if successful and
     * false if the variable should simply be added.
     * @param existing
     * @param newVar
     * @param mode
     * @return 
     */
    private boolean mergeVariable(GenericObject existing, ObjectVariable newVar, MergeMode mode) {
        switch(mode) {
            case ADD:
            case DELETE: // should never happen
                return false;
            case AUTO:
                // we keep a list of variable names that should NOT be merged by default
                // so check that first
                if (!elementsToNotMerge.isEmpty() && elementsToNotMerge.contains(newVar.varname.toLowerCase()))
                    return false;
                // otherwise merge
                break;
            case OVERWRITE:
            default:
                // always merge if possible
                break;
        }
        
        // instead of using setString, we do the loop ourselves so we have access to the original ObjectVariable to add any comments
        for (ObjectVariable oldVar : existing.values) {
            if (oldVar.varname.equalsIgnoreCase(newVar.varname)) {
                // copy everything over
                // could merge the comments instead of copying, but that would likely result in odd outcomes
                if (!"".equals(newVar.getHeadComment()))
                    oldVar.setHeadComment(newVar.getHeadComment());
                oldVar.setValue(newVar.getValue());
                if (!"".equals(newVar.getInlineComment()))
                    oldVar.setInlineComment(newVar.getInlineComment());
                return true;
            }
        }
        return false;
    }
    
    private void deleteVariable(GenericObject existing, ObjectVariable varToDelete) {
        // we must loop through the object to see if there is a variable with
        // an EXACT match of both name and value
        
        ObjectVariable found = null;
        for (ObjectVariable oldVar : existing.values) {
            if (oldVar.varname.equalsIgnoreCase(varToDelete.varname)
                    && oldVar.getValue().equalsIgnoreCase(varToDelete.getValue())) {
                found = oldVar;
                break;
            }
        }
        existing.getAllWritable().remove(found);
        existing.values.remove(found);
    }
}
