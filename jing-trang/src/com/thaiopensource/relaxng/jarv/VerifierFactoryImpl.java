package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.SchemaPatternBuilder;
import com.thaiopensource.relaxng.impl.SchemaBuilderImpl;
import com.thaiopensource.relaxng.util.DraconianErrorHandler;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.VerifierFactory;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

public class VerifierFactoryImpl extends VerifierFactory {
  private final XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();
  private final DatatypeLibraryFactory dlf = new DatatypeLibraryLoader();
  private final ErrorHandler eh = new DraconianErrorHandler();

  public VerifierFactoryImpl() { }

  public Schema compileSchema(InputSource is) throws SAXException, IOException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    Parseable parseable = new SAXParseable(xrc, is, eh);
    try {
      return new SchemaImpl(SchemaBuilderImpl.parse(parseable, eh, dlf, spb), spb);
    }
    catch (IncorrectSchemaException e) {
      throw new SAXException("unreported schema error");
    }
  }
}
