package com.thaiopensource.relaxng.auto;

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.util.Service;
import org.xml.sax.ErrorHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.util.Enumeration;

public class SchemaReceiverLoader implements SchemaReceiverFactory {
  private final Service service = new Service(SchemaReceiverFactory.class);
  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             XMLReaderCreator xrc,
                                             ErrorHandler eh,
                                             SchemaOptions options,
                                             DatatypeLibraryFactory dlf,
                                             SchemaReceiverFactory parent) {
    for (Enumeration enum = service.getProviders(); enum.hasMoreElements();) {
      SchemaReceiverFactory srf = (SchemaReceiverFactory)enum.nextElement();
      SchemaReceiver sr = srf.createSchemaReceiver(namespaceUri, xrc, eh, options, dlf,
                                                   parent == null ? this : parent);
      if (sr != null)
        return sr;
    }
    return null;
  }
}
