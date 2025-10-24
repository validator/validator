//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.pseudofunctions;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.selectors.PseudoFunctionSelector;

import java.util.ArrayList;

/**
 * PseudoFunctionSlotted
 */
public class PseudoFunctionSlotted extends PseudoFunctionSelector {

    private String representation = null;

    public PseudoFunctionSlotted(String name, String value) {
        setName(name);
        setParam(value);
        setAsPseudoElement();
    }

    public PseudoFunctionSlotted(String name, ArrayList<CssSelectors> selector_list) {
        this(name, CssSelectors.toArrayString(selector_list));
    }

}
