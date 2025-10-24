[Home](../index.md) > [Documentation](index.md) > JJDoc

---

JJDoc takes a JavaCC parser specification and produces documentation for the `BNF` grammar.

### <a name="cli"></a>Command line options

JJDoc can operate in several modes, determined by command line options.

| Option | Default | Description |
| :--- | :--- | :--- |
| `TEXT` | `false` | Setting `TEXT` to true causes JJDoc to generate a plain text format description of the `BNF`. Some formatting is done via tab characters, but the intention is to leave it as plain as possible. The default value of `TEXT` causes JJDoc to generate a hyperlinked HTML document.|
| `BNF` | `false` | Setting BNF to true causes JJDoc to generate a pure BNF document.|
| `ONE_TABLE` | `true` | The default value of ONE_TABLE is used to generate a single HTML table for the BNF. Setting it to false will produce one table for every production in the grammar.|
| `OUTPUT_FILE` | `<input dir>` | The default behavior is to put the JJDoc output into a file with either .html or .txt added as a suffix to the input file's base name. You can supply a different file name with this option.|
| `CSS` | `<css file>` | This option allows you to specify a `CSS` file name. If you supply a file name in this option it will appear in a `LINK` element in the `HEAD` section of the file. This option only applies to HTML output.|

Comments in the JavaCC source that immediately precede a production are passed through to the generated documentation.

### <a name="example"></a>Example

Example outputs from JJDoc for the JavaCC grammar are given as [text](javacc.txt) or [HTML](javacc.html).

<br>

---

You're done with the JavaCC documentation!

[Home](../index.md)

<br>
