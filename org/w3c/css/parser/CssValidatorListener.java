//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Warnings;

import java.util.ArrayList;

/**
 * Implements this interface if you want to use the CSS1 parser.
 *
 * @version $Revision$
 */
public interface CssValidatorListener {

    /**
     * Adds a vector of properties to a selector.
     *
     * @param selector   the selector
     * @param properties Properties to associate with contexts
     */
    public void handleRule(ApplContext ac, CssSelectors selector,
                           ArrayList<CssProperty> properties);

    /**
     * Handles an at-rule.
     * <p/>
     * <p>The parameter <code>value</code> can be :
     * <DL>
     * <DT>CssString
     * <DD>The value coming from a string.
     * <DT>CssURL
     * <DD>The value coming from an URL.
     * <DT>Vector
     * <DD>The value is a vector of declarations (it contains Couple).
     * </DL>
     *
     * @param ident The ident for this at-rule (for example: 'font-face')
     * @param value The string representation of this at-rule
     */
    public void handleAtRule(ApplContext ac, String ident, String value);

    /**
     * Notify all errors
     *
     * @param errors All errors in the style sheet
     * @see CssError
     * @see CssErrorToken
     */
    public void notifyErrors(Errors errors);

    /**
     * Notify all warnings
     *
     * @param warnings All warnings in the style sheet
     * @see org.w3c.css.util.Warning
     */
    public void notifyWarnings(Warnings warnings);

    public void addCharSet(String charset);

    public void newAtRule(AtRule atRule);

    public void endOfAtRule();

    public void setImportant(boolean important);

    public void setSelectorList(ArrayList<CssSelectors> selectors);

    public void setProperty(ArrayList<CssProperty> properties);

    public void endOfRule();

    public void removeThisRule();

    public void removeThisAtRule();

}
