
package editor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
    private static final int REV = 0;
    
    private static final String PROJECT_URL = "http://sourceforge.net/projects/eug";
    private static final String UPDATE_URL = "http://eug.sourceforge.net/version.xml";
    
    private static String latestVersion = null;
    private static String latestVersionLink = null;
    
    private static Date lastCheck = new Date(0);
    private static final long ONE_HOUR_MS = 3600000;
    
    
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
            JAXBContext jaxbContext = JAXBContext.newInstance(Release.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Release r = (Release) jaxbUnmarshaller.unmarshal(new URL(UPDATE_URL));
            if (isNewVersion(r.version)) {
                latestVersion = r.version;
                latestVersionLink = r.url;
                return true;
            }
        } catch (JAXBException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
        }
        return false;
    }
    
    private static boolean isNewVersion(String version) {
        String[] split = version.split("\\.");
        int major = 0, minor = 0, rev = 0;
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
    
    @XmlRootElement
    public static class Release {
        @XmlElement(defaultValue = "false")
        public boolean recommended;
        @XmlElement
        public String version;
        @XmlElement
        public String url;
    }
}
