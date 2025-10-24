<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="es" lang="es">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Acerca del Servicio de Validaci&oacute;n de CSS del W3C</title>
<link rev="made" href="mailto:www-validator-css@w3.org"/>
<link rev="start" href="./" title="Home Page"/>
<style type="text/css" media="all">
	@import "style/base.css";
	@import "style/docs.css";
    </style>
<meta name="revision" content="$Id$"/>
</head>
<body>
  <div id="banner">
   <h1 id="title"><a href="https://www.w3.org/"><img alt="W3C" width="110" height="61" id="logo" src="./images/w3c.png" /></a>
   <a href="./"><span>Servicio de Validaci&oacute;n de CSS</span></a></h1>
   <p id="tagline">
     Check Cascading Style Sheets (CSS) and (X)HTML documents with style sheets
   </p>
  </div>
  
  
<div class="doc">
<h2>Acerca del Validador de CSS</h2>
<h3 id="TableOfContents">&Iacute;ndice</h3>
<div id="toc">
<ol>
<li>Acerca de este servicio
  <ol>
<li>
<a href="#what">&iquest;Qu&eacute; es esto? &iquest;Lo necesito?</a>
</li>
<li>
<a href="#help">&iexcl;La explicaci&oacute;n anterior es incomprensible! &iexcl;Ayuda!</a>
</li>
<li>
<a href="#reference">De modo que, &iquest;esto es lo que decide qu&eacute; es CSS correcto y qu&eacute; no lo es?</a>
</li>
<li>
<a href="#free">&iquest;Cu&aacute;nto cuesta?</a>
</li>
<li>
<a href="#who">&iquest;Qui&eacute;n desarrolló esta herramienta? &iquest;Qui&eacute;n la mantiene?</a>
</li>
<li>
<a href="#contact">&iquest;C&oacute;mo contacto con los autores? &iquest;Cómo informo de un error?</a>
</li>
<li>
<a href="#participate">&iquest;Puedo ayudar?</a>
</li>
</ol>
</li>
<li>El rinc&oacute;n de los desarrolladores
  <ol>
<li>
<a href="#code">&iquest;En qu&eacute; est&aacute; escrito el Validador CSS? &iquest;Se encuentra el código fuente disponible en alg&uacute;n sitio?</a>
</li>
<li>
<a href="#install">&iquest;Puedo instalar y ejecutar el Validador CSS por mi mismo?</a>
</li>
<li>
<a href="#api">&iquest;Puedo construir una aplicaci&oacute;n basada en este validador? &iquest;Existe una API?</a>
</li>
</ol>
</li>
</ol>
</div>
<!-- toc -->
<h3 id="about">Acerca de este servicio</h3>
<h4 id="what">&iquest;Qu&eacute; es esto? &iquest;Lo necesito?</h4>
<p>El servicio de Validaci&oacute;n de CSS del W3C es un software libre creado por el W3C para ayudar a los dise&ntilde;adores y desarrolladores web a validar Hojas de Estilo en Cascada (CSS). 
Puede utilizarse mediante este <a href="./">servicio gratu&iacute;to</a> en la web, o puede descargarse y ser usado bien como un programa java, o como un servlet java en un servidor Web.</p>
<p>¿Es necesario para <em>tí</em>? Si eres un desarrollador Web o un dise&ntilde;ador Web, esta herramienta ser&aacute; un aliado inestimable. No s&oacute;lo comparar&aacute; tus hojas de estilo con las especificaciones CSS, ayud&aacute;ndote a encontrar errores comunes, errores tipogr&aacute;ficos, o usos incorrectos de CSS, tambi&eacute;n te dir&aacute; cuando tu CSS presenta algun riesgo en cuanto a usabilidad.</p>
<h4 id="help">&iexcl;La explicaci&oacute;n anterior es incomprensible! &iexcl;Ayuda!</h4>
<p>La mayor parte de los documentos de la Web est&aacute;n escritos en un lenguaje inform&aacute;tico llamado HTML. Este lenguaje puede utilizarse para crear p&aacute;ginas con informaci&oacute;n estructurada, enlaces, y objetos multimedia. Para colores, texto y posicionamiento HTML utiliza un lenguaje de estilo llamado CSS, acr&oacute;nimo de "Cascading Style Sheets" (Hojas de Estilo en Cascada). 
Lo que hace esta herramienta es ayudar a las personas que escriben CSS a comprobar, y corregir si es necesario, sus Hojas de Estilo en Cascada.</p>
<h4 id="reference">De modo que, &iquest;&eacute;sto es lo que decide qu&eacute; es CSS correcto y qu&eacute; no lo es?</h4>
<p>No. Es una herramienta &uacute;til y confiable, pero es una herramienta de software, y como todo software, tiene algunos 
<a href="https://github.com/w3c/css-validator/issues">errores e incidencias</a> &amp; <a href="https://www.w3.org/Bugs/Public/buglist.cgi?product=CSSValidator">errores e incidencias</a>.
La verdadera referencia sobre Hojas de Estilo en Cascada son las <a href="https://www.w3.org/Style/CSS/#specs">Especificaciones de CSS</a>.</p>
<h4 id="free">&iquest;Cu&aacute;nto cuesta?</h4>
<p>Nada. El servicio es gratis. El código fuente es <a href="DOWNLOAD.html">abierto</a> y eres libre de descargarlo, usarlo, modificarlo, distribuirlo y <a href="https://www.w3.org/Consortium/Legal/copyright-software">m&aacute;s</a>.
Si realmente te gusta, eres bienvenido a <a href="#participate">unirte al proyecto</a> o hacer una donaci&oacute;n al W3C a través del
 <a href="https://www.w3.org/Consortium/sup">programa de apoyo del W3C</a>, pero nadie te obliga.</p>
<h4 id="who">&iquest;Qui&eacute;n desarrolló esta herramienta? &iquest;Qui&eacute;n la mantiene?</h4>
<p>El W3C mantiene y aloja la herramienta, gracias al trabajo y contribuciones del personal del W3C, desarrolladores y traductores voluntarios. Lee la <a href="thanks.html">p&aacute;gina de cr&eacute;ditos y reconocimientos</a> para m&aacute;s detalles. <a href="#participate">T&uacute; tambi&eacute;n puedes ayudar</a>.</p>
<h4 id="participate">&iquest;Puedo ayudar?</h4>
<p>Por supueso. Si eres un programador java, puedes ayudar al proyecto del Validador CSS revisando el <a href="#code">c&oacute;digo</a>,
adoptando y corrigiendo <a href="https://github.com/w3c/css-validator/issues">bugs</a> &amp; <a href="https://www.w3.org/Bugs/Public/buglist.cgi?product=CSSValidator">bugs</a>,
o ayudando a construir nuevas funciones.</p>
<p>Pero no necesitas ser un programador para ayudar a construir y mantener esta herramienta: tambi&eacute;n puedes ayudar a mejorar la documentaci&oacute;n, participar en la traducci&oacute;n del validador a tu lenguaje, o suscribirte a la 
<a href="https://lists.w3.org/Archives/Public/www-validator-css/">lista de correo</a> y discutir sobre la herramienta o ayudar a otros usuarios.</p>
<h4 id="contact">&iquest;Alguna otra pregunta?</h4>
<p>Si tienes alguna pregunta respecto a CSS o al validador de CSS, revisa los 
<a href="Email">foros y listas de correo</a> disponibles. Pero antes, aseg&uacute;rate de que tu pregunta o comentario no est&aacute; ya cubierto por el documento de <a href="http://www.websitedev.de/css/validator-faq">Preguntas frecuentes del Validador CSS</a>.</p>
<h3 id="dev">El rincon del desarrollador</h3>
<h4 id="code">&iquest;En qu&eacute; est&aacute; escrito el Validador CSS? &iquest;Se encuentra el código fuente disponible en alg&uacute;n sitio?</h4>
<p>El validador CSS del W3C est&aacute; escrito utilizando el lenguaje java, y s&iacute;, su código fuente se encuentra disponible, usando CVS: puedes <a href="https://github.com/w3c/css-validator">navegar el c&oacute;digo en l&iacute;nea</a> 
o seguir las instrucciones para descargar el &aacute;rbol completo del código fuente. Para una r&aacute;pida vista general de las clases usadas en el c&oacute;digo del Validador CSS, revisa el archivo 
<a href="README.html">README</a>.</p>
<h4 id="install">&iquest;Puedo instalar y ejecutar el Validador CSS por m&iacute; mismo?</h4>
<p>Es posible descargar e instalar el validador CSS, y ejecutarlo bien desde la l&iacute;nea de comandos, o como un servlet en un servidor Web. Lee las <a href="DOWNLOAD.html">instrucciones</a> para la instalaci&oacute;n y utilizaci&oacute;n.</p>
<h4 id="api">&iquest;Puedo construir una aplicaci&oacute;n basada en este validador? &iquest;Existe una API?</h4>
<p>S&iacute;, y <a href="api">s&iacute;</a>. El validador CSS tiene una (RESTful) <a href="api">interfaz SOAP</a> que deber&iacute;a hacer razonablemente f&aacute;cil construir aplicaciones (Web o de otro tipo) basadas en &eacute;l. Las buenas maneras y el uso respetuoso de recursos compartidos son, por supuesto, convencionales: aseg&uacute;rate que tus aplicaciones realizan pausas (sleep) entre llamadas al validador, o instala y ejecuta tu propio validador.</p>
</div>
<ul class="navbar" id="menu">
<li>
<strong>
<a href="./" title="P&aacute;gina de inicio del Servicio de Validaci&oacute;n CSS del W3C">Inicio</a>
</strong>
<span class="hideme">|</span>
</li>
<li>
<a href="documentation.html" title="Documentaci&oacute;n del Servicio de Validaci&oacute;n CSS del W3C">Documentaci&oacute;n</a>
<span class="hideme">|</span>
</li>
<li>
<a href="DOWNLOAD.html" title="Descarga el Validador CSS">Descarga</a>
<span class="hideme">|</span>
</li>
<li>
<a href="Email.html" title="C&oacute;mo realizar comentarios sobre este servicio">Comentarios</a>
<span class="hideme">|</span>
</li>
<li>
<a href="thanks.html" title="Cr&eacute;ditos y Agradecimientos">Cr&eacute;ditos</a>
<span class="hideme">|</span>
</li>
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
<a href="https://www.w3.org/Style/CSS/learning" title="Aprende m&aacute;s sobre Hojas de Estilo en Cascada">
<img src="images/woolly-icon" alt="CSS"/>
</a>
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
