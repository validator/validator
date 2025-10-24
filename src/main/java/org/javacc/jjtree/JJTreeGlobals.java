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
package org.javacc.jjtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

class JJTreeGlobals
{
  static void initialize() {
    toolList = new ArrayList();
    parserName = null;
    packageName = "";
    parserImplements = null;
    parserClassBodyStart = null;
    parserImports = null;
    productions = new HashMap();

    jjtreeOptions = new HashSet();
    jjtreeOptions.add("JJTREE_OUTPUT_DIRECTORY");
    jjtreeOptions.add("MULTI");
    jjtreeOptions.add("NODE_PREFIX");
    jjtreeOptions.add("NODE_PACKAGE");
    jjtreeOptions.add("NODE_EXTENDS");
    jjtreeOptions.add("NODE_CLASS");
    jjtreeOptions.add("NODE_STACK_SIZE");
    jjtreeOptions.add("NODE_DEFAULT_VOID");
    jjtreeOptions.add("OUTPUT_FILE");
    jjtreeOptions.add("CHECK_DEFINITE_NODE");
    jjtreeOptions.add("NODE_SCOPE_HOOK");
    jjtreeOptions.add("TRACK_TOKENS");
    jjtreeOptions.add("NODE_FACTORY");
    jjtreeOptions.add("NODE_USES_PARSER");
    jjtreeOptions.add("BUILD_NODE_FILES");
    jjtreeOptions.add("VISITOR");
    jjtreeOptions.add("VISITOR_EXCEPTION");
    jjtreeOptions.add("VISITOR_DATA_TYPE");
    jjtreeOptions.add("VISITOR_RETURN_TYPE");
    jjtreeOptions.add("VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME");
    jjtreeOptions.add("NODE_INCLUDES");
  }

  static {
    initialize();
  }

  /**
   * This set stores the JJTree-specific options that should not be
   * passed down to JavaCC
   */
  private static Set jjtreeOptions;

  public static boolean isOptionJJTreeOnly(String optionName)
  {
    return jjtreeOptions.contains(optionName.toUpperCase());
  }

  static List toolList = new ArrayList();

  /**
   * Use this like className.
   **/
  public static String parserName;

  /**
   * The package that the parser lives in.  If the grammar doesn't
   * specify a package it is the empty string.
   **/
  public static String packageName = "";

  /**
   * The package the node files live in. If the NODE_PACKAGE option
   * is not set, then this defaults to packageName.
   **/
  public static String nodePackageName = "";

  /** The <code>implements</code> token of the parser class.  If the
   * parser doesn't have one then it is the first "{" of the parser
   * class body.
   **/
  public static Token parserImplements;

  /** The first token of the parser class body (the <code>{</code>).
   * The JJTree state is inserted after this token.
   **/
  public static Token parserClassBodyStart;

  /** The first token of the <code>import</code> list, or the
   * position where such a list should be inserted. The import
   * for the Node Package is inserted after this token.
   **/
  public static Token parserImports;

  /**
   * This is mapping from production names to ASTProduction objects.
   **/
  static Map productions = new HashMap();

}

/*end*/
