
/*
 * Rewritten to use generics (ironic, isn't it?), 6/06.
 */

package eug.shared;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents an object present in the game file,
 * whatever its type. It has four main vectors for storing descendants:
 * <ul>
 * <li>{@link #children}: a vector which contains all the objects beneath it.
 * (For example, populations for a province object)
 * <li>{@link #values}: same, but for the attributes.<br>
 * Example: <PRE>"ID = 45"</PRE>
 * <li>{@link #lists}: same, but for lists.<br>
 * Example: <PRE>"ownedprovinces = { 45 46 50 }"</PRE>
 * <li>{@link #allWritable}: this vector contains all the descendants.
 * EVERY OBJECT PRESENT IN <CODE>children</CODE>, <CODE>values</CODE> OR
 * <CODE>lists</CODE> MUST BE IN IT, OR THEY WILL NOT BE SAVED.<br>
 * It is used to preserve the original order of descendants when saving the file
 * (not required by the game, but anything else would be very confusing for users).
 * </ul>
 * <p>
 * Unless otherwise noted, all get/set variable operations (e.g.,
 * <code>getString()</code>) are case insensitive.
 */
public final class GenericObject implements WritableObject, Cloneable {
    
    // <editor-fold defaultstate="collapsed" desc=" Variables ">
    
    // The header and inline comments, if any, are not stored in allWritable.
    private HeaderComment headComment = null;
    private InlineComment inlineComment = null;
    
    //modif leo
    
    /**
     * List of all object children of this object. This may include empty objects.
     */
    public List<GenericObject> children;
    
    /**
     * List of all list children of this object. This does not include empty lists, as they
     * are parsed as empty objects instead.
     */
    public List<GenericList> lists;
    
    private GenericObject parent;
    
    /**
     * List of all value children of this object (e.g. "id = 10000").
     */
    public List<ObjectVariable> values;
    /** @since EUGFile 1.01.03 */
    private List<Comment> generalComments;  // comments that are not attached to a node
    public String name;
    
    /**
     * List of every child of this object so that the order is preserved.
     */
    private List<WritableObject> allWritable;
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Constructors ">
    
    /**
     * Creates a root object. Use
     * {@link #createChild(GenericObject,String) createChild} to create
     * non-root objects.
     */
    public GenericObject() {
        this(null, "root");
    }
    
    private GenericObject(GenericObject par, String t) {
        parent = par;
        name = t;
        
        children = new ArrayList<>(0);
        lists = new ArrayList<>(0);
        values = new ArrayList<>(0);
        generalComments = new ArrayList<>(0);
        allWritable = new ArrayList<>(0);
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Getters ">

    /**
     * Gets the child object with the given index.
     * @param id the index of the object to retrieve (0 is the first child,
     *           1 is the second, etc.).
     * @return the specified child object.
     */
    public GenericObject getChild(int id) {
        if (id >= children.size())
            return null;
        else
            return children.get(id);
    }

    /**
     * Gets the first child object with the given name.
     * @param childname the name of the object to retrieve.
     * @return the named child object.
     */
    public GenericObject getChild(String childname) {
        for (GenericObject child : children)
            if (child.name.equalsIgnoreCase(childname))
                return child;
        
        return null;
    }

    /**
     * Gets the last child object with the given name.
     * @param childname the name of the object to retrieve.
     * @return the named child object.
     */
    public GenericObject getLastChild(String childname) {
        for (int i = children.size()-1; i >= 0; i--)
            if (children.get(i).name.equalsIgnoreCase(childname))
                return children.get(i);

        return null;
    }

    /**
     * Gets all child objects with the given name.
     * @param name the name of the objects to retrieve.
     * @return the named child objects.
     */
    public List<GenericObject> getChildren(String name) {
        return children.stream()
                .filter(child -> child.name.equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    /**
     * Gets the child list with the given name.
     * @param listname the name of the list to retrieve.
     * @return the named list.
     */
    public GenericList getList(String listname) {
        for (GenericList list : lists)
            if (list.getName().equalsIgnoreCase(listname))
                return list;
        
        GenericObject l = getChild(listname);
        
        if (l == null)
            return null;
        
        if (l.allWritable.isEmpty()) // which means it could have been an empty list parsed as an object with no children
            return convertToList(l);
        
        return null;
    }

    /**
     * Gets the last child list with the given name.
     * @param listname the name of the list to retrieve.
     * @return the named list.
     */
    public GenericList getLastList(String listname) {
        for (int i = lists.size() - 1; i >= 0; i--)
            if (lists.get(i).getName().equalsIgnoreCase(listname))
                return lists.get(i);

        GenericObject l = getLastChild(listname);

        if (l == null)
            return null;

        if (l.allWritable.isEmpty()) // which means it could have been an empty list parsed as an object with no children
            return convertToList(l);

        return null;
    }
    
    private GenericList convertToList(GenericObject child) {
        GenericList newList = new GenericList(child.name);
        
        final int idx = allWritable.indexOf(child);
        allWritable.set(idx, newList);
        
        children.remove(child);
        lists.add(newList);
        
        return newList;
    }

    /**
     * Gets the child list with the given index.
     * @param id the index of the list to retrieve (0 is the first list,
     *           1 is the second, etc.).
     * @return the specified list.
     */
    public GenericList getList(int id) {
        if (id >= lists.size())
            return null;
        else
            return lists.get(id);
    }

    /**
     * Gets the child variable with the given index.
     * @param id the index of the variable to retrieve (0 is the first variable,
     *           1 is the second, etc.).
     * @return the specified variable.
     */
    public ObjectVariable getVariable(int id) {
        if (id >= values.size())
            return null;
        else
            return values.get(id);
    }

    /**
     * Gets the first child variable with the given name.
     * @param varname the name of the variable to retrieve.
     * @return the named child variable in string form.
     */
    public String getString(String varname) {
        for (ObjectVariable var : values)
            if (var.varname.equalsIgnoreCase(varname))
                return var.getValue();
        
        return "";
    }

    /**
     * Gets the last child variable with the given name.
     * @param varname the name of the variable to retrieve.
     * @return the named child variable in string form.
     */
    public String getLastString(String varname) {
        for (int i = values.size() - 1; i >= 0; i--)
            if (values.get(i).varname.equalsIgnoreCase(varname))
                return values.get(i).getValue();

        return "";
    }
    
    /**
     * Gets the last child variable with one of the given names.
     */
    public String getLastString(String varname1, String varname2) {
        for (int i = values.size() - 1; i >= 0; i--) {
            ObjectVariable value = values.get(i);
            if (value.varname.equalsIgnoreCase(varname1)
                    || value.varname.equalsIgnoreCase(varname2)) {
                return value.getValue();
            }
        }
        return "";
    }
    
    /**
     * Returns all values of the given string. For example, if an object
     * contains something like
     * <pre>
     * vp = 100
     * vp = 101
     * </pre>
     * , then <code>getStrings("vp")</code> will return a list containing "100"
     * and "101".
     * @since EUGFile 1.02.00
     */
    public List<String> getStrings(String name) {
        return values.stream()
                .filter(var -> var.varname.equalsIgnoreCase(name))
                .map(var -> var.getValue())
                .collect(Collectors.toList());
    }

    /**
     * Gets the first child variable with the given name.
     * @param varname the name of the variable to retrieve.
     * @return the named child variable in int form.
     */
    public int getInt(String varname) {
        final String val = getString(varname);
        if (val.length() != 0)
            return Integer.parseInt(val);
        else
            return -1;
    }

    /**
     * Gets the last child variable with the given name.
     * @param varname the name of the variable to retrieve.
     * @return the named child variable in int form.
     */
    public int getLastInt(String varname) {
        final String val = getLastString(varname);
        if (val.length() != 0)
            return Integer.parseInt(val);
        else
            return -1;
    }

    /**
     * Gets the first child variable with the given name.
     * @param varname the name of the variable to retrieve.
     * @return the named child variable in double form.
     */
    public double getDouble(String varname) {
        final String val = getString(varname);
        if (val.length() != 0)
            return Double.parseDouble(val);
        else
            return -1.0;
    }

    /**
     * Gets the last child variable with the given name.
     * @param varname the name of the variable to retrieve.
     * @return the named child variable in double form.
     */
    public double getLastDouble(String varname) {
        final String val = getLastString(varname);
        if (val.length() != 0)
            return Double.parseDouble(val);
        else
            return -1.0;
    }

    /**
     * Gets the first child variable with the given name. Returns {@code true}
     * if the variable's value is {@code "yes"}, ignoring case; {@code false}
     * otherwise.
     * @param varname the name of the variable to retrieve.
     * @return the named child variable in boolean form.
     */
    public boolean getBoolean(String varname) {
        final String val = getString(varname);
        if (val.length() != 0)
            return val.equalsIgnoreCase("yes");
        else
            return false;
    }

    /**
     * Gets the last child variable with the given name.
     * @param varname the name of the variable to retrieve.
     * @return the named child variable in boolean form.
     */
    public boolean getLastBoolean(String varname) {
        final String val = getLastString(varname);
        if (val.length() != 0)
            return val.equalsIgnoreCase("yes");
        else
            return false;
    }

    /**
     * Gets the parent of this object. If this object is the root, the parent is null.
     * @return this object's parent.
     */
    public GenericObject getParent() {
        return parent;
    }
    
    /**
     * Simple recursive method to find the root node.
     * @return the root node
     * @since EUGFile 1.01.03
     */
    public GenericObject getRoot() {
        return (parent == null ? this : parent.getRoot());
    }
    
    /**
     * Gives direct access to the list of all writable objects stored in this
     * object. The list is not cloned before returning.<p>
     * This method should only be used with great, great caution. Modifying the
     * returned list could cause major problems.
     */
    public List<WritableObject> getAllWritable() {
        return allWritable;
    }
    
    // Doesn't exactly belong in this section, but it is a getter, so...
    
    /**
     * Returns the comment immediately following the variable with the given
     * name.
     * @since 1.02.00
     */
    public String getCommentAfterVar(String varname) {
        for (int i = 0; i < allWritable.size(); i++) {
            WritableObject obj = allWritable.get(i);
            
            if (obj instanceof ObjectVariable) {
                ObjectVariable var = (ObjectVariable) obj;
                
                if (!var.varname.equalsIgnoreCase(varname))
                    continue;
                
                if (var.getInlineComment().length() != 0)
                    return var.getInlineComment();
                
                WritableObject next = allWritable.get(i+1);
                
                if (next instanceof ObjectVariable) {
                    return ((ObjectVariable) next).getHeadComment();
                } else if (next instanceof GenericObject) {
                    return ((GenericObject) next).getHeadComment();
                } else if (next instanceof GenericList) {
                    return ((GenericList) next).getHeaderComment();
                } else if (next instanceof Comment) {
                    return ((Comment) next).getComment();
                } else {
                    return "";
                }
            }
        }
        return "";
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Counters ">
    
    public int nbChild() {
        return children.size();
    }
    
    public int nbChild(String type) {
        int nb = 0;
        
        for (GenericObject child : children) {
            if (child.name.equalsIgnoreCase(type))
                nb++;
        }
        return nb;
    }
    
    public int nbList() {
        return lists.size();
    }
    
    public int nbVar() {
        return values.size();
    }
    
    public int nbVar(String type) {
        int nb = 0;
        for (ObjectVariable var : values)
            if (var.varname.equalsIgnoreCase(type))
                nb++;
        return nb;
    }
    
    /**
     * The total size of the object: the number of sub-objects + the number of
     * sub-lists + the number of values. Comments are not included in the count.
     * @return the number of children of this object
     */
    public int size() {
        return allWritable.size();
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Mutators ">
    
    public void removeVariable(int id) {
        allWritable.remove(values.get(id));
        values.remove(id);
    }
    
    public boolean removeVariable(String name) {
        ObjectVariable var = null;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).varname.equalsIgnoreCase(name)) {
                var = values.get(i);
                values.remove(i);
                break;
            }
        }
        
        if (var == null)
            return false;
        
        allWritable.remove(var);
        return true;
    }
    
    public void removeChild(GenericObject val){
        this.children.remove(val);
        this.allWritable.remove(val);
    }
    
    public boolean removeChild(String name) {
        GenericObject obj = getChild(name);
        if (obj != null) {
            removeChild(obj);
            return true;
        }
        return false;
    }
    
    public void removeList(GenericList list) {
        this.lists.remove(list);
        this.allWritable.remove(list);
    }
    
    public boolean removeList(String listname) {
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).getName().equalsIgnoreCase(listname)) {
                allWritable.remove(lists.get(i));
                lists.remove(i);
                return true;
            }
        }
        return false;
    }
    
    public void setString(String varname, String newval) {
        setString(varname, newval, false);
    }
    
    public void setString(String varname, String newval, boolean quotes) {
        for (ObjectVariable var : values) {
            if (var.varname.equalsIgnoreCase(varname)) {
                var.setValue(newval, quotes);
                return;
            }
        }
        
        //pas de variable de ce nom, on la cr�e
        this.addString(varname, newval, quotes);
    }
    
    public void setInt(String varname, int newval) {
        setString(varname, String.valueOf(newval));
    }
    
    public void setDouble(String varname, double newval) {
        setString(varname, String.valueOf(newval));
    }
    
    public void setBoolean(String varname, boolean newval) {
        setString(varname, (newval ? "yes" : "no"));
    }
    
    public void clear() {
        children.clear();
        lists.clear();
        values.clear();
        allWritable.clear();
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Queries ">
    
    public int isChildOf(GenericObject parent){
        return parent.children.indexOf(this);
    }
    
    public boolean hasString(String varname) {
        return values.stream()
                .anyMatch(var -> var.varname.equalsIgnoreCase(varname));
    }
    
    public boolean contains(String str) {
        return hasString(str);
    }
    
    public boolean containsChild(String name) {
        return getChild(name) != null;
    }
    
    public boolean containsList(String name) {
        return getList(name) != null;
    }
    
    public boolean isEmpty() {
        return allWritable.isEmpty();
    }
    
    /**
     * Returns whether this node is a root node or not.
     * @since EUGFile 1.02.00
     */
    public boolean isRoot() {
        return parent == null;
    }
    
    /**
     * Trims the capacity of all lists used by this object to their current size.
     */
    public void trimToSize() {
        ((ArrayList) children).trimToSize();
        ((ArrayList) lists).trimToSize();
        ((ArrayList) values).trimToSize();
        ((ArrayList) generalComments).trimToSize();
        ((ArrayList) allWritable).trimToSize();
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Adders ">
    
    public void addString(String varname, String val) {
        addString(varname, val, false);
    }
    
    public void addString(String varname, String val, boolean quotes) {
        ObjectVariable newvariable = new ObjectVariable(varname, val, quotes);
        values.add(newvariable);
        allWritable.add(newvariable);
    }
    
    public void addString(String varname, String val, boolean quotes, String headComment, String inlineComment) {
        ObjectVariable newVar = new ObjectVariable(varname, val, quotes);
        newVar.setHeadComment(headComment);
        newVar.setInlineComment(inlineComment);
        values.add(newVar);
        allWritable.add(newVar);
    }
    
    public void addChild(GenericObject newval){
        if (newval == null)
            throw new NullPointerException("Can't add a null child");
        
        this.children.add(newval);
        this.allWritable.add(newval);
    }
    
    public GenericObject createChild(String name) {
        final GenericObject child = new GenericObject(this, name);
        addChild(child);
        return child;
    }
    
    public void addList(GenericList newval) {
        this.lists.add(newval);
        this.allWritable.add(newval);
    }
    
    public void addList(String name, String[] vals) {
        final GenericList list = new GenericList(name);
        list.addAll(vals);
        addList(list);
    }
    
    public void addList(String name, List<String> vals) {
        final GenericList list = new GenericList(name);
        list.addAll(vals);
        addList(list);
    }
    
    public void addList(String name, String[] vals, String headComment, String inlineComment) {
        final GenericList list = new GenericList(name);
        list.addAll(vals);
        list.setHeaderComment(headComment);
        list.setInlineComment(inlineComment);
        addList(list);
    }
    
    public void addList(String name, List<String> vals, String headComment, String inlineComment) {
        final GenericList list = new GenericList(name);
        list.addAll(vals);
        list.setHeaderComment(headComment);
        list.setInlineComment(inlineComment);
        addList(list);
    }
    
    public GenericList createList(String name) {
        final GenericList list = new GenericList(name);
        addList(list);
        return list;
    }
    
    /**
     * Adds all children of <code>other</code> to this object.
     */
    public void addAllChildren(GenericObject other) {
        for (WritableObject o : other.allWritable) {
            if (o instanceof GenericObject) {
                ((GenericObject) o).parent = this;
                this.children.add((GenericObject) o);
            } else if (o instanceof GenericList) {
                this.lists.add((GenericList) o);
            } else if (o instanceof ObjectVariable) {
                this.values.add((ObjectVariable) o);
            }
            this.allWritable.add(o);
        }
    }
    
    public void addNumber(String name, double val) {
        addString(name, String.format("%.3f", val), false);
    }
    
    public void addInt(String name, int val) {
        addString(name, Integer.toString(val), false);
    }
    
    public void addBoolean(String name, boolean val) {
        addString(name, (val ? "yes" : "no"), false);
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Comments ">
    
    /** @since EUGFile 1.01.03 */
    public void addGeneralComment(String comment) {
        // TODO: Figure out whether a given string is a header or inline comment. Currently the default is header.
        addGeneralComment(comment, true);
    }
    
    /** @since EUGFile 1.01.03 */
    public void addGeneralComment(String comment, boolean header) {
        if (comment == null || comment.length() == 0)
            return;
        
        final Comment c =
                (header ? new HeaderComment(comment) : new InlineComment(comment));
        generalComments.add(c);
        allWritable.add(c);
    }
    
    /**
     * Adds a comment to the beginning of the root object. Note that the last
     * comment added will appear first.
     * @since EUGFile 1.02.00
     */
    public void addFileHeaderComment(String comment, boolean header) {
        if (comment == null || comment.length() == 0)
            return;
        
        final Comment c =
                (header ? new HeaderComment(comment) : new InlineComment(comment));
        
        final GenericObject root = getRoot(); // avoid calling recursive methods more than once
        root.generalComments.add(0, c);
        root.allWritable.add(0, c);
    }
    
    public String getHeadComment() {
        return headComment == null ? "" : headComment.getComment();
    }
    
    public void setHeadComment(String comment) {
        if (comment == null || comment.length() == 0) {
            headComment = null;
            return;
        }
        headComment = new HeaderComment(comment);
    }
    
    public void addHeadComment(String comment) {
        if (headComment == null)
            headComment = new HeaderComment(comment);
        else
            headComment.appendComment(comment);
    }
    
    public void setInlineComment(String comment) {
        if (comment == null || comment.length() == 0)
            inlineComment = null;
        else
            inlineComment = new InlineComment(comment);
    }
    
    public String getInlineComment() {
        return (inlineComment == null ? "" : inlineComment.getComment());
    }
    
    /**
     * Cause a blank line to appear in the output at the current index.
     * This could be useful for, e.g., adding the deathdate to an event object
     * and then adding a blank line before the commands.
     * @since EUGFile 1.04.00pre1
     */
    public void addBlankLine() {
        allWritable.add(new InlineComment(""));
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" toFileString ">
    
    public void toFileString(final BufferedWriter bw) throws IOException {
        toFileString(bw, Style.DEFAULT);
    }
    
    public void toFileString(final BufferedWriter bw, final Style style) throws IOException {
        toFileString(bw, "", style);
    }
    
    public void toFileString(final BufferedWriter bw, String comment) throws IOException {
        toFileString(bw, comment, Style.DEFAULT);
    }
    
    public void toFileString(final BufferedWriter bw, String comment, Style style) throws IOException {
        //special starter method made for root
        //root is a "virtual" document node that does not appear in file
        //so we start by writing children directly
        
        if (comment != null && comment.length() != 0) {
            new HeaderComment(comment).toFileString(bw, 0, style);
            bw.newLine();
        }
        
        // Changed from foreach
        // EU4 saves require the last node (a checksum) to be the last line of the file
        for (Iterator<WritableObject> it = allWritable.iterator(); it.hasNext();) {
            WritableObject obj = it.next();
            obj.toFileString(bw, 0, style);
            if (it.hasNext())
                bw.newLine();
        }
        
        bw.close();
    }
    
    @Override
    public void toFileString(final BufferedWriter bw, int depth) throws IOException {
        toFileString(bw, depth, Style.DEFAULT);
    }
    
    @Override
    public void toFileString(final BufferedWriter bw, int depth, Style style) throws IOException {
        
        if (name.equals("root")) {
            toFileString(bw, style);
            return;
        }
        
        final boolean parentSameLine = style.isInline(parent);
        final boolean sameLine = parentSameLine || style.isInline(this);
        
        if (!parentSameLine && headComment != null) {
            headComment.toFileString(bw, depth, style);
        }
        
        final String localtab = (parentSameLine ? "" : style.getTab(depth));
        
        bw.write(localtab);
        
        if (!"".equals(name)) {
            bw.write(name);
            style.printEqualsSign(bw, depth);
            style.printOpeningBrace(bw, depth);
        } else {
            style.printOpeningBrace(bw, depth);
        }
        
        if (!sameLine)
            bw.newLine();
        
        for (WritableObject obj : allWritable) {
            if (obj instanceof ObjectVariable || obj instanceof Comment) {
                if (!sameLine) {
                    style.printTab(bw, depth+1);
                }
            }
            
            obj.toFileString(bw, depth+1, style);
            
            if (sameLine)
                bw.write(' ');
            else
                bw.newLine();
        }
        
        if (parentSameLine) {
            bw.write('}');
        } else if (sameLine) {
            bw.write("} ");
            if (inlineComment != null)
                inlineComment.toFileString(bw, depth, style);
        } else {
            bw.write(localtab + "}");
            if (inlineComment != null) {
                bw.write(" ");
                inlineComment.toFileString(bw, depth, style);
            }
            if (style.newLineAfterObject())
                bw.newLine();
        }
    }
    
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Overrides ">
    
    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        else if (other == null || !(other instanceof GenericObject))
            return false;
        
        return equals((GenericObject)other);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        //hash = 47 * hash + (this.headComment != null ? this.headComment.hashCode() : 0);
        //hash = 47 * hash + (this.inlineComment != null ? this.inlineComment.hashCode() : 0);
        hash = 47 * hash + (this.allWritable != null ? this.allWritable.hashCode() : 0);
        return hash;
    }
    
    private boolean equals(GenericObject other) {
//        if (parent == other.parent &&
//                (headComment != null ? headComment.equals(other.headComment) : other.headComment == null) &&
//                (inlineComment != null ? inlineComment.equals(other.inlineComment) : other.inlineComment == null)) {
        if (!name.equals(other.name))
            return false;
        
        if (!allWritable.equals(other.allWritable))
            return false;
        
        return true;
//        } else {
//            return false;
//        }
    }
    
    /**
     * Creates and returns a shallow copy of this object. Note that children
     * are not cloned, but the lists of children are.
     * @return a shallow copy of this object.
     */
    @Override
    public GenericObject clone() {
        final GenericObject retValue = new GenericObject(parent, name);
        
        retValue.children.addAll(children);
        retValue.lists.addAll(lists);
        retValue.values.addAll(values);
        retValue.generalComments.addAll(generalComments);
        retValue.allWritable.addAll(allWritable);
        
        if (headComment != null)
            retValue.setHeadComment(headComment.getComment());
        if (inlineComment != null)
            retValue.setInlineComment(inlineComment.getComment());
        
        return retValue;
    }
    
    public GenericObject deepClone() {
        final GenericObject retValue = new GenericObject(parent, name);
        
        for (WritableObject obj : allWritable) {
            if (obj instanceof GenericObject) {
                GenericObject go = ((GenericObject)obj).deepClone();
                retValue.addChild(go);
            } else if (obj instanceof GenericList) {
                GenericList gl = ((GenericList)obj).clone();
                retValue.addList(gl);
            } else if (obj instanceof ObjectVariable) {
                ObjectVariable ov = ((ObjectVariable)obj).clone();
                retValue.values.add(ov);
                retValue.allWritable.add(ov);
            } else if (obj instanceof InlineComment) {
                Comment com = ((InlineComment)obj).clone();
                retValue.generalComments.add(com);
                retValue.allWritable.add(com);
            }
        }
            
        if (headComment != null)
            retValue.setHeadComment(headComment.getComment());
        if (inlineComment != null)
            retValue.setInlineComment(inlineComment.getComment());
        
        return retValue;
    }
    
    public String toString(Style style) {
        final java.io.StringWriter sw = new java.io.StringWriter();
        final BufferedWriter bw = new BufferedWriter(sw);
        
        try {
            this.toFileString(bw, 0, style);
            bw.close();
        } catch (IOException ex) {  // won't happen
            throw new Error(ex);
        }
        
        return sw.toString();
    }
    
    @Override
    public String toString() {
        return toString(Style.DEFAULT);
    }
    
    public String childrenToString() {
        final java.io.StringWriter sw = new java.io.StringWriter();
        final BufferedWriter bw = new BufferedWriter(sw);
        
        try {
            this.toFileString(bw);
            bw.close();
        } catch (IOException ex) {  // won't happen
            throw new Error(ex);
        }
        
        return sw.toString();
    }
    
    // </editor-fold>
}
