
import React from 'react'
import {shallow, mount} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import {ShareDialogContent} from '../../../../components/ShareDialog/ShareDialogContent'

describe('ShareDialogContent', () => {
    describe('componentWillMount', () => {
        it('calls props.getResoucePermissions', () => {
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
            assert(getResourcePermissions.called)
        })
    })
    
    describe('render', () => {
        let content
        beforeEach(() => {
            content = shallow(
                <ShareDialogContent resourceType="testType" resourceId="testId" getResourcePermissions={() => {}} />
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
})
