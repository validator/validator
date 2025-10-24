<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ru" lang="ru">
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>О сервисе W3C для проверки CSS</title>
    <link rev="made" href="mailto:www-validator-css@w3.org" />
    <link rev="start" href="./" title="Home Page" />
    <style type="text/css" media="all">
	@import "style/base.css";
	@import "style/docs.css";
    </style>
    <meta name="revision" content="$Id$" />
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

      <h2>О сервисе проверки CSS</h2>

<h3 id="TableOfContents">Содержание</h3>
<div id="toc">
<ol>
  <li>О сервисе
  <ol>
<li><a href="#what">Что это? Зачем это мне?</a></li>
<li><a href="#help">Описание выше слишком путанное! Объясните!</a></li>
<li><a href="#reference">Это официальная проверка на корректность CSS?</a></li>
<li><a href="#free">Сколько это стоит?</a></li>
<li><a href="#who">Кто написал это приложение? Кто его поддерживает?</a></li>
<li><a href="#contact">Как мне связаться с авторами? Сообщить об ошибке?</a></li>
<li><a href="#participate">Могу ли я помочь?</a></li>
  </ol>
  </li>
  <li>Уголок разработчика
  <ol>
    <li><a href="#code">На чем написан сервис проверки CSS? Доступны ли исходники?</a></li>
    <li><a href="#install">Могу ли я установить и запустить сервис проверки сам?</a></li>
    <li><a href="#api">Могу ли я построить приложение с использованием данного сервиса? Есть ли API?</a></li>
  </ol>
  </li>
</ol>
</div><!-- toc -->
<h3 id="about">О сервисе</h3>

<h4 id="what">Что это? Зачем это мне?</h4>

<p>Сервис проверки CSS&#x00a0;— бесплатное приложение, созданное организацией W3C для помощи веб-дизайнерам и веб-разработчикам в проверке каскадных таблиц стилей (CSS). Он может быть использован как <a href="./">бесплатный сервис</a> в сети или загружен для запуска на веб-сервере в качестве Java-приложения или сервлета.</p>

<p>Зачем это <em>вам</em>? Если вы веб-разработчик или веб-дизайнер, то этот сервис может стать бесценным помощником: он не только сравнивает таблицы стилей со спецификациями и помогает обнаружить ошибки, опечатки, неправильное использование CSS, но и сообщает о риске возникновения проблем с доступностью контента.</p>

<h4 id="help">Описание выше слишком путанное! Объясните!</h4>
<p>Большинство документов в сети написаны на компьютерном языке HTML. Он может быть использован для создания страниц со структурированной информацией, ссылками, мультимедийными объектами. Для цветов, шрифтов и верстки HTML использует язык описания стилей CSS («Cascade Style Sheets», «каскадные таблицы стилей»). Этот сервис позволяет людям проверить написанные ими таблицы стилей и, если потребуется, внести в них изменения.</p>

<h4 id="reference">Это официальная проверка на корректность CSS?</h4>
<p>Нет. Это надежная и полезная утилита, но это всего лишь программа, и, как у любого программного обеспечения, у нее есть <a href="https://github.com/w3c/css-validator/issues">ошибки и проблемы</a> &amp; <a href="https://www.w3.org/Bugs/Public/buglist.cgi?product=CSSValidator">ошибки и проблемы</a>. Актуальный справочник по таблицам каскадных стилей есть в их <a href="https://www.w3.org/Style/CSS/#specs">спецификации</a>.</p>

<h4 id="free">Сколько это стоит?</h4>
<p>Нисколько, это бесплатный сервис. Исходный код <a href="DOWNLOAD.html">открыт</a>, и вы можете свободно загрузить его, использовать, модифицировать, распространять&#x00a0;— <a href="https://www.w3.org/Consortium/Legal/copyright-software">делать с ним что угодно</a>.
Если этот сервис нравится вам, то вы можете <a href="#participate">присоединиться к проект</a> или добровольно спонсировать W3C через <a href="https://www.w3.org/Consortium/sup">программу поддержки</a>, но никто не заставляет вас это делать.</p>

<h4 id="who">Кто написал это приложение? Кто его поддерживает?</h4>
<p>Данный сервис размещается и обслуживается на сервере W3C, благодаря вкладу и работе членов W3C, добровольных разработчиков и переводчиков. Для подробной информации смотрите страницу <a href="thanks.html">создателей и участников</a>. Вы также можете <a href="#participate">внести свой вклад</a>.</p>

<h4 id="participate">Могу ли я помочь?</h4>
<p>Конечно. Если вы программируете на Java, то можете помочь проекту, проверяя, улучшая, <a href="https://github.com/w3c/css-validator/issues">исправляя</a> &amp; <a href="https://www.w3.org/Bugs/Public/buglist.cgi?product=CSSValidator">исправляя</a> исходный <a href="#code">код</a>, либо добавляя новые функции.</p>
<p>Для помощи в разработке и поддержке вам не обязательно быть программистом&#x00a0;— вы можете помочь улучшить документацию, перевести интерфейс на свой язык или подписаться на <a href="https://lists.w3.org/Archives/Public/www-validator-css/">лист рассылки</a> для обсуждения сервиса и помощи другим пользователям.</p>

<h4 id="contact">Есть еще вопросы?</h4>
<p>Если у вас возникли вопросы по CSS или сервису проверки CSS, задайте их в доступных
<a href="Email">рассылках и форумах</a>. Но перед этим убедитесь, что ответа нет в <a href="http://www.websitedev.de/css/validator-faq">FAQ сервиса проверки CSS</a>.</p>


<h3 id="dev">Уголок разработчика</h3>
<h4 id="code">На чем написан сервис проверки CSS? Доступны ли исходники?</h4>
<p>Сервис W3C для проверки CSS написан на Java; исходный код открыт и доступен через CVS. Вы можете
<a href="https://github.com/w3c/css-validator">посмотреть код в сети</a>, либо скачать его в соответствии с инструкциями. Для быстрого ознакомления с используемыми классами, ознакомьтесь с файлом <a href="README.html">README</a>.</p>

<h4 id="install">Могу ли я сам установить и запустить сервис проверки?</h4>
<p>Да, можете скачать и установить сервис проверки и запустить его, либо из командной строки, либо как сервлет. Ознакомьтесь с <a href="DOWNLOAD.html">инструкциями</a> по установке и запуску.</p>

<h4 id="api">Могу ли я построить приложение с использованием данного сервиса? Есть ли API?</h4>
<p>Да, и еще раз <a href="api.html">да</a>. Сервис проверки обладает <a href="api.html">интерфейсом SOAP</a> (RESTful), с помощью которого достаточно легко использовать его в приложениях (веб- или любых других). Если вы пользуетесь доступом к общему ресурсу, то учтите правила сетевого этикета: убедитесь, что приложение вызывает функцию sleep() между вызовами сервиса, либо установите свою копию.</p>
</div>
   <ul class="navbar"  id="menu">
	<li><strong><a href="./" title="Главная страница сервиса W3C по проверке CSS">Главная страница</a></strong> <span class="hideme">|</span></li>
        <li><a href="documentation.html" title="Документация по сервису W3C для проверки CSS">Документация</a> <span class="hideme">|</span></li>
        <li><a href="DOWNLOAD.html" title="Скачивание приложения проверки CSS">Скачивание</a> <span class="hideme">|</span></li>
        <li><a href="Email.html" title="Как оставить отзыв">Отзывы</a> <span class="hideme">|</span></li>
        <li><a href="thanks.html" title="Создатели и участники">Создатели</a><span class="hideme">|</span></li>

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

      <a href="https://www.w3.org/Style/CSS/learning" title="Изучите больше о Cascading Style Sheets"><img src="images/woolly-icon" alt="CSS" /></a>
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
