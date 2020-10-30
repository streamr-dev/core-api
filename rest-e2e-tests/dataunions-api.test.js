const assert = require('chai').assert
const Streamr = require('./streamr-api-clients')

describe('DataUnions API', () => {
    describe('GET /api/v1/dataunions', () => {

        it('responds with status code 200', async () => {
            const response = await Streamr.api.v1.dataunions.list().call()
            assert.equal(response.status, 200)
        })

    })
})
