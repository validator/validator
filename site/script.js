/*
 * Copyright (c) 2005, 2006, 2007 Henri Sivonen
 * Copyright (c) 2007 Mozilla Foundation
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
 */

function boot() {
	schemaChanged()
}

function schemaChanged() {
	if (document.getElementById) {
		var input = document.getElementById("schema")
		var select = document.getElementById("preset")
		if (input) {
			var value = input.value
			if (select) {
				if (select.firstChild) {
					for (var n = select.firstChild; n != null; n = n.nextSibling) {
						if (n.value == value) {
							n.selected = true
							toggleParsers(value)
							return
						}
					}
					select.firstChild.selected = true
					toggleParsers("")
				}
			}
		}
	}
}

function presetChanged() {
	if (document.getElementById) {
		var input = document.getElementById("schema")
		var select = document.getElementById("preset")
		if (input && select) {
			input.value = select.value
		}
	}
	toggleParsers(select.value)
}

function toggleParsers(newValue) {
	if (document.getElementById) {
		var preset = document.getElementById("preset")
		if (preset) {
			if (isHtmlCompatiblePreset(newValue)) {
				var select = document.getElementById("parser")
				if (select) {
					if (select.firstChild) {
						for (var n = select.firstChild; n != null; n = n.nextSibling) {
							n.disabled = false
						}
					}
				}							
			} else {
				var select = document.getElementById("parser")
				if (select) {
					if (select.firstChild) {
						if (select.firstChild.selected) {
							select.firstChild.nextSibling.selected = true
						}
						for (var n = select.firstChild; n != null; n = n.nextSibling) {
							n.disabled = (n.value == "" || isHtmlParserValue(n.value))
						}
					}
				}							
			}
		}
	}	
}

function isHtmlParserValue(parser) {
	return (parser.indexOf("html") == 0)
}

function isHtmlCompatiblePreset(preset) {
	return (  preset == ""
	       || preset.indexOf("http://hsivonen.iki.fi/xhtml-schema/xhtml-basic.rng") == 0
	       || preset.indexOf("http://hsivonen.iki.fi/xhtml-schema/xhtml-strict.rng") == 0
	       || preset.indexOf("http://hsivonen.iki.fi/xhtml-schema/xhtml-strict-wcag.rng") == 0
	       || preset.indexOf("http://hsivonen.iki.fi/xhtml-schema/xhtml-transitional.rng") == 0
	       || preset.indexOf("http://hsivonen.iki.fi/xhtml-schema/xhtml-transitional-wcag.rng") == 0
	       || preset.indexOf("http://syntax.whattf.org/relaxng/html5full.rnc") == 0
	       )
}

function parserChanged() {
	if (document.getElementById) {
		var parser = document.getElementById("parser")
		if (parser) {
			if (isHtmlParserValue(parser.value)) {
				var select = document.getElementById("preset")
				if (select) {
					if (select.firstChild) {
						for (var n = select.firstChild; n != null; n = n.nextSibling) {
							n.disabled = !isHtmlCompatiblePreset(n.value)
						}
					}
				}
			} else {
				var select = document.getElementById("preset")
				if (select) {
					if (select.firstChild) {
						for (var n = select.firstChild; n != null; n = n.nextSibling) {
							n.disabled = false
						}
					}
				}			
			}
		}
	}
}

function formSubmission() {
	if (document.getElementById) {
		if (document.getElementsByTagName) {
			var form = document.getElementsByTagName("form")[0]
			if (form.checkValidity) {
				if (!form.checkValidity()) {
					return true
				}
			}
		}
		var submit = document.getElementById("submit")
		if (submit) {
			submit.disabled = true
		}
		var preset = document.getElementById("preset")
		if (preset) {
			preset.disabled = true
		}
		var parser = document.getElementById("parser")
		if (parser) {
			if ("" == parser.value) {
				parser.disabled = true
			}
		}
		var schema = document.getElementById("schema")
		if (schema) {
			if ("" == schema.value) {
				schema.disabled = true
			}
		}
		return true
	}
}

