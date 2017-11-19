var aname = readCookie("aname");
var refreshTime = 2000;
var nextReadId = 0;
var timeoutVar = null;

function changeAgent(newAgent) {
    setAgent(newAgent, function () {
        aname = newAgent;
        fillAttrs();
        fillTitle();
    });
}

function setRefresh(node) {
    refreshTime = parseInt(node.value);
    if (timeoutVar != null)
        clearTimeout(timeoutVar);
    refresh();
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

function fillAttrs() {
    var div = document.getElementById("attrList");
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status == 200) {
                div.innerHTML = this.responseText;
            }
        }
    };
    req.open("POST", "request", true);
    req.setRequestHeader("Content-Type", "application/json");
    var qJson = JSON.stringify({agent: aname, type: "getAttrs", query: ""});
    req.send(qJson);

}

function refresh() {
    var divs = document.getElementById("readings");
    divs.childNodes.forEach(function (div) {refreshReading(div)});
    divs = document.getElementById("plots");
    divs.childNodes.forEach(function (div) {refreshReading(div)});
    timeoutVar = setTimeout(refresh, refreshTime);
}

function refreshReading(reading) {
    var attrQ = reading.attrQ;
    //alert(attrQ);
    var req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status == 200) {
                var newVal = JSON.parse(this.responseText).value;
                if (newVal == null)
                    newVal = this.responseText;
                console.log(this.responseText);
                if (reading.isPlot) {
                    reading.values.push(newVal);
                    if (reading.values.length > 20)
                        reading.values.shift();
                   Plotly.newPlot(reading.contId, [{x: Array(20).fill().map(function (x, i) { return i + 1; }),
                                                    y:reading.values, type: 'scatter'}], {title: attrQ});
                }
                else {
                    document.getElementById(reading.contId).innerHTML = "<br>" + attrQ + ":" + newVal;//counter++;
                }

            }
            else {
                console.error("Failed refresh request" + attrQ);
                console.error(this.status);
                reading.parentNode.removeChild(reading);
            }
        }


    };
    req.open("POST", "request", true);
    req.setRequestHeader("Content-Type", "application/json");
    var qJson = JSON.stringify({agent: reading.aname, type: "attrQ", query: attrQ});
    req.send(qJson);
}



function readAttr(plot, sync) {
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
        newDiv.values  = [];
    }
    newDiv.appendChild(content);
    newDiv.attrQ = attrQ;
    newDiv.aname = aname;
    refreshReading(newDiv);

    var divs = document.getElementById((plot) ? 'plots' : (sync) ? 'readings' : 'readingsNoRefresh');
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
                fillAttrs();
            }
            else {
                cBox.innerHTML = "Operation unsuccessful"; //TODO dodać co poszło nie tak
                cBox.style.color = "red";
            }
        }
    };
    req.open("POST", "request", true);
    req.setRequestHeader("Content-Type", "application/json");
    var qJson = JSON.stringify({agent: aname, type: reqType, query:reqText});
    req.send(qJson);
}

window.onload = function () {
    fillTitle();
    fillAttrs();
    setTimeout(refresh, 1000);
};