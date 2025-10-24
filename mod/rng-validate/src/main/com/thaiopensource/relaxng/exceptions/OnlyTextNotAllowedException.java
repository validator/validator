package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class OnlyTextNotAllowedException extends AbstractValidationException {

    public OnlyTextNotAllowedException(Locator locator,
            Name currentElement, Name parent) {
        super(localizer.message("only_text_not_allowed", NameFormatter.format(currentElement)), locator, currentElement, parent);
    }

}
