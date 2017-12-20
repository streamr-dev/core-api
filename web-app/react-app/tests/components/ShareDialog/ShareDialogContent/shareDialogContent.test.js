
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
            const content = shallow(
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
            content.instance().componentWillMount()
            assert(getResourcePermissions.calledOnce)
        })
    })
    
    describe('render', () => {
        let content
        beforeEach(() => {
            content = shallow(
                <ShareDialogContent
                    resourceType="testType"
                    resourceId="testId"
                    getResourcePermissions={() => {}}
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
            assert.deepStrictEqual(ownerRow.props(), {
                resourceType: 'testType',
                resourceId: 'testId'
            })
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('should return right kind of object with right kind of attrs', () => {
            assert.equal(typeof mapDispatchToProps(), 'object')
            assert.equal(typeof mapDispatchToProps().getResourcePermissions, 'function')
        })
        
        describe('getResourcePermissions', () => {
            it('should dispatch getResourcePermission with right attrs', () => {
                const dispatchSpy = sinon.spy()
                const getStub = sinon.stub(permissionActions, 'getResourcePermissions').callsFake((type, id,) => {
                    return `${type}-${id}`
                })
                mapDispatchToProps(dispatchSpy, {
                    resourceType: 'myType',
                    resourceId: 'myId'
                })
                assert(dispatchSpy.calledOnce)
                assert(getStub.calledOnce)
                assert(dispatchSpy.calledWith('myType-myId'))
            })
        })
    })
})
