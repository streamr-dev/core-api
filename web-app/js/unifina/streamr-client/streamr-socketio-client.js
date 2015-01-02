(function(exports) {

function StreamrClient(options) {
	// Default options
	this.options = {
		socketIoUrl: "http://localhost:8090"
	}
	this.streams = {}
	this.socket = null
    this.connected = false
	$.extend(this.options, options || {})
}

StreamrClient.prototype.subscribe = function(streamId, callback, options) {
	var _this = this
	this.streams[streamId] = {
		handler: function(response) {
			_this.handleResponse(response, streamId, callback)
		},
		options: options,
		counter: 0
	}
	
	return this.streams[streamId]
}

StreamrClient.prototype.isConnected = function() {
	return this.connected
}

StreamrClient.prototype.reconnect = function() {
	return this.connect(true)
}

StreamrClient.prototype.connect = function(reconnect) {
	var _this = this
	
	if (this.connected)
		this.disconnect()
	
	console.log("Connecting to "+this.options.socketIoUrl)
	this.socket = io(this.options.socketIoUrl, {forceNew: true})
	
	this.socket.on('ui', function(data) {
		if (typeof data == 'string' || data instanceof String) {
			data = JSON.parse(data)
		}
		
		// Look up the handler
		_this.streams[data.channel].handler(data)
	})
	
	this.socket.on('subscribed', function(data) {
		console.log("Subscribed to "+data.channels)
		$(_this).trigger('subscribed', [data.channels])
	})
	
	var onConnect = function() {
		console.log("Connected!")
		
		var subscriptions = []
		for (var streamId in _this.streams) {
			subscriptions.push({channel: streamId, options: _this.streams[streamId].options })
		}
		
		console.log("Subscribing to "+JSON.stringify(subscriptions))
		_this.socket.emit('subscribe', subscriptions)
	}
	
	// On connect/reconnect, send subscription requests
	this.socket.on('connect', onConnect)
	this.socket.on('disconnect', function() {
		console.log("Disconnected.")
	})

	this.connected = true
	return this.streams
}

StreamrClient.prototype.disconnect = function() {
	this.socket.disconnect()
	this.streams = {}
	this.connected = false
}

StreamrClient.prototype.handleResponse = function(message, streamId, callback) {
	var stream = this.streams[streamId]
	
	// If no counter is present, this is the purged empty message that 
	// should not be processed but must increment counter
	if (message.counter==null) {
		stream.counter++;
		return
	}
	
	// Update ack counter
	if (message.counter > stream.counter) {
		this.disconnect()
		throw "Messages SKIPPED! Counter: "+message.counter+", expected: "+stream.counter
	}
	else if (message.counter < stream.counter) {
		console.log("Already received message: "+message.counter+", expecting: "+stream.counter);
		return // ok?
	}
	
	stream.counter = message.counter + 1;
	
	callback(message);
}

exports.StreamrClient = StreamrClient

})(typeof(exports) !== 'undefined' ? exports : window)