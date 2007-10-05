/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2007 Mozilla Foundation
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    static {
        PrudentHttpEntityResolver.setParams(5000, 5000, 100);
        PrudentHttpEntityResolver.setUserAgent(System.getProperty(
                "nu.validator.servlet.version",
                "VerifierServlet-RELAX-NG-Validator/2.x (http://validator.nu/)"));
    }

    
    
    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }



    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String serverName = request.getServerName();
        log4j.debug("pathInfo: " + pathInfo);
        log4j.debug("serverName: " + serverName);
        if (serverName.endsWith("validator.nu")) {
            if ("validator.nu".equals(serverName)) {
                if ("/".equals(pathInfo)) {
                    new VerifierServletTransaction(request, response).service();
                } else if ("/html5/".equals(pathInfo)) {
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    String queryString = request.getQueryString();
                    response.setHeader("Location", "http://html5.validator.nu/" + (queryString == null ? "" : "?" + queryString));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if ("html5.validator.nu".equals(serverName)) {
                if ("/".equals(pathInfo)) {
                    new Html5ConformanceCheckerTransaction(request, response).service();
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if ("parsetree.validator.nu".equals(serverName)) {
                if ("/".equals(pathInfo)) {
                    new ParseTreePrinter(request, response).service();
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            if ("/".equals(pathInfo)) {
                new VerifierServletTransaction(request, response).service();
            } else if ("/html5/".equals(pathInfo)) {
                new Html5ConformanceCheckerTransaction(request, response).service();
            } else if ("/parsetree/".equals(pathInfo)) {
                new ParseTreePrinter(request, response).service();
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }



    /**
     * @see javax.servlet.http.HttpServlet#doOptions(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doOptions(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        System.out.println("OPTIONS");
        super.doOptions(arg0, arg1);
    }
}
