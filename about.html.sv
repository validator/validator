<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="sv" lang="sv">
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Om W3C:s CSS valideringstjänst</title>
    <link rev="made" href="mailto:www-validator-css@w3.org" />
    <link rev="start" href="./" title="Hemsida" />
    <style type="text/css" media="all">
	@import "style/base.css";
	@import "style/docs.css";
    </style>
    <meta name="revision" content="$Id$" />
  </head>

<body>
<div id="banner">
<h1 id="title"><a href="https://www.w3.org/"><img alt="W3C" width="110" height="61" id="logo" src="./images/w3c.png" /></a>
  <a href="./"><span>CSS valideringstjänst</span></a></h1>
     <p id="tagline">
       Granska formatmallar ("<span xml:lang="en" lang="en">Check Cascading Style Sheets</span>, CSS) och (X)HTML-dokument med formatmallar
     </p>
    </div>
   <div class="doc">

<h2>Om CSS-valideraren</h2>

<h3 id="TableOfContents">Innehåll</h3>
<div id="toc">
<ol>
  <li><a href="#about">Om denna tjänst</a>
  <ol>
<li><a href="#what">Vad är detta? Behöver jag den?</a></li>
<li><a href="#help">Förklaringen ovan är obegriplig! Hjälp!</a></li>
<li><a href="#reference">Så, detta är den auktoritet som bestämmer vad som är korrekt eller inkorrekt CSS?</a></li>
<li><a href="#free">Hur mycket kostar den?</a></li>
<li><a href="#who">Vem konstruerade detta verktyg? Vem underhåller det?</a></li>
<li><a href="#contact">Hur kontaktar jag författarna? Rapporterar en bugg?</a></li>
<li><a href="#participate">Kan jag hjälpa till?</a></li>
  </ol>
  </li>
  <li><a href="#dev">För utvecklare</a>
  <ol>
    <li><a href="#code">Vilket språk är CSS-valideraren skriven i? Finns källkoden tillgänglig?</a></li>
    <li><a href="#install">Kan jag göra en egen installation av CSS-valideraren och köra den?</a></li>
    <li><a href="#api">Kan jag bygga en tillämpning ovanpå denna validerare? Finns det ett API?</a></li>
  </ol>
  </li>
</ol>
</div><!-- toc -->
<h3 id="about">Om denna tjänst</h3>

<h4 id="what">Vad är detta? Behöver jag den?</h4>

<p>

W3C:s CSS-valideringstjänst är en fri programvara skapad av W3C, för
att hjälpa webbdesigners och webbutvecklare genom att granska
användning av formatmallar (<span xml:lang="en" lang="en">Cascading
Style Sheets</span>, CSS).  Valideringstjänsten kan användas på denna
<a href="./">gratis webbtjänst</a>, eller laddas ned och användas
antingen som ett fristående Javaprohram, eller som en Java-servet på
en webbserver.

</p><p>

Behöver <em>du</em> den? Om du är en webbutvecklare eller
webbdesigner, så kan detta verktyg vara en ovärderlig hjälp. Den
hjälper dig inte bara med att utvärdera dina formatmallar gentemot
CSS-specifikationerna, att hitta fel i dina formatmallar, hitta
skrivfel, eller upptäcka felaktig användning av CSS, den kan även
informera dig om när din CSS kan orsaka användarsvårigheter.

</p>

<h4 id="help">Förklaringen ovan är obegriplig! Hjälp!</h4>

<p>

De flesta dokument på webben uttrycks i ett datorspråk som kallas
HTML. Detta språk kan användas för att skapa sidor med strukturerad
information, länkar och multimediala objekt. HTML använder ett
formatteringsspråk som kallas CSS (akronym för "<span xml:lang="en"
lang="en">Cascading Style Sheets</span>") för att färgsätta, ordna
text och skapa layout. Detta verktyg hjälper till vid användning av
CSS och vid skapandet av CSS formatmallar.

</p>


<h4 id="reference">Så, detta är den auktoritet som bestämmer vad som är korrekt eller inkorrekt CSS?</h4>

<p>

Nej. Det är ett verktyg, hjälpande och tillförlitligt, men ändå ett
programvaruverktyg, och som all annan programvara så har den några
<a href="https://github.com/w3c/css-validator/issues">fel och brister</a> &amp; <a href="https://www.w3.org/Bugs/Public/buglist.cgi?product=CSSValidator">fel och brister</a>.

Den sanna måttstocken för CSS-formatmallar är 
<a href="https://www.w3.org/Style/CSS/#specs">CSS specifikationerna</a>.

</p>

<h4 id="free">Hur mycket kostar den?</h4>

<p>

Ingenting. Tjänsten är gratis. Källkoden är 
<a href="DOWNLOAD.html">öppen</a>, och du kan ladda ned den, använda den,
modifiera den, distribuera den och
<a href="https://www.w3.org/Consortium/Legal/copyright-software">annat</a>.

Om du skulle vilja, så kan du 
<a href="#participate">deltaga i detta projekt</a>
eller donera till W3C via 
 <a href="https://www.w3.org/Consortium/sup">W3C:s supperterprogram</a>.

</p>

<h4 id="who">Vem konstruerade detta verktyg? Vem underhåller det?</h4>

<p>

W3C underhåller och tillhandahåller detta verktyg, tack vare arbete
och bidrag från W3C:s stab, frivilliga utvecklare och översättar.

Mer information om detta hittas på
<a href="thanks.html">sidan med tack</a>.
<a href="#participate">Du kan också hjälpa till</a>.

</p>

<h4 id="participate">Kan jag hjälpa till?</h4>

<p>

Naturligtvis. Om du är Javaprogrammerare, så kan du hjälpa projektet
CSS-valideraren genom att granska
<a href="#code">koden</a>,
diagnosticera och rätta
<a href="https://github.com/w3c/css-validator/issues">fel</a> &amp; <a href="https://www.w3.org/Bugs/Public/buglist.cgi?product=CSSValidator">fel</a>,
eller att hjälpa till med att konstruera ny funktionalitet.

</p><p>

Men du behöver inte vara en kodare för att hjälpa till i arbetet med
att bygga och underhålla detta verktyg; du kan också hjälpa till med
att förbättra dokumentation, översätta valideraren till ditt språk,
eller prenumerera på 
<a href="https://lists.w3.org/Archives/Public/www-validator-css/">dess epostlista</a>
och diskutera verktyget och hjälpa andra användare.

</p>

<h4 id="contact">Andra typer av frågor?</h4>

<p>

Om du har frågor om CSS eller CSS-valideraren så kan du leta efter
information på
<a href="Email">epostlistor och diskussionsfora</a>.

Men innan du gör det, kontrollera om din fråga eller kommentar redan
är behandlats i
<a href="http://www.websitedev.de/css/validator-faq">CSS-validerarens <acronym title="Frequently Asked Questions">FAQ</acronym>-dokument</a>.

</p>

<h3 id="dev">För utvecklare</h3>

<h4 id="code">Vilket språk är CSS-valideraren skriven i? Finns källkoden tillgänglig?</h4>

<p>

W3C:s CSS-validerare är skriven i Java, och ja, dess källkod finns
tillgänglig via CVS.

Du kan
<a href="https://github.com/w3c/css-validator">inspektera koden online</a>
eller följa anvisningarna där för att ladda ned hela källkodsträdet.

En snabb översikt över klasserna i koden för CSS-valideraren finns på
<a href="README.html">README</a>-filen.

</p>

<h4 id="install">Kan jag göra en egen installation av CSS-valideraren och köra den?</h4>

<p>

Det går att ladda ned och installera CSS-valideraren, och köra den
antingen från kommandoraden eller som en servlet i en webbserver.

Läs
<a href="DOWNLOAD.html">anvisningarna</a>
om ur den installeras och används.

</p>

<h4 id="api">Kan jag bygga en tillämpning ovanpå denna validerare? Finns det ett API?</h4>

<p>

Ja, och <a href="api.html">yes</a>.

CSS-valideraren har ett (RESTFUL)
<a href="api.html">SOAP-gränssnitt</a>
som gör det ganska enkelt att bygga tillämpningar (webbtillämpningar
och annat) ovanpå den.

Gott beteende och förnuftigt användande av delade resurser är
naturligtvis att rekommendera; försäkra dig om att din tillämpning
sover ( <code>sleep()</code> ) mellan anrop till valideraren, eller
installera din egen instans av valideraren.

</p>


</div>
   <ul class="navbar"  id="menu">
	<li><strong><a href="./" title="Hemsida för W3C:s CSS-valideringstjänst">Hemsida</a></strong> <span class="hideme">|</span></li>
        <li><a href="documentation.html" title="Dokumentation om W3C:s CSS-valideringstjänst">Dokumentation</a> <span class="hideme">|</span></li>
        <li><a href="DOWNLOAD.html" title="Ladda ned CSS-valideraren">Ladda ned</a> <span class="hideme">|</span></li>
        <li><a href="Email.html" title="Hur man ger synpunkter på denna tjänst">Synpunkter</a> <span class="hideme">|</span></li>
        <li><a href="thanks.html" title="Tack och erkännanden">Tack</a><span class="hideme">|</span></li>

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
