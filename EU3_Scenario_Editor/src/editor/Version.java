
package editor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class holds the current version of the editor and handles automatic
 * version checking, downloading the project RSS feed from SourceForge.net.
 * <p>
 * Version checks will be cached for an hour while the editor is running.
 */
public class Version {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Version.class.getName());
    
    private static final int MAJOR = 0;
    private static final int MINOR = 9;
    private static final int REV = 11;
    
    private static final String PROJECT_URL = "https://sourceforge.net/projects/eug";
    private static final String UPDATE_URL = "https://eug.sourceforge.net/version.xml";
    
    private static String latestVersion = null;
    private static String latestVersionLink = null;
    
    private static Date lastCheck = new Date(0);
    private static final long ONE_HOUR_MS = 3600000;
    
    private Version () {}
    
    private static boolean fetchLatestVersion() {
        if (new Date().getTime() - lastCheck.getTime() < ONE_HOUR_MS) {
            if (latestVersion != null)
                return isNewVersion(latestVersion);
        }
        
        try { 
            lastCheck = new Date();

            // the document will have only one item, something like:
            // <release>
            //  <recommended>true</recommended>
            //  <version>X.Y.Z</version>
            //  <url>http://sourceforge.net/whatever</url>
            // </release>
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new URL(UPDATE_URL).openStream());
            doc.normalize();
            
            Element release = (Element) doc.getElementsByTagName("release").item(0);
            
            //boolean recommended = Boolean.parseBoolean(release.getElementsByTagName("recommended").item(0).getTextContent());
            String version = release.getElementsByTagName("version").item(0).getTextContent();
            String url = release.getElementsByTagName("url").item(0).getTextContent();
            
            if (isNewVersion(version)) {
                latestVersion = version;
                latestVersionLink = url;
                return true;
            }
        } catch (ParserConfigurationException | SAXException | MalformedURLException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private static boolean isNewVersion(String version) {
        String[] split = version.split("\\.");
        int major = 0;
        int minor = 0;
        int rev = 0;
        if (split.length > 0)
            major = Integer.parseInt(split[0]);
        if (split.length > 1)
            minor = Integer.parseInt(split[1]);
        if (split.length > 2)
            rev = Integer.parseInt(split[2]);
        
        if (major > MAJOR)
            return true;
        if (minor > MINOR)
            return true;
        return rev > REV;
    }
    
    public static boolean isNewVersionAvailable() {
        return fetchLatestVersion();
    }
    
    public static String getLatestVersion() {
        return latestVersion;
    }
    
    public static String getLatestVersionLink() {
        return latestVersionLink;
    }
    
    public static String getCurrentVersion() {
        return MAJOR + "." + MINOR + "." + REV;
    }
    
    public static String getProjectURL() {
        return PROJECT_URL;
    }
}
