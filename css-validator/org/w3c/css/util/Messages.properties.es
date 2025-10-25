# Spanish translation, last updated in sync with Messages.properties.en,v 1.31

direction: ltr
language_name: Español
more_options: Más opciones
title_more_options: Show/Hide extra validation options
all: Todos
normal_report: Informe normal
most_important: Las más importantes
no_warnings: Sin advertencias
profile: Perfil
no_special_profile: Ninguno en especial
css1: CSS versión 1
css2: CSS versión 2
css21: CSS versión 2.1
css3: CSS versión 3
css3svg: CSS versión 3 + SVG
svg: SVG
svgbasic: SVG Básico
svgtiny: SVG Reducido
mobile: Móvil
ATSC_TV_profile: Perfil de TV ATSC
TV_profile: Perfil de TV
medium: Medio
aural: auditivo
braille: braille
embossed: relieve
handheld: pequeños dispositivos
print: impresión
projection: proyección
screen: pantalla
tty: teletipo
tv: televisión
presentation: presentación

type: Tipo
no_special_type: Automático
html_doc: HTML
css_doc: CSS

check: Check
W3C_CSS_validation_service: El Servicio de Validación de CSS del W3C
check_CSS: Verifica Hojas de Estilo en Cascada (CSS) y documentos (X)HTML con hojas de estilo
by_URI: mediante URI
by_file_upload: mediante Carga de Archivo
by_direct_input: mediante Entrada directa
validate_by_URI: Validar mediante URI
enter_uri: Introduce la URI de un documento (HTML con CSS o sólo CSS) que desees validar
page_address: Dirección de la página a Validar
address: Dirección
submit_uri: Enviar archivo para su validación
validate_by_file_upload: Validar mediante Carga de un Archivo
choose_document: Elige el documento que desees validar
choose_local_file: Elige un Archivo Local para su Carga y Validación
local_CSS_file: Archivo CSS local
submit_file: Enviar archivo para su validación
validate_by_input: Validar mediante entrada directa
enter_CSS: Introduce el código CSS que desees validar
submit_input: Enviar archivo para su validación
note: Nota
note_xhtml_valid: Si deseas validar tu hoja de estilo CSS incrustada en un documento (X)HTML, deberías antes <a href="https://validator.w3.org/">comprobar que el  (X)HTML utilizado es válido</a>
# End of variables added along with Index page template

W3C_validator_results: Resultados del Validador CSS del W3C para 
# file_title: <!-- provided by the user -->
CSS_validation_service: Servicio de Validación CSS
jump_to: Ir a
# errors_count: <!-- generated on the fly -->
errors: Los Errores 
# warnings_count <!-- generated on the fly -->
warnings: Las Advertencias 
validated_CSS: Su Hoja de Estilo validada
congrats: ¡Enhorabuena! No error encontrado.
# The sentence defined by doc_validates_before_link and doc_validates_after_link variables goes: \
# "This document validates as CSSXX!" where XX stands for the CSS version used \
# Since the content of the link about the CSS version is generated on the fly, \
# it is important to define what goes before the link and what goes afterwards in 2 separate \
# variables because the position of the link in the sentence varies in different languages
doc_validates_before_link: ¡Este documento es
doc_validates_after_link: válido!
no_errors_interoperable_msg: Puede mostrar este icono en cualquier página que valide para que los usuarios vean \
que se ha preocupado por crear una página Web interoperable. A continuación \
se encuentra el XHTML que puede usar para añadir el icono a su página Web:
valid_CSS: ¡CSS Válido!
# <!-- close the img tag with > instead of /> if using HTML<= 4.01 -->
no_errors_close_tags_msg: cierre la etiqueta img con &gt; en lugar de /&gt; si utiliza HTML &lt;= 4.01
no_errors_dl_local_msg: Si lo desea, puede descargar una copia de la imagen para guardarla en su \
directorio web local y cambiar el fragmento anterior de XHTML para referenciar \
a la imagen en local en lugar de a la de éste servidor.
no_errors_create_link_msg: Si desea crear un enlace con esta página (es decir, con los resultados \
de la validación) para hacer que sea más fácil revalidar la página en el futuro, \
o para permitir que otras personas validen su página, el URI es:
no_errors_forHTML_only_msg: para documentos HTML/XML únicamente
no_errors_bookmark_msg: O, simplemente, puede añadir la página actual a su lista de marcadores o favoritos.
note_valide_html: Para funcionar como previsto, tu hoja de estilo CSS necesita un "arbol sintactico". Esto significa que usted necesita usar <a href="https://validator.w3.org/"> un valido codigo HTML</a>.
top: Top
# hook_html_validator <!-- generated on the fly -->
not-css1-style: /* ¡ TENGA CUIDADO ! ¡ Esta propiedad no está incluida en CSS1 ! */
errors_sorry_msg: Disculpas! Hemos encontrado las siguientes errores
# errors_list: <!-- generated on the fly -->
# warnings_list: <!-- generated on the fly -->
# rules_count: <!-- generated on the fly -->
valid_CSS_info: Información de CSS válida
# charset: <!-- generated on the fly -->
# rules_list: <!-- generated on the fly -->
CSS_not_found: No se ha encontrado ninguna hoja de estilo
home: Inicio
about: Acerca de este servicio
doc: Documentación
dl: Download
feedback: Comentarios
credits: Créditos
home_title: Página de inicio del Servicio de Validación CSS del  W3C
about_title: Acerca de este servicio
doc_title: Documentación del Servicio de Validación CSS del W3C
dl_title: Descargar y installar el validador CSS
feedback_title: Cómo realizar comentarios sobre este servicio
credits_title: Créditos y Agradecimientos
W3C_quality_msg: Actividad de Garantí de Calidad del W3C, ofreciéndole herramientas de calidad Web gratuítas y más 
learn_more_msg: Aprenda má sobre las Hojas de Estilo en Cascada
support: Apoye esta herramienta, conviértase en un 
supporter: Contribuidor del W3C
# End of variables added along with Velocity

# Defines your own error and warning message here
content-type: text/html; charset=utf-8
content-language: es
output-encoding-name: utf-8

# You can change the level warning like this (example) :
# warning.redefinition.level: 5
#  level is an integer between 0 and 9 (all others values are ignored)
warning.redefinition: Redefinición de \u201C%s\u201D

# used by xml parser
warning.style-inside-comment: Do not put style rules inside HTML comments as they may be removed by user agent

# used by org.w3c.css.properties.Css1Style
warning.same-colors: Colores iguales para \u201C%s\u201D y \u201C%s\u201D
warning.no-color: Hay un color de fondo establecido y no hay color de primer plano
warning.no-background-color: Hay un color de primer plano establecido y no hay color de fondo
#warning.color.mixed-capitalization is now obsolete
#warning.color.mixed-capitalization: Aunque los nombres de los colores no son sensibles a las mayúsculas, es recomendable utilizar capitalización mixta para hacerlos más legibles: \u201C%s\u201D
warning.no-generic-family: \u201C%s\u201D: Es recomendable ofrecer una familia genérica como última alternativa
warning.with-space: Los nombres de familias que contengan espacios en blanco deben entrecomillarse. Si no se hace, cualquier espacio \
en blanco anterior o posterior al nombre será ignorado y cualquier secuencia de espacios en blanco dentro del nombre \
será convertida a un único espacio. 
warning.no-padding: Es recomendable tener un área de relleno (padding) con el color de fondo
warning.same-colors2: Color de primer plano y color de fondo iguales en dos contextos \u201C%s\u201D y \u201C%s\u201D
warning.relative-absolute: Hay algunas longitudes absolutas y relativas en \u201C%s\u201D. No es una hoja de estilo robusta.

# used by org.w3c.css.properties.CssSelectors
warning.unknown-html: \u201C%s\u201D no es un elemento de HTML
warning.html-inside: El elemento HTML no puede estar dentro de otro elemento
warning.body-inside: El elemento BODY no puede estar dentro de otro elemento que no sea el elemento HTML
warning.pseudo-classes: La pseudo-clase de Anchor \u201C%s\u201D sólo tiene efecto en los elementos 'A'
warning.pseudo:  Pseudo-elemento o pseudo-clase \u201C%s\u201D desconocido(a) en el perfil por defecto (\u201C%s\u201D)
# not used by org.w3c.css.properties.CssSelectors for the moment
warning.noinside: \u201C%s\u201D no puede estar dentro de un elemento de línea
warning.withblock: Cuidado. Los pseudo-elementos sólo se pueden unir a elementos de bloque
warning.block-level: Estas propiedad se aplica a elementos de bloque.

# used by org.w3c.css.parser.Frame
warning.no-declaration: No hay declaraciones en la regla

# used by org.w3c.css.parser.CssFouffa
warning.unsupported-import: Las hojas de estilo importadas no se comprueban en los modos de entrada directa y carga de archivo

# used by org.w3c.css.values.CssColor
warning.out-of-range: \u201C%s\u201D está fuera de rango
error.invalid-color: Función RGB no válida

warning.marker: La propiedad marker-offset se aplica a elementos con 'display: marker'

# used by org.w3c.css.properties.ACssStyle
warning.relative: Utilizar unidades relativas da lugar a hojas de estilo más robustas en la propiedad \u201C%s\u201D

# used by org.w3c.css.css.StyleSheetParser and org.w3c.css.css.StyleSheetXMLParser
error.at-rule: Lo lamentamos, la regla-arroba \u201C%s\u201D no está implementada.
warning.at-rule: Lo lamentamos, la regla-arroba \u201C%s\u201D no está implementada.

# used by all properties and values
error.operator: \u201C%s\u201D es un operador incorrecto
error.negative-value: Valores negativos de \u201C%s\u201D no están permitidos
error.few-value: Faltan valores para la propiedad \u201C%s\u201D

# be careful here, values comes first
# You can't write something like this : For the color, blue is an incorrect value
error.value: \u201C%s\u201D no es un valor de \u201C%s\u201D

#used by org.w3c.css.properties3.CssToggleGroup
error.groupname: \u201C%s\u201D no es un nombre de grupo correcto. Use un identificador válido

#used by org.w3c.css.properties3.CssGroupReset
error.nogroup: \u201C%s\u201D no ha sido establecido por la propiedad toggle-group

#used by org.w3c.css.properties3.CssGlyphOrVert
error.anglevalue: El valor tiene que estar comprendido entre -360 y 360, y ser divisible por 90

#used by org.w3c.css.properties3.CssTextKashidaSpace
error.percentage: se espera un valor en porcentaje

#used by org.w3c.css.properties.CssTextAlign
warning.xsl: el valor \u201C%s\u201D sólo se aplica a XSL

#used by org.w3c.css.parser.analyzer.CssParser
warning.medialist : la lista de medios (medialist) debería comenzar por 'media :' \u201C%s\u201D
error.nocomb: El combinador \u201C%s\u201D entre selectores no está permitido en este perfil o versión

#used by org.w3c.css.properties.CssDirection
warning.direction: use la nueva propiedad de CSS3 'writing-mode' en lugar de usar 'direction' para los elementos de bloque

# used by org.w3c.css.properties.CssTextDecoration
error.same-value: \u201C%s\u201D aparece dos veces

error.generic-family.quote: Los nombres de familia genéricos son palabras reservadas y, por tanto, no deben entrecomillarse.

# used by org.w3c.css.properties.CssClip
error.shape: Definición de figura no válida rect(<top>,<right>,<bottom>,<left>)
error.shape-separator: Separador no válido en la definición de figura. Debe ser una coma.

# used by org.w3c.css.properties.CssContent
error.attr: Definición de attr no válida attr(X)
error.function: Definición de función no válida 
error.counter: Definición de contador no válida counter(<identifier>[,<list-style-type>]?)
error.counters: Definición de contadores no válida counters(<identifier>,<string>[,<list-style-type>]?)

# used by org.w3c.css.font.Src
error.format: Definición de formato no válida format(<string>[,<string>]*)
error.local: Definición de localización no válida local(<string>|<ident>+)

# used by org.w3c.css.values.CssAngle, org.w3c.css.values.CssFrequency, org.w3c.css.values.CssTime, org.w3c.css.values.CssLength
error.unit: \u201C%s\u201D es una unidad incorrecta

# used by org.w3c.css.aural.ACssAzimuth
error.degree: La posición debe estar especificada en términos de grados.

# used by org.w3c.css.aural.ACssElevation
error.elevation.range: Especificar la elevación como un ángulo entre '-90deg' y '90deg'.

# used by org.w3c.css.aural.ACssPitchRange
error.range: El valor está fuera del rango. Este valor debe estar comprendido entre '0' y '100'.

# used by org.w3c.css.properties.CssTextShadow
error.two-lengths: Un offset de sombra se especifica con dos valores <length> (Opcionalmente, depués del offset de sombra puede especificarse un ratio de difuminado.)

error.integer: Éste número debe ser un entero.
error.comma: Falta una coma para separar.

# used by org.w3c.css.values.CssPercentage
error.percent: \u201C%s\u201D no es un porcentaje correcto

# used by org.w3c.css.values.CssString
error.string: \u201C%s\u201D no es una cadena correcta

# used by org.w3c.css.values.CssURL
error.url: \u201C%s\u201D no es un URL correcto

# used by org.w3c.css.values.CssColor
error.rgb: \u201C%s\u201D no es un color válido de 3 o 6 cifras hexadecimales
error.angle: \u201C%s\u201D no es un ángulo válido. El valor debe estar comprendido entre 0 y 360

# used by org.w3c.css.values.CssNumber
error.zero: Únicamente 0 puede ser un \u201C%s\u201D. Debe especificarse una unidad detrás de la cifra
warning.zero: Únicamente 0 puede ser un \u201C%s\u201D. Debe especificarse una unidad detrás de la cifra

# used by org.w3c.css.parser.CssPropertyFactory 
error.noexistence-at-all: La propiedad \u201C%s\u201D no existe
error.noexistence-media: La propiedad \u201C%s\u201D no existe en el medio \u201C%s\u201D
error.noexistence: La propiedad \u201C%s\u201D no existe en \u201C%s\u201D pero existe en \u201C%s\u201D
error.noexistence-typo: La propiedad \u201C%s\u201D no existe. El nombre más parecido, de una propiedad válida, es \u201C%s\u201D
warning.noexistence: La propiedad \u201C%s\u201D no existe en \u201C%s\u201D pero existe en \u201C%s\u201D
warning.noexistence-media: La propiedad \u201C%s\u201D no existe en el medio \u201C%s\u201D
warning.notforusermedium : La propiedad \u201C%s\u201D no existe en este medio de usuario
warning.noothermedium : Las propiedades de otros medios podrían no funcionar en el medio de usuario
# used by org.w3c.css.parser.AtRule*
error.noatruleyet : Las reglas-arroba que no sean @import no son soportadas por CSS1 \u201C%s\u201D
# used by org.w3c.css.parser.analyzer.CssParser
error.notforcss1 : El valor \u201C%s\u201D no existe en CSS1

# used by org.w3c.css.parser.CssFouffa
error.unrecognize: Faltan valores o no se reconocen los valores

# used by org.w3c.css.parser.CssFouffa
generator.unrecognize: Error de análisis sintáctico
generator.dontmixhtml: Error sintáctica. Hoja de estilo ne pueden incluir fragmentos de HTML. 

# used by org.w3c.css.parser.CssSelectors
error.pseudo-element: El pseudo-elemento \u201C%s\u201D no puede aparecer aquí en el contexto \u201C%s\u201D
error.pseudo-class: La pseudo-clase .\u201C%s\u201D no puede aparecer aquí en el contexto de HTML \u201C%s\u201D
error.pseudo: Pseudo-clase o pseudo-elemento \u201C%s\u201D desconocido(a)
error.id: ¡El selector de ID #\u201C%s\u201D no es válido! En un selector simple sólo puede especificarse un selector de ID: \u201C%s\u201D.
error.space: Si se utiliza el selector de atributo ~= entonces el valor de \u201C%s\u201D no puede contener espacios.
error.todo: Lo lamentamos, esta función \u201C%s\u201D todavía no está implementada.
error.incompatible: \u201C%s\u201D y \u201C%s\u201D son incompatibles
warning.incompatible: \u201C%s\u201D y \u201C%s\u201D son incompatibles
error.notformobile : \u201C%s\u201D no puede usarse en perfiles móviles
error.notforatsc : \u201C%s\u201D no puede usarse en perfiles ATSC
error.notfortv : \u201C%s\u201D no puede usarse en perfiles de televisión
error.notversion : \u201C%s\u201D no puede usarse en esta versión de CSS: \u201C%s\u201D
warning.notversion: \u201C%s\u201D no se puede utilizar en esta versión de CSS: \u201C%s\u201D

error.media: medio no reconocido \u201C%s\u201D 
error.page: página pseudo-nombrada no reconocida \u201C%s\u201D

error.unrecognized.link: elemento de enlace o instrucción de procesamiento de hoja de estilo xml no reconocida.

# used by StyleSheetGeneratorHTML
generator.context: Contexto
generator.request: Se ha producido un error en el procesado de su hoja de estilo. \
Por favor, corrija su petición o envíe un correo a plh@w3.org.
generator.unrecognized: No reconocido
generator.invalid-number: Número no válido
generator.property: Propiedad no válida
generator.line: Línea
generator.not-found: Archivo no encontrado
generator.doc-html: <!-- removed this confusing message olivier 2006-12-14 -->
generator.doc: <!-- removed this confusing message olivier 2006-12-14 -->

# used by the parser
parser.semi-colon: Tentativa de encontrar un punto y coma antes del nombre de la propiedad. Añádalo

parser.unknown-dimension: dimensión desconocida

parser.old_class: En CSS1, un nombre de clase puede empezar por un dígito (".55ft"), \
excepto si es una magnitud de medida (".55in"). En CSS2, esas clases son interpretadas como \
magnitudes de medida desconocidas (para permitir añadir nuevas magnitudes en un futuro)

parser.old_id: En CSS1, un nombre de id puede empezar por un dígito ("#55ft"), \
excepto si es una magnitud de medida ("#55in"). En CSS2, esos nombres son interpretados como \
magnitudes de medida desconocidas (para permitir añadir nuevas magnitudes en un futuro)

parser.class_dim: En CSS1, un nombre de clase puede empezar por un dígito (".55ft"), \
excepto si es una magnitud de medida (".55in")

parser.id_dim: En CSS1, un nombre de id puede empezar por un dígito ("#55ft"), \
excepto si es una magnitud de medida ("#55in")

parser.charset:La regla @charset sólo puede aparecer al comienzo de la hoja \
de estilo. Por favor, compruebe que no hay espacios antes.

parser.charsetspecial:Este perfil tiene una sintaxis muy específica para @charset: \
@charset seguido de un espacio exactamente, seguido por el nombre de la codificación \
entre comillas, seguido inmediatamente por un punto y coma.
warning.charsetspecial:Este perfil tiene una sintaxis muy específica para @charset: \
@charset seguido de un espacio exactamente, seguido por el nombre de la codificación \
entre comillas, seguido inmediatamente por un punto y coma.

warning.old_id:En CSS1, un nombre de id puede empezar por un dígito ("#55ft"), \
excepto si es una magnitud de medida ("#55in"). En CSS2, esos nombres son interpretados como \
magnitudes de medida desconocidas (para permitir añadir nuevas magnitudes en un futuro)

warning.old_class:En CSS1, un nombre de id puede empezar por un dígito (".55ft"), \
excepto si es una magnitud de medida (".55in"). En CSS2, esos nombres son interpretados como \
magnitudes de medida desconocidas (para permitir añadir nuevas magnitudes en un futuro)

# used by the servlet
servlet.invalid-request: Se ha enviado una petición no válida.
servlet.process: No se puede procesar el objeto

warning.atsc : \u201C%s\u201D podría no ser compatible con el medio atsc-tv
error.onlyATSC : \u201C%s\u201D esta función es sólo para el medio atsc-tv

warning.otherprofile : la propiedad \u201C%s\u201D no existe en este perfil, pero es válida conforme a otro perfil

#used by org.w3c.css.parser.analyzer.CssParser
error.nocomb: La combinación \u201C%s\u201D entre selectores no está permitida en este perfil o versión
warning.nocomb: No se permite el combinador \u201C%s\u201D entre selectores en este perfil (\u201C%s\u201D)
error.unknown: Error desconocido

warning.float-no-width: En (x)HTML+CSS, los elementos flotados han de tener un ancho (width) declarado. Únicamente elementos con un ancho intrínseco (html, img, input, textarea, select u object) no se ven afectados
parser.charsetcss1: En CSS1 no se pueden utilizar reglas @charset
parser.attrcss1: Los selectores de atributo no son válidos en CSS1
parser.invalid_id_selector: Selector ID no válido
parser.import_not_allowed: Sólo se permite @import después de una declaración válida @charset y @import

error.divisortype: El divisor debe ser un número
error.email: Las direcciones de e-mail no pueden ser validadas por esta herramienta, podrías ser víctima de un engaño
error.conflicting-charset: Conflicto en la definición del juego de caracteres entre la red, @charset \u201C%s\u201D y el juego de caracteres \u201C%s\u201D
error.bg_order: En la definición de fondo de CSS3, \u201Cbg_position\u201D debe aparecer antes que \u201Cbg_size\u201D si ambas están presentes
error.divzero: División entre cero
