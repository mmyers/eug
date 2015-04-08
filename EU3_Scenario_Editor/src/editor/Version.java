
package editor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    private static final int MINOR = 8;
    private static final int REV = 7;
    
    private static final String PROJECT_URL = "http://sourceforge.net/projects/eug";
    private static final String RSS_URL = "http://sourceforge.net/projects/eug/rss?path=/Clausewitz%20Scenario%20Editor";
    private static final String UPDATE_URL = "http://eug.sourceforge.net/version.xml";
    
    private static String latestVersion = null;
    private static String latestVersionLink = null;
    
    private static Date lastCheck = new Date(0);
    private static final long ONE_HOUR_MS = 3600000;
    
    //                                                                format: Sun, 24 Aug 2014 07:13:40 UT
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'UT'");
    
    
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
    
    private static boolean fetchLatestVersionRSS() {
        if (new Date().getTime() - lastCheck.getTime() < ONE_HOUR_MS) {
            if (latestVersion != null)
                return isNewVersion(latestVersion);
        }
        
        try {
            lastCheck = new Date();
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(RSS_URL);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            // looking for items with the following format:
        
//    <item>
//      <title><![CDATA[/Clausewitz Scenario Editor/Scenario Editor X.Y.Z/Scenario_Editor_X.Y.Z.zip]]></title>
//      <link>(download URL here)</link>
//      <guid>(download URL here also)</guid>
//      <pubDate>Sun, 24 Aug 2014 07:13:40 UT</pubDate>
//      <description><![CDATA[/Clausewitz Scenario Editor/Scenario Editor X.Y.Z/Scenario_Editor_X.Y.Z.zip]]></description>
//      <files:sf-file-id xmlns:files="https://sourceforge.net/api/files.rdf#">15976589</files:sf-file-id>
//      <files:extra-info xmlns:files="https://sourceforge.net/api/files.rdf#">empty (Zip archive data)</files:extra-info>
//      <media:content xmlns:media="http://video.search.yahoo.com/mrss/" type="application/zip; charset=binary" url="(download URL)" filesize="279419">
//        <media:hash algo="md5">1eca86f3aceaa722badc35830eb75d99</media:hash>
//      </media:content>
//    </item>
            
            XPathExpression itemExpr = xpath.compile("/rss/channel/item");
            XPathExpression titleExpr = xpath.compile("title");
            XPathExpression linkExpr = xpath.compile("link");
            XPathExpression dateExpr = xpath.compile("pubDate");
            Date latest = new Date(0L);
            String latestTitle = "", latestLink = "";
            NodeList items = (NodeList) itemExpr.evaluate(doc, XPathConstants.NODESET);
            // need to find title and link in each
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                
                String title = titleExpr.evaluate(item);
                if (title.contains("Scenario Editor") && !title.contains("Code")) {
                    String link = linkExpr.evaluate(item);
                    String strdate = dateExpr.evaluate(item);
                    Date date = DATE_FORMAT.parse(strdate);
                    
                    if (date.after(latest)) {
                        latest = date;
                        latestTitle = title;
                        latestLink = link;
                    }
                }
            }
            
            // count backwards from the .zip until we meet an underscore
            String version = latestTitle.substring(latestTitle.lastIndexOf('_') + 1, latestTitle.lastIndexOf('.'));
            
            if (isNewVersion(version)) {
                latestVersion = version;
                latestVersionLink = latestLink;
                return true;
            }
            return false;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            log.log(Level.SEVERE, "", ex);
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
