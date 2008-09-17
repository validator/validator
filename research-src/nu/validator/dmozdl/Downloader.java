/*
 * Copyright (c) 2008 Mozilla Foundation
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

package nu.validator.dmozdl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import nu.validator.xml.PrudentHttpEntityResolver;

import org.apache.log4j.PropertyConfigurator;
import org.mortbay.util.IO;
import org.xml.sax.InputSource;

public class Downloader implements Runnable {

    private final BufferedReader in;

    private final PrintWriter out;

    private final File rootDir;

    /**
     * @param in
     * @param out
     * @param rootDir
     * @param resolver
     */
    public Downloader(BufferedReader in, PrintWriter out, File rootDir) {
        this.in = in;
        this.out = out;
        this.rootDir = rootDir;
    }

    public void run() {
        String inLine = null;
        for (;;) {
            try {
                while ((inLine = in.readLine()) != null) {
                    String md5;
                    String url;
                    int index = inLine.indexOf('\t');
                    md5 = inLine.substring(0, index);
                    url = inLine.substring(index + 1, inLine.length());
                    InputSource is;

                    PrudentHttpEntityResolver resolver;
                    resolver = new PrudentHttpEntityResolver(1024*1024, false, null);
                    resolver.setAcceptAllKnownXmlTypes(false);
                    resolver.setAllowGenericXml(false);
                    resolver.setAllowRnc(false);
                    resolver.setAllowXhtml(false);
                    resolver.setAllowHtml(true);
                    
                    try {
                        is = resolver.resolveEntity(null, url);
                    } catch (Exception e) {
                        continue;
                    }
                    String charset = is.getEncoding();
                    if (charset == null || charset.indexOf('\t') != -1) {
                        charset = "null";
                    }
                    File top = new File(rootDir, md5.substring(0, 2));
                    synchronized (rootDir) {
                        top.mkdir();
                    }
                    File second = new File(top, md5.substring(2, 4));
                    synchronized (rootDir) {
                        second.mkdir();
                    }
                    File outFile = new File(second, md5 + ".gz");
                    InputStream inStream = is.getByteStream();
                    try {
                        OutputStream outStream = new GZIPOutputStream(
                                new FileOutputStream(outFile));
                        IO.copy(inStream, outStream);
                        outStream.flush();
                        outStream.close();
                    } catch (Exception e) {
                        outFile.delete();
                        continue;
                    } finally {
                        inStream.close();
                    }
                    out.println(md5 + '\t' + url + '\t' + charset);
                }
                return;
            } catch (Exception e) {

            }
        }
    }

    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure(new Properties());
        PrudentHttpEntityResolver.setUserAgent("Mozilla/5.0 (automated dmoz downloader)");
        PrudentHttpEntityResolver.setParams(5000, 5000, 10);
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "utf-8"));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "utf-8"), true);
        File rootDir = new File(args[2]);
        for (int i = 0; i < 8; i++) {
            (new Thread(new Downloader(in, out, rootDir))).start();
        }
    }
}
