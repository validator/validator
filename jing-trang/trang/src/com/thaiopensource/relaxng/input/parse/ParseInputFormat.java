package com.thaiopensource.relaxng.input.parse;

import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.InputFailedException;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;

import java.io.IOException;

public abstract class ParseInputFormat implements InputFormat {
  private final boolean commentsNeedTrimming;
  protected ParseInputFormat(boolean commentsNeedTrimming) {
    this.commentsNeedTrimming = commentsNeedTrimming;
  }
  public SchemaCollection load(String uri, String encoding, ErrorHandler eh) throws InputFailedException, IOException, SAXException {
    InputSource in = new InputSource(uri);
    if (encoding != null)
      in.setEncoding(encoding);
    Parseable parseable = makeParseable(in, eh);
    try {
      return SchemaBuilderImpl.parse(parseable,
                                     eh,
                                     new DatatypeLibraryLoader(),
                                     commentsNeedTrimming);
    }
    catch (IncorrectSchemaException e) {
      throw new InputFailedException();
    }
  }

  protected abstract Parseable makeParseable(InputSource in, ErrorHandler eh);
}
