/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2007-2018 Mozilla Foundation
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
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import jakarta.servlet.http.HttpServletRequest;

import nu.validator.vendor.relaxng.datatype.DatatypeException;

import nu.validator.datatype.ContentSecurityPolicy;
import nu.validator.datatype.Html5DatatypeException;
import nu.validator.io.BoundedInputStream;
import nu.validator.io.ObservableInputStream;
import nu.validator.io.StreamBoundException;
import nu.validator.io.StreamObserver;
import nu.validator.io.SystemIdIOException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
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
public class PrudentHttpEntityResolver
        implements EntityResolver {

    private static final Logger log4j = Logger.getLogger(PrudentHttpEntityResolver.class);

    private static HttpClient client;
    
    private static boolean clientStarted = false;
    
    private static int connectionTimeoutMs;
    
    private static int socketTimeoutMs;

    private static int maxRequests;

    private long sizeLimit;

    private final ErrorHandler errorHandler;

    private int requestsLeft;

    private boolean allowRnc = false;

    private boolean allowCss = false;

    private boolean allowHtml = false;

    private boolean allowXhtml = false;

    private boolean acceptAllKnownXmlTypes = false;

    private boolean allowGenericXml = true;

    private final ContentTypeParser contentTypeParser;

    private static final List<String> FORBIDDEN_HOSTS = Arrays.asList( //
            "localhost", //
            "127.0.0.1", //
            "0.0.0.0", //
            "[::1]", //
            "[0:0:0:0:0:0:0:1]", //
            "[0000:0000:0000:0000:0000:0000:0000:0001]", //
            "[::]", //
            "[::0]", //
            "[0000:0000:0000:0000:0000:0000:0000:0000]", //
            "[0:0:0:0:0:0:0:0]" //
    );

    private boolean allowForbiddenHosts = false;

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
        PrudentHttpEntityResolver.connectionTimeoutMs = connectionTimeout;
        PrudentHttpEntityResolver.socketTimeoutMs = socketTimeout;
        PrudentHttpEntityResolver.maxRequests = maxRequests;
        
        // Don't create any Jetty objects here - defer until first HTTP request
        client = null;
        clientStarted = false;
    }
    
    private static synchronized void ensureClientStarted() {
        if (!clientStarted) {
            if (client == null) {
                // Create the client on first use to avoid Jetty logging during initialization
                boolean promiscuousSSL = "true".equals(System.getProperty(
                        "nu.validator.xml.promiscuous-ssl", "true"));
                
                SslContextFactory.Client sslContextFactory;
                if (promiscuousSSL) {
                    sslContextFactory = new SslContextFactory.Client(true);
                } else {
                    sslContextFactory = new SslContextFactory.Client();
                }
                
                ClientConnector clientConnector = new ClientConnector();
                clientConnector.setSslContextFactory(sslContextFactory);
                
                HttpClientTransport transport = new HttpClientTransportOverHTTP(clientConnector);
                
                client = new HttpClient(transport);
                client.setFollowRedirects(true);
                client.setMaxConnectionsPerDestination(maxRequests);
                client.setMaxRequestsQueuedPerDestination(
                        Integer.parseInt(System.getProperty("nu.validator.servlet.max-total-connections","200")));
                client.setMaxRedirects(
                        Integer.parseInt(System.getProperty("nu.validator.servlet.max-redirects","20")));
                client.setConnectTimeout(connectionTimeoutMs);
                client.setIdleTimeout(socketTimeoutMs);
                // Set response buffer size from max-file-size property (default 2MB)
                int maxFileSize = Integer.parseInt(System.getProperty(
                        "nu.validator.servlet.max-file-size", "2097152"));
                client.setResponseBufferSize(maxFileSize);
            }
            
            try {
                client.start();
                clientStarted = true;
            } catch (Exception e) {
                log4j.error("Failed to start HTTP client", e);
                throw new RuntimeException("Failed to start HTTP client", e);
            }
        }
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
        this.allowForbiddenHosts = "true".equals(
                System.getProperty("nu.validator.servlet.allow-forbidden-hosts"));
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

        String allowedAddressType = System.getProperty(
            "nu.validator.servlet.allowed-address-type", "all");

        if ("none".equals(allowedAddressType)) {
            throw new IOException("URL-based checks are prohibited.");
        }

        if (requestsLeft > -1) {
            if (requestsLeft == 0) {
                throw new IOException(
                        "Number of permitted HTTP requests exceeded.");
            } else {
                requestsLeft--;
            }
        }
        Request jettyRequest = null;
        try {
            URL url = null;
            try {
                url = URL.parse(systemId);
                if ("same-origin".equals(allowedAddressType)) {
                    URL currentURL = URL.parse(request.getRequestURL().toString());

                    String currentURLOrigin = currentURL.scheme() + currentURL.host() + currentURL.port();
                    String targetURLOrigin = url.scheme() + url.host() + url.port();

                    if (!currentURLOrigin.equals(targetURLOrigin)) {
                        throw new IOException("Cross-origin requests are prohibited.");
                    }
                }
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
            
            // Ensure the HTTP client is initialized before creating requests
            ensureClientStarted();
            
            try {
                jettyRequest = client.newRequest(systemId);
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
            if (!allowForbiddenHosts
                    && FORBIDDEN_HOSTS.contains(url.host().toHostString())) {
                throw new IOException( "Forbidden host.");
            }
            if (url.port() != 80 && url.port() != 81 && url.port() != 443
                    && url.port() < 1024) {
                throw new IOException("Forbidden port.");
            }
            jettyRequest.header("User-Agent", userAgent);
            jettyRequest.header("Accept", buildAccept());
            jettyRequest.header("Accept-Encoding", "gzip");
            if (request != null && request.getAttribute(
                    "http://validator.nu/properties/accept-language") != null) {
                jettyRequest.header("Accept-Language", (String) request.getAttribute(
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
            ContentResponse response = jettyRequest.send();
            boolean ignoreResponseStatus = false;
            if (request != null && request.getAttribute(
                    "http://validator.nu/properties/ignore-response-status") != null) {
                ignoreResponseStatus = (boolean) request.getAttribute(
                        "http://validator.nu/properties/ignore-response-status");
            }
            int statusCode = response.getStatus();
            if (statusCode != 200 && !ignoreResponseStatus) {
                String msg = "HTTP resource not retrievable."
                        + " The HTTP status from the remote server was: "
                        + statusCode + ".";
                SAXParseException spe = new SAXParseException(msg, publicId,
                        systemId, -1, -1,
                        new SystemIdIOException(systemId, msg));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw new ResourceNotRetrievableException(
                        String.format("%s: %s", systemId, msg));
            }
            byte[] content = response.getContent();
            if (content == null || content.length == 0) {
                String msg = "Empty response.";
                SAXParseException spe = new SAXParseException(msg, publicId,
                        systemId, -1, -1,
                        new SystemIdIOException(systemId, msg));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw new ResourceNotRetrievableException(
                        String.format("%s: %s", systemId, msg));
            }
            long len = content.length;
            if (sizeLimit > -1 && len > sizeLimit) {
                SAXParseException spe = new SAXParseException(
                        "Resource size exceeds limit.",
                        publicId,
                        systemId,
                        -1,
                        -1,
                        new StreamBoundException("Resource size exceeds limit."));
                if (errorHandler != null) {
                    errorHandler.fatalError(spe);
                }
                throw spe;
            }
            TypedInputSource is;
            HttpField ct = response.getHeaders().getField("Content-Type");
            String contentType = null;
            final String baseUri = systemId;
            if (ct != null) {
                contentType = ct.getValue();
            }
            is = contentTypeParser.buildTypedInputSource(baseUri, publicId,
                    contentType);

            HttpField cl = response.getHeaders().getField("Content-Language");
            if (cl != null) {
                is.setLanguage(cl.getValue().trim());
            }

            HttpField xuac = response.getHeaders().getField("X-UA-Compatible");
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

            HttpField csp = response.getHeaders().getField("Content-Security-Policy");
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

            InputStream stream = new java.io.ByteArrayInputStream(content);
            if (sizeLimit > -1) {
                stream = new BoundedInputStream(stream, sizeLimit, baseUri);
            }
            HttpField ce = response.getHeaders().getField("Content-Encoding");
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

                        @Override
                        public void closeCalled() {
                            log4j.debug("closeCalled");
                        }

                        @Override
                        public void exceptionOccurred(Exception ex)
                                throws IOException {
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
                            log4j.debug("finalizerCalled");
                        }

                    }));
            return is;
        } catch (InterruptedException | java.util.concurrent.TimeoutException | java.util.concurrent.ExecutionException e) {
            throw new IOException("HTTP request failed: " + e.getMessage(), e);
        } catch (IOException | RuntimeException | SAXException e) {
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
     * @return Returns the allowCss.
     */
    public boolean isAllowCss() {
        return allowCss;
    }

    /**
     * @param allowCss
     *            The allowCss to set.
     */
    public void setAllowCss(boolean allowCss) {
        this.allowCss = allowCss;
        this.contentTypeParser.setAllowCss(allowCss);
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
        return !isAllowGenericXml() && !isAllowRnc() && !isAllowCss()
                && !isAllowXhtml();
    }

    public class ResourceNotRetrievableException extends SAXException {
        public ResourceNotRetrievableException(String message) {
            super(message);
        }
    }
}
