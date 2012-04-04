/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2007-2012 Mozilla Foundation
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

package nu.validator.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.xml.PrudentHttpEntityResolver;

import org.apache.log4j.Logger;


/**
 * @version $Id$
 * @author hsivonen
 */
public class VerifierServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 7811043632732680935L;

    private static final Logger log4j = Logger.getLogger(VerifierServlet.class);
    
    static final String GENERIC_HOST = System.getProperty("nu.validator.servlet.host.generic", "");
    
    static final String HTML5_HOST = System.getProperty("nu.validator.servlet.host.html5", "");    
    
    static final String PARSETREE_HOST = System.getProperty("nu.validator.servlet.host.parsetree", "");     
    
    static final String GENERIC_PATH = System.getProperty("nu.validator.servlet.path.generic", "/");
    
    static final String HTML5_PATH = System.getProperty("nu.validator.servlet.path.html5", "/html5/");    
    
    static final String PARSETREE_PATH = System.getProperty("nu.validator.servlet.path.parsetree", "/parsetree/");     
    
    static final boolean W3C_BRANDING = "1".equals(System.getProperty("nu.validator.servlet.w3cbranding"));

    private static final byte[] GENERIC_ROBOTS_TXT;
    
    private static final byte[] HTML5_ROBOTS_TXT;

    private static final byte[] PARSETREE_ROBOTS_TXT;

    private static final byte[] STYLE_CSS;

    private static final byte[] SCRIPT_JS;

    private static final byte[] ICON_PNG;

    private static final byte[] W3C_PNG;

    private static final byte[] VNU_PNG;

    private static final byte[] HTML_PNG;

    private static final byte[] ABOUT_HTML;

    static {
        try {
            GENERIC_ROBOTS_TXT = buildRobotsTxt(GENERIC_HOST, GENERIC_PATH, HTML5_HOST, HTML5_PATH, PARSETREE_HOST, PARSETREE_PATH);
            HTML5_ROBOTS_TXT = buildRobotsTxt(HTML5_HOST, HTML5_PATH, GENERIC_HOST, GENERIC_PATH, PARSETREE_HOST, PARSETREE_PATH);
            PARSETREE_ROBOTS_TXT = buildRobotsTxt(PARSETREE_HOST, PARSETREE_PATH, HTML5_HOST, HTML5_PATH, GENERIC_HOST, GENERIC_PATH);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try {
            if (W3C_BRANDING) {
                STYLE_CSS = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/w3c/style.css");
                SCRIPT_JS = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/w3c/script.js");
                ICON_PNG = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/w3c/icon.png");
                W3C_PNG = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/w3c/w3c.png");
                VNU_PNG = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/w3c/vnu.png");
                HTML_PNG = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/w3c/html.png");
                ABOUT_HTML = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/w3c/about.html");
            } else {
                W3C_PNG = VNU_PNG = HTML_PNG = ABOUT_HTML = null;
                STYLE_CSS = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/style.css");
                SCRIPT_JS = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/script.js");
                ICON_PNG = readFromClassLoaderIntoByteArray("nu/validator/localentities/files/icon.png");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PrudentHttpEntityResolver.setParams(
            Integer.parseInt(System.getProperty("nu.validator.servlet.connection-timeout","5000")),
            Integer.parseInt(System.getProperty("nu.validator.servlet.socket-timeout","5000")),
            100);
        PrudentHttpEntityResolver.setUserAgent("Validator.nu/LV");
        // force some class loading
        new VerifierServletTransaction(null, null);
        new MessageEmitterAdapter(null, false, null, 0, null);
    }

    /**
     * @return
     * @throws UnsupportedEncodingException
     */
    private static byte[] buildRobotsTxt(String primaryHost, String primaryPath, String secondaryHost, String secondaryPath, String tertiaryHost, String tertiaryPath) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append("User-agent: *\nDisallow: ");
        builder.append(primaryPath);
        builder.append("?\n");
        if (primaryHost.equals(secondaryHost)) {
            builder.append("Disallow: ");
            builder.append(secondaryPath);
            builder.append("?\n");            
        }
        if (primaryHost.equals(tertiaryHost)) {
            builder.append("Disallow: ");
            builder.append(tertiaryPath);
            builder.append("?\n");            
        }
        return builder.toString().getBytes("UTF-8");
    }
    
    private static byte[] readFromClassLoaderIntoByteArray(String name)
            throws IOException {
        InputStream ios = VerifierServlet.class.getClassLoader().getResourceAsStream(
                name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (int b = ios.read(); b != -1; b = ios.read()) {
                baos.write(b);
            }
            ios.close();
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private void writeResponse(byte[] buffer, String type,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType(type);
            response.setContentLength(buffer.length);
            response.setDateHeader("Expires",
                    System.currentTimeMillis() + 43200000); // 12 hours
            OutputStream out = response.getOutputStream();
            out.write(buffer);
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return;
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("/robots.txt".equals(request.getPathInfo())) {
            String serverName = request.getServerName();
            byte[] robotsTxt = null;
            if (hostMatch(GENERIC_HOST, serverName)) {
                robotsTxt = GENERIC_ROBOTS_TXT;
            } else if (hostMatch(HTML5_HOST, serverName)) {
                robotsTxt = HTML5_ROBOTS_TXT;
            } else if (hostMatch(PARSETREE_HOST, serverName)) {
                robotsTxt = PARSETREE_ROBOTS_TXT;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            writeResponse(robotsTxt, "text/plain; charset=utf-8", response);
            return;
        } else if ("/style.css".equals(request.getPathInfo())) {
            writeResponse(STYLE_CSS, "text/css; charset=utf-8", response);
            return;
        } else if ("/script.js".equals(request.getPathInfo())) {
            writeResponse(SCRIPT_JS, "text/javascript; charset=utf-8", response);
            return;
        } else if ("/icon.png".equals(request.getPathInfo())) {
            writeResponse(ICON_PNG, "image/png", response);
            return;
        } else if (W3C_BRANDING && "/w3c.png".equals(request.getPathInfo())) {
            writeResponse(W3C_PNG, "image/png", response);
            return;
        } else if (W3C_BRANDING && "/vnu.png".equals(request.getPathInfo())) {
            writeResponse(VNU_PNG, "image/png", response);
            return;
        } else if (W3C_BRANDING && "/html.png".equals(request.getPathInfo())) {
            writeResponse(HTML_PNG, "image/png", response);
            return;
        } else if (W3C_BRANDING && "/about.html".equals(request.getPathInfo())) {
            writeResponse(ABOUT_HTML, "text/html; charset=utf-8", response);
            return;
        }
        doPost(request, response);
    }

    private boolean hostMatch(String reference, String host) {
        if ("".equals(reference)) {
            return true;
        } else {
            // XXX case-sensitivity
            return reference.equalsIgnoreCase(host);
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#doOptions(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doOptions(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if ("*".equals(pathInfo)) { // useless RFC 2616 complication
            return;
        } else if ("/robots.txt".equals(pathInfo)) {
            String serverName = request.getServerName();
            if (hostMatch(GENERIC_HOST, serverName)
                    || hostMatch(HTML5_HOST, serverName)
                    || hostMatch(PARSETREE_HOST, serverName)) {
                sendGetOnlyOptions(request, response);
                return;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        doPost(request, response);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doTrace(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/"; // Fix for Jigsaw
        }
        String serverName = request.getServerName();
        if ("/robots.txt".equals(pathInfo)) {
            // if we get here, we've got a POST
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        log4j.debug("pathInfo: " + pathInfo);
        log4j.debug("serverName: " + serverName);
        boolean isOptions = "OPTIONS".equals(request.getMethod());
 
        if ("validator.nu".equals(serverName) && "/html5/".equals(pathInfo)) {
                response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                String queryString = request.getQueryString();
                response.setHeader("Location", "http://html5.validator.nu/" + (queryString == null ? "" : "?" + queryString));
        } else if (hostMatch(GENERIC_HOST, serverName) && GENERIC_PATH.equals(pathInfo)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            if (isOptions) {
                response.setHeader("Access-Control-Policy-Path", GENERIC_PATH);
                sendOptions(request, response);
            } else {
                new VerifierServletTransaction(request, response).service();
            }        
        } else if (hostMatch(HTML5_HOST, serverName) && HTML5_PATH.equals(pathInfo)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            if (isOptions) {
                sendOptions(request, response);
            } else {
                new Html5ConformanceCheckerTransaction(request, response).service();
            }
        } else if (hostMatch(PARSETREE_HOST, serverName) && PARSETREE_PATH.equals(pathInfo)) {
            if (isOptions) {
                sendGetOnlyOptions(request, response);
            } else {
                new ParseTreePrinter(request, response).service();
            }        
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void sendGetOnlyOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Allow", "GET, HEAD, OPTIONS");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, OPTIONS");
        response.setContentType("application/octet-stream");
        response.setContentLength(0);
    }

    private void sendOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Max-Age", "43200"); // 12 hours
        response.setHeader("Allow", "GET, HEAD, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, OPTIONS");
        response.setContentType("application/octet-stream");
        response.setContentLength(0);
    }
}
