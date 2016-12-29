var io = require('socket.io').listen(8000);
var cp = require('child_process');
var sleep = require('sleep');
var readline = require('readline');
var fs = require('fs');

var clients = [];
var conn_client = -1;
var sequence = 0;
// var client_num = 1;

console.log("Server start!");

io.on('connection', function(socket) {

	conn_client++;
	var my_client_num = conn_client;
  	clients[my_client_num] = socket;

	socket.on('login',function(data){
		console.log(data);
		clients[my_client_num].emit('loginAck');
	});

	socket.on('disconnect',function(){
		io.emit('all_disconnect');
		process.exit();
	});
});
