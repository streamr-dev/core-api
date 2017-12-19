import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

describe('DashboardItem', () => {
    let sandbox
    
    beforeEach(() => {
        sandbox = sinon.sandbox.create()
    })
    
    afterEach(() => {
        sandbox.restore()
    })
    
    it('is not developed yet', () => {
        assert(false)
    })
    
})
