import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'

import IntegrationKeyHandlerSegment from '../../../../../components/ProfilePage/IntegrationKeyHandler/IntegrationKeyHandlerSegment'

describe('IntegrationKeyHandler', () => {

    describe('render', () => {
        it('renders ControlLabel correctly', () => {
            const el = shallow(<IntegrationKeyHandlerSegment
                tableFields={[]}
                inputFields={[]}
                integrationKeys={[]}
                service=""
                name="test"
                getIntegrationKeysByService={() => {}}
                createIntegrationKey={() => {}}
                deleteIntegrationKey={() => {}}
            />)
            const label = el.childAt(0).childAt(0)
            assert(label.is('ControlLabel'))
            assert.equal(label.childAt(0).text(), 'test')
        })
        it('renders IntegrationKeyHandlerTable correctly', () => {
            const onDelete = () => {}
            const el = shallow(<IntegrationKeyHandlerSegment
                tableFields={[1,2,3]}
                inputFields=""
                integrationKeys={[3,2,1]}
                service=""
                name="test"
                getIntegrationKeysByService={() => {}}
                createIntegrationKey=""
                deleteIntegrationKey=""
                copy="test"
                onDelete={onDelete}
            />)
            const table = el.childAt(0).childAt(1)
            assert(table.is('IntegrationKeyHandlerTable'))
            assert.deepStrictEqual(table.props(), {
                tableFields: [1,2,3],
                integrationKeys: [3,2,1],
                copy: 'test',
                onDelete
            })
        })
        it('renders IntegrationKeyHandlerInput correctly', () => {
            const onNew = () => {}
            const el = shallow(<IntegrationKeyHandlerSegment
                tableFields={[]}
                inputFields={[1,2,3]}
                integrationKeys={[3,2,1]}
                service=""
                name="test"
                getIntegrationKeysByService={() => {}}
                createIntegrationKey=""
                deleteIntegrationKey=""
                onNew={onNew}
            />)
            const input = el.childAt(0).childAt(2)
            assert(input.is('IntegrationKeyHandlerInput'))
            assert.deepStrictEqual(input.props(), {
                inputFields: [1,2,3],
                onNew
            })
        })
    })

})
