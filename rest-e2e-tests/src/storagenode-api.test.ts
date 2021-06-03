import { assert } from 'chai'
import Streamr from './streamr-api-clients'
import _ from 'lodash';
import { StreamrClient } from 'streamr-client'
import { getStreamrClient, assertEqualEthereumAddresses } from './test-utilities'
import { EthereumAccount } from './EthereumAccount';

const ETHEREUM_ADDRESS_PREFIX = '0x';
const createMockEthereumAddress = () => {
	const PREFIX = 'ABCdeF'
	const LENGTH = 40;
	return ETHEREUM_ADDRESS_PREFIX + PREFIX + _.padStart(Math.floor(Math.random() * Number.MAX_SAFE_INTEGER).toString(16), (LENGTH - PREFIX.length), '0');
};
const createStream = async (user: EthereumAccount ) => {
	const stream = await getStreamrClient(user).createStream();
	return stream.id;
};
const findStreamsByStorageNode = (storageNodeAddress: string) => {
	return Streamr.api.v1.storagenodes
		.findStreamsByStorageNode(storageNodeAddress)
		.call();
};
const findStorageNodesByStream = (streamId: string) => {
	return Streamr.api.v1.storagenodes
		.findStorageNodesByStream(streamId)
		.call();
};
const addStorageNodeToStream = async (storageNodeAddress: string, streamId: string, user: EthereumAccount|undefined) => {
	return Streamr.api.v1.storagenodes
		.addStorageNodeToStream(storageNodeAddress, streamId)
		.withAuthenticatedUser(user)
		.call();
};
const removeStorageNodeFromStream = async (storageNodeAddress: string, streamId: string, user: EthereumAccount|undefined) => {
	return Streamr.api.v1.storagenodes
		.removeStorageNodeFromStream(storageNodeAddress, streamId)
		.withAuthenticatedUser(user)
		.call();
};
const getStorageNodeCount = async (streamId: string) => {
	const response = await findStorageNodesByStream(streamId);
	const json = await response.json()
	return json.length;
};

describe('Storage Node API', () => {

	const streamOwner = StreamrClient.generateEthereumAccount()
	const otherUser = StreamrClient.generateEthereumAccount()

	describe('GET /storageNodes/:address/stream', () => {
		let storageNodeAddress: string;
		let streamId: string;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream(streamOwner);
			await addStorageNodeToStream(storageNodeAddress, streamId, streamOwner);
		});

		it('happy path', async () => {
			const response = await findStreamsByStorageNode(storageNodeAddress);
			const json = await response.json()
			assert.equal(json.length, 1);
			assert.equal(json[0].id, streamId);
		});

		it('case insensitive', async () => {
			const upperCaseAddress = ETHEREUM_ADDRESS_PREFIX + storageNodeAddress.substring(ETHEREUM_ADDRESS_PREFIX.length).toUpperCase()
			const response = await findStreamsByStorageNode(upperCaseAddress);
			const json = await response.json()
			assert.equal(json.length, 1);
			assert.equal(json[0].id, streamId);
		});

		it('no storage nodes', async () => {
			const nonExistingStorageNodeAddress = createMockEthereumAddress();
			const response = await findStreamsByStorageNode(nonExistingStorageNodeAddress);
			const json = await response.json()
			assert.equal(json.length, 0);
		});

		it('malformed storage node address', async () => {
			const response = await findStreamsByStorageNode('foobar');
			assert.equal(response.status, 422);
		});
	});

	describe('GET /streams/:streamId/storageNodes', () => {
		let storageNodeAddress: string;
		let streamId: string;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream(streamOwner);
			await addStorageNodeToStream(storageNodeAddress, streamId, streamOwner);
		});

		it('happy path', async () => {
			const response = await findStorageNodesByStream(streamId);
			const json = await response.json();
			assert.equal(json.length, 1);
			assertEqualEthereumAddresses(json[0].storageNodeAddress, storageNodeAddress);
		});

		it('stream not found', async () => {
			const response = await findStorageNodesByStream('non-existing');
			assert.equal(response.status, 404);
		});
	});

	describe('POST /streams/:streamId/storageNodes', () => {
		let storageNodeAddress: string;
		let streamId: string;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream(streamOwner);
		});

		it('happy path', async () => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId, streamOwner);
			assert.equal(response.status, 200)
			const json = await response.json()
			assertEqualEthereumAddresses(json.storageNodeAddress, storageNodeAddress);
			const storageNodeCount = await getStorageNodeCount(streamId);
			assert.equal(storageNodeCount, 1);
		});

		it('duplicate', async () => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId, streamOwner);
			assert.equal(response.status, 200)
		});

		it('validation error', async() => {
			const response = await addStorageNodeToStream('foobar', streamId, streamOwner);
			assert.equal(response.status, 422)
		});

		it('unauthorized', async() => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId, undefined);
			assert.equal(response.status, 401)
		});

		it('forbidden', async() => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId, otherUser);
			assert.equal(response.status, 403)
		});
	});

	describe('DELETE /streams/:streamId/storageNodes/:address', () => {
		let storageNodeAddress: string;
		let streamId: string;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream(streamOwner);
			await addStorageNodeToStream(storageNodeAddress, streamId, streamOwner);
		});

		it('happy path', async () => {
			const response = await removeStorageNodeFromStream(storageNodeAddress, streamId, streamOwner);
			assert.equal(response.status, 204);
			const storageNodeCount = await getStorageNodeCount(streamId);
			assert.equal(storageNodeCount, 0);
		});

		it('not found', async () => {
			const nonExistingStorageNodeAddress = createMockEthereumAddress();
			const response = await removeStorageNodeFromStream(nonExistingStorageNodeAddress, streamId, streamOwner);
			assert.equal(response.status, 404);
		});

		it('unauthorized', async() => {
			const response = await removeStorageNodeFromStream(storageNodeAddress, streamId, undefined);
			assert.equal(response.status, 401)
		});

		it('forbidden', async() => {
			const response = await removeStorageNodeFromStream(storageNodeAddress, streamId, otherUser);
			assert.equal(response.status, 403)
		});

		it('malformed storage node address', async () => {
			const response = await removeStorageNodeFromStream('foobar', streamId, streamOwner);
			assert.equal(response.status, 422);
		});
	});
});
