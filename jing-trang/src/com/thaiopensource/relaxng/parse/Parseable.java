package com.thaiopensource.relaxng.parse;

public interface Parseable {
  ParsedPattern parse(SchemaBuilder f, Scope scope) throws BuildException, IllegalSchemaException;
  ParsedPattern parseInclude(String uri, SchemaBuilder f, IncludedGrammar g)
          throws BuildException, IllegalSchemaException;
  ParsedPattern parseExternal(String uri, SchemaBuilder f, Scope s)
          throws BuildException, IllegalSchemaException;
}
