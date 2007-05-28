package fi.iki.hsivonen.xml;

import java.io.InputStream;
import java.io.Reader;

import org.xml.sax.InputSource;

/**
 * @version $Id$
 * @author hsivonen
 */
public class TypedInputSource extends InputSource {

    private String type;
    
    /**
     * 
     */
    public TypedInputSource() {
        super();
    }

    /**
     * @param arg0
     */
    public TypedInputSource(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public TypedInputSource(InputStream arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public TypedInputSource(Reader arg0) {
        super(arg0);
    }

    
    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }
}
