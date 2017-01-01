var io = require('socket.io-client');
var fs = require('fs');

var my_number;

var socket = io.connect('http://127.0.0.1:8000');

var args = process.argv.slice(2);
socket.emit('login', args[0], args[1]);

var usrName = args[0];

socket.on('loginAck',function(){
        console.log("login: ack success!");
});

var stdin = process.openStdin();
stdin.addListener("data", function(d) {
    // note:  d is an object, and when converted to a string it will
    // end with a linefeed.  so we (rather crudely) account for that  
    // with toString() and then trim() 
    var data = d.toString().trim();
    var arr = data.toString().split(":");
    var buff;
    if (arr[0] === "put") {
        fs.readFile(arr[2], 'binary', function(err, data) {
            if (err)
                return console.log(err);
            else {
                socket.emit('fileUpload', arr[1], arr[2], data);
            }
        });
    }
    else if (arr[0] === "get") {
        socket.emit('fileDownload', usrName, arr[1]);
    }
    else {
        socket.emit('message', arr[0], arr[1]);
    }
});

socket.on('registerAck',function(){
        console.log("register: ack success!");
});

socket.on('messageAck',function(){
        console.log("message: ack success!");
});

socket.on('messageFromOther',function(name, data){
        console.log("message from "+name+": "+data);
});


socket.on('fileDownloadAck',function(filename, data){
        fs.writeFile("../"+filename, data, 'binary');
        console.log("get "+filename+" success");
});
