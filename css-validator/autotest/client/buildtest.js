function getTestList(uri) {
    var xmlhttp = new XMLHttpRequest();
//    xmlhttp.open("GET", "/css-validator/autotest/testsuite/xml/"+
//		 "bugs.xml",false);
    xmlhttp.open("GET", uri, false);
    xmlhttp.setRequestHeader('Cache-Control','no-cache')
    //    xmlhttp.setRequestHeader('Accept','application/json')
    //   xmlhttp.onreadystatechange=function() {
    //	if (xmlhttp.readyState==4) {
    //	    checkResults(testrow, resultrow, xmlhttp);
    //	}
    //    }
    xmlhttp.send(null);
    generateTestTable(xmlhttp);
}

// generate the table with the first row
// parameter the id of the table created
// the table is as follow:
// <table id="tableid">
//   <tr>
//     [0] <th>Test Case</th>
//     [1] <th>Profile</th>
//     [2] <th>Validity</th> (expected)
//     [3] <th>Errors</th> (int) (FIXME or "-" ? ) (expected)
//     [4] <th>Warnings</th> (int, expected).
//   </tr>
function createTableFromID(tableid) {
    var table = document.createElement("table");
    table.setAttribute("id", tableid);
    table.setAttribute("border", "2");
    
    var tbody =  document.createElement("tbody");
    
    var tr = document.createElement("tr");
    // first row
    var cell =  document.createElement("th");
    var tnode =  document.createTextNode("Test Case: "+tableid+" ");
    cell.appendChild(tnode);
    var anc = document.createElement("a");
    anc.setAttribute("href", "javascript:checkTableTests('"+tableid+"')");
    tnode =  document.createTextNode("[check]");
    anc.appendChild(tnode);
    cell.appendChild(anc);
    tnode =  document.createTextNode(" ");
    cell.appendChild(tnode);
    anc = document.createElement("a");
    anc.setAttribute("href", "javascript:resetresults('"+tableid+"')");
    tnode =  document.createTextNode("[reset]");
    anc.appendChild(tnode);
    cell.appendChild(anc);
    tr.appendChild(cell);

    cell =  document.createElement("th");
    tnode =  document.createTextNode("Profile");
    cell.appendChild(tnode);
    tr.appendChild(cell);

    cell =  document.createElement("th");
    tnode =  document.createTextNode("Validity");
    cell.appendChild(tnode);
    tr.appendChild(cell);

    cell =  document.createElement("th");
    tnode =  document.createTextNode("Errors");
    cell.appendChild(tnode);
    tr.appendChild(cell);

    cell =  document.createElement("th");
    tnode =  document.createTextNode("Warnings");
    cell.appendChild(tnode);
    tr.appendChild(cell);
    tbody.appendChild(tr);
    table.appendChild(tbody);

    return table;
}

function generateLinkToTable(tableId) {
    var controldiv = document.getElementById("controldiv");
    if (controldiv) {
	var divul = controldiv.getElementsByTagName("ul");
	if (divul.length > 0) {
	    divul = divul[0];
	} else {
	    divul = document.createElement("ul");
	    controldiv.appendChild(divul);
	}
	var li = document.createElement("li");
	var anc = document.createElement("a");
	anc.setAttribute("href", "#"+tableId);
	var tnode = document.createTextNode(tableId);
	anc.appendChild(tnode);
	li.appendChild(anc);
	divul.appendChild(li);
    }
}

function getTestTable(typetag) {
    var tableId = "default";
    if (typetag) {
	tableId = typetag.getAttribute("title");
    }
    var table = document.getElementById(tableId);
    if (!table) {
	table = createTableFromID(tableId);
	var tableanchor = document.getElementById("tableanchor");
	tableanchor.appendChild(table);
	generateLinkToTable(tableId);
    }
    // and attach the newly created table at the right place
    var tbody = table.getElementsByTagName("tbody");
    if (tbody) {
	return tbody[0];
    }
    return null;
}

function generateTestTable(req) {
    var testxml = req.responseXML;
    var docuri = testxml.documentURI;
    if (!docuri) {
	docuri = testxml.URL; // damn Safari...
    }
    var allTypes = testxml.getElementsByTagName("type");
    // for each <type title="foo"> get the table, find test
    // and add them
    for (var i=0; i<allTypes.length; i++) {
	var testTable = getTestTable(allTypes[i]);
	var allTests = allTypes[i].getElementsByTagName("test");
	fillTableWithTests(docuri, testTable, allTests);
    }
}

function fillTableWithTests(baseuri, table, allTests) {
    var base_uri = baseuri.substring(0, baseuri.lastIndexOf("css-validator/autotest")) + "css-validator/autotest";
    var validator_uri = baseuri.substring(0, baseuri.lastIndexOf("css-validator/autotest")) + "css-validator/"
    for (var i=0; i<allTests.length; i++) {
	var indivTest = allTests[i];
	// check the profile
	var cssprofile = "css21";
	if (indivTest.hasAttribute("profile")) {
	    cssprofile = indivTest.getAttribute("profile");
	}
        // check the warning level
        var warningLevel = 1;
        if (indivTest.hasAttribute("warning")) {
            warningLevel = indivTest.getAttribute("warning");
        }
        // check medium
        var medium = "all";
        if (indivTest.hasAttribute("medium")) {
            medium = indivTest.getAttribute("medium");
        }
	// and the test case local file... or URI.
	var testfile = indivTest.getElementsByTagName("file");
	if (testfile.length != 0) {
	    testfile = base_uri + "/" + testfile[0].firstChild.data;
	} else {
	    testfile = (indivTest.getElementsByTagName("url"))[0].firstChild.data;
	}
	// the description, if any
	var testdesc = indivTest.getElementsByTagName("description");
	if (testdesc.length != 0) {
	    testdesc = testdesc[0].firstChild.data;
	} else {
	    testdesc = "No Description";
	}
	// validity
	var validity = (indivTest.getElementsByTagName("result"))[0].getAttribute("valid");
	// expected errors
	var nberrors = indivTest.getElementsByTagName("errors");
	if (nberrors.length != 0) {
	    nberrors = nberrors[0].firstChild.data;
	} else {
	    nberrors = 0;
	}
	
	// expected warnings.
	var nbwarnings = indivTest.getElementsByTagName("warnings");
	if(nbwarnings.length != 0) {
	    nbwarnings = nbwarnings[0].firstChild.data;
	} else {
	    nbwarnings = 0;
	}
	// now create the TR
	// will be
	// <tr>
	//   [0]<td rowspan="2"> 
	//       <a href="<testcase uri>">[Testcase]</a>
	//       <a href="<result page>">[Results]</a>
	//       <p>description</p>
	//      </td>
	//  [1]<td rowspan="2">profile</td>
	//  [2]<td>validity</td>
	//  [3]<td>errors</td>
	//  [4]<td>warnings</td>
	// </tr>
	var tr = document.createElement("tr");
	tr.setAttribute("class", "expected");
	
	var cell = document.createElement("td");
	cell.setAttribute("rowspan","2");
	var anc =  document.createElement("a");
	anc.setAttribute("href",testfile);
        anc.setAttribute("medium", medium);
	anc.setAttribute("warning", warningLevel);
	var text = document.createTextNode("[Testcase]");
	anc.appendChild(text);
	cell.appendChild(anc);

	text = document.createTextNode(" - ");
	cell.appendChild(text);
	
	anc =  document.createElement("a");
	var checkuri = validator_uri+"validator?uri="+
	    urlencode(testfile)+"&profile="+
	    cssprofile+"&usermedium="+medium+"&warning="+warningLevel; 
	// FIXME medium, warning level
	anc.setAttribute("href",checkuri);
	text = document.createTextNode("[Result]");
	anc.appendChild(text);
	cell.appendChild(anc);
	
	anc =  document.createElement("p");
	anc.innerHTML = testdesc;
	cell.appendChild(anc);
	tr.appendChild(cell);

	cell = document.createElement("td");
	cell.setAttribute("rowspan","2");
	text = document.createTextNode(cssprofile);
	cell.appendChild(text);
	tr.appendChild(cell);

	cell = document.createElement("td");
	text = document.createTextNode(validity);
	cell.appendChild(text);
	tr.appendChild(cell);

	cell = document.createElement("td");
	text = document.createTextNode(nberrors);
	cell.appendChild(text);
	tr.appendChild(cell);

	cell = document.createElement("td");
	text = document.createTextNode(nbwarnings);
	cell.appendChild(text);
	tr.appendChild(cell);

        var result_row = getResultRow();
	table.appendChild(result_row);
	table.insertBefore(tr,result_row);
    }
}

function getResultRow() {
    var tr = document.createElement("tr");
    tr.setAttribute("class", "result");
    var cell = document.createElement("td");
    var text = document.createTextNode("-");
    cell.appendChild(text);
    tr.appendChild(cell);

    cell = document.createElement("td");
    text = document.createTextNode("-");
    cell.appendChild(text);
    tr.appendChild(cell);

    cell = document.createElement("td");
    text = document.createTextNode("-");
    cell.appendChild(text);
    tr.appendChild(cell);

    return tr;
}
