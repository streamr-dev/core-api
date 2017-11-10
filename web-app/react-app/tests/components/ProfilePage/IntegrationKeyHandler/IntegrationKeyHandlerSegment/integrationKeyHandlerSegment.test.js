import React from 'react'
import {shallow, mount} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import {IntegrationKeyHandlerSegment} from '../../../../../components/ProfilePage/IntegrationKeyHandler/IntegrationKeyHandlerSegment'

describe('IntegrationKeyHandler', () => {
    describe('componentDidMount', () => {
        it('calls props.getIntegrationKeyByService', () => {
            const spy = sinon.spy()
            mount(
                <IntegrationKeyHandlerSegment
                    tableFields={[]}
                    inputFields={[]}
                    integrationKeys={[]}
                    service="testService"
                    name="testName"
                    className=""
                    getIntegrationKeysByService={spy}
                    createIntegrationKey={() => {
                    }}
                    deleteIntegrationKey={() => {
                    }}
                />
            )
            assert(spy.calledWith('testService'))
        })
    })
    
    describe('onNew', () => {
        it('must call props.createIntegrationKey', () => {
            const spy = sinon.spy()
            const el = mount(
                <IntegrationKeyHandlerSegment
                    tableFields={[]}
                    inputFields={[]}
                    integrationKeys={[]}
                    service="testService"
                    name="testName"
                    className=""
                    getIntegrationKeysByService={() => {
                    }}
                    createIntegrationKey={spy}
                    deleteIntegrationKey={() => {
                    }}
                />
            )
            el.instance().onNew({
                just: 'testing',
                name: 'name'
            })
            assert(spy.calledWith({
                name: 'name',
                service: 'testService',
                json: {
                    just: 'testing'
                }
            }))
        })
    })
    
    describe('onDelete', () => {
        it('must call props.deleteIntegrationKey', () => {
            const spy = sinon.spy()
            const el = mount(
                <IntegrationKeyHandlerSegment
                    tableFields={[]}
                    inputFields={[]}
                    integrationKeys={[]}
                    service="testService"
                    name="testName"
                    className=""
                    getIntegrationKeysByService={() => {
                    }}
                    createIntegrationKey={() => {
                    }}
                    deleteIntegrationKey={spy}
                />
            )
            el.instance().onDelete('testId')
            assert(spy.calledWith('testId'))
        })
    })
    
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
            const el = shallow(<IntegrationKeyHandlerSegment
                tableFields={[1,2,3]}
                inputFields=""
                integrationKeys={[3,2,1]}
                service=""
                name="test"
                getIntegrationKeysByService={() => {}}
                createIntegrationKey=""
                deleteIntegrationKey=""
            />)
            const table = el.childAt(0).childAt(1)
            assert(table.is('IntegrationKeyHandlerTable'))
            assert.deepStrictEqual(table.props(), {
                fields: [1,2,3],
                integrationKeys: [3,2,1],
                onDelete: el.instance().onDelete
            })
        })
        it('renders IntegrationKeyHandlerInput correctly', () => {
            const el = shallow(<IntegrationKeyHandlerSegment
                tableFields={[]}
                inputFields={[1,2,3]}
                integrationKeys={[3,2,1]}
                service=""
                name="test"
                getIntegrationKeysByService={() => {}}
                createIntegrationKey=""
                deleteIntegrationKey=""
            />)
            const input = el.childAt(0).childAt(2)
            assert(input.is('IntegrationKeyHandlerInput'))
            assert.deepStrictEqual(input.props(), {
                fields: [1,2,3],
                onNew: el.instance().onNew
            })
        })
    })
})
