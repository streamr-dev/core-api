const assert = require('chai').assert
const fetch = require('node-fetch')

const URL = 'http://localhost:8081/streamr-core/api/v1'

const TIMEOUT = 5000

describe('CORS Requests', () => {
    const origin = 'http://localhost:8080'

    function getOptions() {
        return fetch(URL, {
            "headers": {
                origin,
            },
            "method": "OPTIONS",
            "mode": "cors"
        })
    }

    it('includes appropriate CORS allow headers in response', async () => {
        const optionsResponse = await getOptions()
        const allowedOrigin = optionsResponse.headers.get('access-control-allow-origin')
        const allowedHeaders = optionsResponse.headers.get('access-control-allow-headers')
        assert.include(allowedOrigin, origin, 'Should allow arbitrary origin')
        // test for allowed Streamr-Client header is important
        assert.include(allowedHeaders, 'Streamr-Client', 'Must allow custom Streamr-Client HTTP header.')
    }).timeout(TIMEOUT)
})
