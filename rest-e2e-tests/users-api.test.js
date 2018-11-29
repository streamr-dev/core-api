const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')
const assertResponseIsError = require('./test-utilities.js').assertResponseIsError

const URL = 'http://localhost:8081/streamr-core/api/v1/'
const LOGGING_ENABLED = false

const ADMIN_USER_TOKEN = 'tester-admin-api-key'
const USER_TOKEN = 'tester1-api-key'

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)

const timestamp = Date.now()

describe('Users API', () => {
    describe('POST /api/v1/users', () => {
        it('requires authentication', async () => {
            const response = await Streamr.api.v1.users
                .create({
                    username: `users-api-test-${timestamp}@streamr.com`,
                    password: 'password',
                    name: 'Users API Test',
                })
                .call()

            await assertResponseIsError(response, 401, 'NOT_AUTHENTICATED')
        })

        it('checks user permission level', async () => {
            const response = await Streamr.api.v1.users
                .create({
                    username: `users-api-test-${timestamp}@streamr.com`,
                    password: 'password',
                    name: 'Users API Test',
                })
                .withAuthToken(USER_TOKEN)
                .call()

            await assertResponseIsError(response, 403, 'NOT_PERMITTED')
        })

        it('validates body', async () => {
            const response = await Streamr.api.v1.users
                .create({})
                .withAuthToken(ADMIN_USER_TOKEN)
                .call()

            await assertResponseIsError(response, 422, 'VALIDATION_ERROR')
        })

        context('when called with valid body and headers', () => {
            let response
            let json

            before(async () => {
                response = await Streamr.api.v1.users
                    .create({
                        username: `users-api-test-${timestamp}@streamr.com`,
                        password: 'password',
                        name: 'Users API Test',
                    })
                    .withAuthToken(ADMIN_USER_TOKEN)
                    .call()
                json = await response.json()
            })

            it('responds with 200', () => {
                assert.equal(response.status, 200)
            })

            it('responds with User', () => {
                assert.deepEqual(json, {
                    username: `users-api-test-${timestamp}@streamr.com`,
                    name: 'Users API Test',
                    timezone: 'UTC'
                })
            })
        })
    })
})
