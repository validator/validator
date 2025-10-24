// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

/**
 * PseudoFunction<br />
 * Created: Sep 2, 2005 4:04:45 PM<br />
 */
public class PseudoFunctionSelector implements Selector {

    private String name;
    private Object param;
    private boolean isElement = false;
    private String representation = null;

    /**
     * Creates a new empty function selector
     */
    public PseudoFunctionSelector() {

    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this pseudo-function
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the parameter of this pseudo-function.
     *
     * @return the parameter of this pseudo-function.
     */
    public Object getParam() {
        return param;
    }

    /**
     * Sets the parameter of this pseudo-function
     *
     * @param param The param to set.
     */
    public void setParam(Object param) {
        this.param = param;
    }

    public void setAsPseudoElement() {
        isElement = true;
    }

    public void setAsPseudoClass() {
        isElement = false;
    }
    /**
     * Returns the specifictiy of this pseudo-function
     *
     * @return
     */
    public int getSpecificity() {
        return 0;
    }

    /**
     * @see Selector#canApply(Selector)
     */
    public boolean canApply(Selector other) {
        return false;
    }

    /**
     * @see Selector#toString()
     */
    public String toString() {
        if (representation == null) {
            StringBuilder sb = new StringBuilder();
            if (isElement) {
                sb.append(':');
            }
            sb.append(':');
            sb.append(name);
            sb.append('(');
            sb.append(param);
            sb.append(')');
            representation = sb.toString();
        }
        return representation;
    }

    public String functionName() {
        StringBuilder sb = new StringBuilder();
        if (isElement) {
            sb.append(':');
        }
        sb.append(':');
        sb.append(name);
        sb.append('(');
        sb.append(')');
        return sb.toString();
    }
}
