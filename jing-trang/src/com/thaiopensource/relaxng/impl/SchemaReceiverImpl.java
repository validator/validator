package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.auto.SchemaReceiver;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.auto.SchemaFuture;
import com.thaiopensource.relaxng.parse.ParseReceiver;
import com.thaiopensource.relaxng.parse.BuildException;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;

public class SchemaReceiverImpl implements SchemaReceiver {
  private final ParseReceiver parser;
  private final ErrorHandler eh;
  private final DatatypeLibraryFactory dlf;
  private final SchemaOptions options;

  public SchemaReceiverImpl(ParseReceiver parser, ErrorHandler eh, SchemaOptions options, DatatypeLibraryFactory dlf) {
    this.parser = parser;
    this.eh = eh;
    this.options = options;
    this.dlf = dlf;
  }

  public SchemaFuture installHandlers(XMLReader xr) throws SAXException {
    final SchemaPatternBuilder pb = new SchemaPatternBuilder();
    final PatternFuture pf = SchemaBuilderImpl.installHandlers(parser, xr, eh, dlf, pb);
    return new SchemaFuture() {
      public Schema getSchema() throws IncorrectSchemaException, SAXException, IOException {
        return SchemaLanguageImpl.wrapPattern(pf.getPattern(options.contains(SchemaOptions.ATTRIBUTES)),
                                              pb, eh, options);
      }
      public RuntimeException unwrapException(RuntimeException e) throws SAXException, IOException, IncorrectSchemaException {
        if (e instanceof BuildException)
          return SchemaBuilderImpl.unwrapBuildException((BuildException)e);
        return e;
      }
    };
  }
}
