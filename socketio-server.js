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
		var channel
		if (typeof data == 'string' || data instanceof String) {
			var idx = data.indexOf("\"channel\":")
			channel = data.substring(idx+11, data.indexOf("\"",idx+11))
		}
		else channel = data.channel
		
		io.sockets.in(channel).emit('ui', data);
	})
	
	socket.on('disconnect', function () {
	  console.log("Client disconnected: "+socket.id)
	})
})
