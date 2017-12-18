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
    
})
