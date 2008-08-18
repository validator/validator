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

    
	<pattern name='Triggered on mutually exclusive elements'>

	<!-- Exclusions  - - - - - - - - - - - - - - - - - - - - - - - - - -->

		<rule context='h:form'>
			<report test='ancestor::h:form'>
				The &#x201C;form&#x201D; element cannot contain any nested 
				&#x201C;form&#x201D; elements.
			</report>
		</rule>

		<rule context='h:dfn'>
			<report test='ancestor::h:dfn'>
				The &#x201C;dfn&#x201D; element cannot contain any nested 
				&#x201C;dfn&#x201D; elements.
			</report>
		</rule>

		<rule context='h:noscript'>
			<report test='ancestor::h:noscript'>
				The &#x201C;noscript&#x201D; element cannot contain any nested 
				&#x201C;noscript&#x201D; elements.
			</report>
		</rule>

		<rule context='h:label'>
			<report test='ancestor::h:label'>
				The &#x201C;label&#x201D; element cannot contain any nested 
				&#x201C;label&#x201D; elements.
			</report>
		</rule>

		<rule context='h:address'>
			<report test='ancestor::h:address'>
				The &#x201C;address&#x201D; element cannot contain any nested 
				&#x201C;address&#x201D; elements.
			</report>
		</rule>

		<rule context='h:blockquote'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;blockquote&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The sectioning element &#x201C;blockquote&#x201D; cannot 
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:section'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;section&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;section&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The sectioning element &#x201C;section&#x201D; cannot 
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:nav'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;nav&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;nav&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The sectioning element &#x201C;nov&#x201D; cannot 
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:article'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;article&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;article&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The sectioning element &#x201C;article&#x201D; cannot 
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:aside'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;aside&#x201D; cannot 
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;aside&#x201D; cannot 
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The sectioning element &#x201C;aside&#x201D; cannot 
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:header'>
			<report test='ancestor::h:header'>
				The &#x201C;header&#x201D; element cannot appear as a 
				descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The &#x201C;header&#x201D; element cannot appear as a 
				descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The &#x201C;header&#x201D; element cannot appear as a 
				descendant of the &#x201C;address&#x201D; element.
			</report>

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

		<rule context='h:footer'>
			<report test='ancestor::h:header'>
				The &#x201C;footer&#x201D; element cannot appear as a 
				descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The &#x201C;footer&#x201D; element cannot appear as a 
				descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The &#x201C;footer&#x201D; element cannot appear as a 
				descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:h1|h:h2|h:h3|h:h4|h:h5|h:h6'>
			<report test='ancestor::h:footer'>
				The &#x201C;h1&#x201D;&#x2013;&#x201C;h6&#x201D; elements 
				cannot appear as descendants of the &#x201C;footer&#x201D; 
				element.
			</report>
			<report test='ancestor::h:address'>
				The &#x201C;h1&#x201D;&#x2013;&#x201C;h6&#x201D; elements 
				cannot appear as descendants of the &#x201C;address&#x201D; 
				element.
			</report>
		</rule>

	<!-- Interactive element exclusions -->
	
		<!-- 
		   - Interactive descendants:
		   - a
		   - video[controls]
		   - audio[controls]
		   - details
		   - datagrid
		   - bb
		   - menu[type=toolbar]
		   - button
		   - input[type!=hidden]
		   - textarea
		   - select
		   -
		   - Interactive ancestors
		   - a
		   - button
		   - bb
		  -->

		<rule context='h:a'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;a&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;a&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;a&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:datagrid'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;datagrid&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;datagrid&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;datagrid&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:details'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;details&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;details&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;details&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:button'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;button&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;button&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;button&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:textarea'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;textarea&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;textarea&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;textarea&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:select'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;select&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;select&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;select&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:input[not(translate(@type, "HIDEN", "hiden")="hidden")]'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;input&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;input&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;input&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:bb'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;bb&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;bb&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;bb&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>
		
		<rule context='h:menu[translate(@type, "TOLBAR", "tolbar")="toolbar"]'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;menu type=toolbar&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;menu type=toolbar&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;menu type=toolbar&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:video[@controls]'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;video controls&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;video controls&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;video controls&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:audio[@controls]'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;audio controls&#x201D; cannot 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;audio controls&#x201D; cannot 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;audio controls&#x201D; cannot 
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

	<!-- REVISIT fieldset http://lists.whatwg.org/pipermail/whatwg-whatwg.org/2006-April/006181.html -->

	<!-- Misc requirements -->
		
		<rule context='h:area'>
			<assert test='ancestor::h:map'>
				The &#x201C;area&#x201D; element must have a 
				&#x201C;map&#x201D; ancestor.
			</assert>
		</rule>

		<rule context='h:img[@ismap]'>
			<assert test='ancestor::h:a[@href]'>
				The &#x201C;img&#x201D; element with the 
				&#x201C;ismap&#x201D; attribute set must have an 
				&#x201C;a&#x201D; ancestor with the &#x201C;href&#x201D; 
				attribute.
			</assert>
		</rule>

		<rule context='h:progress[@max and @value]'>
			<assert test='number(@value) &lt;= number(@max)'>
				The value of the  &#x201C;value&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</assert>
		</rule>

		<rule context='h:progress[not(@max) and @value]'>
			<assert test='number(@value) &lt;= 1'>
				The value of the  &#x201C;value&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</assert>
		</rule>

		<!-- 
			min <= value <= max
    		min <= low <= high <= max
			min <= optimum <= max 
		-->

		<rule context='h:meter'>
			<report test='@min and @value and not(number(@min) &lt;= number(@value))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;value&#x201D; attribute.
			</report>
			<report test='not(@min) and @value and not(0 &lt;= number(@value))'>
				The value of the &#x201C;value&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@value and @max and not(number(@value) &lt;= number(@max))'>
				The value of the  &#x201C;value&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='@value and not(@max) and not(number(@value) &lt;= 1)'>
				The value of the  &#x201C;value&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
			<report test='@min and @max and not(number(@min) &lt;= number(@max))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='not(@min) and @max and not(0 &lt;= number(@max))'>
				The value of the &#x201C;max&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@min and not(@max) and not(number(@min) &lt;= 1)'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
			<report test='@min and @low and not(number(@min) &lt;= number(@low))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;low&#x201D; attribute.
			</report>
			<report test='not(@min) and @low and not(0 &lt;= number(@low))'>
				The value of the &#x201C;low&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@min and @high and not(number(@min) &lt;= number(@high))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;high&#x201D; attribute.
			</report>
			<report test='not(@min) and @high and not(0 &lt;= number(@high))'>
				The value of the &#x201C;high&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@low and @high and not(number(@low) &lt;= number(@high))'>
				The value of the  &#x201C;low&#x201D; attribute must be less than or equal to
				the value of the &#x201C;high&#x201D; attribute.
			</report>
			<report test='@high and @max and not(number(@high) &lt;= number(@max))'>
				The value of the  &#x201C;high&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='@high and not(@max) and not(number(@high) &lt;= 1)'>
				The value of the  &#x201C;high&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
			<report test='@low and @max and not(number(@low) &lt;= number(@max))'>
				The value of the  &#x201C;low&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='@low and not(@max) and not(number(@low) &lt;= 1)'>
				The value of the  &#x201C;low&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
			<report test='@min and @optimum and not(number(@min) &lt;= number(@optimum))'>
				The value of the  &#x201C;min&#x201D; attribute must be less than or equal to
				the value of the &#x201C;optimum&#x201D; attribute.
			</report>
			<report test='not(@min) and @optimum and not(0 &lt;= number(@optimum))'>
				The value of the &#x201C;optimum&#x201D; attribute must be greater than or equal to
				zero when the &#x201C;min&#x201D; attribute is absent.
			</report>
			<report test='@optimum and @max and not(number(@optimum) &lt;= number(@max))'>
				The value of the  &#x201C;optimum&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</report>
			<report test='@optimum and not(@max) and not(number(@optimum) &lt;= 1)'>
				The value of the  &#x201C;optimum&#x201D; attribute must be less than or equal to
				one when the &#x201C;max&#x201D; attribute is absent.
			</report>
		</rule>


	<!-- Obsolete Elements - - - - - - - - - - - - - - - - - - - - - - -->

		<rule context='h:center'>
			<report test='true()'>
				The &#x201C;center&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:font'>
			<report test='true()'>
				The &#x201C;font&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:big'>
			<report test='true()'>
				The &#x201C;big&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:s'>
			<report test='true()'>
				The &#x201C;s&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:strike'>
			<report test='true()'>
				The &#x201C;strike&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:tt'>
			<report test='true()'>
				The &#x201C;tt&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:u'>
			<report test='true()'>
				The &#x201C;u&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:acronym'>
			<report test='true()'>
				The &#x201C;acronym&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:dir'>
			<report test='true()'>
				The &#x201C;dir&#x201D; element is obsolete.
			</report>
		</rule>

		<rule context='h:applet'>
			<report test='true()'>
				The &#x201C;applet&#x201D; element is obsolete.
			</report>
		</rule>

	<!-- Encoding Declaration -->

		<rule context='/h:html/h:head/h:meta[@charset]
		             | /h:html/h:head/h:meta["content-type" = translate(@http-equiv, "CONTEYP", "conteyp")]'>
			<assert test='position()=1'>
				The internal character encoding declaration must be the first child of 
				the &#x201C;head&#x201D; element.
			</assert>
		</rule>

	<!-- required attributes  - - - - - - - - - - - - - - - - - - - -->

		<rule context='h:map[@id and @name]'>
			<assert test='@id = @name'>
				The &#x201C;id&#x201D; attribute on a &#x201C;map&#x201D; element must have an 
				the same value as the &#x201C;name&#x201D; attribute.
			</assert>
		</rule>

		<rule context='h:bdo[not(@dir)]'>
			<report test='true()'>
				A &#x201C;bdo&#x201D; element must have an 
				&#x201C;dir&#x201D; attribute.
			</report>
		</rule>
		
	<!-- table in datagrid -->
	
		<rule context='h:datagrid/h:table'>
			<assert test="( (not(((../*)|(../text()[normalize-space()]))[2]))
							 or
							 ((preceding-sibling::*) or (preceding-sibling::text()[normalize-space()])) )">
				When a &#x201C;table&#x201D; is the first child of &#x201C;datagrid&#x201D;, it 
				must not have following siblings.
			</assert>
		</rule>

		<rule context='h:datagrid/h:select'>
			<assert test="( (not(((../*)|(../text()[normalize-space()]))[2]))
							 or
							 ((preceding-sibling::*) or (preceding-sibling::text()[normalize-space()])) )">
				When a &#x201C;select&#x201D; element is the first child of a 
				&#x201C;datagrid&#x201D; element, it must not have following siblings.
			</assert>
		</rule>

		<rule context='h:datagrid/h:datalist'>
			<assert test="( (not(((../*)|(../text()[normalize-space()]))[2]))
							 or
							 ((preceding-sibling::*) or (preceding-sibling::text()[normalize-space()])) )">
				When a &#x201C;datalist&#x201D; element is the first child of a 
				&#x201C;datagrid&#x201D; element, it must not have following siblings.
			</assert>
		</rule>

	</pattern>

<!-- lang and xml:lang in XHTML  - - - - - - - - - - - - - - - - - -->

	<pattern name='lang and xml:lang in XHTML'>
		<rule context='h:*[@lang and @xml:lang]'>
			<assert test='@lang = @xml:lang'>
				When the attribute &#x201C;lang&#x201D; is specified, the element must also have 
				the attribute &#x201C;lang&#x201D; in the XML namespace present with the same 
				value.
			</assert>
		</rule>
		<rule context='h:*[@lang and not(@xml:lang)]'>
			<report test='true()'>
				When the attribute &#x201C;lang&#x201D; is specified, the element must also have 
				the attribute &#x201C;lang&#x201D; in the XML namespace present with the same 
				value.
			</report>
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
			<assert test='id(@repeat-template)/self::h:*[translate(@repeat, "TEMPLA", "templa")="template"]'>
				The &#x201C;repeat-template&#x201D; attribute must refer to a 
				repetition template.
			</assert>
		</rule>
	</pattern>

	<pattern name='for on label must refer to a form control'>
		<rule context='h:label[@for]'>
			<assert test='id(@for)/self::h:input[not(translate(@type, "HIDEN", "hiden")="hidden")] or 
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
		<rule context='h:input[@template and translate(@type, "AD", "ad")="add"]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='id(@template)/self::h:*[translate(@repeat, "TEMPLA", "templa")="template"]'>
				The &#x201C;template&#x201D; attribute of an 
				&#x201C;input&#x201D; element that is of 
				&#x201C;type="add"&#x201D; must refer to a repetition template.
			</assert>
		</rule>
		<rule context='h:button[@template and translate(@type, "AD", "ad")="add"]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='id(@template)/self::h:*[translate(@repeat, "TEMPLA", "templa")="template"]'>
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
		<rule context='h:input[translate(@type, "REMOV", "remov")=remove]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An &#x201C;input&#x201D; element of 
				&#x201C;type="remove"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[translate(@type, "REMOV", "remov")=remove]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A &#x201C;button&#x201D; element of 
				&#x201C;type="remove"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:input[translate(@type, "MOVEUP", "moveup")=move-up]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An &#x201C;input&#x201D; element of 
				&#x201C;type="move-up"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[translate(@type, "MOVEUP", "moveup")=move-up]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A &#x201C;button&#x201D; element of 
				&#x201C;type="move-up"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:input[translate(@type, "MOVEDWN", "movedwn")=move-down]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				An &#x201C;input&#x201D; element of 
				&#x201C;type="move-down"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
		<rule context='h:button[translate(@type, "MOVEDWN", "movedwn")=move-down]'>
			<!-- REVISIT deal with SVG, MathML, XUL, etc. later -->
			<assert test='ancestor::h:*[@repeat]'>
				A &#x201C;button&#x201D; element of 
				&#x201C;type="move-down"&#x201D; must have a repetition block 
				or a repetition template as an ancestor.
			</assert>
		</rule>
	</pattern>


<!-- Unique Definitions  - - - - - - - - - - - - - - - - - - - - - -->
	
	<!-- Only one definition per term per document' -->

<!-- ARIA containment    - - - - - - - - - - - - - - - - - - - - - -->

	<pattern name='Mutually Exclusive Role triggers'>

		<rule context='*[@role="option"]'>
			<assert test='../@role="listbox" or
			              ../@role="combobox"'>
				An element with &#x201C;role=option&#x201D; requires 
				&#x201C;role=listbox&#x201D; or &#x201C;role=combobox&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="menuitem"]'>
			<assert test='../@role="menu"'>
				An element with &#x201C;role=menuitem&#x201D; requires 
				&#x201C;role=menu&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="menuitemcheckbox"]'>
			<assert test='../@role="menu"'>
				An element with &#x201C;role=menuitemcheckbox&#x201D; requires 
				&#x201C;role=menu&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="menuitemradio"]'>
			<assert test='../@role="menu"'>
				An element with &#x201C;role=menuitemradio&#x201D; requires 
				&#x201C;role=menu&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="tab"]'><!-- XXX spec lacks reciprocal requirement -->
			<assert test='../@role="tablist"'>
				An element with &#x201C;role=tab&#x201D; requires 
				&#x201C;role=tablist&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="treeitem"]'>
			<assert test='../@role="tree"'>
				An element with &#x201C;role=treeitem&#x201D; requires 
				&#x201C;role=tree&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="listitem"]'>
			<assert test='../@role="list"'>
				An element with &#x201C;role=listitem&#x201D; requires 
				&#x201C;role=list&#x201D; on the parent.
			</assert>
		</rule>

		<rule context='*[@role="row"]'>
			<assert test='../@role="grid" or 
			              ../../@role="grid" or
						  ../@role="treegrid" or 
			              ../../@role="treegrid"'>
				An element with &#x201C;role=row&#x201D; requires 
				&#x201C;role=treegrid&#x201D; or &#x201C;role=grid&#x201D; on the parent.
			</assert>
		</rule> 
		<!-- XXX hoping for a spec change so not bothering with the reciprocal case -->

		<rule context='*[@role="gridcell"]'>
			<assert test='../@role="row"'>
				An element with &#x201C;role=gridcell&#x201D; requires 
				&#x201C;role=row&#x201D; on the parent.
			</assert>
		</rule>
		<!-- XXX hoping for a spec change so not bothering with the reciprocal case -->

	</pattern>
	
	<pattern name='Not Option'>
		<rule context='*[not(@role="option")]'>
			<report test='../@role="listbox" or
			              ../@role="combobox"'>
				An element must not be a child of
				&#x201C;role=listbox&#x201D; or &#x201C;role=combobox&#x201D; unless it has &#x201C;role=option&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not menuitem*'>
		<rule context='*[not(@role="menuitem" or 
		                     @role="menuitemcheckbox" or 
		                     @role="menuitemradio")]'>
			<report test='../@role="menu"'>
				An element must not be a child of
				&#x201C;role=menu&#x201D; unless it has 
				&#x201C;role=menuitem&#x201D;, 
				&#x201C;role=menuitemcheckbox&#x201D; or 
				&#x201C;role=menuitemradio&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not treeitem'>
		<rule context='*[not(@role="treeitem")]'>
			<report test='../@role="tree"'>
				An element must not be a child of
				&#x201C;role=tree&#x201D; unless it has 
				&#x201C;role=treeitem&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not listitem'>
		<rule context='*[not(@role="listitem")]'>
			<report test='../@role="list"'>
				An element must not be a child of
				&#x201C;role=tree&#x201D; unless it has 
				&#x201C;role=treeitem&#x201D;.
			</report>
		</rule>
		<!-- XXX role=group omitted due to lack of detail in spec -->
	</pattern>
	
	<pattern name='Not radio'>
		<rule context='*[not(@role="radio")]'>
			<report test='../@role="radiogroup"'>
				An element must not be a child of
				&#x201C;role=radiogroup&#x201D; unless it has 
				&#x201C;role=radio&#x201D;.
			</report>
		</rule>
		 <!-- XXX directory must contain only link but seems bogus -->
	</pattern>
	
	<pattern name='aria-activedescendant must refer to a descendant'>
		<rule context='*[@aria-activedescendant]'>
			<assert test='id(@aria-activedescendant) = descendant::*'>
				The &#x201C;aria-activedescendant&#x201D; attribute must refer to a 
				descendant element.
			</assert>
		</rule>
	</pattern>

	<pattern name='controls must not dangle'>
		<rule context='*[@aria-controls]'>
			<assert test='id(@aria-controls)'>
				The &#x201C;aria-controls&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='describedby must not dangle'>
		<rule context='*[@aria-describedby]'>
			<assert test='id(@aria-describedby)'>
				The &#x201C;aria-describedby&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='flowto must not dangle'>
		<rule context='*[@aria-flowto]'>
			<assert test='id(@aria-flowto)'>
				The &#x201C;aria-flowto&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='labelledby must not dangle'>
		<rule context='*[@aria-labelledby]'>
			<assert test='id(@aria-labelledby)'>
				The &#x201C;aria-labelledby&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='owns must not dangle'>
		<rule context='*[@aria-owns]'>
			<assert test='id(@aria-owns)'>
				The &#x201C;aria-owns&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

</schema>
