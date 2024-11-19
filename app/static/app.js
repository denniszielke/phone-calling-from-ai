
const callButton = document.getElementById('callButton');
const statusMessage = document.getElementById('statusMessage');
const reportDiv = document.getElementById('report');

function onCallButton() {
    const phonenumber = document.getElementById('phonenumber').value;

    const callDetails = {
        number: phonenumber
    };


    theUrl = window.location.href + "call";
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "POST", theUrl, false );
    xmlHttp.send( callDetails );
    
    reportDiv.textContent = xmlHttp.responseText;   
}

callButton.addEventListener('click', onCallButton);
