package com.thaiopensource.validate.xerces;

import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.ValidatorHandler;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.xml.sax.ErrorHandler;

class SchemaImpl extends AbstractSchema {
  private final SymbolTable symbolTable;
  private final XMLGrammarPool grammarPool;

  SchemaImpl(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
    this.symbolTable = symbolTable;
    this.grammarPool = grammarPool;
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new ValidatorHandlerImpl(symbolTable, grammarPool, eh);
  }
}
