import {assert} from 'chai'
import Streamr from './streamr-api-clients'
import {StreamrClient} from 'streamr-client'
import {createProduct, getSessionToken} from './test-utilities'

describe('Permissions API', () => {
    const me = StreamrClient.generateEthereumAccount()
    const existingUser = StreamrClient.generateEthereumAccount()
    const beneficiaryAddress = '0x0000400006000008000000000000000000000100';

    before(async () => {
        // Make sure the "existingUser" exists by logging them in
        await getSessionToken(existingUser)
    })

    describe('POST /api/v1/products/{productId}/permissions', () => {
        let product: any

        before(async () => {
            product = await createProduct(me, beneficiaryAddress, 'NORMAL')
        })

        it('can grant a permission to an existing user using Ethereum address', async () => {
            const response = await Streamr.api.v1.products
                .grant(product.id, existingUser.address, 'product_get')
                .withAuthenticatedUser(me)
                .call()
            assert.equal(response.status, 200)
        })

        it('can grant a permission to a non-existing user using Ethereum address', async () => {
            const response = await Streamr.api.v1.products
                .grant(product.id, StreamrClient.generateEthereumAccount().address, 'product_get')
                .withAuthenticatedUser(me)
                .call()
            assert.equal(response.status, 200)
        })
    })

    describe('DELETE /api/v1/products/{productId}/permissions/{permissionId}', function () {
        let product: any

        before(async function () {
            product = await createProduct(me, beneficiaryAddress, 'NORMAL')
        })

        it('delete a permission', async function () {
            const permissionResponse = await Streamr.api.v1.products
                .grant(product.id, StreamrClient.generateEthereumAccount().address, 'product_get')
                .withAuthenticatedUser(me)
                .call()
            const permissionGet = await permissionResponse.json()
            assert.equal(permissionResponse.status, 200)

            const response = await Streamr.api.v1.products
                .deletePermission(product.id, permissionGet.id)
                .withAuthenticatedUser(me)
                .call()
            assert.equal(response.status, 204)
        })

        it('deleting last share permission is not allowed', async function () {
            const ownPermissionsResponse = await Streamr.api.v1.products
                .getOwnPermissions(product.id)
                .withAuthenticatedUser(me)
                .call()
            const ownPermissions = await ownPermissionsResponse.json()
            assert.equal(ownPermissionsResponse.status, 200)
            const sharePermission = ownPermissions.filter((permission: any) => permission.operation === 'product_share')[0]

            const response = await Streamr.api.v1.products
                .deletePermission(product.id, sharePermission.id)
                .withAuthenticatedUser(me)
                .call()
            assert.equal(response.status, 500)
        })

        it('deletes permissions', async function () {
            const ownPermissionsResponse = await Streamr.api.v1.products
                .getOwnPermissions(product.id)
                .withAuthenticatedUser(me)
                .call()
            const ownPermissions = await ownPermissionsResponse.json()
            assert.equal(ownPermissionsResponse.status, 200)
            const permissionsToDelete = ownPermissions.filter((permission: any) => permission.operation !== 'product_share')

            permissionsToDelete.forEach(async function (permission: any) {
                const response = await Streamr.api.v1.streams
                    .deletePermission(product.id, permission.id)
                    .withAuthenticatedUser(me)
                    .call()
                assert.equal(response.status, 204, `delete permission unexpected status ${response.status} for ${JSON.stringify(permission)}`)
            })
        })
    })
})
