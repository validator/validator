/*
 * Copyright (c) 2011-2014 Mozilla Foundation
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
        "aglsterms.act", // extension
        "aglsterms.accessibility", // extension
        "aglsterms.accessmode", // extension
        "aglsterms.aggregationlevel", // extension
        "aglsterms.availability", // extension
        "aglsterms.case", // extension
        "aglsterms.category", // extension
        "aglsterms.datelicensed", // extension
        "aglsterms.documenttype", // extension
        "aglsterms.function", // extension
        "aglsterms.isbasisfor", // extension
        "aglsterms.isbasedon", // extension
        "aglsterms.jurisdiction", // extension
        "aglsterms.mandate", // extension
        "aglsterms.protectivemarking", // extension
        "aglsterms.regulation", // extension
        "aglsterms.servicetype", // extension
        "alexaverifyid", // extension
        "apple-mobile-web-app-capable", // extension
        "apple-mobile-web-app-status-bar-style", // extension
        "application-name",
        "author",
        "baiduspider", // extension
        "bug.component", // extension
        "bug.product", // extension
        "bug.short_desc", // extension
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
        "essaydirectory", // extension
        "format-detection", // extension
        "fragment", // extension
        "generator",
        "geo.a1", // extension
        "geo.a2", // extension
        "geo.a3", // extension
        "geo.country", // extension
        "geo.lmk", // extension
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
        "icbm", // extension        
        "itemsperpage", // extension
        "keywords",
        "meta_date", // extension
        "mobile-web-app-capable", // extension
        "msapplication-config", // extension
        "msapplication-navbutton-color", // extension
        "msapplication-starturl", // extension
        "msapplication-task", // extension
        "msapplication-tilecolor", // extension
        "msapplication-tileimage", // extension
        "msapplication-tooltip", // extension
        "msapplication-window", // extension
        "msvalidate.01", // extension
        "norton-safeweb-site-verification", // extension
        "rating", // extension
        "referrer", // extension
        "review_date", // extension
        "revisit-after", // extension
        "rights-standard", // extension
        "robots", // extension
        "skype_toolbar", // extension
        "slurp", // extension
        "startindex", // extension
        "startver", // extension
        "teoma", // extension
        "twitter:app:country ", // extension
        "twitter:app:id:googleplay", // extension
        "twitter:app:id:ipad ", // extension
        "twitter:app:id:iphone", // extension
        "twitter:app:url:googleplay", // extension
        "twitter:app:url:ipad", // extension
        "twitter:app:url:iphone", // extension
        "twitter:card", // extension
        "twitter:creator", // extension
        "twitter:creator:id", // extension
        "twitter:description", // extension
        "twitter:domain", // extension
        "twitter:image", // extension
        "twitter:image0", // extension
        "twitter:image1", // extension
        "twitter:image2", // extension
        "twitter:image3", // extension
        "twitter:image:height", // extension
        "twitter:image:src", // extension
        "twitter:image:width", // extension
        "twitter:site", // extension
        "twitter:site:id", // extension
        "twitter:title", // extension
        "twitter:url", // extension
        "verify-v1", // extension
        "viewport", // extension
        "wot-verification", // extension
        "wt.ac", // extension
        "wt.ad", // extension
        "wt.cg_n", // extension
        "wt.cg_s", // extension
        "wt.mc_id", // extension
        "wt.si_p", // extension
        "wt.sv", // extension
        "wt.ti", // extension
        "y_key", // extension
        "yandex-verification", // extension
        "zoomcategory", // extension
        "zoomimage", // extension
        "zoompageboost", // extension
        "zoomtitle", // extension
        "zoomwords" // extension
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
