const assert = require('chai').assert
const fs = require('fs')
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const assertResponseIsError = require('./test-utilities.js').assertResponseIsError

const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const API_KEY = 'stream-api-tester-key'
const API_KEY_2 = 'stream-api-tester2-key'

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)

describe('Streams API', () => {
    let streamId

    before(async () => {
        const response = await Streamr.api.v1.streams
            .create({
                name: 'stream-id-' + Date.now()
            })
            .withApiKey(API_KEY)
            .execute()
        streamId = response.id
    })

	describe('POST /api/v1/streams', function() {

		it('happy path', async function() {
			const assertValidResponse = async (response, json, properties, expectedId) => {
				assert.equal(response.status, 200)
				assert.equal(json.name, properties.name)
				assert.equal(json.description, properties.description)
				assert.deepEqual(json.config, JSON.parse(properties.config))
				assert.equal(json.partitions, properties.partitions)
				assert.equal(json.uiChannel, properties.uiChannel)
				if (expectedId !== undefined) {
					assert.equal(json.id, expectedId)
				}
			}
			const properties = {
				name: 'Mock name',
				description: "Mock description",
				config: JSON.stringify({
				   fields: [
					  {
						 name: 'mock-field',
						 type: 'string'
					  }
				   ]
				}),
				partitions: 12,
				uiChannel: false
			}
			const createResponse = await Streamr.api.v1.streams
				.create(properties)
				.withApiKey(API_KEY)
				.call()
			const createResponseJson = await createResponse.json()
			await assertValidResponse(createResponse, createResponseJson, properties)
			const streamId = createResponseJson.id
			const fetchResponse = await Streamr.api.v1.streams
				.get(streamId)
				.withApiKey(API_KEY)
				.call()
			await assertValidResponse(fetchResponse, await fetchResponse.json(), properties, streamId)
		});

		it('invalid properties', async function() {
			const response = await Streamr.api.v1.streams
				.create({
					partitions: 0
				})
				.withApiKey(API_KEY)
				.call()
			assert.equal(response.status, 422)
		})

	})

    describe('GET /api/v1/streams/:id/permissions/me', () => {
        it('responds with status 404 when authenticated but stream does not exist', async () => {
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions('non-existing-stream-id')
                .withApiKey(API_KEY)
                .call()
            assert.equal(response.status, 404)
        })
        it('succeeds with authentication', async () => {
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .withApiKey(API_KEY)
                .call()
            assert.equal(response.status, 200)
        })
        it('succeeds with no authentication', async () => {
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .call()
            assert.equal(response.status, 200)
        })
        it('responds with status 401 when wrong token even if endpoint does not require authentication', async () => {
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .withSessionToken('wrong-token')
                .call()
            assert.equal(response.status, 401)
        })
        it('responds with status 401 when wrong API key even if endpoint does not require authentication', async () => {
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .withApiKey('wrong-api-key')
                .call()
            assert.equal(response.status, 401)
        })
    })

    describe('GET /api/v1/streams/:id/validation', () => {
        it('does not require authentication', async () => {
            const response = await Streamr.api.v1.streams.getValidationInfo(streamId).call()
            assert.equal(response.status, 200)
        })
    })

    describe('GET /api/v1/streams/:id/publishers', () => {
        it('does not require authentication', async () => {
            const response = await Streamr.api.v1.streams.getPublishers(streamId).call()
            assert.equal(response.status, 200)
        })
    })

    describe('GET /api/v1/streams/:id/subscribers', () => {
        it('does not require authentication', async () => {
            const response = await Streamr.api.v1.streams.getSubscribers(streamId).call()
            assert.equal(response.status, 200)
        })
    })

    describe('POST /api/v1/streams/:id/fields', () => {
        it('requires authentication', async () => {
            const response = await Streamr.api.v1.streams
                .setFields(streamId, [
                    {
                        name: 'text',
                        type: 'string'
                    },
                    {
                        name: 'user',
                        type: 'map'
                    }
                ])
                .call()

            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('validates existence of Stream', async () => {
            const response = await Streamr.api.v1.streams
                .setFields('non-existing-stream', [
                    {
                        name: 'text',
                        type: 'string'
                    },
                    {
                        name: 'user',
                        type: 'map'
                    }
                ])
                .withApiKey(API_KEY)
                .call()

            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires stream_edit permission on Stream', async () => {
            const response = await Streamr.api.v1.streams
                .setFields(streamId, [
                    {
                        name: 'text',
                        type: 'string'
                    },
                    {
                        name: 'user',
                        type: 'map'
                    }
                ])
                .withApiKey(API_KEY_2)
                .call()

            await assertResponseIsError(response, 403, 'FORBIDDEN', 'stream_edit')
        })

        context('when called with valid body and permissions', () => {
            let response

            before(async () => {
                response = await Streamr.api.v1.streams
                    .setFields(streamId, [
                        {
                            name: 'text',
                            type: 'string'
                        },
                        {
                            name: 'user',
                            type: 'map'
                        }
                    ])
                    .withApiKey(API_KEY)
                    .call()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('updates stream config fields', async () => {
                const json = await response.json()
                assert.deepEqual(json.config.fields, [
                    {
                        name: 'text',
                        type: 'string'
                    },
                    {
                        name: 'user',
                        type: 'map'
                    }
                ])
            })
        })
    })
})
