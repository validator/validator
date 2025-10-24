<!---
Instructions to update documentation for a new release:
- Add release notes docs/release-notes.md.
- Add download links to docs/downloads.md. 
- Change the latest version in the Download & Installation section of README.md#download.
- Change the latest version in the Download & Installation section of docs/index.md#download.
-->

[Home](index.md) > Release Notes

This page is a complete log of changes that have taken place since the release of JavaCC v0.5 in October 1996.

It also includes the change history for JJTree, JJDoc and the C++ versions of JavaCC and JJTree.

## <a name="toc"></a>Contents

  * [JavaCC](#javacc)
  * [JJTree](#jjtree)
  * [JJDoc](#jjdoc)
  * [JavaCC (C++ version)](#javacc-c-version)
  * [JJTree (C++ version)](#jjtree-c-version)

---
## JavaCC

### Version history

#### 7.0.x

* [7.0.14](#modifications-in-javacc-7-0-14)
* [7.0.13](#modifications-in-javacc-7-0-13)
* [7.0.12](#modifications-in-javacc-7-0-12)
* [7.0.11](#modifications-in-javacc-7-0-11)
* [7.0.10](#modifications-in-javacc-7-0-10)
* [7.0.9](#modifications-in-javacc-7-0-9)
* [7.0.8](#modifications-in-javacc-7-0-8)
* [7.0.7](#modifications-in-javacc-7-0-7)
* [7.0.6](#modifications-in-javacc-7-0-6)
* [7.0.5](#modifications-in-javacc-7-0-5)
* [7.0.4](#modifications-in-javacc-7-0-4)
* [7.0.3](#modifications-in-javacc-7-0-3)
* [7.0.2](#modifications-in-javacc-7-0-2)
* [7.0.1](#modifications-in-javacc-7-0-1)
* [7.0.0](#modifications-in-javacc-7-0-0)

#### 6.0.x

* [6.1.2](#modifications-in-javacc-6-1-2)
* [6.0.0](#modifications-in-javacc-6-0-0)

#### 4.x

* [4.2](#modifications-in-javacc-4-2)
* [4.1](#modifications-in-javacc-4-1)
* [4.0](#modifications-in-javacc-4-0)

#### 3.x

* [3.2](#modifications-in-javacc-3-2)
* [3.1](#modifications-in-javacc-3-1)
* [3.0](#modifications-in-javacc-3-0)

#### 2.x

* [2.1](#modifications-in-javacc-2-1)
* [2.0](#modifications-in-javacc-2-0)

#### 1.x

* [1.2](#modifications-in-javacc-1-2)
* [1.0](#modifications-in-javacc-1-0)

#### 0.x

* [0.8-pre2](#modifications-in-javacc-0-8-pre2)
* [0.8-pre1](#modifications-in-javacc-0-8-pre1)
* [0.7.1](#modifications-in-javacc-0-7-1)
* [0.7](#modifications-in-javacc-0-7)
* [0.7-pre7](#modifications-in-javacc-0-7-pre7)
* [0.7-pre6](#modifications-in-javacc-0-7-pre6)
* [0.7-pre5](#modifications-in-javacc-0-7-pre5)
* [0.7-pre4](#modifications-in-javacc-0-7-pre4)
* [0.7-pre3](#modifications-in-javacc-0-7-pre3)
* [0.7-pre2](#modifications-in-javacc-0-7-pre2)

---
### Modifications in JavaCC 7.0.14

* Added use of Maven toolchains plugin to ensure building with the (appropriate) local JDK 8

---
### Modifications in JavaCC 7.0.13

* \#267     : Resolve merge conflicts from #245
* \#245     : Fix issue #243 (Character code is returned instead of the symbol in the message)
* \#232     : Revert "Try to fix &#123;&#123;&#123;&#123;&#123;&#125;&#125;&#125;&#125;&#125; issue in GitHub Pages"
* \#231     : Try to fix &#123;&#123;&#123;&#123;&#123;&#125;&#125;&#125;&#125;&#125; issue in GitHub Pages

---
### Modifications in JavaCC 7.0.12

* \#230     : Remove unused char from TokenMgrError.template in LexicalEr
* \#228     : Remove redundant cast in TokenMgrError template
* \#224     : Production part in javacc_input cannot be omitted
* \#223     : Fix annotations for JavaCharStream 
* \#222     : Generate max. one deprecated annotation per method
* \#219     : Fix mismatched javadoc
* \#213     : Fix legacy links to Apache Lucene's grammar file
* \#212     : Test for allocation expression #189
* \#211     : Bad defaultVisit() method generated
* \#210     : Another change for marked for removal 
* \#209     : Improve lexical error message 
* \#208     : Relocated misplaced annotations
* \#207     : Changed methods marked for removal 
* \#206     : Build xml improvements 
* \#205     : Small fixes on warnings
* \#203     : Missing change for doc for token_manager_decls (issue #190) 
* \#202     : Fix doc for token_manager_decls (issue #190)
* \#200     : Update index.md and _config.yml
* \#199     : Fix QueryParser.jj url in README

---
### Modifications in JavaCC 7.0.11

* \#193			: Code error in class RCharacterList.SortDescriptors(): fixed

---
### Modifications in JavaCC 7.0.10

 * \#183			: Lookahead is broken since 7.0.5 (introduced in commit fbac68f)

---
### Modifications in JavaCC 7.0.9

* pom.xml		: change deployment on OSS to automatic release
* documentation	: fix broken links

---
### Modifications in JavaCC 7.0.8

* \#175			: Doesn't work in Turkish Locale
* \#73			: javacc/c++: invalid lookahead processing: missing one lookahead to get rid of the conflict

---
### Modifications in JavaCC 7.0.7

* JavaCC.jj		: remove unused c++ declaration blocs tokens
* \#172			: add a specific '-version' command line argument

---
### Modifications in JavaCC 7.0.6

The following changes are not upward compatible with the previous 7.0.5 version but have 
a very little impact on existing grammars. Main advantage is to prepare a more smooth upgrade
with the upcoming javacc-8.0.0 major release.
 
* C++ generation: renaming the option TOKEN_EXTENDS          by TOKEN_SUPER_CLASS
* C++ generation: renaming the option TOKEN_INCLUDES         by TOKEN_INCLUDE 
* C++ generation: renaming the option PARSER_INCLUDES        by PARSER_INCLUDE 
* C++ generation: renaming the option TOKEN_MANAGER_INCLUDES by TOKEN_MANAGER_INCLUDE

---
### Modifications in JavaCC 7.0.5

* \#42: new README.md file.
* \#71: Add support for Java7 language features.
* \#75: Allow empty type parameters in Java code of grammar files.
* \#77: javacc.org is out of date.
* \#92: Avoid warning of unused import in generated ParserTokenManager.
* \#99: LookaheadSuccess creation performance improved.

Removing IDE specific files.
Declare trace_indent only if debug parser is enabled.
CPPParser.jj grammar added to grammars.
Build with Maven is working again.

---WARNING---WARNING---WARNING---  
Required Java Platform: Standard Edition 7.0: known under Eclipse as JavaSE-1.7

---
### Modifications in JavaCC 7.0.4

Internal refactoring

---
### Modifications in JavaCC 7.0.3

Internal refactoring

---
### Modifications in JavaCC 7.0.2

C++ generation: Fixes of private/public scope for Node constructors.
Fix JJTParserState::closeNodeScope signature to avoid ambiguity in
resolving the overloaded function name.

---
### Modifications in JavaCC 7.0.1

C++ generation: Fix generation of variadic template return type of BNF
production that were missing in the code file.

Refactoring of the build process.

---
### Modifications in JavaCC 7.0.0

This version initiates the table driven parsing process as a optional feature.

Ant can now publish the distribution to Maven Central using the target
'deploy' for the SNAPSHOT then 'stage' for the RELEASE.

The c++ generation has been refactored.

---
### Modifications in JavaCC 6.1.2

This version brings fixes of the JavaCC for c++ generation with also a
refactoring of the generated code. Main features/fixes added are

   JavaCC
       the ResultTYpe accepts now C++ the template syntax with typename,
       varyading parameter and also the '::' namespace qualifier.
       Non terminal production accepts varyadin template arguments.


   JJTree
       Each AST node is generated in his own file.
       SimpleNode is also generated in his own file.
       See examples/JJTreeExamples/cpp.

---
### Modifications in JavaCC 6.0.0

Added support for C++ code generation - for almost all features - except ERROR_REPORTING.
The generated code mostly uses the same design as the Java version.

C++ specific issues

Since C++ is somewhat different from Java in code organization in terms of header files etc.,
we made the following changes:

* Parser class in PARSER_BEGIN/PARSER_END - this cannot be supported as the	parser is generated into the header file. So in the C++ version you can only	define the contents of the class, not the class itself.

* Include files - in order to write any action code for the lexer/parser/tree, you might need to include header files. So we allow to specify what the header file is for each of the components - see the list of C++ options below for more details.

* Limitation on code in actions - since we don't have a full C++ parser embedded in the JavaCC grammar, we don't support arbitrary C++ code. So be aware of this when you write the actions. In general, it's a good idea to make them all function calls.

* Object lifetimes - in general, the parser deletes all the objects it creates - including the tokens.	So any data that you want to pass out of the parser should be copied to your own objects that you can return.	Note that tree created is a "return value" of the parser so that's not deleted.

* Exceptions - due to the way the code evolved, we could not use exceptions in C++. So we have an ErrorHandler interface instead which by default just prints syntax error and continues trying to parse. Please see the apiroutiunes.html documt for more details.

* Wide character support - C++ version is fully wide character compatible and it has been tested on Windows, Linux and Mac environments.

Added a new option:
  -OUTPUT_LANGUAGE - string value - "java" and "c++" are two currently allowed values

Implemented all JavaCC options for the C++ version as well.

Added new options relevant to C++:
  -NAMESPACE - namespace to be used for all the generated classes.
  -PARSER_INCLUDES - a single include file that gets includedin the parser. Use this to include all the declarations needed for your actions.
  -PARSER_SUPER_CLASS - super class of the parser class (as we do not allow class declaration in PARSER_BEGIN/PARSER_END for C++)
  -TOKEN_INCLUDES - a single include file for the token class
  -TOKEN_MANAGER_INCLUDES - a single include file for the TokenManager
  -TOKEN_MANAGER_SUPER_CLASS - super class of the token manager
  -IGNORE_ACTIONS - an option to ignore all the action so a clean parser can be generated even when jjtree is used. Useful for debugging/profiling/testing

---
### Modifications in JavaCC 4.2

Release 4.2 is a maintenance release, incorporating a number of bug fixes and enhancements. For a complete list, please see the [issue tracker](https://javacc.dev.java.net/issues/buglist.cgi?component=javacc&field0-0-0=target_milestone&type0-0-0=equals&value0-0-0=4.2).

---
### Modifications in JavaCC 4.1

Release 4.1 is a maintenance release, incorporating a number of bug fixes and enhancements. For a complete list, please see the [issue tracker](https://javacc.dev.java.net/issues/buglist.cgi?component=javacc&field0-0-0=target_milestone&type0-0-0=equals&value0-0-0=4.1).

---
### Modifications in JavaCC 4.0

See the bug list in issue tracker for all the bugs fixed in this release.
JJTree and JavaCC both now support 1.5 syntax.
We now support accessing token fields in the grammar like: s=<ID>.image
Convenient constructors for passing encoding directly to the grammar
Tabsetting is now customizable.
SimpleNode can now extend a class using the NODE_EXTENDS option.
JAVACODE and BNF productions take optional access modifiers.

---
### Modifications in JavaCC 3.2

New regular expression kind with range operator where the upperbound is
optional, meaning just minimum, no max - (<RE>){n,}
Fix for issue 41 where it takes exponential time to minimumSize

Added the LICENSE file to the installation root directory.
Fixed issues #: 10, 11, 13, 2, 4, 5, 7
In particular, the generated code should now compile with JDK 1.5 (Tiger)

---
### Modifications in JavaCC 3.1

Open source with BSD license.
Fixed the copyright text in the source files.

---
### Modifications in JavaCC 3.0

No GUI version anymore.

Fixed a bug in handling string literals when they intersect some
regular expression.

Split up initializations of jj_la1_* vars into smaller methods so
that there is no code size issue. This is a recently reported bug.

---
### Modifications in JavaCC 2.1

Added a new option - KEEP_LINE_COLUMN default true.

If you set this option to false, the generated CharStream will not
have any line/column tracking code. It will be your responsibility
to do it some other way. This is needed for systems which don't care
about giving error messages etc.

API Changes: JavaCC no longer generates one of the 4 stream classes:

```java
ASCII_CharStream
ASCII_UCodeESC_CharStream
UCode_CharStream
UCode_UCodeESC_CharStream
```

In stead, it now supports two kinds of streams:

```java
SimpleCharStream
JavaCharStream
```

Both can be instantiated using a Reader object.

SimpleCharStream just reads the characters from the Reader using the
read(char[], int, int) method. So if you want to support a specific
encoding - like SJIS etc., you will first create the Reader object
with that encoding and instantiate the SimpleCharStream with that
Reader so your encoding is automatically used. This should solve a
whole bunch of issues with UCode* classes that were reported.

JavaCharStream is pretty much like SimpleCharStream, but it also does
`\uxxxx` processing as used by the Java programming language.

Porting old grammars:

Just replace Stream class names as follows -

* if you are using ASCII_CharStream or UCode_CharStream, change it to SimpleCharStream
* if you are using ASCII_UCodeESC_CharStream or UCode_UCodeESC_CharStream, change it to JavaCharStream

The APIs remain the same.

Also, the CharStream interface remains the same. So, if you have been using
USER_CHAR_STREAM option, then you don't need to change anything.

---
### Modifications in JavaCC 2.0

Added CPP grammar to examples directory (contributed by Malome Khomo).

GUI is now available to run JavaCC.  You can control all aspects of
JJTree and JavaCC (except creating and editing the grammar file)
through this GUI.

Desktop icons now available on a variety of platforms so you can
run JavaCC by double clicking the icon.

Bash on NT support improved.

Uninstaller included.

Fixed some minor bugs.

---
### Modifications in JavaCC 1.2

Moved JavaCC to the Metamata installer and made it available for
download from Metamata's web site.

Added Java 1.2 grammars to the examples directory.

Added repetition range specifications for regular expressions.
You can specify exact number of times a particular re should
occur or a {man, max} range, e.g,

```java
TOKEN:
{
     < TLA: (["A"-"Z"]){3} > // Three letter acronyms!

  |

     // An incomplete spec for the DOS file name format
     < DOS_FILENAME: (~[".", ":", ";", "\\"]) {1,8}
                     ( "." (~[".", ":", ";", "\\"]){1,3})? >
}
```

The translation is right now expanding out these many number of times
so use it with caution.

You can now specify actions/state changes for EOF. It is right now
very strict in that it has to look exactly like:

```java
 <*> TOKEN:
 {
    < EOF > { action } : NEW_STATE
 }
```

which means that EOF is still EOF in every state except that now
you can specify what state changes  if any or what java code
if any to execute on seeing EOF.

This should help in writing grammars for processing C/C++ #include
files, without going through hoops as in the old versions.

---
### Modifications in JavaCC 1.0

Fixed bugs related to usage of JavaCC with Java 2.

Many other bug fixes.

---
### Modifications in JavaCC 0.8-pre2

Mainly bug fixes.

---
### Modifications in JavaCC 0.8-pre1

Changed all references to Stream classes in the JavaCC code itself and
changed them to Reader/Writer.

Changed all the generated \*CharStream classes to use Reader instead of
InputStream. The names of the generated classes still say \*CharStream.
For compatibility reasons, the old constructors are still supported.
All the constructors that take InputStream create InputStreamReader
objects for reading the input data. All users parsing non-ASCII inputs
should continue to use the InputStream constructors.

Generate inner classes instead of top level classes where appropriate.

---
### Modifications in JavaCC 0.7.1

Fixed a bug in the handling of empty PARSER_BEGIN...PARSER_END
regions.

Fixed a bug in Java1.1noLA.jj - the improved performance Java grammar.

Fixed a spurious definition that was being generated into the parser
when USER_TOKEN_MANAGER was set to true.

---
### Modifications in JavaCC 0.7

Fixed the error reporting routines to delete duplicate entries from
the "expected" list.

Generated braces around the "if (true) ..." construct inserted
by JavaCC to prevent the dangling else problem.

Added code to consume_token that performs garbage collections of
tokens no longer necessary for error reporting purposes.

Fixed a bug with OPTIMIZE_TOKEN_MANAGER when there is a common prefix
for two or more (complex) regular expressions.

Fixed a JJTree bug where a node annotation #P() caused a null pointer
error.

Only generate the jjtCreate() methods if the NODE_FACTORY option is
set.

Fixed a bug where the name of the JJTree state file was being used in
the declaration of the field.

Updated the performance page to demonstrate how JavaCC performance
has improved since Version 0.5.

---
### Modifications in JavaCC 0.7-pre7

Added an option CACHE_TOKENS with a default value of false.  You
can generate slightly faster and (it so happens) more compact
parsers if you set CACHE_TOKENS to true.

Improved time and space requirements as compared to earlier
versions - regardless of the setting of CACHE_TOKENS.

Performance has improved roughly 10% (maybe even a little more).
Space requirements have reduced approximately 30%.

It is now possible to generate a Java parser whose class file is
only 28K in size.  To do this, run JavaCC on Java1.1noLA.jj with
options ERROR_REPORTING=false and CACHE_TOKENS=true.

And over the next few months, there is still places where space
and time can be trimmed!

The token_mask arrays are completely gone and replaced by bit
vectors.

Nested switch statements have been flattened.

Fixed a bug in the outputting of code to generate a method

```java
jjCheckNAddStates(int i)
```

calls to which are generated, but not the method.

Generating the 'static' keyword for the backup method of the
UCode\*.java files when STATIC flag is set.

---
### Modifications in JavaCC 0.7-pre6

Extended the generated CharStream classes with a method to adjust the
line and column numbers for the beginning of a token.  Look at the C++
grammar in the distribution to see an example usage.

Fixed the JavaCC front-end so that error messages are given with line
numbers relative to the original .jjt file if the .jj file is generated
by pre-processing using jjtree.

Removed support for old deprecated features:

. IGNORE_IN_BNF can no longer be used.  Until this version, you
  would get a deprecated warning message if you did use it.

. The extra {} in TOKEN specifications can no longer be used.  Until
  this version, you would get a deprecated warning message if your
  did use it.

ParseError is no longer supported.  It is now ParseException.  Please
delete the existing generated files for ParseError and ParseException.
The right ParseException will automatically get regenerated.

Completed step 1 in getting rid of the token mask arrays.  This
occupies space and is also somewhat inefficient.  Essentially,
replaced all "if" statements that test a token mask entry with
faster "switch" statements.  The token mask array still exist for
error reporting - but they will be removed in the next step (in
the next release).  As a result, we have noticed improved parser
speeds (up to 10% for the Java grammar).

As a consequence of doing step 1, but not step 2, the size of the
generated parser has increased a small amount.  When step 2 is
completed, the size of the generated parser will go down to be even
smaller than what it was before.

Cache tokens one step ahead during parsing for performance reasons.

Made the static token mask fields "final".  Note that the token
mask arrays will go away in the next release.

The Java 1.1 grammar was corrected to allow interfaces nested within
blocks.  The JavaCC grammar was corrected to fix a bug in its
handling of the ">>>=" operator.

Fixed a bug in the optimizations of the lexical analyzer.

Many changes have been made to JJTree.  See the JJTree release
notes for more information.

---
### Modifications in JavaCC 0.7-pre5

Fixed a bug with TOKEN_MGR_DECLS introduced in 0.7pre4.

Enhanced JavaCC input grammar to allow JavaCC reserved words in
Java code (such as actions).  This too was disallowed in 0.7pre4
only and has been rectified.

The JavaCC+JJTree grammar is now being offered to our users.  You
can find it in the examples directory.

Fixed an array index out of bounds bug in the parser - that sometimes
can happen when a non-terminal can expand to more than 100 other
non-terminals.

Fixed a bug in generating parsers with USER_CHAR_STREAM set to true.

Created an alternate Java 1.1 grammar in which lookaheads have been
modified to minimize the space requirements of the generated
parser.  See the JavaGrammars directory under the examples directory.

Provided instructions on how you can make your own grammars space
efficient (until JavaCC is improved to do this).  See the
JavaGrammars directory under the examples directory.

Updated all examples to make them current.  Some examples had become
out of date due to newer versions of JavaCC.

Updated the VHDL example - Chris Grimm made a fresh contribution.
This seems to be a real product quality example now.

Fixed bugs in the Obfuscator example that has started being used
for real obfuscation by some users.
The token manager class is non-final (this was a bug).

Many changes have been made to JJTree.  See the JJTree release
notes for more information.

Fixed all token manager optimization bugs that we know about.

Fixed all UNICODE lexing bugs that we know about.

Fixed an array index out of bounds bug in the token manager.

---
### Modifications in JavaCC 0.7-pre4

The only significant change for this version is that we incorporated
the Java grammar into the JavaCC grammar.  The JavaCC front end is
therefore able to parse the entire grammar file intelligently rather
than simple ignore the actions.

---
### Modifications in JavaCC 0.7-pre3

WE HAVE NOT ADDED ANY MAJOR FEATURES TO JAVACC FOR THIS PRERELEASE.
WE'VE FOCUSED MAINLY ON BUG FIXES.  BUT HERE IS WHAT HAS CHANGED:

Fixed the JavaCC license agreement to allow redistributions of example
grammars.

Fixed a couple of bugs in the JavaCC grammar.

Fixed an obscure bug that caused spurious '\r's to be generated
on Windows machines.

Changed the generated \*CharStream classes to take advantage of the
STATIC flag setting.  With this (like the token manager and parser)
the \*CharStream class also will have all the methods and variables to
be static with STATIC flag.

A new option OPTIMIZE_TOKEN_MANAGER is introduced. It defaults to
true.  When this option is set, optimizations for the TokenManager, in
terms of size *and* time are performed.

This option is automatically set to false if DEBUG_TOKEN_MANAGER is
set to true.

The new option OPTIMIZE_TOKEN_MANAGER might do some unsafe
optimization that can cause your token manager not to compile or run
properly. While we don't expect this to happen that much, in case it
happens, just turn off the option so that those optimizations will not
happen and you can continue working. Also, if this happens, please
send us the grammar so we can analyze the problem and fix JavaCC.

A String-valued option OUTPUT_DIRECTORY is implemented. This can be
used to instruct JavaCC to generate all the code files in a particular
directory.  By default, this is set to user.dir.

Fixed a minor bug (in 0.7pre2) in that the specialToken field was not
being set before a lexical action for a TOKEN type reg. exp.

Added a toString method to the Token class to return the image.

---
### Modifications in JavaCC 0.7-pre2

AS USUAL, KEEP IN MIND THAT THIS IS A PRERELEASE THAT WE HAVE NOT
TESTED EXTENSIVELY.  THERE ARE A FEW KNOWN BUGS THAT ARE STILL PRESENT
IN THIS VERSION.  QUALITY CONTROL FOR PRERELEASES ARE SIGNIFICANTLY
LOWER THAN STABLE RELEASES - I.E., WE DON'T MIND THE PRESENCE OF BUGS
THAT WE WOULD FEEL EMBARRASSED ABOUT IN STABLE RELEASES.

Main feature release for 0.7pre2 is a completely redone JJTree.  It
now bootstraps itself.  See the JJTree release notes for more
information.

Error recovery constructs have been modified a bit from 0.7pre1.  The
parser methods now throw only ParseException by default.  You can now
specify a "throws" clause with your non-terminals to add other
exceptions to this list explicitly.  Please see the help web page at:

[http://www.suntest.com/JavaCCBeta/newerrorhandling.html](http://www.suntest.com/JavaCCBeta/newerrorhandling.html)

for complete information on error recovery.

A new Java grammar improved for performance in the presence of very
complex expressions is now included.  This is NewJava1.1.jj.

More optimizations for the size of the token manager's java and class
files.  The generated .java files are about 10-15% smaller that
0.7pre1 (and 40-45%) smaller compared to 0.6. The class files (with
-O) are about 20% smaller compared to 0.6.

The parser size has been decreased.  The current optimizations affect
grammars that have small amounts of non-1 lookaheads.  For example the
generated code for the Java grammar has now reduced by 10%.

Extended the Token class to introduce a new factory function that
takes the token kind and returns a new Token object. This is done to
facilitate creating Objects of subclasses of Token based on the kind.
Look at the generated file Token.java for more details.

The restriction on the input size (to be < 2 gbytes) for the token
manager is gone.  Now the lexer can tokenize any size input (no
limit).

Removed all the references to System.out.println in the \*CharStream
classes.  Now all these are thrown as Error objects.

Fixed a very old problem with giving input from System.in.

---
## JJTree

### Version history

THIS FILE IS A COMPLETE LOG OF ALL CHANGES THAT HAVE TAKEN PLACE SINCE THE RELEASE OF VERSION 0.2.2.

#### 6.x

* [6.0](#modifications-in-jjtree-6-0)

#### 4.x

* [4.2](#modifications-in-jjtree-4-2)
* [4.3](#modifications-in-jjtree-4-2)
* [4.0](#modifications-in-jjtree-4-0)

#### 0.x

* [0.3-pre6](#modifications-in-jjtree-0-3-pre6)
* [0.3-pre5](#modifications-in-jjtree-0-3-pre5)
* [0.3-pre4](#modifications-in-jjtree-0-3-pre4)
* [0.3-pre3](#modifications-in-jjtree-0-3-pre3)
* [0.3-pre2](#modifications-in-jjtree-0-3-pre2)
* [0.3-pre1](#modifications-in-jjtree-0-3-pre1)
* [0.2.6](#modifications-in-jjtree-0-2-6)
* [0.2.5](#modifications-in-jjtree-0-2-5)
* [0.2.4](#modifications-in-jjtree-0-2-4)
* [0.2.3](#modifications-in-jjtree-0-2-3)
* [0.2.2](#modifications-in-jjtree-0-2-2)

---
### Modifications in JJTree 6.0

JJTree is extended to support C++ code generation starting with version 6.0.
The generated code mostly uses the same design as the Java version.
Note that there are still some kinks being worked on - like the NODE_FACTORY option etc.

Implemented all JavaCC options for the C++ version as well.

Implemented the following C++ specific options:
  - NODE_INCLUDES - a common include file for all the node classes.

Added support for C++ code generation for the tree builder.

Added the new option:
  -OUTPUT_LANGUAGE - string valued options with "java" and "c++" currently allowed.
  -VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME - include the name of the class being visited in the Visitor

---
### Modifications in JJTree 4.2

Release 4.2 is a maintenance release, incorporating a number of bug fixes and enhancements. For a complete list, please see the [issue tracker](https://javacc.dev.java.net/issues/buglist.cgi?component=javacc&field0-0-0=target_milestone&type0-0-0=equals&value0-0-0=4.2).

---
### Modifications in JJTree 4.1

Release 4.1 is a maintenance release, incorporating a number of bug fixes and enhancements. For a complete list, please see the [issue tracker](https://javacc.dev.java.net/issues/buglist.cgi?component=javacc&field0-0-0=target_milestone&type0-0-0=equals&value0-0-0=4.1).

---
### Modifications in JJTree 4.0

New option NODE_EXTENDS to specify a classname that
SimpleNode extends so that boiler plate code can be  put
in that class. See the examples/Interpreter for an example
usage.

---
### Modifications in JJTree 0.3-pre6

Fixed bug where Writers were not being closed correctly.

---
### Modifications in JJTree 0.3-pre5

Fixed a bug where a node annotation #P() caused a null pointer error.

Only generate the jjtCreate() methods if the NODE_FACTORY option is
set.

Fixed a bug where the name of the JJTree state file was being used in
the declaration of the field.

---
### Modifications in JJTree 0.3-pre4

Made the constructors of nodes public.  Also made the node identifier
constants and the associated strings public.

Fixed a misleading error message that was produced when the output
file couldn't be generated for some reason.

Brought the HTML documentation up to date.

Fixed a bug where the file containing the JJTree state class was
ignoring the OUTPUT_DIRECTORY option.

Fixed a bug where a construction like this:

```java
a=foo() #Foo
```

was being incorrectly handled and generating bad Java code.

Changed the visitor support from a void function to one which takes a
parameter and returns a result.  This is a non-compatible change, so
you will have to update your code if it uses the visitor support.

[Also, if the string option VISITOR_EXCEPTION is set, its value is
also used in the signatures of the various methods in the pattern.

Please note: this exception support is very provisional and will be
replaced in a following version by a more general solution.  It's only
here because I needed it for one of my own projects and thought it
might be useful to someone else too.  Don't use it unless you're
prepared to change your code again later.

---
### Modifications in JJTree 0.3-pre3

JJTree now uses the same grammar as JavaCC.  This means that Java code
in parser actions is parsed.

Added support for the Visitor design pattern.  If the VISITOR option
is true, JJTree adds an accept method to the node classes that it
generates, and also generates a visitor interface.  This interface is
regenerated every time that JJTree is run, so that new nodes will
cause compilation errors in concrete visitors that have not been
updated for them.

Added a couple of examples to illustrate the Visitor support.
JJTreeExamples/eg4.jjt is yet another version of the expression tree
builder which uses a visitor to dump the expression tree; and
VTransformer is a variation of the Java source code transformer.

VTransformer is also possibly directly useful as a tool that inserts
visitor accept methods into class files that were generated with
earlier versions of JJTree.

Added the BUILD_NODE_FILES option, with a default value of true.  If
set to false, it prevents JJTree from generating SimpleNode.java and
nodes that are usually built in MULTI mode.  Node.java is still
generated, as are the various tree constants, etc.

Code that is inserted into the grammar is now enclosed in the standard
@bgen/@egen pair.

The JJTree state object is now generated into its own file if it
doesn't already exist.  This is to make it easier to modify.

Fixed a couple of bugs in the HTML example grammar where the closing
tags didn't match the opening tags.

Fixed a bug where JJTree was trying to clear the node scope while
handling an exception, even when the node had been closed successfully.

NODE_FACTORY no longer implies NODE_USES_PARSER.

If you have been using NODE_FACTORY, then now you'll also need
NODE_USES_PARSER.  Unless, of course, you were never using the parser
in your node factories.

Removed not very useful debugging stuff from the JJTree state object.
It was causing problems with parsers running in security environments
where access to arbitrary properties is disallowed.

---
### Modifications in JJTree 0.3-pre2

The state that JJTree inserts into the parser class is now guarded by
the formal comments.

The JJTree syntax has been changed so that the node descriptor now
comes after the throws clause, rather than before it.

Fixed a bug where string-valued options did not have their quotes
stripped.

Fixed a bug where nodes were being closed early for actions within
ZeroOrMore etc., expansion units.

The special identifier 'jjtThis' was not being translated in parameter
lists or in the BNF declaration section.  Fixed it.

Added the OUTPUT_DIRECTORY option.  The default value is "".

Reinstated node factory methods.  They are enabled by setting the
NODE_FACTORY option to true.  Unlike the original node factory methods
they take two arguments: the node identifier constant and a reference
to the parser.  The reference is null for static parsers.

Added the NODE_USES_PARSER option with a default value of false.  When
set to true, JJTree will call the node constructor with a reference to
the parser object as an extra parameter.  This reference is null for
static parsers.

---
### Modifications in JJTree 0.3-pre1


JJTree 0.3pre1 has been bootstrapped with JJTree 0.2.6.

Some aspects of JJTree 0.3pre1 are not backwards-compatible with
0.2.6.  Some users will need to modify their parsers in
straightforward ways in order to work with the new version.  See the
file JJTREE-FIX to find out if you need to change anything, and for
detailed instructions on what to change.

JJTree works with the JavaCC exception handling code.  Any unhandled
exceptions within a node scope are caught, the node stack is cleaned
up a bit, and then the exception is rethrown.

Indefinite and Greater-Than nodes have been generalized into
conditional nodes.

Definite nodes now take any integer expression to indicate now many
children they take.  Conditional nodes take any boolean expression to
indicate whether the node is closed and pushed on to the node stack.

The life cycle of a node is now defined.

User-defined parser methods can be called when a node scope is entered
and exited.

The NODE_STACK_SIZE and CHECK_DEFINITE_NODE options are now ignored.

The NODE_SCOPE_HOOK option has been added.  This boolean option
determines whether calls to certain user-defined parser methods are
generated at the beginning and end of each node scope.  The default
value for this option is false.

The special identifier jjtThis can now be used in the declarations
section of a production, as well as on the left hand side of call to a
nonterminal.

A new method arity() has been added to the JJTree state.  It returns
the number of nodes that have been pushed so far in the current node
scope.

The Node interface has changed.  The method jjtAddChild() now takes an
argument to indicate the index of the child it is adding.

The node factory methods are no longer used.  You can remove all your
jjtCreate() methods.

The node constructor now takes an int parameter instead of a String.
You will have to modify your node implementations to use the new
signature.  The protected field 'identifier' no longer exists: you can
use the jjtNodeName[] array to map from the new parameter to the old
String.

The implementation of SimpleNode has changed significantly.  It now
uses an array to hold any child nodes instead of a Vector.  It no
longer implements a node factory, its constructor takes an int instead
of a String, and it uses the jjtNodeName[] mechanism for dumping.  The
setInfo() and getInfo() methods have been removed.

The implementation of the state that JJTree keeps in the parser has
changed.  It is now considerably more lightweight: the auxiliary
classes JJTreeNodeStack and JJTreeNodeStackEnum have been abolished.

The JJTree state method currentNode() has been removed.  Any calls to
the method in an action within a node scope are automatically replaced
by references to the special identifier jjtThis.

---
### Modifications in JJTree 0.2.6

Made appropriate internal modifications for the JavaCC 0.7 parse error exceptions.

---
### Modifications in JJTree 0.2.5

Fixed a bug where the current node was not being updated in the right
place for final user actions.

---
### Modifications in JJTree 0.2.4

Fixed a bug where bad code was generated for void nodes in MULTI mode.

Fixed a bug where a node decoration directly on an action generated
bad code. For example,

```java
{} #MyNode
```

---
### Modifications in JJTree 0.2.3

Added toString() and toString(String) methods to SimpleNode and
modified the dumping code to use them.  Now you can easily customize
how a node appears in the tree dump, without having to reproduce the
tree walking machinery.  See SimpleNode.java for details.

Clarified the concept of node scope. currentNode() now refers to the
node currently being built for the current scope.  It used to be
incorrectly implemented as referring to the most recently created
node, and was synonymous with peekNode().

This change may break some existing programs.  Those programs should
be changed to use peekNode() where they currently use currentNode().

Added jjtThis to every user action.  It refers to the same node that
currentNode() does, but is already cast to the appropriate node type.

The final user action in a node scope is different from all the
others.  When it is executed the node has been fully created, had its
children added, and has been pushed on the node stack.  By contrast,
other user actions within the scope are called when the children are
still on the stack, and the current node is not.

Added the nodeCreated() method so that final actions within greater
than nodes can tell whether the node was created or not.

Fixed several stupid bugs in the Macintosh main class.

Fixed names of internally used JJTree classes so that they use the JJT
prefix and the name of the parser.  This is to avoid naming conflicts
where there is more than one JJTree parser in the same package.

---
### Modifications in JJTree 0.2.2

The main change between Beanstalk 0.2 and JJTree 0.2.2 is the removal
of the factory classes.  Their function is now performed by a static
method in the node classes themselves.

The state maintained in the parser class has been changed from bs to
jjtree. The prefix on the Node class methods has been changed from bs
to jjt.

A new node method jjtGetNumChildren() returns the number of children
the node has.  You can use this in conjunction with jjtGetChild() to
iterate over the children.

Two new options have been added: OUTPUT_FILE and NODE_DEFAULT_VOID.

---
## JJDoc

### Version history

THIS FILE IS A COMPLETE LOG OF ALL CHANGES THAT HAVE TAKEN PLACE SINCE THE RELEASE OF VERSION 0.1.

#### 0.1.x

* [0.1.6](#modifications-in-jjdoc-0-1-6)
* [0.1.5](#modifications-in-jjdoc-0-1-5)
* [0.1.4](#modifications-in-jjdoc-0-1-4)
* [0.1.3](#modifications-in-jjdoc-0-1-3)
* [0.1.2](#modifications-in-jjdoc-0-1-2)
* [0.1.1](#modifications-in-jjdoc-0-1-1)
* [0.1.0](#modifications-in-jjdoc-0-1-0)

---
### Modifications in JJDoc 0.1.6

Refactored to make Generator a settable interface,
enabling Maven or other generators.
Existing Generator becomes TextGenerator.

---
### Modifications in JJDoc 0.1.5

Fixed bug where Writers were not being closed correctly.
Fixed bug where help message would not appear on command line.
Added the "CSS" option.

---
### Modifications in JJDoc 0.1.4

Now accepts JavaCC try/catch/finally blocks.

---
### Modifications in JJDoc 0.1.3

Made appropriate internal modifications for the JavaCC 0.7 parse error exceptions.

---
### Modifications in JJDoc 0.1.2

Fixed bug where ()+ expansions were being reported as ()\*.

---
### Modifications in JJDoc 0.1.1

Fixed several stupid bugs in the Macintosh main class.

Fixed bug where production comments were being generated multiple
times in text modes and one table HTML mode.

Moved production comments to the correct place: they were coming out
with the preceding production in one table HTML mode.

---
### Modifications in JJDoc 0.1.0

JJDoc takes a JavaCC parser specification and produces documentation
for the BNF grammar.  It can operate in three modes, determined by
command line options.

```java
TEXT                   (default false)
```

Setting TEXT to true causes JJDoc to generate a plain text format
description of the BNF.  Some formatting is done via tab characters,
but the intention is to leave it as plain as possible.

The default value of TEXT causes JJDoc to generate a hyperlinked HTML
document.

```java
ONE_TABLE              (default true)
```

The default value of ONE_TABLE is used to generate a single HTML table
for the BNF.  Setting it to false will produce one table for every
production in the grammar.

```java
OUTPUT_FILE
```

The default behavior is to put the JJDoc output into a file with
either .html or .txt added as a suffix to the input file's base name.
You can supply a different file name with this option.

---
## JavaCC (C++ version)

Javacc was extended to support C++ code generation starting with version 6.0. The generated code mostly uses the same design as the Java version.

### Version history

THIS FILE IS A COMPLETE LOG OF ALL CHANGES THAT HAVE TAKEN PLACE SINCE THE RELEASE OF VERSION 6.0.

#### 6.x

* [6.0](#modifications-in-javacc-cpp-6-0)
* [C++ specific issues](#c-specific-issues)

---
### Modifications in JavaCC CPP 6.0

Implemented all JavaCC options for the C++ version as well.

Added new options relevant to C++:
- `NAMESPACE` - namespace to be used for all the generated classes.
- `PARSER_INCLUDES` - a single include file that gets included in the parser. Use this to include all the declarations needed for your actions.
- `PARSER_SUPER_CLASS` - super class of the parser class (as we do not allow class declaration in `PARSER_BEGIN/PARSER_END` for C++).
- `TOKEN_INCLUDES` - a single include file for the token class.
- `TOKEN_MANAGER_INCLUDES` - a single include file for the `TokenManager`.
- `TOKEN_MANAGER_SUPER_CLASS` - super class of the token manager.
- `IGNORE_ACTIONS` - an option to ignore all the action so a clean parser can be generated even when `jjtree` is used. Useful for debugging/profiling/testing.

---
## C++ SPECIFIC ISSUES

Since C++ is somewhat different from Java in code organization in terms of header files etc. we made the following changes:
- Parser class in `PARSER_BEGIN/PARSER_END` - this cannot be supported as the parser is generated into the header file. So in the C++ version you can only define the contents of the class, not the class itself.
- Include files - in order to write any action code for the lexer/parser/tree, you might need to include header files. So we allow to specify what the header file is for each of the components - see the list of C++ options below for more details.
- Limitation on code in actions - since we don't have a full C++ parser embedded in the JavaCC grammar, we don't support arbitrary C++ code. So be aware of this when you write the actions. In general, it's a good idea to make them all function calls.
- Object lifetimes - in general, the parser deletes all the objects it creates - including the tokens. So any data that you want to pass out of the parser should be copied to your own objects that you can return. Note that tree created is a "return value" of the parser so that's not deleted.
- Exceptions - due to the way the code evolved, we could not use exceptions in C++. So we have an `ErrorHandler` interface instead which by default just prints syntax error and continues trying to parse. Please see the [JavaCC API](/documentation/api.md) for more details.
- Wide character support - C++ version is fully wide character compatible and it has been tested on Windows, Linux and Mac environments.

---
## JJTree (C++ version)

JJTree is extended to support C++ code generation starting with version 6.0. The generated code mostly uses the same design as the Java version.

Note that there are still some kinks being worked on - like the NODE_FACTORY option etc.

### Version history

THIS FILE IS A COMPLETE LOG OF ALL CHANGES THAT HAVE TAKEN PLACE SINCE THE RELEASE OF VERSION 6.0.

#### 6.x

* [6.0](#modifications-in-jjtree-cpp-6-0)

---
### Modifications in JJTree cpp-6.0

Implemented all JavaCC options for the C++ version.

Implemented the following C++ specific options:
- `NODE_INCLUDES` - a common include file for all the node classes.

---
[Home](index.md)

---