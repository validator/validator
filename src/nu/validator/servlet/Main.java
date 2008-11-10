/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2007-2008 Mozilla Foundation
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


import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import nu.validator.servletfilter.InboundGzipFilter;
import nu.validator.servletfilter.InboundSizeLimitFilter;

import org.apache.log4j.PropertyConfigurator;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.ajp.Ajp13SocketConnector;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.servlet.GzipFilter;
import org.mortbay.thread.QueuedThreadPool;

/**
 * @version $Id$
 * @author hsivonen
 */
public class Main {

    private static final long SIZE_LIMIT = Integer.parseInt(System.getProperty(
            "nu.validator.servlet.max-file-size", "2097152"));
    
    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure(System.getProperty("nu.validator.servlet.log4j-properties", "log4j.properties"));
        Server server = new Server();
        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMaxThreads(100);
        server.setThreadPool(pool);
        Connector connector;
        int stopPort = -1;
        if (args.length > 0 && "ajp".equals(args[0])) {
            connector = new Ajp13SocketConnector();
            int port = Integer.parseInt(args[1]);
            connector.setPort(port);
            connector.setHost("127.0.0.1");
            if (args.length > 2) {
                stopPort = Integer.parseInt(args[2]);
            }
        } else {
            connector = new SocketConnector();
            int port = Integer.parseInt(args[0]);
            connector.setPort(port);
            if (args.length > 1) {
                stopPort = Integer.parseInt(args[1]);
            }
        }
        server.addConnector(connector);
        
        Context context = new Context(server, "/");
        context.addFilter(new FilterHolder(new GzipFilter()), "/*", Handler.REQUEST);
        context.addFilter(new FilterHolder(new InboundSizeLimitFilter(SIZE_LIMIT)), "/*", Handler.REQUEST);
        context.addFilter(new FilterHolder(new InboundGzipFilter()), "/*", Handler.REQUEST);
        context.addFilter(new FilterHolder(new MultipartFormDataFilter()), "/*", Handler.REQUEST);
        context.addServlet(new ServletHolder(new VerifierServlet()), "/*");
        
        if (stopPort != -1) {
            try {
                Socket clientSocket = new Socket(
                        InetAddress.getByName("127.0.0.1"), stopPort);
                InputStream in = clientSocket.getInputStream();
                in.read();
                in.close();
                clientSocket.close();
            } catch (ConnectException e) {
                
            }

            server.start();
            
            ServerSocket serverSocket = new ServerSocket(stopPort, 0, InetAddress.getByName("127.0.0.1"));
            Socket s = serverSocket.accept();
            
            server.stop();
            
            OutputStream out = s.getOutputStream();
            out.close();
            s.close();
            serverSocket.close();
        } else {
            server.start();
            System.in.read();            
            server.stop();
        }
    }
}