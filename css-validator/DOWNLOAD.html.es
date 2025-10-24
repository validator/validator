<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="es" lang="es">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Descarga e Instalaci&oacute;n del Validador de CSS</title>
<link rev="made" href="mailto:www-validator-css@w3.org"/>
<link rev="start" href="./" title="Home Page"/>
<style type="text/css" media="all">
    @import "style/base.css";  
  	@import "style/docs.css";
  </style>
<meta name="revision" content="$Id$"/>
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

<div class="doc">
<h2>Descarga e instalaci&oacute;n del Validador de CSS</h2>

<p>This translation of the installation guide for the CSS validator may be out of date. For a reliable, up-to-date guide, refer to the <a href="DOWNLOAD.html.en">English</a> or <a href="DOWNLOAD.html.fr">French</a> versions.</p>

<h3 id="download">Descarga el Validador de CSS</h3>
<h4 id="source">Descarga la fuente</h4>
<p>
      El <a href="https://github.com/w3c/css-validator">validador de CSS</a> est&aacute; disponible para su descarga mediante CVS.
      Sigue las <a href="https://dev.w3.org/cvsweb/">instrucciones</a> para acceder
      al servidor p&uacute;blico de CVS del W3C y descarga 2002/css-validator. Ten en cuenta  
      que la versi&oacute;n en l&iacute;nea del Validador de CSS es, generalmente, m&aacute;s antigua que la 
      versi&oacute;n CVS, de modo que los resultados y la apariencia pueden variar ligeramente...
    </p>
<h4>Descarga como un paquete java (jar o war)</h4>
<!--<p>A determinar... s&oacute;lo necesitamos una ubicaci&oacute;n estable para poner los archivos jar/war con regularidad</p>-->
<p><a href="https://github.com/w3c/css-validator/releases/latest/download/css-validator.jar">css-validator.jar</a></p>


<h3>Gu&iacute;a de instalaci&oacute;n</h3>
<p>El servicio de validaci&oacute;n de CSS es un software servlet, escrito en Java. Puede instalarse en cualquier motor servlet,
y tambi&eacute;n se puede utilizar como una sencilla herramienta en l&iacute;nea de comandos.
El servicio oficial  de Validaci&oacute;n CSS del W3C funciona con el servidor Jigsaw, que es la configuraci&oacute;n recomendada.
Sin embargo, por simplicidad, en este documento proporcionaremos principalmente detalles sobre c&oacute;mo instalarlo como un servlet de servicio en línea con Tomcat, el motor servlet de Apache.
</p>
<p>Tambi&eacute;n se ofrecen, a continuaci&oacute;n, las instrucciones para la instalaci&oacute;n del servlet con Jigsaw, as&iacute; como para la ejecuci&oacute;n en un entorno de l&iacute;nea de comandos.</p>
<h4 id="prereq">Prerrequisitos</h4>
<p>Esta gu&iacute;a de instalaci&oacute;n asume que has descargado, instalado y probado: </p>
<ul class="instructions">
<li>Un entorno java en funcionamiento,</li>
<li>La herramienta de desarrollo java <a href="https://ant.apache.org/">Ant</a>
</li>
<li>Un contenedor del servlet Web java, como 
		<a href="https://www.w3.org/Jigsaw/">Jigsaw</a>, <a href="https://tomcat.apache.org/">Tomcat</a> o
		<a href="http://www.mortbay.org/">Jetty</a> si planeas utilizar el validador como un servicio en l&iacute;nea. 
		Esta gu&iacute;a &uacute;nicamente cubre en detalle Tomcat y Jigsaw.</li>
</ul>
<p id="prereq-libs">Para la instalaci&oacute;n del validador en tu sistema, necesitar&aacute;s descargar y/o encontrar en tu sistema las siguientes librer&iacute;as de java:</p>
<ul class="instructions">
<li>servlet.jar
	(que, si tienes Tomcat instalado en [<span class="const">TOMCAT_DIR</span>],
	 deber&iacute;as encontrar en [<span class="const">TOMCAT_DIR</span>]/common/lib/, posiblemente con el nombre servlet-api.jar. Si no, cons&iacute;guelo en 
	<a href="http://java.sun.com/products/servlet/DOWNLOAD.html">java.sun.com</a>
</li>
<li>
<a href="https://jigsaw.w3.org/Devel/classes-2.2/20060329/">jigsaw.jar</a>
</li>
<li>xercesImpl.jar y xml-apis.jar (que puedes descargar con
	<a href="https://www.apache.org/dist/xml/xerces-j/">xerces-j-bin</a>).</li>
	<li><a href="http://ccil.org/~cowan/XML/tagsoup/">tagsoup.jar</a></li>
</ul>
<h4>Instalaci&oacute;n del Validador de CSS con Tomcat</h4>
<ol class="instructions">
<li>
		Descarga el validador seg&uacute;n lo explicado <a href="#source">anteriormente</a>.
	</li>
<li>Copia la carpeta del código fuente completa ("<span class="dir">.../css-validator/</span>") al directorio  <span class="dir">webapps</span>
		dentro de tu instalaci&oacute;n de Tomcat. Normalmente, este ser&aacute; 
		<span class="dir">[<span class="const">TOMCAT_DIR</span>]/webapps/</span>.
		El código fuente del Validador est&aacute;n ahora en <span class="dir">[<span class="const">TOMCAT_DIR</span>]/webapps/css-validator</span>,
		a la que llamaraemos a partir de ahora <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>.
	</li>
<li>En "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>" crea un directorio "<span class="dir">WEB-INF</span>", y en "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF</span>" crea un directorio "<span class="dir">lib</span>":<br/>
<kbd>mkdir -p WEB-INF/lib</kbd>
</li>
<li>Copia todos los ficheros jar (mencionados en los <a href="#prereq-libs">prerequisitos</a>) al directorio "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/lib</span>"</li>
<li>Compila el código fuente del validador que se encuentra en el directorio <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>,
		Ejecuta <kbd>ant</kbd>, asegurándote de que los archivos jar que descargaste estan establecidos correctamente en tu variable de entorno CLASSPATH. 
		Con caracter general, lo siguiente funcionar&aacute;:<br/>
<kbd>CLASSPATH=.:./WEB-INF/lib:$CLASSPATH ant</kbd>
</li>
<li>Copia o mueve "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/</span>
<span class="file">css-validator.jar</span>"
	a "<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/lib/</span>".</li>
<li>
		Copia o mueve el archivo "<span class="file">web.xml</span>" de
		"<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/</span>" a
		"<span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/</span>".
	</li>
<li>
		Para finalizar, reinicia el servidor Tomcat:<br/>
<kbd>"cd <span class="dir">[<span class="const">TOMCAT_DIR</span>]</span>; <span class="dir">./bin/</span>
<span class="file">shutdown.sh</span>; <span class="dir">./bin/</span>
<span class="file">startup.sh</span>;"</kbd>
</li>
</ol>
<h4>Instalaci&oacute;n en el Servidor Web Jigsaw</h4>
<ol class="instructions">
<li>Primero, descarga el código fuente, seg&uacute;n lo descrito anteriormente, consigue los jars necesarios, y compila el código con <kbd>ant</kbd>.</li>
<li>A continuación configura el directorio de inicio del validador (normalmente es css-validator) de forma que pueda funcionar como un contenedor del servlet. Para ello necesitas tener instalado Jigsaw (consulta las p&aacute;ginas de Jigsaw para una breve introducci&oacute;n (es realmente f&aacute;cil)) y a continuaci&oacute;n iniciar Jigsaw Admin. Cambia el HTTPFrame a ServletDirectoryFrame.</li>
<li>El siguiente paso es crear un recurso "validator", teniendo como class
'ServletWrapper' y como frame 'ServletWrapperFrame'. El &uacute;ltimo deber&iacute;a agregarse &eacute;l mismo autom&aacute;ticamente. La class del servlet es
org.w3c.css.servlet.CssValidator. Si ya existe un fichero llamado 'validator', por favor ren&oacute;mbralo. Es importante que este 'alias' sea siempre 'validator'.</li>
<li>Para finalizar, arranca jigsaw y ejecuta el validador. Comprueba qu&eacute; HTML deseas invocar. Normalmente tu URL ser&aacute; similar a esta:<br/>
http://localhost:8001/css-validator/validator.html</li>
</ol>
<h3>Utilizaci&oacute;n en l&iacute;nea de comandos</h3>
<p>El validador CSS puede utilizarse tambi&eacute;n como una herramienta de l&iacute;nea de comandos, si tu ordenador tiene instalado java. Compila css-validator.jar seg&uacute;n lo explicado anteriormente, y ejecuta:<br/>
<kbd>java -jar css-validator.jar http://www.w3.org/</kbd>
</p>
</div>
<ul class="navbar" id="menu">
<li>
<strong>
<a href="./" title="P&aacute;gina de inicio del Servicio de Validaci&oacute;n CSS del  W3C">Inicio</a>
</strong>
<span class="hideme">|</span>
</li>
<li>
<a href="about.html" title="Acerca de este servicio">Acerca de este servicio</a>
<span class="hideme">|</span>
</li>
<li>
<a href="documentation.html" title="Documentaci&oacute;n del Servicio de Validaci&oacute;n CSS del W3C">Documentaci&oacute;n</a>
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
<a href="https://www.w3.org/Style/CSS/learning" title="Learn more about Cascading Style Sheets">
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
