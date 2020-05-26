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

    describe('POST /api/v1/streams/:id/uploadCsvFile', () => {
        it('requires authentication', async () => {
            const response = await Streamr.api.v1.streams
                .uploadCsvFile(streamId, fs.createReadStream('./test-data/test-csv.csv'))
                .call()

            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('validates existence of Stream', async () => {
            const response = await Streamr.api.v1.streams
                .uploadCsvFile('non-existing-id', fs.createReadStream('./test-data/test-csv.csv'))
                .withApiKey(API_KEY)
                .call()

            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires stream_publish permission on Stream', async () => {
            const response = await Streamr.api.v1.streams
                .uploadCsvFile(streamId, fs.createReadStream('./test-data/test-csv.csv'))
                .withApiKey(API_KEY_2)
                .call()

            await assertResponseIsError(response, 403, 'FORBIDDEN', 'stream_publish')
        })

        it('validates that file is CSV', async () => {
            const response = await Streamr.api.v1.streams
                .uploadCsvFile(streamId, fs.createReadStream('./test-data/file.txt'))
                .withApiKey(API_KEY)
                .call()

            await assertResponseIsError(response, 400, 'NOT_RECOGNIZED_AS_CSV')
        })

        context('when called with valid body and permissions', () => {
            let response

            before(async () => {
                response = await Streamr.api.v1.streams
                    .uploadCsvFile(streamId, fs.createReadStream('./test-data/test-csv.csv'))
                    .withApiKey(API_KEY)
                    .call()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('returns json', async () => {
                const json = await response.json()
                assert.deepEqual(Object.keys(json), ['fileId', 'schema'])
                assert.isString(json.fileId)
                assert.deepEqual(json.schema, {
                    headers: [
                        'seq',
                        'age',
                        'digit',
                        'word',
                    ],
                    timeZone: 'UTC',
                    timestampColumnIndex: null
                })
            })
        })
    })

    describe('POST /api/v1/streams/:id/confirmCsvFileUpload', function() {
        it('requires authentication', async () => {
            const response = await Streamr.api.v1.streams
                .confirmCsvUpload(streamId, {
                    fileUrl: '',
                    timestampColumnIndex: '0',
                    dateFormat: 'unix'
                })
                .call()

            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('validates existence of Stream', async function() {
            const response = await Streamr.api.v1.streams
                .confirmCsvUpload('non-existing-stream-id', {
                    fileUrl: '',
                    timestampColumnIndex: '0',
                    dateFormat: 'unix'
                })
                .withApiKey(API_KEY)
                .call()

            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires stream_publish permission on Stream', async function() {
            const response = await Streamr.api.v1.streams
                .confirmCsvUpload(streamId, {
                    fileUrl: '',
                    timestampColumnIndex: '0',
                    dateFormat: 'unix'
                })
                .withApiKey(API_KEY_2)
                .call()

            await assertResponseIsError(response, 403, 'FORBIDDEN', 'stream_publish')
        })

        context('when called with valid body and permissions', function() {
            this.timeout(5000)
            let response

            before(async function() {
                const uploadResponse = await Streamr.api.v1.streams
                    .uploadCsvFile(streamId, fs.createReadStream('./test-data/test-csv.csv'))
                    .withApiKey(API_KEY)
                    .call()
                const uploadJson = await uploadResponse.json()

                response = await Streamr.api.v1.streams
                    .confirmCsvUpload(streamId, {
                        fileId: uploadJson.fileId,
                        timestampColumnIndex: 0,
                        dateFormat: 'unix'
                    })
                    .withApiKey(API_KEY)
                    .call()
            })

            it('responds with 200', function() {
                assert.equal(response.status, 200)
            })

            it('updates stream config fields', async function() {
                const json = await response.json()
                assert.deepEqual(json.config.fields, [{
                    name: 'age',
                    type: 'timestamp'
                },
                {
                    name: 'digit',
                    type: 'timestamp'
                },
                {
                    name: 'word',
                    type: 'string'
                }])
            })
        })
    })
})
