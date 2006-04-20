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
                
<!-- Exclusions - - - - - - - - - - - - - - - - - - - - - - - - -  -->
    
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
				The blockquote element cannot appear as a descendant of the header element.
			</report>
			<report test='ancestor::h:footer'>
				The blockquote element cannot appear as a descendant of the footer element.
			</report>
		</rule>
	</pattern>
	
	<!-- FIXME exactly one hn in header -->

<!-- IDREFs - - - - - - - - - - - - - - - - - - - - - - - - - - -  -->

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
	

</schema>
