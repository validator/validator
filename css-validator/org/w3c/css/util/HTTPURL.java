/*
 * (c) COPYRIGHT 1995-1999 MIT, INRIA and Keio University. All Rights reserved.
 * W3C Intellectual Property Notice and Legal Disclaimers:
 *  http://www.w3.org/Consortium/Legal/
 *
 * HTTPURL.java
 * $Id$
 */
package org.w3c.css.util;

import org.w3c.www.mime.MimeType;
import org.w3c.www.mime.MimeTypeFormatException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/**
 * @author Philippe Le Hegaret
 * @version $Revision$
 */
public class HTTPURL {

    /**
     * Don't create this class
     */
    private HTTPURL() {
    }


    public static String getHTTPStatusCode(int status) {
        switch (status) {
            case 100:
                return "Continue";
            case 101:
                return "Switching Protocols";
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 202:
                return "Accepted";
            case 203:
                return "Non-Authoritative Information";
            case 204:
                return "No Content";
            case 205:
                return "Reset Content";
            case 206:
                return "Partial Content";
            case 300:
                return "Multiple Choices";
            case 301:
                return "Moved Permanently";
            case 302:
                return "Found";
            case 303:
                return "See Other";
            case 304:
                return "Not Modified";
            case 305:
                return "Use Proxy";
            case 306:
                return "(Unused)";
            case 307:
                return "Temporary Redirect";
            case 308:
                return "Permanent Redirect";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 402:
                return "Payment Required";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 405:
                return "Method Not Allowed";
            case 406:
                return "Not Acceptable";
            case 407:
                return "Proxy Authentication Required";
            case 408:
                return "Request Timeout";
            case 409:
                return "Conflict";
            case 410:
                return "Gone";
            case 411:
                return "Length Required";
            case 412:
                return "Precondition Failed";
            case 413:
                return "Request Entity Too Large";
            case 414:
                return "Request-URI Too Long";
            case 415:
                return "Unsupported Media Type";
            case 416:
                return "Requested Range Not Satisfiable";
            case 417:
                return "Expectation Failed";
            case 500:
                return "Internal Server Error";
            case 501:
                return "Not Implemented";
            case 502:
                return "Bad Gateway";
            case 503:
                return "Service Unavailable";
            case 504:
                return "Gateway Timeout";
            case 505:
                return "HTTP Version Not Supported";
            default:
                return Integer.toString(status, 10);
        }
    }

    public static URL getURL(String url) throws IOException {
        // url = URLEncoder.encode(url);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            //if (!url.startsWith("http:")) { // ook!? dkfj://wwww.3.org -> http://dkfj://www.w3.org
            if (url.indexOf("://") == -1) { // the protocol is missing
                return new URL("http://" + url);
            } else {
                throw (IOException) e.fillInStackTrace();
            }
        }
    }

    public static URL getURL(URL base, String url)
            throws MalformedURLException {
        //	url = URLEncoder.encode(url);
        return new URL(base, url);
    }

    private static URLConnection getConnection(URL url, int count)
            throws IOException {
        return getConnection(url, null, count, null);
    }


    private static void setSSLVerifier(HttpsURLConnection uConn) {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            uConn.setSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        // Step 2: hostname verifier
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };
        uConn.setHostnameVerifier(hv);
    }

    private static URLConnection getConnection(URL url, URL referrer, int count,
                                               ApplContext ac)
            throws IOException {
        if (count > 5) {
            throw new ProtocolException("Server redirected too many " +
                    "times (5)");
        }
        // add the referrer, if not the same as the target URL
        URL ref = (url.equals(referrer) ? null : referrer);

        if (Util.servlet) {
            String protocol = url.getProtocol();
            if (!(("https".equalsIgnoreCase(protocol)) || ("http".equalsIgnoreCase(protocol)))) {
                System.err.println("[WARNING] : someone is trying to get the file: "
                        + url);
                throw new FileNotFoundException("import " + url +
                        ": Operation not permitted");
            }
            if (url.getPort() >= 0 && url.getPort() != 80 && url.getPort() != 443 && url.getPort() <= 1024) {
                System.err.println("[WARNING] : someone is trying to access a forbidden port: "
                        + url);
                throw new FileNotFoundException("import " + url +
                        ": Operation not permitted");
            }
        }

        URLConnection urlC = url.openConnection();

        if (Util.onDebug) {
            System.err.println("Accessing " + url);
            if (ac.getCredential() != null) {
                System.err.println("with [" + ac.getCredential() + ']');
            }
        }
        // setting timeouts
        urlC.setConnectTimeout(ac.getConnectTimeout());
        urlC.setReadTimeout(ac.getReadTimeout());

        // avoid all kind of caches
        urlC.setRequestProperty("Pragma", "no-cache");
        urlC.setRequestProperty("Cache-Control", "no-cache, no-store");
        // for the fun
        // urlC.setRequestProperty("User-Agent", CssValidator.server_name);
        // referrer
        setReferrer(urlC, ref);
        // relay authorization information
        if (ac.getCredential() != null) {
            urlC.setRequestProperty("Authorization", ac.getCredential());
        }
        // relay languages
        if (ac.getLang() != null) {
            if (ac.getLang().indexOf('*') == -1) {
                urlC.setRequestProperty("Accept-Language", ac.getLang() + ",*");
            } else {
                urlC.setRequestProperty("Accept-Language", ac.getLang());
            }
        }
        // should I put an Accept header?
        urlC.setRequestProperty("Accept",
                "text/css,text/html,text/xml,"
                        + "application/xhtml+xml,application/xml,"
                        + "image/svg+xml,*/*;q=0.1");

        if (urlC instanceof HttpURLConnection) {
            HttpURLConnection httpURL = (HttpURLConnection) urlC;
            int status;

            httpURL.setInstanceFollowRedirects(false);
            if (urlC instanceof HttpsURLConnection) {
                try {
                    urlC.connect();
                } catch (IOException ioex) {
                    setSSLVerifier((HttpsURLConnection) urlC);
                    urlC.connect();
                }
            } else {
                urlC.connect();
            }

            try {
                status = httpURL.getResponseCode();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new FileNotFoundException(url + ": " +
                        getHTTPStatusCode(404));
            }

            switch (status) {
                case HttpURLConnection.HTTP_OK:
                    // nothing to do
                    break;
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case 307:
                case 308:
                    try {
                        URL u = getURL(url, httpURL.getHeaderField("Location"));
                        return getConnection(u, ref, count + 1, ac);
                    } catch (Exception ex) {
                        // usually a NPE when Location is absent on a redirect.
                        // in any case, we will count this as non existent result.
                        throw new FileNotFoundException(url + ":" + " Error in " + status);
                    } finally {
                        httpURL.disconnect();
                    }
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    String realm = httpURL.getHeaderField("WWW-Authenticate");
                    httpURL.disconnect();
                    if (realm != null) {
                        throw new ProtocolException(realm);
                    }
                default:
                    try {
                        if (httpURL.getResponseMessage() != null) {
                            throw new FileNotFoundException(url + ": " +
                                    httpURL.getResponseMessage());
                        } else {
                            throw new FileNotFoundException(url + ": " +
                                    getHTTPStatusCode(status));
                        }
                    } finally {
                        httpURL.disconnect();
                    }
            }
        } else {
            urlC.connect();
        }
        return urlC;
    }

    public static URLConnection getConnection(URL url)
            throws IOException {
        return getConnection(url, 0);
    }

    public static URLConnection getConnection(URL url, ApplContext ac)
            throws IOException {
        return getConnection(url, ac.getReferrer(), 0, ac);
    }

    /* more madness */
    public static InputStream getInputStream(ApplContext ac, URLConnection uco)
            throws IOException {
        InputStream orig_stream = uco.getInputStream();
        String charset;
        String encoding;
        if (orig_stream == null) {
            return orig_stream; // let it fail elsewhere
        }
        encoding = uco.getContentEncoding();
        // not set -> return
        if (encoding != null) {
            if (encoding.equalsIgnoreCase("gzip")) {
                orig_stream = new GZIPInputStream(orig_stream);
            }
        }
        charset = getCharacterEncoding(ac, uco);
        if ((charset == null) || (charset.regionMatches(true, 0, "utf", 0, 3))) {
            UnicodeInputStream is = new UnicodeInputStream(orig_stream);
            charset = is.getEncodingFromStream();
            if (charset != null) {
                ac.setCharsetForURL(uco.getURL(), charset, true);
            }
            return is;
        }
        return orig_stream;
    }

    public static String getCharacterEncoding(ApplContext ac,
                                              URLConnection uco) {
        String charset = ac.getCharsetForURL(uco.getURL());
        if (charset != null) {
            return charset;
        }
        String mtypestr = uco.getContentType();
        if (mtypestr == null) {
            return mtypestr;
        }
        MimeType mt;
        try {
            mt = new MimeType(mtypestr);
        } catch (MimeTypeFormatException mex) {
            return null;
        }
        charset = mt.getParameterValue("charset");
        if (charset != null) {
            ac.setCharsetForURL(uco.getURL(), charset, false);
        }
        return charset;
    }

    // used to set referrer
    private static void setReferrer(URLConnection connection, URL referrer) {
        if (referrer == null) {
            return;
        }
        URL current = connection.getURL();
        String curProtocol = current.getProtocol();
        String refProtocol = referrer.getProtocol();
        if ("https".equalsIgnoreCase(refProtocol)) {
            if (!"https".equalsIgnoreCase(curProtocol)) {
                // exit, we won't disclose information on non-https
                // connections  (ref using https, req using http)
                return;
            }
            // ok so we have https for both, avoid leaking information
            // so check that hosts are the same
            if (!current.getHost().equalsIgnoreCase(referrer.getHost())) {
                return;
            }
        }
        // ok good, let's do it
        connection.setRequestProperty("Referer", referrer.toExternalForm());
    }

    /**
     *
     */
    public static void main(String[] args)
            throws Exception {
        int c;
        InputStream in = HTTPURL.getConnection(
                getURL(args[0])).getInputStream();

        while ((c = in.read()) != -1) {
            System.err.print((char) c);
        }
        System.exit(0);
    }
}
