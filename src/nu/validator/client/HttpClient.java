/*
 * Copyright (c) 2012 Mozilla Foundation
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

package nu.validator.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

public class HttpClient {

    public static void main(String[] args) {
        URLConnection connection = null;
        InputStream input  = null;
        String host = System.getProperty("nu.validator.client.host",
                "127.0.0.1");
        String port = System.getProperty("nu.validator.client.port", "8888");
        String origin = "http://" + host + ":" + port;
        String level = System.getProperty("nu.validator.client.level", null);
        String parser = System.getProperty("nu.validator.client.parser", null);
        String charset = System.getProperty("nu.validator.client.charset", null);
        String contentType = System.getProperty(
                "nu.validator.client.content-type", null);
        boolean hasErrors = false;
        String url = origin + "/?laxtype="
                + System.getProperty("nu.validator.client.laxtype", "yes");
        url += "&out="
                + System.getProperty("nu.validator.client.out", "gnu");
        if (!"no".equals(System.getProperty("nu.validator.client.asciiquotes"))) {
            url += "&asciiquotes=yes";
        }
        if (level != null) {
            url += "&level=" + level;
        }
        if (parser != null) {
            url += "&parser=" + parser;
        }
        if (charset != null) {
            url += "&charset=" + charset;
        }
        try {
            String filepath = null;
            String filename = null;
            int filecount = (args.length == 0 ? 1 : args.length);
            for (int i = 0; i < filecount; i++) {
                if (args.length == 0 || "-".equals(args[i])) {
                  System.err.println("Waiting for document content on standard input...");
                    input = System.in;
                    filename = "[stdin]";
                } else {
                    filename = args[i];
                    filepath = filename;
                    input = new FileInputStream(filepath);
                }
                URL validator = new URL(url + "&doc=" + filename);
                connection = validator.openConnection();
                if (filename.endsWith(".xhtml")) {
                    contentType = (contentType == null ? "application/xhtml+xml"
                            : contentType);
                } else {
                    contentType = (contentType == null ? "text/html"
                            : contentType);
                }
                if ("application/xhtml+xml".equals(contentType)) {
                    parser = (parser == null ? "xml" : parser);
                }
                connection.setRequestProperty("Content-Type", contentType);
                connection.setDoOutput(true);
                connection.setReadTimeout(10000);
                byte buffer[] = new byte[4096];
                int len = 4096;
                try (OutputStream output = connection.getOutputStream()) {
                    while ((len = input.read(buffer, 0, 4096)) != -1) {
                        output.write(buffer, 0, len);
                    }
                }
                input.close();
                try (BufferedReader responseReader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()))) {
                    int c;
                    while ((c = responseReader.read()) != -1) {
                        System.out.print((char) c);
                        hasErrors = true;
                    }
                }
            }
            System.exit(hasErrors ? 1 : 0);
        } catch (MalformedURLException | ProtocolException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                System.out.printf(
                        "\nerror: Expected to find validator service"
                                + " at %s but could not connect. Stopping.\n",
                        origin);
                System.exit(1);
            } else {
                e.printStackTrace();
                System.exit(1);
            }
        } finally {
            connection = null;
            input = null;
        }
    }

}
