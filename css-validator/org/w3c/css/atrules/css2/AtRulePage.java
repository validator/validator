//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.atrules.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.util.ArrayList;


/**
 * This class manages all page selectors defined in CSS2
 */
public class AtRulePage extends org.w3c.css.atrules.css.AtRulePage {

    static final String[] _pseudo = {
            ":left", ":right", ":first"
    };

    /*
     * Sets the name of the page
     * name will be a pseudo name :first, :left, :right
     * or a random name without semi-colon at the beginning
     */
    public AtRulePage addSelector(String name, ArrayList<String> pseudo, ApplContext ac)
            throws InvalidParamException {
        if (names == null) {
            names = new ArrayList<>();
        } else {
            // we allow only one value,
            throw new InvalidParamException("notversion", name, ac.getCssVersionString(), ac);
        }
        if (pseudos == null) {
            pseudos = new ArrayList<>();
        }
        names.add(name);
        if ((pseudo != null) && pseudo.size() > 1) {
            // and only one pseudo at most.
            throw new InvalidParamException("notversion", pseudo.toString(), ac.getCssVersionString(), ac);
        }
        // second check, that it is in the list
        if (pseudo != null && !pseudo.isEmpty()) {
            String p = pseudo.get(0);
            for (String _p : _pseudo) {
                if (_p.equals(p)) {
                    pseudos.add(pseudo);
                    return this;
                }
            }
        } else {
            pseudos.add(pseudo);
            return this;
        }
        // failed...
        throw new InvalidParamException("notversion", pseudo.toString(), ac.getCssVersionString(), ac);
    }
}
