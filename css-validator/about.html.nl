<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nl" lang="nl">
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Over de W3C CSS Validation Service</title>
    <link rev="made" href="mailto:www-validator-css@w3.org" />
    <link rev="start" href="./" title="Home Pagina" />
    <style type="text/css" media="all">
	@import "style/base.css";
	@import "style/docs.css";
    </style>
    <meta name="revision" content="$Id$" />

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

      <h2>Over de CSS Validator</h2>

<h3 id="TableOfContents">Index</h3>
<div id="toc">
<ol>
  <li>Over deze Service
  <ol>
<li><a href="#what">Wat is dit? Heb ik dit nodig?</a></li>
<li><a href="#help">De uitleg hierboven gaat mij te snel! Help!</a></li>
<li><a href="#reference">Dus deze service bepaalt wat wel of geen correct CSS is?</a></li>
<li><a href="#free">Wat kost dit mij?</a></li>
<li><a href="#who">Wie heeft deze tool geschreven? En wie onderhoudt hem?</a></li>

<li><a href="#contact">Hoe kom ik in contact met de auteurs? Hoe stuur ik een nieuw probleem in?</a></li>
<li><a href="#participate">Kan ik helpen?</a></li>
  </ol>
  </li>
  <li>Voor Ontwikkelaars
  <ol>
    <li><a href="#code">In welke taal is de CSS Validator geschreven? Is de broncode ergens beschikbaar?</a></li>
    <li><a href="#install">Kan ik de CSS Validator zelf installeren en draaien?</a></li>

    <li><a href="#api">Kan ik een applicatie maken die gebruik maakt van de validator? Is er een API?</a></li>
  </ol>
  </li>
</ol>
</div><!-- toc -->
<h3 id="about">Over deze Service</h3>

<h4 id="what">Wat is dit? Heb ik dit nodig?</h4>

<p>De W3C CSS Validation Service is een gratis stukje software gemaakt door het W3C 
om Web designers en Web developers te helpen hun Cascading Style Sheets (CSS) te controleren op geldigheid.
De service kan <a href="./">gratis</a> gebruikt worden op het web, of worden gedownload 
en gebruikt als java programma of als java servlet op een Web server.</p>

<p>Heb <em>jij</em> dit nodig? Als je een Web developer of een Web designer bent zal deze tool 
een erg waardevol hulpmiddel zijn. Je style sheets worden niet alleen vergeleken met de offici&euml;le 
CSS specificaties, de tool helpt je ook errors, typefouten en incorrect gebruik van CSS te vinden. De validator
geeft ook aan als jouw CSS mogelijk risico's met zich meebrengt als het gaat over gebruikersvriendelijkheid.</p>

<h4 id="help">De uitleg hierboven gaat mij te snel! Help!</h4>
<p>De meeste documenten op het Web zijn geschreven in een computertaal die HTML wordt genoemd. Deze taal
kan gebruikt worden om webpagina's te cre&euml;ren met gestructureerde informatie, links en multimedia objecten.
Om kleuren, tekst en lay-out op te maken maakt HTML gebruik van de stylingtaal CSS, een afkorting voor "Cascading Style Sheets". 
Deze tool helpt mensen die zich bezig houden met het ontwikkelen van webpagina's hun CSS te valideren en eventueel te herstellen.</p>

<h4 id="reference">Dus deze service bepaalt wat wel of geen correct CSS is?</h4>
<p>Nee. Het is een helpvolle en betrouwbare tool, maar het blijft software, en zoals elk stuk software heeft de validator een aantal
<a href="https://github.com/w3c/css-validator/issues">bugs en problemen</a> &amp; <a href="https://www.w3.org/Bugs/Public/buglist.cgi?product=CSSValidator">legacy bugs en problemen</a>.
De echte referentie over Cascading Style Sheets zijn de offici&euml;le <a href="https://www.w3.org/Style/CSS/#specs">CSS 
Specificaties</a>.</p>

<h4 id="free">Wat kost dit mij?</h4>
<p>Niets. De service is gratis. De broncode is <a href="DOWNLOAD.html">open</a> en je bent vrij om de code te downloaden,
te gebruiken, te wijzigen, te verspreiden 
en <a href="https://www.w3.org/Consortium/Legal/copyright-software">meer</a>.
Verder ben je, als je dat wilt, welkom om <a href="#participate">mee te werken</a> aan dit project of om te doneren aan het W3C via het
 <a href="https://www.w3.org/Consortium/sup">W3C supporters programma</a>.</p>

<h4 id="who">Wie heeft deze tool geschreven? En wie onderhoudt hem?</h4>

<p>W3C onderhoudt en bewaart de validator, met dank aan de bijdragen van het W3C en van
vrijwillige ontwikkelaars en vertalers. Zie de <a href="thanks.html">bedankpagina</a>
voor details. <a href="#participate">Ook jij kunt helpen</a>.</p>

<h4 id="participate">Kan ik helpen?</h4>
<p>Natuurlijk, graag zelfs! Als je een java programmeur bent kun je meehelpen aan het CSS Validator project door 
de <a href="#code">broncode</a> op te halen en te helpen bij het oplossen van <a href="https://github.com/w3c/css-validator/issues">bugs</a> &amp; <a href="https://www.w3.org/Bugs/Public/buglist.cgi?product=CSSValidator">legacy bugs</a>,
of door het helpen bij het implementeren van nieuwe functies.</p>
<p>Maar je hoeft niet perse een coder te zijn om te helpen bij het bouwen en onderhouden van deze tool: je kunt ook helpen de documentatie
te verbeteren, mee te werken door de validator in jouw taal te vertalen, of door je aan te melden bij de
<a href="https://lists.w3.org/Archives/Public/www-validator-css/">mailing-list</a> en te discussi&euml;ren over de tool of anderen te helpen.</p>

<h4 id="contact">Heb je verder nog vragen?</h4>
<p>Mocht je verder nog vragen hebben over CSS of de CSS validator, bekijk dan even de beschikbare
<a href="Email">mailing-lists en fora</a>, maar controleer voordat je dat doet of je vraag nog niet is beantwoord in de
<a href="http://www.websitedev.de/css/validator-faq">CSS Validator <acronym title="Frequently Asked Questions (Veelgestelde vragen)">FAQ</acronym></a>.</p>

<h3 id="dev">Voor Ontwikkelaars</h3>
<h4 id="code">In welke taal is de CSS Validator geschreven? Is de broncode ergens beschikbaar?</h4>
<p>De W3C CSS validator is geschreven in java en ja, de broncode is beschikbaar.
Door middel van CVS kun je de code <a href="https://github.com/w3c/css-validator">online bekijken</a> 
of volg de instructies op die pagina om alle beschikbare code te downloaden. Voor een snel overzicht van de classes die gebruikt worden in de
CSS Validator code kun je de <a href="README.html">README</a> lezen.</p>

<h4 id="install">Kan ik de CSS Validator zelf installeren en draaien?</h4>
<p>Het is mogelijk om de CSS validator te downloaden en te installeren, en het te draaien van de command line, of 
als een servlet op een Web server. Lees de <a href="DOWNLOAD.html">instructies</a> voor meer informatie over de installatie en het gebruik van de validator.</p>

<h4 id="api">Kan ik een applicatie maken die gebruik maakt van de validator? Is er een API?</h4>
<p>Ja, en <a href="api.html">ja</a>. De CSS Validator heeft een (RESTful) <a href="api.html">SOAP interface</a>
die het relatief gemakkelijk zou moeten maken om applicaties (zowel voor het Web of stand alone) op de validator te ontwikkelen. Natuurlijk is het hierbij 
belangrijk de ter beschikking gestelde bronnen te respecteren: zorg ervoor dat je applicaties even wachten (sleep()) tussen de aanroepen naar de validator
of installeer en draai je eigen instantie van de validator.</p>
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
     
     <li><a href="about.html.bg"
    lang="bg"
    xml:lang="bg"
    hreflang="bg"
    rel="alternate">Български</a></li>
  <li><a href="about.html.de"
         lang="de"
         xml:lang="de"
         hreflang="de"
         rel="alternate">Deutsch</a>
     </li>
     
     <li><a href="about.html.en"
         lang="en"
         xml:lang="en"
         hreflang="en"
         rel="alternate">English</a>
     </li>
     
     <li><a href="about.html.es"
         lang="es"
         xml:lang="es"
         hreflang="es"
         rel="alternate">Español</a>
     </li>
     
     <li><a href="about.html.fr"
         lang="fr"
         xml:lang="fr"
         hreflang="fr"
         rel="alternate">Français</a>
     </li>
     
     <li><a href="about.html.ko"
         lang="ko"
         xml:lang="ko"
         hreflang="ko"
         rel="alternate">한국어</a>
     </li>
     
     <li><a href="about.html.it"
         lang="it"
         xml:lang="it"
         hreflang="it"
         rel="alternate">Italiano</a>
     </li>
     
     <li><a href="about.html.nl"
         lang="nl"
         xml:lang="nl"
         hreflang="nl"
         rel="alternate">Nederlands</a>
     </li>
     
     <li><a href="about.html.ja"
         lang="ja"
         xml:lang="ja"
         hreflang="ja"
         rel="alternate">日本語</a>
     </li>
     
     <li><a href="about.html.pl-PL"
         lang="pl-PL"
         xml:lang="pl-PL"
         hreflang="pl-PL"
         rel="alternate">Polski</a>
     </li>
     
     <li><a href="about.html.pt-BR"
         lang="pt-BR"
         xml:lang="pt-BR"
         hreflang="pt-BR"
         rel="alternate">Português</a>
     </li>
     
     <li><a href="about.html.ru"
         lang="ru"
         xml:lang="ru"
         hreflang="ru"
         rel="alternate">Русский</a>
     </li>
     
     <li><a href="about.html.sv"
         lang="sv"
         xml:lang="sv"
         hreflang="sv"
         rel="alternate">Svenska</a>
     </li>
     
     <li><a href="about.html.zh-cn"
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
