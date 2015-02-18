/*
 * Copyright (c) 2008 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.xml;

import java.io.IOException;
import java.net.MalformedURLException;

import org.whattf.io.DataUri;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;

public class DataUriEntityResolver implements EntityResolver {

    private final EntityResolver delegate;
    
    private final ErrorHandler errorHandler;

    private boolean laxContentType;

    private boolean allowRnc = false;

    private boolean allowHtml = false;

    private boolean allowXhtml = false;

    private boolean acceptAllKnownXmlTypes = false;

    private boolean allowGenericXml = true;

    private final IRIFactory iriFactory;

    private final ContentTypeParser contentTypeParser;
    
    /**
     * @param delegate
     */
    public DataUriEntityResolver(EntityResolver delegate, boolean laxContentType,
            ErrorHandler errorHandler) {
        this.laxContentType = laxContentType;
        this.errorHandler = errorHandler;
        this.iriFactory = new IRIFactory();
        this.iriFactory.useSpecificationXMLSystemID(true);
        this.iriFactory.useSchemeSpecificRules("data", true);
        this.contentTypeParser = new ContentTypeParser(errorHandler,
                laxContentType, this.allowRnc, this.allowHtml, this.allowXhtml,
                this.acceptAllKnownXmlTypes, this.allowGenericXml);
        this.delegate = delegate;
    }

    public DataUriEntityResolver() {
        this(null, false ,null);
    }
    
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        if (DataUri.startsWithData(systemId)) {
            IRI iri;
            try {
                iri = iriFactory.construct(systemId);
            } catch (IRIException e) {
                IOException ioe = (IOException) new IOException(e.getMessage()).initCause(e);
                SAXParseException spe = new SAXParseException(e.getMessage(),
                        publicId, systemId, -1, -1, ioe);
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            try {
                systemId = iri.toASCIIString();
            } catch (MalformedURLException e) {
                IOException ioe = (IOException) new IOException(e.getMessage()).initCause(e);
                SAXParseException spe = new SAXParseException(e.getMessage(),
                        publicId, systemId, -1, -1, ioe);
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            DataUri du = new DataUri(systemId);
            TypedInputSource is = contentTypeParser.buildTypedInputSource(systemId, publicId,
                    du.getContentType());
            is.setByteStream(du.getInputStream());
            return is;
        } else if (delegate != null) {
            return delegate.resolveEntity(publicId, systemId);
        } else {
            throw new IOException("Unsupported URI scheme.");
        }
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
        this.contentTypeParser.setAllowRnc(allowRnc);
    }

    /**
     * @param b
     */
    public void setAllowHtml(boolean allowHtml) {
        this.allowHtml = allowHtml;
        this.contentTypeParser.setAllowHtml(allowHtml);
    }

    /**
     * Returns the acceptAllKnownXmlTypes.
     * 
     * @return the acceptAllKnownXmlTypes
     */
    public boolean isAcceptAllKnownXmlTypes() {
        return acceptAllKnownXmlTypes;
    }

    /**
     * Sets the acceptAllKnownXmlTypes.
     * 
     * @param acceptAllKnownXmlTypes
     *            the acceptAllKnownXmlTypes to set
     */
    public void setAcceptAllKnownXmlTypes(boolean acceptAllKnownXmlTypes) {
        this.acceptAllKnownXmlTypes = acceptAllKnownXmlTypes;
        this.contentTypeParser.setAcceptAllKnownXmlTypes(acceptAllKnownXmlTypes);
    }

    /**
     * Returns the allowGenericXml.
     * 
     * @return the allowGenericXml
     */
    public boolean isAllowGenericXml() {
        return allowGenericXml;
    }

    /**
     * Sets the allowGenericXml.
     * 
     * @param allowGenericXml
     *            the allowGenericXml to set
     */
    public void setAllowGenericXml(boolean allowGenericXml) {
        this.allowGenericXml = allowGenericXml;
        this.contentTypeParser.setAllowGenericXml(allowGenericXml);
    }

    /**
     * Returns the allowXhtml.
     * 
     * @return the allowXhtml
     */
    public boolean isAllowXhtml() {
        return allowXhtml;
    }

    /**
     * Sets the allowXhtml.
     * 
     * @param allowXhtml
     *            the allowXhtml to set
     */
    public void setAllowXhtml(boolean allowXhtml) {
        this.allowXhtml = allowXhtml;
        this.contentTypeParser.setAllowXhtml(allowXhtml);
    }

    /**
     * Returns the allowHtml.
     * 
     * @return the allowHtml
     */
    public boolean isAllowHtml() {
        return allowHtml;
    }

    public boolean isOnlyHtmlAllowed() {
        return !isAllowGenericXml() && !isAllowRnc() && !isAllowXhtml();
    }
}