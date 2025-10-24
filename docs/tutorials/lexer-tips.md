[Home](../index.md) > [Tutorials](index.md) > Lexer Tips

--------------------------------------------------------------------------------

There are many ways to write the lexical specification for a grammar, but the performance of the generated token manager can vary significantly depending on how you do this.

This section presents a few tips for writing good lexical specifications.

### <a name="toc"></a>Contents

- [**String Literals**](#string-literals)
  * [Use string literals as much as possible](#tip1)
  * [Avoid string literals for the same token](#tip2)
  * [Order string literals by length](#tip3)
- [**Lexical States**](#lexical-states)
  * [Minimize use of lexical states](#tip4)
  * [Use SKIP as much as possible](#tip5)
  * [Avoid using SKIP with lexical actions and state changes](#tip6)
  * [Avoid using MORE if possible](#tip7)
- [**Other**](#other)
  * [Use ~[] by itself](#tip8)
  * [Avoid using IGNORE_CASE selectively](#tip9)


## <a name="string-literals"></a>String Literals

### <a name="tip1"></a>Use string literals as much as possible

Try to specify as many string literals as possible.

These are recognized by a Deterministic Finite Automata (DFA), which is much faster than the Non-deterministic Finite Automata (NFA) needed to recognize other kinds of complex regular expressions.

For example, to skip blanks / tabs / new lines:

```java
SKIP : { " " | "\t" | "\n" }
```

is more efficient than doing:

```java
SKIP : { < ([" ", "\t", "\n"])+ > }
```

Because in the first case you only have `String` literals, it will generate a DFA whereas for the second case it will generate an NFA.

### <a name="tip2"></a>Avoid string literals for the same token

Try to avoid having a choice of String literals for the same token.

For example:

```java
< NONE : "\"none\"" | "\'none\'" >
```

Instead, have two different token types for this and use a non-terminal which is a choice between those choices.

The above example can be written as:

```java
< NONE1 : "\"none\"" >
|
< NONE2 : "\'none\'" >
```

and define a non-terminal called `None()` as:

```java
void None() : {}
{
  <NONE1> | <NONE2>
}
```

This will make recognition much faster. Note that if the choice is between two complex regular expressions, it is OK to have the choice.

### <a name="tip3"></a>Order string literals by length

Specify all string literals in order of increasing length, i.e. all shorter string literals before longer ones.

This will help optimizing the bit vectors needed for string literals.


## <a name="lexical-states"></a>Lexical States

### <a name="tip4"></a>Minimize use of lexical states

Try to minimize the use of lexical states.

When using them, try to move all your complex regular expressions into a single lexical state, leaving others to just recognize simple string literals.

### <a name="tip5"></a>Use SKIP as much as possible

Try to `SKIP` as much possible if you don't care about certain patterns.

Here, you have to be a bit careful about `EOF`. Seeing an `EOF` after `SKIP` is fine whereas, seeing an `EOF` after a `MORE` is a lexical error.

### <a name="tip6"></a>Avoid using SKIP with lexical actions and state changes

Try to avoid lexical actions and lexical state changes with `SKIP` specifications, especially for single character `SKIP`'s like ` `, `\t`, `\n` etc).

For such cases, a simple loop is generated to eat up the `SKIP`'ed single characters. So, if there is a lexical action or state change associated with this, it is not possible to it this way.

### <a name="tip7"></a>Avoid using MORE if possible

Try to avoid specifying lexical actions with `MORE` specifications.

Generally every `MORE` should end up in a `TOKEN` (or `SPECIAL_TOKEN`) finally so you can do the action there at the `TOKEN` level, if it is possible.

## <a name="other"></a>Other

### <a name="tip8"></a>Use `~[]` by itself

Try to use the pattern `~[]` by itself as much as possible.

For example, doing

```java
MORE : { < ~[] > }
```

is better than doing
```java
TOKEN : { < (~[])+ > }
```

Of course, if your grammar dictates that one of these cannot be used, then you don't have a choice, but try to use `< ~[] >` as much as possible.

### <a name="tip9"></a>Avoid using IGNORE_CASE selectively

There is heavy performance penalty for setting `IGNORE_CASE` for some regular expressions and not for others in the same lexical state.

Best practise is to set the `IGNORE_CASE` option at the grammar level. If that is not possible, then try to have it set for *all* regular expressions in a lexical state.

<br>

---

[NEXT >>](examples.md)

<br>
