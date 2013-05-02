/*
 * Copyright (c) 2012 Vadim Zaslawski, Ontos AG
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

package nu.validator.servlet;

import java.io.IOException;
import java.util.Deque;

import nu.validator.servlet.OutlineBuildingXMLReaderWrapper.Section;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.XhtmlSaxEmitter;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class XhtmlOutlineEmitter {

    private static final char[] OUTLINE = "Outline".toCharArray();

    private final Deque<Section> outline;

    private final XhtmlSaxEmitter emitter;

    private final AttributesImpl attrs = new AttributesImpl();

    public XhtmlOutlineEmitter(final ContentHandler contentHandler,
            final Deque<Section> outline) {
        this.emitter = new XhtmlSaxEmitter(contentHandler);
        this.outline = outline;
    }

    public void emit() throws SAXException {
        if (outline != null) {
            attrs.clear();
            attrs.addAttribute("id", "outline");
            emitter.startElement("section", attrs);
            emitter.startElement("h2");
            emitter.characters(OUTLINE);
            emitter.endElement("h2");
            try {
                emitOutline(outline, 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            emitter.endElement("section");
        }
    }

    protected void emitOutline(Deque<Section> outline, int currentDepth)
            throws IOException, SAXException {
        emitter.startElement("ol");
        for (Section section : outline) {
            emitter.startElement("li");
            StringBuilder headingText = section.getHeadingTextBuilder();
            if (headingText.length() > 0) {
                emitter.startElementWithClass("span", "heading");
                emitter.characters(headingText.toString().toCharArray());
                emitter.endElement("span");
            } else if ("h1".equals(section.getElementName())
                    || "h2".equals(section.getElementName())
                    || "h3".equals(section.getElementName())
                    || "h4".equals(section.getElementName())
                    || "h5".equals(section.getElementName())
                    || "h6".equals(section.getElementName())) {
                emitter.characters(("[section implied by empty "
                        + section.getElementName() + " element]").toCharArray());
            } else {
                emitter.characters(("[" + section.getElementName() + " element with no heading]").toCharArray());
            }
            Deque<Section> sections = section.sections;
            if (!sections.isEmpty()) {
                emitOutline(sections, currentDepth + 1);
            }
            emitter.endElement("li");
        }
        emitter.endElement("ol");
    }

}
