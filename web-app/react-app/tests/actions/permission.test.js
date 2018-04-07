import assert from 'assert-diff'
import thunk from 'redux-thunk'
import configureMockStore from 'redux-mock-store'
import moxios from 'moxios'
import sinon from 'sinon'

import * as helpers from '../../helpers/createLink'

import * as actions from '../../actions/permission'

const middlewares = [thunk]
const mockStore = configureMockStore(middlewares)

sinon.stub(helpers, 'default')
    .callsFake((uri) => uri.replace(/^\//, ''))

describe('Permission actions', () => {
    let store

    beforeEach(() => {
        moxios.install()
        store = mockStore({
            permission: {
                byTypeAndId: {},
                error: null,
                fetching: false
            }
        })
    })

    afterEach(() => {
        moxios.uninstall()
        store.clearActions()
    })

    describe('getApiUrl (tested indirectly)', () => {
        it('user correct url for dashboard', async done => {
            const id = 'afasdfasdfasgsdfg'
            store.dispatch(actions.getResourcePermissions('DASHBOARD', id))
            await moxios.promiseWait()
            const request = moxios.requests.mostRecent()
            assert(request.url.match(`dashboards/${id}/permissions`))
            done()
            request.respondWith({
                status: 200
            })
        })
        it('user correct url for canvas', async done => {
            const id = 'afasdfasdfasgsdfg'
            store.dispatch(actions.getResourcePermissions('CANVAS', id))
            await moxios.promiseWait()
            const request = moxios.requests.mostRecent()
            assert(request.url.match(`canvases/${id}/permissions`))
            done()
            request.respondWith({
                status: 200
            })
        })
        it('user correct url for stream', async done => {
            const id = 'afasdfasdfasgsdfg'
            store.dispatch(actions.getResourcePermissions('STREAM', id))
            await moxios.promiseWait()
            let request = moxios.requests.mostRecent()
            assert(request.url.match(`streams/${id}/permissions`))
            done()
            request.respondWith({
                status: 200
            })
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
            moxios.stubRequest(`api/v1/dashboards/${resourceId}/permissions`, {
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
        it('creates GET_RESOURCE_PERMISSIONS_FAILURE with the error when fetching permissions failed', async (done) => {
            const resourceType = 'DASHBOARD'
            const resourceId = 'asdfasdfasasd'
            moxios.stubRequest(`api/v1/dashboards/${resourceId}/permissions`, {
                status: 500,
                response: {
                    message: 'test',
                    code: 'TEST'
                }
            })

            const expectedActions = [{
                type: actions.GET_RESOURCE_PERMISSIONS_REQUEST,
            }, {
                type: actions.GET_RESOURCE_PERMISSIONS_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }]

            try {
                await store.dispatch(actions.getResourcePermissions(resourceType, resourceId))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0,2), expectedActions)
                assert.equal(store.getActions()[2].title, 'Error')
                done()
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
            it('should call SAVE on every new permission', async done => {
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
                store = mockStore({
                    permission: {
                        byTypeAndId: {
                            [resourceType]: {
                                [resourceId]: permissions
                            }
                        }
                    }
                })
                store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId))
                await moxios.promiseWait()
                const requests = moxios.requests
                assert.equal(requests.at(0).config.method, 'post')
                assert.equal(requests.at(1).config.method, 'post')
                assert.deepStrictEqual(JSON.parse(requests.at(0).config.data), permissions[0])
                assert.deepStrictEqual(JSON.parse(requests.at(1).config.data), permissions[1])
                done()
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
                store = mockStore({
                    permission: {
                        byTypeAndId: {
                            [resourceType]: {
                                [resourceId]: permissions
                            }
                        }
                    }
                })
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

                await store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId))
                assert.deepStrictEqual(store.getActions().slice(0, 4), expectedActions)
            })
            it('should create SAVE_ADDED_RESOURCE_PERMISSION_FAILURE for failed permissions', async (done) => {
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

                store = mockStore({
                    permission: {
                        byTypeAndId: {
                            [resourceType]: {
                                [resourceId]: permissions
                            }
                        }
                    }
                })

                moxios.wait(() => {
                    const requests = moxios.requests
                    requests.at(0).respondWith({
                        status: 500,
                        response: {
                            message: 'test',
                            code: 'TEST'
                        }
                    })
                    requests.at(1).respondWith({
                        status: 500,
                        response: {
                            message: 'test2',
                            code: 'TEST2'
                        }
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
                        error: {
                            message: 'test',
                            code: 'TEST',
                            statusCode: 500
                        }
                    }
                }, {
                    type: actions.SAVE_ADDED_RESOURCE_PERMISSION_FAILURE,
                    resourceType,
                    resourceId,
                    permission: {
                        ...permissions[1],
                        error: {
                            message: 'test2',
                            code: 'TEST2',
                            statusCode: 500
                        }
                    }
                }]

                try {
                    await store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId))
                } catch (e) {
                    assert.deepStrictEqual(store.getActions().slice(0, 4), expectedActions)
                    done()
                }
            })
        })
        describe('removed permissions', () => {
            it('should call DELETE on every removed permissions', async () => {
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

                store = mockStore({
                    permission: {
                        byTypeAndId: {
                            [resourceType]: {
                                [resourceId]: permissions
                            }
                        }
                    }
                })

                store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId))
                await moxios.promiseWait()
                const requests = moxios.requests
                assert.equal(requests.at(0).config.method, 'delete')
                assert.equal(requests.at(1).config.method, 'delete')
                assert.equal(requests.at(0).url, `api/v1/dashboards/${resourceId}/permissions/2`)
                assert.equal(requests.at(1).url, `api/v1/dashboards/${resourceId}/permissions/3`)
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

                store = mockStore({
                    permission: {
                        byTypeAndId: {
                            [resourceType]: {
                                [resourceId]: permissions
                            }
                        }
                    }
                })

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

                await store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId))
                assert.deepStrictEqual(store.getActions().slice(0, 4), expectedActions)
            })
            it('should create SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE for failed permissions', async (done) => {
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

                store = mockStore({
                    permission: {
                        byTypeAndId: {
                            [resourceType]: {
                                [resourceId]: permissions
                            }
                        }
                    }
                })

                moxios.wait(() => {
                    const requests = moxios.requests
                    requests.at(0).respondWith({
                        status: 500,
                        response: {
                            message: 'test',
                            code: 'TEST'
                        }
                    })
                    requests.at(1).respondWith({
                        status: 500,
                        response: {
                            message: 'test2',
                            code: 'TEST2'
                        }
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
                        error: {
                            message: 'test',
                            code: 'TEST',
                            statusCode: 500
                        }
                    }
                }, {
                    type: actions.SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE,
                    resourceType,
                    resourceId,
                    permission: {
                        ...permissions[2],
                        error: {
                            message: 'test2',
                            code: 'TEST2',
                            statusCode: 500
                        }
                    }
                }]

                try {
                    await store.dispatch(actions.saveUpdatedResourcePermissions(resourceType, resourceId))
                } catch (e) {
                    assert.deepStrictEqual(store.getActions().slice(0, 4), expectedActions)
                    done()
                }
            })
        })
    })

    describe('removeAllResourcePermissionsByUser', () => {
        it('should call REMOVE on every one of the user\'s permissions', () => {
            const resourceType = 'DASHBOARD'
            const resourceId = 'asdfasdfasasd'
            const user = 'user2'
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
                user: user,
                operation: 'read',
                new: false
            }, {
                id: '4',
                user: user,
                operation: 'write',
                new: false
            }]
            store = mockStore({
                permission: {
                    byTypeAndId: {
                        [resourceType]: {
                            [resourceId]: permissions
                        }
                    }
                }
            })
            const expectedActions = [{
                type: actions.REMOVE_RESOURCE_PERMISSION,
                resourceType,
                resourceId,
                permission: {
                    user,
                    operation: 'read'
                }
            }, {
                type: actions.REMOVE_RESOURCE_PERMISSION,
                resourceType,
                resourceId,
                permission: {
                    user,
                    operation: 'write'
                }
            }, {
                type: actions.REMOVE_RESOURCE_PERMISSION,
                resourceType,
                resourceId,
                permission: {
                    user,
                    operation: 'share'
                }
            }]

            store.dispatch(actions.removeAllResourcePermissionsByUser(resourceType, resourceId, user))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
    })

    describe('setResourceHighestOperationForUser', () => {
        const resourceType = 'DASHBOARD'
        const resourceId = 'asdfasdfasasd'
        const user = 'test'
        describe('giving higher operation adds permissions', () => {
            it('must add read, write and share if called with share', () => {
                const permissions = [{
                    user,
                    operation: 'read'
                }, {
                    user,
                    operation: 'write'
                }, {
                    user,
                    operation: 'share'
                }]
                store = mockStore({
                    permission: {
                        byTypeAndId: {
                            [resourceType]: {
                                [resourceId]: []
                            }
                        }
                    }
                })
                const expectedActions = permissions.map(permission => ({
                    type: actions.ADD_RESOURCE_PERMISSION,
                    resourceType,
                    resourceId,
                    permission
                }))
                store.dispatch(actions.setResourceHighestOperationForUser(resourceType, resourceId, user, 'share'))
                assert.deepStrictEqual(store.getActions(), expectedActions)
            })
        })
        describe('giving lower operation removes permissions', () => {
            it('must add read, write and share if called with share', () => {
                const currentPermissions = [{
                    user,
                    operation: 'read'
                }, {
                    user,
                    operation: 'write'
                }, {
                    user,
                    operation: 'share'
                }]
                store = mockStore({
                    permission: {
                        byTypeAndId: {
                            [resourceType]: {
                                [resourceId]: currentPermissions
                            }
                        }
                    }
                })
                const expectedActions = currentPermissions.slice(1, 3).map(permission => ({
                    type: actions.REMOVE_RESOURCE_PERMISSION,
                    resourceType,
                    resourceId,
                    permission
                }))
                store.dispatch(actions.setResourceHighestOperationForUser(resourceType, resourceId, user, 'read'))
                assert.deepStrictEqual(store.getActions(), expectedActions)
            })
        })
    })
})