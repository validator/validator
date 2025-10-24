[Home](../index.md) > [Tutorials](index.md) > Examples

---

This section contains some examples to get you started using JavaCC and JJTree.

Once you have tried out and understood each of these examples, you should take a look at the [grammar repository](http://mindprod.com/jgloss/javacc.html), and more complex examples under the `examples/` directory.

But even with just these examples, you should be able to get started on reasonably complex grammars.

### <a name="toc"></a>Contents

- [**JavaCC Examples**](#javacc)
  * [Instructions](#javacc-instructions)
  * [Example1.jj](#javacc-example-1)
  * [Example2.jj](#javacc-example-2)
  * [Example3.jj](#javacc-example-3)
  * [NL_Xlator.jj](#javacc-nl-xlator)
  * [IdList.jj](#javacc-id-list)
- [**JJTree Examples**](#jjtree)
  * [Instructions](#jjtree-instructions)
  * [Example1.jjt](#jjtree-example-1)
  * [Example2.jjt](#jjtree-example-2)
  * [Example3.jjt](#jjtree-example-3)
  * [Example4.jjt](#jjtree-example-4)
- [**Example Grammars**](#examples)


## <a name="javacc"></a>JavaCC Examples

### <a name="javacc-instructions"></a>Instructions

---

The following instructions show you how to get started with JavaCC. The instructions below are with respect to Example1.jj, but you can build any parser using the same set of commands.

1. Run `javacc` on the grammar input file to generate a bunch of Java files that implement the parser and lexical analyzer (or token manager):

```java
javacc Example1.jj
```

2. Now compile the resulting Java programs:

```java
javac *.java
```

3. The parser is now ready to use. To run the parser, type:

```java
java Example1
```

The `Example1` parser and others in this directory are designed to take input from standard input. `Example1` recognizes matching braces followed by zero or more line terminators and then an end of file.

Examples of legal strings in this grammar are:

`{}`, `{% raw %}{{{{{}}}}}{% endraw %}` // ... etc

Examples of illegal strings are:

`{}{}`, `}{}}`, `{ }`, `{x}` // ... etc

Try typing various different inputs to `Example1`. Remember `<control-d>` may be used to indicate the end of file (this is on the UNIX platform).

#### Output

Here are some sample runs:

1. The parser processes the string `{% raw %}{{}}{% endraw %}` successfully.

```java
$ java Example1
{% raw %}{{}}{% endraw %}<return>
<control-d>
```

2. The parser tries to process the string `{x` but throws a `TokenMgrError`.

```
$ java Example1
{x<return>
Lexical error at line 1, column 2.  Encountered: "x"
TokenMgrError: Lexical error at line 1, column 2.  Encountered: "x" (120), after : ""
        at Example1TokenManager.getNextToken(Example1TokenManager.java:146)
        at Example1.getToken(Example1.java:140)
        at Example1.MatchedBraces(Example1.java:51)
        at Example1.Input(Example1.java:10)
        at Example1.main(Example1.java:6)
```

3. The parser tries to process the string `{}}` but throws a `ParseException`.

```
$ java Example1
{}}<return>
ParseException: Encountered "}" at line 1, column 3.
Was expecting one of:
    <EOF>
    "\n" ...
    "\r" ...
        at Example1.generateParseException(Example1.java:184)
        at Example1.jj_consume_token(Example1.java:126)
        at Example1.Input(Example1.java:32)
        at Example1.main(Example1.java:6)
```

<br>

### <a name="javacc-example-1"></a>Example1.jj

---

`Example1.jj` is a simple JavaCC grammar that recognizes a set of left braces followed by the same number of right braces and finally followed by zero or more line terminators and finally an end of file.

```java
options {
  LOOKAHEAD = 1;
  CHOICE_AMBIGUITY_CHECK = 2;
  OTHER_AMBIGUITY_CHECK = 1;
  STATIC = true;
  DEBUG_PARSER = false;
  DEBUG_LOOKAHEAD = false;
  DEBUG_TOKEN_MANAGER = false;
  ERROR_REPORTING = true;
  JAVA_UNICODE_ESCAPE = false;
  UNICODE_INPUT = false;
  IGNORE_CASE = false;
  USER_TOKEN_MANAGER = false;
  USER_CHAR_STREAM = false;
  BUILD_PARSER = true;
  BUILD_TOKEN_MANAGER = true;
  SANITY_CHECK = true;
  FORCE_LA_CHECK = false;
}

PARSER_BEGIN(Example1)

/**
 * Simple brace matcher.
 */
public class Example1 {

  /** Main entry point. */
  public static void main(String args[]) throws ParseException {
    Example1 parser = new Example1(System.in);
    parser.Input();
  }

}

PARSER_END(Example1)

/** Root production. */
void Input() :
{}
{
  MatchedBraces() ("\n"|"\r")* <EOF>
}

/** Brace matching production. */
void MatchedBraces() :
{}
{
  "{" [ MatchedBraces() ] "}"
}
```

This grammar file starts with settings for all the options offered by JavaCC. In this case the option settings are their default values. Hence these option settings were really not necessary. One could as well have completely omitted the options section, or omitted one or more of the individual option settings. The details of the individual options is described in the JavaCC [documentation](../documentation/grammar.md#options).

Following this is a Java compilation unit enclosed between `PARSER_BEGIN(name)` and `PARSER_END(name)`. This compilation unit can be of arbitrary complexity. The only constraint on this compilation unit is that it must define a class called `name` - the same as the arguments to `PARSER_BEGIN` and `PARSER_END`. This is the name that is used as the prefix for the Java files generated by the parser generator. The parser code that is generated is inserted immediately before the closing brace of the class called `name`.

In the above example, the class in which the parser is generated contains a main program. This main program creates an instance of the parser object (an object of type `Example1`) by using a constructor that takes one argument of type `java.io.InputStream` (`System.in` in this case).

The main program then makes a call to the non-terminal in the grammar that it would like to parse - `Input` in this case. All non-terminals have equal status in a JavaCC generated parser, and hence one may parse with respect to any grammar non-terminal.

Following this is a list of productions. In this example, there are two productions that define the non-terminals `Input` and `MatchedBraces` respectively. In JavaCC grammars, non-terminals are written and implemented (by JavaCC) as Java methods. When the non-terminal is used on the left-hand side of a production, it is considered to be declared and its syntax follows the Java syntax. On the right-hand side, its use is similar to a method call in Java.

Each production defines its left-hand side non-terminal followed by a colon. This is followed by a bunch of declarations and statements within braces (in both cases in the above example, there are no declarations and hence this appears as `{}`) which are generated as common declarations and statements into the generated method. This is then followed by a set of expansions also enclosed within braces.

Lexical tokens (regular expressions) in a JavaCC input grammar are either simple strings (`{`, `}`, `\n`, and `\r` in the above example), or a more complex regular expression. In our example above, there is one such regular expression `<EOF>` which is matched by the end of file. All complex regular expressions are enclosed within angular brackets.

The first production above says that the non-terminal `Input` expands to the non-terminal `MethodBraces` followed by zero or more line terminators (`\n` or `\r`) and then the end of file.

The second production above says that the non-terminal `MatchedBraces` expands to the token `{` followed by an optional nested expansion of `MatchedBraces` followed by the token `}`. Square brackets `[...]` in a JavaCC input file indicate that the `...` is optional.

`[...]` may also be written as `(...)?`. These two forms are equivalent. Other structures that may appear in expansions are:

```java
e1 | e2 | e3 | ... : A choice of e1, e2, e3, etc.
( e )+             : One or more occurrences of e
( e )*             : Zero or more occurrences of e
```

Note that these may be nested within each other, so we can have something like:

```java
(( e1 | e2 )* [ e3 ] ) | e4
```

To build this parser, simply run JavaCC on this file and compile the resulting Java files:

```java
javacc Example1.jj
javac *.java
```

Now you should be able to run the generated parser. Make sure that the current directory is in your `CLASSPATH` and type:

```java
java Example1
```

Now type a sequence of matching braces followed by a return and an end of file (`CTRL-D` on UNIX machines). If this is a problem on your machine, you can create a file and pipe it as input to the generated parser in this manner (piping also does not work on all machines - if this is a problem, just replace `System.in` in the grammar file with `new FileInputStream("testfile")` and place your input inside this file):

```java
java Example1 < myfile
```

Also try entering illegal sequences such as mismatched braces, spaces, and carriage returns between braces as well as other characters and take a look at the error messages produced by the parser.

<br>

### <a name="javacc-example-2"></a>Example2.jj

---

`Example2.jj` is a minor modification to `Example1.jj` to allow white space characters to be interspersed among the braces such that the following input will now be legal:

`{}{}`, `{ }`, `{\n}` // ... etc

```java
PARSER_BEGIN(Example2)

/**
 * Simple brace matcher.
 */
public class Example2 {

  /** Main entry point. */
  public static void main(String args[]) throws ParseException {
    Example2 parser = new Example2(System.in);
    parser.Input();
  }

}

PARSER_END(Example2)

SKIP :
{
  " "
| "\t"
| "\n"
| "\r"
}

/** Root production. */
void Input() :
{}
{
  MatchedBraces() <EOF>
}

/** Brace matching production. */
void MatchedBraces() :
{}
{
  "{" [ MatchedBraces() ] "}"
}
```

The first thing you will note is that we have omitted the options section. This does not change anything since the options in `Example1.jj` were all assigned their default values.

The other difference between this file and `Example1.jj` is that this file contains a lexical specification - the region that starts with `SKIP`. Within this region are 4 regular expressions - `space`, `tab`, `newline`, and `return`. This says that matches of these regular expressions are to be ignored (and not considered for parsing).

Hence whenever any of these 4 characters are encountered, they are just thrown away.

In addition to `SKIP`, JavaCC has three other lexical specification regions. These are:


`. TOKEN:`         This is used to specify lexical tokens (see next example)  
`. SPECIAL_TOKEN:` This is used to specify lexical tokens that are to be ignored during parsing.
                   In this sense, `SPECIAL_TOKEN` is the same as `SKIP`.
                   However, these tokens can be recovered within parser actions to be handled appropriately.  
`. MORE:`          This specifies a partial token.
                   A complete token is made up of a sequence of MORE's followed by a `TOKEN` or `SPECIAL_TOKEN`.

Please take a look at some of the more complex grammars such as the Java grammars for examples of usage of these lexical specification regions.

You may build `Example2` and invoke the generated parser with input from the keyboard as standard input.

You can also try generating the parser with the various debug options turned on and see what the output looks like. To do this type:

```java
javacc -debug_parser Example2.jj
javac Example2*.java
java Example2
```

Then type:

```java
javacc -debug_token_manager Example2.jj
javac Example2*.java
java Example2
```

Note that token manager debugging produces a lot of diagnostic information and it is typically used to look at debug traces a single token at a time.

<br>

### <a name="javacc-example-3"></a>Example3.jj

---

`Example3.jj` is the third and final version of our matching brace detector.

```java
PARSER_BEGIN(Example3)

/**
 * Simple brace matcher.
 */
public class Example3 {

  /** Main entry point. */
  public static void main(String args[]) throws ParseException {
    Example3 parser = new Example3(System.in);
    parser.Input();
  }

}

PARSER_END(Example3)

SKIP :
{
  " "
| "\t"
| "\n"
| "\r"
}

TOKEN :
{
  <LBRACE: "{">
| <RBRACE: "}">
}

/** Root production. */
void Input() :
{ int count; }
{
  count=MatchedBraces() <EOF>
  { System.out.println("The levels of nesting is " + count); }
}

/** Brace counting production. */
int MatchedBraces() :
{ int nested_count=0; }
{
  <LBRACE> [ nested_count=MatchedBraces() ] <RBRACE>
  { return ++nested_count; }
}
```

This example illustrates the use of the `TOKEN` region for specifying lexical tokens. In this case, `{` and `}` are defined as tokens and given names `LBRACE` and `RBRACE` respectively. These labels can then be used within angular brackets (as in the example) to refer to this token. Typically such token specifications are used for complex tokens such as identifiers and literals. Tokens that are simple strings are left as is (in the previous examples).

This example also illustrates the use of actions in the grammar productions. The actions inserted in this example count the number of matching braces. Note the use of the declaration region to declare variables `count` and `nested_count`. Also note how the non-terminal `MatchedBraces` returns its value as a function return value.

<br>

### <a name="javacc-nl-xlator"></a>NL_Xlator.jj

---

This example goes into the details of writing regular expressions in JavaCC grammar files. It also illustrates a slightly more complex set of actions that translate the expressions described by the grammar into English.

```java
PARSER_BEGIN(NL_Xlator)

/**
 * New line translator.
 */
public class NL_Xlator {

  /** Main entry point. */
  public static void main(String args[]) throws ParseException {
    NL_Xlator parser = new NL_Xlator(System.in);
    parser.ExpressionList();
  }

}

PARSER_END(NL_Xlator)

SKIP :
{
  " "
| "\t"
| "\n"
| "\r"
}

TOKEN :
{
  < ID: ["a"-"z","A"-"Z","_"] ( ["a"-"z","A"-"Z","_","0"-"9"] )* >
|
  < NUM: ( ["0"-"9"] )+ >
}

/** Top level production. */
void ExpressionList() :
{
	String s;
}
{
	{
	  System.out.println("Please type in an expression followed by a \";\" or ^D to quit:");
	  System.out.println("");
	}
  ( s=Expression() ";"
	{
	  System.out.println(s);
	  System.out.println("");
	  System.out.println("Please type in another expression followed by a \";\" or ^D to quit:");
	  System.out.println("");
	}
  )*
  <EOF>
}

/** An Expression. */
String Expression() :
{
	java.util.Vector termimage = new java.util.Vector();
	String s;
}
{
  s=Term()
	{
	  termimage.addElement(s);
	}
  ( "+" s=Term()
	{
	  termimage.addElement(s);
	}
  )*
	{
	  if (termimage.size() == 1) {
	    return (String)termimage.elementAt(0);
          } else {
            s = "the sum of " + (String)termimage.elementAt(0);
	    for (int i = 1; i < termimage.size()-1; i++) {
	      s += ", " + (String)termimage.elementAt(i);
	    }
	    if (termimage.size() > 2) {
	      s += ",";
	    }
	    s += " and " + (String)termimage.elementAt(termimage.size()-1);
            return s;
          }
	}
}

/** A Term. */
String Term() :
{
	java.util.Vector factorimage = new java.util.Vector();
	String s;
}
{
  s=Factor()
	{
	  factorimage.addElement(s);
	}
  ( "*" s=Factor()
	{
	  factorimage.addElement(s);
	}
  )*
	{
	  if (factorimage.size() == 1) {
	    return (String)factorimage.elementAt(0);
          } else {
            s = "the product of " + (String)factorimage.elementAt(0);
	    for (int i = 1; i < factorimage.size()-1; i++) {
	      s += ", " + (String)factorimage.elementAt(i);
	    }
	    if (factorimage.size() > 2) {
	      s += ",";
	    }
	    s += " and " + (String)factorimage.elementAt(factorimage.size()-1);
            return s;
          }
	}
}

/** A Factor. */
String Factor() :
{
	Token t;
	String s;
}
{
  t=<ID>
	{
	  return t.image;
	}
|
  t=<NUM>
	{
	  return t.image;
	}
|
  "(" s=Expression() ")"
	{
	  return s;
	}
}
```

The new concept in the above example is the use of more complex regular expressions. The regular expression:

```java
< ID: ["a"-"z","A"-"Z","_"] ( ["a"-"z","A"-"Z","_","0"-"9"] )* >
```

creates a new regular expression whose name is `ID`. This can be referred anywhere else in the grammar simply as `<ID>`. What follows in square brackets are a set of allowable characters - in this case it is any of the lower or upper case letters or the underscore. This is followed by `0` or more occurrences of any of the lower or upper case letters, digits, or the underscore.

Other constructs that may appear in regular expressions are:

```java
( ... )+	: One or more occurrences of ...
( ... )?	: An optional occurrence of ... (Note that in the case
            of lexical tokens, (...)? and [...] are not equivalent)
( r1 | r2 | ... ) : Any one of r1, r2, ...
```

A construct of the form `[...]` is a pattern that is matched by the characters specified in `...` . These characters can be individual characters or character ranges. A `~` before this construct is a pattern that matches any character not specified in `...`. Therefore:

```java
["a"-"z"] matches all lower case letters
~[] matches any character
~["\n","\r"] matches any character except the new line characters
```

When a regular expression is used in an expansion, it takes a value of type `Token`. This is generated into the generated parser directory as `Token.java`. In the above example, we have defined a variable of type `Token` and assigned the value of the regular expression to it.

<br>

### <a name="javacc-id-list"></a>IdList.jj

---
This example illustrates an important attribute of the `SKIP` specification.

```java
PARSER_BEGIN(IdList)

/**
 * ID lister.
 */
public class IdList {

  /** Main entry point. */
  public static void main(String args[]) throws ParseException {
    IdList parser = new IdList(System.in);
    parser.Input();
  }

}

PARSER_END(IdList)

SKIP : {
  " "
| "\t"
| "\n"
| "\r"
}

TOKEN : {
  < Id: ["a"-"z","A"-"Z"] ( ["a"-"z","A"-"Z","0"-"9"] )* >
}

/** Top level production. */
void Input() :
{}
{
  ( <Id> )+ <EOF>
}
```java


The main point to note is that the regular expressions in the `SKIP` specification are only ignored *between tokens* and not *within tokens*. This grammar accepts any sequence of identifiers with white space in between.

A legal input for this grammar is:

```java
abc xyz123 A B C \t\n aaa
```

This is because any number of the `SKIP` regular expressions are allowed in between consecutive `<Id>`'s. However, the following is not a legal input:

```java
xyz 123
```

This is because the space character after `xyz` is in the `SKIP`category and therefore causes one token to end and another to begin. This requires `123` to be a separate token and hence does not match the grammar.

If spaces were OK within `<Id>`'s, then all one has to do is to replace the definition of Id to:

```java
TOKEN : {
  < Id: ["a"-"z","A"-"Z"] ( (" ")* ["a"-"z","A"-"Z","0"-"9"] )* >
}
```

Note that having a space character within a `TOKEN` specification does not mean that the space character cannot be used in the `SKIP` specification. All this means is that any space character that appears in the context where it can be placed within an identifier will participate in the match for `<Id>`, whereas all other space characters will be ignored. The details of the matching algorithm are described in the JavaCC documentation.

As a corollary, one must define as tokens anything within which characters such as white space characters must not be present. In the above example, if `<Id>` was defined as a grammar production rather than a lexical token as shown below this paragraph, then `xyz 123` would have been recognized as a legitimate `<Id>` (wrongly).

```java
void Id() : {
}
{
  <["a"-"z","A"-"Z"]> ( <["a"-"z","A"-"Z","0"-"9"]> )*
}
```

Note that in the above definition of non-terminal `Id`, it is made up of a sequence of single character tokens (note the location of `<...>`s), and hence white space is allowed between these characters.

<br>

## <a name="jjtree"></a>JJTree Examples

### <a name="jjtree-instructions"></a>Instructions

---

This section gives instructions on how to run the JJTree examples and the output you can expect to see.

<br>

### <a name="jjtree-example-1"></a>Example1.jjt

---

This example is just the JavaCC grammar, with a little extra code in the parser's main method to call the dump method on the generated tree. It illustrates how the default behavior of JJTree will produce a tree of non-terminals.

```java
PARSER_BEGIN(Example1)

/**
 * An Arithmetic Grammar.
 */
public class Example1 {

  /** Main entry point. */
  public static void main(String args[]) {
    System.out.println("Reading from standard input...");
    Example1 t = new Example1(System.in);
    try {
      SimpleNode n = t.Start();
      n.dump("");
      System.out.println("Thank you.");
    } catch (Exception e) {
      System.out.println("Oops.");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}

PARSER_END(Example1)


SKIP :
{
  " "
| "\t"
| "\n"
| "\r"
| <"//" (~["\n","\r"])* ("\n"|"\r"|"\r\n")>
| <"/*" (~["*"])* "*" (~["/"] (~["*"])* "*")* "/">
}

TOKEN : /* LITERALS */
{
  < INTEGER_LITERAL:
        <DECIMAL_LITERAL> (["l","L"])?
      | <HEX_LITERAL> (["l","L"])?
      | <OCTAL_LITERAL> (["l","L"])?
  >
|
  < #DECIMAL_LITERAL: ["1"-"9"] (["0"-"9"])* >
|
  < #HEX_LITERAL: "0" ["x","X"] (["0"-"9","a"-"f","A"-"F"])+ >
|
  < #OCTAL_LITERAL: "0" (["0"-"7"])* >
}

TOKEN : /* IDENTIFIERS */
{
  < IDENTIFIER: <LETTER> (<LETTER>|<DIGIT>)* >
|
  < #LETTER: ["_","a"-"z","A"-"Z"] >
|
  < #DIGIT: ["0"-"9"] >
}

/** Main production. */
SimpleNode Start() : {}
{
  Expression() ";"
  { return jjtThis; }
}

/** An Expression. */
void Expression() : {}
{
  AdditiveExpression()
}

/** An Additive Expression. */
void AdditiveExpression() : {}
{
  MultiplicativeExpression() ( ( "+" | "-" ) MultiplicativeExpression() )*
}

/** A Multiplicative Expression. */
void MultiplicativeExpression() : {}
{
  UnaryExpression() ( ( "*" | "/" | "%" ) UnaryExpression() )*
}

/** A Unary Expression. */
void UnaryExpression() : {}
{
  "(" Expression() ")" | Identifier() | Integer()
}

/** An Identifier. */
void Identifier() : {}
{
  <IDENTIFIER>
}

/** An Integer. */
void Integer() : {}
{
  <INTEGER_LITERAL>
}
```

The only bit of JJTree-specific code is an action in the start production that dumps the constructed parse tree when the parse is complete. It uses JJTree simple mode.

The input file is `Example1.jjt`.

```java
$ jjtree Example1.jjt
> Reading from file Example1.jjt . . .
> Annotated grammar generated successfully in Example1.jj
```

JJTree has now generated the JavaCC parser source, as well as Java source for the parse tree node building classes. Running JavaCC in the normal way generates the remaining Java code.

```java
$ javacc Example1.jj
> Reading from file Example1.jj . . .
> File "TokenMgrError.java" does not exist.  Will create one.
> File "ParseException.java" does not exist.  Will create one.
> File "Token.java" does not exist.  Will create one.
> File "ASCII_CharStream.java" does not exist.  Will create one.
> Parser generated successfully.
```

Compile and run the Java program as usual. The expression is read from the standard input (you type in `(a + b) * (c + 1);`):

```java
$ javac Example1.java
$ java Example1
> Reading from standard input...
(a + b) * (c + 1);
Start
 Expression
  AdditiveExpression
   MultiplicativeExpression
    UnaryExpression
     Expression
      AdditiveExpression
       MultiplicativeExpression
        UnaryExpression
         Identifier
       MultiplicativeExpression
        UnaryExpression
         Identifier
    UnaryExpression
     Expression
      AdditiveExpression
       MultiplicativeExpression
        UnaryExpression
         Identifier
       MultiplicativeExpression
        UnaryExpression
         Integer
```
<br>

### <a name="jjtree-example-2"></a>Example2.jjt

---

This example is the same grammar as `Example1.jjt` with modifications to customize the generated tree. It illustrates how unnecessary intermediate nodes can be suppressed, and how actions in the grammar can attach extra information to the nodes.

```java
options {
  MULTI=true;
  KEEP_LINE_COLUMN = false;
}

PARSER_BEGIN(Example2)

/**
 * An Arithmetic Grammar.
 */
public class Example2 {

  /** Main entry point. */
  public static void main(String args[]) {
    System.out.println("Reading from standard input...");
    Example2 t = new Example2(System.in);
    try {
      ASTStart n = t.Start();
      n.dump("");
      System.out.println("Thank you.");
    } catch (Exception e) {
      System.out.println("Oops.");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}

PARSER_END(Example2)

SKIP :
{
  " "
| "\t"
| "\n"
| "\r"
| <"//" (~["\n","\r"])* ("\n"|"\r"|"\r\n")>
| <"/*" (~["*"])* "*" (~["/"] (~["*"])* "*")* "/">
}

TOKEN : /* LITERALS */
{
  < INTEGER_LITERAL:
        <DECIMAL_LITERAL> (["l","L"])?
      | <HEX_LITERAL> (["l","L"])?
      | <OCTAL_LITERAL> (["l","L"])?
  >
|
  < #DECIMAL_LITERAL: ["1"-"9"] (["0"-"9"])* >
|
  < #HEX_LITERAL: "0" ["x","X"] (["0"-"9","a"-"f","A"-"F"])+ >
|
  < #OCTAL_LITERAL: "0" (["0"-"7"])* >
}

TOKEN : /* IDENTIFIERS */
{
  < IDENTIFIER: <LETTER> (<LETTER>|<DIGIT>)* >
|
  < #LETTER: ["_","a"-"z","A"-"Z"] >
|
  < #DIGIT: ["0"-"9"] >
}

/** Main production. */
ASTStart Start() : {}
{
  Expression() ";"
  { return jjtThis; }
}

/** An Expression. */
void Expression() #void : {}
{
  AdditiveExpression()
}

/** An Additive Expression. */
void AdditiveExpression() #void : {}
{
  (
    MultiplicativeExpression() ( ( "+" | "-" ) MultiplicativeExpression() )*
  ) #Add(>1)
}

/** A Multiplicative Expression. */
void MultiplicativeExpression() #void : {}
{
  (
    UnaryExpression() ( ( "*" | "/" | "%" ) UnaryExpression() )*
  ) #Mult(>1)
}

/** A Unary Expression. */
void UnaryExpression() #void : {}
{
  "(" Expression() ")" | MyID() | Integer()
}

/** An Identifier. */
void MyID() :
{
  Token t;
}
{
  t=<IDENTIFIER>
  {
    jjtThis.setName(t.image);
  }
}

/** An Integer. */
void Integer() : {}
{
  <INTEGER_LITERAL>
}
```

This is a modification of the first example to illustrate how the parse tree can be customized:

```java
$ jjtree Example2.jjt
> Reading from file Example2.jjt . . .
> File "Node.java" does not exist.  Will create one.
> File "SimpleNode.java" does not exist.  Will create one.
> File "ASTStart.java" does not exist.  Will create one.
> File "ASTAdd.java" does not exist.  Will create one.
> File "ASTMult.java" does not exist.  Will create one.
> File "ASTInteger.java" does not exist.  Will create one.
> Annotated grammar generated successfully in Example2.jj
```

```java
$ javacc Example2.jj
> Reading from file Example2.jj . . .
> File "TokenMgrError.java" does not exist.  Will create one.
> File "ParseException.java" does not exist.  Will create one.
> File "Token.java" does not exist.  Will create one.
> File "ASCII_CharStream.java" does not exist.  Will create one.
> Parser generated successfully.
```

```java
$ javac Example2.java
$ java Example2
> Reading from standard input...
(a + b) * (c + 1);
Start
 Mult
  Add
   Identifier: a
   Identifier: b
  Add
   Identifier: c
   Integer
```

Look at `Example.jjt` to see how node annotations can be used to restructure the parse tree, and at `ASTMyID.java` to see how you can write your own node classes that maintain more information from the input stream.

<br>

### <a name="jjtree-example-3"></a>Example3.jjt

---

This example is a modification of `Example2.jjt` with the `NODE_DEFAULT_VOID` option set. This instructs JJTree to treat all undecorated non-terminals as if they were decorated as `#void`. The default JJTree behavior is to treat such non-terminals as if they were decorated with the name of the non-terminal.

```java
options {
  MULTI=true;
  NODE_DEFAULT_VOID=true;
}

PARSER_BEGIN(Example3)

/**
 * An Arithmetic Grammar.
 */
public class Example3 {
  /** Main entry point. */
  public static void main(String args[]) {
    System.out.println("Reading from standard input...");
    Example3 t = new Example3(System.in);
    try {
      ASTStart n = t.Start();
      n.dump("");
      System.out.println("Thank you.");
    } catch (Exception e) {
      System.out.println("Oops.");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}

PARSER_END(Example3)


SKIP :
{
  " "
| "\t"
| "\n"
| "\r"
| <"//" (~["\n","\r"])* ("\n"|"\r"|"\r\n")>
| <"/*" (~["*"])* "*" (~["/"] (~["*"])* "*")* "/">
}

TOKEN : /* LITERALS */
{
  < INTEGER_LITERAL:
        <DECIMAL_LITERAL> (["l","L"])?
      | <HEX_LITERAL> (["l","L"])?
      | <OCTAL_LITERAL> (["l","L"])?
  >
|
  < #DECIMAL_LITERAL: ["1"-"9"] (["0"-"9"])* >
|
  < #HEX_LITERAL: "0" ["x","X"] (["0"-"9","a"-"f","A"-"F"])+ >
|
  < #OCTAL_LITERAL: "0" (["0"-"7"])* >
}

TOKEN : /* IDENTIFIERS */
{
  < IDENTIFIER: <LETTER> (<LETTER>|<DIGIT>)* >
|
  < #LETTER: ["_","a"-"z","A"-"Z"] >
|
  < #DIGIT: ["0"-"9"] >
}

/** Main production. */
ASTStart Start() #Start : {}
{
  Expression() ";"
  { return jjtThis; }
}

/** An Expression. */
void Expression() : {}
{
  AdditiveExpression()
}

/** An Additive Expression. */
void AdditiveExpression() : {}
{
  (
    MultiplicativeExpression() ( ( "+" | "-" ) MultiplicativeExpression() )*
  ) #Add(>1)
}

/** A Multiplicative Expression. */
void MultiplicativeExpression() : {}
{
  (
    UnaryExpression() ( ( "*" | "/" | "%" ) UnaryExpression() )*
  ) #Mult(>1)
}

/** A Unary Expression. */
void UnaryExpression() : {}
{
  "(" Expression() ")" | Identifier() | Integer()
}

/** An Identifier. */
void Identifier() #MyID :
{
  Token t;
}
{
  t=<IDENTIFIER>
  {
    jjtThis.setName(t.image);
  }
}

/** An Integer. */
void Integer() #Integer : {}
{
  <INTEGER_LITERAL>
}
```

This example can be run in the same manner as you ran `Example2.jjt`.

<br>

### <a name="jjtree-example-4"></a>Example4.jjt

---

This is a modification of `Example3.jjt` with the `VISITOR` option set. This instructs JJTree to insert a `jjtAccept()` method into all nodes it generates, and to produce a visitor class. The visitor is used to dump the tree.

```java
options {
  MULTI=true;
  VISITOR=true;
  NODE_DEFAULT_VOID=true;
}

PARSER_BEGIN(Example4)

/**
 * An Arithmetic Grammar.
 */
public class Example4 {

  /** Main entry point. */
  public static void main(String args[]) {
    System.out.println("Reading from standard input...");
    Example4 t = new Example4(System.in);
    try {
      ASTStart n = t.Start();
      Eg4Visitor v = new Eg4DumpVisitor();
      n.jjtAccept(v, null);
      System.out.println("Thank you.");
    } catch (Exception e) {
      System.out.println("Oops.");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
}

PARSER_END(Example4)


SKIP :
{
  " "
| "\t"
| "\n"
| "\r"
| <"//" (~["\n","\r"])* ("\n"|"\r"|"\r\n")>
| <"/*" (~["*"])* "*" (~["/"] (~["*"])* "*")* "/">
}

TOKEN : /* LITERALS */
{
  < INTEGER_LITERAL:
        <DECIMAL_LITERAL> (["l","L"])?
      | <HEX_LITERAL> (["l","L"])?
      | <OCTAL_LITERAL> (["l","L"])?
  >
|
  < #DECIMAL_LITERAL: ["1"-"9"] (["0"-"9"])* >
|
  < #HEX_LITERAL: "0" ["x","X"] (["0"-"9","a"-"f","A"-"F"])+ >
|
  < #OCTAL_LITERAL: "0" (["0"-"7"])* >
}

TOKEN : /* IDENTIFIERS */
{
  < IDENTIFIER: <LETTER> (<LETTER>|<DIGIT>)* >
|
  < #LETTER: ["_","a"-"z","A"-"Z"] >
|
  < #DIGIT: ["0"-"9"] >
}

/** Main production. */
ASTStart Start() #Start : {}
{
  Expression() ";"
  { return jjtThis; }
}


/** An Expression. */
void Expression() : {}
{
  AdditiveExpression()
}

/** An Additive Expression. */
void AdditiveExpression() : {}
{
  (
    MultiplicativeExpression() ( ( "+" | "-" ) MultiplicativeExpression() )*
  ) #Add(>1)
}

/** A Multiplicative Expression. */
void MultiplicativeExpression() : {}
{
  (
    UnaryExpression() ( ( "*" | "/" | "%" ) UnaryExpression() )*
  ) #Mult(>1)
}

/** A Unary Expression. */
void UnaryExpression() : {}
{
  "(" Expression() ")" | Identifier() | Integer()
}

/** An Identifier. */
void Identifier() #MyOtherID :
{
  Token t;
}
{
  t=<IDENTIFIER>
  {
    jjtThis.setName(t.image);
  }
}

/** An Integer. */
void Integer() #Integer : {}
{
  <INTEGER_LITERAL>
}
```

This example again can be run in the same manner as you ran `Example2.jjt`. One thing to take care in this case is that you must run `jjtree` on a clean directory (that does not contain previously generated files).

For example, the file `SimpleNode.java` is different when the option `VISITOR` is set to `true`.

<br>

### <a name="examples"></a>Example Grammars

---

The following list of grammars was created by the JavaCC community.

* [AsnParser.jj](../grammars/AsnParser.jj)
* [CPPParser.jj](../grammars/CPPParser.jj)
* [CParser.jj](../grammars/CParser.jj)
* [ChemNumber.jj](../grammars/ChemNumber.jj)
* [Cobol.jj](../grammars/Cobol.jj)
* [DTDParser.jj](../grammars/DTDParser.jj)
* [EcmaScript.jjt](../grammars/EcmaScript.jjt)
* [ExpressParser.jj](../grammars/ExpressParser.jj)
* [FormsPlSql.jj](../grammars/FormsPlSql.jj)
* [GdmoTranslator.jj](../grammars/GdmoTranslator.jj)
* [IDLParser.jj](../grammars/IDLParser.jj)
* [JSONParser.jjt](../grammars/JSONParser.jjt)
* [OberonParser.jj](../grammars/OberonParser.jj)
* [PHP.jj](../grammars/PHP.jj)
* [PetalParser.jj](../grammars/PetalParser.jj)
* [PlSql.jj](../grammars/PlSql.jj)
* [RTFParser.jj](../grammars/RTFParser.jj)
* [infosapient.jj](../grammars/infosapient.jj)

<br>

---

You're done with the JavaCC tutorials!

[Home](../index.md)

<br>
