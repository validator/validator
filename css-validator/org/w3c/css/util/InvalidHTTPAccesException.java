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

package org.w3c.css.util;


/**
 * InvalidAccesException is a runtime exception throwed when an acces is
 * impossible to a HTTP ressource.
 *
 * @author Philippe Le Hegaret
 * @version $Revision$
 */
public class InvalidHTTPAccesException extends InvalidAccesException {

    // HTTP reason
    int status;

    // HTTP requested uri
    String uri;

    // message
    String message;

    // additional informations
    Utf8Properties informations;

    /**
     * Creates a new InvalidHTTPAccesException
     */
    public InvalidHTTPAccesException(int status, String uri, String message,
                                     Utf8Properties informations) {
        super(message);
        this.status = status;
        this.uri = uri;
        this.message = message;
        this.informations = informations;
    }

    /**
     * Creates a new InvalidHTTPAccesException
     */
    public InvalidHTTPAccesException(int status, String uri, String message) {
        this(status, uri, message, null);
    }

    /**
     * Returns the HTTP reason of the failure NOT_FOUND, UNAUTHORIZED, ...
     */
    public int getHTTPReason() {
        return status;
    }

    /**
     * Get the requested URI
     */
    public String getURI() {
        return uri;
    }

    /**
     * Get the error message The message can come from the server or from the
     * application.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns some additionals HTTP information. These informations are useful
     * if you want to reply to the client For example, if the HTTP reason is
     * UNAUTHORIZED, it will contain the header WWW-Authenticate and
     * Authentication-Info.
     */
    public Utf8Properties getInformations() {
        return informations;
    }
}
