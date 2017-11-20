import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'

import ProfilePage from '../../../components/ProfilePage'

describe('ProfilePageHandler', () => {
    
    describe('render', () => {
        it('should be a Row', () => {
            const el = shallow(<ProfilePage/>)
            assert(el.is('Row'))
        })
        it('should have correct children with correct props', () => {
            const el = shallow(<ProfilePage/>)
            
            const col1 = el.childAt(0)
            assert(col1.is('Col'))
            assert.deepStrictEqual(col1.props().xs, 12)
            assert.deepStrictEqual(col1.props().sm, 6)
            
            const profileSettings = col1.childAt(0)
            assert(profileSettings.is('Connect(ProfileSettings)'))
            
            const col2 = el.childAt(1)
            assert(col2.is('Col'))
            assert.deepStrictEqual(col1.props().xs, 12)
            assert.deepStrictEqual(col1.props().sm, 6)
    
            const apiCredentials = col2.childAt(0)
            assert(apiCredentials.is('APICredentials'))
            
            const col3 = el.childAt(2)
            assert(col3.is('Col'))
            assert.deepStrictEqual(col1.props().xs, 12)
            assert.deepStrictEqual(col1.props().sm, 6)
    
            const integrationKeyHandler = col3.childAt(0)
            assert(integrationKeyHandler.is('IntegrationKeyHandler'))
            
            const notifier = el.childAt(3)
            assert(notifier.is('Connect(Notifier)'))
        })
    })
    
})
