package com.thaiopensource.relaxng.input.dtd;

import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.translate.util.InvalidParamsException;
import com.thaiopensource.relaxng.translate.util.ParamProcessor;
import com.thaiopensource.relaxng.translate.util.AbstractParam;
import com.thaiopensource.relaxng.translate.util.Param;
import com.thaiopensource.relaxng.translate.util.ParamFactory;
import com.thaiopensource.relaxng.translate.util.AbsoluteUriParam;
import com.thaiopensource.relaxng.translate.util.NCNameParam;
import com.thaiopensource.relaxng.translate.util.NmtokenParam;
import com.thaiopensource.relaxng.translate.util.InvalidParamValueException;
import com.thaiopensource.xml.dtd.om.Dtd;
import com.thaiopensource.xml.dtd.parse.DtdParserImpl;
import com.thaiopensource.xml.dtd.parse.ParseException;
import com.thaiopensource.xml.dtd.app.UriEntityManager;
import com.thaiopensource.xml.util.Naming;
import com.thaiopensource.util.Localizer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.util.Map;

public class DtdInputFormat implements InputFormat {
  static private class NamespaceDeclParamFactory implements ParamFactory {
    private final Map prefixMap;

    NamespaceDeclParamFactory(Map prefixMap) {
      this.prefixMap = prefixMap;
    }

    public Param createParam(String name) {
      if (!name.startsWith("xmlns:"))
        return null;
      final String prefix = name.substring(6);
      if (!Naming.isNcname(prefix))
        return null;
      return new AbsoluteUriParam() {
        public void setAbsoluteUri(String uri) {
          prefixMap.put(prefix, uri);
        }
      };
    }
  }

  static private abstract class DeclPatternParam extends AbstractParam {
    private final Localizer localizer;

    DeclPatternParam(Localizer localizer) {
      this.localizer = localizer;
    }

    public void set(String value) throws InvalidParamValueException {
      if (value.indexOf('%') < 0)
        throw new InvalidParamValueException(localizer.message("no_percent"));
      if (value.lastIndexOf('%') != value.indexOf('%'))
        throw new InvalidParamValueException(localizer.message("multiple_percent"));
      if (!Naming.isNcname(value.replace('%', 'x')))
        throw new InvalidParamValueException(localizer.message("not_ncname_with_percent"));
      setDeclPattern(value);
    }

    abstract void setDeclPattern(String pattern);
  }

  public SchemaCollection load(String uri, String[] params, String outputFormat, ErrorHandler eh)
          throws InvalidParamsException, IOException, SAXException {
    final ErrorReporter er = new ErrorReporter(eh, DtdInputFormat.class);
    final Converter.Options options = new Converter.Options();
    if ("xsd".equals(outputFormat)) {
      options.inlineAttlistDecls = true;
      options.generateStart = false;
    }
    ParamProcessor pp = new ParamProcessor();
    pp.declare("inline-attlist",
               new AbstractParam() {
                 public void set(boolean value) {
                   options.inlineAttlistDecls = value;
                 }
               });
    pp.declare("xmlns",
               new AbsoluteUriParam() {
                 public void set(String value) throws InvalidParamValueException {
                   if (value.equals(""))
                    setAbsoluteUri(value);
                   else
                    super.set(value);
                 }

                 protected void setAbsoluteUri(String value) {
                   options.defaultNamespace = value;
                 }
               });
    pp.declare("any-name",
               new NCNameParam() {
                 protected void setNCName(String value) {
                   options.anyName = value;
                 }
               });
    pp.declare("strict-any",
               new AbstractParam() {
                 public void set(boolean value) {
                   options.strictAny = value;
                 }
               });
    pp.declare("annotation-prefix",
               new NCNameParam() {
                 protected void setNCName(String value) {
                   options.annotationPrefix = value;
                 }
               });
    pp.declare("colon-replacement",
               new NmtokenParam() {
                 protected void setNmtoken(String value) {
                   options.colonReplacement = value;
                 }
               });
    pp.declare("generate-start",
               new AbstractParam() {
                 public void set(boolean value) {
                   options.generateStart = value;
                 }
               });
    pp.declare("element-define",
               new DeclPatternParam(er.getLocalizer()) {
                 void setDeclPattern(String pattern) {
                   options.elementDeclPattern = pattern;
                 }
               });
    pp.declare("attlist-define",
               new DeclPatternParam(er.getLocalizer()) {
                 void setDeclPattern(String pattern) {
                   options.attlistDeclPattern = pattern;
                 }
               });
    pp.setParamFactory(new NamespaceDeclParamFactory(options.prefixMap));
    pp.process(params, eh);
    try {
      Dtd dtd = new DtdParserImpl().parse(uri, new UriEntityManager());
      try {
        return new Converter(dtd, er, options).convert();
      }
      catch (ErrorReporter.WrappedSAXException e) {
        throw e.getException();
      }
    }
    catch (ParseException e) {
      throw new SAXParseException(e.getMessageBody(), null, e.getLocation(), e.getLineNumber(), e.getColumnNumber());
    }
  }

}
