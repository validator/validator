package org.javacc.parser;

public interface TokenManagerCodeGenerator {
  /**
   * Generate the code for the token manager. Note that the code generator just
   * produces a buffer.
   */
  void generateCode(TokenizerData tokenizerData);

  /**
   * Complete the code generation and save any output file(s).
   */
  void finish(TokenizerData tokenizerData);
}
