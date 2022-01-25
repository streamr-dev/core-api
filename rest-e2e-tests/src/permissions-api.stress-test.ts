import {assert} from 'chai'
import Streamr from './streamr-api-clients'
import {Stream, StreamrClient} from 'streamr-client'
import {getStreamrClient} from './test-utilities'

describe('POST /api/v2/streams/{id}/permissions', function () {

    this.timeout(200 * 1000)

    let stream: Stream
    const me = StreamrClient.generateEthereumAccount()

    before(async () => {
        stream = await getStreamrClient(me).createStream({
            id: `/permissions-api.test.js-${Date.now()}`,
            name: `permissions-api.test.js-${Date.now()}`
        })
    })

    describe('race conditions', () => {
        // The worst case is that there are parallel requests open for all the different operations
        const operations = [
            'product_get',
            'product_edit',
            'product_subscribe',
            'product_publish',
            'product_delete',
            'product_share',
        ]

        // Tests here are repeated 50 times, as they have some chance of an individual attempt
        // succeeding even if the race condition is not handled properly
        const ITERATIONS = 50

        const assertSuccessResponses = (responses: Response[]) => {
            const httpStatuses = responses.map((r) => r.status)
            const fails = httpStatuses.filter(status => status !== 200)
            if (fails.length > 0) {
                assert.fail(`Race condition test failed on ${fails.length}/${ITERATIONS} iterations: [${fails}]`)
            }
        }

        it('survives a race condition when granting multiple permissions to a non-existing user using Ethereum address', async () => {
            for (let i = 0; i < ITERATIONS; i++) {
                const responses = await Promise.all(operations.map((operation: string) => {
                    return Streamr.api.v1.streams
                        .grant(stream.id, StreamrClient.generateEthereumAccount().address, operation)
                        .withAuthenticatedUser(me)
                        .call()
                }))
                // All response statuses must be 200
                const httpStatuses = responses.map((r) => r.status)
                const fails = httpStatuses.filter(status => status !== 200)
                if (fails.length > 0) {
                    assert.fail(`Race condition test failed on ${fails.length}/${ITERATIONS} iterations: [${fails}]`)
                }
            }
        })
    })
})
