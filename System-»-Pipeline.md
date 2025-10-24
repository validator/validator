The checker makes extensive use of SAX pipelines. When a document is
being validated, the main processing loop is in the parser that pulls
data from the `InputStream` and pushes out parse events.

These events are pushed through a pipeline of filters. Then each parse
event is repeated to each validator in a chain of side-by-side
validators. The parser, the filters and the validators push messages to
a SAX `ErrorHandler`. In the cases of the HTML, XHTML and XML output
formats, the `ErrorHandler` implementation in turn pushes out SAX events
corresponding to the generated result document.

## Parser Pipeline

### HTML Pipeline

1.  `nu.validator.htmlparser.sax.HtmlParser` (in streaming mode)
2.  `nu.validator.xml.WiretapXMLReaderWrapper` (feeding each event first
    to `nu.validator.source.LocationRecorder`)
3.  `nu.validator.xml.AttributesPermutingXMLReaderWrapper`
4.  Validators

### XML Pipeline

1.  `nu.validator.gnu.xml.aelfred2.SAXDriver`
2.  `nu.validator.xml.IdFilter`
3.  `nu.validator.xml.WiretapXMLReaderWrapper` (feeding each event first
    to `nu.validator.source.LocationRecorder`)
4.  `nu.validator.xml.NamespaceDroppingXMLReaderWrapper` (if there are
    namespaces to be filtered out)
5.  `nu.validator.xml.AttributesPermutingXMLReaderWrapper`
6.  `nu.validator.servlet.RootNamespaceSniffer` (if schema was left to
    autodetect)
7.  Validators

## Validator Chain

All (schema-based and non-schema-based) validators have to implement
`com.thaiopensource.validate.Validator`. Non-schema-based checkers
inherit from `nu.validator.checker.Checker` and are wrapped in
`nu.validator.checker.jing.CheckerValidator` to implement the required
interface.

When more than one schema is specified, the corresponding validators are
chained using `com.thaiopensource.relaxng.impl.CombineValidator`.

## Output Pipeline

1.  `nu.validator.messages.MessageEmitterAdapter` (implements
    `ErrorHandler`)
2.  `nu.validator.messages.MessageEmitter`

### HTML and XHTML

1.  `nu.validator.messages.XhtmlMessageEmitter` (extends
    `MessageEmitter`)
2.  `nu.validator.htmlparser.sax.HtmlSerializer` (HTML) /
    `nu.validator.htmlparser.sax.XmlSerializer` (XHTML)
3.  `java.io.OutputStream`

### XML

1.  `nu.validator.messages.XmlMessageEmitter` (extends `MessageEmitter`)
2.  `nu.validator.htmlparser.sax.XmlSerializer`
3.  `java.io.OutputStream`

### JSON

1.  `nu.validator.messages.JsonMessageEmitter` (extends
    `MessageEmitter`)
2.  `nu.validator.json.JsonHandler`
3.  `java.io.OutputStream`

### GNU

1.  `nu.validator.messages.GnuMessageEmitter` (extends `MessageEmitter`)
2.  `java.io.OutputStream`

### Text

1.  `nu.validator.messages.TextMessageEmitter` (extends
    `MessageEmitter`)
2.  `java.io.OutputStream`
