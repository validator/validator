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

import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.ajp.Ajp13SocketConnector;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @version $Id$
 * @author hsivonen
 */
public class Main {

    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure(System.getProperty("nu.validator.servlet.log4j-properties", "log4j.properties"));
        new VerifierServletTransaction(null, null);
        Server server = new Server();
        Connector connector;
        if (args.length > 0 && "ajp".equals(args[0])) {
            connector = new Ajp13SocketConnector();
            int port = Integer.parseInt(args[1]);
            connector.setPort(port);
            connector.setHost("127.0.0.1");
        } else {
            connector = new SocketConnector();
            int port = Integer.parseInt(args[0]);
            connector.setPort(port);
        }
        server.addConnector(connector);

        Context context = new Context(server, "/");
        context.addServlet(new ServletHolder(new VerifierServlet()), "/*");
        
        server.start();
        
        System.in.read(); // XXX do something smarter
        server.stop();
    }
}