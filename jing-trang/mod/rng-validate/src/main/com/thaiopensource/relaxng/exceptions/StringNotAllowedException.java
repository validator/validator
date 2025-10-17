package com.thaiopensource.relaxng.exceptions;

import java.util.Iterator;
import java.util.Map;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class StringNotAllowedException extends AbstractValidationException {

    private static String formatMessage(Name currentElement, String value, Map exceptions) {
        if (exceptions.isEmpty()) {
            Object[] values = new Object[3];
            values[0] = NameFormatter.format(currentElement);
            values[1] = value;
            return localizer.message("string_not_allowed", values);
        } else {
            StringBuffer sb = new StringBuffer();
            for (Iterator iter = exceptions.keySet().iterator(); iter.hasNext();) {
                String msg = (String) iter.next();
                sb.append(' ');
                sb.append(msg);
            }
            Object[] values = new Object[4];
            values[0] = NameFormatter.format(currentElement);
            values[1] = value;
            values[2] = sb;
            return localizer.message("string_not_allowed_explain", values);
        }
    }

    private final String value;
    
    private final Map exceptions;
    
    public StringNotAllowedException(Locator locator,
            Name currentElement, Name parent, String value, Map exceptions) {
        super(formatMessage(currentElement, value, exceptions), locator, currentElement, parent);
        this.value = value;
        this.exceptions = exceptions;
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the exceptions.
     * 
     * @return the exceptions
     */
    public Map getExceptions() {
        return exceptions;
    }

}
