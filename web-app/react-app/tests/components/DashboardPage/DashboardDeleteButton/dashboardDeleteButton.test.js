
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import * as createLink from '../../../../utils/createLink'
import * as parseState from '../../../../utils/parseState'
import * as actions from '../../../../actions/dashboard'
import sinon from 'sinon'

import {DashboardDeleteButton, mapStateToProps, mapDispatchToProps} from '../../../../components/DashboardPage/DashboardDeleteButton'

sinon.stub(createLink, 'default').callsFake(url => url)

describe('DashboardDeleteButton', () => {
    let dashboardDeleteButton
    let dashboard
    let sandbox

    beforeEach(() => {
        sandbox = sinon.sandbox.create()
        global.window = {}
        dashboard = {
            name: 'test'
        }
    })

    afterEach(() => {
        sandbox.reset()
    })

    describe('onDelete', () => {
        it('must call props.deleteDashboard and set the window.location', (done) => {
            const locationMock = sinon.stub(global.window.location, 'assign')
            const mock = sandbox.stub().callsFake(() => new Promise((resolve) => {
                resolve()
                setTimeout(() => {
                    assert(locationMock.calledOnce)
                    assert(locationMock.calledWith('/dashboard/list'))
                    done()
                })
            }))
            dashboardDeleteButton = shallow(<DashboardDeleteButton
                deleteDashboard={mock}
                dashboard={dashboard}
            />)
            dashboardDeleteButton.instance().onDelete()
        })
    })

    describe('render', () => {
        it('must render ConfirmButton with onDelete as props.confirmCallBack', () => {
            const deleteButton = shallow(<DashboardDeleteButton
                dashboard={dashboard}
            />)
            const confirmButton = deleteButton.find('ConfirmButton')
            assert.deepStrictEqual(confirmButton.props().confirmCallback, deleteButton.instance().onDelete)
        })
    })

    describe('mapStateToProps', () => {
        it('must return parseDashboard(state)', () => {
            const stub = sandbox.stub(parseState, 'parseDashboard').callsFake((state) => state.id)
            assert.equal(mapStateToProps({
                id: 'test'
            }), 'test')
            assert(stub.calledOnce)
        })
    })

    describe('mapDispatchToProps', () => {
        it('must dispatch deleteDashboard(id)', () => {
            const deleteDashboardStub = sandbox.stub(actions, 'deleteDashboard').callsFake(id => id)
            const dispatchSpy = sandbox.spy()
            mapDispatchToProps(dispatchSpy).deleteDashboard('test')
            assert(deleteDashboardStub.calledOnce)
            assert(dispatchSpy.calledOnce)
            assert(dispatchSpy.calledWith('test'))
        })
    })
})
