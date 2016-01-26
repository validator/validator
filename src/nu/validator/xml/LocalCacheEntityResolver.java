package nu.validator.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 * @author hsivonen
 */
public class LocalCacheEntityResolver implements EntityResolver {

    private Map<String, String> pathMap;

    private EntityResolver delegate;
    
    private boolean allowRnc = false;

    /**
     * The map must be safe for concurrent reads.
     * 
     * @param pathMap 
     * @param delegate
     */
    public LocalCacheEntityResolver(Map<String, String> pathMap, EntityResolver delegate) {
        this.pathMap = pathMap;
        this.delegate = delegate;
    }
    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        String path = pathMap.get(systemId);
        if(path != null) {
            File f = new File(path);
            if (f.exists()) {
                TypedInputSource is = new TypedInputSource();
                is.setByteStream(new FileInputStream(f));
                is.setSystemId(systemId);
                is.setPublicId(publicId);
                if(systemId.endsWith(".rnc")) {
                    is.setType("application/relax-ng-compact-syntax");
                    if(!allowRnc) {
                        throw new IOException("Not an XML resource: " + systemId);
                    }
                } else if(systemId.endsWith(".dtd")) {
                    is.setType("application/xml-dtd");  
                } else if(systemId.endsWith(".ent")) {
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
     * @param allowRnc The allowRnc to set.
     */
    public void setAllowRnc(boolean allowRnc) {
        this.allowRnc = allowRnc;
    }
}