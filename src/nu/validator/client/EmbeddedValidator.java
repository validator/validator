/*
 * Copyright (c) 2017 Mozilla Foundation
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
package nu.validator.client;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import nu.validator.htmlparser.sax.XmlSerializer;
import nu.validator.json.Serializer;
import nu.validator.messages.GnuMessageEmitter;
import nu.validator.messages.JsonMessageEmitter;
import nu.validator.messages.MessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.messages.TextMessageEmitter;
import nu.validator.messages.XmlMessageEmitter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;

/**
 * Nu validator client for HTML validation from within another
 * application.
 */
public class EmbeddedValidator {

    public static final String SCHEMA_URL = "http://s.validator.nu/html5-rdfalite.rnc";

    public static enum OutputFormat {
        TEXT, XML, JSON, GNU
    }

    private boolean asciiQuotes = false;
    private boolean detectLanguages = false;
    private boolean forceHTML = false;
    private int lineOffset = 0;
    private boolean loadEntities = false;
    private boolean noStream = false;
    private OutputFormat outputFormat = OutputFormat.JSON;
    private String schemaUrl = SCHEMA_URL;

    /**
     * Validate the file at the given path
     * 
     * @param path
     *            a valid {@link Path} to a readable file
     * @return validation output {@link String}
     * @throws IllegalStateException
     * @throws IOException
     * @throws SAXException
     */
    public String validate(Path path) throws IOException, SAXException {
        try (OneOffValidator validator = new OneOffValidator(asciiQuotes, detectLanguages, forceHTML, lineOffset, loadEntities, noStream, outputFormat, schemaUrl)) {
            return validator.validate(path);
        }
    }

    /**
     * Validate the input source
     * 
     * @param in
     *            a valid {@link InputStream} to a readable file
     * @return validation output {@link String}
     * @throws IllegalStateException
     * @throws IOException
     * @throws SAXException
     */
    public String validate(InputStream in) throws IOException, SAXException {
        try (OneOffValidator validator = new OneOffValidator(asciiQuotes, detectLanguages, forceHTML, lineOffset, loadEntities, noStream, outputFormat, schemaUrl)) {
            return validator.validate(in);
        }
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    /**
     * Set the output format for the validation
     * 
     * @param outputFormat
     *            {@link OutputFormat}, not {@code null}
     * @throws IllegalArgumentException
     *             if argument is {@code null}
     */
    public void setOutputFormat(OutputFormat outputFormat) {
        if (outputFormat == null) {
            throw new IllegalArgumentException("outputFormat can not be null");
        }
        this.outputFormat = outputFormat;
    }

    public boolean isLoadEntities() {
        return loadEntities;
    }

    /**
     * @param loadEntities
     *            {@code true} to have XML parser load remote DTDs, etc
     */
    public void setLoadEntities(boolean loadEntities) {
        this.loadEntities = loadEntities;
    }

    public boolean isNoLangDetect() {
        return detectLanguages;
    }

    /**
     * @param detectLanguages
     *            {@code true} to enable language detection, {@code false} to
     *            disable language detection
     */
    public void setNoLangDetect(boolean noLangDetect) {
        this.detectLanguages = noLangDetect;
    }

    public boolean isNoStream() {
        return noStream;
    }

    /**
     * @param noStream
     *            if {@code true}, HTML parser will buffer instead of streaming
     */
    public void setNoStream(boolean noStream) {
        this.noStream = noStream;
    }

    public boolean isForceHTML() {
        return forceHTML;
    }

    /**
     * @param forceHTML
     *            if {@code true}, input will be validated as HTML regardless of
     *            its actual document type
     */
    public void setForceHTML(boolean forceHTML) {
        this.forceHTML = forceHTML;
    }

    public boolean isAsciiQuotes() {
        return asciiQuotes;
    }

    /**
     * @param asciiQuotes
     *            {@code true} if curly quotes ({@code '\u2018'} &
     *            {@code '\u2019'}) in emitted warnings and errors should be
     *            replaced with ascii quotes
     */
    public void setAsciiQuotes(boolean asciiQuotes) {
        this.asciiQuotes = asciiQuotes;
    }

    public int getLineOffset() {
        return lineOffset;
    }

    /**
     * @param lineOffset
     *            offset to add or subtract from the line number in emitted
     *            warnings and errors
     */
    public void setLineOffset(int lineOffset) {
        this.lineOffset = lineOffset;
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    /**
     * Default value is {@value #SCHEMA_URL}
     * 
     * @param schemaUrl
     *            url to the required schema
     */
    public void setSchemaUrl(String schemaUrl) {
        if (schemaUrl != null && !schemaUrl.startsWith("http:")) {
            throw new IllegalArgumentException("schemaUrl should be a URL");
        }
        this.schemaUrl = schemaUrl;
    }

    /**
     * Self-contained, single use class for encapsulated building of an embedded
     * validator.
     */
    private class OneOffValidator implements AutoCloseable {

        private static final String MSG_SUCCESS = "Document checking completed. No errors found.";
        private static final String MSG_FAIL = "Document checking completed.";
        private static final String EXTENSION_ERROR = "File was not checked. Files must have .html, .xhtml, .htm, or .xht extensions.";

        private final AtomicBoolean used = new AtomicBoolean(false);
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final BufferedOutputStream bufOut = new BufferedOutputStream(out);

        private final SimpleDocumentValidator validator;
        private final MessageEmitterAdapter errorHandler;
        private final boolean forceHtml;

        private OneOffValidator(boolean asciiQuotes, boolean detectLanguages, boolean forceHtml, int lineOffset, boolean loadEntities,
                boolean noStream, OutputFormat outputFormat, String schemaUrl) throws SAXException {
            this.validator = new SimpleDocumentValidator(true, false, !detectLanguages);
            this.errorHandler = newErrorHandler(lineOffset, asciiQuotes, outputFormat);
            this.forceHtml = forceHtml;
            try {
                this.validator.setUpMainSchema(schemaUrl == null ? SCHEMA_URL : schemaUrl, new SystemErrErrorHandler());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            this.validator.setUpValidatorAndParsers(errorHandler, noStream, loadEntities);
        }

        public MessageEmitterAdapter newErrorHandler(int lineOffset, boolean asciiQuotes, OutputFormat outputFormat) throws SAXException {
            boolean showSource = true;
            boolean batchMode = true;
            MessageEmitterAdapter adapter = new MessageEmitterAdapter(null, this.validator.getSourceCode(), showSource,
                    new ImageCollector(this.validator.getSourceCode()), lineOffset, batchMode, newEmitter(asciiQuotes, outputFormat));
            adapter.setErrorsOnly(false);
            adapter.setHtml(true);
            adapter.start(null);
            return adapter;
        }

        private MessageEmitter newEmitter(boolean asciiQuotes, OutputFormat outputFormat) {
            switch (outputFormat) {
            case TEXT:
                return new TextMessageEmitter(this.out, asciiQuotes);
            case GNU:
                return new GnuMessageEmitter(this.out, asciiQuotes);
            case JSON:
                return new JsonMessageEmitter(new Serializer(this.out), null);
            case XML:
                return new XmlMessageEmitter(new XmlSerializer(this.out));
            default:
                throw new UnsupportedOperationException("OutputFormat " + outputFormat + " not supported");
            }
        }

        private String validate(Path path) throws IOException, SAXException {
            if (!used.compareAndSet(false, true)) {
                throw new IllegalStateException("OneOffValidator instances are not reusable");
            }
            try {
                if (Files.notExists(path) || !Files.isReadable(path)) {
                    errorHandler.warning(new SAXParseException(
                            "File not found.", null, path.toString(), -1, -1));
                } else if (isXhtml(path.toFile())) {
                    if (forceHtml) {
                        validator.checkHtmlFile(path.toFile(), true);
                    } else {
                        validator.checkXmlFile(path.toFile());
                    }
                } else if (isHtml(path.toFile())) {
                    validator.checkHtmlFile(path.toFile(), true);
                } else {
                    errorHandler.warning(new SAXParseException(EXTENSION_ERROR, null, path.toString(), -1, -1));
                }
            } catch (SAXException e) {
                errorHandler.warning(new SAXParseException(e.getMessage(), null, path.toString(), -1, -1));
            }

            errorHandler.end(MSG_SUCCESS, MSG_FAIL, "");
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }

        private boolean isXhtml(File file) {
            String name = file.getName();
            return name.endsWith(".xhtml") || name.endsWith(".xht");
        }

        private boolean isHtml(File file) {
            String name = file.getName();
            return name.endsWith(".html") || name.endsWith(".htm");
        }

        private String validate(InputStream in) throws IOException, SAXException {
            if (!used.compareAndSet(false, true)) {
                throw new IllegalStateException("OneOffValidator instances are not reusable");
            }
            validator.checkHtmlInputSource(new InputSource(in));
            errorHandler.end(MSG_SUCCESS, MSG_FAIL, "");
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }

        @Override
        public void close() {
            try {
                bufOut.close();
            } catch (IOException e) {
            }
            try {
                out.close();
            } catch (IOException e) {
            }
        }

    }

}
