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
			_this.handleMessage(response, callback)
		},
		counter: 0,
		socket: null
	}
	
	return this.streams[streamId]
}

StreamrClient.prototype.isConnected = function() {
	return this.connected
}

StreamrClient.prototype.connect = function(reconnect) {
	if (this.connected)
		this.disconnect()
	
	for (var streamId in this.streams) {
		
		// Restart stream unless reconnecting
		if (!reconnect)
			this.streams[streamId].counter = 0
		
		var request = { 
			url : this.options.atmosphereUrl+streamId,
			transport: 'long-polling',
			fallbackTransport: 'long-polling',
			executeCallbackBeforeReconnect : true,
			maxRequest: Number.MAX_VALUE,
			headers: {
				"X-C": function() {
					return this.streams[streamId].counter
				}
			}
		}
		
		request.onMessage = this.streams[streamId].handler
		this.streams[streamId].socket = $.atmosphere.subscribe(request)
	}
		
	this.connected = true
}

StreamrClient.prototype.disconnect = function() {
	// ...
	this.connected = false
}

StreamrClient.prototype.handleMessage =	function(response, callback) {
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

    		if (msgObj != null)
    			callback(msgObj.messages);

        }
    }
}

exports.StreamrClient = StreamrClient

})(typeof(exports) !== 'undefined' ? exports : window)