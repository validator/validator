package com.thaiopensource.relaxng.mns;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.SAXSchemaLanguage;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.relaxng.CompactSchemaLanguage;
import com.thaiopensource.relaxng.auto.SchemaFuture;
import com.thaiopensource.relaxng.auto.SchemaReceiver;
import com.thaiopensource.relaxng.auto.SchemaReceiverFactory;
import com.thaiopensource.relaxng.auto.AutoSchemaLanguage;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URL;

class SchemaReceiverImpl implements SchemaReceiver {
  private static final String MNS_SCHEMA = "mns.rng";
  private static final String RNC_MEDIA_TYPE = "application/x-rnc";
  private final XMLReaderCreator xrc;
  private final ErrorHandler eh;
  private final boolean attributesSchema;
  private final SchemaOptions options;
  private final DatatypeLibraryFactory dlf;
  private final SchemaLanguage autoSchemaLanguage;
  private Schema mnsSchema = null;

  public SchemaReceiverImpl(XMLReaderCreator xrc,
                            ErrorHandler eh,
                            SchemaOptions options,
                            DatatypeLibraryFactory dlf,
                            SchemaReceiverFactory srf) {
    this.xrc = xrc;
    this.eh = eh;
    this.options = options.remove(SchemaOptions.ATTRIBUTES);
    this.attributesSchema = options.contains(SchemaOptions.ATTRIBUTES);
    this.dlf = dlf;
    this.autoSchemaLanguage = new AutoSchemaLanguage(srf);
  }

  public SchemaFuture installHandlers(XMLReader xr) {
    return new SchemaImpl(attributesSchema).installHandlers(xr, this);
  }

  Schema getMnsSchema() throws IOException, IncorrectSchemaException, SAXException {
   if (mnsSchema == null) {
      String className = SchemaReceiverImpl.class.getName();
      String resourceName = className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/resources/" + MNS_SCHEMA;
      URL mnsSchemaUrl = SchemaReceiverImpl.class.getClassLoader().getResource(resourceName);
      mnsSchema = SAXSchemaLanguage.getInstance().createSchema(xrc,
                                                               new InputSource(mnsSchemaUrl.toString()),
                                                               eh,
                                                               SchemaOptions.NONE,
                                                               dlf);
    }
    return mnsSchema;
  }

  ErrorHandler getErrorHandler() {
    return eh;
  }

  Schema createChildSchema(InputSource inputSource, String schemaType, boolean isAttributesSchema) throws IOException, IncorrectSchemaException, SAXException {
    SchemaLanguage lang = isRnc(schemaType) ? CompactSchemaLanguage.getInstance() : autoSchemaLanguage;
    return lang.createSchema(xrc,
                             inputSource,
                             eh,
                             isAttributesSchema ? options.add(SchemaOptions.ATTRIBUTES) : options,
                             dlf);
  }

  private static boolean isRnc(String schemaType) {
    if (schemaType == null)
      return false;
    schemaType = schemaType.trim();
    return schemaType.equals(RNC_MEDIA_TYPE);
  }
}
