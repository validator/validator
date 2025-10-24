// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javacc.parser;

import static org.javacc.parser.JavaCCGlobals.cu_from_insertion_point_2;
import static org.javacc.parser.JavaCCGlobals.cu_name;
import static org.javacc.parser.JavaCCGlobals.cu_to_insertion_point_1;
import static org.javacc.parser.JavaCCGlobals.cu_to_insertion_point_2;
import static org.javacc.parser.JavaCCGlobals.getFileExtension;
import static org.javacc.parser.JavaCCGlobals.getIdString;
import static org.javacc.parser.JavaCCGlobals.jj2index;
import static org.javacc.parser.JavaCCGlobals.jjtreeGenerated;
import static org.javacc.parser.JavaCCGlobals.lookaheadNeeded;
import static org.javacc.parser.JavaCCGlobals.maskVals;
import static org.javacc.parser.JavaCCGlobals.maskindex;
import static org.javacc.parser.JavaCCGlobals.staticOpt;
import static org.javacc.parser.JavaCCGlobals.tokenCount;
import static org.javacc.parser.JavaCCGlobals.toolName;
import static org.javacc.parser.JavaCCGlobals.toolNames;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Generate the parser.
 */
public class ParseGen extends CodeGenerator implements JavaCCParserConstants {

	public void start(boolean isJavaModernMode) throws MetaParseException {

		Token t = null;

		if (JavaCCErrors.get_error_count() != 0) {
			throw new MetaParseException();
		}

		if (Options.getBuildParser()) {
			final List<String> tn = new ArrayList<String>(toolNames);
			tn.add(toolName);

			// This is the first line generated -- the the comment line at the top of the generated parser
			genCodeLine("/* " + getIdString(tn, cu_name + ".java") + " */");

			boolean implementsExists = false;
			//final boolean extendsExists = false;

			if (cu_to_insertion_point_1.size() != 0) {
				Object firstToken = cu_to_insertion_point_1.get(0);
				printTokenSetup((Token) firstToken);
				this.ccol = 1;
				for (final Iterator<Token> it = cu_to_insertion_point_1.iterator(); it.hasNext();) {
					t = it.next();
					if (t.kind == IMPLEMENTS) {
						implementsExists = true;
					} else if (t.kind == CLASS) {
						implementsExists = false;
					}

					printToken(t);
				}
			}

			if (implementsExists) {
				genCode(", ");
			} else {
				genCode(" implements ");
			}
			genCode(cu_name + "Constants ");
			if (cu_to_insertion_point_2.size() != 0) {
				printTokenSetup((cu_to_insertion_point_2.get(0)));
				for (final Iterator<Token> it = cu_to_insertion_point_2.iterator(); it.hasNext();) {
					printToken(it.next());
				}
			}

			genCodeLine("");
			genCodeLine("");

			new ParseEngine().build(this);

			if (Options.getStatic()) {
				genCodeLine("  static private " + Options.getBooleanType()
						+ " jj_initialized_once = false;");
			}
			if (Options.getUserTokenManager()) {
				genCodeLine("  /** User defined Token Manager. */");
				genCodeLine("  " + staticOpt() + "public TokenManager token_source;");
			} else {
				genCodeLine("  /** Generated Token Manager. */");
				genCodeLine("  " + staticOpt() + "public " + cu_name + "TokenManager token_source;");
				if (!Options.getUserCharStream()) {
					if (Options.getJavaUnicodeEscape()) {
						genCodeLine("  " + staticOpt() + "JavaCharStream jj_input_stream;");
					} else {
						genCodeLine("  " + staticOpt() + "SimpleCharStream jj_input_stream;");
					}
				}
			}
			genCodeLine("  /** Current token. */");
			genCodeLine("  " + staticOpt() + "public Token token;");
			genCodeLine("  /** Next token. */");
			genCodeLine("  " + staticOpt() + "public Token jj_nt;");
			if (!Options.getCacheTokens()) {
				genCodeLine("  " + staticOpt() + "private int jj_ntk;");
			}
			if (Options.getDepthLimit() > 0) {
				genCodeLine("  " + staticOpt() + "private int jj_depth;");
			}
			if (jj2index != 0) {
				genCodeLine("  " + staticOpt() + "private Token jj_scanpos, jj_lastpos;");
				genCodeLine("  " + staticOpt() + "private int jj_la;");
				if (lookaheadNeeded) {
					genCodeLine("  /** Whether we are looking ahead. */");
					genCodeLine("  " + staticOpt() + "private " + Options.getBooleanType()
							+ " jj_lookingAhead = false;");
					genCodeLine("  " + staticOpt() + "private " + Options.getBooleanType()
							+ " jj_semLA;");
				}
			}
			if (Options.getErrorReporting()) {
				genCodeLine("  " + staticOpt() + "private int jj_gen;");
				genCodeLine("  " + staticOpt() + "final private int[] jj_la1 = new int["
						+ maskindex + "];");
				final int tokenMaskSize = (tokenCount - 1) / 32 + 1;
				for (int i = 0; i < tokenMaskSize; i++) {
					genCodeLine("  static private int[] jj_la1_" + i + ";");
				}
				genCodeLine("  static {");
				for (int i = 0; i < tokenMaskSize; i++) {
					genCodeLine("	   jj_la1_init_" + i + "();");
				}
				genCodeLine("	}");
				for (int i = 0; i < tokenMaskSize; i++) {
					genCodeLine("	private static void jj_la1_init_" + i + "() {");
					genCode("	   jj_la1_" + i + " = new int[] {");
					for (final Iterator<?> it = maskVals.iterator(); it.hasNext();) {
						final int[] tokenMask = (int[]) (it.next());
						genCode("0x" + Integer.toHexString(tokenMask[i]) + ",");
					}
					genCodeLine("};");
					genCodeLine("	}");
				}
			}
			if (jj2index != 0 && Options.getErrorReporting()) {
				genCodeLine("  " + staticOpt() + "final private JJCalls[] jj_2_rtns = new JJCalls["
						+ jj2index + "];");
				genCodeLine("  " + staticOpt() + "private " + Options.getBooleanType()
						+ " jj_rescan = false;");
				genCodeLine("  " + staticOpt() + "private int jj_gc = 0;");
			}
			genCodeLine("");

			if (Options.getDebugParser()) {
				genCodeLine("  {");
				genCodeLine("      enable_tracing();");
				genCodeLine("  }");
			}

			if (!Options.getUserTokenManager()) {
				if (Options.getUserCharStream()) {
					genCodeLine("  /** Constructor with user supplied CharStream. */");
					genCodeLine("  public " + cu_name + "(CharStream stream) {");
					if (Options.getStatic()) {
						genCodeLine("	 if (jj_initialized_once) {");
						genCodeLine("	   System.out.println(\"ERROR: Second call to constructor of static parser.  \");");
						genCodeLine("	   System.out.println(\"	   You must either use ReInit() "
								+ "or set the JavaCC option STATIC to false\");");
						genCodeLine("	   System.out.println(\"	   during parser generation.\");");
						genCodeLine("	   throw new "+(Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException")+"();");
						genCodeLine("	 }");
						genCodeLine("	 jj_initialized_once = true;");
					}
					if (Options.getTokenManagerUsesParser()) {
						genCodeLine("	 token_source = new " + cu_name
								+ "TokenManager(this, stream);");
					} else {
						genCodeLine("	 token_source = new " + cu_name + "TokenManager(stream);");
					}
					genCodeLine("	 token = new Token();");
					if (Options.getCacheTokens()) {
						genCodeLine("	 token.next = jj_nt = token_source.getNextToken();");
					} else {
						genCodeLine("	 jj_ntk = -1;");
					}
					if (Options.getDepthLimit() > 0) {
						genCodeLine("    jj_depth = -1;");
					}
					if (Options.getErrorReporting()) {
						genCodeLine("	 jj_gen = 0;");
						if (maskindex > 0) {
							genCodeLine("	 for (int i = 0; i < " + maskindex
									+ "; i++) jj_la1[i] = -1;");
						}
						if (jj2index != 0) {
							genCodeLine("	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
						}
					}
					genCodeLine("  }");
					genCodeLine("");
					genCodeLine("  /** Reinitialise. */");
					genCodeLine("  " + staticOpt() + "public void ReInit(CharStream stream) {");

					if (Options.isTokenManagerRequiresParserAccess()) {
						genCodeLine("	 token_source.ReInit(this,stream);");
					} else {
						genCodeLine("	 token_source.ReInit(stream);");
					}


					genCodeLine("	 token = new Token();");
					if (Options.getCacheTokens()) {
						genCodeLine("	 token.next = jj_nt = token_source.getNextToken();");
					} else {
						genCodeLine("	 jj_ntk = -1;");
					}
					if (Options.getDepthLimit() > 0) {
						genCodeLine("    jj_depth = -1;");
					}
					if (lookaheadNeeded) {
						genCodeLine("	 jj_lookingAhead = false;");
					}
					if (jjtreeGenerated) {
						genCodeLine("	 jjtree.reset();");
					}
					if (Options.getErrorReporting()) {
						genCodeLine("	 jj_gen = 0;");
						if (maskindex > 0) {
							genCodeLine("	 for (int i = 0; i < " + maskindex
									+ "; i++) jj_la1[i] = -1;");
						}
						if (jj2index != 0) {
							genCodeLine("	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
						}
					}
					genCodeLine("  }");
				} else {

					if (!isJavaModernMode) {
						genCodeLine("  /** Constructor with InputStream. */");
						genCodeLine("  public " + cu_name + "(java.io.InputStream stream) {");
						genCodeLine("	  this(stream, null);");
						genCodeLine("  }");
						genCodeLine("  /** Constructor with InputStream and supplied encoding */");
						genCodeLine("  public " + cu_name
								+ "(java.io.InputStream stream, String encoding) {");
						if (Options.getStatic()) {
							genCodeLine("	 if (jj_initialized_once) {");
							genCodeLine("	   System.out.println(\"ERROR: Second call to constructor of static parser.  \");");
							genCodeLine("	   System.out.println(\"	   You must either use ReInit() or "
									+ "set the JavaCC option STATIC to false\");");
							genCodeLine("	   System.out.println(\"	   during parser generation.\");");
							genCodeLine("	   throw new "+(Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException")+"();");
							genCodeLine("	 }");
							genCodeLine("	 jj_initialized_once = true;");
						}

						if (Options.getJavaUnicodeEscape()) {
							if (!Options.getGenerateChainedException()) {
								genCodeLine("	 try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } "
										+ "catch(java.io.UnsupportedEncodingException e) {"
										+ " throw new RuntimeException(e.getMessage()); }");
							} else {
								genCodeLine("	 try { jj_input_stream = new JavaCharStream(stream, encoding, 1, 1); } "
										+ "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
							}
						} else {
							if (!Options.getGenerateChainedException()) {
								genCodeLine("	 try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } "
										+ "catch(java.io.UnsupportedEncodingException e) { "
										+ "throw new RuntimeException(e.getMessage()); }");
							} else {
								genCodeLine("	 try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } "
										+ "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
							}
						}
						if (Options.getTokenManagerUsesParser() && !Options.getStatic()) {
							genCodeLine("	 token_source = new " + cu_name
									+ "TokenManager(this, jj_input_stream);");
						} else {
							genCodeLine("	 token_source = new " + cu_name
									+ "TokenManager(jj_input_stream);");
						}
						genCodeLine("	 token = new Token();");
						if (Options.getCacheTokens()) {
							genCodeLine("	 token.next = jj_nt = token_source.getNextToken();");
						} else {
							genCodeLine("	 jj_ntk = -1;");
						}
						if (Options.getDepthLimit() > 0) {
							genCodeLine("    jj_depth = -1;");
						}
						if (Options.getErrorReporting()) {
							genCodeLine("	 jj_gen = 0;");
							if (maskindex > 0) {
								genCodeLine("	 for (int i = 0; i < " + maskindex
										+ "; i++) jj_la1[i] = -1;");
							}
							if (jj2index != 0) {
								genCodeLine("	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
							}
						}
						genCodeLine("  }");
						genCodeLine("");

						genCodeLine("  /** Reinitialise. */");
						genCodeLine("  " + staticOpt()
								+ "public void ReInit(java.io.InputStream stream) {");
						genCodeLine("	  ReInit(stream, null);");
						genCodeLine("  }");

						genCodeLine("  /** Reinitialise. */");
						genCodeLine("  "
								+ staticOpt()
								+ "public void ReInit(java.io.InputStream stream, String encoding) {");
						if (!Options.getGenerateChainedException()) {
							genCodeLine("	 try { jj_input_stream.ReInit(stream, encoding, 1, 1); } "
									+ "catch(java.io.UnsupportedEncodingException e) { "
									+ "throw new RuntimeException(e.getMessage()); }");
						} else {
							genCodeLine("	 try { jj_input_stream.ReInit(stream, encoding, 1, 1); } "
									+ "catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }");
						}

						if (Options.isTokenManagerRequiresParserAccess()) {
							genCodeLine("	 token_source.ReInit(this,jj_input_stream);");
						} else {
							genCodeLine("	 token_source.ReInit(jj_input_stream);");
						}

						genCodeLine("	 token = new Token();");
						if (Options.getCacheTokens()) {
							genCodeLine("	 token.next = jj_nt = token_source.getNextToken();");
						} else {
							genCodeLine("	 jj_ntk = -1;");
						}
						if (Options.getDepthLimit() > 0) {
							genCodeLine("    jj_depth = -1;");
						}
						if (jjtreeGenerated) {
							genCodeLine("	 jjtree.reset();");
						}
						if (Options.getErrorReporting()) {
							genCodeLine("	 jj_gen = 0;");
							genCodeLine("	 for (int i = 0; i < " + maskindex
									+ "; i++) jj_la1[i] = -1;");
							if (jj2index != 0) {
								genCodeLine("	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
							}
						}
						genCodeLine("  }");
						genCodeLine("");

					}

					final String readerInterfaceName = isJavaModernMode ? "Provider" : "java.io.Reader";
					final String stringReaderClass = isJavaModernMode ? "StringProvider"
							: "java.io.StringReader";


					genCodeLine("  /** Constructor. */");
					genCodeLine("  public " + cu_name + "(" + readerInterfaceName + " stream) {");
					if (Options.getStatic()) {
						genCodeLine("	 if (jj_initialized_once) {");
						genCodeLine("	   System.out.println(\"ERROR: Second call to constructor of static parser. \");");
						genCodeLine("	   System.out.println(\"	   You must either use ReInit() or "
								+ "set the JavaCC option STATIC to false\");");
						genCodeLine("	   System.out.println(\"	   during parser generation.\");");
						genCodeLine("	   throw new "+(Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException")+"();");
						genCodeLine("	 }");
						genCodeLine("	 jj_initialized_once = true;");
					}
					if (Options.getJavaUnicodeEscape()) {
						genCodeLine("	 jj_input_stream = new JavaCharStream(stream, 1, 1);");
					} else {
						genCodeLine("	 jj_input_stream = new SimpleCharStream(stream, 1, 1);");
					}
					if (Options.getTokenManagerUsesParser() && !Options.getStatic()) {
						genCodeLine("	 token_source = new " + cu_name
								+ "TokenManager(this, jj_input_stream);");
					} else {
						genCodeLine("	 token_source = new " + cu_name
								+ "TokenManager(jj_input_stream);");
					}
					genCodeLine("	 token = new Token();");
					if (Options.getCacheTokens()) {
						genCodeLine("	 token.next = jj_nt = token_source.getNextToken();");
					} else {
						genCodeLine("	 jj_ntk = -1;");
					}
					if (Options.getDepthLimit() > 0) {
						genCodeLine("    jj_depth = -1;");
					}
					if (Options.getErrorReporting()) {
						genCodeLine("	 jj_gen = 0;");
						if (maskindex > 0) {
							genCodeLine("	 for (int i = 0; i < " + maskindex
									+ "; i++) jj_la1[i] = -1;");
						}
						if (jj2index != 0) {
							genCodeLine("	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
						}
					}
					genCodeLine("  }");
					genCodeLine("");

					// Add-in a string based constructor because its convenient (modern only to prevent regressions)
					if (isJavaModernMode) {
						genCodeLine("  /** Constructor. */");
						genCodeLine("  public " + cu_name
								+ "(String dsl) throws ParseException, "+Options.getTokenMgrErrorClass() +" {");
						genCodeLine("	   this(new " + stringReaderClass + "(dsl));");
						genCodeLine("  }");
						genCodeLine("");

						genCodeLine("  public void ReInit(String s) {");
						genCodeLine("	  ReInit(new " + stringReaderClass + "(s));");
						genCodeLine("  }");

					}


					genCodeLine("  /** Reinitialise. */");
					genCodeLine("  " + staticOpt() + "public void ReInit(" + readerInterfaceName
							+ " stream) {");
					if (Options.getJavaUnicodeEscape()) {
						genCodeLine("	if (jj_input_stream == null) {");
						genCodeLine("	   jj_input_stream = new JavaCharStream(stream, 1, 1);");
						genCodeLine("	} else {");
						genCodeLine("	   jj_input_stream.ReInit(stream, 1, 1);");
						genCodeLine("	}");
					} else {
						genCodeLine("	if (jj_input_stream == null) {");
						genCodeLine("	   jj_input_stream = new SimpleCharStream(stream, 1, 1);");
						genCodeLine("	} else {");
						genCodeLine("	   jj_input_stream.ReInit(stream, 1, 1);");
						genCodeLine("	}");
					}

					genCodeLine("	if (token_source == null) {");

					if (Options.getTokenManagerUsesParser() && !Options.getStatic()) {
						genCodeLine(" token_source = new " + cu_name + "TokenManager(this, jj_input_stream);");
					} else {
						genCodeLine(" token_source = new " + cu_name + "TokenManager(jj_input_stream);");
					}

					genCodeLine("	}");
					genCodeLine("");

					if (Options.isTokenManagerRequiresParserAccess()) {
						genCodeLine("	 token_source.ReInit(this,jj_input_stream);");
					} else {
						genCodeLine("	 token_source.ReInit(jj_input_stream);");
					}

					genCodeLine("	 token = new Token();");
					if (Options.getCacheTokens()) {
						genCodeLine("	 token.next = jj_nt = token_source.getNextToken();");
					} else {
						genCodeLine("	 jj_ntk = -1;");
					}
					if (Options.getDepthLimit() > 0) {
						genCodeLine("    jj_depth = -1;");
					}
					if (jjtreeGenerated) {
						genCodeLine("	 jjtree.reset();");
					}
					if (Options.getErrorReporting()) {
						genCodeLine("	 jj_gen = 0;");
						if (maskindex > 0) {
							genCodeLine("	 for (int i = 0; i < " + maskindex
									+ "; i++) jj_la1[i] = -1;");
						}
						if (jj2index != 0) {
							genCodeLine("	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
						}
					}
					genCodeLine("  }");

				}
			}
			genCodeLine("");
			if (Options.getUserTokenManager()) {
				genCodeLine("  /** Constructor with user supplied Token Manager. */");
				genCodeLine("  public " + cu_name + "(TokenManager tm) {");
			} else {
				genCodeLine("  /** Constructor with generated Token Manager. */");
				genCodeLine("  public " + cu_name + "(" + cu_name + "TokenManager tm) {");
			}
			if (Options.getStatic()) {
				genCodeLine("	 if (jj_initialized_once) {");
				genCodeLine("	   System.out.println(\"ERROR: Second call to constructor of static parser. \");");
				genCodeLine("	   System.out.println(\"	   You must either use ReInit() or "
						+ "set the JavaCC option STATIC to false\");");
				genCodeLine("	   System.out.println(\"	   during parser generation.\");");
				genCodeLine("	   throw new "+(Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException")+"();");
				genCodeLine("	 }");
				genCodeLine("	 jj_initialized_once = true;");
			}
			genCodeLine("	 token_source = tm;");
			genCodeLine("	 token = new Token();");
			if (Options.getCacheTokens()) {
				genCodeLine("	 token.next = jj_nt = token_source.getNextToken();");
			} else {
				genCodeLine("	 jj_ntk = -1;");
			}
			if (Options.getDepthLimit() > 0) {
				genCodeLine("    jj_depth = -1;");
			}
			if (Options.getErrorReporting()) {
				genCodeLine("	 jj_gen = 0;");
				if (maskindex > 0) {
					genCodeLine("	 for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
				}
				if (jj2index != 0) {
					genCodeLine("	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
				}
			}
			genCodeLine("  }");
			genCodeLine("");
			if (Options.getUserTokenManager()) {
				genCodeLine("  /** Reinitialise. */");
				genCodeLine("  public void ReInit(TokenManager tm) {");
			} else {
				genCodeLine("  /** Reinitialise. */");
				genCodeLine("  public void ReInit(" + cu_name + "TokenManager tm) {");
			}
			genCodeLine("	 token_source = tm;");
			genCodeLine("	 token = new Token();");
			if (Options.getCacheTokens()) {
				genCodeLine("	 token.next = jj_nt = token_source.getNextToken();");
			} else {
				genCodeLine("	 jj_ntk = -1;");
			}
			if (Options.getDepthLimit() > 0) {
				genCodeLine("    jj_depth = -1;");
			}
			if (jjtreeGenerated) {
				genCodeLine("	 jjtree.reset();");
			}
			if (Options.getErrorReporting()) {
				genCodeLine("	 jj_gen = 0;");
				if (maskindex > 0) {
					genCodeLine("	 for (int i = 0; i < " + maskindex + "; i++) jj_la1[i] = -1;");
				}
				if (jj2index != 0) {
					genCodeLine("	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();");
				}
			}
			genCodeLine("  }");
			genCodeLine("");
			genCodeLine("  " + staticOpt()
					+ "private Token jj_consume_token(int kind) throws ParseException {");
			if (Options.getCacheTokens()) {
				genCodeLine("	 Token oldToken = token;");
				genCodeLine("	 if ((token = jj_nt).next != null) jj_nt = jj_nt.next;");
				genCodeLine("	 else jj_nt = jj_nt.next = token_source.getNextToken();");
			} else {
				genCodeLine("	 Token oldToken;");
				genCodeLine("	 if ((oldToken = token).next != null) token = token.next;");
				genCodeLine("	 else token = token.next = token_source.getNextToken();");
				genCodeLine("	 jj_ntk = -1;");
			}
			genCodeLine("	 if (token.kind == kind) {");
			if (Options.getErrorReporting()) {
				genCodeLine("	   jj_gen++;");
				if (jj2index != 0) {
					genCodeLine("	   if (++jj_gc > 100) {");
					genCodeLine("		 jj_gc = 0;");
					genCodeLine("		 for (int i = 0; i < jj_2_rtns.length; i++) {");
					genCodeLine("		   JJCalls c = jj_2_rtns[i];");
					genCodeLine("		   while (c != null) {");
					genCodeLine("			 if (c.gen < jj_gen) c.first = null;");
					genCodeLine("			 c = c.next;");
					genCodeLine("		   }");
					genCodeLine("		 }");
					genCodeLine("	   }");
				}
			}
			if (Options.getDebugParser()) {
				genCodeLine("	   trace_token(token, \"\");");
			}
			genCodeLine("	   return token;");
			genCodeLine("	 }");
			if (Options.getCacheTokens()) {
				genCodeLine("	 jj_nt = token;");
			}
			genCodeLine("	 token = oldToken;");
			if (Options.getErrorReporting()) {
				genCodeLine("	 jj_kind = kind;");
			}
			genCodeLine("	 throw generateParseException();");
			genCodeLine("  }");
			genCodeLine("");
			if (jj2index != 0) {
				genCodeLine("  @SuppressWarnings(\"serial\")");
				genCodeLine("  static private final class LookaheadSuccess extends "+(Options.isLegacyExceptionHandling() ? "java.lang.Error" : "java.lang.RuntimeException")+" {");
				genCodeLine("    @Override");
				genCodeLine("    public Throwable fillInStackTrace() {");
				genCodeLine("      return this;");
				genCodeLine("    }");
				genCodeLine("  }");
				genCodeLine("  static private final LookaheadSuccess jj_ls = new LookaheadSuccess();");
				genCodeLine("  " + staticOpt() + "private " + Options.getBooleanType()
						+ " jj_scan_token(int kind) {");
				genCodeLine("	 if (jj_scanpos == jj_lastpos) {");
				genCodeLine("	   jj_la--;");
				genCodeLine("	   if (jj_scanpos.next == null) {");
				genCodeLine("		 jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();");
				genCodeLine("	   } else {");
				genCodeLine("		 jj_lastpos = jj_scanpos = jj_scanpos.next;");
				genCodeLine("	   }");
				genCodeLine("	 } else {");
				genCodeLine("	   jj_scanpos = jj_scanpos.next;");
				genCodeLine("	 }");
				if (Options.getErrorReporting()) {
					genCodeLine("	 if (jj_rescan) {");
					genCodeLine("	   int i = 0; Token tok = token;");
					genCodeLine("	   while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }");
					genCodeLine("	   if (tok != null) jj_add_error_token(kind, i);");
					if (Options.getDebugLookahead()) {
						genCodeLine("	 } else {");
						genCodeLine("	   trace_scan(jj_scanpos, kind);");
					}
					genCodeLine("	 }");
				} else if (Options.getDebugLookahead()) {
					genCodeLine("	 trace_scan(jj_scanpos, kind);");
				}
				genCodeLine("	 if (jj_scanpos.kind != kind) return true;");
				genCodeLine("	 if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;");
				genCodeLine("	 return false;");
				genCodeLine("  }");
				genCodeLine("");
			}
			genCodeLine("");
			genCodeLine("/** Get the next Token. */");
			genCodeLine("  " + staticOpt() + "final public Token getNextToken() {");
			if (Options.getCacheTokens()) {
				genCodeLine("	 if ((token = jj_nt).next != null) jj_nt = jj_nt.next;");
				genCodeLine("	 else jj_nt = jj_nt.next = token_source.getNextToken();");
			} else {
				genCodeLine("	 if (token.next != null) token = token.next;");
				genCodeLine("	 else token = token.next = token_source.getNextToken();");
				genCodeLine("	 jj_ntk = -1;");
			}
			if (Options.getErrorReporting()) {
				genCodeLine("	 jj_gen++;");
			}
			if (Options.getDebugParser()) {
				genCodeLine("	   trace_token(token, \" (in getNextToken)\");");
			}
			genCodeLine("	 return token;");
			genCodeLine("  }");
			genCodeLine("");
			genCodeLine("/** Get the specific Token. */");
			genCodeLine("  " + staticOpt() + "final public Token getToken(int index) {");
			if (lookaheadNeeded) {
				genCodeLine("	 Token t = jj_lookingAhead ? jj_scanpos : token;");
			} else {
				genCodeLine("	 Token t = token;");
			}
			genCodeLine("	 for (int i = 0; i < index; i++) {");
			genCodeLine("	   if (t.next != null) t = t.next;");
			genCodeLine("	   else t = t.next = token_source.getNextToken();");
			genCodeLine("	 }");
			genCodeLine("	 return t;");
			genCodeLine("  }");
			genCodeLine("");
			if (!Options.getCacheTokens()) {
				genCodeLine("  " + staticOpt() + "private int jj_ntk_f() {");
				genCodeLine("	 if ((jj_nt=token.next) == null)");
				genCodeLine("	   return (jj_ntk = (token.next=token_source.getNextToken()).kind);");
				genCodeLine("	 else");
				genCodeLine("	   return (jj_ntk = jj_nt.kind);");
				genCodeLine("  }");
				genCodeLine("");
			}
			if (Options.getErrorReporting()) {
				if (!Options.getGenerateGenerics()) {
					genCodeLine("  " + staticOpt()
							+ "private java.util.List jj_expentries = new java.util.ArrayList();");
				} else {
					genCodeLine("  "
							+ staticOpt()
							+ "private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();");
				}
				genCodeLine("  " + staticOpt() + "private int[] jj_expentry;");
				genCodeLine("  " + staticOpt() + "private int jj_kind = -1;");
				if (jj2index != 0) {
					genCodeLine("  " + staticOpt() + "private int[] jj_lasttokens = new int[100];");
					genCodeLine("  " + staticOpt() + "private int jj_endpos;");
					genCodeLine("");
					genCodeLine("  " + staticOpt()
							+ "private void jj_add_error_token(int kind, int pos) {");
					genCodeLine("	 if (pos >= 100) {");
					genCodeLine("		return;");
					genCodeLine("	 }");
					genCodeLine("");
					genCodeLine("	 if (pos == jj_endpos + 1) {");
					genCodeLine("	   jj_lasttokens[jj_endpos++] = kind;");
					genCodeLine("	 } else if (jj_endpos != 0) {");
					genCodeLine("	   jj_expentry = new int[jj_endpos];");
					genCodeLine("");
					genCodeLine("	   for (int i = 0; i < jj_endpos; i++) {");
					genCodeLine("		 jj_expentry[i] = jj_lasttokens[i];");
					genCodeLine("	   }");
					genCodeLine("");
					if (!Options.getGenerateGenerics()) {
						genCodeLine("	   for (java.util.Iterator it = jj_expentries.iterator(); it.hasNext();) {");
						genCodeLine("		 int[] oldentry = (int[])(it.next());");
					} else {
						genCodeLine("	   for (int[] oldentry : jj_expentries) {");
					}

					genCodeLine("		 if (oldentry.length == jj_expentry.length) {");
					genCodeLine("		   boolean isMatched = true;");
					genCodeLine("");
					genCodeLine("		   for (int i = 0; i < jj_expentry.length; i++) {");
					genCodeLine("			 if (oldentry[i] != jj_expentry[i]) {");
					genCodeLine("			   isMatched = false;");
					genCodeLine("			   break;");
					genCodeLine("			 }");
					genCodeLine("");
					genCodeLine("		   }");
					genCodeLine("		   if (isMatched) {");
					genCodeLine("			 jj_expentries.add(jj_expentry);");
					genCodeLine("			 break;");
					genCodeLine("		   }");
					genCodeLine("		 }");
					genCodeLine("	   }");
					genCodeLine("");
					genCodeLine("	   if (pos != 0) {");
					genCodeLine("		 jj_lasttokens[(jj_endpos = pos) - 1] = kind;");
					genCodeLine("	   }");
					genCodeLine("	 }");
					genCodeLine("  }");
				}
				genCodeLine("");
				genCodeLine("  /** Generate ParseException. */");
				genCodeLine("  " + staticOpt() + "public ParseException generateParseException() {");
				genCodeLine("	 jj_expentries.clear();");
				genCodeLine("	 " + Options.getBooleanType() + "[] la1tokens = new "
						+ Options.getBooleanType() + "[" + tokenCount + "];");
				genCodeLine("	 if (jj_kind >= 0) {");
				genCodeLine("	   la1tokens[jj_kind] = true;");
				genCodeLine("	   jj_kind = -1;");
				genCodeLine("	 }");
				genCodeLine("	 for (int i = 0; i < " + maskindex + "; i++) {");
				genCodeLine("	   if (jj_la1[i] == jj_gen) {");
				genCodeLine("		 for (int j = 0; j < 32; j++) {");
				for (int i = 0; i < (tokenCount - 1) / 32 + 1; i++) {
					genCodeLine("		   if ((jj_la1_" + i + "[i] & (1<<j)) != 0) {");
					genCode("			 la1tokens[");
					if (i != 0) {
						genCode((32 * i) + "+");
					}
					genCodeLine("j] = true;");
					genCodeLine("		   }");
				}
				genCodeLine("		 }");
				genCodeLine("	   }");
				genCodeLine("	 }");
				genCodeLine("	 for (int i = 0; i < " + tokenCount + "; i++) {");
				genCodeLine("	   if (la1tokens[i]) {");
				genCodeLine("		 jj_expentry = new int[1];");
				genCodeLine("		 jj_expentry[0] = i;");
				genCodeLine("		 jj_expentries.add(jj_expentry);");
				genCodeLine("	   }");
				genCodeLine("	 }");
				if (jj2index != 0) {
					genCodeLine("	 jj_endpos = 0;");
					genCodeLine("	 jj_rescan_token();");
					genCodeLine("	 jj_add_error_token(0, 0);");
				}
				genCodeLine("	 int[][] exptokseq = new int[jj_expentries.size()][];");
				genCodeLine("	 for (int i = 0; i < jj_expentries.size(); i++) {");
				if (!Options.getGenerateGenerics()) {
					genCodeLine("	   exptokseq[i] = (int[])jj_expentries.get(i);");
				} else {
					genCodeLine("	   exptokseq[i] = jj_expentries.get(i);");
				}
				genCodeLine("	 }");


				if (isJavaModernMode) {
					// Add the lexical state onto the exception message
					genCodeLine("	 return new ParseException(token, exptokseq, tokenImage, token_source == null ? null : " +cu_name+ "TokenManager.lexStateNames[token_source.curLexState]);");
				} else {
					genCodeLine("	 return new ParseException(token, exptokseq, tokenImage);");
				}

				genCodeLine("  }");
			} else {
				genCodeLine("  /** Generate ParseException. */");
				genCodeLine("  " + staticOpt() + "public ParseException generateParseException() {");
				genCodeLine("	 Token errortok = token.next;");
				if (Options.getKeepLineColumn()) {
					genCodeLine("	 int line = errortok.beginLine, column = errortok.beginColumn;");
				}
				genCodeLine("	 String mess = (errortok.kind == 0) ? tokenImage[0] : errortok.image;");
				if (Options.getKeepLineColumn()) {
					genCodeLine("	 return new ParseException("
							+ "\"Parse error at line \" + line + \", column \" + column + \".  "
							+ "Encountered: \" + mess);");
				} else {
					genCodeLine("	 return new ParseException(\"Parse error at <unknown location>.  "
							+ "Encountered: \" + mess);");
				}
				genCodeLine("  }");
			}
			genCodeLine("");

			genCodeLine("  " + staticOpt() + "private " + Options.getBooleanType()
					+ " trace_enabled;");
			genCodeLine("");
			genCodeLine("/** Trace enabled. */");
			genCodeLine("  " + staticOpt() + "final public boolean trace_enabled() {");
			genCodeLine("	 return trace_enabled;");
			genCodeLine("  }");
			genCodeLine("");

			if (Options.getDebugParser()) {
				genCodeLine("  " + staticOpt() + "private int trace_indent = 0;");

				genCodeLine("/** Enable tracing. */");
				genCodeLine("  " + staticOpt() + "final public void enable_tracing() {");
				genCodeLine("	 trace_enabled = true;");
				genCodeLine("  }");
				genCodeLine("");
				genCodeLine("/** Disable tracing. */");
				genCodeLine("  " + staticOpt() + "final public void disable_tracing() {");
				genCodeLine("	 trace_enabled = false;");
				genCodeLine("  }");
				genCodeLine("");
				genCodeLine("  " + staticOpt() + "protected void trace_call(String s) {");
				genCodeLine("	 if (trace_enabled) {");
				genCodeLine("	   for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
				genCodeLine("	   System.out.println(\"Call:	\" + s);");
				genCodeLine("	 }");
				genCodeLine("	 trace_indent = trace_indent + 2;");
				genCodeLine("  }");
				genCodeLine("");
				genCodeLine("  " + staticOpt() + "protected void trace_return(String s) {");
				genCodeLine("	 trace_indent = trace_indent - 2;");
				genCodeLine("	 if (trace_enabled) {");
				genCodeLine("	   for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
				genCodeLine("	   System.out.println(\"Return: \" + s);");
				genCodeLine("	 }");
				genCodeLine("  }");
				genCodeLine("");
				genCodeLine("  " + staticOpt()
						+ "protected void trace_token(Token t, String where) {");
				genCodeLine("	 if (trace_enabled) {");
				genCodeLine("	   for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
				genCodeLine("	   System.out.print(\"Consumed token: <\" + tokenImage[t.kind]);");
				genCodeLine("	   if (t.kind != 0 && !tokenImage[t.kind].equals(\"\\\"\" + t.image + \"\\\"\")) {");
				genCodeLine("		 System.out.print(\": \\\"\" + "+Options.getTokenMgrErrorClass() + ".addEscapes("+"t.image) + \"\\\"\");");
				genCodeLine("	   }");
				genCodeLine("	   System.out.println(\" at line \" + t.beginLine + "
						+ "\" column \" + t.beginColumn + \">\" + where);");
				genCodeLine("	 }");
				genCodeLine("  }");
				genCodeLine("");
				genCodeLine("  " + staticOpt() + "protected void trace_scan(Token t1, int t2) {");
				genCodeLine("	 if (trace_enabled) {");
				genCodeLine("	   for (int i = 0; i < trace_indent; i++) { System.out.print(\" \"); }");
				genCodeLine("	   System.out.print(\"Visited token: <\" + tokenImage[t1.kind]);");
				genCodeLine("	   if (t1.kind != 0 && !tokenImage[t1.kind].equals(\"\\\"\" + t1.image + \"\\\"\")) {");
				genCodeLine("		 System.out.print(\": \\\"\" + "+Options.getTokenMgrErrorClass() + ".addEscapes("+"t1.image) + \"\\\"\");");
				genCodeLine("	   }");
				genCodeLine("	   System.out.println(\" at line \" + t1.beginLine + \""
						+ " column \" + t1.beginColumn + \">; Expected token: <\" + tokenImage[t2] + \">\");");
				genCodeLine("	 }");
				genCodeLine("  }");
				genCodeLine("");
			} else {
				genCodeLine("  /** Enable tracing. */");
				genCodeLine("  " + staticOpt() + "final public void enable_tracing() {");
				genCodeLine("  }");
				genCodeLine("");
				genCodeLine("  /** Disable tracing. */");
				genCodeLine("  " + staticOpt() + "final public void disable_tracing() {");
				genCodeLine("  }");
				genCodeLine("");
			}

			if (jj2index != 0 && Options.getErrorReporting()) {
				genCodeLine("  " + staticOpt() + "private void jj_rescan_token() {");
				genCodeLine("	 jj_rescan = true;");
				genCodeLine("	 for (int i = 0; i < " + jj2index + "; i++) {");
				genCodeLine("	   try {");
				genCodeLine("		 JJCalls p = jj_2_rtns[i];");
				genCodeLine("");
				genCodeLine("		 do {");
				genCodeLine("		   if (p.gen > jj_gen) {");
				genCodeLine("			 jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;");
				genCodeLine("			 switch (i) {");
				for (int i = 0; i < jj2index; i++) {
					genCodeLine("			   case " + i + ": jj_3_" + (i + 1) + "(); break;");
				}
				genCodeLine("			 }");
				genCodeLine("		   }");
				genCodeLine("		   p = p.next;");
				genCodeLine("		 } while (p != null);");
				genCodeLine("");
				genCodeLine("		 } catch(LookaheadSuccess ls) { }");
				genCodeLine("	 }");
				genCodeLine("	 jj_rescan = false;");
				genCodeLine("  }");
				genCodeLine("");
				genCodeLine("  " + staticOpt() + "private void jj_save(int index, int xla) {");
				genCodeLine("	 JJCalls p = jj_2_rtns[index];");
				genCodeLine("	 while (p.gen > jj_gen) {");
				genCodeLine("	   if (p.next == null) { p = p.next = new JJCalls(); break; }");
				genCodeLine("	   p = p.next;");
				genCodeLine("	 }");
				genCodeLine("");
				genCodeLine("	 p.gen = jj_gen + xla - jj_la; ");
				genCodeLine("	 p.first = token;");
				genCodeLine("	 p.arg = xla;");
				genCodeLine("  }");
				genCodeLine("");
			}

			if (jj2index != 0 && Options.getErrorReporting()) {
				genCodeLine("  static final class JJCalls {");
				genCodeLine("	 int gen;");
				genCodeLine("	 Token first;");
				genCodeLine("	 int arg;");
				genCodeLine("	 JJCalls next;");
				genCodeLine("  }");
				genCodeLine("");
			}

			if (cu_from_insertion_point_2.size() != 0) {
				printTokenSetup((cu_from_insertion_point_2.get(0)));
				this.ccol = 1;
				for (final Iterator<?> it = cu_from_insertion_point_2.iterator(); it.hasNext();) {
					t = (Token) it.next();
					printToken(t);
				}
				printTrailingComments(t);
			}
			genCodeLine("");

			saveOutput(Options.getOutputDirectory() + File.separator + cu_name
					+ getFileExtension(Options.getOutputLanguage()));

		} // matches "if (Options.getBuildParser())"

	}

	public static void reInit() {
		lookaheadNeeded = false;
	}

}
