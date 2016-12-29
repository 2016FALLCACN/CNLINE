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

  	/* server's handler for "login" */
	socket.on('login',function(id, pwd){
		console.log(id);
		console.log(pwd);
		/* find valid user */
		var valid = false;
		var userArray = fs.readFileSync('user.cfg').toString().split("\n");

		for(i in userArray) {
			if(userArray[i] == id+":"+pwd){
				console.log("VALID!");
				valid = true;
			}
		}
	
		if(valid)
			clients[my_client_num].emit('loginAck', "success");
		else
			clients[my_client_num].emit('loginAck', "fail");
	
	});
	/* server's handler for "register" */
	socket.on('register',function(id, pwd){
		console.log(id);
		console.log(pwd);

		var valid = true;
		var userArray = fs.readFileSync('user.cfg').toString().split("\n");
		for(i in userArray) {
			var arr = userArray[i].toString().split(":");
			if(arr[0] == id){
				console.log("USED!");
				valid = false;
			}
		}

		if(valid) {
			fs.appendFile('user.cfg', id+":"+pwd+"\n", function (err){});
			clients[my_client_num].emit('registerAck', "success");
		} else
			clients[my_client_num].emit('registerAck', "fail");

	});

	/*socket.on('disconnect',function(){
		io.emit('all_disconnect');
		process.exit();
	});*/
});
