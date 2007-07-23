/*
 * Copyright (c) 2005 Henri Sivonen
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

package fi.iki.hsivonen.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;

import fi.iki.hsivonen.io.BoundednputStream;
import fi.iki.hsivonen.io.ObservableInputStream;
import fi.iki.hsivonen.io.StreamObserver;

/**
 * @version $Id: PrudentHttpEntityResolver.java,v 1.1 2005/01/08 08:11:26
 *          hsivonen Exp $
 * @author hsivonen
 */
public class PrudentHttpEntityResolver implements EntityResolver {

    private static final Logger log4j = Logger.getLogger(PrudentHttpEntityResolver.class);

    private static final Pattern CHARSET = Pattern.compile("^\\s*charset\\s*=\\s*(\\S+)\\s*$");

    private static final MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

    private static final HttpClient client = new HttpClient(manager);

    private static int maxRequests;

    private int sizeLimit;

    private final ErrorHandler errorHandler;

    private int requestsLeft;

    private boolean laxContentType;

    private boolean allowRnc = false;

    private boolean allowHtml = false;

    private boolean allowXhtml = false;

    private boolean acceptAllKnownXmlTypes = false;

    private boolean allowGenericXml = true;

    private final IRIFactory iriFactory;

    /**
     * Sets the timeouts of the HTTP client.
     * 
     * @param connectionTimeout
     *            timeout until connection established in milliseconds. Zero
     *            means no timeout.
     * @param socketTimeout
     *            timeout for waiting for data in milliseconds. Zero means no
     *            timeout.
     */
    public static void setParams(int connectionTimeout, int socketTimeout,
            int maxRequests) {
        HttpConnectionManagerParams hcmp = client.getHttpConnectionManager().getParams();
        hcmp.setConnectionTimeout(connectionTimeout);
        hcmp.setSoTimeout(socketTimeout);
        hcmp.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION,
                maxRequests);
        hcmp.setMaxTotalConnections(maxRequests * 2);
        PrudentHttpEntityResolver.maxRequests = maxRequests;
    }

    public static void setUserAgent(String ua) {
        client.getParams().setParameter("http.useragent", ua);
    }

    /**
     * @param connectionTimeout
     * @param socketTimeout
     * @param sizeLimit
     */
    public PrudentHttpEntityResolver(int sizeLimit, boolean laxContentType,
            ErrorHandler errorHandler) {
        this.sizeLimit = sizeLimit;
        this.requestsLeft = maxRequests;
        this.laxContentType = laxContentType;
        this.errorHandler = errorHandler;
        this.iriFactory = new IRIFactory();
        this.iriFactory.useSpecificationXMLSystemID(true);
        this.iriFactory.useSchemeSpecificRules("http", true);
        this.iriFactory.useSchemeSpecificRules("https", true);
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        if (requestsLeft > -1) {
            if (requestsLeft == 0) {
                throw new IOException(
                        "Number of permitted HTTP requests exceeded.");
            } else {
                requestsLeft--;
            }
        }
        GetMethod m = null;
        try {
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
            if (!iri.isAbsolute()) {
                SAXParseException spe = new SAXParseException(
                        "Not an absolute URI.", publicId, systemId, -1, -1,
                        new IOException("Not an absolute URI."));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            String scheme = iri.getScheme();
            if (!("http".equals(scheme) || "https".equals(scheme))) {
                String msg = "Unsupported URI scheme: \u201C" + scheme + "\u201D.";
                SAXParseException spe = new SAXParseException(
                        msg, publicId,
                        systemId, -1, -1, new IOException(msg));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            String host = iri.getHost();
            if ("127.0.0.1".equals(host) || "localhost".equals(host)) {
                SAXParseException spe = new SAXParseException(
                        "Attempted to connect to localhost.", publicId,
                        systemId, -1, -1, new IOException("Attempted to connect to localhost."));
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
            try {
                m = new GetMethod(systemId);
            } catch (IllegalArgumentException e) {
                SAXParseException spe = new SAXParseException(
                        e.getMessage(),
                        publicId,
                        systemId,
                        -1,
                        -1,
                        (IOException) new IOException(e.getMessage()).initCause(e));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            m.setFollowRedirects(true);
            m.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            m.addRequestHeader("Accept", buildAccept());
            log4j.info(systemId);
            client.executeMethod(m);
            int statusCode = m.getStatusCode();
            if (statusCode != 200) {
                String msg = "HTTP resource not retrievable. The HTTP status from the remote server was: " + statusCode + ".";
                SAXParseException spe = new SAXParseException(
                        msg, publicId,
                        m.getURI().toString(), -1, -1, new IOException(msg));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            long len = m.getResponseContentLength();
            if (sizeLimit > -1 && len > sizeLimit) {
                SAXParseException spe = new SAXParseException(
                        "Resource size exceeds limit.", publicId,
                        m.getURI().toString(), -1, -1, new IOException("Resource size exceeds limit."));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            TypedInputSource is = new TypedInputSource();
            is.setPublicId(publicId);
            is.setSystemId(m.getURI().toString());
            Header ct = m.getResponseHeader("Content-Type");
            if (ct != null) {
                String val = ct.getValue();
                String[] params = val.split(";");
                String type = params[0].trim();
                boolean wasRnc = false;
                boolean wasHtml = false;
                if (isAllowRnc()) {
                    if (rncContentType(type, is)) {
                        wasRnc = true;
                        is.setType("application/relax-ng-compact-syntax");
                    }
                }
                if (!wasRnc) {
                    if (isAllowHtml()) {
                        if ("text/html".equals(type)) {
                            is.setType(type);
                            wasHtml = true;
                        } else if (isOnlyHtmlAllowed()) {
                            if (laxContentType && "text/plain".equals(type)) {
                                is.setType(type);
                                wasHtml = true;
                                if (errorHandler != null) {
                                    errorHandler.warning(new SAXParseException(
                                            "Being lax about non-HTML Content-Type: "
                                                    + type, is.getPublicId(),
                                            is.getSystemId(), -1, -1));
                                }
                            } else {
                                String msg = "Non-HTML Content-Type: \u201C" + type + "\u201D.";
                                SAXParseException spe = new SAXParseException(
                                        msg,
                                        publicId, m.getURI().toString(), -1,
                                        -1, new IOException(msg));
                                if (errorHandler != null) {
                                    errorHandler.fatalError(spe);
                                }
                                throw spe;
                            }
                        }
                    } 
                    if (!wasHtml && (isAllowGenericXml() || isAllowXhtml() || isAcceptAllKnownXmlTypes())) {
                        if (!xmlContentType(type, is)) {
                            String msg = "Non-XML Content-Type: \u201C" + type + "\u201D.";
                            SAXParseException spe = new SAXParseException(
                                    msg, publicId,
                                    m.getURI().toString(), -1, -1,
                                    new IOException(msg));
                            if (errorHandler != null) {
                                errorHandler.fatalError(spe);
                            }
                            throw spe;
                        } else {
                            is.setType(type);
                        }
                    }
                }
                String charset = null;
                for (int i = 1; i < params.length; i++) {
                    Matcher matcher = CHARSET.matcher(params[i]);
                    if (matcher.matches()) {
                        charset = matcher.group(1);
                        break;
                    }
                }
                if (charset != null) {
                    is.setEncoding(charset);
                } else if (type.startsWith("text/") && !wasHtml) {
                    if (laxContentType) {
                        if (errorHandler != null) {
                            errorHandler.warning(new SAXParseException(
                                    "text/* type without a charset parameter seen. Would have defaulted to US-ASCII had the lax option not been chosen.",
                                    is.getPublicId(), is.getSystemId(), -1, -1));
                        }
                    } else {
                        is.setEncoding("US-ASCII");
                        if (errorHandler != null) {
                            errorHandler.warning(new SAXParseException(
                                    "text/* type without a charset parameter seen. Defaulting to US-ASCII per section 3.1 of RFC 3023.",
                                    is.getPublicId(), is.getSystemId(), -1, -1));
                        }
                    }
                }
            }
            final GetMethod meth = m;
            InputStream stream = m.getResponseBodyAsStream();
            if (sizeLimit > -1) {
                stream = new BoundednputStream(stream, sizeLimit);
            }
            is.setByteStream(new ObservableInputStream(stream,
                    new StreamObserver() {
                        private final Logger log4j = Logger.getLogger("fi.iki.hsivonen.xml.PrudentEntityResolver.StreamObserver");

                        private boolean released = false;

                        public void closeCalled() {
                            log4j.debug("closeCalled");
                            if (!released) {
                                log4j.debug("closeCalled, not yet released");
                                released = true;
                                try {
                                    meth.releaseConnection();
                                } catch (Exception e) {
                                    log4j.debug(
                                            "closeCalled, releaseConnection", e);
                                }
                            }
                        }

                        public void exceptionOccurred(Exception ex) {
                            if (!released) {
                                released = true;
                                try {
                                    meth.abort();
                                } catch (Exception e) {
                                    log4j.debug("exceptionOccurred, abort", e);
                                } finally {
                                    try {
                                        meth.releaseConnection();
                                    } catch (Exception e) {
                                        log4j.debug(
                                                "exceptionOccurred, releaseConnection",
                                                e);
                                    }
                                }
                            }
                        }

                        public void finalizerCalled() {
                            if (!released) {
                                released = true;
                                try {
                                    meth.abort();
                                } catch (Exception e) {
                                    log4j.debug("finalizerCalled, abort", e);
                                } finally {
                                    try {
                                        meth.releaseConnection();
                                    } catch (Exception e) {
                                        log4j.debug(
                                                "finalizerCalled, releaseConnection",
                                                e);
                                    }
                                }
                            }
                        }

                    }));
            return is;
        } catch (IOException e) {
            if (m != null) {
                try {
                    m.abort();
                } catch (Exception ex) {
                    log4j.debug("abort", ex);
                } finally {
                    try {
                        m.releaseConnection();
                    } catch (Exception ex) {
                        log4j.debug("releaseConnection", ex);
                    }
                }
            }
            throw e;
        } catch (SAXException e) {
            if (m != null) {
                try {
                    m.abort();
                } catch (Exception ex) {
                    log4j.debug("abort", ex);
                } finally {
                    try {
                        m.releaseConnection();
                    } catch (Exception ex) {
                        log4j.debug("releaseConnection", ex);
                    }
                }
            }
            throw e;
        } catch (RuntimeException e) {
            if (m != null) {
                try {
                    m.abort();
                } catch (Exception ex) {
                    log4j.debug("abort", ex);
                } finally {
                    try {
                        m.releaseConnection();
                    } catch (Exception ex) {
                        log4j.debug("releaseConnection", ex);
                    }
                }
            }
            throw e;
        }
    }

    protected boolean xmlContentType(String type, InputSource is)
            throws SAXException {
        if ("application/xhtml-voice+xml".equals(type)) {
            if (errorHandler != null) {
                errorHandler.warning(new SAXParseException(
                        "application/xhtml-voice+xml is an obsolete type.",
                        is.getPublicId(), is.getSystemId(), -1, -1));
            }
        }
        boolean typeOk = "application/xml".equals(type)
                || "text/xml".equals(type) || type.endsWith("+xml")
                || "application/xml-external-parsed-entity".equals(type)
                || "text/xml-external-parsed-entity".equals(type)
                || "application/xml-dtd".equals(type)
                || "application/octet-stream".equals(type);
        if (!typeOk && laxContentType) {
            boolean laxOk = "text/plain".equals(type)
                    || "text/html".equals(type) || "text/xsl".equals(type);
            if (laxOk && errorHandler != null) {
                errorHandler.warning(new SAXParseException(
                        "Being lax about non-XML Content-Type: " + type,
                        is.getPublicId(), is.getSystemId(), -1, -1));
            }
            return laxOk;
        } else {
            return typeOk;
        }
    }

    protected boolean rncContentType(String type, InputSource is)
            throws SAXException {
        boolean typeOk = "application/relax-ng-compact-syntax".equals(type);
        if (!typeOk) {
            typeOk = "application/vnd.relax-ng.rnc".equals(type);
            if (typeOk && errorHandler != null) {
                errorHandler.warning(new SAXParseException(
                        "application/vnd.relax-ng.rnc is an unregistered type. application/relax-ng-compact-syntax is the registered type.",
                        is.getPublicId(), is.getSystemId(), -1, -1));
            }
        }
        if (!typeOk) {
            typeOk = "application/octet-stream".equals(type)
                    && is.getSystemId().endsWith(".rnc");
        }
        if (!typeOk && laxContentType) {
            boolean laxOk = "text/plain".equals(type)
                    && is.getSystemId().endsWith(".rnc");
            if (laxOk && errorHandler != null) {
                errorHandler.warning(new SAXParseException(
                        "Being lax about non-RNC Content-Type: " + type,
                        is.getPublicId(), is.getSystemId(), -1, -1));
            }
            return laxOk;
        } else {
            return typeOk;
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
    }

    /**
     * @param b
     */
    public void setAllowHtml(boolean expectHtml) {
        this.allowHtml = expectHtml;
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
    }

    private String buildAccept() {
        Set<String> types = new TreeSet<String>();
        if (isAllowRnc()) {
            types.add("application/relax-ng-compact-syntax");
        }
        if (isAllowHtml()) {
            types.add("text/html; q=0.9");
        }
        if (isAllowXhtml()) {
            types.add("application/xhtml+xml");
            types.add("application/xml; q=0.5");
        }
        if (isAcceptAllKnownXmlTypes()) {
            types.add("application/xhtml+xml");
//            types.add("application/atom+xml");
            types.add("image/svg+xml");
            types.add("application/docbook+xml");
            types.add("application/xml; q=0.5");
            types.add("text/xml; q=0.3");
            types.add("*/*; q=0.1");
        }
        if (isAllowGenericXml()) {
            types.add("application/xml; q=0.5");
            types.add("text/xml; q=0.3");
            types.add("*/*; q=0.1");
        }
        StringBuilder buf = new StringBuilder();
        for (Iterator<String> iter = types.iterator(); iter.hasNext();) {
            String str = iter.next();
            buf.append(str);
            buf.append(", ");
        }
        for (int i = 0; i < 2; i++) {
            int len = buf.length();
            if (len > 0) {
                buf.deleteCharAt(len - 1);
            }
        }
        return buf.toString();
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