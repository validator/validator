[Home](../index.md) > [Tutorials](index.md) > Token Manager

---

This tutorial describes the JavaCC token manager. It covers lexical states, lexical actions, and the use of `SPECIAL_TOKEN`s.

### <a name="toc"></a>Contents

- [**Lexical States**](#lexical-states)
  * [Token matching](#token-matching)
- [**Lexical Actions**](#lexical-actions)
  * [Variables within lexical actions](#variables)
  * [Access to class level declarations within lexical actions](#class-declarations)
- [**Special Tokens**](#special-tokens)

## <a name="lexical-states"></a>Lexical States

The JavaCC lexical specification is organized into a set of *lexical states*, each of which is named with a unique identifier. There is a standard lexical state called `DEFAULT`. The generated token manager is at any moment in one of these lexical states. When the token manager is initialized, it starts off in the `DEFAULT` state, by default. The starting lexical state can also be specified as a parameter while constructing a token manager object.

Each lexical state contains an ordered list of regular expressions - the order is derived from the order of occurrence in the input file. There are four types of regular expressions: `SKIP`, `MORE`, `TOKEN`, and `SPECIAL_TOKEN`.

All regular expressions that occur as expansion units in the grammar are considered to be in the `DEFAULT` lexical state and their order of occurrence is determined by their position in the grammar file.

### <a name="token-matching"></a>Token matching

All regular expressions in the current lexical state are considered as potential match candidates. The token manager consumes the maximum number of characters from the input stream possible that match one of these regular expressions. That is, the token manager prefers the longest possible match. If there are multiple longest matches of the same length, the regular expression that is matched is the one with the earliest order of occurrence in the grammar file.

As mentioned above, the token manager is in exactly one state at any moment. At this moment, the token manager only considers the regular expressions defined in this state for matching purposes. After a match, one can specify an action to be executed as well as a new lexical state to move to. If a new lexical state is not specified, the token manager remains in the current state.

## <a name="lexical-actions"></a>Lexical Actions

The regular expression type specifies what action to take when a regular expression has been successfully matched:

Type | Action
:--- | :---
`SKIP` | Simply throw away the matched string (after executing any lexical action).
`MORE` | Continue to whatever the next state is, taking the matched string along. This string will be a prefix of the new matched string.
`TOKEN` | Create a token using the matched string and send it to the parser (or any caller).
`SPECIAL_TOKEN` | Creates a special token that does not participate in parsing. Already described earlier. <br> *N.B. The mechanism of accessing special tokens is at the end of this page.*

Whenever the end of file `<EOF>` is detected it causes the creation of an `<EOF>` token, regardless of the current state of the lexical analyzer. However, if an `<EOF>` is detected in the middle of a match for a regular expression - or immediately after a MORE regular expression has been matched - an error is reported.

After the regular expression is matched, the lexical action is executed. All the variables and methods declared in the `TOKEN_MGR_DECLS` region (see below) are available here for use. In addition, the variables and methods listed below are also available for use.

Immediately after this, the token manager changes state to that specified (if any).

After that the action specified by the type of the regular expression is taken (`SKIP`, `MORE`, etc). If the type is `TOKEN`, the matched token is returned. If the type is `SPECIAL_TOKEN`, the matched token is saved to be returned along with the next `TOKEN` that is matched.

### <a name="variables"></a>Variables within lexical actions

The following variables are available for use within lexical actions:

1. `StringBuffer image (READ/WRITE)`

The `image` variable (different from the `image` field of the matched token) is a `StringBuffer` that contains all the characters that have been matched since the last `SKIP`, `TOKEN`, or `SPECIAL_TOKEN`. You are free to make whatever changes you wish to it so long as you do not assign it to `null` since this variable is also used by the generated token manager.

If you make changes to `image`, this change is passed on to subsequent matches (if the current match is a `MORE`). The content of `image` *does not* automatically get assigned to the `image` field of the matched token. If you wish this to happen, you must explicitly assign it in a lexical action of a `TOKEN` or `SPECIAL_TOKEN` regular expression.

#### Example 1

```java
<DEFAULT> MORE : { "a" : S1 }

<S1> MORE :
{
  "b"
    { int l = image.length()-1; image.setCharAt(l, image.charAt(l).toUpperCase()); }
    ^1                                                                             ^2
    : S2
}

<S2> TOKEN :
{
  "cd" { x = image; } : DEFAULT
       ^3
}
```

In the above example, the value of `image` at the 3 points marked by `^1`, `^2`, and `^3` are:
```
At ^1: "ab"
At ^2: "aB"
At ^3: "aBcd"
```

2. `int lengthOfMatch (READ ONLY)`

This is the length of the current match (it is not cumulative over `MORE`'s). You should not modify this variable.

#### Example 2

Using the same example as above, the values of `lengthOfMatch` are:
```
At ^1: 1 (the size of "b")
At ^2: 1 (does not change due to lexical actions)
At ^3: 2 (the size of "cd")
```

3. `int curLexState (READ ONLY)`

This is the index of the current lexical state. You should not modify this variable. Integer constants whose names are those of the lexical state are generated into the `...Constants` file, so you can refer to lexical states without worrying about their actual index value.

4. `inputStream (READ ONLY)`

This is an input stream of the appropriate type depending on the values of options `UNICODE_INPUT` and `JAVA_UNICODE_ESCAPE` and is one of:
* `ASCII_CharStream`
* `ASCII_UCodeESC_CharStream`
* `UCode_CharStream`
* `UCode_UCodeESC_CharStream`

The stream is currently at the last character consumed for this match. Methods of `inputStream` can be called. For example, `getEndLine` and `getEndColumn` can be called to get the line and column number information for the current match. `inputStream` may not be modified.

5. `Token matchedToken (READ/WRITE)`

This variable may be used only in actions associated with `TOKEN` and `SPECIAL_TOKEN` regular expressions. This is set to be the token that will get returned to the parser. You may change this variable and thereby cause the changed token to be returned to the parser instead of the original one. It is here that you can assign the value of variable `image` to `matchedToken.image`. Typically that's how your changes to `image` has effect outside of the lexical actions.

#### Example 3

If we modify the last regular expression specification of the above example to:
```java
<S2> TOKEN :
{
  "cd" { matchedToken.image = image.toString(); } : DEFAULT
}
```

Then the token returned to the parser will have its `.image` field set to `aBcd`. If this assignment was not performed, then the `.image` field will remain as `abcd`.

6. `void SwitchTo(int)`

Calling this method switches you to the specified lexical state. This method may also be called from parser actions, in addition to being called from lexical actions. However, care must be taken when using this method to switch states from the parser since the lexical analysis could be many tokens ahead of the parser in the presence of large lookaheads. When you use this method within a lexical action, you must ensure that it is the last statement executed in the action, otherwise you may get unexpected behaviour.

If there is a state change specified using the `: state` syntax it overrides all `switchTo` calls, hence there is no point having a `switchTo` call when there is an explicit state change specified. In general, calling this method should be resorted to only when you cannot do it any other way. Using this method of switching states also causes you to lose some of the semantic checking that JavaCC does when you use the standard syntax.

### <a name="class-declarations"></a>Access to class level declarations within lexical actions

Lexical actions have access to a set of class level declarations. These declarations are introduced within the JavaCC file using the following syntax:

```java
token_manager_decls ::=
  "TOKEN_MGR_DECLS" ":"
  "{" java_declarations_and_code "}"
```

These declarations are accessible from all lexical actions.

#### Example 1 - Comments

```java
SKIP :
{
  "/*" : WithinComment
}

<WithinComment> SKIP :
{
  "*/" : DEFAULT
}

<WithinComment> MORE :
{
  <~[]>
}
```

#### Example 2 - String literals with actions to print the length of the `String`

```java
TOKEN_MGR_DECLS :
{
  int stringSize;
}

MORE :
{
  "\"" {stringSize = 0;} : WithinString
}

<WithinString> TOKEN :
{
  <STRLIT: "\""> {System.out.println("Size = " + stringSize);} : DEFAULT
}

<WithinString> MORE :
{
  <~["\n","\r"]> {stringSize++;}
}
```

## <a name="special-tokens"></a>Special Tokens

Special tokens are like tokens, except that they are permitted to appear anywhere in the input file (between any two tokens). Special tokens can be specified in the grammar input file using the reserved word `SPECIAL_TOKEN` instead of `TOKEN`, for example:

```java
SPECIAL_TOKEN :
{
  <SINGLE_LINE_COMMENT: "//" (~["\n","\r"])* ("\n"|"\r"|"\r\n")>
}
```

Any regular expression defined to be a `SPECIAL_TOKEN` may be accessed in a special manner from user actions in the lexical and grammar specifications. This allows these tokens to be recovered during parsing while at the same time these tokens do not participate in the parsing.

JavaCC has been bootstrapped to use this feature to automatically copy relevant comments from the input grammar file into the generated files.

The class Token now has an additional field:

```java
Token specialToken;
```

This field points to the special token immediately prior to the current token (special or otherwise). If the token immediately prior to the current token is a regular token (and not a special token), then this field is set to `null`. The `next` fields of regular tokens continue to have the same meaning, i.e. they point to the next regular token except in the case of the `EOF` token where the `*next` field is `null`. The `next` field of special tokens point to the special token immediately following the current token. If the token immediately following the current token is a regular token, the `next` field is set to `null`.

### Example

Suppose you wish to print all special tokens prior to the regular token `t` (but only those that are after the regular token before `t`):

```java
// if there are no special tokens return control to the caller
if (t.specialToken == null) {
  return;
}

// walk back the special token chain until it reaches the first special token after the previous regular token
Token tmp_t = t.specialToken;
while (tmp_t.specialToken != null) {
  tmp_t = tmp_t.specialToken;
}

// now walk the special token chain forward and print them in the process
while (tmp_t != null) {
  System.out.println(tmp_t.image);
  tmp_t = tmp_t.next;
}
```
<br>

---

[NEXT >>](lookahead.md)

<br>
