
import expect from 'expect'
import thunk from 'redux-thunk'
import configureMockStore from 'redux-mock-store'
import moxios from 'moxios'

import * as actions from '../../actions/dashboard'
import * as notificationActions from '../../actions/notification'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

global.Streamr = {
    createLink: ({uri}) => uri
}

describe('Dashboard actions', () => {
    let store
    
    beforeEach(() => {
        moxios.install()
        store = mockStore({
            dashboardsById: {},
            openDashboard: {
                id: null
            },
            error: null
        })
    })
    
    
    afterEach(() => {
        moxios.uninstall()
        store.clearActions()
    })
    
    describe('getAndReplaceDashboards', () => {
        it('creates GET_AND_REPLACE_DASHBOARDS_SUCCESS when fetching dashboards has succeeded', () => {
            moxios.stubRequest('api/v1/dashboards', {
                status: 200,
                response: [{
                    id: 'test',
                    name: 'test'
                },{
                    id: 'test2',
                    name: 'test2'
                }]
            })
            
            const expectedActions = [{
                type: actions.GET_AND_REPLACE_DASHBOARDS_REQUEST
            }, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_SUCCESS,
                dashboards: [{
                    id: 'test',
                    name: 'test'
                },{
                    id: 'test2',
                    name: 'test2'
                }]
            }]
            
            return store.dispatch(actions.getAndReplaceDashboards())
                .then(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
        it('creates GET_ALL_INTEGRATION_KEYS_FAILURE when fetching integration keys has failed', () => {
            moxios.stubRequest('api/v1/dashboards', {
                status: 500,
                response: new Error('test-error')
            })
        
            const expectedActions = [{
                type: actions.GET_AND_REPLACE_DASHBOARDS_REQUEST
            }, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_FAILURE,
                error: new Error('test-error')
            }]
        
            return store.dispatch(actions.getAndReplaceDashboards())
                .catch(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    })
    
    describe('getDashboard', () => {
        it('creates GET_DASHBOARD_SUCCESS when fetching a dashboard has succeeded', () => {
            const id = 'asdfasdfasasd'
            moxios.stubRequest(`api/v1/dashboards/${id}`, {
                status: 200,
                response: {
                    id: 'test',
                    name: 'test',
                    layout: {
                        testing: true
                    }
                }
            })
        
            const expectedActions = [{
                type: actions.GET_DASHBOARD_REQUEST,
                id
            }, {
                type: actions.GET_DASHBOARD_SUCCESS,
                dashboard: {
                    id: 'test',
                    name: 'test',
                    layout: {
                        testing: true
                    }
                }
            }]
        
            return store.dispatch(actions.getDashboard(id))
                .then(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
        it('also accepts layout as a string', () => {
            const id = 'asdfasdfasasd'
            moxios.stubRequest(`api/v1/dashboards/${id}`, {
                status: 200,
                response: {
                    id: 'test',
                    name: 'test',
                    layout: JSON.stringify({
                        testing: true
                    })
                }
            })
        
            const expectedActions = [{
                type: actions.GET_DASHBOARD_REQUEST,
                id
            }, {
                type: actions.GET_DASHBOARD_SUCCESS,
                dashboard: {
                    id: 'test',
                    name: 'test',
                    layout: {
                        testing: true
                    }
                }
            }]
        
            return store.dispatch(actions.getDashboard(id))
                .then(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    
        it('creates GET_ALL_INTEGRATION_KEYS_FAILURE when fetching integration keys has failed', () => {
            moxios.stubRequest('api/v1/dashboards', {
                status: 500,
                response: new Error('test-error')
            })
        
            const expectedActions = [{
                type: actions.GET_AND_REPLACE_DASHBOARDS_REQUEST
            }, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_FAILURE,
                error: new Error('test-error')
            }]
        
            return store.dispatch(actions.getAndReplaceDashboards())
                .catch(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    })
    
    describe('updateAndSaveDashboard', () => {
        it('creates UPDATE_AND_SAVE_DASHBOARD_SUCCESS and CREATE_NOTIFICATION when fetching a dashboard has succeeded', () => {
            const id = 'test'
            const db = {
                id,
                name: 'test',
                layout: {
                    test: true
                }
            }
            moxios.stubRequest(`api/v1/dashboards/${id}`, {
                status: 200,
                response: db
            })
        
            const expectedActions = [{
                type: actions.UPDATE_AND_SAVE_DASHBOARD_REQUEST
            }, {
                type: notificationActions.CREATE_NOTIFICATION,
                notification: {
                    type: 'success'
                }
            }, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_SUCCESS,
                dashboard: db
            }]
        
            return store.dispatch(actions.updateAndSaveDashboard(db))
                .then(() => {
                    expect(store.getActions()[0]).toEqual(expectedActions[0])
                    expect(store.getActions()[1].type).toEqual(expectedActions[1].type)
                    expect(store.getActions()[1].notification.type).toEqual(expectedActions[1].notification.type)
                    expect(store.getActions()[2]).toEqual(expectedActions[2])
                })
        })
        it('creates UPDATE_AND_SAVE_DASHBOARD_FAILURE and CREATE_NOTIFICATION when fetching a dashboard has succeeded', () => {
            const id = 'test'
            const db = {
                id,
                name: 'test',
                layout: {
                    test: true
                }
            }
            moxios.stubRequest(`api/v1/dashboards/${id}`, {
                status: 500,
                response: new Error('test')
            })
        
            const expectedActions = [{
                type: actions.UPDATE_AND_SAVE_DASHBOARD_REQUEST
            }, {
                type: notificationActions.CREATE_NOTIFICATION,
                notification: {
                    type: 'error'
                }
            }, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_FAILURE,
                error: new Error('test')
            }]
        
            return store.dispatch(actions.updateAndSaveDashboard(db))
                .catch(() => {
                    expect(store.getActions()[0]).toEqual(expectedActions[0])
                    expect(store.getActions()[1].type).toEqual(expectedActions[1].type)
                    expect(store.getActions()[1].notification.type).toEqual(expectedActions[1].notification.type)
                    expect(store.getActions()[2]).toEqual(expectedActions[2])
                })
        })
        it('uses POST request if createNew = true', done => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                expect(request.url).toBe('api/v1/dashboards')
                expect(request.config.method.toLowerCase()).toEqual('post')
                done()
                request.respondWith({
                    status: 200,
                    response: request.config.data
                })
            })
            store.dispatch(actions.updateAndSaveDashboard({
                id: 'test',
                layout: 'test'
            }, true))
        })
        it('uses PUT request if createNew = false', done => {
            const id = 'test'
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                expect(request.url).toBe(`api/v1/dashboards/${id}`)
                expect(request.config.method.toLowerCase()).toEqual('put')
                done()
                request.respondWith({
                    status: 200,
                    response: request.config.data
                })
            })
            store.dispatch(actions.updateAndSaveDashboard({
                id,
                layout: 'test'
            }, false))
        })
    })
    
    describe('deleteDashboard', () => {
        it('creates DELETE_DASHBOARD_SUCCESS when deleting dashboard has succeeded', () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                expect(request.config.method).toEqual('delete')
                request.respondWith({
                    status: 200
                })
            })
            
            const expectedActions = [{
                type: actions.DELETE_DASHBOARD_REQUEST,
                id: 'test'
            }, {
                type: actions.DELETE_DASHBOARD_SUCCESS,
                id: 'test'
            }]
            
            return store.dispatch(actions.deleteDashboard('test'))
                .then(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
        
        it('creates DELETE_DASHBOARD_FAILURE when deleting dashboard has failed', () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                expect(request.config.method).toEqual('delete')
                request.respondWith({
                    status: 500,
                    response: new Error('test')
                })
            })
            
            const expectedActions = [{
                type: actions.DELETE_DASHBOARD_REQUEST,
                id: 'test'
            }, {
                type: actions.DELETE_DASHBOARD_FAILURE,
                error: new Error('test')
            }]
            
            return store.dispatch(actions.deleteDashboard('test'))
                .catch(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    })
    
    describe('getMyDashboardPermissions', () => {
        it('creates GET_MY_DASHBOARD_PERMISSIONS_SUCCESS with the operations when fetching permissions has succeeded', () => {
            const id = 'asdfasdfasasd'
            moxios.stubRequest(`api/v1/dashboards/${id}/permissions/me`, {
                status: 200,
                response: [{
                    operation: 'test'
                }, {
                    operation: 'test2'
                }]
            })
        
            const expectedActions = [{
                id,
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_REQUEST
            }, {
                id,
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
                permissions: ['test', 'test2']
            }]
        
            return store.dispatch(actions.getMyDashboardPermissions(id))
                .then(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
        it('creates GET_MY_DASHBOARD_PERMISSIONS_FAILURE when fetching permissions has failed', () => {
            const id = 'asdfasdfasasd'
            moxios.stubRequest(`api/v1/dashboards/${id}/permissions/me`, {
                status: 500,
                response: new Error('test-error')
            })
        
            const expectedActions = [{
                id,
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_REQUEST
            }, {
                id,
                type: actions.GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
                error: new Error('test-error')
            }]
        
            return store.dispatch(actions.getMyDashboardPermissions(id))
                .catch(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    })
    
    describe('updateDashboard', () => {
        it('must return correct action', () => {
            expect(actions.updateDashboard({
                id: 'test'
            })).toEqual({
                type: actions.UPDATE_DASHBOARD,
                dashboard: {
                    id: 'test'
                }
            })
        })
    
        describe('addDashboardItem', () => {
            it('must return correct action', () => {
                expect(actions.updateDashboardItem({
                    id: 'test',
                    items: [{
                        canvas: 'a',
                        module: 0,
                        thirdField: 'a'
                    }]
                }, {
                    canvas: 'b',
                    module: 0,
                    thirdField: 'test'
                })).toEqual({
                    type: actions.UPDATE_DASHBOARD,
                    dashboard: {
                        id: 'test',
                        items: [{
                            canvas: 'a',
                            module: 0,
                            thirdField: 'a'
                        },{
                            canvas: 'b',
                            module: 0,
                            thirdField: 'test'
                        }]
                    }
                })
            })
        })
    
        describe('updateDashboardItem', () => {
            it('must return correct action', () => {
                expect(actions.updateDashboardItem({
                    id: 'test',
                    items: [{
                        canvas: 'a',
                        module: 0,
                        thirdField: 'a'
                    },{
                        canvas: 'b',
                        module: 0,
                        thirdField: 'a'
                    }]
                }, {
                    canvas: 'b',
                    module: 0,
                    thirdField: 'test'
                })).toEqual({
                    type: actions.UPDATE_DASHBOARD,
                    dashboard: {
                        id: 'test',
                        items: [{
                            canvas: 'a',
                            module: 0,
                            thirdField: 'a'
                        },{
                            canvas: 'b',
                            module: 0,
                            thirdField: 'test'
                        }]
                    }
                })
            })
        })
    
        describe('removeDashboardItem', () => {
            it('must return correct action', () => {
                expect(actions.removeDashboardItem({
                    id: 'test',
                    items: [{
                        canvas: 'a',
                        module: 0,
                        thirdField: 'a'
                    },{
                        canvas: 'b',
                        module: 0,
                        thirdField: 'a'
                    }]
                }, {
                    canvas: 'b',
                    module: 0,
                    thirdField: 'test'
                })).toEqual({
                    type: actions.UPDATE_DASHBOARD,
                    dashboard: {
                        id: 'test',
                        items: [{
                            canvas: 'a',
                            module: 0,
                            thirdField: 'a'
                        }]
                    }
                })
            })
        })
    })
    
    describe('createDashboard', () => {
        it('must return correct action', () => {
            expect(actions.createDashboard({
                id: 'test'
            })).toEqual({
                type: actions.CREATE_DASHBOARD,
                dashboard: {
                    id: 'test'
                }
            })
        })
        describe('createDashboard', () => {
            const id = 'test'
            it('must return correct action', () => {
                expect(actions.newDashboard(id)).toEqual({
                    type: actions.CREATE_DASHBOARD,
                    dashboard: {
                        id,
                        name: 'Untitled Dashboard',
                        items: []
                    }
                })
            })
        })
    })
    
    describe('openDashboard', () => {
        const id = 'test'
        it('must return correct action', () => {
            expect(actions.openDashboard(id)).toEqual({
                type: actions.OPEN_DASHBOARD,
                id
            })
        })
    })
})