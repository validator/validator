package com.thaiopensource.xml.infer;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.thaiopensource.relaxng.output.common.Name;

public class InferHandler extends DefaultHandler {
  private final Map inferrerMap = new HashMap();
  private OpenElement openElement = null;
  private final Set startSet = new HashSet();
  private List attributeNames = new Vector();
  private final DatatypeRepertoire datatypes;
  private final StringBuffer textBuffer = new StringBuffer();

  private static class OpenElement {
    OpenElement parent;
    ElementDeclInferrer inferrer;
    Name prevElementName = ContentModelInferrer.START;

    public OpenElement(OpenElement parent, ElementDeclInferrer inferrer) {
      this.parent = parent;
      this.inferrer = inferrer;
    }
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    Name name = new Name(uri, localName);
    if (openElement == null)
      startSet.add(name);
    else {
      if (textBuffer.length() > 0) {
        if (!DatatypeInferrer.isWhiteSpace(textBuffer.toString()))
          openElement.inferrer.addText();
        textBuffer.setLength(0);
      }
      if (openElement.prevElementName.equals(name))
        openElement.inferrer.setMulti(name);
      else {
        openElement.inferrer.addSequence(openElement.prevElementName, name);
        openElement.prevElementName = name;
      }
    }
    for (int i = 0, len = attributes.getLength(); i < len; i++)
      attributeNames.add(new Name(attributes.getURI(i), attributes.getLocalName(i)));
    ElementDeclInferrer inferrer = (ElementDeclInferrer)inferrerMap.get(name);
    if (inferrer == null) {
      inferrer = new ElementDeclInferrer(datatypes, attributeNames);
      inferrerMap.put(name, inferrer);
    }
    else
      inferrer.addAttributeNames(attributeNames);
    for (int i = 0, len = attributes.getLength(); i < len; i++)
      inferrer.addAttributeValue((Name)attributeNames.get(i), attributes.getValue(i));
    attributeNames.clear();
    openElement = new OpenElement(openElement, inferrer);
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    if (openElement.inferrer.wantValue())
      textBuffer.append(ch, start, length);
    else {
      for (int i = 0; i < length; i++)
        switch (ch[start + i]) {
          case ' ':
          case '\t':
          case '\n':
          case '\r':
            break;
          default:
            openElement.inferrer.addText();
            return;
        }
    }
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    if (openElement.inferrer.wantValue()) {
      openElement.inferrer.addValue(textBuffer.toString());
      textBuffer.setLength(0);
    }
    else
      openElement.inferrer.addSequence(openElement.prevElementName, ContentModelInferrer.END);
    openElement = openElement.parent;
  }

  public Schema getSchema() {
    Schema schema = new Schema();
    for (Iterator iter = inferrerMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      ElementDecl decl = ((ElementDeclInferrer)entry.getValue()).infer();
      Name name = (Name)entry.getKey();
      schema.getElementDecls().put(name, decl);
    }
    schema.setStart(makeStart());
    return schema;
  }

  private Particle makeStart() {
    Particle start = null;
    for (Iterator iter = startSet.iterator(); iter.hasNext();) {
      Particle tem = new ElementParticle((Name)iter.next());
      if (start == null)
        start = tem;
      else
        start = new ChoiceParticle(start, tem);
    }
    return start;
  }

  public InferHandler(DatatypeLibraryFactory factory) {
    this.datatypes = new DatatypeRepertoire(factory);
  }
}
