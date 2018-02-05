import assert from 'assert-diff'
import thunk from 'redux-thunk'
import configureMockStore from 'redux-mock-store'
import moxios from 'moxios'

import * as originalActions from '../../actions/dashboard'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

global.Streamr = {
    createLink: ({uri}) => uri
}

describe('Dashboard actions', () => {
    let store
    let actions
    
    beforeEach(() => {
        moxios.install()
        store = mockStore({
            dashboardsById: {},
            openDashboard: {
                id: null
            },
            error: null
        })
        actions = originalActions
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
                }, {
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
                }, {
                    id: 'test2',
                    name: 'test2'
                }]
            }]
            
            await store.dispatch(actions.getAndReplaceDashboards())
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates GET_ALL_INTEGRATION_KEYS_FAILURE when fetching integration keys has failed', async (done) => {
            moxios.stubRequest('api/v1/dashboards', {
                status: 500,
                response: {
                    message: 'test-error',
                    code: 'TEST'
                }
            })
            
            const expectedActions = [{
                type: actions.GET_AND_REPLACE_DASHBOARDS_REQUEST
            }, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_FAILURE,
                error: {
                    message: 'test-error',
                    code: 'TEST',
                    statusCode: 500
                }
            }]
            
            try {
                await store.dispatch(actions.getAndReplaceDashboards())
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
                done()
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
        
        it('creates GET_ALL_INTEGRATION_KEYS_FAILURE when fetching integration keys has failed', async (done) => {
            moxios.stubRequest('api/v1/dashboards', {
                status: 500,
                response: {
                    message: 'test-error',
                    code: 'TEST'
                }
            })
            
            const expectedActions = [{
                type: actions.GET_AND_REPLACE_DASHBOARDS_REQUEST
            }, {
                type: actions.GET_AND_REPLACE_DASHBOARDS_FAILURE,
                error: {
                    message: 'test-error',
                    code: 'TEST',
                    statusCode: 500
                }
            }]
            
            try {
                await store.dispatch(actions.getAndReplaceDashboards())
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
                done()
            }
        })
    })
    
    describe('updateAndSaveDashboard', () => {
        it('creates UPDATE_AND_SAVE_DASHBOARD_SUCCESS and creates notification when fetching a dashboard has succeeded', async () => {
            const id = 'test'
            const db = {
                id,
                name: 'test',
                layout: {
                    test: true
                },
                ownPermissions: []
            }
            moxios.stubRequest(`api/v1/dashboards/${id}`, {
                status: 200,
                response: {
                    ...db,
                    id: 'test2'
                }
            })
            
            const expectedActions = [{
                type: actions.UPDATE_AND_SAVE_DASHBOARD_REQUEST
            }, {
                level: 'success'
            }, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_SUCCESS,
                dashboard: db
            }]
            
            await store.dispatch(actions.updateAndSaveDashboard(db))
            assert.deepStrictEqual(store.getActions()[0], expectedActions[0])
            assert.equal(store.getActions()[1].level, expectedActions[1].level)
            assert.deepStrictEqual(store.getActions()[2], expectedActions[2])
        })
        it('creates also CHANGE_ID if the id has changed', async () => {
            const id = 'test'
            const db = {
                id,
                name: 'test',
                layout: {
                    test: true
                },
                ownPermissions: []
            }
            moxios.stubRequest(`api/v1/dashboards/${id}`, {
                status: 200,
                response: {
                    ...db,
                    id: 'new_test'
                }
            })
            
            const expectedActions = [{
                type: actions.UPDATE_AND_SAVE_DASHBOARD_REQUEST
            }, {
                level: 'success'
            }, {
                type: actions.CHANGE_DASHBOARD_ID,
                oldId: 'test',
                newId: 'new_test'
            }, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_SUCCESS,
                dashboard: {
                    ...db,
                    id: 'new_test'
                }
            }, {
                type: notificationActions.CREATE_NOTIFICATION,
                notification: {
                    type: 'success'
                }
            }]
            
            await store.dispatch(actions.updateAndSaveDashboard(db))
            assert.deepStrictEqual(store.getActions().slice(0, 3), expectedActions.slice(0, 3))
            assert.equal(store.getActions()[3].type, expectedActions[3].type)
            assert.equal(store.getActions()[3].notification.type, expectedActions[3].notification.type)
        })
        it('creates UPDATE_AND_SAVE_DASHBOARD_FAILURE and creates notification when fetching a dashboard has failed', async (done) => {
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
                response: {
                    message: 'test',
                    code: 'TEST'
                }
            })
            
            const expectedActions = [{
                type: actions.UPDATE_AND_SAVE_DASHBOARD_REQUEST
            }, {
                level: 'error'
            }, {
                type: actions.UPDATE_AND_SAVE_DASHBOARD_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }]
            
            try {
                await store.dispatch(actions.updateAndSaveDashboard(db))
            } catch (e) {
                assert.deepStrictEqual(store.getActions()[0], expectedActions[0])
                assert.equal(store.getActions()[1].level, expectedActions[1].level)
                assert.deepStrictEqual(store.getActions()[2], expectedActions[2])
                done()
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
            store.dispatch(originalActions.updateAndSaveDashboard({
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
            store.dispatch(originalActions.updateAndSaveDashboard({
                id,
                layout: 'test',
                new: false
            }))
        })
    })
    
    describe('deleteDashboard', () => {
        it('creates DELETE_DASHBOARD_SUCCESS when deleting dashboard has succeeded', async () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'delete')
                request.respondWith({
                    status: 200
                })
            })
            
            const expectedActions = [{
                type: originalActions.DELETE_DASHBOARD_REQUEST,
                id: 'test'
            }, {
                type: originalActions.DELETE_DASHBOARD_SUCCESS,
                id: 'test'
            }]
            
            await store.dispatch(originalActions.deleteDashboard('test'))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        
        it('creates DELETE_DASHBOARD_FAILURE when deleting dashboard has failed', async (done) => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'delete')
                request.respondWith({
                    status: 500,
                    response: {
                        message: 'test',
                        code: 'TEST'
                    }
                })
            })
            
            const expectedActions = [{
                type: originalActions.DELETE_DASHBOARD_REQUEST,
                id: 'test'
            }, {
                type: originalActions.DELETE_DASHBOARD_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }]
            
            try {
                await store.dispatch(originalActions.deleteDashboard('test'))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
                done()
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
                type: originalActions.GET_MY_DASHBOARD_PERMISSIONS_REQUEST
            }, {
                id,
                type: originalActions.GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
                permissions: ['test', 'test2']
            }]
            
            await store.dispatch(originalActions.getMyDashboardPermissions(id))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates GET_MY_DASHBOARD_PERMISSIONS_FAILURE when fetching permissions has failed', async (done) => {
            const id = 'asdfasdfasasd'
            moxios.stubRequest(`api/v1/dashboards/${id}/permissions/me`, {
                status: 500,
                response: {
                    message: 'test-error',
                    code: 'TEST'
                }
            })
            
            const expectedActions = [{
                id,
                type: originalActions.GET_MY_DASHBOARD_PERMISSIONS_REQUEST
            }, {
                id,
                type: originalActions.GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
                error: {
                    message: 'test-error',
                    code: 'TEST',
                    statusCode: 500
                }
            }]
            
            try {
                await store.dispatch(originalActions.getMyDashboardPermissions(id))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
                done()
            }
        })
    })
    
    describe('updateDashboard', () => {
        it('must return correct action', () => {
            assert.deepStrictEqual(originalActions.updateDashboard({
                id: 'test'
            }), {
                type: originalActions.UPDATE_DASHBOARD,
                dashboard: {
                    id: 'test'
                }
            })
        })
        
        describe('addDashboardItem', () => {
            it('must return correct action', () => {
                const db = {
                    id: 'test',
                    items: [{
                        canvas: 'a',
                        module: 0,
                        thirdField: 'a'
                    }]
                }
                assert.deepStrictEqual(originalActions.addDashboardItem(db, {
                    canvas: 'b',
                    module: 0,
                    thirdField: 'test'
                }), {
                    type: originalActions.UPDATE_DASHBOARD,
                    dashboard: {
                        id: 'test',
                        items: [{
                            canvas: 'a',
                            module: 0,
                            thirdField: 'a'
                        }, {
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
                const db = {
                    id: 'test',
                    items: [{
                        canvas: 'a',
                        module: 0,
                        thirdField: 'a'
                    }, {
                        canvas: 'b',
                        module: 0,
                        thirdField: 'a'
                    }]
                }
                assert.deepStrictEqual(originalActions.updateDashboardItem(db, {
                    canvas: 'b',
                    module: 0,
                    thirdField: 'test'
                }), {
                    type: originalActions.UPDATE_DASHBOARD,
                    dashboard: {
                        id: 'test',
                        items: [{
                            canvas: 'a',
                            module: 0,
                            thirdField: 'a'
                        }, {
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
                assert.deepStrictEqual(originalActions.removeDashboardItem({
                    id: 'test',
                    items: [{
                        canvas: 'a',
                        module: 0,
                        thirdField: 'a'
                    }, {
                        canvas: 'b',
                        module: 0,
                        thirdField: 'a'
                    }]
                }, {
                    canvas: 'b',
                    module: 0,
                    thirdField: 'test'
                }), {
                    type: originalActions.UPDATE_DASHBOARD,
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
    
    describe('updateDashboardChanges', async () => {
        store.dispatch(originalActions.createDashboard({
            id: 'test',
            name: 'test',
            name2: 'test2'
        }))
        const expectedActions = [{
            type: originalActions.UPDATE_DASHBOARD,
            dashboard: {
                id: 'test',
                name: 'test3',
                name2: 'test2'
            }
        }]
        
        await store.dispatch(originalActions.updateDashboardChanges('test', {
            name: 'test3'
        }))
        assert.deepStrictEqual(store.getActions(), expectedActions)
    })
    
    describe('createDashboard', () => {
        it('must return correct action', () => {
            assert.deepStrictEqual(originalActions.createDashboard({
                id: 'test'
            }), {
                type: originalActions.CREATE_DASHBOARD,
                dashboard: {
                    id: 'test'
                }
            })
        })
        describe('createDashboard', () => {
            const id = 'test'
            it('must return correct action', () => {
                assert.deepStrictEqual(originalActions.newDashboard(id), {
                    type: originalActions.CREATE_DASHBOARD,
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
            assert.deepStrictEqual(originalActions.openDashboard(id), {
                type: originalActions.OPEN_DASHBOARD,
                id
            })
        })
    })
    
    describe('lockDashboardEditing', () => {
        assert.deepStrictEqual(originalActions.lockDashboardEditing('test'), {
            type: originalActions.LOCK_DASHBOARD_EDITING,
            id: 'test'
        })
    })
    
    describe('unlockDashboardEditing', () => {
        assert.deepStrictEqual(originalActions.unlockDashboardEditing('test'), {
            type: originalActions.UNLOCK_DASHBOARD_EDITING,
            id: 'test'
        })
    })
})