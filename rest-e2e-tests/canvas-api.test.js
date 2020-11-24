const assert = require('chai').assert
const fs = require('fs')
const sleep = require('sleep-promise')
const Streamr = require('./streamr-api-clients')
const StreamrClient = require('streamr-client')
const getStreamrClient = require('./test-utilities.js').getStreamrClient

describe('Canvas API', function() {

	describe('read from stream, write to another stream', function() {

		this.timeout(30000);

		const user = StreamrClient.generateEthereumAccount();
		let inputStreamId;
		let outputStreamId;
		let canvasId;

		const createStream = async (name) => {
			const response = await getStreamrClient(user).createStream({
				name,
				config: {
					fields: [{
						'name': 'streamField',
						'type': 'string'
					}]
				}
			})
			return response.id
		};

		before(async () => {
			const inputStreamName = 'inputStream-' + Date.now();
			const outputStreamName = 'outputStream-' + Date.now();
			inputStreamId = await createStream(inputStreamName);
			outputStreamId = await createStream(outputStreamName);
			const canvasJson = fs.readFileSync('./test-data/canvas-api.test.js-canvas-encrypted-data.json', "utf8")
				.replace('CANVAS_NAME', `canvas-api.test.js-${Date.now()}`)
				.replace('INPUT_STREAM_ID', inputStreamId)
				.replace('INPUT_STREAM_NAME', inputStreamName)
				.replace('OUTPUT_STREAM_ID', outputStreamId)
				.replace('OUTPUT_STREAM_NAME', outputStreamName);
			canvasId = (await Streamr.api.v1.canvases
				.create(JSON.parse(canvasJson))
				.withAuthenticatedUser(user)
				.execute()).id
			await Streamr.api.v1.canvases
				.start(canvasId)
				.withAuthenticatedUser(user)
				.call();
			await sleep(5000);  // some time that allow canvas to start
		});

		const sendMessageToInputStream = async () => {
			const msg = {
				streamField: 'mock-content'
			}
			const client = getStreamrClient(user);
			await client.connect();
			client.publish(inputStreamId, msg);
		};

		const waitForMessage = async (done) => {
			const client = getStreamrClient(user);
			await client.connect();
			client.subscribe({ stream: outputStreamId }, (message) => {
				assert.equal(message.streamField, 'MOCK-CONTENT');
				done();
			});
		};

		it('send through canvas', (done) => {
			waitForMessage(done)
			sendMessageToInputStream();
		});

		after(async () => {
			await Streamr.api.v1.canvases
				.stop(canvasId)
				.withAuthenticatedUser(user)
				.call();
		});

	});

});