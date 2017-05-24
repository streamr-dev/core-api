
import reducer from '../../reducers/accounts'
import * as actions from '../../actions/accounts'
import expect from 'expect'

describe('todos reducer', () => {
    
    beforeEach(() => {
        global._ = require('underscore')
    })
    
    afterEach(() => {
        delete global._
    })
    
    it('should return the initial state', () => {
        expect(
            reducer(undefined, {})
        ).toEqual({
            list: [],
            error: null,
            fetching: false
        })
    })
    
    it('should handle GET_ALL_ACCOUNTS', () => {
        expect(
            reducer({}, {
                type: actions.GET_ALL_ACCOUNTS_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({}, {
                type: actions.GET_ALL_ACCOUNTS_SUCCESS,
                accounts: [{
                    id: 1
                }, {
                    id: 2
                }, {
                    id: 3
                }]
            })
        ).toEqual({
            fetching: false,
            list: [{
                id: 1
            }, {
                id: 2
            }, {
                id: 3
            }],
            error: null
        })
        
        expect(
            reducer({
                list: ['test']
            }, {
                type: actions.GET_ALL_ACCOUNTS_FAILURE,
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: 'test-error'
        })
    })
    
    it('should handle CREATE_ACCOUNT', () => {
        expect(
            reducer({}, {
                type: actions.CREATE_ACCOUNT_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({
                list: [{
                    id: 1
                }, {
                    id: 2
                }]
            }, {
                type: actions.CREATE_ACCOUNT_SUCCESS,
                account: {
                    id: 3
                }
            })
        ).toEqual({
            fetching: false,
            list: [{
                id: 1
            }, {
                id: 2
            }, {
                id: 3
            }],
            error: null
        })
        
        expect(
            reducer({
                list: [{
                    id: 1
                }, {
                    id: 2
                }, {
                    id: 3
                }]
            }, {
                type: actions.CREATE_ACCOUNT_FAILURE,
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            list: [{
                id: 1
            }, {
                id: 2
            }, {
                id: 3
            }],
            error: 'test-error'
        })
    })
    
    it('should handle DELETE_ACCOUNT', () => {
        expect(
            reducer({}, {
                type: actions.DELETE_ACCOUNT_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({
                list: [{
                    id: 1
                }, {
                    id: 2
                }, {
                    id: 3
                }]
            }, {
                type: actions.DELETE_ACCOUNT_SUCCESS,
                id: 3
            })
        ).toEqual({
            fetching: false,
            list: [{
                id: 1
            }, {
                id: 2
            }],
            error: null
        })
        
        expect(
            reducer({
                list: ['test']
            }, {
                type: actions.CREATE_ACCOUNT_FAILURE,
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: 'test-error'
        })
    })
})