package com.thaiopensource.relaxng.parse;

public interface SubParseable extends Parseable {
  ParsedPattern parseAsInclude(SchemaBuilder f, IncludedGrammar g)
          throws BuildException, IllegalSchemaException;
  String getUri();
}
