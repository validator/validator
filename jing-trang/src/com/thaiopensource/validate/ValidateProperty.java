package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;

public class ValidateProperty {
  public static final ErrorHandlerPropertyId ERROR_HANDLER = new ErrorHandlerPropertyId("ERROR_HANDLER");
  public static final EntityResolverPropertyId ENTITY_RESOLVER = new EntityResolverPropertyId("ENTITY_RESOLVER");
  public static final XMLReaderCreatorPropertyId XML_READER_CREATOR
          = new XMLReaderCreatorPropertyId("XML_READER_CREATOR");

  private ValidateProperty() { }

  public static class ErrorHandlerPropertyId extends PropertyId {
    public ErrorHandlerPropertyId(String name) {
      super(name, ErrorHandler.class);
    }

    public ErrorHandler get(PropertyMap properties) {
      return (ErrorHandler)properties.get(this);
    }

    public ErrorHandler put(PropertyMapBuilder builder, ErrorHandler value) {
      return (ErrorHandler)builder.put(this, value);
    }
  }

  public static class EntityResolverPropertyId extends PropertyId {
    public EntityResolverPropertyId(String name) {
      super(name, EntityResolver.class);
    }

    public EntityResolver get(PropertyMap properties) {
      return (EntityResolver)properties.get(this);
    }

    public EntityResolver put(PropertyMapBuilder builder, EntityResolver value) {
      return (EntityResolver)builder.put(this, value);
    }
  }

  public static class XMLReaderCreatorPropertyId extends PropertyId {
    public XMLReaderCreatorPropertyId(String name) {
      super(name, XMLReaderCreator.class);
    }

    public XMLReaderCreator get(PropertyMap properties) {
      return (XMLReaderCreator)properties.get(this);
    }

    public XMLReaderCreator put(PropertyMapBuilder builder, XMLReaderCreator value) {
      return (XMLReaderCreator)builder.put(this, value);
    }
  }
}
