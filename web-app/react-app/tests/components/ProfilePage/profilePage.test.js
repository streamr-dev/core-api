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
        it('should have a Notifier', () => {
            const el = shallow(<ProfilePage/>)
            assert(el.find('Connect(StreamrNotifierWrapper)'))
        })
        it('should have a ProfileSettings', () => {
            const el = shallow(<ProfilePage/>)
            assert(el.find('Connect(ProfileSettings)'))
        })
        it('should have a APICredentials', () => {
            const el = shallow(<ProfilePage/>)
            assert(el.find('Connect(APICredentials)'))
        })
        it('should have a IntegrationKeyHandler', () => {
            const el = shallow(<ProfilePage/>)
            assert(el.find('Connect(IntegrationKeyHandler)'))
        })
    })
    
})
