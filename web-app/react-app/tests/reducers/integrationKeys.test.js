
import reducer from '../../reducers/integrationKeys'
import * as actions from '../../actions/integrationKeys'
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
            listsByService: {},
            error: null,
            fetching: false
        })
    })
    
    it('should handle GET_AND_REPLACE_INTEGRATION_KEYS', () => {
        expect(
            reducer({}, {
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({}, {
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS,
                integrationKeys: [{
                    id: 1,
                    service: 'A'
                }, {
                    id: 2,
                    service: 'B'
                }, {
                    id: 3,
                    service: 'B'
                }]
            })
        ).toEqual({
            fetching: false,
            listsByService: {
                A: [{
                    id: 1,
                    service: 'A'
                }],
                B: [{
                    id: 2,
                    service: 'B'
                }, {
                    id: 3,
                    service: 'B'
                }]
            },
            error: null
        })
        
        expect(
            reducer({
                list: ['test']
            }, {
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE,
                error: new Error('test-error')
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: new Error('test-error')
        })
    })
    
    it('should handle GET_AND_REPLACE_INTEGRATION_KEYS', () => {
        expect(
            reducer({}, {
                type: actions.GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST
            })
        ).toEqual({
            fetching: true
        })
    
        expect(
            reducer({}, {
                type: actions.GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS,
                service: 'B',
                integrationKeys: [{
                    id: 1
                }, {
                    id: 2
                }, {
                    id: 3
                }]
            })
        ).toEqual({
            fetching: false,
            listsByService: {
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
                type: actions.GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE,
                error: new Error('test-error')
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: new Error('test-error')
        })
    })
    
    it('should handle CREATE_INTEGRATION_KEY', () => {
        expect(
            reducer({}, {
                type: actions.CREATE_INTEGRATION_KEY_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({
                listsByService: {
                    A: [{
                        id: 1,
                        service: 'A'
                    }],
                    B: [{
                        id: 2,
                        service: 'B'
                    }]
                }
            }, {
                type: actions.CREATE_INTEGRATION_KEY_SUCCESS,
                integrationKey: {
                    id: 3,
                    service: 'A'
                }
            })
        ).toEqual({
            fetching: false,
            listsByService: {
                A: [{
                    id: 1,
                    service: 'A'
                }, {
                    id: 3,
                    service: 'A'
                }],
                B: [{
                    id: 2,
                    service: 'B'
                }]
            },
            error: null
        })
        
        expect(
            reducer({
                listsByService: {
                    A: [{
                        id: 1
                    }, {
                        id: 2
                    }, {
                        id: 3
                    }]
                }
            }, {
                type: actions.CREATE_INTEGRATION_KEY_FAILURE,
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            listsByService: {
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
    
    it('should handle DELETE_INTEGRATION_KEY', () => {
        expect(
            reducer({}, {
                type: actions.DELETE_INTEGRATION_KEY_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({
                listsByService: {
                    A: [{
                        id: 1
                    }, {
                        id: 2
                    }, {
                        id: 3
                    }]
                }
            }, {
                type: actions.DELETE_INTEGRATION_KEY_SUCCESS,
                id: 3
            })
        ).toEqual({
            fetching: false,
            listsByService: {
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
                type: actions.CREATE_INTEGRATION_KEY_FAILURE,
                error: new Error('test-error')
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: new Error('test-error')
        })
    })
})