const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const StreamrClient = require('streamr-client')

const URL = 'http://localhost/api/v1/'
const LOGGING_ENABLED = false

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)
const schemaValidator = new SchemaValidator()

const API_KEY = 'tester1-api-key'

describe('Permissions API', function() { // use "function" instead of arrow because of this.timeout(...)
    this.timeout(120 * 1000)

    const me = StreamrClient.generateEthereumAccount()
    const nonExistingUser = StreamrClient.generateEthereumAccount()
    const existingUser = StreamrClient.generateEthereumAccount()

    let mySessionToken

    before(async () => {
        // Get sessionToken for user "me"
        mySessionToken = await new StreamrClient({
            restUrl: URL,
            auth: {
                privateKey: me.privateKey,
            }
        }).session.getSessionToken()

        // Make sure the "existingUser" exists by logging them in
        await new StreamrClient({
            restUrl: URL,
            auth: {
                privateKey: existingUser.privateKey,
            }
        }).session.getSessionToken()
    })

    describe('POST /api/v1/streams/{id}/permissions', () => {
        let stream

        before(async () => {
            stream = await Streamr.api.v1.streams
                .create({
                    name: `permissions-api.test.js-${Date.now()}`
                })
                .withSessionToken(mySessionToken)
                .execute()
        })

        it('can grant a permission to an existing user using email address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, 'tester1@streamr.com', 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 200)
        })

        it('can grant a permission to a non-existing user using email address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, `${Date.now()}@foobar.invalid`, 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 200)
        })

        it('can grant a permission to an existing user using Ethereum address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, existingUser.address, 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 200)
        })

        it('can grant a permission to a non-existing user using Ethereum address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, StreamrClient.generateEthereumAccount().address, 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 200)
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
})
