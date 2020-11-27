const assert = require('chai').assert
const fs = require('fs')
const util = require('util');
const Streamr = require('./streamr-api-clients')
const StreamrClient = require('streamr-client');
const getStreamrClient = require('./test-utilities.js').getStreamrClient
const pollCondition = require('./test-utilities.js').pollCondition

describe('Canvas API', function() {

	describe('read from stream, write to another stream', function() {

		this.timeout(60000);

		const user = StreamrClient.generateEthereumAccount();
		const subscriberClient = getStreamrClient(user, { autoConnect: true });
		const publisherClient = getStreamrClient(user, { autoConnect: true });
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

		const startCanvas = async (id) => {
			await Streamr.api.v1.canvases
				.start(id)
				.withAuthenticatedUser(user)
				.call();
			await pollCondition(async () => {
				const response = await Streamr.api.v1.canvases
					.get(id)
					.withAuthenticatedUser(user)
					.execute();
				return (response.state === 'RUNNING');
			});
		};

		const stopCanvas = async () => {
			await Streamr.api.v1.canvases
				.stop(canvasId)
				.withAuthenticatedUser(user)
				.call();
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
				.execute()).id;
			await startCanvas(canvasId);
		});

		const subscribe = (onMessage, onReady) => {
			const subscription = subscriberClient.subscribe({ stream: outputStreamId }, (message) => onMessage(message));
			subscription.once('subscribed', () => onReady())
		};

		const publish = () => {
			const msg = {
				streamField: 'mock-content'
			}
			publisherClient.publish(inputStreamId, msg);
		};

		it('send through canvas', (done) => {
			subscribe(message => {
				assert.equal(message.streamField, 'MOCK-CONTENT');
				done()
			}, () => publish());
		});

		after(async () => {
			await Promise.all([stopCanvas(), publisherClient.disconnect(), subscriberClient.disconnect()]);
		});

	});

});