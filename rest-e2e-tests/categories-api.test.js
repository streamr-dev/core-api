const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')


const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)
const schemaValidator = new SchemaValidator()

describe('Categories API', () => {
    describe('GET /api/v1/categories', () => {
        let response
        let json

        before(async () => {
            response = await Streamr.api.v1.categories.list().call()
            json = await response.json()
        })

        it('responds with status code 200', () => {
            assert.equal(response.status, 200)
        })

        it('body contains at least expected array of categories', () => {
            assert.includeDeepMembers(json, [
                {
                    'id': 'ad-id',
                    'name': 'Advertising',
                    'imageUrl': null
                },
                {
                    'id': 'automobile-id',
                    'name': 'Automobile',
                    'imageUrl': 'http://localhost:8081/streamr-core/uploads/auto.png'
                },
                {
                    'id': 'cryptocurrencies-id',
                    'name': 'Cryptocurrency',
                    'imageUrl': 'http://localhost:8081/streamr-core/uploads/crypto.png'
                },
                {
                    'id': 'financial-id',
                    'name': 'Financial',
                    'imageUrl': 'http://localhost:8081/streamr-core/uploads/finance.png'
                },
                {
                    'id': 'personal-id',
                    'name': 'Personal',
                    'imageUrl': null
                },
                {
                    'id': 'satellite-id',
                    'name': 'Satellite',
                    'imageUrl': 'http://localhost:8081/streamr-core/uploads/satellites-in-space-680px.png'
                }
            ])
        })

        it('body passes schema validation', () => {
            json.forEach((categoryData) => {
                const errors = schemaValidator.validateCategory(categoryData)
                assert(errors.length === 0, schemaValidator.toMessages(errors))
            })
        })
    })
})