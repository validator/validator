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
    
	<pattern name='dfn cannot nest'> <!-- FIXME port to RNG also -->
		<rule context='h:dfn'>
			<report test='ancestor::h:dfn'>
				The dfn element cannot contain any nested dfn elements.
			</report>
		</rule>
	</pattern>

	<pattern name='label cannot nest'> <!-- FIXME port to RNG also -->
		<rule context='h:label'>
			<report test='ancestor::h:label'>
				The label element cannot contain any nested label elements.
			</report>
		</rule>
	</pattern>

	<pattern name='blockquote not allowed in headers or footers'> <!-- FIXME port to RNG also -->
		<rule context='h:blockquote'>
			<report test='ancestor::h:header'>
				The blockquote element cannot appear as a descendant of the 
				header element.
			</report>
			<report test='ancestor::h:footer'>
				The blockquote element cannot appear as a descendant of the 
				footer element.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Exactly one heading in header'>
		<rule context='h:header'>
			<assert test='count(descendant::h:h1 
			                  | descendant::h:h2 
			                  | descendant::h:h3 
			                  | descendant::h:h4 
			                  | descendant::h:h5 
			                  | descendant::h:h6) = 1'>
				The header element must have exactly one h1&#x2013;h6 descendant.
			</assert>
		</rule>
	</pattern>

<!-- IDREFs  - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

	<!-- Assuming that ID uniqueness is already enforced. -->

	<pattern name='contextmenu must refer to a menu'>
		<rule context='*[@contextmenu]'>
			<assert test='/descendant::h:menu/@id = @contextmenu or 
			              /descendant::h:menu/@xml:id = @contextmenu'>
				The contextmenu attribute must refer to a menu element.
			</assert>
		</rule>
	</pattern>

	<pattern name='repeat-template must refer to a repetition template'>
		<rule context='*[@repeat-template]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='/descendant::h:*[@repeat="template"]/@id = @repeat-template or 
			              /descendant::h:*[@repeat="template"]/@xml:id = @repeat-template'>
				The repeat-template attribute must refer to a repetition template.
			</assert>
		</rule>
	</pattern>

	<pattern name='for on label must refer to a form control'>
		<rule context='h:label[@for]'>
			<assert test='/descendant::h:input/@id = @for or 
			              /descendant::h:input/@xml:id = @for or
			              /descendant::h:textarea/@id = @for or 
			              /descendant::h:textarea/@xml:id = @for or
			              /descendant::h:select/@id = @for or 
			              /descendant::h:select/@xml:id = @for or
			              /descendant::h:button/@id = @for or 
			              /descendant::h:button/@xml:id = @for or
			              /descendant::h:output/@id = @for or 
			              /descendant::h:output/@xml:id = @for'>
				The for attribute of the label element must refer to a form 
				control.
			</assert>
		</rule>
	</pattern>

	<pattern name='add button template must refer to a repetition template'>
		<rule context='h:input[@template and @type="add"]'>
			<assert test='/descendant::h:*[@repeat="template"]/@id = @template or 
			              /descendant::h:*[@repeat="template"]/@xml:id = @template'>
				The template attribute of an input element that is of type="add" 
				must refer to a repetition template.
			</assert>
		</rule>
		<rule context='h:button[@template and @type="add"]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='/descendant::h:*[@repeat="template"]/@id = @template or 
			              /descendant::h:*[@repeat="template"]/@xml:id = @template'>
				The template attribute of an button element that is of 
				type="add" must refer to a repetition template.
			</assert>
		</rule>
	</pattern>

	<pattern name='list on input must refer to a select or a datalist'>
		<rule context='h:input[@list]'>
			<assert test='/descendant::h:datalist/@id = @list or 
			              /descendant::h:datalist/@xml:id = @list or
			              /descendant::h:select/@id = @list or 
			              /descendant::h:select/@xml:id = @list'>
				The list attribute of the input element must refer to a 
				datalist element or to a select element.
			</assert>
		</rule>
	</pattern>
		
	<!-- FIXME check that they are in the same table -->
	<!--<pattern name='headers must refer to th elements'>
		<rule context='h:td[@headers]'>
			<key name='id' path='@id'/>
			<assert test='key("id", @headers)'>
				headers must refer to th elements.
			</assert>
		</rule>
	</pattern>-->

	<!-- FIXME form attribute -->
	
	<!-- FIXME output for -->

<!-- Form Constraints  - - - - - - - - - - - - - - - - - - - - - - -->

	<pattern name='Non-multiple select can have up to one selected option'>
		<rule context='h:select[not(@multiple)]'>
			<report test='count(descendant::h:option[@selected]) > 1'>
				The select element cannot have more than one selected option 
				unless the multiple attribute is specified.
			</report>
		</rule>
	</pattern>

	<!-- REVISIT is this too constraining for scripted apps? -->
	<pattern name='remove, move-up and move-down only in repetition blocks/templates'>
		<rule context='h:input[@type=remove]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An input element of type="remove" must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[@type=remove]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A button element of type="remove" must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:input[@type=move-up]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An input element of type="move-up" must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[@type=move-up]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A button element of type="move-up" must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:input[@type=move-down]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An input element of type="move-down" must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[@type=move-down]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A button element of type="move-down" must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
	</pattern>

<!-- Unique Definitions  - - - - - - - - - - - - - - - - - - - - - -->
	
	<pattern name='Only one definition per term per document'>
		<rule context='h:dfn'>
			<report test='ancestor::h:dfn'>
				The dfn element cannot contain any nested dfn elements.
			</report>
		</rule>
	</pattern>

	
	<!-- for meter enforce
minimum value ≤ actual value ≤ maximum value 
minimum value ≤ low boundary ≤ high boundary ≤ maximum value 
minimum value ≤ optimum point ≤ maximum value -->
</schema>
