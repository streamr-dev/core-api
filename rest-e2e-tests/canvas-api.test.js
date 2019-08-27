const sleep = require('sleep-promise')
const StreamrClient = require('streamr-client')
const assert = require('chai').assert
const fs = require('fs')
const initStreamrApi = require('./streamr-api-clients')
const SchemaValidator = require('./schema-validator')
const assertResponseIsError = require('./test-utilities.js').assertResponseIsError

const REST_URL = 'http://localhost:8081/streamr-core/api/v1'
const WS_URL = 'ws://localhost:8890/api/v1/ws'
const LOGGING_ENABLED = false

const API_KEY = 'tester1-api-key'

const Streamr = initStreamrApi(REST_URL, LOGGING_ENABLED)

const pollCondition = async (condition, timeout = 10*1000, interval = 100) => {
    let timeElapsed = 0
    let result
    while (!result && timeElapsed < timeout) {
        await sleep(interval)
        timeElapsed += interval
        result = await condition()
    }
    return result
}

// Depends on a pre-existing canvas and stream
// The tests should be run sequentially
describe('Canvas API', () => {

    let streamrClient

    before(() => {
        streamrClient = new StreamrClient({
            url: WS_URL,
            restUrl: REST_URL,
            auth: {
                apiKey: API_KEY,
            }
        })
    })

    after(() => {
        streamrClient.disconnect()
    })

    describe('POST /api/v1/canvases/:id/start', () => {

        it('starts the canvas', async () => {
            const response = await Streamr.api.v1.canvases
                .start('run-canvas-spec')
                .withAuthToken(API_KEY)
                .call()

            const json = await response.json()
            assert.equal(response.status, 200, JSON.stringify(json))
            assert.equal(json.state, 'RUNNING')
        })

    })

    describe('Canvases receive data', function() {
        // sets timeout on before and all test cases in this suite
        this.timeout(10 * 1000)

        before('Produce data to stream', async () => {
            await sleep(5000) // Allow time for canvas to start properly

            const promises = []
            for (let i=1; i<=100; i++) {
                promises.push(streamrClient.publish('run-canvas-spec', {
                    numero: i,
                    areWeDoneYet: false,
                }))
            }
            await Promise.all(promises)
        })

        describe('POST /api/v1/canvases/:canvasId/modules/:moduleId/request', () => {

            it('Shows correct output values on the Stream module', async () => {
                let response
                let json
                await pollCondition(async () => {
                    response = await Streamr.api.v1.canvases
                        .getRuntimeState('run-canvas-spec', 'modules/0')
                        .withAuthToken(API_KEY)
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
                        .getRuntimeState('run-canvas-spec', 'modules/1')
                        .withAuthToken(API_KEY)
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
                .stop('run-canvas-spec')
                .withAuthToken(API_KEY)
                .call()

            const json = await response.json()
            assert.equal(response.status, 200, JSON.stringify(json))
            assert.equal(json.state, 'STOPPED')
        })

    })

})
