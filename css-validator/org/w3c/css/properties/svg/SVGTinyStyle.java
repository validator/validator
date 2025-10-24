//
// $Id$
// From Sijtsche de Jong
//
// COPYRIGHT (c) 1995-2002 World Wide Web Consortium, (MIT, INRIA, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.svg;

import org.w3c.css.properties.css.CssFill;
import org.w3c.css.properties.css.CssFillRule;
import org.w3c.css.properties.css.CssStroke;
import org.w3c.css.properties.css.CssStrokeDasharray;
import org.w3c.css.properties.css.CssStrokeDashoffset;
import org.w3c.css.properties.css.CssStrokeLinecap;
import org.w3c.css.properties.css.CssStrokeLinejoin;
import org.w3c.css.properties.css.CssStrokeMiterlimit;
import org.w3c.css.properties.css.CssStrokeWidth;
import org.w3c.css.properties.css3.Css3Style;

public class SVGTinyStyle extends Css3Style {

    public CssFillRule cssFillRule;
    public CssStrokeDasharray cssStrokeDasharray;
    public CssStrokeDashoffset cssStrokeDashoffset;
    public CssStrokeLinecap cssStrokeLinecap;
    public CssStrokeLinejoin cssStrokeLinejoin;
    public CssStrokeMiterlimit cssStrokeMiterlimit;
    public CssStrokeWidth cssStrokeWidth;
    public CssFill cssFill;
    public CssStroke cssStroke;


    public CssFillRule getFillRule() {
        if (cssFillRule == null) {
            cssFillRule = (CssFillRule) style.CascadingOrder(new CssFillRule(),
                    style, selector);
        }
        return cssFillRule;
    }

    public CssStrokeWidth getStrokeWidth() {
        if (cssStrokeWidth == null) {
            cssStrokeWidth = (CssStrokeWidth) style.CascadingOrder(new CssStrokeWidth(),
                    style, selector);
        }
        return cssStrokeWidth;
    }

    public CssStrokeLinecap getStrokeLinecap() {
        if (cssStrokeLinecap == null) {
            cssStrokeLinecap = (CssStrokeLinecap) style.CascadingOrder(new CssStrokeLinecap(),
                    style, selector);
        }
        return cssStrokeLinecap;
    }

    public CssStrokeLinejoin getStrokeLinejoin() {
        if (cssStrokeLinejoin == null) {
            cssStrokeLinejoin = (CssStrokeLinejoin) style.CascadingOrder(new CssStrokeLinejoin(),
                    style, selector);
        }
        return cssStrokeLinejoin;
    }

    public CssStrokeDashoffset getStrokeDashoffset() {
        if (cssStrokeDashoffset == null) {
            cssStrokeDashoffset = (CssStrokeDashoffset) style.CascadingOrder(new CssStrokeDashoffset(),
                    style, selector);
        }
        return cssStrokeDashoffset;
    }

    public CssStrokeDasharray getStrokeDasharray() {
        if (cssStrokeDasharray == null) {
            cssStrokeDasharray = (CssStrokeDasharray) style.CascadingOrder(new CssStrokeDasharray(),
                    style, selector);
        }
        return cssStrokeDasharray;
    }

    public CssStrokeMiterlimit getStrokeMiterlimit() {
        if (cssStrokeMiterlimit == null) {
            cssStrokeMiterlimit = (CssStrokeMiterlimit) style.CascadingOrder(new CssStrokeMiterlimit(),
                    style, selector);
        }
        return cssStrokeMiterlimit;
    }

    public CssFill getFill() {
        if (cssFill == null) {
            cssFill = (CssFill) style.CascadingOrder(new CssFill(),
                    style, selector);
        }
        return cssFill;
    }

    public CssStroke getStroke() {
        if (cssStroke == null) {
            cssStroke = (CssStroke) style.CascadingOrder(new CssStroke(),
                    style, selector);
        }
        return cssStroke;
    }


    /**
     * Returns the name of the actual selector
     */
    public String getSelector() {
        return (selector.getElement().toLowerCase());
    }

}
