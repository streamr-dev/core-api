const assert = require('chai').assert
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const StreamrClient = require('streamr-client')

const URL = 'http://localhost/api/v1/'
const LOGGING_ENABLED = false

const Streamr = initStreamrApi(URL, LOGGING_ENABLED)
const schemaValidator = new SchemaValidator()

const API_KEY = 'tester1-api-key'

describe('Permissions API', () => {
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
})
