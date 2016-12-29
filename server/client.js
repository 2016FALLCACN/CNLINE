var io = require('socket.io-client');
var fs = require('fs');
var sleep = require('sleep');

var my_number;

var socket = io.connect('http://127.0.0.1:8000');

socket.emit('login', "aaa", "bbb");

socket.on('loginAck',function(){
        console.log("login: ack success!");
});

var stdin = process.openStdin();

stdin.addListener("data", function(d) {
    // note:  d is an object, and when converted to a string it will
    // end with a linefeed.  so we (rather crudely) account for that  
    // with toString() and then trim() 
    var arr = d.toString().trim().split(":");
    socket.emit('register', arr[0], arr[1]);
  });

socket.on('registerAck',function(){
        console.log("register: ack success!");
});
