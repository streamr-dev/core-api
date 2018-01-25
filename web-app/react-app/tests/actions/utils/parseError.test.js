
import expect from 'expect'
import parseError from '../../../actions/utils/parseError'

describe('parseError', () => {
    it ('must first return res.response.data', () => {
        const res = {
            response: {
                data: 'test'
            },
            message: 'wrong!'
        }
        expect(parseError(res)).toEqual('test')
    })
    it ('must must return res.message if no res.response or res.response.data', () => {
        const res = {
            response: {
                moi: 'moi'
            },
            message: 'test'
        }
        const res2 = {
            message: 'test'
        }
        expect(parseError(res)).toEqual('test')
        expect(parseError(res2)).toEqual('test')
    })
    it ('must must return default message if none of these is available', () => {
        const res = {
            fasdfasdf: {
                dfasdf: 'moi'
            },
            asdfasf: 'test'
        }
        expect(parseError(res)).toEqual({
            error: 'Something went wrong'
        })
    })
})