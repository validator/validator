package com.thaiopensource.relaxng.nrl;

import com.thaiopensource.relaxng.auto.SchemaReceiverFactory;
import com.thaiopensource.relaxng.auto.SchemaReceiver;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;
import org.xml.sax.ErrorHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class NrlSchemaReceiverFactory implements SchemaReceiverFactory {
  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             XMLReaderCreator xrc,
                                             ErrorHandler eh,
                                             SchemaOptions options,
                                             DatatypeLibraryFactory dlf,
                                             SchemaReceiverFactory parent) {
    if (!SchemaImpl.NRL_URI.equals(namespaceUri))
      return null;
    return new SchemaReceiverImpl(xrc, eh, options, dlf, parent);
  }
}
