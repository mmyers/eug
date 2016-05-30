/*
 * EUGFileIO.java
 *
 * Created on July 11, 2006, 3:43 PM
 */

package eug.parser;

import eug.shared.GenericObject;
import eug.shared.Style;
import eug.shared.Version;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Static class which performs I/O of {@link GenericObject GenericObjects}.
 * @author Michael Myers
 */
public final class EUGFileIO {
    
    /**
     * Constant string which can be passed as the <code>comment</code>
     * parameter into one of the save functions, indicating that no header
     * comment is desired.
     */
    public static final String NO_COMMENT = "&^$#"; // not likely to ever appear elsewhere
    
    /** No instances are allowed. */
    private EUGFileIO() { }
    
    /**
     * Loads a {@link GenericObject} tree from the given filename.
     * @param filename the name of the file to parse.
     * @return the root of the parsed tree, or <CODE>null</CODE>
     * if there was an error.
     */
    public static GenericObject load(String filename) {
        final CWordFile f = new CWordFile();
        return f.load(filename);
    }
    
    /**
     * Loads a {@link GenericObject} tree from the given file.
     * @param file the file to parse.
     * @return the root of the parsed tree, or <CODE>null</CODE>
     * if there was an error.
     */
    public static GenericObject load(File file) {
        return load(file.getAbsolutePath());
    }
    
    /**
     * Loads a {@link GenericObject} tree from the given string.
     * @param str the string to parse.
     * @return the root of the parsed tree.
     */
    public static GenericObject loadFromString(String str) {
        final CWordFile f = new CWordFile();
        return f.loadFromString(str);
    }
    
    public static GenericObject load(String filename, ParserSettings settings) {
        final CWordFile f = new CWordFile(settings);
        return f.load(filename);
    }
    
    public static GenericObject load(File file, ParserSettings settings) {
        return load(file.getAbsolutePath(), settings);
    }

    /**
     * Loads all files into a single GenericObject.
     * Meant to be used with FilenameResolver.listFiles().
     */
    public static GenericObject loadAll(File[] files) {
        if (files == null)
            return null;
        if (files.length == 1)
            return load(files[0]);

        GenericObject root = new GenericObject();
        for (java.io.File file : files) {
            GenericObject obj = load(file);
            if (obj != null)
                root.addAllChildren(obj);
        }
        return root;
    }

    /**
     * Loads all files into a single GenericObject.
     * Meant to be used with FilenameResolver.listFiles().
     */
    public static GenericObject loadAll(File[] files, ParserSettings settings) {
        if (files == null)
            return null;
        if (files.length == 1)
            return load(files[0], settings);
        
        GenericObject root = new GenericObject();
        for (java.io.File file : files) {
            GenericObject obj = load(file, settings);
            if (obj != null)
                root.addAllChildren(obj);
        }
        return root;
    }

    public static GenericObject loadAll(String[] files) {
        if (files == null)
            return null;
        if (files.length == 1)
            return load(files[0]);

        GenericObject root = new GenericObject();
        for (String filename : files) {
            GenericObject obj = load(filename);
            if (obj != null)
                root.addAllChildren(obj);
        }
        return root;
    }

    public static GenericObject loadAll(String[] files, ParserSettings settings) {
        if (files == null)
            return null;
        if (files.length == 1)
            return load(files[0], settings);
        
        GenericObject root = new GenericObject();
        for (String filename : files) {
            GenericObject obj = load(filename, settings);
            if (obj != null)
                root.addAllChildren(obj);
        }
        return root;
    }
    
    /**
     * Loads a {@link GenericObject} tree from the given string.
     * @param str the string to parse.
     * @param settings the set of rules to use during parsing.
     * @return the root of the parsed tree.
     */
    public static GenericObject loadFromString(String str, ParserSettings settings) {
        final CWordFile f = new CWordFile(settings);
        return f.loadFromString(str);
    }
    
    /**
     * Saves the given {@link GenericObject} tree to the given filename,
     * overwriting it if it exists.
     * A comment with the version of the parser will be added to the top.
     * @param obj the root of the <CODE>GenericObject</CODE> tree to save.
     * @param filename the name of the file that the tree will be saved to.
     * @return <CODE>true</CODE> if the tree was successfully saved.
     * @see Version#getVersion()
     */
    public static boolean save(final GenericObject obj, String filename) {
        return save(obj, filename, "File generated by " + Version.getVersion());
    }
    
    /**
     * Save the given {@link GenericObject} tree to the given filename,
     * overwriting it if it exists.
     * @param obj the tree to save.
     * @param filename the name of the file to save the tree to.
     * @param comment a comment to write at the start of the file. If it is
     * {@link #NO_COMMENT}, no comment (not even the standard header) will be
     * written.
     * @return <CODE>true</CODE> if the save was successful.
     * @see #NO_COMMENT
     */
    public static boolean save(final GenericObject obj, String filename, String comment) {
        return save(obj, filename, comment, true);
    }
    
    public static boolean save(final GenericObject obj, String filename, String comment, boolean overwrite) {
        return save(obj, filename, comment, overwrite, Style.DEFAULT);
    }
    
    public static boolean save(final GenericObject obj, String filename, String comment, boolean overwrite, Style style) {
        //error check
        if (obj == null)
            return false;
        
//        long startTime = System.nanoTime();
        
        try {
            if (comment == null || comment.length() == 0)
                comment = NO_COMMENT;
            
            if (!overwrite && new File(filename).canRead()) {
                BufferedWriter w = new BufferedWriter(new FileWriter(filename, true));
                obj.toFileString(w,
                        String.format("Added on %1$te-%1$tm-%1$tY", Calendar.getInstance()) +
                        (NO_COMMENT.equals(comment) ? "" : "\n\n" + comment),
                        style);
            } else {
                BufferedWriter w = new BufferedWriter(new FileWriter(filename));
                if (filename.endsWith(".eu4")) {
                    w.write("EU4txt"); // magic EU4 string
                    w.newLine();
                } else if (filename.endsWith(".ck2")) {
                    w.write("CK2txt");
                    w.newLine();
                }
                obj.toFileString(w, (NO_COMMENT.equals(comment) ? "" : comment), style);
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        
//        if (CWordFile.timingInfo)
//            System.out.println("Saving took " + (System.nanoTime()-startTime) + " ns.\n");
        
        return true;
    }
    
}
