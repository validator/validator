package com.thaiopensource.relaxng.match;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;
import com.thaiopensource.relaxng.impl.SchemaPatternBuilder;
import com.thaiopensource.relaxng.impl.SchemaBuilderImpl;
import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.FeasibleTransform;
import com.thaiopensource.relaxng.impl.MatchablePatternImpl;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;

import java.io.IOException;

/**
 * Provides method to load a MatchablePattern by parsing.
 */
public class MatchablePatternLoader {
  public static final int COMPACT_SYNTAX_FLAG = 01;
  public static final int FEASIBLE_FLAG = 02;
  public MatchablePattern load(InputSource in,
                               ErrorHandler eh,
                               DatatypeLibraryFactory dlf,
                               XMLReaderCreator xrc,
                               int flags) throws IOException, SAXException, IncorrectSchemaException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    Parseable parseable;
    if ((flags & COMPACT_SYNTAX_FLAG) != 0)
      parseable = new CompactParseable(in, eh);
    else {
      if (xrc == null)
        xrc = new Jaxp11XMLReaderCreator();
      parseable = new SAXParseable(xrc, in, eh);
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
