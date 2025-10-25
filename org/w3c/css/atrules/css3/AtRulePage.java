//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.util.ArrayList;


/**
 * This class manages all page selectors defined inCSS3
 */
public class AtRulePage extends org.w3c.css.atrules.css.AtRulePage {

    static final String[] _pseudo = {
            ":left", ":right", ":first", ":blank"
    };

    private String keyword = "page";

    /*
  * Sets the name of the page
  * name will be a pseudo name :first, :left, :right
  * or a random name without semi-colon at the beginning
  */
    public AtRulePage addSelector(String name, ArrayList<String> pseudo, ApplContext ac)
            throws InvalidParamException {
        if (names == null) {
            names = new ArrayList<>();
        }
        if (pseudos == null) {
            pseudos = new ArrayList<>();
        }
        names.add(name);
        // second check, that it is in the list
        if (pseudo != null && !pseudo.isEmpty()) {
            for (String p : pseudo) {
                boolean found = false;
                for (String _p : _pseudo) {
                    if (_p.equals(p)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new InvalidParamException("unrecognize", p, ac);

                }
            }
        }
        pseudos.add(pseudo);
        return this;
    }

    public void setKeyword(String k) {
        keyword = k;
    }

    /**
     * Returns the at rule keyword
     */
    public String effectiveKeyword() {
        return keyword;
    }
}
