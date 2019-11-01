const sleep = require('sleep-promise')
const StreamrClient = require('streamr-client')
const assert = require('chai').assert
const fs = require('fs')
const initStreamrApi = require('./streamr-api-clients')

const REST_URL = 'http://localhost:8081/streamr-core/api/v1'
const WS_URL = 'ws://localhost:8890/api/v1/ws'
const LOGGING_ENABLED = false

const Streamr = initStreamrApi(REST_URL, LOGGING_ENABLED)

const TIMEOUT = 30 * 1000

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

// The tests should be run sequentially
describe('Canvas API', function() {

    let streamrClient
    let sessionToken
    let stream
    let canvas

    // sets timeout on before and all test cases in this suite
    this.timeout(TIMEOUT)

    before(async () => {
        // Generate a new user to isolate the test and not require any pre-existing resources
        const freshUser = StreamrClient.generateEthereumAccount()

        // Print private key to console in case you need to debug by logging in as this user (import to MetaMask, log in with Ethereum)
        console.log(`User created: ${JSON.stringify(freshUser)}`)

        streamrClient = new StreamrClient({
            url: WS_URL,
            restUrl: REST_URL,
            auth: {
                privateKey: freshUser.privateKey,
            },
        })
        await streamrClient.connect()

        sessionToken = await streamrClient.session.getSessionToken()

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
            .withSessionToken(sessionToken)
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
                .withSessionToken(sessionToken)
                .call()

            const json = await response.json()
            assert.equal(response.status, 200, JSON.stringify(json))
            assert.equal(json.state, 'RUNNING')
        })

    })

    describe('Canvases receive data', () => {

        before('Produce data to stream', async () => {
            // Allow time for canvas to start properly. If values don't make it to the canvas, this may be the reason.
            await sleep(15 * 1000)

            for (let i=1; i<=100; i++) {
                await streamrClient.publish(stream.id, {
                    numero: i,
                })
            }
        })

        describe('POST /api/v1/canvases/:canvasId/modules/:moduleId/request', () => {

            it('Shows correct output values on the Stream module', async () => {
                let response
                let json
                await pollCondition(async () => {
                    response = await Streamr.api.v1.canvases
                        .getRuntimeState(canvas.id, 'modules/0')
                        .withSessionToken(sessionToken)
                        .call()

                    json = await response.json()
                    console.log(`json.json.outputs[0].value === ${json.json.outputs[0].value}`)
                    return json.json.outputs[0].value === 100
                })

                assert.equal(response.status, 200, JSON.stringify(json))
                assert.equal(json.json.name, 'Stream', 'Unexpected name on module!')
                assert.equal(json.json.outputs[0].name, 'numero', 'Unexpected name on output!')
                assert.equal(json.json.outputs[0].value, 100, 'Stream module did not output the correct values')
                assert(json.success)
            })

            it('Shows correct state on the Sum module', async () => {
                let response
                let json
                await pollCondition(async () => {
                    response = await Streamr.api.v1.canvases
                        .getRuntimeState(canvas.id, 'modules/1')
                        .withSessionToken(sessionToken)
                        .call()

                    json = await response.json()
                    console.log(`json.json.outputs[0].value === ${json.json.outputs[0].value}`)
                    return json.json.outputs[0].value === 10100 // sum(1:100) * 2
                })

                assert.equal(response.status, 200, JSON.stringify(json))
                assert.equal(json.json.name, 'Sum', 'Unexpected name on module!')
                assert.equal(json.json.inputs[0].name, 'in', 'Unexpected name on input!')
                assert.equal(json.json.inputs[0].value, 200, 'Sum module did not receive the correct values!')
                assert.equal(json.json.outputs[0].value, 10100, 'Sum module did not output the correct value!')
                assert(json.success)
            })
        })

    })

    describe('POST /api/v1/canvases/:id/stop', () => {

        it('stops the canvas', async () => {
            const response = await Streamr.api.v1.canvases
                .stop(canvas.id)
                .withSessionToken(sessionToken)
                .call()

            const json = await response.json()
            assert.equal(response.status, 200, JSON.stringify(json))
            assert.equal(json.state, 'STOPPED')
        })

    })

})
