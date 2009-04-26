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

		<rule context='h:section|h:nav|h:article|h:aside|h:footer'>
			<report test='ancestor::h:header'>
				The sectioning element &#x201C;<name/>&#x201D; must not
				appear as a descendant of the &#x201C;header&#x201D; element.
			</report>
			<report test='ancestor::h:footer'>
				The sectioning element &#x201C;<name/>&#x201D; must not
				appear as a descendant of the &#x201C;footer&#x201D; element.
			</report>
			<report test='ancestor::h:address'>
				The sectioning element &#x201C;<name/>&#x201D; must not
				appear as a descendant of the &#x201C;address&#x201D; element.
			</report>
		</rule>

		<rule context='h:h1|h:h2|h:h3|h:h4|h:h5|h:h6'>
			<report test='ancestor::h:footer'>
				The &#x201C;<name/>&#x201D; element must not appear as a 
				descendant of the &#x201C;footer&#x201D; element.
			</report>
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

		<rule context='h:a|h:datagrid|h:details|h:bb'>
			<report test='ancestor::h:a'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;bb&#x201D; element.
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
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;<name/>&#x201D; must not 
				appear as a descendant of the &#x201C;bb&#x201D; element.
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
			<report test='ancestor::h:bb'>
				The interactive element &#x201C;<name/>&#x201D;
				with the attribute &#x201C;controls&#x201D; must not
				appear as a descendant of the &#x201C;bb&#x201D; element.
			</report>
		</rule>

		<rule context='h:menu[translate(@type, "TOLBAR", "tolbar")="toolbar"]'>
			<report test='ancestor::h:a'>
				The element &#x201C;menu&#x201D;
				with the attribute &#x201C;toolbar&#x201D; must not
				appear as a descendant of the &#x201C;a&#x201D; element.
			</report>
			<report test='ancestor::h:button'>
				The element &#x201C;menu&#x201D;
				with the attribute &#x201C;toolbar&#x201D; must not
				appear as a descendant of the &#x201C;button&#x201D; element.
			</report>
			<report test='ancestor::h:bb'>
				The element &#x201C;menu&#x201D;
				with the attribute &#x201C;toolbar&#x201D; must not
				appear as a descendant of the &#x201C;bb&#x201D; element.
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
			<report test='ancestor::h:bb'>
				The element &#x203C;img&#x201D;
				with the attribute &#x201C;usemap&#x201D; must not
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

		<rule context='h:center|h:font|h:big|h:s|h:strike|h:tt|h:u|h:acronym|h:dir|h:applet'>
			<report test='true()'>
				The &#x201C;<name/>&#x201D; element is obsolete.
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
			<assert test='translate(@lang, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz") = translate(@xml:lang, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "abcdefghijklmnopqrstuvwxyz")'>
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
