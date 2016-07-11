/*
 * Copyright (c) 2007-2008 Mozilla Foundation
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import nu.validator.messages.types.MessageType;

import org.xml.sax.SAXException;

public class GnuMessageEmitter extends MessageEmitter {

    private final Writer writer;

    private final TextMessageTextHandler messageTextHandler;
    
    private char[] fileName;

    private static Writer newOutputStreamWriter(OutputStream out) {
        CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
        enc.onMalformedInput(CodingErrorAction.REPLACE);
        enc.onUnmappableCharacter(CodingErrorAction.REPLACE);
        return new OutputStreamWriter(out, enc);
    }

    public GnuMessageEmitter(OutputStream out, boolean asciiQuotes) {
        this.writer = newOutputStreamWriter(out);
        this.messageTextHandler = new TextMessageTextHandler(writer, asciiQuotes);
    }

    @Override
    public void startMessage(MessageType type, String systemId,
            int oneBasedFirstLine, int oneBasedFirstColumn,
            int oneBasedLastLine, int oneBasedLastColumn, boolean exact)
            throws SAXException {
        try {
            if (systemId == null) {
                if (fileName != null) {
                    writer.write(fileName, 0, fileName.length);
                }
            } else {
                writer.write(toCString(systemId));
            }
            writer.write(':');
            if (oneBasedLastLine != -1) {
                // We have a location, do we have start, too?
                if (oneBasedFirstLine != -1) {
                    writer.write(Integer.toString(oneBasedFirstLine));  
                    // GNU only allows the start point to have a col if the 
                    // end has a col too
                    if (oneBasedFirstColumn != -1 && oneBasedLastColumn != -1) {
                        writer.write('.');
                        writer.write(Integer.toString(oneBasedFirstColumn));                    
                    }
                    writer.write('-');
                }
                writer.write(Integer.toString(oneBasedLastLine));  
                // GNU only allows the end point to have a col if the 
                // start had a col too
                if (oneBasedFirstColumn != -1 && oneBasedLastColumn != -1) {
                    writer.write('.');
                    writer.write(Integer.toString(oneBasedLastColumn));                    
                }
                writer.write(':');
            }                
            writer.write(' ');
            writer.write(type.getFlatType());
            writer.write(':');
            writer.write(' ');
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endMessages()
     */
    @Override
    public void endMessages(String language) throws SAXException {
         try {
             writer.flush();
             writer.close();
         } catch (IOException e) {
         throw new SAXException(e.getMessage(), e);
         }
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startMessages(java.lang.String, boolean)
     */
    @Override
    public void startMessages(String documentUri, boolean willShowSource) throws SAXException {
        if (documentUri == null) {
            fileName = null;
        } else {
            fileName = toCString(documentUri);
        }
    }

    private char[] toCString(String documentUri) {
        StringBuilder sb = new StringBuilder(documentUri.length() + 2);
        sb.append('\"');
        for (int i = 0; i < documentUri.length(); i++) {
            char c = documentUri.charAt(i);
            if (c == '\"') {
                sb.append("%22");
            } else {
                sb.append(c);
            }
        }
        sb.append('\"');
        char[] rv = new char[sb.length()];
        sb.getChars(0, sb.length(), rv, 0);
        return rv;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startText()
     */
    @Override
    public MessageTextHandler startText() throws SAXException {
        return messageTextHandler;
    }

    @Override
    public void endMessage() throws SAXException {
        try {
            writer.write('\n');
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
}
