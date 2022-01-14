import {assert} from 'chai'
import _ from 'lodash';
import {Wallet} from '@ethersproject/wallet'
import {EthereumAccount} from './EthereumAccount';
import Streamr from "./streamr-api-clients";
import {ConfigTest, StreamrClient, StreamrClientOptions} from 'streamr-client'

export const assertResponseIsError = async (response: any, statusCode: number, programmaticCode: string, includeInMessage?: string) => {
    const json = await response.json()
    assert.equal(response.status, statusCode)
    assert.equal(json.code, programmaticCode)
    if (includeInMessage) {
        assert.include(json.message, includeInMessage)
    }
}

export const assertStreamrClientResponseError = async (request: any, expectedStatusCode: number, programmaticCode: string, includeInMessage: string) => {
    return request
        .then(() => {
            assert.fail('Should response with an error code')
        })
        .catch((e: any) => {
            assert.equal(e.response.status, expectedStatusCode)
            assert.include(e.message, programmaticCode)
            assert.include(e.message, includeInMessage)
        })
}

export const assertEqualEthereumAddresses = (actual: string | undefined, expected: string) => {
    const normalized = (address: string | undefined) => address ? address.toLowerCase : address
    assert.equal(normalized(actual), normalized(expected))
}

export const getStreamrClient = (user?: EthereumAccount) => {
    const options: StreamrClientOptions = {
        ...ConfigTest,
        restUrl: 'http://localhost/api/v1',
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

export const createProduct = async (owner: EthereumAccount, beneficiaryAddress: string, productType: string) => {
    const properties = {
        name: 'product e2e test: ' + Date.now(),
        beneficiaryAddress,
        type: productType,
        owner: owner.address,
    }
    const json = await Streamr.api.v1.products
        .create(properties)
        .withAuthenticatedUser(owner)
        .execute()
    return json
}

