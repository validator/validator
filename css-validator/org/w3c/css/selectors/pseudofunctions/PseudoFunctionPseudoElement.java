//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.pseudofunctions;

import org.w3c.css.selectors.PseudoFunctionSelector;

/**
 * A functional pseudo-element whose argument isn't a selector, such as
 * ::highlight(), ::part(), or ::view-transition-group()
 */
public class PseudoFunctionPseudoElement extends PseudoFunctionSelector {

    public PseudoFunctionPseudoElement(String name, String param) {
        setName(name);
        setParam(param);
        setAsPseudoElement();
    }

}
