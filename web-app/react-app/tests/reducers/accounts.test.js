
import reducer from '../../reducers/accounts'
import * as actions from '../../actions/accounts'
import expect from 'expect'

global._ = require('lodash')

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
            listsByType: {},
            error: null,
            fetching: false
        })
    })
    
    it('should handle GET_AND_REPLACE_ACCOUNTS', () => {
        expect(
            reducer({}, {
                type: actions.GET_AND_REPLACE_ACCOUNTS_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({}, {
                type: actions.GET_AND_REPLACE_ACCOUNTS_SUCCESS,
                accounts: [{
                    id: 1,
                    type: 'A'
                }, {
                    id: 2,
                    type: 'B'
                }, {
                    id: 3,
                    type: 'B'
                }]
            })
        ).toEqual({
            fetching: false,
            listsByType: {
                A: [{
                    id: 1,
                    type: 'A'
                }],
                B: [{
                    id: 2,
                    type: 'B'
                }, {
                    id: 3,
                    type: 'B'
                }]
            },
            error: null
        })
        
        expect(
            reducer({
                list: ['test']
            }, {
                type: actions.GET_AND_REPLACE_ACCOUNTS_FAILURE,
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: 'test-error'
        })
    })
    
    it('should handle GET_AND_REPLACE_ACCOUNTS', () => {
        expect(
            reducer({}, {
                type: actions.GET_ACCOUNTS_BY_TYPE_REQUEST
            })
        ).toEqual({
            fetching: true
        })
    
        expect(
            reducer({}, {
                type: actions.GET_ACCOUNTS_BY_TYPE_SUCCESS,
                accountType: 'B',
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
            listsByType: {
                B: [{
                    id: 1
                }, {
                    id: 2
                }, {
                    id: 3
                }],
            },
            error: null
        })
        
        expect(
            reducer({
                list: ['test']
            }, {
                type: actions.GET_ACCOUNTS_BY_TYPE_FAILURE,
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
                listsByType: {
                    A: [{
                        id: 1,
                        type: 'A'
                    }],
                    B: [{
                        id: 2,
                        type: 'B'
                    }]
                }
            }, {
                type: actions.CREATE_ACCOUNT_SUCCESS,
                account: {
                    id: 3,
                    type: 'A'
                }
            })
        ).toEqual({
            fetching: false,
            listsByType: {
                A: [{
                    id: 1,
                    type: 'A'
                }, {
                    id: 3,
                    type: 'A'
                }],
                B: [{
                    id: 2,
                    type: 'B'
                }]
            },
            error: null
        })
        
        expect(
            reducer({
                listsByType: {
                    A: [{
                        id: 1
                    }, {
                        id: 2
                    }, {
                        id: 3
                    }]
                }
            }, {
                type: actions.CREATE_ACCOUNT_FAILURE,
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            listsByType: {
                A: [{
                    id: 1
                }, {
                    id: 2
                }, {
                    id: 3
                }]
            },
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
                listsByType: {
                    A: [{
                        id: 1
                    }, {
                        id: 2
                    }, {
                        id: 3
                    }]
                }
            }, {
                type: actions.DELETE_ACCOUNT_SUCCESS,
                id: 3
            })
        ).toEqual({
            fetching: false,
            listsByType: {
                A: [{
                    id: 1
                }, {
                    id: 2
                }]
            },
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