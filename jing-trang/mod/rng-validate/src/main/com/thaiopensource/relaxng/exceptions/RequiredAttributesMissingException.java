package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class RequiredAttributesMissingException extends AbstractValidationException {

    public RequiredAttributesMissingException(Locator locator,
            Name currentElement, Name parent) {
        super(localizer.message("required_attributes_missing", NameFormatter.format(currentElement)), locator, currentElement, parent);
    }

}
