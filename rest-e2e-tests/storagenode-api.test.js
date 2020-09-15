const assert = require("chai").assert;
const initStreamrApi = require("./streamr-api-clients");
const _ = require("lodash");

const URL = "http://localhost:8081/streamr-core/api/v1/";
const API_KEY = "product-api-tester-key";
const API_KEY_OTHER_USER = 'product-api-tester2-key'
const LOGGING_ENABLED = false;
const Streamr = initStreamrApi(URL, LOGGING_ENABLED);

const createMockEthereumAddress = () => {
	const LENGTH = 40;
	return "0x" + _.padStart(Math.floor(Math.random() * Number.MAX_SAFE_INTEGER).toString(16),LENGTH, "0");
};
const createStream = () => {
	return Streamr.api.v1.streams
		.create()
		.withApiKey(API_KEY)
		.execute()
		.then((json) => json.id);
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
const addStorageNodeToStream = (storageNodeAddress, streamId, apiKey = API_KEY) => {
	return Streamr.api.v1.storagenodes
		.addStorageNodeToStream(storageNodeAddress, streamId)
		.withApiKey(apiKey)
		.call();
};
const removeStorageNodeFromStream = (storageNodeAddress, streamId, apiKey = API_KEY) => {
	return Streamr.api.v1.storagenodes
		.removeStorageNodeFromStream(storageNodeAddress, streamId)
		.withApiKey(apiKey)
		.call();
};
const getStorageNodeCount = async (streamId) => {
	const response = await findStorageNodesByStream(streamId);
	const json = await response.json()
	return json.length;
};

describe("Storage Node API", () => {
	describe("GET /storageNodes/:address/stream", () => {
		let storageNodeAddress;
		let streamId;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream();
			await addStorageNodeToStream(storageNodeAddress, streamId);
		});

		it("happy path", async () => {
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

	describe("GET /streams/:streamId/storageNodes", () => {
		let storageNodeAddress;
		let streamId;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream();
			await addStorageNodeToStream(storageNodeAddress, streamId);
		});

		it("happy path", async () => {
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

	describe("POST /streams/:streamId/storageNodes", () => {
		let storageNodeAddress;
		let streamId;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream();
		});

		it("happy path", async () => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId);
			assert.equal(response.status, 200)
			const json = await response.json()
			assert.equal(json.storageNodeAddress, storageNodeAddress);
			const storageNodeCount = await getStorageNodeCount(streamId);
			assert.equal(storageNodeCount, 1);
		});

		it('duplicate', async () => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId);
			assert.equal(response.status, 400)
		});

		it('validation error', async() => {
			const response = await addStorageNodeToStream('foobar', streamId);
			assert.equal(response.status, 422)
		});

		it('unauthorized', async() => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId, null);
			assert.equal(response.status, 401)
		});

		it('forbidden', async() => {
			const response = await addStorageNodeToStream(storageNodeAddress, streamId, API_KEY_OTHER_USER);
			assert.equal(response.status, 403)
		});
	});

	describe("DELETE /streams/:streamId/storageNodes/:address", () => {
		let storageNodeAddress;
		let streamId;

		before(async () => {
			storageNodeAddress = createMockEthereumAddress();
			streamId = await createStream();
			await addStorageNodeToStream(storageNodeAddress, streamId);
		});

		it("happy path", async () => {
			const response = await removeStorageNodeFromStream(storageNodeAddress, streamId);
			assert.equal(response.status, 204);
			const storageNodeCount = await getStorageNodeCount(streamId);
			assert.equal(storageNodeCount, 0);
		});

		it('not found', async () => {
			const nonExistingStorageNodeAddress = createMockEthereumAddress();
			const response = await removeStorageNodeFromStream(nonExistingStorageNodeAddress, streamId);
			assert.equal(response.status, 404);
		});

		it('unauthorized', async() => {
			const response = await removeStorageNodeFromStream(storageNodeAddress, streamId, null);
			assert.equal(response.status, 401)
		});

		it('forbidden', async() => {
			const response = await removeStorageNodeFromStream(storageNodeAddress, streamId, API_KEY_OTHER_USER);
			assert.equal(response.status, 403)
		});

		it('malformed storage node address', async () => {
			const response = await removeStorageNodeFromStream('foobar', streamId);
			assert.equal(response.status, 400);
		});
	});
});
