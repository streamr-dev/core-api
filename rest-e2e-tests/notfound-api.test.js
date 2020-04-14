const assert = require('chai').assert
const StreamrClient = require('streamr-client')
const initStreamrApi = require('./streamr-api-clients')
const REST_URL = 'http://localhost:8081/streamr-core/api/v1'
const LOGGING_ENABLED = false
const Streamr = initStreamrApi(REST_URL, LOGGING_ENABLED)
const API_KEY = 'product-api-tester-key'

async function newSessionToken() {
    // Generate a new user to isolate the test and not require any pre-existing resources
    const freshUser = StreamrClient.generateEthereumAccount()
    const client = new StreamrClient({
        restUrl: REST_URL,
        auth: {
            privateKey: freshUser.privateKey,
        },
    })
    await client.connect()

    const sessionToken = await client.session.getSessionToken()
    if (client.isConnected()) {
        await client.disconnect()
    }

    return sessionToken
}

describe('REST API', function() {
    describe('GET /api/v1/page-not-found', function() {
        const assertContentLengthIsZero = async function (response) {
            const body = await response.text()
            const bodyLenBytes = body.length
            assert.equal(bodyLenBytes, 0)

            const cl = response.headers.get('Content-Length')
            assert.equal(cl, 0)
        }
        it('anonymous access responds with 404', async () => {
            const response = await Streamr.api.v1.not_found.notFound().call()
            assert.equal(response.status, 404)
            await assertContentLengthIsZero(response)
        })
        it('with auth token responds with 404', async () => {
            const response = await Streamr.api.v1.not_found.withApiKey(API_KEY).notFound().call()
            assert.equal(response.status, 404)
            await assertContentLengthIsZero(response)
        })
        it('with session token responds with 404', async () => {
            const sessionToken = await newSessionToken()
            const response = await Streamr.api.v1.not_found.withSessionToken(sessionToken).notFound().call()
            assert.equal(response.status, 404)
            await assertContentLengthIsZero(response)
        })
    })
})
