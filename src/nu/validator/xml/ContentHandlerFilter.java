package nu.validator.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 * @author hsivonen
 */
public class ContentHandlerFilter implements ContentHandler {

    protected ContentHandler contentHandler;
    
    protected ErrorHandler errorHandler;
    
    protected Locator locator;
    
    /**
     * @param chars
     * @param start
     * @param length
     * @throws org.xml.sax.SAXException
     */
    public void characters(char[] chars, int start, int length) throws SAXException {
        contentHandler.characters(chars, start, length);
    }
    /**
     * @throws org.xml.sax.SAXException
     */
    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }
    /**
     * @param uri
     * @param local
     * @param qName
     * @throws org.xml.sax.SAXException
     */
    public void endElement(String uri, String local, String qName)
            throws SAXException {
        contentHandler.endElement(uri, local, qName);
    }
    /**
     * @param arg0
     * @throws org.xml.sax.SAXException
     */
    public void endPrefixMapping(String arg0) throws SAXException {
        contentHandler.endPrefixMapping(arg0);
    }
    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws org.xml.sax.SAXException
     */
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        contentHandler.ignorableWhitespace(arg0, arg1, arg2);
    }
    /**
     * @param arg0
     * @param arg1
     * @throws org.xml.sax.SAXException
     */
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        contentHandler.processingInstruction(arg0, arg1);
    }
    /**
     * @param locator
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        contentHandler.setDocumentLocator(locator);
    }
    /**
     * @param arg0
     * @throws org.xml.sax.SAXException
     */
    public void skippedEntity(String arg0) throws SAXException {
        contentHandler.skippedEntity(arg0);
    }
    /**
     * @throws org.xml.sax.SAXException
     */
    public void startDocument() throws SAXException {
        contentHandler.startDocument();
    }
    /**
     * @param uri
     * @param local
     * @param qName
     * @param attrs
     * @throws org.xml.sax.SAXException
     */
    public void startElement(String uri, String local, String qName,
            Attributes attrs) throws SAXException {
        contentHandler.startElement(uri, local, qName, attrs);
    }
    /**
     * @param arg0
     * @param arg1
     * @throws org.xml.sax.SAXException
     */
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        contentHandler.startPrefixMapping(arg0, arg1);
    }
    protected void fatal(String message) throws SAXException {
        SAXParseException spe = new SAXParseException(message, locator);
        errorHandler.fatalError(spe);
        throw spe;
    }
    protected void err(String message) throws SAXException {
        SAXParseException spe = new SAXParseException(message, locator);
        errorHandler.error(spe);
    }
    /**
     * Returns the contentHandler.
     * 
     * @return the contentHandler
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }
    /**
     * Sets the contentHandler.
     * 
     * @param contentHandler the contentHandler to set
     */
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }
    /**
     * Returns the errorHandler.
     * 
     * @return the errorHandler
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    /**
     * Sets the errorHandler.
     * 
     * @param errorHandler the errorHandler to set
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
