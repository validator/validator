package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class UnfinishedElementException extends AbstractValidationException {

    public UnfinishedElementException(Locator locator,
            Name currentElement, Name parent) {
        super(localizer.message("unfinished_element", NameFormatter.format(currentElement)), locator, currentElement, parent);
    }

}
