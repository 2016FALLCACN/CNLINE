var cp = require('child_process');
var readline = require('readline');

var io = require('socket.io').listen(60000);
var fs = require('fs');

var clients = []; // socket of connected client
var client_name = []; // username of connected client
var client_userid = []; // userid of connected client
var conn_client = -1; // index of clients[] and client_name(online)

console.log("Server start!");


var userArray = fs.readFileSync('user.cfg').toString().split("\n");

/*create directory tree to store upload files*/
var rootDir = "uploads"; 

try {
  fs.accessSync(rootDir);
} catch (e) {
  fs.mkdirSync(rootDir);
}

var tokens = [];

for (i in userArray) {
    tokens = userArray[i].split(":");
    console.log(tokens[0]);
    try {
        fs.accessSync(rootDir+"/"+tokens[0]);
    } catch (e) {
        fs.mkdirSync(rootDir+"/"+tokens[0]);
    }
}

io.on('connection', function(socket) {

	conn_client++;
	var my_client_num = conn_client;
  	clients[my_client_num] = socket;

  	/* server's handler for "login" */
	socket.on('login',function(id, pwd){
		console.log("[Login]:"+id);
		console.log("[Login]:"+pwd);
		/* find valid user */
		var valid = false;

        userArray = fs.readFileSync('user.cfg').toString().split("\n");
		for(i in userArray) {
			if(userArray[i] == id+":"+pwd){
				client_userid[my_client_num] = i;
				console.log("VALID!");
				valid = true;
			}
		}
	
		if(valid) {
			client_name[my_client_num] = id;
			var sendArr = [];
			for(i in userArray) {
				var oneline = userArray[i].toString().split(":");
				if(oneline[0] != "")
					sendArr[i] = oneline[0];
			}
			clients[my_client_num].emit('loginAck', "success", sendArr);
		} else
			clients[my_client_num].emit('loginAck', "fail", []);
	
	});

	socket.on('askUserList',function(){
		/* find valid user */
		var sendArr = [];
		for(i in userArray) {
			var oneline = userArray[i].toString().split(":");
			if(oneline[0] != "")
				sendArr[i] = oneline[0];
		}
		clients[my_client_num].emit('userListAck', sendArr);
	});

	/* server's handler for "register" */
	socket.on('register',function(id, pwd){
		console.log("[Register]:"+id);
		console.log("[Register]:"+pwd);

        userArray = fs.readFileSync('user.cfg').toString().split("\n");
		var valid = true;
		for(i in userArray) {
			var arr = userArray[i].toString().split(":");
			if(arr[0] == id){
				console.log("USED!");
				valid = false;
			}
		}

		if(valid) {
			fs.appendFile('user.cfg', id+":"+pwd+"\n", function (err){});
            try {
                fs.accessSync(rootDir+"/"+id);
            } catch (e) {
                fs.mkdirSync(rootDir+"/"+id);
            }
            // [MSGLOG] add a directory for message logs
			if (!fs.existsSync("message_logs/" + id)){
    			fs.mkdirSync("message_logs/" + id);
			}
			io.to(socket.id).emit('registerAck', "success");
		} else
			io.to(socket.id).emit('registerAck', "fail");

	});

	socket.on('message',function(objectName ,data){
		console.log("[Message to]: "+objectName);
		console.log("[Message]: "+data);

		/* send to the other */
		var objectIndex;
		var find = false;
		for(i in client_name) {
			if(client_name[i] == objectName){
				objectIndex = i;
				find = true;
				console.log("FIND YOU!");
			//	break;
			}
		}
		if(find && objectName != client_name[my_client_num])
			clients[objectIndex].emit('messageFromOther', client_name[my_client_num], data);

		// [MSGLOG] append chat entry to a file
			var logfs = require('fs');
			var folder_name = (client_name[my_client_num] < objectName)? client_name[my_client_num] : objectName;
			var file_name = ( (client_name[my_client_num] >= objectName)? client_name[my_client_num] : objectName);
			console.log("Writing file on : " + "message_logs/" + folder_name + "/" + file_name + ".log");
			fs.appendFile("message_logs/" + folder_name + "/" + file_name + ".log", "[" + client_name[my_client_num] + "]" + data + "\n" , 'utf8', function (err) {
  				if (err) return console.log(err);});
		/* write to file */
		// var first, second;
		// fs.appendFile("HistoricalMsg/"+first+second+".cfg", data+"\n", function (err){});
	
		clients[my_client_num].emit('messageAck', "success");

	});

	// [MSGLOG]
	socket.on('getLog', function(objectName) {
		console.log("[Log request]: ("  + client_name[my_client_num] + ", " + objectName + ")" );
		var folder_name = (client_name[my_client_num] < objectName)? client_name[my_client_num] : objectName;
		var file_name = ( (client_name[my_client_num] >= objectName)? client_name[my_client_num] : objectName);
		var path = "message_logs/" + folder_name + "/" + file_name + ".log";


		var count = 0;
		if (fs.existsSync(path)) {
			var logArray = fs.readFileSync(path).toString().split("\n");
			for (i in logArray) {
				if (logArray[i].length > 0) {
					var entryUser = logArray[i].slice(1, logArray[i].indexOf(']'));
					var entryMsg = logArray[i].slice(logArray[i].indexOf(']') + 1);
					clients[my_client_num].emit('logData', "MSG", count.toString(), entryUser, entryMsg);
					count++;
				}
			}
		}
		clients[my_client_num].emit('logData', "FIN", count.toString(), " ", " ");
		
	});

	socket.on('fileUpload',function(senderName, receiverName , filename, data){
		console.log("[File to]: "+receiverName);
		console.log("[File Name]: "+filename);

		/* send to the other */
        userArray = fs.readFileSync('user.cfg').toString().split("\n");
		var find = false;
		for(i in userArray) {
            var validUser = userArray[i].toString().split(":");
			if(validUser[0] === receiverName && receiverName != ""){
				find = true;
				console.log("FIND YOU!");
			}
		}
        
		if (find) {
            fs.stat(rootDir+'/'+receiverName, function(err, stats) {
                if (err && err.errno === 34) {
                    console.log("no such directory");
                    io.to(socket.id).emit('uploadStatus', "fail");
                }
                else {
                    var writeDestDir = rootDir+'/'+receiverName+'/'+senderName;
                    try {
                        fs.accessSync(writeDestDir);
                    } catch (e) {
                        fs.mkdirSync(writeDestDir);
                    }
                    fs.writeFileSync(writeDestDir+'/'+filename, data, 'binary');
                    console.log("file upload success!");
                    //clients[my_client_num].emit('fileUploadAck', "success");
                    io.to(socket.id).emit('uploadStatus', "success");
                }
            });
	    }
        else {
            console.log("no such user");
            io.to(socket.id).emit('uploadStatus', "fail");
        }	

	});
	
    socket.on('fileDownload',function(receiverName, senderName, filename){
        userArray = fs.readFileSync('user.cfg').toString().split("\n");
		for(i in userArray) {
            var validUser = userArray[i].toString().split(":");
			if(validUser[0] === receiverName){
				find = true;
				console.log("FIND YOU!");
			}
		}
		console.log("[File request]: "+receiverName);
		console.log("[File Name]: "+filename);
		/* send to the other */
        var data;
//        fs.readFileSync(rootDir+"/"+receiverName+"/"+filename, data, 'binary');
        fs.readFile(rootDir+"/"+receiverName+"/"+senderName+"/"+filename, 'binary', function(err, data){
            if (err) {
                console.log("cannot open the file:"+filename);
                io.to(socket.id).emit('fileDownloadAck', "", "");
            }
            else {
                io.to(socket.id).emit('fileDownloadAck', filename, data);
                console.log("file download success!");
            }
        })

	});

    socket.on('listDownloadFiles', function(receiverName, senderName) {
        var listDir = rootDir+'/'+receiverName+'/'+senderName;
        try {
            fs.accessSync(listDir);
        } catch (e) {
            console.log("no such directory");
        }
        fs.readdir(listDir, function(err, files) {
            for (i in files) {
                console.log(files[i]);
            }
            io.to(socket.id).emit('listDownloadOptions', files);
        })
    });

	socket.on('logout',function(username){
		io.to(socket.id).emit('logoutAck');
		var userIndex;
		var find = false;
		for(i in client_name) {
			if(client_name[i] == username){
				clients.splice(i, 1);
				client_name.splice(i, 1);
				client_userid.splice(i, 1);
				conn_client--;
				break;
			}
		}

	});
});



function sleep(msec) {
    var time = new Date().getTime();
    while(new Date().getTime() - time < msec);
}
