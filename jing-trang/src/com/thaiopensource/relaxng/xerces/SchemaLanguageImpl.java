package com.thaiopensource.relaxng.xerces;

import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.parsers.CachingParserPool;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.util.SynchronizedSymbolTable;

import java.io.IOException;

class SchemaLanguageImpl implements SchemaLanguage {
  public Schema createSchema(XMLReaderCreator xrc, InputSource in, ErrorHandler eh, SchemaOptions options, DatatypeLibraryFactory dlf)
          throws IOException, SAXException, IncorrectSchemaException {
    SymbolTable symbolTable = new SymbolTable();
    XMLGrammarPreparser preparser = new XMLGrammarPreparser(symbolTable);
    XMLGrammarPool grammarPool = new XMLGrammarPoolImpl();
    preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
    preparser.setGrammarPool(grammarPool);
    SAXXMLErrorHandler xeh = new SAXXMLErrorHandler(eh);
    preparser.setErrorHandler(xeh);
    try {
      preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, toXMLInputSource(in));
    }
    catch (XNIException e) {
      throw ValidatorHandlerImpl.toSAXException(e);
    }
    if (xeh.getHadError())
      throw new IncorrectSchemaException();
    return new SchemaImpl(new SynchronizedSymbolTable(symbolTable),
                          new CachingParserPool.SynchronizedGrammarPool(grammarPool));
  }

  private static XMLInputSource toXMLInputSource(InputSource in) {
    XMLInputSource xin = new XMLInputSource(in.getPublicId(), in.getSystemId(), null);
    xin.setByteStream(in.getByteStream());
    xin.setCharacterStream(in.getCharacterStream());
    xin.setEncoding(in.getEncoding());
    return xin;
  }
}
