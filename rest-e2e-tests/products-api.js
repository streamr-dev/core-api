const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')

const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const AUTH_TOKEN = 'product-api-tester-key'
const AUTH_TOKEN_2 = 'product-api-tester2-key'
const DEVOPS_USER_TOKEN = 'devops-user-key'


async function createProductAndReturnId(productBody) {
    const response = await Streamr.api.v1.products
        .create(productBody)
        .withAuthToken(AUTH_TOKEN)
        .call()
    const json = await response.json()
    return json.id
}


const Streamr = initStreamrApi(URL, LOGGING_ENABLED)

describe('Products API', () => {
    const genericProductBody = {
        name: 'Product',
        description: 'Description of the product.',
        imageUrl: 'product.png',
        category: 'satellite-id',
        ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
        beneficiaryAddress: '0x0000000000000000000011111111111111111111',
        pricePerSecond: 5,
        priceCurrency: 'USD',
        minimumSubscriptionInSeconds: 60
    }

    let createdProductId

    describe('POST /api/v1/products', () => {

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody)
        })

        it('requires authentication', async () => {
            const body = {}
            const response = await Streamr.api.v1.products
                .create(body)
                .call()
            const json = await response.json()

            assert.equal(response.status, 401)
            assert.equal(json.code, 'NOT_AUTHENTICATED')
        })

        it('requires a valid body', async () => {
            const body = {}
            const response = await Streamr.api.v1.products
                .create(body)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 422)
            assert.equal(json.code, 'VALIDATION_ERROR')
        })

        it('requires a valid category in body', async () => {
            const body = {
                ...genericProductBody
            }
            body.category = 'non-existing-category-id'
            const response = await Streamr.api.v1.products
                .create(body)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 422)
            assert.equal(json.code, 'VALIDATION_ERROR')
            assert.include(json.message, 'category (typeMismatch)')
        })

        context('called with valid body', () => {
            let response

            before(async () => {
                const body = {
                    name: 'Product',
                    description: 'Description of the product.',
                    imageUrl: 'product.png',
                    category: 'satellite-id',
                    ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                    beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                    pricePerSecond: 5,
                    priceCurrency: 'USD',
                    minimumSubscriptionInSeconds: 60
                }
                response = await Streamr.api.v1.products
                    .create(body)
                    .withAuthToken(AUTH_TOKEN)
                    .call()
            })

            it ('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with created Product', async () => {
                const json = await response.json()
                assert.isString(json.id)
                assert.isAtLeast(json.id.length, 40)
                assert.isString(json.created)
                assert.equal(json.created, json.updated)

                delete json.id
                delete json.created
                delete json.updated

                assert.deepEqual(json, {
                    name: 'Product',
                    description: 'Description of the product.',
                    imageUrl: 'product.png',
                    category: 'satellite-id',
                    streams: [],
                    state: 'NOT_DEPLOYED',
                    previewStream: null,
                    previewConfigJson: null,
                    ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                    beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                    pricePerSecond: 5,
                    priceCurrency: 'USD',
                    minimumSubscriptionInSeconds: 60
                })
            })
        })
    })

    describe('GET /api/v1/products/:id', () => {

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody)
        })

        it('Product must exist', async () => {
            const response = await Streamr.api.v1.products
                .get('non-existing-product-id')
                .call()
            const json = await response.json()

            assert.equal(response.status, 404)
            assert.equal(json.code, 'NOT_FOUND')
        })

        it('requires read permission', async () => {
            const response = await Streamr.api.v1.products
                .get(createdProductId)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'read')
        })

        context('given valid :id and having read permission', () => {
            let response
            let json

            before(async () => {
                response = await Streamr.api.v1.products
                    .get(createdProductId)
                    .withAuthToken(AUTH_TOKEN)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assert.equal(json.id, createdProductId)
                assert.hasAllKeys(json, [
                    'id',
                    'name',
                    'description',
                    'imageUrl',
                    'category',
                    'streams',
                    'state',
                    'previewStream',
                    'previewConfigJson',
                    'ownerAddress',
                    'beneficiaryAddress',
                    'pricePerSecond',
                    'priceCurrency',
                    'minimumSubscriptionInSeconds',
                    'created',
                    'updated'
                ])
            })
        })

    })

    describe('PUT /api/v1/products/:id', () => {

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody)
        })

        const newBody = {
            name: 'Product (updated)',
            description: 'Description of the product.',
            imageUrl: 'product-2.png',
            category: 'automobile-id',
            streams: []
        }

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .update(createdProductId, newBody)
                .call()
            const json = await response.json()

            assert.equal(response.status, 401)
            assert.equal(json.code, 'NOT_AUTHENTICATED')
        })

        it('requires a valid body', async () => {
            const body = {}
            const response = await Streamr.api.v1.products
                .update(createdProductId, body)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 422)
            assert.equal(json.code, 'VALIDATION_ERROR')
        })

        it('Product must exist', async () => {
            const response = await Streamr.api.v1.products
                .update('non-existing-product-id', newBody)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 404)
            assert.equal(json.code, 'NOT_FOUND')
        })

        it('requires write permission', async () => {
            const response = await Streamr.api.v1.products
                .update(createdProductId, newBody)
                .withAuthToken(AUTH_TOKEN_2)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'write')
        })

        context('given valid body, parameters and having write permission', () => {
            let response
            let json

            before(async () => {
                response = await Streamr.api.v1.products
                    .update(createdProductId, newBody)
                    .withAuthToken(AUTH_TOKEN)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with updated Product', () => {
                delete json.created
                delete json.updated

                assert.deepEqual(json, {
                    id: createdProductId,
                    name: 'Product (updated)',
                    description: 'Description of the product.',
                    imageUrl: 'product-2.png',
                    category: 'automobile-id',
                    streams: [],

                    // above are changes

                    state: 'NOT_DEPLOYED',
                    previewStream: null,
                    previewConfigJson: null,
                    ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                    beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                    pricePerSecond: 5,
                    priceCurrency: 'USD',
                    minimumSubscriptionInSeconds: 60
                })
            })
        })
    })

    describe('POST /api/v1/products/:id/setDeploying', () => {

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .setDeploying('id')
                .call()
            const json = await response.json()

            assert.equal(response.status, 401)
            assert.equal(json.code, 'NOT_AUTHENTICATED')
        })

        it('Product must exist', async () => {
            const response = await Streamr.api.v1.products
                .setDeploying('non-existing-product-id')
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 404)
            assert.equal(json.code, 'NOT_FOUND')
        })

        it('requires write permission', async () => {
            const response = await Streamr.api.v1.products
                .setDeploying(createdProductId)
                .withAuthToken(AUTH_TOKEN_2)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'write')
        })

        context('given valid parameters and having write permission', () => {
            let response
            let json

            before(async () => {
                response = await Streamr.api.v1.products
                    .setDeploying(createdProductId)
                    .withAuthToken(AUTH_TOKEN)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assert.equal(json.id, createdProductId)
                assert.hasAllKeys(json, [
                    'id',
                    'name',
                    'description',
                    'imageUrl',
                    'category',
                    'streams',
                    'state',
                    'previewStream',
                    'previewConfigJson',
                    'ownerAddress',
                    'beneficiaryAddress',
                    'pricePerSecond',
                    'priceCurrency',
                    'minimumSubscriptionInSeconds',
                    'created',
                    'updated'
                ])
            })

            it('state of Product is DEPLOYING', () => {
                assert.equal(json.state, 'DEPLOYING')
            })
        })
    })

    describe('POST /api/v1/products/:id/setDeployed', () => {

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody)
        })

        const deployedBody = {
            ownerAddress: '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF',
            beneficiaryAddress: '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF',
            pricePerSecond: 10,
            priceCurrency: 'DATA',
            minimumSubscriptionInSeconds: 15,
            blockNumber: 35000,
            blockIndex: 80
        }

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .setDeployed(createdProductId, genericProductBody)
                .call()
            const json = await response.json()

            assert.equal(response.status, 401)
            assert.equal(json.code, 'NOT_AUTHENTICATED')
        })

        it('requires a valid body', async () => {
            const response = await Streamr.api.v1.products
                .setDeployed(createdProductId, {})
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 422)
            assert.equal(json.code, 'VALIDATION_ERROR')
        })

        it('Product must exist', async () => {
            const response = await Streamr.api.v1.products
                .setDeployed('non-existing-product-id', deployedBody)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 404)
            assert.equal(json.code, 'NOT_FOUND')
        })

        it('requires DevOps role', async () => {
            const response = await Streamr.api.v1.products
                .setDeployed(createdProductId, deployedBody)
                .withAuthToken(AUTH_TOKEN_2)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.message, 'DevOps role required')
        })

        context('given valid body, parameters and having DevOps permission', () => {
            let response
            let json

            before(async () => {
                response = await Streamr.api.v1.products
                    .setDeployed(createdProductId, deployedBody)
                    .withAuthToken(DEVOPS_USER_TOKEN)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assert.equal(json.id, createdProductId)
                assert.hasAllKeys(json, [
                    'id',
                    'name',
                    'description',
                    'imageUrl',
                    'category',
                    'streams',
                    'state',
                    'previewStream',
                    'previewConfigJson',
                    'ownerAddress',
                    'beneficiaryAddress',
                    'pricePerSecond',
                    'priceCurrency',
                    'minimumSubscriptionInSeconds',
                    'created',
                    'updated'
                ])
            })

            it('state of Product is DEPLOYED', () => {
                assert.equal(json.state, 'DEPLOYED')
            })

            it('ownerAddress of Product is updated', () => {
                assert.equal(json.ownerAddress, '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF')
            })

            it('beneficiaryAddress of Product is updated', () => {
                assert.equal(json.beneficiaryAddress, '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF')
            })

            it('pricePerSecond of Product is updated', () => {
                assert.equal(json.pricePerSecond, 10)
            })

            it('priceCurrency of Product is updated', () => {
                assert.equal(json.priceCurrency, 'DATA')
            })

            it('minimumSubscriptionInSeconds of Product is updated', () => {
                assert.equal(json.minimumSubscriptionInSeconds, 15)
            })
        })

        it('is an idempotent operation', async () => {
            const response1 = await Streamr.api.v1.products
                .setDeployed(createdProductId, deployedBody)
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()
            const json1 = await response1.json()

            const response2 = await Streamr.api.v1.products
                .setDeployed(createdProductId, deployedBody)
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()
            const json2 = await response2.json()

            const response3 = await Streamr.api.v1.products
                .setDeployed(createdProductId, deployedBody)
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()
            const json3 = await response3.json()

            assert.equal(response1.status, 200)
            assert.equal(response2.status, 200)
            assert.equal(response3.status, 200)

            assert.deepEqual(json1, json2)
            assert.deepEqual(json2, json3)
        })
    })

    describe('POST /api/v1/products/:id/setUndeploying', () => {

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .setUndeploying(createdProductId)
                .call()
            const json = await response.json()

            assert.equal(response.status, 401)
            assert.equal(json.code, 'NOT_AUTHENTICATED')
        })

        it('Product must exist', async () => {
            const response = await Streamr.api.v1.products
                .setUndeploying('non-existing-id')
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 404)
            assert.equal(json.code, 'NOT_FOUND')
        })

        it('requires write permission', async () => {
            const response = await Streamr.api.v1.products
                .setUndeploying(createdProductId)
                .withAuthToken(AUTH_TOKEN_2)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'write')
        })

        it('Product state cannot be NOT_DEPLOYED', async () => {
            const response = await Streamr.api.v1.products
                .setUndeploying(createdProductId)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 409)
            assert.equal(json.code, 'INVALID_STATE_TRANSITION')
            assert.equal(json.message, 'Invalid transition NOT_DEPLOYED -> UNDEPLOYING')
        })

        it('Product state cannot be DEPLOYING', async () => {
            await Streamr.api.v1.products
                .setDeploying(createdProductId, {
                    tx: '0xf4e80713fe051fe1d1f3e21abac5ace99cea012e307406fc51b507121d864c0f'
                })
                .withAuthToken(AUTH_TOKEN)
                .call()

            const response = await Streamr.api.v1.products
                .setUndeploying(createdProductId)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 409)
            assert.equal(json.code, 'INVALID_STATE_TRANSITION')
            assert.equal(json.message, 'Invalid transition DEPLOYING -> UNDEPLOYING')
        })

        context('given Product in DEPLOYED state and having write permission', () => {
            let response
            let json

            before(async () => {
                await Streamr.api.v1.products
                    .setDeployed(createdProductId, {
                        ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                        beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                        pricePerSecond: 5,
                        priceCurrency: 'USD',
                        minimumSubscriptionInSeconds: 60,
                        blockNumber: 35000,
                        blockIndex: 80
                    })
                    .withAuthToken(DEVOPS_USER_TOKEN)
                    .call()

                response = await Streamr.api.v1.products
                    .setUndeploying(createdProductId)
                    .withAuthToken(AUTH_TOKEN)
                    .call()

                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assert.equal(json.id, createdProductId)
                assert.hasAllKeys(json, [
                    'id',
                    'name',
                    'description',
                    'imageUrl',
                    'category',
                    'streams',
                    'state',
                    'previewStream',
                    'previewConfigJson',
                    'ownerAddress',
                    'beneficiaryAddress',
                    'pricePerSecond',
                    'priceCurrency',
                    'minimumSubscriptionInSeconds',
                    'created',
                    'updated'
                ])
            })

            it('state of Product is UNDEPLOYING', () => {
                assert.equal(json.state, 'UNDEPLOYING')
            })
        })
    })

    describe('POST /api/v1/products/:id/setUndeployed', () => {

        const undeployedBody = {
            blockNumber: 35001,
            blockIndex: 80
        }

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .call()
            const json = await response.json()

            assert.equal(response.status, 401)
            assert.equal(json.code, 'NOT_AUTHENTICATED')
        })

        it('Product must exist', async () => {
            const response = await Streamr.api.v1.products
                .setUndeployed('non-existing-id', undeployedBody)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 404)
            assert.equal(json.code, 'NOT_FOUND')
        })

        it('Product state cannot be NOT_DEPLOYED', async () => {
            const response = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 409)
            assert.equal(json.code, 'INVALID_STATE_TRANSITION')
            assert.equal(json.message, 'Invalid transition NOT_DEPLOYED -> NOT_DEPLOYED')
        })

        it('Product state cannot be DEPLOYING', async () => {
            await Streamr.api.v1.products
                .setDeploying(createdProductId, {
                    tx: '0xf4e80713fe051fe1d1f3e21abac5ace99cea012e307406fc51b507121d864c0f'
                })
                .withAuthToken(AUTH_TOKEN)
                .call()

            const response = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 409)
            assert.equal(json.code, 'INVALID_STATE_TRANSITION')
            assert.equal(json.message, 'Invalid transition DEPLOYING -> NOT_DEPLOYED')
        })


        it('requires DevOps role', async () => {
            await Streamr.api.v1.products
                .setDeployed(createdProductId, {
                    ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                    beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                    pricePerSecond: 5,
                    priceCurrency: 'USD',
                    minimumSubscriptionInSeconds: 60,
                    blockNumber: 35000,
                    blockIndex: 80
                })
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()

            const response = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .withAuthToken(AUTH_TOKEN)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.message, 'DevOps role required')
        })

        context('given Product in DEPLOYED state and having DevOps permission', () => {
            let response
            let json

            before(async () => {
                await Streamr.api.v1.products
                    .setDeployed(createdProductId, {
                        ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                        beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                        pricePerSecond: 5,
                        priceCurrency: 'USD',
                        minimumSubscriptionInSeconds: 60,
                        blockNumber: 35000,
                        blockIndex: 80
                    })
                    .withAuthToken(DEVOPS_USER_TOKEN)
                    .call()

                response = await Streamr.api.v1.products
                    .setUndeployed(createdProductId, undeployedBody)
                    .withAuthToken(DEVOPS_USER_TOKEN)
                    .call()

                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assert.equal(json.id, createdProductId)
                assert.hasAllKeys(json, [
                    'id',
                    'name',
                    'description',
                    'imageUrl',
                    'category',
                    'streams',
                    'state',
                    'previewStream',
                    'previewConfigJson',
                    'ownerAddress',
                    'beneficiaryAddress',
                    'pricePerSecond',
                    'priceCurrency',
                    'minimumSubscriptionInSeconds',
                    'created',
                    'updated'
                ])
            })

            it('state of Product is NOT_DEPLOYED', () => {
                assert.equal(json.state, 'NOT_DEPLOYED')
            })
        })

        it('is an idempotent operation', async () => {
            await Streamr.api.v1.products
                .setDeployed(createdProductId, {
                    ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                    beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                    pricePerSecond: 5,
                    priceCurrency: 'USD',
                    minimumSubscriptionInSeconds: 60,
                    blockNumber: 35000,
                    blockIndex: 80
                })
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()

            const response1 = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()
            const json1 = await response1.json()

            const response2 = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()
            const json2 = await response2.json()

            const response3 = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .withAuthToken(DEVOPS_USER_TOKEN)
                .call()
            const json3 = await response3.json()

            assert.equal(response1.status, 200)
            assert.equal(response2.status, 200)
            assert.equal(response3.status, 200)

            assert.deepEqual(json1, json2)
            assert.deepEqual(json2, json3)
        })
    })

    // TODO: WIP write more..
})