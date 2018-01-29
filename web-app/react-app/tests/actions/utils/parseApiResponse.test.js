
import assert from 'assert-diff'
import {parseError} from '../../../actions/utils/parseApiResponse'

describe('parseApiResponse', () => {
    describe('parseError', () => {
        it('must return a right kind of object', () => {
            assert.deepStrictEqual(parseError({
                response: {
                    status: 313,
                    data: {
                        message: 'testMessage',
                        code: 'TEST'
                    }
                }
            }), {
                message: 'testMessage',
                code: 'TEST',
                statusCode: 313
            })
        })
        it('must use the default message if no message given', () => {
            assert.deepStrictEqual(parseError({
                response: {
                    status: 313,
                    data: {}
                }
            }), {
                message: 'Something went wrong',
                code: undefined,
                statusCode: 313
            })
        })
    })
})
