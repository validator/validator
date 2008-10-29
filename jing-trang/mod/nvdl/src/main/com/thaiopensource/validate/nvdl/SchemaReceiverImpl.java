package com.thaiopensource.validate.nvdl;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.auto.SchemaFuture;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.prop.wrap.WrapProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.util.Name;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URL;

class SchemaReceiverImpl implements SchemaReceiver {
  private static final String NVDL_SCHEMA = "nvdl.rng";
  private static final String RNC_MEDIA_TYPE = "application/x-rnc";
  private final PropertyMap properties;
  private final Name attributeOwner;
  private final SchemaReader autoSchemaReader;
  private Schema nvdlSchema = null;
  private static final PropertyId subSchemaProperties[] = {
    ValidateProperty.ERROR_HANDLER,
    ValidateProperty.XML_READER_CREATOR,
    ValidateProperty.ENTITY_RESOLVER,
    SchemaReceiverFactory.PROPERTY,
  };

  public SchemaReceiverImpl(PropertyMap properties) {
    this.attributeOwner = WrapProperty.ATTRIBUTE_OWNER.get(properties);
    PropertyMapBuilder builder = new PropertyMapBuilder();
    for (int i = 0; i < subSchemaProperties.length; i++) {
      Object value = properties.get(subSchemaProperties[i]);
      if (value != null)
        builder.put(subSchemaProperties[i], value);
    }
    this.properties = builder.toPropertyMap();
    this.autoSchemaReader = new AutoSchemaReader(SchemaReceiverFactory.PROPERTY.get(properties));
  }

  public SchemaFuture installHandlers(XMLReader xr) {
    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    if (attributeOwner != null)
      WrapProperty.ATTRIBUTE_OWNER.put(builder, attributeOwner);
    return new SchemaImpl(builder.toPropertyMap()).installHandlers(xr, this);
  }

  Schema getNvdlSchema() throws IOException, IncorrectSchemaException, SAXException {
    if (nvdlSchema == null) {
      String className = SchemaReceiverImpl.class.getName();
      String resourceName = className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resources/" + NVDL_SCHEMA;
      URL nvdlSchemaUrl = getResource(resourceName);
      nvdlSchema = SAXSchemaReader.getInstance().createSchema(new InputSource(nvdlSchemaUrl.toString()),
                                                              properties);
    }
    return nvdlSchema;
  }

  private static URL getResource(String resourceName) {
    ClassLoader cl = SchemaReceiverImpl.class.getClassLoader();
    // XXX see if we should borrow 1.2 code from Service
    if (cl == null)
      return ClassLoader.getSystemResource(resourceName);
    else
      return cl.getResource(resourceName);
  }

  PropertyMap getProperties() {
    return properties;
  }

  Schema createChildSchema(InputSource inputSource, String schemaType, PropertyMap options, boolean isAttributesSchema) throws IOException, IncorrectSchemaException, SAXException {
    SchemaReader reader = isRnc(schemaType) ? CompactSchemaReader.getInstance() : autoSchemaReader;
    PropertyMapBuilder builder = new PropertyMapBuilder(properties);
    if (isAttributesSchema)
      WrapProperty.ATTRIBUTE_OWNER.put(builder, ValidatorImpl.OWNER_NAME);
    for (int i = 0, len = options.size(); i < len; i++)
      builder.put(options.getKey(i), options.get(options.getKey(i)));
    return reader.createSchema(inputSource, builder.toPropertyMap());
  }

  Option getOption(String uri) {
    Option option = autoSchemaReader.getOption(uri);
    if (option != null)
      return option;
    return CompactSchemaReader.getInstance().getOption(uri);
  }

  private static boolean isRnc(String schemaType) {
    if (schemaType == null)
      return false;
    schemaType = schemaType.trim();
    return schemaType.equals(RNC_MEDIA_TYPE);
  }
}
