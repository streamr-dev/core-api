
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import * as actions from '../../actions/integrationKey'
import assert from 'assert-diff'
import moxios from 'moxios'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

global.Streamr = {
    createLink: ({uri}) => uri
}

describe('IntegrationKey actions', () => {
    let store
    
    beforeEach(() => {
        moxios.install()
        store = mockStore({
            integrationKeys: [],
            error: null,
            fetching: false
        })
    })
    
    afterEach(() => {
        moxios.uninstall()
        store.clearActions()
    })
    
    describe('getAndReplaceIntegrationKeys', () => {
        it('creates GET_ALL_INTEGRATION_KEYS_SUCCESS when fetching integrationKeys has succeeded', async () => {
            moxios.stubRequest('api/v1/integrationkeys', {
                status: 200,
                response: [{
                    name: 'test',
                    json: '{"moi": "moimoi"}'
                },{
                    name: 'test2',
                    json: '{"moitaas": "aihei"}'
                }]
            })
        
            const expectedActions = [{
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST
            }, {
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS,
                integrationKeys: [{
                    name: 'test',
                    json: '{"moi": "moimoi"}'
                },{
                    name: 'test2',
                    json: '{"moitaas": "aihei"}'
                }]
            }]
        
            await store.dispatch(actions.getAndReplaceIntegrationKeys())
            assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
        })
    
        it('creates GET_ALL_INTEGRATION_KEYS_FAILURE when fetching integration keys has failed', async () => {
            moxios.stubRequest('api/v1/integrationkeys', {
                status: 500,
                response: new Error('test-error')
            })
        
            const expectedActions = [{
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST
            }, {
                type: actions.GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE,
                error: new Error('test-error')
            }]
        
            try {
                await store.dispatch(actions.getAndReplaceIntegrationKeys())
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
            }
        })
    })
    
    describe('createIntegrationKey', () => {
        it('creates CREATE_INTEGRATION_KEY_SUCCESS when creating integration key has succeeded', async () => {
            moxios.wait(() => {
                const request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'post')
                request.respondWith({
                    status: 200,
                    response: request.config.data
                })
            })
        
            const expectedActions = [{
                type: actions.CREATE_INTEGRATION_KEY_REQUEST
            }, {
                type: actions.CREATE_INTEGRATION_KEY_SUCCESS,
                integrationKey: {
                    name: 'test',
                    json: 'moi'
                }
            }]
        
            await store.dispatch(actions.createIntegrationKey({
                name: 'test',
                json: 'moi'
            }))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
    
        it('creates CREATE_INTEGRATION_KEY_FAILURE when creating integration key has failed', async () => {
            moxios.wait(() => {
                const request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'post')
                request.respondWith({
                    status: 500,
                    response: new Error('test')
                })
            })
        
            const expectedActions = [{
                type: actions.CREATE_INTEGRATION_KEY_REQUEST
            }, {
                type: actions.CREATE_INTEGRATION_KEY_FAILURE,
                error: new Error('test')
            }]
        
            try {
                await store.dispatch(actions.createIntegrationKey({
                    name: 'test',
                    json: 'moi'
                }))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
            }
        })
    })
    
    describe('deleteIntegrationKey', () => {
        it('creates DELETE_INTEGRATION_KEY_SUCCESS when deleting integration key has succeeded', async () => {
            moxios.wait(() => {
                const request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'delete')
                request.respondWith({
                    status: 200
                })
            })
        
            const expectedActions = [{
                type: actions.DELETE_INTEGRATION_KEY_REQUEST,
                id: 'test'
            }, {
                type: actions.DELETE_INTEGRATION_KEY_SUCCESS,
                id: 'test'
            }]
        
            await store.dispatch(actions.deleteIntegrationKey('test'))
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
    
        it('creates DELETE_INTEGRATION_KEY_FAILURE when deleting integration key has failed', async () => {
            moxios.wait(() => {
                const request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'delete')
                request.respondWith({
                    status: 500,
                    response: new Error('test')
                })
            })
        
            const expectedActions = [{
                type: actions.DELETE_INTEGRATION_KEY_REQUEST,
                id: 'test'
            }, {
                type: actions.DELETE_INTEGRATION_KEY_FAILURE,
                error: new Error('test')
            }]
        
            try {
                await store.dispatch(actions.deleteIntegrationKey('test'))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
            }
        })
    })
})