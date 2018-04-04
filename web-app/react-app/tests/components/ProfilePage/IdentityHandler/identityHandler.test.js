import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'
import * as actions from '../../../../actions/integrationKey'
import {IdentityHandler, mapStateToProps, mapDispatchToProps} from '../../../../components/ProfilePage/IdentityHandler'

describe('IdentityHandler', () => {
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
                <IdentityHandler
                    getIntegrationKeysByService={spy}
                />
            )
            assert(spy.calledOnce)
            assert(spy.calledWith('ETHEREUM_ID'))
        })
    })
    describe('onNew', () => {
        it('must call props.createIdentity', () => {
            const spy = sinon.spy()
            const el = shallow(
                <IdentityHandler
                    createIdentity={spy}
                    getIntegrationKeysByService={() => {}}
                />
            )
            el.instance().onNew({
                just: 'testing',
                name: 'name'
            })
            assert(spy.calledWith({
                name: 'name',
                service: 'ETHEREUM_ID',
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
                <IdentityHandler
                    deleteIntegrationKey={spy}
                    getIntegrationKeysByService={() => {}}
                />
            )
            el.instance().onDelete('testId')
            assert(spy.calledOnce)
            assert(spy.calledWith('testId'))
        })
    })
    describe('render', () => {
        it('should render correctly', () => {
            const handler = shallow(
                <IdentityHandler
                    deleteIntegrationKey={() => {}}
                    getIntegrationKeysByService={() => {}}
                />
            )
            const handlerSegment = handler.find('IntegrationKeyHandlerSegment')
            assert(handlerSegment.exists())
            assert.equal(handlerSegment.props().service, 'ETHEREUM_ID')
            //assert.equal(JSON.stringify(handlerSegment.props().inputFields), JSON.stringify(['address']))
            assert.equal(handlerSegment.props().onNew, handler.instance().onNew)
            assert.equal(handlerSegment.props().onDelete, handler.instance().onDelete)
            assert.equal(handlerSegment.props().tableFields[0][0], 'address')
            assert.equal(handlerSegment.props().tableFields[0][1]('12345123451234512345'), '123451234512345...')
        })
    })
    describe('mapStateToProps', () => {
        it('must return right kind of object', () => {
            assert.deepStrictEqual(mapStateToProps({
                integrationKey: {
                    listsByService: {
                        ETHEREUM_ID: [1, 2, 3]
                    },
                    error: 'testError'
                }
            }), {
                integrationKeys: [1, 2, 3],
                error: 'testError'
            })
        })
        it('must use empty array as integrationKeys in found none', () => {
            assert.deepStrictEqual(mapStateToProps({
                integrationKey: {
                    listsByService: {},
                    error: 'testError'
                }
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
            assert.equal(typeof mapDispatchToProps().createIdentity, 'function')
            assert.equal(typeof mapDispatchToProps().getIntegrationKeysByService, 'function')
        })
        describe('deleteIntegrationKey', () => {
            it('must dispatch deleteIntegrationKey', () => {
                const dispatchSpy = sinon.spy()
                const deleteStub = sandbox.stub(actions, 'deleteIntegrationKey')
                    .callsFake((id) => id)
                mapDispatchToProps(dispatchSpy).deleteIntegrationKey('test')
                assert(dispatchSpy.calledOnce)
                assert(deleteStub.calledOnce)
                assert(dispatchSpy.calledWith('test'))
            })
        })
        describe('createIntegrationKey', () => {
            it('must dispatch createIdentity', () => {
                const dispatchSpy = sinon.spy()
                const deleteStub = sandbox.stub(actions, 'createIdentity')
                    .callsFake((key) => key)
                mapDispatchToProps(dispatchSpy).createIdentity('test')
                assert(dispatchSpy.calledOnce)
                assert(deleteStub.calledOnce)
                assert(dispatchSpy.calledWith('test'))
            })
        })
        describe('getIntegrationKeysByService', () => {
            it('must dispatch getIntegrationKeysByService', () => {
                const dispatchSpy = sinon.spy()
                const deleteStub = sandbox.stub(actions, 'getIntegrationKeysByService')
                    .callsFake((service) => service)
                mapDispatchToProps(dispatchSpy).getIntegrationKeysByService('test')
                assert(dispatchSpy.calledOnce)
                assert(deleteStub.calledOnce)
                assert(dispatchSpy.calledWith('test'))
            })
        })
    })
})
