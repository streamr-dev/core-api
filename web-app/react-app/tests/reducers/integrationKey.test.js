import reducer from '../../reducers/integrationKey'
import * as actions from '../../actions/integrationKey'
import assert from 'assert-diff'
import _ from 'lodash'


describe('IntegrationKey reducer', () => {
    
    beforeEach(() => {
        global._ = _
    })
    
    afterEach(() => {
        delete global._
    })
    
    it('should return the initial state', () => {
        assert.deepStrictEqual(reducer(undefined, {}), {
            listsByService: {},
            error: null,
            fetching: false
        })
    })
    
    describe('GET_AND_REPLACE_INTEGRATION_KEYS', () => {
        it('should handle GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST', () => {
            assert.deepStrictEqual(reducer({}, {
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST
            }), {
                fetching: true
            })
        })
        it('should handle GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS', () => {
            assert.deepStrictEqual(reducer({}, {
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
            }), {
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
        })
        it('should handle GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE', () => {
            assert.deepStrictEqual(reducer({
                list: ['test']
            }, {
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE,
                error: 'test-error'
            }), {
                fetching: false,
                list: ['test'],
                error: 'test-error'
            })
        })
    })
    describe('GET_INTEGRATION_KEYS_BY_SERVICE', () => {
        it('should handle GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST', () => {
            assert.deepStrictEqual(reducer({}, {
                type: actions.GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST
            }), {
                fetching: true
            })
        })
        it('should handle GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS', () => {
            assert.deepStrictEqual(reducer({}, {
                type: actions.GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS,
                service: 'B',
                integrationKeys: [{
                    id: 1
                }, {
                    id: 2
                }, {
                    id: 3
                }]
            }), {
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
        })
        it('should handle GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE', () => {
            assert.deepStrictEqual(reducer({
                list: ['test']
            }, {
                type: actions.GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE,
                error: 'test-error'
            }), {
                fetching: false,
                list: ['test'],
                error: 'test-error'
            })
        })
    })
    
    describe('CREATE_INTEGRATION_KEY', () => {
        it('should handle CREATE_INTEGRATION_KEY_REQUEST', () => {
            assert.deepStrictEqual(reducer({}, {
                type: actions.CREATE_INTEGRATION_KEY_REQUEST
            }), {
                fetching: true
            })
        })
        it('should handle CREATE_INTEGRATION_KEY_SUCCESS', () => {
            assert.deepStrictEqual(reducer({
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
            }), {
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
        })
        it('should handle CREATE_INTEGRATION_KEY_FAILURE', () => {
            assert.deepStrictEqual(reducer({
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
            }), {
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
    })
    
    describe('DELETE_INTEGRATION_KEY', () => {
        it('should handle DELETE_INTEGRATION_KEY_REQUEST', () => {
            assert.deepStrictEqual(reducer({}, {
                type: actions.DELETE_INTEGRATION_KEY_REQUEST
            }), {
                fetching: true
            })
        })
        it('should handle DELETE_INTEGRATION_KEY_SUCCESS', () => {
            assert.deepStrictEqual(reducer({
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
            }), {
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
        })
        it('should handle CREATE_INTEGRATION_KEY_FAILURE', () => {
            assert.deepStrictEqual(reducer({
                list: ['test']
            }, {
                type: actions.CREATE_INTEGRATION_KEY_FAILURE,
                error: 'test-error'
            }), {
                fetching: false,
                list: ['test'],
                error: 'test-error'
            })
        })
    })
})