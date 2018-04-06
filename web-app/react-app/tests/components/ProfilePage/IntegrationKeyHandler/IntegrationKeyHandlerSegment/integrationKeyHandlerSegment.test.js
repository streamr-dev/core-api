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
            const label = el.find('ControlLabel')
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
            const table = el.find('IntegrationKeyHandlerTable')
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
            const input = el.find('IntegrationKeyHandlerInput')
            assert.deepStrictEqual(input.props(), {
                inputFields: [1,2,3],
                onNew
            })
        })
        it('does not render IntegrationKeyHandlerInput if props.showInput === false', () => {
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
                showInput={false}
            />)
            assert.equal(el.find('IntegrationKeyHandlerInput').length, 0)
        })
    })

})
