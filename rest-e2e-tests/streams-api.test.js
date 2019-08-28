const assert = require('chai').assert
const fs = require('fs')
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const assertResponseIsError = require('./test-utilities.js').assertResponseIsError

const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const AUTH_TOKEN = 'stream-api-tester-key'
const AUTH_TOKEN_2 = 'stream-api-tester2-key'

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)

describe('Streams API', () => {
    let streamId

    before(async () => {
        const response = await Streamr.api.v1.streams
            .create({
                name: 'stream-id-' + Date.now()
            })
            .withApiKey(AUTH_TOKEN)
            .execute()
        streamId = response.id
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
                .withApiKey(AUTH_TOKEN)
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
                .withApiKey(AUTH_TOKEN_2)
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
                    .withApiKey(AUTH_TOKEN)
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
                .withApiKey(AUTH_TOKEN)
                .call()

            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires WRITE permission on Stream', async () => {
            const response = await Streamr.api.v1.streams
                .uploadCsvFile(streamId, fs.createReadStream('./test-data/test-csv.csv'))
                .withApiKey(AUTH_TOKEN_2)
                .call()

            await assertResponseIsError(response, 403, 'FORBIDDEN', 'write')
        })

        it('validates that file is CSV', async () => {
            const response = await Streamr.api.v1.streams
                .uploadCsvFile(streamId, fs.createReadStream('./test-data/file.txt'))
                .withApiKey(AUTH_TOKEN)
                .call()

            await assertResponseIsError(response, 400, 'NOT_RECOGNIZED_AS_CSV')
        })

        context('when called with valid body and permissions', () => {
            let response

            before(async () => {
                response = await Streamr.api.v1.streams
                    .uploadCsvFile(streamId, fs.createReadStream('./test-data/test-csv.csv'))
                    .withApiKey(AUTH_TOKEN)
                    .call()
            })

            it('responds with 500', () => {
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

    describe('POST /api/v1/streams/:id/confirmCsvFileUpload', () => {
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

        it('validates existence of Stream', async () => {
            const response = await Streamr.api.v1.streams
                .confirmCsvUpload('non-existing-stream-id', {
                    fileUrl: '',
                    timestampColumnIndex: '0',
                    dateFormat: 'unix'
                })
                .withApiKey(AUTH_TOKEN)
                .call()

            await assertResponseIsError(response, 404, 'NOT_FOUND')
        })

        it('requires WRITE permission on Stream', async () => {
            const response = await Streamr.api.v1.streams
                .confirmCsvUpload(streamId, {
                    fileUrl: '',
                    timestampColumnIndex: '0',
                    dateFormat: 'unix'
                })
                .withApiKey(AUTH_TOKEN_2)
                .call()

            await assertResponseIsError(response, 403, 'FORBIDDEN', 'write')
        })

        context('when called with valid body and permissions', () => {
            let response

            before(async () => {
                const uploadResponse = await Streamr.api.v1.streams
                    .uploadCsvFile(streamId, fs.createReadStream('./test-data/test-csv.csv'))
                    .withApiKey(AUTH_TOKEN)
                    .call()
                const uploadJson = await uploadResponse.json()

                response = await Streamr.api.v1.streams
                    .confirmCsvUpload(streamId, {
                        fileId: uploadJson.fileId,
                        timestampColumnIndex: 0,
                        dateFormat: 'unix'
                    })
                    .withApiKey(AUTH_TOKEN)
                    .call()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('updates stream config fields', async () => {
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
