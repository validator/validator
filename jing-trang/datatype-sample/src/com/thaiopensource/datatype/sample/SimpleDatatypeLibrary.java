package com.thaiopensource.datatype.sample;

import org.relaxng.datatype.*;
import org.relaxng.datatype.helpers.ParameterlessDatatypeBuilder;
import org.relaxng.datatype.helpers.StreamingValidatorImpl;

public abstract class SimpleDatatypeLibrary implements Datatype,
						       DatatypeLibrary,
						       DatatypeLibraryFactory {
  private final String uri;
  private final String localName;
  private final DatatypeBuilder datatypeBuilder;

  protected SimpleDatatypeLibrary(String uri, String localName) {
    this.uri = uri;
    this.localName = localName;
    this.datatypeBuilder = new ParameterlessDatatypeBuilder(this);
  }

  public DatatypeLibrary createDatatypeLibrary(String uri) {
    return this.uri.equals(uri) ? this : null;
  }

  public DatatypeBuilder createDatatypeBuilder(String localName)
    throws DatatypeException {
    if (!this.localName.equals(localName))
      throw new DatatypeException();
    return datatypeBuilder;
  }

  public Datatype createDatatype(String localName)
    throws DatatypeException {
    return createDatatypeBuilder(localName).createDatatype();
  }

  protected abstract boolean isValid(String literal);

  public void checkValid(String literal, ValidationContext context)
    throws DatatypeException {
    if (!isValid(literal, context))
      throw new DatatypeException();
  }

  public boolean isValid(String literal, ValidationContext context) {
    return isValid(literal);
  }

  public DatatypeStreamingValidator createStreamingValidator(ValidationContext context) {
    return new StreamingValidatorImpl(this, context);
  }

  public Object createValue(String literal, ValidationContext context) {
    if (!isValid(literal, context))
      return null;
    return literal;
  }

  public boolean sameValue(Object obj1, Object obj2) {
    return obj1.equals(obj2);
  }

  public int valueHashCode(Object obj) {
    return obj.hashCode();
  }

  public int getIdType() {
    return ID_TYPE_NULL;
  }

  public boolean isContextDependent() {
    return false;
  }
}
