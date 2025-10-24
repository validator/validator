[Home](../index.md) > [Tutorials](index.md) > Error Handling

---

This tutorial describes the error recovery features in JavaCC.

## Exception Classes

JavaCC supports two exception types:

| Exception Type | Description |
| :---           | :---        |
| `TokenMgrError` | Whenever the token manager detects a problem, it throws the exception `TokenMgrError`. |
| `ParseException` | Whenever the parser detects a problem, it throws the exception `ParseException`. |

`TokenMgrError` is a subclass of `Error` while `ParseException` is a subclass of `Exception`.

The reasoning here is that the token manager is never expected to throw an exception - you must be careful in defining your token specifications such that you cover all cases. Hence the suffix `Error` in `TokenMgrError`. You do not have to worry about this exception - if you have designed your tokens well, it should never get thrown.

Conversely, it is typical to attempt recovery from `Parser` errors. If you still want to recover from token manager errors you can do it - it's just that you are not forced to catch them.

The JavaCC grammar specification includes a syntax to specify additional exceptions that may be thrown by methods corresponding to non-terminals. This syntax is identical to the Java `throws ...` syntax.

Here is an example of how to use this:

```java
void VariableDeclaration() throws SymbolTableException, IOException :
{...}
{
  ...
}
```

`VariableDeclaration` is defined to throw exceptions `SymbolTableException` and `IOException` in addition to `ParseException`.

## Error Reporting

The scheme for error reporting is straightforward - simply modify the file `ParseException.java` for your purposes. Typically, you should modify the `getMessage()` method to do your own customized error reporting. All information regarding these methods can be obtained from the comments in the generated files `ParseException.java` and `TokenMgrError.java`. It will also help to understand the standard Java functionality of the class `Throwable`.

There is a method in the generated parser called `generateParseException()`. You can call this method anytime you wish to generate an object of type `ParseException`. This object will contain all the choices that the parser has attempted since the last successfully consumed token.

## Error Recovery

JavaCC offers two kinds of error recovery - *shallow recovery* and *deep recovery*.

Shallow recovery recovers if none of the current choices have succeeded in being selected, while deep recovery is when a choice is selected but then an error happens sometime during the parsing of this choice.

### Shallow error recovery

Consider the following example:

```java
void Stm() :
{}
{
  IfStm()
|
  WhileStm()
}
```

Let's assume that `IfStm` starts with the reserved word `if` and `WhileStm` starts with the reserved word `while`. Suppose you want to recover by skipping all the way to the next semicolon when neither `IfStm` nor `WhileStm` can be matched by the next input token (assuming a `LOOKAHEAD` of `1`). That is, the next token is neither `if` nor `while`.

You can specify the following:

```java
void Stm() :
{}
{
  IfStm()
|
  WhileStm()
|
  error_skipto(SEMICOLON)
}
```

But you have to define `error_skipto` first. So far as JavaCC is concerned, `error_skipto` is just like any other non-terminal.

The following is one way to define `error_skipto` (here we use the standard `JAVACODE` production):

```java
JAVACODE
void error_skipto(int kind) {
  ParseException e = generateParseException();  // generate the exception object
  System.out.println(e.toString());  // print the error message
  Token t;
  // consume tokens all the way up to a token of "kind" - use a do-while loop
  // rather than a while because the current token is the one immediately before
  // the erroneous token (in our case the token immediately before what should
  // have been "if"/"while".
  do {
    t = getNextToken();
  }
  while (t.kind != kind);
}
```

That's it for shallow error recovery. In a future version of JavaCC we will have support for modular composition of grammars. When this happens, one can place all these error recovery routines into a separate module that can be imported into the main grammar module. We intend to supply a library of useful routines (for error recovery and otherwise) when we implement this capability.

### Deep Error Recovery

Using the same example as for shallow recovery:

```java
void Stm() :
{}
{
  IfStm()
|
  WhileStm()
}
```

In this case we wish to recover in the same way, however we wish to recover even when there is an error deeper into the parse.

For example, suppose the next token was `while` - therefore the choice `WhileStm` was taken. But suppose that during the parse of `WhileStm` some error is encountered - say we have `while (foo { stm; }` i.e. the closing parentheses has been missed. Shallow recovery will not work for this situation, we need deep recovery to achieve this. For this, we offer a new syntactic entity in JavaCC - the `try-catch-finally` block.

First, let us refactor the above example for deep error recovery and then explain the `try-catch-finally` block in more detail:

```java
void Stm() :
{}
{
  try {
    (
      IfStm()
    |
      WhileStm()
    )
  }
  catch (ParseException e) {
    error_skipto(SEMICOLON);
  }
}
```

That's it. If there is any unrecovered error during the parse of `IfStm` or `WhileStm`, then the catch block takes over. You can have any number of `catch` blocks and optionally a `finally` block (just as with Java errors). What goes into the `catch` blocks is *Java code*, not JavaCC expansions.

For example, the above example could have been rewritten as:

```java
void Stm() :
{}
{
  try {
    (
      IfStm()
    |
      WhileStm()
    )
  }
  catch (ParseException e) {
    System.out.println(e.toString());
    Token t;
    do {
      t = getNextToken();
    } while (t.kind != SEMICOLON);
  }
}
```

It is best to avoid placing too much Java code in the `catch` and `finally` blocks since it overwhelms the grammar reader - it is best to define methods that you can then from the `catch` blocks.

Note that in the second version of the example, we essentially copied the code out of the implementation of `error_skipto`. But we left out the first statement - the call to `generateParseException()`. In this case, the `catch` block already provides us with the exception. Even if you did call this method, you will get back an identical object.

<br>

---

[NEXT >>](lexer-tips.md)

<br>
