
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import * as dashboardActions from '../../../../actions/dashboard'
import {ShortcutManager} from 'react-shortcuts'
import sinon from 'sinon'

import {ShortcutHandler, mapDispatchToProps} from '../../../../components/DashboardPage/ShortcutHandler'

describe('ShortcutHandler', () => {
    let sandbox
    
    beforeEach(() => {
        sandbox = sinon.sandbox.create()
    })
    
    afterEach(() => {
        sandbox.reset()
    })
    
    describe('getChildContextTypes', () => {
        it('must add shortcutManager', () => {
            const manager = shallow(<ShortcutHandler/>)
            assert(manager.instance().getChildContext().shortcuts instanceof ShortcutManager)
        })
    })
    
    describe('handleShortcuts', () => {
        describe('SAVE', () => {
            it('must call props.updateAndSaveCurrentDashboard', () => {
                const spy = sandbox.spy()
                const manager = shallow(<ShortcutHandler
                    updateAndSaveCurrentDashboard={spy}
                />)
                manager.instance().handleShortcuts('SAVE', new Event('Event'))
                assert(spy.calledOnce)
            })
            it('must call event.preventDefault', () => {
                const spy = sandbox.spy()
                const manager = shallow(<ShortcutHandler
                    updateAndSaveCurrentDashboard={() => {}}
                />)
                manager.instance().handleShortcuts('SAVE', {
                    preventDefault: spy
                })
                assert(spy.calledOnce)
            })
        })
    })
    
    describe('render', () => {
        it('must render Shortcuts with correct handler', () => {
            const manager = shallow(<ShortcutHandler>test</ShortcutHandler>)
            const shortcuts = manager.find('Shortcuts')
            assert.deepStrictEqual(shortcuts.props().handler, manager.instance().handleShortcuts)
            assert.equal()
        })
        it('must have the same name than the key in keymap', () => {
            const manager = shallow(<ShortcutHandler>test</ShortcutHandler>)
            assert.deepStrictEqual(Object.keys(ShortcutHandler.keymap)[0], manager.props().name)
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('must dispatch updateAndSaveCurrentDashboard when called updateAndSaveCurrentDashboard', () => {
            const updateAndSaveCurrentDashboard = sandbox.stub(dashboardActions, 'updateAndSaveCurrentDashboard').callsFake(() => {})
            const dispatchSpy = sandbox.spy()
            mapDispatchToProps(dispatchSpy).updateAndSaveCurrentDashboard()
            assert(updateAndSaveCurrentDashboard.calledOnce)
            assert(dispatchSpy.calledOnce)
        })
    })
})
