[Home](../index.md) > [Documentation](index.md) > Command Line Interface

---

You can obtain a synopsis of the command line syntax by simply typing:

```java
$ javacc
```

Output:

```
Java Compiler Compiler Version 7.0.nn (Parser Generator)

Usage:
    javacc option-settings inputfile

"option-settings" is a sequence of settings separated by spaces.
Each option setting must be of one of the following forms:

    -optionname=value (e.g., -STATIC=false)
    -optionname:value (e.g., -STATIC:false)
    -optionname       (equivalent to -optionname=true.  e.g., -STATIC)
    -NOoptionname     (equivalent to -optionname=false. e.g., -NOSTATIC)

Option settings are not case-sensitive, so one can say "-nOsTaTiC" instead
of "-NOSTATIC".  Option values must be appropriate for the corresponding
option, and must be either an integer, a boolean, or a string value.

The integer valued options are:

    CHOICE_AMBIGUITY_CHECK (default : 2)
    DEPTH_LIMIT            (default : 0)
    LOOKAHEAD              (default : 1)
    OTHER_AMBIGUITY_CHECK  (default : 1)

The boolean valued options are:

    BUILD_PARSER                    (default : true)
    BUILD_TOKEN_MANAGER             (default : true)
    CACHE_TOKENS                    (default : false)
    COMMON_TOKEN_ACTION             (default : false)
    DEBUG_LOOKAHEAD                 (default : false)
    DEBUG_PARSER                    (default : false)
    DEBUG_TOKEN_MANAGER             (default : false)
    ERROR_REPORTING                 (default : true)
    FORCE_LA_CHECK                  (default : false)
    GENERATE_ANNOTATIONS            (default : false)
    GENERATE_BOILERPLATE            (default : true)
    GENERATE_CHAINED_EXCEPTION      (default : false)
    GENERATE_GENERICS               (default : false)
    GENERATE_STRING_BUILDER         (default : false)
    IGNORE_CASE                     (default : false)
    JAVA_UNICODE_ESCAPE             (default : false)
    KEEP_LINE_COLUMN                (default : true)
    NO_DFA                          (default : false)
    SANITY_CHECK                    (default : true)
    STATIC                          (default : true)
    SUPPORT_CLASS_VISIBILITY_PUBLIC (default : true)
    TOKEN_MANAGER_USES_PARSER       (default : false)
    UNICODE_INPUT                   (default : false)
    USER_CHAR_STREAM                (default : false)
    USER_TOKEN_MANAGER              (default : false)

The string valued options are:

    GRAMMAR_ENCODING             (default : <<empty>>)
    JAVA_TEMPLATE_TYPE           (default : classic)
    JDK_VERSION                  (default : 1.5)
    OUTPUT_DIRECTORY             (default : .)
    PARSER_CODE_GENERATOR        (default : <<empty>>)
    PARSER_SUPER_CLASS
    TOKEN_EXTENDS                (default : <<empty>>)
    TOKEN_FACTORY                (default : <<empty>>)
    TOKEN_INCLUDE                (default : <<empty>>)
    TOKEN_MANAGER_CODE_GENERATOR (default : <<empty>>)
    TOKEN_MANAGER_INCLUDE        (default : <<empty>>)
    TOKEN_MANAGER_SUPER_CLASS
    TOKEN_SUPER_CLASS
    
EXAMPLE:
    javacc -STATIC=false -LOOKAHEAD:2 -debug_parser mygrammar.jj

ABOUT JavaCC:

    JavaCC is a parser generator for the Java programming language.
```

Any option may be set either on the command line as shown above, or in the grammar file as described in the [JavaCC grammar](grammar.md). The effect is exactly the same.

If the same option is set in both the command line and the grammar file, then the option setting in the command line takes precedence.

<br>

---

[NEXT >>](grammar.md)

<br>
