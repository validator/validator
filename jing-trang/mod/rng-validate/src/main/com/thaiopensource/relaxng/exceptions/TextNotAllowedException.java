package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class TextNotAllowedException extends AbstractValidationException {

    public TextNotAllowedException(Locator locator,
            Name currentElement) {
        super(localizer.message("text_not_allowed", NameFormatter.format(currentElement)), locator, currentElement, null);
    }

}
