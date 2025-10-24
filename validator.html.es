<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="es" lang="es" dir="ltr">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="Content-Script-Type" content="text/javascript" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <title>El Servicio de Validación de CSS del W3C</title>
    <link rev="made" href="mailto:www-validator-css@w3.org" />
    <link rev="start" href="./" title="Página de inicio del Servicio de Validación CSS del  W3C" />
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
      Verifica Hojas de Estilo en Cascada (CSS) y documentos (X)HTML con hojas de estilo
    </p>
   </div>
  <div id="frontforms">
      <ul id="tabset_tabs">
        <li><a href="#validate-by-uri" class="active">mediante URI</a></li><li><a href="#validate-by-upload">mediante Carga de Archivo</a></li><li><a href="#validate-by-input">mediante Entrada directa</a></li>
      </ul>
      <div id="fields">

      <fieldset id="validate-by-uri" class="tabset_content front">
        <legend>Validar mediante URI</legend>
        <form method="get" action="validator">
        <p class="instructions">
          Introduce la URI de un documento (HTML con CSS o sólo CSS) que desees validar:
        </p>
        <p>
           <label title="Dirección de la página a Validar" for="uri">Dirección:
             <input type="text" name="uri" id="uri" size="45" />
           </label>
        </p>
              <fieldset id="extra_opt_uri" class="moreoptions">
    <legend class="toggletext" title="Show/Hide extra validation options">Más opciones</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_uri">
      <label for="profile_uri">Perfil:</label>
    </th>
    <td headers="header_profile_uri">
      <select id="profile_uri" name="profile">
        <option value="none">Ninguno en especial</option>
        <option value="css1">CSS versión 1</option>
        <option value="css2">CSS versión 2</option>
        <option value="css21">CSS versión 2.1</option>
        <option value="css3">CSS versión 3</option>
        <option selected="selected" value="css3svg">CSS Level 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Básico</option>
        <option value="svgtiny">SVG Reducido</option>  
        <option value="mobile">Móvil</option>
        <option value="atsc-tv">Perfil de TV ATSC</option>
        <option value="tv">Perfil de TV</option>
      </select>
    </td>
    <th id="header_medium_uri">
      <label for="medium_uri">Medio:</label>
    </th>
    <td headers="header_medium_uri">
      <select id="medium_uri" name="usermedium">
        <option selected="selected" value="all">Todos</option>
        <option value="aural">auditivo</option>
        <option value="braille">braille</option>
        <option value="embossed">relieve</option>
        <option value="handheld">pequeños dispositivos</option>
        <option value="print">impresión</option>
        <option value= "projection">proyección</option>
        <option value="screen">pantalla</option>
        <option value="tty">teletipo</option>
        <option value="tv">televisión</option>
        <option value="presentation">presentación</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_warning_uri">
      <label for="warning_uri">Las Advertencias :</label>
    </th>
    <td
        colspan="1"
         headers="header_warning_uri">
      <select id="warning_uri" name="warning"> 
        <option value="2">Todos</option>
        <option selected="selected" value="1">Informe normal</option>
        <option value="0">Las más importantes</option>
        <option value="no">Sin advertencias</option>
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
  <input type="hidden" name="lang" value="es" />
    <label title="Enviar archivo para su validación">
      <input type="submit" value="Check" />
    </label>
  </p>
      </form>
      </fieldset>

      <fieldset id="validate-by-upload"  class="tabset_content front">
        <legend>Validar mediante Carga de un Archivo</legend>
      <form method="post" enctype="multipart/form-data" action="validator">
        <p class="instructions">Elige el documento que desees validar:</p>
        <p>
          <label title="Elige un Archivo Local para su Carga y Validación" for="file">Archivo CSS local:
          <input type="file" id="file" name="file" size="30" /></label></p>
                      <fieldset id="extra_opt_upload" class="moreoptions">
    <legend class="toggletext" title="Show/Hide extra validation options">Más opciones</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_upload">
      <label for="profile_upload">Perfil:</label>
    </th>
    <td headers="header_profile_upload">
      <select id="profile_upload" name="profile">
        <option value="none">Ninguno en especial</option>
        <option value="css1">CSS versión 1</option>
        <option value="css2">CSS versión 2</option>
        <option value="css21">CSS versión 2.1</option>
        <option value="css3">CSS versión 3</option>
        <option selected="selected" value="css3svg">CSS Level 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Básico</option>
        <option value="svgtiny">SVG Reducido</option>  
        <option value="mobile">Móvil</option>
        <option value="atsc-tv">Perfil de TV ATSC</option>
        <option value="tv">Perfil de TV</option>
      </select>
    </td>
    <th id="header_medium_upload">
      <label for="medium_upload">Medio:</label>
    </th>
    <td headers="header_medium_upload">
      <select id="medium_upload" name="usermedium">
        <option selected="selected" value="all">Todos</option>
        <option value="aural">auditivo</option>
        <option value="braille">braille</option>
        <option value="embossed">relieve</option>
        <option value="handheld">pequeños dispositivos</option>
        <option value="print">impresión</option>
        <option value= "projection">proyección</option>
        <option value="screen">pantalla</option>
        <option value="tty">teletipo</option>
        <option value="tv">televisión</option>
        <option value="presentation">presentación</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_warning_upload">
      <label for="warning_upload">Las Advertencias :</label>
    </th>
    <td
        colspan="1"
         headers="header_warning_upload">
      <select id="warning_upload" name="warning"> 
        <option value="2">Todos</option>
        <option selected="selected" value="1">Informe normal</option>
        <option value="0">Las más importantes</option>
        <option value="no">Sin advertencias</option>
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
  <input type="hidden" name="lang" value="es" />
    <label title="Enviar archivo para su validación">
      <input type="submit" value="Check" />
    </label>
  </p>
      </form>
      </fieldset>

      <fieldset id="validate-by-input"  class="tabset_content front">
        <legend>Validar mediante entrada directa</legend>
        <form action="validator" enctype="multipart/form-data" method="post">
        <p class="instructions">Introduce el código CSS que desees validar:</p>
        <p>
          <textarea name="text" rows="12" cols="70"></textarea>
        </p>      
              <fieldset id="extra_opt_input" class="moreoptions">
    <legend class="toggletext" title="Show/Hide extra validation options">Más opciones</legend>
    <div class="options">
    <table>
    <tr>
    <th id="header_profile_input">
      <label for="profile_input">Perfil:</label>
    </th>
    <td headers="header_profile_input">
      <select id="profile_input" name="profile">
        <option value="none">Ninguno en especial</option>
        <option value="css1">CSS versión 1</option>
        <option value="css2">CSS versión 2</option>
        <option value="css21">CSS versión 2.1</option>
        <option value="css3">CSS versión 3</option>
        <option selected="selected" value="css3svg">CSS Level 3 + SVG</option>
        <option value="svg">SVG</option>
        <option value="svgbasic">SVG Básico</option>
        <option value="svgtiny">SVG Reducido</option>  
        <option value="mobile">Móvil</option>
        <option value="atsc-tv">Perfil de TV ATSC</option>
        <option value="tv">Perfil de TV</option>
      </select>
    </td>
    <th id="header_medium_input">
      <label for="medium_input">Medio:</label>
    </th>
    <td headers="header_medium_input">
      <select id="medium_input" name="usermedium">
        <option selected="selected" value="all">Todos</option>
        <option value="aural">auditivo</option>
        <option value="braille">braille</option>
        <option value="embossed">relieve</option>
        <option value="handheld">pequeños dispositivos</option>
        <option value="print">impresión</option>
        <option value= "projection">proyección</option>
        <option value="screen">pantalla</option>
        <option value="tty">teletipo</option>
        <option value="tv">televisión</option>
        <option value="presentation">presentación</option>
      </select>
    </td>
    </tr>
    <tr>
        <th id="header_type_input">
      <label for="type_input">Tipo:</label>
    </th>
    <td headers="header_type_input">
      <select id="type_input" name="type">
        <option selected="selected" value="none">Automático</option>
        <option value="html">HTML</option>
        <option value="css">CSS</option>
      </select>
    </td>
        <th id="header_warning_input">
      <label for="warning_input">Las Advertencias :</label>
    </th>
    <td
         headers="header_warning_input">
      <select id="warning_input" name="warning"> 
        <option value="2">Todos</option>
        <option selected="selected" value="1">Informe normal</option>
        <option value="0">Las más importantes</option>
        <option value="no">Sin advertencias</option>
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
  <input type="hidden" name="lang" value="es" />
    <label title="Enviar archivo para su validación">
      <input type="submit" value="Check" />
    </label>
  </p>
      </form>
      </fieldset>
      </div><!-- fields -->
  </div> <!-- frontforms -->
  
  <div id="w3c-include"><script type="text/javascript" src="https://www.w3.org/QA/Tools/w3c-include.js"></script></div>

  <div class="intro">
  <p><strong>Nota</strong>: Si deseas validar tu hoja de estilo CSS incrustada en un documento (X)HTML, deberías antes <a href="https://validator.w3.org/">comprobar que el  (X)HTML utilizado es válido</a>.
  </p>
  </div>
  <ul class="navbar" id="menu">
    <li><a href="about.html" title="Acerca de este servicio">Acerca de este servicio</a> <span class="hideme">|</span></li>
    <li><a href="documentation.html" title="Documentación del Servicio de Validación CSS del W3C">Documentación</a> <span class="hideme">|</span></li>
    <li><a href="DOWNLOAD.html" title="Descargar y installar el validador CSS">Download</a> <span class="hideme">|</span></li>
    <li><a href="Email.html" title="Cómo realizar comentarios sobre este servicio">Comentarios</a> <span class="hideme">|</span></li>
    <li><a href="thanks.html" title="Créditos y Agradecimientos">Créditos</a></li>
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
     <a href="https://www.w3.org/Style/CSS/learning" title="Aprenda má sobre las Hojas de Estilo en Cascada"><img src="images/woolly-icon" alt="CSS" /></a>
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
