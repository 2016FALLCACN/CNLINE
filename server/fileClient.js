var args = process.argv.slice(2);
var fs = require('fs');
var path = require('path');
var net = require('net');

var client = new net.Socket();
client.connect(8001, '140.112.30.34', function() {
    console.log('Connected');
});

var append = 0;
client.on('data', function(data) {
    var tail = data.toString().indexOf("<FINISH>");
    if (data.toString() === "ready") {
        console.log("server ready");
        if (args[0] === "upload" || 
            args[0] === "download") {
            client.write("mode:"+args[0]);
            for (i = 0; i < 70000000; i++);
            client.write("receiver:"+args[1]);
            for (i = 0; i < 70000000; i++);
            client.write("sender:"+args[2]);
            for (i = 0; i < 70000000; i++);
            client.write("filename:"+args[3]);
        }
        else {
            console.log("invalid command");
            client.destroy();
        }
    }
    if (data.toString() === "waitForFile") {
        console.log("start to send file");
        fs.readFile(args[3], function(err, data) {
            if (err) {
                console.log("cannot open file:"+args[3]);
            }
            else {
                console.log(path.basename(args[3]));
                client.write(data);
                for (i = 0; i < 100000000; i++);
                console.log("[send data]after loop");
                client.write("<FINISH>");
            }
        });
    }
    else if (data.toString() === "sendFile") {
        append = 0;
    }
    else if (data.toString() === "no such receiver") {
        console.log("no such receiver");
    }
    else if (data.toString() === "no such sender") {
        console.log("no such sender");
    }
    else if (data.toString() === "finish") {
        console.log("receive file finish==============");
        append = 0;
        client.destroy();
    }
    else {
        if (args[0] === "download") {
            if (!append) {
                fs.writeFileSync(args[3], data);
                append = 1;
            } 
            else {
                fs.appendFileSync(args[3], data);
                //console.log(data);
            }
            console.log("file download success");
            if (tail > -1) {
                console.log("I find the tail. ha ha ha");
                client.destroy();
            }
        }
    }
    //console.log('Received: ' + data);
    //client.destroy(); // kill client after server's response
});

client.on('close', function() {
    console.log('Connection closed');
});
