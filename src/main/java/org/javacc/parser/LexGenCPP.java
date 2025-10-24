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

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.javacc.utils.OutputFileGenerator;

import static org.javacc.parser.JavaCCGlobals.*;

/**
 * Generate lexer.
 */
public class LexGenCPP extends LexGen //CodeGenerator implements JavaCCParserConstants
{
  @Override
  void PrintClassHead()
  {
    int i, j;

    List<String> tn = new ArrayList<String>(toolNames);
    tn.add(toolName);

    switchToStaticsFile();

    //standard includes
    switchToIncludeFile();
    genCodeLine("#include \"stdio.h\"");
    genCodeLine("#include \"JavaCC.h\"");
    genCodeLine("#include \"CharStream.h\"");
    genCodeLine("#include \"Token.h\"");
    genCodeLine("#include \"ErrorHandler.h\"");
    genCodeLine("#include \"TokenManager.h\"");
    genCodeLine("#include \"" + cu_name + "Constants.h\"");

    if (Options.stringValue(Options.USEROPTION__CPP_TOKEN_MANAGER_INCLUDE).length() > 0) {
      genCodeLine("#include \"" + Options.stringValue(Options.USEROPTION__CPP_TOKEN_MANAGER_INCLUDE) + "\"\n");
    }

    genCodeLine("");

    if (Options.stringValue(Options.USEROPTION__CPP_NAMESPACE).length() > 0) {
      genCodeLine("namespace " + Options.stringValue("NAMESPACE_OPEN"));
    }

    genCodeLine("class " + cu_name + ";");

    int l = 0, kind;
    i = 1;
    /* namespace?
    for (;;)
    {
      if (cu_to_insertion_point_1.size() <= l)
        break;

      kind = ((Token)cu_to_insertion_point_1.get(l)).kind;
      if(kind == PACKAGE || kind == IMPORT) {
        for (; i < cu_to_insertion_point_1.size(); i++) {
          kind = ((Token)cu_to_insertion_point_1.get(i)).kind;
          if (kind == CLASS)
          {
            cline = ((Token)(cu_to_insertion_point_1.get(l))).beginLine;
            ccol = ((Token)(cu_to_insertion_point_1.get(l))).beginColumn;
            for (j = l; j < i; j++) {
              printToken((Token)(cu_to_insertion_point_1.get(j)));
            }
            if (kind == SEMICOLON)
              printToken((Token)(cu_to_insertion_point_1.get(j)));
            genCodeLine("");
            break;
          }
        }
        l = ++i;
      }
      else
        break;
    }*/

    genCodeLine("");
    genCodeLine("/** Token Manager. */");
    String superClass = Options.stringValue(Options.USEROPTION__TOKEN_MANAGER_SUPER_CLASS);
    genClassStart(null, tokMgrClassName, new String[]{}, new String[]{"public TokenManager" + (superClass == null ? "" : ", public " + superClass) });

    if (token_mgr_decls != null && token_mgr_decls.size() > 0)
    {
      Token t = (Token)token_mgr_decls.get(0);
      boolean commonTokenActionSeen = false;
      boolean commonTokenActionNeeded = Options.getCommonTokenAction();

      printTokenSetup((Token)token_mgr_decls.get(0));
      ccol = 1;

      switchToMainFile();
      for (j = 0; j < token_mgr_decls.size(); j++)
      {
        t = (Token)token_mgr_decls.get(j);
        if (t.kind == IDENTIFIER &&
            commonTokenActionNeeded &&
            !commonTokenActionSeen) {
          commonTokenActionSeen = t.image.equals("CommonTokenAction");
          if (commonTokenActionSeen)
        	  t.image = cu_name + "TokenManager::" + t.image;
        }

        printToken(t);
      }

      switchToIncludeFile();
      genCodeLine("  void CommonTokenAction(Token* token);");

      if (Options.getTokenManagerUsesParser()) {
    	  genCodeLine("  void setParser(void* parser) {");
    	  genCodeLine("      this->parser = (" + cu_name + "*) parser;");
    	  genCodeLine("  }");
      }
      genCodeLine("");

      if (commonTokenActionNeeded && !commonTokenActionSeen)
        JavaCCErrors.warning("You have the COMMON_TOKEN_ACTION option set. " +
            "But it appears you have not defined the method :\n"+
            "      " + staticString + "void CommonTokenAction(Token *t)\n" +
        "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");

    }
    else if (Options.getCommonTokenAction())
    {
      JavaCCErrors.warning("You have the COMMON_TOKEN_ACTION option set. " +
          "But you have not defined the method :\n"+
          "      " + staticString + "void CommonTokenAction(Token *t)\n" +
      "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");
    }

    genCodeLine("");
    genCodeLine("  FILE *debugStream;");

    generateMethodDefHeader("  void ", tokMgrClassName, "setDebugStream(FILE *ds)");
    genCodeLine("{ debugStream = ds; }");

    switchToIncludeFile();
    if(Options.getTokenManagerUsesParser()){
      genCodeLine("");
      genCodeLine("private:");
      genCodeLine("  " + cu_name + "* parser = nullptr;");
    }
    switchToMainFile();
  }

  void DumpDebugMethods() throws IOException
  {
    writeTemplate("/templates/cpp/DumpDebugMethods.template",
          "maxOrdinal", maxOrdinal,
          "stateSetSize", stateSetSize);
  }

  static void BuildLexStatesTable()
  {
    Iterator<TokenProduction> it = rexprlist.iterator();
    TokenProduction tp;
    int i;

    String[] tmpLexStateName = new String[lexstate_I2S.size()];
    while (it.hasNext())
    {
      tp = it.next();
      List<RegExprSpec> respecs = tp.respecs;
      List<TokenProduction> tps;

      for (i = 0; i < tp.lexStates.length; i++)
      {
        if ((tps = (List<TokenProduction>)allTpsForState.get(tp.lexStates[i])) == null)
        {
          tmpLexStateName[maxLexStates++] = tp.lexStates[i];
          allTpsForState.put(tp.lexStates[i], tps = new ArrayList());
        }

        tps.add(tp);
      }

      if (respecs == null || respecs.size() == 0)
        continue;

      RegularExpression re;
      for (i = 0; i < respecs.size(); i++)
        if (maxOrdinal <= (re = ((RegExprSpec)respecs.get(i)).rexp).ordinal)
          maxOrdinal = re.ordinal + 1;
    }

    kinds = new int[maxOrdinal];
    toSkip = new long[maxOrdinal / 64 + 1];
    toSpecial = new long[maxOrdinal / 64 + 1];
    toMore = new long[maxOrdinal / 64 + 1];
    toToken = new long[maxOrdinal / 64 + 1];
    toToken[0] = 1L;
    actions = new Action[maxOrdinal];
    actions[0] = actForEof;
    hasTokenActions = actForEof != null;
    initStates = new Hashtable();
    canMatchAnyChar = new int[maxLexStates];
    canLoop = new boolean[maxLexStates];
    stateHasActions = new boolean[maxLexStates];
    lexStateName = new String[maxLexStates];
    singlesToSkip = new NfaState[maxLexStates];
    System.arraycopy(tmpLexStateName, 0, lexStateName, 0, maxLexStates);

    for (i = 0; i < maxLexStates; i++)
      canMatchAnyChar[i] = -1;

    hasNfa = new boolean[maxLexStates];
    mixed = new boolean[maxLexStates];
    maxLongsReqd = new int[maxLexStates];
    initMatch = new int[maxLexStates];
    newLexState = new String[maxOrdinal];
    newLexState[0] = nextStateForEof;
    hasEmptyMatch = false;
    lexStates = new int[maxOrdinal];
    ignoreCase = new boolean[maxOrdinal];
    rexprs = new RegularExpression[maxOrdinal];
    RStringLiteral.allImages = new String[maxOrdinal];
    canReachOnMore = new boolean[maxLexStates];
  }

  static int GetIndex(String name)
  {
    for (int i = 0; i < lexStateName.length; i++)
      if (lexStateName[i] != null && lexStateName[i].equals(name))
        return i;

    throw new Error(); // Should never come here
  }

  public static void AddCharToSkip(char c, int kind)
  {
    singlesToSkip[lexStateIndex].AddChar(c);
    singlesToSkip[lexStateIndex].kind = kind;
  }

  public void start() throws IOException
  {
    if (!Options.getBuildTokenManager() ||
        Options.getUserTokenManager() ||
        JavaCCErrors.get_error_count() > 0)
      return;

    keepLineCol = Options.getKeepLineColumn();
    List choices = new ArrayList();
    Enumeration e;
    TokenProduction tp;
    int i, j;

    staticString = (Options.getStatic() ? "static " : "");
    tokMgrClassName = cu_name + "TokenManager";

    PrintClassHead();
    BuildLexStatesTable();

    e = allTpsForState.keys();

    boolean ignoring = false;

    while (e.hasMoreElements())
    {
      NfaState.ReInit();
      RStringLiteral.ReInit();

      String key = (String)e.nextElement();

      lexStateIndex = GetIndex(key);
      lexStateSuffix = "_" + lexStateIndex;
      List<TokenProduction> allTps = (List<TokenProduction>)allTpsForState.get(key);
      initStates.put(key, initialState = new NfaState());
      ignoring = false;

      singlesToSkip[lexStateIndex] = new NfaState();
      singlesToSkip[lexStateIndex].dummy = true;

      if (key.equals("DEFAULT"))
        defaultLexState = lexStateIndex;

      for (i = 0; i < allTps.size(); i++)
      {
        tp = (TokenProduction)allTps.get(i);
        int kind = tp.kind;
        boolean ignore = tp.ignoreCase;
        List<RegExprSpec> rexps = tp.respecs;

        if (i == 0)
          ignoring = ignore;

        for (j = 0; j < rexps.size(); j++)
        {
          RegExprSpec respec = (RegExprSpec)rexps.get(j);
          curRE = respec.rexp;

          rexprs[curKind = curRE.ordinal] = curRE;
          lexStates[curRE.ordinal] = lexStateIndex;
          ignoreCase[curRE.ordinal] = ignore;

          if (curRE.private_rexp)
          {
            kinds[curRE.ordinal] = -1;
            continue;
          }

          if (curRE instanceof RStringLiteral &&
              !((RStringLiteral)curRE).image.equals(""))
          {
            ((RStringLiteral)curRE).GenerateDfa(this, curRE.ordinal);
            if (i != 0 && !mixed[lexStateIndex] && ignoring != ignore)
              mixed[lexStateIndex] = true;
          }
          else if (curRE.CanMatchAnyChar())
          {
            if (canMatchAnyChar[lexStateIndex] == -1 ||
                canMatchAnyChar[lexStateIndex] > curRE.ordinal)
              canMatchAnyChar[lexStateIndex] = curRE.ordinal;
          }
          else
          {
            Nfa temp;

            if (curRE instanceof RChoice)
              choices.add(curRE);

            temp = curRE.GenerateNfa(ignore);
            temp.end.isFinal = true;
            temp.end.kind = curRE.ordinal;
            initialState.AddMove(temp.start);
          }

          if (kinds.length < curRE.ordinal)
          {
            int[] tmp = new int[curRE.ordinal + 1];

            System.arraycopy(kinds, 0, tmp, 0, kinds.length);
            kinds = tmp;
          }
          //System.out.println("   ordina : " + curRE.ordinal);

          kinds[curRE.ordinal] = kind;

          if (respec.nextState != null &&
              !respec.nextState.equals(lexStateName[lexStateIndex]))
            newLexState[curRE.ordinal] = respec.nextState;

          if (respec.act != null && respec.act.getActionTokens() != null &&
              respec.act.getActionTokens().size() > 0)
            actions[curRE.ordinal] = respec.act;

          switch(kind)
          {
          case TokenProduction.SPECIAL :
            hasSkipActions |= (actions[curRE.ordinal] != null) ||
            (newLexState[curRE.ordinal] != null);
            hasSpecial = true;
            toSpecial[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
            toSkip[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
            break;
          case TokenProduction.SKIP :
            hasSkipActions |= (actions[curRE.ordinal] != null);
            hasSkip = true;
            toSkip[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
            break;
          case TokenProduction.MORE :
            hasMoreActions |= (actions[curRE.ordinal] != null);
            hasMore = true;
            toMore[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);

            if (newLexState[curRE.ordinal] != null)
              canReachOnMore[GetIndex(newLexState[curRE.ordinal])] = true;
            else
              canReachOnMore[lexStateIndex] = true;

            break;
          case TokenProduction.TOKEN :
            hasTokenActions |= (actions[curRE.ordinal] != null);
            toToken[curRE.ordinal / 64] |= 1L << (curRE.ordinal % 64);
            break;
          }
        }
      }

      // Generate a static block for initializing the nfa transitions
      NfaState.ComputeClosures();

      for (i = 0; i < initialState.epsilonMoves.size(); i++)
        ((NfaState)initialState.epsilonMoves.elementAt(i)).GenerateCode();

      hasNfa[lexStateIndex] = (NfaState.generatedStates != 0);
      if (hasNfa[lexStateIndex])
      {
        initialState.GenerateCode();
        initialState.GenerateInitMoves(this);
      }

      if (initialState.kind != Integer.MAX_VALUE && initialState.kind != 0)
      {
        if ((toSkip[initialState.kind / 64] & (1L << initialState.kind)) != 0L ||
            (toSpecial[initialState.kind / 64] & (1L << initialState.kind)) != 0L)
          hasSkipActions = true;
        else if ((toMore[initialState.kind / 64] & (1L << initialState.kind)) != 0L)
          hasMoreActions = true;
        else
          hasTokenActions = true;

        if (initMatch[lexStateIndex] == 0 ||
            initMatch[lexStateIndex] > initialState.kind)
        {
          initMatch[lexStateIndex] = initialState.kind;
          hasEmptyMatch = true;
        }
      }
      else if (initMatch[lexStateIndex] == 0)
        initMatch[lexStateIndex] = Integer.MAX_VALUE;

      RStringLiteral.FillSubString();

      if (hasNfa[lexStateIndex] && !mixed[lexStateIndex])
        RStringLiteral.GenerateNfaStartStates(this, initialState);

      RStringLiteral.DumpDfaCode(this);

      if (hasNfa[lexStateIndex])
        NfaState.DumpMoveNfa(this);

      if (stateSetSize < NfaState.generatedStates)
        stateSetSize = NfaState.generatedStates;
    }

    for (i = 0; i < choices.size(); i++)
      ((RChoice)choices.get(i)).CheckUnmatchability();

    NfaState.DumpStateSets(this);
    CheckEmptyStringMatch();
    NfaState.DumpNonAsciiMoveMethods(this);
    RStringLiteral.DumpStrLiteralImages(this);
    DumpFillToken();
    DumpGetNextToken();

    if (Options.getDebugTokenManager())
    {
      NfaState.DumpStatesForKind(this);
      DumpDebugMethods();
    }

    if (hasLoop)
    {
      switchToStaticsFile();
      genCodeLine("static int  jjemptyLineNo[" + maxLexStates + "];");
      genCodeLine("static int  jjemptyColNo[" + maxLexStates + "];");
      genCodeLine("static bool jjbeenHere[" + maxLexStates + "];");
      switchToMainFile();
    }

    if (hasSkipActions)
      DumpSkipActions();
    if (hasMoreActions)
      DumpMoreActions();
    if (hasTokenActions)
      DumpTokenActions();

    NfaState.PrintBoilerPlateCPP(this);

    String charStreamName;
    if (Options.getUserCharStream())
      charStreamName = "CharStream";
    else
    {
      if (Options.getJavaUnicodeEscape())
        charStreamName = "JavaCharStream";
      else
        charStreamName = "SimpleCharStream";
    }

    writeTemplate("/templates/cpp/TokenManagerBoilerPlateMethods.template",
      "charStreamName", "CharStream",
      "parserClassName", cu_name,
      "defaultLexState", "defaultLexState",
      "lexStateNameLength", lexStateName.length);

    dumpBoilerPlateInHeader();

    // in the include file close the class signature
    DumpStaticVarDeclarations(); // static vars actually inst

    switchToIncludeFile(); // remaining variables
    writeTemplate("/templates/cpp/DumpVarDeclarations.template",
      "charStreamName", "CharStream",
      "lexStateNameLength", lexStateName.length);
    genCodeLine(/*{*/ "};");

    switchToStaticsFile();
// TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
    String fileName = Options.getOutputDirectory() + File.separator +
                      tokMgrClassName +
                      getFileExtension(Options.getOutputLanguage());
    saveOutput(fileName);
  }

  private void dumpBoilerPlateInHeader() {
    switchToIncludeFile();
    genCodeLine("#ifndef JAVACC_CHARSTREAM");
    genCodeLine("#define JAVACC_CHARSTREAM CharStream");
    genCodeLine("#endif");
    genCodeLine("");

    genCodeLine("private:");
    genCodeLine("  void ReInitRounds();");
    genCodeLine("");
    genCodeLine("public:");
    genCodeLine("  " + tokMgrClassName + "(JAVACC_CHARSTREAM *stream, int lexState = " + defaultLexState + ");");
    genCodeLine("  virtual ~" + tokMgrClassName + "();");
    genCodeLine("  void ReInit(JAVACC_CHARSTREAM *stream, int lexState = " + defaultLexState + ");");
    genCodeLine("  void SwitchTo(int lexState);");
    genCodeLine("  void clear();");
    genCodeLine("  const JJSimpleString jjKindsForBitVector(int i, " + Options.getLongType() + " vec);");
    genCodeLine("  const JJSimpleString jjKindsForStateVector(int lexState, int vec[], int start, int end);");
    genCodeLine("");
  }

  private void DumpStaticVarDeclarations() throws IOException
  {
    int i;

    switchToStaticsFile(); // remaining variables
    genCodeLine("");
    genCodeLine("/** Lexer state names. */");
    genStringLiteralArrayCPP("lexStateNames", lexStateName);

    if (maxLexStates > 1)
    {
      genCodeLine("");
      genCodeLine("/** Lex State array. */");
      genCode("static const int jjnewLexState[] = {");

      for (i = 0; i < maxOrdinal; i++)
      {
        if (i % 25 == 0)
          genCode("\n   ");

        if (newLexState[i] == null)
          genCode("-1, ");
        else
          genCode(GetIndex(newLexState[i]) + ", ");
      }
      genCodeLine("\n};");
    }

    if (hasSkip || hasMore || hasSpecial)
    {
      // Bit vector for TOKEN
      genCode("static const " + Options.getLongType() + " jjtoToken[] = {");
      for (i = 0; i < maxOrdinal / 64 + 1; i++)
      {
        if (i % 4 == 0)
          genCode("\n   ");
        genCode("0x" + Long.toHexString(toToken[i]) + "L, ");
      }
      genCodeLine("\n};");
    }

    if (hasSkip || hasSpecial)
    {
      // Bit vector for SKIP
      genCode("static const " + Options.getLongType() + " jjtoSkip[] = {");
      for (i = 0; i < maxOrdinal / 64 + 1; i++)
      {
        if (i % 4 == 0)
          genCode("\n   ");
        genCode("0x" + Long.toHexString(toSkip[i]) + "L, ");
      }
      genCodeLine("\n};");
    }

    if (hasSpecial)
    {
      // Bit vector for SPECIAL
      genCode("static const " + Options.getLongType() + " jjtoSpecial[] = {");
      for (i = 0; i < maxOrdinal / 64 + 1; i++)
      {
        if (i % 4 == 0)
          genCode("\n   ");
        genCode("0x" + Long.toHexString(toSpecial[i]) + "L, ");
      }
      genCodeLine("\n};");
    }

    /*if (hasMore) // Not needed as we just use else
    {
      // Bit vector for MORE
      genCode("static const " + Options.getLongType() + " jjtoMore[] = {");
      for (i = 0; i < maxOrdinal / 64 + 1; i++)
      {
        if (i % 4 == 0)
          genCode("\n   ");
        genCode("0x" + Long.toHexString(toMore[i]) + "L, ");
      }
      genCodeLine("\n};");
    }*/
  }

  void DumpFillToken()
  {
    final double tokenVersion = JavaFiles.getVersion("Token.java");
    final boolean hasBinaryNewToken = tokenVersion > 4.09;

    generateMethodDefHeader("Token *", tokMgrClassName, "jjFillToken()");
    genCodeLine("{");
    genCodeLine("   Token *t;");
    genCodeLine("   JJString curTokenImage;");
    if (keepLineCol)
    {
      genCodeLine("   int beginLine   = -1;");
      genCodeLine("   int endLine     = -1;");
      genCodeLine("   int beginColumn = -1;");
      genCodeLine("   int endColumn   = -1;");
    }

    if (hasEmptyMatch)
    {
      genCodeLine("   if (jjmatchedPos < 0)");
      genCodeLine("   {");
      genCodeLine("       curTokenImage = image.c_str();");

      if (keepLineCol)
      {
        genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        genCodeLine("      beginLine = endLine = input_stream->getEndLine();");
        genCodeLine("      beginColumn = endColumn = input_stream->getEndColumn();");
        genCodeLine("   }");
      }

      genCodeLine("   }");
      genCodeLine("   else");
      genCodeLine("   {");
      genCodeLine("      JJString im = jjstrLiteralImages[jjmatchedKind];");
      genCodeLine("      curTokenImage = (im.length() == 0) ? input_stream->GetImage() : im;");

      if (keepLineCol)
      {
        genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        genCodeLine("      beginLine = input_stream->getBeginLine();");
        genCodeLine("      beginColumn = input_stream->getBeginColumn();");
        genCodeLine("      endLine = input_stream->getEndLine();");
        genCodeLine("      endColumn = input_stream->getEndColumn();");
        genCodeLine("   }");
      }

      genCodeLine("   }");
    }
    else
    {
      genCodeLine("   JJString im = jjstrLiteralImages[jjmatchedKind];");
      genCodeLine("   curTokenImage = (im.length() == 0) ? input_stream->GetImage() : im;");
      if (keepLineCol)
      {
        genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        genCodeLine("     beginLine = input_stream->getBeginLine();");
        genCodeLine("     beginColumn = input_stream->getBeginColumn();");
        genCodeLine("     endLine = input_stream->getEndLine();");
        genCodeLine("     endColumn = input_stream->getEndColumn();");
        genCodeLine("   }");
      }
    }

    if (Options.getTokenFactory().length() > 0) {
      genCodeLine("   t = " + getClassQualifier(Options.getTokenFactory()) + "newToken(jjmatchedKind, curTokenImage);");
    } else if (hasBinaryNewToken)
    {
      genCodeLine("   t = " + getClassQualifier("Token") + "newToken(jjmatchedKind, curTokenImage);");
    }
    else
    {
      genCodeLine("   t = " + getClassQualifier("Token") + "newToken(jjmatchedKind);");
      genCodeLine("   t->kind = jjmatchedKind;");
      genCodeLine("   t->image = curTokenImage;");
    }
    genCodeLine("   t->specialToken = nullptr;");
    genCodeLine("   t->next = nullptr;");

    if (keepLineCol) {
      genCodeLine("");
      genCodeLine("   if (input_stream->getTrackLineColumn()) {");
        genCodeLine("   t->beginLine = beginLine;");
        genCodeLine("   t->endLine = endLine;");
        genCodeLine("   t->beginColumn = beginColumn;");
        genCodeLine("   t->endColumn = endColumn;");
      genCodeLine("   }");
    }

    genCodeLine("");
    genCodeLine("   return t;");
    genCodeLine("}");
  }

  void DumpGetNextToken()
  {
    int i;

    switchToIncludeFile();
    genCodeLine("");
    genCodeLine("public:");
    genCodeLine("    int curLexState = 0;");
    genCodeLine("    int jjnewStateCnt = 0;");
    genCodeLine("    int jjround = 0;");
    genCodeLine("    int jjmatchedPos = 0;");
    genCodeLine("    int jjmatchedKind = 0;");
    genCodeLine("");
    switchToMainFile();
    genCodeLine("const int defaultLexState = " + defaultLexState + ";");
    genCodeLine("/** Get the next Token. */");
    generateMethodDefHeader("Token *", tokMgrClassName, "getNextToken()");
    genCodeLine("{");
    if (hasSpecial) {
      genCodeLine("  Token *specialToken = nullptr;");
    }
    genCodeLine("  Token *matchedToken = nullptr;");
    genCodeLine("  int curPos = 0;");
    genCodeLine("");
    genCodeLine("  for (;;)");
    genCodeLine("  {");
    genCodeLine("   EOFLoop: ");
    //genCodeLine("   {");
    //genCodeLine("      curChar = input_stream->BeginToken();");
    //genCodeLine("   }");
    genCodeLine("   if (input_stream->endOfInput())");
    genCodeLine("   {");
    //genCodeLine("     input_stream->backup(1);");

    if (Options.getDebugTokenManager())
      genCodeLine("      fprintf(debugStream, \"Returning the <EOF> token.\\n\");");

    genCodeLine("      jjmatchedKind = 0;");
    genCodeLine("      jjmatchedPos = -1;");
    genCodeLine("      matchedToken = jjFillToken();");

    if (hasSpecial)
      genCodeLine("      matchedToken->specialToken = specialToken;");

    if (nextStateForEof != null || actForEof != null)
      genCodeLine("      TokenLexicalActions(matchedToken);");

    if (Options.getCommonTokenAction())
      genCodeLine("      CommonTokenAction(matchedToken);");

    genCodeLine("      return matchedToken;");
    genCodeLine("   }");
    genCodeLine("   curChar = input_stream->BeginToken();");

    if (hasMoreActions || hasSkipActions || hasTokenActions)
    {
      genCodeLine("   image = jjimage;");
      genCodeLine("   image.clear();");
      genCodeLine("   jjimageLen = 0;");
    }

    genCodeLine("");

    String prefix = "";
    if (hasMore)
    {
      genCodeLine("   for (;;)");
      genCodeLine("   {");
      prefix = "  ";
    }

    String endSwitch = "";
    String caseStr = "";
    // this also sets up the start state of the nfa
    if (maxLexStates > 1)
    {
      genCodeLine(prefix + "   switch(curLexState)");
      genCodeLine(prefix + "   {");
      endSwitch = prefix + "   }";
      caseStr = prefix + "     case ";
      prefix += "    ";
    }

    prefix += "   ";
    for(i = 0; i < maxLexStates; i++)
    {
      if (maxLexStates > 1)
        genCodeLine(caseStr + i + ":");

      if (singlesToSkip[i].HasTransitions())
      {
        // added the backup(0) to make JIT happy
        genCodeLine(prefix + "{ input_stream->backup(0);");
        if (singlesToSkip[i].asciiMoves[0] != 0L &&
            singlesToSkip[i].asciiMoves[1] != 0L)
        {
          genCodeLine(prefix + "   while ((curChar < 64" + " && (0x" +
              Long.toHexString(singlesToSkip[i].asciiMoves[0]) +
              "L & (1L << curChar)) != 0L) || \n" +
              prefix + "          (curChar >> 6) == 1" +
              " && (0x" +
              Long.toHexString(singlesToSkip[i].asciiMoves[1]) +
          "L & (1L << (curChar & 077))) != 0L)");
        }
        else if (singlesToSkip[i].asciiMoves[1] == 0L)
        {
          genCodeLine(prefix + "   while (curChar <= " +
              (int)MaxChar(singlesToSkip[i].asciiMoves[0]) + " && (0x" +
              Long.toHexString(singlesToSkip[i].asciiMoves[0]) +
          "L & (1L << curChar)) != 0L)");
        }
        else if (singlesToSkip[i].asciiMoves[0] == 0L)
        {
          genCodeLine(prefix + "   while (curChar > 63 && curChar <= " +
              ((int)MaxChar(singlesToSkip[i].asciiMoves[1]) + 64) +
              " && (0x" +
              Long.toHexString(singlesToSkip[i].asciiMoves[1]) +
          "L & (1L << (curChar & 077))) != 0L)");
        }

        genCodeLine(prefix + "{");
        if (Options.getDebugTokenManager())
        {
          if (maxLexStates > 1) {
            genCodeLine("      fprintf(debugStream, \"<%s>\" , addUnicodeEscapes(lexStateNames[curLexState]).c_str());");
          }

          genCodeLine("      fprintf(debugStream, \"Skipping character : %c(%d)\\n\", curChar, (int)curChar);");
        }

        genCodeLine(prefix + "if (input_stream->endOfInput()) { goto EOFLoop; }");
        genCodeLine(prefix + "curChar = input_stream->BeginToken();");
        genCodeLine(prefix + "}");
        genCodeLine(prefix + "}");
      }

      if (initMatch[i] != Integer.MAX_VALUE && initMatch[i] != 0)
      {
        if (Options.getDebugTokenManager())
          genCodeLine("      fprintf(debugStream, \"   Matched the empty string as %s token.\\n\", addUnicodeEscapes(tokenImage[" + initMatch[i] + "]).c_str());");

        genCodeLine(prefix + "jjmatchedKind = " + initMatch[i] + ";");
        genCodeLine(prefix + "jjmatchedPos = -1;");
        genCodeLine(prefix + "curPos = 0;");
      }
      else
      {
        genCodeLine(prefix + "jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
        genCodeLine(prefix + "jjmatchedPos = 0;");
      }

      if (Options.getDebugTokenManager()) {
        genCodeLine("   fprintf(debugStream, " +
          "\"<%s>Current character : %c(%d) at line %d column %d\\n\","+
          "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, " +
          "input_stream->getEndLine(), input_stream->getEndColumn());");
      }

      genCodeLine(prefix + "curPos = jjMoveStringLiteralDfa0_" + i + "();");

      if (canMatchAnyChar[i] != -1)
      {
        if (initMatch[i] != Integer.MAX_VALUE && initMatch[i] != 0)
          genCodeLine(prefix + "if (jjmatchedPos < 0 || (jjmatchedPos == 0 && jjmatchedKind > " +
              canMatchAnyChar[i] + "))");
        else
          genCodeLine(prefix + "if (jjmatchedPos == 0 && jjmatchedKind > " +
              canMatchAnyChar[i] + ")");
        genCodeLine(prefix + "{");

        if (Options.getDebugTokenManager()) {
          genCodeLine("           fprintf(debugStream, \"   Current character matched as a %s token.\\n\", addUnicodeEscapes(tokenImage[" + canMatchAnyChar[i] + "]).c_str());");
        }
        genCodeLine(prefix + "   jjmatchedKind = " + canMatchAnyChar[i] + ";");

        if (initMatch[i] != Integer.MAX_VALUE && initMatch[i] != 0)
          genCodeLine(prefix + "   jjmatchedPos = 0;");

        genCodeLine(prefix + "}");
      }

      if (maxLexStates > 1)
        genCodeLine(prefix + "break;");
    }

    if (maxLexStates > 1)
      genCodeLine(endSwitch);
    else if (maxLexStates == 0)
      genCodeLine("       jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");

    if (maxLexStates > 1)
      prefix = "  ";
    else
      prefix = "";

    if (maxLexStates > 0)
    {
      genCodeLine(prefix + "   if (jjmatchedKind != 0x" + Integer.toHexString(Integer.MAX_VALUE) + ")");
      genCodeLine(prefix + "   {");
      genCodeLine(prefix + "      if (jjmatchedPos + 1 < curPos)");

      if (Options.getDebugTokenManager()) {
        genCodeLine(prefix + "      {");
        genCodeLine(prefix + "         fprintf(debugStream, " +
        "\"   Putting back %d characters into the input stream.\\n\", (curPos - jjmatchedPos - 1));");
      }

      genCodeLine(prefix + "         input_stream->backup(curPos - jjmatchedPos - 1);");

      if (Options.getDebugTokenManager()) {
        genCodeLine(prefix + "      }");
      }

      if (Options.getDebugTokenManager())
      {
          genCodeLine("    fprintf(debugStream, " +
              "\"****** FOUND A %d(%s) MATCH (%s) ******\\n\", jjmatchedKind, addUnicodeEscapes(tokenImage[jjmatchedKind]).c_str(), addUnicodeEscapes(input_stream->GetSuffix(jjmatchedPos + 1)).c_str());");
      }

      if (hasSkip || hasMore || hasSpecial)
      {
        genCodeLine(prefix + "      if ((jjtoToken[jjmatchedKind >> 6] & " +
        "(1L << (jjmatchedKind & 077))) != 0L)");
        genCodeLine(prefix + "      {");
      }

      genCodeLine(prefix + "         matchedToken = jjFillToken();");

      if (hasSpecial)
        genCodeLine(prefix + "         matchedToken->specialToken = specialToken;");

      if (hasTokenActions)
        genCodeLine(prefix + "         TokenLexicalActions(matchedToken);");

      if (maxLexStates > 1)
      {
        genCodeLine("       if (jjnewLexState[jjmatchedKind] != -1)");
        genCodeLine(prefix + "       curLexState = jjnewLexState[jjmatchedKind];");
      }

      if (Options.getCommonTokenAction())
        genCodeLine(prefix + "         CommonTokenAction(matchedToken);");

      genCodeLine(prefix + "         return matchedToken;");

      if (hasSkip || hasMore || hasSpecial)
      {
        genCodeLine(prefix + "      }");

        if (hasSkip || hasSpecial)
        {
          if (hasMore)
          {
            genCodeLine(prefix + "      else if ((jjtoSkip[jjmatchedKind >> 6] & " +
            "(1L << (jjmatchedKind & 077))) != 0L)");
          }
          else
            genCodeLine(prefix + "      else");

          genCodeLine(prefix + "      {");

          if (hasSpecial)
          {
            genCodeLine(prefix + "         if ((jjtoSpecial[jjmatchedKind >> 6] & " +
            "(1L << (jjmatchedKind & 077))) != 0L)");
            genCodeLine(prefix + "         {");

            genCodeLine(prefix + "            matchedToken = jjFillToken();");

            genCodeLine(prefix + "            if (specialToken == nullptr)");
            genCodeLine(prefix + "               specialToken = matchedToken;");
            genCodeLine(prefix + "            else");
            genCodeLine(prefix + "            {");
            genCodeLine(prefix + "               matchedToken->specialToken = specialToken;");
            genCodeLine(prefix + "               specialToken = (specialToken->next = matchedToken);");
            genCodeLine(prefix + "            }");

            if (hasSkipActions)
              genCodeLine(prefix + "            SkipLexicalActions(matchedToken);");

            genCodeLine(prefix + "         }");

            if (hasSkipActions)
            {
              genCodeLine(prefix + "         else");
              genCodeLine(prefix + "            SkipLexicalActions(nullptr);");
            }
          }
          else if (hasSkipActions)
            genCodeLine(prefix + "         SkipLexicalActions(nullptr);");

          if (maxLexStates > 1)
          {
            genCodeLine("         if (jjnewLexState[jjmatchedKind] != -1)");
            genCodeLine(prefix + "         curLexState = jjnewLexState[jjmatchedKind];");
          }

          genCodeLine(prefix + "         goto EOFLoop;");
          genCodeLine(prefix + "      }");
        }

        if (hasMore)
        {
          if (hasMoreActions)
            genCodeLine(prefix + "      MoreLexicalActions();");
          else if (hasSkipActions || hasTokenActions)
            genCodeLine(prefix + "      jjimageLen += jjmatchedPos + 1;");

          if (maxLexStates > 1)
          {
            genCodeLine("      if (jjnewLexState[jjmatchedKind] != -1)");
            genCodeLine(prefix + "      curLexState = jjnewLexState[jjmatchedKind];");
          }
          genCodeLine(prefix + "      curPos = 0;");
          genCodeLine(prefix + "      jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");

          genCodeLine(prefix + "   if (!input_stream->endOfInput()) {");
          genCodeLine(prefix + "         curChar = input_stream->readChar();");

          if (Options.getDebugTokenManager()) {
            genCodeLine("   fprintf(debugStream, " +
             "\"<%s>Current character : %c(%d) at line %d column %d\\n\","+
             "addUnicodeEscapes(lexStateNames[curLexState]).c_str(), curChar, (int)curChar, " +
             "input_stream->getEndLine(), input_stream->getEndColumn());");
          }
          genCodeLine(prefix + "   continue;");
          genCodeLine(prefix + " }");
        }
      }

      genCodeLine(prefix + "   }");
      genCodeLine(prefix + "   int error_line = input_stream->getEndLine();");
      genCodeLine(prefix + "   int error_column = input_stream->getEndColumn();");
      genCodeLine(prefix + "   JJString error_after;");
      genCodeLine(prefix + "   bool EOFSeen = false;");
      genCodeLine(prefix + "   if (input_stream->endOfInput()) {");
      genCodeLine(prefix + "      EOFSeen = true;");
      genCodeLine(prefix + "      error_after = curPos <= 1 ? EMPTY : input_stream->GetImage();");
      genCodeLine(prefix + "      if (curChar == '\\n' || curChar == '\\r') {");
      genCodeLine(prefix + "         error_line++;");
      genCodeLine(prefix + "         error_column = 0;");
      genCodeLine(prefix + "      }");
      genCodeLine(prefix + "      else");
      genCodeLine(prefix + "         error_column++;");
      genCodeLine(prefix + "   }");
      genCodeLine(prefix + "   if (!EOFSeen) {");
      genCodeLine(prefix + "      error_after = curPos <= 1 ? EMPTY : input_stream->GetImage();");
      genCodeLine(prefix + "   }");
      genCodeLine(prefix + "   errorHandler->lexicalError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, this);");
    }

    if (hasMore)
      genCodeLine(prefix + " }");

    genCodeLine("  }");
    genCodeLine("}");
    genCodeLine("");
  }

  public void DumpSkipActions()
  {
    Action act;

    generateMethodDefHeader("void ", tokMgrClassName, "SkipLexicalActions(Token *matchedToken)");
    genCodeLine("{");
    genCodeLine("   switch(jjmatchedKind)");
    genCodeLine("   {");

    Outer:
      for (int i = 0; i < maxOrdinal; i++)
      {
        if ((toSkip[i / 64] & (1L << (i % 64))) == 0L)
          continue;

        for (;;)
        {
          if (((act = (Action)actions[i]) == null ||
              act.getActionTokens() == null ||
              act.getActionTokens().size() == 0) && !canLoop[lexStates[i]])
            continue Outer;

          genCodeLine("      case " + i + " : {");

          if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
          {
            genCodeLine("         if (jjmatchedPos == -1)");
            genCodeLine("         {");
            genCodeLine("            if (jjbeenHere[" + lexStates[i] + "] &&");
            genCodeLine("                jjemptyLineNo[" + lexStates[i] + "] == input_stream->getBeginLine() &&");
            genCodeLine("                jjemptyColNo[" + lexStates[i] + "] == input_stream->getBeginColumn())");
            genCodeLine("               errorHandler->lexicalError(JJString(\"(\"Error: Bailing out of infinite loop caused by repeated empty string matches \" + \"at line \" + input_stream->getBeginLine() + \", \" + \"column \" + input_stream->getBeginColumn() + \".\")), this);");
            genCodeLine("            jjemptyLineNo[" + lexStates[i] + "] = input_stream->getBeginLine();");
            genCodeLine("            jjemptyColNo[" + lexStates[i] + "] = input_stream->getBeginColumn();");
            genCodeLine("            jjbeenHere[" + lexStates[i] + "] = true;");
            genCodeLine("         }");
          }

          if ((act = (Action)actions[i]) == null ||
              act.getActionTokens().size() == 0)
            break;

          genCode(  "         image.append");
          if (RStringLiteral.allImages[i] != null) {
            genCodeLine("(jjstrLiteralImages[" + i + "]);");
            genCodeLine("        lengthOfMatch = jjstrLiteralImages[" + i + "].length();");
          } else {
            genCodeLine("(input_stream->GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));");
          }

          printTokenSetup((Token)act.getActionTokens().get(0));
          ccol = 1;

          for (int j = 0; j < act.getActionTokens().size(); j++)
            printToken((Token)act.getActionTokens().get(j));
          genCodeLine("");

          break;
        }

        genCodeLine("         break;");
        genCodeLine("       }");
      }

    genCodeLine("      default :");
    genCodeLine("         break;");
    genCodeLine("   }");
    genCodeLine("}");
  }

  public void DumpMoreActions()
  {
    Action act;

    generateMethodDefHeader("void ", tokMgrClassName, "MoreLexicalActions()");
    genCodeLine("{");
    genCodeLine("   jjimageLen += (lengthOfMatch = jjmatchedPos + 1);");
    genCodeLine("   switch(jjmatchedKind)");
    genCodeLine("   {");

    Outer:
      for (int i = 0; i < maxOrdinal; i++)
      {
        if ((toMore[i / 64] & (1L << (i % 64))) == 0L)
          continue;

        for (;;)
        {
          if (((act = (Action)actions[i]) == null ||
              act.getActionTokens() == null ||
              act.getActionTokens().size() == 0) && !canLoop[lexStates[i]])
            continue Outer;

          genCodeLine("      case " + i + " : {");

          if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
          {
            genCodeLine("         if (jjmatchedPos == -1)");
            genCodeLine("         {");
            genCodeLine("            if (jjbeenHere[" + lexStates[i] + "] &&");
            genCodeLine("                jjemptyLineNo[" + lexStates[i] + "] == input_stream->getBeginLine() &&");
            genCodeLine("                jjemptyColNo[" + lexStates[i] + "] == input_stream->getBeginColumn())");
            genCodeLine("               errorHandler->lexicalError(JJString(\"(\"Error: Bailing out of infinite loop caused by repeated empty string matches \" + \"at line \" + input_stream->getBeginLine() + \", \" + \"column \" + input_stream->getBeginColumn() + \".\")), this);");
            genCodeLine("            jjemptyLineNo[" + lexStates[i] + "] = input_stream->getBeginLine();");
            genCodeLine("            jjemptyColNo[" + lexStates[i] + "] = input_stream->getBeginColumn();");
            genCodeLine("            jjbeenHere[" + lexStates[i] + "] = true;");
            genCodeLine("         }");
          }

          if ((act = (Action)actions[i]) == null ||
              act.getActionTokens().size() == 0)
          {
            break;
          }

          genCode(  "         image.append");

          if (RStringLiteral.allImages[i] != null)
            genCodeLine("(jjstrLiteralImages[" + i + "]);");
          else
            genCodeLine("(input_stream->GetSuffix(jjimageLen));");

          genCodeLine("         jjimageLen = 0;");
          printTokenSetup((Token)act.getActionTokens().get(0));
          ccol = 1;

          for (int j = 0; j < act.getActionTokens().size(); j++)
            printToken((Token)act.getActionTokens().get(j));
          genCodeLine("");

          break;
        }

        genCodeLine("         break;");
        genCodeLine("       }");
      }

    genCodeLine("      default :");
    genCodeLine("         break;");

    genCodeLine("   }");
    genCodeLine("}");
  }

  public void DumpTokenActions()
  {
    Action act;
    int i;

    generateMethodDefHeader("void ", tokMgrClassName, "TokenLexicalActions(Token *matchedToken)");
    genCodeLine("{");
    genCodeLine("   switch(jjmatchedKind)");
    genCodeLine("   {");

    Outer:
      for (i = 0; i < maxOrdinal; i++)
      {
        if ((toToken[i / 64] & (1L << (i % 64))) == 0L)
          continue;

        for (;;)
        {
          if (((act = (Action)actions[i]) == null ||
              act.getActionTokens() == null ||
              act.getActionTokens().size() == 0) && !canLoop[lexStates[i]])
            continue Outer;

          genCodeLine("      case " + i + " : {");

          if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
          {
            genCodeLine("         if (jjmatchedPos == -1)");
            genCodeLine("         {");
            genCodeLine("            if (jjbeenHere[" + lexStates[i] + "] &&");
            genCodeLine("                jjemptyLineNo[" + lexStates[i] + "] == input_stream->getBeginLine() &&");
            genCodeLine("                jjemptyColNo[" + lexStates[i] + "] == input_stream->getBeginColumn())");
            genCodeLine("               errorHandler->lexicalError(JJString(\"Error: Bailing out of infinite loop caused by repeated empty string matches " + "at line \" + input_stream->getBeginLine() + \", " + "column \" + input_stream->getBeginColumn() + \".\"), this);");
            genCodeLine("            jjemptyLineNo[" + lexStates[i] + "] = input_stream->getBeginLine();");
            genCodeLine("            jjemptyColNo[" + lexStates[i] + "] = input_stream->getBeginColumn();");
            genCodeLine("            jjbeenHere[" + lexStates[i] + "] = true;");
            genCodeLine("         }");
          }

          if ((act = (Action)actions[i]) == null ||
              act.getActionTokens().size() == 0)
            break;

          if (i == 0)
          {
            genCodeLine("      image.setLength(0);"); // For EOF no image is there
          }
          else
          {
            genCode(  "        image.append");

            if (RStringLiteral.allImages[i] != null) {
              genCodeLine("(jjstrLiteralImages[" + i + "]);");
              genCodeLine("        lengthOfMatch = jjstrLiteralImages[" + i + "].length();");
            } else {
              genCodeLine("(input_stream->GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));");
            }
          }

          printTokenSetup((Token)act.getActionTokens().get(0));
          ccol = 1;

          for (int j = 0; j < act.getActionTokens().size(); j++)
            printToken((Token)act.getActionTokens().get(j));
          genCodeLine("");

          break;
        }

        genCodeLine("         break;");
        genCodeLine("       }");
      }

    genCodeLine("      default :");
    genCodeLine("         break;");
    genCodeLine("   }");
    genCodeLine("}");
  }
}
