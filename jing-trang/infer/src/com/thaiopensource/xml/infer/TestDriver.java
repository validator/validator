package com.thaiopensource.xml.infer;

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import com.thaiopensource.util.UriOrFile;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class TestDriver {
  static public void main(String[] args) throws SAXException, IOException {
    InferHandler handler = new InferHandler();
    XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();
    XMLReader xr = xrc.createXMLReader();
    xr.setContentHandler(handler);
    for (int i = 0; i < args.length; i++)
       xr.parse(new InputSource(UriOrFile.toUri(args[i])));
    Schema schema = handler.getSchema();
    for (Iterator iter = schema.getElementDecls().entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      Name name = (Name)entry.getKey();
      String ns = name.getNamespaceUri();
      if (!ns.equals(""))
        System.out.print("{" + ns + "}");
      System.out.print(name.getLocalName());
      System.out.print(" = ");
      System.out.println(ParticleDumper.toString(((ElementDecl)entry.getValue()).getContentModel(), ns));
    }
  }
}
