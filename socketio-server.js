var io = require('socket.io')(8090);

io.on('connection', function (socket) {
	console.log("Client connected: "+socket.id)

	socket.on('subscribe', function(data) {
		if (data.channels) {
			data.channels.forEach(function(channel) {
				console.log("Client "+socket.id+" subscribed to channel "+channel)
				socket.join(channel)
			})
		}
	})

	socket.on('unsubscribe', function(data) {
		if (data.channels) {
			data.channels.forEach(function(channel) {
				console.log("Client "+socket.id+" unsubscribed from channel "+channel)
				socket.leave(channel)
			})
		}
	})

	socket.on('ui', function (data) {
	  console.log("UI message: "+data)
	  socket.to(data.channel).emit(data)
	})
	
	socket.on('disconnect', function () {
	  console.log("Client disconnected: "+socket.id)
	})
})
