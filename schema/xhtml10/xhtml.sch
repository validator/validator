<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<!-- Mechanically extracted from RNG files which had this license: -->
<!--
Copyright (c) 2005 Petr Nalevka
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
<!-- Exclusions ported to Schematron by Henri Sivonen. -->

<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron" 
xmlns="http://www.ascc.net/xml/schematron" 
xmlns:rng="http://relaxng.org/ns/structure/1.0">
   <sch:ns xmlns="http://relaxng.org/ns/structure/1.0" prefix="html" uri="http://www.w3.org/1999/xhtml"/>
   
   <!-- start exclusions -->

	<pattern name='label cannot nest'>
		<rule context='html:label'>
			<report test='ancestor::html:label'>
				The &#x201C;label&#x201D; element cannot contain any nested 
				&#x201C;label&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='form cannot nest'>
		<rule context='html:form'>
			<report test='ancestor::html:form'>
				The &#x201C;form&#x201D; element cannot contain any nested 
				&#x201C;form&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='a'>
		<rule context='html:a'>
			<report test='ancestor::html:a'>
				The &#x201C;a&#x201D; element cannot contain any nested 
				&#x201C;a&#x201D; elements.
			</report>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;a&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='pre'>
		<rule context='html:pre'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;pre&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='img'>
		<rule context='html:img'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;img&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='object'>
		<rule context='html:object'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;object&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='applet'>
		<rule context='html:applet'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;applet&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='big'>
		<rule context='html:big'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;big&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='small'>
		<rule context='html:small'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;small&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='sub'>
		<rule context='html:sub'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;sub&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='sup'>
		<rule context='html:sup'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;sup&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='font'>
		<rule context='html:font'>
			<report test='ancestor::html:pre'>
				The &#x201C;pre&#x201D; element cannot contain any nested 
				&#x201C;font&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='input'>
		<rule context='html:input'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;input&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='select'>
		<rule context='html:select'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;select&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='textarea'>
		<rule context='html:textarea'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;textarea&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='input'>
		<rule context='html:input'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;input&#x201D; elements.
			</report>
		</rule>
	</pattern>
                            
	<pattern name='button'>
		<rule context='html:button'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;button&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='form'>
		<rule context='html:form'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;form&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='isindex'>
		<rule context='html:isindex'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;isindex&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='fieldset'>
		<rule context='html:fieldset'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;fieldset&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='iframe'>
		<rule context='html:iframe'>
			<report test='ancestor::html:button'>
				The &#x201C;button&#x201D; element cannot contain any nested 
				&#x201C;iframe&#x201D; elements.
			</report>
		</rule>
	</pattern>

   
   <!-- end exclusions -->
   
   
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="pre.content">
        
      <sch:rule context="html:pre">
            
         <sch:report test="html:img or html:object">
                 pre element cannot contain img and object elements.
            </sch:report>
        
      </sch:rule>
    
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="nested.a">
          
      <sch:rule context="html:a">
              
         <sch:report test="descendant::html:a">
                   a element cannot contain any nested a elements.
              </sch:report>
          
      </sch:rule>
      
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="a.href.hreflang">
    
      <sch:rule context="html:a">
      
         <sch:report test="(@hreflang) and not(@href)">
        If element a contains hreflang attribute href attribute is required as well.
      </sch:report>
    
      </sch:rule>
  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="button.img.usemap">
      
      <sch:rule context="html:button">
          
         <sch:report test="descendant::html:img[@usemap]">
               Images contained in button element shall not have usemap atribute specified.
          </sch:report>
      
      </sch:rule>
  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="select.multiple.selected.options">
          
      <sch:rule context="html:select">
              
         <sch:report test="not(@multiple) and count(html:option[@selected]) &gt; 1">
                   Select elements which aren't marked as multiple may not have more then one selected option.
              </sch:report>
          
      </sch:rule>
      
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="button.content">
          
      <sch:rule context="html:button">
              
         <sch:report test="html:form or html:a or html:input or html:select or html:textarea or html:button or html:label">
                   Button may contain all flow elements but excluding a, form and all form controls.
              </sch:report>
          
      </sch:rule>
      
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="id.name.attr">
    
      <sch:rule context="html:a | html:applet | html:form | html:frame | html:iframe | html:img | html:map">
        
         <sch:report test="string-length(@id) &gt; 0 and string-length(@name) &gt; 0 and @id != @name">
             Id and name attribute (if present both) needs to have to same value for element <sch:name/>.
        </sch:report>
    
      </sch:rule>

   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="id.name.attr">
      
      <sch:rule context="html:*">
        
         <sch:report test="string-length(@id) &gt; 0 and preceding::html:*[self::html:a | self::html:applet | self::html:form | self::html:frame | self::html:iframe | self::html:img | self::html:map]/@name = @id">
            The id and name attributes share the same name space. The id attribute
            of element <sch:name/> collides with the a name attribute of some preceding
            element.
        </sch:report>
        
         <sch:report test="string-length(@id) &gt; 0 and following::html:*[self::html:a | self::html:applet | self::html:form | self::html:frame | self::html:iframe | self::html:img | self::html:map]/@name = @id">
            The id and name attributes share the same name space. This means that
            they cannot both define an anchor with the same name in the same
            document. The id attribute
            of element <sch:name/> collides with the a name attribute of some following
            element.
        </sch:report>
      
      </sch:rule>
  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="button.content.trasitional">
  
      <sch:rule context="html:button">
      
         <sch:report test="html:iframe">
           Button may not contain iframe elements.
      </sch:report>
  
      </sch:rule>

   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="pre.content.transitional">
    
      <sch:rule context="html:pre">
        
         <sch:report test="html:applet or html:font or html:basefont or html:sub or html:sup or html:small or html:big or html:iframe or html:map">
             pre element cannot contain applet, map, iframe, big, small, font, basefont, sub and sup element.
        </sch:report>
    
      </sch:rule>

   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="param.attribute.name.required.transitional">
    
      <sch:rule context="html:param">
        
         <sch:assert test="html:param/@name">
             param element has a required name attribute.
        </sch:assert>
    
      </sch:rule>

   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="nested.form">
        
      <sch:rule context="html:form">
            
         <sch:report test="descendant::html:form">
                 form element cannot have any nested form elements
            </sch:report>
        
      </sch:rule>
    
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="input.radio.checkbox.value">
          
      <sch:rule context="html:input">
              
         <sch:report test="((@type = 'radio' or @type = 'checkbox') and (not(@value) or string-length(@value) = 0))">
                   Radio buttons and checkboxes need to have some value specified.
              </sch:report>
          
      </sch:rule>
      
   </sch:pattern>
   <sch:diagnostics/>
</sch:schema>