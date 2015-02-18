package nu.validator.localentities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import nu.validator.xml.TypedInputSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version $Id: LocalCacheEntityResolver.java 74 2008-09-17 10:34:15Z hsivonen $
 * @author hsivonen
 */
public class LocalCacheEntityResolver implements EntityResolver {

    private static final ClassLoader LOADER = LocalCacheEntityResolver.class.getClassLoader();

    private static final Map<String, String> PATH_MAP = new HashMap<String, String>();

    static {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    LOADER.getResourceAsStream("nu/validator/localentities/files/entitymap"), "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                if ("".equals(line.trim())) {
                    break;
                }
                String s[] = line.split("\t");
                PATH_MAP.put(s[0], "nu/validator/localentities/files/" + s[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getPresetsAsStream() {
        return LOADER.getResourceAsStream("nu/validator/localentities/files/presets");
    }

    public static InputStream getHtml5SpecAsStream() {
        return LOADER.getResourceAsStream("nu/validator/localentities/files/html5spec");
    }
    
    private EntityResolver delegate;

    private boolean allowRnc = false;

    /**
     * The map must be safe for concurrent reads.
     * 
     * @param pathMap
     * @param delegate
     */
    public LocalCacheEntityResolver(EntityResolver delegate) {
        this.delegate = delegate;
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        String path = PATH_MAP.get(systemId);
        if (path != null) {
            InputStream stream = LOADER.getResourceAsStream(path);
            if (stream != null) {
                TypedInputSource is = new TypedInputSource();
                is.setByteStream(stream);
                is.setSystemId(systemId);
                is.setPublicId(publicId);
                if (systemId.endsWith(".rnc")) {
                    is.setType("application/relax-ng-compact-syntax");
                    if (!allowRnc) {
                        throw new IOException("Not an XML resource: "
                                + systemId);
                    }
                } else if (systemId.endsWith(".dtd")) {
                    is.setType("application/xml-dtd");
                } else if (systemId.endsWith(".ent")) {
                    is.setType("application/xml-external-parsed-entity");
                } else {
                    is.setType("application/xml");
                }
                return is;
            }
        }
        return delegate.resolveEntity(publicId, systemId);
    }

    /**
     * @return Returns the allowRnc.
     */
    public boolean isAllowRnc() {
        return allowRnc;
    }

    /**
     * @param allowRnc
     *            The allowRnc to set.
     */
    public void setAllowRnc(boolean allowRnc) {
        this.allowRnc = allowRnc;
    }
}