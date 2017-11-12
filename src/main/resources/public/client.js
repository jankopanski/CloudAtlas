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
    setTimeout(refresh, refreshTime);
}

function refreshReading(reading) {
    //reading.textContent += " refreshed";
}

function readAttr() {
    var attrQ = document.getElementById('attr').value;
    var newDiv = document.createElement('div');
    var closeButton = document.createElement('span');
    var content = document.createElement('div');
    closeButton.id = 'close';
    closeButton.onclick=(function() {this.parentNode.parentNode.removeChild(this.parentNode); return false;});
    closeButton.textContent = "x";

    newDiv.appendChild(closeButton);
    newDiv.className = 'reading';
    content.innerHTML += "<br>" + attrQ;
    newDiv.appendChild(content);
    newDiv.attrQ = attrQ;
    refreshReading(newDiv);

    var divs = document.getElementById('readings');
    divs.appendChild(newDiv);
}
