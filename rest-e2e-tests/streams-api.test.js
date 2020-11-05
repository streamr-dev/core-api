const assert = require('chai').assert
const Streamr = require('./streamr-api-clients')
const assertResponseIsError = require('./test-utilities.js').assertResponseIsError
const assertStreamrClientResponseError = require('./test-utilities.js').assertStreamrClientResponseError
const getStreamrClient = require('./test-utilities.js').getStreamrClient
const testUsers = require('./test-utilities.js').testUsers
const StreamrClient = require('streamr-client')

describe('Streams API', () => {

	let streamId
	const streamOwner = StreamrClient.generateEthereumAccount()
	const anonymousUser = StreamrClient.generateEthereumAccount()
	const ensDomainOwner = testUsers.ensDomainOwner

    before(async () => {
        const response = await getStreamrClient(streamOwner).createStream({
            name: 'stream-id-' + Date.now()
        })
        streamId = response.id
	})

	describe('POST /api/v1/streams', function() {

		it('happy path', async function() {
			const assertValidResponse = (json, properties, expectedId) => {
				assert.equal(json.name, properties.name)
				assert.equal(json.description, properties.description)
				assert.deepEqual(json.config, properties.config)
				assert.equal(json.partitions, properties.partitions)
				assert.equal(json.uiChannel, properties.uiChannel)
				assert.equal(json.autoConfigure, properties.autoConfigure)
				assert.equal(json.storageDays, properties.storageDays)
				assert.equal(json.inactivityThresholdHours, properties.inactivityThresholdHours)
				if (expectedId !== undefined) {
					assert.equal(json.id, expectedId)
				}
			}
			const properties = {
				name: 'Mock name',
				description: "Mock description",
				config: {
				   fields: [
					  {
						 name: 'mock-field',
						 type: 'string'
					  }
				   ]
				},
				partitions: 12,
				autoConfigure: false,
				storageDays: 66,
				inactivityThresholdHours: 4,
				uiChannel: false
			}
			const createResponse = await getStreamrClient(streamOwner).createStream(properties)
			assertValidResponse(createResponse, properties)
			const streamId = createResponse.id
			const fetchResponse = await getStreamrClient(streamOwner).getStream(streamId)
			assertValidResponse(fetchResponse, properties, streamId)
		});

		it('invalid properties', async function() {
			const request = getStreamrClient(streamOwner).createStream({
				partitions: 999
			})
			await assertStreamrClientResponseError(request, 422)
		})

		it('create with sandbox domain id', async function() {
			const streamId = 'sandbox/foo/bar' + Date.now()
			const properties = {
				id: streamId
			}
			const response = await getStreamrClient(streamOwner).createStream(properties)
			assert.equal(response.id, streamId)
		})

		it('create with owned domain id', async function() {
			const streamId = 'testdomain1.eth/foo/bar' + Date.now()
			const properties = {
				id: streamId
			}
			const response = await getStreamrClient(ensDomainOwner).createStream(properties)
			assert.equal(response.id, streamId)
		})

		it('create with invalid domain id', async function() {
			const sandboxDomainId = 'foobar.eth/loremipsum'
			const properties = {
				id: sandboxDomainId
			}
			const request = getStreamrClient(streamOwner).createStream(properties)
			await assertStreamrClientResponseError(request, 422)
		})

	})

    describe('GET /api/v1/streams/:id', () => {
        it('works with uri-encoded ids', async () => {
            const id = 'sandbox/streams-api.test.js/stream-' + Date.now()
            await getStreamrClient(streamOwner).createStream({
                id
            })
            const json = await getStreamrClient(streamOwner).getStream(id)
            assert.equal(json.id, id)
        })
    })

    describe('GET /api/v1/streams/:id/permissions/me', () => {
        it('responds with status 404 when authenticated but stream does not exist', async () => {
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions('non-existing-stream-id')
                .withAuthenticatedUser(streamOwner)
                .call()
            assert.equal(response.status, 404)
        })
        it('succeeds with authentication', async () => {
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .withAuthenticatedUser(streamOwner)
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
            const sessionToken = 'wrong-token';
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .withHeader('Authorization', `Bearer ${sessionToken}`)
                .call()
            assert.equal(response.status, 401)
        })
        it('responds with status 401 when wrong API key even if endpoint does not require authentication', async () => {
            const apiKey = 'wrong-api-key'
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .withHeader('Authorization', `Token ${apiKey}`)
                .call()
            assert.equal(response.status, 401)
        })
    })

    describe('GET /api/v1/streams/:id/validation', () => {
        it('does not require authentication', async () => {
            const json = await getStreamrClient(anonymousUser).getStreamValidationInfo(streamId)
            assert.isOk(json)
        })
    })

    describe('GET /api/v1/streams/:id/publishers', () => {
        it('does not require authentication', async () => {
            const json = await getStreamrClient(anonymousUser).getStreamPublishers(streamId)
            assert.isOk(json)
        })
    })

    describe('GET /api/v1/streams/:id/subscribers', () => {
        it('does not require authentication', async () => {
            const json = await getStreamrClient(anonymousUser).getStreamSubscribers(streamId)
            assert.isOk(json)
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
                .withAuthenticatedUser(streamOwner)
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
                .withAuthenticatedUser(anonymousUser)
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
                    .withAuthenticatedUser(streamOwner)
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
