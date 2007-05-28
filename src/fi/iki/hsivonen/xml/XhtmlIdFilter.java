package fi.iki.hsivonen.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class XhtmlIdFilter extends XMLFilterImpl {

    public XhtmlIdFilter() {
        super();
    }

    public XhtmlIdFilter(XMLReader parent) {
        super(parent);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml".equals(uri)) {
            int index = atts.getIndex("", "id");
            if (index != -1 && !"ID".equals(atts.getType(index))) {
                AttributesImpl ai = new AttributesImpl(atts);
                ai.setType(index, "ID");
                super.startElement(uri, localName, qName, ai);
            } else {
                super.startElement(uri, localName, qName, atts);                
            }
        } else {
            super.startElement(uri, localName, qName, atts);
        }
    }
}
