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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

public class SvgAnalysisHandler implements ContentHandler, LexicalHandler,
        DTDHandler, DeclHandler {

    private ScoreBoard generalScoreBoard = new ScoreBoard();
    
    private Map<String, ScoreBoard> scoreBoardsByCreator = new HashMap<String, ScoreBoard>();
    
    private boolean nonSvgRoot = false;

    private boolean nonNamespaceSvgRoot = false;

    private boolean otherNamespaceSvgRoot = false;

    private boolean hasFlowRoot = false;

    private final Set<NameTriple> prefixedSvgElements = new HashSet<NameTriple>();

    private final Set<NameTriple> foreignElementsInMetadata = new HashSet<NameTriple>();

    private final Set<NameTriple> foreignElementsElsewhere = new HashSet<NameTriple>();

    private final Set<String> unconventionalXLinkPrefixes = new HashSet<String>();

    private final Set<NameTriple> prefixedAttributes = new HashSet<NameTriple>();

    private final Set<NameTriple> fontAttributes = new HashSet<NameTriple>();

    private final Set<String> fontParent = new HashSet<String>();
    
    private final Set<String> piTargets = new HashSet<String>();

    private final Set<String> requiredExtensions = new HashSet<String>();
    
    private final LinkedList<String> stack = new LinkedList<String>();

    private final Set<String> internalEntities = new HashSet<String>();
    
    private String creator = null;
    
    private boolean hasDoctype = false;

    private boolean hasInternalSubset = false;

    private boolean hasMetadata = false;

    private boolean hasStyleAttribute = false;

    private boolean hasPresentationAttributes = false;
    
    private boolean hasStyleElement = false;

    private boolean hasDefinitionElementsOutsideDefs = false;
    
    private void push(String name) {
        stack.addFirst(name);
    }

    private void pop() {
        stack.removeFirst();
    }

    private String peek() {
        if (stack.size() == 0) {
            return null;
        }
        return stack.getFirst();
    }

    private boolean hasAncestor(String name) {
        for (String node : stack) {
            if (node == name) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("boxing") private void fillScoreBoard(ScoreBoard board) {
        board.total++;
        
        if (nonSvgRoot) { board.nonSvgRoot++; }

        if (nonNamespaceSvgRoot) { board.nonNamespaceSvgRoot++; }

        if (otherNamespaceSvgRoot) { board.otherNamespaceSvgRoot++; }

        if (hasFlowRoot) { board.hasFlowRoot++; }

        if (hasDoctype) { board.hasDoctype++; }

        if (hasInternalSubset) { board.hasInternalSubset++; }

        if (hasMetadata) { board.hasMetadata++; }

        if (hasStyleAttribute) { board.hasStyleAttribute++; }

        if (hasPresentationAttributes) { board.hasPresentationAttributes++; }
        
        if (hasStyleElement) { board.hasStyleElement++; }

        if (hasDefinitionElementsOutsideDefs) { board.hasDefinitionElementsOutsideDefs++; }
        
        Integer creatorCount = board.creator.get(creator);
        if (creatorCount == null) {
            board.creator.put(creator, 1);
        } else {
            board.creator.put(creator, creatorCount.intValue() + 1);
        }
        
        fillMapFromSetTriple(board.prefixedSvgElements, prefixedSvgElements);

        fillMapFromSetTriple(board.foreignElementsInMetadata, foreignElementsInMetadata);

        fillMapFromSetTriple(board.foreignElementsElsewhere, foreignElementsElsewhere);

        fillMapFromSetTriple(board.prefixedAttributes, prefixedAttributes);

        fillMapFromSetTriple(board.fontAttributes, fontAttributes);

        fillMapFromSetString(board.unconventionalXLinkPrefixes, unconventionalXLinkPrefixes);
        
        fillMapFromSetString(board.fontParent, fontParent);
        
        fillMapFromSetString(board.piTargets, piTargets);

        fillMapFromSetString(board.requiredExtensions, requiredExtensions);
        
        fillMapFromSetString(board.internalEntities, internalEntities);
    }
    
    @SuppressWarnings("boxing") private void fillMapFromSetString(Map<String, Integer> map, Set<String> set) {
        if (set.size() > 0) {
            Integer count = map.get("ANY");
            if (count == null) {
                map.put("ANY", 1);
            } else {
                map.put("ANY", count.intValue() + 1);
            }
        }
        for (String object : set) {
            Integer count = map.get(object);
            if (count == null) {
                map.put(object, 1);
            } else {
                map.put(object, count.intValue() + 1);
            }
        }
    }

    @SuppressWarnings("boxing") private void fillMapFromSetTriple(Map<NameTriple, Integer> map, Set<NameTriple> set) {
        if (set.size() > 0) {
            Integer count = map.get(NameTriple.ANY_MARKER);
            if (count == null) {
                map.put(NameTriple.ANY_MARKER, 1);
            } else {
                map.put(NameTriple.ANY_MARKER, count.intValue() + 1);
            }
        }
        for (NameTriple object : set) {
            Integer count = map.get(object);
            if (count == null) {
                map.put(object, 1);
            } else {
                map.put(object, count.intValue() + 1);
            }
        }
    }

    public void print() {
        generalScoreBoard.printScoreBoard();
                
        for (Map.Entry<String, ScoreBoard> entry : scoreBoardsByCreator.entrySet()) {
        
            System.out.println("\n********************************\n");

            System.out.println(entry.getKey());
            
            entry.getValue().printScoreBoard();
        }
    }
    
    public void startDocument() throws SAXException {
        nonSvgRoot = false;
        nonNamespaceSvgRoot = false;
        otherNamespaceSvgRoot = false;
        hasFlowRoot = false;
        prefixedSvgElements.clear();
        foreignElementsInMetadata.clear();
        foreignElementsElsewhere.clear();
        unconventionalXLinkPrefixes.clear();
        prefixedAttributes.clear();
        piTargets.clear();
        stack.clear();
        creator = null;
        hasDoctype = false;
        fontAttributes.clear();
        hasInternalSubset = false;
        hasMetadata = false;
        fontParent.clear();
        internalEntities.clear();
        hasStyleAttribute = false;
        hasStyleElement = false;
        hasDefinitionElementsOutsideDefs = false;
        hasPresentationAttributes = false;
        requiredExtensions.clear();
    }

    public void endDocument() throws SAXException {
        if (creator == null) {
            creator = "NO CREATOR";
        }
        fillScoreBoard(generalScoreBoard);
        ScoreBoard creatorBoard = scoreBoardsByCreator.get(creator);
        if (creatorBoard == null) {
            creatorBoard = new ScoreBoard();
            scoreBoardsByCreator.put(creator, creatorBoard);
        }
        fillScoreBoard(creatorBoard);
    }

    public void endElement(String uri, String localName, String name)
            throws SAXException {
        pop();
    }

    public void startElement(String uri, String localName, String qname,
            Attributes atts) throws SAXException {
        String parent = peek();
        if (parent == null) {
            if (localName == "svg") {
                if (uri == "") {
                    nonNamespaceSvgRoot = true;
                } else if (uri != "http://www.w3.org/2000/svg") {
                    otherNamespaceSvgRoot = true;
                }
            } else {
                nonSvgRoot = true;
            }
        }

        if (localName == "flowRoot") {
            hasFlowRoot = true;
        } else if (!nonSvgRoot && "font" == localName) {
            fontParent.add(peek());         
        } else if ("style" == localName) {
            hasStyleElement = true;         
        }
        
        if ("clipPath" == localName ||  
        "color-profile" == localName ||  "cursor" == localName ||  "filter" == localName ||  "font" == localName ||  "linearGradient" == localName ||  "marker" == localName ||  
        "mask" == localName ||  "pattern" == localName ||  "radialGradient" == localName ||  "solidColor" == localName ||  "symbol" == localName) {
            if (hasAncestor("defs")) {
                hasDefinitionElementsOutsideDefs = true;
            }
        }
        
        NameTriple eltTriple = new NameTriple(localName, qname, uri);

        if (uri == "http://www.w3.org/2000/svg") {
            if (eltTriple.getPrefix() != "") {
                // we've got a prefixed element in the SVG ns
                prefixedSvgElements.add(eltTriple);
            }
        } else {
            if (hasAncestor("metadata")) {
                hasMetadata = true;
                foreignElementsInMetadata.add(eltTriple);
            } else {
                foreignElementsElsewhere.add(eltTriple);
            }
        }

        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            String attLocal = atts.getLocalName(i);
            String attQname = atts.getQName(i);
            String attUri = atts.getURI(i);
            NameTriple attTriple = new NameTriple(attLocal, attQname, attUri);
            if (!nonSvgRoot && "font" == localName) {
                fontAttributes.add(attTriple);
            }
            String prefix = attTriple.getPrefix();
            if (prefix != "") {
                if ("http://www.w3.org/XML/1998/namespace" == attUri) {
                    // do nothing
                } else if ("http://www.w3.org/1999/xlink" == attUri) {
                    if (prefix != "xlink") {
                        unconventionalXLinkPrefixes.add(prefix);
                    }
                } else {
                    prefixedAttributes.add(attTriple);
                }
            } else {
                if ("style" == attLocal) {
                    hasStyleAttribute = true;
                } else if ("requiredExtensions" == attLocal) {
                    requiredExtensions.add(atts.getValue(i));
                } else if ("alignment-baseline" == attLocal || 
                        "baseline-shift" == attLocal || 
                        "clip" == attLocal || 
                        "clip-path" == attLocal || 
                        "clip-rule" == attLocal || 
                        "color" == attLocal || 
                        "color-interpolation" == attLocal || 
                        "color-interpolation-filters" == attLocal || 
                        "color-profile" == attLocal || 
                        "color-rendering" == attLocal || 
                        "cursor" == attLocal || 
                        "direction" == attLocal || 
                        "display" == attLocal || 
                        "dominant-baseline" == attLocal || 
                        "enable-background" == attLocal || 
                        "fill" == attLocal || 
                        "fill-opacity" == attLocal || 
                        "fill-rule" == attLocal || 
                        "filter" == attLocal || 
                        "flood-color" == attLocal || 
                        "flood-opacity" == attLocal || 
                        "font" == attLocal || 
                        "font-family" == attLocal || 
                        "font-size" == attLocal || 
                        "font-size-adjust" == attLocal || 
                        "font-stretch" == attLocal || 
                        "font-style" == attLocal || 
                        "font-variant" == attLocal || 
                        "font-weight" == attLocal || 
                        "glyph-orientation-horizontal" == attLocal || 
                        "glyph-orientation-vertical" == attLocal || 
                        "image-rendering" == attLocal || 
                        "kerning" == attLocal || 
                        "letter-spacing" == attLocal || 
                        "lighting-color" == attLocal || 
                        "marker" == attLocal || 
                        "marker-end" == attLocal || 
                        "marker-mid" == attLocal || 
                        "marker-start" == attLocal || 
                        "mask" == attLocal || 
                        "opacity" == attLocal || 
                        "overflow" == attLocal || 
                        "pointer-events" == attLocal || 
                        "shape-rendering" == attLocal || 
                        "stop-color" == attLocal || 
                        "stop-opacity" == attLocal || 
                        "stroke" == attLocal || 
                        "stroke-dasharray" == attLocal || 
                        "stroke-dashoffset" == attLocal || 
                        "stroke-linecap" == attLocal || 
                        "stroke-linejoin" == attLocal || 
                        "stroke-miterlimit" == attLocal || 
                        "stroke-opacity" == attLocal || 
                        "stroke-width" == attLocal || 
                        "text-anchor" == attLocal || 
                        "text-decoration" == attLocal || 
                        "text-rendering" == attLocal || 
                        "unicode-bidi" == attLocal || 
                        "visibility" == attLocal || 
                        "word-spacing" == attLocal || 
                        "writing-mode" == attLocal || 
                        "audio-level" == attLocal || 
                        "display-align" == attLocal || 
                        "line-increment" == attLocal || 
                        "solid-color" == attLocal || 
                        "solid-opacity" == attLocal || 
                        "text-align" == attLocal || 
                        "vector-effect" == attLocal || 
                        "viewport-fill" == attLocal || 
                        "viewport-fill-opacity" == attLocal) {
                    hasPresentationAttributes = true;
                }
            }
        }
                
        if (uri == "http://www.w3.org/2000/svg") {
            push(localName);
        } else {
            push("");
        }
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (creator == null) {
            String str = new String(ch, start, length);
            if (str.startsWith(" Created with Arkyan's SVGCensus script")) {
                creator = "Arkyan's SVGCensus script";
            } else if (str.startsWith(" Created with ")) {
                int index = str.indexOf(' ', 14);
                creator = str.substring(14, index);
            } else if (str.startsWith(" Generator: Adobe Illustrator")) {
                creator = "Adobe Illustrator";
            } else if (str.startsWith(" Generator: ")) {
                int index = str.indexOf(' ', 12);
                creator = str.substring(12, index);
            }
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        piTargets.add(target);
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        hasDoctype = true;
    }

    public void startEntity(String name) throws SAXException {
    }

    public void notationDecl(String name, String publicId, String systemId)
            throws SAXException {
        hasInternalSubset = true;
    }

    public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
        hasInternalSubset = true;
    }

    public void attributeDecl(String name, String name2, String type,
            String mode, String value) throws SAXException {
        hasInternalSubset = true;
    }

    public void elementDecl(String name, String model) throws SAXException {
        hasInternalSubset = true;
    }

    public void externalEntityDecl(String name, String publicId, String systemId)
            throws SAXException {
        hasInternalSubset = true;
    }

    public void internalEntityDecl(String name, String value)
            throws SAXException {
        hasInternalSubset = true;
        internalEntities.add(name);
    }

}
