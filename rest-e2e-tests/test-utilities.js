const assert = require('chai').assert
const _ = require('lodash');
const StreamrClient = require('streamr-client')

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

const assertEqualEthereumAddresses = (actual, expected) => {
	const normalized = address => address ? address.toLowerCase : address
	assert.equal(normalized(actual), normalized(expected))
}

const getStreamrClient = (user) => {
	return new StreamrClient({
		auth: {
			privateKey: user.privateKey
		},
		url: 'ws://localhost/api/v1/ws',
		restUrl: 'http://localhost/api/v1',
		tokenAddress: '0xbAA81A0179015bE47Ad439566374F2Bae098686F',
		tokenAddressSidechain: '0x73Be21733CC5D08e1a14Ea9a399fb27DB3BEf8fF',
		factoryMainnetAddress: '0x5E959e5d5F3813bE5c6CeA996a286F734cc9593b',
		sidechain: {
			url: 'http://localhost:8546'
		},
		mainnet: {
			url: 'http://localhost:8545'
		},
		autoConnect: false,
		autoDisconnect: false
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
	tokenHolder: '0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0',    // owns tokens in dev mainchain and sidechain
	ensDomainOwner: '0xe5af7834455b7239881b85be89d905d6881dcb4751063897f12be1b0dd546bdb'  // owns testdomain1.eth ENS domain in dev mainchain
}, privateKey => ( { privateKey } ))

module.exports = {
	assertResponseIsError,
	assertStreamrClientResponseError,
	assertEqualEthereumAddresses,
	getSessionToken,
	testUsers,
	getStreamrClient
}