package com.thaiopensource.relaxng.mns;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.ValidatorHandler;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.xml.util.StringSplitter;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class SchemaImpl implements Schema {
  static final String BEARER_URI = "http://www.thaiopensoure.com/mns/instance";
  static final String BEARER_LOCAL_NAME = "globalAttributesBearer";
  private static final String MNS_URI = "http://www.thaiopensource.com/ns/mns";
  private static final String BEARER_PREFIX = "m";
  private final Map modeMap = new HashMap();
  private Mode startMode;
  private static final String DEFAULT_MODE_NAME = "#default";

  static class ElementAction {
    private Schema schema;
    private Mode mode;
    private Set covered = new HashSet();

    ElementAction(Schema schema, Mode mode) {
      this.schema = schema;
      this.mode = mode;
    }

    Mode getMode() {
      return mode;
    }

    Schema getSchema() {
      return schema;
    }

    Set getCoveredNamespaces() {
      return covered;
    }
  }

  static class Mode {
    private boolean defined = false;
    private boolean strict = false;
    private boolean strictDefined = false;
    private Map elementMap = new HashMap();
    private Map attributesMap = new HashMap();

    boolean isStrict() {
      return strict;
    }

    Schema getAttributesSchema(String ns) {
      return (Schema)attributesMap.get(ns);
    }

    ElementAction getElementAction(String ns) {
      return (ElementAction)elementMap.get(ns);
    }
  }

  private class Handler extends DelegatingContentHandler {
    private final MnsSchemaFactory factory;
    private final ValidatorHandler validator;
    private ElementAction currentElementAction;
    private boolean incorrectChildSchemas = false;

    Handler(MnsSchemaFactory factory, ValidatorHandler validator) {
      super(validator);
      this.factory = factory;
      this.validator = validator;
    }

    void checkValid() throws IncorrectSchemaException {
      if (incorrectChildSchemas || !validator.isValidSoFar())
        throw new IncorrectSchemaException();
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
      super.startElement(uri, localName, qName, attributes);
      if (!validator.isValidSoFar() || !MNS_URI.equals(uri))
        return;
      if (localName.equals("rules")) {
        String modeName = attributes.getValue("", "startMode");
        if (modeName == null)
          modeName = DEFAULT_MODE_NAME;
        startMode = lookupCreateMode(modeName);
        return;
      }
      if (localName.equals("cover")) {
        currentElementAction.covered.add(attributes.getValue("", "ns"));
        return;
      }
      String modesValue = attributes.getValue("", "modes");
      String[] modeNames;
      if (modesValue == null)
        modeNames = new String[] { DEFAULT_MODE_NAME };
      else
        modeNames = StringSplitter.split(modesValue);
      Mode[] modes = new Mode[modeNames.length];
      for (int i = 0; i < modes.length; i++) {
        modes[i] = lookupCreateMode(modeNames[i]);
        modes[i].defined = true;
      }
      if (localName.equals("strict") || localName.equals("lax")) {
        boolean strict = localName.equals("strict");
        for (int i = 0; i < modes.length; i++) {
          // XXX error if strictDefined is true
          modes[i].strict = strict;
          modes[i].strictDefined = true;
        }
        return;
      }
      boolean isAttribute = localName.equals("validateAttributes");
      String ns = attributes.getValue("", "ns");
      String schemaUri = attributes.getValue("", "schema");
      try {
        if (isAttribute) {
          Schema schema = factory.createChildSchema(wrapAttributesSchema(schemaUri));
          for (int i = 0; i < modes.length; i++)
            modes[i].attributesMap.put(ns, schema); // XXX error if already defined
        }
        else {
          Schema schema = factory.createChildSchema(new InputSource(schemaUri));
          String modeName = attributes.getValue("", "useMode");
          if (modeName == null)
            modeName = DEFAULT_MODE_NAME;
          currentElementAction = new ElementAction(schema, lookupCreateMode(modeName));
          for (int i = 0; i < modes.length; i++)
            modes[i].elementMap.put(ns, currentElementAction); // XXX error if already defined
        }
      }
      catch (IncorrectSchemaException e) {
        incorrectChildSchemas = true;
      }
      catch (IOException e) {
        // XXX wrap and rethrow
      }
    }
  }

  SchemaImpl(InputSource in, MnsSchemaFactory factory)
          throws IOException, SAXException, IncorrectSchemaException {
    XMLReader xr = factory.getXMLReaderCreator().createXMLReader();
    ErrorHandler eh = factory.getErrorHandler();
    Handler h = new Handler(factory, factory.getMnsSchema().createValidator(eh));
    xr.setContentHandler(h);
    xr.setErrorHandler(eh);
    xr.parse(in);
    h.checkValid();
    // XXX warn for undefined modes
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new ValidatorHandlerImpl(startMode, eh);
  }

  public ValidatorHandler createValidator() {
    return createValidator(null);
  }

  private static InputSource wrapAttributesSchema(String attributesSchemaUri) {
    StringBuffer buf = new StringBuffer();
    buf.append("<element name=\"");
    buf.append(BEARER_PREFIX);
    buf.append(':');
    buf.append(BEARER_LOCAL_NAME);
    buf.append('"');
    buf.append(" xmlns=\"");
    buf.append(WellKnownNamespaces.RELAX_NG);
    buf.append('"');
    buf.append(" xmlns:");
    buf.append(BEARER_PREFIX);
    buf.append("=\"");
    buf.append(BEARER_URI);
    buf.append("\"><externalRef href=\"");
    buf.append(attributesSchemaUri);
    buf.append("\"/></element>");
    return new InputSource(new StringReader(buf.toString()));
  }

  private Mode lookupCreateMode(String name) {
    Mode mode = (Mode)modeMap.get(name);
    if (mode == null) {
      mode = new Mode();
      modeMap.put(name, mode);
    }
    return mode;
  }
}
