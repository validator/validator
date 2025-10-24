<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"
                      "http://www.w3.org/TR/REC-html40/loose.dtd">
<html>
  <head>
    <title>CSS Validator Project</title>

    <link href="style/page.css" type="text/css" rel="STYLESHEET">
    <meta name="ROBOTS" content="NOINDEX, NOFOLLOW">
    <LINK REL="STYLESHEET" TITLE="default" MEDIA="screen" HREF="style/general.css" TYPE="text/css">
    <META name="ROBOTS" content="NOINDEX, NOFOLLOW">
		<meta lang="nl" name="Translator" content="Sijtsche de Jong">

</head>
  <body>
    <a class="left" href="https://www.w3.org">
      <IMG SRC="http://www.w3.org/Icons/w3c_home" BORDER="0" ALT="w3c"></A>
    <a class="right" href="https://www.w3.org/Jigsaw/"><IMG
	  SRC="http://jigsaw.w3.org/Icons/jigpower.gif" ALT="Jigsaw Powered"
	  BORDER="0" WIDTH="94" HEIGHT="52"></a>
    <br>

    <div class="t1">CSS</div>
    <div class="t2">Validator</div>

    <h1 class="center">CSS Validator versie 2.0</h1>
    
    <p>
      Dit is een validator voor CSS. Zie de <a href="COPYRIGHT.html">copyright notities</a>.
    </p>
    <p>
      U kunt het programma op een webserver uitvoeren, op de command line of
      in uw recente browser.
    </p>

    <p>
      Voor vragen of problemen met de validator kunt u een 
      <a href="mailto:Philippe.Le_Hegaret@sophia.inria.fr">email</a> sturen.
    </p>

    <p>Deze directory bevat:</p>
    <dl>
      <dt>
	<a href="org/w3c/css/parser/analyzer">org.w3c.css.parser.analyzer</a>
      <dd>
	De parser die is gegenereerd met 
	<a href="http://www.suntest.com/JavaCC">JavaCC 0.7.1</a>.
	Waarschijnlijk wilt u deze directory niet bekijken.
	    <dt>
	<a href="org/w3c/css/parser">org.w3c.css.parser</a>
      <dd>
	Het front-end van de parser
      <dt>
	<a href="org/w3c/css/values">org.w3c.css.values</a>
      <dd>
	Alle waarden voor de parser om een expressie samen te stellen.
	    <dt>
	<a href="org/w3c/css/properties">org.w3c.css.properties</a>
      <dd>
	Alle eigenschappen voor cascading style sheet versie 1.
	    <dt>
	<a href="org/w3c/css/aural">org.w3c.css.aural</a>
      <dd>
	Voor Aural eigenschappen.
      <dt>
	<a href="org/w3c/css/table">org.w3c.css.table</a>
      <dd>
	Alle CSS2 tabeleigenschappen.
	    <dt>
	<a href="org/w3c/css/user">org.w3c.css.user</a>
      <dd>
	Alle CSS2 gebruikerseigenschappen.
	    <dt>
	<a href="org/w3c/css/font">org.w3c.css.font</a>
      <dd>
	Alle CSS2 lettertype eigenschappen.
	    <dt>
	<a href="org/w3c/css/paged">org.w3c.css.paged</a>
      <dd>
	Alle CSS2 properties.
	    <dt>
	<a href="org/w3c/css/css">org.w3c.css.css</a> 
      <dd>
	De validator is hier !
	    <dt>
	<a href="org/w3c/css/util">org.w3c.css.util</a>
      <dd>
	utilities voor een groot aantal classes
	    <dt>
	<a href="org/w3c/css/servlet">org.w3c.css.servlet</a>
      <dd>
	De validator servlet.
	<div class="box">
	  <p>
			In de servlet modus worden URL-achtige files uitgeschakeld. Wees voorzichtig
			met URL's. U kunt een URL verzoek doen en als uw site bijzondere authorisatie
			voor de toegang tot webpagina's heeft is het gevaarlijk om de validator hierop
			te gebruiken. U kunt alle URL verzoeken uitschakelen met de init parameter
			'import'. Geef deze parameter de waarde 'false' (standaard) en elke URL request
			(behalve file:) is toegestaan. Zie de     
	    validator on it. You can desactivated all URL request with the init
	    parameter 'import'. Set this parameter to 'false' (default) means
	    any URL request (except file:) are authorized.  see the <a
	    href="docs/org.w3c.css.servlet.CssValidator.html">javadoc documentatie</a>
	    voor meer informatie over de servlet.
	  </p>
	</div></dd>
      <dt>
	docs</dt>
      <dd>
	the <a href="docs/packages.html">javadoc documentatie</a>.</dd>
      <dt>
	<a href="HOWTO.html">HOE</a></dt>
      <dd>
	Hoe kunt u uw eigen properties toevoegen?
	  </dd>
      <dt>
	<a href="BUGS.html">BUGS</a></dt>
      <dd>
	alle bug reports</dd>
      <dt>
	<a href="TODO.html">TODO</a></dt>
      <dd>
	Ik zal nog wel veel te doen hebben in deze file.
	    </dd>
      <dt>
	<a href="DOWNLOAD.html">RUN</a></dt>
      <dd>
	Hoe gebruikt u de validator op uw eigen systeem.</dd>
    </dl>
    
    <p>
  		Er zijn veel configuratiebestanden in de validator (in Java zijn dit 
      properties of eigenschappen).
    </p>
      
    <ul>
      <li>package org.w3c.css.parser
	<dl>
	  <dt>
	    <a href="org/w3c/css/parser/Config.properties">Config.properties</a></dt>
	  <dd>
	    De standaard CssStyle voor het parsen van een document.</dd>
	  <dt>
	    <a href="org/w3c/css/parser/Elements.dtd4">Elements.dtd4</a></dt>
	  <dd>
	    Alle HTML elementen die worden herkend door een selector.</dd>
	</dl></li>
      <li>package org.w3c.css.util
	<dl>
	  <dt>
	    <a href="org/w3c/css/util/Messages.properties">Messages.properties</a></dt>
	  <dd>
	    Alle waarschuwingen en foutmeldingen die door de parser worden gebruikt.</dd>
	</dl></li>
      <li>package org.w3c.css.properties
	<dl>
	  <dt>
	    <a href="org/w3c/css/properties/CSS1Default.properties">CSS1Default.properties</a></dt>
	  <dd>
	    Erfelijkheidsinstellingen voor alle CSS1 eigenschappen.</dd>
	  <dt>
	    <a href="org/w3c/css/properties/CSS1Properties.properties">CSS1Properties.properties</a></dt>
	  <dd>
	    Alle CSS1 eigenschappen.</dd>
	</dl></li>
      <li>package org.w3c.css.aural
	<dl>
	  <dt>
	    <a href="org/w3c/css/aural/ACSSDefault.properties">ACSSDefault.properties</a></dt>
	  <dd>
	    Erfelijkheidsinstellingen voor alle aural properties.</dd>
	  <dt>
	    <a href="org/w3c/css/aural/AuralProperties.properties">AuralProperties.properties</a></dt>
	  <dd>
	    Alle CSS1 en Aural eigenschappen.</dd>
	  <dt>
	    <a href="org/w3c/css/aural/AuralDefault.properties">AuralDefault.properties</a></dt>
	  <dd>
	    Configuratie voor Aural browser.</dd>
	</dl></li>
      <li>package org.w3c.css.font
	<dl>
	  <dt>
	    <a href="org/w3c/css/font/Font.properties">Font.properties</a></dt>
	  <dd>
	    Erfelijkheidsinstellingen voor alle lettertype eigenschappen en 
	    configuratiefile voor de parser.</dd>
	</dl></li>
      <li>package org.w3c.css.css
	<dl>
	  <dt>
	    <a href="org/w3c/css/css/format.properties">format.properties</a></dt>
	  <dd>
	    Beschikbare uitvoerformaat voor de validator.</dd>
	  <dt>
	    <a href="org/w3c/css/css/text.properties">text.properties</a></dt>
	  <dd>
	    text output format.</dd>
	  <dt>
	    <a href="org/w3c/css/css/html.properties">html.properties</a></dt>
	  <dd>
	    html uitvoereigenschappen.</dd>
	</dl></li>
    </ul>
    
    <hr class="large">
    <img src="images/mwcss.gif" alt="gemaakt met CSS">
    <address class="right"><a href="Email.html">validator-css</a></address>
  </body>
</html>

