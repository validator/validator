<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="es" lang="es">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Manual de Usuario del Validador de CSS</title>
  <link rev="made" href="mailto:www-validator-css@w3.org" />
  <link rev="start" href="./" title="Home Page" />
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
   <a href="./"><span>Servicio de Validaci&oacute;n de CSS</span></a></h1>
   <p id="tagline">
     Check Cascading Style Sheets (CSS) and (X)HTML documents with style sheets
   </p>
  </div>

<div id="main">
<!-- This DIV encapsulates everything in this page - necessary for the positioning -->

<div class="doc">
<h2>Manual de Usuario del Validador de CSS</h2>

<h3 id="TableOfContents">Tabla de Contenidos</h3>

<div id="toc">
<ul>
  <li><a href="#use">Cómo utilizar el Validador de CSS</a>
    <ul>
      <li><a href="#url">Validación mediante respuesta a URL</a></li>
      <li><a href="#fileupload">Validación mediante carga de archivo</a></li>
      <li><a href="#directinput">Validación mediante entrada directa</a></li>
      <li><a href="#basicvalidation">¿Qué hace la validación básica?</a></li>
    </ul>
  </li>
  <li><a href="#advanced">Validación avanzada</a>
    <ul>
	<li><a href="#paramwarnings">Parámetro warnings</a></li>
	<li><a href="#paramprofile">Parámetro profile</a></li>
	<li><a href="#parammedium">Parámetro medium</a></li>
    </ul>
  </li>
  <li><a href="#expert">Para expertos</a>
    <ul>
	<li><a href="#requestformat">Formato de Petición de Validación</a></li>
		<li><a href="#api">API de Servicio Web del Validador de CSS</a></li>
    </ul>
   </li>
</ul>
</div>

<p id="skip"></p>

<h3 id="use">Cómo utilizar el Validador de CSS</h3>

<p>
La manera más sencilla de comprobar un documento es utilizar la interfaz básica. En esta página 
 encontrarás tres formularios que corresponden a tres posibilidades:
</p>

<h4 id="url">Validación mediante URL</h4>
<p>
    Simplemente introduce la URL del documento que desees validar.
    Este documento puede ser HTML o CSS.
</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/uri_basic_es.png" alt="Validación mediante el formulario URI" />

<h4 id="fileupload">Validación mediante carga de archivo</h4>    
<p>
    Esta solución permite cargar y comprobar un archivo local. Haz clic en el 
    botón "Examinar..." y selecciona el archivo que deseas validar.
</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/file_upload_basic_es.png" 
    alt="Validación mediante Carga de Archivo" />
<p>
    En este caso, sólo se permiten documentos CSS. Esto significa que no puedes
    cargar documentos (X)HTML. También debes ser cuidadoso con las reglas 
    @import pues sólo se seguirán si referencian explícitamente a 
    una URL pública (olvida las rutas relativas con esta solución)
</p>
   
<h4 id="directinput">Validación mediante entrada directa</h4>
<p>
    Este modo es perfecto para probar fragmentos de CSS. Sólo tienes que escribir tu CSS en el área de texto
</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/direct_input_basic_es.png" 
    alt="Validación mediante entrada directa" />
<p>
    Los mismos comentarios de antes son de aplicación. Ten en cuenta que esta solución es 
    muy conveniente si tienes un problema y necesitas ayuda de la comunidad.
    También es muy útil para informar de un error del validador, ya que puedes enlazar a la URL 
    resultante para ofrecer un caso de prueba.
</p>    

<h4 id="basicvalidation">¿Qué hace la validación básica?</h4>

<p>    
    Cuando se utiliza la interfaz básica, el validador comprobará el cumplimiento 
    de <a href="https://www.w3.org/TR/CSS21">CSS 2.1</a>, que es la recomendación técnica
    actual de CSS.<br />
    Esto producirá una salida XHTML sin ninguna advertencia (únicamente verás los errores).<br />
    El parámetro medium está fijado en "all", que es el medio adecuado para todos los dispositivos 
    (ver <a href="https://www.w3.org/TR/CSS2/media.html">
    http://www.w3.org/TR/CSS2/media.html</a> para una descripción completa de los medios).
</p>

<h3 id="advanced">Validación avanzada</h3>

<p>
    Si necesitas una comprobación más específica, puedes utilizar la interfaz avanzada que 
    permite especificar tres parámetros. A continuación tienes una breve ayuda sobre cada uno
    de estos parámetros.
</p>

<h4 id="paramwarnings">Warnings</h4>

<p>
    Este parámetro es para ajustar el nivel de detalle del Validador de CSS. El validador puede 
    ofrecerte dos tipos de mensajes: errores (errors) y advertencias (warnings).
    Los errores ocurren cuando el CSS comprobado no respeta la recomendación CSS.
    Las advertencias se diferencian de los errores en que no suponen un problema referente a la 
    especificación. Se ofrecen para advertir (!) al desarrollador CSS sobre los 
    puntos que podrían ser peligrosos y conducir a un comportamiento extraño en algunos agentes de usuario.
</p>
<p>
    Una advertencia típica referente a font-family: si no ofreces un tipo de letra genérico, 
    obtendrás un aviso diciendo que deberías añadir uno al final de la regla, 
    de otro modo un agente de usuario que no conozca ninguna de los otros tipos 
    activará su tipo predeterminado, lo que puede dar lugar a una visualización extraña
</p>

<h4 id="paramprofile">Profile</h4>

<p>
    El validador CSS puede comprobar diferentes perfiles de CSS. Un perfil engloba todas las  
    características que se esperan de una implementación en una plataforma determinada. 
    Esta definición está tomada del  
    <a href="https://www.w3.org/Style/2004/css-charter-long.html#modules-and-profiles0">
	sitio de CSS
    </a>. La opción predeterminada corresponde al más utilizado en la actualidad: 
    <a href="https://www.w3.org/TR/CSS2">CSS 2</a>.
</p>

<h4 id="parammedium">Medium</h4>

<p>
    El parámetro medium es el equivalente a la regla @media, aplicada a todo el documento. Encontrarás más información sobre medios en  
    <a href="https://www.w3.org/TR/CSS2/media.html">
	http://www.w3.org/TR/CSS2/media.html
    </a>.
</p>

<h3 id="expert">Sólo para Expertos</h3>

<h4 id="requestformat">Formato de Petición de Validación</h4>
<p>A continuación se ofrece una tabla con los parámetros que pueden usarse para enviar una consulta al Validador de CSS del W3C.</p>

<p>Si deseas utilizar el servidor de validación público del W3C, utiliza los siguientes parámetros junto con la siguiente URI base:<br />
<kbd>http://jigsaw.w3.org/css-validator/validator</kbd><br />
(sustituir por la dirección de tu propio servidor si deseas llamar a una instancia privada del validador).</p>

<p><strong>Nota</strong>: Si deseas llamar al validador de forma programada para un conjunto de documentos, por favor asegúrate de que tu script duerma (<code>sleep</code>) durante <strong>al menos 1 segundo</strong> entre peticiones.
El servicio de validación de CSS es un servicio gratuito y público para todos, tu respeto es apreciado. Gracias.</p>

<table class="refdoc">
  <tbody>
    <tr>
      <th>Parámetro</th>
      <th>Descripción</th>
      <th>Valor por defecto</th>
    </tr>
    <tr>
      <th>uri</th>
      <td>El <acronym title="Universal Resource Locator">URL</acronym> del 
        documento a validar. Se permiten documentos CSS y HTML.</td>
      <td>Ninguno, pero debe proporcionarse un valor para este parámetro o bien el valor <code>text</code>.</td>
    </tr>
    <tr>
      <th>text</th>
      <td>El documento a validar, sólo se permite CSS.</td>
      <td>Ninguno, pero debe proporcionarse un valor para este parámetro o bien el valor <code>uri</code>.</td>
    </tr>
    <tr>
      <th>usermedium</th>
      <td>Se utiliza <a href="https://www.w3.org/TR/CSS2/media.html">medium</a> para la  
	  validación de medios como <code>screen</code>, <code>print</code>, <code>braille</code>...</td>
      <td><code>all</code></td>
    </tr>
    <tr>
      <th>output</th>
      <td>Activa los diferentes formatos de salida del validador. Los formatos posibles son 
	<code>text/html</code> y <code>html</code> (documento XHTML, 
	Content-Type: text/html), 
	<code>application/xhtml+xml</code> y <code>xhtml</code> (documento XHTML, Content-Type: application/xhtml+xml), 
	<code>application/soap+xml</code> y <code>soap12</code> (documento SOAP 1.2, Content-Type: application/soap+xml), 
	<code>text/plain</code> y <code>text</code> (documento de texto, 
	Content-Type: text/plain),
	cualquier otro (documento XHTML, Content-Type: text/plain)	
      </td>
      <td>html</td>
    </tr>
    <tr>
      <th>profile</th>
      <td>El perfil de CSS usado para la validación. Puede ser
        <code>css1</code>, <code>css2</code>, <code>css21</code>,
        <code>css3</code>, <code>svg</code>, <code>svgbasic</code>,
        <code>svgtiny</code>, <code>mobile</code>, <code>atsc-tv</code>,
        <code>tv</code> o <code>none</code></td>
      <td>la Recomendación más reciente del W3C: CSS 2</td>
    </tr>
    <tr>
      <th>lang</th>
      <td>El lenguaje utilizado para la respuesta, actualmente <code>en</code>,
        <code>fr</code>, <code>it</code>, <code>ko</code>, <code>ja</code>, <code>es</code>,
        <code>zh-cn</code>, <code>nl</code>, <code>de</code>.</td>
      <td>Inglés (<code>en</code>).</td>
    </tr>
    <tr>
      <th>warning</th>
      <td>El nivel de advertencias, <code>no</code> para no mostrar ninguna, <code>0</code> 
	para un nivel bajo, <code>1</code>o <code>2</code> para un nivel alto
      </td>
      <td>2</td>
    </tr>
  </tbody>
</table>

<h4 id="api">API de Servicio Web del Validador de CSS: documentación de la interfaz de validación</h4>
<p>    
    Para obtener más ayuda técnica, en particular acerca de la salida SOAP 1.2 y todas las formas posibles de llamar al validador, ver la  
    <a href="./api.html">API de Servicio Web del Validador de CSS</a>.       
</p>

</div>
</div>
<!-- End of "main" DIV. -->

   <ul class="navbar"  id="menu">
	<li><strong><a href="./" title="P&aacute;gina de inicio del Servicio de Validaci&oacute;n de CSS del W3C">Inicio</a></strong> <span class="hideme">|</span></li>
	<li><a href="about.html" title="Acerca de este servicio">Acerca de este servicio</a> <span class="hideme">|</span></li>
        <li><a href="documentation.html" title="Documentaci&oacute;n del Servicio de Validaci&oacute;n de CSS del W3C">Documentaci&oacute;n</a> <span class="hideme">|</span></li>
        <li><a href="DOWNLOAD.html" title="Descarga el validador CSS">Descarga</a> <span class="hideme">|</span></li>
        <li><a href="Email.html" title="C&oacute;mo realizar comentarios sobre este servicio">Comentarios</a> <span class="hideme">|</span></li>
        <li><a href="thanks.html" title="Cr&eacute;ditos y Agradecimientos">Cr&eacute;ditos</a><span class="hideme">|</span></li>
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

      <a href="https://www.w3.org/Style/CSS/learning" title="Aprende m&aacute;s sobre Hojas de Estilo en Cascada"><img src="images/woolly-icon" alt="CSS" /></a>
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



