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

package nu.validator.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import nu.validator.htmlparser.impl.CharacterHandler;
import nu.validator.xml.TypedInputSource;

public final class SourceCode implements CharacterHandler {

    private static Location[] SOURCE_LOCATION_ARRAY_TYPE = new Location[0];

    private String uri;
    
    private int expectedLength;

    private final SortedSet<Location> reverseSortedLocations = new TreeSet<Location>(
            new ReverseLocationComparator());

    private final SortedSet<Location> exactErrors = new TreeSet<Location>();

    private final SortedSet<Location> rangeEnds = new TreeSet<Location>();

    private final List<Line> lines = new ArrayList<Line>();

    private Line currentLine = null;

    private boolean prevWasCr = false;
    
    private final LocationRecorder locationRecorder;
    
    public SourceCode() {
        this.locationRecorder = new LocationRecorder(this);
    }

    public void initialize(InputSource inputSource) {
        this.uri = inputSource.getSystemId();
        if (inputSource instanceof TypedInputSource) {
            TypedInputSource typedInputSource = (TypedInputSource) inputSource;
            int length = typedInputSource.getLength();
            if (length == -1) {
                expectedLength = 2048;
            } else {
                expectedLength = length;
            }
        } else {
            expectedLength = 2048;
        }
    }
    
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        int s = start;
        int end = start + length;
        for (int i = start; i < end; i++) {
            char c = ch[i];
            switch (c) {
                case '\r':
                    if (s < i) {
                        currentLine.characters(ch, s, i - s);
                    }
                    newLine();
                    s = i + 1;
                    prevWasCr = true;
                    break;
                case '\n':
                    if (!prevWasCr) {
                        if (s < i) {
                            currentLine.characters(ch, s, i - s);
                        }
                        newLine();
                    }
                    s = i + 1;
                    prevWasCr = false;
                    break;
                default:
                    prevWasCr = false;
                    break;
            }
        }
        if (s < end) {
            currentLine.characters(ch, s, end - s);
        }
    }

    private void newLine() {
        int offset;
        char[] buffer;
        if (currentLine == null) {
            offset = 0;
            buffer = new char[expectedLength];
        } else {
            offset = currentLine.getOffset() + currentLine.getBufferLength();
            buffer = currentLine.getBuffer();
        }
        currentLine = new Line(buffer, offset);
        lines.add(currentLine);
    }

    public void end() throws SAXException {
        if (currentLine.getBufferLength() == 0) {
            // Theoretical impurity with line separators vs. terminators
            lines.remove(lines.size() - 1);
        }
    }

    public void start() throws SAXException {
        reverseSortedLocations.clear();
        lines.clear();
        currentLine = null;
        newLine();
        prevWasCr = false;
    }

    void addLocatorLocation(int oneBasedLine, int oneBasedColumn) {
        reverseSortedLocations.add(new Location(this, oneBasedLine - 1,
                oneBasedColumn - 1));
    }

    public void exactError(int oneBasedLine, int oneBasedColumn,
            SourceHandler extractHandler) throws SAXException {
        int zeroBasedLine = oneBasedLine - 1;
        int zeroBasedColumn = oneBasedColumn - 1;
        Location location = new Location(this, zeroBasedLine, zeroBasedColumn);
        exactErrors.add(location);
        if (isWithinKnownSource(location)) {
            Location start = location.step(-15);
            Location end = location.step(6);
            try {
                extractHandler.start();
                emitContent(start, location, extractHandler);
                extractHandler.startCharHilite(oneBasedLine, oneBasedColumn);
                emitCharacter(location, extractHandler);
                extractHandler.endCharHilite();
                newLineIfAtEndOfLine(location, extractHandler);
                location = location.next();
                emitContent(location, end, extractHandler);
            } finally {
                extractHandler.end();
            }
        }
    }

    /**
     * @param location
     * @param handler
     * @throws SAXException
     */
    private void newLineIfAtEndOfLine(Location location, SourceHandler handler) throws SAXException {
        Line line = getLine(location.getLine());
        if (location.getColumn() + 1 == line.getBufferLength()) {
            handler.newLine();
        }
    }

    public void rangeEndError(int oneBasedLine, int oneBasedColumn,
            SourceHandler extractHandler) throws SAXException {
        int zeroBasedLine = oneBasedLine - 1;
        int zeroBasedColumn = oneBasedColumn - 1;
        Location location = new Location(this, zeroBasedLine, zeroBasedColumn);
        Location startRange = null;
        for (Location loc : reverseSortedLocations) {
            if (loc.compareTo(location) < 0) {
                startRange = loc;
                break;
            }
        }
        if (startRange == null) {
            startRange = new Location(this, 0, 0);
        } else {
            startRange = startRange.next();
        }
        reverseSortedLocations.add(location);
        rangeEnds.add(location);
        Location endRange = location.next();
        Location start = startRange.step(-10);
        Location end = endRange.step(6);
        try {
            extractHandler.start();
            emitContent(start, startRange, extractHandler);
            extractHandler.startRange(oneBasedLine, oneBasedColumn);
            emitContent(startRange, endRange, extractHandler);
            extractHandler.endRange();
            emitContent(endRange, end, extractHandler);
        } finally {
            extractHandler.end();
        }
    }

    public void lineError(int oneBasedLine, SourceHandler extractHandler)
            throws SAXException {
        if (oneBasedLine <= lines.size()) {
            Line line = lines.get(oneBasedLine - 1);
            try {
                extractHandler.start();
                extractHandler.characters(line.getBuffer(), line.getOffset(),
                        line.getBufferLength());
            } finally {
                extractHandler.end();
            }
        }
    }

    private boolean isWithinKnownSource(Location location) {
        if (location.getLine() >= lines.size()) {
            return false;
        }
        Line line = lines.get(location.getLine());
        if (line.getBufferLength() > location.getColumn()) {
            return true;
        } else {
            return false;
        }
    }

    Line getLine(int line) {
        return lines.get(line);
    }

    int getNumberOfLines() {
        return lines.size();
    }

    void emitCharacter(Location location, SourceHandler handler)
            throws SAXException {
        Line line = getLine(location.getLine());
        handler.characters(line.getBuffer(), line.getOffset()
                + location.getColumn(), 1);
    }

    /**
     * Emits content between from a location (inclusive) until a location
     * (exclusive). There is no way to point to line breaks. They are always
     * emitted together with the last character on a line.
     * 
     * @param from
     * @param until
     * @param handler
     * @throws SAXException
     */
    void emitContent(Location from, Location until, SourceHandler handler)
            throws SAXException {
        if (from.compareTo(until) >= 0) {
            return;
        }
        int fromLine = from.getLine();
        int untilLine = until.getLine();
        Line line = getLine(fromLine);
        if (fromLine == untilLine) {
            handler.characters(line.getBuffer(), line.getOffset()
                    + from.getColumn(), until.getColumn() - from.getColumn());
        } else {
            // first line
            int length = line.getBufferLength() - from.getColumn();
            if (length > 0) {
                handler.characters(line.getBuffer(), line.getOffset()
                        + from.getColumn(), length);
            }
            handler.newLine();
            // lines in between
            int wholeLine = fromLine + 1;
            while (wholeLine < untilLine) {
                line = getLine(wholeLine);
                handler.characters(line.getBuffer(), line.getOffset(),
                        line.getBufferLength());
                handler.newLine();
            }
            // last line
            int untilCol = until.getColumn();
            if (untilCol > 0) {
                line = getLine(untilLine);
                handler.characters(line.getBuffer(), line.getOffset(), untilCol);
            }
        }
    }

    public void emitSource(SourceHandler handler) throws SAXException {
        List<Range> ranges = new LinkedList<Range>();
        Location[] locations = reverseSortedLocations.toArray(SOURCE_LOCATION_ARRAY_TYPE);
        int i = locations.length - 1;
        for (Location loc : rangeEnds) {
            while (i >= 0 && locations[i].compareTo(loc) >= 0) {
                i--;
            }
            Location start = i >= 0 ? locations[i].next() : new Location(this,
                    0, 0);
            Location end = loc.next();
            ranges.add(new Range(start, end, loc));
        }
        try {
            handler.start();
            Iterator<Range> rangeIter = ranges.iterator();
            Iterator<Location> exactIter = exactErrors.iterator();
            Location previousLocation = new Location(this, 0, 0);
            Location exact = null;
            Location rangeStart = null;
            Location rangeEnd = null;
            Location rangeLoc = null;
            if (exactIter.hasNext()) {
                exact = exactIter.next();
            }
            if (rangeIter.hasNext()) {
                Range r = rangeIter.next();
                rangeStart = r.getStart();
                rangeEnd = r.getEnd();
                rangeLoc = r.getLoc();
            }
            while (exact != null || rangeEnd != null) {
                if (exact != null && (rangeStart == null || exact.compareTo(rangeStart) < 0) && (rangeEnd == null || exact.compareTo(rangeEnd) < 0)) { // exact first?
                    emitContent(previousLocation, exact, handler);
                    handler.startCharHilite(exact.getLine() + 1, exact.getColumn() + 1);
                    emitCharacter(exact, handler);
                    handler.endCharHilite();
                    newLineIfAtEndOfLine(exact, handler);
                    previousLocation = exact.next();

                    if (exactIter.hasNext()) {
                        exact = exactIter.next();
                    } else {
                        exact = null;
                    }
                } else if (rangeStart != null) { // range start first?
                    emitContent(previousLocation, rangeStart, handler);
                    handler.startRange(rangeLoc.getLine() + 1, rangeLoc.getColumn() + 1);
                    previousLocation = rangeStart;
                    rangeStart = null;
                } else { // range end first?
                    emitContent(previousLocation, rangeEnd, handler);
                    handler.endRange();
                    previousLocation = rangeEnd;
                    rangeEnd = null;

                    if (rangeIter.hasNext()) {
                        Range r = rangeIter.next();
                        rangeStart = r.getStart();
                        rangeEnd = r.getEnd();
                        rangeLoc = r.getLoc();
                    } else {
                        rangeEnd = null;
                    }
                }
            }
            emitContent(previousLocation, new Location(this, lines.size(), 0),
                    handler);
        } finally {
            handler.end();
        }
    }

    /**
     * Returns the uri.
     * 
     * @return the uri
     */
    String getUri() {
        return uri;
    }

    /**
     * Returns the locationRecorder. The returned object is guaranteed 
     * to also implement <code>LexicalHandler</code>.
     * 
     * @return the locationRecorder
     */
    public ContentHandler getLocationRecorder() {
        return locationRecorder;
    }
    
    
}
