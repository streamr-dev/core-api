import assert from 'assert-diff'
import thunk from 'redux-thunk'
import configureMockStore from 'redux-mock-store'
import moxios from 'moxios'
import axios from 'axios'
import sinon from 'sinon'

import * as helpers from '../../helpers/createLink'

import * as actions from '../../actions/user'

const middlewares = [thunk]
const mockStore = configureMockStore(middlewares)

sinon.stub(helpers, 'default')
    .callsFake((uri) => uri.replace(/^\//, ''))

moxios.promiseWait = () => new Promise(resolve => moxios.wait(resolve))

describe('User actions', () => {
    let store
    
    beforeEach(() => {
        moxios.install(axios)
        store = mockStore({
            user: {
                currentUser: {},
                error: null,
                fetching: false,
                saved: true
            }
        })
    })
    
    afterEach(() => {
        moxios.uninstall()
        store.clearActions()
    })
    
    describe('getCurrentUser', () => {
        it('creates GET_CURRENT_USER_SUCCESS when fetching resources succeeded', async () => {
            const user = {
                id: 1,
                name: 'tester',
                email: 'test@tester.test'
            }
            moxios.stubRequest('api/v1/users/me', {
                status: 200,
                response: user
            })
            const expectedActions = [{
                type: actions.GET_CURRENT_USER_REQUEST,
            }, {
                type: actions.GET_CURRENT_USER_SUCCESS,
                user
            }]
            
            await store.dispatch(actions.getCurrentUser())
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
        it('creates GET_CURRENT_USER_SUCCESS when fetching resources succeeded', async () => {
            moxios.stubRequest('api/v1/users/me', {
                status: 500,
                response: new Error('test')
            })
            
            const expectedActions = [{
                type: actions.GET_CURRENT_USER_REQUEST,
            }, {
                type: actions.GET_CURRENT_USER_FAILURE,
                error: new Error('test')
            }]
    
            try {
                await store.dispatch(actions.getCurrentUser())
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
                assert.deepStrictEqual(store.getActions()[2].level, 'error')
            }
        })
    })
    
    describe('saveCurrentUser', () => {
        it('should post the user to the api', async () => {
            const user = {
                id: '1',
                name: 'tester',
                email: 'test@tester.test'
            }
            store = mockStore({
                user: {
                    currentUser: user
                }
            })
            store.dispatch(actions.saveCurrentUser(user))
            await moxios.promiseWait()
            const requests = moxios.requests
            assert.equal(requests.at(0).config.method, 'post')
            assert.deepStrictEqual({
                id: requests.at(0).config.data.get('id'),
                name: requests.at(0).config.data.get('name'),
                email: requests.at(0).config.data.get('email')
            }, user)
        })
        it('should post the user to the api as FormData if sendAsForm=true', async () => {
            const user = {
                id: '1',
                name: 'tester',
                email: 'test@tester.test'
            }
            store = mockStore({
                user: {
                    currentUser: user
                }
            })
            store.dispatch(actions.saveCurrentUser(user, true))
            await moxios.promiseWait()
            const requests = moxios.requests
            assert.equal(requests.at(0).config.method, 'post')
            assert.equal(requests.at(0).headers['Content-Type'], 'application/x-www-form-urlencoded')
            assert.deepStrictEqual({
                id: requests.at(0).config.data.get('id'),
                name: requests.at(0).config.data.get('name'),
                email: requests.at(0).config.data.get('email'),
            }, user)
        })
        it('creates SAVE_CURRENT_USER_SUCCESS when saving user succeeded', async () => {
            const user = {
                id: '1',
                name: 'tester',
                email: 'test@tester.test'
            }
            store = mockStore({
                user: {
                    currentUser: user
                }
            })
            moxios.promiseWait().then(() => {
                const requests = moxios.requests
                assert.equal(requests.at(0).config.method, 'post')
                assert.deepStrictEqual({
                    id: requests.at(0).config.data.get('id'),
                    name: requests.at(0).config.data.get('name'),
                    email: requests.at(0).config.data.get('email'),
                }, user)
                requests.at(0).respondWith({
                    status: 200,
                    response: user
                })
            })
            
            const expectedActions = [{
                type: actions.SAVE_CURRENT_USER_REQUEST
            }, {
                type: actions.SAVE_CURRENT_USER_SUCCESS,
                user
            }]
            
            await store.dispatch(actions.saveCurrentUser(user, true))
            assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
            assert.deepStrictEqual(store.getActions()[2].level, 'success')
        })
        it('creates SAVE_CURRENT_USER_FAILURE when saving user failed', async () => {
            const user = {
                id: '1',
                name: 'tester',
                email: 'test@tester.test'
            }
            store = mockStore({
                user: {
                    currentUser: user
                }
            })
            moxios.promiseWait().then(() => {
                const requests = moxios.requests
                assert.equal(requests.at(0).config.method, 'post')
                assert.deepStrictEqual({
                    id: requests.at(0).config.data.get('id'),
                    name: requests.at(0).config.data.get('name'),
                    email: requests.at(0).config.data.get('email'),
                }, user)
                requests.at(0).respondWith({
                    status: 500,
                    response: 'test'
                })
            })
            
            const expectedActions = [{
                type: actions.SAVE_CURRENT_USER_REQUEST
            }, {
                type: actions.SAVE_CURRENT_USER_FAILURE,
                error: 'test'
            }]
            
            try {
                await store.dispatch(actions.saveCurrentUser(user, true))
            } catch (e) {
                assert.deepStrictEqual(store.getActions().slice(0, 2), expectedActions)
                assert.deepStrictEqual(store.getActions()[2].level, 'error')
            }
        })
    })
    
    describe('updateCurrentUserName', () => {
        it('creates UPDATE_CURRENT_USER', async () => {
            const store = mockStore({
                user: {
                    currentUser: {
                        id: 'test',
                        email: 'test2',
                        name: 'test3',
                        timezone: 'test4'
                    }
                }
            })
            await store.dispatch(actions.updateCurrentUserName('test5'))
            const expectedActions = [{
                type: actions.UPDATE_CURRENT_USER,
                user: {
                    id: 'test',
                    email: 'test2',
                    name: 'test5',
                    timezone: 'test4'
                }
            }]
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
    })
    
    describe('updateCurrentUserTimezone', () => {
        it('creates UPDATE_CURRENT_USER', async () => {
            const store = mockStore({
                user: {
                    currentUser: {
                        id: 'test',
                        email: 'test2',
                        name: 'test3',
                        timezone: 'test4'
                    }
                }
            })
            await store.dispatch(actions.updateCurrentUserTimezone('test5'))
            const expectedActions = [{
                type: actions.UPDATE_CURRENT_USER,
                user: {
                    id: 'test',
                    email: 'test2',
                    name: 'test3',
                    timezone: 'test5'
                }
            }]
            assert.deepStrictEqual(store.getActions(), expectedActions)
        })
    })
})