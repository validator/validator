<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<!-- Mechanically extracted from RNG files which had this license: -->
<!--
Copyright (c) 2005 Petr Nalevka
Copyright (c) 2007-2010 Mozilla Foundation
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
-->

<!-- Use of value-of removed from from reports by Henri Sivonen. -->
<!-- Exclusions and for IDREF ported to Schematron by Henri Sivonen. -->

<schema  
xmlns="http://www.ascc.net/xml/schematron">
  <ns prefix="html" uri="http://www.w3.org/1999/xhtml"/>
   
  <!-- lang and xml:lang in XHTML  - - - - - - - - - - - - - - - - - -->

	<pattern name='lang and xml:lang in XHTML'>
		<rule context='html:*[@lang and @xml:lang]'>
			<assert test='@lang = @xml:lang'>
				When the attribute &#x201C;lang&#x201D; is specified, the element must also have 
				the attribute &#x201C;lang&#x201D; in the XML namespace present with the same 
				value.
			</assert>
		</rule>
		<rule context='html:*[@lang and not(@xml:lang)]'>
			<report test='true()'>
				When the attribute &#x201C;lang&#x201D; is specified, the element must also have 
				the attribute &#x201C;lang&#x201D; in the XML namespace present with the same 
				value.
			</report>
		</rule>
	</pattern>

	<pattern name='general'>
   <!-- start exclusions -->

		<rule context='html:label'>
			<report test='ancestor::html:label'>
				The &#x201C;label&#x201D; element cannot contain any nested 
				&#x201C;label&#x201D; elements.
			</report>
		</rule>

		<rule context='html:form'>
			<report test='ancestor::html:form'>
				The &#x201C;form&#x201D; element cannot contain any nested 
				&#x201C;form&#x201D; elements.
			</report>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;form&#x201D; elements.
			</report>
		</rule>

		<rule context='html:a'>
			<report test='ancestor::html:a'>
				The &#x201C;a&#x201D; element cannot contain any nested 
				&#x201C;a&#x201D; elements.
			</report>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;a&#x201D; elements.
			</report>
      <report test="(@hreflang) and not(@href)">
        If element &#x201C;a&#x201D; has an &#x201C;hreflang&#x201D; attribute, the &#x201C;href&#x201D; attribute is required as well.
      </report>
		</rule>

		<rule context='html:pre'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;pre&#x201D; elements.
      </report>
		</rule>

		<rule context='html:img'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;img&#x201D; elements.
			</report>
		</rule>

		<rule context='html:object'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;object&#x201D; elements.
			</report>
		</rule>

		<rule context='html:applet'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;applet&#x201D; elements.
			</report>
		</rule>

		<rule context='html:big'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;big&#x201D; elements.
			</report>
		</rule>

		<rule context='html:small'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;small&#x201D; elements.
			</report>
		</rule>

		<rule context='html:sub'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;sub&#x201D; elements.
			</report>
		</rule>

		<rule context='html:sup'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;sup&#x201D; elements.
			</report>
		</rule>

		<rule context='html:font'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;font&#x201D; elements.
			</report>
		</rule>

		<rule context='html:input'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;input&#x201D; elements.
			</report>
      <report test="((@type = 'radio' or @type = 'checkbox') and (not(@value) or string-length(@value) = 0))">
                   Radio buttons and checkboxes need to have a value specified.
      </report>
		</rule>

		<rule context='html:select'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;select&#x201D; elements.
			</report>
      <report test="not(@multiple) and count(html:option[@selected]) &gt; 1">
                   A &#x201C;select&#x201D; element that is not marked as multiple may not have more then one selected &#x201C;option&#x201D;.
      </report>
 		</rule>

		<rule context='html:textarea'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;textarea&#x201D; elements.
			</report>
		</rule>

		<rule context='html:button'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;button&#x201D; elements.
			</report>
		</rule>

		<rule context='html:isindex'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;isindex&#x201D; elements.
			</report>
		</rule>

		<rule context='html:fieldset'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;fieldset&#x201D; elements.
			</report>
		</rule>

		<rule context='html:iframe'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;iframe&#x201D; elements.
			</report>
		</rule>

	</pattern>
   
   
   <!-- IDREF -->

 	<pattern name='second'>
		<rule context='html:label[@for]'>
			<assert test='id(@for)/self::html:input or 
			              id(@for)/self::html:textarea or 
			              id(@for)/self::html:select or 
			              id(@for)/self::html:button or 
			              id(@for)/self::html:output'>
				The &#x201C;for&#x201D; attribute of the &#x201C;label&#x201D; 
				element must refer to a form control.
			</assert>
		</rule>
    <rule context="html:img[@usemap]">
      <report test="ancestor::button">
               Images contained in button element shall not have usemap atribute specified.
      </report>
    </rule>


	</pattern>
   
   <!-- end IDREF -->
   
   <!--

   <pattern xmlns="http://relaxng.org/ns/structure/1.0" name="id.name.attr">
    
      <rule context="html:a | html:applet | html:form | html:frame | html:iframe | html:img | html:map">
        
         <report test="string-length(@id) &gt; 0 and string-length(@name) &gt; 0 and @id != @name">
             Id and name attribute (if present both) needs to have to same value for element <name/>.
        </report>
    
      </rule>

   </pattern>
   
   <pattern xmlns="http://relaxng.org/ns/structure/1.0" name="id.name.attr">
      
      <rule context="html:*">
        
         <report test="string-length(@id) &gt; 0 and preceding::html:*[self::html:a | self::html:applet | self::html:form | self::html:frame | self::html:iframe | self::html:img | self::html:map]/@name = @id">
            The id and name attributes share the same name space. The id attribute
            of element <name/> collides with the a name attribute of some preceding
            element.
        </report>
        
         <report test="string-length(@id) &gt; 0 and following::html:*[self::html:a | self::html:applet | self::html:form | self::html:frame | self::html:iframe | self::html:img | self::html:map]/@name = @id">
            The id and name attributes share the same name space. This means that
            they cannot both define an anchor with the same name in the same
            document. The id attribute
            of element <name/> collides with the a name attribute of some following
            element.
        </report>
      
      </rule>
  
   </pattern>

-->

</schema>
