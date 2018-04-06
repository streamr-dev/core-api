import assert from 'assert-diff'
import {parseError} from '../../../actions/utils/parseApiResponse'

const extendedError = (message, extendWith) => {
    const e = new Error(message)
    Object.keys(extendWith).forEach(key => {
        e[key] = extendWith[key]
    })
    return e
}

describe('parseApiResponse', () => {
    describe('parseError', () => {
        it('must first return res.response.data', () => {
            const error = extendedError('', {
                response: {
                    data: {
                        message: 'test',
                        code: 'TEST'
                    },
                    status: 500
                }
            })
            assert.deepStrictEqual(parseError(error), {
                message: 'test',
                code: 'TEST',
                statusCode: 500
            })
        })

        it('must then return error.message', () => {
            const error = new Error('testError')
            assert.deepStrictEqual(parseError(error), {
                message: 'testError',
                code: undefined,
                statusCode: undefined
            })
        })

        it('must return default message if nothing else is found', () => {
            assert.deepStrictEqual(parseError({}), {
                message: 'Something went wrong',
                code: undefined,
                statusCode: undefined
            })
        })
    })
})
