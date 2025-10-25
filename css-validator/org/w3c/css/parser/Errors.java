//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import java.util.ArrayList;

/**
 * Controls all errors in the validator
 *
 * @version $Revision$
 */
public final class Errors {

    private ArrayList<CssError> errorData = new ArrayList<CssError>();

    /**
     * Add an error.
     *
     * @param error The new error.
     */
    public final void addError(CssError error) {
        errorData.add(error);
    }

    /**
     * Add errors.
     *
     * @param errors All errors
     */
    public final void addErrors(Errors errors) {
        errorData.addAll(errors.errorData);
    }

    /**
     * Get the number of errors.
     */
    public final int getErrorCount() {
        return errorData.size();
    }

    /**
     * Get an array with all errors.
     */
    public final CssError[] getErrors() {
        CssError out[] = new CssError[errorData.size()];
        errorData.toArray(out);
        return out;
    }

    /**
     * Get an error with an index.
     *
     * @param index the error index.
     */
    public final CssError getErrorAt(int index) {
        return errorData.get(index);
    }

}
