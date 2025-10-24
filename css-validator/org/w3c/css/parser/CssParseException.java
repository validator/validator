//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import org.w3c.css.parser.analyzer.ParseException;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

import java.util.ArrayList;

/**
 * @version $Revision$
 */
public class CssParseException extends ParseException {

    /**
     * The list of context when the error appears
     */
    ArrayList<CssSelectors> contexts;

    /**
     * the property name
     */
    String property;

    /**
     * the skipped text
     */
    String skippedString;

    /**
     * An expression
     */
    CssExpression exp;

    /**
     * The real exception
     */
    Exception parseException;

    private boolean error;

    /**
     * Create a new CssParseException
     */
    public CssParseException(Exception exc) {
        parseException = exc;
        if (parseException instanceof ParseException) {
            ParseException e = (ParseException) exc;
            error = (e.currentToken != null
                    && e.expectedTokenSequences != null
                    && e.tokenImage != null);
        }
    }

    public Exception getException() {
        return parseException;
    }

    public boolean isParseException() {
        return (parseException instanceof ParseException);
    }

    /**
     * Get the exception message
     */
    public String getMessage() {
        if (!error) {
            return parseException.getMessage();
        } else {
            return null;
        }
    }

    /**
     * Set the attribute contexts
     *
     * @param contexts the new value for the attribute
     */
    public void setContexts(ArrayList<CssSelectors> contexts) {
        this.contexts = contexts;
    }

    /**
     * Returns the attribute contexts
     *
     * @return the value of the attribute
     */
    public ArrayList<CssSelectors> getContexts() {
        return contexts;
    }

    /**
     * Set the attribute property
     *
     * @param property the new value for the attribute
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Returns the attribute property
     *
     * @return the value of the attribute
     */
    public String getProperty() {
        return property;
    }

    /**
     * Set the attribute skippedString
     *
     * @param skippedString the new value for the attribute
     */
    public void setSkippedString(String skippedString) {
        this.skippedString = skippedString;
    }

    /**
     * Returns the attribute skippedString
     *
     * @return the value of the attribute
     */
    public String getSkippedString() {
        return skippedString;
    }

    /**
     * Set the attribute exp
     *
     * @param exp the new value for the attribute
     */
    public void setExp(CssExpression exp) {
        this.exp = exp;
    }

    /**
     * Returns the attribute exp
     *
     * @return the value of the attribute
     */
    public CssExpression getExp() {
        return exp;
    }

    public String getErrorType() {
        if (parseException == null) {
            return null;
        }
        if (parseException instanceof InvalidParamException) {
            return ((InvalidParamException) parseException).getErrorType();
        }
        if (parseException instanceof ParseException) {
            return CssError.parserError;
        }
        return parseException.getClass().toString();
    }
}
