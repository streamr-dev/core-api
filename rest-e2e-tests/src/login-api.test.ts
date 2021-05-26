import {assert} from 'chai'
import fetch from 'node-fetch'
import {getSessionToken} from './test-utilities'
import {StreamrClient} from "streamr-client";

const URL = 'http://localhost/api/v1'
const TIMEOUT = 10000

describe('Login API', () => {

    function getUserDetails(sessionToken: string) {
        return fetch(`${URL}/users/me`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + sessionToken,
            },
        })
    }

    function getStreamPermissions(streamId: string, sessionToken?: string) {
        const headers: any = {
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

    function sleep(ms: number) {
        return new Promise(resolve => setTimeout(() => resolve(undefined), ms))
    }

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

            const user = StreamrClient.generateEthereumAccount();
            const token = await getSessionToken(user)
            const userDetailsReponse = await getUserDetails(token)
            const me = await userDetailsReponse.json()
            const lastLogin = new Date(me.lastLogin).getTime()
            assert(lastLogin >= testStartTime, `user lastLogin was not updated. lastLogin: ${lastLogin}, testStartTime: ${testStartTime}`)
        }).timeout(TIMEOUT)
    })
})
