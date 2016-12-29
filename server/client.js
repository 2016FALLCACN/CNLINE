var io = require('socket.io-client');
var fs = require('fs');
var sleep = require('sleep');

var my_number;

var socket = io.connect('http://127.0.0.1:8000');

socket.emit('login', "aaa", "bbb");

socket.on('loginAck',function(){
        console.log("ack success!");
});
