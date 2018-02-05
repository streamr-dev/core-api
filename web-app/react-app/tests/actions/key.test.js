import assert from 'assert-diff'
import thunk from 'redux-thunk'
import configureMockStore from 'redux-mock-store'
import moxios from 'moxios'
import sinon from 'sinon'

import * as helpers from '../../helpers/createLink'

import * as actions from '../../actions/key'

const middlewares = [thunk]
const mockStore = configureMockStore(middlewares)

sinon.stub(helpers, 'default')
    .callsFake((uri) => uri.replace(/^\//, ''))

moxios.promiseWait = () => new Promise(resolve => moxios.wait(resolve))

describe('Key actions', () => {
    let store

    beforeEach(() => {
        moxios.install()
        store = mockStore({
            key: {
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
        it('use correct url for stream', async done => {
            const resourceId = 'afasdfasdfasgsdfg'
            store.dispatch(actions.getResourceKeys('STREAM', resourceId))
            await moxios.promiseWait()
            const request = moxios.requests.mostRecent()
            assert(request.url.match(`streams/${resourceId}/keys`))
            done()
            request.respondWith({
                status: 200
            })
        })
        it('use correct url for stream with id', async done => {
            const resourceId = 'afasdfasdfasgsdfg'
            const keyId = 'fdfdasdfa'
            store.dispatch(actions.removeResourceKey('STREAM', resourceId, keyId))
            await moxios.promiseWait()
            const request = moxios.requests.mostRecent()
            assert(request.url.match(`streams/${resourceId}/keys/${keyId}`))
            done()
            request.respondWith({
                status: 200
            })
        })
        it('use correct url for stream', async done => {
            const resourceId = 'afasdfasdfasgsdfg'
            store.dispatch(actions.getResourceKeys('USER', resourceId))
            await moxios.promiseWait()
            const request = moxios.requests.mostRecent()
            assert(request.url.match(`users/${resourceId}/keys`))
            done()
            request.respondWith({
                status: 200
            })
        })
        it('use correct url for stream with id', async done => {
            const resourceId = 'afasdfasdfasgsdfg'
            const keyId = 'me'
            store.dispatch(actions.removeResourceKey('USER', resourceId, keyId))
            await moxios.promiseWait()
            const request = moxios.requests.mostRecent()
            assert(request.url.match(`users/${resourceId}/keys/${keyId}`))
            done()
            request.respondWith({
                status: 200
            })
        })
    })

    describe('getResourceKeys', () => {
        it('creates GET_RESOURCE_KEYS_SUCCESS when fetching resources succeeded', async () => {
            const resourceType = 'STREAM'
            const resourceId = 'testId'
            const keys = [{
                id: 'test',
                name: 'test'
            }]
            moxios.stubRequest(`api/v1/streams/${resourceId}/keys`, {
                status: 200,
                response: keys
            })

            const expectedActions = [{
                type: actions.GET_RESOURCE_KEYS_REQUEST,
            }, {
                type: actions.GET_RESOURCE_KEYS_SUCCESS,
                resourceType,
                resourceId,
                keys
            }]

            await store.dispatch(actions.getResourceKeys(resourceType, resourceId))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates GET_RESOURCE_PERMISSIONS_FAILURE with the error when fetching keys failed', async (done) => {
            const resourceType = 'STREAM'
            const resourceId = 'testId'
            moxios.stubRequest(`api/v1/streams/${resourceId}/keys`, {
                status: 500,
                response: {
                    error: 'test',
                    code: 'TEST'
                }
            })

            const expectedActions = [{
                type: actions.GET_RESOURCE_KEYS_REQUEST,
            }, {
                type: actions.GET_RESOURCE_KEYS_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }]

            try {
                await store.dispatch(actions.getResourceKeys(resourceType, resourceId))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0,2), expectedActions)
                assert.equal(store.getActions()[2].level, 'error')
                done()
            }
        })
    })

    describe('addResourceKey', () => {
        it('creates ADD_RESOURCE_KEY_SUCCESS for succeeded key addition', async () => {
            const resourceType = 'STREAM'
            const resourceId = 'testId'
            const key = [{
                id: '1',
                name: 'test'
            }]
            moxios.wait(() => {
                const request = moxios.requests.at(0)
                assert.equal(request.config.method, 'post')
                request.respondWith({
                    status: 200,
                    response: key
                })
            })

            const expectedActions = [{
                type: actions.ADD_RESOURCE_KEY_REQUEST
            }, {
                type: actions.ADD_RESOURCE_KEY_SUCCESS,
                resourceType,
                resourceId,
                key
            }]

            await store.dispatch(actions.addResourceKey(resourceType, resourceId, key))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates ADD_RESOURCE_KEY_FAILURE for failed at key addition', async (done) => {
            const resourceType = 'STREAM'
            const resourceId = 'testId'
            const key = [{
                id: '1',
                name: 'test'
            }]
            moxios.wait(() => {
                const request = moxios.requests.at(0)
                assert.equal(request.config.method, 'post')
                request.respondWith({
                    status: 500,
                    response: {
                        error: 'test',
                        code: 'TEST'
                    }
                })
            })

            const expectedActions = [{
                type: actions.ADD_RESOURCE_KEY_REQUEST
            }, {
                type: actions.ADD_RESOURCE_KEY_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }]

            try {
                await store.dispatch(actions.addResourceKey(resourceType, resourceId, key))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
                assert.equal(store.getActions()[2].level, 'error')
                done()
            }
        })
    })

    describe('removeResourceKey', () => {
        it('creates REMOVE_RESOURCE_KEY_SUCCESS for succeeded at key removal', async () => {
            const resourceType = 'STREAM'
            const resourceId = 'testId'
            const keyId = '1'
            moxios.wait(() => {
                const request = moxios.requests.at(0)
                assert.equal(request.config.method, 'delete')
                request.respondWith({
                    status: 200
                })
            })

            const expectedActions = [{
                type: actions.REMOVE_RESOURCE_KEY_REQUEST
            }, {
                type: actions.REMOVE_RESOURCE_KEY_SUCCESS,
                resourceType,
                resourceId,
                keyId
            }]

            await store.dispatch(actions.removeResourceKey(resourceType, resourceId, keyId))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates REMOVE_RESOURCE_KEY_FAILURE for failed key removal', async (done) => {
            const resourceType = 'STREAM'
            const resourceId = 'testId'
            const keyId = '1'
            moxios.wait(() => {
                const request = moxios.requests.at(0)
                assert.equal(request.config.method, 'delete')
                request.respondWith({
                    status: 500,
                    response: {
                        error: 'test',
                        code: 'TEST'
                    }
                })
            })

            const expectedActions = [{
                type: actions.REMOVE_RESOURCE_KEY_REQUEST
            }, {
                type: actions.REMOVE_RESOURCE_KEY_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }]

            try {
                await store.dispatch(actions.removeResourceKey(resourceType, resourceId, keyId))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
                assert.equal(store.getActions()[2].level, 'error')
                done()
            }
        })
    })
})