var fs = require('fs');
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

// Load the TCP Library
var net = require('net');
// Keep track of the chat clients
var cp;
// Start a TCP Server
net.createServer(function (socket) {

  // Send a message to all clients
  function broadcast(message, sender) {
    clients.forEach(function (client) {
      // Don't want to send it to sender
      if (client === sender) return;
      client.write(message);
    });
    // Log it to the server output too
    process.stdout.write(message)
    //console.log(message);
  }
    
  cp = require('child_process').fork('serverSlave.js');
  cp.send('socket', socket);

}).listen(8001);

// Put a friendly message on the terminal of the server.
console.log("Chat server running at port 8001\n");

