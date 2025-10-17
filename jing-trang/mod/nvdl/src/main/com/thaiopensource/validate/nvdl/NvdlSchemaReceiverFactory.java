package com.thaiopensource.validate.nvdl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.Option;

/**
 * A Schema receiver factory that knows how to create NVDL schema receivers.
 */
public class NvdlSchemaReceiverFactory implements SchemaReceiverFactory {
  /**
   * Checks if the namespace is the NVDL namespace and if yes then it creates
   * a schema receiver, otherwise returns null.
   */
  public SchemaReceiver createSchemaReceiver(String namespaceUri, PropertyMap properties) {
    if (!SchemaImpl.NVDL_URI.equals(namespaceUri))
      return null;
    return new SchemaReceiverImpl(properties);
  }

  /**
   * No options handling, always returns null.
   */
  public Option getOption(String uri) {
    return null;
  }
}
