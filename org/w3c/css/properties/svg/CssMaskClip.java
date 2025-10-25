//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;

/**
 * @spec https://www.w3.org/TR/2014/CR-css-masking-1-20140826/#the-mask-clip
 */
public class CssMaskClip extends org.w3c.css.properties.css.CssMaskClip {

    public static final CssIdent no_clip = CssIdent.getIdent("no-clip");

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        // <geometry-box> | <no-clip>
        CssIdent idt = CssClipPath.getGeometryBoxAllowedValue(ident);
        if (idt != null) {
            return idt;
        }
        if (no_clip.equals(ident)) {
            return no_clip;
        }
        return null;
    }

    /**
     * Create a new CssMaskClip
     */
    public CssMaskClip() {
        value = initial;
    }

    /**
     * Creates a new CssMaskClip
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssMaskClip(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        ArrayList<CssValue> v = new ArrayList<>();
        CssValue val;
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        value = val;
                        break;
                    }
                    if (getAllowedIdent(id) != null) {
                        v.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (op != COMMA && !expression.end()) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
        }
        if (!v.isEmpty()) {
            value = (v.size() == 1) ? v.get(0) : new CssLayerList(v);
        }
    }

    public CssMaskClip(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

