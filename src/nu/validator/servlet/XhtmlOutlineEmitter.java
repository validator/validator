/*
 * Copyright (c) 2012 Vadim Zaslawski, Ontos AG
 * Copyright (c) 2012-2017 Mozilla Foundation
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

    private static final char[] OUTLINE = //
            "Structural outline".toCharArray();

    private static final char[] HEADINGOUTLINE = //
            "Heading-level outline".toCharArray();

    private final Deque<Section> outline;

    private final Deque<Section> headingOutline;

    private final XhtmlSaxEmitter emitter;

    private final AttributesImpl attrs = new AttributesImpl();

    public XhtmlOutlineEmitter(final ContentHandler contentHandler,
            final Deque<Section> outline, final Deque<Section> headingOutline) {
        this.emitter = new XhtmlSaxEmitter(contentHandler);
        this.outline = outline;
        this.headingOutline = headingOutline;
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

    boolean hasH1 = false;

    boolean hasH2 = false;

    boolean hasH3 = false;

    boolean hasH4 = false;

    boolean hasH5 = false;

    boolean emittedDummyH1 = false;

    boolean emittedDummyH2 = false;

    boolean emittedDummyH3 = false;

    boolean emittedDummyH4 = false;

    boolean emittedDummyH5 = false;

    public void emitHeadings() throws SAXException {
        hasH1 = false;
        hasH2 = false;
        hasH3 = false;
        hasH4 = false;
        hasH5 = false;
        emittedDummyH1 = false;
        emittedDummyH2 = false;
        emittedDummyH3 = false;
        emittedDummyH4 = false;
        emittedDummyH5 = false;
        if (headingOutline != null) {
            attrs.clear();
            attrs.addAttribute("id", "headingoutline");
            emitter.startElement("section", attrs);
            emitter.startElement("h2");
            emitter.characters(HEADINGOUTLINE);
            emitter.endElement("h2");
            try {
                emitHeadingOutline(headingOutline, 0);
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
            if (!section.getIsMasked()) {
                emitter.startElement("li");
                StringBuilder headingText = section.getHeadingTextBuilder();
                if (headingText.length() > 0) {
                    emitter.startElementWithClass("span", "heading");
                    emitter.characters(headingText.toString().toCharArray());
                    if (section.getSubheadSections() != null) {
                        for (Section subhead : section.getSubheadSections()) {
                            emitter.characters(": ".toCharArray());
                            StringBuilder subheadText = subhead.getHeadingTextBuilder();
                            emitter.characters(
                                    subheadText.toString().toCharArray());
                        }
                    }
                    emitter.endElement("span");
                } else if (section.hasEmptyHeading()) {
                    emitter.characters(("[" + section.getElementName()
                            + " element with empty heading]").toCharArray());
                } else if ("h1".equals(section.getElementName())
                        || "h2".equals(section.getElementName())
                        || "h3".equals(section.getElementName())
                        || "h4".equals(section.getElementName())
                        || "h5".equals(section.getElementName())
                        || "h6".equals(section.getElementName())) {
                    emitter.characters(("[section implied by empty "
                            + section.getElementName()
                            + " element]").toCharArray());
                } else {
                    emitter.characters(("[" + section.getElementName()
                            + " element with no heading]").toCharArray());
                }
            }
            Deque<Section> sections = section.sections;
            if (!sections.isEmpty()) {
                emitOutline(sections, currentDepth + 1);
            }
            if (!section.getIsMasked()) {
                emitter.endElement("li");
            }
        }
        emitter.endElement("ol");
    }

    protected void emitHeadingOutline(Deque<Section> headingOutline,
            int currentDepth) throws IOException, SAXException {
        for (Section section : headingOutline) {
            String headingName = section.getHeadingElementName();
            if ("h1".equals(headingName)) {
                hasH1 = true;
                hasH2 = false;
                hasH3 = false;
                hasH4 = false;
                hasH5 = false;
                emittedDummyH2 = false;
                emittedDummyH3 = false;
                emittedDummyH4 = false;
                emittedDummyH5 = false;
            } else if ("h2".equals(headingName)) {
                hasH2 = true;
                hasH3 = false;
                hasH4 = false;
                hasH5 = false;
                emittedDummyH3 = false;
                emittedDummyH4 = false;
                emittedDummyH5 = false;
            } else if ("h3".equals(headingName)) {
                hasH3 = true;
                hasH4 = false;
                hasH5 = false;
                emittedDummyH4 = false;
                emittedDummyH5 = false;
            } else if ("h4".equals(headingName)) {
                hasH4 = true;
                hasH5 = false;
                emittedDummyH5 = false;
            } else if ("h5".equals(headingName)) {
                hasH5 = true;
            }
            if ("h1".equals(headingName) || "h2".equals(headingName)
                    || "h3".equals(headingName) || "h4".equals(headingName)
                    || "h5".equals(headingName) || "h6".equals(headingName)) {
                StringBuilder headingText = section.getHeadingTextBuilder();
                if (!"h1".equals(headingName) && !hasH1 && !emittedDummyH1) {
                    emitMissingHeading("h1");
                    emittedDummyH1 = true;
                }
                if (!"h1".equals(headingName) && !"h2".equals(headingName)
                        && !hasH2 && !emittedDummyH2) {
                    emitMissingHeading("h2");
                    emittedDummyH2 = true;
                }
                if (!"h1".equals(headingName) && !"h2".equals(headingName)
                        && !"h3".equals(headingName) && !hasH3
                        && !emittedDummyH3) {
                    emitMissingHeading("h3");
                    emittedDummyH3 = true;
                }
                if (!"h1".equals(headingName) && !"h2".equals(headingName)
                        && !"h3".equals(headingName)
                        && !"h4".equals(headingName) && !hasH4
                        && !emittedDummyH4) {
                    emitMissingHeading("h4");
                    emittedDummyH4 = true;
                }
                if ("h6".equals(headingName) && !hasH5 && !emittedDummyH5) {
                    emitMissingHeading("h5");
                    emittedDummyH5 = true;
                }
                emitter.startElementWithClass("p", headingName);
                emitter.startElementWithClass("span", "headinglevel");
                emitter.characters(("<" + headingName + ">").toCharArray());
                emitter.endElement("span");
                if (headingText.length() > 0) {
                    emitter.characters(
                            (" " + headingText.toString()).toCharArray());
                } else {
                    emitter.startElementWithClass("span", "missingheading");
                    emitter.characters((" [empty]").toCharArray());
                    emitter.endElement("span");
                }
                emitter.endElement("p");
            }
            Deque<Section> sections = section.sections;
            if (!sections.isEmpty()) {
                emitHeadingOutline(sections, currentDepth + 1);
            }
        }
    }

    private void emitMissingHeading(String headingName) throws SAXException {
        emitter.startElementWithClass("p", headingName);
        emitter.startElementWithClass("span", "missingheadinglevel");
        emitter.characters(("<" + headingName + ">").toCharArray());
        emitter.endElement("span");
        emitter.startElementWithClass("span", "missingheading");
        emitter.characters((" [missing]").toCharArray());
        emitter.endElement("span");
        emitter.endElement("p");
    }
}
