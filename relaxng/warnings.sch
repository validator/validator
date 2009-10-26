<?xml version="1.0"?>
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- RELAX NG Schema for HTML 5: Warnings                          -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<schema xmlns='http://www.ascc.net/xml/schematron'>
  <ns prefix='h' uri='http://www.w3.org/1999/xhtml'/>
  <pattern name="Warnings for HTML5 attributes that are obsolete but conforming">
    <rule context='h:meta'>
      <report test='translate(@http-equiv,"CONTELAGUA", "contelagua")="content-language"'>
        Using the &#x201C;meta&#x201D; element to specify the document-wide default language is obsolete.
        Consider specifying the language on the root element instead.
      </report>
    </rule>
    <rule context='h:img[@border]'>
      <report test='@border="0"'>
        The &#x201C;border&#x201D; attribute is obsolete.
        Consider specifying &#x201C;a img, img[usemap] { border: 0; }&#x201C; in CSS instead.
      </report>
    </rule>
    <rule context='h:script[translate(@language, "JAVSCRIPT", "javscript")="javascript"]'>
      <report test='not(@type) or translate(@type, "EXJAVSCRIPT", "exjavscript")="text/javascript"'>
        The &#x201C;language&#x201D; attribute is obsolete. It is not needed.
      </report>
    </rule>
    <rule context='h:a'>
      <report test='@name'>
        The &#x201C;name&#x201D; attribute is obsolete. Consider putting an
        &#x201C;id&#x201D; attribute on the nearest container instead.
      </report>
    </rule>
    <rule context='h:table'>
      <report test='@summary'>
        The &#x201C;summary&#x201D; attribute is obsolete.
        Consider describing the structure of complex tables in a &#x201C;caption&#x201D; element
        or in a paragraph, and pointing to the paragraph using the &#x201C;aria-describedby&#x201D;.
      </report>
    </rule>
  </pattern>
</schema>
