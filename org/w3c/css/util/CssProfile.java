//
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.util;

public enum CssProfile {
    EMPTY(""), NONE("none"), SVG("svg"), SVGBASIC("svgbasic"), SVGTINY("svgtiny"),
    MOBILE("mobile"), TV("tv"), ATSCTV("atsc-tv");
    private final String profile;

    CssProfile(String version) {
        this.profile = version;
    }

    public String toString() {
        return profile;
    }

    public static CssProfile resolve(ApplContext ac, String s) {
        for (CssProfile p : CssProfile.values()) {
            if (p.toString().equals(s)) {
                return p;
            }
        }
        // special cases
        switch (s) {
            case "css3svg":
                return SVG;
        }
        return EMPTY;
    }
}

