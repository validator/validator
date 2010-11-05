package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

import java.util.Set;

public class RequiredElementsMissingOneOfException extends AbstractValidationException {

    private Set<String> missingElementNames;

    public RequiredElementsMissingOneOfException(Locator locator,
            Name currentElement, Set<String> missingElementNames, Name parent) {
        // XXX can parent ever be null with this error?
        super(parent == null ? localizer.message("required_elements_missing_one_of", NameFormatter.format(currentElement), missingElementNames.toString()) : localizer.message("required_elements_missing_one_of_parent", NameFormatter.format(parent), missingElementNames.toString()), locator, currentElement, parent);
        this.missingElementNames = missingElementNames;
    }

    public Set<String> getMissingElementNames() {
      return missingElementNames;
    }

}
