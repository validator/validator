package com.thaiopensource.validate.picl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import com.thaiopensource.xml.sax.DelegatingContentHandler;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import java.util.Vector;
import java.util.Stack;

class SchemaParser extends DelegatingContentHandler implements SchemaFuture, NamespaceContext {
  private final Vector constraints = new Vector();
  private final PropertyMap properties;
  private final CountingErrorHandler ceh;
  private Locator locator;
  private final Stack prefixes = new Stack();
  private final Localizer localizer = new Localizer(SchemaParser.class);
  private final PatternParser patternParser;

  SchemaParser(PropertyMap properties, Schema piclSchema) {
    this.properties = properties;
    ceh = new CountingErrorHandler(ValidateProperty.ERROR_HANDLER.get(properties));
    Validator validator = piclSchema.createValidator(new SinglePropertyMap(ValidateProperty.ERROR_HANDLER, ceh));
    setDelegate(validator.getContentHandler());
    patternParser = new PatternParser(ceh, localizer);
  }

  public void setDocumentLocator(Locator locator) {
    super.setDocumentLocator(locator);
    this.locator = locator;
  }

  public void startDocument()
          throws SAXException {
    super.startDocument();
    prefixes.push("xml");
    prefixes.push(WellKnownNamespaces.XML);
  }

  public void startPrefixMapping(String prefix, String uri)
          throws SAXException {
    if (prefix == null)
      prefix = "";
    prefixes.push(prefix);
    if (uri != null && uri.length() == 0)
      uri = null;
    prefixes.push(uri);
    super.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix)
          throws SAXException {
    prefixes.pop();
    prefixes.pop();
    super.endPrefixMapping(prefix);
  }

  public void startElement(String namespaceURI, String localName,
                           String qName, Attributes atts)
          throws SAXException {
    super.startElement(namespaceURI, localName, qName, atts);
    if (ceh.getHadErrorOrFatalError())
      return;
    if (!localName.equals("constraint"))
      return;
    String key = atts.getValue("", "key");
    try {
      Pattern keyPattern = patternParser.parse(key, locator, this);
      String ref = atts.getValue("", "ref");
      if (ref != null) {
        Pattern refPattern = patternParser.parse(ref, locator, this);
        constraints.addElement(new KeyRefConstraint(keyPattern, refPattern));
      }
      else
        constraints.addElement(new KeyConstraint(keyPattern));
    }
    catch (InvalidPatternException e) {
    }
  }

  public Schema getSchema() throws IncorrectSchemaException {
    if (ceh.getHadErrorOrFatalError())
      throw new IncorrectSchemaException();
    Constraint constraint;
    if (constraints.size() == 1)
      constraint = (Constraint)constraints.elementAt(0);
    else {
      Constraint[] v = new Constraint[constraints.size()];
      for (int i = 0; i < v.length; i++)
        v[i] = (Constraint)constraints.elementAt(i);
      constraint = new MultiConstraint(v);
    }
    return new SchemaImpl(properties, constraint);
  }

  public RuntimeException unwrapException(RuntimeException e) {
    return e;
  }

  public String getNamespaceUri(String prefix) {
    for (int i = prefixes.size(); i > 0; i -= 2) {
      if (prefixes.elementAt(i - 2).equals(prefix))
        return (String)prefixes.elementAt(i - 1);
    }
    return null;
  }

  public String defaultPrefix() {
    return "";
  }
}
