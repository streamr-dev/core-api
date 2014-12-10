var io = require('socket.io')(8090);

io.on('connection', function (socket) {
	console.log("Client connected: "+socket.id)

	socket.on('subscribe', function(data) {
		if (data.channels) {
			data.channels.forEach(function(channel) {
				console.log("Client "+socket.id+" subscribed to channel "+channel)
				socket.join(channel, function(err) {
					socket.emit('subscribe', {channel:channel, error:err})
					console.log("Socket "+socket.id+" is now in rooms: "+socket.rooms)
				})
				
			})
		}
	})

	socket.on('unsubscribe', function(data) {
		if (data.channels) {
			data.channels.forEach(function(channel) {
				console.log("Client "+socket.id+" unsubscribed from channel "+channel)
				socket.leave(channel, function(err) {
					socket.emit('unsubscribe', {channel:channel, error:err})
					console.log("Socket "+socket.id+" is now in rooms: "+socket.rooms)
				})
			})
		}
	})

	socket.on('ui', function (data) {
	  console.log("UI message: "+JSON.stringify(data))
	  io.sockets.in(data.channel).emit('ui', data);
	})
	
	socket.on('disconnect', function () {
	  console.log("Client disconnected: "+socket.id)
	})
})
