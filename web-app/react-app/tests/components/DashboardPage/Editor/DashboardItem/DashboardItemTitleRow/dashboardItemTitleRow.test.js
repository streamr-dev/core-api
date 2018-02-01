import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import * as dashboardActions from '../../../../../../actions/dashboard'
import sinon from 'sinon'

import {
    DashboardItemTitleRow,
    mapDispatchToProps,
    mapStateToProps
} from '../../../../../../components/DashboardPage/Editor/DashboardItem/DashboardItemTitleRow'

describe('DashboardItemTitleRow', () => {
    let sandbox
    
    beforeEach(() => {
        sandbox = sinon.sandbox.create()
    })
    
    afterEach(() => {
        sandbox.reset()
    })
    
    describe('onRemove', () => {
        it('should call props.remove with right attrs', () => {
            const spy = sandbox.spy()
            const el = shallow(<DashboardItemTitleRow
                dashboard="testdb"
                item="testitem"
                remove={spy}
            />)
            el.instance().onRemove()
            assert(spy.calledWith)
            assert(spy.calledWith('testdb', 'testitem'))
        })
    })
    
    describe('startEdit', () => {
        it('must set state.editing to !state.editing', () => {
            const el = shallow(<DashboardItemTitleRow
                dashboard="testdb"
                item="testitem"
            />)
            assert(!el.state().editing)
            el.instance().startEdit()
            assert(el.state().editing)
        })
    })
    
    describe('endEdit', () => {
        it('must set state.editing to !state.editing', () => {
            const el = shallow(<DashboardItemTitleRow
                dashboard="testdb"
                item="testitem"
            />)
            el.setState({
                editing: true
            })
            assert(el.state().editing)
            el.instance().endEdit()
            assert(!el.state().editing)
        })
    })
    
    describe('saveName', () => {
        it('must call props.update with right attrs', () => {
            const spy = sandbox.spy()
            const el = shallow(<DashboardItemTitleRow
                dashboard="testDb"
                item="testItem"
                update={spy}
            />)
            el.instance().saveName({
                target: {
                    value: 'testValue'
                }
            })
            assert(spy.calledOnce)
            assert(spy.calledWith('testDb', 'testItem', {
                title: 'testValue'
            }))
        })
    })
    
    describe('render', () => {
        describe('title', () => {
            it('must render title in span instead of input if !state.editing', () => {
                const el = shallow(<DashboardItemTitleRow
                    dashboard="testDb"
                    item={{
                        title: 'title'
                    }}
                />)
                const span = el.find('span')
                assert(span)
                assert(!el.find('input').length)
                assert.equal(span.text(), 'title')
            })
            it('must render title in span instead of input if !state.editing', () => {
                const el = shallow(<DashboardItemTitleRow
                    dashboard="testDb"
                    item={{
                        title: 'title'
                    }}
                />)
                el.setState({
                    editing: true
                })
                const input = el.find('input')
                assert(input)
                assert(!el.find('span').length)
                assert.equal(input.props().value, 'title')
            })
            it('must set input props correctly', () => {
                const el = shallow(<DashboardItemTitleRow
                    dashboard="testDb"
                    item={{
                        title: 'title'
                    }}
                />)
                el.setState({
                    editing: true
                })
                const input = el.find('input')
                assert.deepStrictEqual(input.props().onChange, el.instance().saveName)
                assert.deepStrictEqual(input.props().onBlur, el.instance().onBlur)
            })
        })
        
        describe('controls', () => {
            it('must render controls only if !props.isLocked', () => {
                const el = shallow(<DashboardItemTitleRow
                    dashboard="testDb"
                    item={{
                        title: 'title'
                    }}
                    isLocked={true}
                />)
                assert.equal(el.find('.controlContainer').length, 0)
                el.setProps({
                    isLocked: false
                })
                assert.equal(el.find('.controlContainer').length, 1)
            })
            it('must render correct buttons', () => {
                const el = shallow(<DashboardItemTitleRow
                    dashboard="testDb"
                    item={{
                        title: 'title'
                    }}
                />)
                assert.equal(el.find('Button')
                    .at(0)
                    .props()
                    .onClick, el.instance().startEdit)
                assert.equal(el.find('Button')
                    .at(1)
                    .props()
                    .onClick, el.instance().onRemove)
            })
        })
    })
    
    describe('mapStateToProps', () => {
        it('must map the state correctly', () => {
            const db1 = {
                a: 1
            }
            const db2 = {
                b: 2
            }
            assert.deepStrictEqual(mapStateToProps({
                dashboard: {
                    dashboardsById: {
                        1: db1,
                        2: db2
                    },
                    openDashboard: {
                        id: 2
                    }
                }
            }), {
                dashboard: db2
            })
        })
    })
    
    describe('mapDispatchToProps', () => {
        describe('update', () => {
            it('must dispatch updateDashboardItem with correct attrs', () => {
                const stub = sandbox.stub(dashboardActions, 'updateDashboardItem').callsFake(() => 'test')
                const dispatchSpy = sandbox.spy()
                const db = {
                    a: 1
                }
                const item = {
                    b: 2
                }
                const changes = {
                    c: 3
                }
                mapDispatchToProps(dispatchSpy).update(db, item, changes)
                assert(dispatchSpy.calledOnce)
                assert(stub.calledOnce)
                assert(dispatchSpy.calledWith('test'))
                assert(stub.calledWith(db, {
                    ...item,
                    ...changes
                }))
            })
        })
        describe('remove', () => {
            it('must dispatch removeDashboardItem with correct attrs', () => {
                const stub = sandbox.stub(dashboardActions, 'removeDashboardItem').callsFake(() => 'test')
                const dispatchSpy = sandbox.spy()
                const db = {
                    a: 1
                }
                const item = {
                    b: 2
                }
                mapDispatchToProps(dispatchSpy).remove(db, item)
                assert(dispatchSpy.calledOnce)
                assert(stub.calledOnce)
                assert(dispatchSpy.calledWith('test'))
                assert(stub.calledWith(db, item))
            })
        })
    })
})
