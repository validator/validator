package org.w3c.css.atrules.css.media;

import org.w3c.css.css.StyleSheetOrigin;
import org.w3c.css.parser.CssError;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssValue;

public abstract class MediaFeature implements StyleSheetOrigin {
    public CssValue value;

    /**
     * The origin of this property.
     * the author's style sheets override the reader's style sheet which
     * override the UA's default values. An imported style sheet has the same
     * origin as the style sheet from which it is imported.
     *
     * @see StyleSheetOrigin#BROWSER
     * @see StyleSheetOrigin#READER
     * @see StyleSheetOrigin#AUTHOR
     */
    public int origin;

    /**
     * the position of the first character of this value.
     */
    public int line;

    /**
     * the origin file.
     */
    public String sourceFile;

    public String combinator = null;


    public abstract boolean equals(MediaFeature other);

    public abstract String getFeatureName();


    /**
     * Set the combinator used in more complex expressions.
     *
     * @param combinator, the combinator string (and, or etc...)
     */
    public void setCombinator(String combinator) {
        this.combinator = combinator;
    }

    /**
     * Get the combinator string if present, null otherwise
     *
     * @return a String
     */
    public String getCombinator() {
        return combinator;
    }

    public void reportDeprecatedMediaFeature(ApplContext ac, String modifier) {
        String feature;
        if (modifier != null) {
            feature = String.format("%s-%s", modifier, getFeatureName());
        } else {
            feature = getFeatureName();
        }
        CssError err = new CssError(new InvalidParamException("deprecatedmediafeature",
                feature, ac));
        ac.getFrame().addError(err);
    }

    /**
     * Update the source file and the line.
     * Overrides this method for a macro
     *
     * @param line   The line number where this property is defined
     * @param source The source file where this property is defined
     */
    public void setInfo(int line, String source) {
        this.line = line;
        this.sourceFile = source;
    }

    /**
     * Fix the origin of this property
     * Overrides this method for a macro
     *
     * @param origin, an <EM>int</EM>
     * @see #BROWSER
     * @see #READER
     * @see #AUTHOR
     */
    public void setOrigin(int origin) {
        this.origin = origin;
    }

    /**
     * Returns the attribute origin
     *
     * @return the value of the attribute
     */
    public int getOrigin() {
        return origin;
    }

    /**
     * Returns the source file.
     */
    public final String getSourceFile() {
        return sourceFile;
    }

    /**
     * Returns the line number in the source file.
     */
    public final int getLine() {
        return line;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (value == null) {
            return getFeatureName();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getFeatureName());
        sb.append(':').append(' ').append(value.toString());
        return sb.toString();
    }
}
