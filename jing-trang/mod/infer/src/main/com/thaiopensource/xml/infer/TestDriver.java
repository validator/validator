package com.thaiopensource.xml.infer;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.xml.util.Name;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.util.Map;

public class TestDriver {
  static public void main(String[] args) throws SAXException, IOException {
    InferHandler handler = new InferHandler(new DatatypeLibraryLoader());
    XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();
    XMLReader xr = xrc.createXMLReader();
    xr.setContentHandler(handler);
    for (int i = 0; i < args.length; i++)
       xr.parse(new InputSource(UriOrFile.toUri(args[i])));
    Schema schema = handler.getSchema();
    for (Map.Entry<Name, ElementDecl> entry : schema.getElementDecls().entrySet()) {
      Name name = entry.getKey();
      String ns = name.getNamespaceUri();
      if (!ns.equals(""))
        System.out.print("{" + ns + "}");
      System.out.print(name.getLocalName());
      System.out.print(" = ");
      ElementDecl elementDecl = entry.getValue();
      Particle particle = elementDecl.getContentModel();
      if (particle != null)
        System.out.println(ParticleDumper.toString(particle, ns));
      else
        System.out.println("xsd:" + elementDecl.getDatatype().getLocalName());
      for (Map.Entry<Name, AttributeDecl> attEntry : elementDecl.getAttributeDecls().entrySet()) {
        System.out.print("  @");
        AttributeDecl att = attEntry.getValue();
        Name attName = attEntry.getKey();
        ns = attName.getNamespaceUri();
        if (!ns.equals(""))
          System.out.print("{" + ns + "}");
        System.out.print(attName.getLocalName());
        Name typeName = att.getDatatype();
        if (typeName == null)
          System.out.print(" string");
        else
          System.out.print(" xsd:" + typeName.getLocalName());
        if (att.isOptional())
          System.out.println(" optional");
        else
          System.out.println(" required");
      }
    }
  }
}
