package com.thaiopensource.relaxng.auto;

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.util.Service;
import org.xml.sax.ErrorHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.util.Enumeration;

public class SchemaLanguageFactorySchemaReceiverFactory implements SchemaReceiverFactory {
  private final SchemaLanguageFactory slf;

  public SchemaLanguageFactorySchemaReceiverFactory(SchemaLanguageFactory slf) {
    this.slf = slf;
  }

  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             XMLReaderCreator xrc,
                                             ErrorHandler eh,
                                             SchemaOptions options,
                                             DatatypeLibraryFactory dlf,
                                             SchemaReceiverFactory parent) {
    SchemaLanguage sl = slf.createSchemaLanguage(namespaceUri);
    if (sl == null)
      return null;
    return new SchemaLanguageSchemaReceiver(sl, xrc, eh, options, dlf);
  }
}
