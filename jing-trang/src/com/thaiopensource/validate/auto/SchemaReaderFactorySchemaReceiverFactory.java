package com.thaiopensource.validate.auto;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.SchemaReaderFactory;

public class SchemaReaderFactorySchemaReceiverFactory implements SchemaReceiverFactory {
  private final SchemaReaderFactory srf;

  public SchemaReaderFactorySchemaReceiverFactory(SchemaReaderFactory srf) {
    this.srf = srf;
  }

  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             PropertyMap properties) {
    SchemaReader sr = srf.createSchemaReader(namespaceUri);
    if (sr == null)
      return null;
    return new SchemaReaderSchemaReceiver(sr, properties);
  }
}
