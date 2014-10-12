/*
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

package org.whattf.datatype.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.whattf.datatype.Language;

public class LanguageTester {

    private BufferedReader in;
    private Datatype datatype;

    /**
     * @throws IOException 
     * 
     */
    public LanguageTester() throws IOException {
        super();
        URL url = new URL("http://unicode.org/cldr/data/tools/java/org/unicode/cldr/util/data/langtagTest.txt");
        in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        datatype = Language.THE_INSTANCE;
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        new LanguageTester().run();
    }

    private void run() throws IOException {
        String line = null;
        while ((line = in.readLine()) != null) {
            test(line);
        }
    }

    private void test(String line) {
        int hash = line.indexOf('#');
        if (hash > -1) {
            line = line.substring(0, hash);
        }
        line = line.trim();
        try {
            datatype.checkValid(line, null);
            System.out.println(line + ": OK.");
        } catch (DatatypeException e) {
            System.out.println(line + ": " + e.getMessage());
        }
    }

}
