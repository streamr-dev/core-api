/* globals jest */

import expect from 'expect'
import thunk from 'redux-thunk'
import configureMockStore from 'redux-mock-store'

jest.mock('uuid', () => ({
    v4: () => 'randomId'
}))

import * as actions from '../../actions/notification'

const middlewares = [ thunk ]
const mockStore = configureMockStore(middlewares)

describe('Notification actions', () => {
    let store
    let origSetTimeout
    
    beforeEach(() => {
        origSetTimeout = global.setTimeout
        store = mockStore({
            byId: {}
        })
    })
    afterEach(() => {
        global.setTimeout = origSetTimeout
        store.clearActions()
    })
    it('creates CREATE_NOTIFICATION with wanted info', () => {
        global.setTimeout = () => {}
        const notif = {
            id: 'test',
            type: 'success',
            title: 'test',
            message: 'testAgain',
            delay: 1000
        }
        const expectedActions = [{
            type: actions.CREATE_NOTIFICATION,
            notification: notif
        }]
    
        store.dispatch(actions.showNotification(notif))
        expect(store.getActions()).toEqual(expectedActions)
    })
    it('uses uuid.v4 if no id given', () => {
        global.setTimeout = () => {}
        const notif = {
            type: 'success',
            title: 'test',
            message: 'testAgain',
            delay: 1000
        }
        const expectedActions = [{
            type: actions.CREATE_NOTIFICATION,
            notification: {
                ...notif,
                id: 'randomId'
            }
        }]
        store.dispatch(actions.showNotification(notif))
        expect(store.getActions()).toEqual(expectedActions)
    })
    it('uses delay of 4000 if no delay is given', () => {
        global.setTimeout = () => {}
        const notif = {
            type: 'success',
            title: 'test',
            message: 'testAgain'
        }
        const expectedActions = [{
            type: actions.CREATE_NOTIFICATION,
            notification: {
                ...notif,
                delay: 4000,
                id: 'randomId'
            }
        }]
        store.dispatch(actions.showNotification(notif))
        expect(store.getActions()).toEqual(expectedActions)
    })
    it('must create REMOVE_NOTIFICATION when called removeNotification', () => {
        expect(actions.removeNotification('test')).toEqual({
            type: actions.REMOVE_NOTIFICATION,
            id: 'test'
        })
    
    })
    it('uses creates REMOVE_NOTIFICATION after delay', () => {
        const notif = {
            id: 'test',
            type: 'success',
            title: 'test',
            message: 'testAgain',
            delay: 200
        }
        const expectedActions = [{
            type: actions.CREATE_NOTIFICATION,
            notification: notif
        }]
        const expectedActions2 = [{
            type: actions.CREATE_NOTIFICATION,
            notification: notif
        },{
            type: actions.REMOVE_NOTIFICATION,
            id: 'test'
        }]
        store.dispatch(actions.showNotification(notif))
        expect(store.getActions()).toEqual(expectedActions)
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                try {
                    expect(store.getActions()).toEqual(expectedActions2)
                } catch (e) {
                    reject(e)
                }
                resolve()
            }, 250)
        })
    })
    it('must call showNotification with "success" type when called showSuccess', () => {
        global.setTimeout = () => {}
        const notif = {
            id: 'test',
            title: 'test',
            message: 'testAgain',
            delay: 1000
        }
        const expectedActions = [{
            type: actions.CREATE_NOTIFICATION,
            notification: {
                ...notif,
                type: 'success'
            }
        }]
        store.dispatch(actions.showSuccess(notif))
        expect(store.getActions()).toEqual(expectedActions)
    })
    it('must call showNotification with "info" type when called showInfo', () => {
        global.setTimeout = () => {}
        const notif = {
            id: 'test',
            title: 'test',
            message: 'testAgain',
            delay: 1000
        }
        const expectedActions = [{
            type: actions.CREATE_NOTIFICATION,
            notification: {
                ...notif,
                type: 'info'
            }
        }]
        store.dispatch(actions.showInfo(notif))
        expect(store.getActions()).toEqual(expectedActions)
    })
    it('must call showNotification with "error" type when called showError', () => {
        global.setTimeout = () => {}
        const notif = {
            id: 'test',
            title: 'test',
            message: 'testAgain',
            delay: 1000
        }
        const expectedActions = [{
            type: actions.CREATE_NOTIFICATION,
            notification: {
                ...notif,
                type: 'error'
            }
        }]
        store.dispatch(actions.showError(notif))
        expect(store.getActions()).toEqual(expectedActions)
    })
})