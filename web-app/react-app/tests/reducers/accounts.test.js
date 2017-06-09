
import reducer from '../../reducers/integrationKey'
import * as actions from '../../actions/integrationKey'
import expect from 'expect'
import _ from 'lodash'


describe('IntegrationKey reducer', () => {
    
    beforeEach(() => {
        global._ = _
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
            listsByService: {
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
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE,
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: 'test-error'
        })
    })
    
    it('should handle GET_AND_REPLACE_INTEGRATION_KEYS', () => {
        expect(
            reducer({}, {
                type: actions.GET_INTEGRATION_KEYS_BY_TYPE_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({}, {
                type: actions.GET_INTEGRATION_KEYS_BY_TYPE_SUCCESS,
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
                type: actions.GET_INTEGRATION_KEYS_BY_TYPE_FAILURE,
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: 'test-error'
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
                        type: 'A'
                    }],
                    B: [{
                        id: 2,
                        type: 'B'
                    }]
                }
            }, {
                type: actions.CREATE_INTEGRATION_KEY_SUCCESS,
                integrationKey: {
                    id: 3,
                    type: 'A'
                }
            })
        ).toEqual({
            fetching: false,
            listsByService: {
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
                error: 'test-error'
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: 'test-error'
        })
    })
})