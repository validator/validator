package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class RequiredElementsMissingException extends AbstractValidationException {

    private String missingElementName;

    public RequiredElementsMissingException(Locator locator,
            Name currentElement, String missingElementName, Name parent) {
        // XXX can parent ever be null with this error?
        super(parent == null ? localizer.message("required_elements_missing", NameFormatter.format(currentElement), missingElementName) : localizer.message("required_elements_missing_parent", NameFormatter.format(parent), missingElementName), locator, currentElement, parent);
        this.missingElementName = missingElementName;
    }

    public String getMissingElementName() {
      return missingElementName;
    }

}
