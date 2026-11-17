/*
 * Copyright (c) 2026 Mozilla Foundation
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

package nu.validator.validation.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import nu.validator.messages.GnuMessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Tests that SimpleDocumentValidator leaves the logging configuration of the
 * application that calls it alone, and that it logs the URL of a document it
 * fetches only when asked to.
 *
 * See: https://github.com/validator/validator/issues/1313
 */
public class LoggingTest {

    private static int passed = 0;

    private static int failed = 0;

    private static final String URL_LOGGER = //
            "nu.validator.xml.PrudentHttpEntityResolver";

    private static final List<String> logged = Collections.synchronizedList(
            new ArrayList<String>());

    private static final AppenderSkeleton APPENDER = new AppenderSkeleton() {
        @Override
        protected void append(LoggingEvent event) {
            logged.add(event.getLoggerName() + " "
                    + event.getRenderedMessage());
        }

        @Override
        public boolean requiresLayout() {
            return false;
        }

        @Override
        public void close() {
        }
    };

    private static void resetLogging() {
        Logger root = Logger.getRootLogger();
        root.removeAllAppenders();
        root.addAppender(APPENDER);
        root.setLevel(Level.INFO);
    }

    private static void check(boolean logUrls, String url) throws Exception {
        SimpleDocumentValidator validator = new SimpleDocumentValidator(
                logUrls, false);
        MessageEmitterAdapter errorHandler = new MessageEmitterAdapter(null,
                validator.getSourceCode(), false,
                new ImageCollector(validator.getSourceCode()), 0, true,
                new GnuMessageEmitter(new ByteArrayOutputStream(), false));
        errorHandler.setHtml(true);
        errorHandler.start(null);
        validator.setUpMainSchema("http://s.validator.nu/html5-all.rnc",
                new SystemErrErrorHandler());
        validator.setUpValidatorAndParsers(errorHandler, false, false);
        validator.checkHttpURL(url, "LoggingTest", errorHandler);
        errorHandler.end("", "", "");
    }

    private static ServerSocket startServer() throws IOException {
        ServerSocket server = new ServerSocket(0, 50,
                InetAddress.getByName("127.0.0.1"));
        String body = "<!DOCTYPE html><html lang=en><title>t</title>";
        String response = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html; charset=utf-8\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "Connection: close\r\n\r\n" + body;
        Thread thread = new Thread(() -> {
            while (!server.isClosed()) {
                try (Socket socket = server.accept()) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(),
                                    StandardCharsets.ISO_8859_1));
                    String line;
                    while ((line = reader.readLine()) != null
                            && !line.isEmpty()) {
                        // Skip the request line and headers.
                    }
                    OutputStream out = socket.getOutputStream();
                    out.write(response.getBytes(StandardCharsets.ISO_8859_1));
                    out.flush();
                } catch (IOException e) {
                    // The server socket was closed, or a client went away.
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        return server;
    }

    private static void expect(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
            System.out.println("  PASS: " + name);
        } else {
            failed++;
            System.out.println("  FAIL: " + name + " (" + detail + ")");
        }
    }

    private static void expectUrlLogged(String name, boolean logUrls,
            String url) {
        resetLogging();
        logged.clear();
        try {
            check(logUrls, url);
        } catch (Exception e) {
            expect(name, false, "unexpected " + e);
            return;
        }
        expect(name, logged.contains(URL_LOGGER + " " + url) == logUrls,
                "logged " + logged);
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        System.setProperty("nu.validator.servlet.allow-forbidden-hosts",
                "true");
        ServerSocket server = startServer();
        String url = "http://127.0.0.1:" + server.getLocalPort() + "/";

        System.out.println("Testing that logging is left alone...");
        resetLogging();
        new SimpleDocumentValidator(true, true, false);
        Logger root = Logger.getRootLogger();
        Enumeration<?> appenders = root.getAllAppenders();
        expect("initializeLog4j leaves the root logger alone",
                appenders.hasMoreElements()
                        && appenders.nextElement() == APPENDER
                        && !appenders.hasMoreElements()
                        && root.getLevel() == Level.INFO,
                "root logger changed");

        System.out.println();
        System.out.println("Testing URL logging...");
        expectUrlLogged("URL logged with logUrls", true, url);
        expectUrlLogged("URL not logged without logUrls", false, url);

        server.close();

        System.out.println();
        System.out.println("Results: " + passed + " passed, "
                + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
