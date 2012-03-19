/*
 * Copyright (c) 2011 Mozilla Foundation
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

package org.whattf.datatype;

import java.util.Arrays;

import org.relaxng.datatype.DatatypeException;

public class MetaName extends AbstractDatatype {

    private static final String[] VALID_NAMES = {
        "alexaverifyid", // extension
        "apple-mobile-web-app-capable", // extension
        "apple-mobile-web-app-status-bar-style", // extension
        "application-name",
        "author",
        "baiduspider", // extension
        "csrf-param", // extension
        "csrf-token", // extension
        "dc.date.issued", // extension
        "dc.language", // extension
        "dcterms.abstract", // extension
        "dcterms.accessrights", // extension
        "dcterms.accrualmethod", // extension
        "dcterms.accrualperiodicity", // extension
        "dcterms.accrualpolicy", // extension
        "dcterms.alternative", // extension
        "dcterms.audience", // extension
        "dcterms.available", // extension
        "dcterms.bibliographiccitation", // extension
        "dcterms.conformsto", // extension
        "dcterms.contributor", // extension
        "dcterms.coverage", // extension
        "dcterms.created", // extension
        "dcterms.creator", // extension
        "dcterms.date", // extension
        "dcterms.dateaccepted", // extension
        "dcterms.datecopyrighted", // extension
        "dcterms.datesubmitted", // extension
        "dcterms.description", // extension
        "dcterms.educationlevel", // extension
        "dcterms.extent", // extension
        "dcterms.format", // extension
        "dcterms.hasformat", // extension
        "dcterms.haspart", // extension
        "dcterms.hasversion", // extension
        "dcterms.identifier", // extension
        "dcterms.instructionalmethod", // extension
        "dcterms.isformatof", // extension
        "dcterms.ispartof", // extension
        "dcterms.isreferencedby", // extension
        "dcterms.isreplacedby", // extension
        "dcterms.isrequiredby", // extension
        "dcterms.issued", // extension
        "dcterms.isversionof", // extension
        "dcterms.language", // extension
        "dcterms.license", // extension
        "dcterms.mediator", // extension
        "dcterms.medium", // extension
        "dcterms.modified", // extension
        "dcterms.provenance", // extension
        "dcterms.publisher", // extension
        "dcterms.references", // extension
        "dcterms.relation", // extension
        "dcterms.replaces", // extension
        "dcterms.requires", // extension
        "dcterms.rights", // extension
        "dcterms.rightsholder", // extension
        "dcterms.source", // extension
        "dcterms.spatial", // extension
        "dcterms.subject", // extension
        "dcterms.tableofcontents", // extension
        "dcterms.temporal", // extension
        "dcterms.title", // extension
        "dcterms.type", // extension
        "dcterms.valid", // extension
        "description",
        "designer", // extension
        "es.title", // extension
        "essaydirectory", // extension
        "format-detection", // extension
        "generator",
        "geo.country", // extension
        "geo.placename", // extension
        "geo.position", // extension
        "geo.region", // extension
        "globrix.bathrooms", // extension
        "globrix.bedrooms", // extension
        "globrix.condition", // extension
        "globrix.features", // extension
        "globrix.instruction", // extension
        "globrix.latitude", // extension
        "globrix.longitude ", // extension
        "globrix.outsidespace", // extension
        "globrix.parking", // extension
        "globrix.period", // extension
        "globrix.poa", // extension
        "globrix.postcode", // extension
        "globrix.price", // extension
        "globrix.priceproximity", // extension
        "globrix.tenure", // extension
        "globrix.type", // extension
        "globrix.underoffer", // extension
        "google-site-verification", // extension
        "googlebot", // extension
        "handheldfriendly", // extension
        "icbm", // extension        
        "itemsperpage", // extension
        "keywords",
        "meta_date", // extension
        "mobileoptimized", // extension
        "msapplication-navbutton-color", // extension
        "msapplication-starturl", // extension
        "msapplication-task", // extension
        "msapplication-tooltip", // extension
        "msapplication-window", // extension
        "msvalidate.01", // extension
        "norton-safeweb-site-verification", // extension
        "publisher", //extension
        "rating", // extension
        "referrer", // extension
        "review_date", // extension
        "revisit-after", // extension
        "rights-standard", // extension
        "robots", // extension
        "slurp", // extension
        "startindex", // extension
        "teoma", // extension
        "totalresults", // extension
        "verify-v1", // extension
        "viewport", // extension
        "wt.ac", // extension
        "wt.ad", // extension
        "wt.cg_n", // extension
        "wt.cg_s", // extension
        "wt.mc_id", // extension
        "wt.sv", // extension
        "wt.ti", // extension
        "y_key", // extension
        "yandex-verification" // extension
    };
    
    /**
     * The singleton instance.
     */
    public static final MetaName THE_INSTANCE = new MetaName();
    
    /**
     * Package-private constructor
     */
    private MetaName() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        String token = toAsciiLowerCase(literal);
        if (Arrays.binarySearch(VALID_NAMES, token) < 0) {
            throw newDatatypeException("Keyword ", token, " is not registered.");
        }
    }

    @Override public String getName() {
        return "metadata name";
    }

}
