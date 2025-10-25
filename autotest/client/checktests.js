function checkResults(testrow, resultrow, req) {
    var allTestTds = testrow.getElementsByTagName("td");
    var allTds = resultrow.getElementsByTagName("td");
    
    var validation_res = eval('(' + req.responseText + ')');
    var gotRegression = false;
    
    allTds[0].removeChild(allTds[0].firstChild);
    allTds[0].appendChild(document.createTextNode(validation_res.cssvalidation.validity));
    var isValid = new Boolean(validation_res.cssvalidation.validity);
    var validExpected = (allTestTds[2].innerHTML == "true");
    if (validExpected == validation_res.cssvalidation.validity) {
	allTds[0].setAttribute("class", "valid");
    } else {
	allTds[0].setAttribute("class", "invalid");
	gotRegression = true;
    }

    var errorsExpected = parseInt(allTestTds[3].innerHTML,10);
    allTds[1].innerHTML = validation_res.cssvalidation.result.errorcount;
    if ( errorsExpected == validation_res.cssvalidation.result.errorcount) {
	allTds[1].setAttribute("class", "valid");
    } else {
	allTds[1].setAttribute("class", "invalid");
	gotRegression = true;
    }
    
    var warningsExpected = parseInt(allTestTds[4].innerHTML,10);
    allTds[2].innerHTML = validation_res.cssvalidation.result.warningcount;
    if ( warningsExpected == validation_res.cssvalidation.result.warningcount) {
	allTds[2].setAttribute("class", "valid");
    } else {
	allTds[2].setAttribute("class", "invalid");
	gotRegression = true;
    }
    
    allTestTds[0].setAttribute("class", (gotRegression ? "invalid" : "valid"));
}

function checkURI(testrow, resultrow, encodedURI, context) {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open("GET", "/css-validator/validator?uri="+
		 encodedURI+
		 "&profile="+context.cssprofile+
		 "&usermedium="+context.medium+"&output=json&warning="+context.warninglevel,true);
    xmlhttp.setRequestHeader('Accept','application/json')
    xmlhttp.onreadystatechange=function() {
	if (xmlhttp.readyState==4) {
	    checkResults(testrow, resultrow, xmlhttp);
	}
    }
    xmlhttp.send(null);
//    checkResults(testrow, resultrow, xmlhttp);
}    

function checkTest(testrow, resultrow) {
    var allTds = testrow.getElementsByTagName("td");
    var anchor = allTds[0].getElementsByTagName("a");
    var uri    = urlencode(anchor[0].getAttribute("href"));
    var context = { 'cssprofile': "css21",
        'warninglevel': "1",
	'medium' : "all" };
    context.cssprofile = allTds[1].firstChild.data;
    if (anchor[0].hasAttribute("warning")) {
	context.warninglevel = anchor[0].getAttribute("warning");
    }
    if (anchor[0].hasAttribute("medium")) {
	context.medium = anchor[0].getAttribute("medium");
    }
    checkURI(testrow, resultrow, uri, context)
}

function checkTableTests(tableid) {
    var testTable = document.getElementById(tableid);
    if (testTable) {
	var allTests = testTable.getElementsByTagName('tr')
	for (i=0; i< allTests.length; i++) {
	    var cname = allTests[i].className;
	    if (cname == "expected") {
		checkTest(allTests[i], allTests[i+1]);
	    }
	}
    }
}

function checkAllTests() {
    var controldiv = document.getElementById("controldiv");
    if (controldiv) {
	var allLis = controldiv.getElementsByTagName("li");
	for (var i=0; i<allLis.length; i++) {
	    var a = allLis[i].getElementsByTagName("a");
	    if (a.length > 0) {
		var thref = a[0].getAttribute("href");
		var tableId = thref.substring(1);
		checkTableTests(tableId);
	    }
	}
    }
}

function resetresults(tableid) {
    var testTable = document.getElementById(tableid);
    if (testTable) {
	var allTests = testTable.getElementsByTagName('tr')
	for (var i=0; i< allTests.length; i++) {
	    var cname = allTests[i].className;
	    if (cname == "expected") {
		var allTds = allTests[i].getElementsByTagName("td");
		if (allTds[0].hasAttribute("class")) {
		    allTds[0].removeAttribute("class");
		}
	    } else {
		var allTds = allTests[i].getElementsByTagName("td")
		for (var j=0; j< allTds.length; j++) {
		    if (allTds[j].hasAttribute("class")) {
			allTds[j].removeAttribute("class");
		    }
		}
	    }
	}
    }
}