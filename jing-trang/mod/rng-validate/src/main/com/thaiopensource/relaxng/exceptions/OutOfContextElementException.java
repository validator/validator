package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class OutOfContextElementException extends AbstractValidationException {

    public OutOfContextElementException(Locator locator,
            Name currentElement, Name parent) {
        super(parent == null ? localizer.message("out_of_context_element", NameFormatter.format(currentElement)) : localizer.message("out_of_context_element_parent", NameFormatter.format(currentElement), NameFormatter.format(parent)), locator, currentElement, parent);
    }

}
