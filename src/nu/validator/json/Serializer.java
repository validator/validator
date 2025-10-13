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

package nu.validator.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

public class Serializer implements JsonHandler {

    private enum State {
        INITIAL, DOCUMENT, ARRAY, OBJECT, VALUE, STRING
    }

    private final List<State> stack = new ArrayList<>();

    private boolean hadCallback = false;

    private boolean first = false;

    private final Writer writer;

    private static Writer newOutputStreamWriter(OutputStream out) {
        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
        enc.onMalformedInput(CodingErrorAction.REPLACE);
        enc.onUnmappableCharacter(CodingErrorAction.REPLACE);
        return new OutputStreamWriter(out, enc);
    }
    
    public Serializer(OutputStream out) {
        this.writer = newOutputStreamWriter(out);
        push(State.INITIAL);
    }

    private void push(State state) {
        stack.add(state);
    }

    private void pop() {
        stack.remove(stack.size() - 1);
    }

    private State peek() {
        int size = stack.size();
        if (size == 0) {
            return null;
        } else {
            return stack.get(size - 1);
        }
    }

    @Override
    public void bool(boolean bool) throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    writer.write(Boolean.toString(bool));
                    if (state == State.VALUE) {
                        pop();
                    }
                    first = false;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    private void charactersImpl(char[] ch, int start, int length)
            throws IOException {
        int s = start;
        int end = start + length;
        for (int i = start; i < end; i++) {
            char c = ch[i];
            if (c <= '\u001F' || c == '\"' || c == '\\') {
                if (s < i) {
                    writer.write(ch, s, i - s);
                }
                s = i + 1;
                writer.write('\\');
                switch (c) {
                    case '\"':
                        writer.write('\"');
                        break;
                    case '\\':
                        writer.write('\\');
                        break;
                    case '\u0008':
                        writer.write('b');
                        break;
                    case '\u000C':
                        writer.write('f');
                        break;
                    case '\n':
                        writer.write('n');
                        break;
                    case '\r':
                        writer.write('r');
                        break;
                    case '\t':
                        writer.write('t');
                        break;
                    default:
                        String hex = Integer.toHexString(c);
                        if (hex.length() == 1) {
                            writer.write("u000");
                            writer.write(hex);
                        } else {
                            writer.write("u00");
                            writer.write(hex);                            
                        }
                        break;
                }
            }
        }
        if (s < end) {
            writer.write(ch, s, end - s);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case STRING:
                    charactersImpl(ch, start, length);
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void endArray() throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    writer.write(']');
                    pop();
                    first = false;
                    if (peek() == State.VALUE) {
                        pop();
                    }
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case DOCUMENT:
                    if (hadCallback) {
                        writer.write(')');
                    }
                    writer.write('\n');
                    writer.flush();
                    writer.close();
                    pop();
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void endObject() throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case OBJECT:
                    writer.write('}');
                    writer.flush();
                    pop();
                    first = false;
                    if (peek() == State.VALUE) {
                        pop();
                    }
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void endString() throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case STRING:
                    writer.write('\"');
                    pop();
                    first = false;
                    if (peek() == State.VALUE) {
                        pop();
                    }
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void key(String key) throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case OBJECT:
                    if (!first) {
                        writer.write(',');
                    }
                    writer.write('\"');
                    charactersImpl(key.toCharArray(), 0, key.length());
                    writer.write('\"');
                    writer.write(':');
                    push(State.VALUE);
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void number(int number) throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    writer.write(Integer.toString(number));
                    if (state == State.VALUE) {
                        pop();
                    }
                    first = false;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void number(long number) throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    writer.write(Long.toString(number));
                    if (state == State.VALUE) {
                        pop();
                    }
                    first = false;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void number(float number) throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    writer.write(Float.toString(number));
                    if (state == State.VALUE) {
                        pop();
                    }
                    first = false;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void number(double number) throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    writer.write(Double.toString(number));
                    if (state == State.VALUE) {
                        pop();
                    }
                    first = false;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void startArray() throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    writer.write('[');
                    push(State.ARRAY);
                    first = true;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void startDocument(String callback) throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case INITIAL:
                    if (callback == null) {
                        hadCallback = false;
                    } else {
                        hadCallback = true;
                        writer.write(callback);
                        writer.write('(');
                    }
                    push(State.DOCUMENT);
                    first = true;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void startObject() throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    writer.write('{');
                    push(State.OBJECT);
                    first = true;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void startString() throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    writer.write('\"');
                    push(State.STRING);
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void string(String string) throws SAXException {
        try {
            State state = peek();
            switch (state) {
                case ARRAY:
                    if (!first) {
                        writer.write(',');
                    }
                    // fall thru
                case DOCUMENT:
                case VALUE:
                    if (string == null) {
                        writer.write("null");
                    } else {
                        writer.write('\"');
                        charactersImpl(string.toCharArray(), 0, string.length());
                        writer.write('\"');
                    }
                    if (state == State.VALUE) {
                        pop();
                    }
                    first = false;
                    break;
                default:
                    throw new SAXException("Illegal state for callback.");
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

}
