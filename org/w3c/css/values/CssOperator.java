//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

/**
 * Used by properties to verify the semantics
 */
public interface CssOperator {
    public static final char COMMA = ',';
    public static final char MINUS = '-';
    public static final char PLUS = '+';
    public static final char MUL = '*';
    public static final char DIV = '/';
    public static final char SPACE = ' ';
    public static final char EQUAL = '='; // used for vendor extensions
}
