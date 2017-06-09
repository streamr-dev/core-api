
import reducer from '../../reducers/dashboard'
import * as actions from '../../actions/dashboard'
import expect from 'expect'
import _ from 'lodash'

describe('Dashboard reducer', () => {
    
    const initialState = {
        dashboardsById: {},
        openDashboard: {
            id: null,
            saved: false,
            new: true
        },
        error: null,
        fetching: false
    }
    
    beforeEach(() => {
        global._ = _
    })
    
    afterEach(() => {
        delete global._
    })
    
    it('should return the initial state', () => {
        expect(
            reducer(undefined, {})
        ).toEqual(initialState)
    })
    
    it('should handle OPEN_DASHBOARD', () => {
        expect(
            reducer(initialState, {
                type: actions.OPEN_DASHBOARD,
                id: 'test'
            })
        ).toEqual({
            ...initialState,
            openDashboard: {
                id: 'test',
                saved: true,
                new: false
            }
        })
    })
    
    it('should handle GET_AND_REPLACE_DASHBOARDS', () => {
        expect(
            reducer(initialState, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_REQUEST
            })
        ).toEqual({
            ...initialState,
            fetching: true
        })
        
        expect(
            reducer({
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
            })
        ).toEqual({
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
    
    
        expect(
            reducer(initialState, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_FAILURE,
                error: new Error('test')
            })
        ).toEqual({
            ...initialState,
            fetching: false,
            error: new Error('test')
        })
    })
    
    it('should handle GET_DASHBOARD', () => {
        expect(
            reducer(initialState, {
                type: actions.GET_DASHBOARD_REQUEST
            })
        ).toEqual({
            ...initialState,
            fetching: true
        })
        
        expect(
            reducer({
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
            })
        ).toEqual({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test'
                },
                test2: {
                    id: 'test2'
                }
            },
            openDashboard: {
                id: null,
                saved: true,
                new: false
            },
            error: null,
            fetching: false
        })
        
        
        expect(
            reducer(initialState, {
                type: actions.GET_DASHBOARD_FAILURE,
                error: new Error('test')
            })
        ).toEqual({
            ...initialState,
            fetching: false,
            error: new Error('test')
        })
    })
    
    it('should handle UPDATE_AND_SAVE_DASHBOARD', () => {
        expect(
            reducer(initialState, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_REQUEST
            })
        ).toEqual({
            ...initialState,
            fetching: true
        })
        
        expect(
            reducer({
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
            })
        ).toEqual({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test'
                },
                test2: {
                    id: 'test2'
                }
            },
            openDashboard: {
                id: null,
                saved: true,
                new: false
            },
            error: null,
            fetching: false
        })
        
        expect(
            reducer(initialState, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_FAILURE,
                error: new Error('test')
            })
        ).toEqual({
            ...initialState,
            fetching: false,
            error: new Error('test')
        })
    })
    
    it('should handle DELETE_DASHBOARD', () => {
        expect(
            reducer(initialState, {
                type: actions.DELETE_DASHBOARD_REQUEST
            })
        ).toEqual({
            ...initialState,
            fetching: true
        })
        
        expect(
            reducer({
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
            })
        ).toEqual({
            ...initialState,
            dashboardsById: {
                test2: {
                    id: 'test2'
                }
            },
            error: null,
            fetching: false
        })
        
        expect(
            reducer(initialState, {
                type: actions.DELETE_DASHBOARD_FAILURE,
                error: new Error('test')
            })
        ).toEqual({
            ...initialState,
            fetching: false,
            error: new Error('test')
        })
    })
    
    it('should handle GET_MY_DASHBOARD_PERMISSIONS', () => {
        expect(
            reducer(initialState, {
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_REQUEST
            })
        ).toEqual({
            ...initialState,
            fetching: true
        })
        
        expect(
            reducer({
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
            })
        ).toEqual({
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
        
        expect(
            reducer(initialState, {
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
                error: new Error('test')
            })
        ).toEqual({
            ...initialState,
            fetching: false,
            error: new Error('test')
        })
    })
    
    it('should handle UPDATE_DASHBOARD', () => {
        expect(
            reducer({
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
            })
        ).toEqual({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    a: 2
                }
            },
            openDashboard: {
                id: null,
                saved: false,
                new: false
            },
            error: null,
            fetching: false
        })
    })
    
    it('should handle CREATE_DASHBOARD', () => {
        expect(
            reducer({
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
            })
        ).toEqual({
            ...initialState,
            dashboardsById: {
                test: {
                    id: 'test',
                    a: 2
                }
            },
            openDashboard: {
                id: null,
                saved: false,
                new: true
            },
            error: null,
            fetching: false
        })
    })
})