package com.thaiopensource.relaxng.translate.util;

import com.thaiopensource.xml.sax.Resolver;
import com.thaiopensource.xml.sax.ResolverInstantiationException;

public abstract class ResolverParam extends AbstractParam {
  private final ClassLoader loader;

  public ResolverParam(ClassLoader loader) {
    this.loader = loader;
  }

  public void set(String value) throws InvalidParamValueException {
    try {
      setResolver(Resolver.newInstance(value, loader));
    }
    catch (ResolverInstantiationException e) {
      throw new InvalidParamValueException(e.getMessage());
    }
  }
  
  protected abstract void setResolver(Resolver resolver) throws InvalidParamValueException;
}
