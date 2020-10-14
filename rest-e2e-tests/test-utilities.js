const assert = require('chai').assert
const StreamrClient = require('streamr-client')

async function assertResponseIsError(response, statusCode, programmaticCode, includeInMessage) {
    const json = await response.json()
    assert.equal(response.status, statusCode)
    assert.equal(json.code, programmaticCode)
    if (includeInMessage) {
        assert.include(json.message, includeInMessage)
    }
}

async function newSessionToken(restURL, privateKey) {
	const client = new StreamrClient({
		restUrl: restURL,
		auth: {
			privateKey
		},
	})
	await client.connect()

	const sessionToken = await client.session.getSessionToken()
	if (client.isConnected()) {
		await client.disconnect()
	}

	return sessionToken
}

module.exports = {
	assertResponseIsError: assertResponseIsError,
	newSessionToken: newSessionToken
}