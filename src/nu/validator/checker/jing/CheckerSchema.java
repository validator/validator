/*
 * Copyright (c) 2010-2018 Mozilla Foundation
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

package nu.validator.checker.jing;

import nu.validator.checker.Checker;
import nu.validator.checker.ConformingButObsoleteWarner;
import nu.validator.checker.DebugChecker;
import nu.validator.checker.LanguageDetectingChecker;
import nu.validator.checker.MicrodataChecker;
import nu.validator.checker.RdfaLiteChecker;
import nu.validator.checker.NormalizationChecker;
import nu.validator.checker.TextContentChecker;
import nu.validator.checker.UncheckedSubtreeWarner;
import nu.validator.checker.UsemapChecker;
import nu.validator.checker.XmlPiChecker;
import nu.validator.checker.schematronequiv.Assertions;
import nu.validator.checker.table.TableChecker;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.Validator;

public class CheckerSchema implements Schema {

    public static final CheckerSchema DEBUG_CHECKER = new CheckerSchema(PropertyMap.EMPTY, DebugChecker.class);
    
    public static final CheckerSchema NORMALIZATION_CHECKER = new CheckerSchema(PropertyMap.EMPTY, NormalizationChecker.class);
    
    public static final CheckerSchema TEXT_CONTENT_CHECKER = new CheckerSchema(PropertyMap.EMPTY, TextContentChecker.class);

    public static final CheckerSchema UNCHECKED_SUBTREE_WARNER = new CheckerSchema(PropertyMap.EMPTY, UncheckedSubtreeWarner.class);

    public static final CheckerSchema USEMAP_CHECKER = new CheckerSchema(PropertyMap.EMPTY, UsemapChecker.class);

    public static final CheckerSchema TABLE_CHECKER = new CheckerSchema(PropertyMap.EMPTY, TableChecker.class);

    public static final CheckerSchema ASSERTION_SCH = new CheckerSchema(PropertyMap.EMPTY, Assertions.class);
    
    public static final CheckerSchema CONFORMING_BUT_OBSOLETE_WARNER = new CheckerSchema(PropertyMap.EMPTY, ConformingButObsoleteWarner.class);
    
    public static final CheckerSchema XML_PI_CHECKER = new CheckerSchema(PropertyMap.EMPTY, XmlPiChecker.class);

    public static final CheckerSchema MICRODATA_CHECKER = new CheckerSchema(PropertyMap.EMPTY, MicrodataChecker.class);

    public static final CheckerSchema RDFALITE_CHECKER = new CheckerSchema(PropertyMap.EMPTY, RdfaLiteChecker.class);

    public static final CheckerSchema LANGUAGE_DETECTING_CHECKER = //
            new CheckerSchema(PropertyMap.EMPTY,
                    LanguageDetectingChecker.class);

    private final PropertyMap properties;
    
    private final Class<? extends Checker> klazz;
    
    /**
     * @param properties
     * @param klazz
     */
    public CheckerSchema(PropertyMap properties, Class<? extends Checker> klazz) {
        this.properties = properties;
        this.klazz = klazz;
    }

    @Override
    public Validator createValidator(PropertyMap props) {
        try {
            return new CheckerValidator(
                    klazz.getDeclaredConstructor().newInstance(), props);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PropertyMap getProperties() {
        return properties;
    }
}
