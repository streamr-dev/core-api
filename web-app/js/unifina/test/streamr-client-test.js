
var assert = require('assert')

global.window = {

}

global.Streamr = {

}

var StreamrClient = require('../streamr-client/streamr-client').StreamrClient

describe('StreamrClient', function() {
	var client

	beforeEach(function() {
		
		global.$ = function(o) {

		}
		
		global.$.extend = function(o) {
			return o
		}
		
		global.$.atmosphere = {
			subscribe: function(request) {
				return "socket"
			}
		}

		client = new StreamrClient()
	})

	it('should call the callback when a message is received', function(done) {
		var subscription = client.subscribe("stream1", function(messages) {
			assert.equal(messages.length, 1)
			assert.equal(messages[0].msg, "foo")
			done()
		})
		client.connect()
		
		var msg = "{\"msg\":\"foo\"}"
		global.$.parseJSON = function(str) {
			return {messages:[{msg:"foo"}]}
		}
		
		// Fake message
		subscription.handler({
			status: 200,
			responseBody: msg
		})
	})

})

