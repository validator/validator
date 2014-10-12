/*
 * Copyright (c) 2007 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.whattf.datatype;

import org.relaxng.datatype.DatatypeException;

/**
 * 
 * @version $Id$
 * @author hsivonen
 */
public class Html5DatatypeException extends DatatypeException {

    private Class datatypeClass;
    
    private String[] segments;

    /** To flag datatype exceptions that are handled as warnings */
    final boolean warning;

    public Html5DatatypeException(int index, Class datatypeClass, String datatypeName, String message) {
        super(index, "Bad " + datatypeName + ": " + message);
        this.datatypeClass = datatypeClass;
        this.segments = new String[1];
        this.segments[0] = message;
        this.warning = false;
    }

    public Html5DatatypeException(int index, Class datatypeClass, String datatypeName, String head, String literal, String tail) {
        super(index, "Bad " + datatypeName + ": " + head + '\u201C' + literal + '\u201D' + tail);
        this.datatypeClass = datatypeClass;
        this.segments = new String[3];
        this.segments[0] = head;
        this.segments[1] = literal;
        this.segments[2] = tail;
        this.warning = false;
    }

    public Html5DatatypeException(Class datatypeClass, String datatypeName, String message) {
        this(-1, datatypeClass, datatypeName, message);
    }

    public Html5DatatypeException(Class datatypeClass, String datatypeName, String head, String literal, String tail) {
        this(-1, datatypeClass, datatypeName, head, literal, tail);
    }

    /* for datatype exceptions that are handled as warnings, the following are
     * alternative forms of all the above, with an additional "warning" parameter */

    public Html5DatatypeException(int index, Class datatypeClass, String datatypeName, String message, boolean warning) {
        super(index, "Bad " + datatypeName + ": " + message);
        this.datatypeClass = datatypeClass;
        this.segments = new String[1];
        this.segments[0] = message;
        this.warning = warning;
    }

    public Html5DatatypeException(int index, Class datatypeClass, String datatypeName, String head, String literal, String tail, boolean warning) {
        super(index, "Bad " + datatypeName + ": " + head + '\u201C' + literal + '\u201D' + tail);
        this.datatypeClass = datatypeClass;
        this.segments = new String[3];
        this.segments[0] = head;
        this.segments[1] = literal;
        this.segments[2] = tail;
        this.warning = warning;
    }

    public Html5DatatypeException(Class datatypeClass, String datatypeName, String message, boolean warning) {
        this(-1, datatypeClass, datatypeName, message, warning);
    }

    public Html5DatatypeException(Class datatypeClass, String datatypeName, String head, String literal, String tail, boolean warning) {
        this(-1, datatypeClass, datatypeName, head, literal, tail, warning);
    }
    
    /**
     * Returns the datatypeClass.
     * 
     * @return the datatypeClass
     */
    public Class getDatatypeClass() {
        return datatypeClass;
    }

    /**
     * Returns the segments.
     * 
     * @return the segments
     */
    public String[] getSegments() {
        return segments;
    }

   /** 
     * Returns true if the datatype exception should be handled as a warning, false otherwise.
     *
     * @return true if the datatype exception should be handled as a warning, false otherwise.
     */
    public boolean isWarning()
    {   
      return warning;
    }   

}
