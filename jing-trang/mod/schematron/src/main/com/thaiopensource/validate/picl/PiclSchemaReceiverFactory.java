package com.thaiopensource.validate.picl;

import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.Option;
import com.thaiopensource.util.PropertyMap;

public class PiclSchemaReceiverFactory implements SchemaReceiverFactory {
  private static final String PICL_URI = SchemaReader.BASE_URI + "picl";
  public SchemaReceiver createSchemaReceiver(String namespaceUri, PropertyMap properties) {
    if (!PICL_URI.equals(namespaceUri))
      return null;
    return new SchemaReceiverImpl(properties);
  }

  public Option getOption(String uri) {
    return null;
  }

}
