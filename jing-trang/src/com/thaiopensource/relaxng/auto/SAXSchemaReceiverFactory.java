package com.thaiopensource.relaxng.auto;

import org.xml.sax.ErrorHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.relaxng.impl.SchemaReceiverImpl;
import com.thaiopensource.relaxng.parse.sax.SAXParseReceiver;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;

public class SAXSchemaReceiverFactory implements SchemaReceiverFactory {
  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             XMLReaderCreator xrc,
                                             ErrorHandler eh,
                                             SchemaOptions options,
                                             DatatypeLibraryFactory dlf,
                                             SchemaReceiverFactory srf) {
    // XXX allow namespaces with incorrect version
    if (!WellKnownNamespaces.RELAX_NG.equals(namespaceUri))
      return null;
    return new SchemaReceiverImpl(new SAXParseReceiver(xrc, eh), eh, options, dlf);
  }
}
