//
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
// Updated September 14th 2000 Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang 1997-2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2015/WD-css-display-3-20151015/#propdef-display
 * @spec https://www.w3.org/TR/2016/CR-css-flexbox-1-20160526/#flex-containers
 * @spec https://www.w3.org/TR/2016/CR-css-grid-1-20160929/#valdef-display-subgrid
 * @spec https://www.w3.org/TR/2014/WD-css-ruby-1-20140805/#propdef-display
 */
public class CssDisplay extends org.w3c.css.properties.css.CssDisplay {

    public static CssIdent[] display_outside;
    public static CssIdent[] display_inside;
    public static CssIdent[] display_listitem;
    public static CssIdent[] display_internal;
    public static CssIdent[] display_box;
    public static CssIdent[] display_legacy;
    // for display-listitem
    public static CssIdent[] display_flow;


    static {
        String[] DISPLAY_OUTSIDE = {"block", "inline", "run-in"};
        String[] DISPLAY_INSIDE = {"flow", "flow-root", "table", "flex", "grid", "ruby", "subgrid"};
        String[] DISPLAY_LISTITEM = {"list-item"};
        String[] DISPLAY_INTERNAL = {"table-row-group", "table-header-group", "table-footer-group",
                "table-row", "table-cell", "table-column-group", "table-column", "table-caption",
                "ruby-base", "ruby-text", "ruby-base-container", "ruby-text-container"};
        String[] DISPLAY_BOX = {"contents", "none"};
        String[] DISPLAY_LEGACY = {"inline-block", "inline-list-item", "inline-table",
                "inline-flex", "inline-grid"};
        String[] DISPLAY_FLOW = {"flow", "flow-root"};

        display_outside = new CssIdent[DISPLAY_OUTSIDE.length];
        int i = 0;
        for (String aDISPLAY : DISPLAY_OUTSIDE) {
            display_outside[i++] = CssIdent.getIdent(aDISPLAY);
        }
        i = 0;
        display_inside = new CssIdent[DISPLAY_INSIDE.length];
        for (String aDISPLAY : DISPLAY_INSIDE) {
            display_inside[i++] = CssIdent.getIdent(aDISPLAY);
        }
        i = 0;
        display_listitem = new CssIdent[DISPLAY_LISTITEM.length];
        for (String aDISPLAY : DISPLAY_LISTITEM) {
            display_listitem[i++] = CssIdent.getIdent(aDISPLAY);
        }
        i = 0;
        display_internal = new CssIdent[DISPLAY_INTERNAL.length];
        for (String aDISPLAY : DISPLAY_INTERNAL) {
            display_internal[i++] = CssIdent.getIdent(aDISPLAY);
        }
        i = 0;
        display_box = new CssIdent[DISPLAY_BOX.length];
        for (String aDISPLAY : DISPLAY_BOX) {
            display_box[i++] = CssIdent.getIdent(aDISPLAY);
        }
        i = 0;
        display_legacy = new CssIdent[DISPLAY_LEGACY.length];
        for (String aDISPLAY : DISPLAY_LEGACY) {
            display_legacy[i++] = CssIdent.getIdent(aDISPLAY);
        }
        i = 0;
        display_flow = new CssIdent[DISPLAY_FLOW.length];
        for (String aDISPLAY : DISPLAY_FLOW) {
            display_flow[i++] = CssIdent.getIdent(aDISPLAY);
        }
    }

    public static CssIdent getMatchingIdentInArray(CssIdent ident, CssIdent[] identArray) {
        for (CssIdent id : identArray) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssDisplay
     */
    public CssDisplay() {
        value = initial;
    }

    /**
     * Create a new CssDisplay
     *
     * @param ac         The context
     * @param expression The expression for this property
     * @param check      true if explicit check is needed
     * @throws org.w3c.css.util.InvalidParamException Values are incorect
     */
    public CssDisplay(ApplContext ac, CssExpression expression,
                      boolean check) throws InvalidParamException {
        int count = expression.getCount();
        if (check && count > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;
        char op;

        boolean inside = false;
        boolean outside = false;
        boolean listitem = false;
        // flow can be in listitem and inside...
        boolean flow = false;

        CssValueList v = new CssValueList();
        CssIdent id;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() == CssTypes.CSS_IDENT) {
                CssIdent id_val = val.getIdent();
                id = null;
                // let's check the values which can occur only once.
                if (count == 1) {
                    if (inherit.equals(id_val)) {
                        id = inherit;
                        value = val;
                    } else if ((id = getMatchingIdentInArray(id_val, display_box)) != null) {
                        value = val;
                    } else if ((id = getMatchingIdentInArray(id_val, display_internal)) != null) {
                        value = val;
                    } else if ((id = getMatchingIdentInArray(id_val, display_legacy)) != null) {
                        value = val;
                    }
                }
                if (id == null) {
                    // oustide, list-item and inside (flow) remains.
                    id = getMatchingIdentInArray(id_val, display_outside);
                    if (id != null) {
                        if (!outside) {
                            outside = true;
                            v.add(val);
                        } else {
                            throw new InvalidParamException("value", id_val,
                                    getPropertyName(), ac);
                        }
                    } else {
                        id = getMatchingIdentInArray(id_val, display_listitem);
                        if (id != null) {
                            // valid only if we don't have inside, or if inside is flow

                            if (!listitem && (!inside || flow)) {
                                listitem = true;
                                v.add(val);
                            } else {
                                throw new InvalidParamException("value", id_val,
                                        getPropertyName(), ac);
                            }

                        } else { // inside, with special casing for flow.
                            id = getMatchingIdentInArray(id_val, display_inside);
                            if (id == null || inside) {
                                throw new InvalidParamException("value", id_val,
                                        getPropertyName(), ac);
                            }
                            v.add(val);
                            inside = true;
                            flow = (getMatchingIdentInArray(id_val, display_flow) != null);
                        }
                    }
                }
            } else {
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
            }

            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
            expression.next();
        }

        if (v.size() > 0) {
            value = v.size() == 1 ? v.get(0) : v;
        }
    }

    public CssDisplay(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */

    public boolean isDefault() {
        return (value == inline);
    }

}
