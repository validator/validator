package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

import java.util.Set;

public class RequiredAttributesMissingOneOfException extends AbstractValidationException {

    Set<String> attributeLocalNames;

    public RequiredAttributesMissingOneOfException(Locator locator,
          Name currentElement, Set<String> attributeLocalNames, Name parent) {
      super(localizer.message("required_attributes_missing_one_of", NameFormatter.format(currentElement), attributeLocalNames.toString()), locator, currentElement, parent);
      this.attributeLocalNames = attributeLocalNames;
    }

    public Set<String> getAttributeLocalNames() {
      return attributeLocalNames;
    }
}
