package com.thaiopensource.validate.xerces;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.xerces.SAXXMLErrorHandler;
import com.thaiopensource.validate.xerces.SchemaImpl;
import org.apache.xerces.parsers.CachingParserPool;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.SynchronizedSymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.util.EntityResolverWrapper;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

import java.io.IOException;

class SchemaReaderImpl implements SchemaReader {
  private static final PropertyId[] supportedPropertyIds = {
    ValidateProperty.ERROR_HANDLER,
    ValidateProperty.ENTITY_RESOLVER,
  };
  public Schema createSchema(InputSource in, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    SymbolTable symbolTable = new SymbolTable();
    XMLGrammarPreparser preparser = new XMLGrammarPreparser(symbolTable);
    XMLGrammarPool grammarPool = new XMLGrammarPoolImpl();
    preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
    preparser.setGrammarPool(grammarPool);
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    SAXXMLErrorHandler xeh = new SAXXMLErrorHandler(eh);
    preparser.setErrorHandler(xeh);
    EntityResolver er = ValidateProperty.ENTITY_RESOLVER.get(properties);
    if (er != null)
      preparser.setEntityResolver(new EntityResolverWrapper(er));
    try {
      preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, toXMLInputSource(in));
    }
    catch (XNIException e) {
      throw ValidatorImpl.toSAXException(e);
    }
    if (xeh.getHadError())
      throw new IncorrectSchemaException();
    return new SchemaImpl(new SynchronizedSymbolTable(symbolTable),
                          new CachingParserPool.SynchronizedGrammarPool(grammarPool),
                          properties,
                          supportedPropertyIds);
  }

  public Option getOption(String uri) {
    return null;
  }

  private static XMLInputSource toXMLInputSource(InputSource in) {
    XMLInputSource xin = new XMLInputSource(in.getPublicId(), in.getSystemId(), null);
    xin.setByteStream(in.getByteStream());
    xin.setCharacterStream(in.getCharacterStream());
    xin.setEncoding(in.getEncoding());
    return xin;
  }
}
