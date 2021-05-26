import {assert} from 'chai'
import _ from 'lodash';
import {StreamrClient, StreamrClientOptions} from 'streamr-client'
import {Wallet} from '@ethersproject/wallet'
import {EthereumAccount} from './EthereumAccount';

export const assertResponseIsError = async (response: any, statusCode: number, programmaticCode: string, includeInMessage?: string) => {
	const json = await response.json()
	assert.equal(response.status, statusCode)
	assert.equal(json.code, programmaticCode)
	if (includeInMessage) {
		assert.include(json.message, includeInMessage)
	}
}

export const assertStreamrClientResponseError = async (request: any, expectedStatusCode: number) => {
	return request
		.then(() => {
			assert.fail('Should response with an error code')
		})
		.catch((e: any) => {
			assert.equal(e.response.status, expectedStatusCode)
		})
}

export const assertEqualEthereumAddresses = (actual: string | undefined, expected: string) => {
	const normalized = (address: string | undefined) => address ? address.toLowerCase : address
	assert.equal(normalized(actual), normalized(expected))
}

export const getStreamrClient = (user?: EthereumAccount) => {
	const options: StreamrClientOptions = {
		url: 'ws://localhost/api/v1/ws',
		restUrl: 'http://localhost/api/v1',
		tokenAddress: '0xbAA81A0179015bE47Ad439566374F2Bae098686F',
		tokenSidechainAddress: '0x73Be21733CC5D08e1a14Ea9a399fb27DB3BEf8fF',
		dataUnion: {
			factoryMainnetAddress: '0x4bbcBeFBEC587f6C4AF9AF9B48847caEa1Fe81dA',
			factorySidechainAddress: '0x4A4c4759eb3b7ABee079f832850cD3D0dC48D927',
			templateMainnetAddress: '0x7bFBAe10AE5b5eF45e2aC396E0E605F6658eF3Bc',
			templateSidechainAddress: '0x36afc8c9283CC866b8EB6a61C6e6862a83cd6ee8',
		},
		sidechain: {
			url: 'http://localhost:8546'
		},
		mainnet: {
			url: 'http://localhost:8545'
		},
		autoConnect: false,
		autoDisconnect: false
	}
	if (user !== undefined) {
		options.auth = {
			privateKey: user.privateKey
		}
	}
	return new StreamrClient(options)
}

const cachedSessionTokens: any = {}

export const getSessionToken = async (user: EthereumAccount) => {
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

export const testUsers = _.mapValues({
	devOpsUser: '0x628acb12df34bb30a0b2f95ec2e6a743b386c5d4f63aa9f338bec6f613160e78',     // user_id=6 in the test DB, has ROLE_DEV_OPS authority
	tokenHolder: '0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0',    // owns tokens in dev mainchain and sidechain
	ensDomainOwner: '0xe5af7834455b7239881b85be89d905d6881dcb4751063897f12be1b0dd546bdb'  // owns testdomain1.eth ENS domain in dev mainchain
}, (privateKey: string) => ({
	privateKey,
	address: new Wallet(privateKey).address
}))
