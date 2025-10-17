package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class ImpossibleAttributeIgnoredException extends AbstractValidationException {

    private final Name attributeName;
    
    public ImpossibleAttributeIgnoredException(Locator locator,
            Name currentElement, Name parent, Name attributeName) {
        super(localizer.message("impossible_attribute_ignored", NameFormatter.format(attributeName), NameFormatter.format(currentElement)), locator, currentElement, parent);
        this.attributeName = attributeName;
    }

    /**
     * Returns the attributeName.
     * 
     * @return the attributeName
     */
    public Name getAttributeName() {
        return attributeName;
    }
}
