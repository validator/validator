package com.thaiopensource.relaxng.mns2;

import com.thaiopensource.relaxng.auto.SchemaReceiverFactory;
import com.thaiopensource.relaxng.auto.SchemaReceiver;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;
import org.xml.sax.ErrorHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class Mns2SchemaReceiverFactory implements SchemaReceiverFactory {
  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             XMLReaderCreator xrc,
                                             ErrorHandler eh,
                                             SchemaOptions options,
                                             DatatypeLibraryFactory dlf,
                                             SchemaReceiverFactory parent) {
    if (!SchemaImpl.MNS2_URI.equals(namespaceUri))
      return null;
    return new SchemaReceiverImpl(xrc, eh, options, dlf, parent);
  }
}
