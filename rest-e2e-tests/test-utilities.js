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

const cachedSessionTokens = {}

async function getSessionToken(privateKey) {
	if (cachedSessionTokens[privateKey] === undefined) {
		const client = new StreamrClient({
			restUrl: REST_URL,
			auth: {
				privateKey
			},
		})
		const sessionToken = await client.session.getSessionToken()
		cachedSessionTokens[privateKey] = sessionToken
		return sessionToken
	} else {
		return cachedSessionTokens[privateKey]
	}
}

module.exports = {
	assertResponseIsError: assertResponseIsError,
	getSessionToken: getSessionToken
}