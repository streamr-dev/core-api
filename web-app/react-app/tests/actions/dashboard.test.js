
import assert from 'assert-diff'
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
        it('creates GET_AND_REPLACE_DASHBOARDS_SUCCESS when fetching dashboards has succeeded', async () => {
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
            
            await store.dispatch(actions.getAndReplaceDashboards())
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates GET_ALL_INTEGRATION_KEYS_FAILURE when fetching integration keys has failed', async () => {
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

            try {
                await store.dispatch(actions.getAndReplaceDashboards())
            } catch (e) {
                
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
            }
        })
    })
    
    describe('getDashboard', () => {
        it('creates GET_DASHBOARD_SUCCESS when fetching a dashboard has succeeded', async () => {
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
        
            await store.dispatch(actions.getDashboard(id))
            assert.deepStrictEqual(store.getActions(), expectedActions)
            
        })
        it('also accepts layout as a string', async () => {
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
        
            await store.dispatch(actions.getDashboard(id))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
    
        it('creates GET_ALL_INTEGRATION_KEYS_FAILURE when fetching integration keys has failed', async () => {
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
        
            try {
                await store.dispatch(actions.getAndReplaceDashboards())
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
            }
        })
    })
    
    describe('updateAndSaveDashboard', () => {
        it('creates UPDATE_AND_SAVE_DASHBOARD_SUCCESS and CREATE_NOTIFICATION when fetching a dashboard has succeeded', async () => {
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
        
            await store.dispatch(actions.updateAndSaveDashboard(db))
            assert.deepStrictEqual(store.getActions()[0], expectedActions[0])
            assert.equal(store.getActions()[1].type, expectedActions[1].type)
            assert.equal(store.getActions()[1].notification.type, expectedActions[1].notification.type)
            assert.deepStrictEqual(store.getActions()[2], expectedActions[2])
        })
        it('creates UPDATE_AND_SAVE_DASHBOARD_FAILURE and CREATE_NOTIFICATION when fetching a dashboard has succeeded', async () => {
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
        
            try {
                await store.dispatch(actions.updateAndSaveDashboard(db))
            } catch (e) {
                assert.deepStrictEqual(store.getActions()[0], expectedActions[0])
                assert.equal(store.getActions()[1].type, expectedActions[1].type)
                assert.equal(store.getActions()[1].notification.type, expectedActions[1].notification.type)
                assert.deepStrictEqual(store.getActions()[2], expectedActions[2])
            }
        })
        it('uses POST request if dashboard.new = true', done => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert.equal(request.url, 'api/v1/dashboards')
                assert.equal(request.config.method.toLowerCase(), 'post')
                done()
                request.respondWith({
                    status: 200,
                    response: request.config.data
                })
            })
            store.dispatch(actions.updateAndSaveDashboard({
                id: 'test',
                layout: 'test',
                new: true
            }))
        })
        it('uses PUT request if dashboard.new = false', done => {
            const id = 'test'
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert.equal(request.url, `api/v1/dashboards/${id}`)
                assert.equal(request.config.method.toLowerCase(), 'put')
                done()
                request.respondWith({
                    status: 200,
                    response: request.config.data
                })
            })
            store.dispatch(actions.updateAndSaveDashboard({
                id,
                layout: 'test',
                new: false
            }))
        })
    })
    
    describe('deleteDashboard', () => {
        it('creates DELETE_DASHBOARD_SUCCESS when deleting dashboard has succeeded', () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'delete')
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
        })
        
        it('creates DELETE_DASHBOARD_FAILURE when deleting dashboard has failed', async () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'delete')
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
            
            try {
                await store.dispatch(actions.deleteDashboard('test'))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
            }
        })
    })
    
    describe('getMyDashboardPermissions', () => {
        it('creates GET_MY_DASHBOARD_PERMISSIONS_SUCCESS with the operations when fetching permissions has succeeded', async () => {
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
        
            await store.dispatch(actions.getMyDashboardPermissions(id))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates GET_MY_DASHBOARD_PERMISSIONS_FAILURE when fetching permissions has failed', async () => {
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
        
            try {
                await store.dispatch(actions.getMyDashboardPermissions(id))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
            }
        })
    })
    
    describe('updateDashboard', () => {
        it('must return correct action', () => {
            assert.deepStrictEqual(actions.updateDashboard({
                id: 'test'
            }), {
                type: actions.UPDATE_DASHBOARD,
                dashboard: {
                    id: 'test'
                }
            })
        })
    
        describe('addDashboardItem', () => {
            it('must return correct action', () => {
                assert.deepStrictEqual(actions.updateDashboardItem({
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
                }), {
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
                assert.deepStrictEqual(actions.updateDashboardItem({
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
                }), {
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
                assert.deepStrictEqual(actions.removeDashboardItem({
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
                }), {
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
            assert.deepStrictEqual(actions.createDashboard({
                id: 'test'
            }), {
                type: actions.CREATE_DASHBOARD,
                dashboard: {
                    id: 'test'
                }
            })
        })
        describe('createDashboard', () => {
            const id = 'test'
            it('must return correct action', () => {
                assert.deepStrictEqual(actions.newDashboard(id), {
                    type: actions.CREATE_DASHBOARD,
                    dashboard: {
                        id,
                        name: 'Untitled Dashboard',
                        items: [],
                        layout: {},
                        editingLocked: false
                    }
                })
            })
        })
    })
    
    describe('openDashboard', () => {
        const id = 'test'
        it('must return correct action', () => {
            assert.deepStrictEqual(actions.openDashboard(id), {
                type: actions.OPEN_DASHBOARD,
                id
            })
        })
    })
})