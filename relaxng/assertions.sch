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

	<pattern name='Triggered on mutually exclusive elements and prohibited-descendant cases'>

	<!-- Exclusions and prohibited-descendant contraints  - - - - - - - - - - - -->

	<rule context='h:form|h:dfn|h:noscript|h:address'>
			<report test='ancestor::*[name() = name(current())]'>
				The &#x201C;<name/>&#x201D; element must not contain any nested 
				&#x201C;<name/>&#x201D; elements.
			</report>
		</rule>

	<rule context='h:label'>
			<report test='ancestor::*[name() = name(current())]'>
				The &#x201C;<name/>&#x201D; element must not contain any nested 
				&#x201C;<name/>&#x201D; elements.
			</report>
			<report test='count(descendant::h:input
			                   | descendant::h:button
			                   | descendant::h:select
			                   | descendant::h:keygen
			                   | descendant::h:textarea) > 1'>
				The &#x201C;label&#x201D; element may contain at most one
				&#x201C;input&#x201D;,
				&#x201C;button&#x201D;,
				&#x201C;select&#x201D;,
				or &#x201C;textarea&#x201D; descendant.
			</report>
			<report test='@for and 
			              not(//h:input[not(translate(@type, "HIDEN", "hiden")="hidden")][@id = current()/@for] or 
			              //h:textarea[@id = current()/@for] or 
			              //h:select[@id = current()/@for] or 
			              //h:button[@id = current()/@for] or 
			              //h:keygen[@id = current()/@for] or 
			              //h:output[@id = current()/@for])'>
				The &#x201C;for&#x201D; attribute of the &#x201C;label&#x201D; 
				element must refer to a form control.
			</report>
		</rule>

		<rule context='h:section|h:nav|h:article|h:aside'>
			<report test='ancestor::h:address'>
				The sectioning element &#x201C;<name/>&#x201D; must not
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:footer'>
			<report test='ancestor::h:header'>
				The element &#x201C;footer&#x201D; must not
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The element &#x201C;footer&#x201D; must not
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The element &#x201C;footer&#x201D; must not
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:h1|h:h2|h:h3|h:h4|h:h5|h:h6'>
			<report test='ancestor::h:address'>
				The &#x201C;<name/>&#x201D; element must not appear as a 
				descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:header'>
			<report test='ancestor::h:footer'>
				The &#x201C;<name/>&#x201D; element must not appear as a 
				descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The &#x201C;<name/>&#x201D; element must not appear as a 
				descendant of the &#x201C;address&#x201D; element.
			</report>
			<report test='ancestor::h:header'>
				The &#x201C;header&#x201D; element must not appear as a 
				descendant of the &#x201C;header&#x201D; element.
			</report>
		</rule>

		<rule context='h:table'>
			<report test='ancestor::h:caption'>
				The element &#x201C;table&#x201D; must not appear as a
				descendant of the &#x201C;caption&#x201D; element.
			</report>
		</rule>

	<!-- Interactive element exclusions -->
	
		<!-- 
		   - Interactive descendants:
		   - a
		   - video[controls]
		   - audio[controls]
		   - details
		   - menu[type=toolbar]
		   - button
		   - input[type!=hidden]
		   - textarea
		   - select
		   - img[usemap]
		   - embed
		   - iframe
		   - keygen
		   - label
		   - object[usemap]
		
		   - Interactive ancestors
		   - a
		   - button
		  -->

		<rule context='h:a|h:details|h:embed|h:iframe|h:label'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

		<rule context='h:button|h:textarea|h:select|h:keygen|h:input[not(translate(@type, "HIDEN", "hiden")="hidden")]'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:label[@for] and not(ancestor::h:label[@for = current()/@id])'>
				Any &#x201C;<name/>&#x201D; descendant of a &#x201C;label&#x201D;
				element with a &#x201C;for&#x201D; attribute must have an
				ID value that matches that &#x201C;for&#x201D; attribute.
			</report>
		</rule>

		<rule context='h:video[@controls]|h:audio[@controls]'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;<name/>&#x201D;
				with the attribute &#x201C;controls&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;<name/>&#x201D;
				with the attribute &#x201C;controls&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

		<rule context='h:menu[translate(@type, "TOLBAR", "tolbar")="toolbar"]'>
			<report test='ancestor::h:a'>
				The element &#x201C;menu&#x201D;
				with the attribute &#x201C;type=toolbar&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The element &#x201C;menu&#x201D;
				with the attribute &#x201C;type=toolbar&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

		<rule context='h:img[@usemap]'>
			<report test='ancestor::h:a'>
				The element &#x201C;img&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The element &#x201C;img&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

		<rule context='h:object[@usemap]'>
			<report test='ancestor::h:a'>
				The element &#x201C;object&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The element &#x201C;object&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
		</rule>

	<!-- REVISIT fieldset http://lists.whatwg.org/pipermail/whatwg-whatwg.org/2006-April/006181.html -->

	<!-- Misc requirements -->
		
		<rule context='h:script[translate(@language, "JAVSCRIPT", "javscript")="javascript"]'>
			<assert test='not(@type) or translate(@type, "EXJAVSCRIPT", "exjavscript")="text/javascript"'>
				A &#x201C;script&#x201D; element with the 
				&#x201C;language="JavaScript"&#x201D; attribute set must not have a 
				&#x201C;type&#x201D; attribute whose value is not 
				&#x201C;text/javascript&#x201D;.
			</assert>
		</rule>

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

		<rule context='h:time'>
			<report test='ancestor::h:time'>
				The element &#x201C;meter&#x201D; must not
				appear as a descendant of the &#x201C;meter&#x201D; element.
			</report>
		</rule>

		<rule context='h:progress'>
			<report test='ancestor::h:progress'>
				The element &#x201C;progress&#x201D; must not
				appear as a descendant of the &#x201C;progress&#x201D; element.
			</report>
			<assert test='@max and @value and number(@value) &lt;= number(@max)'>
				The value of the  &#x201C;value&#x201D; attribute must be less than or equal to
				the value of the &#x201C;max&#x201D; attribute.
			</assert>
			<assert test='not(@max) and @value and number(@value) &lt;= 1'>
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
			<report test='ancestor::h:meter'>
				The element &#x201C;meter&#x201D; must not
				appear as a descendant of the &#x201C;meter&#x201D; element.
			</report>
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

		<rule context='h:center|h:font|h:big|h:s|h:strike|h:tt|h:u|h:basefont'>
			<report test='true()'>
				The &#x201C;<name/>&#x201D; element is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:acronym'>
			<report test='true()'>
				The &#x201C;acronym&#x201D; element is obsolete. Use the &#x201C;abbr&#x201D; element instead.
			</report>
		</rule>

		<rule context='h:dir'>
			<report test='true()'>
				The &#x201C;dir&#x201D; element is obsolete. Use the &#x201C;ul&#x201D; element instead.
			</report>
		</rule>

		<rule context='h:applet'>
			<report test='true()'>
				The &#x201C;applet&#x201D; element is obsolete. Use the &#x201C;object&#x201D; element instead.
			</report>
		</rule>

		<rule context='h:frameset|h:noframes'>
			<report test='true()'>
				The &#x201C;<name/>&#x201D; element is obsolete. Use the &#x201C;iframe&#x201D; element and CSS instead, or use server-side includes.
			</report>
		</rule>

	<!-- Obsolete Attributes- - - - - - - - - - - - - - - - - - - - - - -->

		<rule context='h:*[@abbr]'>
			<report test='self::h:td|self::h:th'>
				The &#x201C;abbr&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@archive]'>
			<report test='self::h:object'>
				The &#x201C;archive&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@axis]'>
			<report test='self::h:td|self::h:th'>
				The &#x201C;axis&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@charset]'>
			<report test='self::h:link|self::h:a'>
				The &#x201C;charset&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@codebase]'>
			<report test='self::h:object'>
				The &#x201C;codebase&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@codetype]'>
			<report test='self::h:object'>
				The &#x201C;codetype&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@coords]'>
			<report test='self::h:a'>
				The &#x201C;coords&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@declare]'>
			<report test='self::h:object'>
				The &#x201C;declare&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@longdesc]'>
			<report test='self::h:img|self::h:iframe'>
				The &#x201C;longdesc&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@methods]'>
			<report test='self::h:link|self::h:a'>
				The &#x201C;methods&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@name]'>
			<report test='self::h:img|self::h:embed'>
				The &#x201C;name&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@nohref]'>
			<report test='self::h:area'>
				The &#x201C;nohref&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@profile]'>
			<report test='self::h:head'>
				The &#x201C;profile&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@rev]'>
			<report test='self::h:link|self::h:a'>
				The &#x201C;rev&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@scheme]'>
			<report test='self::h:meta'>
				The &#x201C;scheme&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@scope]'>
			<report test='self::h:td'>
				The &#x201C;scope&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@shape]'>
			<report test='self::h:a'>
				The &#x201C;shape&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@standby]'>
			<report test='self::h:object'>
				The &#x201C;standby&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@target]'>
			<report test='self::h:link'>
				The &#x201C;target&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@type]'>
			<report test='self::h:param'>
				The &#x201C;type&#x201D; attribute is obsolete.
			</report>
			<report test='self::h:li|self::h:ol|self::h:ul'>
				The &#x201C;type&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@urn]'>
			<report test='self::h:a'>
				The &#x201C;urn&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@usemap]'>
			<report test='self::h:input'>
				The &#x201C;usemap&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@valuetype]'>
			<report test='self::h:param'>
				The &#x201C;valuetype&#x201D; attribute is obsolete.
			</report>
		</rule>

		<rule context='h:*[@version]'>
			<report test='self::h:html'>
				The &#x201C;version&#x201D; attribute is obsolete.
			</report>
		</rule>

	<!-- Obsolete presentational Attributes- - - - - - - - - - - - - - - - - - - - - - -->

		<rule context='h:*[@align]'>
			<report test='self::h:caption|self::h:iframe|self::h:img
				|self::h:input|self::h:object|self::h:embed|self::h:legend|self::h:table
				|self::h:hr|self::h:div|self::h:h1|self::h:h2|self::h:h3
				|self::h:h4|self::h:h5|self::h:h6|self::h:p|self::h:col
				|self::h:colgroup|self::h:tbody|self::h:td|self::h:tfoot
				|self::h:th|self::h:thead|self::h:tr'>
				The &#x201C;align&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@alink]'>
			<report test='self::h:body'>
				The &#x201C;alink&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@background]'>
			<report test='self::h:body'>
				The &#x201C;background&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@bgcolor]'>
			<report test='self::h:table|self::h:tr|self::h:td|self::h:th
				|self::h:body'>
				The &#x201C;bgcolor&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@border]'>
			<report test='self::h:table|self::h:object'>
				The &#x201C;border&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@cellpadding]'>
			<report test='self::h:table'>
				The &#x201C;cellpadding&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@cellspacing]'>
			<report test='self::h:table'>
				The &#x201C;cellspacing&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@char]'>
			<report test='self::h:col|self::h:colgroup|self::h:tbody
				|self::h:td|self::h:tfoot|self::h:th|self::h:thead|self::h:tr'>
				The &#x201C;char&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@charoff]'>
			<report test='self::h:col|self::h:colgroup|self::h:tbody
				|self::h:td|self::h:tfoot|self::h:th|self::h:thead|self::h:tr'>
				The &#x201C;charoff&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@clear]'>
			<report test='self::h:br'>
				The &#x201C;clear&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@compact]'>
			<report test='self::h:dl|self::h:menu|self::h:ol|self::h:ul'>
				The &#x201C;compact&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@frameborder]'>
			<report test='self::h:iframe'>
				The &#x201C;frameborder&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@frame]'>
			<report test='self::h:table'>
				The &#x201C;frame&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@height]'>
			<report test='self::h:td|self::h:th'>
				The &#x201C;height&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@hspace]'>
			<report test='self::h:img|self::h:object'>
				The &#x201C;hspace&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@link]'>
			<report test='self::h:body'>
				The &#x201C;link&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@marginheight]'>
			<report test='self::h:iframe'>
				The &#x201C;marginheight&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@marginwidth]'>
			<report test='self::h:iframe'>
				The &#x201C;marginwidth&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@noshade]'>
			<report test='self::h:hr'>
				The &#x201C;noshade&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@nowrap]'>
			<report test='self::h:td|self::h:th'>
				The &#x201C;nowrap&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@rules]'>
			<report test='self::h:table'>
				The &#x201C;rules&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@scrolling]'>
			<report test='self::h:iframe'>
				The &#x201C;scrolling&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@size]'>
			<report test='self::h:hr'>
				The &#x201C;size&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@text]'>
			<report test='self::h:body'>
				The &#x201C;text&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@valign]'>
			<report test='self::h:col|self::h:colgroup|self::h:tbody
				|self::h:td|self::h:tfoot|self::h:th|self::h:thead|self::h:tr'>
				The &#x201C;valign&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@vlink]'>
			<report test='self::h:body'>
				The &#x201C;vlink&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@vspace]'>
			<report test='self::h:img|self::h:object'>
				The &#x201C;vspace&#x201D; attribute is obsolete. Use CSS instead.
			</report>
		</rule>

		<rule context='h:*[@width]'>
			<report test='self::h:hr|self::h:table|self::h:td|self::h:th
				|self::h:col|self::h:colgroup|self::h:pre'>
				The &#x201C;width&#x201D; attribute is obsolete. Use CSS instead.
			</report>
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

	</pattern>

<!-- lang and xml:lang in XHTML  - - - - - - - - - - - - - - - - - -->

	<pattern name='lang and xml:lang in XHTML'>
		<rule context='h:*[@lang and @xml:lang]'>
			<assert test='translate(@lang, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz") = translate(@xml:lang, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz")'>
				When the attribute &#x201C;lang&#x201D; in no namespace and the attribute 
				&#x201C;lang&#x201D; in the XML namespace are both present, they must have the 
				same value.
			</assert>
		</rule>
	</pattern>

<!-- IDREFs  - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

	<!-- Assuming that ID uniqueness is already enforced. -->

	<pattern name='contextmenu must refer to a menu'>
		<rule context='h:*[@contextmenu]'>
		  <assert test='//h:menu[@id = current()/@contextmenu]'>
				The &#x201C;contextmenu&#x201D; attribute must refer to a 
				&#x201C;menu&#x201D; element.
			</assert>
		</rule>
	</pattern>

	<pattern name='list on input must refer to a select or a datalist'>
		<rule context='h:input[@list]'>
			<assert test='//h:datalist[@id = current()/@list] or 
			              //h:select[@id = current()/@list]'>
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

<!-- Unique Definitions  - - - - - - - - - - - - - - - - - - - - - -->
	
	<!-- Only one definition per term per document' -->

<!-- ARIA containment    - - - - - - - - - - - - - - - - - - - - - -->

	<pattern name='Mutually Exclusive Role triggers'>

    <!-- XXX columnheader and rowheader require row parent -->

		<rule context='*[@role="option"]'>
			<assert test='../@role="listbox"'>
				An element with &#x201C;role=option&#x201D; requires 
				&#x201C;role=listbox&#x201D; on the parent.
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

		<rule context='*[@role="tab"]'>
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
				&#x201C;role=treegrid&#x201D; or &#x201C;role=grid&#x201D; on the parent or grandparent.
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
			<report test='../@role="listbox"'>
				An element must not be a child of
				&#x201C;role=listbox&#x201D; unless it has &#x201C;role=option&#x201D;.
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
				&#x201C;role=list&#x201D; unless it has 
				&#x201C;role=listitem&#x201D;.
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
	</pattern>
	
	<pattern name='Not gridcell'>
		<rule context='*[not(@role="gridcell")]'>
			<report test='../@role="row"'>
				An element must not be a child of
				&#x201C;role=row&#x201D; unless it has 
				&#x201C;role=gridcell&#x201D;.
			</report>
		</rule>
	</pattern>
	
	<pattern name='Not tab'>
		<rule context='*[not(@role="tab")]'>
			<report test='../@role="tablist"'>
				An element must not be a child of
				&#x201C;role=tablist&#x201D; unless it has 
				&#x201C;role=role&#x201D;.
			</report>
		</rule>
	</pattern>

  <!-- XXX combobox requires a listbox child -->
	
	<pattern name='aria-activedescendant must refer to a descendant'>
		<rule context='*[@aria-activedescendant]'>
			<assert test='descendant::*[@id = current()/@aria-activedescendant]'>
				The &#x201C;aria-activedescendant&#x201D; attribute must refer to a 
				descendant element.
			</assert>
		</rule>
	</pattern>

	<pattern name='controls must not dangle'>
		<rule context='*[@aria-controls]'>
		  <assert test='//*[@id = current()/@aria-controls]'>
				The &#x201C;aria-controls&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='describedby must not dangle'>
		<rule context='*[@aria-describedby]'>
		  <assert test='//*[@id = current()/@aria-describedby]'>
				The &#x201C;aria-describedby&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='flowto must not dangle'>
		<rule context='*[@aria-flowto]'>
		  <assert test='//*[@id = current()/@aria-flowto]'>
				The &#x201C;aria-flowto&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='labelledby must not dangle'>
		<rule context='*[@aria-labelledby]'>
		  <assert test='//*[@id = current()/@aria-labelledby]'>
				The &#x201C;aria-labelledby&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

	<pattern name='owns must not dangle'>
		<rule context='*[@aria-owns]'>
		  <assert test='//*[@id = current()/@aria-owns]'>
				The &#x201C;aria-owns&#x201D; attribute must point to an element in the 
				same document.
			</assert>
		</rule>
	</pattern>

</schema>
