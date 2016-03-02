var assert = require('assert')

var StreamrClient = require('../../streamr-atmosphere-client/streamr-atmosphere-client').StreamrClient

describe('StreamrClient', function() {
	var client

	before(function() {
		global.window = {}
		global.Streamr = {}
	})

	beforeEach(function() {
		
		global.$ = function(o) {

		}
		
		global.$.extend = function(o) {
			return o
		}
		
		global.$.atmosphere = {
			subscribe: function(request) {
				return "mocked socket"
			},
			unsubscribe: function() {}
		}

		client = new StreamrClient()
	})

	it('should call the callback when a message is received', function(done) {
		var subscription = client.subscribe("stream1", function(message) {
			assert.equal(message.counter, 0)
			done()
		})
		client.connect()
		
		global.$.parseJSON = function(str) {
			return {messages:[{counter:0}]}
		}
		
		// Fake message
		subscription.handler({
			status: 200,
			responseBody: "msg"
		})
	})
	
	it('should throw exception if counter skips', function(done) {
		var subscription = client.subscribe("stream1", function(message) {
			console.log("Callback called for message "+message.counter)
		})
		client.connect()
		
		global.$.parseJSON = function(str) {
			return {messages:[{counter:0}, {counter:2}]}
		}
		
		assert.throws(function() {
			subscription.handler({
				status: 200,
				responseBody: "msg"
			})			
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
		
		global.$.parseJSON = function(str) {
			return {messages:[{counter:0}, {counter:1}, {counter:2}]}
		}
		
		subscription.handler({
			status: 200,
			responseBody: "msg"
		})			
	})
	
	it('should increment counter on empty messages', function(done) {
		var count = 0
		var subscription = client.subscribe("stream1", function(message) {
			console.log("Count: "+count)
			if (message.counter === 2)
				done()
		})
		client.connect()
		
		global.$.parseJSON = function(str) {
			return {messages:[{counter:0}, {}, {counter:2}]}
		}
		
		subscription.handler({
			status: 200,
			responseBody: "msg"
		})			
	})
	
	it('should reset subscriptions on disconnect', function(done) {
		client.subscribe("stream1", function(message) {})
		client.connect()
		client.disconnect()
		client.subscribe("stream2", function(message) {})
		var streams = client.connect()
		var i=0
		for (var s in streams) {
			console.log(JSON.stringify(streams[s]))
			i++
		}
		assert.equal(i,1)
		done()
	})
	
	it('should set the request and socket when connected', function(done) {
		var subscription = client.subscribe("stream1", function(message) {})
		client.connect()
		assert(subscription.socket)
		assert(subscription.request)
		
		done()
	})
	
	it('should disconnect the socket when disconnected', function(done) {
		var subscription = client.subscribe("stream1", function(message) {})
		client.connect()
		$.atmosphere.unsubscribe = done
		client.disconnect()
	})
	
	it('should report that its connected after connecting', function(done) {
		client.subscribe("stream1", function(message) {})
		client.connect()
		assert(client.isConnected())
		done()
	})

	it('should report that its not connected after disconnecting', function(done) {
		client.subscribe("stream1", function(message) {})
		client.connect()
		client.disconnect()
		assert(!client.isConnected())
		done()
	})
	
})

