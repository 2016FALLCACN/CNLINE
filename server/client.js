var io = require('socket.io-client');
var fs = require('fs');
var sleep = require('sleep');

var my_number;

var socket = io.connect('http://127.0.0.1:8000');

var args = process.argv.slice(2);
socket.emit('login', args[0], args[1]);

socket.on('loginAck',function(){
        console.log("login: ack success!");
});

var stdin = process.openStdin();
stdin.addListener("data", function(d) {
    // note:  d is an object, and when converted to a string it will
    // end with a linefeed.  so we (rather crudely) account for that  
    // with toString() and then trim() 
    var data = d.toString().trim();
    socket.emit('message', data);
});

socket.on('registerAck',function(){
        console.log("register: ack success!");
});

socket.on('messageAck',function(){
        console.log("message: ack success!");
});
