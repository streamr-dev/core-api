const assert = require('chai').assert
const Web3 = require('web3')
const keythereum = require('keythereum')
const ethereumJsUtil = require('ethereumjs-util')
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')

const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const AUTH_TOKEN = 'product-api-tester-key'
const AUTH_TOKEN_2 = 'product-api-tester2-key'
const DEVOPS_USER_TOKEN = 'devops-user-key'

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)
const schemaValidator = new SchemaValidator()

function assertIsSubscription(data) {
    const errors = schemaValidator.validateSubscription(data)
    assert(errors.length === 0, schemaValidator.toMessages(errors))
}

async function assertResponseIsError(response, statusCode, programmaticCode, includeInMessage) {
    const json = await response.json()
    assert.equal(response.status, statusCode)
    assert.equal(json.code, programmaticCode)
    if (includeInMessage) {
        assert.include(json.message, includeInMessage)
    }
}

async function createProductAndReturnId(productBody) {
    const json = await Streamr.api.v1.products
        .create(productBody)
        .withAuthToken(AUTH_TOKEN)
        .execute()
    return json.id
}

async function createSubscription(subscriptionBody) {
    await Streamr.api.v1.subscriptions
        .create(subscriptionBody)
        .withAuthToken(DEVOPS_USER_TOKEN)
        .execute()
}

async function obtainChallenge() {
    const json = await Streamr.api.v1.login
        .challenge()
        .execute()
    return json
}

async function submitChallenge(challenge, signature) {
    const json = await Streamr.api.v1.integration_keys
        .create({
            name: 'My Ethereum ID',
            service: 'ETHEREUM_ID',
            challenge: challenge,
            signature: signature
        })
        .withAuthToken(AUTH_TOKEN_2)
        .execute()
    return json
}

describe('Subscriptions API', () => {
    describe('POST /api/v1/subscriptions', () => {

        let paidProductId
        let freeProductId

        before(async () => {
            paidProductId = await createProductAndReturnId({
                name: 'Paid Product',
                description: 'Description of the product.',
                imageUrl: 'https://www.streamr.com/uploads/product.png',
                category: 'satellite-id',
                streams: [],
                ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                pricePerSecond: 5,
                priceCurrency: 'USD',
                minimumSubscriptionInSeconds: 60,
            })

            freeProductId = await createProductAndReturnId({
                name: 'Free Product',
                description: 'Description of the product.',
                imageUrl: 'https://www.streamr.com/uploads/product2.png',
                category: 'satellite-id',
                streams: [],
                pricePerSecond: 0,
                priceCurrency: 'DATA',
                minimumSubscriptionInSeconds: 30,
            })
        })

        it('requires authentication', async () => {
            const body = {
                product: paidProductId,
                endsAt: 1520840312
            }
            const response = await Streamr.api.v1.subscriptions
                .create(body)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('validates body', async () => {
            const body = {}
            const response = await Streamr.api.v1.subscriptions
                .create(body)
                .withAuthToken(AUTH_TOKEN)
                .call()
            await assertResponseIsError(response, 422, 'VALIDATION_ERROR')
        })

        it('validates existence of Product', async () => {
            const body = {
                product: 'non-existing-product',
                endsAt: 1520840312
            }
            const response = await Streamr.api.v1.subscriptions
                .create(body)
                .withAuthToken(AUTH_TOKEN)
                .call()
            await assertResponseIsError(response, 422, 'VALIDATION_ERROR', 'product')
        })

        context('when passing body with address', () => {
            it('requires DevOps role', async () => {
                const body = {
                    product: paidProductId,
                    endsAt: 1520840312,
                    address: '0x0000000000000000000000000000000000000000'
                }
                const response = await Streamr.api.v1.subscriptions
                    .create(body)
                    .withAuthToken(AUTH_TOKEN)
                    .call()
                await assertResponseIsError(response, 403, 'FORBIDDEN', 'DevOps role required')
            })

            it('responds with 204 given valid parameters', async () => {
                const body = {
                    product: paidProductId,
                    endsAt: 1520840312,
                    address: '0x0000000000000000000000000000000000000000'
                }
                const response = await Streamr.api.v1.subscriptions
                    .create(body)
                    .withAuthToken(DEVOPS_USER_TOKEN)
                    .call()
                assert.equal(response.status, 204)
            })
        })

        context('when passing body without address', () => {
            it('verifies that Product is free', async () => {
                const body = {
                    product: paidProductId,
                    endsAt: 1520840312
                }
                const response = await Streamr.api.v1.subscriptions
                    .create(body)
                    .withAuthToken(AUTH_TOKEN)
                    .call()
                await assertResponseIsError(response, 400, 'PRODUCT_IS_NOT_FREE')
            })

            it('responds with 204 given valid parameters', async () => {
                const body = {
                    product: freeProductId,
                    endsAt: 1520840312
                }
                const response = await Streamr.api.v1.subscriptions
                    .create(body)
                    .withAuthToken(AUTH_TOKEN)
                    .call()
                assert.equal(response.status, 204)
            })
        })
    })

    describe('GET /api/v1/subscriptions', () => {
        let productId
        let publicAddress

        before(async () => {
            productId = await createProductAndReturnId({
                name: 'Product',
                description: 'Description of the product.',
                category: 'satellite-id',
                streams: [],
                ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                pricePerSecond: 5,
                priceCurrency: 'USD',
                minimumSubscriptionInSeconds: 60,
            })

            const generatedKey = keythereum.create()
            const privateKey = '0x' + generatedKey.privateKey.toString('hex')
            publicAddress = '0x' + ethereumJsUtil.privateToAddress(generatedKey.privateKey).toString('hex')

            await createSubscription({
                product: productId,
                address: '0x0000000000000000000000000000000000000000',
                endsAt: 1520840312
            })

            await createSubscription({
                product: productId,
                address: '0x0000000000000000000000000000000000000005',
                endsAt: 1570840312
            })

            await createSubscription({
                product: productId,
                address: publicAddress,
                endsAt: 1540840312
            })

            const challenge = await obtainChallenge()
            const web3 = new Web3()

            const signedChallenge = web3.eth.accounts.sign(challenge.challenge, privateKey)

            const response = await submitChallenge(challenge, signedChallenge.signature)
            response
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.subscriptions
                .list()
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response
            let json

            before(async () => {
                response = await Streamr.api.v1.subscriptions
                    .list()
                    .withAuthToken(AUTH_TOKEN_2)
                    .call()
                json = await response.json()
            })

            it ('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with list of subscriptions', () => {
                assert.isAtLeast(json.length, 1)
                json.forEach(subscriptionData => assertIsSubscription(subscriptionData))
                const picked = json.find(subscriptionData => subscriptionData.address === publicAddress)
                assert.isNotNull(picked)
                assert.deepEqual(picked.endsAt, '2018-10-29T19:11:52Z')
                assert.deepEqual(picked.product.id, productId)
            })
        })
    })
})
