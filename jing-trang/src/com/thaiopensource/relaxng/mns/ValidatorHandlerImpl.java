package com.thaiopensource.relaxng.mns;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.relaxng.Schema;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

class ValidatorHandlerImpl extends DefaultHandler implements ValidatorHandler {
  private final Map namespaceMap;
  private ErrorHandler eh;
  private Locator locator;
  private Subtree subtrees = null;
  private boolean validSoFar = true;
  private boolean complete = false;
  private final Set attributeNamespaces = new HashSet();
  private PrefixMapping prefixMapping = null;

  static private class Subtree {
    final Subtree parent;
    final ValidatorHandler validator;
    final String namespace;
    int depth = 0;
    int foreignDepth = 0;

    Subtree(String namespace, ValidatorHandler validator, Subtree parent) {
      this.namespace = namespace;
      this.validator = validator;
      this.parent = parent;
    }
  }

  static private class PrefixMapping {
    final String prefix;
    final String uri;
    final PrefixMapping parent;

    PrefixMapping(String prefix, String uri, PrefixMapping parent) {
      this.prefix = prefix;
      this.uri = uri;
      this.parent = parent;
    }
  }

  ValidatorHandlerImpl(Map namespaceMap, ErrorHandler eh) {
    this.namespaceMap = namespaceMap;
    this.eh = eh;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    for (Subtree st = subtrees; st != null; st = st.parent)
      st.validator.characters(ch, start, length);
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    for (Subtree st = subtrees; st != null; st = st.parent)
      st.validator.ignorableWhitespace(ch, start, length);
  }


  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    if (subtrees != null && uri.equals(subtrees.namespace) && subtrees.foreignDepth == 0)
      subtrees.depth++;
    else {
      NamespaceSchemaInfo nsi = (NamespaceSchemaInfo)namespaceMap.get(uri);
      if (nsi == null || nsi.elementSchema == null) {
        if (subtrees != null)
          subtrees.foreignDepth++;
      }
      else {
        subtrees = new Subtree(uri, createValidator(nsi.elementSchema), subtrees);
        startSubtree(subtrees.validator);
      }
    }
    for (Subtree st = subtrees; st != null; st = st.parent)
      st.validator.startElement(uri, localName, qName, attributes);
    for (int i = 0, len = attributes.getLength(); i < len; i++) {
      String ns = attributes.getURI(i);
      if (!ns.equals("") && !ns.equals(uri) && !attributeNamespaces.contains(ns)) {
        attributeNamespaces.add(ns);
        validateAttributes(ns, attributes);
      }
    }
    attributeNamespaces.clear();
  }

  private void validateAttributes(String ns, Attributes attributes) throws SAXException {
    NamespaceSchemaInfo nsi = (NamespaceSchemaInfo)namespaceMap.get(ns);
    if (nsi == null || nsi.attributesSchema == null)
      return;
    ValidatorHandler vh = createValidator(nsi.attributesSchema);
    startSubtree(vh);
    vh.startElement(MnsSchemaFactory.BEARER_URI, MnsSchemaFactory.BEARER_LOCAL_NAME, MnsSchemaFactory.BEARER_LOCAL_NAME,
                    new NamespaceFilteredAttributes(ns, attributes));
    vh.endElement(MnsSchemaFactory.BEARER_URI, MnsSchemaFactory.BEARER_LOCAL_NAME, MnsSchemaFactory.BEARER_LOCAL_NAME);
    endSubtree(vh);
  }

  private ValidatorHandler createValidator(Schema schema) {
    // XXX use per-schema pool of validator handlers
    return schema.createValidator(eh);
  }

  private void startSubtree(ValidatorHandler vh) throws SAXException {
    if (locator != null)
      vh.setDocumentLocator(locator);
    vh.startDocument();
    for (PrefixMapping pm = prefixMapping; pm != null; pm = pm.parent)
      vh.startPrefixMapping(pm.prefix, pm.uri);
  }

  private void endSubtree(ValidatorHandler vh) throws SAXException {
    for (PrefixMapping pm = prefixMapping; pm != null; pm = pm.parent)
      vh.endPrefixMapping(pm.prefix);
    vh.endDocument();
    if (!vh.isValidSoFar())
      validSoFar = false;
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    if (subtrees == null)
      return;
    for (Subtree st = subtrees; st != null; st = st.parent)
      st.validator.endElement(uri, localName, qName);
    if (subtrees.foreignDepth > 0)
      subtrees.foreignDepth--;
    else if (subtrees.depth > 0)
      subtrees.depth--;
    else {
      endSubtree(subtrees.validator);
      subtrees = subtrees.parent;
    }
  }

  public void endDocument()
          throws SAXException {
    complete = true;
  }

  public void startPrefixMapping(String prefix, String uri)
          throws SAXException {
    super.startPrefixMapping(prefix, uri);
    prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
  }

  public void endPrefixMapping(String prefix)
          throws SAXException {
    super.endPrefixMapping(prefix);
    prefixMapping = prefixMapping.parent;
  }

  public boolean isValidSoFar() {
    for (Subtree st = subtrees; st != null; st = st.parent)
      if (!st.validator.isValidSoFar())
        return false;
    return validSoFar;
  }

  public boolean isComplete() {
    return complete;
  }

  public void reset() {
    validSoFar = true;
    complete = false;
    subtrees = null;
    locator = null;
  }

  public void setErrorHandler(ErrorHandler eh) {
    this.eh = eh;
  }

  public ErrorHandler getErrorHandler() {
    return eh;
  }
}
