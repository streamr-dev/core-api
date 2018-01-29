
import configureMockStore from 'redux-mock-store'
import thunk from 'redux-thunk'
import * as actions from '../../actions/canvas'
import assert from 'assert-diff'
import moxios from 'moxios'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

global.Streamr = {
    createLink: ({uri}) => uri
}

describe('Canvas actions', () => {
    let store
    
    beforeEach(() => {
        moxios.install()
        store = mockStore({
            list: [],
            error: null,
            fetching: false
        })
    })
    
    afterEach(() => {
        moxios.uninstall()
        store.clearActions()
    })
    
    it('creates GET_RUNNING_CANVASES_SUCCESS when fetching running canvases has succeeded', async () => {
        moxios.wait(() => {
            const request = moxios.requests.mostRecent()
            expect(request.url).toMatch(/api\/v1\/canvases/)
            expect(request.config.params).toEqual({
                state: 'running',
                adhoc: false,
                sort: 'dateCreated',
                order: 'desc'
            })
            request.respondWith({
                status: 200,
                response: [{
                    id: 'test',
                    name: 'test'
                },{
                    id: 'test2',
                    name: 'test2'
                }]
            })
        })
        
        const expectedActions = [{
            type: actions.GET_RUNNING_CANVASES_REQUEST
        }, {
            type: actions.GET_RUNNING_CANVASES_SUCCESS,
            canvases: [{
                id: 'test',
                name: 'test'
            },{
                id: 'test2',
                name: 'test2'
            }]
        }]
        
        await store.dispatch(actions.getRunningCanvases())
        assert.deepStrictEqual(store.getActions(), expectedActions)
    })
    
    it('creates GET_RUNNING_CANVASES_FAILURE when fetching running canvases has failed', async () => {
        moxios.wait(() => {
            const request = moxios.requests.mostRecent()
            expect(request.url).toMatch(/api\/v1\/canvases/)
            expect(request.config.params).toEqual({
                state: 'running',
                adhoc: false,
                sort: 'dateCreated',
                order: 'desc'
            })
            request.respondWith({
                status: 500,
                response: {
                    message: 'test',
                    code: 'TEST'
                }
            })
        })
        
        const expectedActions = [{
            type: actions.GET_RUNNING_CANVASES_REQUEST
        }, {
            type: actions.GET_RUNNING_CANVASES_FAILURE,
            error: {
                message: 'test',
                code: 'TEST',
                statusCode: 500
            }
        }]
        
        try {
            await store.dispatch(actions.getRunningCanvases())
        } catch (e) {
            assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions.slice(0, 2))
        }
    })
})