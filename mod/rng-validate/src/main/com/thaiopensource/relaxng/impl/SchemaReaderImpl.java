package com.thaiopensource.relaxng.impl;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.AbstractSchemaReader;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.prop.wrap.WrapProperty;
import com.thaiopensource.xml.sax.Resolver;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

public abstract class SchemaReaderImpl extends AbstractSchemaReader {
  private static final PropertyId[] supportedPropertyIds = {
    ValidateProperty.XML_READER_CREATOR,
    ValidateProperty.ERROR_HANDLER,
    ValidateProperty.ENTITY_RESOLVER,
    ValidateProperty.URI_RESOLVER,
    ValidateProperty.RESOLVER,
    RngProperty.DATATYPE_LIBRARY_FACTORY,
    RngProperty.CHECK_ID_IDREF,
    RngProperty.FEASIBLE,
    WrapProperty.ATTRIBUTE_OWNER,
  };

  public Schema createSchema(SAXSource source, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    Resolver resolver = ResolverFactory.createResolver(properties);
    ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
    DatatypeLibraryFactory dlf = RngProperty.DATATYPE_LIBRARY_FACTORY.get(properties);
    if (dlf == null)
      dlf = new DatatypeLibraryLoader();
    try {
      Pattern start = SchemaBuilderImpl.parse(createParseable(source, resolver, eh), eh, dlf, spb,
                                              properties.contains(WrapProperty.ATTRIBUTE_OWNER));
      return wrapPattern(start, spb, properties);
    }
    catch (IllegalSchemaException e) {
      throw new IncorrectSchemaException();
    }
  }

  public Option getOption(String uri) {
    return RngProperty.getOption(uri);
  }

  static Schema wrapPattern(Pattern start, SchemaPatternBuilder spb, PropertyMap properties) throws SAXException, IncorrectSchemaException {
    properties = AbstractSchema.filterProperties(properties, supportedPropertyIds);
    if (properties.contains(RngProperty.FEASIBLE))
      start = FeasibleTransform.transform(spb, start);
    Schema schema = new PatternSchema(spb, start, properties);
    if (spb.hasIdTypes() && properties.contains(RngProperty.CHECK_ID_IDREF)) {
      ErrorHandler eh = ValidateProperty.ERROR_HANDLER.get(properties);
      IdTypeMap idTypeMap = new IdTypeMapBuilder(eh, start).getIdTypeMap();
      if (idTypeMap == null)
        throw new IncorrectSchemaException();
      Schema idSchema;
      if (properties.contains(RngProperty.FEASIBLE))
        idSchema = new FeasibleIdTypeMapSchema(idTypeMap, properties);
      else
        idSchema = new IdTypeMapSchema(idTypeMap, properties);
      schema = new CombineSchema(schema, idSchema, properties);
    }
    return schema;
  }

  protected abstract Parseable createParseable(SAXSource source, Resolver resolver, ErrorHandler eh) throws SAXException;
}
