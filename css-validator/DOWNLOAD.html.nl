<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nl" lang="nl">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Download en Installeer de CSS Validator</title>
  <link rev="made" href="mailto:www-validator-css@w3.org" />
  <link rev="start" href="./" title="Home Pagina" />
  <style type="text/css" media="all">
    @import "style/base.css";  
  	@import "style/docs.css";
  </style>
  <meta name="revision"
  content="$Id$" />

</head>

<body>
  <div id="banner">
   <h1 id="title"><a href="https://www.w3.org/"><img alt="W3C" width="110" height="61" id="logo" src="./images/w3c.png" /></a>
   <a href="./"><span>CSS Validation Service</span></a></h1>
   <p id="tagline">
     Check Cascading Style Sheets (CSS) and (X)HTML documents with style sheets
   </p>
  </div>


   <div class="doc">
<h2>Download en Installeer de CSS Validator</h2>

<p>This translation of the installation guide for the CSS validator may be out of date. For a reliable, up-to-date guide, refer to the <a href="DOWNLOAD.html.en">English</a> or <a href="DOWNLOAD.html.fr">French</a> versions.</p>


<h3 id="download">Download de CSS Validator</h3>	

	<h4 id="source">Download de broncode</h4>
    <p>
      De <a href='https://github.com/w3c/css-validator'>CSS validator</a> is als download beschikbaar via CVS.
      Volg de <a href='http://dev.w3.org/cvsweb/'>instructies</a> om
	      de publieke W3C CVS server te benaderen en download 2002/css-validator. Opmerking: de online versie
	  van de CSS Validator is in het algemeen ouder dan de CVS versie, dus resultaten en het uiterlijk
	  kan verschillen.
    </p>	
	<h4>Download de CSS Validator als java package (jar of war)</h4>

	<!--<p>Werk in uitvoering - Er moet een locatie gevonden worden waar we de bestanden neer kunnen zetten.</p>-->
	<p><a href="https://github.com/w3c/css-validator/releases/latest/download/css-validator.jar">css-validator.jar</a></p>



<h3>Installatie handleiding</h3>
<p>De CSS validatie service is een Java servlet. De service kan ge&iuml;nstalleerd worden op elke servlet engine, 
en kan ook gebruik worden als een simpele command-line tool.
De officie&euml;le W3C CSS Validatie service draait op de aanbevolen Jigsaw server,
maar, om het eenvoudig te houden zullen we, in dit document details geven over hoe de validatie service te installeren
op Apache's servlet engine Tomcat.</p>

<p>Een aantal instructies over het installeren van de servlet met Jigsaw, en het draaien van de service als command-line tool worden 
onderaan gegeven.</p>

<h4 id="prereq">Systeemeisen</h4>

<p>Deze installatie handleiding gaat er vanuit dat je gedownload, ge&iuml;nstalleerd en getest hebt:</p>

<ul class="instructions">
	<li>Een werkende Java omgeving,</li>
	<li>De <a href="https://ant.apache.org/">Ant</a> Java builder</li>
	<li>Een Java Web servlet container zoals 
		<a href="https://www.w3.org/Jigsaw/">Jigsaw</a>, <a href="https://tomcat.apache.org/">Tomcat</a> of
		<a href="http://www.mortbay.org/">Jetty</a> als je van plan bent de validator als een online service aan te bieden. 
		Deze handleiding gaat alleen in detail in op de installatie van Tomcat en Jigsaw.</li>

</ul>
<p id="prereq-libs">Voor de installatie van de validator op je systeem is het nodig
	dat je de volgende Java libraries gedownload hebt:</p>
<ul class="instructions">
	<li>servlet.jar
	(deze bevind zich, als je Tomcat ge&iuml;nstalleerd hebt, in [<span class="const">TOMCAT_DIR</span>]/common/lib/, 
	waarschijnlijk on de naam servlet-api.jar. Zo niet, haal hem hier op: 
	<a href="http://java.sun.com/products/servlet/DOWNLOAD.html">java.sun.com</a></li>
	<li><a href="https://jigsaw.w3.org/Devel/classes-2.2/20060329/">jigsaw.jar</a></li>
	<li>xercesImpl.jar en xml-apis.jar (die gedownload worden met
	<a href="https://www.apache.org/dist/xml/xerces-j/">xerces-j-bin</a>).</li>
	<li><a href="http://ccil.org/~cowan/XML/tagsoup/">tagsoup.jar</a></li>
</ul>

<h4>Installatie op Tomcat</h4>
<ol class="instructions">
	<li>
		Download de Validator zoals <a href="#source">hierboven</a> uitgelegd wordt.
	</li>
	<li>Kopi&euml;er de hele broncode map ("<span class="dir">.../css-validator/</span>") naar de <span class="dir">webapps</span>

		map naar de installatiemap van Tomcat. Normaal is dit 
		<span class="dir">[<span class="const">TOMCAT_DIR</span>]/webapps/</span>.
		De broncode van de Validator staat nu in <span class="dir">[<span class="const">TOMCAT_DIR</span>]/webapps/css-validator</span>,
		die we vanaf nu <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span> noemen.
	</li>
	<li>Maak een nieuwe directory "<span class="dir">WEB-INF</span>" in "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>", en een map "<span class="dir">lib</span>" in "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF</span>"<br />

		<kbd>mkdir -p WEB-INF/lib</kbd>
		</li>
	<li>Kopi&euml;er alle jar bestanden (van de <a href="#prereq-libs">systeemeisen</a>) naar de map "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/lib</span>"</li>
	<li>Compileer de broncode van de Validator vanuit de map <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>,
		Draai <kbd>ant</kbd>, en zorg ervoor dat de jar bestanden die je gedownload hebt correct ingesteld zijn in je CLASSPATH omgevingsvariabele. 
		In het algemeen zal het volgende gewoon werken:<br />

		<kbd>CLASSPATH=.:./WEB-INF/lib:$CLASSPATH ant</kbd>
	</li>
	<li>Kopi&euml;er of verplaats "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/</span><span class="file">css-validator.jar</span>"
	naar "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/lib/</span>".</li>

	<li>
		Kopi&euml;er of verplaats "<span class="file">web.xml</span>" van
		"<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/</span>" naar
		"<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/</span>".
	</li>
    <li>

		Herlaad de Tomcat server als laatste:<br />
<kbd>"cd <span class="dir">[<span class="const">TOMCAT_DIR</span>]</span>; <span class="dir">./bin/</span><span class="file">shutdown.sh</span>; <span class="dir">./bin/</span><span class="file">startup.sh</span>;"</kbd>
	</li>

</ol>

<h4>Installatie op Jigsaw Web Server</h4>
<ol class="instructions">
<li>Download de broncode zoals hierboven beschreven, haal de benodigde jars, en build de broncode met <kbd>ant</kbd>.</li>

<li>Daarna moet je de validator home directory configureren (normaal gesproken is dit
css-validator) zodat hij kan werken als een servlet container. Voor dit doel
is het nodig dat je Jigsaw ge&iuml;nstalleerd hebt (zie de Jigsaw pagina's voor een korte
instructie (het is erg eenvoudig)) en start Jigsaw Admin. Verander de HTTPFrame
naar ServletDirectoryFrame.</li>

<li>De volgende stap is het aanmaken van een resource "validator", met als class
'ServletWrapper' en als frame 'ServletWrapperFrame'. De latter zou zichzelf automatisch
moeten toevoegen. De class van de servlet is org.w3c.css.servlet.CssValidator.
Als er al een bestand is met de naam 'validator', verander die dan.
Het is belangrijk dat dit 'alias' altijd de naam 'validator' heeft.</li>

<li>Start als laatste Jigsaw en draai de validator. Controleer de HTML die je wilt uitvoeren.
Normaal gesproken zal je URI er als volgt uitzien:<br />

http://localhost:8001/css-validator/validator.html</li>
</ol>

<h3>Command-line gebruik</h3>

<p>De CSS validator kan ook gebruik worden als command-line tool, als je computer
Java ge&iuml;nstalleerd heeft. Build de css-validator.jar zoals hierboven beschreven, en draai hem als:<br />
<kbd>java -jar css-validator.jar http://www.w3.org/</kbd>
</p>
</div>
   <ul class="navbar"  id="menu">
	<li><strong><a href="./" title="Home pagina van de W3C CSS Validatie Service">Home</a></strong> <span class="hideme">|</span></li>
	<li><a href="about.html" title="Over deze service">Over</a> <span class="hideme">|</span></li>
        <li><a href="documentation.html" title="Documentatie voor de W3C CSS Validatie Service">Documentatie</a> <span class="hideme">|</span></li>

        <li><a href="DOWNLOAD.html" title="Download de CSS validator">Download</a> <span class="hideme">|</span></li>
        <li><a href="Email.html" title="Hoe reacties te geven over deze service">Reacties</a> <span class="hideme">|</span></li>
        <li><a href="thanks.html" title="Credits en Erkenning">Credits</a><span class="hideme">|</span></li>
      </ul>

      <ul id="lang_choice">
     
     <li><a href="DOWNLOAD.html.bg"
    lang="bg"
    xml:lang="bg"
    hreflang="bg"
    rel="alternate">Български</a></li>
  <li><a href="DOWNLOAD.html.de"
         lang="de"
         xml:lang="de"
         hreflang="de"
         rel="alternate">Deutsch</a>
     </li>
     
     <li><a href="DOWNLOAD.html.en"
         lang="en"
         xml:lang="en"
         hreflang="en"
         rel="alternate">English</a>
     </li>
     
     <li><a href="DOWNLOAD.html.es"
         lang="es"
         xml:lang="es"
         hreflang="es"
         rel="alternate">Español</a>
     </li>
     
     <li><a href="DOWNLOAD.html.fr"
         lang="fr"
         xml:lang="fr"
         hreflang="fr"
         rel="alternate">Français</a>
     </li>
     
     <li><a href="DOWNLOAD.html.ko"
         lang="ko"
         xml:lang="ko"
         hreflang="ko"
         rel="alternate">한국어</a>
     </li>
     
     <li><a href="DOWNLOAD.html.it"
         lang="it"
         xml:lang="it"
         hreflang="it"
         rel="alternate">Italiano</a>
     </li>
     
     <li><a href="DOWNLOAD.html.nl"
         lang="nl"
         xml:lang="nl"
         hreflang="nl"
         rel="alternate">Nederlands</a>
     </li>
     
     <li><a href="DOWNLOAD.html.ja"
         lang="ja"
         xml:lang="ja"
         hreflang="ja"
         rel="alternate">日本語</a>
     </li>
     
     <li><a href="DOWNLOAD.html.pl-PL"
         lang="pl-PL"
         xml:lang="pl-PL"
         hreflang="pl-PL"
         rel="alternate">Polski</a>
     </li>
     
     <li><a href="DOWNLOAD.html.pt-BR"
         lang="pt-BR"
         xml:lang="pt-BR"
         hreflang="pt-BR"
         rel="alternate">Português</a>
     </li>
     
     <li><a href="DOWNLOAD.html.ru"
         lang="ru"
         xml:lang="ru"
         hreflang="ru"
         rel="alternate">Русский</a>
     </li>
     
     <li><a href="DOWNLOAD.html.sv"
         lang="sv"
         xml:lang="sv"
         hreflang="sv"
         rel="alternate">Svenska</a>
     </li>
     
     <li><a href="DOWNLOAD.html.zh-cn"
         lang="zh-cn"
         xml:lang="zh-cn"
         hreflang="zh-cn"
         rel="alternate">简体中文</a>
     </li>
</ul>



   <div id="footer">
   <p id="activity_logos">

      <a href="https://www.w3.org/Style/CSS/learning" title="Leer meer over Cascading Style Sheets"><img src="images/woolly-icon" alt="CSS" /></a>
   </p>

   <p id="support_logo">
   <a href="https://www.w3.org/donate/">
   <img src="https://www.w3.org/QA/Tools/I_heart_validator" alt="I heart Validator logo" title=" Validators Donation Program" />
   </a>
   </p>

   <p class="copyright"><span lang="en" dir="ltr">Copyright &copy; 2025 <a href="https://www.w3.org/">World Wide Web Consortium</a>.<br> <abbr title="World Wide Web Consortium">W3C</abbr><sup>&reg;</sup> <a href="https://www.w3.org/policies/#disclaimers">liability</a>, <a href="https://www.w3.org/policies/#trademarks">trademark</a> and <a rel="license" href="https://www.w3.org/copyright/document-license/" title="W3C Document License">permissive license</a> rules apply.</span></p>

</div>
  </body>

</html>




