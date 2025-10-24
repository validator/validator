<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ru" lang="ru">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title>Руководство пользователя сервиса проверки CSS</title>
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

<div id="main">
<!-- This DIV encapsulates everything in this page - necessary for the positioning -->

<div class="doc">
<h2>Руководство пользователя сервиса проверки CSS</h2>

<h3 id="TableOfContents">Содержание</h3>

<div id="toc">
<ul>
  <li><a href="#use">Как использовать сервис проверки CSS</a>
    <ul>
      <li><a href="#url">Проверка по URL</a></li>
      <li><a href="#fileupload">Проверка загруженного файла</a></li>
      <li><a href="#directinput">Проверка непосредственно введенного кода</a></li>
      <li><a href="#basicvalidation">Что делает основная проверка?</a></li>
    </ul>
  </li>
  <li><a href="#advanced">Расширенная проверка</a>
    <ul>
	<li><a href="#paramwarnings">Предупреждения</a></li>
	<li><a href="#paramprofile">Настройка профиля</a></li>
	<li><a href="#parammedium">Настройка носителя информации</a></li>
    </ul>
  </li>
  <li><a href="#expert">Для специалистов</a>
    <ul>
	<li><a href="#requestformat">Формат запроса на проверку</a></li>
	<li><a href="#api">API веб-сервиса проверки CSS</a></li>
    </ul>
   </li>
</ul>
</div>

<p id="skip"></p>

<h3 id="use">Как использовать сервис проверки CSS</h3>

<p>Самый простой способ проверки предоставляет основной интерфейс. На его странице вы найдете описание трех форм, соответствующих трем способам проверки:</p>

<h4 id="url">Проверка по URL</h4>
<p>Просто введите URL документа, который хотите проверить. Документ может в формате HTML или CSS.</p>
<img style="display: block; margin-left:auto; margin-right: auto;" src="./images/uri_basic.png" alt="Проверка по URL" />

<h4 id="fileupload">Проверка загруженного файла</h4>
<p>Этот способ позволяет вам загрузить файл на сервер и проверить его. Нажмите кнопку «Обзор...» и выберите файл, который вы хотите проверить.</p>
<img style="display: block; margin-left:auto; margin-right: auto;" 
    src="./images/file_upload_basic.png" 
    alt="Проверка загруженного файла" />

<p>В этом случае допускаются только документы CSS. То есть, вы не можете загрузить документы (X)HTML. Также следует учесть наличие директив @import, так как они будут обработаны, только если явно ссылаются на общедоступный ресурс (так что не используйте в этом варианте проверки относительные пути).</p>

<h4 id="directinput">Проверка непосредственно введенного кода</h4>

<p>Этот способ идеален для проверки части CSS-файла. Вам только необходимо код в текстовое поле.</p>
<img style="display: block; margin-left:auto; margin-right: auto;" src="./images/direct_input_basic.png" alt="Проверка непосредственно введенного кода" />

<p>Справедливы приведенные ранее замечания. Заметьте, что этот способ очень удобен, если требуется определенная помощь других людей. Также это удобно для отправки сообщения об ошибке, поскольку вы можете создать ссылаться на результаты проверки в качестве тестового примера.</p>

<h4 id="basicvalidation">Что делает основная проверка?</h4>

<p>При использовании упрощенного интерфейса, сервис проверит документы на соответствие <a href="https://www.w3.org/TR/CSS2">CSS&#x00a0;2</a>&#x00a0;— текущей технической рекомендации для CSS.<br />
Он выдаст отчет в XHTML без каких-либо предупреждений (будет только информация об ошибках).<br />
Опция «среда» будет установлена в значение «all», что обозначает соответствие всем устройствам (смотрите <a href="https://www.w3.org/TR/CSS2/media.html">http://www.w3.org/TR/CSS2/media.html</a> для полного описания сред).</p>

<h3 id="advanced">Расширенная проверка</h3>

<p>Если нужна более конкретная проверка, можно использовать расширенный интерфейс, позволяющий указать три параметра. Далее приводится краткая справка по каждому из них.</p>

<h4 id="paramwarnings">Предупреждения</h4>

<p>Этот параметр полезен для настройки подробности отчетов сервиса проверки CSS. Действительно, сервис может выдавать два типа сообщений: ошибки и предупреждения. Ошибки выдаются, когда проверяемый CSS не соответствует рекомендации. Предупреждения отличаются от ошибок тем, что не относятся к проблемам выполнения спецификации. Они используются, чтобы предупредить (!) разработчика CSS, что некоторые аспекты могут быть опасны и странно обрабатываться пользовательскими приложениями.</p>

<p>Типичное предупреждение касается font-family: если вы не укажете базовый тип шрифта, вы получите предупреждение, говорящее о том, что вы должны добавить таковой в конец соответствующего правила CSS, иначе пользовательские приложения, не обладающие списком перечисленных в правиле шрифтов, переключатся на шрифт по умолчанию, что может исказить отображение информации.</p>

<h4 id="paramprofile">Профиль</h4>

<p>Сервис проверки может работать с различными профилями CSS. Профиль перечисляет все особенности и возможности реализации на конкретной платформе. Это определение взято с <a href="https://www.w3.org/Style/2004/css-charter-long.html#modules-and-profiles0">сайта CSS</a>. Выбор по умолчанию соответствует наиболее часто используемому, <a href="https://www.w3.org/TR/CSS2">CSS&#x00a0;2</a>.</p>

<h4 id="parammedium">Носитель</h4>

<p>Задание носителя соответствует правилу @media, применяющемуся ко всему документу. Вы можете найти больше информации о носителях по адресу <a href="https://www.w3.org/TR/CSS2/media.html">http://www.w3.org/TR/CSS2/media.html</a>.</p>

<h3 id="expert">Только для специалистов</h3>

<h4 id="requestformat">Формат запроса на проверку</h4>
<p>Ниже приведена таблица с параметрами, которые вы можете использовать для в запросах к сервису проверки CSS от W3C.</p>

<p>Если вы хотите использовать общий сервер проверки W3C, то используйте приведенные ниже параметры с основным URI<br />
<kbd>http://jigsaw.w3.org/css-validator/validator</kbd><br />
замените его адресом своего сервера, если вы хотите обратиться к собственной установке сервиса проверки.</p>

<p><strong>Примечание</strong>: если нужно программно вызывать сервис для множества документов, то убедитесь в, что ваши программы используют задержку <strong>минимум в 1 секунду</strong> между обращениями.
Сервис проверки предоставляется бесплатно для всех, поэтому будем признательны за уважительное отношение. Спасибо за понимание.</p>

<table class="refdoc">
  <tbody>
    <tr>
      <th>Параметр</th>
      <th>Описание</th>
      <th>Значение по умолчанию</th>
    </tr>
    <tr>
      <th>uri</th>
      <td><acronym title="Universal Resource Locator">URL</acronym> документа для проверки. Документ может в формате HTML или CSS.</td>
      <td>По умолчанию не установлен, однако для проверки документа должен быть указан либо этот параметр, либо <code>text</code>.</td>
    </tr>
    <tr>
      <th>text</th>
      <td>Документ для проверки; принимается только формат CSS.</td>
      <td>По умолчанию не установлен, однако для проверки документа должен быть указан либо этот параметр, либо <code>uri</code>.</td>
    </tr>
    <tr>
      <th>usermedium</th>
      <td><a href="https://www.w3.org/TR/CSS2/media.html">Среда</a>, используемая для проверки; например. <code>screen</code>, <code>print</code>, <code>braille</code>...</td>
      <td><code>all</code></td>
    </tr>
    <tr>
      <th>output</th>
      <td>Переключает различных форматов выдачи результата проверки. Возможные значения: <code>text/html</code> и <code>html</code> (документ XHTML, Content-Type: text/html), <code>application/xhtml+xml</code> и <code>xhtml</code> (документ XHTML, Content-Type: application/xhtml+xml), <code>application/soap+xml</code> и <code>soap12</code> (документ SOAP 1.2, Content-Type: application/soap+xml), <code>text/plain</code> и <code>text</code> (текстовый документ, Content-Type: text/plain), другие значения (документ XHTML document, Content-Type: text/html)</td>
      <td>html</td>
    </tr>
    <tr>
      <th>profile</th>
      <td>Профиль CSS, используемый для проверки. Может принимать значения
        <code>css1</code>, <code>css2</code>, <code>css21</code>,
        <code>css3</code>, <code>svg</code>, <code>svgbasic</code>,
        <code>svgtiny</code>, <code>mobile</code>, <code>atsc-tv</code>,
        <code>tv</code> или <code>none</code></td>
      <td>Последняя рекомендация W3C: CSS&#x00a0;2</td>
    </tr>
    <tr>
      <th>lang</th>
      <td>Язык отчета. В данный момент может принимать значения <code>en</code>, <code>fr</code>, <code>ja</code>, <code>es</code>, <code>zh-cn</code>, <code>nl</code>, <code>de</code>, <code>it</code>, <code>pl</code>.</td>
      <td>Английский (<code>en</code>).</td>
    </tr>
    <tr>
      <th>warning</th>
      <td>Уровень сообщений о предупреждениях: <code>no</code>&#x00a0;— для отключения предупреждений, <code>0</code>&#x00a0;— для минимального количества предупреждений, <code>1</code> или <code>2</code>&#x00a0;— для дальнейших уровней.</td>
      <td>2</td>
    </tr>
  </tbody>
</table>

<h4 id="api">API веб-сервиса проверки CSS: документация для интерфейса SOAP&#x00a0;1.2 сервиса проверки</h4>

<p>Более подробную техническую справку, в частности, по выводу данных в SOAP&#x00a0;1.2, а также о всех возможных способах вызова сервиса проверки, смотрите <a href="./api.html">API веб-сервиса проверки CSS</a>.</p>

</div>
</div>
<!-- End of "main" DIV. -->

   <ul class="navbar"  id="menu">
	<li><strong><a href="./" title="Главная страница сервиса W3C по проверке CSS">Главная страница</a></strong> <span class="hideme">|</span></li>
        <li><a href="documentation.html" title="Документация по сервису W3C для проверки CSS">Документация</a> <span class="hideme">|</span></li>
        <li><a href="DOWNLOAD.html" title="Скачивание приложения проверки CSS">Скачивание</a> <span class="hideme">|</span></li>
        <li><a href="Email.html" title="Как оставить отзыв">Отзывы</a> <span class="hideme">|</span></li>
        <li><a href="thanks.html" title="Создатели и участники">Создатели</a><span class="hideme">|</span></li>

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



