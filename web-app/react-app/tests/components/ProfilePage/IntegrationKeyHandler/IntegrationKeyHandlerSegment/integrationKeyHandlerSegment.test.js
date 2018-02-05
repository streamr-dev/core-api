import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import * as integrationKeyActions from '../../../../../actions/integrationKey.js'

import {IntegrationKeyHandlerSegment, mapStateToProps, mapDispatchToProps} from '../../../../../components/ProfilePage/IntegrationKeyHandler/IntegrationKeyHandlerSegment'

describe('IntegrationKeyHandler', () => {
    let sandbox
    
    beforeEach(() => {
        sandbox = sinon.sandbox.create()
    })
    
    afterEach(() => {
        sandbox.restore()
    })
    
    describe('componentDidMount', () => {
        it('calls props.getIntegrationKeyByService', () => {
            const spy = sinon.spy()
            shallow(
                <IntegrationKeyHandlerSegment
                    tableFields={[]}
                    inputFields={[]}
                    integrationKeys={[]}
                    service="testService"
                    name="testName"
                    className=""
                    getIntegrationKeysByService={spy}
                    createIntegrationKey={() => {}}
                    deleteIntegrationKey={() => {}}
                />
            )
            assert(spy.calledOnce)
            assert(spy.calledWith('testService'))
        })
    })
    
    describe('onNew', () => {
        it('must call props.createIntegrationKey', () => {
            const spy = sinon.spy()
            const el = shallow(
                <IntegrationKeyHandlerSegment
                    tableFields={[]}
                    inputFields={[]}
                    integrationKeys={[]}
                    service="testService"
                    name="testName"
                    className=""
                    getIntegrationKeysByService={() => {}}
                    createIntegrationKey={spy}
                    deleteIntegrationKey={() => {}}
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
            const el = shallow(
                <IntegrationKeyHandlerSegment
                    tableFields={[]}
                    inputFields={[]}
                    integrationKeys={[]}
                    service="testService"
                    name="testName"
                    className=""
                    getIntegrationKeysByService={() => {}}
                    createIntegrationKey={() => {}}
                    deleteIntegrationKey={spy}
                />
            )
            el.instance().onDelete('testId')
            assert(spy.calledOnce)
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
    
    describe('mapStateToProps', () => {
        it('must return right kind of object', () => {
            assert.deepStrictEqual(mapStateToProps({
                integrationKey: {
                    listsByService: {
                        service: [1, 2, 3]
                    },
                    error: 'testError'
                }
            }, {
                service: 'service'
            }), {
                integrationKeys: [1, 2, 3],
                error: 'testError'
            })
        })
        it('must use empty array as integrationKeys in found none', () => {
            assert.deepStrictEqual(mapStateToProps({
                integrationKey: {
                    listsByService: {
                        service: [1, 2, 3]
                    },
                    error: 'testError'
                }
            }, {
                service: 'wrongService'
            }), {
                integrationKeys: [],
                error: 'testError'
            })
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('must return right kind of object with right type of attrs', () => {
            assert.equal(typeof mapDispatchToProps(), 'object')
            assert.equal(typeof mapDispatchToProps().deleteIntegrationKey, 'function')
            assert.equal(typeof mapDispatchToProps().createIntegrationKey, 'function')
            assert.equal(typeof mapDispatchToProps().getIntegrationKeysByService, 'function')
        })
        
        describe('deleteIntegrationKey', () => {
            it('must dispatch deleteIntegrationKey', () => {
                const dispatchSpy = sinon.spy()
                const deleteStub = sandbox.stub(integrationKeyActions, 'deleteIntegrationKey')
                    .callsFake((id) => id)
                mapDispatchToProps(dispatchSpy).deleteIntegrationKey('test')
                assert(dispatchSpy.calledOnce)
                assert(deleteStub.calledOnce)
                assert(dispatchSpy.calledWith('test'))
            })
        })
        
        describe('createIntegrationKey', () => {
            it('must dispatch createIntegrationKey', () => {
                const dispatchSpy = sinon.spy()
                const deleteStub = sandbox.stub(integrationKeyActions, 'createIntegrationKey')
                    .callsFake((key) => key)
                mapDispatchToProps(dispatchSpy).createIntegrationKey('test')
                assert(dispatchSpy.calledOnce)
                assert(deleteStub.calledOnce)
                assert(dispatchSpy.calledWith('test'))
            })
        })
        
        describe('getIntegrationKeysByService', () => {
            it('must dispatch getIntegrationKeysByService', () => {
                const dispatchSpy = sinon.spy()
                const deleteStub = sandbox.stub(integrationKeyActions, 'getIntegrationKeysByService')
                    .callsFake((service) => service)
                mapDispatchToProps(dispatchSpy).getIntegrationKeysByService('test')
                assert(dispatchSpy.calledOnce)
                assert(deleteStub.calledOnce)
                assert(dispatchSpy.calledWith('test'))
            })
        })
    })
    
})
