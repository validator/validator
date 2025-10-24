//
// $Id$
// From Sijtsche de Jong
//
// COPYRIGHT (c) 1995-2002 World Wide Web Consortium, (MIT, INRIA, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.svg;

import org.w3c.css.properties.css.CssMarker;
import org.w3c.css.properties.css.CssMarkerEnd;
import org.w3c.css.properties.css.CssMarkerMid;
import org.w3c.css.properties.css.CssMarkerStart;
import org.w3c.css.properties.css.CssMaskBorderMode;
import org.w3c.css.properties.css.CssMaskBorderOutset;
import org.w3c.css.properties.css.CssMaskBorderRepeat;
import org.w3c.css.properties.css.CssMaskBorderSlice;
import org.w3c.css.properties.css.CssMaskBorderSource;
import org.w3c.css.properties.css.CssMaskBorderWidth;
import org.w3c.css.properties.css.CssMaskClip;
import org.w3c.css.properties.css.CssMaskComposite;
import org.w3c.css.properties.css.CssMaskImage;
import org.w3c.css.properties.css.CssMaskMode;
import org.w3c.css.properties.css.CssMaskOrigin;
import org.w3c.css.properties.css.CssMaskPosition;
import org.w3c.css.properties.css.CssMaskRepeat;
import org.w3c.css.properties.css.CssMaskSize;
import org.w3c.css.properties.css.CssMaskType;
import org.w3c.css.properties.css.colorprofile.CssName;
import org.w3c.css.properties.css.colorprofile.CssRenderingIntent;
import org.w3c.css.properties.css.colorprofile.CssSrc;

public class SVGStyle extends SVGBasicStyle {

    public CssMarkerStart cssMarkerStart;
    public CssMarkerMid cssMarkerMid;
    public CssMarkerEnd cssMarkerEnd;
    public CssMarker cssMarker;

    // @color-profile
    public CssRenderingIntent colorProfileCssRenderingIntent;
    public CssName colorProfileCssName;
    public CssSrc colorProfileCssSrc;

    public CssMaskClip cssMaskClip;
    public CssMaskComposite cssMaskComposite;
    public CssMaskImage cssMaskImage;
    public CssMaskMode cssMaskMode;
    public CssMaskOrigin cssMaskOrigin;
    public CssMaskPosition cssMaskPosition;
    public CssMaskRepeat cssMaskRepeat;
    public CssMaskSize cssMaskSize;
    public CssMaskType cssMaskType;

    public CssMaskBorderSource cssMaskBorderSource;
    public CssMaskBorderMode cssMaskBorderMode;
    public CssMaskBorderSlice cssMaskBorderSlice;
    public CssMaskBorderWidth cssMaskBorderWidth;
    public CssMaskBorderOutset cssMaskBorderOutset;
    public CssMaskBorderRepeat cssMaskBorderRepeat;

    public CssMaskBorderWidth getMaskBorderWidth() {
        if (cssMaskBorderWidth == null) {
            cssMaskBorderWidth = (CssMaskBorderWidth) style.CascadingOrder(new CssMaskBorderWidth(),
                    style, selector);
        }
        return cssMaskBorderWidth;
    }
    
    public CssMaskBorderSource getMaskBorderSource() {
        if (cssMaskBorderSource == null) {
            cssMaskBorderSource = (CssMaskBorderSource) style.CascadingOrder(new CssMaskBorderSource(),
                    style, selector);
        }
        return cssMaskBorderSource;
    }

    public CssMaskBorderSlice getMaskBorderSlice() {
        if (cssMaskBorderSlice == null) {
            cssMaskBorderSlice = (CssMaskBorderSlice) style.CascadingOrder(new CssMaskBorderSlice(),
                    style, selector);
        }
        return cssMaskBorderSlice;
    }

    public CssMaskBorderRepeat getMaskBorderRepeat() {
        if (cssMaskBorderRepeat == null) {
            cssMaskBorderRepeat = (CssMaskBorderRepeat) style.CascadingOrder(new CssMaskBorderRepeat(),
                    style, selector);
        }
        return cssMaskBorderRepeat;
    }

    public CssMaskBorderOutset getMaskBorderOutset() {
        if (cssMaskBorderOutset == null) {
            cssMaskBorderOutset = (CssMaskBorderOutset) style.CascadingOrder(new CssMaskBorderOutset(),
                    style, selector);
        }
        return cssMaskBorderOutset;
    }
    
    public CssMaskBorderMode getMaskBorderMode() {
        if (cssMaskBorderMode == null) {
            cssMaskBorderMode = (CssMaskBorderMode) style.CascadingOrder(new CssMaskBorderMode(),
                    style, selector);
        }
        return cssMaskBorderMode;
    }

    public CssMaskType getMaskType() {
        if (cssMaskType == null) {
            cssMaskType = (CssMaskType) style.CascadingOrder(new CssMaskType(),
                    style, selector);
        }
        return cssMaskType;
    }

    public CssMaskSize getMaskSize() {
        if (cssMaskSize == null) {
            cssMaskSize = (CssMaskSize) style.CascadingOrder(new CssMaskSize(),
                    style, selector);
        }
        return cssMaskSize;
    }

    public CssMaskPosition getMaskPosition() {
        if (cssMaskPosition == null) {
            cssMaskPosition = (CssMaskPosition) style.CascadingOrder(new CssMaskPosition(),
                    style, selector);
        }
        return cssMaskPosition;
    }

    public CssMaskRepeat getMaskRepeat() {
        if (cssMaskRepeat == null) {
            cssMaskRepeat = (CssMaskRepeat) style.CascadingOrder(new CssMaskRepeat(),
                    style, selector);
        }
        return cssMaskRepeat;
    }

    public CssMaskOrigin getMaskOrigin() {
        if (cssMaskOrigin == null) {
            cssMaskOrigin = (CssMaskOrigin) style.CascadingOrder(new CssMaskOrigin(),
                    style, selector);
        }
        return cssMaskOrigin;
    }

    public CssMaskMode getMaskMode() {
        if (cssMaskMode == null) {
            cssMaskMode = (CssMaskMode) style.CascadingOrder(new CssMaskMode(),
                    style, selector);
        }
        return cssMaskMode;
    }

    public CssMaskImage getMaskImage() {
        if (cssMaskImage == null) {
            cssMaskImage = (CssMaskImage) style.CascadingOrder(new CssMaskImage(),
                    style, selector);
        }
        return cssMaskImage;
    }

    public CssMaskComposite getMaskComposite() {
        if (cssMaskComposite == null) {
            cssMaskComposite = (CssMaskComposite) style.CascadingOrder(new CssMaskComposite(),
                    style, selector);
        }
        return cssMaskComposite;
    }

    public CssMaskClip getMaskClip() {
        if (cssMaskClip == null) {
            cssMaskClip = (CssMaskClip) style.CascadingOrder(new CssMaskClip(),
                    style, selector);
        }
        return cssMaskClip;
    }

    public CssMarkerStart getMarkerStart() {
        if (cssMarkerStart == null) {
            cssMarkerStart = (CssMarkerStart) style.CascadingOrder(new CssMarkerStart(),
                    style, selector);
        }
        return cssMarkerStart;
    }

    public CssMarkerMid getMarkerMid() {
        if (cssMarkerMid == null) {
            cssMarkerMid = (CssMarkerMid) style.CascadingOrder(new CssMarkerMid(),
                    style, selector);
        }
        return cssMarkerMid;
    }

    public CssMarkerEnd getMarkerEnd() {
        if (cssMarkerEnd == null) {
            cssMarkerEnd = (CssMarkerEnd) style.CascadingOrder(new CssMarkerEnd(),
                    style, selector);
        }
        return cssMarkerEnd;
    }

    public CssMarker getMarker() {
        if (cssMarker == null) {
            cssMarker = (CssMarker) style.CascadingOrder(new CssMarker(),
                    style, selector);
        }
        return cssMarker;
    }

    // @color-profile

    public CssRenderingIntent getColorProfileRenderingIntent() {
        if (colorProfileCssRenderingIntent == null) {
            colorProfileCssRenderingIntent = (CssRenderingIntent) style.CascadingOrder(new CssRenderingIntent(),
                    style, selector);
        }
        return colorProfileCssRenderingIntent;
    }

    public CssName getColorProfileName() {
        if (colorProfileCssName == null) {
            colorProfileCssName = (CssName) style.CascadingOrder(new CssName(),
                    style, selector);
        }
        return colorProfileCssName;
    }

    public CssSrc getColorProfileSrc() {
        if (colorProfileCssSrc == null) {
            colorProfileCssSrc = (CssSrc) style.CascadingOrder(new CssSrc(),
                    style, selector);
        }
        return colorProfileCssSrc;
    }
}
