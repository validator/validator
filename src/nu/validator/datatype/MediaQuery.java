/*
 * Copyright (c) 2007-2019 Mozilla Foundation
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

package nu.validator.datatype;

import java.io.StringReader;

import org.relaxng.datatype.DatatypeException;
import org.w3c.css.css.StyleSheetParser;
import org.w3c.css.parser.CssError;
import org.w3c.css.parser.CssParseException;
import org.w3c.css.parser.Errors;
import org.w3c.css.util.ApplContext;

public class MediaQuery extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final MediaQuery THE_INSTANCE = new MediaQuery();

    protected MediaQuery() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        ApplContext ac = new ApplContext("en");
        ac.setCssVersionAndProfile("css3svg");
        ac.setMedium("all");
        ac.setSuggestPropertyName(false);
        ac.setTreatVendorExtensionsAsWarnings(true);
        ac.setTreatCssHacksAsWarnings(true);
        ac.setWarningLevel(-1);
        ac.setFakeURL("file://localhost/StyleElement");
        String literalString = literal.toString();
        String style;
        if (isMediaCondition()) {
            style = String.format("@media all and %s %s", literalString, "{}");
        } else {
            style = String.format("@media %s %s", literalString, "{}");
        }
        StyleSheetParser styleSheetParser = new StyleSheetParser(ac);
        styleSheetParser.parseStyleSheet(ac, new StringReader(style), null);
        styleSheetParser.getStyleSheet().findConflicts(ac);
        Errors errors = styleSheetParser.getStyleSheet().getErrors();
        for (int i = 0; i < errors.getErrorCount(); i++) {
            String message = "";
            String cssProperty = "";
            String cssMessage = "";
            CssError error = errors.getErrorAt(i);
            Throwable ex = error.getException();
            if (ex instanceof CssParseException) {
                CssParseException cpe = (CssParseException) ex;
                if ("generator.unrecognize" //
                        .equals(cpe.getErrorType())) {
                    cssMessage = "Parse Error";
                }
                if (cpe.getProperty() != null) {
                    cssProperty = String.format("\u201c%s\u201D: ",
                            cpe.getProperty());
                }
                if (cpe.getMessage() != null) {
                    cssMessage = cpe.getMessage();
                }
                if (!"".equals(cssMessage)) {
                    message = cssProperty + cssMessage + ".";
                }
            } else {
                message = ex.getMessage();
            }
            if (!"".equals(message)) {
                throw newDatatypeException(message);
            }
        }
    }

    protected boolean isMediaCondition() {
        return false;
    }

    @Override
    public String getName() {
        return "media query";
    }

}
