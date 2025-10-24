[Home](../index.md) > [Tutorials](index.md) > CharStream

---

This tutorial describes the `CharStream` classes including their usage, constructors and methods.

*N.B. Some details may not be relevant for the `CharStream` interface (to be used with `USER_CHAR_STREAM`).*

## CharStream Classes

There are 4 different types of `CharStream` classes that are generated based on combinations of various options.

| Type | Options | Description |
| :--- | :---    | :---        |
| `ASCII_CharStream` | Generated when neither of the two options - `UNICODE_INPUT` or `JAVA_UNICODE_ESCAPE` is set. | This class treats the input as a stream of 1-byte (`ISO-LATIN1`) characters. Note that this class can also be used to parse binary files. It just reads a byte and returns it as a 16 bit quantity to the lexical analyzer. So any character returned by this class will be in the range `\u0000`-`\u00ff`. |
| `ASCII_UCodeESC_CharStream` | Generated when the option `JAVA_UNICODE_ESCAPE` is set and the `UNICODE_INPUT` option is not set. | This class treats the input as a stream of 1-byte characters. However, the special escape sequence `("\\\\")* "\\" ("u")+` is treated as a tag indicating that the next 4 bytes following the tag will be hexadecimal digits forming a 4-digit hex number whose value will be treated as the value of the character at the position indicated by the first backslash. Note that this value can be anything in the range `0x0`-`0xffff`. |
| `UCode_CharStream` | Generated when the option `UNICODE_INPUT` is set and the option `JAVA_UNICODE_ESCAPE` is not set. | This class treats the input as a stream of 2-byte characters. So it reads 2 bytes `b1` and `b2` and returns them as a single character using the expression `b1 << 8 | b2` assuming big-endian order. So in particular all the characters in the range `0x00`-`0xff` are assumed to be stored as 2 bytes with the first (higher-order) byte being `0`. |
| `UCode_UCodeESC_CharStream` | Generated when both the options `UNICODE_INPUT` and `JAVA_UNICODE_ESCAPE` are set. | This class input is a stream of 2-byte characters (just like the `UCode_CharStream` class) and the special escape sequence `("\\\\")* "\\" ("u")+` is treated as a tag indicating that the next 4 2-byte characters following the tag will be hexadecimal digits forming a 4-digit hex number whose value will be treated as the value of the character at the position indicated by the first backslash. Note that this value can be any value in the range `0x0`-`0xffff`. Also note that the backslash(es) and u(s) are all assumed to be given as 2-byte characters (with the higher order byte value being `0`). |

*N.B. None of the above classes can be used to read characters in a mixed mode, i.e. some characters given as 1-byte characters and others as 2-byte characters. To do this, you need to set the `USER_CHAR_STREAM` option to `true` and define your own `CharStream`.*

In the following sections we will use the notation `XXXCharStream` that stands for any of the above described 4 classes.

## Constructors

```java
/**
 * Takes an input stream, starting line and column numbers
 * and constructs a CharStream object. It also creates buffers
 * of initial size 4K for buffering the characters and also for
 * line and column numbers for each of those characters.
 */
public XXXCharStream(java.io.InputStream dstream, int startline, int startcolumn)
```

```java
/**
 * Takes an input stream, starting line and column numbers
 * and constructs a CharStream object. It also creates buffers
 * of initial size buffsize for buffering the characters and also
 * for line and column numbers for each of those characters.
 */
public XXXCharStream(java.io.InputStream dstream, int startline, int startcolumn, int buffersize)
```

So when you have an estimate on the maximum size of any token that can occur, you can use that size to optimize the buffer sizes. Note that these sizes are only initial sizes and they will be expanded as and when needed (in 2K steps).

## Methods

All the following methods will be static or non-static depending on whether the `STATIC` option is `true` or `false` at the generation time. Also only those methods that users can use in their lexical actions (using the `input_stream` variable of the lexical analyzer) are documented here.

The rest of the (public) methods are very tightly coupled with the implementation of the lexical analyzer and thus should not be used in lexical actions. In the future we will streamline this by making that part of the interface an inner class to the lexical analyzer.

```java
/**
 * This method returns the next "character" in the input according
 * to the rules of the CharStream class as described above. It will
 * throw java.io.IOException if it reaches EOF during the process
 * of "constructing" the character. It also updates the line and
 * column number and buffers the character for any possible
 * backtracking that may be required later. It also stores the
 * line and column numbers for the same purpose.
 */
public final char readChar() throws java.io.IOException
```

```java
/**
 * This method returns the line number for the beginning of the current match.
 */
public final int getBeginLine()
```

```java
/**
 * This method returns the column number for the beginning of the current match.
 */
public final int getBeginColumn()
```

```java
/**
 * This method returns the line number for the ending of the current match.
 */
public final int getEndLine()
```

```java
/**
 * This method returns the column number for the ending of the current match.
 */
public final int getEndColumn()
```

```java
/**
 * This method puts back amount number of characters into the stream.
 *
 * N.B. The amount indicates the number of characters as constructed
 * by readChar. Since the buffers used are circular buffers, it cannot
 * check for illegal amount values, it just wraps around. So it is the
 * user's responsibility to make sure that those many characters are
 * really produced before a call to this method.
 */
public final void backup(int amount)
```

```java
/**
 * Returns the image of the current match. As far as the XXXCharStream
 * is concerned, all characters after the last call to the private
 * method BeginToken are considered a part of the current match.
 */
public final String GetImage()
```

```java
/**
 * This method reinitializes the XXXCharStream classes with a
 * (possibly new) input stream and starting line and column numbers.
 */
public void ReInit(java.io.InputStream dstream, int startline, int startcolumn)
```

```java
/**
 * This method reinitializes the XXXCharStream classes with a
 * (possibly new) input stream and starting line and column numbers
 * and adjusts the size of the buffers to buffersize, by extending them.
 *
 * N.B. If the value of buffersize is less than the current buffer sizes,
 * they remain unchanged.
 */
public void ReInit(java.io.InputStream dstream, int startline, int startcolumn, int buffersize)

```

```java
/**
 * This method adjusts the line and column number of the beginning
 * of the current match to newLine and newCol and also adjusts the
 * line and column numbers for all the characters in the lookahead buffer.
 */
public void adjustBeginLineColumn(int newLine, int newCol)
```
<br>

---

[NEXT >>](error-handling.md)

<br>
