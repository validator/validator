package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.XMLReaderCreator;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

import java.io.IOException;

public class SAXParseable implements Parseable {
  static public final String URI = SchemaParser.relaxng10URI;
  private XMLReaderCreator xrc;
  private InputSource in;
  private ErrorHandler eh;

  public SAXParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh) {
    this.xrc = xrc;
    this.in = in;
    this.eh = eh;
  }

  public ParsedPattern parse(SchemaBuilder schemaBuilder, Scope scope) throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = xrc.createXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, null, scope);
      xr.parse(in);
      return sp.getStartPattern();
    }
    catch (SAXException e) {
      throw toBuildException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

  public ParsedPattern parseInclude(String uri, SchemaBuilder schemaBuilder, IncludedGrammar g)
          throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = xrc.createXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, g, g);
      xr.parse(makeInputSource(xr, uri));
      return sp.getStartPattern();
    }
    catch (SAXException e) {
     throw toBuildException(e);
    }
    catch (IOException e) {
     throw new BuildException(e);
    }
  }

  public ParsedPattern parseExternal(String uri, SchemaBuilder schemaBuilder, Scope s)
          throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = xrc.createXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, null, s);
      xr.parse(makeInputSource(xr, uri));
      return sp.getStartPattern();
    }
    catch (SAXException e) {
      throw toBuildException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

  private InputSource makeInputSource(XMLReader xr, String systemId) throws IOException, SAXException {
    EntityResolver er = xr.getEntityResolver();
    if (er != null) {
      InputSource inputSource = er.resolveEntity(null, systemId);
      if (inputSource != null)
	return inputSource;
    }
    return new InputSource(systemId);
  }

  private static BuildException toBuildException(SAXException e) {
    Exception inner = e.getException();
    if (inner instanceof BuildException)
      throw (BuildException)inner;
    throw new BuildException(e);
  }
}
