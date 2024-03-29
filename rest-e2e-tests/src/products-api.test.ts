import {assert} from 'chai'
import Streamr from './streamr-api-clients'
import {SchemaValidator} from './schema-validator'
import {assertResponseIsError, getStreamrClient, testUsers} from './test-utilities'
import {EthereumAccount} from './EthereumAccount'
import {Response} from 'node-fetch'
import fs from 'fs'
import zlib from 'zlib'

const schemaValidator = new SchemaValidator()

function assertIsPermission(data: any) {
    const errors = schemaValidator.validatePermission(data)
    assert(errors.length === 0, schemaValidator.toMessages(errors))
}

function assertIsProduct(data: any) {
    const errors = schemaValidator.validateProduct(data)
    assert(errors.length === 0, schemaValidator.toMessages(errors))
}

function assertIsStreamID(id: string) {
    // TODO: validate
    assert(id !== undefined, "Stream ID is undefined")
}

async function createProductAndReturnId(productBody: any, user: EthereumAccount) {
    const json = await Streamr.api.v1.products
        .create(productBody)
        .withAuthenticatedUser(user)
        .execute()
    return json.id
}

async function createStreamAndReturnId(streamBody: any, user: EthereumAccount) {
    const stream = await getStreamrClient(user).createStream(streamBody)
    return stream.id
}

describe('Products API', function () {
    let genericProductBody: any
    let streamId1: string
    let streamId2: string
    let streamId3: string
    const productOwner = testUsers.tokenHolder
    const otherUser = testUsers.ensDomainOwner
    const devOpsUser = testUsers.devOpsUser

    this.timeout(1000 * 25)

    before(async () => {
        streamId1 = await createStreamAndReturnId({
            id: `${productOwner.address}/test-stream/${Date.now()}`,
            name: 'stream-1'
        }, productOwner)
        streamId2 = await createStreamAndReturnId({
            id: `${productOwner.address}/test-stream/${Date.now()}`,
            name: 'stream-2'
        }, productOwner)
        streamId3 = await createStreamAndReturnId({
            id: `${productOwner.address}/test-stream/${Date.now()}`,
            name: 'stream-3'
        }, productOwner)

        genericProductBody = {
            name: 'Product',
            type: 'NORMAL',
            description: 'Description of the product.',
            category: 'satellite-id',
            chain: 'ETHEREUM',
            streams: [
                streamId1,
                streamId2
            ],
            ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
            beneficiaryAddress: '0x0000000000000000000011111111111111111111',
            pricePerSecond: '5',
            priceCurrency: 'USD',
            minimumSubscriptionInSeconds: 60,
            contact: {
                email: 'contact@streamr.network',
                url: 'https://streamr.network',
                social1: 'https://github.com/streamr-dev',
                social2: 'https://twitter.com/streamr',
                social3: 'https://www.youtube.com/channel/UCGWEA61RueG-9DV53s-ZyJQ',
                social4: 'https://medium.com/streamrblog'
            },
            termsOfUse: {
                commercialUse: true,
                redistribution: true,
                reselling: true,
                storage: true,
                termsName: 'Terms of Use',
                termsUrl: 'https://streamr.network/termsofuse.html',
            },
        }
    })

    describe('POST /api/v2/products', () => {

        let createdProductId

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires authentication', async () => {
            const body = {}
            const response = await Streamr.api.v1.products
                .create(body)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('succeeds with empty body', async () => {
            const body = {}
            const response = await Streamr.api.v1.products
                .create(body)
                .withAuthenticatedUser(productOwner)
                .call()
            assert.equal(response.status, 200)
        })

        it('validates existence of category (in body)', async () => {
            const body = {
                ...genericProductBody
            }
            body.category = 'non-existing-category-id'
            const response = await Streamr.api.v1.products
                .create(body)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 422, 'VALIDATION_ERROR', 'Invalid category: non-existing-category-id (typeMismatch)')
        })

        it('validates existence of streams (in body)', async () => {
            const body = {
                ...genericProductBody,
                streams: [
                    'non-existing-stream-id-1',
                    'non-existing-stream-id-2'
                ]
            }
            const response = await Streamr.api.v1.products
                .create(body)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 403, 'FORBIDDEN', ' does not have permission to Grant Stream (id non-existing-stream-id-1)')
        })

        it('requires stream grant permission on products streams', async () => {
            const streamId = await createStreamAndReturnId({
                id: `/test-stream/${Date.now()}`,
                name: 'other user\'s stream'
            }, otherUser)

            const body = {
                ...genericProductBody,
                streams: [streamId]
            }

            const response = await Streamr.api.v1.products
                .create(body)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 403, 'FORBIDDEN')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response

            before(async () => {
                response = await Streamr.api.v1.products
                    .create(genericProductBody)
                    .withAuthenticatedUser(productOwner)
                    .call()
            })

            it('responds with 200', () => {
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
                    type: 'NORMAL',
                    chain: 'ETHEREUM',
                    description: 'Description of the product.',
                    imageUrl: null,
                    thumbnailUrl: null,
                    category: 'satellite-id',
                    streams: [
                        streamId1,
                        streamId2
                    ],
                    state: 'NOT_DEPLOYED',
                    previewStreamId: null,
                    previewConfigJson: null,
                    ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                    beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                    pricePerSecond: '5',
                    isFree: false,
                    priceCurrency: 'USD',
                    minimumSubscriptionInSeconds: 60,
                    owner: 'Anonymous User',
                    contact: {
                        email: 'contact@streamr.network',
                        url: 'https://streamr.network',
                        social1: 'https://github.com/streamr-dev',
                        social2: 'https://twitter.com/streamr',
                        social3: 'https://www.youtube.com/channel/UCGWEA61RueG-9DV53s-ZyJQ',
                        social4: 'https://medium.com/streamrblog'
                    },
                    termsOfUse: {
                        commercialUse: true,
                        redistribution: true,
                        reselling: true,
                        storage: true,
                        termsName: 'Terms of Use',
                        termsUrl: 'https://streamr.network/termsofuse.html',
                    },
                })
            })
        })
    })

    describe('GET /api/v2/products/:id', () => {

        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .get('non-existing-product-id')
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires product_get permission', async () => {
            const response = await Streamr.api.v1.products
                .get(createdProductId)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_get')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let json: any

            before(async () => {
                response = await Streamr.api.v1.products
                    .get(createdProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with found Product', () => {
                assertIsProduct(json)
            })
        })
    })

    describe('PUT /api/v2/products/:id', () => {
        const newBody = {
            name: 'Product (updated)',
            description: 'Description of the product.',
            imageUrl: 'https://streamr.network/uploads/product-2.png',
            category: 'automobile-id',
            streams: [],
            ownerAddress: '0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
            beneficiaryAddress: '0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC',
            pricePerSecond: '4556',
            priceCurrency: 'DATA',
            minimumSubscriptionInSeconds: 30000,
            contact: {
                email: 'contact@streamr.network',
                url: 'https://streamr.network',
                social1: 'https://github.com/streamr-dev',
                social2: 'https://twitter.com/streamr',
                social3: 'https://www.youtube.com/channel/UCGWEA61RueG-9DV53s-ZyJQ',
                social4: 'https://medium.com/streamrblog'
            },
            termsOfUse: {
                commercialUse: true,
                redistribution: true,
                reselling: true,
                storage: true,
                termsName: 'Terms of Use',
                termsUrl: 'https://streamr.network/termsofuse.html',
            },
        }

        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .update(createdProductId, newBody)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('validates body', async () => {
            const body = {}
            const response = await Streamr.api.v1.products
                .update(createdProductId, body)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 422, 'VALIDATION_ERROR')
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .update('non-existing-product-id', newBody)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires product_edit permission on Product', async () => {
            const response = await Streamr.api.v1.products
                .update(createdProductId, newBody)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_edit')
        })


        it('command object is validated when updating Product fields', async () => {
            const response = await Streamr.api.v1.products
                .update(createdProductId, {
                    ...newBody,
                    beneficiaryAddress: 'not_a_valid_ethereum_address'
                })
                .withAuthenticatedUser(otherUser)
                .call()
            assertResponseIsError(response, 422, 'VALIDATION_ERROR')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let json: any

            before(async () => {
                response = await Streamr.api.v1.products
                    .update(createdProductId, newBody)
                    .withAuthenticatedUser(productOwner)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with updated Product', () => {
                assertIsProduct(json)

                delete json.created
                delete json.updated

                assert.deepEqual(json, {
                    id: createdProductId,
                    type: 'NORMAL',
                    name: 'Product (updated)',
                    description: 'Description of the product.',
                    imageUrl: null,
                    thumbnailUrl: null,
                    category: 'automobile-id',
                    chain: 'ETHEREUM',
                    streams: [],

                    // above are changes

                    state: 'NOT_DEPLOYED',
                    previewStreamId: null,
                    previewConfigJson: null,
                    ownerAddress: '0xAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    beneficiaryAddress: '0xCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC',
                    pricePerSecond: '4556',
                    isFree: false,
                    priceCurrency: 'DATA',
                    minimumSubscriptionInSeconds: 30000,
                    owner: 'Anonymous User',
                    contact: {
                        email: 'contact@streamr.network',
                        url: 'https://streamr.network',
                        social1: 'https://github.com/streamr-dev',
                        social2: 'https://twitter.com/streamr',
                        social3: 'https://www.youtube.com/channel/UCGWEA61RueG-9DV53s-ZyJQ',
                        social4: 'https://medium.com/streamrblog'
                    },
                    termsOfUse: {
                        commercialUse: true,
                        redistribution: true,
                        reselling: true,
                        storage: true,
                        termsName: 'Terms of Use',
                        termsUrl: 'https://streamr.network/termsofuse.html',
                    },
                })
            })
        })
    })

    describe('POST /api/v2/products/:id/setDeploying', () => {

        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .setDeploying('id')
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .setDeploying('non-existing-product-id')
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires product_edit permission on Product', async () => {
            const response = await Streamr.api.v1.products
                .setDeploying(createdProductId)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_edit')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let json: any

            before(async () => {
                response = await Streamr.api.v1.products
                    .setDeploying(createdProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assertIsProduct(json)
            })

            it('state of Product is now DEPLOYING', () => {
                assert.equal(json.state, 'DEPLOYING')
            })
        })
    })

    describe('POST /api/v2/products/:id/setDeployed', () => {
        const deployedBody = {
            ownerAddress: '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF',
            beneficiaryAddress: '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF',
            pricePerSecond: '10',
            priceCurrency: 'DATA',
            minimumSubscriptionInSeconds: 15,
            blockNumber: 35000,
            blockIndex: 80
        }

        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .setDeployed(createdProductId, genericProductBody)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('validates body', async () => {
            const response = await Streamr.api.v1.products
                .setDeployed(createdProductId, {})
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 422, 'VALIDATION_ERROR')
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .setDeployed('non-existing-product-id', deployedBody)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires DevOps role', async () => {
            const response = await Streamr.api.v1.products
                .setDeployed(createdProductId, deployedBody)
                .withAuthenticatedUser(otherUser)
                .call()
            await assertResponseIsError(response, 403, 'FORBIDDEN', 'DevOps role required')
        })

        it('verifies legality of state transition', async () => {
            const productId = await createProductAndReturnId(genericProductBody, productOwner)

            await Streamr.api.v1.products
                .setDeployed(productId, deployedBody)
                .withAuthenticatedUser(devOpsUser)
                .execute()

            await Streamr.api.v1.products
                .setUndeploying(productId)
                .withAuthenticatedUser(productOwner)
                .execute()

            const response = await Streamr.api.v1.products
                .setDeployed(productId, {
                    ...deployedBody,
                    blockNumber: 35005
                })
                .withAuthenticatedUser(devOpsUser)
                .call()

            await assertResponseIsError(response, 409, 'INVALID_STATE_TRANSITION')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let json: any

            before(async () => {
                response = await Streamr.api.v1.products
                    .setDeployed(createdProductId, deployedBody)
                    .withAuthenticatedUser(devOpsUser)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assertIsProduct(json)
            })

            it('state of Product is now DEPLOYED', () => {
                assert.equal(json.state, 'DEPLOYED')
            })

            it('ownerAddress of Product is updated', () => {
                assert.equal(json.ownerAddress, '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF')
            })

            it('beneficiaryAddress of Product is updated', () => {
                assert.equal(json.beneficiaryAddress, '0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF')
            })

            it('pricePerSecond of Product is updated', () => {
                assert.equal(json.pricePerSecond, '10')
            })

            it('priceCurrency of Product is updated', () => {
                assert.equal(json.priceCurrency, 'DATA')
            })

            it('minimumSubscriptionInSeconds of Product is updated', () => {
                assert.equal(json.minimumSubscriptionInSeconds, 15)
            })

            it('is an idempotent operation', async () => {
                const response1 = await Streamr.api.v1.products
                    .setDeployed(createdProductId, deployedBody)
                    .withAuthenticatedUser(devOpsUser)
                    .call()
                const json1 = await response1.json()

                const response2 = await Streamr.api.v1.products
                    .setDeployed(createdProductId, deployedBody)
                    .withAuthenticatedUser(devOpsUser)
                    .call()
                const json2 = await response2.json()

                const response3 = await Streamr.api.v1.products
                    .setDeployed(createdProductId, deployedBody)
                    .withAuthenticatedUser(devOpsUser)
                    .call()
                const json3 = await response3.json()

                assert.equal(response1.status, 200)
                assert.equal(response2.status, 200)
                assert.equal(response3.status, 200)

                assert.deepEqual(json1, json2)
                assert.deepEqual(json2, json3)
            })
        })
    })

    describe('POST /api/v2/products/:id/setUndeploying', () => {

        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .setUndeploying(createdProductId)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .setUndeploying('non-existing-id')
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires product_edit permission on Product', async () => {
            const response = await Streamr.api.v1.products
                .setUndeploying(createdProductId)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_edit')
        })

        it('verifies legality of state transition', async () => {
            const response = await Streamr.api.v1.products
                .setUndeploying(createdProductId)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 409, 'INVALID_STATE_TRANSITION')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let json: any

            before(async () => {
                await Streamr.api.v1.products
                    .setDeployed(createdProductId, {
                        ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                        beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                        pricePerSecond: '5',
                        priceCurrency: 'USD',
                        minimumSubscriptionInSeconds: 60,
                        blockNumber: 35000,
                        blockIndex: 80
                    })
                    .withAuthenticatedUser(devOpsUser)
                    .call()

                response = await Streamr.api.v1.products
                    .setUndeploying(createdProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()

                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assertIsProduct(json)
            })

            it('state of Product is now UNDEPLOYING', () => {
                assert.equal(json.state, 'UNDEPLOYING')
            })
        })
    })

    describe('POST /api/v2/products/:id/setUndeployed', () => {
        const undeployedBody = {
            blockNumber: 35001,
            blockIndex: 80
        }

        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .setUndeployed('non-existing-id', undeployedBody)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires DevOps role', async () => {
            const productId = await createProductAndReturnId(genericProductBody, productOwner)
            await Streamr.api.v1.products
                .setDeployed(productId, {
                    ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                    beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                    pricePerSecond: '5',
                    priceCurrency: 'USD',
                    minimumSubscriptionInSeconds: 60,
                    blockNumber: 35000,
                    blockIndex: 80
                })
                .withAuthenticatedUser(devOpsUser)
                .call()

            const response = await Streamr.api.v1.products
                .setUndeployed(productId, undeployedBody)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 403, 'FORBIDDEN', 'DevOps role required')
        })

        it('verifies legality of state transition', async () => {
            const response = await Streamr.api.v1.products
                .setUndeployed(createdProductId, undeployedBody)
                .withAuthenticatedUser(devOpsUser)
                .call()
            await assertResponseIsError(response, 409, 'INVALID_STATE_TRANSITION')
        })

        context('given Product in DEPLOYED state and having DevOps permission', () => {
            let response: Response
            let json: any

            before(async () => {
                await Streamr.api.v1.products
                    .setDeployed(createdProductId, {
                        ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                        beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                        pricePerSecond: '5',
                        priceCurrency: 'USD',
                        minimumSubscriptionInSeconds: 60,
                        blockNumber: 35000,
                        blockIndex: 80
                    })
                    .withAuthenticatedUser(devOpsUser)
                    .call()

                response = await Streamr.api.v1.products
                    .setUndeployed(createdProductId, undeployedBody)
                    .withAuthenticatedUser(devOpsUser)
                    .call()

                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assertIsProduct(json)
            })

            it('state of Product is NOT_DEPLOYED', () => {
                assert.equal(json.state, 'NOT_DEPLOYED')
            })

            it('is an idempotent operation', async () => {
                await Streamr.api.v1.products
                    .setDeployed(createdProductId, {
                        ownerAddress: '0xAAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD',
                        beneficiaryAddress: '0x0000000000000000000011111111111111111111',
                        pricePerSecond: '5',
                        priceCurrency: 'USD',
                        minimumSubscriptionInSeconds: 60,
                        blockNumber: 35000,
                        blockIndex: 80
                    })
                    .withAuthenticatedUser(devOpsUser)
                    .call()

                const response1 = await Streamr.api.v1.products
                    .setUndeployed(createdProductId, undeployedBody)
                    .withAuthenticatedUser(devOpsUser)
                    .call()
                const json1 = await response1.json()

                const response2 = await Streamr.api.v1.products
                    .setUndeployed(createdProductId, undeployedBody)
                    .withAuthenticatedUser(devOpsUser)
                    .call()
                const json2 = await response2.json()

                const response3 = await Streamr.api.v1.products
                    .setUndeployed(createdProductId, undeployedBody)
                    .withAuthenticatedUser(devOpsUser)
                    .call()
                const json3 = await response3.json()

                assert.equal(response1.status, 200)
                assert.equal(response2.status, 200)
                assert.equal(response3.status, 200)

                assert.deepEqual(json1, json2)
                assert.deepEqual(json2, json3)
            })
        })
    })

    describe('GET /api/v2/products', () => {
        it('anonymous user can fetch public Products with publicAccess=true', async () => {
            const response = await Streamr.api.v1.products
                .list({
                    publicAccess: true
                })
                .call()
            const json = await response.json()

            assert.equal(response.status, 200)
            assert.isAtLeast(json.length, 4)
            json.forEach((productData: any) => assertIsProduct(productData))
        })
    })

    describe('GET /api/v2/products/:id/streams', () => {
        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .listStreams('non-existing-id')
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires product_get permission on Product', async () => {
            const response = await Streamr.api.v1.products
                .listStreams(createdProductId)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_get')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let productsStreamIds: any

            before(async () => {
                response = await Streamr.api.v1.products
                    .listStreams(createdProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()

                productsStreamIds = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('Streams have expected ids', () => {
                assert.sameMembers(productsStreamIds.map((streamId: string) => streamId), [streamId1, streamId2])
            })

            it('responds with list of Streams', () => {
                productsStreamIds.forEach((streamId: string) => assertIsStreamID(streamId))
            })
        })
    })

    describe('POST /api/v2/products/:id/images', () => {
        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
            // Warm um JVM
            const response: Response = await Streamr.api.v1.products
                .uploadImage(createdProductId, fs.createReadStream('./test-data/500-by-400-image.png'))
                .withAuthenticatedUser(productOwner)
                .call()
            await response.json()
        })

        it('requires authentication', async () => {
            const fileBytes = Buffer.from([])
            const response = await Streamr.api.v1.products
                .uploadImage(createdProductId, fileBytes)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('requires existing Product', async () => {
            const fileBytes = Buffer.from([])
            const response = await Streamr.api.v1.products
                .uploadImage('non-existing-product-id', fileBytes)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires product_edit permission on Product', async () => {
            const fileBytes = Buffer.from([])
            const response = await Streamr.api.v1.products
                .uploadImage(createdProductId, fileBytes)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_edit')
        })

        it('requires parameter "file" to be a multipart file', async () => {
            const fileBytes = 'I am not a file'
            const response = await Streamr.api.v1.products
                .uploadImage(createdProductId, fileBytes)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 400, 'PARAMETER_MISSING')
        })

        it('verifies file size', (done) => {
            // TODO: Unpack file, how to do without going to File system?
            const wstream = fs.createWriteStream('./test-data/bigfile.txt')

            fs.createReadStream('./test-data/bigfile.txt.gz')
                .pipe(zlib.createUnzip())
                .pipe(wstream)

            wstream.on('finish', async () => {
                const response = await Streamr.api.v1.products
                    .uploadImage(createdProductId, fs.createReadStream('./test-data/bigfile.txt'))
                    .withAuthenticatedUser(productOwner)
                    .call()
                await assertResponseIsError(response, 413, 'FILE_TOO_LARGE')
                done()
            })

        })

        it('verifies file contents', async () => {
            const response = await Streamr.api.v1.products
                .uploadImage(createdProductId, fs.createReadStream('./test-data/file.txt'))
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 415, 'UNSUPPORTED_FILE_TYPE')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let json: any

            before(async () => {
                response = await Streamr.api.v1.products
                    .uploadImage(createdProductId, fs.createReadStream('./test-data/500-by-400-image.png'))
                    .withAuthenticatedUser(productOwner)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assertIsProduct(json)
            })

            it('response Product contains image URL', () => {
                assert.isDefined(json.imageUrl)
                assert.isNotNull(json.imageUrl)
            })

            it('response Product contains image thumbnail URL', () => {
                assert.isDefined(json.thumbnailUrl)
                assert.isNotNull(json.thumbnailUrl)
            })

            /* TODO: Fix this
            it('image URL works', (done) => {
                const readStream = fs.createReadStream('./rest-e2e-tests/test-data/500-by-400-image.png')
                let bufs = []
                readStream.on('data', (d) => bufs.push(d))
                readStream.on('end', async () => {
                    const expected = Buffer.concat(bufs)

                    const response2 = await fetch(json.imageUrl)
                    const actual = await response2.buffer()

                    assert.equal(actual, expected)
                    done()
                })
            })
            */

            it('can replace existing image with a new image', async () => {
                const response2 = await Streamr.api.v1.products
                    .uploadImage(createdProductId, fs.createReadStream('./test-data/500-by-400-image-2.png'))
                    .withAuthenticatedUser(productOwner)
                    .call()
                const json2 = await response2.json()

                assert.notEqual(json2.imageUrl, json.imageUrl)
            })
        })
    })

    describe('PUT /api/v2/products/:id/streams/:streamId', () => {
        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .addStream('non-existing-id', streamId3)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND', 'Product')
        })

        it('requires product_edit permission on Product', async () => {
            const response = await Streamr.api.v1.products
                .addStream(createdProductId, streamId3)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_edit')
        })

        it('requires existing Stream', async () => {
            const response = await Streamr.api.v1.products
                .addStream(createdProductId, 'non-existing-id')
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 403, 'FORBIDDEN', ' does not have permission to Grant Stream (id non-existing-id)')
        })

        it('requires Grant permission on Stream', async () => {
            const streamId4 = await createStreamAndReturnId({
                id: `/test-stream/${Date.now()}`,
                name: 'stream-3'
            }, otherUser)

            const response = await Streamr.api.v1.products
                .addStream(createdProductId, streamId4)
                .withAuthenticatedUser(productOwner)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'Grant')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response

            before(async () => {
                response = await Streamr.api.v1.products
                    .addStream(createdProductId, streamId3)
                    .withAuthenticatedUser(productOwner)
                    .call()
            })

            it('responds with 204', () => {
                assert.equal(response.status, 204)
            })

            it('adds stream to Product', async () => {
                const response = await Streamr.api.v1.products
                    .listStreams(createdProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()
                const productsStreamIds = await response.json()
                assert.include(productsStreamIds.map((streamId: any) => streamId), streamId3)
            })
        })
    })

    describe('DELETE /api/v2/products/:id/streams/:streamId', () => {
        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .removeStream('non-existing-id', streamId3)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND', 'Product')
        })

        it('requires product_delete permission on Product', async () => {
            const response = await Streamr.api.v1.products
                .removeStream(createdProductId, streamId3)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_delete')
        })

        it('requires existing Stream', async () => {
            const response = await Streamr.api.v1.products
                .removeStream(createdProductId, 'non-existing-id')
                .withAuthenticatedUser(productOwner)
                .call()
            assert.equal(response.status, 204)
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response

            before(async () => {
                response = await Streamr.api.v1.products
                    .removeStream(createdProductId, streamId1)
                    .withAuthenticatedUser(productOwner)
                    .call()
            })

            it('responds with 204', () => {
                assert.equal(response.status, 204)
            })

            it('removes stream from Product', async () => {
                const response = await Streamr.api.v1.products
                    .listStreams(createdProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()
                const productsStreamIds = await response.json()
                assert.deepEqual(productsStreamIds, [streamId2])
            })
        })

        it('responds with 204 when removing stream that is not associated with Product', async () => {
            const response = await Streamr.api.v1.products
                .removeStream(createdProductId, streamId3)
                .withAuthenticatedUser(productOwner)
                .call()
            assert.equal(response.status, 204)
        })
    })

    describe('GET /api/v2/products/:id/permissions/me', () => {
        let createdProductId: string

        before(async () => {
            createdProductId = await createProductAndReturnId(genericProductBody, productOwner)
        })

        context('when called with valid params, body, and headers', () => {
            let response: Response
            let json: any

            before(async () => {
                response = await Streamr.api.v1.products.permissions
                    .getOwnPermissions(createdProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()

                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with expected Permissions', () => {
                assert.equal(json.length, 4)
                json.forEach((p: any) => assertIsPermission(p))
                assert.deepEqual(json.map((p: any) => p.operation), ['product_get', 'product_edit', 'product_delete', 'product_share'])
            })
        })
    })

    describe('GET /api/v2/products/:id/deployFree', () => {
        let freeProductId: string

        before(async () => {
            const freeProductBody = {
                name: 'Product',
                description: 'Description of the product.',
                category: 'satellite-id',
                streams: [
                    streamId1,
                    streamId2
                ],
                pricePerSecond: 0,
                priceCurrency: 'USD',
                minimumSubscriptionInSeconds: 60,
            }
            freeProductId = await createProductAndReturnId(freeProductBody, productOwner)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .deployFree(freeProductId)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .deployFree('non-existing-id')
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires product_share permission on Product', async () => {
            const response = await Streamr.api.v1.products
                .deployFree(freeProductId)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_share')
        })

        it('verifies that Product is free', async () => {
            const paidProductId = await createProductAndReturnId(genericProductBody, productOwner)

            const response = await Streamr.api.v1.products
                .deployFree(paidProductId)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 400, 'PRODUCT_IS_NOT_FREE')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let json: any

            before(async () => {
                response = await Streamr.api.v1.products
                    .deployFree(freeProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()

                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assertIsProduct(json)
            })

            it('state of Product is now DEPLOYED', () => {
                assert.equal(json.state, 'DEPLOYED')
            })

            it('cannot be called again (already DEPLOYED)', async () => {
                const response = await Streamr.api.v1.products
                    .deployFree(freeProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()
                await assertResponseIsError(response, 409, 'INVALID_STATE_TRANSITION')
            })
        })
    })

    describe('GET /api/v2/products/:id/undeployFree', () => {
        let freeProductId: string

        before(async () => {
            const freeProductBody = {
                name: 'Product',
                description: 'Description of the product.',
                category: 'satellite-id',
                streams: [
                    streamId1,
                    streamId2
                ],
                pricePerSecond: 0,
                priceCurrency: 'USD',
                minimumSubscriptionInSeconds: 60,
            }
            freeProductId = await createProductAndReturnId(freeProductBody, productOwner)
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.products
                .undeployFree(freeProductId)
                .call()
            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('requires existing Product', async () => {
            const response = await Streamr.api.v1.products
                .undeployFree('non-existing-id')
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires product_share permission on Product', async () => {
            const response = await Streamr.api.v1.products
                .undeployFree(freeProductId)
                .withAuthenticatedUser(otherUser)
                .call()
            const json = await response.json()

            assert.equal(response.status, 403)
            assert.equal(json.code, 'FORBIDDEN')
            assert.equal(json.operation, 'product_share')
        })

        it('verifies that Product is free', async () => {
            const paidProductId = await createProductAndReturnId(genericProductBody, productOwner)

            const response = await Streamr.api.v1.products
                .undeployFree(paidProductId)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 400, 'PRODUCT_IS_NOT_FREE')
        })

        it('verifies that Product is deployed', async () => {
            const response = await Streamr.api.v1.products
                .undeployFree(freeProductId)
                .withAuthenticatedUser(productOwner)
                .call()
            await assertResponseIsError(response, 409, 'INVALID_STATE_TRANSITION')
        })

        context('when called with valid params, body, headers, and permissions', () => {
            let response: Response
            let json: any

            before(async () => {
                // Deploy 1st
                await Streamr.api.v1.products
                    .deployFree(freeProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()

                response = await Streamr.api.v1.products
                    .undeployFree(freeProductId)
                    .withAuthenticatedUser(productOwner)
                    .call()

                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with Product', () => {
                assertIsProduct(json)
            })

            it('state of Product is now NOT_DEPLOYED', () => {
                assert.equal(json.state, 'NOT_DEPLOYED')
            })
        })
    })
})
