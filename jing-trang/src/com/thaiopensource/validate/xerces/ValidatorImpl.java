package com.thaiopensource.validate.xerces;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.validation.EntityState;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.DTDHandler;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.util.Hashtable;

class ValidatorImpl extends ParserConfigurationSettings implements Validator, ContentHandler, DTDHandler, XMLLocator, XMLEntityResolver, EntityState {

  private final XMLSchemaValidator schemaValidator = new XMLSchemaValidator();
  private final XMLErrorReporter errorReporter = new XMLErrorReporter();
  private final ValidationManager validationManager = new ValidationManager();
  private final NamespaceContext namespaceContext = new NamespaceSupport();
  private final XMLAttributes attributes = new XMLAttributesImpl();
  private final SymbolTable symbolTable;
  private final XMLComponent[] components;
  private Locator locator;
  private final Hashtable entityTable = new Hashtable();
  private boolean pushedContext = false;

  // XXX deal with baseURI

  static private final String[] recognizedFeatures = {
    Features.SCHEMA_AUGMENT_PSVI,
    Features.SCHEMA_FULL_CHECKING,
    Features.VALIDATION,
    Features.SCHEMA_VALIDATION,
  };

  static private final String[] recognizedProperties = {
    Properties.XMLGRAMMAR_POOL,
    Properties.SYMBOL_TABLE,
    Properties.ERROR_REPORTER,
    Properties.ERROR_HANDLER,
    Properties.VALIDATION_MANAGER,
    Properties.ENTITY_MANAGER,
    Properties.ENTITY_RESOLVER,
  };

  ValidatorImpl(SymbolTable symbolTable, XMLGrammarPool grammarPool, PropertyMap properties) {
    this.symbolTable = symbolTable;
    XMLErrorHandler errorHandlerWrapper = new ErrorHandlerWrapper(ValidateProperty.ERROR_HANDLER.get(properties));
    components = new XMLComponent[] { errorReporter, schemaValidator };
    for (int i = 0; i < components.length; i++) {
      addRecognizedFeatures(components[i].getRecognizedFeatures());
      addRecognizedProperties(components[i].getRecognizedProperties());
    }
    addRecognizedFeatures(recognizedFeatures);
    addRecognizedProperties(recognizedProperties);
    setFeature(Features.SCHEMA_AUGMENT_PSVI, false);
    setFeature(Features.SCHEMA_FULL_CHECKING, true);
    setFeature(Features.VALIDATION, true);
    setFeature(Features.SCHEMA_VALIDATION, true);
    setProperty(Properties.XMLGRAMMAR_POOL, grammarPool);
    setProperty(Properties.SYMBOL_TABLE, symbolTable);
    errorReporter.setDocumentLocator(this);
    setProperty(Properties.ERROR_REPORTER, errorReporter);
    setProperty(Properties.ERROR_HANDLER, errorHandlerWrapper);
    setProperty(Properties.VALIDATION_MANAGER, validationManager);
    // In Xerces 2.4.0, XMLSchemaValidator uses ENTITY_MANAGER when
    // it should use ENTITY_RESOLVER
    setProperty(Properties.ENTITY_MANAGER, this);
    setProperty(Properties.ENTITY_RESOLVER, this);
    reset();
  }

  public void reset() {
    validationManager.reset();
    namespaceContext.reset();
    for (int i = 0; i < components.length; i++)
      components[i].reset(this);
    validationManager.setEntityState(this);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId) {
    // nothing needed
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName) {
    entityTable.put(name, name);
  }

  public boolean isEntityDeclared(String name) {
    return entityTable.get(name) != null;
  }

  public boolean isEntityUnparsed(String name) {
    return entityTable.get(name) != null;
  }

  public void startDocument()
          throws SAXException {
    try {
      schemaValidator.startDocument(locator == null ? null : this, null, namespaceContext, null);
    }
    catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endDocument()
          throws SAXException {
    try {
      schemaValidator.endDocument(null);
    }
    catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void startElement(String namespaceURI, String localName,
                           String qName, Attributes atts)
          throws SAXException {
    try {
      if (!pushedContext)
        namespaceContext.pushContext();
      else
        pushedContext = false;
      for (int i = 0, len = atts.getLength(); i < len; i++)
        attributes.addAttribute(makeQName(atts.getURI(i), atts.getLocalName(i), atts.getQName(i)),
                                symbolTable.addSymbol(atts.getType(i)),
                                atts.getValue(i));
      schemaValidator.startElement(makeQName(namespaceURI, localName, qName), attributes, null);
      attributes.removeAllAttributes();
    }
    catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endElement(String namespaceURI, String localName,
                         String qName)
          throws SAXException {
    try {
      schemaValidator.endElement(makeQName(namespaceURI, localName, qName), null);
      namespaceContext.popContext();
    }
    catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void startPrefixMapping(String prefix, String uri)
          throws SAXException {
    try {
      if (!pushedContext) {
        namespaceContext.pushContext();
        pushedContext = true;
      }
      if (prefix == null)
        prefix = XMLSymbols.EMPTY_STRING;
      else
        prefix = symbolTable.addSymbol(prefix);
      if (uri != null) {
        if (uri.equals(""))
          uri = null;
        else
          uri = symbolTable.addSymbol(uri);
      }
      namespaceContext.declarePrefix(prefix, uri);
    }
    catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void endPrefixMapping(String prefix)
          throws SAXException {
    // do nothing
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    try {
      schemaValidator.characters(new XMLString(ch, start, length), null);
    }
    catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    try {
      schemaValidator.ignorableWhitespace(new XMLString(ch, start, length), null);
    }
    catch (XNIException e) {
      throw toSAXException(e);
    }
  }

  public void processingInstruction(String target, String data)
          throws SAXException {
    // do nothing
  }

  public void skippedEntity(String name)
          throws SAXException {
    // do nothing
  }

  private QName makeQName(String namespaceURI, String localName, String qName) {
    localName = symbolTable.addSymbol(localName);
    String prefix;
    if (namespaceURI.equals("")) {
      namespaceURI = null;
      prefix = XMLSymbols.EMPTY_STRING;
      qName = localName;
    }
    else {
      namespaceURI = symbolTable.addSymbol(namespaceURI);
      if (qName.equals("")) {
        prefix = namespaceContext.getPrefix(namespaceURI);
        if (prefix == XMLSymbols.EMPTY_STRING)
          qName = localName;
        else if (prefix == null)
          qName = localName; // XXX what to do?
        else
          qName = symbolTable.addSymbol(prefix + ":" + localName);
      }
      else {
        qName = symbolTable.addSymbol(qName);
        int colon = qName.indexOf(':');
        if (colon > 0)
          prefix = symbolTable.addSymbol(qName.substring(0, colon));
        else
          prefix = XMLSymbols.EMPTY_STRING;
      }
    }
    return new QName(prefix, localName, qName, namespaceURI);
  }

  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
          throws XNIException, IOException {
    return null;
  }

  public String getPublicId() {
    return locator.getPublicId();
  }

  public String getEncoding() {
    return null;
  }

  public String getBaseSystemId() {
    return null;
  }

  public String getLiteralSystemId() {
    return null;
  }

  public String getExpandedSystemId() {
    return locator.getSystemId();
  }

  public int getLineNumber() {
    return locator.getLineNumber();
  }

  public int getColumnNumber() {
    return locator.getColumnNumber();
  }

  static SAXException toSAXException(XNIException e) {
    if (e instanceof XMLParseException) {
      XMLParseException pe = (XMLParseException)e;
      return new SAXParseException(pe.getMessage(),
                                   pe.getPublicId(),
                                   pe.getExpandedSystemId(),
                                   pe.getLineNumber(),
                                   pe.getColumnNumber(),
                                   pe.getException());
    }
    Exception nested = e.getException();
    if (nested == null)
      return new SAXException(e.getMessage());
    if (nested instanceof SAXException)
      return (SAXException)nested;
    if (nested instanceof RuntimeException)
      throw (RuntimeException)nested;
    return new SAXException(nested);
  }
}
