//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import org.w3c.css.parser.analyzer.ParseException;
import org.w3c.css.parser.analyzer.Token;

import java.util.ArrayList;

/**
 * @version $Revision$
 */
public class CssErrorToken extends CssError {

    /**
     * the property name
     */
    String property;

    /**
     * the expected text
     */
    String[] expectedTokens;

    /**
     * the token in error
     */
    String errorToken;
    /**
     * the skipped text
     */
    String skippedString;

    public CssErrorToken(ParseException ex, String skippedString) {
        Token errtoken = ex.currentToken;
        this.skippedString = skippedString;
        line = errtoken.next.beginLine;
        ArrayList<String> expected = new ArrayList<String>();
        for (int[] idx : ex.expectedTokenSequences) {
            if (idx.length > 0) {
                expected.add(ex.tokenImage[idx[0]]);
            }
        }
        expectedTokens = new String[expected.size()];
        expectedTokens = expected.toArray(expectedTokens);
        errorToken = errtoken.next.image;
        error = ex;
    }

    /**
     * Get the name of the property.
     */
    public String getPropertyName() {
        return property;
    }

    /**
     * Get the expected text.
     */
    public String[] getExpected() {
        return expectedTokens;
    }

    /**
     * Get the skipped text.
     */
    public String getSkippedString() {
        return skippedString;
    }

    public String getErrorToken() {
        return errorToken;
    }
}
