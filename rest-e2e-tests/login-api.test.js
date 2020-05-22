const assert = require('chai').assert
const fetch = require('node-fetch')
const StreamrClient = require('streamr-client')

const URL = 'http://localhost/api/v1'

const API_KEY = 'tester1-api-key'

const TIMEOUT = 5000

describe('Login API', () => {

    function loginWithApiKey(apiKey) {
        return fetch(`${URL}/login/apikey`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                apiKey: apiKey
            })
        })
    }

    function getUserDetails(sessionToken) {
        return fetch(`${URL}/users/me`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer '+sessionToken
            }
        })
    }

    function getStreamPermissions(streamId, sessionToken) {
        const headers = {
            'Content-Type': 'application/json',
        }
        if (sessionToken) {
            headers.Authorization = 'Bearer '+sessionToken
        }
        return fetch(`${URL}/streams/${streamId}/permissions/me`, {
            method: 'GET',
            headers
        })
    }

    function sleep(ms) {
        return new Promise(resolve => setTimeout(() => resolve(), ms))
    }

    describe('POST /api/v1/login/apikey', () => {

        it('responds with status code 200', async () => {
            const response = await loginWithApiKey(API_KEY)
            assert.equal(response.status, 200)
        }).timeout(TIMEOUT)

        it('response contains sessionToken', async () => {
            const response = await loginWithApiKey(API_KEY)
            const json = await response.json()
            assert(json.token != null, 'session token was null!')
        }).timeout(TIMEOUT)

        it('responds with status 401 when wrong token even if endpoint does not require authentication', async () => {
            const response = await getStreamPermissions('some-stream-id', 'wrong-token')
            assert.equal(response.status, 401)
        }).timeout(TIMEOUT)

        it('responds with status 404 when no token provided if endpoint does not require authentication', async () => {
            const response = await getStreamPermissions('some-stream-id')
            assert.equal(response.status, 404)
        }).timeout(TIMEOUT)

        it('updates the user\'s lastLogin field', async () => {
            const testStartTime = Date.now()

            await sleep(1000) // lastLogin only has second precision, so wait 1 sec

            const response = await loginWithApiKey(API_KEY)
            const json = await response.json()

            const userDetailsReponse = await getUserDetails(json.token)
            const me = await userDetailsReponse.json()
            const lastLogin = new Date(me.lastLogin).getTime()
            assert(lastLogin >= testStartTime, `user lastLogin was not updated. lastLogin: ${lastLogin}, testStartTime: ${testStartTime}`)
        }).timeout(TIMEOUT)

        it('does not fail even when flooded with simultaneous logins for same user (CORE-1660)', () => {
            const promises = []
            for (let i=0; i<100; i++) {
                promises.push(loginWithApiKey(API_KEY))
            }

            return Promise.all(promises)
                .then((results) => {
                    const errors = results.filter(it => it.status !== 200)
                    return Promise.all(errors.map(it => it.json()))
                })
                .then((errorsJson) => {
                    if (errorsJson.length) {
                        throw new Error(`Got errors: ${JSON.stringify(errorsJson)}`)
                    }
                })
        }).timeout(TIMEOUT)
    })
})
