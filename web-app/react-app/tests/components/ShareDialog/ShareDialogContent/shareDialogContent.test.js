
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import * as permissionActions from '../../../../actions/permission.js'

import {ShareDialogContent, mapDispatchToProps} from '../../../../components/ShareDialog/ShareDialogContent'

describe('ShareDialogContent', () => {
    describe('componentWillMount', () => {
        it('calls props.getResourcePermissions', () => {
            const getResourcePermissions = sinon.spy()
            shallow(
                <ShareDialogContent
                    permissions={[]}
                    resourceType=""
                    resourceId=""
                    anonymousPermission={{}}
                    owner=""
                    getResourcePermissions={getResourcePermissions}
                    addPermission={() => {}}
                    removePermission={() => {}}
                />
            )
            assert(getResourcePermissions.calledOnce)
        })
    })
    
    describe('render', () => {
        let content
        let onClose
        beforeEach(() => {
            onClose = () => {}
            content = shallow(
                <ShareDialogContent
                    resourceType="testType"
                    resourceId="testId"
                    getResourcePermissions={() => {}}
                    onClose={onClose}
                />
            )
        })
        it('should contain ShareDialogOwnerRow', () => {
            const ownerRow = content.find('Connect(ShareDialogOwnerRow)')
            assert.deepStrictEqual(ownerRow.props(), {
                resourceType: 'testType',
                resourceId: 'testId'
            })
        })
        it('should contain ShareDialogPermissionRow', () => {
            const ownerRow = content.find('Connect(ShareDialogPermissionRow)')
            assert.deepStrictEqual(ownerRow.props(), {
                resourceType: 'testType',
                resourceId: 'testId'
            })
        })
        it('should contain ShareDialogInputRow', () => {
            const ownerRow = content.find('Connect(ShareDialogInputRow)')
            assert.equal(ownerRow.props().resourceType, 'testType')
            assert.equal(ownerRow.props().resourceId, 'testId')
            assert.equal(ownerRow.props().onClose, onClose)
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('should return right kind of object with right kind of attrs', () => {
            assert.equal(typeof mapDispatchToProps(), 'object')
            assert.equal(typeof mapDispatchToProps().getResourcePermissions, 'function')
        })
        
        describe('getResourcePermissions', () => {
            it('should dispatch getResourcePermission with right attrs when called getResourcePermissions', () => {
                const dispatchSpy = sinon.spy()
                const getStub = sinon.stub(permissionActions, 'getResourcePermissions').callsFake((type, id,) => {
                    return `${type}-${id}`
                })
                mapDispatchToProps(dispatchSpy, {
                    resourceType: 'myType',
                    resourceId: 'myId'
                }).getResourcePermissions()
                assert(dispatchSpy.calledOnce)
                assert(getStub.calledOnce)
                assert(dispatchSpy.calledWith('myType-myId'))
            })
        })
    })
})
