package com.thaiopensource.relaxng.auto;

import org.xml.sax.ErrorHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;
import com.thaiopensource.relaxng.auto.SchemaReceiver;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;

public interface SchemaReceiverFactory {
  SchemaReceiver createSchemaReceiver(String namespaceUri,
                                      XMLReaderCreator xrc,
                                      ErrorHandler eh,
                                      SchemaOptions options,
                                      DatatypeLibraryFactory dlf,
                                      SchemaReceiverFactory parent);
}
