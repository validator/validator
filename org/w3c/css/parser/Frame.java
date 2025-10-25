//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Warning;
import org.w3c.css.util.Warnings;

/**
 * @version $Revision$
 */
public class Frame {

    public ApplContext ac;
    private Errors errors;
    private Warnings warnings;
    private CssFouffa cssFouffa;

    private String sourceFile;
    private int line;

    /**
     * Create a new Frame.
     *
     * @param cssFouffa  The current parser.
     * @param sourceFile The name of the source file.
     */
    public Frame(CssFouffa cssFouffa, String sourceFile, int warningLevel) {
        this.sourceFile = sourceFile;
        this.cssFouffa = cssFouffa;
        errors = new Errors();
        warnings = new Warnings(warningLevel);
    }

    /**
     * Create a new Frame with a line number.
     *
     * @param cssFouffa  The current parser.
     * @param sourceFile The name of the source file.
     * @param beginLine  The begin line
     */
    public Frame(CssFouffa cssFouffa, String sourceFile, int beginLine,
                 int warningLevel) {
        this(cssFouffa, sourceFile, warningLevel);
        line = beginLine;
    }

    /**
     * Adds an error to this frame.
     *
     * @param error The new error.
     */
    public void addError(CssError error) {
        error.sourceFile = getSourceFile();
        error.line = getLine();
        errors.addError(error);
    }

    /**
     * Returns all errors.
     */
    public Errors getErrors() {
        return errors;
    }

    /**
     * Adds a warning to this frame.
     *
     * @param warningMessage the warning message
     *                       (see org.w3c.css.util.Messages.properties).
     * @see org.w3c.css.util.Warning
     */
    public void addWarning(String warningMessage) {
        warnings.addWarning(new Warning(getSourceFile(), getLine(),
                warningMessage, 0, ac));
    }

    /**
     * Adds a warning to this frame.
     *
     * @param warningMessage the warning message
     *                       (see org.w3c.css.util.Messages.properties).
     * @see org.w3c.css.util.Warning
     */
    public void addWarning(String warningMessage, int level) {
        warnings.addWarning(new Warning(getSourceFile(), getLine(),
                warningMessage, level, ac));
    }

    /**
     * Adds a warning to this frame with a message.
     *
     * @param warningMessage the warning message
     *                       (see org.w3c.css.util.Messages.properties).
     * @param message        An add-on message.
     * @see org.w3c.css.util.Warning
     */
    public void addWarning(String warningMessage, String message) {
        warnings.addWarning(new Warning(getSourceFile(), getLine(),
                warningMessage, 0,
                new String[]{message}, ac));
    }

    /**
     * Adds a warning to this frame with a message.
     *
     * @param warningMessage the warning message
     *                       (see org.w3c.css.util.Messages.properties).
     * @param param1         An add-on message.
     * @param param2         An add-on message.
     * @see org.w3c.css.util.Warning
     */
    public void addWarning(String warningMessage, String param1, String param2) {
        warnings.addWarning(new Warning(getSourceFile(), getLine(),
                warningMessage, 0,
                new String[]{param1, param2}, ac));
    }

    /**
     * Adds a warning to this frame with a message.
     *
     * @param warningMessage the warning message
     *                       (see org.w3c.css.util.Messages.properties).
     * @param messages       Some add-on messages.
     * @see org.w3c.css.util.Warning
     */
    public void addWarning(String warningMessage, String[] messages) {
        warnings.addWarning(new Warning(getSourceFile(), getLine(),
                warningMessage, 0, messages, ac));
    }

    /**
     * Get all warnings.
     */
    public Warnings getWarnings() {
        return warnings;
    }

    /**
     * Get the name of the source file.
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Get the begin line.
     */
    public int getBeginLine() {
        return line;
    }

    /**
     * Get the current line.
     */
    public int getLine() {
        //return line; //+ cssFouffa.token.beginLine;
        return line + cssFouffa.token.beginLine;
    }

    /**
     * Merge two frames.
     *
     * @param frame The other frame for merging.
     */
    public void join(Frame frame) {
        errors.addErrors(frame.errors);
        warnings.addWarnings(frame.warnings);
    }
}

