var io = require('socket.io-client');
var fs = require('fs');
var path = require('path');
var args = process.argv.slice(2);

var socket = io.connect('http://127.0.0.1:8000');

/*args[0] = mode[down|up], args[1] = sender/receiver name, args[2] = sender/receiver name, args[3] = file name*/
console.log(args[0]+' '+args[1]+' '+args[2]+' '+args[3]);

/*args[0] = mode:up, args[1] = sender name, args[2] = receiver name, args[3] = file name*/
if (args[0] === "up") {
    fs.readFile(args[3], 'binary', function(err, data) {
        console.log("enter");
        if (err) {
            console.log("cannot open file:"+args[3]);
        }
        else {
            console.log(path.basename(args[3]));
            socket.emit('fileUpload', args[1], args[2], path.basename(args[3]), data);
        }   
    });
}
/*args[0] = mode:down, args[1] = receiver name, args[2] = sender name, args[3] = file name*/
else if (args[0] === "down") {
    console.log("download start");
    socket.emit('fileDownload', args[1], args[2], args[3]);
}
else{
    console.log("invalid command");
    process.exit();
}

socket.on('fileDownloadAck', function(filename, data) {
    if (filename === "") {
        console.log("cannot fetch the file");
    }
    else {
        fs.writeFileSync(filename, data, 'binary');
        console.log("get "+filename+" success");
    }
    process.exit();
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

