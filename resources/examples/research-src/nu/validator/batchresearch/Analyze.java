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

package nu.validator.batchresearch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Analyze {

    public static void main(String[] args) throws Exception {
        PrintWriter output = new PrintWriter(new OutputStreamWriter(
                new GZIPOutputStream(new FileOutputStream(args[2])),
                "ISO-8859-1"));
        int runs = Integer.parseInt(args[0]);

        int count = 0;
        int round = 0;

        int length = 0;

        for (int i = 0; i < runs; i++) {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(args[1])),
                    "ISO-8859-1"));
            Map<String, IntWrap> theDict = new HashMap<String, IntWrap>();
            try {
                String prevUrl = null;
                String line = null;
                boolean collect = false;
                while ((line = input.readLine()) != null) {
                    length += line.length();
                    String message = null;
                    String url = null;
                    String mode = null;
                    String phase = null;
                    String[] s = line.split("\t");
                    if (s.length == 4) {
                        url = s[0];
                        mode = s[1];
                        phase = s[2];
                        message = s[3];
                    } else {
                        System.out.printf("Bad line: %s\n", line);
                        System.out.printf("Length: %d\n", length);
                        break;
                    }
                    if ("Q".equals(mode)) {
                        continue;
                    }
                    if (!url.equals(prevUrl)) {
                        count++;
                        prevUrl = url;
                        int newRound = (count / 100) * 100;
                        if (round != newRound) {
                            round = newRound;
//                            System.out.printf("%d %d\n", i, round);
                        }
                        collect = message.equals("NO PARSE ERRORS");
                        if (collect) {
                            System.out.println(url);
                        }
                    }
                    if (!collect) {
                        continue;
                    }
                    if (message.hashCode() % runs == i) {
                        IntWrap wrap = theDict.get(message);
                        if (wrap == null) {
                            theDict.put(message, new IntWrap());
                        } else {
                            wrap.increment();
                        }
                    }
                }
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String, IntWrap> entry : theDict.entrySet()) {
                double ratio = ((double) entry.getValue().getValue())
                        / ((double) count);
                if (ratio > 0.0001)
                    output.printf("%6.4f\t%s\n", ratio, entry.getKey());
            }
            output.close();
        }

    }
}