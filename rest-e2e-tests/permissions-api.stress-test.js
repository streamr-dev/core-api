const assert = require('chai').assert
const Streamr = require('./streamr-api-clients')
const StreamrClient = require('streamr-client')
const getStreamrClient = require('./test-utilities.js').getStreamrClient

describe('POST /api/v1/streams/{id}/permissions', function() {

	this.timeout(200 * 1000)

	let stream
	const me = StreamrClient.generateEthereumAccount()

	before(async () => {
		stream = await getStreamrClient(me).createStream({
			name: `permissions-api.test.js-${Date.now()}`
		})
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
		const assertSuccessResponses = (responses) => {
			const httpStatuses = responses.map((r) => r.status)
			const fails = httpStatuses.filter(status => status !== 200)
			if (fails.length > 0) {
				assert.fail(`Race condition test failed on ${fails.length}/${ITERATIONS} iterations: [${fails}]`)
			}
		}

		it('survives a race condition when granting multiple permissions to a non-existing user using Ethereum address', async () => {
			for (let i=0; i<ITERATIONS; i++) {
				const responses = await Promise.all(operations.map((operation) => {
					return Streamr.api.v1.streams
						.grant(stream.id, StreamrClient.generateEthereumAccount().address, operation)
						.withAuthenticatedUser(me)
						.call()
				}))
				assertSuccessResponses(responses)
			}
		})

		it('survives a race condition when granting multiple permissions to a non-existing user using email address', async () => {
			for (let i=0; i<ITERATIONS; i++) {
				const responses = await Promise.all(operations.map((operation) => {
					return Streamr.api.v1.streams
						.grant(stream.id, `race-condition-${i}@foobar.invalid`, operation)
						.withAuthenticatedUser(me)
						.call()
				}))
				assertSuccessResponses(responses)
			}
		})
	})

})
