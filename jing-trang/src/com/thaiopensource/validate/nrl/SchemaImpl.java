package com.thaiopensource.validate.nrl;

import com.thaiopensource.util.Localizer;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.Uri;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidatorHandler;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.xml.sax.XmlBaseHandler;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

class SchemaImpl implements Schema {
  static private final String IMPLICIT_MODE_NAME = "#implicit";
  static private final String WRAPPER_MODE_NAME = "#wrapper";
  static final String NRL_URI = "http://www.thaiopensource.com/ns/nrl";
  private final Hashtable modeMap = new Hashtable();
  private Mode startMode;
  private Mode defaultBaseMode;
  private final boolean attributesSchema;

  static private final class WrappedIOException extends RuntimeException {
    private final IOException exception;

    private WrappedIOException(IOException exception) {
      this.exception = exception;
    }

    private IOException getException() {
      return exception;
    }
  }


  private class Handler extends DelegatingContentHandler implements SchemaFuture {
    private final SchemaReceiverImpl sr;
    private boolean hadError = false;
    private final ErrorHandler eh;
    private final Localizer localizer = new Localizer(SchemaImpl.class);
    private Locator locator;
    private final XmlBaseHandler xmlBaseHandler = new XmlBaseHandler();
    private int foreignDepth = 0;
    private Mode currentMode = null;
    private String defaultSchemaType;
    private ValidatorHandler validator;
    private ElementsOrAttributes match;
    private ActionSet actions;
    private AttributeActionSet attributeActions;
    private ModeUsage modeUsage;
    private boolean anyNamespace;

    Handler(SchemaReceiverImpl sr) {
      this.sr = sr;
      this.eh = ValidateProperty.ERROR_HANDLER.get(sr.getProperties());
    }

    public void setDocumentLocator(Locator locator) {
      xmlBaseHandler.setLocator(locator);
      this.locator = locator;
    }

    public void startDocument() throws SAXException {
      try {
        validator = sr.getNrlSchema().createValidator(sr.getProperties());
      }
      catch (IOException e) {
        throw new WrappedIOException(e);
      }
      catch (IncorrectSchemaException e) {
        throw new RuntimeException("internal error in RNG schema for NRL");
      }
      setDelegate(validator);
      if (locator != null)
        super.setDocumentLocator(locator);
      super.startDocument();
    }

    public Schema getSchema() throws IncorrectSchemaException, SAXException {
      if (validator == null || !validator.isValidSoFar())
        throw new IncorrectSchemaException();
      Hashset openModes = new Hashset();
      Hashset checkedModes = new Hashset();
      for (Enumeration enum = modeMap.keys(); enum.hasMoreElements();) {
        String modeName = (String)enum.nextElement();
        Mode mode = (Mode)modeMap.get(modeName);
        if (!mode.isDefined())
          error("undefined_mode", modeName, mode.getWhereUsed());
        for (Mode tem = mode; tem != null; tem = tem.getBaseMode()) {
          if (checkedModes.contains(tem))
            break;
          if (openModes.contains(tem)) {
            error("mode_cycle", tem.getName(), tem.getWhereDefined());
            break;
          }
          openModes.add(tem);
        }
        checkedModes.addAll(openModes);
        openModes.clear();
      }
      if (hadError)
        throw new IncorrectSchemaException();
      return SchemaImpl.this;
    }

    public RuntimeException unwrapException(RuntimeException e) throws SAXException, IOException, IncorrectSchemaException {
      if (e instanceof WrappedIOException)
        throw ((WrappedIOException)e).getException();
      return e;
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
      super.startElement(uri, localName, qName, attributes);
      xmlBaseHandler.startElement();
      String xmlBase = attributes.getValue(WellKnownNamespaces.XML, "base");
      if (xmlBase != null)
        xmlBaseHandler.xmlBaseAttribute(xmlBase);
      if (!NRL_URI.equals(uri) || foreignDepth > 0) {
        foreignDepth++;
        return;
      }
      if (!validator.isValidSoFar())
        return;
      if (localName.equals("rules"))
        parseRules(attributes);
      else if (localName.equals("mode"))
        parseMode(attributes);
      else if (localName.equals("namespace"))
        parseNamespace(attributes);
      else if (localName.equals("anyNamespace"))
        parseAnyNamespace(attributes);
      else if (localName.equals("validate"))
        parseValidate(attributes);
      else if (localName.equals("reject"))
        parseReject(attributes);
      else if (localName.equals("pass"))
        parsePass(attributes);
      else if (localName.equals("delve"))
        parseDelve(attributes);
      else if (localName.equals("allow"))
        parseAllow(attributes);
      else if (localName.equals("context"))
        parseContext(attributes);
      else
        throw new RuntimeException("unexpected element \"" + localName + "\"");
    }

    public void endElement(String namespaceURI, String localName,
                           String qName)
            throws SAXException {
      super.endElement(namespaceURI, localName, qName);
      xmlBaseHandler.endElement();
      if (foreignDepth > 0) {
        foreignDepth--;
        return;
      }
    }

    private void parseRules(Attributes attributes) {
      startMode = getModeAttribute(attributes, "startMode");
      if (startMode == null) {
        startMode = lookupCreateMode(IMPLICIT_MODE_NAME);
        currentMode = startMode;
        startMode.noteDefined(null);
      }
      startMode.noteUsed(locator);
      if (attributesSchema) {
        Mode wrapper = lookupCreateMode(WRAPPER_MODE_NAME);
        ActionSet actions = new ActionSet();
        actions.addNoResultAction(new AllowAction(new ModeUsage(startMode, startMode)));
        wrapper.bindElement(Mode.ANY_NAMESPACE, actions);
        wrapper.noteDefined(null);
        startMode = wrapper;
      }
      defaultSchemaType = getSchemaType(attributes);
    }

    private void parseMode(Attributes attributes) throws SAXException {
      currentMode = getModeAttribute(attributes, "name");
      if (currentMode.isDefined()) {
        error("duplicate_mode", currentMode.getName());
        error("first_mode", currentMode.getName(), currentMode.getWhereDefined());
      }
      else {
        Mode base = getModeAttribute(attributes, "extends");
        if (base != null)
          currentMode.setBaseMode(base);
        currentMode.noteDefined(locator);
      }
    }

    private void parseNamespace(Attributes attributes) throws SAXException {
      anyNamespace = false;
      parseRule(getNs(attributes), attributes);
    }

    private void parseAnyNamespace(Attributes attributes) throws SAXException {
      anyNamespace = true;
      parseRule(Mode.ANY_NAMESPACE, attributes);
    }

    private void parseRule(String ns, Attributes attributes) throws SAXException {
      match = toElementsOrAttributes(attributes.getValue("", "match"),
                                     ElementsOrAttributes.ELEMENTS);
      if (match.containsAttributes()) {
        attributeActions = new AttributeActionSet();
        if (!currentMode.bindAttribute(ns, attributeActions)) {
          if (ns.equals(Mode.ANY_NAMESPACE))
            error("duplicate_attribute_action_any_namespace");
          else
            error("duplicate_attribute_action", ns);
        }
      }
      if (match.containsElements()) {
        actions = new ActionSet();
        if (!currentMode.bindElement(ns, actions)) {
          if (ns.equals(Mode.ANY_NAMESPACE))
            error("duplicate_element_action_any_namespace");
          else
            error("duplicate_element_action", ns);
        }
      }
      else
        actions = null;
    }

    private void parseValidate(Attributes attributes) throws SAXException {
      String schemaUri = getSchema(attributes);
      String schemaType = getSchemaType(attributes);
      if (schemaType == null)
        schemaType = defaultSchemaType;
      try {
        if (attributeActions != null) {
          Schema schema = sr.createChildSchema(new InputSource(schemaUri), schemaType, true);
          attributeActions.addSchema(schema);
        }
        if (actions != null) {
          modeUsage = getModeUsage(attributes);
          Schema schema = sr.createChildSchema(new InputSource(schemaUri), schemaType, false);
          actions.addNoResultAction(new ValidateAction(modeUsage, schema));
        }
        else
          modeUsage = null;
      }
      catch (IncorrectSchemaException e) {
        hadError = true;
      }
      catch (IOException e) {
        throw new WrappedIOException(e);
      }
    }

    private void parsePass(Attributes attributes) {
      if (attributeActions != null)
        attributeActions.setPass(true);
      if (actions != null) {
        modeUsage = getModeUsage(attributes);
        actions.setResultAction(new PassAction(modeUsage));
      }
      else
        modeUsage = null;
    }

    private void parseDelve(Attributes attributes) {
      if (actions != null) {
        modeUsage = getModeUsage(attributes);
        actions.setResultAction(new DelveAction(modeUsage));
      }
      else
        modeUsage = null;
    }

    private void parseAllow(Attributes attributes) {
      if (actions != null) {
        modeUsage = getModeUsage(attributes);
        actions.addNoResultAction(new AllowAction(modeUsage));
      }
      else
        modeUsage = null;
    }

    private void parseReject(Attributes attributes) {
      if (actions != null) {
        modeUsage = getModeUsage(attributes);
        actions.addNoResultAction(new RejectAction(modeUsage));
      }
      else
        modeUsage = null;
      if (attributeActions != null)
        attributeActions.setReject(true);
    }

    private void parseContext(Attributes attributes) throws SAXException {
      if (anyNamespace) {
        error("context_any_namespace");
        return;
      }
      Mode mode = getUseMode(attributes);
      try {
        Vector paths = Path.parse(attributes.getValue("", "path"));
        // XXX warning if modeUsage is null
        if (modeUsage != null) {
          for (int i = 0, len = paths.size(); i < len; i++) {
            Path path = (Path)paths.elementAt(i);
            if (!modeUsage.addContext(path.isRoot(), path.getNames(), mode))
              error("duplicate_path", path.toString());
          }
        }
      }
      catch (Path.ParseException e) {
        error(e.getMessageKey());
      }
    }

    private String getSchema(Attributes attributes) throws SAXException {
      String schemaUri = attributes.getValue("", "schema");
      if (Uri.hasFragmentId(schemaUri))
        error("schema_fragment_id");
      return Uri.resolve(xmlBaseHandler.getBaseUri(),
                         Uri.escapeDisallowedChars(schemaUri));
    }

    private String getSchemaType(Attributes attributes) {
      return attributes.getValue("", "schemaType");
    }

    private ElementsOrAttributes toElementsOrAttributes(String value, ElementsOrAttributes defaultValue) {
      if (value == null)
        return defaultValue;
      ElementsOrAttributes eoa = ElementsOrAttributes.NEITHER;
      if (value.indexOf("elements") >= 0)
        eoa = eoa.addElements();
      if (value.indexOf("attributes") >= 0)
        eoa = eoa.addAttributes();
      return eoa;
    }

    private ModeUsage getModeUsage(Attributes attributes) {
      return new ModeUsage(getUseMode(attributes), currentMode);
    }

    private Mode getUseMode(Attributes attributes) {
      Mode mode = getModeAttribute(attributes, "useMode");
      if (mode == null)
        return Mode.CURRENT;
      mode.noteUsed(locator);
      return mode;
    }

    private String getNs(Attributes attributes) throws SAXException {
      String ns = attributes.getValue("", "ns");
      if (ns != null && !Uri.isAbsolute(ns) && !ns.equals(""))
        error("ns_absolute");
      return ns;
    }

    void error(String key) throws SAXException {
      hadError = true;
      if (eh == null)
        return;
      eh.error(new SAXParseException(localizer.message(key), locator));
    }

    void error(String key, String arg) throws SAXException {
      hadError = true;
      if (eh == null)
        return;
      eh.error(new SAXParseException(localizer.message(key, arg), locator));
    }

    void error(String key, String arg, Locator locator) throws SAXException {
      hadError = true;
      if (eh == null)
        return;
      eh.error(new SAXParseException(localizer.message(key, arg), locator));
    }

    void error(String key, String arg1, String arg2) throws SAXException {
      hadError = true;
      if (eh == null)
        return;
      eh.error(new SAXParseException(localizer.message(key, arg1, arg2), locator));
    }

  }

  SchemaImpl(boolean attributesSchema) {
    this.attributesSchema = attributesSchema;
    makeBuiltinMode("#allow", AllowAction.class);
    makeBuiltinMode("#pass", PassAction.class);
    makeBuiltinMode("#delve", DelveAction.class);
    defaultBaseMode = makeBuiltinMode("#reject", RejectAction.class);
  }

  private Mode makeBuiltinMode(String name, Class cls) {
    Mode mode = lookupCreateMode(name);
    ActionSet actions = new ActionSet();
    ModeUsage modeUsage = new ModeUsage(Mode.CURRENT, mode);
    if (cls == PassAction.class)
      actions.setResultAction(new PassAction(modeUsage));
    else if (cls == AllowAction.class)
      actions.addNoResultAction(new AllowAction(modeUsage));
    else if (cls == DelveAction.class)
      actions.setResultAction(new DelveAction(modeUsage));
    else
      actions.addNoResultAction(new RejectAction(modeUsage));
    mode.bindElement(Mode.ANY_NAMESPACE, actions);
    mode.noteDefined(null);
    AttributeActionSet attributeActions = new AttributeActionSet();
    if (attributesSchema)
      attributeActions.setReject(true);
    else
      attributeActions.setPass(true);
    mode.bindAttribute(Mode.ANY_NAMESPACE, attributeActions);
    return mode;
  }

  SchemaFuture installHandlers(XMLReader in, SchemaReceiverImpl sr) {
    Handler h = new Handler(sr);
    in.setContentHandler(h);
    return h;
  }

  public ValidatorHandler createValidator(PropertyMap properties) {
    return new ValidatorHandlerImpl(startMode, properties);
  }

  private Mode getModeAttribute(Attributes attributes, String localName) {
    return lookupCreateMode(attributes.getValue("", localName));
  }

  private Mode lookupCreateMode(String name) {
    if (name == null)
      return null;
    name = name.trim();
    Mode mode = (Mode)modeMap.get(name);
    if (mode == null) {
      mode = new Mode(name, defaultBaseMode);
      modeMap.put(name, mode);
    }
    return mode;
  }

}
