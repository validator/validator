//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Messages;

/**
 * This class represents an unknown error during the parse.
 *
 * @version $Revision$
 */
public class CssError {

    static final String parserError = "generator.unrecognize";

    /**
     * The source file
     */
    String sourceFile;

    /**
     * The beginLine number in the file
     */
    int beginLine;

    /**
     * The beginColumn number in the file
     */
    int beginColumn;

    /**
     * The endLine number in the file
     */
    int endLine;

    /**
     * The endColumn number in the file
     */
    int endColumn;

    /**
     * The line number in the file
     */
    int line;

    /**
     * The error type, taken from
     * the error, if defined.
     */
    String type = null;
    /**
     * The unknown error
     */
    Throwable error;

    /**
     * Create a new CssError
     */
    public CssError() {
    }

    /**
     * Create a new CssError with begin-end lines-columns range
     *
     * @param sourceFile  The source file
     * @param beginLine   The error beginning line number
     * @param beginColumn The error beginning column number
     * @param endLine     The error end line number
     * @param endColumn   The error end column number
     * @param error       The exception
     */
    public CssError(String sourceFile, int beginLine, int beginColumn,
            int endLine, int endColumn, Throwable error) {
        this.sourceFile = sourceFile;
        this.line = -1;
        this.beginLine = beginLine;
        this.beginColumn = beginColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.error = error;
    }

    /**
     * Create a new CssError
     *
     * @param sourceFile The source file
     * @param line       The error line number
     * @param error      The exception
     */
    public CssError(String sourceFile, int line, Throwable error) {
        this.sourceFile = sourceFile;
        this.line = line;
        this.error = error;
    }

    /**
     * Create a new CssError
     *
     * @param error The exception
     */
    public CssError(Throwable error) {
        this.error = error;
    }

    /**
     * Get the source file
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Get the source file
     */
    public String getSourceFileEscaped() {
        return Messages.escapeString(sourceFile);
    }


    /**
     * get the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * get the beginLine number
     */
    public int getBeginLine() {
        return beginLine;
    }

    /**
     * get the beginColumn number
     */
    public int getBeginColumn() {
        return beginColumn;
    }

    /**
     * get the endLine number
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * get the endColumn number
     */
    public int getEndColumn() {
        return endColumn;
    }

    /**
     * get the unknown error
     */
    public Throwable getException() {
        return error;
    }

    /**
     * get the error type, null if undefined
     */
    public String getType() {
        if (type == null) {
            if (error == null) {
                return null;
            }
            if (error instanceof InvalidParamException) {
                InvalidParamException exception = (InvalidParamException) error;
                type = exception.getErrorType();
            } else if (error instanceof CssParseException) {
                type = ((CssParseException) error).getErrorType();
            } else {
                type = error.getClass().getName();
            }
        }
        return type;
    }
}

