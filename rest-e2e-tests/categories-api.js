const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')

const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false


const Streamr = initStreamrApi(URL, LOGGING_ENABLED)

describe('Categories API', () => {
    it('GET /api/v1/categories', async () => {
        const response = await Streamr.api.v1.categories.list().call()
        const json = await response.json()

        assert.equal(response.status, 200)
        assert.deepEqual(json, [
            {
                'id': 'ad-id',
                'name': 'Advertising',
                'imageUrl': null
            },
            {
                'id': 'automobile-id',
                'name': 'Automobile',
                'imageUrl': 'auto.png'
            },
            {
                'id': 'cryptocurrencies-id',
                'name': 'Cryptocurrency',
                'imageUrl': 'crypto.png'
            },
            {
                'id': 'financial-id',
                'name': 'Financial',
                'imageUrl': 'finance.png'
            },
            {
                'id': 'personal-id',
                'name': 'Personal',
                'imageUrl': null
            },
            {
                'id': 'satellite-id',
                'name': 'Satellite',
                'imageUrl': 'satellites-in-space-680px.png'
            }
        ])
    })
})