const sleep = require('sleep-promise')
const StreamrClient = require('streamr-client')
const assert = require('chai').assert
const fs = require('fs')
const Emitter = require('events')
const Streamr = require('./streamr-api-clients')

const REST_URL = 'http://localhost/api/v1'
const WS_URL = 'ws://localhost/api/v1/ws'
const TIMEOUT = 130 * 1000
const NUM_MESSAGES = 50
const WAIT_TIME = 15000

const pollCondition = async (condition, timeout = TIMEOUT, interval = 100) => {
    let timeElapsed = 0
    let result
    while (!result && timeElapsed < timeout) {
        await sleep(interval)
        timeElapsed += interval
        result = await condition()
    }
    return result
}

async function createStreamrClient(user) {
    const client = new StreamrClient({
        url: WS_URL,
        restUrl: REST_URL,
        auth: {
            privateKey: user.privateKey,
        },
    })
    await client.connect()
    return client
}

const freshUser = StreamrClient.generateEthereumAccount()

// The tests should be run sequentially
describe('Canvas API', function() {

    let streamrClient
    let stream
	let canvas

    // sets timeout on before and all test cases in this suite
    this.timeout(TIMEOUT)

    before(async () => {
        streamrClient = await createStreamrClient(freshUser)

        // Create a unique stream for this test
        stream = await streamrClient.createStream({
            name: `canvas-api.test.js-${Date.now()}`,
            config: {
                fields: [
                    {
                        name: 'numero',
                        type: 'number',
                    }
                ]
            }
        })
        assert(stream.id != null)

        // Create a unique canvas for this test. Canvas structure is this:
        /*
          Stream------->Multiply------>Sum
          Constant(2)-->
         */
        const canvasTemplate = JSON.parse(fs.readFileSync('./test-data/canvas-api.test.js-canvas.json'))
        canvasTemplate.name = `canvas-api.test.js-${Date.now()}`
        // Configure the newly created stream onto the Stream module
        canvasTemplate.modules[0].params[0].value = stream.id

        const canvasResponse = await Streamr.api.v1.canvases
            .create(canvasTemplate)
            .withAuthenticatedUser(freshUser)
            .call()

        canvas = await canvasResponse.json()
        assert.equal(canvasResponse.status, 200, JSON.stringify(canvas))
    })

    after(async () => {
        if (streamrClient.isConnected()) {
            await streamrClient.disconnect()
        }
    })

    describe('POST /api/v1/canvases/:id/start', () => {

        it('starts the canvas', async () => {
            const response = await Streamr.api.v1.canvases
                .start(canvas.id)
                .withAuthenticatedUser(freshUser)
                .call()

            const json = await response.json()
            assert.equal(response.status, 200, JSON.stringify(json))
            assert.equal(json.state, 'RUNNING')
        })

    })

    describe('Canvases receive data', () => {
        let messages = []
        let subscription

        before((done) => {
            const table = canvas.modules.find(({ name }) => name === 'Table')
            subscription = streamrClient.subscribe({
                stream: table.uiChannel.id,
            }, (msg) => {
                messages.push(msg)
            })
            subscription.once('error', (error) => {
                throw error
            })
            subscription.once('subscribed', () => done())
        })

        after((done) => {
            subscription.once('unsubscribed', done)
            streamrClient.unsubscribe(subscription)
        })

        before('Produce data to stream', async () => {
            // Allow time for canvas to start properly. If values don't make it to the canvas, this may be the reason.
            await sleep(WAIT_TIME)

            for (let i = 1; i <= NUM_MESSAGES; i++) {
                await streamrClient.publish(stream.id, {
                    numero: i,
                })
            }
            // Wait for data to land in storage
            await sleep(WAIT_TIME)
        })

        it('received messages on uiChannel', async () => {
            // should have NUM_MESSAGES new row messages
            assert.equal(messages.filter(({ nr }) => nr).length, NUM_MESSAGES)
        })

        describe('POST /api/v1/canvases/:canvasId/modules/:moduleId/request', () => {
            it('Shows correct output values on the Stream module', async () => {
                let response
                let json
                await pollCondition(async () => {
                    response = await Streamr.api.v1.canvases
                        .getRuntimeState(canvas.id, 'modules/0')
                        .withAuthenticatedUser(freshUser)
                        .call()

                    json = await response.json()
                    return json.json.outputs[0].value === NUM_MESSAGES
                })

                assert.equal(response.status, 200, JSON.stringify(json))
                assert.equal(json.json.name, 'Stream', 'Unexpected name on module!')
                assert.equal(json.json.outputs[0].name, 'numero', 'Unexpected name on output!')
                assert.equal(json.json.outputs[0].value, NUM_MESSAGES, 'Stream module did not output the correct values')
                assert(json.success)
            })

            it('Shows correct state on the Sum module', async () => {
                let response
                let json
                await pollCondition(async () => {
                    response = await Streamr.api.v1.canvases
                        .getRuntimeState(canvas.id, 'modules/1')
                        .withAuthenticatedUser(freshUser)
                        .call()

                    json = await response.json()
                    return json.json.inputs[0].value === NUM_MESSAGES * 2
                })

                assert.equal(response.status, 200, JSON.stringify(json))
                assert.equal(json.json.name, 'Sum', 'Unexpected name on module!')
                assert.equal(json.json.inputs[0].name, 'in', 'Unexpected name on input!')
                assert.equal(json.json.inputs[0].value, NUM_MESSAGES * 2, 'Sum module did not receive the correct values!')
                // sum(1:NUM_MESSAGES) * 2
                assert.equal(json.json.outputs[0].value, NUM_MESSAGES * (NUM_MESSAGES + 1), 'Sum module did not output the correct value!')
                assert(json.success)
            })
        })

    })

    describe('POST /api/v1/canvases/:id/stop', () => {
        it('stops the canvas', async () => {
            const response = await Streamr.api.v1.canvases
                .stop(canvas.id)
                .withAuthenticatedUser(freshUser)
                .call()

            const json = await response.json()
            assert.equal(response.status, 200, JSON.stringify(json))
            assert.equal(json.state, 'STOPPED')
        })
    })

    function TestRestartingCanvas() {
        const messages = []
        let resentMessages
        let subscription
        const messageEmitter = new Emitter()

        before('cycle start/stop, subscribe, start', async () => {
            let done
            const p = new Promise((resolve, reject) => done = (err) => err ? reject(err) : resolve())
            const r1 = await Streamr.api.v1.canvases
                .start(canvas.id)
                .withAuthenticatedUser(freshUser)
                .call()
            assert.equal(r1.status, 200, 'Canvas start failed')
            const r2 = await Streamr.api.v1.canvases
                .stop(canvas.id)
                .withAuthenticatedUser(freshUser)
                .call()
            assert.equal(r2.status, 200, 'Canvas stop failed')

            const table = canvas.modules.find(({ name }) => name === 'Table')
            subscription = streamrClient.subscribe({
                stream: table.uiChannel.id,
                resend: {
                    last: table.options.uiResendLast.value,
                },
            }, (msg) => {
                messages.push(msg)
                messageEmitter.emit('message', msg)
            })

            subscription.once('resent', () => {
                resentMessages = messages.slice()
            })

            subscription.once('error', (error) => {
                throw error
            })

            subscription.once('no_resend', () => {
                throw new Error('should not have no_resend')
            })

            // wait for subscription before starting again
            subscription.once('subscribed', async () => {
                // restart for second time
                const r3 = await Streamr.api.v1.canvases
                    .start(canvas.id)
                    .withAuthenticatedUser(freshUser)
                    .call()
                assert.equal(r3.status, 200, 'Canvas restart failed')

                // reduce flakiness by allowing the subscriptions of the canvas some time to get set up
                await sleep(5000)
                done()
            })
            return p
        })

        afterEach(() => {
            messageEmitter.removeAllListeners('message')
        })

        after((done) => {
            subscription.once('unsubscribed', done)
            streamrClient.unsubscribe(subscription)
        })

        after(async () => {
            await Streamr.api.v1.canvases
                .stop(canvas.id)
                .withAuthenticatedUser(freshUser)
                .call()
        })

        it('gets uiResendLast resent messages', (done) => {
            const table = canvas.modules.find(({ name }) => name === 'Table')
            if (!resentMessages) {
                // wait for resent if no resent event yet
                subscription.once('resent', () => {
                    assert.equal(resentMessages && resentMessages.length, table.options.uiResendLast.value,
                        `Resent messages (waited): ${resentMessages.map((msg) => JSON.stringify(msg))}`)
                    done()
                })
                return
            }

            assert.equal(resentMessages && resentMessages.length, table.options.uiResendLast.value,
                `Resent messages (didn't wait): ${resentMessages.map((msg) => JSON.stringify(msg))}`)
            done()
        })

        it('can get new messages', (done) => {
            const expected = `${NUM_MESSAGES + 1}.0`

            const onMessage = (msg) => {
                // check for message we just published
                if (msg && msg.nr && msg.nr[1] === expected) {
                    // success, got message before timeout
                    messageEmitter.removeListener('message', onMessage) // clean up
                    done()
                    return
                } else {
                    // Ignore other messages or log them for debugging
                    // console.log(`Got other message: ${JSON.stringify(msg)}`)
                }
            }
            messageEmitter.on('message', onMessage)

            streamrClient.publish(stream.id, {
                numero: NUM_MESSAGES + 1,
            })
        })
    }

    describe('restarting canvas', () => {
        describe('1st time', TestRestartingCanvas)
        describe('2nd time', TestRestartingCanvas)
        describe('3rd time', TestRestartingCanvas)
        describe('4th time', TestRestartingCanvas)
        describe('5th time', TestRestartingCanvas)
    })
})


function TestClockTable() {
    let streamrClient
    let canvas
    let subscription

    // sets timeout on before and all test cases in this suite
    this.timeout(80000)

    before(async () => {
        streamrClient = await createStreamrClient(freshUser)
    })

    before(async () => {
        const canvasTemplate = JSON.parse(fs.readFileSync('./test-data/canvas-api.test.js-canvas-clock.json'))
        canvasTemplate.name = `canvas-api.test.js-${Date.now()}`

        const canvasResponse = await Streamr.api.v1.canvases
            .create(canvasTemplate)
            .withAuthenticatedUser(freshUser)
            .call()

        canvas = await canvasResponse.json()
        assert.equal(canvasResponse.status, 200)
    })

    it('starts sending uiChannel messages', (done) => {
        const table = canvas.modules.find(({ name }) => name === 'Table')
        const messages = []
        subscription = streamrClient.subscribe({
            stream: table.uiChannel.id,
        }, (msg) => {
            messages.push(msg)
            if (msg.nr) {
                // end after first new row message
                streamrClient.unsubscribe(subscription)
            }
        })

        subscription.once('unsubscribed', () => {
            assert.ok(messages.filter(({ nr }) => nr).length, 'got at least one new row message')
            done()
        })

        subscription.once('error', (error) => {
            throw error
        })

        Streamr.api.v1.canvases
            .start(canvas.id)
            .withAuthenticatedUser(freshUser)
            .call()
            .then((r1) => {
                assert.equal(r1.status, 200)
            })
    })

    after(async () => {
        if (canvas) {
            await Streamr.api.v1.canvases
                .stop(canvas.id)
                .withAuthenticatedUser(freshUser)
                .call()
                .catch(console.warn) // ignore
        }
        if (streamrClient && streamrClient.isConnected()) {
            await streamrClient.disconnect()
        }
    })
}

describe('clock -> table canvas', () => {
    // repeat test a number of times since it seems to fail intermittently
    describe('1st time', TestClockTable)
    describe('2nd time', TestClockTable)
    describe('3rd time', TestClockTable)
    describe('4th time', TestClockTable)
    describe('5th time', TestClockTable)
})
