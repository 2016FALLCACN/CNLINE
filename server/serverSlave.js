var fs = require('fs');
var rootDir = "uploads";
var clients = [];
var userArray; 

process.on('message', function(m, socket) {
    if (m === 'socket') {
        socket.name = socket.remoteAddress+":"+socket.remotePort;
        clients.push(socket);
        socket.write("ready");

        var sender, receiver, filename, append = 0;
        var writeDestDir;
        var upload = 0;
        socket.on('data', function(data) {
            var option = data.toString().split(':');
            var tail = data.toString().indexOf("<FINISH>");
           /* if (tail > -1) {
                console.log("I find the tail.ha ha ha!!!!!!!");
            }*/
            if (option[0] === "mode") {
                console.log(option[0]);
                console.log(data.toString());
                if (option[1] === "upload")
                    upload = 1;
                else if (option[1] === "download")
                    upload = 0;
            }
            else if (option[0] === "filename") {
                console.log(option[0]);
                console.log(data.toString());
                filename = option[1];
                if (upload) {
                    socket.write("waitForFile");
                }
                else {
                    writeDestDir = rootDir+'/'+receiver+'/'+sender;
                    console.log(writeDestDir+'/'+filename);
                    fs.readFile(writeDestDir+'/'+filename, function(err, data) {
                        if (err) {
                            console.log("cannot open file: "+writeDestDir+'/'+filename);
                        }
                        else {
                            socket.write("sendFile");
                            for (i = 0; i < 100000000; i++);
                            socket.write(data);
                            for (i = 0; i < 500000000; i++);
                            console.log("[send data]after loop");
                            socket.write("<FINISH>");
                        }
                    });
                }
            }
            else if (option[0] === "receiver") {
                console.log(option[0]);
                console.log(data.toString());
                receiver = option[1];
                
                if (upload) {
                    userArray = fs.readFileSync('user.cfg').toString().split('\n');
                    var find = false;
                    for (i in userArray) {
                        var validUser = userArray[i].toString().split(':');
                        if (validUser[0] === receiver && receiver != '') {
                            find = true;
                            console.log("[receiver]FIND YOU!");
                        }
                    }
                    if (!find) {
                        socket.write("no such receiver");
                    }
                }
            }
            else if (option[0] === "sender") {
                console.log(option[0]);
                console.log(data.toString());
                sender = option[1];
                
                userArray = fs.readFileSync('user.cfg').toString().split('\n');
                var find = false;
                for (i in userArray) {
                    var validUser = userArray[i].toString().split(':');
                    if (validUser[0] === sender && sender != '') {
                        find = true;
                        console.log("[sender]FIND YOU!");
                    }
                }
                if (find) {
                    writeDestDir = rootDir+'/'+receiver+'/'+sender;
                    try {
                        fs.accessSync(writeDestDir);
                    } catch (e) {
                        fs.mkdirSync(writeDestDir);
                    }
                }
                else {
                    socket.write("no such sender");
                }
            }
            else if (option[0] === "finish") {
                console.log("file upload finish ===========================");
                append = 0;
            }
            else if (option[0] === "test") {
                console.log("how may tests do we have");
            }
            else {
                if (!append) {
                    fs.writeFileSync(writeDestDir+'/'+filename, data);
                    append = 1;
                }
                else {
                    fs.appendFileSync(writeDestDir+'/'+filename, data);
                }
                console.log("file upload success");
                if (tail > -1) {
                    console.log("I find the tail.ha ha ha!!!!!!!");
                    socket.write("finish");
                }
            }
        });
        
        socket.on('end', function() {
            clients.splice(clients.indexOf(socket), 1);
            console.log(socket.name+" left the chat");
            process.exit();
        });
    }
});
