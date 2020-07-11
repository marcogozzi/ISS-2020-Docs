var stompClient = null;
var hostAddr = "http://localhost:8080/move";
var i = 1;

//SIMULA UNA FORM che invia comandi POST
function sendRequestData( params, method) {
    method = method || "post"; // il metodo POST � usato di default
    //console.log(" sendRequestData  params=" + params + " method=" + method);
    var form = document.createElement("form");
    form.setAttribute("method", method);
    form.setAttribute("action", hostAddr);
    var hiddenField = document.createElement("input");
        hiddenField.setAttribute("type", "hidden");
        hiddenField.setAttribute("name", "move");
        hiddenField.setAttribute("value", params);
     	//console.log(" sendRequestData " + hiddenField.getAttribute("name") + " " + hiddenField.getAttribute("value"));
        form.appendChild(hiddenField);
    document.body.appendChild(form);
    console.log("body children num= "+document.body.children.length );
    form.submit();
    document.body.removeChild(form);
    console.log("body children num= "+document.body.children.length );
}


function postJQuery(themove){
var form = new FormData();
form.append("name",  "move");
form.append("value", "r");

let myForm = document.getElementById('myForm');
let formData = new FormData(myForm);


var settings = {
  "url": "http://localhost:8080/move",
  "method": "POST",
  "timeout": 0,
  "headers": {
       "Content-Type": "text/plain"
   },
  "processData": false,
  "mimeType": "multipart/form-data",
  "contentType": false,
  "data": form
};

$.ajax(settings).done(function (response) {
  //console.log(response);  //The web page
  console.log("done move:" + themove );
});

}

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}


function connect() {
    var socket = new SockJS('/it-unibo-iss');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        stompClient.subscribe('/topic/roomstate', function (msg) {
             showMsgRoom(JSON.parse(msg.body).content);
        });
        stompClient.subscribe('/topic/clientstate', function (msg) {
             showMsgClient(JSON.parse(msg.body).content);
        });
        stompClient.subscribe('/topic/mapstate', function (msg) {
         showRoom(JSON.parse(msg.body).content);
    	});
        stompClient.subscribe('/topic/control', function (msg) {
        	var ctnt = JSON.parse(msg.body).content
        	
        	//"Robot automatically controlled, press to switch to manual"
        	var automaticStr = ctnt.match(/.*auto.*control.*manual/g);
        	
        	//"Robot manually controlled, press to switch to auto"
        	var manualStr = ctnt.match(/.*manual.*control.*auto/g);
        	
			if( manualStr ) { $("#btnrow").show(); }
			else if ( automaticStr ) { $("#btnrow").hide(); }
			
			$("#manual").html( ctnt );
			
         	$( "#manual" ).click(function() { 
         	   $("#manual").html( "Switching..." ); 
               	//if((i++%2)==0) 
               		$("#btnrow").hide(); 
         	});
         	
    	});
    });
}


function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

/*
function sendMove() {
    stompClient.send("/app/move", {}, JSON.stringify({'name': $("#name").val()}));
}
*/

function sendTheMove(move){
	console.log("sendTheMove " + move);
    stompClient.send("/app/move", {}, JSON.stringify({'name': move }));
}

function sendUpdateRequestRoom(){
	console.log(" sendUpdateRequest "  );
    stompClient.send("/app/update", {}, JSON.stringify({'name': 'update' }));
}
function sendUpdateRequestClient(){
	console.log(" sendUpdateRequest "  );
    stompClient.send("/app/client", {}, JSON.stringify({'name': 'client' }));
}
function sendUpdateRequestClientState(){
	console.log(" sendUpdateRequest "  );
    stompClient.send("/app/clientupdate", {}, JSON.stringify({'name': 'clientupdate' }));
}
function sendUpdateRequestClientCid(){
	console.log(" sendUpdateRequest "  + cid);
	//TODO
	var cid = $("#cid").val()
    stompClient.send("/app/clientcid", {}, JSON.stringify({'name': 'progress', 'cid': cid }));
}

function showMsgRoom(message) {
console.log(message);
    $("#applmsgs").html( "<pre>"+message.replace(/\n/g,"<br/>")+"</pre>" );
    //$("#applmsgintable").append("<tr><td>" + message + "</td></tr>");
}
function showMsgClient(message) {
console.log(message);
    $("#clientstate").html( "<pre>"+message.replace(/\n/g,"<br/>")+"</pre>" );
}

function showRoom(message){
	console.log( message );
	 $("#map").html( message );
}


$(function () {
     $("form").on('submit', function (e) {
         e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });

//USED BY SOCKET.IO-BASED GUI  

    $( "#manual" ).click(function() {  
        $( "#manual" ).click(function() {}) //de - activate btn
    	$( "#manual" ).html( "Switching..." ); //update btn text
    	$( "#btnrow" ).hide(); //hide manual control btns
    	sendTheMove("manual") });
    	
    $( "#left" ).click(function() {  sendTheMove("l") });
    $( "#step" ).click(function() {  sendTheMove("p") });
    $( "#right" ).click(function() {  sendTheMove("r") });
    $( "#h" ).click(function() {  sendTheMove("h") });
    $( "#w" ).click(function() {  sendTheMove("w") });
    $( "#s" ).click(function() {  sendTheMove("s") });
    $( "#r" ).click(function() {  sendTheMove("r") });
    $( "#l" ).click(function() {  sendTheMove("l") });
    $( "#x" ).click(function() {  sendTheMove("x") });
    $( "#z" ).click(function() {  sendTheMove("z") });
    $( "#p" ).click(function() {  sendTheMove("p") });


//USED BY POST-BASED BOUNDARY  
    $( "#start" ).click(function() { sendRequestData( "w") });
    $( "#stop" ).click(function()  { sendRequestData( "h") });

	$( "#update" ).click(function() { sendUpdateRequestRoom(  ) });
	$( "#client" ).click(function() { sendUpdateRequestClient(  ) });
	$( "#clientstateupdate" ).click(function() { sendUpdateRequestClientState(  ) });
	
	$( "#clientcid" ).click(function() { sendUpdateRequestClientCid(  )	});
});
