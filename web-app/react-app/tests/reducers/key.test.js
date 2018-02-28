import reducer from '../../reducers/key'
import * as actions from '../../actions/key'
import assert from 'assert-diff'

describe('Key reducer', () => {
    
    it('should return the initial state', () => {
        assert.deepStrictEqual(reducer(undefined, {}), {
            byTypeAndId: {},
            error: null,
            fetching: false
        })
    })
    
    describe('GET_RESOURCE_KEYS', () => {
        it('should set fetching = true on GET_RESOURCE_KEYS_REQUEST', () => {
            assert.deepStrictEqual(
                reducer({}, {
                    type: actions.GET_RESOURCE_KEYS_REQUEST
                }), {
                    fetching: true
                })
        })
        
        it('should add the key to the resource on GET_RESOURCE_KEYS_SUCCESS', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {}
                }, {
                    type: actions.GET_RESOURCE_KEYS_SUCCESS,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    keys: [{
                        id: 'test'
                    }, {
                        id: 'test2'
                    }]
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }, {
                                id: 'test2'
                            }]
                        }
                    },
                    fetching: false,
                    error: null
                })
        })
        
        it('should handle the error on GET_RESOURCE_KEYS_FAILURE', () => {
            assert.deepStrictEqual(
                reducer({}, {
                    type: actions.GET_RESOURCE_KEYS_FAILURE,
                    error: new Error('test-error')
                }), {
                    fetching: false,
                    error: new Error('test-error')
                })
        })
    })
    
    describe('ADD_RESOURCE_KEY', () => {
        it('should set fetching = true on ADD_RESOURCE_KEY_REQUEST', () => {
            assert.deepStrictEqual(
                reducer({}, {
                    type: actions.ADD_RESOURCE_KEY_REQUEST
                }), {
                    fetching: true
                })
        })
    
        it('should add the key to the resource on ADD_RESOURCE_KEY_SUCCESS if the resource already has keys', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }]
                        }
                    }
                }, {
                    type: actions.ADD_RESOURCE_KEY_SUCCESS,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    key: {
                        id: 'test2'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }, {
                                id: 'test2'
                            }]
                        }
                    },
                    fetching: false,
                    error: null
                })
        })
    
        it('should add the key to the resource on ADD_RESOURCE_KEY_SUCCESS if the resource doesn\'t already has keys', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {}
                }, {
                    type: actions.ADD_RESOURCE_KEY_SUCCESS,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    key: {
                        id: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }]
                        }
                    },
                    fetching: false,
                    error: null
                })
        })
    
        it('should handle the error on ADD_RESOURCE_KEY_FAILURE', () => {
            assert.deepStrictEqual(
                reducer({}, {
                    type: actions.ADD_RESOURCE_KEY_FAILURE,
                    error: new Error('test-error')
                }), {
                    fetching: false,
                    error: new Error('test-error')
                })
        })
    })
    
    describe('REMOVE_RESOURCE_KEY', () => {
        it('should set fetching = true on REMOVE_RESOURCE_KEY_REQUEST', () => {
            assert.deepStrictEqual(
                reducer({}, {
                    type: actions.REMOVE_RESOURCE_KEY_REQUEST
                }), {
                    fetching: true
                })
        })
        
        it('should remove the key on REMOVE_RESOURCE_KEY_SUCCESS', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }, {
                                id: 'test2'
                            }, {
                                id: 'test3'
                            }]
                        }
                    }
                }, {
                    type: actions.REMOVE_RESOURCE_KEY_SUCCESS,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    keyId: 'test2'
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }, {
                                id: 'test3'
                            }]
                        }
                    },
                    error: null,
                    fetching: false
                })
        })
        
        it('should do nothing if no key is found on REMOVE_RESOURCE_KEY_SUCCESS', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }]
                        }
                    }
                }, {
                    type: actions.REMOVE_RESOURCE_KEY_SUCCESS,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    keyId: 'test2'
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }]
                        }
                    },
                    error: null,
                    fetching: false
                })
        })
    
        it('should handle the error on REMOVE_RESOURCE_KEY_FAILURE', () => {
            assert.deepStrictEqual(
                reducer({}, {
                    type: actions.REMOVE_RESOURCE_KEY_FAILURE,
                    error: new Error('test-error')
                }), {
                    fetching: false,
                    error: new Error('test-error')
                })
        })
    })
})