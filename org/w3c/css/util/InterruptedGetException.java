// InterruptedGetException.java
// $Id$
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.util;

import java.io.IOException;

/**
 * Thrown when a HTTP Get is interrupted
 *
 * @author Benoit Mahe (bmahe@w3.org)
 * @version $Revision$
 */
public class InterruptedGetException extends IOException {

    public long bytesTransferred = 0;
    public long bytesExpected = 0;

    public InterruptedGetException(String message) {
        super(message);
    }

}
