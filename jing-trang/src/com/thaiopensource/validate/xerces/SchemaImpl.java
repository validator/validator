package com.thaiopensource.validate.xerces;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.Validator;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;

class SchemaImpl implements Schema {
  private final SymbolTable symbolTable;
  private final XMLGrammarPool grammarPool;

  SchemaImpl(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
    this.symbolTable = symbolTable;
    this.grammarPool = grammarPool;
  }

  public Validator createValidator(PropertyMap properties) {
    return new ValidatorImpl(symbolTable, grammarPool, properties);
  }
}
