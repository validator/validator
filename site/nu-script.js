/*
 * @source: https://github.com/validator/validator/blob/main/site/nu-script.js
 *
 * @licstart  The following is the entire license notice for the JavaScript
 * code in this file.
 *
 * Copyright (c) 2005-2007 Henri Sivonen
 * Copyright (c) 2007 Simon Pieters
 * Copyright (c) 2007-2018 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @licend  The above is the entire license notice for the JavaScript code
 * in this file.
 *
 */
var idCount = 0
var urlInput = null
var fileInput = null
var textarea = null
var textareaHidden = null
var dynamicStyle = null
var prevHash = null
var hasTextContent = (createHtmlElement('code').textContent != undefined)

var linePattern = /^#l-?[0-9]+$/
var rangePattern = /^#l-?[0-9]+c[0-9]+$/
var exactPattern = /^#cl-?[0-9]+c[0-9]+$/
var htmlBoilerplate = '<!DOCTYPE html>\n<html lang="">\n<head>\n'
  + '<title>Test</title>\n</head>\n<body>\n<p></p>\n</body>\n</html>'

function boot() {
	installHandlers()
}

function reboot() {
	boot()
	initFieldHolders()
	initUserAgents()
	installDynamicStyle()
	updateFragmentIdHilite()
	window.setInterval(emulateHashChanged, 50)
	replaceYearWarning()
	initFilters()
	injectHyperlinks()
	moveLangAndDirWarningsAndAddLinks()
	replaceSuccessFailure()
}

function installDynamicStyle() {
	dynamicStyle = createHtmlElement('style')
	document.documentElement.firstChild.appendChild(dynamicStyle)
}

function installHandlers() {
	if (document.forms && document.forms.length) {
		document.forms[0].onsubmit = formSubmission
	}
}

function initFieldHolders() {
	urlInput = document.getElementById('doc')
	urlInput.setAttribute('aria-labelledby', 'docselect')
	urlInput.setAttribute('required', '')
	urlInput.setAttribute('placeholder', 'Enter the URL for an HTML, CSS, or SVG document')
	textareaHidden = createHtmlElement('input')
	textarea = createHtmlElement('textarea')
	textarea.setAttribute('autofocus', '')
	textarea.setAttribute('tabindex', '0')
	if (textarea && textareaHidden) {
		textareaHidden.type = 'hidden'
		textareaHidden.name = 'content'
		textarea.cols = 72
		textarea.rows = 15
		textarea.id = 'doc'
		textarea.setAttribute('aria-labelledby', 'docselect')
		copySourceIntoTextArea()
		if (textarea.value == '') {
			textarea.value = htmlBoilerplate
			if (supportsLocalStorage() && localStorage["inputWasCss"] == "yes") {
				textarea.value = ""
			}
		}
	}
	fileInput = createHtmlElement('input')
	if (fileInput) {
		fileInput.type = 'file'
		fileInput.id = 'doc'
		fileInput.name = 'file'
		fileInput.setAttribute('aria-labelledby', 'docselect')
		fileInput.setAttribute('required','')
		fileInput.setAttribute('autofocus','')
		fileInput.setAttribute('tabindex','0')
	}
	var label = document.getElementById("inputlabel");
	label.removeAttribute("for")
	label.textContent = "Check by"
	var modeSelect = createHtmlElement("select")
	modeSelect.id = 'docselect'
	modeSelect.appendChild(createOption('address', ''))
	modeSelect.appendChild(createOption('file upload', 'file'))
	modeSelect.appendChild(createOption('text input', 'textarea'))
	modeSelect.onchange = function() {
	if (this.value == 'file') {
		installFileUpload()
		location.hash = '#file'
	}
	else
		if (this.value == 'textarea') {
			installTextarea()
			location.hash = '#textarea'
		}
		else {
			installUrlInput()
			history.pushState("", document.title, location.pathname);
		}
	}
	label.appendChild(modeSelect)
	if (urlInput.className == 'file') {
		installFileUpload()
		modeSelect.value = 'file'
	} else
		if (urlInput.className == 'textarea') {
		installTextarea()
		modeSelect.value = 'textarea'
	}
	document.querySelector('#show_options')
		.addEventListener('click', function (e) {
			toggleExtraOptions()
		}, false)
	if (location.hash == '#file') {
		installFileUpload()
		modeSelect.value = 'file'
	} else {
		if (location.hash == '#textarea') {
			installTextarea()
			modeSelect.value = 'textarea'
		}
		else {
			installUrlInput()
		}
	}
}

function toggleExtraOptions() {
	var extraoptions = document.querySelector('.extraoptions'),
		extraoptions_useragent = document.querySelector('input[name=useragent]'),
		extraoptions_acceptlanguage = document.querySelector('input[name=acceptlanguage]')
	if (extraoptions.className.indexOf("unhidden") != -1) {
		extraoptions.className = extraoptions.className.replace(/unhidden/, 'hidden')
		extraoptions_useragent.setAttribute("disabled", "")
		extraoptions_acceptlanguage.setAttribute("disabled", "")
	} else {
		extraoptions.className = extraoptions.className.replace(/hidden/, 'unhidden')
		extraoptions_useragent.removeAttribute("disabled")
		extraoptions_acceptlanguage.removeAttribute("disabled")
	}
}

function initUserAgents() {
	var userAgents = document.querySelector("#useragents")
	userAgents.appendChild(createLabeledOption(
		'Mozilla/5.0 (Linux; Android 4.4.2; en-us; SC-04E Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/1.5 Chrome/28.0.1500.94 Mobile Safari/537.36',
		'Android'))
	userAgents.appendChild(createLabeledOption(
		'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36',
		'Chrome'))
	userAgents.appendChild(createLabeledOption(
		'Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0',
		'Firefox'))
	userAgents.appendChild(createLabeledOption(
		'Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko',
		'Internet Explorer'))
	userAgents.appendChild(createLabeledOption(
		'Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25',
		'iPhone/iOS Safari'))
	userAgents.appendChild(createLabeledOption(
		'Mozilla/5.0 (Windows NT 6.3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.89 Safari/537.36 OPR/28.0.1750.48',
		'Opera'))
	userAgents.appendChild(createLabeledOption(
		'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/600.4.10 (KHTML, like Gecko) Version/8.0.4 Safari/600.4.10',
		'Safari'))
	userAgents.appendChild(createLabeledOption(
		'Validator.nu/LV',
		'default'))
	document.querySelector("span.extraoptions").appendChild(userAgents)
	document.querySelector("input[list=useragents]").setAttribute("disabled", "")
	document.querySelector('input[name=acceptlanguage]').setAttribute("disabled", "")
}

function createLabeledOption(value, label) {
	var rv = createHtmlElement('option')
	rv.value = value
	rv.label = label
	return rv
}

function createOption(text, value) {
	var rv = createHtmlElement('option')
	rv.value = value
	var tn = document.createTextNode(text)
	rv.appendChild(tn)
	return rv
}

function formSubmission() {
	if (document.getElementsByTagName) {
		var form = document.getElementsByTagName("form")[0]
		if (form.checkValidity) {
			if (!form.checkValidity()) {
				return true
			}
		}
	}
	disableByIdIfEmptyString("doc")
	if (textareaHidden && textarea) {
	  textareaHidden.value = textarea.value
	}
	return true
}

function undoFormSubmission() {
	enableById("doc")
	return true
}

function disableById(id) {
	var field = document.getElementById(id)
	if (field) {
		field.disabled = true
	}
}

function disableByIdIfEmptyString(id) {
	var field = document.getElementById(id)
	if (field) {
		if ("" == field.value) {
			field.disabled = true
		}
	}
}

function enableById(id) {
	var field = document.getElementById(id)
	if (field) {
		field.disabled = false
	}
}

function createHtmlElement(tagName) {
	return document.createElementNS ? document.createElementNS("http://www.w3.org/1999/xhtml", tagName) : document.createElement(tagName)
}

function injectHyperlinks() {
	var errors = document.getElementsByClassName("error")
	var warnings = document.getElementsByClassName("warning")
	linkify(warnings, "file an issue report",
		"https://github.com/validator/validator/issues/new",
		"File a report in the GitHub issue tracker.")
	linkify(warnings, "checking against the HTML5 + RDFa 1.1 schema",
		"about.html#rdfa",
		"About HTML5 + RDFa 1.1 checking.")
	linkify(warnings, "are treated as top-level headings by many screen readers and other tools",
		"https://html5accessibility.com/stuff/2021/07/08/preserved-in-html/",
		"HTML heading usage and tools support")
	linkify(warnings, "add identifying headings to all sections",
		"https://www.w3.org/wiki/HTML/Usage/Headings/Missing",
		"Identifying section elements with headings")
	linkify(warnings, "add identifying headings to all articles",
		"https://www.w3.org/wiki/HTML/Usage/Headings/Missing",
		"Identifying article elements with headings")
	linkify(errors, "the Media Types section in the current Media Queries specification",
		"https://drafts.csswg.org/mediaqueries/#media-types",
		"Media Types sections in the Media Queries specification")
	linkify(errors, "the Deprecated Media Features section in the current Media Queries specification",
		"https://drafts.csswg.org/mediaqueries/#mf-deprecated",
		"Deprecated Media Features section in the current Media Queries specification")
	linkify(errors, "all image candidate strings must specify a width",
		"https://ericportis.com/posts/2014/srcset-sizes/",
		"srcset and sizes overview")
	}

function replaceSuccessFailure() {
	successfailure = document.querySelector(".success, .failure")
	if (successfailure === null) return
	if (document.querySelector(".non-document-error") !== null) {
		successfailure.className = "fatalfailure"
		successfailure.textContent = "Document checking not completed."
		successfailure.textContent += " The result cannot be determined due to a non-document-error."
	} else if (document.querySelector(".error:not(.hidden), .warning:not(.hidden)") !== null) {
		successfailure.className = "failure"
		successfailure.textContent = "Document checking completed."
	} else {
		successfailure.className = "success"
		successfailure.textContent = "Document checking completed. No errors or warnings to show."
	}
	if (document.querySelector("#results > ol:first-child") !== null) {
		if (document.querySelector("#results > ol:first-child li:not(.hidden)") === null) {
			document.querySelector("#results > ol:first-child").className = "hidden"
		} else {
			document.querySelector("#results > ol:first-child").className = ""
		}
	}
	// replace empty <ol> artifacts in the outline caused by <hgroup>
	var emptyOls = document.querySelectorAll("#outline ol:empty")
	for (var i = 0; i < emptyOls.length; i++) {
		emptyOls[i].remove()
	}

}

function replaceYearWarning() {
	var warnings = document.querySelectorAll(".warning")
	for (var i = 0; i < warnings.length; ++i) {
		warnings[i].firstChild.lastChild.lastChild.textContent =
		warnings[i].firstChild.lastChild.lastChild.textContent
		.replace(/Year may be mistyped.*/, "Year may be mistyped.")
	}
}

function linkify(messages, text, target, title) {
	if (!messages) return
	for (var i = 0; i < messages.length; ++i) {
		messages[i].firstChild.lastChild.innerHTML =
		messages[i].firstChild.lastChild.innerHTML.replace(text,
		"<a href='" + target + "' title='" + title + "'>" + text + "</a>");
	}
}

function moveLangAndDirWarningsAndAddLinks() {
	var warnings = document.getElementsByClassName("warning")
	var messagesContainer = document.querySelector("#results > ol:first-child")
	var langOrDirWarningText = "This document appears to be written in"
	var undetectedMissingLang= "Consider adding a lang attribute"
	var contentLanguageText = "The value of the HTTP Content-Language header is"
	var langTextWithNoLangGuidance = 'For further guidance, consult <a href="https://www.w3.org/International/questions/qa-no-language#nonlinguistic">Tagging text with no language</a>, <a href="https://www.w3.org/International/techniques/authoring-html.en?open=language&open=textprocessing#textprocessing">Declaring the overall language of a page</a> and <a href="https://www.w3.org/International/techniques/authoring-html.en?open=language&open=langvalues#langvalues">Choosing language tags</a>.'
	var langGuidance = 'For further guidance, consult <a href="https://www.w3.org/International/techniques/authoring-html.en?open=language&open=textprocessing#textprocessing">Declaring the overall language of a page</a> and <a href="https://www.w3.org/International/techniques/authoring-html.en?open=language&open=langvalues#langvalues">Choosing language tags</a>.'
	var contentLangGuidance = 'For further guidance, consult <a href="https://www.w3.org/International/questions/qa-http-and-lang">HTTP headers, meta elements and language information</a>.'
	var dirGuidance = 'For further guidance, consult <a href="https://www.w3.org/International/questions/qa-html-dir">Structural markup and right-to-left text in HTML</a> and <a href="https://www.w3.org/International/techniques/authoring-html#using">Setting up a right-to-left page</a>.'
	var ifMisidentifiedGuidance = 'If the HTML checker has misidentified the language of this document, please <a href="https://github.com/validator/validator/issues/new">file an issue report</a> or <a href="mailto:www-validator@w3.org">send e-mail to report the problem</a>.'
	var langOrDirWarning
	var langOrDirLinks
	var ifMisidentifiedLinks
	var warningText
	for (var i = 0; i < warnings.length; ++i) {
		warningText = warnings[i].firstChild.lastChild.textContent
		if (warningText.indexOf(langOrDirWarningText) != -1 || warningText.indexOf(undetectedMissingLang) != -1) {
			langOrDirWarning = warnings[i]
			langOrDirLinks = document.createElement("p")
			if (warningText.indexOf("written in Lorem ipsum text") != -1) {
				warnings[i].firstChild.lastChild.innerHTML
					= warnings[i].firstChild.lastChild.innerHTML.replace(/written in/, "")
				warnings[i].firstChild.lastChild.innerHTML
					= warnings[i].firstChild.lastChild.innerHTML.replace(/Lorem ipsum/, "<i>Lorem ipsum</i>")
				langOrDirLinks.innerHTML = langTextWithNoLangGuidance
			} else if (warningText.indexOf("lang=") != -1 || warningText.indexOf(undetectedMissingLang) != -1) {
				langOrDirLinks.innerHTML = langGuidance
			} else if (warningText.indexOf("Content-Language") != -1) {
				langOrDirLinks.innerHTML = contentLangGuidance
			} else if (warningText.indexOf("dir=") != 1) {
				langOrDirLinks.innerHTML = dirGuidance
			}
			langOrDirWarning.appendChild(langOrDirLinks)
			ifMisidentifiedLinks = document.createElement("p")
			ifMisidentifiedLinks.setAttribute("class", "reportbug")
			ifMisidentifiedLinks.innerHTML = ifMisidentifiedGuidance
			langOrDirWarning.appendChild(ifMisidentifiedLinks)
			messagesContainer.insertBefore(langOrDirWarning, messagesContainer.firstChild)
		} else if (warningText.indexOf(contentLanguageText) != -1) {
			langOrDirWarning = warnings[i]
			langOrDirLinks = document.createElement("p")
			langOrDirLinks.innerHTML = contentLangGuidance
			langOrDirWarning.appendChild(langOrDirLinks)
			messagesContainer.insertBefore(langOrDirWarning, messagesContainer.firstChild)
		}
	}
}

function reflow(element) {
	if (element.offsetHeight) {
		var reflow = element.offsetHeight
	}
}

function installTextarea() {
	var input = document.getElementById('doc')
	var inputRegion = document.getElementById("inputregion")
	if (input && textarea) {
		var form = document.forms[0]
		if (form) {
			form.method = 'post'
			form.enctype = 'multipart/form-data'
			if (document.getElementById("xnote")) {
				input.parentNode.removeChild(document.getElementById("xnote"))
			}
			inputRegion.removeChild(input)
			if (!document.getElementById("csslabel")) {
				var cssLabel = document.createElement("label")
				cssLabel.setAttribute("id", "csslabel")
				cssLabel.setAttribute("title", "Treat the input as CSS.")
				cssLabel.setAttribute("for", "css")
				var cssCheckbox = document.createElement("input")
				cssCheckbox.setAttribute("type", "checkbox")
				cssCheckbox.setAttribute("name", "css")
				cssCheckbox.setAttribute("id", "css")
				cssCheckbox.setAttribute("value", "yes")
				if (supportsLocalStorage() && localStorage["inputWasCss"] == "yes") {
					cssCheckbox.checked = true
				}
				cssCheckbox.addEventListener("change", function(e) {
					if (document.getElementById('doc').value == htmlBoilerplate
							&& e.target.checked) {
						document.getElementById('doc').value = ""
					}
					if (document.getElementById('doc').value == ""
							&& !e.target.checked) {
						document.getElementById('doc').value = htmlBoilerplate
					}
					if (supportsLocalStorage()) {
						if (e.target.checked) {
							localStorage["inputWasCss"] = "yes"
						} else {
							localStorage["inputWasCss"] = "no"
						}
					}
				}, false)
				cssLabel.appendChild(cssCheckbox)
				cssLabel.appendChild(document.createTextNode("CSS"))
				inputRegion.appendChild(cssLabel)
			}
			inputRegion.appendChild(textarea)
			reflow(textarea)
		}
	}
	if (textareaHidden) {
	  var submit = document.getElementById("submit")
	  if (submit) {
	    submit.parentNode.appendChild(textareaHidden)
	  }
	}
	var showSource = document.getElementById("showsource")
	if (showSource) {
		showSource.checked = true
	}
}

function installFileUpload() {
	var input = document.getElementById('doc')
	if (input && fileInput) {
		var form = document.forms[0]
		if (form) {
			if (document.getElementById("csslabel")) {
				input.parentNode.removeChild(document.getElementById("csslabel"))
			}
			form.method = 'post'
			form.enctype = 'multipart/form-data'
			input.parentNode.replaceChild(fileInput, input)
			if (!document.querySelector("#xnote")) {
				var xnote = document.createElement("div")
				xnote.setAttribute('id', 'xnote')
				xnote.appendChild(document.createTextNode(
					"Uploaded files with .xhtml or .xht"
					+ " extensions are parsed using"
					+ " the XML parser."))
				document.getElementById("inputregion")
					.appendChild(xnote)
			}
			reflow(fileInput)
		}
	}
	if (textareaHidden && textareaHidden.parentNode) {
	  textareaHidden.parentNode.removeChild(textareaHidden)
	}
}

function installUrlInput() {
	var input = document.getElementById('doc')
	if (input && urlInput) {
		var form = document.forms[0]
		if (form) {
			form.method = 'get'
			form.enctype = ''
			if (document.getElementById("xnote")) {
				input.parentNode.removeChild(document.getElementById("xnote"))
			}
			if (document.getElementById("csslabel")) {
				input.parentNode.removeChild(document.getElementById("csslabel"))
			}
			input.parentNode.replaceChild(urlInput, input)
			reflow(urlInput)
		}
	}
	if (textareaHidden && textareaHidden.parentNode) {
	  textareaHidden.parentNode.removeChild(textareaHidden)
	}
}

function copySourceIntoTextArea() {
	if (document.getElementById('source') === null) {
		return
	}
	var strings = []
	var source = document.getElementById('source').nextSibling;
	if (source && textarea) {
		var li = source.firstChild
		while (li) {
			var code = li.firstChild
			if (code == null) {
				return
			}
			if (hasTextContent) {
				strings.push(code.textContent)
			}
			else {
				strings.push(code.innerText)
			}
			li = li.nextSibling
		}
		textarea.value = strings.join('\n')
	}
}

function updateFragmentIdHilite() {
	var fragment = window.location.hash
	var selector = null
	if (linePattern.exec(fragment)) {
		selector = fragment + " *"
	} else if (exactPattern.exec(fragment)) {
		selector = fragment
	} else if (rangePattern.exec(fragment)) {
		selector = "html b." + fragment.substring(1)
	}
	var rule = ''
	if (selector) {
		rule = selector + " { background-color: #ffcccc; font-weight: bold; }"
	}
	var newStyle = createHtmlElement('style')
	var ex
	try {
		newStyle.appendChild(document.createTextNode(rule))
	} catch (ex) {
		if (ex.number == -0x7FFF0001) {
			newStyle.styleSheet.cssText = rule
		} else {
			throw ex
		} 
	}
	dynamicStyle.parentNode.replaceChild(newStyle, dynamicStyle)
	dynamicStyle = newStyle
}

function emulateHashChanged() {
	var hash = window.location.hash
	if (prevHash != hash) {
		updateFragmentIdHilite()
		prevHash = hash
	}
}

if (document.getElementById) {
	window.onload = reboot
	if (document.addEventListener) {
		document.addEventListener("DOMContentLoaded", function() {
			window.onload = undefined
			reboot()
			setTimeout(function () {
				var filtersbutton = document.querySelector("#filters button")
				var helptext = document.querySelector("#filters > div")
				if (filtersbutton) {
					filtersbutton.className = "message_filtering"
					if (!window.location.hash) {
						filtersbutton.focus()
					}
					filtersbutton.setAttribute('tabindex', '0')
				}
				if (helptext) {
					helptext.className = "message_filtering"
				}
			}, 500);
		}, false)
	}
	window.onunload = undoFormSubmission
	window.onabort = undoFormSubmission
	boot()
}

function initFilters() {
	var errors,
		warnings,
		info,
		filters,
		helptext,
		heading,
		filtersButton,
		makeFieldset,
		fieldsets,
		fieldset,
		legend,
		toggleFilters,
		checkboxes,
		links,
		messageCollection,
		className,
		links,
		mainForm

	if (!document.getElementsByClassName || !document.querySelectorAll) {
		return
	}
	if (document.getElementsByClassName('non-document-error').length > 0) {
		replaceSuccessFailure()
		return
	}
	if (document.getElementsByTagName('ol').length < 1) {
		// If there's no <ol> on the page, then we have no
		// messages to filter.
		return
	}

	helptext = document.querySelector("#filters > div")
	errors = document.getElementsByClassName("error")
	warnings = document.getElementsByClassName('warning')
	info = document.querySelectorAll('[class=info]')
	filters = document.createElement("section")
	filters.id = "filters"
	filters.className = "unexpanded"
	heading = document.createElement("h2")
	helptext = document.createElement("div")
	helptext.appendChild(document.createTextNode("Use the Message Filtering button below to hide/show particular messages, and to see total counts of errors and warnings."))
	filters.appendChild(helptext)
	filtersButton = document.createElement("button")
	filtersButton.appendChild(document.createTextNode("Message Filtering"))
	heading.appendChild(filtersButton)
	filters.appendChild(heading)
	filtersButton.addEventListener('click', function (e) {
	 	toggleFilters()
	}, false)

	// Generate errors/warnings/info fieldsets
	makeFieldset = function(messages, displayType) {
		var fieldset,
			legend,
			hide,
			show,
			messageList,
			messageGroupList,
			checkbox,
			listitem,
			hidegroup,
			showgroup,
			message,
			messagesObject = {},
			messagesSorted = [],
			type = displayType.toLowerCase(),
			messageGroup,
			uniqueMessage,
			makeCheckbox

		makeCheckbox = function(messageName, messageCollection) {
			var checkbox, label, listitem,
				messageSpan = document.getElementById(messageCollection[0]).getElementsByTagName("p")[0].getElementsByTagName("span")[0].cloneNode(true)

			checkbox = document.createElement("input")
			checkbox.type = "checkbox"
			checkbox.checked = "checked"
			checkbox.vnuMessageName = messageName
			checkbox.vnuMessageCollection = messageCollection
			checkbox.vnuMessageType = type
			label = document.createElement("label")
			label.appendChild(checkbox)
			label.appendChild(messageSpan)
			if (messageCollection.length > 1) {
				label.appendChild(document.createTextNode(' (' + messageCollection.length.toString() + ')'))
			}

			// Restore saved checkbox value from local storage
			if (supportsLocalStorage()) {
				if (localStorage.hasOwnProperty(type + ':' + messageName) && localStorage[type + ':' + messageName] === 'false') {
					checkbox.checked = false
					for (var i = 0; i < messageCollection.length; ++i) {
						document.getElementById(messageCollection[i]).className += ' hidden'
					}
				}
			}

			listitem = document.createElement("li")
			listitem.appendChild(label)
			return listitem
		}

		if (messages.length > 0) {

			// Find the unique messages
			for (var i = 0; i < messages.length; ++i) {
				message = messages[i]
				messageClone = messages[i].cloneNode(true)
					uniqueMessage = messageClone.getElementsByTagName('p')[0].getElementsByTagName('span')[0].textContent
					messageGroupEl = messageClone.getElementsByTagName('p')[0].getElementsByTagName('span')[0].cloneNode(true)
					messageGroupElCode = messageGroupEl.getElementsByTagName("code")
					for (var j = 0; j < messageGroupElCode.length; ++j) {
						messageGroupElCode[j].textContent = "___"
						if (messageGroupElCode[j].parentNode instanceof HTMLAnchorElement) {
							messageGroupElCode[j].parentNode.removeAttribute("href")
						}
					}
					messageGroup = messageGroupEl.textContent
					messageGroupNode = messageGroupEl

				if (!messages.hasOwnProperty(messageGroup)) {
					messages[messageGroup] = {
						messageCollection: [],
						uniqueMessages: {},
						uniqueMessagesLength: 0,
						messageGroupNode: messageGroupNode
					}
				}
				messages[messageGroup].messageCollection.push(message)

				if (!messages[messageGroup].uniqueMessages.hasOwnProperty(uniqueMessage)) {
					messages[messageGroup].uniqueMessages[uniqueMessage] = []
					messages[messageGroup].uniqueMessagesLength += 1
				}
				var id = "vnuId" + idCount
				message.id = id
				messages[messageGroup].uniqueMessages[uniqueMessage].push(id)
				idCount++
			}

			// Generate Hide/Show All buttons
			fieldset = document.createElement("fieldset")
			fieldset.className = "hidden"
			legend = document.createElement("legend")
			legend.appendChild(document.createTextNode(displayType + " (" + messages.length + ") · "))
			hide = document.createElement("a")
			hide.href = ""
			show = hide.cloneNode(true)
			hide.className = "hide"
			show.className = "show"
			hide.appendChild(document.createTextNode("Hide all " + type))
			show.appendChild(document.createTextNode("Show all " + type))
			legend.appendChild(hide)
			legend.appendChild(document.createTextNode(" · "))
			legend.appendChild(show)
			fieldset.appendChild(legend)

			messageList = document.createElement("ol")
			for (messageGroup in messages) {
				if (messages.hasOwnProperty(messageGroup)) {
					messageGroupList = document.createElement("ol")
					for (uniqueMessage in messages[messageGroup].uniqueMessages) {
						if (messages[messageGroup].uniqueMessages.hasOwnProperty(uniqueMessage)) {
							var box = makeCheckbox(uniqueMessage, messages[messageGroup].uniqueMessages[uniqueMessage])
							if (messages[messageGroup].uniqueMessagesLength === 1) {
								messageList.appendChild(box)
							} else {
								messageGroupList.appendChild(box)
							}
						}
					}

					if (messages[messageGroup].uniqueMessagesLength > 1) {
						listitem = document.createElement("li")
						listitem.appendChild(messages[messageGroup].messageGroupNode)
						listitem.appendChild(document.createTextNode(' (' + messages[messageGroup].messageCollection.length.toString() + ') · '))
						hidegroup = document.createElement("a")
						hidegroup.href = ""
						showgroup = hidegroup.cloneNode(true)
						hidegroup.className = "hide"
						showgroup.className = "show"
						hidegroup.appendChild(document.createTextNode("Hide all"))
						showgroup.appendChild(document.createTextNode("Show all"))
						listitem.appendChild(hidegroup)
						listitem.appendChild(document.createTextNode(" · "))
						listitem.appendChild(showgroup)
						listitem.appendChild(messageGroupList)
						messageList.appendChild(listitem)
					}
				}
			}

			fieldset.appendChild(messageList)
			filters.appendChild(fieldset)
		}
	}

	showCount = function() {
		var count = document.querySelectorAll("body ol > li.hidden"),
			span
		span = document.querySelector(".filtercount")
		if (span) {
			span.parentNode.removeChild(span)
		}
		if (count.length > 0) {
			span = document.createElement("span")
			span.className = "filtercount"
			span.appendChild(document.createTextNode(count.length.toString() + (count.length === 1 ? " message" : " messages") + " hidden by filtering"))
			heading.appendChild(document.createTextNode(" "))
			heading.appendChild(span)
		}
		replaceSuccessFailure()
	}

	makeFieldset(errors, 'Errors')
	makeFieldset(warnings, 'Warnings')
	makeFieldset(info, 'Info messages')
	showCount()

	mainForm = document.getElementsByTagName("form")[0]
	mainForm.parentNode.insertBefore(filters, mainForm.nextSibling)
	document.querySelector("*[autofocus]").removeAttribute("autofocus")
	document.querySelector("*[tabindex]").removeAttribute("tabindex")
	fieldsets = filters.getElementsByTagName("fieldset")

	toggleFilters = function() {
		if (helptext.className === "expanded") {
			helptext.className = "message_filtering"
			helptext.textContent = "Use the Message Filtering button below to display options for hiding/showing particular messages, and to see total counts of errors and warnings."
		} else {
			helptext.className = "expanded"
			helptext.textContent = "Press the Message Filtering button to collapse the filtering options and error/warning/info counts."
		}
		filters.className === "expanded" ? filters.className = "unexpanded" : filters.className = "expanded"
		for (var i = 0; i < fieldsets.length; ++i) {
			fieldset = fieldsets[i]
			fieldset.className === "hidden" ? fieldset.className = "unhidden" : fieldset.className = "hidden"
		}
	}

	// Show/hide the messages when the checkboxes are toggled
	checkboxes = document.getElementById("filters").getElementsByTagName("input")
	for (var i = 0; i < checkboxes.length; ++i) {
		checkboxes[i].addEventListener("change", function(e) {
			messageCollection = e.target.vnuMessageCollection
			for (var j = 0; j < messageCollection.length; ++j) {
				className = document.getElementById(messageCollection[j]).className
				if (e.target.checked) {
					document.getElementById(messageCollection[j]).className = className.replace(/\s*hidden\s*/g, "")
				} else {
					document.getElementById(messageCollection[j]).className += ' hidden'
				}
				if (supportsLocalStorage()) {
					localStorage[e.target.vnuMessageType + ':' + e.target.vnuMessageName] = e.target.checked
				}
			}
			showCount()
		}, false)
	}

	links = document.getElementsByClassName("hide")
	for (var n = 0; n < links.length; ++n) {
		links[n].addEventListener("click", function(e) {
			if (e.target.className !== "hide") {
				return
			}
			e.preventDefault()
			var boxes
			if (e.target.parentNode.getElementsByTagName("ol").length > 0) {
				// If this item has <ol> descendants, then
				// it must be a message group, in which
				// case we go back up the tree to its
				// parent, which is an <li>, and get the
				// checkboxes descendants of that.
				boxes = e.target.parentNode.getElementsByTagName("input")
			} else {
				// If this item has no <ol> descendants, then
				// it must not be a message group, in which
				// case we go back up the tree to its
				// parent's parent, which is a <fieldset>,
				// and get the checkboxes descendants of that.
				boxes = e.target.parentNode.parentNode.getElementsByTagName("input")
			}
			for (var i = 0; i < boxes.length; ++i) {
				var box = boxes[i]
				box.removeAttribute("checked")
				box.checked = false
				messageCollection = box.vnuMessageCollection
				for (var j = 0; j < messageCollection.length; ++j) {
					if (document.getElementById(messageCollection[j])) {
						document.getElementById(messageCollection[j]).className += ' hidden'
					}
				}
				if (supportsLocalStorage()) {
					localStorage[box.vnuMessageType + ':' + box.vnuMessageName] = false
				}
			}
			showCount()
		}, false)
	}

	links = document.getElementsByClassName("show")
	for (var n = 0; n < links.length; ++n) {
		links[n].addEventListener("click", function(e) {
			if (e.target.className !== "show") {
				return
			}
			e.preventDefault()
			var boxes
			if (e.target.parentNode.getElementsByTagName("ol").length > 0) {
				// If this item has <ol> descendants, then
				// it must be a message group, in which
				// case we go back up the tree to its
				// parent, which is an <li>, and get the
				// checkboxes descendants of that.
				boxes = e.target.parentNode.getElementsByTagName("input")
			} else {
				// If this item has no <ol> descendants, then
				// it must not be a message group, in which
				// case we go back up the tree to its
				// parent's parent, which is a <fieldset>,
				// and get the checkboxes descendants of that.
				boxes = e.target.parentNode.parentNode.getElementsByTagName("input")
			}
			for (var i = 0; i < boxes.length; ++i) {
				var box = boxes[i]
				box.checked = true
				messageCollection = box.vnuMessageCollection
				for (var j = 0; j < messageCollection.length; ++j) {
					if (document.getElementById(messageCollection[j])) {
						var className = document.getElementById(messageCollection[j]).className
						document.getElementById(messageCollection[j]).className = className.replace(/\s*hidden\s*/g, "")
					}
				}
				if (supportsLocalStorage()) {
					localStorage[box.vnuMessageType + ':' + box.vnuMessageName] = true
				}
			}
			showCount()
		}, false)
	}
}

function supportsLocalStorage() {
	try {
		return 'localStorage' in window && window['localStorage'] !== null
	} catch (e) {
		return false
	}
}
