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

package org.whattf.datatype;

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

    private static StringReader reader;

    private static StringBuilder context;

    private static final int MAX_CONTEXT_LENGTH = 20;

    private void appendToContext(int i) {
        if (i != -1) {
            if (context.length() == MAX_CONTEXT_LENGTH) {
                context.deleteCharAt(0);
            }
            context.append((char) i);
        }
    }

    /**
     * The current character.
     */
    private int current;

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {

        reader = new StringReader(literal.toString());
        context = new StringBuilder(MAX_CONTEXT_LENGTH);

        try {
            current = reader.read();
            appendToContext(current);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loop: for (;;) {
            try {
                switch (current) {
                    case 0xD:
                    case 0xA:
                    case 0x20:
                    case 0x9:
                        current = reader.read();
                        appendToContext(current);
                        break;
                    case 'z':
                    case 'Z':
                        current = reader.read();
                        appendToContext(current);
                        break;
                    case 'm':
                        checkm();
                        break;
                    case 'M':
                        checkM();
                        break;
                    case 'l':
                        checkl();
                        break;
                    case 'L':
                        checkL();
                        break;
                    case 'h':
                        checkh();
                        break;
                    case 'H':
                        checkH();
                        break;
                    case 'v':
                        checkv();
                        break;
                    case 'V':
                        checkV();
                        break;
                    case 'c':
                        checkc();
                        break;
                    case 'C':
                        checkC();
                        break;
                    case 'q':
                        checkq();
                        break;
                    case 'Q':
                        checkQ();
                        break;
                    case 's':
                        checks();
                        break;
                    case 'S':
                        checkS();
                        break;
                    case 't':
                        checkt();
                        break;
                    case 'T':
                        checkT();
                        break;
                    case 'a':
                        checka();
                        break;
                    case 'A':
                        checkA();
                        break;
                    case -1:
                        break loop;
                    default:
                        throw newDatatypeException("Expected command but "
                                + "found \u201c" + (char) current
                                + "\u201d (context: \u201c"
                                + context.toString() + "\u201d).");
                }
            } catch (IOException e) {
                try {
                    skipSubPath();
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
                throw new RuntimeException(e);
            }
        }

        try {
            skipSpaces();
            if (current != -1) {
                throw newDatatypeException("Found unexpected character "
                        + "\u201c" + (char) current + "\u201d.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks an 'm' command.
     */
    private void checkm() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();

        checkArg('m', "x coordinate");
        skipCommaSpaces();
        checkArg('m', "y coordinate");

        boolean expectNumber = skipCommaSpaces2();
        _checkl('m', expectNumber);
    }

    /**
     * Checks an 'M' command.
     */
    private void checkM() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();

        checkArg('M', "x coordinate");
        skipCommaSpaces();
        checkArg('M', "y coordinate");

        boolean expectNumber = skipCommaSpaces2();
        _checkL('M', expectNumber);
    }

    /**
     * Checks an 'l' command.
     */
    private void checkl() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        _checkl('l', true);
    }

    private void _checkl(char command, boolean expectNumber)
            throws IOException, DatatypeException {
        for (;;) {
            switch (current) {
                default:
                    if (expectNumber) {
                        reportUnexpected("coordinate pair for " + "\u201c"
                                + command + "\u201d command", current);
                        skipSubPath();
                    }
                    return;
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
            checkArg(command, "x coordinate");
            skipCommaSpaces();
            checkArg(command, "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks an 'L' command.
     */
    private void checkL() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        _checkL('L', true);
    }

    private void _checkL(char command, boolean expectNumber)
            throws IOException, DatatypeException {
        for (;;) {
            switch (current) {
                default:
                    if (expectNumber) {
                        reportUnexpected("coordinate pair for " + "\u201c"
                                + command + "\u201d command", current);
                        skipSubPath();
                    }
                    return;
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
            checkArg(command, "x coordinate");
            skipCommaSpaces();
            checkArg(command, "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks an 'h' command.
     */
    private void checkh() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('h', current);
                    return;

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
            checkArg('h', "x coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks an 'H' command.
     */
    private void checkH() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('H', current);
                    return;

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
            checkArg('H', "x coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a 'v' command.
     */
    private void checkv() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('v', current);
                    return;

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
            checkArg('v', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a 'V' command.
     */
    private void checkV() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('V', current);
                    return;

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
            checkArg('V', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a 'c' command.
     */
    private void checkc() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('c', current);
                    return;

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

            checkArg('c', "x1 coordinate");
            skipCommaSpaces();
            checkArg('c', "y1 coordinate");
            skipCommaSpaces();
            checkArg('c', "x2 coordinate");
            skipCommaSpaces();
            checkArg('c', "y2 coordinate");
            skipCommaSpaces();
            checkArg('c', "x coordinate");
            skipCommaSpaces();
            checkArg('c', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a 'C' command.
     */
    private void checkC() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('C', current);
                    return;

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

            checkArg('C', "x1 coordinate");
            skipCommaSpaces();
            checkArg('C', "y1 coordinate");
            skipCommaSpaces();
            checkArg('C', "x2 coordinate");
            skipCommaSpaces();
            checkArg('C', "y2 coordinate");
            skipCommaSpaces();
            checkArg('C', "x coordinate");
            skipCommaSpaces();
            checkArg('C', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a 'q' command.
     */
    private void checkq() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('q', current);
                    return;

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

            checkArg('q', "x1 coordinate");
            skipCommaSpaces();
            checkArg('q', "y1 coordinate");
            skipCommaSpaces();
            checkArg('q', "x coordinate");
            skipCommaSpaces();
            checkArg('q', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a 'Q' command.
     */
    private void checkQ() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('Q', current);
                    return;

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

            checkArg('Q', "x1 coordinate");
            skipCommaSpaces();
            checkArg('Q', "y1 coordinate");
            skipCommaSpaces();
            checkArg('Q', "x coordinate");
            skipCommaSpaces();
            checkArg('Q', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks an 's' command.
     */
    private void checks() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('s', current);
                    return;

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

            checkArg('s', "x2 coordinate");
            skipCommaSpaces();
            checkArg('s', "y2 coordinate");
            skipCommaSpaces();
            checkArg('s', "x coordinate");
            skipCommaSpaces();
            checkArg('s', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks an 'S' command.
     */
    private void checkS() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('S', current);
                    return;

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

            checkArg('S', "x2 coordinate");
            skipCommaSpaces();
            checkArg('S', "y2 coordinate");
            skipCommaSpaces();
            checkArg('S', "x coordinate");
            skipCommaSpaces();
            checkArg('S', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a 't' command.
     */
    private void checkt() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('t', current);
                    return;

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

            checkArg('t', "x coordinate");
            skipCommaSpaces();
            checkArg('t', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a 'T' command.
     */
    private void checkT() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('T', current);
                    return;

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

            checkArg('T', "x coordinate");
            skipCommaSpaces();
            checkArg('T', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks an 'a' command.
     */
    private void checka() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('a', current);
                    return;

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

            checkArg('a', "rx radius");
            skipCommaSpaces();
            checkArg('a', "ry radius");
            skipCommaSpaces();
            checkArg('a', "x-axis-rotation");
            skipCommaSpaces();

            switch (current) {
                default:
                    reportUnexpected("\u201c0\u201d or \u201c1\u201d "
                            + "for large-arc-flag for \u201ca\u201d command",
                            current);
                    skipSubPath();
                    return;
                case '0':
                case '1':
                    break;
            }

            current = reader.read();
            appendToContext(current);
            skipCommaSpaces();

            switch (current) {
                default:
                    reportUnexpected("\u201c0\u201d or \u201c1\u201d "
                            + "for sweep-flag for \u201ca\u201d command",
                            current);
                    skipSubPath();
                    return;
                case '0':
                case '1':
                    break;
            }

            current = reader.read();
            appendToContext(current);
            skipCommaSpaces();

            checkArg('a', "x coordinate");
            skipCommaSpaces();
            checkArg('a', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks an 'A' command.
     */
    private void checkA() throws DatatypeException, IOException {
        if (context.length() == 0) {
            appendToContext(current);
        }
        current = reader.read();
        appendToContext(current);
        skipSpaces();
        boolean expectNumber = true;

        for (;;) {
            switch (current) {
                default:
                    if (expectNumber)
                        reportNonNumber('A', current);
                    return;

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

            checkArg('A', "rx radius");
            skipCommaSpaces();
            checkArg('A', "ry radius");
            skipCommaSpaces();
            checkArg('A', "x-axis-rotation");
            skipCommaSpaces();

            switch (current) {
                default:
                    reportUnexpected("\u201c0\u201d or \u201c1\u201d "
                            + "for large-arc-flag for \u201cA\u201d command",
                            current);
                    skipSubPath();
                    return;
                case '0':
                case '1':
                    break;
            }

            current = reader.read();
            appendToContext(current);
            skipCommaSpaces();

            switch (current) {
                default:
                    reportUnexpected("\u201c0\u201d or \u201c1\u201d "
                            + "for sweep-flag for \u201cA\u201d command",
                            current);
                    skipSubPath();
                    return;
                case '0':
                case '1':
                    break;
            }

            current = reader.read();
            appendToContext(current);
            skipCommaSpaces();

            checkArg('A', "x coordinate");
            skipCommaSpaces();
            checkArg('A', "y coordinate");

            expectNumber = skipCommaSpaces2();
        }
    }

    /**
     * Checks a command argument.
     */
    private void checkArg(char command, String arg) throws DatatypeException,
            IOException {
        int mant = 0;
        int mantDig = 0;
        boolean mantPos = true;
        boolean mantRead = false;

        int exp = 0;
        int expDig = 0;
        int expAdj = 0;
        boolean expPos = true;

        switch (current) {
            case '-':
                mantPos = false;
            case '+':
                current = reader.read();
                appendToContext(current);
        }

        m1: switch (current) {
            default:
                reportUnexpected(arg + " for \u201c" + command
                        + "\u201d command", current);
                skipSubPath();
                return;

            case '.':
                break;

            case '0':
                mantRead = true;
                l: for (;;) {
                    current = reader.read();
                    appendToContext(current);
                    switch (current) {
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
                            return;
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
                        mant = mant * 10 + (current - '0');
                    } else {
                        expAdj++;
                    }
                    current = reader.read();
                    appendToContext(current);
                    switch (current) {
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

        if (current == '.') {
            current = reader.read();
            appendToContext(current);
            m2: switch (current) {
                default:
                case 'e':
                case 'E':
                    if (!mantRead) {
                        reportNonNumber(command, current);
                        return;
                    }
                    break;

                case '0':
                    if (mantDig == 0) {
                        l: for (;;) {
                            current = reader.read();
                            appendToContext(current);
                            expAdj--;
                            switch (current) {
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
                                        return;
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
                            mant = mant * 10 + (current - '0');
                            expAdj--;
                        }
                        current = reader.read();
                        appendToContext(current);
                        switch (current) {
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

        switch (current) {
            case 'e':
            case 'E':
                current = reader.read();
                appendToContext(current);
                switch (current) {
                    default:
                        reportNonNumber(command, current);
                        return;
                    case '-':
                        expPos = false;
                    case '+':
                        current = reader.read();
                        appendToContext(current);
                        switch (current) {
                            default:
                                reportNonNumber(command, current);
                                return;
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

                en: switch (current) {
                    case '0':
                        l: for (;;) {
                            current = reader.read();
                            appendToContext(current);
                            switch (current) {
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
                                exp = exp * 10 + (current - '0');
                            }
                            current = reader.read();
                            appendToContext(current);
                            switch (current) {
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

        return;
    }

    /**
     * Skips a sub-path.
     */
    private void skipSubPath() throws IOException {
        for (;;) {
            switch (current) {
                case -1:
                case 'm':
                case 'M':
                    return;
                default:
                    break;
            }
            current = reader.read();
            appendToContext(current);
        }
    }

    /**
     * Skips the whitespaces in the current reader.
     */
    private void skipSpaces() throws IOException {
        for (;;) {
            switch (current) {
                default:
                    return;
                case 0x20:
                case 0x09:
                case 0x0D:
                case 0x0A:
            }
            current = reader.read();
            appendToContext(current);
        }
    }

    /**
     * Skips the whitespaces and an optional comma.
     */
    private void skipCommaSpaces() throws IOException {
        wsp1: for (;;) {
            switch (current) {
                default:
                    break wsp1;
                case 0x20:
                case 0x9:
                case 0xD:
                case 0xA:
            }
            current = reader.read();
            appendToContext(current);
        }
        if (current == ',') {
            wsp2: for (;;) {
                switch (current = reader.read()) {
                    default:
                        appendToContext(current);
                        break wsp2;
                    case 0x20:
                    case 0x9:
                    case 0xA:
                    case 0xD:
                        appendToContext(current);
                }
            }
        }
    }

    /**
     * Skips the whitespaces and an optional comma.
     * 
     * @returns true if comma was skipped.
     */
    private boolean skipCommaSpaces2() throws IOException {
        wsp1: for (;;) {
            switch (current) {
                default:
                    break wsp1;
                case 0x20:
                case 0x9:
                case 0xD:
                case 0xA:
                    break;
            }
            current = reader.read();
            appendToContext(current);
        }

        if (current != ',')
            return false; // no comma.

        wsp2: for (;;) {
            switch (current = reader.read()) {
                default:
                    appendToContext(current);
                    break wsp2;
                case 0x20:
                case 0x9:
                case 0xD:
                case 0xA:
                    appendToContext(current);
                    break;
            }
        }
        return true; // had comma
    }

    private void reportUnexpected(String expected, int ch)
            throws DatatypeException, IOException {
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

    private void reportNonNumber(char command, int ch)
            throws DatatypeException, IOException {
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

    @Override public String getName() {
        return "SVG path data";
    }

}
