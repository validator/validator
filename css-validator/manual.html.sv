<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="sv" lang="sv">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Användarmanual för CSS-valideraren</title>
  <link rev="made" href="mailto:www-validator-css@w3.org" />
  <link rev="start" href="./" title="Hemsida" />
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
   <a href="./"><span>CSS-valideringstjänst</span></a></h1>
   <p id="tagline">
     Granska formatmallar (<span xml:lang="en" lang="en">Cascading Style Sheets</span>, CSS) och (X)HTML-dokument med formatmallar
   </p>
  </div>

<div id="main">
<!-- This DIV encapsulates everything in this page - necessary for the positioning -->

<div class="doc">
<h2>Användarmanual för CSS-valideraren</h2>

<h3 id="TableOfContents">Innehållsförteckning</h3>

<div id="toc">
<ul>
  <li><a href="#use">Hur man använder CSS-valideraren</a>
    <ul>
      <li><a href="#url">Validering med URL</a></li>
      <li><a href="#fileupload">Validering genom filuppladdning</a></li>
      <li><a href="#directinput">Validering genom direkt inmatning</a></li>
      <li><a href="#basicvalidation">Vad åstadkommer enkel validering?</a></li>
    </ul>
  </li>
  <li><a href="#advanced">Avancerad validering</a>
    <ul>
	<li><a href="#paramwarnings">Varningsparameter</a></li>
	<li><a href="#paramprofile">Profilparameter</a></li>
	<li><a href="#parammedium">Mediumparameter</a></li>
    </ul>
  </li>
  <li><a href="#expert">För experter</a>
    <ul>
	<li><a href="#requestformat">Format för valideringsbegäran</a></li>
	<li><a href="#api">Web Service API till CSS_valideraren</a></li>
    </ul>
   </li>
</ul>
</div>

<p id="skip"></p>

<h3 id="use">Hur man använder CSS-valideraren</h3>

<p>

Det enklaste sättet att granska ett dokument är att använda det enkla
gränssnittet. På denna sida hittar du tre formulär som svarar mot tre
möjligheter:

</p>

<h4 id="url">Validering med URL</h4>
<p>

    Mata helt enkelt in URL för det dokument du vill validera.
    Det kan vara ett HTML- eller CSS-dokument.

</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/uri_basic.png" alt="Validering med URI-formulär" />

<h4 id="fileupload">Validering genom filuppladdning</h4>    
<p>

    Med denna lösning kan du ladda upp och granska en lokalt lagrad fil.
    Klicka på "Browse..."-knappen och välj den fil du vill granska.

</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/file_upload_basic.png" 
    alt="Validering genom filuppladdning" />
<p>

    I detta fall är endast CSS-dokument tillåtna. Det betyder att
    du inte kan ladda upp (X)HTML-dokument. Du måste också vara
    försiktig med @import-regler, efterom de endast kommer att
    hämtas om de explicit refererar till en publik URL (relativa URL:er
    fungerar inte med denna lösning).

</p>
   
<h4 id="directinput">Validering genom direkt inmatning</h4>
<p>

    Denna metod är bra om du vill granska CSS-fragment.
    Du behöver bara skriva in din CSS i textytan.

</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/direct_input_basic.png" 
    alt="Validering genom direkt inmatning" />
<p>

    Samma kommentarer som ovan gäller även här. Lägg märke till att
    denna lösning är praktisk om du har ett problem och behöver få hjälp
    från andra användare. Den kan också användas för att rapportera
    fel i valideraren, eftersom du kan ge en länk till din URL som ett
    testfall.

</p>    

<h4 id="basicvalidation">Vad åstadkommer enkel validering?</h4>

<p>    

    Då du använder det enkla gränssnittet så kommer valideraren att granska
    om din kod är korrekt enligt
    <a href="https://www.w3.org/TR/CSS2">CSS 2</a>, som är den aktuella
    tekniska CSS-standarden.<br />

    Den genererar XHTML-utdata med beskrivning av felen, men ger inte
    några varningar.<br />

    Medium sätts till "<code>all</code>", som är det medium som är
    lämpligt för alla apparater (dokumentet
    <a href="https://www.w3.org/TR/CSS2/media.html">http://www.w3.org/TR/CSS2/media.html</a>
    ger en fullständig beskrivning av media).

</p>

<h3 id="advanced">Avancerad validering</h3>

<p>

    Om du behöver en mer genomgående granskning så kan du använda det
    avancerade gränssnittet, där du kan sätta tre parametrar. Här ges
    lite hjälp om hur dessa parametrar används.

</p>

<h4 id="paramwarnings">Varningar</h4>

<p>

    Denna parameter är användbar för att påverka hur mycket
    information CSS-valideraren ger. Valideraren kan ge dig två typer
    av meddelanden: felmeddelanden och varningsmeddelanden.

    Felmeddelanden ges när den CSS som granskas inte uppfyller
    CSS-standarden.

    Varningsmeddelanden skiljer sig från felmeddelanden eftersom de
    inte anmärker på att din kod avviker från standarden. Istället
    varnar de CSS-utvecklaren om att någonting kan vara problematiskt
    och kan ge konstiga beteenden i vissa användaragenter
    (webbläsare).

</p><p>

    En typisk varning handlar om <code>font-family</code>: om du inte
    erbjuder en generisk font så kommer du att få ett
    varningsmeddelande som säger att du borde lägga till en sådan i
    slutet av regeln, annars kommer en användaragent, som inte känner
    till de andra fonterna, att byta till sin standardfont, och detta
    kan resultera i konstigt presentation.

</p>

<h4 id="paramprofile">Profil</h4>

<p>

    CSS-valideraren kan granska olika CSS-profiler.

    En profil ger en förteckning över alla de funktionaliteter som en
    implementation på en specifik plattform förväntas stödja.

    Denna definition tas från
    <a href="https://www.w3.org/Style/2004/css-charter-long.html#modules-and-profiles0">CSS-webbplatsen </a>.

    Standardvalet svarar mot den nu mest använda:
    <a href="https://www.w3.org/TR/CSS2">CSS 2</a>.

</p>

<h4 id="parammedium">Medium</h4>

<p>

    Medium-parameter svarar mot <code>@media</code>-regeln, och gäller
    för hela dokumentet.

    Du hittar mer information om media på
    <a href="https://www.w3.org/TR/CSS2/media.html">http://www.w3.org/TR/CSS2/media.html</a>.

</p>

<h3 id="expert">För experter</h3>

<h4 id="requestformat">Format för valideringsbegäran</h4>

<p>

    Nedan finns en tabell över parametrarna du kan använda för att
    skicka en begäran till W3C:s CSS-validerare.

</p><p>

    Om du vill använda W3C:s offentliga valideringstjänst, använd då
    parametrarna nedan tillsammans med följande bas-URI<br />


    <kbd>http://jigsaw.w3.org/css-validator/validator</kbd><br />

    Ersätt denna med adressen till din egen server om du vill anropa
    en privat instans av valideraren.

</p>

<p>

<strong>Märk</strong>:

    Om du vill anropa valideraren programmatiskt för en hel
    uppsättning dokument, försäkra dig om att ditt skript kommer att
    "sova" (<code>sleep</code>) <strong>minst 1 sekund</strong>
    mellan anropen.

    CSS-valideringstjänsten är en gratis offentlig tjänst för alla att
    använda, så vi uppskattar att du tar hänsyn. Tack.

</p>


<table class="refdoc">
  <tbody>
    <tr>
      <th>Parameter</th>
      <th>Beskrivning</th>
      <th>Standardvärde</th>
    </tr>
    <tr>
      <th>uri</th>
      <td>
	  <acronym title="Universal Resource Locator">URL</acronym>:en
	  för det dokument du vill validera. Både CSS- och
	  HTML-dokument går bra
      </td>
      <td>
	  Inget, men antingen måste denna parameter sättas, eller så
	  måste <code>text</code> anges.
      </td>
    </tr>
    <tr>
      <th>text</th>
      <td>
	  Det dokument som skall valideras, men det måste vara CSS.
      </td>
      <td>
	  Inget, men antingen måste denna parameter sättas, eller så
	  måste <code>uri</code> anges.
      </td>
    </tr>
    <tr>
      <th>usermedium</th>
      <td>
	  <a href="https://www.w3.org/TR/CSS2/media.html">medium</a>
	  som skall användas för validering, såsom
	  <code>screen</code>, <code>print</code>,
	  <code>braille</code>...

      </td>
      <td><code>all</code></td>
    </tr>
    <tr>
      <th>output</th>
      <td>
	  Bestämmer utdataformat från valideraren. Möjliga format är 
	  <code>text/html</code> och <code>html</code> (XHTML-dokument, 
	  Content-Type: text/html), 
	  <code>application/xhtml+xml</code> och <code>xhtml</code>
	  (XHTML-dokument, Content-Type: application/xhtml+xml),
	  <code>application/soap+xml</code> och <code>soap12</code>
	  (SOAP 1.2-dokument, Content-Type: application/soap+xml),
	  <code>text/plain</code> och <code>text</code> (textdokument, 
	  Content-Type: text/plain),
	  allt annat (XHTML-dokument, Content-Type: text/html)	
      </td>
      <td>
	  html
      </td>
    </tr>
    <tr>
      <th>profile</th>
      <td>
	  Den CSS-profil som skall användas vid validering. Det kan
	  vara
	  <code>css1</code>, <code>css2</code>, <code>css21</code>,
	  <code>css3</code>, <code>svg</code>, <code>svgbasic</code>,
	  <code>svgtiny</code>, <code>mobile</code>, <code>atsc-tv</code>,
	  <code>tv</code> eller <code>none</code>
      </td>
      <td>
	  Den senaste W3C-standarden (<span xml:lang="en"
	  lang="en">Recommendation</span>): CSS 2
      </td>
    </tr>
    <tr>
      <th>lang</th>
      <td>
	  Det språk som skall användas i utdatarapporten; för
	  närvarande en av

	  <code>en</code>, <code>fr</code>, <code>ja</code>,
	  <code>es</code>, <code>zh-cn</code>, <code>nl</code>,
	  <code>de</code>, <code>it</code>, <code>pl</code>, och
	  <code>sv</code>.

      </td>
      <td>
	  Engelska (<code>en</code>).
      </td>
    </tr>
    <tr>
      <th>warning</th>
      <td>
	  Varningsnivå, <code>no</code> för inga varningar,
	  <code>0</code> för få varningar, <code>1</code> eller
	  <code>2</code> för fler varningar.
      </td>
      <td>
	  2
      </td>
    </tr>
  </tbody>
</table>

<h4 id="api">Web Service API till CSS_valideraren: dokumentation av SOAP 1.2 gränssnitt</h4>

<p>

    Om du vill ha mer teknisk hjälp, speciellt om SOAP 1.2-utdata och alla
    möjliga sätt att anropa valideraren, läs
    <a href="./api.html">Web Service-gränssnitt för CSS-valideraren</a>.       
</p>

</div>
</div>
<!-- End of "main" DIV. -->

<ul class="navbar"  id="menu">
   <li><strong><a href="./" title="Hemsida för W3C:s CSS valideringstjänst">Hemsida</a></strong> <span class="hideme">|</span></li>
   <li><a href="about.html" title="Om denna tjänst">Om</a> <span class="hideme">|</span></li>
   <li><a href="documentation.html" title="Dokumentation om W3C:s CSS valideringstjänst">Dokumentation</a> <span class="hideme">|</span></li>
   <li><a href="DOWNLOAD.html" title="Ladda ned CSS-valideraren">Ladda ned</a> <span class="hideme">|</span></li>
   <li><a href="Email.html" title="Hur man ger synpunkter på denna tjänst">Synpunkter</a> <span class="hideme">|</span></li>
   <li><a href="thanks.html" title="Tack och erkännanden">Tack</a><span class="hideme">|</span></li>
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

  <a href="https://www.w3.org/Style/CSS/learning" title="Mer information om formatmallar"><img src="images/woolly-icon" alt="CSS" /></a>
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



