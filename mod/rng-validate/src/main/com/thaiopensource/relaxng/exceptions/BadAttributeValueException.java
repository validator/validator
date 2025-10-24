package com.thaiopensource.relaxng.exceptions;

import java.util.Iterator;
import java.util.Map;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class BadAttributeValueException extends AbstractValidationException {

    private static String formatMessage(Name currentElement, Name attributeName, String attributeValue, Map exceptions) {
        if (exceptions.isEmpty()) {
            Object[] values = new Object[3];
            values[0] = NameFormatter.format(attributeName);
            values[1] = NameFormatter.format(currentElement);
            values[2] = attributeValue;
            return localizer.message("bad_attribute_value", values);
        } else {
            StringBuffer sb = new StringBuffer();
            for (Iterator iter = exceptions.keySet().iterator(); iter.hasNext();) {
                String msg = (String) iter.next();
                sb.append(' ');
                sb.append(msg);
            }
            Object[] values = new Object[4];
            values[0] = NameFormatter.format(attributeName);
            values[1] = NameFormatter.format(currentElement);
            values[2] = attributeValue;
            values[3] = sb;
            return localizer.message("bad_attribute_value_explain", values);
        }
    }
    
    private final Name attributeName;
    
    private final String attributeValue;
    
    private final Map exceptions;
    
    public BadAttributeValueException(Locator locator,
            Name currentElement, Name parent, Name attributeName, String attributeValue, Map exceptions) {
        super(formatMessage(currentElement, attributeName, attributeValue, exceptions), locator, currentElement, parent);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.exceptions = exceptions;
    }

    /**
     * Returns the attributeName.
     * 
     * @return the attributeName
     */
    public Name getAttributeName() {
        return attributeName;
    }

    /**
     * Returns the attributeValue.
     * 
     * @return the attributeValue
     */
    public String getAttributeValue() {
        return attributeValue;
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
