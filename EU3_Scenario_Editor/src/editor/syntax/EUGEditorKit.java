/*
 * EUGEditorKit.java
 *
 * Created on June 26, 2007, 11:56 AM
 */

package eug.syntax;

import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.ViewFactory;

/**
 * The only class that is absolutely necessary to have syntax highlighting. To
 * use it in the default form, call
 * <code>myJEditorPane.setEditorKit(new EUGEditorKit());</code>. Further
 * customization is possible by using something like the following:
 * <pre>
 * EUGContext myContext = new EUGContext();
 * DefaultEUGStyleSet myStyleSet = myContext.getStyles();
 * // ... customize style set ...
 * EUGEditorKit myEditorKit = new EUGEditorKit();
 * myEditorKit.setStylePreferences(myContext);
 * myJEditorPane.setEditorKit(myEditorKit);
 * </pre>
 * @see EUGContext
 * @see DefaultEUGStyleSet
 * @author Michael Myers
 */
public class EUGEditorKit extends DefaultEditorKit {
    
    private static final long serialVersionUID = 1L;
    
    private EUGContext preferences;
    
    /** Creates a new instance of EUGEditorKit */
    public EUGEditorKit() {
        super();
    }
    
    public EUGContext getStylePreferences() {
        if (preferences == null) {
            preferences = new EUGContext();
        }
        return preferences;
    }
    
    public void setStylePreferences(EUGContext prefs) {
        preferences = prefs;
    }
    
    /**
     * Creates an uninitialized text storage model
     * that is appropriate for this type of editor.
     * @return the model
     */
    @Override
    public Document createDefaultDocument() {
        return new EUGDocument();
    }
    
    /**
     * Fetches a factory that is suitable for producing
     * views of any models that are produced by this
     * kit.
     * @return the view factory
     */
    @Override
    public ViewFactory getViewFactory() {
        return getStylePreferences();
    }
    
    /**
     * Gets the MIME type of the data that this
     * kit represents support for.
     * @return the string "text/eug"
     */
    @Override
    public String getContentType() {
        return "text/eug";
    }
    
    @Override
    public Object clone() {
        EUGEditorKit kit = (EUGEditorKit) super.clone();
        kit.preferences = preferences;
        return kit;
    }
    
}
