package com.thaiopensource.relaxng.mns;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.SAXSchemaLanguage;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.SchemaLanguage;
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
  private final XMLReaderCreator xrc;
  private final ErrorHandler eh;
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
    this.options = options;
    this.dlf = dlf;
    this.autoSchemaLanguage = new AutoSchemaLanguage(srf);
  }

  public SchemaFuture installHandlers(XMLReader xr) {
    return new SchemaImpl().installHandlers(xr, this);
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

  Schema createChildSchema(InputSource inputSource) throws IOException, IncorrectSchemaException, SAXException {
    return autoSchemaLanguage.createSchema(xrc, inputSource, eh, options, dlf);
  }
}
