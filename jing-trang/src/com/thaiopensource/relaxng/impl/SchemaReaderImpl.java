package com.thaiopensource.relaxng.impl;

import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.rng.RngProperty;
import com.thaiopensource.validate.nrl.NrlSchemaReceiverFactory;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.util.PropertyMap;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class SchemaReaderImpl implements SchemaReader {
  public Schema createSchema(InputSource in, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    XMLReaderCreator xrc = ValidateProperty.XML_READER_CREATOR.get(properties);
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    DatatypeLibraryFactory dlf = RngProperty.DATATYPE_LIBRARY_FACTORY.get(properties);
    Pattern start = SchemaBuilderImpl.parse(createParseable(xrc, in, eh), eh, dlf, spb,
                                            properties.contains(NrlSchemaReceiverFactory.ATTRIBUTE_SCHEMA));
    return wrapPattern(start, spb, properties);
  }

  static Schema wrapPattern(Pattern start, SchemaPatternBuilder spb, PropertyMap properties) throws SAXException, IncorrectSchemaException {
    if (properties.contains(RngProperty.FEASIBLE))
      start = FeasibleTransform.transform(spb, start);
    Schema schema = new PatternSchema(spb, start);
    if (spb.hasIdTypes() && properties.contains(RngProperty.CHECK_ID_IDREF)) {
      ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
      IdTypeMap idTypeMap = new IdTypeMapBuilder(eh, start).getIdTypeMap();
      if (idTypeMap == null)
        throw new IncorrectSchemaException();
      Schema idSchema;
      if (properties.contains(RngProperty.FEASIBLE))
        idSchema = new FeasibleIdTypeMapSchema(idTypeMap);
      else
        idSchema = new IdTypeMapSchema(idTypeMap);
      schema = new CombineSchema(schema, idSchema);
    }
    return schema;
  }

  protected abstract Parseable createParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh);
}
