<?xml version="1.0"?>
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- RELAX NG Schema for HTML 5: Schematron Assertions             -->
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

  <!-- To validate an (X)HTML5 document, you must first validate   -->
  <!-- against the appropriate RELAX NG schema for the (X)HTML5    -->
  <!-- flavor and then also validate against this schema.          -->

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

<schema xmlns='http://www.ascc.net/xml/schematron'>
	<ns prefix='h' uri='http://www.w3.org/1999/xhtml'/>
                
<!-- Exclusions  - - - - - - - - - - - - - - - - - - - - - - - - - -->
    
    <!-- FIXME no nested forms in HTML-compatible docs -->
    
	<pattern name='dfn cannot nest'>
		<rule context='h:dfn'>
			<report test='ancestor::h:dfn'>
				The &#x201C;dfn&#x201D; element cannot contain any nested 
				&#x201C;dfn&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='label cannot nest'>
		<rule context='h:label'>
			<report test='ancestor::h:label'>
				The &#x201C;label&#x201D; element cannot contain any nested 
				&#x201C;label&#x201D; elements.
			</report>
		</rule>
	</pattern>

	<pattern name='blockquote not allowed in headers or footers'>
		<rule context='h:blockquote'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;blockquote&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;blockquote&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
		</rule>
	</pattern>

	<pattern name='section not allowed in headers or footers'>
		<rule context='h:section'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;section&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;section&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
		</rule>
	</pattern>

	<pattern name='nav not allowed in headers or footers'>
		<rule context='h:nav'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;nav&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;nav&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
		</rule>
	</pattern>

	<pattern name='article not allowed in headers or footers'>
		<rule context='h:article'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;article&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;article&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
		</rule>
	</pattern>

	<pattern name='aside not allowed in headers or footers'>
		<rule context='h:aside'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;aside&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;aside&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
		</rule>
	</pattern>

	<pattern name='header not allowed in headers or footers'>
		<rule context='h:header'>
			<report test='ancestor::h:header'>
				The &#x201C;header&#x201D; element cannot appear as a 
				descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The &#x201C;header&#x201D; element cannot appear as a 
				descendant of the &#x201C;footer&#x201D; element.
			</report>
		</rule>
	</pattern>

	<pattern name='footer not allowed in headers or footers'>
		<rule context='h:footer'>
			<report test='ancestor::h:header'>
				The &#x201C;footer&#x201D; element cannot appear as a 
				descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The &#x201C;footer&#x201D; element cannot appear as a 
				descendant of the &#x201C;footer&#x201D; element.
			</report>
		</rule>
	</pattern>
	
	<pattern name='At least one heading in header'>
		<rule context='h:header'>
			<assert test='count(descendant::h:h1 
			                  | descendant::h:h2 
			                  | descendant::h:h3 
			                  | descendant::h:h4 
			                  | descendant::h:h5 
			                  | descendant::h:h6) >= 1'>
				The &#x201C;header&#x201D; element must have at least one 
				&#x201C;h1&#x201D;&#x2013;&#x201C;h6&#x201D; descendant.
			</assert>
		</rule>
	</pattern>
	
<!-- IDREFs  - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

	<!-- Assuming that ID uniqueness is already enforced. -->

	<pattern name='contextmenu must refer to a menu'>
		<rule context='h:*[@contextmenu]'>
			<assert test='id(@contextmenu)/self::h:menu'>
				The &#x201C;contextmenu&#x201D; attribute must refer to a 
				&#x201C;menu&#x201D; element.
			</assert>
		</rule>
	</pattern>

	<pattern name='repeat-template must refer to a repetition template'>
		<rule context='h:*[@repeat-template]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='id(@repeat-template)/self::h:*[@repeat="template"]'>
				The &#x201C;repeat-template&#x201D; attribute must refer to a 
				repetition template.
			</assert>
		</rule>
	</pattern>

	<pattern name='for on label must refer to a form control'>
		<rule context='h:label[@for]'>
			<assert test='id(@for)/self::h:input or 
			              id(@for)/self::h:textarea or 
			              id(@for)/self::h:select or 
			              id(@for)/self::h:button or 
			              id(@for)/self::h:output'>
				The &#x201C;for&#x201D; attribute of the &#x201C;label&#x201D; 
				element must refer to a form control.
			</assert>
		</rule>
	</pattern>

	<pattern name='add button template must refer to a repetition template'>
		<rule context='h:input[@template and @type="add"]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='id(@template)/self::h:*[@repeat="template"]'>
				The &#x201C;template&#x201D; attribute of an 
				&#x201C;input&#x201D; element that is of 
				&#x201C;type="add"&#x201D; must refer to a repetition template.
			</assert>
		</rule>
		<rule context='h:button[@template and @type="add"]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='id(@template)/self::h:*[@repeat="template"]'>
				The &#x201C;template&#x201D; attribute of a 
				&#x201C;button&#x201D; element that is of 
				&#x201C;type="add"&#x201D; must refer to a repetition template.
			</assert>
		</rule>
	</pattern>

	<pattern name='list on input must refer to a select or a datalist'>
		<rule context='h:input[@list]'>
			<assert test='id(@list)/self::h:datalist or 
			              id(@list)/self::h:select'>
				The &#x201C;list&#x201D; attribute of the &#x201C;input&#x201D; 
				element must refer to a &#x201C;datalist&#x201D; element or to 
				a &#x201C;select&#x201D; element.
			</assert>
		</rule>
	</pattern>
		
	<!-- FIXME form attribute -->
	
	<!-- FIXME output for -->

<!-- Form Constraints  - - - - - - - - - - - - - - - - - - - - - - -->

	<pattern name='Non-multiple select can have up to one selected option'>
		<rule context='h:select[not(@multiple)]'>
			<report test='count(descendant::h:option[@selected]) > 1'>
				The &#x201C;select&#x201D; element cannot have more than one 
				selected &#x201C;option&#x201D; descendant unless the 
				&#x201C;multiple&#x201D; attribute is specified.
			</report>
		</rule>
	</pattern>

	<!-- REVISIT is this too constraining for scripted apps? -->
	<pattern name='remove, move-up and move-down only in repetition blocks/templates'>
		<rule context='h:input[@type=remove]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An &#x201C;input&#x201D; element of 
				&#x201C;type="remove"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[@type=remove]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A &#x201C;button&#x201D; element of 
				&#x201C;type="remove"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:input[@type=move-up]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An &#x201C;input&#x201D; element of 
				&#x201C;type="move-up"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[@type=move-up]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A &#x201C;button&#x201D; element of 
				&#x201C;type="move-up"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:input[@type=move-down]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An &#x201C;input&#x201D; element of 
				&#x201C;type="move-down"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[@type=move-down]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A &#x201C;button&#x201D; element of 
				&#x201C;type="move-down"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
	</pattern>

<!-- Unique Definitions  - - - - - - - - - - - - - - - - - - - - - -->
	
	<pattern name='Only one definition per term per document'>
		<rule context='h:dfn'>
			<report test='ancestor::h:dfn'>
				The &#x201C;dfn&#x201D; element cannot contain any nested 
				&#x201C;dfn&#x201D; elements.
			</report>
		</rule>
	</pattern>

	
	
	
	<!-- for meter enforce
minimum value ≤ actual value ≤ maximum value 
minimum value ≤ low boundary ≤ high boundary ≤ maximum value 
minimum value ≤ optimum point ≤ maximum value -->
</schema>
