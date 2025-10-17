package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.impl.SchemaBuilderImpl;
import com.thaiopensource.relaxng.impl.SchemaPatternBuilder;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.parse.sax.UriResolverImpl;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import com.thaiopensource.xml.sax.Resolver;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.VerifierFactory;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

public class VerifierFactoryImpl extends VerifierFactory {
  private final DatatypeLibraryFactory dlf = new DatatypeLibraryLoader();
  private final ErrorHandler eh = new DraconianErrorHandler();

  public VerifierFactoryImpl() { }

  public Schema compileSchema(InputSource inputSource) throws SAXException, IOException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    Resolver resolver = Resolver.newInstance(getEntityResolver());
    Parseable parseable = new SAXParseable(new SAXSource(resolver.createXMLReader(), inputSource),
                                           new UriResolverImpl(resolver),
                                           eh);
    try {
      return new SchemaImpl(SchemaBuilderImpl.parse(parseable, eh, dlf, spb, false), spb);
    }
    catch (IllegalSchemaException e) {
      throw new SAXException("unreported schema error");
    }
  }
}
