const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const assertResponseIsError = require('./test-utilities.js').assertResponseIsError

const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const AUTH_TOKEN = 'stream-api-tester-key'
const AUTH_TOKEN_2 = 'stream-api-tester2-key'

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)

describe('Streams API', () => {
    describe('POST /api/v1/streams/:id/fields', () => {
        let streamId

        before(async () => {
            const response = await Streamr.api.v1.streams
                .create({
                    name: 'stream-id-' + Date.now()
                })
                .withAuthToken(AUTH_TOKEN)
                .execute()
            streamId = response.id
        })

        it('requires authentication', async () => {
            const response = await Streamr.api.v1.streams
                .setFields(streamId, [
                    {
                        name: 'text',
                        type: 'string'
                    },
                    {
                        name: 'user',
                        type: 'object'
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
                        type: 'object'
                    }
                ])
                .withAuthToken(AUTH_TOKEN)
                .call()

            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires WRITE permission on Stream', async () => {
            const response = await Streamr.api.v1.streams
                .setFields(streamId, [
                    {
                        name: 'text',
                        type: 'string'
                    },
                    {
                        name: 'user',
                        type: 'object'
                    }
                ])
                .withAuthToken(AUTH_TOKEN_2)
                .call()

            await assertResponseIsError(response, 403, 'FORBIDDEN', 'write')
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
                            type: 'object'
                        }
                    ])
                    .withAuthToken(AUTH_TOKEN)
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
                        type: 'object'
                    }
                ])
            })
        })
    })
})
