package com.thaiopensource.validate.xerces;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.AbstractSchema;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;

class SchemaImpl extends AbstractSchema {
  private final SymbolTable symbolTable;
  private final XMLGrammarPool grammarPool;

  SchemaImpl(SymbolTable symbolTable,
             XMLGrammarPool grammarPool,
             PropertyMap properties,
             PropertyId[] supportedPropertyIds) {
    super(properties, supportedPropertyIds);
    this.symbolTable = symbolTable;
    this.grammarPool = grammarPool;
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(symbolTable, grammarPool, properties);
  }
}
