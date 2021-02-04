const assert = require('chai').assert
const Streamr = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const assertResponseIsError = require('./test-utilities.js').assertResponseIsError
const testUsers = require('./test-utilities.js').testUsers
const StreamrClient = require('streamr-client')

const schemaValidator = new SchemaValidator()

function assertIsSubscription(data) {
    const errors = schemaValidator.validateSubscription(data)
    assert(errors.length === 0, schemaValidator.toMessages(errors))
}

async function createProductAndReturnId(productBody, user) {
    const json = await Streamr.api.v1.products
        .create(productBody)
        .withAuthenticatedUser(user)
        .execute()
    return json.id
}

async function createSubscription(subscriptionBody, user) {
    await Streamr.api.v1.subscriptions
        .create(subscriptionBody)
        .withAuthenticatedUser(user)
        .execute()
}

describe('Subscriptions API', () => {

	const productOwner = StreamrClient.generateEthereumAccount()
	const subscriber = StreamrClient.generateEthereumAccount()
	const devOpsUser = testUsers.devOpsUser

    describe('POST /api/v1/subscriptions', () => {

        let paidProductId
        let freeProductId

        before(async () => {
            paidProductId = await createProductAndReturnId({
                name: 'Paid Product',
                description: 'Description of the product.',
                imageUrl: 'https://streamr.network/uploads/product.png',
                category: 'satellite-id',
                streams: [],
                ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                pricePerSecond: 5,
                priceCurrency: 'USD',
                minimumSubscriptionInSeconds: 60,
            }, productOwner)

            freeProductId = await createProductAndReturnId({
                name: 'Free Product',
                description: 'Description of the product.',
                imageUrl: 'https://streamr.network/uploads/product2.png',
                category: 'satellite-id',
                streams: [],
                pricePerSecond: 0,
                priceCurrency: 'DATA',
                minimumSubscriptionInSeconds: 30,
            }, productOwner)
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
                .withAuthenticatedUser(productOwner)
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
                .withAuthenticatedUser(productOwner)
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
                    .withAuthenticatedUser(productOwner)
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
                    .withAuthenticatedUser(devOpsUser)
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
                    .withAuthenticatedUser(productOwner)
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
                    .withAuthenticatedUser(productOwner)
                    .call()
                assert.equal(response.status, 204)
            })
        })
    })

    describe('GET /api/v1/subscriptions', () => {
        let productId

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
            }, productOwner)

            await createSubscription({
                product: productId,
                address: '0x0000000000000000000000000000000000000000',
                endsAt: 1520840312
            }, devOpsUser)

            await createSubscription({
                product: productId,
                address: '0x0000000000000000000000000000000000000005',
                endsAt: 1570840312
            }, devOpsUser)

            await createSubscription({
                product: productId,
                address: subscriber.address,
                endsAt: 1540840312
            }, devOpsUser)
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
                    .withAuthenticatedUser(subscriber)
                    .call()
                json = await response.json()
            })

            it ('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with list of subscriptions', () => {
                assert.isAtLeast(json.length, 1)
                json.forEach(subscriptionData => assertIsSubscription(subscriptionData))
                const picked = json.find(subscriptionData => subscriptionData.address === subscriber.address)
                assert.isNotNull(picked)
                assert.deepEqual(picked.endsAt, '2018-10-29T19:11:52Z')
                assert.deepEqual(picked.product.id, productId)
            })
        })
    })
})
