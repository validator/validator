package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.xml.sax.Resolver;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.EntityResolver;

import javax.xml.transform.URIResolver;

public class ResolverFactory {
  static public Resolver createResolver(PropertyMap properties) {
    Resolver resolver = ValidateProperty.RESOLVER.get(properties);
    if (resolver != null)
      return resolver;
    XMLReaderCreator xrc = ValidateProperty.XML_READER_CREATOR.get(properties);
    URIResolver uriResolver = ValidateProperty.URI_RESOLVER.get(properties);
    EntityResolver entityResolver = ValidateProperty.ENTITY_RESOLVER.get(properties);
    return Resolver.newInstance(xrc, uriResolver, entityResolver);
  }
}
