const assert = require('chai').assert
const StreamrClient = require('streamr-client')
const Streamr = require('./streamr-api-clients')

describe('REST API', function() {

    const testUser = StreamrClient.generateEthereumAccount()

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
        it('with session token responds with 404', async () => {
            const response = await Streamr.api.v1.not_found.notFound()
                .withAuthenticatedUser(testUser)
                .call()
            assert.equal(response.status, 404)
            await assertContentLengthIsZero(response)
        })
        it('with session token responds with 404', async () => {
            // Generate a new user to isolate the test and not require any pre-existing resources
            const freshUser = StreamrClient.generateEthereumAccount()
            const response = await Streamr.api.v1.not_found.notFound().withAuthenticatedUser(freshUser).call()
            assert.equal(response.status, 404)
            await assertContentLengthIsZero(response)
        })
    })
})
