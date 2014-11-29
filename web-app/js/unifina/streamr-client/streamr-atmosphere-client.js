(function(exports) {

function StreamrClient(options) {
	// Default options
	this.options = {
		atmosphereUrl: Streamr.projectWebroot+"atmosphere/"
	}
	this.streams = {}
	
    this.detectedTransport = null
    this.connected = false
    
    this.atmosphereEndTag = "<!-- EOD -->"
    
	$.atmosphere.logLevel = 'error'
	$.extend(this.options, options)
}

StreamrClient.prototype.subscribe = function(streamId, callback) {
	var _this = this
	this.streams[streamId] = {
		handler: function(response) {
			_this.handleResponse(response, streamId, callback)
		},
		counter: 0,
		socket: null
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
	
	for (var streamId in this.streams) {
		
		// Restart stream unless reconnecting
		if (!reconnect)
			this.streams[streamId].counter = 0
		
		var request = { 
			url : this.options.atmosphereUrl+streamId,
			transport: 'websocket',
			fallbackTransport: 'long-polling',
			executeCallbackBeforeReconnect : true,
			maxRequest: Number.MAX_VALUE,
			headers: {
				"X-C": function() {
					return _this.streams[streamId].counter
				}
			},
			onMessage: this.streams[streamId].handler
		}
		
		this.streams[streamId].request = request
		this.streams[streamId].socket = $.atmosphere.subscribe(request)
	}
		
	this.connected = true
}

StreamrClient.prototype.disconnect = function() {
	$.atmosphere.unsubscribe()
	this.connected = false
}

StreamrClient.prototype.handleResponse = function(response, streamId, callback) {
	this.detectedTransport = response.transport;
	
	if (response.status == 200) {
        var data = response.responseBody;
        
        // Check for the non-js comments that atmosphere may write

        var end = data.indexOf(this.atmosphereEndTag);
        if (end != -1) {
        	data = data.substring(end+this.atmosphereEndTag.length, data.length);
        }
        
        if (data.length > 0) {
        	var jsonString = '{"messages":['+(data.substring(0,data.length-1))+']}';
        	
    		var clear = [];
    		var msgObj = null;

    		try {
    			msgObj = $.parseJSON(jsonString);
    		} catch (e) {
    			// Atmosphere sends commented out data to WebKit based browsers
    			// Atmosphere bug sometimes allows incomplete responses to reach the client
    			console.log(e);
    		}
    		
    		var stream = this.streams[streamId]
    		
    		if (msgObj != null) {
    			for (var i=0;i<msgObj.messages.length;i++) {
    				var message = msgObj.messages[i]
    				
    				// If no counter is present, this is the purged empty message that 
    				// should not be processed but must increment counter
    				if (message.counter==null) {
    					stream.counter++;
    					continue
    				}
    				
    				// Update ack counter
    				if (message.counter > stream.counter) {
    					throw "Messages SKIPPED! Counter: "+message.counter+", expected: "+stream.counter
    				}
    				else if (message.counter < stream.counter) {
    					console.log("Already received message: "+message.counter+", expecting: "+stream.counter);
    					continue // ok?
    				}
    				
    				stream.counter = message.counter + 1;
    				
        			callback(message);
    			}
    		}

        }
    }
}

exports.StreamrClient = StreamrClient

})(typeof(exports) !== 'undefined' ? exports : window)