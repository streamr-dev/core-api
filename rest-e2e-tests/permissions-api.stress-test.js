const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')
const StreamrClient = require('streamr-client')
const URL = 'http://localhost/api/v1/'
const LOGGING_ENABLED = false
const Streamr = initStreamrApi(URL, LOGGING_ENABLED)

describe('POST /api/v1/streams/{id}/permissions', function() {

	this.timeout(200 * 1000)

	let stream
	let mySessionToken

	before(async () => {
		const me = StreamrClient.generateEthereumAccount()
		mySessionToken = await new StreamrClient({
			restUrl: URL,
			auth: {
				privateKey: me.privateKey,
			}
		}).session.getSessionToken()
		stream = await Streamr.api.v1.streams
			.create({
				name: `permissions-api.test.js-${Date.now()}`
			})
			.withSessionToken(mySessionToken)
			.execute()
	})

	describe('race conditions', () => {
		// The worst case is that there are parallel requests open for all the different operations
		const operations = [
			'stream_get',
			'stream_edit',
			'stream_subscribe',
			'stream_publish',
			'stream_delete',
			'stream_share',
		]

		// Tests here are repeated 50 times, as they have some chance of an individual attempt
		// succeeding even if the race condition is not handled properly
		const ITERATIONS = 50

		it('survives a race condition when granting multiple permissions to a non-existing user using Ethereum address', async () => {
			for (let i=0; i<ITERATIONS; i++) {
				console.log("\titeration: " + (i + 1))
				const responses = await Promise.all(operations.map((operation) => {
					return Streamr.api.v1.streams
						.grant(stream.id, StreamrClient.generateEthereumAccount().address, operation)
						.withSessionToken(mySessionToken)
						.call()
				}))
				// All response statuses must be 200
				assert.deepEqual(responses.map((r) => r.status), operations.map((op) => 200), `Race condition test failed on iteration ${i}`)
			}
		})

		it('survives a race condition when granting multiple permissions to a non-existing user using email address', async () => {
			for (let i=0; i<ITERATIONS; i++) {
				console.log("\titeration: " + (i + 1))
				const responses = await Promise.all(operations.map((operation) => {
					return Streamr.api.v1.streams
						.grant(stream.id, `race-condition-${i}@foobar.invalid`, 'stream_get')
						.withSessionToken(mySessionToken)
						.call()
				}))
				// All response statuses must be 200
				assert.deepEqual(responses.map((r) => r.status), operations.map((op) => 200), `Race condition test failed on iteration ${i}`)
			}
		})
	})

})