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

import nu.validator.saxtree.DocumentFragment;

import com.thaiopensource.xml.util.Name;

public final class EmptySpec extends Spec {

    public static final EmptySpec THE_INSTANCE = new EmptySpec();
    
    /**
     * @param urisByElement
     * @param contextsByElement
     * @param contentModelsByElement
     * @param attributesByElement
     */
    private EmptySpec() {
        super(null, null, null, null);
    }

    /**
     * @see nu.validator.spec.Spec#contentModelDescription(com.thaiopensource.xml.util.Name)
     */
    @Override
    public DocumentFragment contentModelDescription(Name element) {
        return null;
    }

    /**
     * @see nu.validator.spec.Spec#contextDescription(com.thaiopensource.xml.util.Name)
     */
    @Override
    public DocumentFragment contextDescription(Name element) {
        return null;
    }

    /**
     * @see nu.validator.spec.Spec#elementLink(com.thaiopensource.xml.util.Name)
     */
    @Override
    public String elementLink(Name element) {
        return null;
    }

    /**
     * @see nu.validator.spec.Spec#elementSpecificAttributesDescription(com.thaiopensource.xml.util.Name)
     */
    @Override
    public DocumentFragment elementSpecificAttributesDescription(Name element) {
        return null;
    }

    
    
}
