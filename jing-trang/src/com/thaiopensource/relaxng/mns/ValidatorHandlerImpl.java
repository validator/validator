package com.thaiopensource.relaxng.mns;

import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.util.Localizer;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;

class ValidatorHandlerImpl extends DefaultHandler implements ValidatorHandler {
  private SchemaImpl.Mode currentMode;
  private int laxDepth = 0;
  private ErrorHandler eh;
  private Locator locator;
  private Subtree subtrees = null;
  private boolean validSoFar = true;
  private boolean complete = false;
  private final Set attributeNamespaces = new HashSet();
  private PrefixMapping prefixMapping = null;
  private Localizer localizer = new Localizer(ValidatorHandlerImpl.class);

  static private class Subtree {
    final Subtree parent;
    final ValidatorHandler validator;
    final String namespace;
    final Set subsumedNamespaces;
    final SchemaImpl.Mode parentMode;
    final int parentLaxDepth;
    int depth = 0;

    Subtree(String namespace, Set subsumedNamespaces, ValidatorHandler validator, SchemaImpl.Mode parentMode, int parentLaxDepth, Subtree parent) {
      this.namespace = namespace;
      this.subsumedNamespaces = subsumedNamespaces;
      this.validator = validator;
      this.parentMode = parentMode;
      this.parentLaxDepth = parentLaxDepth;
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

  ValidatorHandlerImpl(SchemaImpl.Mode mode, ErrorHandler eh) {
    this.currentMode = mode;
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
    if (namespaceSubsumed(uri))
      subtrees.depth++;
    else {
      SchemaImpl.ElementAction elementAction = currentMode.getElementAction(uri);
      if (elementAction == null) {
        if (laxDepth == 0 && currentMode.isStrict())
          error("element_undeclared_namespace", uri);
        laxDepth++;
      }
      else {
        subtrees = new Subtree(uri,
                               elementAction.getSubsumedNamespaces(),
                               createValidator(elementAction.getSchema()),
                               currentMode,
                               laxDepth,
                               subtrees);
        currentMode = elementAction.getMode();
        laxDepth = 0;
        startSubtree(subtrees.validator);
      }
    }
    for (Subtree st = subtrees; st != null; st = st.parent)
      st.validator.startElement(uri, localName, qName, attributes);
    for (int i = 0, len = attributes.getLength(); i < len; i++) {
      String ns = attributes.getURI(i);
      if (!ns.equals("")
          && !ns.equals(uri)
          && !namespaceSubsumed(uri)
          && !attributeNamespaces.contains(ns)) {
        attributeNamespaces.add(ns);
        validateAttributes(ns, attributes);
      }
    }
    attributeNamespaces.clear();
  }

  private boolean namespaceSubsumed(String ns) {
    return (laxDepth == 0 && subtrees != null
            && (ns.equals(subtrees.namespace) || subtrees.subsumedNamespaces.contains(ns)));
  }

  private void validateAttributes(String ns, Attributes attributes) throws SAXException {
    Schema attributesSchema = currentMode.getAttributesSchema(ns);
    if (attributesSchema == null) {
      if (currentMode.isStrict())
        error("attributes_undeclared_namespace", ns);
      return;
    }
    ValidatorHandler vh = createValidator(attributesSchema);
    startSubtree(vh);
    vh.startElement(SchemaImpl.BEARER_URI, SchemaImpl.BEARER_LOCAL_NAME, SchemaImpl.BEARER_LOCAL_NAME,
                    new NamespaceFilteredAttributes(ns, attributes));
    vh.endElement(SchemaImpl.BEARER_URI, SchemaImpl.BEARER_LOCAL_NAME, SchemaImpl.BEARER_LOCAL_NAME);
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
    for (Subtree st = subtrees; st != null; st = st.parent)
      st.validator.endElement(uri, localName, qName);
    if (laxDepth > 0)
      laxDepth--;
    else if (subtrees.depth > 0)
      subtrees.depth--;
    else {
      endSubtree(subtrees.validator);
      currentMode = subtrees.parentMode;
      laxDepth = subtrees.parentLaxDepth;
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

  private void error(String key, String arg) throws SAXException {
    validSoFar = false;
    if (eh == null)
      return;
    eh.error(new SAXParseException(localizer.message(key, arg), locator));
  }
}
