package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;

import com.thaiopensource.relaxng.impl.NameFormatter;
import com.thaiopensource.xml.util.Name;

public class RequiredAttributesMissingException extends AbstractValidationException {

    private String attributeLocalName;

    public RequiredAttributesMissingException(Locator locator,
            Name currentElement, String attributeLocalName, Name parent) {
        super(localizer.message("required_attributes_missing", NameFormatter.format(currentElement), attributeLocalName), locator, currentElement, parent);
        this.attributeLocalName = attributeLocalName;
    }

    public String getAttributeLocalName() {
      return attributeLocalName;
    }
}
