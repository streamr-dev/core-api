const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')

const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const AUTH_TOKEN = 'product-api-tester-key'
const DEVOPS_USER_TOKEN = 'devops-user-key'

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)

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

describe('Subscriptions API', () => {
    describe('POST /api/v1/subscriptions', () => {

        let subscriptionBody = {}
        let productId

        before(async () => {
            productId = await createProductAndReturnId({
                name: 'Product',
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
        })

        beforeEach(() => {
            subscriptionBody = {
                product: productId,
                address: '0x0000000000000000000000000000000000000000',
                endsAt: 1520840312
            }
        })

        it('requires authentication', async () => {
            const body = subscriptionBody
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
                ...subscriptionBody,
                product: 'non-existing-product'
            }
            const response = await Streamr.api.v1.subscriptions
                .create(body)
                .withAuthToken(AUTH_TOKEN)
                .call()
            await assertResponseIsError(response, 422, 'VALIDATION_ERROR', 'product')
        })

        it('requires DevOps role', async () => {
            const body = subscriptionBody
            const response = await Streamr.api.v1.subscriptions
                .create(body)
                .withAuthToken(AUTH_TOKEN)
                .call()
            await assertResponseIsError(response, 403, 'FORBIDDEN', 'DevOps role required')
        })

        it('responds with 200 given valid parameters', async () => {
            const body = subscriptionBody
            const response = await Streamr.api.v1.subscriptions
                .create(body)
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()
            assert.equal(response.status, 204)
        })
    })
})
