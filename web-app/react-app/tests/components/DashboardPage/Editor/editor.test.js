import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'
import * as createLink from '../../../../helpers/createLink'
import StreamrClient from 'streamr-client'
import * as utils from '../../../../helpers/parseState'
import * as dashboardActions from '../../../../actions/dashboard'

import {
    Editor,
    mapStateToProps,
    mapDispatchToProps
} from '../../../../components/DashboardPage/Editor'

sinon.stub(createLink, 'default').callsFake((url) => url)

describe('Editor', () => {
    let sandbox
    
    beforeEach(() => {
        global.keyId = 'key'
        sandbox = sinon.sandbox.create()
    })
    
    afterEach(() => {
        delete global.keyId
        sandbox.restore()
    })
    
    describe('componentDidMount', () => {
        let el
        beforeEach(() => {
            el = shallow(<Editor/>)
        })
        it('must add window beforeunload listener', () => {
            const stub = sandbox.stub(global.window, 'addEventListener')
            el.instance().onBeforeUnload = 'onBeforeUnload'
            el.instance().componentDidMount()
            assert(stub.calledOnce)
            assert(stub.calledWith('beforeunload', 'onBeforeUnload'))
        })
    })
    
    describe('componentWillReceiveProps', () => {
        let spy
        let el
        beforeEach(() => {
            spy = sandbox.spy()
            el = shallow(<Editor
                dashboard={{
                    id: 'test1'
                }}
                history={{
                    push: spy
                }}
            />)
        })
        it('must change the url if dashboard changed', () => {
            el.instance().componentWillReceiveProps({
                dashboard: {
                    id: 'test2'
                }
            })
            assert(spy.calledOnce)
            assert(spy.calledWith('/test2'))
        })
        it('must not change the url if dashboard not changed', () => {
            el.instance().componentWillReceiveProps({
                dashboard: {
                    id: 'test1'
                }
            })
            assert(spy.notCalled)
        })
        it('must not remove the id from url if new id is null', () => {
            el.instance().componentWillReceiveProps({
                dashboard: {
                    id: null
                }
            })
            assert(spy.calledOnce)
            assert(spy.calledWith('/'))
        })
    })
    
    describe('onLayoutChange', () => {
        it('must call right functions', () => {
            const onResizeSpy = sandbox.spy()
            const updateDashboardLayoutSpy = sandbox.spy()
            const el = shallow(<Editor
                dashboard={{
                    id: 'test'
                }}
                updateDashboardLayout={updateDashboardLayoutSpy}
            />)
            el.instance().onResize = onResizeSpy
            el.instance().onLayoutChange('layout', 'allLayouts')
            assert(onResizeSpy.calledOnce)
            assert(onResizeSpy.calledWith('layout'))
            assert(updateDashboardLayoutSpy.calledOnce)
            assert(updateDashboardLayoutSpy.calledWith('test', 'allLayouts'))
        })
    })
    
    describe('onFullscreenToggle', () => {
        let el
        beforeEach(() => {
            el = shallow(<Editor/>)
        })
        it('must use the given value', () => {
            el.instance().onFullscreenToggle(true)
            assert(el.state().isFullscreen)
            el.instance().onFullscreenToggle(false)
            assert(!el.state().isFullscreen)
        })
        it('must use the opposite value if no value given', () => {
            el.instance().onFullscreenToggle()
            assert(el.state().isFullscreen)
            el.instance().onFullscreenToggle()
            assert(!el.state().isFullscreen)
        })
    })
    
    describe('generateLayout', () => {
        it('must provide a correct-looking result', () => {
            const el = shallow(<Editor
                dashboard={{
                    items: [{
                        id: 'id1',
                        webcomponent: 'streamr-label'
                    }, {
                        id: 'id2',
                        webcomponent: 'streamr-map'
                    }, {
                        id: 'id2',
                        webcomponent: 'streamr-chart'
                    }]
                }}
            />)
            sandbox.stub(Editor, 'generateItemId').callsFake(() => Date.now())
            const layout = el.instance().generateLayout()
            assert(Object.keys(layout).length)
            for (const key in layout) {
                assert(['xs', 'sm', 'md', 'lg'].indexOf(key) >= 0)
                const sizeLayout = layout[key]
                assert(Array.isArray(sizeLayout))
                for (const itemLayout of sizeLayout) {
                    assert(itemLayout.i != undefined)
                    assert(itemLayout.w != undefined)
                    assert(itemLayout.h != undefined)
                }
            }
        })
    })
    
    describe('onResize', () => {
        it('must parse the layout correctly', () => {
            const el = shallow(<Editor/>)
            el.instance().onResize([{
                i: '1',
                w: 1,
                h: 2
            }, {
                i: '2',
                w: 3,
                h: 4
            }, {
                i: '3',
                w: 5,
                h: 6
            }])
            assert.deepStrictEqual(el.state().layoutsByItemId, {
                '1': {
                    i: '1',
                    w: 1,
                    h: 2
                },
                '2': {
                    i: '2',
                    w: 3,
                    h: 4
                },
                '3': {
                    i: '3',
                    w: 5,
                    h: 6
                },
            })
        })
    })
    
    describe('onBeforeUnload', () => {
        it('must return undefined and do nothing if dashboard is saved', () => {
            const el = shallow(<Editor
                dashboard={{
                    id: 'moi',
                    saved: true
                }}
            />)
            let event = {}
            assert.equal(el.instance().onBeforeUnload(event), undefined)
            assert.equal(event.returnValue, undefined)
        })
        it('must return message and set it to the returnValue of event if dashboard is not saved', () => {
            const el = shallow(<Editor
                dashboard={{
                    id: 'moi',
                    saved: false
                }}
            />)
            let event = {}
            assert.notEqual(el.instance().onBeforeUnload(event), undefined)
            assert.notEqual(event.returnValue, undefined)
        })
    })
    
    describe('generateItemId', () => {
        it('must return canvas-module', () => {
            assert.equal(Editor.generateItemId({
                canvas: 'testCanvas',
                module: 100
            }), 'testCanvas-100')
        })
    })
    
    describe('render', () => {
        it('must provide streamrClient to items', () => {
            const el = shallow(<Editor
                dashboard={{
                    items: [{
                        id: 'moi',
                        canvas: 1,
                        module: 2,
                        webcomponent: 'streamr-label'
                    }]
                }}
            />)
            const provider = el.find('StreamrClientProvider')
            assert(provider.props().client instanceof StreamrClient)
            assert(provider.find('DashboardItem'))
        })
    })
    
    describe('mapStateToProps', () => {
        it('must return a right kind of object', () => {
            let stub =  sandbox.stub(utils, 'parseDashboard')
            const dashboard1 = {
                editingLocked: true,
                new: true
            }
            stub.callsFake(() => ({
                dashboard: dashboard1,
                canWrite: true
            }))
            assert.deepStrictEqual(mapStateToProps({
                dashboard: dashboard1
            }), {
                dashboard: dashboard1,
                canWrite: true,
                editorLocked: true
            })
            const dashboard2 = {
                editingLocked: false,
                new: false
            }
            stub.callsFake(() => ({
                dashboard: dashboard2,
                canWrite: true
            }))
            const a = mapStateToProps({
                dashboard: dashboard2
            })
            assert.deepStrictEqual(a, {
                dashboard: dashboard2,
                canWrite: true,
                editorLocked: false
            })
            const dashboard3 = {
                editingLocked: false,
                new: false
            }
            stub.callsFake(() => ({
                dashboard: dashboard3,
                canWrite: false
            }))
            assert.deepStrictEqual(mapStateToProps({
                dashboard: dashboard3
            }), {
                dashboard: dashboard3,
                canWrite: false,
                editorLocked: true
            })
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('should dispatch updateDashboardChanges when called update', () => {
            const dispatchSpy = sandbox.spy()
            const updateDashboardChangesStub = sandbox.stub(dashboardActions, 'updateDashboardChanges').callsFake(() => 'test')
            mapDispatchToProps(dispatchSpy).update('id', 'changes')
            assert(updateDashboardChangesStub.calledOnce)
            assert(updateDashboardChangesStub.calledWith('id', 'changes'))
            assert(dispatchSpy.calledOnce)
            assert(dispatchSpy.calledWith('test'))
        })
        it('should dispatch lockDashboardEditing when called lockEditing', () => {
            const dispatchSpy = sandbox.spy()
            const lockDashboardEditingStub = sandbox.stub(dashboardActions, 'lockDashboardEditing').callsFake(() => 'test')
            mapDispatchToProps(dispatchSpy).lockEditing('id')
            assert(lockDashboardEditingStub.calledOnce)
            assert(lockDashboardEditingStub.calledWith('id'))
            assert(dispatchSpy.calledOnce)
            assert(dispatchSpy.calledWith('test'))
        })
        it('should dispatch unlockDashboardEditing when called unlockEditing', () => {
            const dispatchSpy = sandbox.spy()
            const unlockDashboardEditingStub = sandbox.stub(dashboardActions, 'unlockDashboardEditing').callsFake(() => 'test')
            mapDispatchToProps(dispatchSpy).unlockEditing('id')
            assert(unlockDashboardEditingStub.calledOnce)
            assert(unlockDashboardEditingStub.calledWith('id'))
            assert(dispatchSpy.calledOnce)
            assert(dispatchSpy.calledWith('test'))
        })
        it('should dispatch updateDashboardLayout when called updateDashboardLayout', () => {
            const dispatchSpy = sandbox.spy()
            const updateDashboardLayoutStub = sandbox.stub(dashboardActions, 'updateDashboardLayout').callsFake(() => 'test')
            mapDispatchToProps(dispatchSpy).updateDashboardLayout('id', 'changes')
            assert(updateDashboardLayoutStub.calledOnce)
            assert(updateDashboardLayoutStub.calledWith('id', 'changes'))
            assert(dispatchSpy.calledOnce)
            assert(dispatchSpy.calledWith('test'))
        })
    })
})

