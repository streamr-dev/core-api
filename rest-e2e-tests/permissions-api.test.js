const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const StreamrClient = require('streamr-client')

const URL = 'http://localhost/api/v1/'
const LOGGING_ENABLED = false

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)
const schemaValidator = new SchemaValidator()

describe('Permissions API', () => {

    const me = StreamrClient.generateEthereumAccount()
    const nonExistingUser = StreamrClient.generateEthereumAccount()
    const existingUser = StreamrClient.generateEthereumAccount()

    describe('POST /api/v1/streams/{id}/permissions', () => {
        let stream

        before(async () => {
            stream = await Streamr.api.v1.streams
                .create({
                    name: `permissions-api.test.js-${Date.now()}`
                })
                .withPrivateKey(me.privateKey)
                .execute()

            // Make sure the "existingUser" exists
            await new StreamrClient({
                restUrl: URL,
                auth: {
                    privateKey: existingUser.privateKey,
                }
            }).session.getSessionToken()
        })

        it('can grant a permission to an existing user using email address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, 'tester1@streamr.com', 'stream_get')
                .withPrivateKey(me.privateKey)
                .call()
            assert.equal(response.status, 201)
        })

        it('can grant a permission to a non-existing user using email address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, `${Date.now()}@foobar.invalid`, 'stream_get')
                .withPrivateKey(me.privateKey)
                .call()
            assert.equal(response.status, 201)
        })

        it('can grant a permission to an existing user using Ethereum address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, existingUser.address, 'stream_get')
                .withPrivateKey(me.privateKey)
                .call()
            assert.equal(response.status, 201)
        })

        it('can grant a permission to a non-existing user using Ethereum address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, StreamrClient.generateEthereumAccount().address, 'stream_get')
                .withPrivateKey(me.privateKey)
                .call()
            assert.equal(response.status, 201)
        })

    })
})
