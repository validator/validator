<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nl" lang="nl">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>CSS Validator User Manual</title>
  <link rev="made" href="mailto:www-validator-css@w3.org" />
  <link rev="start" href="./" title="Home Pagina" />
  <style type="text/css" media="all">
    @import "style/base.css";    
    @import "style/docs.css";
  </style>
  <meta name="revision"
  content="$Id$" />
  <!-- SSI Template Version: $Id$ -->

</head>

<body>
  <div id="banner">
   <h1 id="title"><a href="https://www.w3.org/"><img alt="W3C" width="110" height="61" id="logo" src="./images/w3c.png" /></a>
   <a href="./"><span>CSS Validation Service</span></a></h1>
   <p id="tagline">
     Check Cascading Style Sheets (CSS) and (X)HTML documents with style sheets
   </p>
  </div>

<div id="main">
<!-- This DIV encapsulates everything in this page - necessary for the positioning -->

<div class="doc">

<h2>CSS Validator Gebruikers Handleiding</h2>

<h3 id="TableOfContents">Inhoudsopgave</h3>

<div id="toc">
<ul>
  <li><a href="#use">Hoe gebruik ik de CSS Validator</a>
    <ul>
      <li><a href="#url">Valideren via het opgeven van een URL</a></li>
      <li><a href="#fileupload">Valideren via het uploaden van een bestand</a></li>

      <li><a href="#directinput">Valideren via de directe invoer</a></li>
      <li><a href="#basicvalidation">Wat doet de basis validatie?</a></li>
    </ul>
  </li>
  <li><a href="#advanced">Geavanceerde validatie</a>
    <ul>
	<li><a href="#paramwarnings">Waarschuwingen</a></li>

	<li><a href="#paramprofile">Profiel</a></li>
	<li><a href="#parammedium">Medium</a></li>
    </ul>
  </li>
  <li><a href="#expert">Voor de experts</a>
    <ul>
	<li><a href="#requestformat">Validation Verzoek Formaat</a></li>

	<li><a href="#api">CSS Validator Web Service API</a></li>
    </ul>
   </li>
</ul>
</div>

<p id="skip"></p>

<h3 id="use">Hoe gebruik ik de CSS Validator</h3>

<p>
De makkelijkste manier om een document te checken is door de basis interface te gebruiken. In die pagina
 vind je drie formulieren die corresponderen met drie mogelijkheden:

</p>

<h4 id="url">Valideren via het opgeven van een URL</h4>
<p>
    Voer simpelweg de URI in van het document dat je wilt valideren. 
    Het document kan zowel het HTML als het CSS formaat zijn.
</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/uri_basic_nl.png" alt="Validatie door middel van een URI" />

<h4 id="fileupload">Valideren via het uploaden van een bestand</h4>    
<p>
    Deze oplossing staat je toe een bestand dat je wilt valideren te uploaden.
	Klik op de "Bladeren..." knop en selecteer het bestand dat je wilt controleren.
</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/file_upload_basic_nl.png" 
    alt="Valideren via het uploaden van een bestand" />

<p>
    Deze methode staat alleen CSS documenten toe. Dit betekend dat je geen
	(X)HTML documenten kunt uploaden. Je moet ook zorgvuldig zijn met @import regels,
	omdat deze alleen gevolgd worden als deze expliciet naar een publieke URI verwijzen.
	(dus vergeet relatieve paden als je deze oplossing gebruikt)
</p>
   
<h4 id="directinput">Valideren via de directe invoer</h4>
<p>
    Deze methode is perfect voor het testen van CSS fragmenten. Je schrijft gewoon
	je CSS in het tekstveld.
</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/direct_input_basic_nl.png" 
    alt="Valideren via directe invoer" />
<p>
    Dezelfde opmerkingen als hiervoor zijn van toepassing. Deze methode is erg handig als
	je een probleem hebt en help nodig hebt van de community.
    Dit is ook erg handig om een eventuele bug te rapporteren, 
	omdat je recht kun linken naar de resulterende URL om een voorbeeld te geven.
</p>    

<h4 id="basicvalidation">Wat doet de basis validatie?</h4>

<p>    
    Als de basis interface gebruikt wordt, controleert de validator de geldigheid van de CSS
	met <a href="https://www.w3.org/TR/CSS21">CSS 2.1</a>, de huidige CSS standaard.<br />
    De validator produceert een XHTML output zonder waarschuwingen. (standaard krijg je alleen errors te zien)<br />
    Het medium is ingesteld op "all", want dit is het medium dat geschikt is voor alle apparaten.
    (zie ook <a href="https://www.w3.org/TR/CSS2/media.html">
    http://www.w3.org/TR/CSS2/media.html</a> voor een complete beschrijving van de verschillende typen media)
</p>

<h3 id="advanced">Geavanceerde validatie</h3>

<p>
    Als je een iets meer specifieke controle nodig hebt, kun je de geavanceerde interface gebruiken die je
	toestaat drie parameters in te stellen. Hieronder staat een toelichting voor die drie parameters.
</p>

<h4 id="paramwarnings">Waarschuwingen</h4>

<p>
    Deze parameter is handig om het aantal meldingen dat de validator geeft
	te tunen. De validator kan twee typen berichten geven, namelijk errors en waarschuwingen.
    Errors worden gegeven als de gecontroleerde CSS de CSS standaarden niet respecteert.
	Waarschuwingen zijn verschillend van errors omdat deze geen problemen markeren die te maken hebben
	met de specificaties. Waarschuwingen zijn er om de CSS ontwikkelaar te waarschuwen (!) dat er
	punten zijn die misschien gevaarlijk zijn en zouden kunnen leiden tot onvoorspelbaar gedrag
	in combinatie met sommige browsers.
</p>
<p>

    Een typische waarschuwing heeft te maken met font-family: als je geen generiek lettertype opgeeft,
	zal je een waarschuwing krijgen dat je er een moet toevoegen aan het einde van de regel,
	omdat als de browser de gespecificeerde lettertypen niet kent, hij terug zal vallen op zijn standaard
	lettertype, en dat zou kunnen leiden tot lay-outs die zich onvoorspelbaar gedragen.
</p>

<h4 id="paramprofile">Profiel</h4>

<p>
    De CSS validator kan controleren op verschillende CSS profielen.
	Een profiel definieert alle functies die een implementatie op een specifiek platform
	zou moeten implementeren. Deze definitie wordt gehaald van
    <a href="https://www.w3.org/Style/2004/css-charter-long.html#modules-and-profiles0">
	de CSS webpagina
    </a>. De standaard keuze correspondeert met het profiel dat op dit moment het meest wordt toegepast: 
    <a href="https://www.w3.org/TR/CSS2">CSS 2</a>.
</p>

<h4 id="parammedium">Medium</h4>

<p>
    De parameter medium is hetzelfde als de @media regel, die wordt toegepast op het hele document.
	Je kunt meer informatie over media vinden op 
    <a href="https://www.w3.org/TR/CSS2/media.html">
	http://www.w3.org/TR/CSS2/media.html
    </a>.
</p>

<h3 id="expert">Voor de experts</h3>

<h4 id="requestformat">Validation Verzoek Formaat</h4>

<p>Hieronder staat een tabel met de parameters die je kunt gebruiken om een verzoek te sturen naar de W3C
CSS Validator.</p>

<p>Als je de publieke W3C validatie server wilt gebruiken,
gebruik de parameters hieronder in combinatie met de volgende basis URI:<br />
<kbd>http://jigsaw.w3.org/css-validator/validator</kbd><br />
(vervang de URI met het adres van je eigen server 
als je een eigen instantie van de validator wilt aanroepen.)</p>

<p><strong>Opmerking</strong>: Als je de validator wilt aanroepen voor een groep documenten of vaak achter
elkaar, zorg er dan asjeblieft voor dat je script minstens <strong>een seconde</strong> wacht tussen de verschillende verzoeken.
De CSS Validatie service is een gratis, publieke
service voor iedereen, je respect wordt gewaardeerd. Bedankt.</p>

<table class="refdoc">
  <tbody>
    <tr>
      <th>Parameter</th>
      <th>Beschrijving</th>
      <th>Standaardwaarde</th>
    </tr>

    <tr>
      <th>uri</th>
      <td>De <acronym title="Universal Resource Locator">URL</acronym> van
        het document dat gevalideed moet worden. CSS en HTML documenten zijn toegestaan.</td>
      <td>Geen, maar deze of de <code>text</code> parameter moet opgegeven worden.</td>

    </tr>
    <tr>
      <th>text</th>
      <td>Het te valideren document, alleen CSS is toegestaan.</td>
      <td>Geen, maar deze of de <code>uri</code> parameter moet opgegeven worden.</td>
    </tr>

    <tr>
      <th>usermedium</th>
      <td>Het <a href="https://www.w3.org/TR/CSS2/media.html">medium</a> dat gebruikt 
	  wordt voor de validatie, zoals <code>screen</code>,
	  <code>print</code>, <code>braille</code>...</td>

      <td><code>all</code></td>
    </tr>
    <tr>
      <th>output</th>
      <td>Bepaalt het output formaat van de validator. Mogelijke formaten zijn
	<code>text/html</code> en <code>html</code> (XHTML document, 
	Content-Type: text/html), 
	<code>application/xhtml+xml</code> en <code>xhtml</code> (XHTML 
	document, Content-Type: application/xhtml+xml), 
	<code>application/soap+xml</code> en <code>soap12</code> (SOAP 1.2 
	document, Content-Type: application/soap+xml), 
	<code>text/plain</code> en <code>text</code> (text document, 
	Content-Type: text/plain),
	overig (XHTML document, Content-Type: text/plain)	
      </td>

      <td>html</td>
    </tr>
    <tr>
      <th>profile</th>
      <td>Het CSS profiel gebruikt voor de validatie. Opties zijn:
        <code>css1</code>, <code>css2</code>, <code>css21</code>,
        <code>css3</code>, <code>svg</code>, <code>svgbasic</code>,
        <code>svgtiny</code>, <code>mobile</code>, <code>atsc-tv</code>,
        <code>tv</code> of <code>none</code></td>

      <td>De meest recente W3C Recommendation: CSS 2</td>
    </tr>
    <tr>
      <th>lang</th>
      <td>De taal die gebruikt wordt voor de reactie, op dit moment <code>en</code>,
        <code>fr</code>, <code>it</code>, <code>ko</code>, <code>ja</code>, <code>es</code>,
        <code>zh-cn</code>, <code>nl</code> en <code>de</code>.</td>

      <td>English (<code>en</code>).</td>
    </tr>
    <tr>
      <th>warning</th>
      <td>Het waarschuwingen level, <code>no</code> voor geen waarschuwingen, <code>0</code> 
	voor minder waarschuwingen, <code>1</code> of <code>2</code> voor meer waarschuwingen
      </td>

      <td>2</td>
    </tr>
  </tbody>
</table>

<h4 id="api">CSS Validator Web Service API: SOAP 1.2 validation interface documentation</h4>
<p>    
    Voor technische help, en specifieke vragen over de SOAP 1.2 output en alle 
    mogelijke manieren om de validator aan te spreken, zie de 
    <a href="./api.html">CSS Validator Web Service API</a>.       
</p>

</div>
</div>
<!-- End of "main" DIV. -->

   <ul class="navbar"  id="menu">
	<li><strong><a href="./" title="Home pagina van de W3C CSS Validatie Service">Home</a></strong> <span class="hideme">|</span></li>
	<li><a href="about.html" title="Over deze service">Over</a> <span class="hideme">|</span></li>
        <li><a href="documentation.html" title="Documentatie voor de W3C CSS Validatie Service">Documentatie</a> <span class="hideme">|</span></li>

        <li><a href="DOWNLOAD.html" title="Download de CSS validator">Download</a> <span class="hideme">|</span></li>
        <li><a href="Email.html" title="Hoe reacties te geven over deze service">Reacties</a> <span class="hideme">|</span></li>
        <li><a href="thanks.html" title="Credits en Erkenning">Credits</a><span class="hideme">|</span></li>
      </ul>

      <ul id="lang_choice">
     
     <li><a href="manual.html.bg"
    lang="bg"
    xml:lang="bg"
    hreflang="bg"
    rel="alternate">Български</a></li>
  <li><a href="manual.html.de"
         lang="de"
         xml:lang="de"
         hreflang="de"
         rel="alternate">Deutsch</a>
     </li>
     
     <li><a href="manual.html.en"
         lang="en"
         xml:lang="en"
         hreflang="en"
         rel="alternate">English</a>
     </li>
     
     <li><a href="manual.html.es"
         lang="es"
         xml:lang="es"
         hreflang="es"
         rel="alternate">Español</a>
     </li>
     
     <li><a href="manual.html.fr"
         lang="fr"
         xml:lang="fr"
         hreflang="fr"
         rel="alternate">Français</a>
     </li>
     
     <li><a href="manual.html.ko"
         lang="ko"
         xml:lang="ko"
         hreflang="ko"
         rel="alternate">한국어</a>
     </li>
     
     <li><a href="manual.html.it"
         lang="it"
         xml:lang="it"
         hreflang="it"
         rel="alternate">Italiano</a>
     </li>
     
     <li><a href="manual.html.nl"
         lang="nl"
         xml:lang="nl"
         hreflang="nl"
         rel="alternate">Nederlands</a>
     </li>
     
     <li><a href="manual.html.ja"
         lang="ja"
         xml:lang="ja"
         hreflang="ja"
         rel="alternate">日本語</a>
     </li>
     
     <li><a href="manual.html.pl-PL"
         lang="pl-PL"
         xml:lang="pl-PL"
         hreflang="pl-PL"
         rel="alternate">Polski</a>
     </li>
     
     <li><a href="manual.html.pt-BR"
         lang="pt-BR"
         xml:lang="pt-BR"
         hreflang="pt-BR"
         rel="alternate">Português</a>
     </li>
     
     <li><a href="manual.html.ru"
         lang="ru"
         xml:lang="ru"
         hreflang="ru"
         rel="alternate">Русский</a>
     </li>
     
     <li><a href="manual.html.sv"
         lang="sv"
         xml:lang="sv"
         hreflang="sv"
         rel="alternate">Svenska</a>
     </li>
     
     <li><a href="manual.html.zh-cn"
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



