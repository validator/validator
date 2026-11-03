//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * The anchor() and anchor-size() functions, which resolve to a length.
 *
 * @spec https://drafts.csswg.org/css-anchor-position-1/#anchor-pos retrieved 2026-11-03
 * @spec https://drafts.csswg.org/css-anchor-position-1/#anchor-size-fn retrieved 2026-11-03
 */
public class CssAnchor extends CssCheckableValue {

    public static final int type = CssTypes.CSS_ANCHOR;

    public static final CssIdent[] anchor_sides;
    public static final CssIdent[] anchor_sizes;

    static {
        String[] _sides = {"inside", "outside", "top", "left", "right",
                "bottom", "start", "end", "self-start", "self-end", "center"};
        anchor_sides = new CssIdent[_sides.length];
        int i = 0;
        for (String s : _sides) {
            anchor_sides[i++] = CssIdent.getIdent(s);
        }
        String[] _sizes = {"width", "height", "block", "inline",
                "self-block", "self-inline"};
        anchor_sizes = new CssIdent[_sizes.length];
        i = 0;
        for (String s : _sizes) {
            anchor_sizes[i++] = CssIdent.getIdent(s);
        }
    }

    private static boolean isOneOf(CssIdent ident, CssIdent[] idents) {
        for (CssIdent id : idents) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    ApplContext ac;
    boolean isSize;
    CssExpression arguments;
    String _toString = null;

    public final int getRawType() {
        return type;
    }

    public final int getType() {
        return CssTypes.CSS_LENGTH;
    }

    /**
     * Create a new CssAnchor
     */
    public CssAnchor() {
    }

    /**
     * Create a new CssAnchor and check its arguments
     *
     * @param ac        For errors and warnings reports
     * @param function  The function token: "anchor(" or "anchor-size("
     * @param arguments The arguments, or null if there are none
     * @throws InvalidParamException if the arguments are incorrect
     */
    public CssAnchor(ApplContext ac, String function, CssExpression arguments)
            throws InvalidParamException {
        this.ac = ac;
        isSize = function.toLowerCase().startsWith("anchor-size");
        this.arguments = arguments;
        if (arguments != null && arguments.hasCssVariable()) {
            markCssVariable();
        } else {
            checkArguments();
        }
    }

    private String functionName() {
        return isSize ? "anchor-size()" : "anchor()";
    }

    /**
     * Checks the arguments:
     * anchor( <anchor-name>? && <anchor-side>, <length-percentage>? )
     * anchor-size( [ <anchor-name> || <anchor-size> ]? , <length-percentage>? )
     */
    private void checkArguments() throws InvalidParamException {
        boolean gotName = false;
        boolean gotKeyword = false;
        boolean gotFallback = false;
        boolean afterComma = false;

        if (arguments == null || arguments.getCount() == 0) {
            if (!isSize) {
                throw new InvalidParamException("unrecognize", ac);
            }
            return;
        }
        arguments.starts();
        while (!arguments.end()) {
            CssValue val = arguments.getValue();
            char op = arguments.getOperator();
            if (afterComma) {
                if (gotFallback) {
                    throw new InvalidParamException("value", val.toString(),
                            functionName(), ac);
                }
                checkFallback(val);
                gotFallback = true;
            } else if (val.getType() == CssTypes.CSS_DASHED_IDENT && !gotName) {
                gotName = true;
            } else if (val.getType() == CssTypes.CSS_IDENT && !gotKeyword
                    && isOneOf(val.getIdent(),
                    isSize ? anchor_sizes : anchor_sides)) {
                gotKeyword = true;
            } else if (!isSize && val.getType() == CssTypes.CSS_PERCENTAGE
                    && !gotKeyword) {
                // a percentage is an <anchor-side> too
                gotKeyword = true;
            } else if (isSize && !gotName && !gotKeyword
                    && arguments.getCount() == 1) {
                // the comma is omitted when everything before it is
                checkFallback(val);
                gotFallback = true;
            } else {
                throw new InvalidParamException("value", val.toString(),
                        functionName(), ac);
            }
            arguments.next();
            if (!arguments.end()) {
                if (op == COMMA && !afterComma) {
                    afterComma = true;
                } else if (op != SPACE || afterComma) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
        arguments.starts();
        if (!isSize && !gotKeyword) {
            // anchor() requires an <anchor-side>
            throw new InvalidParamException("unrecognize", ac);
        }
        if (afterComma && !gotFallback) {
            throw new InvalidParamException("unrecognize", ac);
        }
    }

    // <length-percentage>
    private void checkFallback(CssValue val) throws InvalidParamException {
        switch (val.getType()) {
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                return;
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, functionName());
                return;
            default:
                throw new InvalidParamException("value", val.toString(),
                        functionName(), ac);
        }
    }

    public void set(String s, ApplContext ac) throws InvalidParamException {
        // we don't support this way of setting the value
        // as we rely on the parsing to create it incrementally
        throw new InvalidParamException("unrecognize", s, ac);
    }

    public void setValue(BigDecimal d) {
        // we don't support this way of setting the value
        // as we rely on the parsing to create it incrementally
    }

    /**
     * Returns the value
     */
    public Object get() {
        return toString();
    }

    public String toString() {
        if (_toString == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(isSize ? "anchor-size(" : "anchor(");
            if (arguments != null) {
                sb.append(arguments.toStringFromStart());
            }
            sb.append(')');
            _toString = sb.toString();
        }
        return _toString;
    }

    public boolean equals(Object value) {
        return (value instanceof CssAnchor) && toString().equals(value.toString());
    }

    // The length an anchor() or anchor-size() function resolves to depends
    // on the layout, so it can't be checked here.

    public boolean isPositive() {
        return true;
    }

    public boolean isStrictlyPositive() {
        return true;
    }

    public boolean isZero() {
        return false;
    }

    public void checkEqualsZero(ApplContext ac, String[] messages) {
    }

    public boolean warnEqualsZero(ApplContext ac, String[] messages) {
        return true;
    }
}
