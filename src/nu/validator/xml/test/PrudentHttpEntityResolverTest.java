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


package nu.validator.xml.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import nu.validator.xml.PrudentHttpEntityResolver;

import org.xml.sax.InputSource;

/**
 * Unit tests for how PrudentHttpEntityResolver reports a redirect that the
 * HTTP client can't follow.
 */
public class PrudentHttpEntityResolverTest {

    private static int passed = 0;

    private static int failed = 0;

    private static final String IGNORE_RESPONSE_STATUS = //
            "http://validator.nu/properties/ignore-response-status";

    // Path -> the raw HTTP response the test server sends for it.
    private static final Map<String, String> RESPONSES = Map.of(
            "/app-scheme", redirect("302", "dongqiudi://v1/user/login?x=y"),
            "/no-host", redirect("302", "https:///foo"),
            "/no-location",
            "HTTP/1.1 302 Found\r\nContent-Length: 0\r\n"
                    + "Connection: close\r\n\r\n",
            "/to-ok", redirect("301", "/ok"),
            "/ok", "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n"
                    + "Content-Length: 2\r\nConnection: close\r\n\r\nok");

    private static String base;

    public static void main(String[] args) throws Exception {
        System.setProperty("nu.validator.servlet.allow-forbidden-hosts",
                "true");
        PrudentHttpEntityResolver.setParams(5000, 5000, 10);
        ServerSocket server = startServer();
        base = "http://127.0.0.1:" + server.getLocalPort();

        for (boolean ignoreStatus : new boolean[] { false, true }) {
            String mode = ignoreStatus ? " (checkerrorpages)" : "";
            expectFailure("redirect to an app scheme" + mode, "/app-scheme",
                    ignoreStatus, "The remote server redirected to "
                            + "“dongqiudi://v1/user/login?x=y”, "
                            + "which isn't an http or https URL.");
            expectFailure("redirect to a URL with no host" + mode,
                    "/no-host", ignoreStatus,
                    "The remote server redirected to “https:///foo”,"
                            + " which couldn't be followed: ");
            expectFailure("redirect with no Location" + mode,
                    "/no-location", ignoreStatus,
                    "The remote server sent a 302 redirect with no Location"
                            + " header.");
            expectBody("redirect to a conforming URL" + mode, "/to-ok",
                    ignoreStatus, "ok");
        }

        server.close();
        System.out.println();
        System.out.println(
                "Results: " + passed + " passed, " + failed + " failed");
        System.exit(failed > 0 ? 1 : 0);
    }

    private static String redirect(String status, String location) {
        return "HTTP/1.1 " + status + " Moved\r\nLocation: " + location
                + "\r\nContent-Length: 0\r\nConnection: close\r\n\r\n";
    }

    private static ServerSocket startServer() throws IOException {
        ServerSocket server = new ServerSocket(0, 50,
                InetAddress.getByName("127.0.0.1"));
        Thread thread = new Thread(() -> {
            while (!server.isClosed()) {
                try (Socket socket = server.accept()) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(),
                                    StandardCharsets.ISO_8859_1));
                    String requestLine = reader.readLine();
                    String line;
                    while ((line = reader.readLine()) != null
                            && !line.isEmpty()) {
                        // Skip the request headers.
                    }
                    String path = requestLine == null ? ""
                            : requestLine.split(" ")[1];
                    String response = RESPONSES.getOrDefault(path,
                            "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n"
                                    + "Connection: close\r\n\r\n");
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

    private static PrudentHttpEntityResolver resolver(boolean ignoreStatus) {
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                PrudentHttpEntityResolverTest.class.getClassLoader(),
                new Class<?>[] { HttpServletRequest.class },
                (proxy, method, args) -> ignoreStatus
                        && "getAttribute".equals(method.getName())
                        && IGNORE_RESPONSE_STATUS.equals(args[0])
                                ? Boolean.TRUE
                                : null);
        return new PrudentHttpEntityResolver(-1, true, null, request);
    }

    private static String readAll(InputSource is) throws IOException {
        try (InputStream in = is.getByteStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void expectFailure(String testName, String path,
            boolean ignoreStatus, String expectedMessagePart) {
        try {
            InputSource is = resolver(ignoreStatus).resolveEntity(null,
                    base + path);
            readAll(is);
            fail(testName + ": no exception");
        } catch (PrudentHttpEntityResolver.ResourceNotRetrievableException e) {
            if (e.getMessage().contains(expectedMessagePart)) {
                pass(testName);
            } else {
                fail(testName + ": message was: " + e.getMessage());
            }
        } catch (Exception e) {
            fail(testName + ": unexpected " + e);
        }
    }

    private static void expectBody(String testName, String path,
            boolean ignoreStatus, String expectedBody) {
        try {
            String body = readAll(resolver(ignoreStatus).resolveEntity(null,
                    base + path));
            if (expectedBody.equals(body)) {
                pass(testName);
            } else {
                fail(testName + ": body was: " + body);
            }
        } catch (Exception e) {
            fail(testName + ": unexpected " + e);
        }
    }

    private static void pass(String testName) {
        System.out.println("PASS: " + testName);
        passed++;
    }

    private static void fail(String testName) {
        System.out.println("FAIL: " + testName);
        failed++;
    }
}
