//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import org.w3c.css.css.StyleSheet;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Warning;
import org.w3c.css.util.Warnings;

/**
 * This class represents a class for one context
 *
 * @version $Revision$
 */
public class CssStyle {

    /**
     * For warnings report.
     */
    protected Warnings warnings;

    /**
     * The entire style sheet.
     */
    protected StyleSheet style;

    /**
     * The context of this style.
     */
    protected CssSelectors selector;

    /**
     * Set the context of this style.
     *
     * @param selectors The context.
     */
    public final void setSelector(CssSelectors selectors) {
        this.selector = selectors;
    }

    /**
     * Set the style sheet of this style.
     *
     * @param style The style sheet.
     */
    public final void setStyleSheet(StyleSheet style) {
        this.style = style;
    }

    /**
     * Add a warning about redefinition of a property to this style.
     *
     * @param property The property.
     */
    public final void addRedefinitionWarning(ApplContext ac,
                                             CssProperty property) {
        warnings.addWarning(new Warning(property, "redefinition", 2, ac));
    }

    /**
     * Add a warning to this style.
     *
     * @param warn The Warning.
     */
    public final void addWarning(ApplContext ac, Warning warn) {
        warnings.addWarning(warn);
    }

    /**
     * Add a property to this style
     *
     * @param property the property to add
     * @param warnings where to add warnings if required
     */
    public final void setProperty(ApplContext ac, CssProperty property, Warnings warnings) {
        this.warnings = warnings;
        property.addToStyle(ac, this);
    }

    /**
     * Find conflicts in this Style
     *
     * @param warnings     For warnings reports.
     * @param allSelectors All contexts is the entire style sheet.
     */
    public void findConflicts(ApplContext ac, Warnings warnings,
                              CssSelectors selector, CssSelectors[] allSelectors) {
        // nothing to do
    }
}
