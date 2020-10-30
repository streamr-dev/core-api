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

const assertStreamrClientResponseError = async (request, expectedStatusCode) => {
	return request
		.then(() => {
			assert.fail('Should response with an error code')
		})
		.catch(e => {
			assert.equal(e.response.status, expectedStatusCode)
		})
}

const getStreamrClient = (user) => {
	return new StreamrClient({
		restUrl: REST_URL,
		auth: {
			privateKey: user.privateKey
		}
	})
}

const cachedSessionTokens = {}

async function getSessionToken(user) {
	const privateKey = user.privateKey
	if (cachedSessionTokens[privateKey] === undefined) {
		const client = getStreamrClient(user)
		const sessionToken = await client.session.getSessionToken()
		cachedSessionTokens[privateKey] = sessionToken
		return sessionToken
	} else {
		return cachedSessionTokens[privateKey]
	}
}

const testUsers = _.mapValues({
	devOpsUser: '0x628acb12df34bb30a0b2f95ec2e6a743b386c5d4f63aa9f338bec6f613160e78',     // user_id=6 in the test DB, has ROLE_DEV_OPS authority
	ensDomainOwner: '0xe5af7834455b7239881b85be89d905d6881dcb4751063897f12be1b0dd546bdb'  // owns testdomain1.eth ENS domain in dev mainchain
}, privateKey => ( { privateKey } ))

module.exports = {
	assertResponseIsError,
	assertStreamrClientResponseError,
	getSessionToken,
	testUsers,
	getStreamrClient
}