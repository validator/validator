<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<!-- Mechanically extracted from RNG files which had this license: -->
<!--
Copyright (c) 2005 Petr Nalevka
Copyright (c) 2007 Mozilla Foundation
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

<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron" 
xmlns="http://www.ascc.net/xml/schematron" 
xmlns:rng="http://relaxng.org/ns/structure/1.0">
   <sch:ns xmlns="http://relaxng.org/ns/structure/1.0" prefix="html" uri="http://www.w3.org/1999/xhtml"/>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.1">
		
      <sch:rule context="html:img">
			
         <sch:assert test="(@alt and string-length(@alt) &gt; 0) or  (@longdesc and string-length(@longdesc) &gt; 0)">
        WCAG 1.0 Checkpoint 1.1 (Priority 1) An image element should have some descriptive text: an alt or longdesc attribute.
      </sch:assert>
		
      </sch:rule>
		
      <sch:rule context="html:input">
			
         <sch:report test="((not(@alt) or string-length(@alt) = 0) and @type = 'image')">
        WCAG 1.0 Checkpoint 1.1 (Priority 1) An image input element should have a descriptive alt atribute.
      </sch:report>
		
      </sch:rule>
		
      <sch:rule context="html:applet">
			
         <sch:assert test="(@alt and string-length(@alt) &gt; 0)">
        WCAG 1.0 Checkpoint 1.1 (Priority 1) An applet element should have some a descriptive alt atribute.
      </sch:assert>
		
      </sch:rule>
		
      <sch:rule context="html:map">
			
         <sch:assert test="(html:area/@alt and string-length(html:area/@alt) &gt; 0) or  descendant::html:a">
        WCAG 1.0 Checkpoint 1.1 (Priority 1) Map's area element should have a descriptive alt atribute or map should contain some a elements.
      </sch:assert>
		
      </sch:rule>
		
      <sch:rule context="html:object">
			
         <sch:assert test="string-length(text()) &gt; 0">WCAG 1.0 Checkpoint 1.1 (Priority 1) An object element should contain some descriptive text.</sch:assert>
		
      </sch:rule>
    
      <sch:rule context="html:applet">
      
         <sch:assert test="string-length(text()) &gt; 0">WCAG 1.0 Checkpoint 1.1 (Priority 1) An applet element should contain some descriptive text.</sch:assert>
    
      </sch:rule>
		
      <sch:rule context="html:frame"> <!-- also Checkpoint 12.2 (Priority 2) -->
			
         <sch:assert test="(@longdesc and string-length(@longdesc) &gt; 0)">WCAG 1.0 Checkpoint 1.1 (Priority 1) A frame element should have some descriptive text: a longdesc attribute.</sch:assert>
		
      </sch:rule>
    <!-- Checkpoint 1.2-1.4 can't be foramlized -->
	
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.2">
    
      <sch:rule context="html:body">
      
         <sch:report test="@bgcolor and @text and (string(@bgcolor) = string(@text))">
        WCAG 1.0 Checkpoint 2.2 (Priority 3) The background color and the foreground color shouldn't be the same.
      </sch:report>
      <!-- Here is just a simple RGB comapison ignorig Hexadecimal numbers
      <sch:report test="
                        string(number(substring(@text,2,2))) != 'NaN' and string(number(substring(@text,4,2))) != 'NaN' and
                        string(number(substring(@text,6,2))) != 'NaN' and string(number(substring(@bgcolor,2,2))) != 'NaN' and
                        string(number(substring(@bgcolor,4,2))) != 'NaN' and string(number(substring(@bgcolor,6,2))) != 'NaN' and
                        (number(substring(@text,2,2))-number(substring(@bgcolor,2,2))) &lt; 10 and
                        (number(substring(@text,4,2))-number(substring(@bgcolor,4,2))) &lt; 10 and
                        (number(substring(@text,6,2))-number(substring(@bgcolor,6,2))) &lt; 10
                       ">
        WCAG 1.0 Checkpoint 2.2 (Priority 3) Differences between body's background and foreground color are to small, this may cause acceszibility problems.
      </sch:report> -->
    
      </sch:rule>
    <!-- Checkpoint 2.1 can't be formalized -->
  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.3">
    <!-- those guidelines correspond also to checkpoint 11.2 (Priority 2) which discourages authors from using deprecated elemens -->
    
      <sch:rule context="html:font | html:basefont">
      
         <sch:report test="*">
        WCAG 1.0 Checkpoint 3.3 (Priority 2) Do not use <sch:name/> element, use stylesheet instead.
      </sch:report>
    
      </sch:rule>
    
      <sch:rule context="html:b | html:i">
      
         <sch:report test="*" role="WARNING">
        WCAG 1.0 Checkpoint 3.3 (Priority 2) Concerning element <sch:name/>: B and I are not recommended. Use strong and em, or stylesheets.
      </sch:report>
    
      </sch:rule>
    
      <sch:rule context="html:body | html:table | html:tr | html:th | html:td">
      
         <sch:report test="@bgcolor">
        WCAG 1.0 Checkpoint 3.3 (Priority 2) Use stylesheets to set background color for <sch:name/> element.
      </sch:report>
    
      </sch:rule>
    
      <sch:rule context="html:ol | html:ul | html:li | html:th | html:td">
      
         <sch:report test="@type">
        WCAG 1.0 Checkpoint 3.3 (Priority 2) Use stylesheets to set type of <sch:name/> element.
      </sch:report>
    
      </sch:rule>
    
      <sch:rule context="html:frameset">
      
         <sch:report test="@rows and (not(contains(@rows, '%')) and not(contains(@rows, '*')))">
        WCAG 1.0 Checkpoint 3.4 (Priority 2) Consider using relative lengths instead of absolute in frameset.
      </sch:report>
      
         <sch:report test="@cols and (not(contains(@cols, '%')) and not(contains(@cols, '*')))">
        WCAG 1.0 Checkpoint 3.4 (Priority 2) Consider using relative lengths instead of absolute in frameset.
      </sch:report>
      <!-- Checkpoint 3.1 and 3.2 can't be formalized -->
    
      </sch:rule>


    <!-- 3.5 hedings order -->
    
      <sch:rule context="html:h1">
      
         <sch:assert test="following::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][1][self::html:h1 or self::html:h2]       or not(following::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
        WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
        Do order heading elements properly. Do not skip heading levels.
      </sch:assert>
    
      </sch:rule>

    
      <sch:rule context="html:h2">
      
         <sch:report test="not(preceding::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
          WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
          The first heading element in the document should be h1.
      
         </sch:report>
      
         <sch:assert test="following::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][1][self::html:h1 or self::html:h2 or self::html:h3]       or not(following::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
        WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
        Do order heading elements properly. Do not skip heading levels.
      </sch:assert>
    
      </sch:rule>

    
      <sch:rule context="html:h3">
      
         <sch:report test="not(preceding::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
          WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
          The first heading element in the document should be h1.
      
         </sch:report>
      
         <sch:assert test="following::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][1][self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4]       or not(following::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
        WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
        Do order heading elements properly. Do not skip heading levels.
      </sch:assert>
    
      </sch:rule>

    
      <sch:rule context="html:h4">
      
         <sch:report test="not(preceding::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
          WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
          The first heading element in the document should be h1.
      
         </sch:report>
      
         <sch:assert test="following::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6][1][self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5]       or not(following::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
        WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
        Do order heading elements properly. Do not skip heading levels.
      </sch:assert>
    
      </sch:rule>
    <!-- There can be any heading after H5 -->
    
      <sch:rule context="html:h5">
      
         <sch:report test="not(preceding::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
          WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
          The first heading element in the document should be h1.
         </sch:report>
    
      </sch:rule>
    
      <sch:rule context="html:h6">
      
         <sch:report test="not(preceding::html:*[self::html:h1 or self::html:h2 or self::html:h3 or self::html:h4 or self::html:h5 or self::html:h6])">
          WCAG 1.0 Checkpoint 3.5 (Priority 2) Use header elements to convey document structure.
          The first heading element in the document should be h1.
      
         </sch:report>
    
      </sch:rule>

  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.4">
    
      <sch:rule context="html:abbr">
      
         <sch:report test="not(@title) and not(preceding::html:abbr[. = string(current())][@title])">
        WCAG 1.0 Checkpoint 4.2 (Priority 3) Title shall be specified for each abbreviation in a document where it first occurs in the document.
      </sch:report>
    
      </sch:rule>
    
      <sch:rule context="html:acronym">
      
         <sch:report test="not(@title) and not(preceding::html:acronym[. = string(current())][@title])">
        WCAG 1.0 Checkpoint 4.2 (Priority 3) Title shall be specified for each acronym in a document where it first occurs in the document.
      </sch:report>
    
      </sch:rule>
    
      <sch:rule context="html:html">
      
         <sch:assert test="@lang  or  @xml:lang">
        WCAG 1.0 Checkpoint 4.3 (Priority 3) The primary language of a document should be identified.
      </sch:assert>
    
      </sch:rule>
    <!-- Checkpoint 4.1 cannot be formalized -->
  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.5">
    
      <sch:rule context="html:table">
      
         <sch:assert test="count(descendant::html:td) &gt; 0 and count(descendant::html:th) &gt; 0">
        WCAG 1.0 Checkpoint 5.1 (Priority 1) Identify row and column headers in table.
      </sch:assert>
      
         <sch:assert test="html:caption">
        WCAG 1.0 Checkpoint 5.1 (Priority 1) A table should have a caption
      </sch:assert>
      
         <sch:assert test="@summary">
        WCAG 1.0 Checkpoint 5.5 (Priority 3) A table should have a summary attribute
      </sch:assert>
    
      </sch:rule>
    
      <sch:rule context="html:th">
      
         <sch:assert test="@abbr">
        WCAG 1.0 Checkpoint 5.6 (Priority 3) A table header should have an abbr attribute to give abbreviation
 </sch:assert>
    
      </sch:rule>
    <!-- Checkpoint 5.2-5.4 can't be formalized -->
  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.6">

		
      <sch:rule context="html:frameset">
			
         <sch:assert test="self::*/html:noframes">
        WCAG 1.0 Checkpoint 6.5 (Priority 2) Specify noframes element for framesets.
      </sch:assert>
		
      </sch:rule>

    
      <sch:rule context="html:a">
      
         <sch:report test="starts-with(string(@href),'javascript:')" role="WARNING">
        WCAG 1.0 Checkpoint 6.5 (Priority 2) Ensure that dynamic content is accessible or provide
        an alternative presentation or page. Avoid creating links that use "javascript" as the URI.
        If a user is not using scripts, then they won't be able to link since the browser can't
        create the link content.
      </sch:report>
   
      </sch:rule>

    <!-- Checkpoint 6.1-6.4 can't be formalized -->
	
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.7">
    
      <sch:rule context="html:blink">
      
         <sch:assert test="*">
        WCAG 1.0 Checkpoint 7.2 (Priority 2) Avoid causing content to blink. Blink element is not part of HTML.
      </sch:assert>
    
      </sch:rule>
    
      <sch:rule context="html:marquee">
      
         <sch:assert test="*">
        WCAG 1.0 Checkpoint 7.3 (Priority 2) Marquee element doesn't provide mechanisms to stop the movement.
        Marquee element is not part of HTML.
      </sch:assert>
    
      </sch:rule>
    
      <sch:rule context="html:meta">
			
         <sch:report test="translate(@http-equiv,'REFRESH','refresh') = 'refresh'">
        WCAG 1.0 Checkpoint 7.4 (Priority 2) The user should control the refreshing of the page.
      </sch:report>
		
      </sch:rule>
    <!-- Checkpoint 7.1 can't be formalized -->
	
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.9">

    
      <sch:rule context="html:*">
      
         <sch:report test="@ondblclick">
        WCAG 1.0 Checkpoint 9.2 (Priority 2) If you must use device-dependent attributes, provide redundant input
        mechanisms. "ondblclick" is device-dependent and can just be triggered by a pointing device.
      </sch:report>
      
         <sch:report test="(@onkeyup and not(@onmouseup)) or (@onmouseup and not(@onkeyup))">
        WCAG 1.0 Checkpoint 9.2 (Priority 2) If you must use device-dependent attributes, provide redundant input
        mechanisms. Use "onmouseup" with "onkeyup" for <sch:name/> element.
      </sch:report>
      
         <sch:report test="(@onkeydown and not(@onmousedown)) or (@onmousedown and not(@onkeydown))">
        WCAG 1.0 Checkpoint 9.2 (Priority 2) If you must use device-dependent attributes, provide redundant input
        mechanisms. Use "onmousedown" with "onkeydown" <sch:name/> for element .
      </sch:report>
      
         <sch:report test="(@onkeypress and not(@onclick)) or (@onclick and not(@onkeypress))">
        WCAG 1.0 Checkpoint 9.2 (Priority 2) If you must use device-dependent attributes, provide redundant input
        mechanisms. Use "onclick" with "onkeypress" for element <sch:name/>.
      </sch:report>

      
         <sch:report test="@tabindex and number(@tabindex) != 'NaN' and count(//html:*[@tabindex]) &lt; number(@tabindex)">
        WCAG 1.0 Checkpoint 9.4 (Priority 3) Create a logical tab order. Wrong tabindex value. Some tabindex
        values are missing in the sequence.
      </sch:report>
      
         <sch:report test="@tabindex and @tabindex = following::html:*/@tabindex">
        WCAG 1.0 Checkpoint 9.4 (Priority 3) Create a logical tab order. Tabindex attribute
        should have
        a unique order number within a document.
      </sch:report>
      
         <sch:report test="@accesskey and @accesskey = following::html:*/@accesskey">
        WCAG 1.0 Checkpoint 9.5 (Priority 3) Keyboard shortcuts should be used properly. This means
        that every accesskey attribute should have
        a unique character within a document. The <sch:name/> element shares the same accesskey with
        some of the following elements.
      </sch:report>
    
      </sch:rule>
    <!-- Checkpoint 9.1, 9.3, 9.5  can't be formalized -->
  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.10">
    <!-- not much more can be done agains pop-ups -->
		
      <sch:rule context="html:script">
			
         <sch:report test="@type = 'text/javascript' and contains(text(),'window.open(')">
        WCAG 1.0 Checkpoint 10.1 (Priority 2) Do not use pop-ups.
      </sch:report>
      
         <sch:report test="@type = 'text/vbscript' and contains(text(),'MsgBox')">
        WCAG 1.0 Checkpoint 10.1 (Priority 2) Do not use pop-ups.
      </sch:report>
	  
      </sch:rule>
    
      <sch:rule context="html:input">
      
         <sch:report test="@value and string-length(@value) = 0">
        WCAG 1.0 Checkpoint 10.4 (Priority 3) For browser comapatibility reasons do not use empty value of <sch:name/> element.
      </sch:report>
   
      </sch:rule>
   
      <sch:rule context="html:textarea">
      
         <sch:report test="string-length(.) = 0">
        WCAG 1.0 Checkpoint 10.4 (Priority 3) For browser comapatibility reasons do not use empty textareas.
      </sch:report>
   
      </sch:rule>
    
      <sch:rule context="html:*">
      
         <sch:report test="@target and string(@target)= '_blank'">
        WCAG 1.0 Checkpoint 10.1 (Priority 2) Content developers should avoid specifying a new window as
        the target of a frame with target="_blank".
      </sch:report>
    
      </sch:rule>
    <!-- Checkpoint 10.2, 10.3 and 10.5 can't be formalized -->
	
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.11">
    <!-- Do not use depricated elemens and attributes -->
    
      <sch:rule context="html:applet | html:basefont | html:center | html:dir | html:font | html:isindex | html:menu | html:s | html:strike | html:u">
      
         <sch:assert test="*">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Element <sch:name/> is depricated. Do not use depricated elements.
      </sch:assert>
    
      </sch:rule>

    
      <sch:rule context="html:caption | html:iframe | html:img | html:object | html:input |     html:legend | html:table | html:hr | html:p | html:div | html:h1 | html:h2 | html:h3 | html:h4 |     html:h5 | html:h6">
      
         <sch:report test="@align">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Attribute align is depricted for element <sch:name/>.
      </sch:report>
    
      </sch:rule>

    
      <sch:rule context="html:body">
      
         <sch:report test="@bgcolor or @link or @alink or @vlink or @background or @text">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Do not use body's depricated attributes: bgcolor, background, text, vlink, alink and link.
      </sch:report>
    
      </sch:rule>

    <!-- also handled by 3.3 -->
    
      <sch:rule context="html:table | html:tr | html:th | html:td">
      
         <sch:report test="@bgcolor">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Bgcolor is depricated for  element <sch:name/>.
      </sch:report>
    
      </sch:rule>

    
      <sch:rule context="html:img | html:object">
      
         <sch:report test="@border or @hspace">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Attribute border or hspace are depricted for element <sch:name/>.
      </sch:report>
    
      </sch:rule>

    
      <sch:rule context="html:br">
      
         <sch:report test="@clear and not(@clear='none')">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Attribute clear is depricted for element br.
      </sch:report>
    
      </sch:rule>

    
      <sch:rule context="html:ol">
      
         <sch:report test="@start">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Attribute start is depricted for element ol.
      </sch:report>
    
      </sch:rule>

    
      <sch:rule context="html:hr">
      
         <sch:report test="@noshade or @size">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Attributes noshade or size are depricted for element hr.
      </sch:report>
    
      </sch:rule>

    
      <sch:rule context="html:dl | html:ol | html:ul">
      
         <sch:report test="@compact">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Compact is depricated for  element <sch:name/>.
      </sch:report>
    
      </sch:rule>

    
      <sch:rule context="html:td | html:th">
      
         <sch:report test="@height or @nowrap">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Height or nowrap is depricated for element <sch:name/>.
      </sch:report>
    
      </sch:rule>

    
      <sch:rule context="html:script">
      
         <sch:report test="@language">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Language is depricated for script element.
      </sch:report>
    
      </sch:rule>
    <!-- also handled in 3.3 -->
    
      <sch:rule context="html:ol | html:ul | html:li">
      
         <sch:report test="@type">
        WCAG 1.0 Checkpoint 11.2 (Priority 2) Type is depricated for <sch:name/> element.
      </sch:report>
    
      </sch:rule>
    <!-- Checkpoint 11.1, 11.3 and 11.4 can't be formalized -->
  
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.12">
		
      <sch:rule context="html:frame">
			
         <sch:assert test="@title and (string-length(@title) &gt; 0)">
        WCAG 1.0 Checkpoint 12.1 (Priority 1) Use title to identify frames.
      </sch:assert>
	  
      </sch:rule>
    <!-- text controls shall explicit labels whose for attribute equals it's id -->
    
      <sch:rule context="html:input">
      <!-- @type = 'text' and ( -->
      
         <sch:report test="@type != 'hidden' and @type != 'submit' and @type != 'button' and not(@id)">
        WCAG 1.0 Checkpoint 12.4 (Priority 2) Do associate controls explicitly with their labels.
        Specify an id attribute for input and match it with label's for attribute.
      </sch:report>

      
         <sch:report test="@type != 'hidden' and @type != 'submit' and @type != 'button' and @id and (not(contains(//html:label/@for,concat(@id,' '))) and not(contains(//html:label/@for,concat(' ',@id))) and not(//html:label/@for = @id))">
        WCAG 1.0 Checkpoint 12.4 (Priority 2) Do associate controls explicitly with their labels.
        Match input's id attribute with a label's for attribute.
      </sch:report>

      
         <sch:report test="@type != 'hidden' and @type != 'submit' and @type != 'button' and parent::html:label and (string(parent::html:label/@for) != string(@id)) and @type = 'text'">
        WCAG 1.0 Checkpoint 12.4 (Priority 2) Explicit label association is prefered to implicit.
        Match input's id attribute with label's for attribute.
        <!--  and  -->
        <!-- WCAG 1.0 Checkpoint 12.4 (Priority 2) Label's for attribute needs to equal to input's id attribute. "<sch:value-of select="string(@id)"/>" != "<sch:value-of select="string(parent::html:label/@for)"/>"-->
      </sch:report>
    
      </sch:rule>
    <!-- text controls shall explicit labels whose for attribute equals it's id -->
    
      <sch:rule context="html:select">
      <!-- @type = 'text' and ( -->
      
         <sch:report test="not(@id)">
        WCAG 1.0 Checkpoint 12.4 (Priority 2) Do associate controls explicitly with their labels.
        Specify an id attribute for input and match it with label's for attribute.
      </sch:report>

      
         <sch:report test="@id and (not(contains(//html:label/@for,concat(@id,' '))) and not(contains(//html:label/@for,concat(' ',@id))) and not(//html:label/@for = @id))">
        WCAG 1.0 Checkpoint 12.4 (Priority 2) Do associate controls explicitly with their labels.
        Match input's id attribute with a label's for attribute.
      </sch:report>

      
         <sch:report test="parent::html:label and (string(parent::html:label/@for) != string(@id)) and @type = 'text'">
        WCAG 1.0 Checkpoint 12.4 (Priority 2) Explicit label association is prefered to implicit.
        Match input's id attribute with label's for attribute.
        <!--  and  -->
        <!-- WCAG 1.0 Checkpoint 12.4 (Priority 2) Label's for attribute needs to equal to input's id attribute. "<sch:value-of select="string(@id)"/>" != "<sch:value-of select="string(parent::html:label/@for)"/>"-->
      </sch:report>
    
      </sch:rule>
    <!-- Checkpoint 12.2, 12.3 can't be formalized -->
	
   </sch:pattern>
   <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="wcag.guideline.13">
    
      <sch:rule context="html:a">
      
         <sch:report test="translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ') = 'CLICK HERE'">
        WCAG 1.0 Checkpoint 13.1 (Priority 2) Clearly identify the target of each link.
        Good link text should not be overly general.
      </sch:report>


      
         <sch:report test="not(child::html:*) and following::html:a[. = string(current()) and not(child::html:*)][string(@href) != string(current()/@href) and not(@title) and not(current()/@title)]">
        WCAG 1.0 Checkpoint 13.1 (Priority 2) If two links refer to different targets
        but share the same link text, distinguish the links by
        using "title" attribute of each a element.
      </sch:report>

      
         <sch:report test="not(child::html:*) and following::html:a[. = string(current()) and not(child::html:*)][string(@href) != string(current()/@href) and @title and current()/@title and string(@title) = string(current()/@title)]">
        WCAG 1.0 Checkpoint 13.1 (Priority 2) If two links refer to different targets
        but share the same link text, distinguish the links by
          specifying a different value for the "title" attribute of each a element. Currently
          both links have same title.
      </sch:report>

    
      </sch:rule>

  
   </sch:pattern>
   <sch:diagnostics/>
</sch:schema>