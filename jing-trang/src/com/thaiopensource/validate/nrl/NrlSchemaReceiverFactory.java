package com.thaiopensource.validate.nrl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.FlagPropertyId;

public class NrlSchemaReceiverFactory implements SchemaReceiverFactory {
  public static final FlagPropertyId ATTRIBUTE_SCHEMA = new FlagPropertyId("ATTRIBUTE_SCHEMA");

  public SchemaReceiver createSchemaReceiver(String namespaceUri, PropertyMap properties) {
    if (!SchemaImpl.NRL_URI.equals(namespaceUri))
      return null;
    return new SchemaReceiverImpl(properties);
  }
}
