//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2013.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2020/CR-css-speech-1-20200310/#cue-before
 */
public class CssCueBefore extends org.w3c.css.properties.css.CssCueBefore {

    /**
     * Create a new CssCueBefore
     */
    public CssCueBefore() {
        value = initial;
    }

    /**
     * Creates a new CssCueBefore
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssCueBefore(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        value = CssCue.checkCueValue(ac, expression, this);
        if (!expression.end()) {
            throw new InvalidParamException("value",
                    value, getPropertyName(), ac);
        }
    }

    public CssCueBefore(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

