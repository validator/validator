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

import java.util.Stack;
import java.util.Hashtable;

class ValidatorHandlerImpl extends DefaultHandler implements ValidatorHandler {
  private SchemaImpl.Mode currentMode;
  private int laxDepth = 0;
  private ErrorHandler eh;
  private Locator locator;
  private Subtree subtrees = null;
  private boolean validSoFar = true;
  private boolean complete = false;
  private final Hashset attributeNamespaces = new Hashset();
  private PrefixMapping prefixMapping = null;
  private final Localizer localizer = new Localizer(ValidatorHandlerImpl.class);
  private final Hashtable validatorHandlerCache = new Hashtable();

  static private class Subtree {
    final Subtree parent;
    final ValidatorHandler validator;
    final Schema schema;
    final String namespace;
    final Hashset coveredNamespaces;
    final boolean prune;
    final SchemaImpl.Mode parentMode;
    final int parentLaxDepth;
    int depth = 0;

    Subtree(String namespace, Hashset coveredNamespaces, boolean prune, ValidatorHandler validator,
            Schema schema, SchemaImpl.Mode parentMode, int parentLaxDepth, Subtree parent) {
      this.namespace = namespace;
      this.coveredNamespaces = coveredNamespaces;
      this.prune = prune;
      this.validator = validator;
      this.schema = schema;
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
    for (Subtree st = subtrees; wantsEvent(st); st = st.parent)
      st.validator.characters(ch, start, length);
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    for (Subtree st = subtrees; wantsEvent(st); st = st.parent)
      st.validator.ignorableWhitespace(ch, start, length);
  }


  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    if (namespaceCovered(uri))
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
                               elementAction.getCoveredNamespaces(),
                               elementAction.getPrune(),
                               createValidatorHandler(elementAction.getSchema()),
                               elementAction.getSchema(),
                               currentMode,
                               laxDepth,
                               subtrees);
        currentMode = elementAction.getMode();
        laxDepth = 0;
        startSubtree(subtrees.validator);
      }
    }
    for (Subtree st = subtrees; wantsEvent(st); st = st.parent) {
      Attributes prunedAtts;
      if (st.prune)
        prunedAtts = new NamespaceFilteredAttributes(uri, true, attributes);
      else
        prunedAtts = attributes;
      st.validator.startElement(uri, localName, qName, prunedAtts);
    }
    for (int i = 0, len = attributes.getLength(); i < len; i++) {
      String ns = attributes.getURI(i);
      if (!ns.equals("")
          && !ns.equals(uri)
          && !namespaceCovered(ns)
          && !attributeNamespaces.contains(ns)) {
        attributeNamespaces.add(ns);
        validateAttributes(ns, attributes);
      }
    }
    attributeNamespaces.clear();
  }

  private boolean namespaceCovered(String ns) {
    return (laxDepth == 0 && subtrees != null
            && (ns.equals(subtrees.namespace) || subtrees.coveredNamespaces.contains(ns)));
  }

  private boolean wantsEvent(Subtree st) {
    return st != null && (!st.prune || (laxDepth == 0 && st == subtrees));
  }

  private void validateAttributes(String ns, Attributes attributes) throws SAXException {
    Schema attributesSchema = currentMode.getAttributesSchema(ns);
    if (attributesSchema == null) {
      if (currentMode.isStrict())
        error("attributes_undeclared_namespace", ns);
      return;
    }
    ValidatorHandler vh = createValidatorHandler(attributesSchema);
    startSubtree(vh);
    vh.startElement(SchemaImpl.BEARER_URI, SchemaImpl.BEARER_LOCAL_NAME, SchemaImpl.BEARER_LOCAL_NAME,
                    new NamespaceFilteredAttributes(ns, false, attributes));
    vh.endElement(SchemaImpl.BEARER_URI, SchemaImpl.BEARER_LOCAL_NAME, SchemaImpl.BEARER_LOCAL_NAME);
    endSubtree(vh);
    releaseValidatorHandler(attributesSchema, vh);
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
    for (Subtree st = subtrees; wantsEvent(st); st = st.parent)
      st.validator.endElement(uri, localName, qName);
    if (laxDepth > 0)
      laxDepth--;
    else if (subtrees.depth > 0)
      subtrees.depth--;
    else {
      endSubtree(subtrees.validator);
      releaseValidatorHandler(subtrees.schema, subtrees.validator);
      currentMode = subtrees.parentMode;
      laxDepth = subtrees.parentLaxDepth;
      subtrees = subtrees.parent;
    }
  }

  private ValidatorHandler createValidatorHandler(Schema schema) {
     Stack stack = (Stack)validatorHandlerCache.get(schema);
     if (stack == null) {
       stack = new Stack();
       validatorHandlerCache.put(schema, stack);
     }
     if (stack.empty())
       return schema.createValidator(eh);
     return (ValidatorHandler)stack.pop();
   }

   private void releaseValidatorHandler(Schema schema, ValidatorHandler vh) {
     vh.reset();
     ((Stack)validatorHandlerCache.get(schema)).push(vh);
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
