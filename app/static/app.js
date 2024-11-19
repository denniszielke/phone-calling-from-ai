
const callButton = document.getElementById('callButton');
const statusMessage = document.getElementById('statusMessage');
const reportDiv = document.getElementById('report');

function onCallButton() {
    const phonenumber = document.getElementById('phonenumber').value;
    const phonetext = document.getElementById('phonetext').value;

    const callDetails = {
        number: phonenumber,
        text: phonetext
    };


    theUrl = window.location.href + "call";
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "POST", theUrl, false );
    xmlHttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    xmlHttp.send( JSON.stringify(callDetails) );
    
    reportDiv.textContent = xmlHttp.responseText;   
}

callButton.addEventListener('click', onCallButton);
