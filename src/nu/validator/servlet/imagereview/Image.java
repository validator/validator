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

package nu.validator.servlet.imagereview;

import org.xml.sax.Locator;

public class Image implements Locator {

    private final String src;

    private final String alt;
    
    private final String lang;
    
    private final boolean rtl;

    private final int width;

    private final int height;

    private final boolean linked;
    
    private final String systemId;
    private final String publicId;
    private final int column;
    private final int line;
    
    /**
     * @param src
     * @param alt
     * @param width
     * @param height
     * @param locator 
     */
    public Image(String src, String alt, String lang, boolean rtl, int width, int height, boolean linked, Locator locator) {
        this.src = src;
        this.alt = alt;
        this.lang = lang;
        this.rtl = rtl;
        this.width = width;
        this.height = height;
        this.linked = linked;
        this.systemId = locator.getSystemId();
        this.publicId = locator.getPublicId();
        this.column = locator.getColumnNumber();
        this.line = locator.getLineNumber();
    }

    /**
     * Returns the src.
     * 
     * @return the src
     */
    public String getSrc() {
        return src;
    }

    /**
     * Returns the alt.
     * 
     * @return the alt
     */
    public String getAlt() {
        return alt;
    }

    /**
     * Returns the width.
     * 
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height.
     * 
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the lang.
     * 
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Returns the linked.
     * 
     * @return the linked
     */
    public boolean isLinked() {
        return linked;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public String getSystemId() {
        return systemId;
    }

    /**
     * Returns the rtl.
     * 
     * @return the rtl
     */
    public boolean isRtl() {
        return rtl;
    }
}
