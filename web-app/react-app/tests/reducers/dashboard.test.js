
import reducer from '../../reducers/dashboard'
import * as actions from '../../actions/dashboard'
import assert from 'assert-diff'
import _ from 'lodash'

describe('Dashboard reducer', () => {
    
    const initialState = {
        dashboardsById: {},
        openDashboard: {
            id: null,
            isFullScreen: false
        },
        error: null,
        fetching: false
    }
    
    it('should return the initial state', () => {
        assert.deepStrictEqual(reducer(undefined, {}), initialState)
    })
    
    it('should handle OPEN_DASHBOARD', () => {
        assert.deepStrictEqual(reducer(initialState, {
            type: actions.OPEN_DASHBOARD,
            id: 'test'
        }), {
            ...initialState,
            openDashboard: {
                id: 'test',
                isFullScreen: false
            }
        })
    })
    
    it('should handle CHANGE_DASHBOARD_ID', () => {
        assert.deepStrictEqual(reducer({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test'
                }
            }
        }, {
            type: actions.CHANGE_DASHBOARD_ID,
            oldId: 'test',
            newId: 'test2'
        }), {
            ...initialState,
            dashboardsById: {
                test2: {
                    id: 'test2'
                }
            }
        })
    })
    
    describe('GET_AND_REPLACE_DASHBOARDS', () => {
        it('should handle GET_AND_REPLACE_DASHBOARDS_REQUEST', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_REQUEST
            }), {
                ...initialState,
                fetching: true
            })
        })
        it('should handle GET_AND_REPLACE_DASHBOARDS_SUCCESS', () => {
            assert.deepStrictEqual(reducer({
                ...initialState,
                dashboardsById: {
                    test: 'moi'
                }
            }, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_SUCCESS,
                dashboards: [{
                    id: 'test'
                }, {
                    id: 'test2'
                }]
            }), {
                ...initialState,
                dashboardsById: {
                    test: {
                        id: 'test'
                    },
                    test2: {
                        id: 'test2'
                    }
                }
            })
        })
        it('should handle GET_AND_REPLACE_DASHBOARDS_FAILURE', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }), {
                ...initialState,
                fetching: false,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            })
        })
    })
    
    describe('GET_DASHBOARD', () => {
        it('should handle GET_DASHBOARD_REQUEST', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.GET_DASHBOARD_REQUEST
            }), {
                ...initialState,
                fetching: true
            })
        })
        it('should handle GET_DASHBOARD_SUCCESS', () => {
            assert.deepStrictEqual(reducer({
                ...initialState,
                dashboardsById: {
                    test: {
                        id: 'test'
                    }
                }
            }, {
                type: actions.GET_DASHBOARD_SUCCESS,
                dashboard: {
                    id: 'test2'
                }
            }), {
                ...initialState,
                dashboardsById: {
                    test: {
                        id: 'test'
                    },
                    test2: {
                        id: 'test2',
                        new: false,
                        saved: true
                    }
                },
                openDashboard: {
                    id: null,
                    isFullScreen: false
                },
                error: null,
                fetching: false
            })
        })
        it('should handle GET_DASHBOARD_FAILURE', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.GET_DASHBOARD_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }), {
                ...initialState,
                fetching: false,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            })
        })
    })
    
    describe('UPDATE_AND_SAVE_DASHBOARD', () => {
        it('should handle UPDATE_AND_SAVE_DASHBOARD_REQUEST', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_REQUEST
            }), {
                ...initialState,
                fetching: true
            })
        })
        it('should handle UPDATE_AND_SAVE_DASHBOARD_SUCCESS', () => {
            assert.deepStrictEqual(reducer({
                ...initialState,
                dashboardsById: {
                    test: {
                        id: 'test'
                    }
                }
            }, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_SUCCESS,
                dashboard: {
                    id: 'test2'
                }
            }), {
                ...initialState,
                dashboardsById: {
                    test: {
                        id: 'test'
                    },
                    test2: {
                        id: 'test2',
                        saved: true,
                        new: false
                    }
                },
                openDashboard: {
                    id: null,
                    isFullScreen: false
                },
                error: null,
                fetching: false
            })
        })
        it('should handle UPDATE_AND_SAVE_DASHBOARD_FAILURE', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }), {
                ...initialState,
                fetching: false,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            })
        })
    })
    
    describe('DELETE_DASHBOARD', () => {
        it('should handle DELETE_DASHBOARD_REQUEST', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.DELETE_DASHBOARD_REQUEST
            }), {
                ...initialState,
                fetching: true
            })
        })
        it('should handle DELETE_DASHBOARD_SUCCESS', () => {
            assert.deepStrictEqual(reducer({
                ...initialState,
                dashboardsById: {
                    test: {
                        id: 'test'
                    },
                    test2: {
                        id: 'test2'
                    }
                }
            }, {
                type: actions.DELETE_DASHBOARD_SUCCESS,
                id: 'test'
            }), {
                ...initialState,
                dashboardsById: {
                    test2: {
                        id: 'test2'
                    }
                },
                error: null,
                fetching: false
            })
        })
        it('should handle DELETE_DASHBOARD_FAILURE', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.DELETE_DASHBOARD_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }), {
                ...initialState,
                fetching: false,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            })
        })
    })
    
    describe('GET_MY_DASHBOARD_PERMISSIONS', () => {
        it('should handle GET_MY_DASHBOARD_PERMISSIONS_REQUEST', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_REQUEST
            }), {
                ...initialState,
                fetching: true
            })
        })
        it('should handle GET_MY_DASHBOARD_PERMISSIONS_SUCCESS', () => {
            assert.deepStrictEqual(reducer({
                ...initialState,
                dashboardsById: {
                    test: {
                        id: 'test'
                    },
                    test2: {
                        id: 'test2'
                    }
                }
            }, {
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
                id: 'test',
                permissions: ['test', 'test2']
            }), {
                ...initialState,
                dashboardsById: {
                    test: {
                        id: 'test',
                        ownPermissions: ['test', 'test2']
                    },
                    test2: {
                        id: 'test2'
                    }
                },
                error: null,
                fetching: false
            })
        })
        it('should handle GET_MY_DASHBOARD_PERMISSIONS_FAILURE', () => {
            assert.deepStrictEqual(reducer(initialState, {
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }), {
                ...initialState,
                fetching: false,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            })
        })
    })
    
    it('should handle UPDATE_DASHBOARD', () => {
        assert.deepStrictEqual(reducer({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    a: 1
                }
            }
        }, {
            type: actions.UPDATE_DASHBOARD,
            dashboard: {
                id: 'test',
                a: 2
            }
        }), {
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    a: 2,
                    saved: false
                }
            },
            openDashboard: {
                id: null,
                isFullScreen: false
            },
            error: null,
            fetching: false
        })
    })
    
    it('should handle CREATE_DASHBOARD', () => {
        assert.deepStrictEqual(reducer({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    a: 1
                }
            }
        }, {
            type: actions.CREATE_DASHBOARD,
            dashboard: {
                id: 'test',
                a: 2
            }
        }), {
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    a: 2,
                    saved: true,
                    new: true
                }
            },
            openDashboard: {
                id: null,
                isFullScreen: false
            },
            error: null,
            fetching: false
        })
    })
    
    it('should handle LOCK_DASHBOARD_EDITING', () => {
        assert.deepStrictEqual(reducer({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test'
                },
                test2: {
                    id: 'test2'
                }
            }
        }, {
            type: actions.LOCK_DASHBOARD_EDITING,
            id: 'test'
        }), {
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    editingLocked: true
                },
                test2: {
                    id: 'test2'
                }
            }
        })
    })
    
    it('should handle UNLOCK_DASHBOARD_EDITING', () => {
        assert.deepStrictEqual(reducer({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    editingLocked: true
                },
                test2: {
                    id: 'test2'
                }
            }
        }, {
            type: actions.UNLOCK_DASHBOARD_EDITING,
            id: 'test'
        }), {
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    editingLocked: false
                },
                test2: {
                    id: 'test2'
                }
            }
        })
    })
})