var assert = require('assert')

global.window = {

}

global.Streamr = {

}

function Eventor() {
	var listeners = {}
	return {
		on: function(en, cb) {
			if (!listeners[en])
				listeners[en] = [cb]
			else listeners[en].push(cb)
		},
		off: function() {},
		trigger: function(e, d) {
			if (listeners[e]) {
				listeners[e].forEach(function(cb) {
					cb(d)
				})
			}
		}
	}
}

var StreamrClient = require('../streamr-client/streamr-socketio-client').StreamrClient

describe('StreamrClient', function() {
	var client
	var socket

	beforeEach(function() {
		
		global.$ = function(o) {

		}
		
		global.$.extend = function(o) {
			return o
		}
		
		socket = new Eventor()
		socket.emit = function() {}
		socket.disconnect = function() {}
		
		global.io = function() {
			return socket
		}

		client = new StreamrClient()
	})
	
	it('should emit a subscribe event when connected', function(done) {
		var subscription = client.subscribe("stream1", function(message) {
			assert.equal(message.counter, 0)
			done()
		})
		socket.emit = function(e, data) {
			if (e==='subscribe' && data.channels.length===1 && data.channels[0]==='stream1')
				done()
		}
		
		client.connect()
		socket.trigger('connect')
	})
	
	it('should emit a subscribe event when reconnected', function(done) {
		var subscription = client.subscribe("stream1", function(message) {
			assert.equal(message.counter, 0)
			done()
		})
		socket.emit = function(e, data) {
			if (e==='subscribe' && data.channels.length===1 && data.channels[0]==='stream1')
				done()
		}
		
		client.connect()
		socket.trigger('reconnect')
	})

	it('should call the callback when a message is received', function(done) {
		var subscription = client.subscribe("stream1", function(message) {
			assert.equal(message.counter, 0)
			done()
		})
		client.connect()
		socket.trigger('connect')
		
		// Fake message
		socket.trigger('ui', {channel:"stream1", counter:0})
	})
	
	it('should throw exception if counter skips', function(done) {
		var subscription = client.subscribe("stream1", function(message) {
			console.log("Callback called for message "+message.counter)
		})
		client.connect()
		socket.trigger('connect')
		
		assert.throws(function() {
			socket.trigger('ui', {channel:"stream1", counter:0})
			socket.trigger('ui', {channel:"stream1", counter:2})
		})
		done()
	})
	
	it('should call the callback once for each message in order', function(done) {
		var count = 0
		var subscription = client.subscribe("stream1", function(message) {
			console.log("Count: "+count+", message: "+message.counter)
			
			if (message.counter !== count)
				throw "Message counter: "+message.counter+", expected: "+count
				
			if (++count === 3)
				done()
		})
		client.connect()
		socket.trigger('connect')
		
		socket.trigger('ui', {channel:"stream1", counter:0})
		socket.trigger('ui', {channel:"stream1", counter:1})
		socket.trigger('ui', {channel:"stream1", counter:2})
	})
	
	it('should reset subscriptions on disconnect', function(done) {
		client.subscribe("stream1", function(message) {})
		client.connect()
		socket.trigger('connect')
		
		client.disconnect()
		socket.trigger('disconnect')
		
		client.subscribe("stream2", function(message) {})
		var streams = client.connect()
		socket.trigger('connect')
		
		var i=0
		for (var s in streams) {
			console.log(JSON.stringify(streams[s]))
			i++
		}
		assert.equal(i,1)
		done()
	})
	
	it('should disconnect the socket when disconnected', function(done) {
		var subscription = client.subscribe("stream1", function(message) {})
		client.connect()
		socket.trigger('connect')
		socket.disconnect = done
		client.disconnect()
		socket.trigger('disconnect')
	})
	
	it('should report that its connected after connecting', function(done) {
		client.subscribe("stream1", function(message) {})
		client.connect()
		socket.trigger('connect')
		assert(client.isConnected())
		done()
	})

	it('should report that its not connected after disconnecting', function(done) {
		client.subscribe("stream1", function(message) {})
		client.connect()
		socket.trigger('connect')
		client.disconnect()
		socket.trigger('disconnect')
		assert(!client.isConnected())
		done()
	})
	
})

