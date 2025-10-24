// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.attributes;

import org.w3c.css.selectors.AttributeSelector;
import org.w3c.css.selectors.Selector;
import org.w3c.css.util.ApplContext;

/**
 * AttributeAny<br />
 * Created: Sep 1, 2005 4:20:49 PM<br />
 */
public class AttributeAny extends AttributeSelector {

    public AttributeAny(String name) {
        super(name);
    }

    public AttributeAny(String name, String prefix) {
        super(name, prefix);
    }

    public boolean canApply(Selector other) {
        return true;
    }

    public void applyAttribute(ApplContext ac, AttributeSelector attr) {
    }


}
