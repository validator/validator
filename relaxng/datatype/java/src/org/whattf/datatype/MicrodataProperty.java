package org.whattf.datatype;

import org.relaxng.datatype.DatatypeException;

/**
 * This datatype shall accept absolute URLs or any string that does
 * not contain '.' or ':' characters.
 */
public class MicrodataProperty extends Iri {

    /**
     * The singleton instance.
     */
    public static final MicrodataProperty THE_INSTANCE = new MicrodataProperty();

    protected MicrodataProperty() {
        super();
    }

    @Override public String getName() {
        return "microdata property";
    }

    @Override public void checkValid(CharSequence literal) throws DatatypeException {
        int len = literal.length();
        for (int i = 0; i < len; i++) {
            char c = literal.charAt(i);
            if (c == '.' || c == ':') {
                super.checkValid(literal);
                break;
            }
        }
    }
}
