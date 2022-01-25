import {assert} from 'chai'
import Streamr from './streamr-api-clients'
import {StreamrClient} from 'streamr-client'
import {createProduct, getStreamrClient, testUsers} from './test-utilities'
import {EthereumAccount} from './EthereumAccount'

const NODE_ADDRESS = '0xFCAd0B19bB29D4674531d6f115237E16AfCE377c'  // address of streamr.ethereum.nodePrivateKey account

const createDataUnion = async (admin: EthereumAccount) => {
    const adminClient = getStreamrClient(admin)
    return await adminClient.deployDataUnion({
        adminFee: 0.1,
        joinPartAgents: [NODE_ADDRESS],
    })
}

const createJoinRequest = async (joiner: EthereumAccount, dataUnionAddress: string) => {
    const joinerClient = getStreamrClient(joiner)
    const joinResponse = await joinerClient.getDataUnion(dataUnionAddress).join()
    return joinResponse.id
}

describe('DataUnions API', () => {

    describe('PUT /api/v2/dataunions/:id/joinRequests/:id', function () {

        this.timeout(60 * 1000)

        it('happy path', async () => {
            const admin = testUsers.tokenHolder
            const dataUnionAddress = (await createDataUnion(admin)).getAddress()
            await createProduct(admin, dataUnionAddress, 'DATAUNION')
            const joiner = StreamrClient.generateEthereumAccount()
            const joinRequestId = await createJoinRequest(joiner, dataUnionAddress)
            const response = await Streamr.api.v1.dataunions
                .approveJoinRequest(joinRequestId, dataUnionAddress)
                .withAuthenticatedUser(admin)
                .call()
            assert.equal(response.status, 200)
            await getStreamrClient().getDataUnion(dataUnionAddress).isMember(joiner.address)
        })
    })
})
