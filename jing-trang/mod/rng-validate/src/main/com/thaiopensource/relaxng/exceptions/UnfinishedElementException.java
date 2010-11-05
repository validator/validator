package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class UnfinishedElementException extends AbstractValidationException {

    private String missingElementName;

    public UnfinishedElementException(Locator locator,
            Name currentElement, String missingElementName, Name parent) {
        super(localizer.message("unfinished_element", NameFormatter.format(currentElement), missingElementName), locator, currentElement, parent);
        this.missingElementName = missingElementName;
    }

    public String getMissingElementName() {
      return missingElementName;
    }

}
