
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import * as actions from '../../actions/integrationKeys'
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
    
    it('creates GET_ALL_ACCOUNTS_SUCCESS when fetching integrationKeys has succeeded', () => {
        moxios.stubRequest('api/v1/integrationKeys', {
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
            type: actions.GET_AND_REPLACE_ACCOUNTS_REQUEST
        }, {
            type: actions.GET_AND_REPLACE_ACCOUNTS_SUCCESS,
            integrationKeys: [{
                name: 'test',
                json: '{"moi": "moimoi"}'
            },{
                name: 'test2',
                json: '{"moitaas": "aihei"}'
            }]
        }]
        
        return store.dispatch(actions.getAndReplaceAccounts())
            .then(() => {
                expect(store.getActions()).toEqual(expectedActions)
            })
    })
    
    it('creates GET_ALL_ACCOUNTS_FAILURE when fetching accounts has failed', () => {
        moxios.stubRequest('api/v1/accounts', {
            status: 500,
            response: new Error('test-error')
        })
        
        const expectedActions = [{
            type: actions.GET_AND_REPLACE_ACCOUNTS_REQUEST
        }, {
            type: actions.GET_AND_REPLACE_ACCOUNTS_FAILURE,
            error: 'test-error'
        }]
        
        return store.dispatch(actions.getAndReplaceAccounts())
            .catch(() => {
                expect(store.getActions()).toEqual(expectedActions)
            })
    })
    
    it('creates CREATE_ACCOUNT_SUCCESS when creating account has succeeded', () => {
        moxios.wait(() => {
            let request = moxios.requests.mostRecent()
            expect(request.config.method).toEqual('post')
            request.respondWith({
                status: 200,
                response: request.config.data
            })
        })
        
        const expectedActions = [{
            type: actions.CREATE_ACCOUNT_REQUEST
        }, {
            type: actions.CREATE_ACCOUNT_SUCCESS,
            account: {
                name: 'test',
                json: 'moi'
            }
        }]
        
        return store.dispatch(actions.createAccount({
            name: 'test',
            json: 'moi'
        }))
            .then(() => {
                expect(store.getActions()).toEqual(expectedActions)
            })
    })
    
    it('creates CREATE_ACCOUNT_FAILURE when creating account has failed', () => {
        moxios.wait(() => {
            let request = moxios.requests.mostRecent()
            expect(request.config.method).toEqual('post')
            request.respondWith({
                status: 500,
                response: new Error('test')
            })
        })
        
        const expectedActions = [{
            type: actions.CREATE_ACCOUNT_REQUEST
        }, {
            type: actions.CREATE_ACCOUNT_FAILURE,
            error: 'test'
        }]
        
        return store.dispatch(actions.createAccount({
            name: 'test',
            json: 'moi'
        }))
            .catch(() => {
                expect(store.getActions()).toEqual(expectedActions)
            })
    })
    
    it('creates DELETE_ACCOUNT_SUCCESS when deleting account has succeeded', () => {
        moxios.wait(() => {
            let request = moxios.requests.mostRecent()
            expect(request.config.method).toEqual('delete')
            request.respondWith({
                status: 200
            })
        })
        
        const expectedActions = [{
            type: actions.DELETE_ACCOUNT_REQUEST,
            id: 'test'
        }, {
            type: actions.DELETE_ACCOUNT_SUCCESS,
            id: 'test'
        }]
        
        return store.dispatch(actions.deleteAccount('test'))
            .then(() => {
                expect(store.getActions()).toEqual(expectedActions)
            })
    })
    
    it('creates DELETE_ACCOUNT_FAILURE when deleting account has failed', () => {
        moxios.wait(() => {
            let request = moxios.requests.mostRecent()
            expect(request.config.method).toEqual('delete')
            request.respondWith({
                status: 500,
                response: new Error('test')
            })
        })
        
        const expectedActions = [{
            type: actions.DELETE_ACCOUNT_REQUEST,
            id: 'test'
        }, {
            type: actions.DELETE_ACCOUNT_FAILURE,
            error: 'test'
        }]
        
        return store.dispatch(actions.deleteAccount('test'))
            .catch(() => {
                expect(store.getActions()).toEqual(expectedActions)
            })
    })
})