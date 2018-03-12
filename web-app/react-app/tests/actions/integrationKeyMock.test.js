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

jest.mock('../../utils/web3Instance')

describe('IntegrationKey actions with web3.js mocked', () => {
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

    describe('createIdentity', () => {
        it('creates CREATE_IDENTITY_FAILURE when creating challenge has failed', async (done) => {
            moxios.wait(() => {
                const request = moxios.requests.mostRecent()
                assert.equal(request.config.method, 'post')
                request.respondWith({
                    status: 500,
                    response: {
                        message: 'test',
                        code: 'TEST'
                    }
                })
            })

            const expectedActions = [{
                type: actions.CREATE_IDENTITY_REQUEST,
                integrationKey: {
                    name: 'test'
                },
            },
            {
                type: actions.CREATE_IDENTITY_FAILURE,
                error: {
                    message: 'test',
                    code: 'TEST',
                    statusCode: 500
                }
            }]

            try {
                await store.dispatch(actions.createIdentity({
                    name: 'test',
                }))
            } catch (e) {
                assert.deepStrictEqual(store.getActions(), expectedActions)
                done()
            }
        })
    })
})
