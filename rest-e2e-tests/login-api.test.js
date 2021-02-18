const assert = require('chai').assert
const fetch = require('node-fetch')
const StreamrClient = require('streamr-client')
const getSessionToken = require('./test-utilities.js').getSessionToken

const URL = 'http://localhost/api/v1'
const TIMEOUT = 10000

describe('Login API', () => {
    function getUserDetails(sessionToken) {
        return fetch(`${URL}/users/me`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + sessionToken,
            },
        })
    }

    function getStreamPermissions(streamId, sessionToken) {
        const headers = {
            'Content-Type': 'application/json',
        }
        if (sessionToken) {
            headers.Authorization = 'Bearer ' + sessionToken
        }
        return fetch(`${URL}/streams/${streamId}/permissions/me`, {
            method: 'GET',
            headers,
        })
    }

    function sleep(ms) {
        return new Promise(resolve => setTimeout(() => resolve(), ms))
    }

    const user = StreamrClient.generateEthereumAccount()

    describe('POST /api/v1/login/response', () => {
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

            const token = await getSessionToken(user)
            const userDetailsReponse = await getUserDetails(token)
            const me = await userDetailsReponse.json()
            const lastLogin = new Date(me.lastLogin).getTime()
            assert(lastLogin >= testStartTime, `user lastLogin was not updated. lastLogin: ${lastLogin}, testStartTime: ${testStartTime}`)
        }).timeout(TIMEOUT)
    })
})
