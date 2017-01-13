var io = require('socket.io-client');
var fs = require('fs');
var path = require('path');
var args = process.argv.slice(2);

var socket = io.connect('http://127.0.0.1:8000');

/*args[0] = sender name, args[1] = receiver name, args[2] = file name*/
console.log(args[0]+' '+args[1]+' '+args[2]);

fs.readFile(args[2], 'binary', function(err, data) {
    console.log("enter");
    if (err) {
        console.log("cannot open file:"+args[2]);
    }
    else {
        console.log(path.basename(args[2]));
        socket.emit('fileUpload', args[0], args[1], path.basename(args[2]), data);
    }
});

socket.on('uploadStatus', function(stat) {
    if (stat === "success") {
        console.log("upload success");
    }
    else {
        console.log("upload fail");
    }
    process.exit();
});
