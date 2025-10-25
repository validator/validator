//
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.util;

public enum CssVersion {
    // css3 is a fictional point, going away from the monolothic CSS1/2/2.1/2.2 specs,
    // css-2015 should be linked to the CSS snapshot 2015 (and might be deleted later on)
    // and CSS is the latest version of specs.
    CSS1("css1"), CSS2("css2"), CSS21("css21"), CSS3("css3"), CSS_2015("css-2015"), CSS("css");
    private final String version;

    CssVersion(String version) {
        this.version = version;
    }

    public String toString() {
        return version;
    }

    public static CssVersion resolve(ApplContext ac, String s)
    //          throws InvalidParamException {
    {
        for (CssVersion v : CssVersion.values()) {
            if (v.toString().equals(s)) {
                return v;
            }
        }
        // now some specific checks:
        switch (s) {
            case "css3svg":
                return CSS3;
        }
        // and known profiles edge cases.
        CssProfile profile = CssProfile.resolve(ac, s);
        switch (profile) {
            case SVG:
            case SVGBASIC:
            case SVGTINY:
            case TV:
            case ATSCTV:
            case MOBILE:
                return CSS21;
            default:
                return getDefault();
        }
        // TODO this or get the default ???
        //       throw new InvalidParamException("invalid-level", s, ac);
    }

    // get the default version of CSS
    public static CssVersion getDefault() {
        return CSS3;
    }
}
