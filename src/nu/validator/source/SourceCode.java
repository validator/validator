/*
 * Copyright (c) 2007-2018 Mozilla Foundation
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import nu.validator.collections.HeadBiasedSortedSet;
import nu.validator.collections.TailBiasedSortedSet;
import nu.validator.htmlparser.common.CharacterHandler;
import nu.validator.xml.TypedInputSource;

import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public final class SourceCode implements CharacterHandler {
    private static final Logger log4j = Logger.getLogger(SourceCode.class);

    private static Location[] SOURCE_LOCATION_ARRAY_TYPE = new Location[0];

    private String uri;
    
    private String type;
    
    private String encoding;

    private int expectedLength;

    private final SortedSet<Location> reverseSortedLocations = new HeadBiasedSortedSet<>(Collections.reverseOrder());

    private final SortedSet<Location> exactErrors = new TailBiasedSortedSet<>();

    private final SortedSet<Location> rangeLasts = new TailBiasedSortedSet<>();
    
    private final SortedSet<Integer> oneBasedLineErrors = new TailBiasedSortedSet<>();

//    private final SortedSet<Location> reverseSortedLocations = new TreeSet<>(Collections.reverseOrder());
//
//    private final SortedSet<Location> exactErrors = new TreeSet<>();
//
//    private final SortedSet<Location> rangeLasts = new TreeSet<>();
//    
//    private final SortedSet<Integer> oneBasedLineErrors = new TreeSet<>();

    
    private final List<Line> lines = new ArrayList<>();

    private Line currentLine = null;

    private boolean prevWasCr = false;

    private final LocationRecorder locationRecorder;

    private boolean isCss = false;

    public SourceCode() {
        this.locationRecorder = new LocationRecorder(this);
    }

    public void setIsCss() {
        this.isCss = true;
    }

    public boolean getIsCss() {
        return this.isCss;
    }

    public void initialize(InputSource inputSource) {
        this.uri = inputSource.getSystemId();
        this.encoding = inputSource.getEncoding();
        if (inputSource instanceof TypedInputSource) {
            TypedInputSource typedInputSource = (TypedInputSource) inputSource;
            int length = typedInputSource.getLength();
            if (length == -1) {
                expectedLength = 2048;
            } else {
                expectedLength = length;
            }
            this.type = typedInputSource.getType();
        } else {
            expectedLength = 2048;
            this.type = null;
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     * @see java.lang.StringBuffer#append(char[], int, int)
     */
    @Override
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

    @Override
    public void end() throws SAXException {
        if (currentLine != null && currentLine.getBufferLength() == 0) {
            // Theoretical impurity with line separators vs. terminators
            lines.remove(lines.size() - 1);
            currentLine = null;
        }
    }

    @Override
    public void start() throws SAXException {
        reverseSortedLocations.clear();
        lines.clear();
        currentLine = null;
        newLine();
        prevWasCr = false;
    }

    public void addLocatorLocation(int oneBasedLine, int oneBasedColumn) {
        log4j.debug(oneBasedLine + ", " + oneBasedColumn);
        reverseSortedLocations.add(new Location(this, oneBasedLine - 1,
                oneBasedColumn - 1));
    }

    public void exactError(Location location, SourceHandler extractHandler)
            throws SAXException {
        exactErrors.add(location);
        Location start = location.step(-15);
        Location end = location.step(15);
        extractHandler.startSource(type, encoding);
        emitContent(start, location, extractHandler);
        extractHandler.startCharHilite(location.getLine() + 1,
                location.getColumn() + 1);
        emitCharacter(location, extractHandler);
        extractHandler.endCharHilite();
        location = location.next();
        emitContent(location, end, extractHandler);
        extractHandler.endSource();
    }
    
    public void rememberExactError(Location location) {
        if (location.getColumn() < 0 || location.getLine() < 0) {
            return;
        }
        exactErrors.add(location);
    }

    public void registerRandeEnd(Locator locator) {
        String systemId = locator.getSystemId();
        if (uri == systemId || (uri != null && uri.equals(systemId))) {
            rangeLasts.add(newLocatorLocation(locator.getLineNumber(), locator.getColumnNumber()));
        }
    }
    
    public void rangeEndError(Location rangeStart, Location rangeLast,
            SourceHandler extractHandler) throws SAXException {
        reverseSortedLocations.add(rangeLast);
        rangeLasts.add(rangeLast);
        Location endRange = rangeLast.next();
        Location start = rangeStart.step(-10);
        if (this.isCss) {
            start = rangeStart.step(-30);
        }
        Location end = endRange.step(6);
        extractHandler.startSource(type, encoding);
        emitContent(start, rangeStart, extractHandler);
        extractHandler.startRange(rangeLast.getLine() + 1,
                rangeLast.getColumn() + 1);
        emitContent(rangeStart, endRange, extractHandler);
        extractHandler.endRange();
        emitContent(endRange, end, extractHandler);
        extractHandler.endSource();
    }

    /**
     * @param rangeLast
     * @return
     */
    public Location rangeStartForRangeLast(Location rangeLast) {
        for (Location loc : reverseSortedLocations) {
            if (loc.compareTo(rangeLast) < 0) {
                return loc.next();
            }
        }
        return new Location(this, 0, 0);
    }

    public void lineError(int oneBasedLine, SourceHandler extractHandler)
            throws SAXException {
        oneBasedLineErrors.add(oneBasedLine);
        Line line = lines.get(oneBasedLine - 1);
        extractHandler.startSource(type, encoding);
        extractHandler.characters(line.getBuffer(), line.getOffset(),
                line.getBufferLength());
        extractHandler.endSource();
    }

    public boolean isWithinKnownSource(Location location) {
        if (location.getLine() >= lines.size()) {
            return false;
        }
        Line line = lines.get(location.getLine());
        return line.getBufferLength() >= location.getColumn();
    }

    public boolean isWithinKnownSource(int oneBasedLine) {
        return !(oneBasedLine > lines.size());
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
        int col = location.getColumn();
        if (col == line.getBufferLength()) {
            handler.newLine();
        } else {
            handler.characters(line.getBuffer(), line.getOffset() + col, 1);
        }
    }

    /**
     * Emits content between from a location (inclusive) until a location
     * (exclusive).
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
            try {
                handler.characters(line.getBuffer(),
                        line.getOffset() + from.getColumn(),
                        until.getColumn() - from.getColumn());
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        } else {
            // first line
            int length = line.getBufferLength() - from.getColumn();
            if (length > 0) {
                if (!((fromLine == 0 || fromLine == lines.size() - 1)
                        && this.isCss)) {
                    try {
                        handler.characters(line.getBuffer(),
                                line.getOffset() + from.getColumn(), length);
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                }
            }
            if (fromLine + 1 != lines.size()) {
                if (!(fromLine == 0 && this.isCss)) {
                    handler.newLine();
                }
            }
            // lines in between
            int wholeLine = fromLine + 1;
            while (wholeLine < untilLine) {
                line = getLine(wholeLine);
                try {
                    handler.characters(line.getBuffer(), line.getOffset(),
                            line.getBufferLength());
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                wholeLine++;
                if (wholeLine != lines.size()) {
                    handler.newLine();
                }
            }
            // last line
            int untilCol = until.getColumn();
            if (untilCol > 0) {
                line = getLine(untilLine);
                if (!(untilLine == lines.size() - 1 && this.isCss)) {
                    try {
                        handler.characters(line.getBuffer(), line.getOffset(),
                                untilCol);
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                }
            }
        }
    }

    public void emitSource(SourceHandler handler) throws SAXException {
        List<Range> ranges = new LinkedList<>();
        Location[] locations = reverseSortedLocations.toArray(SOURCE_LOCATION_ARRAY_TYPE);
        int i = locations.length - 1;
        for (Location loc : rangeLasts) {
            while (i >= 0 && locations[i].compareTo(loc) < 0) {
                i--;
            }
            Location start;
            if (i == locations.length - 1) {
                start = new Location(this, 0, 0);
            } else {
                start = locations[i + 1].next();                
            }
            Location end = loc.next();
            ranges.add(new Range(start, end, loc));
        }
        try {
            handler.startSource(type, encoding);
            handler.setLineErrors(oneBasedLineErrors);
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
                if (exact != null
                        && (rangeStart == null || exact.compareTo(rangeStart) < 0)
                        && (rangeEnd == null || exact.compareTo(rangeEnd) < 0)) { // exact
                                                                                    // first?
                    emitContent(previousLocation, exact, handler);
                    handler.startCharHilite(exact.getLine() + 1,
                            exact.getColumn() + 1);
                    emitCharacter(exact, handler);
                    handler.endCharHilite();
                    previousLocation = exact.next();

                    if (exactIter.hasNext()) {
                        exact = exactIter.next();
                    } else {
                        exact = null;
                    }
                } else if (rangeStart != null) { // range start first?
                    emitContent(previousLocation, rangeStart, handler);
                    handler.startRange(rangeLoc.getLine() + 1,
                            rangeLoc.getColumn() + 1);
                    previousLocation = rangeStart;
                    rangeStart = null;
                } else { // range end first?
                    emitContent(previousLocation, rangeEnd, handler);
                    handler.endRange();
                    previousLocation = rangeEnd;

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
            if (this.isCss) {
                emitContent(previousLocation,
                        new Location(this, lines.size() - 1, 0), handler);
            } else {
                emitContent(previousLocation,
                        new Location(this, lines.size(), 0), handler);
            }
        } finally {
            handler.endSource();
        }
    }

    /**
     * Returns the uri.
     * 
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns the locationRecorder. The returned object is guaranteed to also
     * implement <code>LexicalHandler</code>.
     * 
     * @return the locationRecorder
     */
    public ContentHandler getLocationRecorder() {
        return locationRecorder;
    }

    public Location newLocatorLocation(int oneBasedLine, int oneBasedColumn) {
        return new Location(this, oneBasedLine - 1, oneBasedColumn - 1);
    }
}
