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
		options: options || {},
		queue: [],
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

	this.socket.on('resent', function(data) {
		var stream = _this.streams[data.channel]
		stream.resending = false
		
		console.log("Channel resend complete: "+data.channel)
		
		if (stream.queue.length) {
			console.log("Attempting to process "+stream.queue.length+" queued messages for channel "+data.channel)
			
			var i
			for (i=0;i<stream.queue.length;i++) {
				// If the counter is correct, process the message
				if (stream.queue[i].counter === stream.counter)
					stream.handler(stream.queue[i])
				// Ignore old messages in the queue
				else if (stream.queue[i].counter < stream.counter)
					continue
				// Else stop looping
				else if (stream.queue[i].counter > stream.counter)
					break
			}
			
			// All messages in queue were processed
			if (i===stream.queue.length) {
				stream.queue = []
			}
			// Some messages could not be processed, so compact the queue 
			// and request another resend for the gap!
			else {
				stream.queue.splice(0, i)
				_this.requestResend(data.channel, stream.counter, stream.queue[0].counter-1)
			}
		}
	})
	
	var onConnect = function() {
		console.log("Connected!")
		
		var subscriptions = []
		for (var streamId in _this.streams) {
			var stream = _this.streams[streamId]
			var sub = {channel: streamId, options: stream.options }
			
			// Change resend_all -> resend_from mode if messages have already been received
			if (stream.counter && (stream.options.resend_all || stream.options.resend_from!=null)) {
				delete sub.options.resend_all
				sub.options.resend_from = stream.counter
			}
			
			subscriptions.push(sub)
			
			if (stream.options.resend_all || stream.options.resend_from || stream.options.resend_last) {
				console.log("Waiting for resend for channel "+streamId)
				stream.resending = true
			}
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

StreamrClient.prototype.requestResend = function(streamId, from, to) {
	var stream = this.streams[streamId]
	stream.resending = true
	
	console.log("Requesting resend for "+streamId+" from "+from+" to "+to)
	this.socket.emit('resend', {channel:streamId, from:from, to:to})
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
		stream.queue.push(message)
		
		if (!stream.resending) {
			console.log("Gap detected, requesting resend for channel "+streamId)
			this.requestResend(streamId, stream.counter, message.counter-1)
		}
	}
	else if (message.counter < stream.counter) {
		console.log("Already received message: "+message.counter+", expecting: "+stream.counter);
	}
	else {
		stream.counter = message.counter + 1;
		callback(message);
	}
}

exports.StreamrClient = StreamrClient

})(typeof(exports) !== 'undefined' ? exports : window)