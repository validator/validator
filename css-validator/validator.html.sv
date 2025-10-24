<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="sv" lang="sv" dir="ltr">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Content-Script-Type" content="text/javascript" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <title>W3C:s CSS-valideringstjänst</title>
    <link rev="made" href="mailto:www-validator-css@w3.org" />
    <link rev="start" href="./" title="Hemsida för W3C:s CSS-valideringstjänst" />
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
      Granska Cascading Style Sheets (CSS) och (X)HTML-dokument med formatmallar
    </p>
   </div>
  <div id="frontforms">
      <ul id="tabset_tabs">
        <li><a href="#validate-by-uri" class="active">Genom URI</a></li><li><a href="#validate-by-upload">Genom filuppladdning</a></li><li><a href="#validate-by-input">Genom direktinmatning</a></li>
      </ul>
      <div id="fields">

      <fieldset id="validate-by-uri" class="tabset_content front">
        <legend>Validera genom URI</legend>
        <form method="get" action="validator">
        <p class="instructions">
          Mata in URI för det dokument (HTML med CSS eller enbart CSS) som du vill validera:
        </p>
        <p>
           <label title="Adress för den sida som skall valideras" for="uri">Adress:
             <input type="text" name="uri" id="uri" size="45" />
           </label>
        </p>
              <fieldset id="extra_opt_uri" class="moreoptions">
    <legend class="toggletext" title="Visa/Dölj andra valideringsval">Fler val</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_uri">
      <label for="profile_uri">Profil:</label>
    </th>
    <td headers="header_profile_uri">
      <select id="profile_uri" name="profile">
        <option value="none">Ingen speciell profil</option>
        <option value="css1">CSS nivå 1</option>
        <option value="css2">CSS nivå 2</option>
        <option value="css21">CSS nivå 2.1</option>
        <option value="css3">CSS nivå 3</option>
        <option selected="selected" value="css3svg">CSS nivå 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Basic</option>
        <option value="svgtiny">SVG Tiny</option>  
        <option value="mobile">Mobil</option>
        <option value="atsc-tv">ATSC TV-profil</option>
        <option value="tv">TV-profil</option>
      </select>
    </td>
    <th id="header_medium_uri">
      <label for="medium_uri">Medium:</label>
    </th>
    <td headers="header_medium_uri">
      <select id="medium_uri" name="usermedium">
        <option selected="selected" value="all">Alla</option>
        <option value="aural">ljud</option>
        <option value="braille">braille</option>
        <option value="embossed">präglad</option>
        <option value="handheld">handhållen</option>
        <option value="print">skriv ut</option>
        <option value= "projection">projicering</option>
        <option value="screen">skärm</option>
        <option value="tty">TTY</option>
        <option value="tv">TV</option>
        <option value="presentation">presentation</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_warning_uri">
      <label for="warning_uri">Varningar:</label>
    </th>
    <td
        colspan="1"
         headers="header_warning_uri">
      <select id="warning_uri" name="warning"> 
        <option value="2">Alla</option>
        <option selected="selected" value="1">Normal rapport</option>
        <option value="0">Mest viktigt</option>
        <option value="no">Inga varningar</option>
      </select>
    </td>

    <th id="header_vext_warning_uri">
      <label id="vext_warning_input">Leverantörsutvidgningar:</label>     
    </th>
    <td headers="header_vext_warning_uri">
      <select id="vext_warning_uri" name="vextwarning">
        <option value="">Standardvärde</option>
        <option value="true">Varningar</option>
        <option value="false">Fel</option>
      </select>
    </td>

    </tr>
    </table>
    </div><!-- item_contents -->
  </fieldset><!-- invisible -->
  
  <p class="submit_button">
  <input type="hidden" name="lang" value="sv" />
    <label title="Skicka in URI för validering">
      <input type="submit" value="Granska" />
    </label>
  </p>
      </form>
      </fieldset>

      <fieldset id="validate-by-upload"  class="tabset_content front">
        <legend>Validera genom att ladda upp fil</legend>
      <form method="post" enctype="multipart/form-data" action="validator">
        <p class="instructions">Välj det dokument som du vill validera:</p>
        <p>
          <label title="Välj en lokalt lagrad fil att ladda upp och validera" for="file">Lokal CSS-fil:
          <input type="file" id="file" name="file" size="30" /></label></p>
                      <fieldset id="extra_opt_upload" class="moreoptions">
    <legend class="toggletext" title="Visa/Dölj andra valideringsval">Fler val</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_upload">
      <label for="profile_upload">Profil:</label>
    </th>
    <td headers="header_profile_upload">
      <select id="profile_upload" name="profile">
        <option value="none">Ingen speciell profil</option>
        <option value="css1">CSS nivå 1</option>
        <option value="css2">CSS nivå 2</option>
        <option value="css21">CSS nivå 2.1</option>
        <option value="css3">CSS nivå 3</option>
        <option selected="selected" value="css3svg">CSS nivå 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Basic</option>
        <option value="svgtiny">SVG Tiny</option>  
        <option value="mobile">Mobil</option>
        <option value="atsc-tv">ATSC TV-profil</option>
        <option value="tv">TV-profil</option>
      </select>
    </td>
    <th id="header_medium_upload">
      <label for="medium_upload">Medium:</label>
    </th>
    <td headers="header_medium_upload">
      <select id="medium_upload" name="usermedium">
        <option selected="selected" value="all">Alla</option>
        <option value="aural">ljud</option>
        <option value="braille">braille</option>
        <option value="embossed">präglad</option>
        <option value="handheld">handhållen</option>
        <option value="print">skriv ut</option>
        <option value= "projection">projicering</option>
        <option value="screen">skärm</option>
        <option value="tty">TTY</option>
        <option value="tv">TV</option>
        <option value="presentation">presentation</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_warning_upload">
      <label for="warning_upload">Varningar:</label>
    </th>
    <td
        colspan="1"
         headers="header_warning_upload">
      <select id="warning_upload" name="warning"> 
        <option value="2">Alla</option>
        <option selected="selected" value="1">Normal rapport</option>
        <option value="0">Mest viktigt</option>
        <option value="no">Inga varningar</option>
      </select>
    </td>

    <th id="header_vext_warning_upload">
      <label id="vext_warning_input">Leverantörsutvidgningar:</label>     
    </th>
    <td headers="header_vext_warning_upload">
      <select id="vext_warning_upload" name="vextwarning">
        <option value="">Standardvärde</option>
        <option value="true">Varningar</option>
        <option value="false">Fel</option>
      </select>
    </td>

    </tr>
    </table>
    </div><!-- item_contents -->
  </fieldset><!-- invisible -->
  
  <p class="submit_button">
  <input type="hidden" name="lang" value="sv" />
    <label title="Skicka in fil för validering">
      <input type="submit" value="Granska" />
    </label>
  </p>
      </form>
      </fieldset>

      <fieldset id="validate-by-input"  class="tabset_content front">
        <legend>Validera genom direkt inmatning</legend>
        <form action="validator" enctype="multipart/form-data" method="post">
        <p class="instructions">Mata in den CSS du vill validera:</p>
        <p>
          <textarea name="text" rows="12" cols="70"></textarea>
        </p>      
              <fieldset id="extra_opt_input" class="moreoptions">
    <legend class="toggletext" title="Visa/Dölj andra valideringsval">Fler val</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_input">
      <label for="profile_input">Profil:</label>
    </th>
    <td headers="header_profile_input">
      <select id="profile_input" name="profile">
        <option value="none">Ingen speciell profil</option>
        <option value="css1">CSS nivå 1</option>
        <option value="css2">CSS nivå 2</option>
        <option value="css21">CSS nivå 2.1</option>
        <option value="css3">CSS nivå 3</option>
        <option selected="selected" value="css3svg">CSS nivå 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Basic</option>
        <option value="svgtiny">SVG Tiny</option>  
        <option value="mobile">Mobil</option>
        <option value="atsc-tv">ATSC TV-profil</option>
        <option value="tv">TV-profil</option>
      </select>
    </td>
    <th id="header_medium_input">
      <label for="medium_input">Medium:</label>
    </th>
    <td headers="header_medium_input">
      <select id="medium_input" name="usermedium">
        <option selected="selected" value="all">Alla</option>
        <option value="aural">ljud</option>
        <option value="braille">braille</option>
        <option value="embossed">präglad</option>
        <option value="handheld">handhållen</option>
        <option value="print">skriv ut</option>
        <option value= "projection">projicering</option>
        <option value="screen">skärm</option>
        <option value="tty">TTY</option>
        <option value="tv">TV</option>
        <option value="presentation">presentation</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_type_input">
      <label for="type_input">Typ:</label>
    </th>
    <td headers="header_type_input">
      <select id="type_input" name="type">
        <option selected="selected" value="none">Automatisk</option>
        <option value="html">HTML</option>
        <option value="css">CSS</option>
      </select>
    </td>
        <th id="header_warning_input">
      <label for="warning_input">Varningar:</label>
    </th>
    <td
         headers="header_warning_input">
      <select id="warning_input" name="warning"> 
        <option value="2">Alla</option>
        <option selected="selected" value="1">Normal rapport</option>
        <option value="0">Mest viktigt</option>
        <option value="no">Inga varningar</option>
      </select>
    </td>

    <th id="header_vext_warning_input">
      <label id="vext_warning_input">Leverantörsutvidgningar:</label>     
    </th>
    <td headers="header_vext_warning_input">
      <select id="vext_warning_input" name="vextwarning">
        <option value="">Standardvärde</option>
        <option value="true">Varningar</option>
        <option value="false">Fel</option>
      </select>
    </td>

    </tr>
    </table>
    </div><!-- item_contents -->
  </fieldset><!-- invisible -->
  
  <p class="submit_button">
  <input type="hidden" name="lang" value="sv" />
    <label title="Skicka in data för validering">
      <input type="submit" value="Granska" />
    </label>
  </p>
      </form>
      </fieldset>
      </div><!-- fields -->
  </div> <!-- frontforms -->
  
  <div id="w3c-include"><script type="text/javascript" src="https://www.w3.org/QA/Tools/w3c-include.js"></script></div>

  <div class="intro">
  <p><strong>Märk</strong>: Om du vill validera din CSS-formatmall inbäddad i ett (X)HTML-dokument, så bör du först <a href="https://validator.w3.org/">kontrollera att den (X)HTML du använder är giltig</a>.
  </p>
  </div>
  <ul class="navbar" id="menu">
    <li><a href="about.html" title="Om denna tjänst">Om</a> <span class="hideme">|</span></li>
    <li><a href="documentation.html" title="Dokumentation om W3C:s CSS-valideringstjänst">Dokumentation</a> <span class="hideme">|</span></li>
    <li><a href="DOWNLOAD.html" title="Ladda ner och installera CSS-valideraren">Ladda ner</a> <span class="hideme">|</span></li>
    <li><a href="Email.html" title="Hur man kan ge synpunkter på denna tjänst">Synpunkter</a> <span class="hideme">|</span></li>
    <li><a href="thanks.html" title="Hjälp och tack">Tack till</a></li>
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
     <a href="https://www.w3.org/Style/CSS/learning" title="Lär dig mer om Cascading Style Sheets"><img src="images/woolly-icon" alt="CSS" /></a>
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
