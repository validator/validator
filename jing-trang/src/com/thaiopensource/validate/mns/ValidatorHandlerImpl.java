package com.thaiopensource.validate.mns;

import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidatorHandler;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.mns.ContextMap;
import com.thaiopensource.validate.mns.Hashset;
import com.thaiopensource.validate.mns.ElementsOrAttributes;
import com.thaiopensource.xml.util.Name;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMap;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;
import java.util.Hashtable;

class ValidatorHandlerImpl extends DefaultHandler implements ValidatorHandler {
  private static final String BEARER_URI = "";
  private static final String BEARER_LOCAL_NAME = "#bearer";
  private SchemaImpl.Mode currentMode;
  private int laxDepth = 0;
  private final ErrorHandler eh;
  private final PropertyMap properties;
  private Locator locator;
  private Subtree subtrees = null;
  private boolean validSoFar = true;
  private final Hashset attributeNamespaces = new Hashset();
  private PrefixMapping prefixMapping = null;
  private final Localizer localizer = new Localizer(ValidatorHandlerImpl.class);
  private final Hashtable validatorHandlerCache = new Hashtable();

  static private class Subtree {
    final Subtree parent;
    final ValidatorHandler validator;
    final Schema schema;
    final Hashset coveredNamespaces;
    final ElementsOrAttributes prune;
    final SchemaImpl.Mode parentMode;
    final int parentLaxDepth;
    final Stack context = new Stack();
    final ContextMap contextMap;

    Subtree(Hashset coveredNamespaces, ContextMap contextMap,
            ElementsOrAttributes prune, ValidatorHandler validator,
            Schema schema, SchemaImpl.Mode parentMode, int parentLaxDepth, Subtree parent) {
      this.coveredNamespaces = coveredNamespaces;
      this.contextMap = contextMap;
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

  ValidatorHandlerImpl(SchemaImpl.Mode mode, PropertyMap properties) {
    this.currentMode = mode;
    this.properties = properties;
    this.eh = ValidateProperty.ERROR_HANDLER.get(properties);
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

  private SchemaImpl.Mode getMode() {
    if (subtrees != null) {
      SchemaImpl.Mode mode = (SchemaImpl.Mode)subtrees.contextMap.get(subtrees.context);
      if (mode != null)
        return mode;
    }
    return currentMode;
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    if (namespaceCovered(uri))
      subtrees.context.push(new Name(uri, localName));
    else {
      SchemaImpl.Mode mode = getMode();
      SchemaImpl.ElementAction elementAction = mode.getElementAction(uri);
      if (elementAction == null) {
        if (laxDepth == 0 && !mode.getLax().containsElements())
          error("element_undeclared_namespace", uri);
        laxDepth++;
      }
      else {
        subtrees = new Subtree(elementAction.getCoveredNamespaces(),
                               elementAction.getContextMap(),
                               elementAction.getPrune(),
                               createValidatorHandler(elementAction.getSchema()),
                               elementAction.getSchema(),
                               currentMode,
                               laxDepth,
                               subtrees);
        subtrees.context.push(new Name(uri, localName));
        currentMode = elementAction.getMode();
        laxDepth = 0;
        startSubtree(subtrees.validator);
      }
    }
    for (Subtree st = subtrees; wantsEvent(st); st = st.parent) {
      Attributes prunedAtts;
      if (st.prune.containsAttributes())
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
    return (laxDepth == 0
            && subtrees != null
            && subtrees.coveredNamespaces.contains(ns));
  }

  private boolean wantsEvent(Subtree st) {
    return st != null && (!st.prune.containsElements() || (laxDepth == 0 && st == subtrees));
  }

  private void validateAttributes(String ns, Attributes attributes) throws SAXException {
    SchemaImpl.Mode mode = getMode();
    Schema attributesSchema = mode.getAttributesSchema(ns);
    if (attributesSchema == null) {
      if (!mode.getLax().containsAttributes())
        error("attributes_undeclared_namespace", ns);
      return;
    }
    ValidatorHandler vh = createValidatorHandler(attributesSchema);
    startSubtree(vh);
    vh.startElement(BEARER_URI, BEARER_LOCAL_NAME, BEARER_LOCAL_NAME,
                    new NamespaceFilteredAttributes(ns, false, attributes));
    vh.endElement(BEARER_URI, BEARER_LOCAL_NAME, BEARER_LOCAL_NAME);
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
    else if (!subtrees.context.empty()) {
      subtrees.context.pop();
      if (subtrees.context.empty()) {
        endSubtree(subtrees.validator);
        releaseValidatorHandler(subtrees.schema, subtrees.validator);
        currentMode = subtrees.parentMode;
        laxDepth = subtrees.parentLaxDepth;
        subtrees = subtrees.parent;
      }
    }
  }

  private ValidatorHandler createValidatorHandler(Schema schema) {
     Stack stack = (Stack)validatorHandlerCache.get(schema);
     if (stack == null) {
       stack = new Stack();
       validatorHandlerCache.put(schema, stack);
     }
     if (stack.empty())
       return schema.createValidator(properties);
     return (ValidatorHandler)stack.pop();
   }

   private void releaseValidatorHandler(Schema schema, ValidatorHandler vh) {
     vh.reset();
     ((Stack)validatorHandlerCache.get(schema)).push(vh);
   }

  public void endDocument()
          throws SAXException {
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

  public void reset() {
    validSoFar = true;
    subtrees = null;
    locator = null;
  }

  private void error(String key, String arg) throws SAXException {
    validSoFar = false;
    if (eh == null)
      return;
    eh.error(new SAXParseException(localizer.message(key, arg), locator));
  }
}
