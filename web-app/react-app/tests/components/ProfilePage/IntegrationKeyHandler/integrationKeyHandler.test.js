import React from 'react'
import {shallow, mount} from 'enzyme'
import assert from 'assert-diff'

import IntegrationKeyHandler from '../../../../components/ProfilePage/IntegrationKeyHandler'

describe('IntegrationKeyHandler', () => {
    
    describe('render', () => {
        it('should render correctly', () => {
            const handler = shallow(<IntegrationKeyHandler/>)
            const handlerSegment = handler.childAt(0).childAt(0)
            assert(handlerSegment.exists())
            assert.deepStrictEqual(handlerSegment.props(), {
                service: "ETHEREUM",
                name: "Ethereum",
                inputFields: ['privateKey'],
                tableFields: ['address']
            })
        })
    })
    
})
