//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT, ERCIM and Keio, 2003.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
  This class is the front end of the CSS parser
*/

package org.w3c.css.parser;

import org.w3c.css.atrules.css.AtRuleImport;
import org.w3c.css.atrules.css.AtRuleMedia;
import org.w3c.css.atrules.css.AtRuleNamespace;
import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.css.StyleSheetOrigin;
import org.w3c.css.parser.analyzer.CssParser;
import org.w3c.css.parser.analyzer.CssParserTokenManager;
import org.w3c.css.parser.analyzer.ParseException;
import org.w3c.css.parser.analyzer.TokenMgrError;
import org.w3c.css.properties.PropertiesLoader;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.properties.css3.CssCustomProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.HTTPURL;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Util;
import org.w3c.css.util.WarningParamException;
import org.w3c.css.util.Warnings;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * This class is a front end of the CSS1 parser.
 * <p/>
 * <p/>
 * Example:<br>
 * <code>
 * CssFouffa parser =
 * new CssFouffa(new URL("http://www.w3.org/drafts.css"));<BR>
 * CssValidatorListener myListener = new MyParserListener();<BR>
 * <BR>
 * parser.addListener(myListener);<BR>
 * parser.parseStyle();<BR>
 * </code>
 *
 * @version $Revision$
 */
public final class CssFouffa extends CssParser {

    // all properties
    CssPropertyFactory properties = null;

    // all listeners
    ArrayList<CssValidatorListener> listeners;

    // all errors
    Errors errors;

    // origin of the style sheet
    int origin;

    ArrayList<String> visited = null;


    /**
     * Create a new CssFouffa with a data input and a begin line number.
     *
     * @param ac        The validation context
     * @param reader    The data stream reader
     * @param file      The source file (use for errors, warnings and import)
     * @param beginLine The begin line number in the file. (used for HTML for example)
     * @throws IOException if an I/O error occurs.
     */
    public CssFouffa(ApplContext ac, Reader reader, URL file, int beginLine)
            throws IOException {
        super(reader);
        if (ac.getOrigin() == -1) {
            setOrigin(StyleSheetOrigin.AUTHOR); // default is user
        } else {
            setOrigin(ac.getOrigin()); // default is user
        }
        ac.setFrame(new Frame(this, file.toString(), beginLine,
                ac.getWarningLevel()));
        setApplContext(ac);
        // @@this is a default media ...
        /*
         * AtRuleMedia media = new AtRuleMedia();
         *
         * if (ac.getMedium() == null) { try { media.addMedia("all", ac); }
         * catch (InvalidParamException e) {} //ignore } else { try {
         * media.addMedia(ac.getMedium(), ac); } catch (Exception e) {
         * System.err.println(e.getMessage()); try { media.addMedia("all", ac); }
         * catch (InvalidParamException ex) {} //ignore } } setAtRule(media);
         */
        setURL(file);
        if (Util.onDebug) {
            System.err.println("[DEBUG] CSS version " + ac.getCssVersionString() +
                    " medium " + ac.getMedium() + " at-rule "
                    + getAtRule() + " profile " + ac.getProfileString());
        }

        // load the CssStyle
        String spec = ac.getPropertyKey();
        String classStyle;

        classStyle = PropertiesLoader.config.getProperty(spec);
        if (classStyle == null) {
            spec = CssVersion.getDefault().toString();
            classStyle = PropertiesLoader.config.getProperty(spec);
        }

        Class style;
        try {
            style = Class.forName(classStyle);
            ac.setCssSelectorsStyle(style);
        } catch (ClassNotFoundException e) {
            System.err.println("org.w3c.css.parser.CssFouffa: couldn't" +
                    " load the style");
            e.printStackTrace();
        }

        properties = new CssPropertyFactory(spec);
        listeners = new ArrayList<CssValidatorListener>();
    }

    /**
     * Create a new CssFouffa with a data input and a begin line number.
     *
     * @param input     data input
     * @param file      The source file (use for errors, warnings and import)
     * @param beginLine The begin line number in the file. (used for HTML for example)
     * @throws IOException if an I/O error occurs.
     */
    public CssFouffa(ApplContext ac, InputStream input, String charset,
                     URL file, int beginLine)
            throws IOException {
        this(ac, new InputStreamReader(input, (charset == null) ?
                "iso-8859-1" : charset), file, beginLine);
    }

    /**
     * Create a new CssFouffa with a data input.
     *
     * @param input data input
     * @param file  The source file (use for errors, warnings and import)
     * @throws IOException if an I/O error occurs.
     */
    public CssFouffa(ApplContext ac, InputStream input, URL file)
            throws IOException {
        this(ac, input, (ac.getCharsetForURL(file) != null) ?
                ac.getCharsetForURL(file) : "iso-8859-1", file, 0);
    }

    /**
     * Create a new CssFouffa.
     *
     * @param file The source file (use for data input, errors, warnings and
     *             import)
     * @throws IOException if an I/O error occurs.
     */
    public CssFouffa(ApplContext ac, URL file) throws IOException {
        this(ac, HTTPURL.getConnection(file, ac));

    }

    /**
     * Create a new CssFouffa. internal, to get the URLCOnnection and fill the
     * URL with the relevant one
     */

    private CssFouffa(ApplContext ac, URLConnection uco) throws IOException {
        this(ac, HTTPURL.getInputStream(ac, uco),
                HTTPURL.getCharacterEncoding(ac, uco), uco.getURL(), 0);
        String httpCL = uco.getHeaderField("Content-Location");
        if (httpCL != null) {
            setURL(HTTPURL.getURL(getURL(), httpCL));
        }
    }

    /**
     * Create a new CssFouffa. Used by handleImport.
     *
     * @param in        The source input stream (use for data input, errors,
     *                  warnings and import)
     * @param listeners Works with this listeners
     * @throws IOException if an I/O error occurs.
     */
    private CssFouffa(ApplContext ac, InputStream in, URL url,
                      ArrayList<CssValidatorListener> listeners,
                      ArrayList<String> urlvisited,
                      CssPropertyFactory cssfactory, boolean mode)
            throws IOException {
        this(ac, in, ac.getCharsetForURL(url), url, 0);
        this.visited = urlvisited;
        setURL(url);
        ac.setFrame(new Frame(this, url.toString(), ac.getWarningLevel()));
        setApplContext(ac);
        this.listeners = listeners;
        this.properties = cssfactory;
        this.mode = mode;
    }

    private void ReInit(ApplContext ac, InputStream input,
                        URL file, Frame frame) {
        // reinitialize the parser with a new data input
        // and a new frame for errors and warnings
        super.ReInitWithAc(input, ac, ac.getCharsetForURL(file));
        // @@this is a default media ...
        // AtRuleMedia media;
        // if ("css1".equals(ac.getCssVersionString())) {
        // media = new AtRuleMediaCSS1();
        // } else if ("css2".equals(ac.getCssVersionString())) {
        // media = new AtRuleMedia();
        // } else {
        // media = new AtRuleMedia();
        // }
        /*
         * if (ac.getMedium() == null) { try { media.addMedia("all", ac); }
         * catch (InvalidParamException e) {} //ignore } else { try {
         * media.addMedia(ac.getMedium(), ac); } catch (Exception e) {
         * System.err.println(e.getMessage()); try { media.addMedia("all", ac); }
         * catch (InvalidParamException ex) {} //ignore } } setAtRule(media);
         */
        setURL(file);
        if (Util.onDebug) {
            System.err.println("[DEBUG] CSS version " + ac.getCssVersionString() + " medium " + ac.getMedium() + " profile "
                    + ac.getProfileString());
        }

        String spec = ac.getPropertyKey();

        // load the CssStyle
        String classStyle = PropertiesLoader.config.getProperty(spec);
        if (classStyle == null) {
            spec = CssVersion.getDefault().toString();
            classStyle = PropertiesLoader.config.getProperty(spec);
        }

        Class style;
        try {
            style = Class.forName(classStyle);
            ac.setCssSelectorsStyle(style);
        } catch (ClassNotFoundException e) {
            System.err.println("org.w3c.css.parser.CssFouffa: couldn't" + " load the style");
            e.printStackTrace();
        }

        properties = new CssPropertyFactory(spec);
        // loadConfig(ac.getCssVersionString(), ac.getProfileString());
    }

    /**
     * Reinitializes a new CssFouffa with a data input and a begin line number.
     *
     * @param input     data input
     * @param file      The source file (use for errors, warnings and import)
     * @param beginLine The begin line number in the file. (used for HTML for example)
     * @throws IOException if an I/O error occurs.
     */
    public void ReInit(ApplContext ac, InputStream input, URL file,
                       int beginLine)
            throws IOException {
        Frame f = new Frame(this, file.toString(), beginLine,
                ac.getWarningLevel());
        ac.setFrame(f);
        ReInit(ac, input, file, f);
    }

    /**
     * Reinitializes a new CssFouffa with a data input.
     *
     * @param input data input
     * @param file  The source file (use for errors, warnings and import)
     * @throws IOException if an I/O error occurs.
     */
    public void ReInit(ApplContext ac, InputStream input, URL file)
            throws IOException {
        Frame f = new Frame(this, file.toString(), ac.getWarningLevel());
        ac.setFrame(f);
        ReInit(ac, input, file, f);
    }

    /**
     * Reinitializes a new CssFouffa.
     *
     * @param file The source file (use for data input, errors, warnings and
     *             import)
     * @throws IOException if an I/O error occurs.
     */
    public void ReInit(ApplContext ac, URL file) throws IOException {
        InputStream is;
        URL url;
        Frame f;

        f = new Frame(this, file.toString(), ac.getWarningLevel());
        ac.setFrame(f);
        if (ac.isInputFake()) {
            is = ac.getFakeInputStream(file);
            url = file;
        } else {
            URLConnection urlC = HTTPURL.getConnection(file, ac);
            is = HTTPURL.getInputStream(ac, urlC);
            url = urlC.getURL();
        }
        ReInit(ac, is, url, f);
    }

    /**
     * Set the attribute origin
     *
     * @param origin the new value for the attribute
     */
    private final void setOrigin(int origin) {
        this.origin = origin;
    }

    /**
     * Returns the attribute origin
     *
     * @return the value of the attribute
     */
    public final int getOrigin() {
        return origin;
    }

    /**
     * Adds a listener to the parser.
     *
     * @param listener The listener
     * @see org.w3c.css.parser.CssValidatorListener
     */
    public final void addListener(CssValidatorListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener from the parser
     *
     * @param listener The listener
     * @see org.w3c.css.parser.CssValidatorListener
     */
    public final void removeListener(CssValidatorListener listener) {
        listeners.remove(listener);
    }

    /**
     * Parse the style sheet. This is the main function of this parser.
     * <p/>
     * <p/>
     * Example:<br>
     * <code>
     * CssFouffa parser = new CssFouffa(new
     * URL("http://www.w3.org/drafts.css"));<BR>
     * CssValidatorListener myListener = new MyParserListener();<BR>
     * <BR>
     * parser.addListener(myListener);<BR>
     * parser.parseStyle();<BR>
     * </code>
     *
     * @see org.w3c.css.parser.CssFouffa#addListener
     */
    public void parseStyle() {
        try {
            parserUnit();
        } catch (TokenMgrError e) {
            throw e;
        } catch (Throwable e) {
            if (Util.onDebug) {
                e.printStackTrace();
            }
            RuntimeException ne = new RuntimeException(e.getMessage());
            ne.fillInStackTrace();
            throw (ne);
        }

        Errors errors = ac.getFrame().getErrors();
        Warnings warnings = ac.getFrame().getWarnings();
        // That's all folks, notify all errors and warnings
        for (CssValidatorListener listener : listeners) {
            listener.notifyErrors(errors);
            listener.notifyWarnings(warnings);
        }
    }

    /**
     * Call the namespace declaration statement
     *
     * @param url,    the style sheet where this declaration statement appears.
     * @param prefix, the namespace prefix
     * @param nsname, the file/url name in the declaration statement
     * @param is_url, if the nsname is a file or an url
     */
    public void handleNamespaceDeclaration(URL url, String prefix,
                                           String nsname,
                                           boolean is_url) {
        AtRuleNamespace nsrule = new AtRuleNamespace(prefix, nsname, is_url);
        newAtRule(nsrule);
        endOfAtRule();
        // add the NS in the global context definition
        ac.setNamespace(url, prefix, nsname);
    }

    /**
     * Call by the import statement.
     *
     * @param url  The style sheet where this import statement appears.
     * @param file the file name in the import statement
     */
    public void handleImport(URL url, String file, boolean is_url,
                             AtRuleMedia media) {
        // CssError cssError = null;
        AtRuleImport importrule = new AtRuleImport(file, is_url, media);
        newAtRule(importrule);
        endOfAtRule();

        URL importedURL;
        try {
            importedURL = HTTPURL.getURL(url, file);
        } catch (MalformedURLException mue) {
            if (!Util.noErrorTrace) {
                ac.getFrame()
                        .addError(new CssError(getSourceFile(), getBeginLine(),
                                getBeginColumn(), getEndLine(), getEndColumn(),
                                mue));
            }
            return;
        }

        // add it to the list of linked URIs
        // only if it was a string (otherwise the URI is already there
        // as CssURL contains code to add it directly
        if (!is_url) {
            ac.addLinkedURI(importedURL);
        }
        // check if we need to follow it
        if (!ac.followlinks()) {
            // TODO add a warning ?
            return;
        }

        //if it's not permitted to import... (direct input)
        if (url.getProtocol().equals("file")) {
            ac.getFrame().addWarning("unsupported-import");
            return;
        }

        try {
            String surl = importedURL.toString();

            if (visited == null) {
                visited = new ArrayList<String>();
            } else {
                // check that we didn't already got this URL, or that the
                // number of imports is not exploding
                if (visited.contains(surl)) {
                    CssError cerr = new CssError(getSourceFile(),
                            getBeginLine(), getBeginColumn(), getEndLine(),
                            getEndColumn(), new Exception(
                            "Import loop" + " detected in " + surl));
                    ac.getFrame().addError(cerr);
                    return;
                } else if (visited.size() > 42) {
                    CssError cerr = new CssError(getSourceFile(),
                            getBeginLine(), getBeginColumn(), getEndLine(),
                            getEndColumn(), new Exception("Maximum number"
                            + " of imports " + "reached"));
                    ac.getFrame().addError(cerr);
                    return;
                }
            }
            ArrayList<String> newVisited = new ArrayList<String>(visited);
            newVisited.add(surl);

            if (Util.importSecurity) {
                throw new FileNotFoundException("[SECURITY] You can't " +
                        "import URL sorry.");
            }

            URLConnection importURL = HTTPURL.getConnection(importedURL, ac);
            String charset = HTTPURL.getCharacterEncoding(ac, importURL);

            if (importURL instanceof HttpURLConnection) {
                HttpURLConnection httpURL = (HttpURLConnection) importURL;
                String httpCL = httpURL.getHeaderField("Content-Location");
                if (httpCL != null) {
                    importedURL = HTTPURL.getURL(importedURL, httpCL);
                }
                String mtype = httpURL.getContentType();
                if (mtype == null) {
                    throw new FileNotFoundException(importURL.getURL() +
                            "No Media Type defined");
                } else {
                    if (mtype.toLowerCase().indexOf("text/html") != -1) {
                        throw new FileNotFoundException(importURL.getURL() +
                                ": You can't import" +
                                " an HTML document");
                    }
                }
            }
            Frame f = ac.getFrame();
            try {
                CssFouffa cssFouffa = new CssFouffa(ac,
                        HTTPURL.getInputStream(ac, importURL),
                        importedURL, listeners, newVisited,
                        properties, mode);
                cssFouffa.setOrigin(getOrigin());
                if (!media.isEmpty()) {
                    cssFouffa.setAtRule(media);
                } else {
                    cssFouffa.setAtRule(getAtRule());
                }
                cssFouffa.parseStyle();
            } finally {
                ac.setFrame(f);
            }
        } catch (Exception e) {
            if (!Util.noErrorTrace) {
                ac.getFrame()
                        .addError(new CssError(getSourceFile(), getBeginLine(),
                                getBeginColumn(), getEndLine(), getEndColumn(),
                                e));
            }
        }
    }

    /**
     * Call by the at-rule statement.
     *
     * @param ident  The ident for this at-rule (for example: 'font-face')
     * @param string The string representation of this at-rule
     */
    public void handleAtRule(String ident, String string) {
        if (mode) {
            for (CssValidatorListener listener : listeners) {
                listener.handleAtRule(ac, ident, string);

            }
        } else {
            if (!Util.noErrorTrace) {
                // only @import <string>; or @import <url>; are valids in CSS1
                ParseException error = new ParseException("at-rules are not implemented in CSS1");
                ac.getFrame()
                        .addError(new CssError(getSourceFile(), getBeginLine(),
                                getBeginColumn(), getEndLine(), getEndColumn(),
                                error));
            }
        }
    }

    /**
     * Treat the "\9" CSS declaration hack as a vendor extension warning
     * rather than a fatal error?
     */
    private boolean allowBackslash9Hack() {
        return this.ac.getTreatCssHacksAsWarnings();
    }

    /**
     * Assign an expression to a property. This function create a new property
     * with <code>property</code> and assign to it the expression with the
     * importance.
     *
     * @param property   the name of the property
     * @param expression The expression representation of expression
     * @param important  true if expression id important
     * @return a CssProperty
     * @throw InvalidParamException
     * An error appears during the property creation.
     */
    public CssProperty handleDeclaration(String property, CssExpression expression, boolean important)
            throws InvalidParamException {
        CssProperty prop;
        if (Util.onDebug) {
            System.err.println("Creating " + property + ": " + expression);
        }

        if (property.startsWith("--") && (ac.getCssVersion().compareTo(CssVersion.CSS3) >= 0)) {
            prop = new CssCustomProperty(ac, property, expression);
            // css variable
        } else {
            final CssValue lastValue = expression.getLastValue();

            if (allowBackslash9Hack() && lastValue != null && lastValue.hasBackslash9Hack()) {
                expression.markCssHack();
            }

            try {
                prop = properties.createProperty(ac, getAtRule(), property, expression);
            } catch (InvalidParamException e) {
                throw e;
            } catch (Exception e) {
                if (Util.onDebug) {
                    e.printStackTrace();
                }
                throw new InvalidParamException(e.toString(), ac);
            }

            // set the importance
            if (important) {
                prop.setImportant();
            }
        }
        prop.setOrigin(origin);
        // set informations for errors and warnings
        prop.setInfo(ac.getFrame().getLine(), ac.getFrame().getSourceFile());

        // ok, return the new property
        return prop;

    }

    /**
     * Assign an expression to a MediaFeature. This function create a new
     * media feature with <code>feature</code> and assign to it the expression
     *
     * @param feature    the name of the media feature
     * @param expression The expression representation of expression
     * @return a CssProperty
     * @throw InvalidParamException
     * An error appears during the property creation.
     */
    public MediaFeature handleMediaFeature(AtRuleMedia rule, String feature,
                                           CssExpression expression)
            throws InvalidParamException {
        MediaFeature mf;
        if (Util.onDebug) {
            System.err.println("Creating MediaFeature" + feature + ": " + expression);
        }

        try {
            mf = properties.createMediaFeature(ac, rule, feature, expression);
        } catch (WarningParamException w) {
            ac.getFrame().addWarning(w.getMessage(), feature);
            return null;
        } catch (InvalidParamException e) {
            ac.getFrame().addError(new CssError(getSourceFile(), getBeginLine(),
                    getBeginColumn(), getEndLine(), getEndColumn(), e));
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            if (Util.onDebug) {
                e.printStackTrace();
            }
            throw new InvalidParamException(e.toString(), ac);
        }
        mf.setOrigin(origin);
        // set informations for errors and warnings
        mf.setInfo(ac.getFrame().getLine(), ac.getFrame().getSourceFile());
        // ok, return the new property
        return mf;
    }

    /**
     * Parse only a list of declarations. This is useful to parse the
     * <code>STYLE</code> attribute in a HTML document.
     * <p/>
     * <p/>
     * Example:<br>
     * <code>
     * CssFouffa parser =
     * new CssFouffa(new URL("http://www.w3.org/drafts.css"));<BR>
     * CssValidatorListener myListener = new MyParserListener();<BR>
     * CssSelector selector = new CssSelector();
     * selector.setElement("H1");
     * <BR>
     * parser.addListener(myListener);<BR>
     * parser.parseDeclarations(selector);<BR>
     * </code>
     *
     * @param context The current context
     * @see org.w3c.css.parser.CssFouffa#addListener
     */

    public void parseDeclarations(CssSelectors context) {
        // here we have an example for reusing the parser.
        try {
            ArrayList<CssProperty> properties = attributeDeclarations();

            if (properties != null && properties.size() != 0) {
                handleRule(context, properties);
            }
        } catch (ParseException e) {
            if (!Util.noErrorTrace) {
                CssParseException ex = new CssParseException(e);
                ex.skippedString = "";
                ex.property = currentProperty;
                ex.contexts = currentContext;
                CssError error = new CssError(getSourceFile(), getBeginLine(),
                        getBeginColumn(), getEndLine(), getEndColumn(), ex);
                ac.getFrame().addError(error);
            }
        }

        if (!Util.noErrorTrace) {
            Errors errors = ac.getFrame().getErrors();
            Warnings warnings = ac.getFrame().getWarnings();

            for (CssValidatorListener listener : listeners) {
                listener.notifyErrors(errors);
                listener.notifyWarnings(warnings);
            }
        }
    }

    /**
     * used for the output of the stylesheet
     *
     * @param atRule the
     * @rule that just has been found by the parser in the stylesheet, it is
     * added to the storage for the output
     */
    public void newAtRule(AtRule atRule) {
        for (CssValidatorListener listener : listeners) {
            listener.newAtRule(atRule);
        }
    }

    /**
     * used for the output of the stylesheet
     *
     * @param charset the
     * @charset rule that has been found by the parser
     */
    public void addCharSet(String charset) throws ParseException {
        for (CssValidatorListener listener : listeners) {
            listener.addCharSet(charset);
        }

        Charset c = null;
        try {
            c = Charset.forName(charset);
        } catch (Exception ex) {
            return;
        }
        boolean charsetFromBOM = ac.isCharsetFromBOM(getURL());
        if (charsetFromBOM && ac.getCssVersion().compareTo(CssVersion.CSS3) >= 0) {
            // TODO FIXME proper execption type.
            throw new ParseException(ac.getMsg().getString("parser.charset"));
            //     CssError cerr = new CssError(getSourceFile(), getBeginLine(),
            //         getBeginColumn(), getEndLine(), getEndColumn(), ex);
            //   ac.getFrame().addError(cerr);
        } else {
            Charset originalCharset = ac.getCharsetObjForURL(getURL());
            if (originalCharset == null) {
                ac.setCharsetForURL(getURL(), c);
                try {
                    ReInit(ac, getURL());
                } catch (IOException ioex) {
                }
            } else if (c.compareTo(originalCharset) != 0) {
                InvalidParamException ex = new InvalidParamException("conflicting-charset",
                        new String[]{originalCharset.toString(), charset}, ac);
                CssError cerr = new CssError(getSourceFile(), getBeginLine(),
                        getBeginColumn(), getEndLine(), getEndColumn(), ex);
                ac.getFrame().addError(cerr);
            }
        }
    }

    /**
     * used for the output of the stylesheet the
     *
     * @rule that had been found before is closed here after the content that's
     * in it.
     */
    public void endOfAtRule() {
        for (CssValidatorListener listener : listeners) {
            listener.endOfAtRule();
        }
    }

    /**
     * used for the output of the stylesheet
     *
     * @param important true if the rule was declared important in the stylesheet
     */
    public void setImportant(boolean important) {
        for (CssValidatorListener listener : listeners) {
            listener.setImportant(important);
        }
    }

    /**
     * used for the output of the stylesheet
     *
     * @param selectors a list of one or more selectors to be added to the output
     *                  stylesheet
     */
    public void setSelectorList(ArrayList<CssSelectors> selectors) {
        for (CssValidatorListener listener : listeners) {
            listener.setSelectorList(selectors);
        }
    }

    /**
     * used for the output of the stylesheet
     *
     * @param properties A list of properties that are following eachother in the
     *                   stylesheet (for example: all properties in an
     * @rule)
     */
    public void addProperty(ArrayList<CssProperty> properties) {
        for (CssValidatorListener listener : listeners) {
            listener.setProperty(properties);
        }
    }

    /**
     * used for the output of the stylesheet used to close a rule when it has
     * been read by the parser
     */
    public void endOfRule() {
        for (CssValidatorListener listener : listeners) {
            listener.endOfRule();
        }
    }

    /**
     * used for the output of the stylesheet if an error is found this function
     * is used to remove the whole stylerule from the memorystructure so that it
     * won't appear on the screen
     */
    public void removeThisRule() {
        for (CssValidatorListener listener : listeners) {
            listener.removeThisRule();
        }
    }

    /**
     * used for the output of the stylesheet if an error is found this function
     * is used to remove the whole
     *
     * @rule from the memorystructure so that it won't appear on the screen
     */
    public void removeThisAtRule() {
        for (CssValidatorListener listener : listeners) {
            listener.removeThisAtRule();
        }
    }

    /**
     * Adds a vector of properties to a selector.
     *
     * @param selector     the selector
     * @param declarations Properties to associate with contexts
     */
    public void handleRule(CssSelectors selector, ArrayList<CssProperty> declarations) {
        for (CssValidatorListener listener : listeners) {
            listener.handleRule(ac, selector, declarations);
        }
    }

    /**
     * Return the class name for a property
     *
     * @param property the property name ('font-size' for example)
     * @return the class name ('org.w3c.css.properties.CssFontSize' for example)
     */
    public String getProperty(String property) {
        return properties.getProperty(property);
    }

    /**
     * Set the style
     */
    public void setStyle(Class style) {
        ac.setCssSelectorsStyle(style);
    }


    public CssFouffa(java.io.InputStream stream) {
        super(stream);
        properties = new CssPropertyFactory("css21");
        // loadConfig("css2", null);
    }

    public CssFouffa(java.io.Reader stream) {
        super(stream);
        properties = new CssPropertyFactory("css21");
        // loadConfig("css2", null);
    }

    public CssFouffa(CssParserTokenManager tm) {
        super(tm);
        properties = new CssPropertyFactory("css21");
        // loadConfig("css2", null);
    }

    public CssFouffa(ApplContext ac, Reader reader) {
        super(reader);
        this.ac = ac;
        properties = new CssPropertyFactory(ac.getPropertyKey());
    }

}
