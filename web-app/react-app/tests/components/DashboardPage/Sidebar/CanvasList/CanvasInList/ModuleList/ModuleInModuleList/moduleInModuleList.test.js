
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'
import uuid from 'uuid'
import {
    ModuleInModuleList,
    mapStateToProps,
    mapDispatchToProps
} from '../../../../../../../../components/DashboardPage/Sidebar/CanvasList/CanvasInList/ModuleList/ModuleInModuleList'
import * as dashboardActions from '../../../../../../../../actions/dashboard'

describe('ModuleInModuleList', () => {
    let sandbox
    let lastUuid
    
    beforeEach(() => {
        sandbox = sinon.sandbox.create()
        const oldUuidV4 = uuid.v4
        sandbox.stub(uuid, 'v4').callsFake(() => {
            const id = oldUuidV4()
            lastUuid = id
            return id
        })
    })
    
    afterEach(() => {
        sandbox.restore()
        lastUuid = undefined
    })
    
    describe('onClick', () => {
        it('must add dashboardItem if !props.checked', () => {
            const spy = sandbox.spy()
            const el = shallow(<ModuleInModuleList
                checked={false}
                dashboard={{
                    id: 'idTest'
                }}
                module={{
                    name: 'nameTest',
                    hash: 'hashTest',
                    uiChannel: {
                        webcomponent: 'webcomponentTest'
                    }
                }}
                canvasId="canvasTest"
                addDashboardItem={spy}
            />)
            el.instance().onClick()
            assert(spy.calledOnce)
            assert(spy.calledWith({
                id: 'idTest'
            }, {
                id: lastUuid,
                dashboard: 'idTest',
                module: 'hashTest',
                canvas: 'canvasTest',
                webcomponent: 'webcomponentTest',
                title: 'nameTest'
            }))
        })
        it('must remove dashboardItem if props.checked', () => {
            const spy = sandbox.spy()
            const el = shallow(<ModuleInModuleList
                checked={true}
                dashboard={{
                    id: 'idTest'
                }}
                module={{
                    name: 'nameTest',
                    hash: 'hashTest',
                    uiChannel: {
                        webcomponent: 'webcomponentTest'
                    }
                }}
                canvasId="canvasTest"
                removeDashboardItem={spy}
            />)
            el.instance().onClick()
            assert(spy.calledOnce)
            assert(spy.calledWith({
                id: 'idTest'
            }, {
                id: lastUuid,
                dashboard: 'idTest',
                module: 'hashTest',
                canvas: 'canvasTest',
                webcomponent: 'webcomponentTest',
                title: 'nameTest'
            }))
        })
    })
    
    describe('mapStateToProps', () => {
        it('must not be checked if the module is not in any dashboard', () => {
            const dashboard1 = {
                items: [{
                    canvas: 'canvas1',
                    module: 'module1'
                }]
            }
            assert.deepStrictEqual(mapStateToProps({
                dashboard: {
                    dashboardsById: {
                        1: dashboard1
                    },
                    openDashboard: {
                        id: 1
                    }
                }
            }, {
                module: {
                    hash: 'module2'
                },
                canvasId: 'canvas2'
            }), {
                dashboard: dashboard1,
                checked: false
            })
        })
        it('must not be checked if the module is in another dashboard', () => {
            const dashboard1 = {
                items: [{
                    canvas: 'canvas1',
                    module: 'module1'
                }]
            }
            const dashboard2 = {
                items: [{
                    canvas: 'canvas1',
                    module: 'module1'
                }]
            }
            assert.deepStrictEqual(mapStateToProps({
                dashboard: {
                    dashboardsById: {
                        1: dashboard1,
                        2: dashboard2
                    },
                    openDashboard: {
                        id: 2
                    }
                }
            }, {
                module: {
                    hash: 'module1'
                },
                canvasId: 'canvas1'
            }), {
                dashboard: dashboard2,
                checked: false
            })
        })
        it('must be checked if the module is in the open dashboard', () => {
            const dashboard1 = {
                items: [{
                    canvas: 'canvas1',
                    module: 'module1'
                }]
            }
            const dashboard2 = {
                items: [{
                    canvas: 'canvas1',
                    module: 'module1'
                }]
            }
            assert.deepStrictEqual(mapStateToProps({
                dashboard: {
                    dashboardsById: {
                        1: dashboard1,
                        2: dashboard2
                    },
                    openDashboard: {
                        id: 1
                    }
                }
            }, {
                module: {
                    hash: 'module1'
                },
                canvasId: 'canvas1'
            }), {
                dashboard: dashboard1,
                checked: true
            })
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('must dispatch addDashboardItem if called addDashboardItem', () => {
            const dispatchSpy = sandbox.spy()
            const addDashboardItemStub = sandbox.stub(dashboardActions, 'addDashboardItem').callsFake(() => 'test')
            const item = {
                item: 'yes'
            }
            mapDispatchToProps(dispatchSpy).addDashboardItem(item)
            assert(addDashboardItemStub.calledOnce)
            assert(addDashboardItemStub.calledWith(item))
            assert(dispatchSpy.calledOnce)
            assert(dispatchSpy.calledWith('test'))
        })
        it('must dispatch removeDashboardItem if called removeDashboardItem', () => {
            const dispatchSpy = sandbox.spy()
            const removeDashboardItemStub = sandbox.stub(dashboardActions, 'removeDashboardItem').callsFake(() => 'test')
            const item = {
                item: 'yes'
            }
            mapDispatchToProps(dispatchSpy).removeDashboardItem(item)
            assert(removeDashboardItemStub.calledOnce)
            assert(removeDashboardItemStub.calledWith(item))
            assert(dispatchSpy.calledOnce)
            assert(dispatchSpy.calledWith('test'))
        })
    })
    
})
