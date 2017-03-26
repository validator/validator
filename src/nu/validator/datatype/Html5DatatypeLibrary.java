/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2007-2013 Mozilla Foundation
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

package nu.validator.datatype;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.helpers.ParameterlessDatatypeBuilder;

/**
 * Factory for HTML5 datatypes.
 * @version $Id$
 * @author hsivonen
 */
public class Html5DatatypeLibrary implements DatatypeLibrary {
    
    public Html5DatatypeLibrary() {
        super();
    }

    /**
     * Returns a <code>DatatypeBuilder</code> for a named datatype. This method is 
     * unnecessary for direct access. Just use <code>createDatatype</code>.
     * @param baseTypeLocalName the local name
     * @return a <code>ParameterlessDatatypeBuilder</code> for the local name
     * @see org.relaxng.datatype.DatatypeLibrary#createDatatypeBuilder(java.lang.String)
     */
    @Override
    public DatatypeBuilder createDatatypeBuilder(String baseTypeLocalName)
            throws DatatypeException {
        return new ParameterlessDatatypeBuilder(createDatatype(baseTypeLocalName));
    }

    /**
     * The factory method for the datatypes of this library.
     * @param typeLocalName the local name
     * @return a <code>Datatype</code> instance for the local name
     * @see org.relaxng.datatype.DatatypeLibrary#createDatatype(java.lang.String)
     */
    @Override
    public Datatype createDatatype(String typeLocalName)
            throws DatatypeException {
        if ("ID".equals(typeLocalName)) {
            return new Id();
        } else if ("IDREF".equals(typeLocalName)) {
            return Idref.THE_INSTANCE;
        } else if ("IDREFS".equals(typeLocalName)) {
            return Idrefs.THE_INSTANCE;
        } else if ("pattern".equals(typeLocalName)) {
            return Pattern.THE_INSTANCE;
        } else if ("datetime".equals(typeLocalName)) {
            return Datetime.THE_INSTANCE;
        } else if ("datetime-local".equals(typeLocalName)) {
            return DatetimeLocal.THE_INSTANCE;
        } else if ("datetime-tz".equals(typeLocalName)) {
            return DatetimeTz.THE_INSTANCE;
        } else if ("date-or-time".equals(typeLocalName)) {
            return DateOrTime.THE_INSTANCE;
        } else if ("date".equals(typeLocalName)) {
            return Date.THE_INSTANCE;
        } else if ("month".equals(typeLocalName)) {
            return Month.THE_INSTANCE;
        } else if ("week".equals(typeLocalName)) {
            return Week.THE_INSTANCE;
        } else if ("time".equals(typeLocalName)) {
            return Time.THE_INSTANCE;
        } else if ("iri".equals(typeLocalName)) {
            return Iri.THE_INSTANCE;
        } else if ("iri-ref".equals(typeLocalName)) {
            return IriRef.THE_INSTANCE;
        } else if ("iri-ref-http-or-https".equals(typeLocalName)) {
            return IriRefHttpOrHttps.THE_INSTANCE;
        } else if ("string".equals(typeLocalName)) {
            return AsciiCaseInsensitiveString.THE_INSTANCE;
        } else if ("language".equals(typeLocalName)) {
            return Language.THE_INSTANCE;
        } else if ("media-query".equals(typeLocalName)) {
            return MediaQuery.THE_INSTANCE;
        } else if ("media-condition".equals(typeLocalName)) {
            return MediaCondition.THE_INSTANCE;
        } else if ("mime-type".equals(typeLocalName)) {
            return MimeType.THE_INSTANCE;
        } else if ("browsing-context".equals(typeLocalName)) {
            return BrowsingContext.THE_INSTANCE;
        } else if ("browsing-context-or-keyword".equals(typeLocalName)) {
            return BrowsingContextOrKeyword.THE_INSTANCE;
        } else if ("hash-name".equals(typeLocalName)) {
            return HashName.THE_INSTANCE;
        } else if ("integer".equals(typeLocalName)) {
            return Int.THE_INSTANCE;
        } else if ("integer-non-negative".equals(typeLocalName)) {
            return IntNonNegative.THE_INSTANCE;
        } else if ("integer-positive".equals(typeLocalName)) {
            return IntPositive.THE_INSTANCE;
        } else if ("float".equals(typeLocalName)) {
            return FloatingPointExponent.THE_INSTANCE;
        } else if ("float-non-negative".equals(typeLocalName)) {
            return FloatingPointExponentNonNegative.THE_INSTANCE;
        } else if ("float-positive".equals(typeLocalName)) {
            return FloatingPointExponentPositive.THE_INSTANCE;
        } else if ("css-number-token".equals(typeLocalName)) {
            return CssNumberToken.THE_INSTANCE;
        } else if ("mime-type-list".equals(typeLocalName)) {
            return MimeTypeList.THE_INSTANCE;
        } else if ("circle".equals(typeLocalName)) {
            return Circle.THE_INSTANCE;
        } else if ("rectangle".equals(typeLocalName)) {
            return Rectangle.THE_INSTANCE;
        } else if ("polyline".equals(typeLocalName)) {
            return Polyline.THE_INSTANCE;
        } else if ("xml-name".equals(typeLocalName)) {
            return XmlName.THE_INSTANCE;
        } else if ("meta-charset".equals(typeLocalName)) {
            return MetaCharset.THE_INSTANCE;
        } else if ("microdata-property".equals(typeLocalName)) {
            return MicrodataProperty.THE_INSTANCE;
        } else if ("charset".equals(typeLocalName)) {
            return Charset.THE_INSTANCE;
        } else if ("refresh".equals(typeLocalName)) {
            return Refresh.THE_INSTANCE;
        } else if ("paren-start".equals(typeLocalName)) {
            return ParenthesisStart.THE_INSTANCE;
        } else if ("paren-end".equals(typeLocalName)) {
            return ParenthesisEnd.THE_INSTANCE;
        } else if ("email-address".equals(typeLocalName)) {
            return EmailAddress.THE_INSTANCE;
        } else if ("email-address-list".equals(typeLocalName)) {
            return EmailAddressList.THE_INSTANCE;
        } else if ("keylabellist".equals(typeLocalName)) {
            return KeyLabelList.THE_INSTANCE;
        } else if ("zero".equals(typeLocalName)) {
            return Zero.THE_INSTANCE;
        } else if ("cdo-cdc-pair".equals(typeLocalName)) {
            return CdoCdcPair.THE_INSTANCE;
        } else if ("script".equals(typeLocalName)) {
            return Script.THE_INSTANCE;
        } else if ("script-documentation".equals(typeLocalName)) {
            return ScriptDocumentation.THE_INSTANCE;
        } else if ("functionbody".equals(typeLocalName)) {
            return FunctionBody.THE_INSTANCE;
        } else if ("a-rel".equals(typeLocalName)) {
            return ARel.THE_INSTANCE;
        } else if ("link-rel".equals(typeLocalName)) {
            return LinkRel.THE_INSTANCE;
        } else if ("non-empty-string".equals(typeLocalName)) {
            return NonEmptyString.THE_INSTANCE;
        } else if ("string-without-line-breaks".equals(typeLocalName)) {
            return StringWithoutLineBreaks.THE_INSTANCE;
        } else if ("simple-color".equals(typeLocalName)) {
            return SimpleColor.THE_INSTANCE;
        } else if ("color".equals(typeLocalName)) {
            return Color.THE_INSTANCE;
        } else if ("time-datetime".equals(typeLocalName)) {
            return TimeDatetime.THE_INSTANCE;
        } else if ("svg-pathdata".equals(typeLocalName)) {
            return new SvgPathData();
        } else if ("source-size-list".equals(typeLocalName)) {
            return SourceSizeList.THE_INSTANCE;
        } else if ("image-candidate-strings".equals(typeLocalName)) {
            return ImageCandidateStrings.THE_INSTANCE;
        } else if ("image-candidate-strings-width-required".equals(typeLocalName)) {
            return ImageCandidateStringsWidthRequired.THE_INSTANCE;
        } else if ("image-candidate-url".equals(typeLocalName)) {
            return ImageCandidateURL.THE_INSTANCE;
        } else if ("integrity-metadata".equals(typeLocalName)) {
            return IntegrityMetadata.THE_INSTANCE;
        } else if ("sandbox-allow-list".equals(typeLocalName)) {
            return SandboxAllowList.THE_INSTANCE;
        } else if ("content-security-policy".equals(typeLocalName)) {
            return ContentSecurityPolicy.THE_INSTANCE;
        } else if ("autocomplete-any".equals(typeLocalName)) {
            return AutocompleteDetailsAny.THE_INSTANCE;
        } else if ("autocomplete-text".equals(typeLocalName)) {
            return AutocompleteDetailsText.THE_INSTANCE;
        } else if ("autocomplete-password".equals(typeLocalName)) {
            return AutocompleteDetailsPassword.THE_INSTANCE;
        } else if ("autocomplete-url".equals(typeLocalName)) {
            return AutocompleteDetailsUrl.THE_INSTANCE;
        } else if ("autocomplete-email".equals(typeLocalName)) {
            return AutocompleteDetailsEmail.THE_INSTANCE;
        } else if ("autocomplete-tel".equals(typeLocalName)) {
            return AutocompleteDetailsTel.THE_INSTANCE;
        } else if ("autocomplete-numeric".equals(typeLocalName)) {
            return AutocompleteDetailsNumeric.THE_INSTANCE;
        } else if ("autocomplete-month".equals(typeLocalName)) {
            return AutocompleteDetailsMonth.THE_INSTANCE;
        } else if ("autocomplete-date".equals(typeLocalName)) {
            return AutocompleteDetailsDate.THE_INSTANCE;
        } else if ("custom-element-name".equals(typeLocalName)) {
            return CustomElementName.THE_INSTANCE;
        }
        throw new DatatypeException("Unknown local name for datatype: " + typeLocalName);
    }

}
