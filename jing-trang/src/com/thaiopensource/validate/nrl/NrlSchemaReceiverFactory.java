package com.thaiopensource.validate.nrl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.Option;

public class NrlSchemaReceiverFactory implements SchemaReceiverFactory {
  public SchemaReceiver createSchemaReceiver(String namespaceUri, PropertyMap properties) {
    if (!SchemaImpl.NRL_URI.equals(namespaceUri))
      return null;
    return new SchemaReceiverImpl(properties);
  }

  public Option getOption(String uri) {
    return null;
  }
}
