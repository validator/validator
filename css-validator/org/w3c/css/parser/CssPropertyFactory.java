//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.parser;

import org.w3c.css.atrules.css.AtRuleMedia;
import org.w3c.css.atrules.css.media.Media;
import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.properties.PropertiesLoader;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssProfile;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Utf8Properties;
import org.w3c.css.util.WarningParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Philippe Le Hegaret
 * @version $Revision$
 */
public class CssPropertyFactory implements Cloneable {

    private static final String[] NONSTANDARD_PROPERTIES = //
            {"zoom"};

    private static boolean isNonstandardProperty(String property) {
        if (property.charAt(0) == '-' || property.charAt(0) == '_') {
            return true;
        }
        for (String s : NONSTANDARD_PROPERTIES) {
            if (s.equals(property)) {
                return true;
            }
        }
        return false;
    }

    // all recognized properties are here.
    private Utf8Properties properties;

    //all used profiles are here (in the priority order)
    private static String[] SORTEDPROFILES = PropertiesLoader.getProfiles();

    // private Utf8Properties allprops;

    // does not seem to be used
    // private String usermedium;

    public CssPropertyFactory getClone() {
        try {
            return (CssPropertyFactory) clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Create a new CssPropertyFactory
     */
    public CssPropertyFactory(String profile) {
        properties = PropertiesLoader.getProfile(profile);
        // It's not good to have null properties :-/
        if (properties == null) {
            throw new NullPointerException();
        }
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    private ArrayList<String> getMediaList(String media) {
        ArrayList<String> list = new ArrayList<String>();
        String medium;
        StringTokenizer tok = new StringTokenizer(media, ",");

        while (tok.hasMoreTokens()) {
            medium = tok.nextToken();
            medium = medium.trim();
            list.add(medium);
        }
        return list;
    }

    // bug: FIXME
    // @media screen and (min-width: 400px) and (max-width: 700px), print {
    // a {
    // border: 0;
    // }
    // }
    public synchronized MediaFeature createMediaFeature(ApplContext ac, AtRule atRule, String feature,
                                                        CssExpression expression) throws Exception {
        String modifier = null;
        String classname;
        int dashpos = feature.indexOf('-');
        feature = feature.toLowerCase();
        if (dashpos != -1) {
            if (dashpos == 0) {
                throw vendorMediaException(ac, atRule, feature);
            }
            modifier = feature.substring(0, dashpos);
            // clash between feature name and modifier...
            // link min-width and color-index, so we check we have min- or max-
            if (modifier.equals("min") || modifier.equals("max")) {
                feature = feature.substring(dashpos + 1);
            } else {
                // back to normal
                modifier = null;
            }
        }

        classname = properties.getProperty("mf" + "." + feature.toLowerCase());
        if (classname == null) {
            throw vendorMediaException(ac, atRule, feature);
        }

        try {
            // create an instance of your property class
            Class expressionclass = CssExpression.class;
            if (expression != null) {
                expressionclass = expression.getClass();
            }
            // Maybe it will be necessary to add the check parameter as for
            // create property, so... FIXME
            Class[] parametersType = {ac.getClass(), String.class, expressionclass};
            Constructor constructor = Class.forName(classname).getConstructor(parametersType);
            Object[] parameters = {ac, modifier, expression};
            // invoke the constructor
            return (MediaFeature) constructor.newInstance(parameters);
        } catch (InvocationTargetException e) {
            // catch InvalidParamException
            Exception ex = (Exception) e.getTargetException();
            throw ex;
        }
    }

    private Exception vendorMediaException(ApplContext ac, AtRule atRule,
                                           String feature) throws Exception {
        // I don't know this property
        // TODO get the latest media it applies to
        try {
            AtRuleMedia atRuleMedia = (AtRuleMedia) atRule;
            if (ac.getTreatVendorExtensionsAsWarnings()) {
                throw new WarningParamException("vendor-extension",
                        feature);
            } else {
                throw new InvalidParamException(
                        "noexistence-media", feature,
                        atRuleMedia.getCurrentMedia(), ac);
            }
        } catch (ClassCastException cce) {
            // I don't know this property
            throw new InvalidParamException("noexistence", feature,
                    "not media @rule", ac);
        }
    }

    public synchronized CssProperty createProperty(ApplContext ac, AtRule atRule, String property,
                                                   CssExpression expression) throws Exception {
        String classname = null;
        AtRuleMedia atRuleMedia;
        String media = null;

        // if the property name indicates a vendor extension, exit without checking
        // if we need to raise only a warning.
        if (ac.getTreatVendorExtensionsAsWarnings() && isVendorExtension(property)) {
            throw new WarningParamException("vendor-extension", property);
        }

        try {
            atRuleMedia = (AtRuleMedia) atRule;
            // TODO FIXME in fact, it should use a vector of media instead of extracting
            // only one media, so let's use kludges
            for (Media m : atRuleMedia.getMediaList()) {
                if (!m.getNot()) {
                    media = m.getMedia();
                    break;
                }
            }
        } catch (ClassCastException cce) {
            media = "all";
        }
        classname = setClassName(atRule, media, ac, property);

        // the property does not exist in this profile
        // this is an error... or a warning if it exists in another profile
        if (classname == null) {
            ArrayList<String> pfsOk = new ArrayList<String>();
            String spec = ac.getPropertyKey();

            for (String p : SORTEDPROFILES) {
                if (!p.equals(spec) && PropertiesLoader.getProfile(p).containsKey(property)) {
                    pfsOk.add(p);
                }
            }

            if (pfsOk.size() > 0) {
                if (ac.getCssProfile() == CssProfile.NONE) {
                    String latestVersion = pfsOk.get(pfsOk.size() - 1);
                    CssVersion v = CssVersion.resolve(ac, latestVersion);
                    // should always be true... otherwise there is an issue...
                    if (v.compareTo(ac.getCssVersion()) > 0) {
                        ac.getFrame().addWarning("noexistence", new String[]{property, ac.getMsg().getString(ac.getPropertyKey()), pfsOk.toString()});
                        ac.setCssVersion(v);
                    }
                    classname = setClassName(atRule, media, ac, property);
                } else {

					/*
                                        // This should be uncommented when no-profile in enabled
										if (ac.getProfileString().equals("none")) {
										// the last one should be the best one to use
										String	pf = (String) pfsOk.get(pfsOk.size()-1),
										old_pf = ac.getCssVersionString();
										ac.setCssVersion(pf);
										ac.getFrame().addWarning("noexistence", new String[] { property, ac.getMsg().getString(old_pf), pfsOk.toString() });
										classname = setClassName(atRule, media, ac, property);
										ac.setCssVersion(old_pf);
										}
										else
										*/
                    throw new InvalidParamException("noexistence", new String[]{property, ac.getMsg().getString(ac.getPropertyKey()), pfsOk.toString()}, ac);
                }
            } else {
                if (ac.getSuggestPropertyName()) {
                    String possibleName = findClosestPropertyName(atRule, ac, property);
                    if (possibleName != null) {
                        throw new InvalidParamException("noexistence-typo", new String[]{property, possibleName}, ac);

                    }
                }
                throw new InvalidParamException("noexistence-at-all", property, ac);
            }
        }

        // we have a property name, check about vendor extension or hack in the expression
        if (ac.getTreatVendorExtensionsAsWarnings() && expression.hasVendorExtensions()) {
            throw new WarningParamException("vendor-extension", expression.toStringFromStart());
        }

        if (expression.hasCssVariable()) {
            throw new WarningParamException("css-variable", expression.toStringFromStart());
        }

        if (ac.getTreatCssHacksAsWarnings() && expression.hasCssHack()) {
            throw new WarningParamException("css-hack", expression.toStringFromStart());
        }

        try {
            boolean isCssWide = false;
            CssIdent cssIdent = null;
            if ((expression.getCount() == 1) && (expression.getValue().getRawType() == CssTypes.CSS_IDENT)) {
                cssIdent = expression.getValue().getIdent();
                isCssWide = CssIdent.isCssWide(cssIdent);
            }
            if ((ac.getCssVersion().compareTo(CssVersion.CSS3) >= 0) && isCssWide) {
                // create an instance of your property class
                Class[] parametersType = {};
                Constructor constructor = Class.forName(classname).getConstructor(parametersType);
                Object[] parameters = {};
                expression.next(); // consume token
                // invoke the constructor
                CssProperty p = (CssProperty) constructor.newInstance(parameters);
                p.value = cssIdent;
                return p;
            } else {
                // create an instance of your property class
                Class[] parametersType = {ac.getClass(), expression.getClass(), boolean.class};
                Constructor constructor = Class.forName(classname).getConstructor(parametersType);
                Object[] parameters = {ac, expression, Boolean.TRUE};
                // invoke the constructor
                return (CssProperty) constructor.newInstance(parameters);

            }
        } catch (InvocationTargetException e) {
            // catch InvalidParamException
            Exception ex = (Exception) e.getTargetException();
            //	uncomment for debug - ex.printStackTrace();
            throw ex;
        }
    }

    private String setClassName(AtRule atRule, String media, ApplContext ac, String property) {
        String className;
        String prefix = atRule.lookupPrefix();

        if (!prefix.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            // construct the property key
            sb.append('@').append(atRule.keyword()).append('.').append(property);
            className = PropertiesLoader.getProfile(ac.getPropertyKey()).getProperty(sb.toString());
            if (className != null || atRule.isPropertyLookupStrict()) {
                return className;
            }
        }
        // either prefix is empty, or it's a fallback if not the rule is not strict wrt lookup
        className = PropertiesLoader.getProfile(ac.getPropertyKey()).getProperty(property);
        // a list of media has been specified
        if (className != null && media != null && !media.equals("all")) {
            String propMedia = PropertiesLoader.mediaProperties.getProperty(property);
            if (propMedia == null) {
                return className;
            }
            ArrayList<String> list = getMediaList(media);
            for (String medium : list) {
                if (propMedia.indexOf(medium.toLowerCase()) == -1 && !propMedia.equals("all")) {
                    ac.getFrame().addWarning("noexistence-media", new String[]{property, medium + " (" + propMedia + ")"});
                }
            }
        }
        return className;
    }

    private String findClosestPropertyName(AtRule atRule, ApplContext ac, String property) {
        return null;
    }

    private boolean isVendorExtension(String property) {
        return property.length() > 0 && isNonstandardProperty(property);
    }
}
