package com.thaiopensource.datatype;

public interface DatatypeBuilder {
  void addParam(String name,
		String value,
		DatatypeContext context) throws InvalidParamException;

  Datatype finish();
}
