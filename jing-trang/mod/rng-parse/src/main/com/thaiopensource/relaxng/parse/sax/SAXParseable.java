package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.SubParseable;
import com.thaiopensource.util.Uri;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

public class SAXParseable extends SAXSubParser implements SubParseable {
  private final SAXSource source;

  /**
   *
   * @param source  XMLReader must be non-null
   * @param resolver
   * @param eh
   */
  public SAXParseable(SAXSource source, UriResolver resolver, ErrorHandler eh) {
    super(resolver, eh);
    this.source = source;
  }

  public ParsedPattern parse(SchemaBuilder schemaBuilder, Scope scope) throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = source.getXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, null, scope);
      xr.parse(source.getInputSource());
      return sp.getParsedPattern();
    }
    catch (SAXException e) {
      throw BuildException.fromSAXException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

  public ParsedPattern parseAsInclude(SchemaBuilder schemaBuilder, IncludedGrammar g)
          throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = source.getXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, g, g);
      xr.parse(source.getInputSource());
      return sp.getParsedPattern();
    }
    catch (SAXException e) {
      throw BuildException.fromSAXException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

  public String getUri() {
    return Uri.escapeDisallowedChars(source.getInputSource().getSystemId());
  }
}
