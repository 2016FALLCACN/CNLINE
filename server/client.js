var io = require('socket.io-client');
var fs = require('fs');
var path = require('path');
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
        if (arr[1] === undefined || arr[2] === undefined) {
            console.log("incorrect format");
        }
        else {
            for (i in arr) {
                if (i >= 2) {
                    var cp = require('child_process');
                    cp.exec('node ./file.js up '+args[0]+' '+arr[1]+' '+arr[i]+' /T /F', 
                    function(err, stdout, stderr) {
                        console.log(stdout);
                    });
/*                    fs.readFile(arr[i], 'binary', function(err, data) {
                        if (err) {
                            console.log("cannot open the file:"+baseName);
                        }
                        else {
                            console.log(baseName);
                            socket.emit('fileUpload', arr[1], baseName, data);
                        }
                    });*/
                }
            }
        }
    }
    else if (arr[0] === "get") {
        if (arr[1] === undefined || arr[2] === undefined) {
            console.log("incorrect format");
        }
        else {
            /*arr[1] = senderName, arr[2] = fileName*/
            for (i in arr) {
                if (i >= 2) {
                    var cp = require('child_process');
                    cp.exec('node ./file.js down '+usrName+' '+arr[1]+' '+arr[i]+' /T /F', 
                    function(err, stdout, stderr) {
                        console.log(stdout);
                    });
//                    socket.emit('fileDownload', usrName, arr[1], arr[i]);
                }
            }
        }
    }
    else if (arr[0] === "msg"){
        socket.emit('message', arr[1], arr[2]);
    }
    else if (arr[0] === "list") {
        if (arr[1] === undefined) {
            console.log("incorrect format");
        }
        else {
            /*arr[1] = senderName, arr[2] = fileName*/
            socket.emit('listDownloadFiles', usrName, arr[1]);
        }
    }
    else {
        console.log("invalid command");
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


socket.on('fileDownloadAck', function(filename, data){
        if (filename === "") {
            console.log("cannot fetch the file");
        }
        else {
            fs.writeFile(filename, data, 'binary');
            console.log("get "+filename+" success");
        }
});

socket.on('uploadStatus', function(stat) {
    if (stat === "success") {
        console.log("upload success");
    }
    else {
        console.log("upload fail");
    }
});

socket.on('listDownloadOptions', function(fileArr) {
    for (i in fileArr) {
        console.log(fileArr[i]);
    }
})
