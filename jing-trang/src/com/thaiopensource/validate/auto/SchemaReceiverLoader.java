package com.thaiopensource.validate.auto;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.Service;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;

import java.util.Enumeration;

public class SchemaReceiverLoader implements SchemaReceiverFactory {
  private final Service service = new Service(SchemaReceiverFactory.class);
  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             PropertyMap properties) {
    for (Enumeration enum = service.getProviders(); enum.hasMoreElements();) {
      SchemaReceiverFactory srf = (SchemaReceiverFactory)enum.nextElement();
      SchemaReceiver sr = srf.createSchemaReceiver(namespaceUri, properties);
      if (sr != null)
        return sr;
    }
    return null;
  }
}
