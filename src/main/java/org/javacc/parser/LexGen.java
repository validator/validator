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
public class LexGen extends CodeGenerator implements JavaCCParserConstants
{
  private static final String DUMP_STATIC_VAR_DECLARATIONS_TEMPLATE_RESOURCE_URL = "/templates/DumpStaticVarDeclarations.template";
  private static final String DUMP_DEBUG_METHODS_TEMPLATE_RESOURCE_URL = "/templates/DumpDebugMethods.template";
  private static final String BOILERPLATER_METHOD_RESOURCE_URL = "/templates/TokenManagerBoilerPlateMethods.template";

  public static String staticString;
  public static String tokMgrClassName;

  // Hashtable of vectors
  static Hashtable allTpsForState = new Hashtable();
  public static int lexStateIndex = 0;
  static int[] kinds;
  public static int maxOrdinal = 1;
  public static String lexStateSuffix;
  static String[] newLexState;
  public static int[] lexStates;
  public static boolean[] ignoreCase;
  public static Action[] actions;
  public static Hashtable initStates = new Hashtable();
  public static int stateSetSize;
  public static int totalNumStates;
  public static int maxLexStates;
  public static String[] lexStateName;
  static NfaState[] singlesToSkip;
  public static long[] toSkip;
  public static long[] toSpecial;
  public static long[] toMore;
  public static long[] toToken;
  public static int defaultLexState;
  public static RegularExpression[] rexprs;
  public static int[] maxLongsReqd;
  public static int[] initMatch;
  public static int[] canMatchAnyChar;
  public static boolean hasEmptyMatch;
  public static boolean[] canLoop;
  public static boolean[] stateHasActions;
  public static boolean hasLoop = false;
  public static boolean[] canReachOnMore;
  public static boolean[] hasNfa;
  public static boolean[] mixed;
  public static NfaState initialState;
  public static int curKind;
  static boolean hasSkipActions = false;
  static boolean hasMoreActions = false;
  static boolean hasTokenActions = false;
  static boolean hasSpecial = false;
  static boolean hasSkip = false;
  static boolean hasMore = false;
  public static RegularExpression curRE;
  public static boolean keepLineCol;
  public static String errorHandlingClass;
  public static TokenizerData tokenizerData;
  public static boolean generateDataOnly;

  void PrintClassHead()
  {
    int i, j;
    boolean bHasImport = false;

    List<String> tn = new ArrayList<String>(toolNames);
    tn.add(toolName);
     // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
    genCodeLine("/* " + getIdString(tn, tokMgrClassName + getFileExtension(Options.getOutputLanguage())) + " */");

    int l = 0, kind;
    i = 1;
    for (;;)
    {
      if (cu_to_insertion_point_1.size() <= l)
        break;

      kind = ((Token)cu_to_insertion_point_1.get(l)).kind;
      if(kind == PACKAGE || kind == IMPORT) {
        if (kind == IMPORT)
          bHasImport = true;
        for (; i < cu_to_insertion_point_1.size(); i++) {
          kind = ((Token)cu_to_insertion_point_1.get(i)).kind;
          if (kind == SEMICOLON ||
              kind == ABSTRACT ||
              kind == FINAL ||
              kind == PUBLIC ||
              kind == CLASS ||
              kind == INTERFACE)
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
    }

    genCodeLine("");
    genCodeLine("/** Token Manager. */");

    if (bHasImport && Options.getGenerateAnnotations()) {
      genCodeLine("@SuppressWarnings (\"unused\")");
    }
    if(Options.getSupportClassVisibilityPublic()) {
      //genModifier("public ");
      genModifier("public ");
    }
    //genCodeLine("class " + tokMgrClassName + " implements " +
        //cu_name + "Constants");
    //String superClass = Options.stringValue(Options.USEROPTION__TOKEN_MANAGER_SUPER_CLASS);
    genClassStart(null, tokMgrClassName, new String[]{}, new String[]{cu_name + "Constants"});
    //genCodeLine("{"); // }

    if (token_mgr_decls != null && token_mgr_decls.size() > 0)
    {
      Token t = (Token)token_mgr_decls.get(0);
      boolean commonTokenActionSeen = false;
      boolean commonTokenActionNeeded = Options.getCommonTokenAction();

      printTokenSetup((Token)token_mgr_decls.get(0));
      ccol = 1;

      for (j = 0; j < token_mgr_decls.size(); j++)
      {
        t = (Token)token_mgr_decls.get(j);
        if (t.kind == IDENTIFIER &&
            commonTokenActionNeeded &&
            !commonTokenActionSeen)
          commonTokenActionSeen = t.image.equals("CommonTokenAction");

        printToken(t);
      }

      genCodeLine("");
      if (commonTokenActionNeeded && !commonTokenActionSeen)
        JavaCCErrors.warning("You have the COMMON_TOKEN_ACTION option set. " +
            "But it appears you have not defined the method :\n"+
            "      " + staticString + "void CommonTokenAction(Token t)\n" +
        "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");

    }
    else if (Options.getCommonTokenAction())
    {
      JavaCCErrors.warning("You have the COMMON_TOKEN_ACTION option set. " +
          "But you have not defined the method :\n"+
          "      " + staticString + "void CommonTokenAction(Token t)\n" +
      "in your TOKEN_MGR_DECLS. The generated token manager will not compile.");
    }

    genCodeLine("");
    genCodeLine("  /** Debug output. */");
    genCodeLine("  public " + staticString + " java.io.PrintStream debugStream = System.out;");
    genCodeLine("  /** Set debug output. */");
    genCodeLine("  public " + staticString + " void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }");

    if(Options.getTokenManagerUsesParser()){
      genCodeLine("");
      genCodeLine("  public " + cu_name + " parser = null;");
    }
  }

  @SuppressWarnings("unchecked")
  protected void writeTemplate(String name, Object... additionalOptions) throws IOException
  {
    Map<String, Object> options = new HashMap<String, Object>(Options.getOptions());

    options.put("maxOrdinal", Integer.valueOf(maxOrdinal));
    options.put("maxLexStates", Integer.valueOf(maxLexStates));
    options.put("hasEmptyMatch", Boolean.valueOf(hasEmptyMatch));
    options.put("hasSkip", Boolean.valueOf(hasSkip));
    options.put("hasMore", Boolean.valueOf(hasMore));
    options.put("hasSpecial", Boolean.valueOf(hasSpecial));
    options.put("hasMoreActions", Boolean.valueOf(hasMoreActions));
    options.put("hasSkipActions", Boolean.valueOf(hasSkipActions));
    options.put("hasTokenActions", Boolean.valueOf(hasTokenActions));
    options.put("stateSetSize", stateSetSize);
    options.put("hasActions", hasMoreActions || hasSkipActions || hasTokenActions);
    options.put("tokMgrClassName", tokMgrClassName);
    int x = 0;
    for (int l : maxLongsReqd) x = Math.max(x, l);
    options.put("maxLongs", x);
    options.put("cu_name", cu_name);

    // options.put("", .valueOf(maxOrdinal));


    for (int i = 0; i < additionalOptions.length; i++)
    {
      Object o = additionalOptions[i];

      if (o instanceof Map<?,?>)
      {
        options.putAll((Map<String,Object>) o);
      }
      else
      {
        if (i == additionalOptions.length - 1)
          throw new IllegalArgumentException("Must supply pairs of [name value] args");

        options.put((String) o, additionalOptions[i+1]);
        i++;
      }
    }

    OutputFileGenerator gen = new OutputFileGenerator(name, options);
    StringWriter sw = new StringWriter();
    gen.generate(new PrintWriter(sw));
    sw.close();
    genCode(sw.toString());
  }

  void DumpDebugMethods() throws IOException
  {
    writeTemplate(DUMP_DEBUG_METHODS_TEMPLATE_RESOURCE_URL);
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
        if ((tps = (List)allTpsForState.get(tp.lexStates[i])) == null)
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

    final String codeGeneratorClass = Options.getTokenManagerCodeGenerator();
    keepLineCol = Options.getKeepLineColumn();
    errorHandlingClass = Options.getTokenMgrErrorClass();
    List choices = new ArrayList();
    Enumeration e;
    TokenProduction tp;
    int i, j;

    staticString = (Options.getStatic() ? "static " : "");
    tokMgrClassName = cu_name + "TokenManager";

    if (!generateDataOnly && codeGeneratorClass == null) PrintClassHead();
    BuildLexStatesTable();

    e = allTpsForState.keys();

    boolean ignoring = false;

    while (e.hasMoreElements())
    {
      int startState = -1;
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

          if (!Options.getNoDfa() && curRE instanceof RStringLiteral &&
              !((RStringLiteral)curRE).image.equals(""))
          {
            ((RStringLiteral)curRE).GenerateDfa(this, curRE.ordinal);
            if (i != 0 && !mixed[lexStateIndex] && ignoring != ignore) {
              mixed[lexStateIndex] = true;
            }
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
        startState = initialState.GenerateInitMoves(this);
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

      if (generateDataOnly || codeGeneratorClass != null) {
        RStringLiteral.UpdateStringLiteralData(totalNumStates, lexStateIndex);
        NfaState.UpdateNfaData(totalNumStates, startState, lexStateIndex,
                               canMatchAnyChar[lexStateIndex]);
      } else {
        RStringLiteral.DumpDfaCode(this);
        if (hasNfa[lexStateIndex]) {
          NfaState.DumpMoveNfa(this);
        }
      }
      totalNumStates += NfaState.generatedStates;
      if (stateSetSize < NfaState.generatedStates)
        stateSetSize = NfaState.generatedStates;
    }

    for (i = 0; i < choices.size(); i++)
      ((RChoice)choices.get(i)).CheckUnmatchability();

    CheckEmptyStringMatch();

    if (generateDataOnly || codeGeneratorClass != null) {
      tokenizerData.setParserName(cu_name);
      NfaState.BuildTokenizerData(tokenizerData);
      RStringLiteral.BuildTokenizerData(tokenizerData);
      int[] newLexStateIndices = new int[maxOrdinal];
      StringBuilder tokenMgrDecls = new StringBuilder();
      if (token_mgr_decls != null && token_mgr_decls.size() > 0) {
        Token t = (Token)token_mgr_decls.get(0);
        for (j = 0; j < token_mgr_decls.size(); j++) {
          tokenMgrDecls.append(((Token)token_mgr_decls.get(j)).image + " ");
        }
      }
      tokenizerData.setDecls(tokenMgrDecls.toString());
      Map<Integer, String> actionStrings = new HashMap<Integer, String>();
      for (i = 0; i < maxOrdinal; i++) {
        if (newLexState[i] == null) {
          newLexStateIndices[i] = -1;
        } else {
          newLexStateIndices[i] = GetIndex(newLexState[i]);
        }
        // For java, we have this but for other languages, eventually we will
        // simply have a string.
        Action act = actions[i];
        if (act == null) continue;
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < act.getActionTokens().size(); k++) {
          sb.append(((Token)act.getActionTokens().get(k)).image);
          sb.append(" ");
        }
        actionStrings.put(i, sb.toString());
      }
      tokenizerData.setDefaultLexState(defaultLexState);
      tokenizerData.setLexStateNames(lexStateName);
      tokenizerData.updateMatchInfo(
          actionStrings, newLexStateIndices,
          toSkip, toSpecial, toMore, toToken);
      if (generateDataOnly) return;
      Class<TokenManagerCodeGenerator> codeGenClazz;
      TokenManagerCodeGenerator gen;
      try {
        codeGenClazz = (Class<TokenManagerCodeGenerator>)Class.forName(codeGeneratorClass);
        gen = codeGenClazz.newInstance();
      } catch(Exception ee) {
        JavaCCErrors.semantic_error(
            "Could not load the token manager code generator class: " +
            codeGeneratorClass + "\nError: " + ee.getMessage());
        return;
      }
      gen.generateCode(tokenizerData);
      gen.finish(tokenizerData);
      return;
    }

    RStringLiteral.DumpStrLiteralImages(this);
    DumpFillToken();
    NfaState.DumpStateSets(this);
    NfaState.DumpNonAsciiMoveMethods(this);
    DumpGetNextToken();

    if (Options.getDebugTokenManager())
    {
      NfaState.DumpStatesForKind(this);
      DumpDebugMethods();
    }

    if (hasLoop)
    {
      genCodeLine(staticString + "int[] jjemptyLineNo = new int[" + maxLexStates + "];");
      genCodeLine(staticString + "int[] jjemptyColNo = new int[" + maxLexStates + "];");
      genCodeLine(staticString + "" + Options.getBooleanType() + "[] jjbeenHere = new " + Options.getBooleanType() + "[" + maxLexStates + "];");
    }

    DumpSkipActions();
    DumpMoreActions();
    DumpTokenActions();

    NfaState.PrintBoilerPlate(this);

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

    writeTemplate(BOILERPLATER_METHOD_RESOURCE_URL,
      "charStreamName", charStreamName,
      "lexStateNameLength", lexStateName.length,
      "defaultLexState", defaultLexState,
      "noDfa", Options.getNoDfa(),
      "generatedStates", totalNumStates);

    DumpStaticVarDeclarations(charStreamName);
    genCodeLine(/*{*/ "}");

    // TODO :: CBA --  Require Unification of output language specific processing into a single Enum class
    String fileName = Options.getOutputDirectory() + File.separator +
                      tokMgrClassName +
                      getFileExtension(Options.getOutputLanguage());

    if (Options.getBuildParser()) {
      saveOutput(fileName);
    }
  }

  static void CheckEmptyStringMatch()
  {
    int i, j, k, len;
    boolean[] seen = new boolean[maxLexStates];
    boolean[] done = new boolean[maxLexStates];
    String cycle;
    String reList;

    Outer:
      for (i = 0; i < maxLexStates; i++)
      {
        if (done[i] || initMatch[i] == 0 || initMatch[i] == Integer.MAX_VALUE ||
            canMatchAnyChar[i] != -1)
          continue;

        done[i] = true;
        len = 0;
        cycle = "";
        reList = "";

        for (k = 0; k < maxLexStates; k++)
          seen[k] = false;

        j = i;
        seen[i] = true;
        cycle += lexStateName[j] + "-->";
        while (newLexState[initMatch[j]] != null)
        {
          cycle += newLexState[initMatch[j]];
          if (seen[j = GetIndex(newLexState[initMatch[j]])])
            break;

          cycle += "-->";
          done[j] = true;
          seen[j] = true;
          if (initMatch[j] == 0 || initMatch[j] == Integer.MAX_VALUE ||
              canMatchAnyChar[j] != -1)
            continue Outer;
          if (len != 0)
            reList += "; ";
          reList += "line " + rexprs[initMatch[j]].getLine() + ", column " +
          rexprs[initMatch[j]].getColumn();
          len++;
        }

        if (newLexState[initMatch[j]] == null)
          cycle += lexStateName[lexStates[initMatch[j]]];

        for (k = 0; k < maxLexStates; k++)
          canLoop[k] |= seen[k];

        hasLoop = true;
        if (len == 0)
          JavaCCErrors.warning(rexprs[initMatch[i]],
              "Regular expression" + ((rexprs[initMatch[i]].label.equals(""))
                  ? "" : (" for " + rexprs[initMatch[i]].label)) +
                  " can be matched by the empty string (\"\") in lexical state " +
                  lexStateName[i] + ". This can result in an endless loop of " +
          "empty string matches.");
        else
        {
          JavaCCErrors.warning(rexprs[initMatch[i]],
              "Regular expression" + ((rexprs[initMatch[i]].label.equals(""))
                  ? "" : (" for " + rexprs[initMatch[i]].label)) +
                  " can be matched by the empty string (\"\") in lexical state " +
                  lexStateName[i] + ". This regular expression along with the " +
                  "regular expressions at " + reList + " forms the cycle \n   " +
                  cycle + "\ncontaining regular expressions with empty matches." +
          " This can result in an endless loop of empty string matches.");
        }
      }
  }

  private void DumpStaticVarDeclarations(String charStreamName) throws IOException
  {
    int i;

    genCodeLine("");
    genCodeLine("/** Lexer state names. */");
    genCodeLine("public static final String[] lexStateNames = {");
    for (i = 0; i < maxLexStates; i++)
      genCodeLine("   \"" + lexStateName[i] + "\",");
    genCodeLine("};");

    {
      genCodeLine("");
      genCodeLine("/** Lex State array. */");
      genCode("public static final int[] jjnewLexState = {");

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

    {
      // Bit vector for TOKEN
      genCode("static final long[] jjtoToken = {");
      for (i = 0; i < maxOrdinal / 64 + 1; i++)
      {
        if (i % 4 == 0)
          genCode("\n   ");
        genCode("0x" + Long.toHexString(toToken[i]) + "L, ");
      }
      genCodeLine("\n};");
    }

    {
      // Bit vector for SKIP
      genCode("static final long[] jjtoSkip = {");
      for (i = 0; i < maxOrdinal / 64 + 1; i++)
      {
        if (i % 4 == 0)
          genCode("\n   ");
        genCode("0x" + Long.toHexString(toSkip[i]) + "L, ");
      }
      genCodeLine("\n};");
    }

    {
      // Bit vector for SPECIAL
      genCode("static final long[] jjtoSpecial = {");
      for (i = 0; i < maxOrdinal / 64 + 1; i++)
      {
        if (i % 4 == 0)
          genCode("\n   ");
        genCode("0x" + Long.toHexString(toSpecial[i]) + "L, ");
      }
      genCodeLine("\n};");
    }

    {
      // Bit vector for MORE
      genCode("static final long[] jjtoMore = {");
      for (i = 0; i < maxOrdinal / 64 + 1; i++)
      {
        if (i % 4 == 0)
          genCode("\n   ");
        genCode("0x" + Long.toHexString(toMore[i]) + "L, ");
      }
      genCodeLine("\n};");
    }

    writeTemplate(DUMP_STATIC_VAR_DECLARATIONS_TEMPLATE_RESOURCE_URL,
      "charStreamName", charStreamName,
      "protected", isJavaLanguage() ? "protected" : "",
      "private", isJavaLanguage() ? "private" : "",
      "final", isJavaLanguage() ? "final" : "",
      "lexStateNameLength", lexStateName.length);
  }

  // Assumes l != 0L
  static char MaxChar(long l)
  {
    for (int i = 64; i-- > 0; )
      if ((l & (1L << i)) != 0L)
        return (char)i;

    return 0xffff;
  }

  void DumpFillToken()
  {
    final double tokenVersion = JavaFiles.getVersion("Token.java");
    final boolean hasBinaryNewToken = tokenVersion > 4.09;

    genCodeLine(staticString + "protected Token jjFillToken()");
    genCodeLine("{");
    genCodeLine("   final Token t;");
    genCodeLine("   final String curTokenImage;");
    if (keepLineCol)
    {
      genCodeLine("   final int beginLine;");
      genCodeLine("   final int endLine;");
      genCodeLine("   final int beginColumn;");
      genCodeLine("   final int endColumn;");
    }

    if (hasEmptyMatch)
    {
      genCodeLine("   if (jjmatchedPos < 0)");
      genCodeLine("   {");
      genCodeLine("      if (image == null)");
      genCodeLine("         curTokenImage = \"\";");
      genCodeLine("      else");
      genCodeLine("         curTokenImage = image.toString();");

      if (keepLineCol)
      {
        genCodeLine("      beginLine = endLine = input_stream.getEndLine();");
        genCodeLine("      beginColumn = endColumn = input_stream.getEndColumn();");
      }

      genCodeLine("   }");
      genCodeLine("   else");
      genCodeLine("   {");
      genCodeLine("      String im = jjstrLiteralImages[jjmatchedKind];");
      genCodeLine("      curTokenImage = (im == null) ? input_stream.GetImage() : im;");

      if (keepLineCol)
      {
        genCodeLine("      beginLine = input_stream.getBeginLine();");
        genCodeLine("      beginColumn = input_stream.getBeginColumn();");
        genCodeLine("      endLine = input_stream.getEndLine();");
        genCodeLine("      endColumn = input_stream.getEndColumn();");
      }

      genCodeLine("   }");
    }
    else
    {
      genCodeLine("   String im = jjstrLiteralImages[jjmatchedKind];");
      genCodeLine("   curTokenImage = (im == null) ? input_stream.GetImage() : im;");
      if (keepLineCol)
      {
        genCodeLine("   beginLine = input_stream.getBeginLine();");
        genCodeLine("   beginColumn = input_stream.getBeginColumn();");
        genCodeLine("   endLine = input_stream.getEndLine();");
        genCodeLine("   endColumn = input_stream.getEndColumn();");
      }
    }

    if (Options.getTokenFactory().length() > 0) {
      genCodeLine("   t = " + Options.getTokenFactory() + ".newToken(jjmatchedKind, curTokenImage);");
    } else if (hasBinaryNewToken)
    {
      genCodeLine("   t = Token.newToken(jjmatchedKind, curTokenImage);");
    }
    else
    {
      genCodeLine("   t = Token.newToken(jjmatchedKind);");
      genCodeLine("   t.kind = jjmatchedKind;");
      genCodeLine("   t.image = curTokenImage;");
    }

    if (keepLineCol) {
      genCodeLine("");
      genCodeLine("   t.beginLine = beginLine;");
      genCodeLine("   t.endLine = endLine;");
      genCodeLine("   t.beginColumn = beginColumn;");
      genCodeLine("   t.endColumn = endColumn;");
    }

    genCodeLine("");
    genCodeLine("   return t;");
    genCodeLine("}");
  }

  void DumpGetNextToken()
  {
    int i;

    genCodeLine("");
    genCodeLine(staticString + "int curLexState = " + defaultLexState + ";");
    genCodeLine(staticString + "int defaultLexState = " + defaultLexState + ";");
    genCodeLine(staticString + "int jjnewStateCnt;");
    genCodeLine(staticString + "int jjround;");
    genCodeLine(staticString + "int jjmatchedPos;");
    genCodeLine(staticString + "int jjmatchedKind;");
    genCodeLine("");
    genCodeLine("/** Get the next Token. */");
    genCodeLine("public " + staticString + "Token getNextToken()" +
    " ");
    genCodeLine("{");
    if (hasSpecial) {
      genCodeLine("  Token specialToken = null;");
    }
    genCodeLine("  Token matchedToken;");
    genCodeLine("  int curPos = 0;");
    genCodeLine("");
    genCodeLine("  EOFLoop :\n  for (;;)");
    genCodeLine("  {");
    genCodeLine("   try");
    genCodeLine("   {");
    genCodeLine("      curChar = input_stream.BeginToken();");
    genCodeLine("   }");
    genCodeLine("   catch(Exception e)");
    genCodeLine("   {");

    if (Options.getDebugTokenManager())
      genCodeLine("      debugStream.println(\"Returning the <EOF> token.\\n\");");

    genCodeLine("      jjmatchedKind = 0;");
    genCodeLine("      jjmatchedPos = -1;");
    genCodeLine("      matchedToken = jjFillToken();");

    if (hasSpecial)
      genCodeLine("      matchedToken.specialToken = specialToken;");

    if (nextStateForEof != null || actForEof != null)
      genCodeLine("      TokenLexicalActions(matchedToken);");

    if (Options.getCommonTokenAction())
      genCodeLine("      CommonTokenAction(matchedToken);");

    genCodeLine("      return matchedToken;");
    genCodeLine("   }");

    if (hasMoreActions || hasSkipActions || hasTokenActions)
    {
      genCodeLine("   image = jjimage;");
      genCodeLine("   image.setLength(0);");
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
        genCodeLine(prefix + "try { input_stream.backup(0);");
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

        if (Options.getDebugTokenManager())
        {
          genCodeLine(prefix + "{");
          genCodeLine("      debugStream.println(" +
              (maxLexStates > 1 ?
                  "\"<\" + lexStateNames[curLexState] + \">\" + " : "") +
                  "\"Skipping character : \" + " +
          errorHandlingClass+".addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \")\");");
        }
        genCodeLine(prefix + "      curChar = input_stream.BeginToken();");

        if (Options.getDebugTokenManager())
          genCodeLine(prefix + "}");

        genCodeLine(prefix + "}");
        genCodeLine(prefix + "catch (java.io.IOException e1) { continue EOFLoop; }");
      }

      if (initMatch[i] != Integer.MAX_VALUE && initMatch[i] != 0)
      {
        if (Options.getDebugTokenManager())
          genCodeLine("      debugStream.println(\"   Matched the empty string as \" + tokenImage[" +
              initMatch[i] + "] + \" token.\");");

        genCodeLine(prefix + "jjmatchedKind = " + initMatch[i] + ";");
        genCodeLine(prefix + "jjmatchedPos = -1;");
        genCodeLine(prefix + "curPos = 0;");
      }
      else
      {
        genCodeLine(prefix + "jjmatchedKind = 0x" + Integer.toHexString(Integer.MAX_VALUE) + ";");
        genCodeLine(prefix + "jjmatchedPos = 0;");
      }

      if (Options.getDebugTokenManager())
        genCodeLine("      debugStream.println(" +
            (maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") +
            "\"Current character : \" + " +
            errorHandlingClass+".addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") " +
        "at line \" + input_stream.getEndLine() + \" column \" + input_stream.getEndColumn());");

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

        if (Options.getDebugTokenManager())
          genCodeLine("           debugStream.println(\"   Current character matched as a \" + tokenImage[" +
              canMatchAnyChar[i] + "] + \" token.\");");
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

      if (Options.getDebugTokenManager())
      {
        genCodeLine(prefix + "      {");
        genCodeLine(prefix + "         debugStream.println(" +
        "\"   Putting back \" + (curPos - jjmatchedPos - 1) + \" characters into the input stream.\");");
      }

      genCodeLine(prefix + "         input_stream.backup(curPos - jjmatchedPos - 1);");

      if (Options.getDebugTokenManager())
        genCodeLine(prefix + "      }");

      if (Options.getDebugTokenManager())
      {
        if (Options.getJavaUnicodeEscape() ||
            Options.getUserCharStream())
          genCodeLine("    debugStream.println(" +
              "\"****** FOUND A \" + tokenImage[jjmatchedKind] + \" MATCH " +
              "(\" + "+errorHandlingClass+".addEscapes(new String(input_stream.GetSuffix(jjmatchedPos + 1))) + " +
          "\") ******\\n\");");
        else
          genCodeLine("    debugStream.println(" +
              "\"****** FOUND A \" + tokenImage[jjmatchedKind] + \" MATCH " +
              "(\" + "+errorHandlingClass+".addEscapes(new String(input_stream.GetSuffix(jjmatchedPos + 1))) + " +
          "\") ******\\n\");");
      }

      if (hasSkip || hasMore || hasSpecial)
      {
        genCodeLine(prefix + "      if ((jjtoToken[jjmatchedKind >> 6] & " +
        "(1L << (jjmatchedKind & 077))) != 0L)");
        genCodeLine(prefix + "      {");
      }

      genCodeLine(prefix + "         matchedToken = jjFillToken();");

      if (hasSpecial)
        genCodeLine(prefix + "         matchedToken.specialToken = specialToken;");

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

            genCodeLine(prefix + "            if (specialToken == null)");
            genCodeLine(prefix + "               specialToken = matchedToken;");
            genCodeLine(prefix + "            else");
            genCodeLine(prefix + "            {");
            genCodeLine(prefix + "               matchedToken.specialToken = specialToken;");
            genCodeLine(prefix + "               specialToken = (specialToken.next = matchedToken);");
            genCodeLine(prefix + "            }");

            if (hasSkipActions)
              genCodeLine(prefix + "            SkipLexicalActions(matchedToken);");

            genCodeLine(prefix + "         }");

            if (hasSkipActions)
            {
              genCodeLine(prefix + "         else");
              genCodeLine(prefix + "            SkipLexicalActions(null);");
            }
          }
          else if (hasSkipActions)
            genCodeLine(prefix + "         SkipLexicalActions(null);");

          if (maxLexStates > 1)
          {
            genCodeLine("         if (jjnewLexState[jjmatchedKind] != -1)");
            genCodeLine(prefix + "         curLexState = jjnewLexState[jjmatchedKind];");
          }

          genCodeLine(prefix + "         continue EOFLoop;");
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

          genCodeLine(prefix + "      try {");
          genCodeLine(prefix + "         curChar = input_stream.readChar();");

          if (Options.getDebugTokenManager())
            genCodeLine("   debugStream.println(" +
                (maxLexStates > 1 ? "\"<\" + lexStateNames[curLexState] + \">\" + " : "") +
                "\"Current character : \" + " +
                ""+errorHandlingClass+".addEscapes(String.valueOf(curChar)) + \" (\" + (int)curChar + \") " +
            "at line \" + input_stream.getEndLine() + \" column \" + input_stream.getEndColumn());");
          genCodeLine(prefix + "         continue;");
          genCodeLine(prefix + "      }");
          genCodeLine(prefix + "      catch (java.io.IOException e1) { }");
        }
      }

      genCodeLine(prefix + "   }");
      genCodeLine(prefix + "   int error_line = input_stream.getEndLine();");
      genCodeLine(prefix + "   int error_column = input_stream.getEndColumn();");
      genCodeLine(prefix + "   String error_after = null;");
      genCodeLine(prefix + "   " + Options.getBooleanType() + " EOFSeen = false;");
      genCodeLine(prefix + "   try { input_stream.readChar(); input_stream.backup(1); }");
      genCodeLine(prefix + "   catch (java.io.IOException e1) {");
      genCodeLine(prefix + "      EOFSeen = true;");
      genCodeLine(prefix + "      error_after = curPos <= 1 ? \"\" : input_stream.GetImage();");
      genCodeLine(prefix + "      if (curChar == '\\n' || curChar == '\\r') {");
      genCodeLine(prefix + "         error_line++;");
      genCodeLine(prefix + "         error_column = 0;");
      genCodeLine(prefix + "      }");
      genCodeLine(prefix + "      else");
      genCodeLine(prefix + "         error_column++;");
      genCodeLine(prefix + "   }");
      genCodeLine(prefix + "   if (!EOFSeen) {");
      genCodeLine(prefix + "      input_stream.backup(1);");
      genCodeLine(prefix + "      error_after = curPos <= 1 ? \"\" : input_stream.GetImage();");
      genCodeLine(prefix + "   }");
      genCodeLine(prefix + "   throw new "+errorHandlingClass+"(" +
      "EOFSeen, curLexState, error_line, error_column, error_after, curChar, "+errorHandlingClass+".LEXICAL_ERROR);");
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

    genCodeLine(staticString + "void SkipLexicalActions(Token matchedToken)");
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

          genCodeLine("      case " + i + " :");

          if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
          {
            genCodeLine("         if (jjmatchedPos == -1)");
            genCodeLine("         {");
            genCodeLine("            if (jjbeenHere[" + lexStates[i] + "] &&");
            genCodeLine("                jjemptyLineNo[" + lexStates[i] + "] == input_stream.getBeginLine() &&");
            genCodeLine("                jjemptyColNo[" + lexStates[i] + "] == input_stream.getBeginColumn())");
            genCodeLine("               throw new "+errorHandlingClass+"(" +
                "(\"Error: Bailing out of infinite loop caused by repeated empty string matches " +
                "at line \" + input_stream.getBeginLine() + \", " +
            "column \" + input_stream.getBeginColumn() + \".\"), "+errorHandlingClass+".LOOP_DETECTED);");
            genCodeLine("            jjemptyLineNo[" + lexStates[i] + "] = input_stream.getBeginLine();");
            genCodeLine("            jjemptyColNo[" + lexStates[i] + "] = input_stream.getBeginColumn();");
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
            genCodeLine("(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));");
          }

          printTokenSetup((Token)act.getActionTokens().get(0));
          ccol = 1;

          for (int j = 0; j < act.getActionTokens().size(); j++)
            printToken((Token)act.getActionTokens().get(j));
          genCodeLine("");

          break;
        }

        genCodeLine("         break;");
      }

    genCodeLine("      default :");
    genCodeLine("         break;");
    genCodeLine("   }");
    genCodeLine("}");
  }

  public void DumpMoreActions()
  {
    Action act;

    genCodeLine(staticString + "void MoreLexicalActions()");
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

          genCodeLine("      case " + i + " :");

          if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
          {
            genCodeLine("         if (jjmatchedPos == -1)");
            genCodeLine("         {");
            genCodeLine("            if (jjbeenHere[" + lexStates[i] + "] &&");
            genCodeLine("                jjemptyLineNo[" + lexStates[i] + "] == input_stream.getBeginLine() &&");
            genCodeLine("                jjemptyColNo[" + lexStates[i] + "] == input_stream.getBeginColumn())");
            genCodeLine("               throw new "+errorHandlingClass+"(" +
                "(\"Error: Bailing out of infinite loop caused by repeated empty string matches " +
                "at line \" + input_stream.getBeginLine() + \", " +
            "column \" + input_stream.getBeginColumn() + \".\"), "+errorHandlingClass+".LOOP_DETECTED);");
            genCodeLine("            jjemptyLineNo[" + lexStates[i] + "] = input_stream.getBeginLine();");
            genCodeLine("            jjemptyColNo[" + lexStates[i] + "] = input_stream.getBeginColumn();");
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
            genCodeLine("(input_stream.GetSuffix(jjimageLen));");

          genCodeLine("         jjimageLen = 0;");
          printTokenSetup((Token)act.getActionTokens().get(0));
          ccol = 1;

          for (int j = 0; j < act.getActionTokens().size(); j++)
            printToken((Token)act.getActionTokens().get(j));
          genCodeLine("");

          break;
        }

        genCodeLine("         break;");
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

    genCodeLine(staticString + "void TokenLexicalActions(Token matchedToken)");
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

          genCodeLine("      case " + i + " :");

          if (initMatch[lexStates[i]] == i && canLoop[lexStates[i]])
          {
            genCodeLine("         if (jjmatchedPos == -1)");
            genCodeLine("         {");
            genCodeLine("            if (jjbeenHere[" + lexStates[i] + "] &&");
            genCodeLine("                jjemptyLineNo[" + lexStates[i] + "] == input_stream.getBeginLine() &&");
            genCodeLine("                jjemptyColNo[" + lexStates[i] + "] == input_stream.getBeginColumn())");
            genCodeLine("               throw new "+errorHandlingClass+"(" +
                "(\"Error: Bailing out of infinite loop caused by repeated empty string matches " +
                "at line \" + input_stream.getBeginLine() + \", " +
            "column \" + input_stream.getBeginColumn() + \".\"), "+errorHandlingClass+".LOOP_DETECTED);");
            genCodeLine("            jjemptyLineNo[" + lexStates[i] + "] = input_stream.getBeginLine();");
            genCodeLine("            jjemptyColNo[" + lexStates[i] + "] = input_stream.getBeginColumn();");
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
              genCodeLine("(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));");
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
      }

    genCodeLine("      default :");
    genCodeLine("         break;");
    genCodeLine("   }");
    genCodeLine("}");
  }

  public static void reInit()
  {
    actions = null;
    allTpsForState = new Hashtable();
    canLoop = null;
    canMatchAnyChar = null;
    canReachOnMore = null;
    curKind = 0;
    curRE = null;
    defaultLexState = 0;
    errorHandlingClass = null;
    hasEmptyMatch = false;
    hasLoop = false;
    hasMore = false;
    hasMoreActions = false;
    hasNfa = null;
    hasSkip = false;
    hasSkipActions = false;
    hasSpecial = false;
    hasTokenActions = false;
    ignoreCase = null;
    initMatch = null;
    initStates = new Hashtable();
    initialState = null;
    keepLineCol = false;
    kinds = null;
    lexStateIndex = 0;
    lexStateName = null;
    lexStateSuffix = null;
    lexStates = null;
    maxLexStates = 0;
    maxLongsReqd = null;
    maxOrdinal = 1;
    mixed = null;
    newLexState = null;
    rexprs = null;
    singlesToSkip = null;
    stateHasActions = null;
    stateSetSize = 0;
    staticString = null;
    toMore = null;
    toSkip = null;
    toSpecial = null;
    toToken = null;
    tokMgrClassName = null;
    tokenizerData = new TokenizerData();
    generateDataOnly = false;
  }

}
