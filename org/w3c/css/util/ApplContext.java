/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id$
 */
package org.w3c.css.util;


import org.w3c.css.css.StyleSheet;
import org.w3c.css.parser.Frame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Philippe Le Hegaret
 * @version $Revision$
 */
public class ApplContext {

    // the charset of the first source (url/uploaded/text)
    public static Charset defaultCharset;
    public static Charset utf8Charset;

    static {
        try {
            defaultCharset = Charset.forName("iso-8859-1");
            utf8Charset = Charset.forName("utf-8");
        } catch (Exception ex) {
            // we are in deep trouble here
            defaultCharset = null;
            utf8Charset = null;
        }
    }

    private class BomEncoding {
        Charset uriCharset = null;
        boolean fromBom = false;

        private BomEncoding(Charset c, boolean fb) {
            uriCharset = c;
            fromBom = fb;
        }

        private BomEncoding(Charset c) {
            this(c, false);
        }

        private BomEncoding() {
        }
    }

    // charset definition of traversed URLs
    private HashMap<URL, BomEncoding> uricharsets = null;

    // namespace definitions
    private HashMap<URL, HashMap<String, String>> namespaces = null;

    // default prefix
    public static String defaultPrefix = "*defaultprefix*";
    public static String noPrefix = "*noprefix*";

    private ArrayList<URL> linkedmedia = new ArrayList<URL>();

    int readTimeout = 60000; // ms
    int connectTimeout = 5000; // ms

    String credential = null;
    String lang;
    Messages msgs;
    Frame frame;
    StyleSheet styleSheet = null;
    CssVersion version = CssVersion.getDefault();
    CssProfile profile = CssProfile.NONE;
    String input;
    Class cssselectorstyle;
    int origin = -1;
    String medium;
    private String link;
    int warningLevel = 0;
    boolean treatVendorExtensionsAsWarnings = false;
    boolean treatCssHacksAsWarnings = false;
    boolean suggestPropertyName = true;

    private String propertyKey = null;

    public boolean followlinks() {
        return followlinks;
    }

    public void setFollowlinks(boolean followlinks) {
        this.followlinks = followlinks;
    }

    boolean followlinks = true;

    FakeFile fakefile = null;
    String faketext = null;
    Charset faketextcharset = null;
    URL fakeurl = null;

    URL referrer = null;

    /**
     * Creates a new ApplContext
     */
    public ApplContext(String lang) {
        this.lang = lang;
        msgs = new Messages(lang);
    }

    public int getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(int warningLevel) {
        this.warningLevel = warningLevel;
    }

    public ArrayList<URL> getLinkedURIs() {
        return linkedmedia;
    }

    public void addLinkedURI(URL url) {
        if (url != null) {
            linkedmedia.add(url);
        }
    }

    // as ugly as everything else
    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
        frame.ac = this;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setStyleSheet(StyleSheet styleSheet) {
        this.styleSheet = styleSheet;
    }

    public StyleSheet getStyleSheet() {
        return styleSheet;
    }

    public Class getCssSelectorsStyle() {
        return cssselectorstyle;
    }

    public void setCssSelectorsStyle(Class s) {
        cssselectorstyle = s;
    }

    public Messages getMsg() {
        return msgs;
    }

    public String getContentType() {
        return (msgs != null) ? msgs.getString("content-type") : null;
    }

    public String getContentLanguage() {
        return (msgs != null) ? msgs.getString("content-language") : null;
    }

    /**
     * Searches the properties list for a content-encoding one. If it does not
     * exist, searches for output-encoding-name. If it still does not exists,
     * the method returns the default utf-8 value
     *
     * @return the output encoding of this ApplContext
     */
    public String getContentEncoding() {
        // return (msgs != null) ? msgs.getString("content-encoding") : null;
        String res = null;
        if (msgs != null) {
            res = msgs.getString("content-encoding");
            if (res == null) {
                res = msgs.getString("output-encoding-name");
            }
            if (res != null) {
                // if an encoding has been found, return it
                return res;
            }
        }
        // default encoding
        return Utf8Properties.ENCODING;
    }

    public String getLang() {
        return lang;
    }

    public void setCssVersion(String cssversion) {
        version = CssVersion.resolve(this, cssversion);
        propertyKey = null;
    }

    public void setCssVersion(CssVersion version) {
        this.version = version;
        propertyKey = null;
    }

    public String getCssVersionString() {
        return version.toString();
    }

    public CssVersion getCssVersion() {
        return version;
    }

    public void setProfile(String profile) {
        this.profile = CssProfile.resolve(this, profile);
        propertyKey = null;
    }

    /**
     * get the String used to fetch the relevant property file
     */
    public String getPropertyKey() {
        if (propertyKey != null) {
            return propertyKey;
        }
        if (profile == CssProfile.SVG && version == CssVersion.CSS3) {
            propertyKey = version.toString() + profile.toString();
            return propertyKey;
        }
        if (profile != CssProfile.EMPTY && profile != CssProfile.NONE) {
            propertyKey = profile.toString();
        } else {
            propertyKey = version.toString();
        }
        return propertyKey;
    }

    public CssProfile getCssProfile() {
        return profile;
    }

    public String getProfileString() {
        return profile.toString();
    }

    public void setCssVersionAndProfile(String spec) {
        // for things like SVG, version will be set to the default one
        // CSS21 in that case, as defined in CssVersion
        // and profile will be resolved to svg.
        //
        // if the version resolve then profile will default to NONE

        // TODO should we check profile first and if SVG or MOBILE
        // set specific version of CSS (like CSS2 and not CSS21 for MOBILE) ?
        if ((spec == null) || spec.isEmpty()) {
            version = CssVersion.getDefault();
            profile = CssProfile.SVG;
        } else {
            String low = spec.toLowerCase();
            version = CssVersion.resolve(this, low);
            profile = CssProfile.resolve(this, low);
            // some special cases...
            // http://www.atsc.org/cms/index.php/standards/published-standards/71-atsc-a100-standard
            if (profile.equals(CssProfile.ATSCTV)) {
                version = CssVersion.CSS2;
            }
        }
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getOrigin() {
        return origin;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getMedium() {
        return medium;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String queryString) {
        this.link = queryString;
    }

    public boolean getTreatVendorExtensionsAsWarnings() {
        return treatVendorExtensionsAsWarnings;
    }

    /**
     * Change the behaviour of error reporting for vendor extensions.
     *
     * @param treatVendorExtensionsAsWarnings
     */
    public void setTreatVendorExtensionsAsWarnings(
            boolean treatVendorExtensionsAsWarnings) {
        this.treatVendorExtensionsAsWarnings = treatVendorExtensionsAsWarnings;
    }

    public boolean getTreatCssHacksAsWarnings() {
        return treatCssHacksAsWarnings;
    }

    /**
     * Change the behaviour of error reporting for CSS Hacks.
     *
     * @param treatCssHacksAsWarnings
     */
    public void setTreatCssHacksAsWarnings(boolean treatCssHacksAsWarnings) {
        this.treatCssHacksAsWarnings = treatCssHacksAsWarnings;
    }

    public boolean getSuggestPropertyName() {
        return suggestPropertyName;
    }

    public void setSuggestPropertyName(boolean b) {
        suggestPropertyName = b;
    }

    private boolean isCharsetSupported(String charset) {
        if ("*".equals(charset)) {
            return true;
        }
        try {
            return Charset.isSupported(charset);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * used for storing the charset of the document in use
     * and its update by a @charset statement, or through
     * automatic discovery
     */
    public void setCharsetForURL(URL url, String charset, boolean from_bom) {
        if (uricharsets == null) {
            uricharsets = new HashMap<>();
        }
        Charset c = null;
        try {
            c = Charset.forName(charset);
        } catch (IllegalCharsetNameException icex) {
            // FIXME add a warning in the CSS
        } catch (UnsupportedCharsetException ucex) {
            // FIXME inform about lack of support
        }
        if (c != null) {
            uricharsets.put(url, new BomEncoding(c, from_bom));
        }
    }

    /**
     * used for storing the charset of the document in use
     * and its update by a @charset statement, or through
     * automatic discovery
     */
    public void setCharsetForURL(URL url, Charset charset) {
        if (uricharsets == null) {
            uricharsets = new HashMap<URL, BomEncoding>();
        }
        uricharsets.put(url, new BomEncoding(charset));
    }

    public boolean isCharsetFromBOM(URL url) {
        BomEncoding b;
        if (uricharsets == null) {
            return false;
        }
        b = uricharsets.get(url);
        if (b != null) {
            return b.fromBom;
        }
        return false;
    }

    /**
     * used for storing the charset of the document in use
     * and its update by a @charset statement, or through
     * automatic discovery
     */
    public String getCharsetForURL(URL url) {
        BomEncoding b;
        if (uricharsets == null) {
            return null;
        }
        b = uricharsets.get(url);
        if (b != null) {
            return b.uriCharset.toString();
        }
        return null;
    }

    /**
     * used for storing the charset of the document in use
     * and its update by a @charset statement, or through
     * automatic discovery
     */
    public Charset getCharsetObjForURL(URL url) {
        BomEncoding b;
        if (uricharsets == null) {
            return null;
        }
        b = uricharsets.get(url);
        if (b == null) {
            return null;
        }
        return b.uriCharset;
    }

    /**
     * store content of uploaded file
     */
    public void setFakeFile(FakeFile fakefile) {
        this.fakefile = fakefile;
    }

    /**
     * store content of entered text
     */
    public void setFakeText(String faketext, Charset faketextcharset) {
        this.faketext = faketext;
        this.faketextcharset = faketextcharset;

    }

    public InputStream getFakeInputStream(URL source)
            throws IOException {
        InputStream is = null;
        Charset c = null;
        if (fakefile != null) {
            is = fakefile.getInputStream();
        }
        if (faketext != null) {
            is = new ByteArrayInputStream(faketext.getBytes(faketextcharset));
            c = faketextcharset;
        }
        if (is == null) {
            return null;
        }
        if (c == null) {
            c = getCharsetObjForURL(source);
        }
        if (c == null) {
            UnicodeInputStream uis = new UnicodeInputStream(is);
            String guessedCharset = uis.getEncodingFromStream();
            if (guessedCharset != null) {
                setCharsetForURL(source, guessedCharset, true);
            }
            return uis;
        } else {
            if (utf8Charset.compareTo(c) == 0) {
                return new UnicodeInputStream(is);
            }
        }
        return is;
    }

    public boolean isInputFake() {
        return ((faketext != null) || (fakefile != null));
    }

    public void setFakeURL(String fakeurl) {
        try {
            this.fakeurl = new URL(fakeurl);
        } catch (Exception ex) {
        }
    }

    public URL getFakeURL() {
        return fakeurl;
    }

    /**
     * support for namespaces
     */
    public void setNamespace(URL url, String prefix, String nsname) {
        if (namespaces == null) {
            namespaces = new HashMap<URL, HashMap<String, String>>();
        }
        // reformat the prefix if null.
        String realPrefix = ((prefix != null) && !prefix.isEmpty()) ? prefix : defaultPrefix;

        HashMap<String, String> nsdefs = namespaces.get(url);
        if (nsdefs == null) {
            nsdefs = new HashMap<String, String>();
            nsdefs.put(realPrefix, nsname);
            namespaces.put(url, nsdefs);
        } else {
            // do we need to check if we have a redefinition ?
            nsdefs.put(realPrefix, nsname);
        }
    }

    // true if a namespace is defined in the document (CSS fragment)
    // defined by the URL, with prefix "prefix"
    public boolean isNamespaceDefined(URL url, String prefix) {
        if (prefix == null) { // no prefix, always match
            return true;
        }
        if (prefix.equals("*")) { // any ns, always true
            return true;
        }
        String realPrefix = (!prefix.isEmpty()) ? prefix : defaultPrefix;
        if (namespaces == null) { // no ns defined -> fail
            return false;
        }
        HashMap<String, String> nsdefs = namespaces.get(url);
        if (nsdefs == null) {
            return false;
        }
        return nsdefs.containsKey(realPrefix);
    }

    /**
     * Set the current referrer for possible linked style sheets
     *
     * @param referrer the referring URL
     */
    public void setReferrer(URL referrer) {
        this.referrer = referrer;
    }

    /**
     * get the referrer URL (or null if not relevant)
     *
     * @return an URL
     */
    public URL getReferrer() {
        return referrer;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int timeout) {
        readTimeout = timeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int timeout) {
        connectTimeout = timeout;
    }
}
