/*
 * Copyright 2000-2013  The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// March 5, 2015 - rwhogg - removed unnecessary return statement

package nu.validator.datatype;

import java.io.StringReader;
import java.io.IOException;

import org.relaxng.datatype.DatatypeException;

public class SvgPathData extends AbstractDatatype {

    /**
     * Package-private constructor
     */
    protected SvgPathData() {
        super();
    }

    private static final int MAX_CONTEXT_LENGTH = 20;

    private State appendToContext(State state) {
        if (state.current != -1) {
            if (state.context.length() == MAX_CONTEXT_LENGTH) {
                state.context.deleteCharAt(0);
            }
            state.context.append((char) state.current);
        }
        return state;
    }

    private class State {
        int current;

        StringBuffer context;

        StringReader reader;

        boolean skipped;

        State(int current, StringBuffer context, StringReader reader,
                boolean skipped) {
            this.current = current;
            this.context = context;
            this.reader = reader;
            this.skipped = skipped;
        }
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {

        /**
         * The current character.
         */
        int current;

        final StringReader reader = new StringReader(literal.toString());
        final StringBuffer context = new StringBuffer(MAX_CONTEXT_LENGTH);

        boolean skipped = false;

        State state;

        try {
            current = reader.read();
            state = appendToContext(
                    new State(current, context, reader, skipped));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loop: for (;;) {
            try {
                switch (state.current) {
                    case 0xD:
                    case 0xA:
                    case 0x20:
                    case 0x9:
                        state.current = state.reader.read();
                        state = appendToContext(state);
                        break;
                    case 'z':
                    case 'Z':
                        state.current = state.reader.read();
                        state = appendToContext(state);
                        break;
                    case 'm':
                        state = checkm(state);
                        break;
                    case 'M':
                        state = checkM(state);
                        break;
                    case 'l':
                        state = checkl(state);
                        break;
                    case 'L':
                        state = checkL(state);
                        break;
                    case 'h':
                        state = checkh(state);
                        break;
                    case 'H':
                        state = checkH(state);
                        break;
                    case 'v':
                        state = checkv(state);
                        break;
                    case 'V':
                        state = checkV(state);
                        break;
                    case 'c':
                        state = checkc(state);
                        break;
                    case 'C':
                        state = checkC(state);
                        break;
                    case 'q':
                        state = checkq(state);
                        break;
                    case 'Q':
                        state = checkQ(state);
                        break;
                    case 's':
                        state = checks(state);
                        break;
                    case 'S':
                        state = checkS(state);
                        break;
                    case 't':
                        state = checkt(state);
                        break;
                    case 'T':
                        state = checkT(state);
                        break;
                    case 'a':
                        state = checka(state);
                        break;
                    case 'A':
                        state = checkA(state);
                        break;
                    case -1:
                        break loop;
                    default:
                        throw newDatatypeException("Expected command but "
                                + "found \u201c" + (char) state.current
                                + "\u201d (context: \u201c"
                                + state.context.toString() + "\u201d).");
                }
            } catch (IOException e) {
                try {
                    state = skipSubPath(state);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
                throw new RuntimeException(e);
            }
        }

        try {
            state = skipSpaces(state);
            if (state.current != -1) {
                throw newDatatypeException("Found unexpected character "
                        + "\u201c" + (char) state.current + "\u201d.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks an 'm' command.
     */
    private State checkm(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);

        state = checkArg('m', "x coordinate", state);
        state = skipCommaSpaces(state);
        state = checkArg('m', "y coordinate", state);

        state = skipCommaSpaces2(state);
        boolean expectNumber = state.skipped;
        _checkl('m', expectNumber, state);
        return state;
    }

    /**
     * Checks an 'M' command.
     */
    private State checkM(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);

        state = checkArg('M', "x coordinate", state);
        state = skipCommaSpaces(state);
        state = checkArg('M', "y coordinate", state);

        state = skipCommaSpaces2(state);
        boolean expectNumber = state.skipped;
        _checkL('M', expectNumber, state);
        return state;
    }

    /**
     * Checks an 'l' command.
     */
    private State checkl(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        _checkl('l', true, state);
        return state;
    }

    private State _checkl(char command, boolean expectNumber, State state)
            throws IOException, DatatypeException {
        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber) {
                        reportUnexpected(
                                "coordinate pair for " + "\u201c" + command
                                        + "\u201d command",
                                state.current, state.context);
                        state = skipSubPath(state);
                    }
                    return state;
                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }
            state = checkArg(command, "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg(command, "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks an 'L' command.
     */
    private State checkL(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        _checkL('L', true, state);
        return state;
    }

    private State _checkL(char command, boolean expectNumber, State state)
            throws IOException, DatatypeException {
        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber) {
                        reportUnexpected(
                                "coordinate pair for " + "\u201c" + command
                                        + "\u201d command",
                                state.current, state.context);
                        state = skipSubPath(state);
                    }
                    return state;
                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }
            state = checkArg(command, "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg(command, "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks an 'h' command.
     */
    private State checkh(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('h', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }
            state = checkArg('h', "x coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks an 'H' command.
     */
    private State checkH(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('H', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }
            state = checkArg('H', "x coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a 'v' command.
     */
    private State checkv(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('v', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }
            state = checkArg('v', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a 'V' command.
     */
    private State checkV(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('V', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }
            state = checkArg('V', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a 'c' command.
     */
    private State checkc(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('c', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('c', "x1 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('c', "y1 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('c', "x2 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('c', "y2 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('c', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('c', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a 'C' command.
     */
    private State checkC(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('C', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('C', "x1 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('C', "y1 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('C', "x2 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('C', "y2 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('C', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('C', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a 'q' command.
     */
    private State checkq(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('q', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('q', "x1 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('q', "y1 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('q', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('q', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a 'Q' command.
     */
    private State checkQ(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('Q', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('Q', "x1 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('Q', "y1 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('Q', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('Q', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks an 's' command.
     */
    private State checks(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('s', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('s', "x2 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('s', "y2 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('s', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('s', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks an 'S' command.
     */
    private State checkS(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('S', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('S', "x2 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('S', "y2 coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('S', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('S', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a 't' command.
     */
    private State checkt(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('t', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('t', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('t', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a 'T' command.
     */
    private State checkT(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('T', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('T', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('T', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks an 'a' command.
     */
    private State checka(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('a', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('a', "rx radius", state);
            state = skipCommaSpaces(state);
            state = checkArg('a', "ry radius", state);
            state = skipCommaSpaces(state);
            state = checkArg('a', "x-axis-rotation", state);
            state = skipCommaSpaces(state);

            switch (state.current) {
                default:
                    reportUnexpected("\u201c0\u201d or \u201c1\u201d "
                            + "for large-arc-flag for \u201ca\u201d"
                            + " command", state.current, state.context);
                    state = skipSubPath(state);
                    return state;
                case '0':
                case '1':
                    break;
            }

            state.current = state.reader.read();
            state = appendToContext(state);
            state = skipCommaSpaces(state);

            switch (state.current) {
                default:
                    reportUnexpected("\u201c0\u201d or \u201c1\u201d for"
                            + " sweep-flag for \u201ca\u201d" + " command",
                            state.current, state.context);
                    state = skipSubPath(state);
                    return state;
                case '0':
                case '1':
                    break;
            }

            state.current = state.reader.read();
            state = appendToContext(state);
            state = skipCommaSpaces(state);

            state = checkArg('a', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('a', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks an 'A' command.
     */
    private State checkA(State state) throws DatatypeException, IOException {
        if (state.context.length() == 0) {
            state = appendToContext(state);
        }
        state.current = state.reader.read();
        state = appendToContext(state);
        state = skipSpaces(state);
        boolean expectNumber = true;

        for (;;) {
            switch (state.current) {
                default:
                    if (expectNumber)
                        reportNonNumber('A', state.current, state.context);
                    return state;

                case '+':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
            }

            state = checkArg('A', "rx radius", state);
            state = skipCommaSpaces(state);
            state = checkArg('A', "ry radius", state);
            state = skipCommaSpaces(state);
            state = checkArg('A', "x-axis-rotation", state);
            state = skipCommaSpaces(state);

            switch (state.current) {
                default:
                    reportUnexpected("\u201c0\u201d or \u201c1\u201d for"
                            + " large-arc-flag for \u201cA\u201d" + " command",
                            state.current, state.context);
                    state = skipSubPath(state);
                    return state;
                case '0':
                case '1':
                    break;
            }

            state.current = state.reader.read();
            state = appendToContext(state);
            state = skipCommaSpaces(state);

            switch (state.current) {
                default:
                    reportUnexpected("\u201c0\u201d or \u201c1\u201d for"
                            + " sweep-flag for \u201cA\u201d" + " command",
                            state.current, state.context);
                    state = skipSubPath(state);
                    return state;
                case '0':
                case '1':
                    break;
            }

            state.current = state.reader.read();
            state = appendToContext(state);
            state = skipCommaSpaces(state);

            state = checkArg('A', "x coordinate", state);
            state = skipCommaSpaces(state);
            state = checkArg('A', "y coordinate", state);

            state = skipCommaSpaces2(state);
            expectNumber = state.skipped;
        }
    }

    /**
     * Checks a command argument.
     */
    private State checkArg(char command, String arg, State state)
            throws DatatypeException, IOException {
        int mant = 0;
        int mantDig = 0;
        boolean mantPos = true;
        boolean mantRead = false;

        int exp = 0;
        int expDig = 0;
        int expAdj = 0;
        boolean expPos = true;

        switch (state.current) {
            case '-':
                mantPos = false;
            case '+':
                state.current = state.reader.read();
                state = appendToContext(state);
        }

        m1: switch (state.current) {
            default:
                reportUnexpected(
                        arg + " for \u201c" + command + "\u201d command",
                        state.current, state.context);
                state = skipSubPath(state);
                return state;

            case '.':
                break;

            case '0':
                mantRead = true;
                l: for (;;) {
                    state.current = state.reader.read();
                    state = appendToContext(state);
                    switch (state.current) {
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            break l;
                        case '.':
                        case 'e':
                        case 'E':
                            break m1;
                        default:
                            return state;
                        case '0':
                    }
                }

            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                mantRead = true;
                l: for (;;) {
                    if (mantDig < 9) {
                        mantDig++;
                        mant = mant * 10 + (state.current - '0');
                    } else {
                        expAdj++;
                    }
                    state.current = state.reader.read();
                    state = appendToContext(state);
                    switch (state.current) {
                        default:
                            break l;
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                    }
                }
        }

        if (state.current == '.') {
            state.current = state.reader.read();
            state = appendToContext(state);
            m2: switch (state.current) {
                default:
                case 'e':
                case 'E':
                    if (!mantRead) {
                        reportNonNumber(command, state.current, state.context);
                        return state;
                    }
                    break;

                case '0':
                    if (mantDig == 0) {
                        l: for (;;) {
                            state.current = state.reader.read();
                            state = appendToContext(state);
                            expAdj--;
                            switch (state.current) {
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                    break l;
                                default:
                                    if (!mantRead) {
                                        return state;
                                    }
                                    break m2;
                                case '0':
                            }
                        }
                    }
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    l: for (;;) {
                        if (mantDig < 9) {
                            mantDig++;
                            mant = mant * 10 + (state.current - '0');
                            expAdj--;
                        }
                        state.current = state.reader.read();
                        state = appendToContext(state);
                        switch (state.current) {
                            default:
                                break l;
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                        }
                    }
            }
        }

        switch (state.current) {
            case 'e':
            case 'E':
                state.current = state.reader.read();
                state = appendToContext(state);
                switch (state.current) {
                    default:
                        reportNonNumber(command, state.current, state.context);
                        return state;
                    case '-':
                        expPos = false;
                    case '+':
                        state.current = state.reader.read();
                        state = appendToContext(state);
                        switch (state.current) {
                            default:
                                reportNonNumber(command, state.current,
                                        state.context);
                                return state;
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                        }
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                }

                en: switch (state.current) {
                    case '0':
                        l: for (;;) {
                            state.current = state.reader.read();
                            state = appendToContext(state);
                            switch (state.current) {
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                                    break l;
                                default:
                                    break en;
                                case '0':
                            }
                        }

                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        l: for (;;) {
                            if (expDig < 3) {
                                expDig++;
                                exp = exp * 10 + (state.current - '0');
                            }
                            state.current = state.reader.read();
                            state = appendToContext(state);
                            switch (state.current) {
                                default:
                                    break l;
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                            }
                        }
                }
            default:
        }

        if (!expPos) {
            exp = -exp;
        }
        exp += expAdj;
        if (!mantPos) {
            mant = -mant;
        }
        return state;
    }

    /**
     * Skips a sub-path.
     */
    private State skipSubPath(State state) throws IOException {
        for (;;) {
            switch (state.current) {
                case -1:
                case 'm':
                case 'M':
                    return state;
                default:
                    break;
            }
            state.current = state.reader.read();
            state = appendToContext(state);
        }
    }

    /**
     * Skips the whitespaces in the current reader.
     */
    private State skipSpaces(State state) throws IOException {
        for (;;) {
            switch (state.current) {
                default:
                    return state;
                case 0x20:
                case 0x09:
                case 0x0D:
                case 0x0A:
            }
            state.current = state.reader.read();
            state = appendToContext(state);
        }
    }

    /**
     * Skips the whitespaces and an optional comma.
     */
    private State skipCommaSpaces(State state) throws IOException {
        wsp1: for (;;) {
            switch (state.current) {
                default:
                    break wsp1;
                case 0x20:
                case 0x9:
                case 0xD:
                case 0xA:
            }
            state.current = state.reader.read();
            state = appendToContext(state);
        }
        if (state.current == ',') {
            wsp2: for (;;) {
                switch (state.current = state.reader.read()) {
                    default:
                        state = appendToContext(state);
                        break wsp2;
                    case 0x20:
                    case 0x9:
                    case 0xA:
                    case 0xD:
                        state = appendToContext(state);
                }
            }
        }
        return state;
    }

    /**
     * Skips the whitespaces and an optional comma.
     *
     */
    private State skipCommaSpaces2(State state) throws IOException {
        wsp1: for (;;) {
            switch (state.current) {
                default:
                    break wsp1;
                case 0x20:
                case 0x9:
                case 0xD:
                case 0xA:
                    break;
            }
            state.current = state.reader.read();
            state = appendToContext(state);
        }

        if (state.current != ',') {
            state.skipped = false; // no comma.
            return state;
        }

        wsp2: for (;;) {
            switch (state.current = state.reader.read()) {
                default:
                    state = appendToContext(state);
                    break wsp2;
                case 0x20:
                case 0x9:
                case 0xD:
                case 0xA:
                    state = appendToContext(state);
                    break;
            }
        }
        state.skipped = true; // had comma
        return state;
    }

    private void reportUnexpected(String expected, int ch, StringBuffer context)
            throws DatatypeException {
        if (ch != -1) {
            throw newDatatypeException("Expected " + expected
                    + " but found \u201c" + (char) ch + "\u201d instead "
                    + "(context: \u201c" + context.toString() + "\u201d).");
        } else {
            throw newDatatypeException("Expected " + expected
                    + " but value ended " + "(context: \u201c"
                    + context.toString() + "\u201d).");
        }
    }

    private void reportNonNumber(char command, int ch, StringBuffer context)
            throws DatatypeException {
        if (ch != -1) {
            throw newDatatypeException("Expected number for \u201c" + command
                    + "\u201d command but found " + "\u201c" + (char) ch
                    + "\u201d instead " + "(context: \u201c"
                    + context.toString() + "\u201d).");
        } else {
            throw newDatatypeException("Expected number for \u201c" + command
                    + "\u201d command but value ended " + "(context: \u201c"
                    + context.toString() + "\u201d).");
        }
    }

    @Override
    public String getName() {
        return "SVG path data";
    }

}
