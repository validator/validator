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

package nu.validator.messages;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.XhtmlSaxEmitter;

import org.xml.sax.SAXException;

public final class XhtmlMessageTextHandler implements MessageTextHandler {

    private final AttributesImpl attrs = new AttributesImpl();

    private static final AttributesImpl LINE_BREAK_ATTRS = new AttributesImpl();

    private static final char[] NEWLINE_SUBSTITUTE = { '\u21A9' };

    static {
        LINE_BREAK_ATTRS.addAttribute("class", "lf");
        LINE_BREAK_ATTRS.addAttribute("title", "Line break");
    }

    private final XhtmlSaxEmitter emitter;

    private static final Map<String, String[]> MAGIC_LINKS = new HashMap<>();
    static {
      MAGIC_LINKS.put("Use CSS instead",
          new String[] {"http://wiki.whatwg.org/wiki/Presentational_elements_and_attributes",
            "About using CSS instead of presentational elements and attributes."});
      MAGIC_LINKS.put("register the names as meta extensions",
          new String[] {"http://wiki.whatwg.org/wiki/MetaExtensions",
            "About registering names as meta extensions."});
      MAGIC_LINKS.put("guidance on providing text alternatives for images",
          new String[] {"https://www.w3.org/WAI/tutorials/images/",
            "About providing text alternatives for images."});
    }

    /**
     * @param emitter
     */
    public XhtmlMessageTextHandler(final XhtmlSaxEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        String str = new String(ch);
        Map<Integer, String> linkText = new TreeMap<>();
        int index;
        for (Map.Entry<String, String[]> entry : MAGIC_LINKS.entrySet()) {
          index = str.indexOf(entry.getKey());
          if (index != -1) {
            linkText.put(index,entry.getKey());
          }
        }
        if (!linkText.isEmpty()) {
          int position = start;
          for (Map.Entry<Integer, String> entry : linkText.entrySet()) {
            int linkstart = entry.getKey();
            String text = entry.getValue();
            emitter.characters(ch, position, linkstart - position - start);
            startLink(MAGIC_LINKS.get(text)[0], MAGIC_LINKS.get(text)[1]);
            emitter.characters(ch, linkstart, text.length());
            endLink();
            position = linkstart+text.length();
          }
          if (position < length) {
            characterz(ch, position, length - position);
          }
        } else {
          characterz(ch, start, length);
        }
    }

    private void characterz(char[] ch, int start, int length)
            throws SAXException {
        int end = start + length;
        for (int i = start; i < end; i++) {
            char c = ch[i];
            switch (c) {
                case '\n':
                case '\r':
                    if (start < i) {
                        emitter.characters(ch, start, i - start);
                    }
                    start = i + 1;
                    emitter.startElement("span", LINE_BREAK_ATTRS);
                    emitter.characters(NEWLINE_SUBSTITUTE);
                    emitter.endElement("span");
                    break;
            }
        }
        if (start < end) {
            emitter.characters(ch, start, end - start);
        }
    }

    @Override
    public void endCode() throws SAXException {
        emitter.endElement("code");
    }

    @Override
    public void endLink() throws SAXException {
        emitter.endElement("a");
    }

    @Override
    public void startCode() throws SAXException {
        emitter.startElement("code");
    }

    @Override
    public void startLink(String href, String title) throws SAXException {
        assert href != null;
        attrs.clear();
        attrs.addAttribute("href", href);
        if (title != null) {
            attrs.addAttribute("title", title);
        }
        emitter.startElement("a", attrs);
    }

}
