package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.Schema;
import com.thaiopensource.relaxng.SchemaLanguage;
import com.thaiopensource.relaxng.SchemaOptions;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.parse.Parseable;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class SchemaLanguageImpl implements SchemaLanguage {
  public Schema createSchema(XMLReaderCreator xrc, InputSource in, ErrorHandler eh, SchemaOptions options, DatatypeLibraryFactory dlf)
          throws IOException, SAXException, IncorrectSchemaException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    Pattern start = SchemaBuilderImpl.parse(createParseable(xrc, in, eh), eh, dlf, spb);
    return wrapPattern(start, spb, eh, options);
  }

  static Schema wrapPattern(Pattern start, SchemaPatternBuilder spb, ErrorHandler eh, SchemaOptions options) throws SAXException, IncorrectSchemaException {
    if (options.contains(SchemaOptions.FEASIBLE))
      start = FeasibleTransform.transform(spb, start);
    Schema schema = new PatternSchema(spb, start);
    if (spb.hasIdTypes() && options.contains(SchemaOptions.CHECK_ID_IDREF)) {
      IdTypeMap idTypeMap = new IdTypeMapBuilder(eh, start).getIdTypeMap();
      if (idTypeMap == null)
        throw new IncorrectSchemaException();
      Schema idSchema;
      if (options.contains(SchemaOptions.FEASIBLE))
        idSchema = new FeasibleIdTypeMapSchema(idTypeMap);
      else
        idSchema = new IdTypeMapSchema(idTypeMap);
      schema = new CombineSchema(schema, idSchema);
    }
    return schema;
  }

  protected abstract Parseable createParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh);
}
