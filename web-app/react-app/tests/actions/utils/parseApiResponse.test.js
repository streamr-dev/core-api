
import assert from 'assert-diff'
import {parseError} from '../../../actions/utils/parseApiResponse'

describe('parseApiResponse', () => {
    describe('parseError', () => {
        it ('must first return res.response.data', () => {
            const res = {
                response: {
                    data: {
                        error: 'test',
                        code: 'TEST'
                    },
                    status: 500
                }
            }
            assert.deepStrictEqual(parseError(res), {
                message: 'test',
                code: 'TEST',
                statusCode: 500
            })
        })

        it ('must first return res.response.data', () => {
            const res = {
                response: {
                    data: {},
                    status: 500
                }
            }
            assert.deepStrictEqual(parseError(res), {
                message: 'Something went wrong',
                code: undefined,
                statusCode: 500
            })
        })
    })
})
