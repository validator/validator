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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static org.javacc.parser.JavaCCGlobals.*;

public class ParseEngine {

  private int gensymindex = 0;
  private int indentamt;
  private boolean jj2LA;
  private CodeGenerator codeGenerator;
  private boolean isJavaDialect = Options.isOutputLanguageJava();



  /**
   * These lists are used to maintain expansions for which code generation
   * in phase 2 and phase 3 is required.  Whenever a call is generated to
   * a phase 2 or phase 3 routine, a corresponding entry is added here if
   * it has not already been added.
   * The phase 3 routines have been optimized in version 0.7pre2.  Essentially
   * only those methods (and only those portions of these methods) are
   * generated that are required.  The lookahead amount is used to determine
   * this.  This change requires the use of a hash table because it is now
   * possible for the same phase 3 routine to be requested multiple times
   * with different lookaheads.  The hash table provides a easily searchable
   * capability to determine the previous requests.
   * The phase 3 routines now are performed in a two step process - the first
   * step gathers the requests (replacing requests with lower lookaheads with
   * those requiring larger lookaheads).  The second step then generates these
   * methods.
   * This optimization and the hashtable makes it look like we do not need
   * the flag "phase3done" any more.  But this has not been removed yet.
   */
  private List phase2list = new ArrayList();
  private List phase3list = new ArrayList();
  private java.util.Hashtable phase3table = new java.util.Hashtable();

  /**
   * The phase 1 routines generates their output into String's and dumps
   * these String's once for each method.  These String's contain the
   * special characters '\u0001' to indicate a positive indent, and '\u0002'
   * to indicate a negative indent.  '\n' is used to indicate a line terminator.
   * The characters '\u0003' and '\u0004' are used to delineate portions of
   * text where '\n's should not be followed by an indentation.
   */

  /**
   * Returns true if there is a JAVACODE production that the argument expansion
   * may directly expand to (without consuming tokens or encountering lookahead).
   */
  private boolean javaCodeCheck(Expansion exp) {
    if (exp instanceof RegularExpression) {
      return false;
    } else if (exp instanceof NonTerminal) {
      NormalProduction prod = ((NonTerminal)exp).getProd();
      if (prod instanceof CodeProduction) {
        return true;
      } else {
        return javaCodeCheck(prod.getExpansion());
      }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice)exp;
      for (int i = 0; i < ch.getChoices().size(); i++) {
        if (javaCodeCheck((Expansion)(ch.getChoices().get(i)))) {
          return true;
        }
      }
      return false;
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence)exp;
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion[] units = (Expansion[])seq.units.toArray(new Expansion[seq.units.size()]);
        if (units[i] instanceof Lookahead && ((Lookahead)units[i]).isExplicit()) {
          // An explicit lookahead (rather than one generated implicitly). Assume
          // the user knows what he / she is doing, e.g.
          //    "A" ( "B" | LOOKAHEAD("X") jcode() | "C" )* "D"
          return false;
        } else if (javaCodeCheck((units[i]))) {
          return true;
        } else if (!Semanticize.emptyExpansionExists(units[i])) {
          return false;
        }
      }
      return false;
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore)exp;
      return javaCodeCheck(om.expansion);
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore)exp;
      return javaCodeCheck(zm.expansion);
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne)exp;
      return javaCodeCheck(zo.expansion);
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock)exp;
      return javaCodeCheck(tb.exp);
    } else {
      return false;
    }
  }

  /**
   * An array used to store the first sets generated by the following method.
   * A true entry means that the corresponding token is in the first set.
   */
  private boolean[] firstSet;

  /**
   * Sets up the array "firstSet" above based on the Expansion argument
   * passed to it.  Since this is a recursive function, it assumes that
   * "firstSet" has been reset before the first call.
   */
  private void genFirstSet(Expansion exp) {
    if (exp instanceof RegularExpression) {
      firstSet[((RegularExpression)exp).ordinal] = true;
    } else if (exp instanceof NonTerminal) {
      if (!(((NonTerminal)exp).getProd() instanceof CodeProduction))
      {
        genFirstSet(((BNFProduction)(((NonTerminal)exp).getProd())).getExpansion());
      }
    } else if (exp instanceof Choice) {
      Choice ch = (Choice)exp;
      for (int i = 0; i < ch.getChoices().size(); i++) {
        genFirstSet((Expansion)(ch.getChoices().get(i)));
      }
    } else if (exp instanceof Sequence) {
      Sequence seq = (Sequence)exp;
      Object obj = seq.units.get(0);
      if ((obj instanceof Lookahead) && (((Lookahead)obj).getActionTokens().size() != 0)) {
        jj2LA = true;
      }
      for (int i = 0; i < seq.units.size(); i++) {
        Expansion unit = (Expansion) seq.units.get(i);
        // Javacode productions can not have FIRST sets. Instead we generate the FIRST set
        // for the preceding LOOKAHEAD (the semantic checks should have made sure that
        // the LOOKAHEAD is suitable).
        if (unit instanceof NonTerminal && ((NonTerminal)unit).getProd() instanceof CodeProduction) {
          if (i > 0 && seq.units.get(i-1) instanceof Lookahead) {
            Lookahead la = (Lookahead)seq.units.get(i-1);
            genFirstSet(la.getLaExpansion());
          }
        } else {
          genFirstSet((Expansion)(seq.units.get(i)));
        }
        if (!Semanticize.emptyExpansionExists((Expansion)(seq.units.get(i)))) {
          break;
        }
      }
    } else if (exp instanceof OneOrMore) {
      OneOrMore om = (OneOrMore)exp;
      genFirstSet(om.expansion);
    } else if (exp instanceof ZeroOrMore) {
      ZeroOrMore zm = (ZeroOrMore)exp;
      genFirstSet(zm.expansion);
    } else if (exp instanceof ZeroOrOne) {
      ZeroOrOne zo = (ZeroOrOne)exp;
      genFirstSet(zo.expansion);
    } else if (exp instanceof TryBlock) {
      TryBlock tb = (TryBlock)exp;
      genFirstSet(tb.exp);
    }
  }

  /**
   * Constants used in the following method "buildLookaheadChecker".
   */
  final int NOOPENSTM = 0;
  final int OPENIF = 1;
  final int OPENSWITCH = 2;

  private void dumpLookaheads(Lookahead[] conds, String[] actions) {
    for (int i = 0; i < conds.length; i++) {
      System.err.println("Lookahead: " + i);
      System.err.println(conds[i].dump(0, new HashSet()));
      System.err.println();
    }
  }

  /**
   * This method takes two parameters - an array of Lookahead's
   * "conds", and an array of String's "actions".  "actions" contains
   * exactly one element more than "conds".  "actions" are Java source
   * code, and "conds" translate to conditions - so lets say
   * "f(conds[i])" is true if the lookahead required by "conds[i]" is
   * indeed the case.  This method returns a string corresponding to
   * the Java code for:
   *
   *   if (f(conds[0]) actions[0]
   *   else if (f(conds[1]) actions[1]
   *   . . .
   *   else actions[action.length-1]
   *
   * A particular action entry ("actions[i]") can be null, in which
   * case, a noop is generated for that action.
   */
  String buildLookaheadChecker(Lookahead[] conds, String[] actions) {

    // The state variables.
    int state = NOOPENSTM;
    int indentAmt = 0;
    boolean[] casedValues = new boolean[tokenCount];
    String retval = "";
    Lookahead la;
    Token t = null;
    int tokenMaskSize = (tokenCount-1)/32 + 1;
    int[] tokenMask = null;

    // Iterate over all the conditions.
    int index = 0;
    while (index < conds.length) {

      la = conds[index];
      jj2LA = false;

      if ((la.getAmount() == 0) ||
          Semanticize.emptyExpansionExists(la.getLaExpansion()) ||
          javaCodeCheck(la.getLaExpansion())
      ) {

        // This handles the following cases:
        // . If syntactic lookahead is not wanted (and hence explicitly specified
        //   as 0).
        // . If it is possible for the lookahead expansion to recognize the empty
        //   string - in which case the lookahead trivially passes.
        // . If the lookahead expansion has a JAVACODE production that it directly
        //   expands to - in which case the lookahead trivially passes.
        if (la.getActionTokens().size() == 0) {
          // In addition, if there is no semantic lookahead, then the
          // lookahead trivially succeeds.  So break the main loop and
          // treat this case as the default last action.
          break;
        } else {
          // This case is when there is only semantic lookahead
          // (without any preceding syntactic lookahead).  In this
          // case, an "if" statement is generated.
          switch (state) {
          case NOOPENSTM:
            retval += "\n" + "if (";
            indentAmt++;
            break;
          case OPENIF:
            retval += "\u0002\n" + "} else if (";
            break;
          case OPENSWITCH:
            retval += "\u0002\n" + "default:" + "\u0001";
            if (Options.getErrorReporting()) {
              retval += "\njj_la1[" + maskindex + "] = jj_gen;";
              maskindex++;
            }
            maskVals.add(tokenMask);
            retval += "\n" + "if (";
            indentAmt++;
          }
          codeGenerator.printTokenSetup((Token)(la.getActionTokens().get(0)));
          for (Iterator it = la.getActionTokens().iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
          retval += ") {\u0001" + actions[index];
          state = OPENIF;
        }

      } else if (la.getAmount() == 1 && la.getActionTokens().size() == 0) {
        // Special optimal processing when the lookahead is exactly 1, and there
        // is no semantic lookahead.

        if (firstSet == null) {
          firstSet = new boolean[tokenCount];
        }
        for (int i = 0; i < tokenCount; i++) {
          firstSet[i] = false;
        }
        // jj2LA is set to false at the beginning of the containing "if" statement.
        // It is checked immediately after the end of the same statement to determine
        // if lookaheads are to be performed using calls to the jj2 methods.
        genFirstSet(la.getLaExpansion());
        // genFirstSet may find that semantic attributes are appropriate for the next
        // token.  In which case, it sets jj2LA to true.
        if (!jj2LA) {

          // This case is if there is no applicable semantic lookahead and the lookahead
          // is one (excluding the earlier cases such as JAVACODE, etc.).
          switch (state) {
          case OPENIF:
            retval += "\u0002\n" + "} else {\u0001";
            //$FALL-THROUGH$ Control flows through to next case.
          case NOOPENSTM:
            retval += "\n" + "switch (";
            if (Options.getCacheTokens()) {
              if(Options.isOutputLanguageCpp()) {
                retval += "jj_nt->kind";
              } else {
                retval += "jj_nt.kind";
              }
              retval += ") {\u0001";
            } else {
              retval += "(jj_ntk==-1)?jj_ntk_f():jj_ntk) {\u0001";
            }
            for (int i = 0; i < tokenCount; i++) {
              casedValues[i] = false;
            }
            indentAmt++;
            tokenMask = new int[tokenMaskSize];
            for (int i = 0; i < tokenMaskSize; i++) {
              tokenMask[i] = 0;
            }
            // Don't need to do anything if state is OPENSWITCH.
          }
          for (int i = 0; i < tokenCount; i++) {
            if (firstSet[i]) {
              if (!casedValues[i]) {
                casedValues[i] = true;
                retval += "\u0002\ncase ";
                int j1 = i/32;
                int j2 = i%32;
                tokenMask[j1] |= 1 << j2;
                String s = (String)(names_of_tokens.get(Integer.valueOf(i)));
                if (s == null) {
                  retval += i;
                } else {
                  retval += s;
                }
                retval += ":\u0001";
              }
            }
          }
          retval += "{";
          retval += actions[index];
          retval += "\nbreak;\n}";
          state = OPENSWITCH;

        }

      } else {
        // This is the case when lookahead is determined through calls to
        // jj2 methods.  The other case is when lookahead is 1, but semantic
        // attributes need to be evaluated.  Hence this crazy control structure.

        jj2LA = true;

      }

      if (jj2LA) {
        // In this case lookahead is determined by the jj2 methods.

        switch (state) {
        case NOOPENSTM:
          retval += "\n" + "if (";
          indentAmt++;
          break;
        case OPENIF:
          retval += "\u0002\n" + "} else if (";
          break;
        case OPENSWITCH:
          retval += "\u0002\n" + "default:" + "\u0001";
          if (Options.getErrorReporting()) {
            retval += "\njj_la1[" + maskindex + "] = jj_gen;";
            maskindex++;
          }
          maskVals.add(tokenMask);
          retval += "\n" + "if (";
          indentAmt++;
        }
        jj2index++;
        // At this point, la.la_expansion.internal_name must be "".
        la.getLaExpansion().internal_name = "_" + jj2index;
        la.getLaExpansion().internal_index = jj2index;
        phase2list.add(la);
        retval += "jj_2" + la.getLaExpansion().internal_name + "(" + la.getAmount() + ")";
        if (la.getActionTokens().size() != 0) {
          // In addition, there is also a semantic lookahead.  So concatenate
          // the semantic check with the syntactic one.
          retval += " && (";
          codeGenerator.printTokenSetup((Token)(la.getActionTokens().get(0)));
          for (Iterator it = la.getActionTokens().iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
          retval += ")";
        }
        retval += ") {\u0001" + actions[index];
        state = OPENIF;
      }

      index++;
    }

    // Generate code for the default case.  Note this may not
    // be the last entry of "actions" if any condition can be
    // statically determined to be always "true".

    switch (state) {
    case NOOPENSTM:
      retval += actions[index];
      break;
    case OPENIF:
      retval += "\u0002\n" + "} else {\u0001" + actions[index];
      break;
    case OPENSWITCH:
      retval += "\u0002\n" + "default:" + "\u0001";
      if (Options.getErrorReporting()) {
        retval += "\njj_la1[" + maskindex + "] = jj_gen;";
        maskVals.add(tokenMask);
        maskindex++;
      }
      retval += actions[index];
    }
    for (int i = 0; i < indentAmt; i++) {
      retval += "\u0002\n}";
    }

    return retval;

  }

  void dumpFormattedString(String str) {
    char ch = ' ';
    char prevChar;
    boolean indentOn = true;
    for (int i = 0; i < str.length(); i++) {
      prevChar = ch;
      ch = str.charAt(i);
      if (ch == '\n' && prevChar == '\r') {
        // do nothing - we've already printed a new line for the '\r'
        // during the previous iteration.
      } else if (ch == '\n' || ch == '\r') {
        if (indentOn) {
          phase1NewLine();
        } else {
          codeGenerator.genCodeLine("");
        }
      } else if (ch == '\u0001') {
        indentamt += 2;
      } else if (ch == '\u0002') {
        indentamt -= 2;
      } else if (ch == '\u0003') {
        indentOn = false;
      } else if (ch == '\u0004') {
        indentOn = true;
      } else {
        codeGenerator.genCode(ch);
      }
    }
  }

  // Print CPPCODE method header.
  private String generateCPPMethodheader(CppCodeProduction p) {
    StringBuffer sig = new StringBuffer();
    String ret, params;
    Token t = null;

    String method_name = p.getLhs();
    boolean void_ret = false;
    boolean ptr_ret = false;

//    codeGenerator.printTokenSetup(t); ccol = 1;
//    String comment1 = codeGenerator.getLeadingComments(t);
//    cline = t.beginLine;
//    ccol = t.beginColumn;
//    sig.append(t.image);
//    if (t.kind == JavaCCParserConstants.VOID) void_ret = true;
//    if (t.kind == JavaCCParserConstants.STAR) ptr_ret = true;

    for (int i = 0; i < p.getReturnTypeTokens().size(); i++) {
      t = (Token)(p.getReturnTypeTokens().get(i));
      String s = codeGenerator.getStringToPrint(t);
      sig.append(t.toString());
      sig.append(" ");
      if (t.kind == JavaCCParserConstants.VOID) void_ret = true;
      if (t.kind == JavaCCParserConstants.STAR) ptr_ret = true;
    }

    String comment2 = "";
    if (t != null)
    	comment2 = codeGenerator.getTrailingComments(t);
    ret = sig.toString();

    sig.setLength(0);
    sig.append("(");
    if (p.getParameterListTokens().size() != 0) {
      codeGenerator.printTokenSetup((Token)(p.getParameterListTokens().get(0)));
      for (java.util.Iterator it = p.getParameterListTokens().iterator(); it.hasNext();) {
        t = (Token)it.next();
        sig.append(codeGenerator.getStringToPrint(t));
      }
      sig.append(codeGenerator.getTrailingComments(t));
    }
    sig.append(")");
    params = sig.toString();

    // For now, just ignore comments
    codeGenerator.generateMethodDefHeader(ret, cu_name, p.getLhs()+params, sig.toString());

    return "";
  }

  // Print method header and return the ERROR_RETURN string.
  private String generateCPPMethodheader(BNFProduction p, Token t) {
    StringBuffer sig = new StringBuffer();
    String ret, params;

    String method_name = p.getLhs();
    boolean void_ret = false;
    boolean ptr_ret = false;

    codeGenerator.printTokenSetup(t); ccol = 1;
    String comment1 = codeGenerator.getLeadingComments(t);
    cline = t.beginLine;
    ccol = t.beginColumn;
    sig.append(t.image);
    if (t.kind == JavaCCParserConstants.VOID) void_ret = true;
    if (t.kind == JavaCCParserConstants.STAR) ptr_ret = true;

    for (int i = 1; i < p.getReturnTypeTokens().size(); i++) {
      t = (Token)(p.getReturnTypeTokens().get(i));
      sig.append(codeGenerator.getStringToPrint(t));
      if (t.kind == JavaCCParserConstants.VOID) void_ret = true;
      if (t.kind == JavaCCParserConstants.STAR) ptr_ret = true;
    }

    String comment2 = codeGenerator.getTrailingComments(t);
    ret = sig.toString();

    sig.setLength(0);
    sig.append("(");
    if (p.getParameterListTokens().size() != 0) {
      codeGenerator.printTokenSetup((Token)(p.getParameterListTokens().get(0)));
      for (java.util.Iterator it = p.getParameterListTokens().iterator(); it.hasNext();) {
        t = (Token)it.next();
        sig.append(codeGenerator.getStringToPrint(t));
      }
      sig.append(codeGenerator.getTrailingComments(t));
    }
    sig.append(")");
    params = sig.toString();

    // For now, just ignore comments
    codeGenerator.generateMethodDefHeader(ret, cu_name, p.getLhs()+params, sig.toString());

    // Generate a default value for error return.
    String default_return;
    if (ptr_ret) default_return = "NULL";
    else if (void_ret) default_return = "";
    else default_return = "0";  // 0 converts to most (all?) basic types.

    StringBuffer ret_val =
        new StringBuffer("\n#if !defined ERROR_RET_" + method_name + "\n");
    ret_val.append("#define ERROR_RET_" + method_name + " " +
                   default_return + "\n");
    ret_val.append("#endif\n");
    ret_val.append("#define __ERROR_RET__ ERROR_RET_" + method_name + "\n");

    return ret_val.toString();
  }


  void genStackCheck(boolean voidReturn) {
    if (Options.getDepthLimit() > 0) {
      if (isJavaDialect) {
        codeGenerator.genCodeLine("if(++jj_depth > " + Options.getDepthLimit() + ") {");
        codeGenerator.genCodeLine("  jj_consume_token(-1);");
        codeGenerator.genCodeLine("  throw new ParseException();");
        codeGenerator.genCodeLine("}");
        codeGenerator.genCodeLine("try {");
      } else {
        if (!voidReturn) {
          codeGenerator.genCodeLine("if(jj_depth_error){ return __ERROR_RET__; }");
        } else {
          codeGenerator.genCodeLine("if(jj_depth_error){ return; }");
        }
        codeGenerator.genCodeLine("__jj_depth_inc __jj_depth_counter(this);");
        codeGenerator.genCodeLine("if(jj_depth > " + Options.getDepthLimit() + ") {");
        codeGenerator.genCodeLine("  jj_depth_error = true;");
        codeGenerator.genCodeLine("  jj_consume_token(-1);");
        codeGenerator.genCodeLine("  errorHandler->handleParseError(token, getToken(1), __FUNCTION__, this), hasError = true;");
        if (!voidReturn) {
          codeGenerator.genCodeLine("  return __ERROR_RET__;");  // Non-recoverable error
        } else {
          codeGenerator.genCodeLine("  return;");  // Non-recoverable error
        }
        codeGenerator.genCodeLine("}");
      }
    }
  }

  void genStackCheckEnd() {
    if (Options.getDepthLimit() > 0) {
      if (isJavaDialect) {
        codeGenerator.genCodeLine(" } finally {");
        codeGenerator.genCodeLine("   --jj_depth;");
        codeGenerator.genCodeLine(" }");
      }
    }
  }

  void buildPhase1Routine(BNFProduction p) {
    Token t;
    t = (Token)(p.getReturnTypeTokens().get(0));
    boolean voidReturn = false;
    if (t.kind == JavaCCParserConstants.VOID) {
      voidReturn = true;
    }
    String error_ret = null;
    if (isJavaDialect) {
      codeGenerator.printTokenSetup(t); ccol = 1;
      codeGenerator.printLeadingComments(t);
      codeGenerator.genCode("  " + staticOpt() + "final " +(p.getAccessMod() != null ? p.getAccessMod() : "public")+ " ");
      cline = t.beginLine; ccol = t.beginColumn;
      codeGenerator.printTokenOnly(t);
      for (int i = 1; i < p.getReturnTypeTokens().size(); i++) {
        t = (Token)(p.getReturnTypeTokens().get(i));
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
      codeGenerator.genCode(" " + p.getLhs() + "(");
      if (p.getParameterListTokens().size() != 0) {
        codeGenerator.printTokenSetup((Token)(p.getParameterListTokens().get(0)));
        for (java.util.Iterator it = p.getParameterListTokens().iterator(); it.hasNext();) {
          t = (Token)it.next();
          codeGenerator.printToken(t);
        }
        codeGenerator.printTrailingComments(t);
      }
      codeGenerator.genCode(")");
      codeGenerator.genCode(" throws ParseException");

      for (java.util.Iterator it = p.getThrowsList().iterator(); it.hasNext();) {
        codeGenerator.genCode(", ");
        java.util.List name = (java.util.List)it.next();
        for (java.util.Iterator it2 = name.iterator(); it2.hasNext();) {
          t = (Token)it2.next();
          codeGenerator.genCode(t.image);
        }
      }
    } else {
      error_ret = generateCPPMethodheader(p, t);
    }

    codeGenerator.genCode(" {");

    if ((Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR) && error_ret != null)
        || (Options.getDepthLimit() > 0 && !voidReturn && !isJavaDialect)) {
	      codeGenerator.genCode(error_ret);
    } else {
      error_ret = null;
    }

    genStackCheck(voidReturn);

    indentamt = 4;
    if (Options.getDebugParser()) {
        codeGenerator.genCodeLine("");
        if (isJavaDialect) {
            codeGenerator.genCodeLine("    trace_call(\"" + JavaCCGlobals.addUnicodeEscapes(p.getLhs()) + "\");");
        } else {
      	  codeGenerator.genCodeLine("    JJEnter<std::function<void()>> jjenter([this]() {trace_call  (\"" + JavaCCGlobals.addUnicodeEscapes(p.getLhs()) +"\"); });");
      	  codeGenerator.genCodeLine("    JJExit <std::function<void()>> jjexit ([this]() {trace_return(\"" + JavaCCGlobals.addUnicodeEscapes(p.getLhs()) +"\"); });");
        }
        codeGenerator.genCodeLine("    try {");
        indentamt = 6;
      }

    if (!Options.booleanValue(Options.USEROPTION__CPP_IGNORE_ACTIONS) &&
        p.getDeclarationTokens().size() != 0) {
      codeGenerator.printTokenSetup((Token)(p.getDeclarationTokens().get(0))); cline--;
      for (Iterator it = p.getDeclarationTokens().iterator(); it.hasNext();) {
        t = (Token)it.next();
        codeGenerator.printToken(t);
      }
      codeGenerator.printTrailingComments(t);
    }

    String code = phase1ExpansionGen(p.getExpansion());
    dumpFormattedString(code);
    codeGenerator.genCodeLine("");

    if (p.isJumpPatched() && !voidReturn) {
      if (isJavaDialect) {
	// This line is required for Java!
	codeGenerator.genCodeLine("    throw new "+(Options.isLegacyExceptionHandling() ? "Error" : "RuntimeException")+"(\"Missing return statement in function\");");
      } else {
        codeGenerator.genCodeLine("    throw \"Missing return statement in function\";");
      }
    }
    if (Options.getDebugParser()) {
      if (isJavaDialect) {
        codeGenerator.genCodeLine("    } finally {");
        codeGenerator.genCodeLine("      trace_return(\"" + JavaCCGlobals.addUnicodeEscapes(p.getLhs()) + "\");");
      } else {
        codeGenerator.genCodeLine("    } catch(...) { }");
      }
      if (isJavaDialect) {
        codeGenerator.genCodeLine("    }");
      }
    }
    if (!isJavaDialect && !voidReturn) {
      codeGenerator.genCodeLine("assert(false);");
    }


    if (error_ret != null) {
      codeGenerator.genCodeLine("\n#undef __ERROR_RET__\n");
    }
    genStackCheckEnd();
    codeGenerator.genCodeLine("}");
    codeGenerator.genCodeLine("");
  }

  void phase1NewLine() {
    codeGenerator.genCodeLine("");
    for (int i = 0; i < indentamt; i++) {
      codeGenerator.genCode(" ");
    }
  }

  String phase1ExpansionGen(Expansion e) {
    String retval = "";
    Token t = null;
    Lookahead[] conds;
    String[] actions;
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      retval += "\n";
      if (e_nrw.lhsTokens.size() != 0) {
        codeGenerator.printTokenSetup((Token)(e_nrw.lhsTokens.get(0)));
        for (java.util.Iterator it = e_nrw.lhsTokens.iterator(); it.hasNext();) {
          t = (Token)it.next();
          retval += codeGenerator.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
        retval += " = ";
      }
      String tail = e_nrw.rhsToken == null ? ");" :
                (isJavaDialect ? ")." : ")->") + e_nrw.rhsToken.image + ";";
      if (e_nrw.label.equals("")) {
        Object label = names_of_tokens.get(Integer.valueOf(e_nrw.ordinal));
        if (label != null) {
          retval += "jj_consume_token(" + (String)label + tail;
        } else {
          retval += "jj_consume_token(" + e_nrw.ordinal + tail;
        }
      } else {
        retval += "jj_consume_token(" + e_nrw.label + tail;
      }

      if ( !isJavaDialect && Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR)) {
	          retval += "\n    { if (hasError) { return __ERROR_RET__; } }\n";
      }

    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      retval += "\n";
      if (e_nrw.getLhsTokens().size() != 0) {
        codeGenerator.printTokenSetup((Token)(e_nrw.getLhsTokens().get(0)));
        for (java.util.Iterator it = e_nrw.getLhsTokens().iterator(); it.hasNext();) {
          t = (Token)it.next();
          retval += codeGenerator.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
        retval += " = ";
      }
      retval += e_nrw.getName() + "(";
      if (e_nrw.getArgumentTokens().size() != 0) {
        codeGenerator.printTokenSetup((Token)(e_nrw.getArgumentTokens().get(0)));
        for (java.util.Iterator it = e_nrw.getArgumentTokens().iterator(); it.hasNext();) {
          t = (Token)it.next();
          retval += codeGenerator.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
      }
      retval += ");";
      if ( !isJavaDialect && Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR)) {
	          retval += "\n    { if (hasError) { return __ERROR_RET__; } }\n";
      }
    } else if (e instanceof Action) {
      Action e_nrw = (Action)e;
      retval += "\u0003\n";
      if (!Options.booleanValue(Options.USEROPTION__CPP_IGNORE_ACTIONS) &&
          e_nrw.getActionTokens().size() != 0) {
        codeGenerator.printTokenSetup((Token)(e_nrw.getActionTokens().get(0))); ccol = 1;
        for (Iterator it = e_nrw.getActionTokens().iterator(); it.hasNext();) {
          t = (Token)it.next();
          retval += codeGenerator.getStringToPrint(t);
        }
        retval += codeGenerator.getTrailingComments(t);
      }
      retval += "\u0004";
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice)e;
      conds = new Lookahead[e_nrw.getChoices().size()];
      actions = new String[e_nrw.getChoices().size() + 1];
      actions[e_nrw.getChoices().size()] = "\n" + "jj_consume_token(-1);\n" +
                (isJavaDialect ? "throw new ParseException();"
		                        : ("errorHandler->handleParseError(token, getToken(1), __FUNCTION__, this), hasError = true;" +
		         (Options.booleanValue(Options.USEROPTION__CPP_STOP_ON_FIRST_ERROR) ? "return __ERROR_RET__;\n" : "")));

      // In previous line, the "throw" never throws an exception since the
      // evaluation of jj_consume_token(-1) causes ParseException to be
      // thrown first.
      Sequence nestedSeq;
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        nestedSeq = (Sequence)(e_nrw.getChoices().get(i));
        actions[i] = phase1ExpansionGen(nestedSeq);
        conds[i] = (Lookahead)(nestedSeq.units.get(0));
      }
      retval = buildLookaheadChecker(conds, actions);
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        // For C++, since we are not using exceptions, we will protect all the
        // expansion choices with if (!error)
        boolean wrap_in_block = false;
        if (!JavaCCGlobals.jjtreeGenerated && !isJavaDialect) {
          // for the last one, if it's an action, we will not protect it.
          Expansion elem = (Expansion)e_nrw.units.get(i);
          if (!(elem instanceof Action) ||
              !(e.parent instanceof BNFProduction) ||
              i != e_nrw.units.size() - 1) {
            wrap_in_block = true;
            retval += "\nif (" + (isJavaDialect ? "true" : "!hasError") + ") {";
          }
        }
        retval += phase1ExpansionGen((Expansion)(e_nrw.units.get(i)));
        if (wrap_in_block) {
          retval += "\n}";
        }
      }
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.get(0));
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      retval += "\n";
      int labelIndex = ++gensymindex;
      if (isJavaDialect) {
        retval += "label_" + labelIndex + ":\n";
      }
      retval += "while (" + (isJavaDialect ? "true" : "!hasError") + ") {\u0001";
      retval += phase1ExpansionGen(nested_e);
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";

      if (isJavaDialect) {
        actions[1] = "\nbreak label_" + labelIndex + ";";
      } else {
        actions[1] = "\ngoto end_label_" + labelIndex + ";";
      }

      retval += buildLookaheadChecker(conds, actions);
      retval += "\u0002\n" + "}";
      if (!isJavaDialect) {
        retval += "\nend_label_" + labelIndex + ": ;";
      }
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.get(0));
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      retval += "\n";
      int labelIndex = ++gensymindex;
      if (isJavaDialect) {
        retval += "label_" + labelIndex + ":\n";
      }
      retval += "while (" + (isJavaDialect ? "true" : "!hasError") + ") {\u0001";
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = "\n;";
      if (isJavaDialect) {
        actions[1] = "\nbreak label_" + labelIndex + ";";
      } else {
        actions[1] = "\ngoto end_label_" + labelIndex + ";";
      }
      retval += buildLookaheadChecker(conds, actions);
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
      if (!isJavaDialect) {
        retval += "\nend_label_" + labelIndex + ": ;";
      }
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      Expansion nested_e = e_nrw.expansion;
      Lookahead la;
      if (nested_e instanceof Sequence) {
        la = (Lookahead)(((Sequence)nested_e).units.get(0));
      } else {
        la = new Lookahead();
        la.setAmount(Options.getLookahead());
        la.setLaExpansion(nested_e);
      }
      conds = new Lookahead[1];
      conds[0] = la;
      actions = new String[2];
      actions[0] = phase1ExpansionGen(nested_e);
      actions[1] = "\n;";
      retval += buildLookaheadChecker(conds, actions);
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      Expansion nested_e = e_nrw.exp;
      java.util.List list;
      retval += "\n";
      retval += "try {\u0001";
      retval += phase1ExpansionGen(nested_e);
      retval += "\u0002\n" + "}";
      for (int i = 0; i < e_nrw.catchblks.size(); i++) {
        retval += " catch (";
        list = (java.util.List)(e_nrw.types.get(i));
        if (list.size() != 0) {
          codeGenerator.printTokenSetup((Token)(list.get(0)));
          for (java.util.Iterator it = list.iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += " ";
        t = (Token)(e_nrw.ids.get(i));
        codeGenerator.printTokenSetup(t);
        retval += codeGenerator.getStringToPrint(t);
        retval += codeGenerator.getTrailingComments(t);
        retval += ") {\u0003\n";
        list = (java.util.List)(e_nrw.catchblks.get(i));
        if (list.size() != 0) {
          codeGenerator.printTokenSetup((Token)(list.get(0))); ccol = 1;
          for (java.util.Iterator it = list.iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
      if (e_nrw.finallyblk != null) {
        if (isJavaDialect) {
          retval += " finally {\u0003\n";
        } else {
          retval += " finally {\u0003\n";
        }

        if (e_nrw.finallyblk.size() != 0) {
          codeGenerator.printTokenSetup((Token)(e_nrw.finallyblk.get(0))); ccol = 1;
          for (java.util.Iterator it = e_nrw.finallyblk.iterator(); it.hasNext();) {
            t = (Token)it.next();
            retval += codeGenerator.getStringToPrint(t);
          }
          retval += codeGenerator.getTrailingComments(t);
        }
        retval += "\u0004\n" + "}";
      }
    }
    return retval;
  }

  void buildPhase2Routine(Lookahead la) {
    Expansion e = la.getLaExpansion();
    if (isJavaDialect) {
      codeGenerator.genCodeLine("  " + staticOpt() + "private " + Options.getBooleanType() + " jj_2" + e.internal_name + "(int xla)");
    } else {
      codeGenerator.genCodeLine(" inline bool ", "jj_2" + e.internal_name + "(int xla)");
    }
    codeGenerator.genCodeLine(" {");
    codeGenerator.genCodeLine("    jj_la = xla; jj_lastpos = jj_scanpos = token;");

    String ret_suffix = "";
    if (Options.getDepthLimit() > 0) {
      ret_suffix = " && !jj_depth_error";
    }

    if (isJavaDialect) {
      codeGenerator.genCodeLine("    try { return (!jj_3" + e.internal_name + "()" + ret_suffix + "); }");
      codeGenerator.genCodeLine("    catch(LookaheadSuccess ls) { return true; }");
    } else {
      codeGenerator.genCodeLine("    jj_done = false;");
      codeGenerator.genCodeLine("    return (!jj_3" + e.internal_name + "() || jj_done)" + ret_suffix + ";");
    }
    if (Options.getErrorReporting()) {
      codeGenerator.genCodeLine((isJavaDialect ? "    finally " : " ") + "{ jj_save(" + (Integer.parseInt(e.internal_name.substring(1))-1) + ", xla); }");
    }
    codeGenerator.genCodeLine("  }");
    codeGenerator.genCodeLine("");
    Phase3Data p3d = new Phase3Data(e, la.getAmount());
    phase3list.add(p3d);
    phase3table.put(e, p3d);
  }

  private boolean xsp_declared;

  Expansion jj3_expansion;

  String genReturn(boolean value) {
    String retval = (value ? "true" : "false");
    if (Options.getDebugLookahead() && jj3_expansion != null) {
      String tracecode = "trace_return(\"" + JavaCCGlobals.addUnicodeEscapes(((NormalProduction)jj3_expansion.parent).getLhs()) +
      "(LOOKAHEAD " + (value ? "FAILED" : "SUCCEEDED") + ")\");";
      if (Options.getErrorReporting()) {
        tracecode = "if (!jj_rescan) " + tracecode;
      }
      return "{ " + tracecode + " return " + retval + "; }";
    } else {
      return "return " + retval + ";";
    }
  }

  private void generate3R(Expansion e, Phase3Data inf)
  {
    Expansion seq = e;
    if (e.internal_name.equals(""))
    {
      while (true)
      {
        if (seq instanceof Sequence && ((Sequence)seq).units.size() == 2)
        {
          seq = (Expansion)((Sequence)seq).units.get(1);
        }
        else if (seq instanceof NonTerminal)
        {
          NonTerminal e_nrw = (NonTerminal)seq;
          NormalProduction ntprod = (NormalProduction)(production_table.get(e_nrw.getName()));
          if (ntprod instanceof CodeProduction)
          {
            break; // nothing to do here
          }
          else
          {
            seq = ntprod.getExpansion();
          }
        }
        else
          break;
      }

      if (seq instanceof RegularExpression)
      {
        e.internal_name = "jj_scan_token(" + ((RegularExpression)seq).ordinal + ")";
        return;
      }

      gensymindex++;
//    if (gensymindex == 100)
//    {
//    new Error().codeGenerator.printStackTrace();
//    System.out.println(" ***** seq: " + seq.internal_name + "; size: " + ((Sequence)seq).units.size());
//    }
      e.internal_name = "R_" + e.getProductionName() + "_" + e.getLine()  + "_" + e.getColumn() + "_" + gensymindex;
      e.internal_index = gensymindex;
    }
    Phase3Data p3d = (Phase3Data)(phase3table.get(e));
    if (p3d == null || p3d.count < inf.count) {
      p3d = new Phase3Data(e, inf.count);
      phase3list.add(p3d);
      phase3table.put(e, p3d);
    }
  }

  void setupPhase3Builds(Phase3Data inf) {
    Expansion e = inf.exp;
    if (e instanceof RegularExpression) {
      ; // nothing to here
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set.  So
      // there's no need to check it below for "e_nrw" and "ntexp".  In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (NormalProduction)(production_table.get(e_nrw.getName()));
      if (ntprod instanceof CodeProduction) {
        ; // nothing to do here
      } else {
        generate3R(ntprod.getExpansion(), inf);
      }
    } else if (e instanceof Choice) {
      Choice e_nrw = (Choice)e;
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        generate3R((Expansion)(e_nrw.getChoices().get(i)), inf);
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (Expansion)(e_nrw.units.get(i));
        setupPhase3Builds(new Phase3Data(eseq, cnt));
        cnt -= minimumSize(eseq);
        if (cnt <= 0) break;
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      setupPhase3Builds(new Phase3Data(e_nrw.exp, inf.count));
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      generate3R(e_nrw.expansion, inf);
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      generate3R(e_nrw.expansion, inf);
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      generate3R(e_nrw.expansion, inf);
    }
  }

  private String getTypeForToken() {
    return isJavaDialect ? "Token" : "Token *";
  }

  private String genjj_3Call(Expansion e)
  {
    if (e.internal_name.startsWith("jj_scan_token"))
      return e.internal_name;
    else
      return "jj_3" + e.internal_name + "()";
  }

  Hashtable generated = new Hashtable();
  void buildPhase3Routine(Phase3Data inf, boolean recursive_call) {
    Expansion e = inf.exp;
    Token t = null;
    if (e.internal_name.startsWith("jj_scan_token"))
      return;

    if (!recursive_call) {
      if (isJavaDialect) {
        codeGenerator.genCodeLine("  " + staticOpt() + "private " + Options.getBooleanType() + " jj_3" + e.internal_name + "()");
      } else {
        codeGenerator.genCodeLine(" inline bool ", "jj_3" + e.internal_name + "()");
      }

      codeGenerator.genCodeLine(" {");
      if (!isJavaDialect) {
        codeGenerator.genCodeLine("    if (jj_done) return true;");
        if (Options.getDepthLimit() > 0) {
          codeGenerator.genCodeLine("#define __ERROR_RET__ true");
        }
      }
      genStackCheck(false);
      xsp_declared = false;
      if (Options.getDebugLookahead() && e.parent instanceof NormalProduction) {
        codeGenerator.genCode("    ");
        if (Options.getErrorReporting()) {
          codeGenerator.genCode("if (!jj_rescan) ");
        }
        codeGenerator.genCodeLine("trace_call(\"" + JavaCCGlobals.addUnicodeEscapes(((NormalProduction)e.parent).getLhs()) + "(LOOKING AHEAD...)\");");
        jj3_expansion = e;
      } else {
        jj3_expansion = null;
      }
    }
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      if (e_nrw.label.equals("")) {
        Object label = names_of_tokens.get(Integer.valueOf(e_nrw.ordinal));
        if (label != null) {
          codeGenerator.genCodeLine("    if (jj_scan_token(" + (String)label + ")) " + genReturn(true));
        } else {
          codeGenerator.genCodeLine("    if (jj_scan_token(" + e_nrw.ordinal + ")) " + genReturn(true));
        }
      } else {
        codeGenerator.genCodeLine("    if (jj_scan_token(" + e_nrw.label + ")) " + genReturn(true));
      }
      //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
    } else if (e instanceof NonTerminal) {
      // All expansions of non-terminals have the "name" fields set.  So
      // there's no need to check it below for "e_nrw" and "ntexp".  In
      // fact, we rely here on the fact that the "name" fields of both these
      // variables are the same.
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (NormalProduction)(production_table.get(e_nrw.getName()));
      if (ntprod instanceof CodeProduction) {
        codeGenerator.genCodeLine("    if (true) { jj_la = 0; jj_scanpos = jj_lastpos; " + genReturn(false) + "}");
      } else {
        Expansion ntexp = ntprod.getExpansion();
        //codeGenerator.genCodeLine("    if (jj_3" + ntexp.internal_name + "()) " + genReturn(true));
        codeGenerator.genCodeLine("    if (" + genjj_3Call(ntexp)+ ") " + genReturn(true));
        //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      }
    } else if (e instanceof Choice) {
      Sequence nested_seq;
      Choice e_nrw = (Choice)e;
      if (e_nrw.getChoices().size() != 1) {
        if (!xsp_declared) {
          xsp_declared = true;
          codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
        }
        codeGenerator.genCodeLine("    xsp = jj_scanpos;");
      }
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        nested_seq = (Sequence)(e_nrw.getChoices().get(i));
        Lookahead la = (Lookahead)(nested_seq.units.get(0));
        if (la.getActionTokens().size() != 0) {
          // We have semantic lookahead that must be evaluated.
          lookaheadNeeded = true;
          codeGenerator.genCodeLine("    jj_lookingAhead = true;");
          codeGenerator.genCode("    jj_semLA = ");
          codeGenerator.printTokenSetup((Token)(la.getActionTokens().get(0)));
          for (Iterator it = la.getActionTokens().iterator(); it.hasNext();) {
            t = (Token)it.next();
            codeGenerator.printToken(t);
          }
          codeGenerator.printTrailingComments(t);
          codeGenerator.genCodeLine(";");
          codeGenerator.genCodeLine("    jj_lookingAhead = false;");
        }
        codeGenerator.genCode("    if (");
        if (la.getActionTokens().size() != 0) {
          codeGenerator.genCode("!jj_semLA || ");
        }
        if (i != e_nrw.getChoices().size() - 1) {
          //codeGenerator.genCodeLine("jj_3" + nested_seq.internal_name + "()) {");
          codeGenerator.genCodeLine(genjj_3Call(nested_seq) + ") {");
          codeGenerator.genCodeLine("    jj_scanpos = xsp;");
        } else {
          //codeGenerator.genCodeLine("jj_3" + nested_seq.internal_name + "()) " + genReturn(true));
          codeGenerator.genCodeLine(genjj_3Call(nested_seq) + ") " + genReturn(true));
          //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
        }
      }
      for (int i = 1; i < e_nrw.getChoices().size(); i++) {
        //codeGenerator.genCodeLine("    } else if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
        codeGenerator.genCodeLine("    }");
      }
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      int cnt = inf.count;
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (Expansion)(e_nrw.units.get(i));
        buildPhase3Routine(new Phase3Data(eseq, cnt), true);

//      System.out.println("minimumSize: line: " + eseq.line + ", column: " + eseq.column + ": " +
//      minimumSize(eseq));//Test Code

        cnt -= minimumSize(eseq);
        if (cnt <= 0) break;
      }
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      buildPhase3Routine(new Phase3Data(e_nrw.exp, inf.count), true);
    } else if (e instanceof OneOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      OneOrMore e_nrw = (OneOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      //codeGenerator.genCodeLine("    if (jj_3" + nested_e.internal_name + "()) " + genReturn(true));
      codeGenerator.genCodeLine("    if (" + genjj_3Call(nested_e) + ") " + genReturn(true));
      //codeGenerator.genCodeLine("    if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      codeGenerator.genCodeLine("    while (true) {");
      codeGenerator.genCodeLine("      xsp = jj_scanpos;");
      //codeGenerator.genCodeLine("      if (jj_3" + nested_e.internal_name + "()) { jj_scanpos = xsp; break; }");
      codeGenerator.genCodeLine("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      //codeGenerator.genCodeLine("      if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      codeGenerator.genCodeLine("    }");
    } else if (e instanceof ZeroOrMore) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      Expansion nested_e = e_nrw.expansion;
      codeGenerator.genCodeLine("    while (true) {");
      codeGenerator.genCodeLine("      xsp = jj_scanpos;");
      //codeGenerator.genCodeLine("      if (jj_3" + nested_e.internal_name + "()) { jj_scanpos = xsp; break; }");
      codeGenerator.genCodeLine("      if (" + genjj_3Call(nested_e) + ") { jj_scanpos = xsp; break; }");
      //codeGenerator.genCodeLine("      if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
      codeGenerator.genCodeLine("    }");
    } else if (e instanceof ZeroOrOne) {
      if (!xsp_declared) {
        xsp_declared = true;
        codeGenerator.genCodeLine("    " + getTypeForToken() + " xsp;");
      }
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      Expansion nested_e = e_nrw.expansion;
      codeGenerator.genCodeLine("    xsp = jj_scanpos;");
      //codeGenerator.genCodeLine("    if (jj_3" + nested_e.internal_name + "()) jj_scanpos = xsp;");
      codeGenerator.genCodeLine("    if (" + genjj_3Call(nested_e) + ") jj_scanpos = xsp;");
      //codeGenerator.genCodeLine("    else if (jj_la == 0 && jj_scanpos == jj_lastpos) " + genReturn(false));
    }
    if (!recursive_call) {
      codeGenerator.genCodeLine("    " + genReturn(false));
      genStackCheckEnd();
      if (!isJavaDialect && Options.getDepthLimit() > 0) {
        codeGenerator.genCodeLine("#undef __ERROR_RET__");
      }
      codeGenerator.genCodeLine("  }");
      codeGenerator.genCodeLine("");
    }
  }

  int minimumSize(Expansion e) {
    return minimumSize(e, Integer.MAX_VALUE);
  }

  /*
   * Returns the minimum number of tokens that can parse to this expansion.
   */
  int minimumSize(Expansion e, int oldMin) {
    int retval = 0;  // should never be used.  Will be bad if it is.
    if (e.inMinimumSize) {
      // recursive search for minimum size unnecessary.
      return Integer.MAX_VALUE;
    }
    e.inMinimumSize = true;
    if (e instanceof RegularExpression) {
      retval = 1;
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod = (NormalProduction)(production_table.get(e_nrw.getName()));
      if (ntprod instanceof CodeProduction) {
        retval = Integer.MAX_VALUE;
        // Make caller think this is unending (for we do not go beyond JAVACODE during
        // phase3 execution).
      } else {
        Expansion ntexp = ntprod.getExpansion();
        retval = minimumSize(ntexp);
      }
    } else if (e instanceof Choice) {
      int min = oldMin;
      Expansion nested_e;
      Choice e_nrw = (Choice)e;
      for (int i = 0; min > 1 && i < e_nrw.getChoices().size(); i++) {
        nested_e = (Expansion)(e_nrw.getChoices().get(i));
        int min1 = minimumSize(nested_e, min);
        if (min > min1) min = min1;
      }
      retval = min;
    } else if (e instanceof Sequence) {
      int min = 0;
      Sequence e_nrw = (Sequence)e;
      // We skip the first element in the following iteration since it is the
      // Lookahead object.
      for (int i = 1; i < e_nrw.units.size(); i++) {
        Expansion eseq = (Expansion)(e_nrw.units.get(i));
        int mineseq = minimumSize(eseq);
        if (min == Integer.MAX_VALUE || mineseq == Integer.MAX_VALUE) {
          min = Integer.MAX_VALUE; // Adding infinity to something results in infinity.
        } else {
          min += mineseq;
          if (min > oldMin)
            break;
        }
      }
      retval = min;
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      retval = minimumSize(e_nrw.exp);
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      retval = minimumSize(e_nrw.expansion);
    } else if (e instanceof ZeroOrMore) {
      retval = 0;
    } else if (e instanceof ZeroOrOne) {
      retval = 0;
    } else if (e instanceof Lookahead) {
      retval = 0;
    } else if (e instanceof Action) {
      retval = 0;
    }
    e.inMinimumSize = false;
    return retval;
  }

  void build(CodeGenerator codeGenerator) {
    NormalProduction p;
    JavaCodeProduction jp;
    CppCodeProduction cp;
    Token t = null;

    this.codeGenerator = codeGenerator;
    for (java.util.Iterator prodIterator = bnfproductions.iterator(); prodIterator.hasNext();) {
      p = (NormalProduction)prodIterator.next();
      if (p instanceof CppCodeProduction) {
          cp = (CppCodeProduction)p;

          generateCPPMethodheader(cp);
//          t = (Token)(cp.getReturnTypeTokens().get(0));
//          codeGenerator.printTokenSetup(t); ccol = 1;
//          codeGenerator.printLeadingComments(t);
//          codeGenerator.genCode("  " + staticOpt() + (p.getAccessMod() != null ? p.getAccessMod() + " " : ""));
//          cline = t.beginLine; ccol = t.beginColumn;
//          codeGenerator.printTokenOnly(t);
//          for (int i = 1; i < cp.getReturnTypeTokens().size(); i++) {
//            t = (Token)(cp.getReturnTypeTokens().get(i));
//            codeGenerator.printToken(t);
//          }
//          codeGenerator.printTrailingComments(t);
//          codeGenerator.genCode(" " + cp.getLhs() + "(");
//          if (cp.getParameterListTokens().size() != 0) {
//            codeGenerator.printTokenSetup((Token)(cp.getParameterListTokens().get(0)));
//            for (java.util.Iterator it = cp.getParameterListTokens().iterator(); it.hasNext();) {
//              t = (Token)it.next();
//              codeGenerator.printToken(t);
//            }
//            codeGenerator.printTrailingComments(t);
//          }
//          codeGenerator.genCode(")");
//          for (java.util.Iterator it = cp.getThrowsList().iterator(); it.hasNext();) {
//            codeGenerator.genCode(", ");
//            java.util.List name = (java.util.List)it.next();
//            for (java.util.Iterator it2 = name.iterator(); it2.hasNext();) {
//              t = (Token)it2.next();
//              codeGenerator.genCode(t.image);
//            }
//          }
          codeGenerator.genCodeLine(" {");
          if (Options.getDebugParser()) {
            codeGenerator.genCodeLine("");
            if (isJavaDialect) {
                codeGenerator.genCodeLine("    trace_call(\"" + JavaCCGlobals.addUnicodeEscapes(cp.getLhs()) + "\");");
            } else {
          	  codeGenerator.genCodeLine("    JJEnter<std::function<void()>> jjenter([this]() {trace_call  (\"" + JavaCCGlobals.addUnicodeEscapes(cp.getLhs()) +"\"); });");
          	  codeGenerator.genCodeLine("    JJExit <std::function<void()>> jjexit ([this]() {trace_return(\"" + JavaCCGlobals.addUnicodeEscapes(cp.getLhs()) +"\"); });");
            }
            codeGenerator.genCodeLine("    try {");
          }
          if (cp.getCodeTokens().size() != 0) {
            codeGenerator.printTokenSetup((Token)(cp.getCodeTokens().get(0))); cline--;
            codeGenerator.printTokenList(cp.getCodeTokens());
          }
          codeGenerator.genCodeLine("");
          if (Options.getDebugParser()) {
            codeGenerator.genCodeLine("    } catch(...) { }");
          }
          codeGenerator.genCodeLine("  }");
          codeGenerator.genCodeLine("");
      } else
      if (p instanceof JavaCodeProduction) {
        if (!isJavaDialect) {
          JavaCCErrors.semantic_error("Cannot use JAVACODE productions with C++ output (yet).");
          continue;
        }
        jp = (JavaCodeProduction)p;
        t = (Token)(jp.getReturnTypeTokens().get(0));
        codeGenerator.printTokenSetup(t); ccol = 1;
        codeGenerator.printLeadingComments(t);
        codeGenerator.genCode("  " + staticOpt() + (p.getAccessMod() != null ? p.getAccessMod() + " " : ""));
        cline = t.beginLine; ccol = t.beginColumn;
        codeGenerator.printTokenOnly(t);
        for (int i = 1; i < jp.getReturnTypeTokens().size(); i++) {
          t = (Token)(jp.getReturnTypeTokens().get(i));
          codeGenerator.printToken(t);
        }
        codeGenerator.printTrailingComments(t);
        codeGenerator.genCode(" " + jp.getLhs() + "(");
        if (jp.getParameterListTokens().size() != 0) {
          codeGenerator.printTokenSetup((Token)(jp.getParameterListTokens().get(0)));
          for (java.util.Iterator it = jp.getParameterListTokens().iterator(); it.hasNext();) {
            t = (Token)it.next();
            codeGenerator.printToken(t);
          }
          codeGenerator.printTrailingComments(t);
        }
        codeGenerator.genCode(")");
        if (isJavaDialect) {
          codeGenerator.genCode(" throws ParseException");
        }
        for (java.util.Iterator it = jp.getThrowsList().iterator(); it.hasNext();) {
          codeGenerator.genCode(", ");
          java.util.List name = (java.util.List)it.next();
          for (java.util.Iterator it2 = name.iterator(); it2.hasNext();) {
            t = (Token)it2.next();
            codeGenerator.genCode(t.image);
          }
        }
        codeGenerator.genCode(" {");
        if (Options.getDebugParser()) {
          codeGenerator.genCodeLine("");
          codeGenerator.genCodeLine("    trace_call(\"" + JavaCCGlobals.addUnicodeEscapes(jp.getLhs()) + "\");");
          codeGenerator.genCode("    try {");
        }
        if (jp.getCodeTokens().size() != 0) {
          codeGenerator.printTokenSetup((Token)(jp.getCodeTokens().get(0))); cline--;
          codeGenerator.printTokenList(jp.getCodeTokens());
        }
        codeGenerator.genCodeLine("");
        if (Options.getDebugParser()) {
          codeGenerator.genCodeLine("    } finally {");
          codeGenerator.genCodeLine("      trace_return(\"" + JavaCCGlobals.addUnicodeEscapes(jp.getLhs()) + "\");");
          codeGenerator.genCodeLine("    }");
        }
        codeGenerator.genCodeLine("  }");
        codeGenerator.genCodeLine("");
      } else {
        buildPhase1Routine((BNFProduction)p);
      }
    }

    codeGenerator.switchToIncludeFile();
    for (int phase2index = 0; phase2index < phase2list.size(); phase2index++) {
      buildPhase2Routine((Lookahead)(phase2list.get(phase2index)));
    }

    int phase3index = 0;

    while (phase3index < phase3list.size()) {
      for (; phase3index < phase3list.size(); phase3index++) {
        setupPhase3Builds((Phase3Data)(phase3list.get(phase3index)));
      }
    }

    for (java.util.Enumeration enumeration = phase3table.elements(); enumeration.hasMoreElements();) {
      buildPhase3Routine((Phase3Data)(enumeration.nextElement()), false);
    }
    // for (java.util.Enumeration enumeration = phase3table.elements(); enumeration.hasMoreElements();) {
      // Phase3Data inf = (Phase3Data)(enumeration.nextElement());
      // System.err.println("**** Table for: " + inf.exp.internal_name);
      // buildPhase3Table(inf);
      // System.err.println("**** END TABLE *********");
    // }

    codeGenerator.switchToMainFile();
  }

  public void reInit()
  {
    gensymindex = 0;
    indentamt = 0;
    jj2LA = false;
    phase2list = new ArrayList();
    phase3list = new ArrayList();
    phase3table = new java.util.Hashtable();
    firstSet = null;
    xsp_declared = false;
    jj3_expansion = null;
  }

  // Table driven.
  void buildPhase3Table(Phase3Data inf) {
    Expansion e = inf.exp;
    Token t = null;
    if (e instanceof RegularExpression) {
      RegularExpression e_nrw = (RegularExpression)e;
      System.err.println("TOKEN, " + e_nrw.ordinal);
    } else if (e instanceof NonTerminal) {
      NonTerminal e_nrw = (NonTerminal)e;
      NormalProduction ntprod =
          (NormalProduction)(production_table.get(e_nrw.getName()));
      if (ntprod instanceof CodeProduction) {
        // javacode, true - always (warn?)
        System.err.println("JAVACODE_PROD, true");
      } else {
        Expansion ntexp = ntprod.getExpansion();
        // nt exp's table.
        System.err.println("PRODUCTION, " + ntexp.internal_index);
        //buildPhase3Table(new Phase3Data(ntexp, inf.count));
      }
    } else if (e instanceof Choice) {
      Sequence nested_seq;
      Choice e_nrw = (Choice)e;
      System.err.print("CHOICE, ");
      for (int i = 0; i < e_nrw.getChoices().size(); i++) {
        if (i > 0) System.err.print("\n|");
        nested_seq = (Sequence)(e_nrw.getChoices().get(i));
        Lookahead la = (Lookahead)(nested_seq.units.get(0));
        if (la.getActionTokens().size() != 0) {
          System.err.print("SEMANTIC,");
        } else {
          buildPhase3Table(new Phase3Data(nested_seq, inf.count));
        }
      }
      System.err.println();
    } else if (e instanceof Sequence) {
      Sequence e_nrw = (Sequence)e;
      int cnt = inf.count;
      if (e_nrw.units.size() > 2) {
        System.err.println("SEQ, " + cnt);
        for (int i = 1; i < e_nrw.units.size(); i++) {
          System.err.print("   ");
          Expansion eseq = (Expansion)(e_nrw.units.get(i));
          buildPhase3Table(new Phase3Data(eseq, cnt));
          cnt -= minimumSize(eseq);
          if (cnt <= 0) break;
        }
      } else {
        Expansion tmp = (Expansion)e_nrw.units.get(1);
        while (tmp instanceof NonTerminal) {
          NormalProduction ntprod =
              (NormalProduction)(
                  production_table.get(((NonTerminal)tmp).getName()));
          if (ntprod instanceof CodeProduction) break;
          tmp = ntprod.getExpansion();
        }
        buildPhase3Table(new Phase3Data(tmp, cnt));
      }
      System.err.println();
    } else if (e instanceof TryBlock) {
      TryBlock e_nrw = (TryBlock)e;
      buildPhase3Table(new Phase3Data(e_nrw.exp, inf.count));
    } else if (e instanceof OneOrMore) {
      OneOrMore e_nrw = (OneOrMore)e;
      System.err.println("SEQ PROD " + e_nrw.expansion.internal_index);
      System.err.println("ZEROORMORE " + e_nrw.expansion.internal_index);
    } else if (e instanceof ZeroOrMore) {
      ZeroOrMore e_nrw = (ZeroOrMore)e;
      System.err.print("ZEROORMORE, " + e_nrw.expansion.internal_index);
    } else if (e instanceof ZeroOrOne) {
      ZeroOrOne e_nrw = (ZeroOrOne)e;
      System.err.println("ZERORONE, " + e_nrw.expansion.internal_index);
    } else {
      assert(false);
      // table for nested_e - optional
    }
  }
}

/**
 * This class stores information to pass from phase 2 to phase 3.
 */
class Phase3Data {

  /*
   * This is the expansion to generate the jj3 method for.
   */
  Expansion exp;

  /*
   * This is the number of tokens that can still be consumed.  This
   * number is used to limit the number of jj3 methods generated.
   */
  int count;

  Phase3Data(Expansion e, int c) {
    exp = e;
    count = c;
  }
}
