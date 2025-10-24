//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
// Updated September 20th 2000 Sijtsche de Jong (sy.de.jong@let.rug.nl)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

/**
 * @version $Revision$
 */
public interface CssSelectorsConstant {

    /**
     * [lang="fr"]
     */
    public static final int ATTRIBUTE_EXACT = ' ';
    /**
     * [lang~="fr"]
     */
    public static final int ATTRIBUTE_ONE_OF = '~';
    /**
     * class selector == like [lang~="fr"]
     */
    public static final int ATTRIBUTE_CLASS_SEL = '.';
    /**
     * [lang|="fr"]
     */
    public static final int ATTRIBUTE_BEGIN = '|';
    /**
     * [lang]
     */
    public static final int ATTRIBUTE_ANY = -1;
    /**
     * [foo^="bar"]
     */
    public static final int ATTRIBUTE_START = '^';
    /**
     * [foo$="bar"]
     */
    public static final int ATTRIBUTE_SUFFIX = '$';
    /**
     * [foo*="bar"]
     */
    public static final int ATTRIBUTE_SUBSTR = '*';

    /**
     * Maximun of ATTRIBUTE_ONE_OF
     */
    public static final int ATTRIBUTE_LENGTH = 10;

    // See https://www.w3.org/TR/2018/WD-selectors-4-20181121/#combinators

    public static final String DESCENDANT_COMBINATOR = " ";

    public static final String CHILD_COMBINATOR = ">";

    public static final String NEXT_SIBLING_COMBINATOR = "+";

    public static final String SUBSEQUENT_SIBLING_COMBINATOR = "~";

    public static final String COLUMN_COMBINATOR = "||";
}
