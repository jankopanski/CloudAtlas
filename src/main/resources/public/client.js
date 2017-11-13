function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function clearCookies() {
    document.cookie.split(";").forEach(function (c) {
        document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
    });
}

var aname = readCookie("aname");
var refreshTime = 2000;
var nextReadId = 0;
var counter = 0; //TODO wywalić jak nie będzie mockupów

function setRefresh(node) {
    refreshTime = parseInt(node.value);
    console.log(refreshTime);
}

function fillTitle() {
    if (aname)
        document.title = aname;
    else {
        document.title = "disconnected";
        if (confirm("You are not connected, redirect to connect page?"))
            document.location.href = "index.html";
    }
}

function refresh() {
    var divs = document.getElementById("readings");
    divs.childNodes.forEach(function (div) {refreshReading(div)});
    divs = document.getElementById("plots");
    divs.childNodes.forEach(function (div) {refreshReading(div)});
    setTimeout(refresh, refreshTime);
}

function refreshReading(reading) {
    var attrQ = reading.attrQ;
    //alert(attrQ);
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status == 200) {
                console.log("ok" + attrQ);
                if (reading.isPlot) {
                   Plotly.newPlot(reading.contId, [{x: [1,2,3], y:[counter % 7, counter * counter % 7, (Math.sqrt(counter)) % 7], type: 'scatter'}], {title: attrQ});
                }
                else {
                    document.getElementById(reading.contId).innerHTML = "<br>" + attrQ + ":" + counter++;
                }

            }
            else {
                console.error("Failed refresh request" + attrQ);
                console.error(this.status);
            }
        }


    };
    req.open("POST", "request", true);
    req.setRequestHeader("Content-Type", "application/json");
    var qJson = JSON.stringify({agent: aname, type: "attrQ", query: attrQ});
    req.send(qJson);
}

function readAttr(plot) {
    var attrQ = document.getElementById('attr').value;
    var newDiv = document.createElement('div');
    var closeButton = document.createElement('span');
    var content = document.createElement('div');
    content.id = "reading_" + nextReadId++;
    closeButton.id = 'close';
    closeButton.onclick=(function() {this.parentNode.parentNode.removeChild(this.parentNode); return false;});
    closeButton.textContent = "x";

    newDiv.appendChild(closeButton);
    newDiv.className = 'reading';
    newDiv.contId = content.id;
    newDiv.isPlot = plot;
    if (plot) {
        content.style.width = '400px';
        newDiv.style.width = '430px';
        content.style.height = '400px';
        newDiv.style.height = '430px';
    }
    newDiv.appendChild(content);
    newDiv.attrQ = attrQ;
    refreshReading(newDiv);

    var divs = document.getElementById((plot) ? 'plots' : 'readings');
    divs.appendChild(newDiv);
}

function singleRequest(node, reqType) {
    var reqText = node.value;
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (this.readyState == 4) {
            var cBox = document.getElementById("confirmBox");
            if (this.status == 200) {
                cBox.innerHTML = "Operation successful";
                cBox.style.color = "green";
            }
            else {
                cBox.innerHTML = "Operation unsuccessful"; //TODO dodać co poszło nie tak
                cBox.style.color = "red";
            }
        }
    }
    req.open("POST", "request", true);
    req.setRequestHeader("Content-Type", "application/json");
    var qJson = JSON.stringify({agent: aname, type: reqType, query:reqText});
    req.send(qJson);
}