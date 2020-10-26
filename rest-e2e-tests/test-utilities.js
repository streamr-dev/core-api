const assert = require('chai').assert
const StreamrClient = require('streamr-client')

const REST_URL = 'http://localhost/api/v1'

async function assertResponseIsError(response, statusCode, programmaticCode, includeInMessage) {
    const json = await response.json()
    assert.equal(response.status, statusCode)
    assert.equal(json.code, programmaticCode)
    if (includeInMessage) {
        assert.include(json.message, includeInMessage)
    }
}

async function newSessionToken(privateKey) {
	const client = new StreamrClient({
		restUrl: REST_URL,
		auth: {
			privateKey
		},
	})
	return await client.session.getSessionToken()
}

module.exports = {
	assertResponseIsError: assertResponseIsError,
	newSessionToken: newSessionToken
}