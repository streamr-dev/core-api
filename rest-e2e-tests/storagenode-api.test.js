const assert = require('chai').assert
const Streamr = require('./streamr-api-clients')
const _ = require('lodash');
const StreamrClient = require('streamr-client')
const getStreamrClient = require('./test-utilities.js').getStreamrClient

const createMockEthereumAddress = () => {
	const LENGTH = 40;
	return '0x' + _.padStart(Math.floor(Math.random() * Number.MAX_SAFE_INTEGER).toString(16),LENGTH, '0');
};
const createStream = async (user) => {
	const stream = await getStreamrClient(user).createStream();
	return stream.id;
};
const findStreamsByStorageNode = (storageNodeAddress) => {
	return Streamr.api.v1.storagenodes
		.findStreamsByStorageNode(storageNodeAddress)
		.call();
};
const findStorageNodesByStream = (streamId) => {
	return Streamr.api.v1.storagenodes
		.findStorageNodesByStream(streamId)
		.call();
};
const addStorageNodeToStream = async (storageNodeAddress, streamId, user) => {
	return Streamr.api.v1.storagenodes
		.addStorageNodeToStream(storageNodeAddress, streamId)
		.withAuthenticatedUser(user)
		.call();
};
const removeStorageNodeFromStream = async (storageNodeAddress, streamId, user) => {
	return Streamr.api.v1.storagenodes
		.removeStorageNodeFromStream(storageNodeAddress, streamId)
		.withAuthenticatedUser(user)
		.call();
};
const getStorageNodeCount = async (streamId) => {
	const response = await findStorageNodesByStream(streamId);
	const json = await response.json()
	return json.length;
};

describe('Storage Node API', () => {

	const streamOwner = StreamrClient.generateEthereumAccount()
	const otherUser = StreamrClient.generateEthereumAccount()

	describe('GET /storageNodes/:address/stream', () => {
		let storageNodeAddress;
		let streamId;

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

		it('no storage nodes', async () => {
			const nonExistingStorageNodeAddress = createMockEthereumAddress();
			const response = await findStreamsByStorageNode(nonExistingStorageNodeAddress);
			const json = await response.json()
			assert.equal(json.length, 0);
		});

		it('malformed storage node address', async () => {
			const response = await findStreamsByStorageNode('foobar');
			assert.equal(response.status, 400);
		});
	});

	describe('GET /streams/:streamId/storageNodes', () => {
		let storageNodeAddress;
		let streamId;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream(streamOwner);
			await addStorageNodeToStream(storageNodeAddress, streamId, streamOwner);
		});

		it('happy path', async () => {
			const response = await findStorageNodesByStream(streamId);
			const json = await response.json();
			assert.equal(json.length, 1);
			assert.equal(json[0].storageNodeAddress, storageNodeAddress);
		});

		it('stream not found', async () => {
			const response = await findStorageNodesByStream('non-existing');
			assert.equal(response.status, 404);
		});
	});

	describe('POST /streams/:streamId/storageNodes', () => {
		let storageNodeAddress;
		let streamId;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream(streamOwner);
		});

		it('happy path', async () => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId, streamOwner);
			assert.equal(response.status, 200)
			const json = await response.json()
			assert.equal(json.storageNodeAddress, storageNodeAddress);
			const storageNodeCount = await getStorageNodeCount(streamId);
			assert.equal(storageNodeCount, 1);
		});

		it('duplicate', async () => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId, streamOwner);
			assert.equal(response.status, 400)
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
		let storageNodeAddress;
		let streamId;

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
			assert.equal(response.status, 400);
		});
	});
});
