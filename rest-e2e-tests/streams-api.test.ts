import {assert} from 'chai'
import Streamr from './streamr-api-clients'
import {assertResponseIsError, assertStreamrClientResponseError, getStreamrClient, testUsers} from './test-utilities'
import {StreamrClient} from 'streamr-client'
import {Response} from 'node-fetch'

describe('Streams API', () => {
    const ZERO_ADDRESS: string = '0x0000000000000000000000000000000000000000'

    let streamId: string
    const streamOwner = StreamrClient.generateEthereumAccount()
    const anonymousUser = StreamrClient.generateEthereumAccount()
    const ensDomainOwner = testUsers.ensDomainOwner

    before(async () => {
        const response = await getStreamrClient(streamOwner).createStream({
            name: 'stream-id-' + Date.now(),
        })
        streamId = response.id
    })

    describe('POST /api/v1/streams', function () {

        it('happy path', async function () {
            const assertValidResponse = (json: any, properties: any, expectedId?: string) => {
                assert.equal(json.name, properties.name)
                assert.equal(json.description, properties.description)
                assert.deepEqual(json.config, properties.config)
                assert.equal(json.partitions, properties.partitions)
                assert.equal(json.autoConfigure, properties.autoConfigure)
                assert.equal(json.storageDays, properties.storageDays)
                assert.equal(json.inactivityThresholdHours, properties.inactivityThresholdHours)
                if (expectedId !== undefined) {
                    assert.equal(json.id, expectedId)
                }
            }
            const properties = {
                name: 'Mock name',
                description: 'Mock description',
                config: {
                    fields: [
                        {
                            name: 'mock-field',
                            type: <const>'string',
                        },
                    ],
                },
                partitions: 12,
                autoConfigure: false,
                storageDays: 66,
                inactivityThresholdHours: 4,
                uiChannel: false,
            }
            const createResponse = await getStreamrClient(streamOwner).createStream(properties)
            assertValidResponse(createResponse, properties)
            const streamId = createResponse.id
            const fetchResponse = await getStreamrClient(streamOwner).getStream(streamId)
            assertValidResponse(fetchResponse, properties, streamId)
        })

        it('invalid properties', async function () {
            const request = getStreamrClient(streamOwner).createStream({
                partitions: 999,
            })
            await assertStreamrClientResponseError(request, 422)
        })

        it('create with owned domain id', async function () {
            const streamId = 'testdomain1.eth/foo/bar' + Date.now()
            const properties = {
                id: streamId,
            }
            const response = await getStreamrClient(ensDomainOwner).createStream(properties)
            assert.equal(response.id, streamId)
        })

        it('create with integration key id', async function () {
            const streamId = streamOwner.address + '/foo/bar' + Date.now()
            const properties = {
                id: streamId,
            }
            const response = await getStreamrClient(streamOwner).createStream(properties)
            assert.equal(response.id, streamId)
        })

        it('create with invalid id', async function () {
            const streamId = 'foobar.eth/loremipsum'
            const properties = {
                id: streamId,
            }
            const request = getStreamrClient(streamOwner).createStream(properties)
            await assertStreamrClientResponseError(request, 422)
        })

        it('create stream with duplicate id', async function () {
            const now = Date.now()
            const streamId = 'testdomain1.eth/foobar/test/' + now
            const properties = {
                id: streamId,
                name: 'Hello world!',
            }
            const response = await getStreamrClient(ensDomainOwner).createStream(properties)
            assert.equal(response.id, streamId)
            const errorResponse = getStreamrClient(ensDomainOwner).createStream(properties)
            await assertStreamrClientResponseError(errorResponse, 400)
        })

        it('create stream with too long id', async function () {
            let streamId = 'testdomain1.eth/foobar/' + Date.now() + '/'
            while (streamId.length < 256) {
                streamId = streamId + 'x'
            }
            const properties = {
                id: streamId,
            }
            const response = getStreamrClient(ensDomainOwner).createStream(properties)
            await assertStreamrClientResponseError(response, 422)
        })

        it('create stream with too long description', async function () {
            let streamId = 'testdomain1.eth/foobar/' + Date.now()
            const properties = {
                id: streamId,
                description: 'x'.repeat(256),
            }
            const response = getStreamrClient(ensDomainOwner).createStream(properties)
            await assertStreamrClientResponseError(response, 422)
        })

        it('create stream with too long name', async function () {
            let streamId = 'testdomain1.eth/foobar/' + Date.now()
            const properties = {
                id: streamId,
                name: 'x'.repeat(256),
            }
            const response = getStreamrClient(ensDomainOwner).createStream(properties)
            await assertStreamrClientResponseError(response, 422)
        })
    })

    describe('GET /api/v1/streams', () => {
        it('finds stream by permission name in uppercase/lowercase', async () => {
            const queryParams = {
                operation: 'stream_DELETE',
                noConfig: true,
                grantedAccess: true,
            }
            const response = await Streamr.api.v1.streams
                .list(queryParams)
                .withAuthenticatedUser(streamOwner)
                .call()
            const json = await response.json()
            assert.equal(response.status, 200)
            const result = json.filter((stream: any) => stream.id == streamId)
            assert.equal(result.length, 1, 'response should contain a single stream')
        })
    })

    describe('GET /api/v1/streams/:id', () => {
        it('works with uri-encoded ids', async () => {
            const id = streamOwner.address + '/streams-api.test.js/stream-' + Date.now()
            await getStreamrClient(streamOwner).createStream({
                id,
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
            const sessionToken = 'wrong-token'
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .withHeader('Authorization', `Bearer ${sessionToken}`)
                .call()
            assert.equal(response.status, 401)
        })
        it('responds with status 401 when wrong session token even if endpoint does not require authentication', async () => {
            const bearer = 'wrong-session-token'
            const response = await Streamr.api.v1.streams.permissions
                .getOwnPermissions(streamId)
                .withHeader('Authorization', `Bearer ${bearer}`)
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

    describe('GET /api/v1/streams/:id/publisher/0x0000000000000000000000000000000000000000', () => {
        it('should return 200 if the stream has public publish permission', async () => {
            const stream = await getStreamrClient(streamOwner).createStream({
                name: 'Stream with public publish permission',
            })
            const permission = await Streamr.api.v1.streams
                .grantPublic(stream.id, 'stream_publish')
                .withAuthenticatedUser(streamOwner)
                .call()
            const response = await Streamr.api.v1.streams
                .publisher(stream.id, ZERO_ADDRESS)
                .call()
            assert.equal(response.status, 200)
        })
        it('should return 404 if the stream does not have public publish permission', async () => {
            const stream = await getStreamrClient(streamOwner).createStream({
                name: 'Stream without public publish permission',
            })
            const response = await Streamr.api.v1.streams
                .publisher(stream.id, ZERO_ADDRESS)
                .call()
            assert.equal(response.status, 404)
        })
    })

    describe('GET /api/v1/streams/:id/subscribers', () => {
        it('does not require authentication', async () => {
            const json = await getStreamrClient(anonymousUser).getStreamSubscribers(streamId)
            assert.isOk(json)
        })
    })

    describe('GET /api/v1/streams/:id/subscriber/0x0000000000000000000000000000000000000000', () => {
        it('should return 200 if the stream has public subscribe permission', async () => {
            const stream = await getStreamrClient(streamOwner).createStream({
                name: 'Stream with public subscribe permission',
            })
            const permission = await Streamr.api.v1.streams
                .grantPublic(stream.id, 'stream_subscribe')
                .withAuthenticatedUser(streamOwner)
                .call()
            const response = await Streamr.api.v1.streams
                .subscriber(stream.id, ZERO_ADDRESS)
                .call()
            assert.equal(response.status, 200)
        })
        it('should return 404 if the stream does not have public subscribe permission', async () => {
            const stream = await getStreamrClient(streamOwner).createStream({
                name: 'Stream without public subscribe permission',
            })
            const response = await Streamr.api.v1.streams
                .subscriber(stream.id, ZERO_ADDRESS)
                .call()
            assert.equal(response.status, 404)
        })
    })

    describe('POST /api/v1/streams/:id/fields', () => {
        it('requires authentication', async () => {
            const response = await Streamr.api.v1.streams
                .setFields(streamId, [
                    {
                        name: 'text',
                        type: 'string',
                    },
                    {
                        name: 'user',
                        type: 'map',
                    },
                ])
                .call()

            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('validates existence of Stream', async () => {
            const response = await Streamr.api.v1.streams
                .setFields('non-existing-stream', [
                    {
                        name: 'text',
                        type: 'string',
                    },
                    {
                        name: 'user',
                        type: 'map',
                    },
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
                        type: 'string',
                    },
                    {
                        name: 'user',
                        type: 'map',
                    },
                ])
                .withAuthenticatedUser(anonymousUser)
                .call()

            await assertResponseIsError(response, 403, 'FORBIDDEN', 'stream_edit')
        })

        context('when called with valid body and permissions', () => {
            let response: Response

            before(async () => {
                response = await Streamr.api.v1.streams
                    .setFields(streamId, [
                        {
                            name: 'text',
                            type: 'string',
                        },
                        {
                            name: 'user',
                            type: 'map',
                        },
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
                        type: 'string',
                    },
                    {
                        name: 'user',
                        type: 'map',
                    },
                ])
            })
        })
    })

    describe('DELETE /api/v1/streams/:id', () => {
        it('happy path', async () => {
            const stream = await getStreamrClient(streamOwner).createStream({
                name: 'stream-id-' + Date.now(),
            })
            const deleted = await Streamr.api.v1.streams
                .delete(stream.id)
                .withAuthenticatedUser(streamOwner)
                .call()
            assert.equal(deleted.status, 204)
            assert.equal(deleted.size, 0)
        })
        it('deletes a stream with a permission', async () => {
            const stream = await getStreamrClient(streamOwner).createStream({
                name: 'stream-id-' + Date.now(),
            })
            const sharePermission = await Streamr.api.v1.streams
                .grant(stream.id, anonymousUser.address, 'stream_share')
                .withAuthenticatedUser(streamOwner)
                .call()
            assert.equal(sharePermission.status, 200)
            const deleted = await Streamr.api.v1.streams
                .delete(stream.id)
                .withAuthenticatedUser(streamOwner)
                .call()
            assert.equal(deleted.status, 204)
            assert.equal(deleted.size, 0)
        })

        async function assertStreamsStorageNodeCount(streamId: string, storageNodeCount: number) {
            const nodes = await Streamr.api.v1.storagenodes
                .findStorageNodesByStream(streamId)
                .call()
            if (nodes.status == 404) {
                return 0
            }
            const json = await nodes.json()
            assert.equal(json.length, storageNodeCount)
        }

        it('deletes streams storage nodes', async () => {
            const storageNodeAddress = StreamrClient.generateEthereumAccount().address
            console.log(storageNodeAddress)
            const stream = await getStreamrClient(streamOwner).createStream({
                name: 'stream-id-' + Date.now(),
            })
            console.log(stream.id)
            const storage = await Streamr.api.v1.storagenodes
                .addStorageNodeToStream(storageNodeAddress, stream.id)
                .withAuthenticatedUser(streamOwner)
                .call()
            assert.equal(storage.status, 200)

            await assertStreamsStorageNodeCount(stream.id, 1);

            const deleted = await Streamr.api.v1.streams
                .delete(stream.id)
                .withAuthenticatedUser(streamOwner)
                .call()
            assert.equal(deleted.status, 204)
            assert.equal(deleted.size, 0)

            // assert returns 404
            await assertStreamsStorageNodeCount(stream.id, 0)
        })
    })
})
