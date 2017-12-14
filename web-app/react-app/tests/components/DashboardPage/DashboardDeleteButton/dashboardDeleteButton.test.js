
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import * as createLink from '../../../../helpers/createLink'
import sinon from 'sinon'

import {DashboardDeleteButton} from '../../../../components/DashboardPage/DashboardDeleteButton'

describe('DashboardDeleteButton', () => {
    let dashboardDeleteButton
    let dashboard
    let sandbox
    
    beforeEach(() => {
        sandbox = sinon.sandbox.create()
        sandbox.stub(createLink, 'default').callsFake(url => url)
        global.window = {}
        dashboard = {
            name: 'test'
        }
    })
    
    afterEach(() => {
        sandbox.reset()
    })
    
    describe('onDelete', () => {
        it('must call props.deleteDashboard', (done) => {
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
})
