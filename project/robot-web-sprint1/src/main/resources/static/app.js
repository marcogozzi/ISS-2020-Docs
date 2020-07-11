var stompClient = null;
var hostAddr = "http://localhost:8080/move";



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

function showMsgRoom(message) {
console.log(message );
    $("#applmsgs").html( "<pre>"+message.replace(/\n/g,"<br/>")+"</pre>" );
    //$("#applmsgintable").append("<tr><td>" + message + "</td></tr>");
}
function showMsgClient(message) {
console.log(message );
    $("#clientstate").html( "<pre>"+message.replace(/\n/g,"<br/>")+"</pre>" );
}

$(function () {
     $("form").on('submit', function (e) {
         e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });

//USED BY SOCKET.IO-BASED GUI  

	$( "#h" ).click(function() {  sendTheMove("h") });
	$( "#w" ).click(function() {  sendTheMove("w") });
	$( "#s" ).click(function() {  sendTheMove("s") });
	$( "#r" ).click(function() {  sendTheMove("r") });
	$( "#l" ).click(function() {  sendTheMove("l") });
	$( "#x" ).click(function() {  sendTheMove("x") });
	$( "#z" ).click(function() {  sendTheMove("z") });
	$( "#p" ).click(function() {  sendTheMove("p") });

	$( "#update" ).click(function() { sendUpdateRequestRoom(  ) });
	$( "#client" ).click(function() { sendUpdateRequestClient(  ) });
	$( "#clientstateupdate" ).click(function() { sendUpdateRequestClientState(  ) });
});



