package com.thaiopensource.relaxng.auto;

public class SchemaLanguageLoaderSchemaReceiverFactory extends SchemaLanguageFactorySchemaReceiverFactory {
  public SchemaLanguageLoaderSchemaReceiverFactory() {
    super(new SchemaLanguageLoader());
  }
}
