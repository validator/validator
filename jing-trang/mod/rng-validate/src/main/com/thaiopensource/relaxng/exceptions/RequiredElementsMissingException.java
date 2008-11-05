package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class RequiredElementsMissingException extends AbstractValidationException {

    public RequiredElementsMissingException(Locator locator,
            Name currentElement, Name parent) {
        // XXX can parent ever be null with this error?
        super(parent == null ? localizer.message("required_elements_missing") : localizer.message("required_elements_missing_parent", NameFormatter.format(parent)), locator, currentElement, parent);
    }

}
