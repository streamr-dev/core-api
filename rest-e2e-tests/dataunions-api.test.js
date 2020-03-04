const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')


const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)
const schemaValidator = new SchemaValidator()

describe('DataUnions API', () => {
    describe('GET /api/v1/dataunions', () => {

        it('responds with status code 200', async () => {
            const response = await Streamr.api.v1.dataunions.list().call()
            assert.equal(response.status, 200)
        })

    })
})
