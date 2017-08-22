/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2007-2017 Mozilla Foundation
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
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;

import org.relaxng.datatype.DatatypeException;

import nu.validator.datatype.ContentSecurityPolicy;
import nu.validator.datatype.Html5DatatypeException;
import nu.validator.io.BoundedInputStream;
import nu.validator.io.ObservableInputStream;
import nu.validator.io.StreamBoundException;
import nu.validator.io.StreamObserver;
import nu.validator.io.SystemIdIOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import io.mola.galimatias.URL;
import io.mola.galimatias.GalimatiasParseException;

/**
 * @version $Id: PrudentHttpEntityResolver.java,v 1.1 2005/01/08 08:11:26
 *          hsivonen Exp $
 * @author hsivonen
 */
@SuppressWarnings("deprecation") public class PrudentHttpEntityResolver
        implements EntityResolver {

    private static final Logger log4j = Logger.getLogger(PrudentHttpEntityResolver.class);

    private static HttpClient client;

    private static int maxRequests;

    private long sizeLimit;

    private final ErrorHandler errorHandler;

    private int requestsLeft;

    private boolean allowRnc = false;

    private boolean allowHtml = false;

    private boolean allowXhtml = false;

    private boolean acceptAllKnownXmlTypes = false;

    private boolean allowGenericXml = true;

    private final ContentTypeParser contentTypeParser;

    private String userAgent;

    private HttpServletRequest request;

    /**
     * Sets the timeouts of the HTTP client.
     *
     * @param connectionTimeout
     *            timeout until connection established in milliseconds. Zero
     *            means no timeout.
     * @param socketTimeout
     *            timeout for waiting for data in milliseconds. Zero means no
     *            timeout.
     * @param maxRequests
     *            maximum number of connections to a particular host
     */
    public static void setParams(int connectionTimeout, int socketTimeout,
            int maxRequests) {
        PrudentHttpEntityResolver.maxRequests = maxRequests;
        PoolingHttpClientConnectionManager phcConnMgr;
        Registry<ConnectionSocketFactory> registry = //
        RegistryBuilder.<ConnectionSocketFactory> create() //
        .register("http", PlainConnectionSocketFactory.getSocketFactory()) //
        .register("https", SSLConnectionSocketFactory.getSocketFactory()) //
        .build();
        HttpClientBuilder builder = HttpClients.custom();
        builder.setRedirectStrategy(new LaxRedirectStrategy());
        builder.setMaxConnPerRoute(maxRequests);
        builder.setMaxConnTotal(
                Integer.parseInt(System.getProperty("nu.validator.servlet.max-total-connections","200")));
        if ("true".equals(System.getProperty(
                "nu.validator.xml.promiscuous-ssl", "true"))) { //
            try {
                SSLContext promiscuousSSLContext = new SSLContextBuilder() //
                .loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                        return true;
                    }
                }).build();
                builder.setSslcontext(promiscuousSSLContext);
                HostnameVerifier verifier = //
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
                SSLConnectionSocketFactory promiscuousSSLConnSocketFactory = //
                new SSLConnectionSocketFactory(promiscuousSSLContext, verifier);
                registry = RegistryBuilder.<ConnectionSocketFactory> create() //
                .register("https", promiscuousSSLConnSocketFactory) //
                .register("http",
                        PlainConnectionSocketFactory.getSocketFactory()) //
                .build();
            } catch (KeyManagementException | KeyStoreException
                    | NoSuchAlgorithmException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        phcConnMgr = new PoolingHttpClientConnectionManager(registry);
        phcConnMgr.setDefaultMaxPerRoute(maxRequests);
        phcConnMgr.setMaxTotal(200);
        builder.setConnectionManager(phcConnMgr);
        RequestConfig.Builder config = RequestConfig.custom();
        config.setCircularRedirectsAllowed(true);
        config.setMaxRedirects(
                Integer.parseInt(System.getProperty("nu.validator.servlet.max-redirects","20")));
        config.setConnectTimeout(connectionTimeout);
        config.setCookieSpec(CookieSpecs.BEST_MATCH);
        config.setSocketTimeout(socketTimeout);
        config.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
        client = builder.setDefaultRequestConfig(config.build()).build();
    }

    public void setUserAgent(String ua) {
        userAgent = ua;
    }

    public PrudentHttpEntityResolver(long sizeLimit, boolean laxContentType,
            ErrorHandler errorHandler, HttpServletRequest request) {
        this.request = request;
        this.sizeLimit = sizeLimit;
        this.requestsLeft = maxRequests;
        this.errorHandler = errorHandler;
        this.contentTypeParser = new ContentTypeParser(errorHandler,
                laxContentType, this.allowRnc, this.allowHtml, this.allowXhtml,
                this.acceptAllKnownXmlTypes, this.allowGenericXml);
    }

    public PrudentHttpEntityResolver(long sizeLimit, boolean laxContentType,
            ErrorHandler errorHandler) {
        this(sizeLimit, laxContentType, errorHandler, null);
    }

    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
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
        HttpGet m = null;
        try {
            URL url = null;
            try {
                url = URL.parse(systemId);
            } catch (GalimatiasParseException e) {
                IOException ioe = (IOException) new IOException(e.getMessage()).initCause(e);
                SAXParseException spe = new SAXParseException(e.getMessage(),
                        publicId, systemId, -1, -1, ioe);
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw ioe;
            }
            String scheme = url.scheme();
            if (!("http".equals(scheme) || "https".equals(scheme))) {
                String msg = "Unsupported URI scheme: \u201C" + scheme
                        + "\u201D.";
                SAXParseException spe = new SAXParseException(msg, publicId,
                        systemId, -1, -1, new IOException(msg));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            systemId = url.toString();
            try {
                m = new HttpGet(systemId);
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
            m.setHeader("User-Agent", userAgent);
            m.setHeader("Accept", buildAccept());
            m.setHeader("Accept-Encoding", "gzip");
            if (request != null && request.getAttribute(
                    "http://validator.nu/properties/accept-language") != null) {
                m.setHeader("Accept-Language", (String) request.getAttribute(
                        "http://validator.nu/properties/accept-language"));
            }
            log4j.info(systemId);
            try {
                if (url.port() > 65535) {
                    throw new IOException(
                            "Port number must be less than 65536.");
                }
            } catch (NumberFormatException e) {
                    throw new IOException(
                            "Port number must be less than 65536.");
            }
            HttpResponse response = client.execute(m);
            boolean ignoreResponseStatus = false;
            if (request != null && request.getAttribute(
                    "http://validator.nu/properties/ignore-response-status") != null) {
                ignoreResponseStatus = (boolean) request.getAttribute(
                        "http://validator.nu/properties/ignore-response-status");
            }
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200 && !ignoreResponseStatus) {
                String msg = "HTTP resource not retrievable."
                        + " The HTTP status from the remote server was: "
                        + statusCode + ".";
                SAXParseException spe = new SAXParseException(msg, publicId,
                        m.getURI().toString(), -1, -1,
                        new SystemIdIOException(m.getURI().toString(), msg));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw new ResourceNotRetrievableException(
                        String.format("%s: %s", m.getURI().toString(), msg));
            }
            HttpEntity entity = response.getEntity();
            long len = entity.getContentLength();
            if (sizeLimit > -1 && len > sizeLimit) {
                SAXParseException spe = new SAXParseException(
                        "Resource size exceeds limit.",
                        publicId,
                        m.getURI().toString(),
                        -1,
                        -1,
                        new StreamBoundException("Resource size exceeds limit."));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            TypedInputSource is;
            org.apache.http.Header ct = response.getFirstHeader("Content-Type");
            String contentType = null;
            final String baseUri = m.getURI().toString();
            if (ct != null) {
                contentType = ct.getValue();
            }
            is = contentTypeParser.buildTypedInputSource(baseUri, publicId,
                    contentType);

            Header cl = response.getFirstHeader("Content-Language");
            if (cl != null) {
                is.setLanguage(cl.getValue().trim());
            }

            Header xuac = response.getFirstHeader("X-UA-Compatible");
            if (xuac != null) {
                String val = xuac.getValue().trim();
                if (!"ie=edge".equalsIgnoreCase(val)) {
                    SAXParseException spe = new SAXParseException(
                            "X-UA-Compatible HTTP header must have the value \u201CIE=edge\u201D,"
                                    + " was \u201C" + val + "\u201D.",
                            publicId, systemId, -1, -1);
                    errorHandler.error(spe);
                }
            }

            Header csp = response.getFirstHeader("Content-Security-Policy");
            if (csp != null) {
                try {
                    ContentSecurityPolicy.THE_INSTANCE.checkValid(csp.getValue().trim());
                } catch (DatatypeException e) {
                    SAXParseException spe = new SAXParseException(
                            "Content-Security-Policy HTTP header: "
                                    + e.getMessage(), publicId, systemId, -1,
                            -1);
                    Html5DatatypeException ex5 = (Html5DatatypeException) e;
                    if (ex5.isWarning()) {
                        errorHandler.warning(spe);
                    } else {
                        errorHandler.error(spe);
                    }
                }
            }

            final HttpGet meth = m;
            InputStream stream = entity.getContent();
            if (sizeLimit > -1) {
                stream = new BoundedInputStream(stream, sizeLimit, baseUri);
            }
            Header ce = response.getFirstHeader("Content-Encoding");
            if (ce != null) {
                String val = ce.getValue().trim();
                if ("gzip".equalsIgnoreCase(val)
                        || "x-gzip".equalsIgnoreCase(val)) {
                    stream = new GZIPInputStream(stream);
                    if (sizeLimit > -1) {
                        stream = new BoundedInputStream(stream, sizeLimit,
                                baseUri);
                    }
                }
            }
            is.setByteStream(new ObservableInputStream(stream,
                    new StreamObserver() {
                        private final Logger log4j = Logger.getLogger("nu.validator.xml.PrudentEntityResolver.StreamObserver");

                        private boolean released = false;

                        @Override
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

                        @Override
                        public void exceptionOccurred(Exception ex)
                                throws IOException {
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
                            if (ex instanceof SystemIdIOException) {
                                throw (SystemIdIOException) ex;
                            } else if (ex instanceof IOException) {
                                IOException ioe = (IOException) ex;
                                throw new SystemIdIOException(baseUri,
                                        ioe.getMessage(), ioe);
                            } else if (ex instanceof RuntimeException) {
                                throw (RuntimeException) ex;
                            } else {
                                throw new RuntimeException(
                                        "API contract violation. Wrong exception type.",
                                        ex);
                            }
                        }

                        @Override
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
        } catch (IOException | RuntimeException | SAXException e) {
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
     * @param allowHtml
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

    private String buildAccept() {
        return "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
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

    public class ResourceNotRetrievableException extends SAXException {
        public ResourceNotRetrievableException(String message) {
            super(message);
        }
    }
}
