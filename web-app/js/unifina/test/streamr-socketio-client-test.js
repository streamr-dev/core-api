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
	
	it('should emit a subscribe event on connect/reconnect', function(done) {
		var subscription = client.subscribe("stream1", function(message) {})
		socket.emit = function(e, subscriptions) {
			if (e==='subscribe' && subscriptions.length===1 && subscriptions[0].channel==='stream1')
				done()
		}
		
		client.connect()
		socket.trigger('connect')
	})
	
	it('should include options in the subscription message', function(done) {
		var subscription = client.subscribe("stream1", function(message) {}, {resend:true})
		socket.emit = function(e, subscriptions) {
			if (e==='subscribe' && subscription.options.resend)
				done()
		}
		
		client.connect()
		socket.trigger('connect')
	})

	it('should call the callback when a message is received with correct counter', function(done) {
		var subscription = client.subscribe("stream1", function(message) {
			assert.equal(message.counter, 0)
			done()
		})
		client.connect()
		socket.trigger('connect')
		
		// Fake message
		socket.trigger('ui', {channel:"stream1", counter:0})
	})
	
	it('should not call the callback nor throw an exception when a message is re-received', function(done) {
		var callbackCounter = 0
		var subscription = client.subscribe("stream1", function(message) {
			assert.equal(message.counter, 0)
			callbackCounter++
			if (callbackCounter>1)
				throw "Callback called more than once!"
		})
		client.connect()
		socket.trigger('connect')
		
		// Fake messages
		socket.trigger('ui', {channel:"stream1", counter:0})
		socket.trigger('ui', {channel:"stream1", counter:0})
		socket.trigger('ui', {channel:"stream1", counter:0})
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
	
	describe("resend/gapfill functionality", function() {
		var validResendRequests
		var resendRequestCount
		
		function checkResendRequest(options, idx) {
			var el = validResendRequests[idx]

			if (el.channel===options.channel && el.from===options.from && el.to===options.to)
				return true
			else throw "Illegal resend request: "+JSON.stringify(options)
		}
		
		// Setup a resend response mock
		beforeEach(function() {
			validResendRequests = []
			resendRequestCount = 0
			
			socket.emit = function(event, options) {
				if (event==="resend") {
					// Check that the request is allowed
					checkResendRequest(options, resendRequestCount++)
					
					setTimeout(function() {
						for (var i=options.from;i<=options.to;i++) {
							socket.trigger('ui', {channel:options.channel, counter:i})
						}
						socket.trigger('resent', {channel: options.channel, from:options.from, to:options.to})
					}, 0)
				}
			}
		})
		
		it('should emit a resend request if the first message has a non-zero counter', function(done) {
			var subscription = client.subscribe("stream1", function(message) {})
			client.connect()
			socket.trigger('connect')
			
			validResendRequests.push({channel:"stream1", from:0, to:1})
			socket.emit = function(event, options) {
				checkResendRequest(options,0)
				done()
			}
			
			socket.trigger('ui', {channel:"stream1", counter:2})
		})
		
		it('should emit a resend request if there is a gap in messages', function(done) {
			var subscription = client.subscribe("stream1", function(message) {})
			client.connect()
			socket.trigger('connect')
			
			validResendRequests.push({channel:"stream1", from:1, to:9})
			socket.emit = function(event, options) {
				checkResendRequest(options,0)
				done()
			}
			
			socket.trigger('ui', {channel:"stream1", counter:0})
			socket.trigger('ui', {channel:"stream1", counter:10})
		})
		
		it('should not emit another resend request while waiting for resend', function(done) {
			var subscription = client.subscribe("stream1", function(message) {})
			client.connect()
			socket.trigger('connect')
	
			var emitCounter = 0
			socket.emit = function(event, options) {
				emitCounter++
			}
			
			socket.trigger('ui', {channel:"stream1", counter:0})
			socket.trigger('ui', {channel:"stream1", counter:10})
			socket.trigger('ui', {channel:"stream1", counter:11})
			if (emitCounter!==1)
				throw "emitCounter is "+emitCounter+", expected 1"
			else done()
		})
		
		it('should process queued messages when the resend is complete', function(done) {
			var subscription = client.subscribe("stream1", function(message) {
				if (message.counter===12)
					done()
			})
			client.connect()
			socket.trigger('connect')
	
			validResendRequests.push({channel:"stream1", from:1, to:9})
			
			socket.trigger('ui', {channel:"stream1", counter:0})
			socket.trigger('ui', {channel:"stream1", counter:10})
			socket.trigger('ui', {channel:"stream1", counter:11})
			socket.trigger('ui', {channel:"stream1", counter:12})
		})
		
		it('should ignore retransmissions in the queue', function(done) {
			var subscription = client.subscribe("stream1", function(message) {
				if (message.counter===12)
					done()
			})
			client.connect()
			socket.trigger('connect')
	
			validResendRequests.push({channel:"stream1", from:1, to:9})
			
			socket.trigger('ui', {channel:"stream1", counter:0})
			socket.trigger('ui', {channel:"stream1", counter:10})
			socket.trigger('ui', {channel:"stream1", counter:11})
			socket.trigger('ui', {channel:"stream1", counter:11}) // bogus message
			socket.trigger('ui', {channel:"stream1", counter:5}) // bogus message
			socket.trigger('ui', {channel:"stream1", counter:12})
		})
		
		it('should do another resend request if there are gaps in the queue', function(done) {
			var subscription = client.subscribe("stream1", function(message) {
				if (message.counter===12)
					done()
			})
			client.connect()
			socket.trigger('connect')
	
			validResendRequests.push({channel:"stream1", from:1, to:9})
			validResendRequests.push({channel:"stream1", from:11, to:11})
			
			socket.trigger('ui', {channel:"stream1", counter:0})
			socket.trigger('ui', {channel:"stream1", counter:10})
			socket.trigger('ui', {channel:"stream1", counter:12})
		})
		
		it('should re-request from the latest counter on reconnect', function(done) {
			client.subscribe("stream1", function(message) {}, {resend_all:true})
			client.subscribe("stream2", function(message) {}, {resend_from:0})
			client.subscribe("stream3", function(message) {}) // no resend for stream3
			client.connect()
			socket.trigger('connect')
			
			socket.trigger('ui', {channel:"stream1", counter:0})
			socket.trigger('ui', {channel:"stream1", counter:1})
			socket.trigger('ui', {channel:"stream1", counter:2})
			
			socket.trigger('ui', {channel:"stream2", counter:0})
			socket.trigger('ui', {channel:"stream3", counter:0})
				
			socket.emit = function(event, data) {				
				if (event==="subscribe"
					&& !data[0].options.resend_all
					&& data[0].options.resend_from===3
					&& !data[1].options.resend_all
					&& data[1].options.resend_from===1
					&& !data[2].options.resend_from) {
					done()
				}
			}
			
			socket.trigger('connect')
		})

		it('should set the expected counter to what the expect message says', function(done) {
			var sub = client.subscribe("stream1", function(message) {}, {resend_all:true})
			client.connect()
			socket.trigger('connect')
			socket.trigger('expect', {channel: "stream1", from: 10})
			assert.equal(sub.counter, 10)
			done()
		})
	})	
})

