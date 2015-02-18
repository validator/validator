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

package nu.validator.svgresearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.mortbay.util.IO;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

public class SvgDownloader {

    private static final MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

    private static final HttpClient client = new HttpClient(manager);

    static {
        HttpConnectionManagerParams hcmp = client.getHttpConnectionManager().getParams();
        hcmp.setConnectionTimeout(5000);
        hcmp.setSoTimeout(5000);
        hcmp.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION,
                100);
        hcmp.setMaxTotalConnections(200);
        HttpClientParams hcp = client.getParams();
        hcp.setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
        hcp.setIntParameter(HttpClientParams.MAX_REDIRECTS, 20); // Gecko
        // default
        client.getParams().setParameter(
                "http.useragent",
                "SET_YOUR_OWN_UA");
    }

    private static String toHexString(byte[] md5) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < md5.length; i++) {
            byte b = md5[i];
            int asInt = ((int) b) & 0xFF;
            String s = Integer.toHexString(asInt);
            if (s.length() == 1) {
                sb.append('0');
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private static void retrieve(String uri, File target) {
        for (int i = 0; i < 3; i++) {
            try {
                GetMethod m = new GetMethod(uri);
                m.setFollowRedirects(true);
                m.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
                m.addRequestHeader("Accept", "image/svg+xml, */*");
                client.executeMethod(m);
                int status = m.getStatusCode();
                if (m.getStatusCode() != 200) {
                    System.err.println(status);
                    return;
                }
                InputStream in = m.getResponseBodyAsStream();
                FileOutputStream out = new FileOutputStream(target);
                IO.copy(in, out);
                out.flush();
                out.close();
                in.close();
                m.releaseConnection();
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    /**
     * @param args
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args) throws IOException,
            NoSuchAlgorithmException {
        SortedMap<String, String> theMap = new TreeMap<String, String>();
        MessageDigest md = MessageDigest.getInstance("MD5");
        IRIFactory fac = new IRIFactory();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(args[0]), "utf-8"));
        String line;
        while ((line = in.readLine()) != null) {
            byte[] md5 = md.digest(line.getBytes("utf-8"));
            String md5str = toHexString(md5);
            IRI iri = fac.create("http://upload.wikimedia.org/wikipedia/commons/"
                    + md5str.substring(0, 1)
                    + '/'
                    + md5str.substring(0, 2)
                    + '/' + line);
            String uri = iri.toASCIIString();
            theMap.put(md5str, uri);
        }
        Writer out = new OutputStreamWriter(new FileOutputStream(args[1]),
                "utf-8");
        for (Map.Entry<String, String> entry : theMap.entrySet()) {
            out.write(entry.getKey());
            out.write('\t');
            out.write(entry.getValue());
            out.write('\n');
        }
        out.flush();
        out.close();

        File dir = new File(args[2]);

        int total = theMap.size();
        int count = 0;
        for (Map.Entry<String, String> entry : theMap.entrySet()) {
            File target = new File(dir, entry.getKey() + ".svg");
            retrieve(entry.getValue(), target);
            count++;
            System.out.println(((double) count) / ((double) total));
        }

    }

}
