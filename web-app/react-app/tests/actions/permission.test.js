
import assert from 'assert-diff'
import thunk from 'redux-thunk'
import configureMockStore from 'redux-mock-store'
import moxios from 'moxios'

import * as actions from '../../actions/permission'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

global.Streamr = {
    createLink: ({uri}) => uri
}

describe('Permission actions', () => {
    let store

    beforeEach(() => {
        moxios.install()
        store = mockStore({
            byTypeAndId: {},
            error: null,
            fetching: false
        })
    })
    
    afterEach(() => {
        moxios.uninstall()
        store.clearActions()
    })
    
    describe('getApiUrl (tested indirectly)', () => {
        it('user correct url for dashboard', done => {
            const id = 'afasdfasdfasgsdfg'
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert(request.url.match(`dashboards/${id}/permissions`))
                done()
                request.respondWith({
                    status: 200
                })
            })
            store.dispatch(actions.getResourcePermissions('DASHBOARD', id))
        })
        it('user correct url for canvas', done => {
            const id = 'afasdfasdfasgsdfg'
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert(request.url.match(`canvases/${id}/permissions`))
                done()
                request.respondWith({
                    status: 200
                })
            })
            store.dispatch(actions.getResourcePermissions('CANVAS', id))
        })
        it('user correct url for stream', done => {
            const id = 'afasdfasdfasgsdfg'
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                assert(request.url.match(`streams/${id}/permissions`))
                done()
                request.respondWith({
                    status: 200
                })
            })
            store.dispatch(actions.getResourcePermissions('STREAM', id))
        })
    })
    
    describe('getResourcePermissions', () => {
        it('creates GET_RESOURCE_PERMISSIONS_SUCCESS when fetching resources succeeded', async () => {
            const resourceType = 'DASHBOARD'
            const resourceId = 'asdfasdfasasd'
            const permissions = [{
                user: 'test',
                operation: 'test'
            }]
            moxios.stubRequest(`/api/v1/dashboards/${resourceId}/permissions`, {
                status: 200,
                response: permissions
            })
        
            const expectedActions = [{
                type: actions.GET_RESOURCE_PERMISSIONS_REQUEST,
            }, {
                type: actions.GET_RESOURCE_PERMISSIONS_SUCCESS,
                resourceType,
                resourceId,
                permissions
            }]
        
            await store.dispatch(actions.getResourcePermissions(resourceType, resourceId))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates GET_RESOURCE_PERMISSIONS_FAILURE with the error when fetching permissions failed', async () => {
            const resourceType = 'DASHBOARD'
            const resourceId = 'asdfasdfasasd'
            moxios.stubRequest(`/api/v1/dashboards/${resourceId}/permissions`, {
                status: 500,
                response: new Error('test')
            })
        
            const expectedActions = [{
                type: actions.GET_RESOURCE_PERMISSIONS_REQUEST,
            }, {
                type: actions.GET_RESOURCE_PERMISSIONS_FAILURE,
                error: new Error('test')
            }]
        
            try {
                await store.dispatch(actions.getResourcePermissions(resourceType, resourceId))
            } catch (e) {
                assert.deepStrictEqual(store.getActions(), expectedActions)
            }
        })
    })
    
    describe('addResourcePermission', () => {
        it('creates ADD_RESOURCE_PERMISSION', () => {
            const resourceType = 'DASHBOARD'
            const resourceId = 'asdfasdfasasd'
            const permission = {
                user: 'test',
                operation: 'test'
            }
            assert.deepEqual(actions.addResourcePermission(resourceType, resourceId, permission), {
                type: actions.ADD_RESOURCE_PERMISSION,
                resourceType,
                resourceId,
                permission
            })
        })
    })
    
    describe('removeResourcePermission', () => {
        it('creates REMOVE_RESOURCE_PERMISSION', () => {
            const resourceType = 'DASHBOARD'
            const resourceId = 'asdfasdfasasd'
            const permission = {
                id: 'test'
            }
            assert.deepEqual(actions.removeResourcePermission(resourceType, resourceId, permission), {
                type: actions.REMOVE_RESOURCE_PERMISSION,
                resourceType,
                resourceId,
                permission
            })
        })
    })
    
    describe('saveUpdatedResourcePermissions', () => {
        describe('new permissions', () => {
            it('should call SAVE on every new permission', done => {
                const resourceType = 'DASHBOARD'
                const resourceId = 'asdfasdfasasd'
                const permissions = [{
                    id: '1',
                    user: 'test',
                    operation: 'test',
                    new: true
                }, {
                    id: '2',
                    user: 'test',
                    operation: 'test2',
                    new: true
                }, {
                    id: '3',
                    user: 'test',
                    operation: 'test3',
                    new: false
                }]
                moxios.wait(() => {
                    const requests = moxios.requests
                    assert.equal(requests.at(0).config.method, 'post')
                    assert.equal(requests.at(1).config.method, 'post')
                    assert.deepStrictEqual(JSON.parse(requests.at(0).config.data), permissions[0])
                    assert.deepStrictEqual(JSON.parse(requests.at(1).config.data), permissions[1])
                    done()
                })
    
                store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId, permissions))
            })
            it('should create SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS for succeeded permissions', async () => {
                const resourceType = 'DASHBOARD'
                const resourceId = 'asdfasdfasasd'
                const permissions = [{
                    id: '1',
                    user: 'test',
                    operation: 'test',
                    new: true
                }, {
                    id: '2',
                    user: 'test',
                    operation: 'test2',
                    new: true
                }, {
                    id: '3',
                    user: 'test',
                    operation: 'test3',
                    new: false
                }]
                moxios.wait(() => {
                    const requests = moxios.requests
                    requests.at(0).respondWith({
                        status: 200,
                        response: permissions[0]
                    })
                    requests.at(1).respondWith({
                        status: 200,
                        response: permissions[1]
                    })
                })
    
                const expectedActions = [{
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_REQUEST,
                    resourceType,
                    resourceId,
                    permission: permissions[0]
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_REQUEST,
                    resourceType,
                    resourceId,
                    permission: permissions[1]
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS,
                    resourceType,
                    resourceId,
                    permission: permissions[0]
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS,
                    resourceType,
                    resourceId,
                    permission: permissions[1]
                }]
    
                await store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId, permissions))
                assert.deepStrictEqual(store.getActions(), expectedActions)
            })
            it('should create SAVE_ADDED_RESOURCE_PERMISSION_FAILURE for failed permissions', async () => {
                const resourceType = 'DASHBOARD'
                const resourceId = 'asdfasdfasasd'
                const permissions = [{
                    id: '1',
                    user: 'test',
                    operation: 'test',
                    new: true
                }, {
                    id: '2',
                    user: 'test',
                    operation: 'test2',
                    new: true
                }, {
                    id: '3',
                    user: 'test',
                    operation: 'test3',
                    new: false
                }]
                moxios.wait(() => {
                    const requests = moxios.requests
                    requests.at(0).respondWith({
                        status: 500,
                        response: new Error()
                    })
                    requests.at(1).respondWith({
                        status: 500,
                        response: new Error()
                    })
                })
    
                const expectedActions = [{
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_REQUEST,
                    resourceType,
                    resourceId,
                    permission: permissions[0]
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_REQUEST,
                    resourceType,
                    resourceId,
                    permission: permissions[1]
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_FAILURE,
                    resourceType,
                    resourceId,
                    permission: {
                        ...permissions[0],
                        error: new Error()
                    }
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_FAILURE,
                    resourceType,
                    resourceId,
                    permission: {
                        ...permissions[1],
                        error: new Error()
                    }
                }]
    
                try {
                    await store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId, permissions))
                } catch (e) {
                    assert.deepStrictEqual(store.getActions().slice(0,4), expectedActions)
                }
            })
        })
        describe('removed permissions', () => {
            it('should call DELETE on every removed permissions', done => {
                const resourceType = 'DASHBOARD'
                const resourceId = 'asdfasdfasasd'
                const permissions = [{
                    id: '1',
                    user: 'test',
                    operation: 'test'
                }, {
                    id: '2',
                    user: 'test',
                    operation: 'test2',
                    removed: true
                }, {
                    id: '3',
                    user: 'test',
                    operation: 'test3',
                    removed: true
                }]
                moxios.wait(() => {
                    const requests = moxios.requests
                    assert.equal(requests.at(0).config.method, 'delete')
                    assert.equal(requests.at(1).config.method, 'delete')
                    assert.equal(requests.at(0).url, `/api/v1/dashboards/${resourceId}/permissions/2`)
                    assert.equal(requests.at(1).url, `/api/v1/dashboards/${resourceId}/permissions/3`)
                    done()
                })
    
                store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId, permissions))
            })
            it('should create SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS for succeeded permissions', async () => {
                const resourceType = 'DASHBOARD'
                const resourceId = 'asdfasdfasasd'
                const permissions = [{
                    id: '1',
                    user: 'test',
                    operation: 'test'
                }, {
                    id: '2',
                    user: 'test',
                    operation: 'test2',
                    removed: true
                }, {
                    id: '3',
                    user: 'test',
                    operation: 'test3',
                    removed: true
                }]
                moxios.wait(() => {
                    const requests = moxios.requests
                    requests.at(0).respondWith({
                        status: 200
                    })
                    requests.at(1).respondWith({
                        status: 200
                    })
                })
        
                const expectedActions = [{
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST,
                    resourceType,
                    resourceId,
                    permission: permissions[1]
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST,
                    resourceType,
                    resourceId,
                    permission: permissions[2]
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS,
                    resourceType,
                    resourceId,
                    permission: permissions[1]
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS,
                    resourceType,
                    resourceId,
                    permission: permissions[2]
                }]
        
                await store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId, permissions))
                assert.deepStrictEqual(store.getActions(), expectedActions)
            })
            it('should create SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE for failed permissions', async () => {
                const resourceType = 'DASHBOARD'
                const resourceId = 'asdfasdfasasd'
                const permissions = [{
                    id: '1',
                    user: 'test',
                    operation: 'test'
                }, {
                    id: '2',
                    user: 'test',
                    operation: 'test2',
                    removed: true
                }, {
                    id: '3',
                    user: 'test',
                    operation: 'test3',
                    removed: true
                }]
                moxios.wait(() => {
                    const requests = moxios.requests
                    requests.at(0).respondWith({
                        status: 500,
                        response: new Error()
                    })
                    requests.at(1).respondWith({
                        status: 500,
                        response: new Error()
                    })
                })
        
                const expectedActions = [{
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST,
                    resourceType,
                    resourceId,
                    permission: permissions[1]
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST,
                    resourceType,
                    resourceId,
                    permission: permissions[2]
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE,
                    resourceType,
                    resourceId,
                    permission: {
                        ...permissions[1],
                        error: new Error()
                    }
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE,
                    resourceType,
                    resourceId,
                    permission: {
                        ...permissions[2],
                        error: new Error()
                    }
                }]
                
                try {
                    await store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId, permissions))
                } catch (e) {
                    assert.deepStrictEqual(store.getActions().slice(0,4), expectedActions)
                }
            })
        })
    })
})