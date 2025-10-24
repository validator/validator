//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import java.util.ArrayList;

/**
 * This class is used by the CSS1 parser to generate all expressions.
 *
 * @version $Revision$
 */
public class CssExpression implements CssOperator {

    private ArrayList<ValueOperator> items = new ArrayList<ValueOperator>();
    private int count = 0;
    private int index = 0;
    private int mark = -1;

    private boolean vendor_extension = false;
    private boolean css_hack = false;
    private boolean css_variable = false;

    public boolean hasVendorExtensions() {
        return vendor_extension;
    }

    public void markVendorExtension() {
        vendor_extension = true;
    }

    public boolean hasCssHack() {
        return css_hack;
    }

    public void markCssHack() {
        css_hack = true;
    }

    public boolean hasCssVariable() {
        return css_variable;
    }

    public void markCssVariable() {
        css_variable = true;
    }
    
    /**
     * mark the current position, it can be set to this
     * position later by using reset
     */
    public void mark() {
        mark = index;
    }

    /**
     * reset to the marked location
     * of the start if nothing has been marked.
     */
    public void reset() {
        if (mark >= 0) {
            index = mark;
        } else {
            index = 0;
        }
    }

    /**
     * Add a value to the end of the expression
     * By default the next operator is a space
     *
     * @param value The value to append
     */
    public void addValue(CssValue value) {
        items.add(new ValueOperator(value));
        count++;
    }

    /**
     * Change the next operator
     * Don't check if the operator is correct
     *
     * @param operator The operator
     * @see CssOperator
     */
    public void setOperator(char operator) {
        (items.get(count - 1)).operator = operator;
    }

    /**
     * Change the next operator for the current position
     * Don't check if the operator is correct
     *
     * @param operator The operator
     * @see CssOperator
     */
    public void setCurrentOperator(char operator) {
        (items.get(index)).operator = operator;
    }

    /**
     * Returns the current value of the expression
     * don't change the position in the expression
     */
    public CssValue getValue() {
        if (index == count) {
            return null;
        } else {
            return (items.get(index)).value;
        }
    }

    /**
     * Returns the current value of the expression
     * don't change the position in the expression
     */
    public CssValue getNextValue() {
        if (index + 1 >= count) {
            return null;
        } else {
            return (items.get(index + 1)).value;
        }
    }

    /**
     * Returns the last value in the expression
     * without changing the position in the expression
     */
    public CssValue getLastValue() {
        if (count <= 0) {
            return null;
        } else {
            return items.get(count - 1).value;
        }
    }

	/* Modified by Sijtsche de Jong */

    /**
     * Returns the operator <strong>after</strong> the current value
     * don't change the position in the expression
     */
    public char getOperator() {
        if (index == count) {
            return SPACE;
        } else {
            return (items.get(index)).operator;
        }
    }

    /**
     * Returns the number of elements
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the number of remaining elements
     */
    public int getRemainingCount() {
        return count - index;
    }

    /**
     * Insert the current value at the current position.
     *
     * @param value The value to insert
     */
    public void insert(CssValue value) {
        items.add(index, new ValueOperator(value));
        count++;
    }

    /**
     * Removes the current value and his operator
     */
    public void remove() {
        if (index != count) {
            items.remove(index);
        }
        count--;
    }

    /**
     * @return true if there is no other element
     */
    public boolean end() {
        return (index == count);
    }

    /**
     * Change the position to the beginning
     */
    public void starts() {
        index = 0;
    }

    /**
     * Change the position to the end
     */
    public void ends() {
        index = count;
    }

    /**
     * Change the position to the next
     */
    public void next() {
        if (index < count) {
            index++;
        }
    }

    /**
     * Change the position to the precedent
     */
    public void precedent() {
        if (index > 0)
            index--;
    }

    /**
     * Returns a string representation of the object from the current position.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = index; i < count; i++) {
            ValueOperator vo = items.get(i);
            sb.append(vo.value.toString()).append(vo.operator);
        }
        // remove the last extra operator
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
            return sb.toString();
        } else {
            return "**invalid state**";
        }
    }

    /**
     * Returns a string representation of the object before the current
     * position.
     */
    public String toStringFromStart() {
        StringBuilder sb = new StringBuilder();
        for (ValueOperator anItem : items) {
            sb.append(anItem.value.toString()).append(anItem.operator);
        }
        // care for the last extra operator
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private class ValueOperator {
        CssValue value;
        char operator;

        ValueOperator(CssValue value) {
            this.value = value;
            this.operator = SPACE;
        }
    }
}
