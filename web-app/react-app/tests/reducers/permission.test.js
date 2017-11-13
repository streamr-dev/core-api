import reducer from '../../reducers/permission'
import * as actions from '../../actions/permission'
import assert from 'assert-diff'
import _ from 'lodash'

describe('Permission reducer', () => {
    
    beforeEach(() => {
        global._ = _
    })
    
    afterEach(() => {
        delete global._
    })
    
    it('should return the initial state', () => {
        assert.deepStrictEqual(reducer(undefined, {}), {
            byTypeAndId: {},
            error: null,
            fetching: false
        })
    })
    
    describe('GET_RESOURCE_PERMISSIONS', () => {
        it('should set fetching = true on GET_RESOURCE_PERMISSIONS_REQUEST', () => {
            assert.deepStrictEqual(
                reducer({}, {
                    type: actions.GET_RESOURCE_PERMISSIONS_REQUEST
                }), {
                    fetching: true
                })
        })
        
        it('should add the permission to the resource on GET_RESOURCE_PERMISSIONS_SUCCESS', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {}
                }, {
                    type: actions.GET_RESOURCE_PERMISSIONS_SUCCESS,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permissions: [{
                        operation: 'test'
                    }, {
                        operation: 'test2'
                    }]
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                new: false,
                                fetching: false,
                                removed: false,
                                error: null
                            }, {
                                operation: 'test2',
                                new: false,
                                fetching: false,
                                removed: false,
                                error: null
                            }]
                        }
                    },
                    fetching: false,
                    error: null
                })
        })
        
        it('should handle the error on GET_RESOURCE_PERMISSIONS_FAILURE', () => {
            assert.deepStrictEqual(
                reducer({}, {
                    type: actions.GET_RESOURCE_PERMISSIONS_FAILURE,
                    error: new Error('test-error')
                }), {
                    fetching: false,
                    error: new Error('test-error')
                })
        })
    })
    
    describe('ADD_RESOURCE_PERMISSION', () => {
        it('should add the permission and set new = true on ADD_RESOURCE_PERMISSION', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: []
                        }
                    }
                }, {
                    type: actions.ADD_RESOURCE_PERMISSION,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'test',
                        user: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: true,
                                fetching: false,
                                removed: false,
                                error: null
                            }]
                        }
                    },
                    error: null,
                    fetching: false
                })
        })
        it('should also add the permission on initial state ADD_RESOURCE_PERMISSION', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {}
                }, {
                    type: actions.ADD_RESOURCE_PERMISSION,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'test',
                        user: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: true,
                                fetching: false,
                                removed: false,
                                error: null
                            }]
                        }
                    },
                    error: null,
                    fetching: false
                })
        })
    })
    
    describe('REMOVE_RESOURCE_PERMISSION', () => {
        it('should remove the permission on REMOVE_RESOURCE_PERMISSION', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: false
                            }]
                        }
                    }
                }, {
                    type: actions.REMOVE_RESOURCE_PERMISSION,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'test',
                        user: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: false,
                                fetching: false,
                                removed: true,
                                error: null
                            }]
                        }
                    },
                    error: null,
                    fetching: false
                })
        })
        it('should do nothing if no permission is found on REMOVE_RESOURCE_PERMISSION', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: false
                            }]
                        }
                    }
                }, {
                    type: actions.REMOVE_RESOURCE_PERMISSION,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'adfasdf',
                        user: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: false
                            }]
                        }
                    },
                    error: null,
                    fetching: false
                })
        })
    })
    
    describe('SAVE_REMOVED_RESOURCE_PERMISSION', () => {
        it('should set fetching = true SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                removed: true,
                                new: false
                            }]
                        }
                    }
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'test',
                        user: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                removed: true,
                                new: false,
                                fetching: true
                            }]
                        }
                    },
                    error: null,
                    fetching: false
                })
        })
        it('should remove the permission on SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test'
                            }, {
                                id: 'test2'
                            }]
                        }
                    }
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        id: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                id: 'test2'
                            }]
                        }
                    },
                    fetching: false,
                    error: null
                })
        })
        it('should handle the error and set removed = false on SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: false,
                                removed: true,
                                fetching: true
                            }]
                        }
                    }
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'test',
                        user: 'test',
                        error: new Error('test')
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: false,
                                removed: false,
                                fetching: false,
                                error: new Error('test')
                            }]
                        }
                    },
                    fetching: false,
                    error: null
                })
        })
    })
    
    describe('SAVE_ADDED_RESOURCE_PERMISSION', () => {
        it('should set fetching = true SAVE_ADDED_RESOURCE_PERMISSION_REQUEST', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: true
                            }]
                        }
                    }
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_REQUEST,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'test',
                        user: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: true,
                                fetching: true
                            }]
                        }
                    },
                    error: null,
                    fetching: false
                })
        })
        it('should set new = false on SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: true,
                                fetching: true
                            }]
                        }
                    }
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'test',
                        user: 'test'
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: false,
                                fetching: false
                            }]
                        }
                    },
                    fetching: false,
                    error: null
                })
        })
        it('should handle the error on SAVE_ADDED_RESOURCE_PERMISSION_FAILURE', () => {
            assert.deepStrictEqual(
                reducer({
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: true,
                                fetching: true
                            }]
                        }
                    }
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_FAILURE,
                    resourceId: 'testResourceId',
                    resourceType: 'testResourceType',
                    permission: {
                        operation: 'test',
                        user: 'test',
                        error: new Error('test')
                    }
                }), {
                    byTypeAndId: {
                        testResourceType: {
                            testResourceId: [{
                                operation: 'test',
                                user: 'test',
                                new: true,
                                fetching: false,
                                error: new Error('test')
                            }]
                        }
                    },
                    fetching: false,
                    error: null
                })
        })
    })
})