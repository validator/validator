<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nl" lang="nl" dir="ltr">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Content-Script-Type" content="text/javascript" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <title>De W3C CSS Validatie Service</title>
    <link rev="made" href="mailto:www-validator-css@w3.org" />
    <link rev="start" href="./" title="Home pagina van de W3C CSS Validatie Service" />
    <style type="text/css" media="all">
      @import "style/base.css";
	      </style>   
      <script type="text/javascript" src="scripts/mootools.js"></script>
    <script type="text/javascript" src="scripts/w3c-validator.js"></script>
    </head>

  <body>
   <div id="banner">
    <h1 id="title"><a href="https://www.w3.org/"><img alt="W3C" width="110" height="61" id="logo" src="./images/w3c.png" /></a>
    <a href="./"><span>CSS Validation Service</span></a></h1>
    <p id="tagline">
      Controleer Cascading Style Sheets (CSS) en (X)HTML documenten die gebruik maken van style sheets
    </p>
   </div>
  <div id="frontforms">
      <ul id="tabset_tabs">
        <li><a href="#validate-by-uri" class="active">via een URI</a></li><li><a href="#validate-by-upload">via het uploaden van een bestand</a></li><li><a href="#validate-by-input">via directe invoer</a></li>
      </ul>
      <div id="fields">

      <fieldset id="validate-by-uri" class="tabset_content front">
        <legend>Valideer via een URI</legend>
        <form method="get" action="validator">
        <p class="instructions">
          Geef de URI op van een document (HTML met CSS of alleen CSS) dat je wilt valideren:
        </p>
        <p>
           <label title="Adres van de pagina om de valideren" for="uri">Adres:
             <input type="text" name="uri" id="uri" size="45" />
           </label>
        </p>
              <fieldset id="extra_opt_uri" class="moreoptions">
    <legend class="toggletext" title="Weergeven/Verbergen van extra validatie opties">Meer Opties</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_uri">
      <label for="profile_uri">Profiel:</label>
    </th>
    <td headers="header_profile_uri">
      <select id="profile_uri" name="profile">
        <option value="none">Geen speciaal profiel</option>
        <option value="css1">CSS versie 1</option>
        <option value="css2">CSS versie 2</option>
        <option value="css21">CSS versie 2.1</option>
        <option value="css3">CSS versie 3</option>
        <option selected="selected" value="css3svg">CSS Level 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Basic</option>
        <option value="svgtiny">SVG tiny</option>  
        <option value="mobile">Mobile</option>
        <option value="atsc-tv">ATSC TV profiel</option>
        <option value="tv">TV profiel</option>
      </select>
    </td>
    <th id="header_medium_uri">
      <label for="medium_uri">Medium:</label>
    </th>
    <td headers="header_medium_uri">
      <select id="medium_uri" name="usermedium">
        <option selected="selected" value="all">Alle</option>
        <option value="aural">aural</option>
        <option value="braille">braille</option>
        <option value="embossed">embossed</option>
        <option value="handheld">handheld</option>
        <option value="print">print</option>
        <option value= "projection">projection</option>
        <option value="screen">screen</option>
        <option value="tty">TTY</option>
        <option value="tv">TV</option>
        <option value="presentation">presentation</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_warning_uri">
      <label for="warning_uri">Waarschuwingen:</label>
    </th>
    <td
        colspan="1"
         headers="header_warning_uri">
      <select id="warning_uri" name="warning"> 
        <option value="2">Alle</option>
        <option selected="selected" value="1">Normaal</option>
        <option value="0">Meest belangrijk</option>
        <option value="no">Geen</option>
      </select>
    </td>

    <th id="header_vext_warning_uri">
      <label id="vext_warning_input">Vendor Extensions:</label>     
    </th>
    <td headers="header_vext_warning_uri">
      <select id="vext_warning_uri" name="vextwarning">
        <option value="">Default</option>
        <option value="true">Warnings</option>
        <option value="false">Errors</option>
      </select>
    </td>

    </tr>
    </table>
    </div><!-- item_contents -->
  </fieldset><!-- invisible -->
  
  <p class="submit_button">
  <input type="hidden" name="lang" value="nl" />
    <label title="Verzenden om te valideren">
      <input type="submit" value="Controleer" />
    </label>
  </p>
      </form>
      </fieldset>

      <fieldset id="validate-by-upload"  class="tabset_content front">
        <legend>Valideer via het uploaden van een bestand</legend>
      <form method="post" enctype="multipart/form-data" action="validator">
        <p class="instructions">Kies het document dat je wilt valideren:</p>
        <p>
          <label title="Kies een lokaal bestand dat je wilt uploaden en valideren" for="file">Lokaal CSS bestand:
          <input type="file" id="file" name="file" size="30" /></label></p>
                      <fieldset id="extra_opt_upload" class="moreoptions">
    <legend class="toggletext" title="Weergeven/Verbergen van extra validatie opties">Meer Opties</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_upload">
      <label for="profile_upload">Profiel:</label>
    </th>
    <td headers="header_profile_upload">
      <select id="profile_upload" name="profile">
        <option value="none">Geen speciaal profiel</option>
        <option value="css1">CSS versie 1</option>
        <option value="css2">CSS versie 2</option>
        <option value="css21">CSS versie 2.1</option>
        <option value="css3">CSS versie 3</option>
        <option selected="selected" value="css3svg">CSS Level 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Basic</option>
        <option value="svgtiny">SVG tiny</option>  
        <option value="mobile">Mobile</option>
        <option value="atsc-tv">ATSC TV profiel</option>
        <option value="tv">TV profiel</option>
      </select>
    </td>
    <th id="header_medium_upload">
      <label for="medium_upload">Medium:</label>
    </th>
    <td headers="header_medium_upload">
      <select id="medium_upload" name="usermedium">
        <option selected="selected" value="all">Alle</option>
        <option value="aural">aural</option>
        <option value="braille">braille</option>
        <option value="embossed">embossed</option>
        <option value="handheld">handheld</option>
        <option value="print">print</option>
        <option value= "projection">projection</option>
        <option value="screen">screen</option>
        <option value="tty">TTY</option>
        <option value="tv">TV</option>
        <option value="presentation">presentation</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_warning_upload">
      <label for="warning_upload">Waarschuwingen:</label>
    </th>
    <td
        colspan="1"
         headers="header_warning_upload">
      <select id="warning_upload" name="warning"> 
        <option value="2">Alle</option>
        <option selected="selected" value="1">Normaal</option>
        <option value="0">Meest belangrijk</option>
        <option value="no">Geen</option>
      </select>
    </td>

    <th id="header_vext_warning_upload">
      <label id="vext_warning_input">Vendor Extensions:</label>     
    </th>
    <td headers="header_vext_warning_upload">
      <select id="vext_warning_upload" name="vextwarning">
        <option value="">Default</option>
        <option value="true">Warnings</option>
        <option value="false">Errors</option>
      </select>
    </td>

    </tr>
    </table>
    </div><!-- item_contents -->
  </fieldset><!-- invisible -->
  
  <p class="submit_button">
  <input type="hidden" name="lang" value="nl" />
    <label title="Verzenden om te valideren">
      <input type="submit" value="Controleer" />
    </label>
  </p>
      </form>
      </fieldset>

      <fieldset id="validate-by-input"  class="tabset_content front">
        <legend>Valideer via directe invoer</legend>
        <form action="validator" enctype="multipart/form-data" method="post">
        <p class="instructions">Voer de CSS in die je wilt valideren:</p>
        <p>
          <textarea name="text" rows="12" cols="70"></textarea>
        </p>      
              <fieldset id="extra_opt_input" class="moreoptions">
    <legend class="toggletext" title="Weergeven/Verbergen van extra validatie opties">Meer Opties</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_input">
      <label for="profile_input">Profiel:</label>
    </th>
    <td headers="header_profile_input">
      <select id="profile_input" name="profile">
        <option value="none">Geen speciaal profiel</option>
        <option value="css1">CSS versie 1</option>
        <option value="css2">CSS versie 2</option>
        <option value="css21">CSS versie 2.1</option>
        <option value="css3">CSS versie 3</option>
        <option selected="selected" value="css3svg">CSS Level 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Basic</option>
        <option value="svgtiny">SVG tiny</option>  
        <option value="mobile">Mobile</option>
        <option value="atsc-tv">ATSC TV profiel</option>
        <option value="tv">TV profiel</option>
      </select>
    </td>
    <th id="header_medium_input">
      <label for="medium_input">Medium:</label>
    </th>
    <td headers="header_medium_input">
      <select id="medium_input" name="usermedium">
        <option selected="selected" value="all">Alle</option>
        <option value="aural">aural</option>
        <option value="braille">braille</option>
        <option value="embossed">embossed</option>
        <option value="handheld">handheld</option>
        <option value="print">print</option>
        <option value= "projection">projection</option>
        <option value="screen">screen</option>
        <option value="tty">TTY</option>
        <option value="tv">TV</option>
        <option value="presentation">presentation</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_type_input">
      <label for="type_input">Type:</label>
    </th>
    <td headers="header_type_input">
      <select id="type_input" name="type">
        <option selected="selected" value="none">Automatisch</option>
        <option value="html">HTML</option>
        <option value="css">CSS</option>
      </select>
    </td>
        <th id="header_warning_input">
      <label for="warning_input">Waarschuwingen:</label>
    </th>
    <td
         headers="header_warning_input">
      <select id="warning_input" name="warning"> 
        <option value="2">Alle</option>
        <option selected="selected" value="1">Normaal</option>
        <option value="0">Meest belangrijk</option>
        <option value="no">Geen</option>
      </select>
    </td>

    <th id="header_vext_warning_input">
      <label id="vext_warning_input">Vendor Extensions:</label>     
    </th>
    <td headers="header_vext_warning_input">
      <select id="vext_warning_input" name="vextwarning">
        <option value="">Default</option>
        <option value="true">Warnings</option>
        <option value="false">Errors</option>
      </select>
    </td>

    </tr>
    </table>
    </div><!-- item_contents -->
  </fieldset><!-- invisible -->
  
  <p class="submit_button">
  <input type="hidden" name="lang" value="nl" />
    <label title="Verzenden om te valideren">
      <input type="submit" value="Controleer" />
    </label>
  </p>
      </form>
      </fieldset>
      </div><!-- fields -->
  </div> <!-- frontforms -->
  
  <div id="w3c-include"><script type="text/javascript" src="https://www.w3.org/QA/Tools/w3c-include.js"></script></div>

  <div class="intro">
  <p><strong>Opmerking</strong>: Als je een CSS style sheet wilt valideren die gebruikt wordt in een (X)HTML document, zou je die eerst moeten <a href="https://validator.w3.org/">laten controleren</a>.
  </p>
  </div>
  <ul class="navbar" id="menu">
    <li><a href="about.html" title="Over deze service">Over</a> <span class="hideme">|</span></li>
    <li><a href="documentation.html" title="Documentatie voor de W3C CSS Validatie Service">Documentatie</a> <span class="hideme">|</span></li>
    <li><a href="DOWNLOAD.html" title="Download de CSS validator">Download</a> <span class="hideme">|</span></li>
    <li><a href="Email.html" title="Hoe reacties te geven over deze service">Reacties</a> <span class="hideme">|</span></li>
    <li><a href="thanks.html" title="Credits en Erkenning">Credits</a></li>
  </ul>

   <ul id="lang_choice">
        
        <li><a href="validator.html.de"
            lang="de"
            xml:lang="de"
            hreflang="de"
            rel="alternate">Deutsch</a>
        </li>
        
        <li><a href="validator.html.en"
            lang="en"
            xml:lang="en"
            hreflang="en"
            rel="alternate">English</a>
        </li>
        
        <li><a href="validator.html.es"
            lang="es"
            xml:lang="es"
            hreflang="es"
            rel="alternate">Español</a>
        </li>
        
        <li><a href="validator.html.fr"
            lang="fr"
            xml:lang="fr"
            hreflang="fr"
            rel="alternate">Français</a>
        </li>
        
        <li><a href="validator.html.ko"
            lang="ko"
            xml:lang="ko"
            hreflang="ko"
            rel="alternate">한국어</a>
        </li>
        
        <li><a href="validator.html.it"
            lang="it"
            xml:lang="it"
            hreflang="it"
            rel="alternate">Italiano</a>
        </li>
        
        <li><a href="validator.html.nl"
            lang="nl"
            xml:lang="nl"
            hreflang="nl"
            rel="alternate">Nederlands</a>
        </li>
        
        <li><a href="validator.html.ja"
            lang="ja"
            xml:lang="ja"
            hreflang="ja"
            rel="alternate">日本語</a>
        </li>
        
        <li><a href="validator.html.pl-PL"
            lang="pl-PL"
            xml:lang="pl-PL"
            hreflang="pl-PL"
            rel="alternate">Polski</a>
        </li>
        
        <li><a href="validator.html.pt-BR"
            lang="pt-BR"
            xml:lang="pt-BR"
            hreflang="pt-BR"
            rel="alternate">Português</a>
        </li>
        
        <li><a href="validator.html.ru"
            lang="ru"
            xml:lang="ru"
            hreflang="ru"
            rel="alternate">Русский</a>
        </li>
        
        <li><a href="validator.html.fa"
            lang="fa"
            xml:lang="fa"
            hreflang="fa"
            rel="alternate">فارسی</a>
        </li>
        
        <li><a href="validator.html.sv"
            lang="sv"
            xml:lang="sv"
            hreflang="sv"
            rel="alternate">Svenska</a>
        </li>
        
        <li><a href="validator.html.bg"
            lang="bg"
            xml:lang="bg"
            hreflang="bg"
            rel="alternate">Български</a>
        </li>
        
        <li><a href="validator.html.uk"
            lang="uk"
            xml:lang="uk"
            hreflang="uk"
            rel="alternate">Українська</a>
        </li>
        
        <li><a href="validator.html.cs"
            lang="cs"
            xml:lang="cs"
            hreflang="cs"
            rel="alternate">Čeština</a>
        </li>
        
        <li><a href="validator.html.ro"
            lang="ro"
            xml:lang="ro"
            hreflang="ro"
            rel="alternate">Romanian</a>
        </li>
        
        <li><a href="validator.html.hu"
            lang="hu"
            xml:lang="hu"
            hreflang="hu"
            rel="alternate">Magyar</a>
        </li>
        
        <li><a href="validator.html.el"
            lang="el"
            xml:lang="el"
            hreflang="el"
            rel="alternate">Ελληνικά</a>
        </li>
        
        <li><a href="validator.html.hi"
            lang="hi"
            xml:lang="hi"
            hreflang="hi"
            rel="alternate">हिन्दी</a>
        </li>
        
        <li><a href="validator.html.zh-cn"
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
