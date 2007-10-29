/*
 * Copyright (c) 2007 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.spec;

import java.util.Map;

import nu.validator.saxtree.DocumentFragment;

import com.thaiopensource.xml.util.Name;

public class Spec {
    private final Map<Name, String> urisByElement;

    private final Map<Name, DocumentFragment> contextsByElement;

    private final Map<Name, DocumentFragment> contentModelsByElement;

    private final Map<Name, DocumentFragment> attributesByElement;
    
    /**
     * @param urisByElement
     * @param contextsByElement
     * @param contentModelsByElement
     * @param attributesByElement
     */
    public Spec(final Map<Name, String> urisByElement, final Map<Name, DocumentFragment> contextsByElement, final Map<Name, DocumentFragment> contentModelsByElement, final Map<Name, DocumentFragment> attributesByElement) {
        this.urisByElement = urisByElement;
        this.contextsByElement = contextsByElement;
        this.contentModelsByElement = contentModelsByElement;
        this.attributesByElement = attributesByElement;
    }

    public String elementLink(Name element) {
        return urisByElement.get(element);
    }
    
    public DocumentFragment contextDescription(Name element) {
        return contextsByElement.get(element);
    }
    
    public DocumentFragment contentModelDescription(Name element) {
        return contentModelsByElement.get(element);
    }

    public DocumentFragment elementSpecificAttributesDescription(Name element) {
        return attributesByElement.get(element);
    }

}
