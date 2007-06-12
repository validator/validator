package fi.iki.hsivonen.verifierservlet;

import java.io.IOException;
import java.io.Writer;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TextEmittingErrorHandler extends AbstractErrorHandler {
    
    private final Writer writer;
    
    /**
     * @param writer
     */
    public TextEmittingErrorHandler(final Writer writer) {
        this.writer = writer;
    }
    

    @Override
    protected void errorImpl(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void fatalErrorImpl(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void infoImpl(String str) throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void internalErrorImpl(String message) throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void ioErrorImpl(IOException e) throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void schemaErrorImpl(Exception e) throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void warningImpl(SAXParseException e) throws SAXException {
        // TODO Auto-generated method stub

    }


    /**
     * @see fi.iki.hsivonen.verifierservlet.AbstractErrorHandler#end(java.lang.String, java.lang.String)
     */
    @Override
    public void end(String successMessage, String failureMessage) throws SAXException {
        // TODO Auto-generated method stub
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }


    /**
     * @see fi.iki.hsivonen.verifierservlet.AbstractErrorHandler#start(java.lang.String)
     */
    @Override
    public void start(String documentUri) throws SAXException {
        // TODO Auto-generated method stub
    }

}
