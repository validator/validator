package fi.iki.hsivonen.verifierservlet;

import java.io.IOException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public interface InfoErrorHandler extends ErrorHandler {

    public abstract void end(String successMessage, String failureMessage) throws SAXException;

    public abstract void start() throws SAXException;

    /**
     * @param e
     */
    public abstract void info(String str) throws SAXException;

    /**
     * @param e
     */
    public abstract void ioError(IOException e) throws SAXException;

    /**
     * @param e
     * @throws SAXException
     */
    public abstract void internalError(Throwable e, String message)
            throws SAXException;

    /**
     * @param e
     */
    public abstract void schemaError(Exception e) throws SAXException;

    
    public abstract boolean isErrors();
}