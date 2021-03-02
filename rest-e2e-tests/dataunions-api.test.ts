import { assert } from 'chai'
import Streamr from './streamr-api-clients'
const StreamrClient = require('streamr-client')
import { getStreamrClient, testUsers } from './test-utilities'
import { EthereumAccount } from './EthereumAccount'

const NODE_ADDRESS = '0xFCAd0B19bB29D4674531d6f115237E16AfCE377c';  // address of streamr.ethereum.nodePrivateKey account
const DATA_UNION_VERSION = 2;

const createDataUnion = async (admin: EthereumAccount) => {
	const adminClient = getStreamrClient(admin);
	const dataUnion = await adminClient.deployDataUnion({
		owner: admin.address,
		adminFee: 0.1,
		joinPartAgents: [NODE_ADDRESS]
	});
	return await dataUnion.deployed();
};

const createProduct = async (owner: EthereumAccount, beneficiaryAddress: string) => {
	const properties = {
		beneficiaryAddress,
		type: 'DATAUNION',
		dataUnionVersion: DATA_UNION_VERSION
	}
	const json = await Streamr.api.v1.products
		.create(properties)
		.withAuthenticatedUser(owner)
		.execute();
	return json;
};

const createJoinRequest = async (joiner: EthereumAccount, dataUnion: any) => {
	const joinerClient = getStreamrClient(joiner);
	const joinResponse = await joinerClient.joinDataUnion({
		dataUnion,
		member: joiner.address
	});
	return joinResponse.id
};

describe('DataUnions API', () => {

	describe('GET /api/v1/dataunions', () => {

		it('responds with status code 200', async () => {
			const response = await Streamr.api.v1.dataunions.list().call()
			assert.equal(response.status, 200)
		})

	})

	describe('PUT /api/v1/dataunions/:id/joinRequests/:id', function() {

		this.timeout(60 * 1000);

		let dataUnion: any;
		const admin = testUsers.tokenHolder
		const joiner = StreamrClient.generateEthereumAccount();
		let joinRequestId: string;

		before(async () => {
			dataUnion = await createDataUnion(admin)
			await createProduct(admin, dataUnion.address)
			joinRequestId = await createJoinRequest(joiner, dataUnion)
		});

		it('happy path', async () => {
			const response = await Streamr.api.v1.dataunions
				.approveJoinRequest(joinRequestId, dataUnion.address)
				.withAuthenticatedUser(admin)
				.call();
			assert.equal(response.status, 200);
			await getStreamrClient(admin).hasJoined(joiner.address, { dataUnion })
		});

	})
})
