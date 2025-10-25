# Dutch translation, last updated in sync with Messages.properties.en,v 1.31

direction: ltr
language_name: Nederlands
more_options: Meer Opties
title_more_options: Weergeven/Verbergen van extra validatie opties
all: Alle
normal_report: Normaal
most_important: Meest belangrijk
no_warnings: Geen
profile: Profiel
no_special_profile: Geen speciaal profiel
css1: CSS versie 1
css2: CSS versie 2
css21: CSS versie 2.1
css3: CSS versie 3
svg: SVG
svgbasic: SVG Basic
svgtiny: SVG tiny
mobile: Mobile
ATSC_TV_profile: ATSC TV profiel
TV_profile: TV profiel
medium: Medium
aural: aural
braille: braille
embossed: embossed
handheld: handheld
print: print
projection: projection
screen: screen
tty: TTY
tv: TV
presentation: presentation

type: Type
no_special_type: Automatisch
html_doc: HTML
css_doc: CSS

check: Controleer
W3C_CSS_validation_service: De W3C CSS Validatie Service
check_CSS: Controleer Cascading Style Sheets (CSS) en (X)HTML documenten die gebruik maken van style sheets
by_URI: via een URI
by_file_upload: via het uploaden van een bestand
by_direct_input: via directe invoer
validate_by_URI: Valideer via een URI
enter_uri: Geef de URI op van een document (HTML met CSS of alleen CSS) dat je wilt valideren
page_address: Adres van de pagina om de valideren
address: Adres
submit_uri: Verzenden om te valideren
validate_by_file_upload: Valideer via het uploaden van een bestand
choose_document: Kies het document dat je wilt valideren
choose_local_file: Kies een lokaal bestand dat je wilt uploaden en valideren
local_CSS_file: Lokaal CSS bestand
submit_file: Verzenden om te valideren
validate_by_input: Valideer via directe invoer
enter_CSS: Voer de CSS in die je wilt valideren
submit_input: Verzenden om te valideren
note: Opmerking
note_xhtml_valid: Als je een CSS style sheet wilt valideren die gebruikt wordt in een (X)HTML document, zou je die eerst moeten <a href="https://validator.w3.org/">laten controleren</a>
# End of variables added along with Index page template

W3C_validator_results: W3C CSS Validator Resultaten met object : 
# file_title: <!-- provided by the user -->
CSS_validation_service: CSS Validatie Service
jump_to: Ga naar
# errors_count: <!-- generated on the fly -->
errors: Fouten 
# warnings_count <!-- generated on the fly -->
warnings: Waarschuwingen
validated_CSS: Uw gevalideerde CSS
congrats: Gefeliciteerd! Geen fouten gevonden.
# The sentence defined by doc_validates_before_link and doc_validates_after_link variables goes: \
# "This document validates as CSSXX!" where XX stands for the CSS version used \
# Since the content of the link about the CSS version is generated on the fly, \
# it is important to define what goes before the link and what goes afterwards in 2 separate \
# variables because the position of the link in the sentence varies in different languages
doc_validates_before_link: Dit document is gevalideerd als 
doc_validates_after_link: !
no_errors_interoperable_msg: Om uw lezers te laten zien dat u de moeite heeft genomen om een interoperabele webpagina te maken, \
kunt u dit pictogram op elke gevalideerde pagina plaatsen. Hier is de HTML code \
die u kunt gebruiken om dit pictogram aan uw webpagina toe te voegen:
valid_CSS: Valide CSS!
# <!-- close the img tag with > instead of /> if using HTML<= 4.01 -->
no_errors_close_tags_msg: Sluit de img tag met &gt; in plaats van met /&gt; als u HTML versie 4.01 of eerder gebruikt
no_errors_dl_local_msg: Als u wilt, kunt u een kopie van dit plaatje downloaden, in uw locale webdirectory \
zetten en in het XHTML fragment hierboven refereren aan uw lokaal opgeslagen afbeelding \
in plaats van aan die op deze server.
no_errors_create_link_msg: Als u een link wilt maken naar deze pagina (i.e. naar dit validatieresultaat \
om het gemakkelijker te maken deze pagina later te revalideren of anderen \
in staat te stellen uw pagina te valideren is de URL:
no_errors_forHTML_only_msg: Alleen voor HTML documenten
no_errors_bookmark_msg: Of u kunt de huidige pagina toevoegen aan uw favorieten of hotlist.
note_valide_html: Als je een CSS style sheet wilt valideren die gebruikt wordt in een (X)HTML document, zou je die eerst moeten <a href="https://validator.w3.org/">laten controleren</a>.
top: Top
# hook_html_validator <!-- generated on the fly -->
not-css1-style: /* VOORZICHTIG ! Dit is geen CSS1 eigenschap ! */
errors_sorry_msg: Sorry! We vonden de volgende fouten
# errors_list: <!-- generated on the fly -->
# warnings_list: <!-- generated on the fly -->
# rules_count: <!-- generated on the fly -->
valid_CSS_info: Uw gevalideerde Cascading Style Sheet :
# charset: <!-- generated on the fly -->
# rules_list: <!-- generated on the fly -->
CSS_not_found: Geen style sheet gevonden
home: Home
about: Over
doc: Documentatie
dl: Download
feedback: Reacties
credits: Credits
home_title: Home pagina van de W3C CSS Validatie Service
about_title: Over deze service
doc_title: Documentatie voor de W3C CSS Validatie Service
dl_title: Download de CSS validator
feedback_title: Hoe reacties te geven over deze service
credits_title: Credits en Erkenning
W3C_quality_msg: W3C's Quality Assurance Activity, bringing you free Web quality tools and more
learn_more_msg: Leer meer over Cascading Style Sheets
support: Ondersteun de validator, wordt een
supporter: W3C Supporter
# End of variables added along with Velocity

# Definieert uw eigen error en waarschuwingsberichten hier
content-type: text/html; charset=utf-8
content-language: nl
output-encoding-name: utf-8

# U kunt zo het niveau van waarschuwing aanpassen (voorbeeld) :
# warning.redefinition.level: 5
# level is een integer tussen 0 en 9 (alle andere waarden worden genegeerd)
warning.redefinition: Herdefinitie van \u201C%s\u201D

# used by xml parser
warning.style-inside-comment: Plaats geen style regels in HTML commentaar, \
deze kunnen worden verwijderd door het programma van de gebruiker

# used by org.w3c.css.properties.Css1Style
warning.same-colors: Dezelfde kleuren voor \u201C%s\u201D en \u201C%s\u201D
warning.no-color: Je hebt geen tekstkleur opgegeven (of de kleur is transparant) maar je hebt wel een achtergrondkleur opgegeven. Zorg ervoor dat het opgeven van tekst- en achtergrondkleuren de tekst leesbaar houdt.
warning.no-background-color: Je hebt geen achtergrondkleur opgegeven (of de achtergrondkleur is transparant) maar je hebt wel een tekstkleur opgegeven. Zorg ervoor dat het opgeven van tekst- en achtergrondkleuren de tekst leesbaar houdt.
#warning.color.mixed-capitalization is now obsolete
#warning.color.mixed-capitalization: Alhoewel namen van kleuren case-insensitive zijn is het beter om de gemengde  schrijfwijze te hanteren om de namen beter leesbaar te maken: \u201C%s\u201D
warning.no-generic-family: \u201C%s\u201D: Het is beter een algemene font-family op te geven als laatste alternatief
warning.with-space: Familienamen die spaties bevatten moeten tussen aanhalingstekens worden geplaatst. \
Als quotes worden weggelaten wordt elke tussenruimte voor en achter de naam genegeerd en elke groep \
whitespace-tekens in de naam wordt omgezet naar een spatie.
warning.no-padding: Het is beter een padding te gebruiken met een achtergrondkleur
warning.same-colors2: Dezelfde kleur en achtergrondkleur in twee contexten \u201C%s\u201D en \u201C%s\u201D
warning.relative-absolute: U hebt absolute en relatieve lengtes in \u201C%s\u201D. Dit is geen robuuste style sheet.

# used by org.w3c.css.properties.CssSelectors
warning.unknown-html: \u201C%s\u201D is geen HTML Element
warning.html-inside: HTML element kan niet binnen een ander element voorkomen
warning.body-inside: BODY element kan niet binnen een ander element dan HTML voorkomen
warning.pseudo-classes: Anker pseudo-class \u201C%s\u201D heeft geen effect op andere elementen dan 'A'

# niet gebruikt door org.w3c.css.properties.CssSelectors op dit moment
warning.noinside: \u201C%s\u201D kan niet voorkomen binnen een inline element
warning.withblock: Wees voorzichtig. Pseudo-elementen kunnen alleen worden gekoppeld aan een block-level element
warning.block-level: Deze eigenschap is van toepassing op block-level elementen.

# gebruikt door org.w3c.css.parser.Frame
warning.no-declaration: Geen declaraties in de regel

# used by org.w3c.css.parser.CssFouffa
warning.unsupported-import: De geïmporteerde style sheets worden niet gecontroleerd bij het direct invoeren of uploaden van een style sheet

# gebruikt door org.w3c.css.values.CssColor
warning.out-of-range: \u201C%s\u201D valt buiten het bereik
error.invalid-color: Ongeldige RGB functie

warning.marker: De marker-offset eigenschap is van toepassing op elementen met 'display: marker'

# gebruikt door org.w3c.css.properties.ACssStyle
warning.relative: Het gebruik van relatieve maten geeft robuustere stylesheets bij de eigenschap \u201C%s\u201D

# gebruikt door org.w3c.css.css.StyleSheetParser and org.w3c.css.css.StyleSheetXMLParser
error.at-rule: Sorry, de at-regel \u201C%s\u201D is niet geimplementeerd.
warning.at-rule: Sorry, de at-regel \u201C%s\u201D is niet geimplementeerd.

# gebruikt voor alle eigenschappen en waarden
error.operator: \u201C%s\u201D is een incorrecte operator
error.negative-value: \u201C%s\u201D negatieve waarden zijn niet toegestaan
error.few-value: te weinig waarden voor de eigenschap \u201C%s\u201D

# gebruikt door org.w3c.css.properties3.CssToggleGroup
error.groupname: \u201C%s\u201D is geen correcte groepnaam. Gebruik een geldige identifier

# gebruikt door org.w3c.css.properties3.CssGroupReset
error.nogroup: \u201C%s\u201D is niet geinitialiseerd door een toggle-group eigenschap

error.notforatsc: \u201C%s\u201D kan niet gebruikt worden met het ATSC profiel
error.notfortv: \u201C%s\u201D kan niet gebruikt worden met het TV profiel
error.notversion: \u201C%s\u201D kan niet worden gebruikt met deze versie van CSS : \u201C%s\u201D

# gebruikt door org.w3c.css.properties3.CssGlyphOrVert
error.anglevalue: Waarde moet tussen -360 en 360 zijn en deelbaar zijn door 90

#gebruikt door org.w3c.css.properties3.CssTextKashidaSpace
error.percentage: Waarde moet een percentage zijn

#used by org.w3c.css.properties.cssDirection
warning.direction: het is beter voor block-level elementen de CSS3 eigenschap 'writing-mode' te gebruiken

# wees voorzichtig, waarden komen eerst
# U kunt niet zoiets als dit schrijven: Voor de kleur is blauw een incorrecte waarde
error.value: \u201C%s\u201D geen \u201C%s\u201D waarde

# gebruikt door org.w3c.css.properties.CssTextDecoration
error.same-value: \u201C%s\u201D komt tweemaal voor

error.generic-family.quote: Generieke familienamen zijn sleutelwoorden en moeten daarom tussen aanhalingstekens worden geplaatst

# gebruikt door org.w3c.css.properties.CssClip
error.shape: Ongeldige vormdefinitie rect(<top>,<right>,<bottom>,<left>)
error.shape-separator: Ongeldig scheidingsteken in de vormdefinitie. Dit moet een komma zijn.

# gebruikt door org.w3c.css.properties.CssContent
error.attr: Ongeldige definitie van attr  attr(X)
error.counter: Ongeldige counter definitie counter(<identifier>[,<list-style-type>]?)
error.counters: Ongeldige counters definitie counters(<identifier>,<string>[,<list-style-type>]?)

# gebruikt door org.w3c.css.font.Src
error.format: Ongeldige format definitie format(<string>[,<string>]*)
error.local: Ongeldige format definitie local(<string>|<ident>+)

# gebruikt door org.w3c.css.values.CssAngle, org.w3c.css.values.CssFrequency, org.w3c.css.values.CssTime, org.w3c.css.values.CssLength
error.unit: \u201C%s\u201D is een incorrecte unit

error.unknown: Onbekende error

# gebruikt door org.w3c.css.aural.ACssAzimuth
error.degree: De positie moet worden beschreven in gradaties.

# gebruikt door org.w3c.css.aural.ACssElevation
error.elevation.range: Specificeert de 'elevation' als een hoek, tussen '-90deg' en '90deg'.

# gebruikt door org.w3c.css.aural.ACssPitchRange
error.range: Deze waarde valt buiten het bereik. Deze moet tussen '0' en '100' liggen.

# gebruikt door org.w3c.css.properties.CssTextShadow
error.two-lengths: Een schaduw offset wordt gespecificeerd met twee <length> waarden (Een blur radius kan optioneel worden gespecificeerd na de schaduw offset.)

error.integer: Dit getal moet een integer zijn.
error.comma: Ontbrekende komma als scheidingsteken.

# gebruikt door org.w3c.css.values.CssPercentage
error.percent: \u201C%s\u201D is een incorrect percentage

# gebruikt door org.w3c.css.values.CssString
error.string: \u201C%s\u201D is een incorrecte string

# gebruikt door org.w3c.css.values.CssURL
error.url: \u201C%s\u201D is geen incorrecte URL

# gebruikt door org.w3c.css.values.CssColor
error.rgb: \u201C%s\u201D is geen geldige kleur 3 of 6 hexadecimale getallens
error.angle: \u201C%s\u201D is geen geldige hoek. De waarde moet tussen 0 en 360 liggen

# gebruikt door org.w3c.css.values.CssNumber
error.zero: alleen 0 kan een \u201C%s\u201D zijn. U moet een maat achter uw getal plaatsen
warning.zero: alleen 0 kan een \u201C%s\u201D zijn. U moet een maat achter uw getal plaatsen

# gebruikt door org.w3c.css.parser.CssPropertyFactory
error.noexistence-at-all: Eigenschap \u201C%s\u201D bestaat niet

error.noexistence: Eigenschap \u201C%s\u201D bestaat niet in \u201C%s\u201D maar wel in \u201C%s\u201D
warning.noexistence: Eigenschap \u201C%s\u201D bestaat niet in \u201C%s\u201D maar wel in \u201C%s\u201D

error.noexistence-media: Eigenschap \u201C%s\u201D bestaat niet voor media \u201C%s\u201D
warning.noexistence-media: Eigenschap \u201C%s\u201D bestaat niet voor media \u201C%s\u201D

# gebruikt door org.w3c.css.parser.CssFouffa
error.unrecognize: Te veel waarden of teveel onbekende waarden

# gebruikt door org.w3c.css.parser.CssFouffa
generator.unrecognize: Parse Error
generator.dontmixhtml: Parse Error. Style sheets mogen geen HTML syntax.

# gebruikt door org.w3c.css.parser.CssSelectors
error.pseudo-element: Het pseudo-element \u201C%s\u201D kan niet in deze context voorkomen \u201C%s\u201D
error.pseudo-class: De pseudo-class .\u201C%s\u201D kan niet in deze HTML context voorkomen \u201C%s\u201D
error.pseudo: Onbekend pseudo-element of pseudo-class \u201C%s\u201D
error.id: ID selector #\u201C%s\u201D is ongeldig ! Slechts een ID selector kan worden gespecificeerd in een eenvoudige selector: \u201C%s\u201D.
error.space: Als de attribuut selector ~= wordt gebruikt mag het woord in de waarde \u201C%s\u201D geen spaties bevatten.
error.todo : Sorry de feature \u201C%s\u201D is nog niet geimplementeerd.
error.incompatible: \u201C%s\u201D en \u201C%s\u201D zijn incompatible
warning.incompatible: \u201C%s\u201D en \u201C%s\u201D zijn incompatible

error.media: onbekende media \u201C%s\u201D 
error.page: onbekende pseudo genaamde pagina \u201C%s\u201D


# gebruikt door StyleSheetGeneratorHTML
generator.context: Context
generator.request: Er is een fout opgetreden tijdens de uitvoer van uw style sheet. \
Corrigeer uw verzoek of stuur een mail naar plh@w3.org.
generator.unrecognized: onbekend
generator.invalid-number: Ongeldig getal
generator.property: Ongeldig getal
generator.line: Regel
generator.not-found: Bestand niet gevonden
generator.doc-html:<!-- removed this confusing message olivier 2006-12-14 -->

generator.doc:<!-- removed this confusing message olivier 2006-12-14 -->


# gebruikt door the parser
parser.semi-colon: poging een puntkomma te vinden voor de eigenschapnaam: voeg deze toe

parser.unknown-dimension: Onbekend dimensie.

parser.old_class:In CSS1 kon de naam van een klasse beginnen met een getal (".55ft"), \
tenzij het een dimensie was (".55in"). In CSS2 worden zulke klassen geparsed als \
onbekende dimensies (dit maakt het mogelijk later nieuwe units toe te voegen)

parser.old_id:In CSS1 kon een id beginnen met een getal ("#55ft"), \
tenzij het een dimensie was ("#55in"). In CSS2 worden zulke ids geparsed als \
onbekende dimensies (dit maakt het mogelijk later nieuwe units toe te voegen)

parser.class_dim:In CSS1 kan de naam van een klasse beginnen met een getal (".55ft"), \
tenzij het een dimensie is (".55in").

parser.id_dim:In CSS1 kan een id beginnen met een getal ("#55ft"), \
tenzij het een dimensie is ("#55in").

parser.charset:De @charset-regel mag alleen voorkomen aan het begin van een \
style-sheet. Controleer dat er geen spaties aan voorafgaan.

parser.charsetspecial:Dit profiel heeft een specifieke syntax voor @charset: \
@charset gevolgd door precies een spatie, gevolgd door het type tussen aanhalingstekens \
en een puntkomma.

warning.old_id:In CSS1 kon de naam van een id beginnen met een getal ("#55ft"), \
tenzij het een dimensie is ("#55in"). In CSS2 worden zulke ids geparsed als \
onbekende dimensies (dit maakt het mogelijk later nieuwe units toe te voegen)

warning.old_class:In CSS1 kon de naam van een klasse beginnen met een getal (".55ft"), \
tenzij het een dimensie is (".55in"). In CSS2 worden zulke klassen geparsed als \
onbekende dimensies (dit maakt het mogelijk later nieuwe units toe te voegen)

# gebruikt door de servlet
servlet.invalid-request: U hebt een ongeldig verzoek ingediend.
servlet.process: Kan het object niet verwerken

error.notforcss1 : Waarde \u201C%s\u201D bestaat niet voor CSS1
warning.noothermedium : Eigenschappen voor andere media werken mogelijk niet voor dit gebruikersmedium
warning.notforusermedium : Eigenschap \u201C%s\u201D bestaat niet voor dit gebruikersmedium
error.noatruleyet : Andere @regels dan @import worden niet ondersteund door CSS1 %
error.notformobile : \u201C%s\u201D kan niet worden gebruikt voor het mobile profiel
warning.atsc : \u201C%s\u201D wordt mogelijk niet ondersteund door het medium atsc-tv
error.onlyATSC : deze functie is alleen voor @media atsc-tv

error.unrecognized.link: Onherkenbaar link element of xml-stylesheet PI.

warning.otherprofile : property \u201C%s\u201D bestaat niet in dit profiel, maar is gevalideerd conform een ander profiel

#used by org.w3c.css.parser.analyzer.CssParser
error.nocomb: Combinator \u201C%s\u201D tussen de selectors is niet toegestaan in dit profiel of versie

error.function: Ongeldige functie definitie
warning.charsetspecial: Dit profiel heeft een specifieke syntax voor @charset: @charset, gevolgd door een spatie, gevolgd door de naam van de encoding tussen quotes, direct gevolgd door een puntkomma.
warning.medialist: De medialijst moet starten met 'media :' \u201C%s\u201D
warning.nocomb: Combinatieteken \u201C%s\u201D tussen selectors is niet toegestaan binnen dit profiel (\u201C%s\u201D)
warning.notversion: \u201C%s\u201D kan niet gebruikt worden in deze CSS-versie: \u201C%s\u201D
warning.pseudo: Onbekend pseudo-element of pseudo-class \u201C%s\u201D in het standaard profiel (\u201C%s\u201D)
warning.xsl: waarde \u201C%s\u201D is alleen geldig voor XSL

warning.float-no-width: In (x)HTML+CSS moet de breedte van floated elementen gedeclareerd zijn. Alleen elementen met een intrinsieke breedte (html, img, input, textarea, select, of object) worden hierdoor niet beïnvloed
parser.charsetcss1: De @charset regels zijn niet compatibel met CSS1
parser.attrcss1: De Attribute selectors zijn niet compatibel met CSS1
parser.invalid_id_selector: Niet valide ID selector
parser.import_not_allowed: @import is niet toegestaan na een valide statement anders dan @charset en @import

error.bg_order: In de CSS3 background definitie moet 'bg_position' voor 'bg_size' staan als beide aanwezig zijn

