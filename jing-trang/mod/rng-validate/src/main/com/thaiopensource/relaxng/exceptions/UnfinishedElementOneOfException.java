package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

import java.util.Set;

public class UnfinishedElementOneOfException extends AbstractValidationException {

    private Set<String> missingElementNames;

    public UnfinishedElementOneOfException(Locator locator,
            Name currentElement, Set<String> missingElementNames, Name parent) {
        super(localizer.message("unfinished_element_one_of", NameFormatter.format(currentElement), missingElementNames.toString()), locator, currentElement, parent);
        this.missingElementNames = missingElementNames;
    }

    public Set<String> getMissingElementNames() {
      return missingElementNames;
    }

}
