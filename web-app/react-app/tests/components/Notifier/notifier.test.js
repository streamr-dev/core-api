
import React from 'react'
import sinon from 'sinon'
import {mount} from 'enzyme'
import assert from 'assert-diff'

import {Notifier, mapStateToProps} from '../../../components/Notifier'

describe('Notifier', () => {
    let notifications
    const showSuccessSpy = sinon.spy()
    const showErrorSpy = sinon.spy()
    const Streamr = {
        showSuccess() {
            showSuccessSpy.apply(this, arguments)
        },
        showError() {
            showErrorSpy.apply(this, arguments)
        }
    }
    beforeEach(() => {
        notifications = {}
        global.Streamr = Streamr
    })
    afterEach(() => {
        delete global.Streamr
        showSuccessSpy.reset()
        showErrorSpy.reset()
    })
    
    it('should render null', () => {
        assert.deepStrictEqual(new Notifier().render(), null)
    })
    
    describe('createNotification', () => {
        describe('calling different functions by message.type', () => {
            it('should call showSuccess if message.type is success', () => {
                new Notifier().createNotification({
                    title: 'some title',
                    message: 'some message',
                    delay: 'some delay',
                    type: 'success'
                })
                assert(showSuccessSpy.calledOnce)
                assert(!showErrorSpy.called)
                assert.deepStrictEqual(showSuccessSpy.firstCall.args, [
                    'some message',
                    'some title',
                    'some delay'
                ])
            })
            it('should call showSuccess if message.type is info', () => {
                new Notifier().createNotification({
                    title: 'some other title',
                    message: 'some other message',
                    delay: 'some other delay',
                    type: 'info'
                })
                assert(showSuccessSpy.calledOnce)
                assert(!showErrorSpy.called)
                assert.deepStrictEqual(showSuccessSpy.firstCall.args, [
                    'some other message',
                    'some other title',
                    'some other delay'
                ])
            })
            it('should call showError if message.type is error', () => {
                new Notifier().createNotification({
                    title: 'still some other title',
                    message: 'still some other message',
                    delay: 'still some other delay',
                    type: 'error'
                })
                assert(showErrorSpy.calledOnce)
                assert(!showSuccessSpy.called)
                assert.deepStrictEqual(showErrorSpy.firstCall.args, [
                    'still some other message',
                    'still some other title',
                    'still some other delay'
                ])
            })
        })
    })
    
    describe('componentWillReceiveProps', () => {
        let notifier
        let createNotificationSpy = sinon.spy()
        beforeEach(() => {
            notifier = mount(<Notifier notifications={{}} />)
            notifier.instance().createNotification = createNotificationSpy
        })
        afterEach(() => {
            createNotificationSpy.reset()
        })
        it('should call createNotification for each new notification id', () => {
            const notifications = {
                test1: {
                    id: 'test1',
                    title: 'test1title'
                },
                test2: {
                    id: 'test2',
                    title: 'test2title'
                }
            }
            
            notifier.setProps({
                notifications
            })
            assert(createNotificationSpy.calledTwice)
            assert(createNotificationSpy.calledWith(notifications.test1))
            assert(createNotificationSpy.calledWith(notifications.test2))
        })
        it('should not call createNotification if props already have the notification id', () => {
            const notifications = {
                test1: {
                    id: 'test1',
                    title: 'test1title'
                },
                test2: {
                    id: 'test2',
                    title: 'test2title2'
                },
                test3: {
                    id: 'test3',
                    title: 'test3title'
                }
            }
            notifier.setProps({
                notifications: {
                    test2: {
                        id: 'test2',
                        title: 'test2title'
                    }
                }
            })
            createNotificationSpy.reset()
            
            notifier.setProps({
                notifications
            })
            assert(createNotificationSpy.calledTwice)
            assert(createNotificationSpy.calledWith(notifications.test1))
            assert(createNotificationSpy.calledWith(notifications.test3))
            
            assert(createNotificationSpy.neverCalledWith(notifications.test2))
        })
    })
    
    describe('connected notifier', () => {
        describe('mapStateToProps', () => {
            it('should map the state to props correctly', () => {
                assert.deepStrictEqual(mapStateToProps({
                    notifications: {
                        byId: {
                            a: {
                                c: 1
                            },
                            b: {
                                d: 2
                            }
                        }
                    }
                }), {
                    notifications: {
                        a: {
                            c: 1
                        },
                        b: {
                            d: 2
                        }
                    }
                })
            })
        })
    })
})
