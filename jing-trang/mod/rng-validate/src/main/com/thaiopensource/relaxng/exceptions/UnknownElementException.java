package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class UnknownElementException extends AbstractValidationException {

    public UnknownElementException(Locator locator,
            Name currentElement, Name parent) {
        super(parent == null ? localizer.message("unknown_element", NameFormatter.format(currentElement)) : localizer.message("unknown_element_parent", NameFormatter.format(currentElement), NameFormatter.format(parent)), locator, currentElement, parent);
    }

}
