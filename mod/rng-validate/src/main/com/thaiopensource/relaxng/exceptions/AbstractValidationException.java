package com.thaiopensource.relaxng.exceptions;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

import com.thaiopensource.relaxng.impl.SchemaBuilderImpl;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.xml.util.Name;

public abstract class AbstractValidationException extends SAXParseException {

    protected static final Localizer localizer = new Localizer(SchemaBuilderImpl.class);
    
    private final Name currentElement;
    private final Name parent;
    
    AbstractValidationException(String formattedMessage, Locator locator, Name currentElement, Name parent) {
        super(formattedMessage, locator);
        this.currentElement = currentElement;
        this.parent = parent;
    }

    /**
     * Returns the currentElement.
     * 
     * @return the currentElement
     */
    public Name getCurrentElement() {
        return currentElement;
    }

    /**
     * Returns the parent.
     * 
     * @return the parent
     */
    public Name getParent() {
        return parent;
    }
}
