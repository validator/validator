package com.thaiopensource.relaxng.match;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.impl.FeasibleTransform;
import com.thaiopensource.relaxng.impl.MatchablePatternImpl;
import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.SchemaBuilderImpl;
import com.thaiopensource.relaxng.impl.SchemaPatternBuilder;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.relaxng.parse.compact.UriOpenerImpl;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.parse.sax.UriResolverImpl;
import com.thaiopensource.xml.sax.Resolver;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

/**
 * Provides method to load a MatchablePattern by parsing.
 */
public class MatchablePatternLoader {
  public static final int COMPACT_SYNTAX_FLAG = 01;
  public static final int FEASIBLE_FLAG = 02;
  public MatchablePattern load(SAXSource source,
                               URIResolver uriResolver,
                               ErrorHandler eh,
                               DatatypeLibraryFactory dlf,
                               int flags) throws IOException, SAXException, IncorrectSchemaException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    Parseable parseable;
    Resolver resolver = Resolver.newInstance(uriResolver);
    if ((flags & COMPACT_SYNTAX_FLAG) != 0)
      parseable = new CompactParseable(source.getInputSource(), new UriOpenerImpl(resolver), eh);
    else {
      if (source.getXMLReader() == null)
        source = new SAXSource(resolver.createXMLReader(), source.getInputSource());
      parseable = new SAXParseable(source, new UriResolverImpl(resolver), eh);
    }
    if (dlf == null)
      dlf = new DatatypeLibraryLoader();
    try {
      Pattern start = SchemaBuilderImpl.parse(parseable, eh, dlf, spb, false);
      if ((flags & FEASIBLE_FLAG) != 0)
        start = FeasibleTransform.transform(spb, start);
      return new MatchablePatternImpl(spb, start);
    }
    catch (IllegalSchemaException e) {
      throw new IncorrectSchemaException();
    }
  }
}
