package com.thaiopensource.xml.infer;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import com.thaiopensource.relaxng.output.common.Name;

public class InferHandler extends DefaultHandler {
  private final Map inferrerMap = new HashMap();
  private OpenElement openElement = null;
  private final Set startSet = new HashSet();

  private static class OpenElement {
    OpenElement parent;
    ContentModelInferrer inferrer;
    Name prevElementName = ContentModelInferrer.START;

    public OpenElement(OpenElement parent, ContentModelInferrer inferrer) {
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
    else
      noteElement(name);
    ContentModelInferrer inferrer = (ContentModelInferrer)inferrerMap.get(name);
    if (inferrer == null) {
      inferrer = new ContentModelInferrer();
      inferrerMap.put(name, inferrer);
    }
    openElement = new OpenElement(openElement, inferrer);
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    for (int i = 0; i < length; i++)
      switch (ch[start + i]) {
      case ' ':
      case '\t':
      case '\n':
      case '\r':
        break;
      default:
        noteElement(ContentModelInferrer.TEXT);
        return;
      }
  }

  private void noteElement(Name name) {
    if (openElement.prevElementName.equals(name))
      openElement.inferrer.setMulti(name);
    else {
      openElement.inferrer.addSequence(openElement.prevElementName, name);
      openElement.prevElementName = name;
    }
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    openElement.inferrer.addSequence(openElement.prevElementName, ContentModelInferrer.END);
    openElement = openElement.parent;
  }

  public Schema getSchema() {
    Schema schema = new Schema();
    for (Iterator iter = inferrerMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      ElementDecl decl = new ElementDecl();
      decl.setContentModel(((ContentModelInferrer)entry.getValue()).inferContentModel());
      Name name = (Name)entry.getKey();
      decl.setStart(startSet.contains(name));
      schema.getElementDecls().put(name, decl);
    }
    return schema;
  }

}
