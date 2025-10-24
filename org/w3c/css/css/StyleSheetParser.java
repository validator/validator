//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.css;

import org.w3c.css.atrules.css.AtRuleMedia;
import org.w3c.css.atrules.css.AtRulePage;
import org.w3c.css.parser.AtRule;
import org.w3c.css.parser.CssError;
import org.w3c.css.parser.CssFouffa;
import org.w3c.css.parser.CssParseException;
import org.w3c.css.parser.CssSelectors;
import org.w3c.css.parser.CssValidatorListener;
import org.w3c.css.parser.Errors;
import org.w3c.css.parser.analyzer.ParseException;
import org.w3c.css.parser.analyzer.TokenMgrError;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.selectors.IdSelector;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Messages;
import org.w3c.css.util.UnescapeFilterReader;
import org.w3c.css.util.Util;
import org.w3c.css.util.Warning;
import org.w3c.css.util.Warnings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @version $Revision$
 */
public final class StyleSheetParser
        implements CssValidatorListener, CssParser {

    private static Constructor co = null;
    private static boolean isPreprocessed;

    static {
        try {
            Class c = java.lang.Exception.class;
            Class cp[] = {java.lang.Exception.class};
            co = c.getDeclaredConstructor(cp);
        } catch (NoSuchMethodException ex) {
            co = null;
        }
    }

    CssFouffa cssFouffa;
    StyleSheet style = new StyleSheet();

    public StyleSheetParser(ApplContext ac, boolean isPreprocessed) {
        this.isPreprocessed = isPreprocessed;
        ac.setStyleSheet(getStyleSheet());
    }

    public StyleSheetParser(ApplContext ac) {
        new StyleSheetParser(ac, false);
    }

    public void reInit() {
        style = new StyleSheet();
    }

    public StyleSheet getStyleSheet() {
        return style;
    }

    public void setWarningLevel(int warningLevel) {
        style.setWarningLevel(warningLevel);
    }

    public void notifyErrors(Errors errors) {
        style.addErrors(errors);
    }

    public void notifyWarnings(Warnings warnings) {
        style.addWarnings(warnings);
    }

    /**
     * Adds a vector of properties to a selector.
     *
     * @param selector   the selector
     * @param properties Properties to associate with contexts
     */
    public void handleRule(ApplContext ac, CssSelectors selector,
                           ArrayList<CssProperty> properties) {
        if (selector.getAtRule() instanceof AtRulePage) {
            style.remove(selector);
        }
        for (CssProperty property : properties) {
            property.setSelectors(selector);
            style.addProperty(selector, property);
        }
    }

    // part added by Sijtsche de Jong

    public void addCharSet(String charset) {
        style.addCharSet(charset);
    }

    public void newAtRule(AtRule atRule) {
        style.newAtRule(atRule);
    }

    public void endOfAtRule() {
        style.endOfAtRule();
    }

    public void setImportant(boolean important) {
        style.setImportant(important);
    }

    public void setSelectorList(ArrayList<CssSelectors> selectors) {
        style.setSelectorList(selectors);
    }

    public void setProperty(ArrayList<CssProperty> properties) {
        style.setProperty(properties);
    }

    public void endOfRule() {
        style.endOfRule();
    }

    public void removeThisRule() {
        style.removeThisRule();
    }

    public void removeThisAtRule() {
        style.removeThisAtRule();
    }

    //end of part added by Sijtsche de Jong

    /**
     * Handles an at-rule.
     * <p/>
     * <p>The parameter <code>value</code> can be :
     * <DL>
     * <DT>CssString
     * <DD>The value coming from a string.
     * <DT>CssURL
     * <DD>The value coming from an URL.
     * <DT>Vector
     * <DD>The value is a vector of declarations (it contains properties).
     * This feature is not legal, so be careful.
     * </DL>
     *
     * @param ident  The ident for this at-rule (for example: 'font-face')
     * @param string The string representation if this at-rule
     */
    public void handleAtRule(ApplContext ac, String ident, String string) {
        style.getWarnings().addWarning(new Warning(cssFouffa.getSourceFile(),
                cssFouffa.getLine(),
                "at-rule",
                2,
                new String[]{ident, string},
                ac));
        //stylesheet.addAtRule(atRule);
    }

    /**
     * @param url    the URL containing the style sheet
     * @param title  the title of the stylesheet
     * @param kind   may be a stylesheet or an alternate stylesheet
     * @param media  the media to apply this
     * @param origin the origin of the style sheet
     * @throws IOException an IO error
     */
    public void parseURL(ApplContext ac, URL url, String title,
                         String kind, String media,
                         int origin) {
        boolean doneref = false;
        URL ref = ac.getReferrer();
        setWarningLevel(ac.getWarningLevel());
        if (Util.onDebug) {
            System.err.println("StyleSheet.parseURL(" + url + ", "
                    + title + ", "
                    + kind + ", " + media + ", "
                    + origin + ")");
        }
        if (kind != null) {
            kind = kind.trim().toLowerCase();
            if (!kind.equals("stylesheet") && !kind.equals("alternate stylesheet")) {
                return;
            }
        }
        try {
            ac.setOrigin(origin);
//	    if (cssFouffa == null) {
            cssFouffa = new CssFouffa(ac, url);
            cssFouffa.addListener(this);
//	    } else {
//		cssFouffa.ReInit(ac, url);
//	    }

            //	    cssFouffa.setResponse(res);

            // removed plh 2001-03-08
            // cssFouffa.setOrigin(origin);
            //	    cssFouffa.setDefaultMedium(defaultmedium);
            //	    cssFouffa.doConfig();
            if (media == null) {
                if (ac.getCssVersion() != CssVersion.CSS1) {
                    if (ac.getMedium() == null) {
                        media = "all";
                    } else {
                        media = ac.getMedium();
                    }
                }
            }
            AtRuleMedia m = AtRuleMedia.getInstance(ac.getCssVersion());
            try {
                if (media != null) {
                    addMedias(m, media, ac);
                }
                cssFouffa.setAtRule(m);
            } catch (org.w3c.css.util.InvalidParamException e) {
                Errors er = new Errors();
                er.addError(new org.w3c.css.parser.CssError(url.toString(),
                        -1, e));
                notifyErrors(er);
                return;
            }
            ac.setReferrer(url);
            doneref = true;
            cssFouffa.parseStyle();
        } catch (Exception e) {
            Errors er = new Errors();
            er.addError(new org.w3c.css.parser.CssError(Messages.escapeString(url.toString()),
                    -1, new Exception(Messages.escapeString(e.getMessage()))));
            notifyErrors(er);
        } finally {
            if (doneref) {
                ac.setReferrer(ref);
            }
        }
    }

    // add media, easy version for CSS version < 3, otherwise, reuse the parser
    private void addMedias(AtRuleMedia m, String medias, ApplContext ac) throws InvalidParamException {
        // before CSS3, let's parse it the easy way...
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringTokenizer tokens = new StringTokenizer(medias, ",");
            while (tokens.hasMoreTokens()) {
                m.addMedia(null, tokens.nextToken().trim(), ac);
            }
        } else {
            CssFouffa muP = new CssFouffa(ac, new StringReader(medias));
            try {
                AtRuleMedia arm = muP.parseMediaDeclaration();
                if (arm != null) {
                    m.allMedia = arm.allMedia;
                }
            } catch (ParseException pex) {
                // error already added, so nothing else to do
            }
        }
    }

    /**
     * Parse a style element. The Style element always comes from the user
     *
     * @param reader the reader containing the style data
     * @param url    the name of the file the style element was read in.
     * @throws IOException an IO error
     */
    public void parseStyleElement(ApplContext ac, Reader reader,
                                  String title, String media,
                                  URL url, int lineno) {
        boolean doneref = false;
        style.setWarningLevel(ac.getWarningLevel());
        if (Util.onDebug) {
            System.err.println("StyleSheet.parseStyleElement(" + title + ", "
                    + media + ", " + url
                    + "," + lineno + ")");
        }
        URL ref = ac.getReferrer();
        try {

//	    if (cssFouffa == null) {
            String charset = ac.getCharsetForURL(url);
            if (ac.getCssVersion().compareTo(CssVersion.CSS2) >=0
                    && !isPreprocessed) {
                cssFouffa = new CssFouffa(ac, new UnescapeFilterReader(new BufferedReader(reader)), url, lineno);
            } else {
                cssFouffa = new CssFouffa(ac, reader, url, lineno);

            }
            cssFouffa.addListener(this);
//	    } else {
//		cssFouffa.ReInit(ac, input, url, lineno);
//	    }

            //	    cssFouffa.setResponse(res);
            //	    cssFouffa.setDefaultMedium(defaultmedium);
            //	    cssFouffa.doConfig();
            if (media == null && ac.getCssVersion() != CssVersion.CSS1) {
                media = "all";
            }

            AtRuleMedia m = AtRuleMedia.getInstance(ac.getCssVersion());
            try {
                if (media != null) {
                    addMedias(m, media, ac);
                }
                cssFouffa.setAtRule(m);
            } catch (org.w3c.css.util.InvalidParamException e) {
                Errors er = new Errors();
                er.addError(new org.w3c.css.parser.CssError(url.toString(),
                        -1, e));
                notifyErrors(er);
                return;
            }
            ac.setReferrer(url);
            doneref = true;
            cssFouffa.parseStyle();
        } catch (IOException e) {
            Errors er = new Errors();
            er.addError(new org.w3c.css.parser.CssError(url.toString(),
                    -1, e));
            notifyErrors(er);
        } catch (TokenMgrError e) {
            Errors er = new Errors();
            CssParseException cpe = null;
            if (co != null) {
                try {
                    Object o[] = new Object[1];
                    o[0] = e;
                    Exception new_e = (Exception) co.newInstance(o);
                    cpe = new CssParseException(new_e);
                } catch (Exception ex) {
                    cpe = null;
                }
            }
            if (cpe == null) {
                cpe = new CssParseException(new Exception(e.getMessage()));
            }
            er.addError(new org.w3c.css.parser.CssError(url.toString(),
                    -1,
                    //e.getErrorLine(),
                    cpe));
            notifyErrors(er);
        } catch (RuntimeException e) {
            Errors er = new Errors();
            er.addError(new org.w3c.css.parser.CssError(url.toString(),
                    cssFouffa.getLine(),
                    new CssParseException(e)));
            notifyErrors(er);
        } finally {
            if (doneref) {
                ac.setReferrer(ref);
            }
        }
    }

    /**
     * Parse a style element. The Style element always comes from the user
     *
     * @param input the input stream containing the style data
     * @param url   the name of the file the style element was read in.
     * @throws IOException an IO error
     */
    public void parseStyleElement(ApplContext ac, InputStream input,
                                  String title, String media,
                                  URL url, int lineno) {
        // FIXME better handling of charset using a charset detection library
        String charset = ac.getCharsetForURL(url);
        Charset c = null;
        if (charset == null) {
            parseStyleElement(ac, input, null, title,
                    media, url, lineno);
        } else {
            try {
                c = Charset.forName(charset);
            } catch (UnsupportedCharsetException ucx) {
                Errors er = new Errors();
                er.addError(new org.w3c.css.parser.CssError(url.toString(),
                        -1, ucx));
                notifyErrors(er);
            } catch (Exception ex) {
                // in case of error, ignore it.
                if (Util.onDebug) {
                    System.err.println("Error in StyleSheet.parseStyleElement(" + title + ","
                            + url + "," + lineno + ")");
                }
            }
            parseStyleElement(ac, input, c, title, media, url, lineno);
        }
    }

    /**
     * Parse a style element. The Style element always comes from the user
     *
     * @param input the input stream containing the style data
     * @param url   the name of the file the style element was read in.
     * @throws IOException an IO error
     */
    public void parseStyleElement(ApplContext ac, InputStream input,
                                  Charset charset,
                                  String title, String media,
                                  URL url, int lineno) {
        InputStreamReader reader = null;
        reader = new InputStreamReader(input, (charset == null) ? StandardCharsets.ISO_8859_1 : charset);
        parseStyleElement(ac, reader, title, media, url, lineno);
    }

    /**
     * Unify call to the parser for css doc as a reader.
     *
     * @param ac
     * @param reader
     * @param docref
     */
    public void parseStyleSheet(ApplContext ac, Reader reader, URL docref) {
        parseStyleElement(ac, reader, null, null, (docref == null) ? ac.getFakeURL() : docref, 0);
    }

    /**
     * Parse some declarations. All declarations always comes from the user
     *
     * @param input the inputStream containing the style data
     * @param id    the uniq id
     * @param url   the URL the style element was read in.
     * @throws IOException an IO error
     */
    public void parseStyleAttribute(ApplContext ac, InputStream input, String id,
                                    URL url, int lineno) {
        style.setWarningLevel(ac.getWarningLevel());
        lineno--; // why ?!?!
        if (Util.onDebug) {
            System.err.println("StyleSheet.parseStyleAttribute(" + id + ","
                    + url + "," + lineno + ")");
        }

        try {
            //	    if (cssFouffa == null) {
            String charset = ac.getCharsetForURL(url);
            cssFouffa = new CssFouffa(ac, input, charset, url, lineno);
            cssFouffa.addListener(this);
            //	    } else
//		cssFouffa.ReInit(ac, input, url, lineno);
            CssSelectors selector = new CssSelectors(ac);

            try {
                AtRuleMedia media = AtRuleMedia.getInstance(ac.getCssVersion());
                if (ac.getCssVersion() != CssVersion.CSS1) {
                    media.addMedia(null, "all", ac);
                }
                cssFouffa.setAtRule(media);
            } catch (InvalidParamException e) {
            } //ignore

            try {
                if (id == null || id.length() == 0) {
                    id = "nullId-" + Long.toHexString(System.currentTimeMillis());
                    // TODO add an error/warning ?
                }
                selector.addId(new IdSelector(id.substring(1)));
            } catch (InvalidParamException e) {
                style.removeThisRule();
                ac.getFrame().addError(new CssError(e));
            }
            cssFouffa.parseDeclarations(selector);
        } catch (IOException e) {
            Errors er = new Errors();
            er.addError(new org.w3c.css.parser.CssError(url.toString(),
                    -1, e));
            notifyErrors(er);
        }
    }

    public void setStyle(Class style) {
        cssFouffa.setStyle(style);
    }

}
