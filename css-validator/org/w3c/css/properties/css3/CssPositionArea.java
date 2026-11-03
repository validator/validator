//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://drafts.csswg.org/css-anchor-position-1/#propdef-position-area retrieved 2026-11-03
 */
public class CssPositionArea extends org.w3c.css.properties.css.CssPositionArea {

    // The keyword groups of <position-area>. A value is one keyword from
    // either group of a pair, or one keyword from each group of a pair (in
    // either order). The last two pairs combine a group with itself, so they
    // match [ ... ]{1,2}.
    private static final CssIdent[][][] keywordPairs;

    static {
        String[][][] _pairs = {
                {{"left", "center", "right", "span-left", "span-right",
                        "x-start", "x-end", "span-x-start", "span-x-end",
                        "self-x-start", "self-x-end", "span-self-x-start",
                        "span-self-x-end", "span-all"},
                        {"top", "center", "bottom", "span-top", "span-bottom",
                                "y-start", "y-end", "span-y-start", "span-y-end",
                                "self-y-start", "self-y-end", "span-self-y-start",
                                "span-self-y-end", "span-all"}},
                {{"block-start", "center", "block-end", "span-block-start",
                        "span-block-end", "span-all"},
                        {"inline-start", "center", "inline-end",
                                "span-inline-start", "span-inline-end", "span-all"}},
                {{"self-block-start", "center", "self-block-end",
                        "span-self-block-start", "span-self-block-end", "span-all"},
                        {"self-inline-start", "center", "self-inline-end",
                                "span-self-inline-start", "span-self-inline-end",
                                "span-all"}},
                {{"start", "center", "end", "span-start", "span-end", "span-all"},
                        {"start", "center", "end", "span-start", "span-end",
                                "span-all"}},
                {{"self-start", "center", "self-end", "span-self-start",
                        "span-self-end", "span-all"},
                        {"self-start", "center", "self-end", "span-self-start",
                                "span-self-end", "span-all"}}
        };
        keywordPairs = new CssIdent[_pairs.length][2][];
        for (int i = 0; i < _pairs.length; i++) {
            for (int j = 0; j < 2; j++) {
                keywordPairs[i][j] = new CssIdent[_pairs[i][j].length];
                for (int k = 0; k < _pairs[i][j].length; k++) {
                    keywordPairs[i][j][k] = CssIdent.getIdent(_pairs[i][j][k]);
                }
            }
        }
    }

    /**
     * Checks if an ident is one of the <position-area> keywords.
     */
    public static boolean isAreaKeyword(CssIdent ident) {
        for (CssIdent[][] pair : keywordPairs) {
            if (CssAnchorName.isOneOf(ident, pair[0])
                    || CssAnchorName.isOneOf(ident, pair[1])) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAreaPair(CssIdent first, CssIdent second) {
        for (CssIdent[][] pair : keywordPairs) {
            if ((CssAnchorName.isOneOf(first, pair[0])
                    && CssAnchorName.isOneOf(second, pair[1]))
                    || (CssAnchorName.isOneOf(first, pair[1])
                    && CssAnchorName.isOneOf(second, pair[0]))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses a whole expression as a <position-area> value: one or two
     * keywords.
     */
    static CssValue parsePositionArea(ApplContext ac, CssExpression expression,
                                      String propertyName)
            throws InvalidParamException {
        if (expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssIdent> idents = new ArrayList<>();
        while (!expression.end()) {
            CssValue val = expression.getValue();
            char op = expression.getOperator();
            if (val.getType() != CssTypes.CSS_IDENT
                    || !isAreaKeyword(val.getIdent())) {
                throw new InvalidParamException("value", val,
                        propertyName, ac);
            }
            if (!idents.isEmpty()
                    && !isAreaPair(idents.get(0), val.getIdent())) {
                throw new InvalidParamException("value", val,
                        propertyName, ac);
            }
            idents.add(val.getIdent());
            values.add(val);
            expression.next();
            if (!expression.end() && (op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    /**
     * Create a new CssPositionArea
     */
    public CssPositionArea() {
        value = initial;
    }

    /**
     * Creates a new CssPositionArea
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssPositionArea(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();
        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent id = val.getIdent();
            if (CssIdent.isCssWide(id) || none.equals(id)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = val;
                expression.next();
                return;
            }
        }
        value = parsePositionArea(ac, expression, getPropertyName());
    }

    public CssPositionArea(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
