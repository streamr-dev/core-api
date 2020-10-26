const assert = require('chai').assert
const _ = require('lodash');
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

const testUsers = _.mapValues({
	// user_id=6 in the test DB, has ROLE_DEV_OPS authority
	devOpsUser: '0x628acb12df34bb30a0b2f95ec2e6a743b386c5d4f63aa9f338bec6f613160e78'
}, privateKey => ( { privateKey } ))

module.exports = {
	assertResponseIsError: assertResponseIsError,
	getSessionToken: getSessionToken,
	testUsers: testUsers
}