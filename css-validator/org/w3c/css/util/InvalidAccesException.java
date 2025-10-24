/*
 * Copyright (c) 2000 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 *
 * $Id$
 */

/**
 * InvalidAccesException is a runtime exception throwed when an acces is
 * impossible to a ressource.
 *
 * @version $Revision$
 * @author Philippe Le Hegaret
 */

package org.w3c.css.util;

public class InvalidAccesException extends RuntimeException {

    /**
     * Creates a new InvalidAccesException
     */
    public InvalidAccesException() {
        super();
    }

    /**
     * Creates a new InvalidAccesException with a specified string
     */
    public InvalidAccesException(String s) {
        super(s);
    }
}
