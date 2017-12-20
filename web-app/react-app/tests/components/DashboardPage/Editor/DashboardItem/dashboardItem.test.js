import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import * as createLink from '../../../../../helpers/createLink'
import * as notificationActions from '../../../../../actions/notification'
import sinon from 'sinon'

import {
    DashboardItem,
    mapDispatchToProps,
    mapStateToProps
} from '../../../../../components/DashboardPage/Editor/DashboardItem'

sinon.stub(createLink, 'default').callsFake((url) => url)

describe('DashboardItem', () => {
    let sandbox
    
    beforeEach(() => {
        sandbox = sinon.sandbox.create()
    })
    
    afterEach(() => {
        sandbox.restore()
    })
    
    describe('onResize', () => {
        it('must update wrapper width and height to state', () => {
            const el = shallow(<DashboardItem
                config={{
                    components: {
                        'test-component': {
                            component: 'span',
                            props: {
                                a: 'b'
                            }
                        }
                    }
                }}
            />)
            el.instance().wrapper = {
                offsetWidth: 100,
                offsetHeight: 200
            }
            el.instance().onResize()
            assert.deepStrictEqual(el.state(), {
                width: 100,
                height: 200
            })
        })
    })
    
    describe('componentWillReceiveProps', () => {
        it('must call onResize', () => {
            const spy = sandbox.spy()
            const el = shallow(<DashboardItem
                config={{
                    components: {
                        'test-component': {
                            component: 'span',
                            props: {
                                a: 'b'
                            }
                        }
                    }
                }}
            />)
            el.instance().onResize = spy
            el.instance().componentWillReceiveProps()
            assert(spy.calledOnce)
        })
    })
    
    describe('createWebcomponentUrl', () => {
        let el
        beforeEach(() => {
            el = shallow(<DashboardItem
                item={{
                    canvas: 'canvasId',
                    module: 3
                }}
                config={{
                    components: {
                        'test-component': {
                            component: 'span',
                            props: {
                                a: 'b'
                            }
                        }
                    }
                }}

            />)
        })
        it('must return canvas url if dashboard.new = true', () => {
            el.setProps({
                dashboard: {
                    id: 'test',
                    new: true
                }
            })
            assert.equal(el.instance().createWebcomponentUrl(), '/api/v1/canvases/canvasId/modules/3')
        })
        it('must return dashboard url if dashboard.new != true', () => {
            el.setProps({
                dashboard: {
                    id: 'test',
                    new: false
                }
            })
            assert.equal(el.instance().createWebcomponentUrl(), '/api/v1/dashboards/test/canvases/canvasId/modules/3')
        })
    })
    
    describe('onError', () => {
        afterEach(() => {
            delete process.env.NODE_ENV
        })
        it('must show the correct message in prod mode', () => {
            const errorSpy = sandbox.spy(console, 'error')
            const showErrorSpy = sandbox.spy()
            process.env.NODE_ENV = 'production'
            const el = shallow(<DashboardItem
                showError={showErrorSpy}
                config={{
                    components: {
                        'test-component': {
                            component: 'span',
                            props: {
                                a: 'b'
                            }
                        }
                    }
                }}

            />)
            el.instance().onError({
                message: 'testMsg',
                stack: 'testStack'
            })
            assert(showErrorSpy.calledOnce)
            assert(showErrorSpy.calledWith('Something went wrong!'))
            assert(errorSpy.notCalled)
        })
        it('must show the correct message in dev mode', () => {
            const errorStub = sandbox.stub(console, 'error')
            const showErrorSpy = sandbox.spy()
            const el = shallow(<DashboardItem
                showError={showErrorSpy}
                config={{
                    components: {
                        'test-component': {
                            component: 'span',
                            props: {
                                a: 'b'
                            }
                        }
                    }
                }}

            />)
            el.instance().onError({
                message: 'testMsg',
                stack: 'testStack'
            })
            assert(showErrorSpy.calledOnce)
            assert(showErrorSpy.calledWith({
                message: 'testMsg',
                stack: 'testStack'
            }))
            assert(errorStub.calledOnce)
            assert(errorStub.calledWith('testStack'))
        })
    })
    
    describe('createCustomComponent', () => {
        it('must use the component/props pairs from config', () => {
            const el = shallow(<DashboardItem
                config={{
                    components: {
                        'test-component': {
                            component: 'span',
                            props: {
                                a: 'b'
                            }
                        }
                    }
                }}
                item={{
                    webcomponent: 'test-component'
                }}
            />)
            const el2 = el.instance().createCustomComponent()
            const el2wrapped = shallow(el2)
            assert(el2wrapped.is('span'))
            assert.equal(el2wrapped.props().a, 'b')
        })
    })
    
    describe('mapStateToProps', () => {
        it('must return right kind of object', () => {
            assert.deepStrictEqual(mapStateToProps({
                dashboard: {
                    dashboardsById: {
                        test: 'aapeli'
                    },
                    openDashboard: {
                        id: 'test'
                    }
                },
                config: 'moimoi'
            }), {
                dashboard: 'aapeli',
                config: 'moimoi'
            })
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('must dispatch showError when called showError', () => {
            const dispatchSpy = sandbox.spy()
            const showErrorStub = sandbox.stub(notificationActions, 'showError').callsFake((error) => error)
            const error = 'test'
            mapDispatchToProps(dispatchSpy).showError(error)
            assert(showErrorStub.calledOnce)
            assert(showErrorStub.calledWith({
                title: 'Error!',
                message: 'test'
            }))
            assert(dispatchSpy.calledOnce)
            assert(dispatchSpy.calledWith({
                title: 'Error!',
                message: 'test'
            }))
        })
    })
})
