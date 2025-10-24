<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="sv" lang="sv">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Ladda ned och installera CSS-valideraren</title>
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
   <a href="./"><span>CSS valideringstjänst</span></a></h1>
   <p id="tagline">
     Granska formatmallar (<span xml:lang="en" lang="en">Cascading Style Sheets</span>, CSS) och (X)HTML-dokument med formatmallar
   </p>
  </div>

<div class="doc">
<h2>Ladda ned och installera CSS-valideraren</h2>


<p>This translation of the installation guide for the CSS validator may be out of date. For a reliable, up-to-date guide, refer to the <a href="DOWNLOAD.html.en">English</a> or <a href="DOWNLOAD.html.fr">French</a> versions.</p>




<h3 id="download">Ladda ned CSS-valideraren</h3>	

<h4 id="source">Ladda ned källkod</h4>
<p>
  <a href='https://github.com/w3c/css-validator'>CSS-valideraren</a>
  kan laddas ned med CVS.
  Följ <a href='http://dev.w3.org/cvsweb/'>anvisningarna</a> om
  hur man kontaktar W3C:s offentliga CVS-server, och hämta 2002/css-validator.
  Lägg märke till att online-versionen av CSS-valideraren i allmänhet är
  äldre än CVS-versionen, så resultat och utseende kan variera något...
</p>	
<h4>Ladda ned som Java-paket (jar eller war)</h4>
<!--	<p>Att åtgärdas... vi behöver en stabil plats där jar/war-arkiv kan lagras regelbundet</p>-->
<p>
  <a href="https://github.com/w3c/css-validator/releases/latest/download/css-validator.jar">css-validator.jar</a>
</p>

<h3>Installationsanvisningar</h3>
<p>
  CSS valideringstjänst är ett servlet-program, skriven i Java.
  Den kan installeras i godtycklig servlet-motor, och kan också användas
  från kommandoraden.
  W3C:s officiella CSS-valideringstjänst körs med Jigsaw-servern, vilket
  är den rekommenderade miljön.
  Men för att göra beskrivningen enkel så ger vi här information om hur
  den kan installeras som en online-servlet i Apaches servlet-motor Tomcat.
</p>

<p>
  Några anvisningar om hur man installerar som servlet i Jigsaw, liksom
  hur man  kör från kommandoraden, ges länge ned i detta dokument.
</p>

<h4 id="prereq">Förutsättningar</h4>

<p>
  Denna installationsanvisning antar att du har laddat ned, installerat
  och testat:
</p>

<ul class="instructions">
  <li>En fungerande Javamiljö,
  </li>
  <li><a href="https://ant.apache.org/">Ant</a> hopbyggnadsverktyg för Java
  </li>
  <li>En Java webb-servlet förpackning, såsom
      <a href="https://www.w3.org/Jigsaw/">Jigsaw</a>,
      <a href="https://tomcat.apache.org/">Tomcat</a> eller
      <a href="http://www.mortbay.org/">Jetty</a>,
      vilket behövs om du vill använda valideraren som en online-tjänst.
      Denna handledning ger enbart detaljer för Tomcat och Jigsaw.
  </li>
</ul>

<p id="prereq-libs">
  För att installera valideraren på ditt system, så behöver du ladda
  ned ett antal Java-bibliotek, och/eller ta reda på var i ditt system
  de finns:
</p>
<ul class="instructions">
  <li>servlet.jar
      om Tomcat är installerad i [<span class="const">TOMCAT_DIR</span>],
      så borde den finnas i
      [<span class="const">TOMCAT_DIR</span>]/common/lib/,
      möjligen under namnet servlet-api.jar.
      Om den inte finns där, så kan du hämta den på
      <a href="http://java.sun.com/products/servlet/DOWNLOAD.html">java.sun.com</a>
  </li>
  <li><a href="https://jigsaw.w3.org/Devel/classes-2.2/20060329/">jigsaw.jar</a>
  </li>
  <li>xercesImpl.jar och xml-apis.jar (som kan laddas ned med
      <a href="https://www.apache.org/dist/xml/xerces-j/">xerces-j-bin</a>).
  </li>
  <li><a href="http://ccil.org/~cowan/XML/tagsoup/">tagsoup.jar</a>
  </li>
</ul>

<h4>Installera CSS-valideraren i Tomcat</h4>
<ol class="instructions">
  <li>
      Ladda ned valideraren som förklaras <a href="#source">ovan</a>.
  </li>
  <li>
      Kopiera hela källkodsmappen
      ("<span class="dir">.../css-validator/</span>")
      till mappen <span class="dir">webapps</span> i din installation av
      Tomcat.
      Vanligtvis är detta
      <span class="dir">[<span class="const">TOMCAT_DIR</span>]/webapps/</span>.
      Validerarens källkod är nu i
      <span class="dir">[<span class="const">TOMCAT_DIR</span>]/webapps/css-validator</span>,
      som vi nu kallar
      <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>.
  </li>
  <li>
      I
      "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>"
      ska du nu skapa en mapp "<span class="dir">WEB-INF</span>",
      och i
      "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF</span>"
      skapar du en mapp  "<span class="dir">lib</span>":<br />
      <kbd>mkdir -p WEB-INF/lib</kbd>
  </li>
  <li>
      Kopiera alla jar-filer (från <a href="#prereq-libs">förutsättningar</a>)
      till mappen
      "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/lib</span>"
  </li>
  <li>
      Kompilera validerarens källkod: från mappen
      <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>,
      kör <kbd>ant</kbd>, efter att ha kontrollerat att jar-filerna du
      laddat ned finns i din miljövariablel CLASSPATH.
      I allmänhet fungerar följande:<br />
      <kbd>CLASSPATH=.:./WEB-INF/lib:$CLASSPATH ant</kbd>
  </li>
  <li>
      Kopiera eller flytta
      "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/</span><span class="file">css-validator.jar</span>"
      till
      "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/lib/</span>".
  </li>
  <li>
      Kopiera eller flytta filen "<span class="file">web.xml</span>" 
      från
      "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/</span>" 
      till
      "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/</span>".
  </li>
  <li>
      Slutligen, starta om Tomcat-servern:<br />
      <kbd>"cd <span class="dir">[<span class="const">TOMCAT_DIR</span>]</span>; <span class="dir">./bin/</span><span class="file">shutdown.sh</span>; <span class="dir">./bin/</span><span class="file">startup.sh</span>;"</kbd>
  </li>
</ol>

<h4>Installera i webservern Jigsaw</h4>
<ol class="instructions">
  <li>
      Först laddar du ned källkod som beskrivits tidigare,
      hämtar de jar-filer som behövs, och bygger källkoden med 
      <kbd>ant</kbd>.
  </li>
      
  <li>
      Sedan konfigurerar du validerarens folder (vanligen är detta
      css-validator) så att den kan fungera som en servlet-container.
      För att göra detta behöver du ha Jigsaw installerad (en kort
      beskrivning av hur detta enkelt kan göras finns på
      Jigsaw-sidorna) och sedan startar du Jigsaw Admin.  Ändra
      HTTPFrame till ServletDirectoryFrame.
  </li>
      
  <li>
      Nästa steg är att skapa en resurs "validator", med
      'ServletWrapper' som klass och 'ServletWrapperFrame' som frame.
      Den senare borde automatiskt lägga till sig själv.
      Servlet-klassen är org.w3c.css.servlet.CssValidator.  Om det
      redan finns en fil med namnet 'validator', så bör du döpa om
      den.  Det är viktigt att detta 'alias' alltid benämnes
      'validator'.
  </li>
      
  <li>
      Slutligen skall du starta Jigsaw och köra valideraren.
      Ta reda på vilken HTML du vill använda.
      Vanligen kommer din URL att se ut som:<br />
      http://localhost:8001/css-validator/validator.html
  </li>
</ol>

<h3>Användning från kommandoraden</h3>

<p>
  CSS-valideraren kan även användas som ett kommandoradverktyg,
  om Java är installerad på din dator.
  Bygg css-validator.jar som beskrivits ovan, och kör som:<br />
    <kbd>java -jar css-validator.jar http://www.w3.org/</kbd>
</p>
</div>

<ul class="navbar"  id="menu">
  <li><strong><a href="./" title="Hemsida för W3C:s CSS valideringstjänst">Hemsida</a></strong> <span class="hideme">|</span>
  </li>
  <li><a href="about.html" title="Om denna tjänst">Om</a> <span class="hideme">|</span>
  </li>
  <li><a href="documentation.html" title="Dokumentation om W3C:s CSS valideringstjänst">Dokumentation</a> <span class="hideme">|</span>
  </li>
  <li><a href="Email.html" title="Hur man ger synpunkter på denna tjänst">Synpunkter</a> <span class="hideme">|</span>
  </li>
  <li><a href="thanks.html" title="Tack och erkännanden">Tack</a><span class="hideme">|</span>
  </li>
      
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




