<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ru" lang="ru">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Загрузка и установка сервиса проверки CSS</title>
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
     <a href="./"><span>Сервис проверки CSS</span></a></h1>
     <p id="tagline">
       Проверка таблиц каскадных стилей (CSS) и документов (X)HTML с таблицами стилей
     </p>
    </div>

<div class="doc">
<h2>Загрузка и установка сервиса проверки CSS</h2>

<p>This translation of the installation guide for the CSS validator may be out of date. For a reliable, up-to-date guide, refer to the <a href="DOWNLOAD.html.en">English</a> or <a href="DOWNLOAD.html.fr">French</a> versions.</p>

<h3 id="download">Загрузка</h3>	

	<h4 id="source">Загрузка исходных файлов</h4>
    <p><a href="https://github.com/w3c/css-validator">Сервис проверки CSS</a> доступен для загрузки через CVS. Следуйте <a href="https://dev.w3.org/cvsweb/">инструкциям для доступа к общедоступному серверу CVS</a> и загрузите 2002/css-validator. Заметьте, что на jigsaw.w3.org/css-validator установлена более ранняя версия, нежели та, что хранится в репозитории CVS, так что результаты вывода и внешний вид могут незначительно различаться</p>

	<h4>Загрузка пакета Java (jar или war)</h4>
<!--	<p>TBD... we just need a stable location to put jar/war archives on a regular basis</p>-->
	<p><a href="https://github.com/w3c/css-validator/releases/latest/download/css-validator.jar">css-validator.jar</a></p>


<h3>Руководство по установке</h3>
<p>Сервис является сервлетом Java, он может быть установлен в любой сервлет-контейнер, а также может быть вызван из командной строки. Официальный сервис от W3C работает под управлением сервера Jigsaw, который и рекомендуется для установки локального сервиса. Однако, для простоты, в этом документе мы в основном будем рассказывать, как установить сервис проверки на Tomcat&#x00a0;— контейнере сервлетов от Apache.</p>

<p>Ниже приводится ряд инструкций по установке сервлета на Jigsaw и запуску сервиса из командной строки.</p>

<h4 id="prereq">Требования для установки</h4>

<p>Подразумевается, что вы загрузили, установили и проверили:</p>

<ul class="instructions">
	<li>рабочую среду Java;</li>
	<li>средство разработки <a href="https://ant.apache.org/">Ant</a>;</li>
	<li>контейнер сервлетов&#x00a0;— например, <a href="https://www.w3.org/Jigsaw/">Jigsaw</a>, <a href="https://tomcat.apache.org/">Tomcat</a> или <a href="http://www.mortbay.org/">Jetty</a>&#x00a0;— если планируете создать сетевой сервис; в этой инструкции подробно рассказывается только про Jigsaw и Tomcat.</li>
</ul>

<p id="prereq-libs">Для установки сервиса в своей системе необходимо загрузить или найти у себя на компьютере ряд библиотек Java:</p>

<ul class="instructions">
	<li>servlet.jar (если Tomcat установлен в [<span class="const">TOMCAT_DIR</span>], то можете найти этот файл в [<span class="const">TOMCAT_DIR</span>]/common/lib/); возможно, он будет называться «servlet-api.jar»; если его нет, загрузите с <a href="http://java.sun.com/products/servlet/DOWNLOAD.html">java.sun.com</a>;</li>
	<li><a href="https://jigsaw.w3.org/Devel/classes-2.2/20060329/">jigsaw.jar</a>;</li>
	<li>xercesImpl.jar и xml-apis.jar (могут быть загружены с <a href="https://www.apache.org/dist/xml/xerces-j/">xerces-j-bin</a>);</li>
	<li><a href="http://ccil.org/~cowan/XML/tagsoup/">tagsoup.jar</a>.</li>
</ul>

<h4>Установка сервиса проверки CSS на сервере Tomcat</h4>

<ol class="instructions">
	<li>Загрузите установочные файлы, как описано <a href="#source">выше</a>.</li>
	<li>Скопируйте всю директорию <span class="dir">.../css-validator/</span> в директорию <span class="dir">webapps</span> сервера Tomcat; обычно это <span class="dir">[<span class="const">TOMCAT_DIR</span>]/webapps/</span>. Исходный код сервиса проверки теперь находится в <span class="dir">[<span class="const">TOMCAT_DIR</span>]/webapps/css-validator</span>, которую мы обозначим как <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span>.</li>
	<li>В директории <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span> создайте поддиректорию <span class="dir">WEB-INF</span>, а в <span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF</span>&#x00a0;— поддиректорию <span class="dir">lib</span>:<br /><kbd>mkdir -p WEB-INF/lib</kbd></li>
	<li>Скопируйте все файлы с расширением jar, перечисленные в <a href="#prereq-libs">требованиях для установки</a>, в директорию <span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/lib</span></li>
	<li>Скомпилируйте исходный код: в <span class="dir">[<span class="const">VALIDATOR_DIR</span>]</span> запустите <kbd>ant</kbd> и убедитесь, что загруженные jar-файлы корректно прописаны в переменной окружения CLASSPATH. В общем случае работает следующая конструкция:<br /><kbd>CLASSPATH=.:./WEB-INF/lib:$CLASSPATH ant</kbd></li>
	<li>Скопируйте или переместите <span class="dir">[<span class="const">VALIDATOR_DIR</span>]/</span><span class="file">css-validator.jar</span> в <span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/lib/</span>.</li>
	<li>Скопируйте или переместите файл <span class="file">web.xml</span> из <span class="dir">[<span class="const">VALIDATOR_DIR</span>]/</span> в <span class="dir">[<span class="const">VALIDATOR_DIR</span>]/WEB-INF/</span>.
	</li>
    <li>Последний шаг: перезапустите Tomcat:<br /><kbd>cd <span class="dir">[<span class="const">TOMCAT_DIR</span>]</span>; <span class="dir">./bin/</span><span class="file">shutdown.sh</span>; <span class="dir">./bin/</span><span class="file">startup.sh</span>;</kbd></li>
</ol>

<h4>Установка на сервере Jigsaw</h4>
<ol class="instructions">
<li>Как описано выше, загрузите исходный код и необходимые jar-файлы; скомпилируйте исходники при помощи <kbd>ant</kbd>.</li>
<li>Настройте корневую директорию сервиса (обычно «css-validator») для того, чтобы он мог работать как сервлет. Для этого установите Jigsaw (краткие инструкции смотрите на страницах поддержки Jigsaw&#x00a0;— они действительно не сложные) и запустите администрирование сервера Jigsaw. Измените HTTPFrame на ServletDirectoryFrame.</li>
<li>Создайте ресурс validator в качестве класса ServletWrapper и фрейма ServletWrapperFrame; всё остальное будет сделано автоматически. Класс сервлета&#x00a0;— org.w3c.css.servlet.CssValidator. Если уже существует файл validator, то переименуйте его. Важно, чтобы этот alias всегда назывался «validator».</li>
<li>Последний шаг: запустите Jigsaw и откройте в браузере сервис проверки. Обычно URL выглядит как<br />http://localhost:8001/css-validator/validator.html</li>
</ol>

<h3>Использование из командной строки</h3>

<p>Если на компьютере установлена виртуальная машина Java, то сервис проверки CSS может также вызываться из командной строки. Скомпилируйте css-validator.jar, как указано выше, и запустите следующим образом:<br />
<kbd>java -jar css-validator.jar http://www.w3.org/</kbd></p>
</div>
   <ul class="navbar"  id="menu">
	<li><strong><a href="./" title="Главная страница сервиса W3C по проверке CSS">Главная страница</a></strong> <span class="hideme">|</span></li>
        <li><a href="documentation.html" title="Документация по сервису W3C для проверки CSS">Документация</a> <span class="hideme">|</span></li>
        <li><a href="DOWNLOAD.html" title="Скачивание приложения проверки CSS">Скачивание</a> <span class="hideme">|</span></li>
        <li><a href="Email.html" title="Как оставить отзыв">Отзывы</a> <span class="hideme">|</span></li>
        <li><a href="thanks.html" title="Создатели и участники">Создатели</a><span class="hideme">|</span></li>

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

      <a href="https://www.w3.org/Style/CSS/learning" title="Узнайте больше о CSS"><img src="images/woolly-icon" alt="CSS" /></a>
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




