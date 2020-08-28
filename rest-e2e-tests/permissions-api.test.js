const assert = require('chai').assert
const ethereumJsUtil = require('ethereumjs-util')
const keythereum = require('keythereum')
const Web3 = require('web3')
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const StreamrClient = require('streamr-client')

const URL = 'http://localhost/api/v1/'
const LOGGING_ENABLED = false

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)
const schemaValidator = new SchemaValidator()

const API_KEY = 'tester1-api-key'

describe('Permissions API', function() { // use "function" instead of arrow because of this.timeout(...)
    this.timeout(120 * 1000)

    const me = StreamrClient.generateEthereumAccount()
    const nonExistingUser = StreamrClient.generateEthereumAccount()
    const existingUser = StreamrClient.generateEthereumAccount()

    let mySessionToken

    before(async () => {
        // Get sessionToken for user "me"
        mySessionToken = await new StreamrClient({
            restUrl: URL,
            auth: {
                privateKey: me.privateKey,
            }
        }).session.getSessionToken()

        // Make sure the "existingUser" exists by logging them in
        await new StreamrClient({
            restUrl: URL,
            auth: {
                privateKey: existingUser.privateKey,
            }
        }).session.getSessionToken()
    })

    describe('POST /api/v1/streams/{id}/permissions', () => {
        let stream

        before(async () => {
            stream = await Streamr.api.v1.streams
                .create({
                    name: `permissions-api.test.js-${Date.now()}`
                })
                .withSessionToken(mySessionToken)
                .execute()
        })

        it('can grant a permission to an existing user using email address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, 'tester1@streamr.com', 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 200)
        })

        it('can grant a permission to a non-existing user using email address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, `${Date.now()}@foobar.invalid`, 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 200)
        })

        it('can grant a permission to an existing user using Ethereum address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, existingUser.address, 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 200)
        })

        it('can grant a permission to a non-existing user using Ethereum address', async () => {
            const response = await Streamr.api.v1.streams
                .grant(stream.id, StreamrClient.generateEthereumAccount().address, 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 200)
        })

        describe('race conditions', () => {
            // The worst case is that there are parallel requests open for all the different operations
            const operations = [
                'stream_get',
                'stream_edit',
                'stream_subscribe',
                'stream_publish',
                'stream_delete',
                'stream_share',
            ]

            // Tests here are repeated 50 times, as they have some chance of an individual attempt
            // succeeding even if the race condition is not handled properly
            const ITERATIONS = 50

            it('survives a race condition when granting multiple permissions to a non-existing user using Ethereum address', async () => {
                for (let i=0; i<ITERATIONS; i++) {
                    const responses = await Promise.all(operations.map((operation) => {
                        return Streamr.api.v1.streams
                            .grant(stream.id, StreamrClient.generateEthereumAccount().address, operation)
                            .withSessionToken(mySessionToken)
                            .call()
                    }))
                    // All response statuses must be 200
                    assert.deepEqual(responses.map((r) => r.status), operations.map((op) => 200), `Race condition test failed on iteration ${i}`)
                }
            })

            it('survives a race condition when granting multiple permissions to a non-existing user using email address', async () => {
                for (let i=0; i<ITERATIONS; i++) {
                    const responses = await Promise.all(operations.map((operation) => {
                        return Streamr.api.v1.streams
                            .grant(stream.id, `race-condition-${i}@foobar.invalid`, 'stream_get')
                            .withSessionToken(mySessionToken)
                            .call()
                    }))
                    // All response statuses must be 200
                    assert.deepEqual(responses.map((r) => r.status), operations.map((op) => 200), `Race condition test failed on iteration ${i}`)
                }
            })
        })
    })

    describe('DELETE /api/v1/streams/{streamId}/permissions/{permissionId}', function() {
        let stream

        before(async function() {
            stream = await Streamr.api.v1.streams
                .create({
                    name: `permissions-api.test.js-delete-${Date.now()}`
                })
                .withSessionToken(mySessionToken)
                .execute()
        })

        it('delete a permission', async function() {
            const permissionResponse = await Streamr.api.v1.streams
                .grant(stream.id, StreamrClient.generateEthereumAccount().address, 'stream_get')
                .withSessionToken(mySessionToken)
                .call()
            const permissionGet = await permissionResponse.json()
            assert.equal(permissionResponse.status, 200)

            const response = await Streamr.api.v1.streams
                .delete(stream.id, permissionGet.id)
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 204)
        })

        it('deleting last share permission is not allowed', async function() {
            const ownPermissionsResponse = await Streamr.api.v1.streams
                .getOwnPermissions(stream.id)
                .withSessionToken(mySessionToken)
                .call()
            const ownPermissions = await ownPermissionsResponse.json()
            assert.equal(ownPermissionsResponse.status, 200)
            const sharePermission = ownPermissions.filter((permission) => permission.operation === 'stream_share')[0]

            const response = await Streamr.api.v1.streams
                .delete(stream.id, sharePermission.id)
                .withSessionToken(mySessionToken)
                .call()
            assert.equal(response.status, 500)
        })

        it('deletes permissions', async function() {
            const ownPermissionsResponse = await Streamr.api.v1.streams
                .getOwnPermissions(stream.id)
                .withSessionToken(mySessionToken)
                .call()
            const ownPermissions = await ownPermissionsResponse.json()
            assert.equal(ownPermissionsResponse.status, 200)
            const permissionsToDelete = ownPermissions.filter((permission) => permission.operation !== 'stream_share')

            permissionsToDelete.forEach(async function(permission) {
                const response = await Streamr.api.v1.streams
                    .delete(stream.id, permission.id)
                    .withSessionToken(mySessionToken)
                    .call()
                assert.equal(response.status, 204, `delete permission unexpected status ${response.status} for ${JSON.stringify(permission)}`)
            })
        })
    })

    describe('CORE-1949', function() {
        const AUTH_TOKEN = 'product-api-tester-key'
        async function obtainChallenge(address) {
            const json = await Streamr.api.v1.login
                .challenge(address)
                .execute()
            return json
        }

        async function submitChallenge(challenge, signature) {
            const json = await Streamr.api.v1.integration_keys
                .create({
                    name: 'My Ethereum ID',
                    service: 'ETHEREUM_ID',
                    challenge: challenge,
                    signature: signature
                })
                .withApiKey(AUTH_TOKEN) // TODO: Remove api key
                .execute()
            return json
        }

        async function threadA() {
            // TODO: Implement
            console.log('thread A')
            return 1
        }

        async function threadB() {
            // TODO: Implement
            console.log('thread B')
            return 2
        }

        async function execute(threads) {
            const responses = await Promise.all(threads.map((func) => {
                return func()
            }))
            console.log("results: " + responses)
        }

        it('race condition',async function() {
            const keyA = keythereum.create()
            const privateKeyA = '0x' + keyA.privateKey.toString('hex')
            const publicAddressA = '0x' + ethereumJsUtil.privateToAddress(keyA.privateKey).toString('hex')

            const keyB = keythereum.create()
            const privateKeyB = '0x' + keyB.privateKey.toString('hex')
            const publicAddressB = '0x' + ethereumJsUtil.privateToAddress(keyB.privateKey).toString('hex')

            const challenge = await obtainChallenge(publicAddressA)
            const web3 = new Web3()
            const signedChallenge = web3.eth.accounts.sign(challenge.challenge, privateKeyA)
            const challengeResult = await submitChallenge(challenge, signedChallenge.signature)
            //console.log(challengeResult)

            const stream = await Streamr.api.v1.streams
                .create({
                    name: `permissions-api.test.js-${Date.now()}`
                })
                .withSessionToken(mySessionToken)
                .execute()

            const threads = [
                threadA,
                threadB,
            ]

            execute(threads)
        })
    })
})
