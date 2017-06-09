
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import * as actions from '../../actions/integrationKey'
import expect from 'expect'
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
        it('creates GET_ALL_INTEGRATION_KEYS_SUCCESS when fetching integrationKeys has succeeded', () => {
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
        
            return store.dispatch(actions.getAndReplaceIntegrationKeys())
                .then(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    
        it('creates GET_ALL_INTEGRATION_KEYS_FAILURE when fetching integration keys has failed', () => {
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
        
            return store.dispatch(actions.getAndReplaceIntegrationKeys())
                .catch(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    })
    
    describe('createIntegrationKey', () => {
        it('creates CREATE_INTEGRATION_KEY_SUCCESS when creating integration key has succeeded', () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                expect(request.config.method).toEqual('post')
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
        
            return store.dispatch(actions.createIntegrationKey({
                name: 'test',
                json: 'moi'
            }))
                .then(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    
        it('creates CREATE_INTEGRATION_KEY_FAILURE when creating integration key has failed', () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                expect(request.config.method).toEqual('post')
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
        
            return store.dispatch(actions.createIntegrationKey({
                name: 'test',
                json: 'moi'
            }))
                .catch(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    })
    
    describe('deleteIntegrationKey', () => {
        it('creates DELETE_INTEGRATION_KEY_SUCCESS when deleting integration key has succeeded', () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                expect(request.config.method).toEqual('delete')
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
        
            return store.dispatch(actions.deleteIntegrationKey('test'))
                .then(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    
        it('creates DELETE_INTEGRATION_KEY_FAILURE when deleting integration key has failed', () => {
            moxios.wait(() => {
                let request = moxios.requests.mostRecent()
                expect(request.config.method).toEqual('delete')
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
        
            return store.dispatch(actions.deleteIntegrationKey('test'))
                .catch(() => {
                    expect(store.getActions()).toEqual(expectedActions)
                })
        })
    })
})