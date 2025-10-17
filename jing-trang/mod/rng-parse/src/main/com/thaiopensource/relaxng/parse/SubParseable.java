package com.thaiopensource.relaxng.parse;

public interface SubParseable extends Parseable {
  ParsedPattern parseAsInclude(SchemaBuilder f, IncludedGrammar g)
          throws BuildException, IllegalSchemaException;
  /* The returned URI will have disallowed characters escaped. */
  String getUri();
}
